package com.fitwise.view.payment.authorizenet;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ANetRecurringSubscriptionRequestViewWithPaymentProfile {
    private Long programId;
    private Long devicePlatformId;
    private String customerProfileId;
    private String customerPaymentProfileId;
    private String customerAddressId;
    private String orderId; // Only be sent if the payment has been declined for the first time and re-order is being triggered
}