package com.fitwise.response.payment.authorize.net;

import com.fitwise.view.payment.authorizenet.ANetBillingAddressView;
import com.fitwise.view.payment.authorizenet.ANetSubscriptionId;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PaymentProfile {
    private String paymentProfileId;
    private String maskedCardNumber;
    private String maskedExpirationDate;
    private String cardType;
    private ANetBillingAddressView billTo;
    private List<ANetSubscriptionId> subscriptionIds;

}
