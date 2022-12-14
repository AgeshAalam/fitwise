package com.fitwise.entity;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/*
 * Created by Vignesh G on 16/05/20
 */
@Entity
@Data
@Table(name = "workout_completion", indexes = {
        @Index(name = "index_id", columnList = "workout_completion_id", unique = true),
        @Index(name = "index_user", columnList = "workout_completion_id, member_user_id"),
        @Index(name = "index_user_program", columnList = "workout_completion_id, member_user_id, program_program_id"),
})
public class WorkoutCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workout_completion_id")
    private Long workoutCompletionId;

    private Date completedDate;

    @ManyToOne
    @JoinColumn(name = "member_user_id")
    private User member;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "program_program_id")
    private Programs program;

    private Long workoutScheduleId;

}