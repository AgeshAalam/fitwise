package com.fitwise.view.payment.authorizenet;

import lombok.Getter;
import lombok.Setter;


/**
 * Class that contains all the mandatory fields returned from Authorize.net for a WebHook response
 */
@Getter
@Setter
public class AuthNetWebHookParentResponseView {
    private String notificationId;
    private String eventType;
    private String eventDate;
    private String webhookId;
}
