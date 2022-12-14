package com.fitwise.request.zoom;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Occurrence {

    @JsonProperty("occurrence_id")
    private String occurrenceId;

    @JsonProperty("start_time")
    private String startTime;

    @JsonProperty("duration")
    private Integer duration;

    @JsonProperty("status")
    private String status;

}
