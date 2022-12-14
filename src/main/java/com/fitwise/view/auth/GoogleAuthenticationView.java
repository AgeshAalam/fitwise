package com.fitwise.view.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleAuthenticationView {

    private String googleAuthenticationToken;
    private String clientId;
    private String firstName;
    private String lastName;
    private String userRole;
    private Boolean isRoleAddPermissionEnabled;

}
