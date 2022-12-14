package com.fitwise.response.payment.authorize.net;

import lombok.Getter;
import lombok.Setter;

/**
 * Class that is used to provide the payload response for Authorize.net payment request
 */

@Getter
@Setter
public class AuthNetPayloadResponseView {
    /*
     * Transaction's response id
     */
    private String transactionId;

    private String transactionMessage;
}
