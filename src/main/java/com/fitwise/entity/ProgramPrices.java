package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
public class ProgramPrices {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long programPricesId;
 
    private String country;

    private double price;

    private int tier;

}
