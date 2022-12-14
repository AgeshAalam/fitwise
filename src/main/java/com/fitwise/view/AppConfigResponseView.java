package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AppConfigResponseView {

    private String appVersion;
    private boolean isAppForceLogout;
    private boolean isAppForceUpdate;
    private String faqUrl;
    private String termsAndCondUrl;
    private String privacyPolicyUrl;
    private String supportEmail;
    private String supportPhone;
    private boolean isUserPhoneNumberExists = false;
    private String transitionVideoUrl;
    private String introVideoUrl;
    private boolean isOnboardingCompleted;
    private Date fitwiseLaunchDate;
}