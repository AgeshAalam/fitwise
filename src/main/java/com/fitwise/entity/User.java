package com.fitwise.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fitwise.entity.social.AppleAuthentication;
import com.fitwise.entity.social.FacebookAuthentication;
import com.fitwise.entity.social.GoogleAuthentication;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "user")
public class User extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    private String email;

    private String password;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user")
    Set<GoogleAuthentication> googleAuth;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user")
    Set<FacebookAuthentication> facebookAuth;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user")
    Set<AppleAuthentication> appleAuth;

    @Transient
    @JsonIgnore
    private String passwordConfirm;

    @JsonIgnore
    private boolean isNewSocialRegistration = false;

    /**
     * isFitwisePassword - Boolean to check whether the user has entered
     * password for the Fitwise platform. Note that random password is
     * generated and set for Social login to bypass Spring security
     */
    @JsonIgnore
    private boolean isEnteredFitwisePassword = false;

    /*@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "user_role_mapping", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "role_id"))
    private Set<UserRole> role;*/

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user")
    private List<UserRoleMapping> userRoleMappings = new ArrayList<>();

}