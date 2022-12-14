package com.fitwise.view.payment.stripe;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateStripeCustomerRequestView {
    private String paymentMethodId;
    private Long programId;
    private Long devicePlatformTypeId;
    private String existingOrderId;
    private String offerCode;
    private Long tierId;

}

