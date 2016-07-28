package com.mapr.fs.controllers;

import com.mapr.fs.dao.ClusterDAO;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
@ResponseBody
public class VolumeController {
    private static final Logger log = Logger.getLogger(VolumeController.class);

    @RequestMapping(value = "/volumes/{cluster_name}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getVolumes(
            @PathVariable("cluster_name") String name) throws IOException {

        if (name == null) throw new IllegalArgumentException("cluster_name == null");

        return ResponseEntity.ok(new ClusterDAO().getVolumes(name));
    }

    @RequestMapping(value = "volume/{volume_name}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addVolume(
            @PathVariable String volume_name,
            @RequestParam("cluster_name") String clust_name) throws IOException {

        if (volume_name == null) throw new IllegalArgumentException("volume_name == null");
        if (clust_name == null) throw new IllegalArgumentException("cluster_name == null");

        new ClusterDAO().put(clust_name, volume_name, true);

        return ResponseEntity.status(200).build();
    }

}
