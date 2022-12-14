package com.fitwise.program.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fitwise.entity.Duration;
import com.fitwise.entity.Equipments;
import com.fitwise.entity.ExpertiseLevels;
import com.fitwise.response.ProgramGoalsView;
import com.fitwise.view.SubscriptionPackagePackageIdAndTitleView;
import com.fitwise.view.discounts.ProgramDiscountMappingListResponseView;
import com.fitwise.view.instructor.ProgramTypeWithSubTypeView;

import lombok.Data;

/**
 * Instantiates a new program response model.
 */
@Data
public class ProgramResponseModel {

	/** The program id. */
	private Long programId;
	
	/** The title. */
	private String title;

	private String shortDescription;

	/** The description. */
	private String description;
	
	/** The duration. */
	private Duration duration;

	/** The program type. */
	private ProgramTypeWithSubTypeView programType;

	/** The program expertise. */
	private ExpertiseLevels programExpertise;

	/** The is publish. */
	private boolean isPublish;
	
	/** The flag. */
	private boolean flag;
	
	/** The status. */
	private String status;

	//status for unpublish_edit/block_edit programs
	private String postCompletionStatus;

	/** The image id. */
	private Long imageId;
	
	/** The thumbnail url. */
	private String thumbnailUrl;

	/** The promotion id. */
	private Long promotionId;

	/** The promo video id. */
	private String promoVideoId;

	/** The promo video parsed url. */
	private String promoVideoParsedUrl;

	/** The promotion thumbnail image id. */
	private Long promotionThumbnailImageId;

	/** The promotion thumbnail image url. */
	private String promotionThumbnailImageUrl;

	/** Field for promotion video duration */
	private int promotionDuration;

	/** Field for promotion video upload status */
	private String promotionUploadStatus;

	/** The program price. */
	private String programPrice;

	/** The formatted program price. */
	private String formattedProgramPrice;
	
	/** The goals list. */
	private List<ProgramGoalsView> goalsList;
	
	/** The created date. */
	private Date createdDate;
	private String createdDateFormatted;

	/** The modified date. */
	private Date modifiedDate;
	private String modifiedDateFormatted;

	/** The first name. */
	private String firstName;

	/** The last name. */
	private String lastName;

	/** The schedules. */
	private List<WorkoutScheduleModel> workoutSchedules = new ArrayList<>();
	
	/** The workouts. */
	private List<WorkoutResponseModel> workouts = new ArrayList<>();

	/** The Equipments. */
	private List<Equipments> equipments;

	private List<ProgramPlatformPriceResponseModel> programPlatformPriceResponseModels;

	private String instructorProfileUrl;

	private int activeSubscriptions;
	
	/** The Discounts. */
	private ProgramDiscountMappingListResponseView discountOffers ;

	private boolean isMaximumCountReachedForNewUsers;

	private boolean isMaximumCountReachedForExistingUsers;

	private List<SubscriptionPackagePackageIdAndTitleView> associatedPackages;

}
