package com.fitwise.entity.payments.credits;

import com.fitwise.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class InstructorPaymentCredits {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User instructor;

    private Double totalCredits;

    private String currencyType;

    private String stripeAccountId;

}
