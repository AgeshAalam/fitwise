package com.fitwise.entity.payments.stripe.connect;

import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class StripeAccountAndUserMapping extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stripeAccountId = "";

    @ManyToOne
    private User user;

    private Boolean isOnBoardingCompleted;

    private Boolean isDetailsSubmitted = false;
}
