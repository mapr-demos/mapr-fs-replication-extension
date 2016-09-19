package com.mapr.fs.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.db.MapRDB;
import com.mapr.db.Table;
import com.mapr.fs.dao.dto.PluginConfigurationDTO;
import com.mapr.fs.dao.dto.SourceDTO;
import org.ojai.Document;
import org.ojai.DocumentStream;

import java.io.IOException;
import java.util.*;


public class S3PluginDao extends AbstractDAO {

    private Table replicationConfigTable;
    private static final String TYPE = "s3";
    private static final String SPL_TABLE_NAME = "pluginConfiguration";

    public S3PluginDao() throws IOException {
        String fullPath = this.getFullTableName(TYPE, SPL_TABLE_NAME);
        replicationConfigTable = this.getTable(fullPath);
    }

    public Set<SourceDTO> getAllBucketConfigurations() throws IOException {
        DocumentStream vols = replicationConfigTable.find();
        ObjectMapper mapper = new ObjectMapper();
        Set<SourceDTO> configurations = new HashSet<>();

        if (vols != null) {
            for (Document doc : vols) {
                SourceDTO vd = mapper.readValue(doc.asJsonString(), SourceDTO.class);
                configurations.add(vd);
            }
            return configurations;
        }
        return null;
    }

    public SourceDTO getBucketConfigurationByBucketName(String bucketName) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        Document document = replicationConfigTable.findById(bucketName);
        if (document != null) {
            return mapper.readValue(document.asJsonString(), SourceDTO.class);
        } else return null;
    }

    public void putBucketConfiguration(SourceDTO sourceDTO) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        String doc = mapper.writeValueAsString(sourceDTO);
        replicationConfigTable.insertOrReplace(MapRDB.newDocument(doc));
    }

    public void putVolumeInBucket(String bucketName, PluginConfigurationDTO configuration) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SourceDTO existingBucket = getBucketConfigurationByBucketName(bucketName);
        if (existingBucket != null) {
            existingBucket.getVolumes().put(configuration.getVolumeName(), configuration);
            String doc = mapper.writeValueAsString(existingBucket);
            replicationConfigTable.insertOrReplace(MapRDB.newDocument(doc));
        } else {
            HashMap<String, PluginConfigurationDTO> map = new HashMap<>();
            map.put(configuration.getVolumeName(), configuration);
            String doc = mapper.writeValueAsString(new SourceDTO(bucketName, map));
            replicationConfigTable.insertOrReplace(MapRDB.newDocument(doc));
        }
    }

    public void deleteVolume(String bucketName, String volumeName) throws IOException {
        SourceDTO existingBucket = getBucketConfigurationByBucketName(bucketName);
        if (existingBucket != null) {
            existingBucket.getVolumes().remove(volumeName);
            if (existingBucket.getVolumes().isEmpty()) {
                replicationConfigTable.delete(bucketName);
            } else {
                putBucketConfiguration(existingBucket);
            }
        }
    }

    public Document get(String bucket) {
        return replicationConfigTable.findById(bucket);
    }

    public List<SourceDTO> getAll() throws IOException {

        DocumentStream documents = replicationConfigTable.find();

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

    public List<HashMap<String, PluginConfigurationDTO>> getAllVolumes() throws IOException {

        List<HashMap<String, PluginConfigurationDTO>> list = new LinkedList<>();
        DocumentStream documents = replicationConfigTable.find();
        ObjectMapper mapper = new ObjectMapper();

        for (Document doc : documents) {
            SourceDTO sourceDTO = mapper.readValue(doc.asJsonString(), SourceDTO.class);
            list.add(sourceDTO.getVolumes());
        }

        return list;
    }

}
