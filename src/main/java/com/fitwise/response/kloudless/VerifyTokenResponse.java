package com.fitwise.response.kloudless;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyTokenResponse {

    @SerializedName("account_id")
    private long accountId;

    @SerializedName("client_id")
    private String clientId;

    private String scope;

}
