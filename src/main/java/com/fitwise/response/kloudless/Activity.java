package com.fitwise.response.kloudless;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Activity {

    @SerializedName("id")
    private String id;

    @SerializedName("account")
    private Long account;

    @SerializedName("subscription")
    private Long subscription;

    @SerializedName("action")
    private String action;

    @SerializedName("ip")
    private String ip;

    @SerializedName("modified")
    private String modified;

    @SerializedName("type")
    private String type;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("api")
    private String api;

    @SerializedName("metadata")
    private JsonObject metadata;

    @SerializedName("previous_metadata")
    private JsonObject previousMetadata;

}
