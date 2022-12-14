package com.fitwise.entity.videoCaching;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
public class VideoQuality {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long videoQualityId;

    private String title;

    private String description;

    private String vimeoVideoQuality;

    private String vimeoPublicName;

}
