package com.fitwise.view.payment.authorizenet;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostRefundReasonRequestView {
    private Long memberId;
    private Long refundReasonId;
    private String refundReason;
    private String transactionId;
    private String orderId;
}
