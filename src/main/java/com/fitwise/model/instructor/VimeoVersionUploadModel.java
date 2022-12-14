package com.fitwise.model.instructor;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class VimeoVersionUploadModel implements Serializable{

    /**
     * The default serialization key
     */
    private static final long serialVersionUID = 1L;

    /**
     *  The Approach
     */
    @JsonProperty("approach")
    private String approach;

    /**
     * The status
     */
    @JsonProperty("link")
    private String uploadLink;

    @JsonProperty("redirect_url")
    private String redirectUrl;

    /**
     * The status
     */
    @JsonProperty("size")
    private String size;

}
