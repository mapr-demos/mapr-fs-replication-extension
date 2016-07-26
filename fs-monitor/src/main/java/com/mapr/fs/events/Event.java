package com.mapr.fs.events;

import java.io.IOException;

public interface Event {
    void execute(String volumePath) throws IOException;
}
