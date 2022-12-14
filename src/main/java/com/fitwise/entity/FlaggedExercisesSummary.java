package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.util.Date;

/*
 * Created by Vignesh G on 25/03/20
 */

@Entity
@Getter
@Setter
public class FlaggedExercisesSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long flagId;

    @OneToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "exercise_id")
    private Exercises exercise;

    private int flaggedCount;

    private Date firstFlaggedDate;

    private Date latestFlaggedDate;

    private String flagStatus;

}
