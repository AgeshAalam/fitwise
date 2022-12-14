package com.fitwise.view.payments.appleiap;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InitialPurchaseResponseView {	
	private String productId;
	private String transactionId;
	private String originalTransactionId;
	private Date purchaseDate;
	private String purchaseDateFormatted;
	private Date expiresDate;
	private String expiresDateFormatted;
	private String webOrderLineitemId;
	private String isAutorenew;		
	private String orderId;
}
