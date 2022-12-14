package com.fitwise.entity.instructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class TierTypeDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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
	
	private int durationInMonths;
	
	private int durationInDays;

}
