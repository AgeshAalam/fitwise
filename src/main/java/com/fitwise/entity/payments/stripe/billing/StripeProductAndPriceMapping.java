package com.fitwise.entity.payments.stripe.billing;

import com.stripe.model.Price;
import com.stripe.model.Product;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * A mapping table that consists of the relation between Stripe product and price mappings
 */
@Entity
@Getter
@Setter
public class StripeProductAndPriceMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String productId;

    private String priceId;

    private Double price;

}
