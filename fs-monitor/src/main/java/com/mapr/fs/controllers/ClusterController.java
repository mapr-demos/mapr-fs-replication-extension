package com.mapr.fs.controllers;

import com.mapr.fs.dao.ClusterDAO;
import org.apache.log4j.Logger;
import org.ojai.Document;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@ResponseBody
public class ClusterController {

    private static final Logger log = Logger.getLogger(ClusterController.class);

//    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/clusters", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    Map<String, List<Document>> getClusters() throws IOException {
        Map<String, List<Document>> result = new HashMap<>();
        result.put("clusters", new ClusterDAO().getAll());
        return result;
    }

    @RequestMapping(value = "/cluster/{cluster_name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getClusterInfo(
            @PathVariable("cluster_name") String name) {
        if (name == null) throw new IllegalArgumentException("cluster_name == null");

        return ResponseEntity.ok(new ClusterDAO().getClusterInfo(name));
    }

}
