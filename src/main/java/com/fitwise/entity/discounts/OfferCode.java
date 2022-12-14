package com.fitwise.entity.discounts;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fitwise.entity.AuditingEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class OfferCode extends AuditingEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	private String offerCodeName;
	
	private String status; //used or unused

}
