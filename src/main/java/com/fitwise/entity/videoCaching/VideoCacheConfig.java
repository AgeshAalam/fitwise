package com.fitwise.entity.videoCaching;

import com.fitwise.entity.User;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Data
public class VideoCacheConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long videoCacheConfigId;

    private boolean throughWifi;

    private boolean autoDownload;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "video_quality_id")
    private VideoQuality videoQuality;

}
