package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * The Class PlatformWiseTaxDetail.
 */
@Entity
@Getter
@Setter
public class PlatformWiseTaxDetail extends AuditingEntity{

    /** The platform wise tax detail id. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long platformWiseTaxDetailId;

    /** The platform type. */
    @ManyToOne
    private PlatformType platformType;

    /** The app store tax percentage. */
    private Double appStoreTaxPercentage;

    /** The trainnr tax percentage. */
    private Double trainnrTaxPercentage;

    /** The general tax percentage. */
    private Double generalTaxPercentage;

    /** The credit card tax percentage. */
    private Double creditCardTaxPercentage;

    private Double creditCardTaxNonDomesticAdditionalPercentage;

    /** The credit card charges. */
    private Double creditCardFixedCharges;

    /** The active. */
    private boolean active;
}
