package com.fitwise.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/*
 * Created by Vignesh G on 13/05/20
 */
@Entity
@Data
public class CircuitRepeatCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long CircuitRepeatId;

    private Long RepeatCount;

}
