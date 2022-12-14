package com.fitwise.model;

import lombok.Data;

@Data
public class TierModel {
	
	private Long tierId;
	
	private String tierType;
	
	private Boolean isActive;
	
	private Long tierTypeId;

	private Double monthlyCost;

	private String chargeFrequency;

	private Double minimumCommitment;

	private String cancellationPolicy;

	private Double programsFees;

	private Double programsPackagesFees;

	private Double servicesPackagesFees;

	private Double packagesFees;

	private String directoryLeads;
	
	private String communityNewsletter;

	private String logoCreation;

	private String accountCreationHelp;
	
	private String dedicatedAccountManager;

	private String adCreationHelp; 

}
