package com.mapr.fs.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mapr.db.MapRDB;
import com.mapr.db.Table;
import org.ojai.Document;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class ConsumerDAO extends AbstractDAO {

    private Table consumerTable;
    private static final String TYPE = "consumer";
    private static final String SPL_TABLE_NAME = "state";

    public ConsumerDAO() throws IOException {
        String fullPath = this.getFullTableName(TYPE, SPL_TABLE_NAME);
        consumerTable = this.getTable(fullPath);
    }


    public void put(Path path) throws IOException {
        String json = getFileInfo(path);

        if (json != null) {
            Document document = MapRDB.newDocument(json);
            consumerTable.insertOrReplace(document);
            consumerTable.flush();
        }
    }

    public Document get(Path path) throws IOException {
        Document document = consumerTable.findById(path.toString());
        if (document != null) {
            return document;
        }
        return null;
    }

    public Boolean remove(Path path) throws IOException {

        Document document = consumerTable.findById(path.toString());

        if (document != null) {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(document.toString());
            ((ObjectNode) node).put("removed", true);

            document = MapRDB.newDocument(node.toString());
            consumerTable.insertOrReplace(document);

            return true;
        }

        return false;
    }

    private String getFileInfo(Path path) throws IOException {
        File file = new File(path.toString());
        BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);

        if (file.exists()) {
            return new ObjectMapper().createObjectNode()
                    .put("_id", path.toString())
                    .put("name", file.getName())
                    .put("path", file.getPath())
                    .put("creationTime", attr.creationTime().toString())
                    .put("lastModified", attr.lastModifiedTime().toString())
                    .put("size", file.length())
                    .put("directory", file.isDirectory())
                    .put("removed", false).toString();
        }
        return null;
    }
}
