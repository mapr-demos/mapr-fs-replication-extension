package com.mapr.fs.controllers;

import com.mapr.fs.dao.VolumeStatusDao;
import com.mapr.fs.dao.dto.VolumeStatusDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@RestController
public class StatusPageController {


    private VolumeStatusDao dao;

    public StatusPageController() throws IOException {
        dao = new VolumeStatusDao();
    }

    @RequestMapping(value = "/volumes/status/{volumeName}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getVolumeStatusByName(@PathVariable String volumeName) throws IOException {

        VolumeStatusDto dto = dao.getVolumeFileStatusesByVolumeName(volumeName);
        if (dto != null){
            dto.setFiles(dto.getFiles().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet()));
        }
        return ResponseEntity.ok(dto);
    }

    @RequestMapping(value = "/volumes/status", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllVolumes() throws IOException {

        List<VolumeStatusDto> dtos = dao.getAllVolumeFileStatuses();
        List<String> volumeNames = dtos.stream().map(VolumeStatusDto::getVolumeName).collect(Collectors.toList());

        return ResponseEntity.ok(volumeNames);
    }

}
