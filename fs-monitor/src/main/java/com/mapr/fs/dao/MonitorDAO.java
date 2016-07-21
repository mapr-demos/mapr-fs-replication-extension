package com.mapr.fs.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.db.MapRDB;
import com.mapr.db.Table;
import com.mapr.fs.FileState;
import org.apache.log4j.Logger;
import org.ojai.Document;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MonitorDAO {

    private static final Logger log = Logger.getLogger(MonitorDAO.class);

    public Table getFileStateTable() {
        return fileStateTable;
    }

    private Table fileStateTable;

    private static final String APPS_DIR = "/apps/fs/db/monitor/";
    private static final String FILE_STATE_TABLE = APPS_DIR + "state";

    public MonitorDAO() {
        this.fileStateTable = this.getTable(FILE_STATE_TABLE);
    }

    private static final Object lock = new Object();
    private Table getTable(String tableName) {
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

    public void put(String json) {
            Document document = MapRDB.newDocument(json);
            fileStateTable.insertOrReplace(document);
            fileStateTable.flush();
    }

    public FileState get(Path path) throws IOException {
        Document document = fileStateTable.findById(path.toString());
        if (document != null) {
            return getFileState(document);
        }
        return null;
    }

    public FileState remove(Path path) throws IOException {
        FileState fileState = get(path);
        fileStateTable.delete(path.toString());
        return fileState;
    }

    private FileState getFileState(Document document) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        Path path = Paths.get(document.getString("path"));
        long size = (long) document.getDouble("size");
        List<Long> hashes = mapper.readValue(document.getString("hashes"), mapper.getTypeFactory().constructCollectionType(List.class, Long.class));
        Object inode = document.getValue("inode");

        return new FileState(path, size, hashes, inode);
    }
}
