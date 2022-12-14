package com.fitwise.view.payment.authorizenet;

import lombok.Getter;
import lombok.Setter;

/**
 * Model class to cancel recurring subscription
 */
@Getter
@Setter
public class CancelRecurringSubscriptionRequestView {
    private String subscriptionId;
}
