package com.mapr.fs;

import com.mapr.fs.dao.TestDAO;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class ConfigTest {

  @Before
  public void setUp() throws Exception {
    // TODO : make it dynamic for test
    // copy configuration file for test\
    // this should be fix after
    // https://github.com/mapr-demos/mapr-fs-replication-extension/issues/15

    System.out.println("Copy test config file to tmp");
    URL inputUrl = getClass().getResource("/config.conf");
    File dest = new File("/tmp/config.conf");
    FileUtils.copyURLToFile(inputUrl, dest);


  }

  @Test
  public void testGetTablePath() throws Exception {
    String fullPathForTest = "/tmp/TEST/apps/fs/db/test/table";
    TestDAO dao = new TestDAO();
    assertEquals( fullPathForTest, dao.getFullPath() );
  }




}
