package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequestView {

    private String currentPassword;

    private String newPassword;

}
