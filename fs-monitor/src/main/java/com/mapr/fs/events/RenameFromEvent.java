package com.mapr.fs.events;

import com.mapr.fs.messages.RenameFrom;

import java.io.IOException;

public class RenameFromEvent implements Event {

    private RenameFrom message;

    public RenameFromEvent(RenameFrom message) {
        this.message = message;
    }

    @Override
    public void execute(String volumePath) throws IOException {

    }
}
