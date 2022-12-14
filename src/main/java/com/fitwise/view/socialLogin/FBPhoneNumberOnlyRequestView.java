package com.fitwise.view.socialLogin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FBPhoneNumberOnlyRequestView {
    private String facebookUserProfileId;
    private String userRole;
    private String facebookUserAccessToken;
    private String facebookAppAccessToken;
}
