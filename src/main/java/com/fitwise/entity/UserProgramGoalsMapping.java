package com.fitwise.entity;

import com.fitwise.repository.ProgramExpertiseGoalsMappingRepository;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "user_goal_mapping")
public class UserProgramGoalsMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_goal_id")
    private  long userProgramGoalsMapping;

    @ManyToOne(cascade= {CascadeType.MERGE,CascadeType.DETACH})
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "program_expertise_goals_mapping_id")
    ProgramExpertiseGoalsMapping programExpertiseGoalsMapping;
}
