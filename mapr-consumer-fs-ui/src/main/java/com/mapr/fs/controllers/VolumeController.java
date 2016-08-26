package com.mapr.fs.controllers;

import com.mapr.fs.dao.ClusterDAO;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;

@RestController
public class VolumeController {
    private static final Logger log = Logger.getLogger(VolumeController.class);

    @RequestMapping(value = "/volumes/{cluster_name}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getVolumes(
            @PathVariable("cluster_name") String name) throws IOException {

        if (name == null) throw new IllegalArgumentException("cluster_name == null");

        return ResponseEntity.ok(new ClusterDAO().getVolumes(name));
    }

    @RequestMapping(value = "/volumes/status", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity changeStatus(
            @RequestParam("volume_name") String volume_name,
            @RequestParam("cluster_name") String cluster_name,
            @RequestParam("path") String path,
            @RequestParam("replication") Boolean status) throws IOException {

        if (volume_name == null) throw new IllegalArgumentException("volume_name == null");
        if (cluster_name == null) throw new IllegalArgumentException("cluster_name == null");
        if (path == null) throw  new IllegalArgumentException("path == null");
        if (status == null) throw new IllegalArgumentException("status == null");

        new ClusterDAO().put(cluster_name, volume_name, path, status);
        log.info(cluster_name);
        log.info(volume_name);
        log.info(status);
        log.info(path);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/volumes", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addVolume(
            @RequestParam("volume_name") String volume_name,
            @RequestParam("path") String path,
            @RequestParam("cluster_name") String cluster_name) throws IOException {

        if (volume_name == null) throw new IllegalArgumentException("volume_name == null");
        if (cluster_name == null) throw new IllegalArgumentException("cluster_name == null");
        if (path == null) throw  new IllegalArgumentException("path == null");

        if (checkDir(path, volume_name)) {
            new ClusterDAO().put(cluster_name, volume_name, path, true);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    private static boolean checkDir(String replicationTargetFolder, String topicName) {
        File file  = new File(replicationTargetFolder , topicName);
        if (!file.exists()) {
            return file.mkdirs();
        } else {
            return true;
        }
    }
}
