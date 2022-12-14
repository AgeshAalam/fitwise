package com.fitwise.response;

import lombok.Data;

@Data
public class MemberLocationResponse {

    private Long locationId;

    private String locationType;

    private String address;

    private String landmark;

    private String city;

    private String state;

    private String zipcode;

    private String country;

}
