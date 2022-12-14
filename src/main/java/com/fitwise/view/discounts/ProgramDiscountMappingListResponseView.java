package com.fitwise.view.discounts;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgramDiscountMappingListResponseView {
	List<ProgramDiscountMappingResponseView> currentDiscounts;
	List<ProgramDiscountMappingResponseView> upcomingDiscounts;
	List<ProgramDiscountMappingResponseView> expiredDiscounts;
}
