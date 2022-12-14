package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity
@Getter
@Setter
public class CircuitVoiceOverMappingCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long circuitVoiceOverCompletionId;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "user_id")
    private User member;

    private Date completedDate;

    @ManyToOne(cascade = CascadeType.DETACH)
    private Programs program;

    private Long workoutScheduleId;

    private Long circuitScheduleId;

    private Long circuitVoiceOverMappingId;
}
