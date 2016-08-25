package com.mapr.fs.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.db.MapRDB;
import com.mapr.db.Table;
import com.mapr.fs.dao.dto.ClusterDTO;
import com.mapr.fs.dao.dto.VolumeDTO;
import org.ojai.Document;
import org.ojai.DocumentStream;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class ClusterDAO extends AbstractDAO{

    private Table clusterTable;

    private static final String TYPE = "clusters";
    private static final String SPL_TABLE_NAME = "state";


    public ClusterDAO() throws IOException {
        String fullPath = this.getFullTableName(TYPE, SPL_TABLE_NAME);
        clusterTable = this.getTable(fullPath);
    }


    public void put(String cluster, String volume, String path, Boolean replication) throws IOException {
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
            boolean contain = clusterDTO.getVolumes().contains(getVolumeDTO(cluster, volume, path, !replication));
            if (contain) {
                clusterDTO.getVolumes().remove(getVolumeDTO(cluster, volume, path, !replication));
                clusterDTO.getVolumes().add(getVolumeDTO(cluster, volume, path, replication));
            } else {
                volumeDTO = getVolumeDTO(cluster, volume, path, replication);
                clusterDTO.getVolumes().add(volumeDTO);
            }

        } else {
            if (volume != null && replication != null) {
                volumeDTO = getVolumeDTO(cluster, volume, path, replication);
                clusterDTO.getVolumes().add(volumeDTO);
            }
        }

        String json = mapper.writeValueAsString(clusterDTO);
        if (json != null) {
            put(json);
        }
    }

    public List<VolumeDTO> getAllVolumes() throws IOException {
        DocumentStream ds = clusterTable.find();
        ObjectMapper mapper = new ObjectMapper();
        LinkedList volumes = new LinkedList();

        if (ds != null) {
            for(Document doc : ds) {
                ClusterDTO clusterDTO = mapper.readValue(doc.asJsonString(), ClusterDTO.class);
                volumes.addAll(clusterDTO.getVolumes()
                        .stream()
                        .filter(o -> o != null)
                        .collect(Collectors.toList()));
            }
        }
        return volumes;
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

    private VolumeDTO getVolumeDTO(String clusterName, String volume, String path, Boolean replication) {
        VolumeDTO volumeDTO = new VolumeDTO();
        volumeDTO.setCluster_name(clusterName);
        volumeDTO.setName(volume);
        volumeDTO.setPath(path);
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

