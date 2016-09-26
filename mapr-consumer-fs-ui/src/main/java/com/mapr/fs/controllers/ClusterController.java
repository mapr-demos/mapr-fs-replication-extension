package com.mapr.fs.controllers;

import com.mapr.fs.dao.ClusterDAO;
import com.mapr.fs.dao.dto.ClusterDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class ClusterController {

    @RequestMapping(value = "/clusters", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getClusters() throws IOException {
        Map<String, List<ClusterDTO>> result = new HashMap<>();
        result.put("clusters", new ClusterDAO().getAll());
        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/clusters", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addClusters(@RequestParam("cluster_name") String name) throws IOException {
        new ClusterDAO().put(name, null, null, null);

        return ResponseEntity.ok().build();
    }


    @RequestMapping(value = "/cluster/{cluster_name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getClusterInfo(
            @PathVariable("cluster_name") String name) throws IOException {
        if (name == null) throw new IllegalArgumentException("cluster_name == null");

        return ResponseEntity.ok(new ClusterDAO().getClusterInfo(name));
    }
}
