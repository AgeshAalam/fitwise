package com.fitwise.view.payment.authorizenet;

import com.fitwise.response.payment.authorize.net.PaymentProfile;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetCustomerProfileResponseView {
    private String customerProfileId;
    private String merchantCustomerId;
    List<PaymentProfile> paymentProfileList;
}
