package com.mapr.fs.events;

import java.io.IOException;

/**
 * Marker class to allow us to see which events have side-effects during playback.
 */
public abstract class RealEvent extends SimEvent {
    public abstract void doit() throws IOException;
}
