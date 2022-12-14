package com.fitwise.response.zoom;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Deauthorization {

    @JsonProperty("user_data_retention")
    private String userDataRetention;

    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("signature")
    private String signature;

    @JsonProperty("deauthorization_time")
    private String deauthorizationTime;

    @JsonProperty("client_id")
    private String clientId;

}
