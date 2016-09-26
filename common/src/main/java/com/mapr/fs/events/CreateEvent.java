package com.mapr.fs.events;

import com.mapr.fs.dao.ConsumerDAO;
import com.mapr.fs.messages.Create;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class CreateEvent implements Event {

    private Create message;
    private ConsumerDAO dao;

    public CreateEvent(Create message) throws IOException {
        this.message = message;
        this.dao = new ConsumerDAO();
    }

    @Override
    public void execute(String volumePath) throws IOException {
        String filePath = volumePath + "/" + message.getName();
        Path path = Paths.get(filePath);

        log.info("Executing create event: " + filePath);
        File file = new File(filePath);
        if (!message.isDirectory()) {
            if (!file.createNewFile()) {
                log.error("File can not be created: " + filePath);
                return;
            }
            log.info("File created: " + filePath);
        } else {
            if (!file.mkdir()) {
                log.error("Directory can not be created: " + filePath);
                return;
            }
            log.info("Directory created: " + filePath);
        }
        dao.put(path);
        log.info(dao.get(path).toString());
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
