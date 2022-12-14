package com.fitwise.entity.view;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "view_instructor")
public class ViewInstructor {

    @Id
    @Column(name = "user_id")
    private Long userId;

    private String imageUrl;

    private String name;

    private Double outstandingBalance;

    private Long publishedPackages;

    private Long activePackageSubscriptions;

    private Long publishedPrograms;

    private Long activeProgramSubscriptions;

    private Long exercises;

    private Date onboardedOn;

    private Boolean blocked;

    private String onboardedPaymentMode;

    private Date lastUserAccess;

    private String email;

    private String contactNumber;

    private String tierType;

}
