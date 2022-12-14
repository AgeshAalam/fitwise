package com.fitwise.search.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class SearchModel {

	List<String> expertsLevel;
	List<String> programType;
	List<Long> duration;
	List<Long> price;
	List<Long> instructorYearOfExperience;
	
	String exercise;
	String workout;
	String client;
	String instructor;
	String program;
	String programTypeName;
	String goal;
	String equipment;
	Long duraton;
}
