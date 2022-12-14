package com.fitwise.view.payment.stripe;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StripeRefundRequestView {
    private String chargeId;
    private Double refundableAmount;
}
