package com.fitwise.view.payment.authorizenet;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ANetRefundRequestView {
    private String transactionId;
    private Double refundableAmount;
}
