package com.fitwise.workout.model;

import com.fitwise.view.VideoStandards;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/*
 * Created by Vignesh G on 31/03/20
 */

@Getter
@Setter
public class SupportingVideoMappingModel {

    private String title;

    private String description;

    private String url;

    private String parsedUrl;

    private int duration;

    private Long imageId;

    private String thumbnailUrl;

    private String videoUploadStatus;

    private List<VideoStandards> supportingVideoStandards;

}
