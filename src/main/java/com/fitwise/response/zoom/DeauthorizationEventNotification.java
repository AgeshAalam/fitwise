package com.fitwise.response.zoom;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeauthorizationEventNotification {

    @JsonProperty("event")
    private String event;

    @JsonProperty("payload")
    private Deauthorization payload;

}
