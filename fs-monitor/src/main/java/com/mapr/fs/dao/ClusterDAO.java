package com.mapr.fs.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.db.MapRDB;
import com.mapr.db.Table;
import org.apache.log4j.Logger;
import org.ojai.Document;
import org.ojai.DocumentStream;

import java.io.IOException;
import java.util.*;

public class ClusterDAO {
    private static final Logger log = Logger.getLogger(ClusterDAO.class);

    public Table getFileStateTable() {
        return fileStateTable;
    }

    private Table fileStateTable;

    private static final String APPS_DIR = "/apps/fs/db/clusters/";
    private static final String FILE_STATE_TABLE = APPS_DIR + "state";

    public ClusterDAO() {
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







    public void put(String cluster, String volume, String path) throws IOException {
        Document document = fileStateTable.findById(cluster);
        Map<String, String> map;

        if (document != null) {
            ObjectMapper mapper = new ObjectMapper();
            String json = document.getString("volumes");

            map = mapper.readValue(json, new TypeReference<Map<String, String>>(){});
            map.put(volume, path);
        } else {
            map = new HashMap<>();
            map.put(volume, path);
        }

        String json = toJSON(cluster, map);
        if (json != null) {
            put(json);
        }
    }

    public void put(String json) {
        Document doc = MapRDB.newDocument(json);
        fileStateTable.insertOrReplace(doc);
        fileStateTable.flush();
    }



    public String getAll() throws IOException {

        DocumentStream documents = fileStateTable.find();
        List<Document> list = new LinkedList<>();

        if (documents != null) {
            for (Document doc : documents) {
                list.add(doc);
            }
            ObjectMapper mapper = new ObjectMapper();
            return mapper.createObjectNode()
                    .put("clusters", mapper.writeValueAsString(list)).toString();
        }
        return null;
    }

    public String getClusters() throws IOException {
        DocumentStream documents = fileStateTable.find();
        ObjectMapper mapper = new ObjectMapper();
        Set<String> clusters = new HashSet<>();

        if (documents != null) {
            for (Document document : documents) {
                JsonNode node = mapper.readTree(document.toString());
                clusters.add(node.get("cluster_name").toString());
            }
        }
        return mapper.createObjectNode()
                .put("clusters", mapper.writeValueAsString(clusters)).toString();
    }

    public String getClusterInfo(String clusterName) {
        Document document = fileStateTable.findById(clusterName);
        if (document != null) {
            return new ObjectMapper().createObjectNode()
                    .put("name", document.getString("cluster_name"))
                    .put("volumes", document.getValue("volumes").toString()).toString();
        } else {
            return new ObjectMapper().createObjectNode().toString();
        }
    }

    public String getVolumes(String clusterName) {
        Document document = fileStateTable.findById(clusterName);
        if (document != null) {
            return document.getString("volumes");
        }
        return new ObjectMapper().createObjectNode().toString();
    }

    private String toJSON(String cluster, Map map) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();

        return mapper.createObjectNode()
                .put("_id", cluster)
                .put("cluster_name", cluster)
                .put("volumes", mapper.writeValueAsString(map)).toString();
    }
}

