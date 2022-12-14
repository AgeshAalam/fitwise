package com.fitwise.specifications.jpa.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OfferDao {

    private long subscriptionPackageId;

    private long subscriptionCount;

    private String status;

    private long newOfferCount;

    private long existingOfferCount;

}
