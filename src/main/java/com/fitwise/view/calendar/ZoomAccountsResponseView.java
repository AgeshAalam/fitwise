package com.fitwise.view.calendar;

import lombok.Data;

@Data
public class ZoomAccountsResponseView {

    private String accountEmail;

    private String accountId;

    private boolean isActive;
}
