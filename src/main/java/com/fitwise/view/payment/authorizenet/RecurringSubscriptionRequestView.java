package com.fitwise.view.payment.authorizenet;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecurringSubscriptionRequestView {
    private Long programId;
    private Double subscriptionAmount;
    /*
     * The token which the mobile SDK get while posting transaction card data to Authorize.net.
     * form token is valid only for 15 minutes
     */
    private short length; // Either 7-365 for Days and 1-12 for Months
    private String unit; // Must be either days or months
    private String firstName;
    private String lastName;
    private String cardNumber;
    private String setExpirationDate; // Should be in "MMDD" format. for example., 12/21 should be sent as 1221
    private Long subscriptionPlanId;
    private Long subscriptionStatusId;
    private Long devicePlatformTypeId;
}
