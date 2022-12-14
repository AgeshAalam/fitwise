package com.fitwise.view.payment.stripe;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StripePaymentMethodResponseView {
    private String paymentMethodId;
    private String maskedCardNumber;
    private String cardType;
}
