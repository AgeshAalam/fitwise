package com.fitwise.program.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Gets the general tax.
 *
 * @return the general tax
 */
@Getter
@Setter
public class PlatformWiseTaxDetailsModel {

    /** The platform wise tax detail id. */
    private Long platformWiseTaxDetailId;

    /** The platform id. */
    private Long platformId;

    /** The platform. */
    private String platform;

    /** The app store tax. */
    private Double appStoreTax;

    /** The trainnr tax. */
    private Double trainnrTax;

    /** The general tax. */
    private Double generalTax;

    /** The credit card tax. */
    private Double creditCardTax;

    /** The credit card charges. */
    private Double creditCardFixedCharges;
    
    /** The flat tax. */
    private Double flatTax;
}
