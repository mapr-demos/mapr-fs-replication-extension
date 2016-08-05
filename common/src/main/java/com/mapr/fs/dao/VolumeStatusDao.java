package com.mapr.fs.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.db.MapRDB;
import com.mapr.db.Table;
import com.mapr.fs.dao.dto.FileStatusDto;
import com.mapr.fs.dao.dto.VolumeStatusDto;
import lombok.extern.slf4j.Slf4j;
import org.ojai.Document;
import org.ojai.DocumentStream;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static com.mapr.fs.Config.APPS_DIR;

@Slf4j
public class VolumeStatusDao {

    private static String tableName = APPS_DIR + "volumeStatuses";
    private Table volumeStatusTable;
    private static final Object lock = new Object();

    private Table getVolumeStatusTableTable() {
        Table table;
        log.info("Check DB");
        synchronized (lock) {
             table = (!MapRDB.tableExists(tableName)) ? MapRDB.createTable(tableName) : MapRDB.getTable(tableName);
        }
        return table;
    }

    public VolumeStatusDao(){
        volumeStatusTable = getVolumeStatusTableTable();
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

        VolumeStatusDto volumeStatusDto = getVolumeFileStatusesByVolumeName(volumeName);
        if (volumeStatusDto != null){
            volumeStatusDto.getFiles().add(fileStatusDto);
            return true;
        }
        return false;
    }

}
