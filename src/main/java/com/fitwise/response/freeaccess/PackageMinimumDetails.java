package com.fitwise.response.freeaccess;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PackageMinimumDetails {
	
	/** The subscription package id. */
	private long subscriptionPackageId;
	
	/** The instructor name. */
	private String instructorName;
	
	/** The title. */
	private String title;
	
	/** The price. */
	private String price;
	
	/** The formatted price. */
	private String formattedPrice;

}
