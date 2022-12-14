package com.fitwise.view;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BasicDetailsRequestView {

    private Long userId;
    private String firstName;
    private String lastName;
    private String contactNumber;
    private String about;
    private Long genderId;
    private String countryCode;
    private String isdCode;
    private String isdCountryCode;
}
