package com.mapr.fs.controllers;

import com.mapr.fs.dao.VolumeDAO;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class VolumeController {
    private static final Logger log = Logger.getLogger(VolumeController.class);


    @RequestMapping(value = "/volumes", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity changeStatus(
            @RequestParam("volume_name") String volume_name,
            @RequestParam("path") String path,
            @RequestParam("monitoring") Boolean monitoring) throws IOException {

        if (volume_name == null) throw new IllegalArgumentException("volume_name == null");
        if (path == null) throw new IllegalArgumentException("path == null");
        if (monitoring == null) throw new IllegalArgumentException("monitoring == null");

        path = checkDir(path);

        new VolumeDAO().put(volume_name, path, monitoring);
        log.info(volume_name);
        log.info(path);
        log.info(monitoring);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/volumes", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getVolumes() throws IOException {
        return ResponseEntity.ok(new VolumeDAO().getAllVolumes());
    }


    private static String checkDir(String topicPath) {
        File file  = new File(topicPath);
        if (!file.exists()) {
            file.mkdirs();
            log.info(file +" created");
        }

        return file.getAbsolutePath();
    }
}
