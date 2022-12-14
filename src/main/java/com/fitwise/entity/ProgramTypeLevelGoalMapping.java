package com.fitwise.entity;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ProgramTypeLevelGoalMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long programTypeLevelGoalMappingId;
	
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "program_type_id")
	private ProgramTypes programType;
	
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "program_expertise_level_id")
	private ExpertiseLevels expertiseLevel;
	
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "program_goal_id")
	private ProgramGoals programGoal;
}
