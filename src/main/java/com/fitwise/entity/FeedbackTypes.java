package com.fitwise.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Setter;

import lombok.Getter;

@Entity
@Getter
@Setter
public class FeedbackTypes {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long feedbackTypeId;
	
	private String feedbackType;
	
}
