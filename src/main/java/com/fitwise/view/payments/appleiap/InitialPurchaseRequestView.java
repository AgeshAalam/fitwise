package com.fitwise.view.payments.appleiap;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InitialPurchaseRequestView {
	private String receiptdata;
	private String transactionId;
	private String programId;
	private String orderId;
	private String errMsg;
}
