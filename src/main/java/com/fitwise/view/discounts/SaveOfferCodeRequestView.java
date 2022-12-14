package com.fitwise.view.discounts;

import java.util.Date;
import java.util.Optional;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveOfferCodeRequestView {
	private Boolean isNewUser;
	private Date offerStartDate;
	private Date offerEndDate;
	private String offerCode;
	private String offerName;
	private int durationId;
	private Long offerPriceId;
	private Long programId;
	private String mode;
	private Long packageId;

}
