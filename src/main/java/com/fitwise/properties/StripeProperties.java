package com.fitwise.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class StripeProperties {
    @Value("${stripe.api_key}")
    private String apiKey;

    @Value("${stripe.end_point_secret}")
    private String endPointSecret;

    @Value("${stripe.subscription.expiry.buffer.minutes}")
    private int subscriptionExpiryBufferMinutes;

    @Value("${stripe.refund.period.start.days}")
    private int refundPeriodStart;

    @Value("${stripe.refund.period.end.days}")
    private int refundPeriodEnd;

    /**
     * Should be in UTC
     */
    @Value("${payout.creation.start.time}")
    private String payoutCreationStartTime;

    /**
     * Should be in UTC
     */
    @Value("${payout.creation.end.time}")
    private String payoutCreationEndTime;

}
