package com.fitwise.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * The Class ProgramPriceByPlatform.
 */
@Entity
@Getter
@Setter
public class ProgramPriceByPlatform {

    /** The program price by platform id. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long programPriceByPlatformId;

    /** The price. */
    private Double price;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "program_id")
    private Programs program;

    /** The platform wise tax detail. */
    @ManyToOne
    private PlatformWiseTaxDetail platformWiseTaxDetail;
}
