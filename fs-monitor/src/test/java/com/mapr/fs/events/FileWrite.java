package com.mapr.fs.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Charsets;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Random;

/**
 * Created by tdunning on 7/19/16.
 */
public class FileWrite extends RealEvent {
    @JsonProperty("name")
    public String name;
    @JsonProperty("content")
    public String content = null;
    @JsonProperty("random")
    public int size = 0;

    @Override
    public void doit() throws IOException {
        try (FileOutputStream out = new FileOutputStream(name, true)) {
            if (content != null) {
                try (BufferedWriter buf = new BufferedWriter(new OutputStreamWriter(out, Charsets.UTF_8))) {
                    buf.write(content);
                    buf.newLine();
                }
            }
            if (size > 0) {
                Random gen = new Random(name.hashCode() + size * 7907);
                byte[] r = new byte[size];
                gen.nextBytes(r);
                out.write(r);
            }
        }
    }
}
