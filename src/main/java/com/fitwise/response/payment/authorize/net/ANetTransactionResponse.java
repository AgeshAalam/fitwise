package com.fitwise.response.payment.authorize.net;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ANetTransactionResponse {
    private String responseCode;
    private String transactionId;
    private String transactionStatus;
    private String errorCode;
    private String errorMessage;
    private String eventType;
    private CustomerProfile customerProfile;
    private String countryCode;
}
