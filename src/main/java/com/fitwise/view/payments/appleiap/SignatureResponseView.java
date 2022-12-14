package com.fitwise.view.payments.appleiap;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignatureResponseView {
	 private String bundleIdentifier;
	 private String keyIdentifier;
	 private String productIdentifier;
	 private String offerIdentifier;
	 private String applicationUsername;
	 private UUID nonce; 
	 private Long timestamp;	 
	 private String signature;
}
