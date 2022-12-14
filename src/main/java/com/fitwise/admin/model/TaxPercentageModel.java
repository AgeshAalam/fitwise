package com.fitwise.admin.model;

import java.util.List;

import lombok.Data;

@Data
public class TaxPercentageModel {

	private Long pricePercentageId;
	
	private List<DeviceWisePercentageModel> deviceWisePercentageList;
	
}
