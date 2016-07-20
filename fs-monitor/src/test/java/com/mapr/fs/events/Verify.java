package com.mapr.fs.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Charsets;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

/**
 * Validate the contents of a file.
 */
public class Verify extends RealEvent {
    @JsonProperty("name")
    public String name;
    @JsonProperty("content")
    public String content = null;
    @JsonProperty("random")
    public int size = 0;

    @JsonProperty("offset")
    public int offset = 0;
    @Override
    public void doit() throws IOException {
        try (FileInputStream in = new FileInputStream(name)) {
            if (content != null) {
                byte[] ref = content.getBytes(Charsets.UTF_8);
                byte[] actual = new byte[ref.length];
                int n = in.read(actual);
                if (n != actual.length) {
                    throw new VerificationException("Only read " + n + " bytes, expected " + actual.length);
                }
                if (!Arrays.equals(ref, actual)) {
                    throw new VerificationException("Actual bytes didn't match expected at offset " + offset);
                }
            }
            if (size > 0) {
                Random gen = new Random(name.hashCode() + size * 7907);
                byte[] ref = new byte[size];
                gen.nextBytes(ref);
                byte[] actual = new byte[ref.length];
                int n = in.read(actual);
                if (n != actual.length) {
                    throw new VerificationException("Only read " + n + " bytes, expected " + actual.length);
                }
                if (!Arrays.equals(ref, actual)) {
                    throw new VerificationException("Actual bytes didn't match expected at offset " + offset);
                }
            }
        }
    }

    private static class VerificationException extends IOException {
        public VerificationException(String message) {
            super(message);
        }
    }
}
