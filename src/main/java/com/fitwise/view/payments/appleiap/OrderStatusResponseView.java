package com.fitwise.view.payments.appleiap;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderStatusResponseView {

	private Long programId;
	private Long userId;
	private String orderId;
	private String orderStatus;
}
