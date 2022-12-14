package com.fitwise.entity;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/*
 * Created by Vignesh G on 03/07/20
 */
@Entity
@Data
public class FlaggedExercise extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long flagId;

    @OneToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "exercise_id")
    private Exercises exercise;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(cascade = CascadeType.DETACH)
    private FlaggedVideoReason flaggedVideoReason;

}
