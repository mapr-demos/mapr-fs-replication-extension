package com.mapr.fs.controllers;

import com.mapr.fs.dao.ClusterDAO;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam("replication") Boolean status) throws IOException {

        if (volume_name == null) throw new IllegalArgumentException("volume_name == null");
        if (cluster_name == null) throw new IllegalArgumentException("cluster_name == null");
        if (status == null) throw new IllegalArgumentException("status == null");

        new ClusterDAO().put(cluster_name, volume_name, status);
        log.info(cluster_name);
        log.info(volume_name);
        log.info(status);
        return ResponseEntity.ok().build();
    }



    @RequestMapping(value = "/volumes", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addVolume(
            @RequestParam("volume_name") String volume_name,
            @RequestParam("cluster_name") String cluster_name) throws IOException {

        if (volume_name == null) throw new IllegalArgumentException("volume_name == null");
        if (cluster_name == null) throw new IllegalArgumentException("cluster_name == null");

        new ClusterDAO().put(cluster_name, volume_name, true);

        return ResponseEntity.ok().build();
    }

}
