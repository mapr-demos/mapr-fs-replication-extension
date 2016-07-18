package com.mapr.fs.events;

import com.mapr.fs.ConsumerDAO;
import com.mapr.fs.messages.Delete;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class DeleteEvent implements Event {
    private static final Logger log = Logger.getLogger(DeleteEvent.class);
    private Delete message;
    private ConsumerDAO dao;

    public DeleteEvent(Delete message) {
        this.message = message;
        this.dao = new ConsumerDAO();
    }

    @Override
    public void execute(String volumePath) throws IOException {
        String filePath = volumePath + "/" + message.name;

        log.info("Executing delete event: " + filePath);
        File file = new File(filePath);

        if (!file.delete()) {
            log.error("File/directory can not be deleted: " + filePath);
            return;
        }
        dao.remove(Paths.get(filePath));
        log.info(dao.get(Paths.get(filePath)));
        log.info("File/directory deleted: " + filePath);
    }
}
