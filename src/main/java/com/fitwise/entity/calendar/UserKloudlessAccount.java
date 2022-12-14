package com.fitwise.entity.calendar;

import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class UserKloudlessAccount extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userKloudlessTokenId;

    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    private String token;

    private String accountId;

    private String accountEmail;

    private String service;

    private boolean active;
    
    private String tokentype;
    
    private Integer expiresin;

    private String refreshToken;

    private String scope;

    private String sub;

    private String profileId;
    
    private String providerName;
}
