package com.fitwise.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/*
 * Created by Vignesh G on 18/05/20
 */
@Entity
@Data
public class RestMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long restMetricId;

    private String restMetric;

}
