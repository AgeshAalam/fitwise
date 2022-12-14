package com.fitwise.entity.payments.appleiap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter

public class InitialPricing {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="pricing_id")
	private int id;	
	
	@Column(name = "country")
	private String country;
	
	@Column(name = "territory")
	private String territory;
	
	@Column(name = "dimension")
	private String dimension;
	
	@Column(name = "price")
	private String price;
	
	@Column(name = "tier")
	private String tier;
}
