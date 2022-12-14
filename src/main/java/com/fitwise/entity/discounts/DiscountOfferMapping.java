package com.fitwise.entity.discounts;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.Programs;
import com.fitwise.entity.Workouts;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class DiscountOfferMapping extends AuditingEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long offerMappingId;	
	
	private Long instructorId;
	
	@JsonIgnore
	@OneToOne
	@JoinColumn(name="levelId")
	private DiscountLevel levelMapping;

	
	/** The OfferCodeDetail. */
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
	@JoinColumn(name = "offerCodeId")
	private OfferCodeDetail offerCodeDetail;

	/** The programs. */
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
	@JoinColumn(name = "programId")
	private Programs programs;
	
	private Boolean needDiscountUpdate;
	
	private String discountStatus;
	
	private Boolean needMailUpdate;

}
