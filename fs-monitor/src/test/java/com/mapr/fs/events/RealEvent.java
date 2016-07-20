package com.mapr.fs.events;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by tdunning on 7/19/16.
 */
public abstract class RealEvent extends SimEvent {
    public abstract void doit() throws IOException;
}
