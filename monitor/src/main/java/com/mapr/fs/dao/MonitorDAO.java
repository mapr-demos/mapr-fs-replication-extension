package com.mapr.fs.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.db.MapRDB;
import com.mapr.db.Table;
import com.mapr.fs.FileState;
import org.ojai.Document;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * The Monitor DAO is used to capture and store all event for the FS Monitor
 */
public class MonitorDAO extends AbstractDAO {

    private Table monitorTable;

    private static final String TYPE = "monitor";
    private static final String SPL_TABLE_NAME = "state";

    public MonitorDAO() throws IOException {
        String fullPath = this.getFullTableName(TYPE, SPL_TABLE_NAME);
        monitorTable = this.getTable(fullPath);
    }

    private static final Object lock = new Object();


    public void put(String json) {
        Document document = MapRDB.newDocument(json);
        monitorTable.insertOrReplace(document);
        monitorTable.flush();
    }

    public FileState get(Path path) throws IOException {
        Document document = monitorTable.findById(path.toString());
        if (document != null) {
            return getFileState(document);
        }
        return null;
    }

    public FileState remove(Path path) throws IOException {
        FileState fileState = get(path);
        monitorTable.delete(path.toString());
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
