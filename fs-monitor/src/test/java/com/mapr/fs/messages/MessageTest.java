package com.mapr.fs.messages;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MessageTest extends TestCase {
    public void testCreate() throws IOException {
        validate(new Create(new File("/usr/foo").toPath(), true),
                "\"type\":\"create\",\"name\":\"/usr/foo\",\"directory\":true}");
    }

    public void testDelete() throws IOException {
        validate(new Delete(new File("/usr/foo").toPath()),
                "\"type\":\"delete\",\"name\":\"/usr/foo\"}");
    }

    public void testModify() throws IOException {
        List<Long> offsets = Lists.<Long>newArrayList(0L, 8192L, 16384L);
        List<String> content = Lists.<String>newArrayList("content1", "content2", "content3");

        validate(new Modify(new File("/usr/foo").toPath(), 103L, offsets, content),
                "\"type\":\"modify\",\"name\":\"/usr/foo\",\"fileSize\":103,\"changedBlocks\":[0,8192,16384],\"changedBlocksContent\":[\"content1\",\"content2\",\"content3\"]}");
    }

    public void testRenameFrom() throws IOException {
        validate(new RenameFrom(new File("/usr/from").toPath(), new File("/usr/to").toPath()),
                "\"type\":\"rename_from\",\"oldName\":\"/usr/from\",\"newName\":\"/usr/to\"}");
    }

    public void testRenameTo() throws IOException {
        validate(new RenameTo(new File("/usr/from").toPath(), new File("/usr/to").toPath()),
                "\"type\":\"rename_to\",\"oldName\":\"/usr/from\",\"newName\":\"/usr/to\"}");
    }

    private void validate(Message op, final String ref) throws IOException {
        String json = Message.toJson(op);
        assertEquals("{" + ref, json);
        assertEquals(op, Message.fromJson(json));
    }
}