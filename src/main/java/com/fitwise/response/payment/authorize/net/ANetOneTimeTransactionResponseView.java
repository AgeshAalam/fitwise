package com.fitwise.response.payment.authorize.net;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ANetOneTimeTransactionResponseView {
    private String responseCode;
    private String transactionId;
    private String transactionStatus;
    private String ANetCustomerProfileId;
}
