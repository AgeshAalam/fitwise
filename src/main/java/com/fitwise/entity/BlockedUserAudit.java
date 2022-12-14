package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Setter
@Getter
public class BlockedUserAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long blockedUserAuditId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "user_role_id")
    private UserRole userRole;

    private String status;
    private Date happenedDate;

    @ManyToOne
    @JoinColumn(name = "done_by")
    private User doneBy;
}
