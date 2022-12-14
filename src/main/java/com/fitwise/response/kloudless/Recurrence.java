package com.fitwise.response.kloudless;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Recurrence {

    @JsonProperty("rrule")
    private String rrule;

}
