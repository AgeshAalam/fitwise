package com.fitwise.view.videoCaching;

import lombok.Data;

import java.util.List;

@Data
public class VideoCacheConfigResponseView {

    private boolean throughWifi;

    private boolean autoDownload;

    private List<VideoQualityResponseView> videoQualityList;

}
