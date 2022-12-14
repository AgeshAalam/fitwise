package com.fitwise.view.thumbnail;

import lombok.Data;

import java.util.Date;

/*
 * Created by Vignesh G on 07/08/20
 */
@Data
public class BulkUploadView {

    private Long bulkUploadId;

    private String fileName;

    private Date uploadedDate;

    private String uploadedDateFormatted;

    private Integer totalImages;

    private Integer success;

    private Integer failure;

    private String uploadStatus;

}
