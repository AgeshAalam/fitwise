package com.fitwise.entity.payments.appleiap;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fitwise.entity.AuditingEntity;

import com.fitwise.entity.payments.common.OrderManagement;

import lombok.Getter;
import lombok.Setter;
@Entity
@Getter
@Setter
public class ApplePayment extends AuditingEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "order_id")
	private OrderManagement orderManagement; // Will be sent as reference key in AuthNet request during transaction
												// initiation
	private String receiptNumber; // Fitwise generated InvoiceNumber
		
	private String transactionId; // Transaction id from Apple	
	
	private String originalTransactionId;
	
	private String subscriptionId; // web_order_line_item_id
	
	private Boolean isAutoRenew;
	
	private String statusCode;
	
	private String transactionStatus;

	private String errorMessage;
	
	private Date purchaseDate;
	
	private Date expiryDate;
	
	@Column(name = "isPaymentSettled", columnDefinition = "boolean default false")
	private Boolean isPaymentSettled ;

	private Double programPrice;
}
