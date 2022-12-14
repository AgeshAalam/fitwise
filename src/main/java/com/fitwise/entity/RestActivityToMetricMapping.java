package com.fitwise.entity;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/*
 * Created by Vignesh G on 18/05/20
 */
@Entity
@Data
public class RestActivityToMetricMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mappingId;

    @ManyToOne(cascade = {CascadeType.DETACH})
    private RestActivity restActivity;

    @ManyToOne(cascade = {CascadeType.DETACH})
    private RestMetric restMetric;

    private Long maximumValue;

}
