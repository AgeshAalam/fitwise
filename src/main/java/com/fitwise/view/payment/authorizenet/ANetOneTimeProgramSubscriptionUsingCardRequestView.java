package com.fitwise.view.payment.authorizenet;

import com.fitwise.entity.PlatformType;
import lombok.Getter;
import lombok.Setter;

import javax.print.DocFlavor;


/**
 * Request class for initiating one time payment in Authorize.net
 */

@Setter
@Getter
public class ANetOneTimeProgramSubscriptionUsingCardRequestView {
    private Long programId;

    private Long devicePlatformTypeId;

    /*
     * The token which the mobile SDK get while posting transaction card data to Authorize.net.
     * form token is valid only for 15 minutes
     *
     * User will only get this in-case of Onetime subscription and not for Auto-subscription
     */
    private String formToken;

    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String state;
    private String zip;
    private boolean doSaveCardData;
    private String orderId;
    private String country;

}

