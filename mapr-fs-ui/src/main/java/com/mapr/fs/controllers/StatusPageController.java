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
import java.util.stream.Collectors;


@RestController
public class StatusPageController {


    private VolumeStatusDao dao = new VolumeStatusDao();

    @RequestMapping(value = "/volumes/{volumeName}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getVolumeStatusByName(@PathVariable String volumeName) throws IOException {

        VolumeStatusDto dto = dao.getVolumeFileStatusesByVolumeName(volumeName);
        return ResponseEntity.ok(dto);
    }

    @RequestMapping(value = "/volumes/{volumeName}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllVolumes(@PathVariable String volumeName) throws IOException {

        List<VolumeStatusDto> dtos = dao.getAllVolumeFileStatuses();
        List<String> volumeNames = dtos.stream().map(VolumeStatusDto::getVolumeName).collect(Collectors.toList());
        return ResponseEntity.ok(volumeNames);
    }

}
