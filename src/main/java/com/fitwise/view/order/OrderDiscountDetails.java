package com.fitwise.view.order;

import lombok.Data;

/*
 * Created by Vignesh G on 25/11/20
 */
@Data
public class OrderDiscountDetails {

    private String offerName;
    private String offerCode;
    private Double discountAmount;
    private String formattedDiscountAmount;

}
