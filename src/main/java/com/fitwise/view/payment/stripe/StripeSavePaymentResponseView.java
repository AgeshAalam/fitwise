package com.fitwise.view.payment.stripe;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/*
 * Created by Vignesh.G on 05/04/21
 */
@Setter
@Getter
public class StripeSavePaymentResponseView {

    private Long userId;
    private String customerId;
    private StripePaymentMethodResponseView savedCard;

}
