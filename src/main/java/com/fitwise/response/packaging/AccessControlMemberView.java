package com.fitwise.response.packaging;

import lombok.Data;

/*
 * Created by Vignesh G on 05/10/20
 */
@Data
public class AccessControlMemberView {

    private Long userId;

    private String name;

    private String imageUrl;

    private String email;


}
