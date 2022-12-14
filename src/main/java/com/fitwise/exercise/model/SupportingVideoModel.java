package com.fitwise.exercise.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/*
 * Created by Vignesh G on 18/03/20
 */

@Getter
@Setter
public class SupportingVideoModel implements Serializable {

    private String title;

    private String description;

    private String fileName;

    private Long fileSize;

    private Long imageId;

    private String thumbnailUrl;

    private VimeoModel vimeoData;

    private long duration;

    private String videoUploadStatus;
    
    private Long videoId;

}
