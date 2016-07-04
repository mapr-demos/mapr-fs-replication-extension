package com.mapr.fs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.permission.FsAction;

/**
 * Created by tdunning on 7/3/16.
 */
public class TestFidInfo {
    @org.junit.Test
    public void testFid() throws Exception {
        MapRFileSystem fs = (MapRFileSystem) FileSystem.get(new Configuration());
//        org.apache.hadoop.fs.PathId.getFidInfo


    }
}
