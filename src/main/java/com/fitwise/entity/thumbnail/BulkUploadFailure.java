package com.fitwise.entity.thumbnail;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/*
 * Created by Vignesh G on 07/08/20
 */
@Entity
@Data
public class BulkUploadFailure {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(cascade = CascadeType.DETACH)
    private BulkUpload bulkUpload;

    private String imageTitle;

    private String status;

    private String message;

}
