package com.fitwise.view.socialLogin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppleLoginRequestView {
    private String appleUserId;
    private String email;
    private String userRole;
    private String firstName;
    private String lastName;
    private Boolean isAuthenticatedToAddNewRole;
    private String appleAuthorizationToken;
    private Long platformTypeId;
}
