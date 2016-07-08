package com.mapr.fs.events;

import com.mapr.fs.messages.Modify;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class ModifyEvent implements Event {
    private static final Logger log = Logger.getLogger(ModifyEvent.class);
    private Modify message;

    public ModifyEvent(Modify message) {
        this.message = message;
    }

    @Override
    public void execute(String volumePath) throws IOException {
        String filePath = volumePath + "/" + message.name;

        log.info("Executing modify event: " + filePath);
        truncate(filePath);

        RandomAccessFile file = new RandomAccessFile(new File(filePath), "rw");
        for (int i = 0; i < message.changedBlocks.size(); i++) {
            long offset = message.changedBlocks.get(i);
            String data = message.changedBlocksContent.get(i);

            byte[] decoded = Base64.decodeBase64(data);
            file.seek(offset);
            file.write(decoded);
        }
        file.close();
    }

    private void truncate(String filePath) throws IOException {
        FileChannel chan = new FileOutputStream(new File(filePath), true).getChannel();
        chan.truncate(message.fileSize);
        chan.close();
    }
}
