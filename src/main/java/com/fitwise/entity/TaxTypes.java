package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class TaxTypes {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long taxTypeId;

    private String taxType;

    private String taxNumberType;
}
