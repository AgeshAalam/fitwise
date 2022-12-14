package com.fitwise.view.thumbnail;

import lombok.Data;

/*
 * Created by Vignesh G on 07/08/20
 */
@Data
public class BulkUploadFailureView {

    private Long id;

    private String imageTitle;

    private String status;

    private String message;

}
