package com.mapr.fs;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Records the state of a file, including name, FID, permissions and content.
 */
public class FileState {
    private List<Long> hashes = new ArrayList<>();
    private Object inode;

    public static FileState getFileInfo(Path path) throws IOException {
        if (path.toFile().exists()) {
            try {
                FileState r = new FileState();
                r.inode = fileKey(path);

                HashFunction hf = Hashing.murmur3_128();
                byte[] buffer = new byte[8192];
                InputStream input = new BufferedInputStream(new FileInputStream(path.toFile()), 1024 * 1024);
                int n = input.read(buffer);
                while (n > 0) {
                    r.hashes.add(hf.newHasher()
                            .putBytes(buffer)
                            .hash().asLong());
                }
                return r;
            } catch (NoSuchFileException e) {
                // file could disappear out from under us
                return null;
            }
        } else {
            return null;
        }
    }

    public static Object fileKey(Path path) throws IOException {
        if (path.toFile().exists()) {
            return Files.readAttributes(path, BasicFileAttributes.class).fileKey();
        } else {
            return null;
        }
    }


    /**
     * Find the 8k blocks of other that need to change to make the file contents the same. Assumes that
     * other is no longer than this.
     * @param other    The FileState corresponding to the other file
     * @return         A list of offsets for the 8k blocks that need to be copied to make the files identical.
     */
    public List<Long> changedBlockOffsets(FileState other) {
        long offset = 0;
        List<Long> r = new ArrayList<>();
        Iterator<Long> i = this.hashes.iterator();
        Iterator<Long> j = other.hashes.iterator();
        while (i.hasNext() && j.hasNext()) {
            if (!i.next().equals(j.next())) {
                r.add(offset);
            }
            offset += 8192;
        }

        while (i.hasNext()) {
            r.add(offset);
            offset += 8192;
        }
        return r;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FileState) {
            FileState other = (FileState) obj;
            return this.inode == other.inode && this.hashes.equals(other.hashes);
        } else {
            return false;
        }
    }

    public Object getInode() {
        return inode;
    }
}
