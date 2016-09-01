package com.mapr.fs;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.commons.codec.binary.Base64;
import org.apache.htrace.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Records the state of a file, including name, FID, permissions and content.
 */
public class FileState {
    private final Logger log = Logger.getLogger(this.getClass());
    public static final int BLOCK_SIZE = 8192;
    private long size;
    private List<Long> hashes = new ArrayList<>();
    private Object inode;
    private Path path;

    public FileState(Path path) {
        this.path = path;
    }

    public FileState(Path path, long size, List<Long> hashes, Object inode) {
        this.path = path;
        this.size = size;
        this.hashes = hashes;
        this.inode = inode;
    }

    public static FileState getFileInfo(Path path) throws IOException {
        if (path.toFile().exists()) {
            try {
                FileState r = new FileState(path);
                r.inode = fileKey(path);

                HashFunction hf = Hashing.murmur3_128();
                byte[] buffer = new byte[BLOCK_SIZE];
                InputStream input = new BufferedInputStream(new FileInputStream(path.toFile()), 1024 * 1024);
                r.size = path.toFile().length();
                int n = input.read(buffer);
                while (n > 0) {
                    r.hashes.add(hf.newHasher()
                            .putBytes(buffer, 0, n)
                            .hash().asLong());
                    n = input.read(buffer);
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
     *
     * @param other The FileState corresponding to the other file
     * @return A list of offsets for the 8k blocks that need to be copied to make the files identical.
     */
    public List<Long> changedBlockOffsets(FileState other) {
        long offset = 0;
        List<Long> r = new ArrayList<>();
        Iterator<Long> i = this.hashes.iterator();

        if (other != null) {
            Iterator<Long> j = other.hashes.iterator();

            if (other.hashes.size() > this.hashes.size()) {
                while (i.hasNext() && j.hasNext()) {
                    if (i.next().equals(j.next())) {
                        r.add(offset);
                    }
                    offset += BLOCK_SIZE;
                }
            } else {

                while (i.hasNext() && j.hasNext()) {
                    if (!i.next().equals(j.next())) {
                        r.add(offset);
                    }
                    offset += BLOCK_SIZE;
                }
            }
        }

        while (i.hasNext()) {
            r.add(offset);
            offset += BLOCK_SIZE;
            i.next();
        }
        return r;
    }

    public String toJSON() {
        return new ObjectMapper().createObjectNode()
                .put("_id", path.toString())
                .put("path", path.toString())
                .put("size", size)
                .put("inode", inode.toString())
                .put("hashes", hashes.toString()).toString();
    }

    public List<String> changedBlockContentEncoded(List<Long> offsets) throws IOException {
        List<String> r = new ArrayList<>();
        RandomAccessFile input = new RandomAccessFile(this.path.toFile(), "r");
        int chunckCounter = 0;
        for (long offset : offsets) {
            byte[] buffer = new byte[BLOCK_SIZE];
            input.seek(offset);
            int size = input.read(buffer);
            if (size == -1) {
                throw new EOFException(String.format("Unexpected end of file at offset %d", offset));
            }
            if (size < buffer.length) {
                buffer = Arrays.copyOf(buffer, size);
            }
            String encodedBlock = new String(Base64.encodeBase64(buffer));
            log.debug("Sending chunk # " + chunckCounter++);
            r.add(encodedBlock);
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

    public Long getFileSize() {
        return size;
    }

    @Override
    public String toString() {
        return "FileState{" +
                "size=" + size +
                ", hashes=" + hashes +
                ", inode=" + inode +
                ", path=" + path +
                '}';
    }
}
