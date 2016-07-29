package com.mapr.fs.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.db.MapRDB;
import com.mapr.db.Table;
import com.mapr.fs.dao.dto.ClusterDTO;
import com.mapr.fs.dao.dto.VolumeDTO;
import org.apache.log4j.Logger;
import org.ojai.Document;
import org.ojai.DocumentStream;

import java.io.IOException;
import java.util.*;

import static com.mapr.fs.config.Config.APPS_DIR;

public class ClusterDAO {
    private static final Logger log = Logger.getLogger(ClusterDAO.class);

    public Table getClusterTable() {
        return clusterTable;
    }

    private Table clusterTable;

    private static final String CLUSTER_TABLE = APPS_DIR + "clusters/state";

    public ClusterDAO() {
        this.clusterTable = this.getTable(CLUSTER_TABLE);
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
        Document document = clusterTable.findById(cluster);
        ObjectMapper mapper = new ObjectMapper();
        ClusterDTO clusterDTO;
        VolumeDTO volumeDTO;

        if (document != null) {
            clusterDTO = mapper.readValue(document.asJsonString(), ClusterDTO.class);
        } else {
            clusterDTO = getClusterDTO(cluster);
        }

        if (!clusterDTO.getVolumes().isEmpty()) {
            boolean contain = clusterDTO.getVolumes().contains(getVolumeDTO(volume, !replication));
            if (contain) {
                clusterDTO.getVolumes().remove(getVolumeDTO(volume, !replication));
                clusterDTO.getVolumes().add(getVolumeDTO(volume, replication));
            } else {
                volumeDTO = getVolumeDTO(volume, replication);
                clusterDTO.getVolumes().add(volumeDTO);
            }

        } else {
            if (volume != null && replication != null) {
                volumeDTO = getVolumeDTO(volume, replication);
                clusterDTO.getVolumes().add(volumeDTO);
            }
        }

        String json = mapper.writeValueAsString(clusterDTO);
        if (json != null) {
            put(json);
        }
    }

    public void put(String json) {
        Document doc = MapRDB.newDocument(json);
        clusterTable.insertOrReplace(doc);
        clusterTable.flush();
    }

    public List<Document> getAll() throws IOException {

        DocumentStream documents = clusterTable.find();
        List<Document> list = new LinkedList<>();
        if (documents != null) {
            for (Document doc : documents) {
                list.add(doc);
            }
            return list;
        }
        return null;
    }

    public Map getClusters() throws IOException {
        DocumentStream documents = clusterTable.find();
        Set<String> clusters = new HashSet<>();
        Map<String, Set<String>> map = new HashMap<>();

        if (documents != null) {
            for (Document document : documents) {
                clusters.add(document.getString("cluster_name"));
            }
        }

        map.put("clusters", clusters);
        return map;
    }

    public Document getClusterInfo(String clusterName) {
        return clusterTable.findById(clusterName);
    }

    public List getVolumes(String clusterName) throws IOException {
        Document document = clusterTable.findById(clusterName);
        if (document != null) {
            return document.getList("volumes");
        }
        return null;
    }

    private VolumeDTO getVolumeDTO(String volume, Boolean replication) {
        VolumeDTO volumeDTO = new VolumeDTO();
        volumeDTO.setName(volume);
        volumeDTO.setReplicating(replication);
        return volumeDTO;
    }

    private ClusterDTO getClusterDTO(String cluster) {
        ClusterDTO clusterDTO = new ClusterDTO();
        clusterDTO.set_id(cluster);
        clusterDTO.setCluster_name(cluster);
        clusterDTO.setVolumes(new HashSet<>());
        return clusterDTO;
    }
}

