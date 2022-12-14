package com.fitwise.entity.payments.authNet.cardTypes;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class CardTypeWithProcessingCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cardType;

    private Double domesticProcessingPercentage;

    private Double internationalProcessingPercentage;

    private Double additionalCharge;

}
