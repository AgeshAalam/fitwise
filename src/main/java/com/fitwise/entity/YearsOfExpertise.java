package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class YearsOfExpertise {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long experienceId;

    private String numberOfYears;
}
