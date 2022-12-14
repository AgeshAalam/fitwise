package com.fitwise.util.payments.appleiap;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlRootElement;

/*
 * Created by Vignesh G on 09/03/21
 */
@Getter
@Setter
@XmlRootElement(name = "price")
public class IAPUpdatePricing {
    private String territory;
    private int tier;
    @XmlPath("start_date/text()")
    private String startDate;
}
