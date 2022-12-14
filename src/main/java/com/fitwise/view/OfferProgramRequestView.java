package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OfferProgramRequestView {

    private Long programId;

    private List<Long> discountOffersIds;

}
