package com.fitwise.view.payment.authorizenet;

import lombok.Getter;
import lombok.Setter;

/**
 * Class that holds the Payload data of Authorization capture response
 */

@Getter
@Setter
public class AuthCaptureWebHookPayload {
    private int responseCode;
    private String authCode;
    private String avsResponse;
    private double authAmount;
    private String invoiceNumber;
    private String entityName;
    private String id;
}
