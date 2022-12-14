package com.fitwise.entity;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.CreationTimestamp;

import lombok.Getter;
import lombok.Setter;

/**
 * The Class UserForgotPasswordToken.
 */
@Entity
@Getter
@Setter
public class UserForgotPasswordToken {

    /** The user forgot password id. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userForgotPasswordId;

    /** The reset token. */
    private String resetToken;

    /** The user. */
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    /** The expiry date. */
    @CreationTimestamp
    private Timestamp expiryDate;
}
