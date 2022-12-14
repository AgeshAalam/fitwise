package com.fitwise.view.payment.authorizenet;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionCancellationPayload {
    private String name;
    private Double amount;
    private String status;
    private ANetCustomerProfileResponseView profile;
    private String entityName;
    private String id;
}
