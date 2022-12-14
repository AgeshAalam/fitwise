package com.fitwise.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class AppVersionInfo {
	
	/** The app version id. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int appVersionId;
	
	/** The app version. */
	private String appVersion;
	
	/** The app platform. */
	private String appPlatform;
	
	/** The is latest version. */
	private boolean isLatestVersion;
	
	/** The application Name. */
	private String application;
}
