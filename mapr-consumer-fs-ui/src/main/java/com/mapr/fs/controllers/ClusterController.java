package com.mapr.fs.controllers;

import com.mapr.fs.dao.ClusterDAO;
import org.apache.log4j.Logger;
import org.ojai.Document;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ClusterController {

    @RequestMapping(value = "/clusters", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getClusters() throws IOException {
        Map<String, List<Document>> result = new HashMap<>();
        result.put("clusters", new ClusterDAO().getAll());
        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/clusters", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addClusters(@RequestParam("cluster_name") String name) throws IOException {
        new ClusterDAO().put(name, null, null);

        return ResponseEntity.ok().build();
    }

    private static final Logger log = Logger.getLogger(ClusterController.class);

    @RequestMapping(value = "/cluster/{cluster_name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getClusterInfo(
            @PathVariable("cluster_name") String name) throws IOException {
        if (name == null) throw new IllegalArgumentException("cluster_name == null");

        return ResponseEntity.ok(new ClusterDAO().getClusterInfo(name));
    }
}
