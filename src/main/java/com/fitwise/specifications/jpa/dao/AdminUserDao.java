package com.fitwise.specifications.jpa.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class AdminUserDao {

    private Long userId;

    private String email;

    private String userRole;

    private Date onboardedDate;

    private Date lastAccessDate;

}
