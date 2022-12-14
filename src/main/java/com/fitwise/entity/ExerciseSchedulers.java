package com.fitwise.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Data
@Table(name="exercise_schedulers")
public class ExerciseSchedulers {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "exercise_schedule_id")
	private Long exerciseScheduleId;

	private String title;

	private String description;

	private Long loopCount;

	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "exerciseId")
	private Exercises exercise;

	@Column(name = "exercise_order")
	private Long order;

	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "workout_rest_video_id")
	private WorkoutRestVideos workoutRestVideo;

	private String playType;
	private int setsCount;
	private int repsCount;

	/**
	 * the mapped bot object
	 */
	@JsonIgnore
	@ManyToOne(cascade= {CascadeType.MERGE,CascadeType.DETACH} )
	@JoinColumn(name="workout_id")
	private Workouts workout;

	@ManyToOne(fetch = FetchType.LAZY, cascade= {CascadeType.MERGE,CascadeType.DETACH})
	private Circuit circuit;

	@Column(name = "is_audio")
	private Boolean isAudio;

	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "voiceOverId")
	private VoiceOver voiceOver;
}
