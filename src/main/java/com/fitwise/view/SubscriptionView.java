package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SubscriptionView {

	private Long user_id;
	
	private boolean isAutoRenewal = false;

	private Long program_id;

	private Long instructor_id;
	
	private Long subscription_type_id;
	
	private Long subscription_plan_id;
	
}
