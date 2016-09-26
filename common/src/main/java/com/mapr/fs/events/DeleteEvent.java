package com.mapr.fs.events;

import com.mapr.fs.dao.ConsumerDAO;
import com.mapr.fs.messages.Delete;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@Slf4j
public class DeleteEvent implements Event {

    private Delete message;
    private ConsumerDAO dao;

    public DeleteEvent(Delete message) throws IOException {
        this.message = message;
        this.dao = new ConsumerDAO();
    }

    @Override
    public void execute(String volumePath) throws IOException {
        String filePath = volumePath + "/" + message.getName();

        log.info("Executing delete event: " + filePath);
        File file = new File(filePath);

        if (!file.delete()) {
            log.error("File/directory can not be deleted: " + filePath);
            return;
        }
        dao.remove(Paths.get(filePath));
        log.info(dao.get(Paths.get(filePath)).toString());
        log.info("File/directory deleted: " + filePath);
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
