package com.fitwise.entity.payments.stripe.billing;

import com.fitwise.entity.Programs;
import com.stripe.model.Product;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class StripeProductAndProgramMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stripeProductMappingId;

    @ManyToOne
    private Programs program;

    private String stripeProductId;

    private boolean isActive;

}
