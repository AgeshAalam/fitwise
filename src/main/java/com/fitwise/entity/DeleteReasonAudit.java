package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
public class DeleteReasonAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deleteReasonAuditId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "delete_reasons_id")
    DeleteReasons deleteReasons;

    @ManyToOne
    @JoinColumn(name = "user_role")
    UserRole userRole ;
}
