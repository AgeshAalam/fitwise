package com.fitwise.request.zoom;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitwise.response.zoom.Deauthorization;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataCompliance {

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("deauthorization_event_received")
    private Deauthorization deauthorizationEventReceived;

    @JsonProperty("compliance_completed")
    private Boolean complianceCompleted;

}
