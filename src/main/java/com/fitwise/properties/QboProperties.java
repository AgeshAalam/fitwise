package com.fitwise.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class QboProperties {

    @Value("${qbo.refund.fixed.account.price}")
    private double refundFixedPrice;

    @Value("${qbo.duedate.authnet.buffer.days}")
    private int authnetDateDateLogicBufferInDays;

    @Value("${qbo.duedate.apple.buffer.days}")
    private int appleDateDateLogicBufferInDays;

    @Value("${qbo.welcome.bill.cost}")
    private double vendorWelcomePrice;

    @Value("${qbo.welcome.bill.duedays}")
    private int vendorWelcomeBillDueDays;

    @Value("${fitwise.share}")
    private int fitwiseShare;

    @Value("${qbo.stripe.duedate.buffer}")
    private int dueDateBufferStripe;
}
