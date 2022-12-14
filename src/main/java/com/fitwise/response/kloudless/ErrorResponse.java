package com.fitwise.response.kloudless;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {

    @JsonProperty("error_code")
    private String errorCode;

    private String message;

    @JsonProperty("status_code")
    private int statusCode;

}
