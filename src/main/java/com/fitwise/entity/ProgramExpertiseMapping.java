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

@Entity
@Getter
@Setter
public class ProgramExpertiseMapping {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="program_expertise_mapping_id")
	private Long programExpertiseMappingId;

	@ManyToOne
	@JoinColumn(name = "program_type_id")
	private ProgramTypes programType;
	
	@ManyToOne
	@JoinColumn(name = "expertise_level_id")
	private ExpertiseLevels expertiseLevel;

}
