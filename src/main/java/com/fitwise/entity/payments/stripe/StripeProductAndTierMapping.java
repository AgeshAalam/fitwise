package com.fitwise.entity.payments.stripe;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fitwise.entity.instructor.Tier;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class StripeProductAndTierMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long stripeProductMappingId;

	@ManyToOne
	private Tier tier;

	private String stripeProductId;

	private boolean isActive;
}
