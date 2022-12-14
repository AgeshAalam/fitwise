package com.fitwise.specifications.jpa.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProgramOfferDao {

    private long programId;

    private long subscriptionCount;

    private Long platformTypeId;

    private String stripeSubscriptionStatus;

    private String iosSubscriptionStatus;

    private long newOfferCount;

    private long existingOfferCount;

}
