package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
@Getter
@Setter
public class Audios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long audioId;

    private String filePath;

    private String fileName;

    private long duration;


    @ManyToOne
    private User user;
}
