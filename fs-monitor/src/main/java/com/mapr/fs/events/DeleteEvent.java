package com.mapr.fs.events;

import com.mapr.fs.messages.Delete;

import java.io.IOException;

public class DeleteEvent implements Event {
    private Delete message;

    public DeleteEvent(Delete message) {
        this.message = message;
    }

    @Override
    public void execute(String volumePath) throws IOException {

    }
}
