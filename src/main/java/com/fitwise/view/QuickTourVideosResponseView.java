package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuickTourVideosResponseView {

    private Long quickTourVideoId;

    private String title;

    private String thumbnailUrl;

    private String videoUrl;

    private String parsedVideoUrl;

    private int duration;

}
