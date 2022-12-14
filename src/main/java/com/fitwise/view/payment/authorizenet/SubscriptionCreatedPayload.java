package com.fitwise.view.payment.authorizenet;

import lombok.Getter;
import lombok.Setter;
import net.authorize.api.contract.v1.PaymentProfile;

@Getter
@Setter
public class SubscriptionCreatedPayload {
    private String name;
    private double amount;
    private String status;
    private PaymentProfile profile;
    private String entityName;
    private String id;
}


