package com.fitwise.view.payment.paypal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayPalTransactionRequestView {
    private Long instructorPaymentId;
    private String transactionId;
    private String paymentMode;
    private Long transferDate;
}
