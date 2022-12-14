package com.fitwise.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fitwise.entity.discounts.DiscountOfferMapping;
import com.fitwise.entity.payments.appleiap.SubscriptionGroup;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Class Programs.
 */
@Entity
@Getter
@Setter
@Table(name = "programs",
		indexes = {
			@Index(name = "index_id", columnList = "program_id", unique = true),
			@Index(name = "index_owner_id", columnList = "program_id,user_id", unique = false)
		})
public class Programs extends AuditingEntity{

	/** The program id. */
	@Id
	@Column(name = "program_id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long programId;

	/** The title. */
	private String title;

	@Column(length = 45)
	private String shortDescription;

	/** The description. */
	private String description;

	/** The publishedDate. */
	private String publishedDate;

	/** The duration. */
	@JsonIgnore
	@OneToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "duration_id")
	private Duration duration;

	/** The program type. */
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "program_type_Id")
	private ProgramTypes programType;

	/** The program expertise level. */
	@JsonIgnore
	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "program_expertise_level_id")
	private ExpertiseLevels programExpertiseLevel;

	/** The instructor year of experience. */
	@JsonIgnore
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "instructorProgramExperienceId")
	private InstructorProgramExperience instructorYearOfExperience;

	/** The owner. */
	@JsonIgnore
	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "user_id")
	private User owner;

	/** The publish. */
	private boolean publish;

	/** The flag. */
	private boolean flag;

	/** The status. */
	private String status;

	//Column for status for unpublish_edit/block_edit programs
	private String postCompletionStatus;

	/** The image. */
	@JsonIgnore
	@ManyToOne(cascade = {CascadeType.MERGE,CascadeType.DETACH})
	@JoinColumn(name = "image_id")
	private Images image;

	/** The promotion. */
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
	@JoinColumn(name = "promotion_id")
	private Promotions promotion;


	private Double programPrice;
	

	/** The unique workout count. */
	private Long uniqueWorkoutCount;

	/** The program mapping. */
	@JsonIgnore
	@OneToMany(mappedBy = "programs", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<WorkoutMapping> programMapping= new ArrayList<>();

	/** The workout schedules. */
	@JsonIgnore
	@OneToMany(mappedBy = "programs", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<WorkoutSchedule> workoutSchedules= new ArrayList<>();

	/** The program wise goals. */
	@OneToMany(mappedBy = "program", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<ProgramWiseGoal> programWiseGoals= new ArrayList<>();

	@OneToMany(mappedBy = "program", cascade = { CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.DETACH }, fetch = FetchType.LAZY)
	private Set<ProgramPriceByPlatform> programPriceByPlatforms = new HashSet<>();

	/** Subscription Group */
	@JsonIgnore
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "subscription_id")
	private SubscriptionGroup subscriptionGroup;

	@JsonIgnore
	@OneToOne
	@JoinColumn(name = "program_price_id")
	private ProgramPrices programPrices;

	/** The discount program mapping. */
	@JsonIgnore
	@OneToMany(mappedBy = "programs", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<DiscountOfferMapping> programDiscountMapping= new ArrayList<>();
	
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "program_sub_type_id")
	private ProgramSubTypes programSubType;
	
}



