package com.fitwise.entity.payments.appleiap;

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
public class IntroOfferUserTracking extends AuditingEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	private Long userId;

	private Long programId;
	
	private Long offerCodeId;
	
	//Set true if the user availed intro offer
	private Boolean isAvailedIntroOffer;
	
	private int offerDuration; //in Months
}
