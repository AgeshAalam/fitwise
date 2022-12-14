package com.fitwise.model.instructor;

import lombok.Getter;
import lombok.Setter;

/**
 * Class defining the data model for the video versioning
 */
@Getter
@Setter
public class VideoVersioningModel {
    private Long versioningEntityId;
    private String fileName;
    private String fileSize;
    private String uploadLink;
}
