package com.fitwise.specifications.jpa.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PackageDao {

    private Long subscriptionPackageId;

    private String title;

    private String status;

    private Long duration;

    private String imageUrl;

    private Long programCount;

    private Long sessionCount;

    private Double price;

}
