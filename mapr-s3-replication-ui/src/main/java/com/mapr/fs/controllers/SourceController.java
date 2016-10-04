package com.mapr.fs.controllers;

import com.mapr.fs.dao.S3PluginDao;
import com.mapr.fs.dao.dto.PluginConfigurationDTO;
import com.mapr.fs.dao.dto.SourceDTO;
import com.mapr.fs.service.CreateBucketService;
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

        new CreateBucketService().createBucket(bucket);

        PluginConfigurationDTO configuration =
                new PluginConfigurationDTO(volume, creating, deleting, modifying, moving);

        new S3PluginDao().putVolumeInBucket(bucket, configuration);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/sources/{bucket}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getSourceByBucketName(
            @PathVariable("bucket") String bucket) throws IOException {

        if (bucket == null) throw new IllegalArgumentException("bucket == null");

        log.info(bucket);

        return ResponseEntity.ok(new S3PluginDao().get(bucket));
    }

    @RequestMapping(value = "/sources", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getSources() throws IOException {
        Map<String, List<SourceDTO>> result = new HashMap<>();
        result.put("sources", new S3PluginDao().getAll());
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

        new S3PluginDao().deleteVolume(bucket, volume);
        return ResponseEntity.ok().build();
    }
}
