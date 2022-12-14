package com.fitwise.view.payment.authorizenet;

import lombok.Getter;
import lombok.Setter;


/**
 * Class that is constructed based on the response structure from Authorize.net when
 * a new subscription is created - ARB billing
 */
@Getter
@Setter
public class SubscriptionCreatedResponseView extends AuthNetWebHookParentResponseView {
    SubscriptionCreatedPayload payload;
}
