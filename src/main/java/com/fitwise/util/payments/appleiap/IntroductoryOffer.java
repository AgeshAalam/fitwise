package com.fitwise.util.payments.appleiap;



import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="offer")
public class IntroductoryOffer {

	
	private String territory;
	private String type;
	private String tier;	
	private String mode;
	
	@XmlElement(name="start_date")
	private String startDate;
	@XmlElement(name="end_date")
	private String endDate;
	private String duration;
    @XmlElement(name="number_of_periods")
	private String noOfPeriods;
	
}
