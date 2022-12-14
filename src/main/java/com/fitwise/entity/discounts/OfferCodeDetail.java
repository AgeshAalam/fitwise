package com.fitwise.entity.discounts;

import java.util.Date;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.ProgramPrices;

import com.fitwise.entity.User;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class OfferCodeDetail extends AuditingEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long offerCodeId;
	
	//Offer Reference Name
	private String offerName;
	
	//Apple Promotional Offer Reference Name
	private String appleOfferName;
	
	//Promotional Offer Product Code 
	private String offerCode;
	
	// Promotional Offer Duration
	@JsonIgnore
	@OneToOne
	@JoinColumn(name = "durationId")
	private OfferDuration offerDuration; //static table mapping
	
	// Number of Periods (Default :1)
	private int offerDurationCount;

	// Offer Availability Period
	private Date offerStartingDate;
	
	private Date offerEndingDate;
	
	// Mode :Free or Pay As You Go
	private String offerMode;
	
	//Price
	@OneToOne
	@JoinColumn(name = "program_price_id")
	private ProgramPrices offerPrice;
	
	private Boolean isNewUser;

	private String offerStatus;

	private boolean isInUse;

	private Date discardDate;

	@ManyToOne
	@JoinColumn(name = "owner_user_id")
	private User owner;

}
