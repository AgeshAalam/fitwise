package com.fitwise.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fitwise.entity.admin.ExerciseCategoryMapping;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Exercises {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long exerciseId;

	private String title;
	
	private String description;

	private boolean flag;

	@ManyToOne(fetch = FetchType.LAZY, cascade= { CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH })	
	@JoinColumn(name = "video_management_id")
	private VideoManagement videoManagement;

	private Long loopCount;

	@ManyToOne(fetch = FetchType.LAZY, cascade= CascadeType.ALL)
	@JoinColumn(name = "user_id")
	private User owner;

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name = "exercise_equipment_mapping", joinColumns = @JoinColumn(name = "exercise_id"), inverseJoinColumns = @JoinColumn(name = "equipment_id"))
	private List<Equipments> equipments;

	@ManyToOne(fetch = FetchType.LAZY, cascade= { CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH } )
	@JoinColumn(name = "support_video_management_id")
	private VideoManagement supportVideoManagement;

	@OneToMany(mappedBy = "exercise", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<ExerciseCategoryMapping> exerciseCategoryMappings;

	private boolean isByAdmin;

	@ManyToOne(fetch = FetchType.LAZY, cascade= CascadeType.ALL)
	private User lastModifiedBy;

}
