package com.fitwise.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@Getter
public class AppleProperties {
	
	@Value("${app.secret.key}")
	private String appSecretKey; 
	
	@Value("${validate.receipt.endpoint}")
	private String verifyReceiptEndPoint; 

	 @Value("${apple.api.transaction.env}")
	 private String environment;
	 
	 @Value("${iap.itms.appstore.notify.emailaddress}")
	 public String itmsNotificationToEMailAddress;
	 
	 @Value("${iap.appstore.dev.notification.url}")
	 public String notificationToDev;
	 
	 @Value("${iap.appstore.qa.notification.url}")
	 public String notificationToQa;
	 
	 @Value("${iap.appstore.stg.notification.url}")
	 public String notificationToStg;
	 
	 @Value("${iap.appstore.prd.notification.url}")
	 public String notificationToPrd;
	 
	 @Value("${discounts.appstore.private.key.identifier}")
	 public String keyIdentifier;
}
