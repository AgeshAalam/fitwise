package com.fitwise.request.kloudless;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimeWindow {

    @JsonProperty("start")
    private String start;

    @JsonProperty("end")
    private String end;

}
