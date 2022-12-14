package com.fitwise.program.model;

import lombok.Getter;
import lombok.Setter;

/*
 * Created by Vignesh G on 11/03/20
 */

@Getter
@Setter
public class ProgramPriceResponseModel extends PlatformWiseTaxDetailsModel {

    private Double price;

    private String priceFormatted;

    private Double appStoreTaxAmount;

    private Double trainnrTaxAmount;

    private Double creditCardTaxAmount;

    private Double flatTax;
}
