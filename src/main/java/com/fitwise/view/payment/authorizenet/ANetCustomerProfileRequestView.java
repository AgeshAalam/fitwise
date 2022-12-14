package com.fitwise.view.payment.authorizenet;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ANetCustomerProfileRequestView {
    private Long programId;
    private Long devicePlatformId;
    private String customerProfileId;
    private String customerPaymentProfileId;
    private String customerAddressId;
    private String orderId;
}
