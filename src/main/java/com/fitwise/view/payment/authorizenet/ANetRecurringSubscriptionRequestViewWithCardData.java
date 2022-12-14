package com.fitwise.view.payment.authorizenet;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ANetRecurringSubscriptionRequestViewWithCardData {
    private Long programId;
    /*
     * The token which the mobile SDK get while posting transaction card data to Authorize.net.
     * form token is valid only for 15 minutes
     */
    private String formToken;
    private Long devicePlatformTypeId;
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String country;
    private String orderId; // Only be sent if the payment has been declined for the first time and re-order is being triggered
}
