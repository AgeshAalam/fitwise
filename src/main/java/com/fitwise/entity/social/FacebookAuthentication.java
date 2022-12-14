package com.fitwise.entity.social;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fitwise.entity.User;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class FacebookAuthentication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long facebookAuthenticationId;

    private String facebookUserAccessToken;

    @Transient
    private String facebookAppAccessToken;

    // Profile id which we will get from facebook
    private String facebookUserProfileId;

    @Transient
    private String firstName;

    @Transient
    private String lastName;

    private String userRole;

    private String email;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @JsonIgnore
    User user;

    /**
     * When a user has already registered into platform using a role and now now he is trying to login
     * as another role, a confirmation pop-up is asked in Client side apps to add new role and the confirmation value is set here
     * in the below boolean value
     */
    @Transient
    private Boolean isRoleAddPermissionEnabled;

}
