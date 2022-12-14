package com.fitwise.entity;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/*
 * Created by Vignesh G on 01/07/20
 */
@Entity
@Data
public class ExerciseCompletionAudit extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long exerciseCompletionAuditId;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "user_id")
    private User member;

    @ManyToOne(cascade = CascadeType.DETACH)
    private Programs program;

    private Long workoutScheduleId;

    private Long circuitScheduleId;

    private Long exerciseScheduleId;

    private String action;

}
