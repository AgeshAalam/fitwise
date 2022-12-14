package com.fitwise.entity.payments.appleiap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter

public class AppInformation {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="app_id")
	private int id;
	
	private String providerId;
	
	private String teamId;
	
	private String vendorId;
	
	private String appleId;
	
	private String appTitle;
	
	private String softwareVersion;
	
	@Column(name = "itune_package")
	private String iTunepackage;
	
	@Column(name = "app_shared_sec_key")
	private String appSharedSecretKey;
	
	@Column(columnDefinition = "MEDIUMTEXT")
	private String description;
}
