package com.mapr.fs.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mapr.fs.FileOperation;

/**
 * Records a FileWatcher event
 */
public class FileOperationNotification extends SimEvent {
    @JsonProperty("event")
    public FileOperation action;
}
