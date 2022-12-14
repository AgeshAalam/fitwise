package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "program_wise_goal")
public class ProgramWiseGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "program_wise_goal_id")
    private  Long programWiseGoalId;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "program_id")
    private Programs program;

    @ManyToOne
    @JoinColumn(name = "program_expertise_goals_mapping_id")
    private ProgramExpertiseGoalsMapping programExpertiseGoalsMapping;

}
