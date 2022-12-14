package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
public class V2LoginResponseView implements Serializable {

    private static final long serialVersionUID = -4799228476248247577L;

    private String authToken;

    private Long userId;

    private boolean isNewRolePrompt;

    private boolean hasFitwisePassword = false;

    private Date signUpDate;

    private boolean isSuperAdmin = false;

}
