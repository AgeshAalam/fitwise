package com.fitwise.entity;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/*
 * Created by Vignesh G on 01/07/20
 */
@Entity
@Data
public class WorkoutCompletionAudit extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workoutCompletionAudit;

    @ManyToOne
    private User member;

    @ManyToOne(cascade = CascadeType.DETACH)
    private Programs program;

    private Long workoutScheduleId;

    private String action;

}
