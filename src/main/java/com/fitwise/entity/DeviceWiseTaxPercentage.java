package com.fitwise.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class DeviceWiseTaxPercentage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long deviceWisePercentageId;
	
	private Double appStoreChargePercentage;

	private Double trainnrPlatformPercentage;
	
	private Double taxPercentage;
	
	private String deviceType;
	
}
