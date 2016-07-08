package com.mapr.fs.events;

import com.mapr.fs.messages.Delete;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class DeleteEvent implements Event {
    private static final Logger log = Logger.getLogger(DeleteEvent.class);
    private Delete message;

    public DeleteEvent(Delete message) {
        this.message = message;
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
        log.info("File/directory deleted: " + filePath);
    }
}
