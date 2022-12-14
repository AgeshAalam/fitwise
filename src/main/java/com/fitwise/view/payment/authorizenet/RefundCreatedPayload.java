package com.fitwise.view.payment.authorizenet;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundCreatedPayload {
    private Long responseCode;
    private String avsResponse;
    private double authAmount;
    private String entityName;
    private String id;
}
