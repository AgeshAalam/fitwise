package com.fitwise.entity;

import java.util.Date;

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
public class AppForceLogoutAudit {
	
	/** The app force logout audit id. */
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long appForceLogoutAuditId;
	
	/** The app version id. */
	@ManyToOne
	@JoinColumn(name = "app_version_id")
	private AppVersionInfo appVersionId;
	
	/** The device info details id. */
	@ManyToOne
	@JoinColumn(name = "device_info_detail_id")
	private DeviceInfoDetails deviceInfoDetailsId;
	
	/** The app update info id. */
	@ManyToOne
	@JoinColumn(name = "app_update_info_id")
	private AppUpdateInfo appUpdateInfoId;
	
	/** The app launch action id. */
	@ManyToOne
	@JoinColumn(name = "action_id")
	private AppLaunchAction appLaunchActionId;
	
	/** The user id. */
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User userId;
	
	/** The Created date. */
	private Date CreatedDate;

}
