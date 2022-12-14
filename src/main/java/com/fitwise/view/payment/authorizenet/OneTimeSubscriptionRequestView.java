package com.fitwise.view.payment.authorizenet;

import lombok.Getter;
import lombok.Setter;


/**
 * The Payment Initiation request class
 */
@Getter
@Setter
public class OneTimeSubscriptionRequestView {
    private Long programId;
    private Double subscriptionAmount;
    /*
     * The token which the mobile SDK get while posting transaction card data to Authorize.net.
     * form token is valid only for 15 minutes
     */
    private String formToken;
    private String clientPlatformType;
}
