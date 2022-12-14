package com.fitwise.response.kloudless;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Reminder {

    @JsonProperty("minutes")
    private Long minutes;

    @JsonProperty("method")
    private String method;

    @JsonProperty("email")
    private Email email;

}
