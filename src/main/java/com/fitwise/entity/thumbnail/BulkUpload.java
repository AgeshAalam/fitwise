package com.fitwise.entity.thumbnail;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

/*
 * Created by Vignesh G on 07/08/20
 */
@Entity
@Data
public class BulkUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long bulkUploadId;

    private String fileName;

    private Date uploadedTime;

    private Integer totalImages;

    private Integer success;

    private Integer failure;

    private String uploadStatus;

}
