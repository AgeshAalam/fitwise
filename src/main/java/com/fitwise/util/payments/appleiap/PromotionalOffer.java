package com.fitwise.util.payments.appleiap;



import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="promotional_offer")
public class PromotionalOffer {
	@XmlAttribute(name="remove")
	private String removeOffer;
	
	@XmlElement(name="product_code")
	private String offerCode;
	@XmlElement(name="reference_name")
	private String offerName;
	private String mode;
	private String duration;
	@XmlElement(name="number_of_periods")
	
	private String noOfPeriods;
	@XmlPath("prices/price")
	private List<IAPNewPricing> iapPricing;
}
