package com.fitwise.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/*
 * Created by Vignesh G on 21/09/20
 */
@Entity
@Data
public class TimeSpan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long timeSpanId;

    private Integer days;

    private Integer hours;

    private Integer minutes;

    private Integer seconds;

}
