package com.mapr.fs.events;

import com.mapr.fs.messages.RenameTo;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class RenameToEvent implements Event {

    private static final Logger log = Logger.getLogger(RenameToEvent.class);
    private RenameTo message;

    public RenameToEvent(RenameTo message) {
        this.message = message;
    }

    @Override
    public void execute(String volumePath) throws IOException {

        String oldFilePath = volumePath + "/" + message.oldName;
        String newFilePath = volumePath + "/" + message.newName;

        log.info("Executing rename event: " + oldFilePath + " -> " + newFilePath);

        File oldFile =new File(oldFilePath);
        File newFile =new File(newFilePath);

        if(oldFile.renameTo(newFile)){
            log.info("Rename successful: " + oldFilePath + " -> " + newFilePath);
        }else{
            log.error("Rename failed: " + oldFilePath + " -> " + newFilePath);
        }
    }
}
