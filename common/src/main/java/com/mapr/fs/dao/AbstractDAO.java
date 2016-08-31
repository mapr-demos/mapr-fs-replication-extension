package com.mapr.fs.dao;


import com.mapr.db.MapRDB;
import com.mapr.db.Table;
import com.mapr.fs.Config;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;

/**
 * This class is used as a base for all DAO that needs access to MapR-DB
 */
public class AbstractDAO {

    protected final Logger log = Logger.getLogger(getClass());

    private String fullTableName; // full path to the table
    private String type; // type of table that define the parent folder

    /**
     * Get MapR-DB table from path, create the table if does not exist
     *
     * @param tableName
     * @return table
     * @throws IOException
     */
    protected Table getTable(String tableName) throws IOException {
        Table table;
        log.info("Getting table " + tableName);
        if (!MapRDB.tableExists(tableName)) {
            table = createPathAndTable(tableName);
        } else {
            table = MapRDB.getTable(tableName);
        }
        return table;
    }

    /**
     * Create Path and Table
     *
     * @param tableName full path
     * @return the Table
     * @throws IOException
     */
    protected Table createPathAndTable(String tableName) throws IOException {
        // create folder if needed
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create("maprfs:///"), conf);
        Path path = new Path(tableName); // full path with table name
        Path parent = path.getParent(); //parent folder
        // create folders
        boolean created = fs.mkdirs(parent);
        if (created) {
            log.info("Parent folder created " + parent);
        } else {
            log.info("Parent folder already exists " + parent);
        }

        // create table;
        log.info("Creating table :  " + tableName);
        return MapRDB.createTable(tableName);
    }

    /**
     * Get table name with full path: build from configuration, type and name.
     * Configuration is coming from the configuration file
     *
     * @param type : type of table monitor, consumer, cluster, ...
     * @param name : name of the table
     * @return full path to the table, used by the API
     * @throws IOException
     */
    protected String getFullTableName(String type, String name) throws IOException {
        java.nio.file.Path fullTableName = FileSystems.getDefault().getPath(Config.getAppsDir(), type, name);
        return fullTableName.toString();
    }

}
