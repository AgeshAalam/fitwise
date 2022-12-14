package com.fitwise.entity;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Date;

/*
 * Created by Vignesh G on 15/05/20
 */
@Entity
@Data
public class CircuitCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long circuitCompletionId;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "user_id")
    private User member;

    private Date completedDate;

    @ManyToOne(cascade = CascadeType.DETACH)
    private Programs program;

    private Long workoutScheduleId;

    private Long circuitScheduleId;

    private int noOfTimesPlayed;

    private boolean isCompleted;

}
