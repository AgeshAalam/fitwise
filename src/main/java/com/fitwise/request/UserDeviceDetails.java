package com.fitwise.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDeviceDetails {
	
	/** The device name. */
	private String deviceName;
	
	/** The device model. */
	private String deviceModel;
	
	/** The device platform. */
	private String devicePlatform;
	
	/** The device platform version. */
	private String devicePlatformVersion;
	
	/** The device uuid. */
	private String deviceUuid;
	
	/** The app version. */
	private String appVersion;
	
	/** The role. */
	private String role;

	private String timeZone;
}
