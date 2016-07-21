package com.mapr.fs.events;

import com.mapr.fs.dao.ConsumerDAO;
import com.mapr.fs.messages.RenameTo;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class RenameToEvent implements Event {

    private static final Logger log = Logger.getLogger(RenameToEvent.class);
    private RenameTo message;
    private ConsumerDAO dao;

    public RenameToEvent(RenameTo message) {
        this.message = message;
        this.dao = new ConsumerDAO();
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

            dao.put(Paths.get(newFilePath));
            log.info(dao.get(Paths.get(newFilePath)));
        }else{
            log.error("Rename failed: " + oldFilePath + " -> " + newFilePath);
        }
    }
}
