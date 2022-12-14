package com.fitwise.response;

import com.fitwise.response.packaging.SubscriptionPackageTileView;
import lombok.Data;

import java.util.List;

@Data
public class AdminPackageView {

    private int totalSubscriptions;

    private List<SubscriptionPackageTileView> subscriptionPackages;

    private long totalPackageCount;

    private double netRevenue;

    private String netRevenueFormatted;
}
