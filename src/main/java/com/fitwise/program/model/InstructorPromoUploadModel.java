package com.fitwise.program.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstructorPromoUploadModel {

    private String fileName;
    private long fileSize;
    private String title;
    private String description;
    private Long imageId;
    private Long promotionId;

}
