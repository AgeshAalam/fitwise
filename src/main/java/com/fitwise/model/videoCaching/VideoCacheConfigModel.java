package com.fitwise.model.videoCaching;

import lombok.Data;

@Data
public class VideoCacheConfigModel {

    private boolean throughWifi;

    private boolean autoDownload;

    private Long videoQualityId;
}
