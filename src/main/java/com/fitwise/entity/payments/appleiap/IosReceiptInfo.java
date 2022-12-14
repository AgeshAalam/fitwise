package com.fitwise.entity.payments.appleiap;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.CreationTimestamp;

import com.fitwise.entity.User;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class IosReceiptInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private int id;
	
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "user_id")
	private User user;
	
	@Column(name="program_id")
	private Long programId ;
	
	/** Subscription ID*/
	@Column(name="web_order_line_item_id")
	private String weborderLineitemId;
	
	@Column(name="transaction_id")
	private String transactionId; 
	
	@Column(name="original_transaction_id")
	private String originalTransactionId; 
	
	@Column(name="auto_renew_status")
	private Boolean autoRenewStatus;
	
	@Column(name="autorenew_status_change_date")
	private Date autorenewStatusChangeDate;
	
	@Column(name="purchase_date")
	private Date purchaseDate;
	
	@Column(name="expires_date")
	private Date expiresDate;
	
	@Column(name="cancellation_date")
	private Date cancellationDate;
	
	@Column(name="is_trial_period")
	private Boolean isTrialPeriod;
	
	@Column(name="is_in_intro_offer_period")
	private Boolean isInIntroOffer;
		
	@Column(name="is_in_billing_retry_period")
	private Boolean billingRetry;
	
	@Column(name="expiration_intent")
	private String expirationIntent;
	
	@Column(name="status")
	private String status ; 
	
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "subscription_status_name")
    private AppleSubscriptionStatus subscriptionStatus ;
	
	@Column(name="notificationType")
	private String notificationType ; 
	
	@Column(name="message")
	private String message ; 
	
	@Column(name="receipt_data", columnDefinition = "MEDIUMTEXT")
	private String receiptData ;
	
	@Column(name="latest_receipt_data", columnDefinition = "MEDIUMTEXT")
	private String latestReceiptData ;
	
	@Column(name = "created_date", updatable = false)
	@CreationTimestamp
	private Date createdDate;

	private String OfferName ;
	
	private String offerId ;
}
