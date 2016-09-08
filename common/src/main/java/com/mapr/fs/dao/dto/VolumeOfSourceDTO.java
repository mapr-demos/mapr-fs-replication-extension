package com.mapr.fs.dao.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor(suppressConstructorProperties = true)
@NoArgsConstructor
@EqualsAndHashCode(exclude={"creating", "deleting", "modifying", "moving"})
public class VolumeOfSourceDTO {

    private String volumeName;

    private boolean creating;
    private boolean deleting;
    private boolean modifying;
    private boolean moving;
}
