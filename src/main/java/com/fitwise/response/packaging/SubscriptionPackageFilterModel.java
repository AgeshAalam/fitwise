package com.fitwise.response.packaging;

import lombok.Data;

import java.util.List;

@Data
public class SubscriptionPackageFilterModel {

    private List<PackageFilterView> filterData;
}
