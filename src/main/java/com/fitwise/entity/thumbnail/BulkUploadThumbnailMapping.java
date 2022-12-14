package com.fitwise.entity.thumbnail;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/*
 * Created by Vignesh G on 07/08/20
 */
@Entity
@Data
public class BulkUploadThumbnailMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mappingId;

    @OneToOne(cascade = CascadeType.DETACH)
    private ThumbnailImages thumbnailImages;

    @ManyToOne(cascade = CascadeType.DETACH)
    private BulkUpload bulkUpload;

}
