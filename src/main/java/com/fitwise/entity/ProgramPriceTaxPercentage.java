package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
@Getter
@Setter
public class ProgramPriceTaxPercentage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pricePercentageId;

    /**
     *  DeviceWisePercentage
     */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "deviceWisePercentageId")
    private List<DeviceWiseTaxPercentage> deviceWiseTaxPercentage;

}
