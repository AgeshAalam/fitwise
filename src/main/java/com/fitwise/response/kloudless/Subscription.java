package com.fitwise.response.kloudless;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/*
 * Created by Vignesh G on 02/02/21
 */
@Getter
@Setter
public class Subscription {

    @SerializedName("id")
    @JsonProperty("id")
    private Long id;

    @SerializedName("account")
    @JsonProperty("account")
    private Long account;

    @SerializedName("type")
    @JsonProperty("type")
    private String type;

    @SerializedName("api")
    @JsonProperty("api")
    private String api;

    @SerializedName("active")
    @JsonProperty("active")
    private boolean active;

    @SerializedName("disable_reason")
    @JsonProperty("disable_reason")
    private String disableReason;

    @SerializedName("subscription_type")
    @JsonProperty("subscription_type")
    private String subscriptionType;

    @SerializedName("default")
    @JsonProperty("default")
    private boolean isDefault;

    @SerializedName("expiry")
    @JsonProperty("expiry")
    private String expiry;

    @SerializedName("created")
    @JsonProperty("created")
    private String created;

    @SerializedName("modified")
    @JsonProperty("modified")
    private String modified;

    @SerializedName("last_cursor_updated_at")
    @JsonProperty("last_cursor_updated_at")
    private String lastCursorUpdatedAt;

    @SerializedName("last_cursor")
    @JsonProperty("last_cursor")
    private String lastCursor;

    @SerializedName("monitored_resources")
    @JsonProperty("monitored_resources")
    List<SubscriptionMonitoredResource> monitoredResources;

    @SerializedName("monitored_resource_object_type")
    @JsonProperty("monitored_resource_object_type")
    private String monitoredResourceObjectType;

    @SerializedName("monitored_resource_api")
    @JsonProperty("monitored_resource_api")
    private String monitoredResourceApi;

}
