package com.fitwise.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="workout_rest_video")
public class WorkoutRestVideos {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "workout_rest_video_id")
	private Long workoutRestVideoId;
	
	@Column(name = "restTime")
	private Long restTime;

	private String restDuration;
	
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "videoManagementId")
	private VideoManagement videoManagement;
}
