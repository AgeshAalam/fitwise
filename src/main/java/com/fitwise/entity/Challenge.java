package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Setter
@Getter
@Entity
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long challengeId;
    private String challengeType;
    private Date challengeStartedDay;
    private  Date challengeEndDate;
    private int challengeWorkouts;
    private int ChallengeDays;
    private double percentage;
    private int remainingWorkouts;
    private int completedWorkouts;
    private boolean isExpired;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
