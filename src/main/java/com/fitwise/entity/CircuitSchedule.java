package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/*
 * Created by Vignesh G on 11/05/20
 */
@Entity
@Getter
@Setter
public class CircuitSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "circuit_schedule_id")
    private Long circuitScheduleId;

    @ManyToOne(cascade = CascadeType.DETACH)
    private Circuit circuit;

    @Column(name = "circuit_repeat")
    private Long repeat;

    @Column(name = "rest_between_repeat")
    private Long restBetweenRepeat;

    @Column(name = "circuit_order")
    private Long order;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.DETACH})
    @JoinColumn(name = "workout_id")
    private Workouts workout;

    private boolean isRestCircuit;

    private Long restDuration;

    @Column(name = "is_audio")
    private Boolean isAudio;

}
