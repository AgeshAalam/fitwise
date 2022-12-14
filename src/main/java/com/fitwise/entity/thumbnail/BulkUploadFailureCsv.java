package com.fitwise.entity.thumbnail;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/*
 * Created by Vignesh G on 11/08/20
 */
@Entity
@Data
public class BulkUploadFailureCsv {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(cascade = CascadeType.DETACH)
    private BulkUpload bulkUpload;

    private String fileName;
    private String gender;
    private String location;
    private String fitnessActivity;
    private String people;
    private String equipment;
    private String exerciseMovement;
    private String muscleGroups;
}

