package com.fitwise.view.payment.authorizenet;

import lombok.Getter;
import lombok.Setter;

/**
 * Class that holds the response structure of Refund-Created event response from Authorize.net
 */
@Getter
@Setter
public class RefundCreatedResponseView extends AuthNetWebHookParentResponseView {
    private RefundCreatedPayload payload;
}
