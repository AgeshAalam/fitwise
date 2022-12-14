package com.fitwise.view.payment.stripe;

import lombok.Getter;
import lombok.Setter;

/*
 * Created by Vignesh G on 18/12/20
 */
@Getter
@Setter
public class PackageSubscriptionModel {
    private String paymentMethodId;
    private Long subscriptionPackageId;
    private Long devicePlatformTypeId;
    private String existingOrderId;
    private String offerCode;
}
