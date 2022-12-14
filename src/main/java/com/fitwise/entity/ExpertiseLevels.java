package com.fitwise.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ExpertiseLevels {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "expertise_level_id")
	private Long expertiseLevelId;
	
	@Column(name = "expertise_level")
	private String expertiseLevel;

	@Column(name = "icon_url")
	private  String iconUrl;

	@Column(name = "description")
	private  String description;

}
