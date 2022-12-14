package com.fitwise.program.model;

import lombok.Data;

@Data
public class ProgramResponseDeviceWiseModel {

	private Double appStoreCharge;

	private Double trainnrPlatformCharge;

	private Double taxCharge;

	private String deviceType;
	
	private Double total; 
}
