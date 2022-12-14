package com.fitwise.specifications.jpa.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/*
 * Created by Vignesh.G on 01/07/21
 */
@Getter
@Setter
@AllArgsConstructor
public class RevenueByPlatform {

    private String platformName;

    private Double revenue;

}
