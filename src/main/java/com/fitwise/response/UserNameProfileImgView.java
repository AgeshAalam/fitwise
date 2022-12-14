package com.fitwise.response;

import lombok.Data;

/*
 * Created by Vignesh G on 04/07/20
 */
@Data
public class UserNameProfileImgView {

    private Long userId;

    private String userName;

    private String profileImageUrl;

}
