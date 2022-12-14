package com.fitwise.specifications.jpa.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SubscriptionCountDao {

    private Long stripeActiveSubscriptionCount;

    private Long iosActiveSubscriptionCount;


}
