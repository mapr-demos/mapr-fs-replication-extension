package com.mapr.fs.events;

import com.mapr.fs.dao.ConsumerDAO;
import com.mapr.fs.messages.RenameTo;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@Slf4j
public class RenameToEvent implements Event {

    private RenameTo message;
    private ConsumerDAO dao;

    public RenameToEvent(RenameTo message) throws IOException {
        this.message = message;
        this.dao = new ConsumerDAO();
    }

    @Override
    public void execute(String volumePath) throws IOException {

        String oldFilePath = volumePath + "/" + message.getOldName();
        String newFilePath = volumePath + "/" + message.getNewName();

        log.info("Executing rename event: " + oldFilePath + " -> " + newFilePath);

        File oldFile =new File(oldFilePath);
        File newFile =new File(newFilePath);

        if(oldFile.renameTo(newFile)){
            log.info("Rename successful: " + oldFilePath + " -> " + newFilePath);

            dao.put(Paths.get(newFilePath));
            log.info(dao.get(Paths.get(newFilePath)).toString());
        }else{
            log.error("Rename failed: " + oldFilePath + " -> " + newFilePath);
        }
    }

    @Override
    public String getFileName() {
        return message.getNewName();
    }

    @Override
    public String getFileStatus() {
        return message.getClass().getSimpleName();
    }
}
