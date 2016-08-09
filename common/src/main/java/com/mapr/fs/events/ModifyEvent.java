package com.mapr.fs.events;

import com.mapr.fs.dao.ConsumerDAO;
import com.mapr.fs.messages.Modify;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ModifyEvent implements Event {
    private static final Logger log = Logger.getLogger(ModifyEvent.class);
    private Modify message;
    private ConsumerDAO dao;

    public ModifyEvent(Modify message) throws IOException {
        this.message = message;
        this.dao = new ConsumerDAO();
    }

    @Override
    public void execute(String volumePath) throws IOException {
        String filePath = volumePath + "/" + message.getName();
        Path path = Paths.get(filePath);

        log.info("Executing modify event: " + filePath);
        truncate(filePath);

        RandomAccessFile file = new RandomAccessFile(new File(filePath), "rw");
        for (int i = 0; i < message.getChangedBlocks().size(); i++) {
            long offset = message.getChangedBlocks().get(i);
            String data = message.getChangedBlocksContent().get(i);

            byte[] decoded = Base64.decodeBase64(data);
            file.seek(offset);
            file.write(decoded);
        }
        file.close();

        dao.put(path);
        log.info(dao.get(path));
    }

    private void truncate(String filePath) throws IOException {
        FileChannel chan = new FileOutputStream(new File(filePath), true).getChannel();
        chan.truncate(message.getFileSize());
        chan.close();
    }

    @Override
    public String getFileName() {
        return message.getName();
    }

    @Override
    public String getFileStatus() {
        return message.getClass().getSimpleName();
    }
}
