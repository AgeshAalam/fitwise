package com.fitwise.response.packaging;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PackageTileView {

    private Long subscriptionPackageId;
    private String title;
    private String imageUrl;
    private Long duration;
    private String status;
    private Double price;
    private String priceFormatted;
    private int numberOfCurrentAvailableOffers;
    private boolean freeToAccess = false;

}