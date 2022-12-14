package com.fitwise.response;

import lombok.Data;

@Data
public class LocationResponse {

    private Long locationId;

    private Long locationTypeId;

    private String locationType;

    private String address;

    private String landmark;

    private String city;

    private String state;

    private String zipcode;

    private Long countryId;

    private String country;

    private boolean isDefault;

    private boolean isUsedInPackage;
}
