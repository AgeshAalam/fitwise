package com.fitwise.entity.calendar;

import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
public class ZoomAccount extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long zoomAccountId;

    private String userId;

    private String accountId;

    private String accountEmail;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String accessToken;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String refreshToken;

    private Date tokenExpirationTime;

    private String state;

    private boolean active;

    private String error;

    @OneToOne(fetch = FetchType.LAZY)
    private User user;

}
