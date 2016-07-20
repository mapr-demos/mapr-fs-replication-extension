package com.mapr.fs.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mapr.fs.FileOperation;

/**
 * Created by tdunning on 7/19/16.
 */
public class FileOperationNotification extends SimEvent {
    @JsonProperty("event")
    public FileOperation action;
}
