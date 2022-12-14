package com.fitwise.response.packaging;

import lombok.Data;

import java.util.List;

@Data
public class SubscriptionPackageResponseViewForDiscover {

    private List<SubscriptionPackageTileViewForDiscover> subscriptionPackages;

    private int totalCount;
}
