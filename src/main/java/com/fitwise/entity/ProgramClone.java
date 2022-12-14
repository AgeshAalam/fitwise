package com.fitwise.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ProgramClone {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long programCloneId;
	
	@ManyToOne
	@JoinColumn(name = "program_id")
	private Programs program;
	
	@ManyToOne
	@JoinColumn(name = "duplicate_program_id")
	private Programs duplicateProgram;
	
}
