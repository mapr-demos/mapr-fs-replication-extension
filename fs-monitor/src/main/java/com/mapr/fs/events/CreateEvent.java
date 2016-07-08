package com.mapr.fs.events;

import com.mapr.fs.messages.Create;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class CreateEvent implements Event {
    private static final Logger log = Logger.getLogger(CreateEvent.class);
    private Create message;

    public CreateEvent(Create message) {
        this.message = message;
    }

    @Override
    public void execute(String volumePath) throws IOException {
        String filePath = volumePath + "/" + message.name;

        log.info("Executing create event: " + filePath);
        File file = new File(filePath);
        if (!message.directory) {
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
    }
}
