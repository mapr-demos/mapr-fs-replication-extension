package com.mapr.fs.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.db.MapRDB;
import com.mapr.db.Table;
import com.mapr.fs.dao.dto.MonitorDTO;
import org.ojai.Document;
import org.ojai.DocumentStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VolumeDAO extends AbstractDAO {

    private Table volumeTable;

    private static final String TYPE = "monitor";
    private static final String SPL_TABLE_NAME = "volumes";

    public VolumeDAO() throws IOException {
        String fullPath = this.getFullTableName(TYPE, SPL_TABLE_NAME);
        volumeTable = this.getTable(fullPath);
    }

    public void put(String json) {
        Document document = MapRDB.newDocument(json);
        volumeTable.insertOrReplace(document);
        volumeTable.flush();
    }

    public void put(String volumeName, String path, boolean monitoring) throws IOException {
        Document document = volumeTable.findById(path);
        ObjectMapper mapper = new ObjectMapper();
        MonitorDTO monitorDTO;

        if (document != null) {
            monitorDTO = mapper.readValue(document.asJsonString(), MonitorDTO.class);
            monitorDTO.setMonitoring(monitoring);
        } else {
            monitorDTO = getVolumeDTO(volumeName, path, monitoring);
        }

        String json = mapper.writeValueAsString(monitorDTO);
        if (json != null) {
            put(json);
        }
    }

    public List<MonitorDTO> getAllVolumes() throws IOException {
        DocumentStream document = volumeTable.find();
        List<MonitorDTO> result = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        if (document != null) {
            for (Document doc : document) {
                result.add(mapper.readValue(doc.asJsonString(), MonitorDTO.class));
            }
            return result;
        }
        return null;
    }

    private MonitorDTO getVolumeDTO(String volumeName, String path, boolean monitoring) {
        MonitorDTO monitorDTO = new MonitorDTO();
        monitorDTO.setName(volumeName);
        monitorDTO.set_id(path);
        monitorDTO.setMonitoring(monitoring);
        return monitorDTO;
    }

}
