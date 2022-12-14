package com.fitwise.admin.model;

import lombok.Data;

@Data
public class DeviceWisePercentageModel {
	
	private Double appStoreChargePercentage;

	private Double trainnrPlatformPercentage;
	
	private Double taxPercentage;
	
	private String deviceType;
}
