package com.fitwise.view.payment.stripe;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StripeSavedCardsResponseView {
    private String customerId;
    private List<StripePaymentMethodResponseView> savedCards;
}
