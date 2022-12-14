package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Setter
@Getter
@Table(name = "blocked_user", indexes = {
        @Index(name = "index_id", columnList = "blocked_user_id", unique = true),
        @Index(name = "index_user", columnList = "blocked_user_id, user_id"),
        @Index(name = "index_user_role", columnList = "blocked_user_id, user_id, user_role_id")
})
public class BlockedUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blocked_user_id")
    private Long blockedUserId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "user_role_id")
    private UserRole userRole;

    private Date blockedUserDate;

    @ManyToOne
    @JoinColumn(name = "who_blocked")
    private User whoBlocked;
}
