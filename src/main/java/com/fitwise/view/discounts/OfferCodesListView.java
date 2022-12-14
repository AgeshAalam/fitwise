package com.fitwise.view.discounts;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OfferCodesListView {
	
	private List<SaveOfferCodeResponseView> expiredOffers;
	
	private List<SaveOfferCodeResponseView> currentOffers;
	
	private List<SaveOfferCodeResponseView> upComingOffers;
	
}
