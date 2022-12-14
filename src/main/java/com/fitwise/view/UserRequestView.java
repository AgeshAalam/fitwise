package com.fitwise.view;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NonNull
public class UserRequestView {
    private Long userId;
    private String firstName = "";
    private String lastName = "";
    private String biography = "";
    private long genderId;
    private String dob = "";
    private Boolean notificationStatus;
    private String contactNumber = "";
    private String countryCode;

}
