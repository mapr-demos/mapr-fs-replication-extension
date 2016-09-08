package com.mapr.fs.controllers;

import com.mapr.fs.dao.SourceDAO;
import com.mapr.fs.dao.dto.SourceDTO;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SourceController {
    private static final Logger log = Logger.getLogger(SourceController.class);


    @RequestMapping(value = "/sources", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addSource(
            @RequestParam("bucket") String bucket,
            @RequestParam("volume_name") String volume,
            @RequestParam("creating") Boolean creating,
            @RequestParam("deleting") Boolean deleting,
            @RequestParam("modifying") Boolean modifying,
            @RequestParam("moving") Boolean moving
    ) throws IOException {
        log.info("bucket: " + bucket);
        log.info("volume: " + volume);
        log.info("creating: " + creating);
        log.info("deleting: " + deleting);
        log.info("modifying: " + modifying);
        log.info("moving: " + moving);
        new SourceDAO().put(volume, bucket, creating, deleting, modifying, moving);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/sources/{bucket}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getSourceByBucketName(
            @PathVariable("bucket") String bucket) throws IOException {

        if (bucket == null) throw new IllegalArgumentException("bucket == null");

        log.info(bucket);

        return ResponseEntity.ok(new SourceDAO().get(bucket));
    }

    @RequestMapping(value = "/sources", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getSources() throws IOException {
        Map<String, List<SourceDTO>> result = new HashMap<>();
        result.put("sources", new SourceDAO().getAll());
        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/sources/del", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteSource(
            @RequestParam("bucket") String bucket,
            @RequestParam("volume_name") String volume
    ) throws IOException {
        log.info("bucket: " + bucket);
        log.info("volume: " + volume);

        new SourceDAO().deleteVolume(bucket, volume);
        return ResponseEntity.ok().build();
    }
}
