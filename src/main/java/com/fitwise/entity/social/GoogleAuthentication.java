package com.fitwise.entity.social;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fitwise.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class GoogleAuthentication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long googleAuthenticationId;

    private String googleAuthenticationToken;

    private String clientId;

    @Transient
    private String firstName;

    @Transient
    private String lastName;
    
    private String userRole;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @JsonIgnore
    User user;

    /**
     * When a user has already registered into platform using a role, and now he is trying to log in
     * as another role, now a confirmation pop-up is asked from Client side and the confirmation value is set here
     * in the below boolean value
     */
    @Transient
    private Boolean isRoleAddPermissionEnabled;
}
