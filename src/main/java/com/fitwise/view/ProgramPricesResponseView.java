package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgramPricesResponseView {

    private Long programPricesId;

    private String country;

    private double price;

    private String formattedPrice;
    
    private Double flatTax;
}
