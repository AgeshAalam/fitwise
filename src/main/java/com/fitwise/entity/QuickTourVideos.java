package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class QuickTourVideos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long quickTourVideoId;

    private String createdBy;

    @OneToOne
    @JoinColumn(name = "video_management_id")
    private VideoManagement videoManagement;
}
