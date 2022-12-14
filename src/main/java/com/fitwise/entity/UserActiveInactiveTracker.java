package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/*
 * Created by Vignesh G on 23/03/20
 */
@Entity
@Getter
@Setter
@Table(name = "user_active_inactive_tracker", indexes = {
        @Index(name = "index_id", columnList = "id", unique = true),
        @Index(name = "index_user", columnList = "id, user_id"),
        @Index(name = "index_user_role", columnList = "id, user_id, role_id")
})
public class UserActiveInactiveTracker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private UserRole userRole;

    private boolean isActive;

    @Column(name = "modified_date")
    @UpdateTimestamp
    private Date modifiedDate;

}