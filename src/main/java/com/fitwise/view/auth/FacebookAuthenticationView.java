package com.fitwise.view.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FacebookAuthenticationView {

    private String facebookUserAccessToken;
    private String facebookAppAccessToken;
    private String facebookUserProfileId;
    private String firstName;
    private String lastName;
    private String userRole;
    private String email;
    private Boolean isRoleAddPermissionEnabled;
}
