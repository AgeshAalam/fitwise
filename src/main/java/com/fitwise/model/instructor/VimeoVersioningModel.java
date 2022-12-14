package com.fitwise.model.instructor;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Defining the the vimeo video version model
 */
@Getter
@Setter
public class VimeoVersioningModel {

    @JsonProperty("file_name")
    private String file_name;

    @JsonProperty("upload")
    private VimeoVersionUploadModel upload;
}
