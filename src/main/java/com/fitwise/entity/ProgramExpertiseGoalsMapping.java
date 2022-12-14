package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Setter
@Getter
@Entity
public class ProgramExpertiseGoalsMapping {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="program_expertise_goals_mapping_id")
	private Long programExpertiseGoalsMappingId;

	@ManyToOne
	@JoinColumn(name = "program_expertise_mapping_id")
	private ProgramExpertiseMapping programExpertiseMapping;

	@ManyToOne
	@JoinColumn(name = "program_goal_id")
	private ProgramGoals programGoals;

}
