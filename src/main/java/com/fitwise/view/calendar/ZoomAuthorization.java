package com.fitwise.view.calendar;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZoomAuthorization {

    private boolean authenticated;

    private String authorizationUrl;

    private String accountEmail;

    private String accountId;

    private boolean isActive;
}
