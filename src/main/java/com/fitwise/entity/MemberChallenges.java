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
public class MemberChallenges {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long memberChallengeId;
	
	@ManyToOne
	@JoinColumn(name = "workout_id")
	private Workouts workout;
	
	private String title;
	
	private String description;
	
	private int daysToComplete;
	
	private Long startDate;
	
	private boolean status;
}
