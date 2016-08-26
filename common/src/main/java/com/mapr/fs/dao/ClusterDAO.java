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


public class ClusterDAO extends AbstractDAO {

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

        if (document != null) {
            clusterDTO = mapper.readValue(document.asJsonString(), ClusterDTO.class);
        } else {
            clusterDTO = new ClusterDTO(cluster, cluster, new HashSet<>());
        }

        updateVolumes(cluster, volume, path, replication, clusterDTO);

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

    private void updateVolumes(String cluster, String volume, String path, Boolean replication, ClusterDTO clusterDTO) {
        VolumeDTO volumeDTO;
        if (!clusterDTO.getVolumes().isEmpty()) {
            boolean contain = clusterDTO.getVolumes().contains(new VolumeDTO(cluster, volume, path, !replication));
            if (contain) {
                clusterDTO.getVolumes().remove(new VolumeDTO(cluster, volume, path, !replication));
                clusterDTO.getVolumes().add(new VolumeDTO(cluster, volume, path, replication));
            } else {
                volumeDTO = new VolumeDTO(cluster, volume, path, replication);
                clusterDTO.getVolumes().add(volumeDTO);
            }

        } else {
            if (volume != null && replication != null) {
                volumeDTO = new VolumeDTO(cluster, volume, path, replication);
                clusterDTO.getVolumes().add(volumeDTO);
            }
        }
        filterVolumes(clusterDTO);
    }

    private void filterVolumes(ClusterDTO clusterDTO) {
        clusterDTO.setVolumes(clusterDTO.getVolumes().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet<VolumeDTO>::new)));
    }

    public List<VolumeDTO> getAllVolumes() throws IOException {
        DocumentStream ds = clusterTable.find();
        ObjectMapper mapper = new ObjectMapper();
        LinkedList<VolumeDTO> volumes = new LinkedList();

        if (ds != null) {
            for (Document doc : ds) {
                ClusterDTO clusterDTO = mapper.readValue(doc.asJsonString(), ClusterDTO.class);
                volumes.addAll(clusterDTO.getVolumes()
                        .stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
            }
        }
        return volumes;
    }

    public List<ClusterDTO> getAll() throws IOException {

        DocumentStream documents = clusterTable.find();

        ObjectMapper mapper = new ObjectMapper();
        List<ClusterDTO> list = new LinkedList<>();

        if (documents != null) {
            for (Document doc : documents) {
                ClusterDTO clusterDTO = mapper.readValue(doc.asJsonString(), ClusterDTO.class);
                filterVolumes(clusterDTO);
                list.add(clusterDTO);
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
}

