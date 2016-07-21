package com.mapr.fs.controllers;

import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
@ResponseBody
@RequestMapping("/clusters")
public class ClusterController {

    private static final Logger log = Logger.getLogger(ClusterController.class);


    @RequestMapping(value = "/", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getClusters() throws IOException {
        log.info("==================getClusters==================");
        return ResponseEntity.ok(/*new ClusterDAO().getClusters()*/"==================getClusters==================");
    }

    @RequestMapping(value = "/{cluster_name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getClusterInfo(
            @PathVariable("cluster_name") String name){

        log.info("==================addCluster==================");
        log.info("name : " + name);
        if (name == null) throw new IllegalArgumentException("cluster_name == null");

        return ResponseEntity.ok(/*new ClusterDAO().getClusterInfo(name)*/"==================addCluster==================");
    }
    
}
