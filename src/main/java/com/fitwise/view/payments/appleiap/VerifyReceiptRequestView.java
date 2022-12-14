package com.fitwise.view.payments.appleiap;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyReceiptRequestView {
	private String programId;
	private String receiptdata;
}
