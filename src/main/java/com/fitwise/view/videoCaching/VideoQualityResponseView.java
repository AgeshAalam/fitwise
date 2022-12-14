package com.fitwise.view.videoCaching;

import com.fitwise.entity.videoCaching.VideoQuality;
import lombok.Data;

@Data
public class VideoQualityResponseView {

    private long videoQualityId;

    private VideoQuality videoQuality;

    private String title;

    private String description;

    private boolean isUserSelected;
}
