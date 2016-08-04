package com.mapr.fs.application;

import com.google.common.io.Files;
import junit.framework.TestCase;

import java.io.File;

/**
 *
 * Test case for the Consumer Application
 *
 */
public class ConsumerTest extends TestCase {

  public void testConfiguration() throws Exception {
    
    File tempDir = Files.createTempDir();
    try {
      String replicationFolder = "/tmp/mapr/my.dummy/cluster/replication";
      String topicName = "my_volume";

      // check path to replication folder
      replicationFolder =  tempDir.getAbsolutePath() + replicationFolder;
      String topicReplicationFolder = Consumer.checkDir(  replicationFolder, topicName );
      assertEquals(topicReplicationFolder, tempDir.getAbsolutePath() + "/tmp/mapr/my.dummy/cluster/replication/my_volume");

      // check if file is properly created
      File fileToCheck =  new File(topicReplicationFolder);
      assertEquals( fileToCheck.exists(), true );

    }
    finally {
      tempDir.delete();
    }
  }
}
