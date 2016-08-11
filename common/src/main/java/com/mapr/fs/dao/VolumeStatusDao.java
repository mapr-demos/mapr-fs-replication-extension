package com.mapr.fs.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.db.MapRDB;
import com.mapr.db.Table;
import com.mapr.fs.dao.dto.FileStatusDto;
import com.mapr.fs.dao.dto.VolumeStatusDto;
import org.ojai.Document;
import org.ojai.DocumentStream;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.*;


@Repository
public class VolumeStatusDao extends AbstractDAO {

    private Table volumeStatusTable;
    private static final String TYPE = "clusters";
    private static final String SPL_TABLE_NAME = "volumeStatus";

    public VolumeStatusDao() throws IOException {
        String fullPath = this.getFullTableName(TYPE, SPL_TABLE_NAME);
        volumeStatusTable = this.getTable(fullPath);
    }

    public VolumeStatusDto getVolumeFileStatusesByVolumeName(String volumeName) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        Document document = volumeStatusTable.findById(volumeName);
        if (document != null) {
            return mapper.readValue(document.asJsonString(), VolumeStatusDto.class);
        }
        else return null;
    }

    public List<VolumeStatusDto> getAllVolumeFileStatuses() throws IOException {
        DocumentStream vols = volumeStatusTable.find();
        ObjectMapper mapper = new ObjectMapper();
        List<VolumeStatusDto> volumes = new LinkedList<>();

        if (vols != null) {
            for(Document doc : vols) {
                VolumeStatusDto vd = mapper.readValue(doc.asJsonString(), VolumeStatusDto.class);
                volumes.add(vd);
            }
            return volumes;
        }
        return null;
    }

    public boolean putVolumeStatus(VolumeStatusDto volumeStatusDto) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        if (volumeStatusDto != null) {
            String object = mapper.writeValueAsString(volumeStatusDto);
            volumeStatusTable.insertOrReplace(MapRDB.newDocument(object));
            return true;
        }
        else return false;
    }
    public boolean putFileStatusByVolumeName(String volumeName, FileStatusDto fileStatusDto) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        VolumeStatusDto volumeStatusDto = getVolumeFileStatusesByVolumeName(volumeName);
        if (volumeStatusDto != null){
            if(!volumeStatusDto.getFiles().add(fileStatusDto)){
                volumeStatusDto.getFiles().remove(fileStatusDto);
                volumeStatusDto.getFiles().add(fileStatusDto);
            }
            String doc = mapper.writeValueAsString(volumeStatusDto);
            volumeStatusTable.insertOrReplace(MapRDB.newDocument(doc));
            return true;
        }
        else {
            LinkedHashSet<FileStatusDto> dtoSet = new LinkedHashSet<>();
            dtoSet.add(fileStatusDto);
            VolumeStatusDto dto = createVolumeStatusDTO(volumeName, dtoSet);
            String doc = mapper.writeValueAsString(dto);
            volumeStatusTable.insert(MapRDB.newDocument(doc));
            return true;
        }
    }

    private VolumeStatusDto createVolumeStatusDTO(String volumeName, LinkedHashSet<FileStatusDto> dtoSet) {
        VolumeStatusDto dto = new VolumeStatusDto();
        dto.setVolumeName(volumeName);
        dto.setFiles(dtoSet);
        return dto;
    }
}
