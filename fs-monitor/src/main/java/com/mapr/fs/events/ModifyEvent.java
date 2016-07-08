package com.mapr.fs.events;

import com.mapr.fs.messages.Modify;

import java.io.IOException;

public class ModifyEvent implements Event {
    private Modify message;

    public ModifyEvent(Modify message) {
        this.message = message;
    }

    @Override
    public void execute(String volumePath) throws IOException {

    }
}
