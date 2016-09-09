package com.mapr.fs.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.db.MapRDB;
import com.mapr.db.Table;
import com.mapr.fs.dao.dto.SourceDTO;
import com.mapr.fs.dao.dto.VolumeOfSourceDTO;
import org.ojai.Document;
import org.ojai.DocumentStream;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SourceDAO extends AbstractDAO {
    private Table sourceTable;

    private static final String TYPE = "sources";
    private static final String SPL_TABLE_NAME = "state";


    public SourceDAO() throws IOException {
        String fullPath = this.getFullTableName(TYPE, SPL_TABLE_NAME);
        sourceTable = this.getTable(fullPath);
    }

    public void put(String volume, String path, String bucket, Boolean creating, Boolean deleting, Boolean modifying, Boolean moving) throws IOException {
        Document document = sourceTable.findById(bucket);
        ObjectMapper mapper = new ObjectMapper();

        SourceDTO sourceDTO;

        if (document != null) {
            sourceDTO = mapper.readValue(document.asJsonString(), SourceDTO.class);
        } else {
            sourceDTO = new SourceDTO(bucket, new LinkedHashSet<>());
        }

        updateVolumes(volume, path, creating, deleting, modifying, moving, sourceDTO);

        String json = mapper.writeValueAsString(sourceDTO);
        if (json != null) {
            put(json);
        }
    }

    private void updateVolumes(String volume, String path, Boolean creating, Boolean deleting, Boolean modifying, Boolean moving, SourceDTO sourceDTO) {
        VolumeOfSourceDTO volumeOfSourceDTO;
        if (!sourceDTO.getVolumes().isEmpty()) {
            boolean contain = sourceDTO.getVolumes().contains(new VolumeOfSourceDTO(volume, path, creating, deleting, modifying, moving));
            if (contain) {
                sourceDTO.getVolumes().remove(new VolumeOfSourceDTO(volume, path, creating, deleting, modifying, moving));
                sourceDTO.getVolumes().add(new VolumeOfSourceDTO(volume, path, creating, deleting, modifying, moving));
            } else {
                volumeOfSourceDTO = new VolumeOfSourceDTO(volume, path, creating, deleting, modifying, moving);
                sourceDTO.getVolumes().add(volumeOfSourceDTO);
            }

        } else {
            volumeOfSourceDTO = new VolumeOfSourceDTO(volume, path, creating, deleting, modifying, moving);
            sourceDTO.getVolumes().add(volumeOfSourceDTO);
        }
        filterVolumes(sourceDTO);
    }

    private void filterVolumes(SourceDTO sourceDTO) {
        sourceDTO.setVolumes(sourceDTO.getVolumes().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet<VolumeOfSourceDTO>::new)));
    }

    public Document get(String bucket) {
        return sourceTable.findById(bucket);
    }

    public List<SourceDTO> getAll() throws IOException {

        DocumentStream documents = sourceTable.find();

        ObjectMapper mapper = new ObjectMapper();
        List<SourceDTO> list = new LinkedList<>();

        if (documents != null) {
            for (Document doc : documents) {
                SourceDTO sourceDTO = mapper.readValue(doc.asJsonString(), SourceDTO.class);
                list.add(sourceDTO);
            }
            return list;
        }
        return null;
    }

    public void deleteVolume(String bucketName, String volumeName, String path) throws IOException {
        Document document = sourceTable.findById(bucketName);
        ObjectMapper mapper = new ObjectMapper();
        if (document != null) {

            SourceDTO sourceDTO = mapper.readValue(document.asJsonString(), SourceDTO.class);
            sourceDTO.getVolumes().remove(new VolumeOfSourceDTO(volumeName, path, true, true, true, true));

            String json = mapper.writeValueAsString(sourceDTO);
            if (json != null) {
                put(json);
            }
        }
    }

    public void put(String json) {
        Document doc = MapRDB.newDocument(json);
        sourceTable.insertOrReplace(doc);
        sourceTable.flush();
    }

}
