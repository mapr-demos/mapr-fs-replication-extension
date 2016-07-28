package com.mapr.fs.dao;

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







    public void put(String cluster, String volume, Boolean replication) throws IOException {
        Document document = fileStateTable.findById(cluster);
        ObjectMapper mapper = new ObjectMapper();
        ClusterPOJO clusterPOJO;
        if (document != null) {
            clusterPOJO = mapper.readValue(document.asJsonString(), ClusterPOJO.class);
        } else {
            clusterPOJO = getClusterPOJO(cluster);
        }

        VolumePOJO volumePOJO = new VolumePOJO();
        volumePOJO.setName(volume);
        volumePOJO.setReplicating(replication);
        //TODO check that volume already exists
        clusterPOJO.getVolumes().add(volumePOJO);
        String json = mapper.writeValueAsString(clusterPOJO);
        if (json != null) {
            put(json);
        }
    }

    private ClusterPOJO getClusterPOJO(String cluster) {
        ClusterPOJO clusterPOJO = new ClusterPOJO();
        clusterPOJO.set_id(cluster);
        clusterPOJO.setClusterName(cluster);
        clusterPOJO.setVolumes(new ArrayList<>());
        return clusterPOJO;
    }

    public void put(String json) {
        Document doc = MapRDB.newDocument(json);
        fileStateTable.insertOrReplace(doc);
        fileStateTable.flush();
    }



    public List<Document> getAll() throws IOException {

        DocumentStream documents = fileStateTable.find();
        List<Document> list = new LinkedList<>();
        if (documents != null) {
            for (Document doc : documents) {
                list.add(doc);
            }
            return list;
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

    public String getVolumes(String clusterName) throws IOException {
        Document document = fileStateTable.findById(clusterName);
        ObjectMapper mapper = new ObjectMapper();
        if (document != null) {
            String json = document.getString("volumes");

            Map<String, Boolean> map;
            List<String> result = new ArrayList<>();
            map = mapper.readValue(json, new TypeReference<Map<String, Boolean>>(){});

            for (Map.Entry<String, Boolean> entry : map.entrySet()) {
                if (entry.getValue()) {
                    result.add(entry.getKey());
                }
            }

            return mapper.createObjectNode().put("volumes", mapper.writeValueAsString(result)).toString();
        }
        return new ObjectMapper().createObjectNode().toString();
    }
}

