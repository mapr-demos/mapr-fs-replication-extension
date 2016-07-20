package com.mapr.fs;

import com.mapr.db.MapRDB;
import com.mapr.db.Table;
import org.apache.log4j.Logger;
import org.ojai.Document;

abstract class DAO {
    private static final Logger log = Logger.getLogger(DAO.class);

    private static final Object lock = new Object();
    protected Table getTable(String tableName) {
        Table table;
        log.info("Check DB");
        synchronized (lock) {
            if (!MapRDB.tableExists(tableName)) {
                table = MapRDB.createTable(tableName);
            } else {
                table = MapRDB.getTable(tableName);
            }
        }
        return table;
    }

    public abstract void put(String json);
    public abstract Document get();
}
