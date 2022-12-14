package com.fitwise.view.member;

import com.fitwise.entity.Equipments;
import com.fitwise.entity.PlatformType;
import com.fitwise.program.model.ProgramPlatformPriceResponseModel;
import com.fitwise.view.SubscriptionPackagePackageIdAndTitleView;
import com.fitwise.view.discounts.ProgramDiscountMappingListResponseView;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * Response view for rendering Program detail page in Client side
 */
@Getter
@Setter
public class MemberProgramDetailResponseView {
    private long programId;
    private String programPromoVideoId;
    private String programPromoVideoUrl;
    private String programThumbnail;
    private String programTitle;
    private String shortDescription;
    private String programDescription;
    private Long instructorId;
    private String instructorFirstName;
    private String instructorLastName;
    private String instructorProfileImageUrl;
    private String programPrice;
    private String formattedProgramPrice;
    private int completedWorkouts;
    private List<ProgramPlatformPriceResponseModel> programPlatformPriceResponseModels;
    private String subscriptionStatus;
    private boolean isTrialCompleted;
    private boolean isProgramSubscribed;
    private Date subscribedDate;
    private String subscribedDateFormatted;
    private boolean isProgramAutoSubscribed = false;
    private String subscriptionValidity;
    private List<Equipments> equipments;
    private String programType;
    private String programLevel;
    private Long programDuration;
    private int noOfWorkouts;
    private int progressPercent;
    private String progress;
    private List<MemberWorkoutScheduleResponseView> workouts;
    private PlatformType subscribedViaPlatform;
    private Long subscriptionExpiry;
    private String subscriptionExpiryDate;
    private Date programCompletionDate;
    private String programCompletionDateFormatted;
    private String promotionUploadStatus;
    private String orderStatus;
    // This will be always false in case of authorize.net since we get immediate success/failure response from authorize.net
    private Boolean isOrderUnderProcessing = false;
    private Boolean isUserBlocked = false;
    private Boolean isSubscriptionRestricted;
    private Boolean isARBUnderProcessing = false;
    private String arbStatusMessage = "Processing";
    private ProgramDiscountMappingListResponseView discountOffers ;
    private List<SubscriptionPackagePackageIdAndTitleView> associatedPackages;
    private boolean isProgramSubscribedThroughPackage;
    private boolean isFreeToAccess = false;
    private Double flatTax;
}