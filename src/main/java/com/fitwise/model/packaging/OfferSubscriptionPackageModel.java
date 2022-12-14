package com.fitwise.model.packaging;

import lombok.Data;

import java.util.List;

@Data
public class OfferSubscriptionPackageModel {

    private Long subscriptionPackageId;

    private List<Long> discountOffersIds;
}
