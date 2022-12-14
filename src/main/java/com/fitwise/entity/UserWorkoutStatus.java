package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Setter
@Getter
public class UserWorkoutStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userWorkoutStatusId;
    private String status;
    private Date completionDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "workout_id")
    private Workouts workouts;

    @ManyToOne
    @JoinColumn(name = "program_id")
    private Programs program;


}
