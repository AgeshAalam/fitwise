package com.fitwise.constants.payments.appleiap;

import org.springframework.stereotype.Component;

@Component
public class NotificationConstants {
	//public static final String VERIFICATION_URL="";	
	public static final String MODE_OF_PAYMENT="Apple";
	public static final Long APPLE_PLATFORM=(long)2;
	public static final String PDT_IDENTIFIER="productIdentifier";
	public static final Long DURATION=(long)30;
	//
	public static final String RECIEPT_DATA="receipt-data";
	public static final String RECIEPT_PD="password";
	public static final String EXCL_OLD_TXN="exclude-old-transactions";
	public static final String CONT_TYP="Content-type";
	public static final String JSON="application/json";
	
	/** Move this key value into table */
	//public static final String APP_SECRET_KEY="";
	public static final String INITIAL_BUY="INITIAL_BUY" ;
	public static final String DID_CHANGE_RENEWAL_STATUS="DID_CHANGE_RENEWAL_STATUS" ;
	public static final String DID_RECOVER="DID_RECOVER" ;
	public static final String DID_CHANGE_RENEWAL_PREF="DID_CHANGE_RENEWAL_PREF" ;
	public static final String INTERACTIVE_RENEWAL="INTERACTIVE_RENEWAL" ;
	public static final String CANCEL="CANCEL" ;
	public static final String DID_FAIL_TO_RENEW="DID_FAIL_TO_RENEW" ;
	public static final String RENEWAL="RENEWAL";
	public static final String REFUND="REFUND";
	public static final String DID_RENEW="DID_RENEW";
	//
	public static final String LATEST_RCPT="latest_receipt_info";
	public static final String LATEST_EXPIRED_RCPT="latest_expired_receipt_info";
	public static final String UNIFIED_RCPT="unified_receipt";
	public static final String PENDING_RENEWAL_INFO="pending_renewal_info";
	public static final String PRODUCT_ID="product_id";
	public static final String ORIGINAL_TRANSACTION_ID="original_transaction_id";
	public static final String TRANSACTION_ID="transaction_id";
	public static final String SUBSCRIPTION_ID="web_order_line_item_id";
	public static final String PURCHASE_DT="purchase_date_pst";
	public static final String EXPIRES_DT="expires_date_formatted_pst";
	public static final String EXPIRES_DT_PST="expires_date_pst";
	public static final String CANCEL_DT="cancellation_date_pst";
	public static final String CANCEL_REASON="cancellation_reason";
	public static final String AUTO_RENEW="auto_renew_status";
	public static final String AUTO_RENEW_DT="auto_renew_status_change_date_pst";
	public static final String IS_BILLING_RETRY="is_in_billing_retry_period";
	public static final String IS_TRIAL="is_trial_period";
	public static final String IS_IN_INTRO_OFFER="is_in_intro_offer_period";
	public static final String EXPIRANT_INTENT="expiration_intent";
	public static final String RCPT_ST="status";
	public static final String NOTIFICATION="notification_type";
	public static final String AUTO_RENEW_PDT="auto_renew_product_id";
	public static final String RECEIPT="receipt";
	public static final String IN_APP_ARRAY="in_app";
	public static final String LATEST_RCPT_DATA="latest_receipt";
	public static final String LATEST_EXP_RCPT_DATA="latest_expired_receipt";
	public static final String ENVIRONMENT="environment";
	public static final String ENV_SANDBOX="Sandbox";
	public static final String ENV_PROD="Production";
	
	public static final String CANCEL_0="Transaction was canceled for another reason.";
	public static final String CANCEL_1="Customer cancelled their transaction due to an actual or perceived issue within your app.";
	
	public static final String EXPIRANT_1="The customer voluntarily canceled their subscription.";
	public static final String EXPIRANT_2="Billing error. The customer's payment information was no longer valid.";
	public static final String EXPIRANT_3="The customer did not agree to a recent price increase.";
	public static final String EXPIRANT_4="The product was not available for purchase at the time of renewal.";
	public static final String EXPIRANT_5="Unknown error.";
	public static final String APPLE_TRANSACTION_SUCCESS_RESPONSE_CODE = "0";
	public static final String ORD_RETRY = "Payment has been failed.Retry Payment for this Order. ";
	public static final String REC_ERR ="Receipt Data not found for the given User and Program";
	public static final String REN_ON="Auto Renewal On";
	public static final String REN_OFF="Auto Renewal Off";
	
	public static final String VALUE_ENDPOINT="/v1/payment/iap/notification/redirectNotificationfromAppstore";	
	public static final String DEV_PDT="com.fitwise.trainnr.member.dev";
	public static final String QA_PDT="com.fitwise.trainnr.member.qa";
	public static final String STG_PDT="com.fitwise.trainnr.member.stg";
	public static final String PRD_PDT="com.fitwise.trainnr.member.prd";
	// Offer Identifier
	public static final String OFFER_ID="promotional_offer_id";
	public static final String OFFER_NAME="offer_code_ref_name";
	
}
