package com.fitwise.util.payments.appleiap;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name="price")
public class IAPNewPricing {
	private String territory;
	private int tier;
}
