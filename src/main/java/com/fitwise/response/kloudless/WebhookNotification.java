package com.fitwise.response.kloudless;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

/*
 * Created by Vignesh G on 02/02/21
 */
@Getter
@Setter
public class WebhookNotification {

    @SerializedName("account")
    private Long account;

    @SerializedName("subscription")
    private Long subscription;

}
