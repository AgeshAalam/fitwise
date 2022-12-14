package com.fitwise.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class AppUpdateInfo {
	
	/** The app update info id. */
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long appUpdateInfoId;
	
	/** The user ids. */
	private String userIds;
	
	/** The app version id. */
	@ManyToOne
	@JoinColumn(name = "app_version_id")
	private AppVersionInfo appVersionId;
	
	/** The app launch action id. */
	@ManyToOne
	@JoinColumn(name = "action_id")
	private AppLaunchAction appLaunchActionId;
	
	/** The message. */
	private String message;

}
