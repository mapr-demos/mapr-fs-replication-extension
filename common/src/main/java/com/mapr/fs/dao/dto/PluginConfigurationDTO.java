package com.mapr.fs.dao.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor(suppressConstructorProperties = true)
@NoArgsConstructor
public class PluginConfigurationDTO {

    private String volumeName;
    private String volumePath;
    private boolean createEnabled = false;
    private boolean deleteEnabled = false;
    private boolean modifyEnabled = false;
    private boolean renameEnabled = false;
}