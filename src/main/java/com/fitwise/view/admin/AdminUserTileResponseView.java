package com.fitwise.view.admin;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AdminUserTileResponseView {

    private Long userId;

    private String email;

    private String role;

    private Date onboardedDate;

    private String onboardedDateFormatted;

    private Date lastAccessDate;

    private String lastAccessDateFormatted;

}
