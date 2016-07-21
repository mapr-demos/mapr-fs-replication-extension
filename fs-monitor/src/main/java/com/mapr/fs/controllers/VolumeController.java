package com.mapr.fs.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
@ResponseBody
@RequestMapping(value = "/v1")
public class VolumeController {
    private static final Logger log = Logger.getLogger(VolumeController.class);

    @RequestMapping(value = "/volumes/{cluster_name}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getVolumes(
            @PathVariable("cluster_name") String name) throws JsonProcessingException {
        log.info("==================getVolumes==================");

        if (name == null) throw new IllegalArgumentException("cluster_name == null");
        return ResponseEntity.ok(/*new ClusterDAO().getVolumes(name)*/ "==================getVolumes==================");
    }

    @RequestMapping(value = "/{volumeNme}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addVolume(
            @PathVariable String volumeNme,
            @RequestParam("cluster_name") String clust_name,
            @RequestParam("path") String path) throws IOException {

        log.info("==================addVolume==================");
        log.info("name : " + volumeNme);

        if (volumeNme == null) throw new IllegalArgumentException("vol_name == null");
        if (clust_name == null) throw new IllegalArgumentException("cluster_name == null");
        if (path == null) throw new IllegalArgumentException("path == null");

//        new ClusterDAO().put(clust_name, vol_name, path);

        return ResponseEntity.status(200).build();
    }

}
