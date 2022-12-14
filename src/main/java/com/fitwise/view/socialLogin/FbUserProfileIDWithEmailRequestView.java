package com.fitwise.view.socialLogin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FbUserProfileIDWithEmailRequestView {
    private String facebookUserProfileId;
    private String userRole;
    private String facebookUserAccessToken;
    private String facebookAppAccessToken;
    private String email;
    private int otp;
    private String firstName;
    private String lastName;
}
