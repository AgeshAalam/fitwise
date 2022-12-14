package com.fitwise.view.discounts;


import com.fitwise.constants.discounts.DiscountsConstants;
import com.fitwise.entity.ProgramPrices;
import com.fitwise.entity.discounts.OfferDuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveOfferCodeResponseView implements Comparable<SaveOfferCodeResponseView> {

	private Long offerCodeId;
	private String offerName;	
	private String offerCode;
	private OfferDuration offerDuration;
	private String offerStartDate;
	private String offerEndDate;
	private String offerMode;
	private ProgramPrices offerPrice;
	private String formattedOfferPrice;
	private Boolean isNewUser;	
	private String offerStatus;
	private String formattedSavingsAmount;
	
	@Override
	public int compareTo(SaveOfferCodeResponseView offer) {

		if (offer.getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_PAY_AS_YOU_GO)) {
			if (this.offerPrice.getProgramPricesId() != null && offer.getOfferPrice().getProgramPricesId() != null
					&& this.offerPrice.getProgramPricesId().equals(offer.getOfferPrice().getProgramPricesId())) {
				return Integer.valueOf(this.offerDuration.getDurationId())
						.compareTo(Integer.valueOf(offer.getOfferDuration().getDurationId()));
			}
			return this.offerPrice.getProgramPricesId().compareTo(offer.getOfferPrice().getProgramPricesId());

		} else if (offer.getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_FREE)) {
			return Integer.valueOf(this.offerDuration.getDurationId())
					.compareTo(Integer.valueOf(offer.getOfferDuration().getDurationId()));
		}
		return this.offerPrice.getProgramPricesId().compareTo(offer.getOfferPrice().getProgramPricesId());
	}
}
