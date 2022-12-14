package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Setter
@Getter
public class DeletedUserAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deletedUserAuditId;

    @ManyToOne( cascade = CascadeType.MERGE , fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne( cascade = CascadeType.MERGE , fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private UserRole userRole;

    private Date happenedDate;

    @ManyToOne( cascade = CascadeType.MERGE , fetch = FetchType.LAZY)
    @JoinColumn(name = "done_by")
    private User doneBy;
}
