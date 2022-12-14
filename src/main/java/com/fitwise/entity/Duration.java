package com.fitwise.entity;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Duration {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "duration_id")
	private Long durationId;
	
	private Long duration;
	
}