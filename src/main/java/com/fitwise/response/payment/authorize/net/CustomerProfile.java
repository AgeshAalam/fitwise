package com.fitwise.response.payment.authorize.net;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CustomerProfile {
    private String customerProfileId;
    private List<PaymentProfile> paymentProfileList;
}
