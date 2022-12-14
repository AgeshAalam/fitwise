package com.fitwise.entity;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

/*
 * Created by Vignesh G on 10/07/20
 */
@Entity
@Data
public class WorkoutDiscardFeedback extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long feedbackId;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "program_id")
    private Programs program;

    @ManyToOne(cascade = CascadeType.DETACH)
    private WorkoutSchedule workoutSchedule;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "workoutDiscardFeedback", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<WorkoutDiscardFeedbackMapping> workoutDiscardFeedbackMapping;

}
