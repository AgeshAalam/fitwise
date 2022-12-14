package com.fitwise.entity.videoCaching;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Data
public class VideoCacheConfigAndQualityMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "video_cache_config_id")
    private VideoCacheConfig videoCacheConfig;

    @ManyToOne
    @JoinColumn(name = "video_quality_id")
    private VideoQuality videoQuality;

    private boolean isActive;

}
