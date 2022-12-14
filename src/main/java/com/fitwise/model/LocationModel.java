package com.fitwise.model;

import lombok.Data;

@Data
public class LocationModel {

    private Long locationId;

    private Long locationTypeId;

    private String address;

    private String landmark;

    private String city;

    private String state;

    private String zipcode;

    private Long countryId;

    private boolean isDefault;
}
