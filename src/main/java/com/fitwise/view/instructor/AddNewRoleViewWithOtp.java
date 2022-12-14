package com.fitwise.view.instructor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddNewRoleViewWithOtp {

    private String email;
    private String password;
    private String role;
    private int otp;
}
