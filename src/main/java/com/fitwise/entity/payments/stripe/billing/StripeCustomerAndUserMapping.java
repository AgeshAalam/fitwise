package com.fitwise.entity.payments.stripe.billing;

import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.User;
import com.stripe.model.Customer;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class StripeCustomerAndUserMapping extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private User user;

    private String StripeCustomerId;

}
