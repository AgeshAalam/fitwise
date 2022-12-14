package com.fitwise.service.member;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.fitwise.entity.instructor.InstructorTierDetails;
import com.fitwise.repository.instructor.InstructorTierDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.InstructorConstant;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.constants.discounts.DiscountsConstants;
import com.fitwise.entity.Challenge;
import com.fitwise.entity.CircuitAndVoiceOverMapping;
import com.fitwise.entity.CircuitCompletion;
import com.fitwise.entity.CircuitCompletionAudit;
import com.fitwise.entity.CircuitSchedule;
import com.fitwise.entity.CircuitVoiceOverMappingCompletion;
import com.fitwise.entity.CircuitVoiceOverMappingCompletionAudit;
import com.fitwise.entity.DiscardWorkoutReasons;
import com.fitwise.entity.Duration;
import com.fitwise.entity.Equipments;
import com.fitwise.entity.ExerciseCompletion;
import com.fitwise.entity.ExerciseCompletionAudit;
import com.fitwise.entity.ExerciseSchedulers;
import com.fitwise.entity.Exercises;
import com.fitwise.entity.ExpertiseLevels;
import com.fitwise.entity.FeaturedPrograms;
import com.fitwise.entity.FeedbackTypes;
import com.fitwise.entity.FlaggedExercise;
import com.fitwise.entity.FlaggedExercisesSummary;
import com.fitwise.entity.FlaggedVideoReason;
import com.fitwise.entity.InstructorRestActivity;
import com.fitwise.entity.PlatformType;
import com.fitwise.entity.ProgramPriceByPlatform;
import com.fitwise.entity.ProgramPromoViews;
import com.fitwise.entity.ProgramRating;
import com.fitwise.entity.ProgramTypes;
import com.fitwise.entity.ProgramViewsAudit;
import com.fitwise.entity.Programs;
import com.fitwise.entity.RestActivity;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.UserProgramGoalsMapping;
import com.fitwise.entity.WorkoutCompletion;
import com.fitwise.entity.WorkoutCompletionAudit;
import com.fitwise.entity.WorkoutDiscardFeedback;
import com.fitwise.entity.WorkoutDiscardFeedbackMapping;
import com.fitwise.entity.WorkoutFeedback;
import com.fitwise.entity.WorkoutSchedule;
import com.fitwise.entity.Workouts;
import com.fitwise.entity.discounts.DiscountOfferMapping;
import com.fitwise.entity.member.completion.ProgramCompletion;
import com.fitwise.entity.member.completion.ProgramCompletionAudit;
import com.fitwise.entity.packaging.PackageExternalClientMapping;
import com.fitwise.entity.packaging.PackageMemberMapping;
import com.fitwise.entity.packaging.PackageProgramMapping;
import com.fitwise.entity.payments.appleiap.AppleProductSubscription;
import com.fitwise.entity.payments.authNet.AuthNetArbSubscription;
import com.fitwise.entity.payments.authNet.AuthNetPayment;
import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.payments.stripe.StripeSubscriptionAndUserPackageMapping;
import com.fitwise.entity.payments.stripe.billing.StripePayment;
import com.fitwise.entity.payments.stripe.billing.StripeSubscriptionAndUserProgramMapping;
import com.fitwise.entity.product.FreeAccessProgram;
import com.fitwise.entity.product.FreeProduct;
import com.fitwise.entity.subscription.PackageProgramSubscription;
import com.fitwise.entity.subscription.ProgramSubscription;
import com.fitwise.entity.subscription.SubscriptionPlan;
import com.fitwise.entity.subscription.SubscriptionStatus;
import com.fitwise.exception.ApplicationException;
import com.fitwise.exercise.service.VimeoService;
import com.fitwise.model.member.AllProgramsFilterModel;
import com.fitwise.model.member.DiscardReasonModel;
import com.fitwise.model.member.MemberProgramsFilterModel;
import com.fitwise.model.member.RecommendedAndTrendingFilterModel;
import com.fitwise.model.videoCaching.VideoCacheExerciseModel;
import com.fitwise.model.videoCaching.VideoCacheProgramModel;
import com.fitwise.model.videoCaching.VideoCacheWorkoutModel;
import com.fitwise.model.videoCaching.VideoCachingRequestModel;
import com.fitwise.program.model.ProgramPlatformPriceResponseModel;
import com.fitwise.program.model.ProgramTileModel;
import com.fitwise.program.model.ProgramTypeWithProgramTileModel;
import com.fitwise.repository.CircuitAndVoiceOverMappingRepository;
import com.fitwise.repository.CircuitVoiceOverMappingCompletionAuditRepo;
import com.fitwise.repository.CircuitVoiceOverMappingCompletionRepository;
import com.fitwise.repository.DurationRepo;
import com.fitwise.repository.ExerciseScheduleRepository;
import com.fitwise.repository.ExpertiseLevelRepository;
import com.fitwise.repository.FeaturedProgramsRepository;
import com.fitwise.repository.FlaggedExerciseRepository;
import com.fitwise.repository.FlaggedExercisesSummaryRepository;
import com.fitwise.repository.FlaggedVideoReasonsRepository;
import com.fitwise.repository.ProgramPromoViewsRepository;
import com.fitwise.repository.ProgramRatingRepository;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.ProgramTypeRepository;
import com.fitwise.repository.ProgramViewsAuditRepository;
import com.fitwise.repository.RestActivityRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.UserProgramGoalsMappingRepository;
import com.fitwise.repository.WorkoutRepository;
import com.fitwise.repository.WorkoutScheduleRepository;
import com.fitwise.repository.challenge.ChallengeRepository;
import com.fitwise.repository.circuit.CircuitScheduleRepository;
import com.fitwise.repository.discountsRepository.DiscountOfferMappingRepository;
import com.fitwise.repository.feedback.DiscardWorkoutReasonsRepository;
import com.fitwise.repository.feedback.FeedbackTypesRepository;
import com.fitwise.repository.feedback.WorkoutDiscardFeedbackRepository;
import com.fitwise.repository.feedback.WorkoutFeedbackRepository;
import com.fitwise.repository.member.CircuitCompletionAuditRepository;
import com.fitwise.repository.member.CircuitCompletionRepository;
import com.fitwise.repository.member.ExerciseCompletionAuditRepository;
import com.fitwise.repository.member.ExerciseCompletionRepository;
import com.fitwise.repository.member.ProgramCompletionAuditRepository;
import com.fitwise.repository.member.ProgramCompletionRepository;
import com.fitwise.repository.member.WorkoutCompletionAuditRepository;
import com.fitwise.repository.member.WorkoutCompletionRepository;
import com.fitwise.repository.order.OrderManagementRepository;
import com.fitwise.repository.packaging.PackageExternalClientMappingRepository;
import com.fitwise.repository.packaging.PackageMemberMappingRepository;
import com.fitwise.repository.packaging.PackageProgramMappingRepository;
import com.fitwise.repository.payments.appleiap.AppleProductSubscriptionRepository;
import com.fitwise.repository.payments.authnet.AuthNetArbSubscriptionRepository;
import com.fitwise.repository.payments.authnet.AuthNetPaymentRepository;
import com.fitwise.repository.payments.stripe.StripeSubscriptionAndUserPackageMappingRepository;
import com.fitwise.repository.payments.stripe.billing.StripePaymentRepository;
import com.fitwise.repository.payments.stripe.billing.StripeSubscriptionAndUserProgramMappingRepository;
import com.fitwise.repository.subscription.PackageProgramSubscriptionRepository;
import com.fitwise.repository.subscription.ProgramSubscriptionRepo;
import com.fitwise.service.SubscriptionService;
import com.fitwise.service.admin.FitwiseShareService;
import com.fitwise.service.discountservice.DiscountsService;
import com.fitwise.service.freeproduct.FreeAccessService;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.specifications.ProgramSpecifications;
import com.fitwise.specifications.jpa.ProgramJpa;
import com.fitwise.specifications.jpa.dao.PackageSubscriptionDAOWithStripe;
import com.fitwise.specifications.jpa.dao.ProgramSubscriptionDAOWithStripe;
import com.fitwise.specifications.jpa.dao.WorkoutCompletionDAO;
import com.fitwise.utils.Convertions;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.RequestParamValidator;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.utils.parsing.ProgramDataParsing;
import com.fitwise.view.AudioResponseView;
import com.fitwise.view.MemberWorkoutDetailResponseView;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.SubscriptionPackagePackageIdAndTitleView;
import com.fitwise.view.circuit.CircuitScheduleResponseView;
import com.fitwise.view.discounts.ProgramDiscountMappingListResponseView;
import com.fitwise.view.discounts.ProgramDiscountMappingResponseView;
import com.fitwise.view.member.ExerciseCompletionResponse;
import com.fitwise.view.member.Filter;
import com.fitwise.view.member.MemberFilterView;
import com.fitwise.view.member.MemberProgramDetailResponseView;
import com.fitwise.view.member.MemberWorkoutScheduleResponseView;
import com.fitwise.view.member.MyProgramView;
import com.fitwise.view.member.TodaysProgramView;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * Created by Vignesh G on 24/04/20
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MemberProgramService {

    @Autowired
    UserComponents userComponents;

    @Autowired
    ProgramTypeRepository programTypeRepository;

    @Autowired
    ProgramSubscriptionRepo programSubscriptionRepo;

    @Autowired
    ProgramRepository programRepository;

    @Autowired
    SubscriptionService subscriptionService;

    @Autowired
    UserProfileRepository userProfileRepository;

    @Autowired
    VimeoService vimeoService;

    @Autowired
    WorkoutCompletionRepository workoutCompletionRepository;

    @Autowired
    WorkoutScheduleRepository workoutScheduleRepository;

    @Autowired
    ExpertiseLevelRepository expertiseLevelRepository;

    @Autowired
    DurationRepo durationRepo;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    FitwiseUtils fitwiseUtils;

    @Autowired
    AuthNetArbSubscriptionRepository authNetArbSubscriptionRepository;

    @Autowired
    private CircuitScheduleRepository circuitScheduleRepository;

    @Autowired
    private ExerciseScheduleRepository exerciseScheduleRepository;

    @Autowired
    private ExerciseCompletionRepository exerciseCompletionRepository;

    @Autowired
    private CircuitCompletionRepository circuitCompletionRepository;

    @Autowired
    private ValidationService validationService;

    @Autowired
    RestActivityRepository restActivityRepository;

    @Autowired
    private UserProgramGoalsMappingRepository userProgramGoalsMappingRepository;

    @Autowired
    ExerciseCompletionAuditRepository exerciseCompletionAuditRepository;
    @Autowired
    CircuitCompletionAuditRepository circuitCompletionAuditRepository;
    @Autowired
    WorkoutCompletionAuditRepository workoutCompletionAuditRepository;
    @Autowired
    FlaggedVideoReasonsRepository flaggedVideoReasonsRepository;
    @Autowired
    FlaggedExerciseRepository flaggedExerciseRepository;
    @Autowired
    FlaggedExercisesSummaryRepository flaggedExercisesSummaryRepository;

    @Autowired
    private OrderManagementRepository orderManagementRepository;

    @Autowired
    ProgramRatingRepository programRatingRepository;

    @Autowired
    AuthNetPaymentRepository authNetPaymentRepository;

    @Autowired
    AppleProductSubscriptionRepository appleProductSubscriptionRepository;
    @Autowired
    ProgramDataParsing programDataParsing;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ProgramViewsAuditRepository programViewsAuditRepository;

    @Autowired
    StripePaymentRepository stripePaymentRepository;
    @Autowired
    StripeSubscriptionAndUserProgramMappingRepository stripeSubscriptionAndUserProgramMappingRepository;

    @Autowired
    private FeaturedProgramsRepository featuredProgramsRepository;

    @Autowired
    DiscountOfferMappingRepository dOfferMappingRepository;
    @Autowired
    private DiscountsService discountsService;

    @Autowired
    private CircuitAndVoiceOverMappingRepository circuitAndVoiceOverMappingRepository;

    @Autowired
    private CircuitVoiceOverMappingCompletionRepository circuitVoiceOverMappingCompletionRepository;

    @Autowired
    private CircuitVoiceOverMappingCompletionAuditRepo circuitVoiceOverMappingCompletionAuditRepo;
    @Autowired
    private PackageProgramSubscriptionRepository packageProgramSubscriptionRepository;

    @Autowired
    private PackageProgramMappingRepository packageProgramMappingRepository;

    @Autowired
    private StripeSubscriptionAndUserPackageMappingRepository stripeSubscriptionAndUserPackageMappingRepository;

    @Autowired
    private PackageMemberMappingRepository packageMemberMappingRepository;

    @Autowired
    private PackageExternalClientMappingRepository packageExternalClientMappingRepository;

    @Autowired
    private WorkoutDiscardFeedbackRepository workoutDiscardFeedbackRepository;

    @Autowired
    private DiscardWorkoutReasonsRepository discardWorkoutReasonsRepository;

    @Autowired
    private FeedbackTypesRepository feedbackTypesRepository;

    @Autowired
    private WorkoutFeedbackRepository workoutFeedbackRepository;

    @Autowired
    private ProgramPromoViewsRepository programPromoViewsRepository;

    @Autowired
    private ProgramCompletionAuditRepository programCompletionAuditRepository;

    @Autowired
    private ProgramCompletionRepository programCompletionRepository;

    private final FitwiseShareService fitwiseShareService;
    private final ProgramJpa programJpa;
    private final FreeAccessService freeAccessService;
    private final InstructorTierDetailsRepository instructorTierDetailsRepository;

    /**
     * Method used to get the details of a Program in Member app
     *
     * @param programId
     * @return
     * @throws ApplicationException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     * @throws ParseException
     */
    @Transactional
    public MemberProgramDetailResponseView getProgramDetails(Long programId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ParseException {
        long startTimeInMllis = new Date().getTime();
        long profilingStartTimeInMillis = new Date().getTime();
        long profilingEndTimeInMillis;
        if (programId == null || programId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_NULL, MessageConstants.ERROR);
        }
        Programs program = programRepository.findByProgramId(programId);
        if (program == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, MessageConstants.ERROR);
        }
        profilingEndTimeInMillis = new Date().getTime();
        log.info("Program Validation Time" +(profilingEndTimeInMillis-profilingStartTimeInMillis));
        profilingStartTimeInMillis = new Date().getTime();
        List<String> programStatusList = Arrays.asList(InstructorConstant.PLAN, InstructorConstant.UPLOAD, InstructorConstant.SCHEDULE, InstructorConstant.PRICE, InstructorConstant.PRE_PUBLISH, DBConstants.BLOCK_EDIT, DBConstants.UNPUBLISH_EDIT);
        boolean isAccessRestricted = programStatusList.stream().anyMatch(program.getStatus()::equalsIgnoreCase);
        if (isAccessRestricted) {
            throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_MEMBER_PROGRAM_NOT_AVAILABLE, MessageConstants.ERROR);
        }
        profilingEndTimeInMillis = new Date().getTime();
        log.info("Access restriction validation time taken in millis" +(profilingEndTimeInMillis-profilingStartTimeInMillis));
        profilingStartTimeInMillis = new Date().getTime();
        User user = userComponents.getAndValidateUser();
        profilingEndTimeInMillis = new Date().getTime();
        log.info("Validate User : time taken in millis :" +(profilingEndTimeInMillis-profilingStartTimeInMillis));
        MemberProgramDetailResponseView memberProgramDetailResponseView = new MemberProgramDetailResponseView();
        memberProgramDetailResponseView.setProgramId(programId);
        profilingStartTimeInMillis = new Date().getTime();
        boolean isMemberBlocked = false;
        //Checking whether the user is blocked
        if (user != null) {
            isMemberBlocked = fitwiseUtils.isCurrentMemberBlocked();
            memberProgramDetailResponseView.setIsUserBlocked(isMemberBlocked);
        }
        profilingEndTimeInMillis = new Date().getTime();
        log.info("Validate User block status : time taken in millis :" +(profilingEndTimeInMillis-profilingStartTimeInMillis));
        //Parsing the video id from Vimeo response and generating the .mp4 url
        if (program.getPromotion() != null && program.getPromotion().getVideoManagement() != null && !ValidationUtils.isEmptyString(program.getPromotion().getVideoManagement().getUrl())) {
            String vimeoUrl = program.getPromotion().getVideoManagement().getUrl();
            String vimeoId = "";
            if (vimeoUrl.contains("/")) {
                String[] videoIds = vimeoUrl.split("/");
                vimeoId = videoIds[2];
            }
            if (!vimeoId.isEmpty()) {
                profilingStartTimeInMillis = new Date().getTime();
                memberProgramDetailResponseView.setProgramPromoVideoUrl(vimeoService.getVimeoUrlFromId(Long.parseLong(vimeoId)));
                profilingEndTimeInMillis = new Date().getTime();
                log.info("Vimeo url for promo video : Time taken in millis : " + (profilingEndTimeInMillis - profilingStartTimeInMillis));
                memberProgramDetailResponseView.setProgramPromoVideoId(program.getPromotion().getVideoManagement().getUrl());

            }
        }
        if (program.getImage() != null && !ValidationUtils.isEmptyString(program.getImage().getImagePath()))
            memberProgramDetailResponseView.setProgramThumbnail(program.getImage().getImagePath());
        memberProgramDetailResponseView.setProgramTitle(program.getTitle());
        memberProgramDetailResponseView.setShortDescription(program.getShortDescription());
        memberProgramDetailResponseView.setProgramDescription(program.getDescription());
        //Field to allow subscription
        profilingStartTimeInMillis = new Date().getTime();
        List<String> subscriptionRestrictedStatusList = Arrays.asList(InstructorConstant.BLOCK, InstructorConstant.UNPUBLISH);
        boolean isProgramBlockOrUnpublished = subscriptionRestrictedStatusList.stream().anyMatch(program.getStatus()::equalsIgnoreCase);
        memberProgramDetailResponseView.setIsSubscriptionRestricted(isProgramBlockOrUnpublished);
        profilingEndTimeInMillis = new Date().getTime();
        log.info("Program Restricted status validation : Time taken in millis : " + (profilingEndTimeInMillis - profilingStartTimeInMillis));
        memberProgramDetailResponseView.setInstructorId(program.getOwner().getUserId());
        //Setting Instructor Name to the response
        profilingStartTimeInMillis = new Date().getTime();
        UserProfile instructor = userProfileRepository.findByUser(program.getOwner());
        if (instructor != null) {
            memberProgramDetailResponseView.setInstructorFirstName(instructor.getFirstName());
            memberProgramDetailResponseView.setInstructorLastName(instructor.getLastName());
            if (instructor.getProfileImage() != null && !ValidationUtils.isEmptyString(instructor.getProfileImage().getImagePath()))
                memberProgramDetailResponseView.setInstructorProfileImageUrl(instructor.getProfileImage().getImagePath());
        }
        profilingEndTimeInMillis = new Date().getTime();
        log.info("Instructor details : Time taken in millis : " + (profilingEndTimeInMillis - profilingStartTimeInMillis));


        //Setting program price
        profilingStartTimeInMillis = new Date().getTime();
        if (program.getProgramPrice() != null) {
            DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
            String price = KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(program.getProgramPrice());
            memberProgramDetailResponseView.setProgramPrice(decimalFormat.format(program.getProgramPrice()));
            memberProgramDetailResponseView.setFormattedProgramPrice(price);
            List<ProgramPlatformPriceResponseModel> programPlatformPriceModels = new ArrayList<>();
            InstructorTierDetails instructorTierDetails = instructorTierDetailsRepository.findByUserAndActive(program.getOwner(), true);
            Double trainnrTax = 15.0;
            if (instructorTierDetails == null) {
                trainnrTax = instructorTierDetails.getTier().getTierTypeDetails().getProgramsFees();
            }
            for (ProgramPriceByPlatform programPriceByPlatform : program.getProgramPriceByPlatforms()) {
                ProgramPlatformPriceResponseModel programPlatformPriceResponseModel = new ProgramPlatformPriceResponseModel();
                programPlatformPriceResponseModel.setProgramPriceByPlatformId(programPriceByPlatform.getProgramPriceByPlatformId());
                programPlatformPriceResponseModel.setPlatformWiseTaxDetailId(programPriceByPlatform.getPlatformWiseTaxDetail().getPlatformWiseTaxDetailId());
                programPlatformPriceResponseModel.setPlatform(programPriceByPlatform.getPlatformWiseTaxDetail().getPlatformType().getPlatform());
                programPlatformPriceResponseModel.setPlatformId(programPriceByPlatform.getPlatformWiseTaxDetail().getPlatformType().getPlatformTypeId());
                programPlatformPriceResponseModel.setPrice(programPriceByPlatform.getPrice());
                programPlatformPriceResponseModel.setAppStoreTax(programPriceByPlatform.getPlatformWiseTaxDetail().getAppStoreTaxPercentage());
                programPlatformPriceResponseModel.setTrainnrTax(trainnrTax);
                programPlatformPriceResponseModel.setGeneralTax(programPriceByPlatform.getPlatformWiseTaxDetail().getGeneralTaxPercentage());
                programPlatformPriceModels.add(programPlatformPriceResponseModel);
            }
            memberProgramDetailResponseView.setProgramPlatformPriceResponseModels(programPlatformPriceModels);
        }
        profilingEndTimeInMillis = new Date().getTime();
        log.info("Platform wise price construction : Time taken in millis : " + (profilingEndTimeInMillis - profilingStartTimeInMillis));


        boolean isTrialOrSubscribed = false;
        //To check whether the program is auto-subscribed.
        boolean programAutoSubscribed = false;
        //To check whether the program is subscribed.
        boolean isProgramSubscribed = false;
        boolean isTrialSubscription = false;
        boolean isNewSubscription = true;
        boolean isViaProgramSubscription = false;
        boolean isViaPackageSubscription = false;
        Date expiryDate = null;
        Date subscribedDate = null;
        String subscriptionValidity = null;

        List<SubscriptionPackagePackageIdAndTitleView> associatedPackages = new ArrayList<>();
        profilingStartTimeInMillis = new Date().getTime();
        List<PackageProgramMapping> packageProgramMappingList = packageProgramMappingRepository.findByProgramAndSubscriptionPackageStatus(program, KeyConstants.KEY_PUBLISH);
        if (!packageProgramMappingList.isEmpty()) {
            for (PackageProgramMapping packageProgramMapping : packageProgramMappingList) {
                if (!packageProgramMapping.getSubscriptionPackage().isRestrictedAccess()) {
                    SubscriptionPackagePackageIdAndTitleView associatedPackageView = new SubscriptionPackagePackageIdAndTitleView();
                    associatedPackageView.setSubscriptionPackageId(packageProgramMapping.getSubscriptionPackage().getSubscriptionPackageId());
                    associatedPackageView.setTitle(packageProgramMapping.getSubscriptionPackage().getTitle());
                    associatedPackages.add(associatedPackageView);
                } else {
                    if (user != null) {
                        PackageMemberMapping packageMemberMapping = packageMemberMappingRepository.findTop1BySubscriptionPackageSubscriptionPackageIdAndUserUserId(packageProgramMapping.getSubscriptionPackage().getSubscriptionPackageId(), user.getUserId());
                        PackageExternalClientMapping packageExternalClientMapping = packageExternalClientMappingRepository.findTop1BySubscriptionPackageSubscriptionPackageIdAndExternalClientEmail(packageProgramMapping.getSubscriptionPackage().getSubscriptionPackageId(), user.getEmail());
                        if (packageMemberMapping != null || packageExternalClientMapping != null) {
                            SubscriptionPackagePackageIdAndTitleView associatedPackageView = new SubscriptionPackagePackageIdAndTitleView();
                            associatedPackageView.setSubscriptionPackageId(packageProgramMapping.getSubscriptionPackage().getSubscriptionPackageId());
                            associatedPackageView.setTitle(packageProgramMapping.getSubscriptionPackage().getTitle());
                            associatedPackages.add(associatedPackageView);
                        }
                    }
                }
            }
        }
        profilingEndTimeInMillis = new Date().getTime();
        log.info("Associated packages query and construction : " + (profilingEndTimeInMillis - profilingStartTimeInMillis));
        memberProgramDetailResponseView.setAssociatedPackages(associatedPackages);

        boolean isProgramSubscribedThroughPackages = false;

        profilingStartTimeInMillis = new Date().getTime();
        if (user != null) {

            /*
             * Restarting program for user.
             * */
            try {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
                String token = request.getHeader(Constants.X_AUTHORIZATION);
                String timeZoneName = userComponents.getTimeZone(token);
                checkAndResetProgramCompletion(timeZoneName, user, program);
            } catch (Exception e) {
                log.error("Exception while restarting program from Member program L2 : " + e.getMessage());
            }
            profilingEndTimeInMillis = new Date().getTime();
            log.info("Reset program completion : Time taken in millis : " + (profilingEndTimeInMillis - profilingStartTimeInMillis));
            profilingStartTimeInMillis = new Date().getTime();
            // Checking whether the program is subscribed by the user
            ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), program.getProgramId());
            PackageProgramSubscription packageProgramSubscription;
            packageProgramSubscription = packageProgramSubscriptionRepository.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), program.getProgramId());
            profilingEndTimeInMillis = new Date().getTime();
            log.info("Program and package subscription query : Time taken in millis : " + (profilingEndTimeInMillis - profilingStartTimeInMillis));
            boolean isPaidProgramSubscription = false;
            boolean isPaidPackageSubscription = false;
            String programSubscriptionStatus = null;
            String packageSubscriptionStatus = null;
            profilingStartTimeInMillis = new Date().getTime();
            if (programSubscription != null) {
                SubscriptionStatus subscriptionStatus = subscriptionService.getMemberProgramSubscriptionStatus(programSubscription);
                programSubscriptionStatus = subscriptionStatus.getSubscriptionStatusName();
                if (subscriptionStatus != null && (subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID) || subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAYMENT_PENDING))) {
                    isPaidProgramSubscription = true;
                }
            }
            if (packageProgramSubscription != null) {
                SubscriptionStatus subscriptionStatus = subscriptionService.getMemberPackageProgramSubscriptionStatus(packageProgramSubscription);
                packageSubscriptionStatus = subscriptionStatus.getSubscriptionStatusName();
                if (subscriptionStatus != null && (subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID) || subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAYMENT_PENDING))) {
                    isPaidPackageSubscription = true;
                }
            }
            if (programSubscription != null && packageProgramSubscription != null) {
                if ((isPaidProgramSubscription && isPaidPackageSubscription) || isPaidProgramSubscription) {
                    expiryDate = subscriptionService.getProgramSubscriptionExpiry(programSubscription);
                    //For bug id - 136962 , as per Arvind input ,given precedence to program followed by package if both are paid
                    isViaProgramSubscription = true;
                } else if (isPaidPackageSubscription) {
                    isViaPackageSubscription = true;
                    expiryDate = subscriptionService.getPackageSubscriptionExpiry(packageProgramSubscription.getPackageSubscription());
                } else if (programSubscriptionStatus.equalsIgnoreCase(KeyConstants.KEY_TRIAL) && packageSubscriptionStatus.equalsIgnoreCase(KeyConstants.KEY_EXPIRED)) {
                    isViaProgramSubscription = true;
                } else if (programSubscriptionStatus.equalsIgnoreCase(KeyConstants.KEY_EXPIRED) && packageSubscriptionStatus.equalsIgnoreCase(KeyConstants.KEY_EXPIRED)) {
                    Date programExpiryDate = subscriptionService.getProgramSubscriptionExpiry(programSubscription);
                    Date packageExpiryDate = subscriptionService.getPackageSubscriptionExpiry(packageProgramSubscription.getPackageSubscription());
                    if (packageExpiryDate.after(programExpiryDate)) {
                        isViaPackageSubscription = true;
                        expiryDate = packageExpiryDate;
                    } else {
                        isViaProgramSubscription = true;
                        expiryDate = programExpiryDate;
                    }
                }
            } else if (programSubscription != null && packageProgramSubscription == null) {
                expiryDate = subscriptionService.getProgramSubscriptionExpiry(programSubscription);
                isViaProgramSubscription = true;
            } else if (packageProgramSubscription != null && programSubscription == null) {
                isViaPackageSubscription = true;
                expiryDate = subscriptionService.getPackageSubscriptionExpiry(packageProgramSubscription.getPackageSubscription());
            }
            profilingEndTimeInMillis = new Date().getTime();
            log.info("Checking whether program subscribed via program subscription or package subscription : Time taken in millis : " + (profilingEndTimeInMillis - profilingStartTimeInMillis));
            //Program not available for blocked member without subscription
            if (isMemberBlocked && programSubscription == null && packageProgramSubscription == null) {
                throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_BLOCKED_MEMBER_PROGRAM_NOT_AVAILABLE, MessageConstants.ERROR);
            }
            boolean isSubscriptionExpired = false;
            String orderStatus = null;
            boolean isOrderUnderProcessing = false;
            SubscriptionStatus subscriptionStatus = null;
            boolean setExpiryDate = false;
            PlatformType subscribedViaPlatform = null;
            profilingStartTimeInMillis = new Date().getTime();
            if (isViaProgramSubscription) {
                //program processing state is not updated in L2 page.So condition moved here.
                subscribedViaPlatform = programSubscription.getSubscribedViaPlatform();
                subscribedDate = programSubscription.getSubscribedDate();
                OrderManagement orderManagement = orderManagementRepository.findTop1ByUserAndProgramOrderByCreatedDateDesc(user, program);
                if (orderManagement != null && orderManagement.getOrderStatus() != null) {
                    orderStatus = orderManagement.getOrderStatus();
                    if (orderManagement.getOrderStatus().equalsIgnoreCase(KeyConstants.KEY_PROCESSING)) {
                        isOrderUnderProcessing = true;
                    }
                }
                subscriptionStatus = subscriptionService.getMemberProgramSubscriptionStatus(programSubscription);
                if (subscriptionStatus != null) {
                    String statusName = subscriptionStatus.getSubscriptionStatusName();
                    if (statusName.equals(KeyConstants.KEY_PAID) || statusName.equals(KeyConstants.KEY_PAYMENT_PENDING) || statusName.equals(KeyConstants.KEY_EXPIRED)) {
                        isNewSubscription = false;
                    }
                    if (KeyConstants.KEY_EXPIRED.equalsIgnoreCase(subscriptionStatus.getSubscriptionStatusName())) {
                        isSubscriptionExpired = true;
                    } else if (KeyConstants.KEY_TRIAL.equals(subscriptionStatus.getSubscriptionStatusName())) {
                        int completedWorkouts = workoutCompletionRepository.countByMemberUserIdAndProgramProgramId(user.getUserId(), programId);
                        int trialWorkouts = fitwiseUtils.getTrialWorkoutsCountForProgram(programSubscription.getUser(), programSubscription.getProgram());
                        if (completedWorkouts >= trialWorkouts) {
                            isSubscriptionExpired = true;
                        }
                    }
                    /**
                     * Setting whether the program subscription is Active along with Auto-Subscription Active status
                     * Also setting the order status  - Success/Failure/Processing
                     */
                    if (!subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_TRIAL)) {
                        if (programSubscription.getSubscribedViaPlatform().getPlatformTypeId() == 2) {
                            /**
                             * Program is subscribed via ios App
                             */
                            if (orderManagement != null) {
                                //Setting the order status based on the latest entry from the order management table
                                setExpiryDate = true;
                                if ((subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID)) || (subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAYMENT_PENDING))) {
                                    isProgramSubscribed = true;
                                }
                                // Setting whether the program's auto-susbcription is ON / OFF
                                AppleProductSubscription subscription = appleProductSubscriptionRepository.findTop1ByProgramAndUserOrderByModifiedDateDesc(program, user);
                                if (subscription != null && subscription.getAppleSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_ACTIVE)) {
                                    programAutoSubscribed = true;
                                }
                            }
                        } else {
                            /**
                             * Program is subscribed via Android / Web apps
                             */

                            // Setting whether the subscription is active for the program
                            if ((subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID)) || (subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAYMENT_PENDING))) {
                                isProgramSubscribed = true;
                                setExpiryDate = true;
                            }

                            // Setting the order status in the response
                            if (orderManagement != null) {
                                if (KeyConstants.KEY_AUTH_NET_PAYMENT_MODE.equals(orderManagement.getModeOfPayment())) {

                                    // Checking whether the program is auto-subscribed by the user
                                    AuthNetArbSubscription arbSubscription = authNetArbSubscriptionRepository.
                                            findTop1ByUserUserIdAndProgramProgramIdOrderByModifiedDateDesc(user.getUserId(), program.getProgramId());

                                    // Checking whether the auto-subscription is active or not
                                    if (arbSubscription != null && arbSubscription.getAuthNetSubscriptionStatus() != null &&
                                            arbSubscription.getAuthNetSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_ACTIVE)) {
                                        programAutoSubscribed = true;
                                    }

                                    AuthNetPayment authNetPayment = authNetPaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
                                    if (authNetPayment == null) {
                                        throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERROR_FETCHING_ORDER_TRANSACTION_DETAILS, null);
                                    }
                                    if (authNetPayment != null && authNetPayment.getResponseCode() != null && authNetPayment.getResponseCode().equalsIgnoreCase(KeyConstants.ANET_TRANSACTION_SUCCESS_RESPONSE_CODE)) {
                                        orderStatus = KeyConstants.KEY_SUCCESS;
                                    } else {
                                        orderStatus = KeyConstants.KEY_FAILURE;
                                    }
                                    memberProgramDetailResponseView.setIsARBUnderProcessing(authNetPayment.getIsARBUnderProcessing());
                                } else if (KeyConstants.KEY_STRIPE.equals(orderManagement.getModeOfPayment())) {

                                    StripeSubscriptionAndUserProgramMapping stripeSubscriptionAndUserProgramMapping = stripeSubscriptionAndUserProgramMappingRepository.findTop1ByUserUserIdAndProgramProgramIdOrderByModifiedDateDesc(user.getUserId(), program.getProgramId());
                                    if (stripeSubscriptionAndUserProgramMapping != null && stripeSubscriptionAndUserProgramMapping.getSubscriptionStatus() != null &&
                                            stripeSubscriptionAndUserProgramMapping.getSubscriptionStatus().getSubscriptionStatus().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_ACTIVE)) {
                                        programAutoSubscribed = true;
                                    }

                                    StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
                                    if (stripePayment == null) {
                                        throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERROR_FETCHING_ORDER_TRANSACTION_DETAILS, null);
                                    }
                                    // Getting the payment status
                                    if (stripePayment != null && KeyConstants.KEY_PAID.equals(stripePayment.getTransactionStatus())) {
                                        orderStatus = KeyConstants.KEY_SUCCESS;
                                    } else {
                                        orderStatus = KeyConstants.KEY_FAILURE;
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (isViaPackageSubscription) {
                subscriptionStatus = subscriptionService.getMemberPackageProgramSubscriptionStatus(packageProgramSubscription);
                subscribedDate = packageProgramSubscription.getSubscribedDate();

                subscribedViaPlatform = packageProgramSubscription.getPackageSubscription().getSubscribedViaPlatform();
                if (subscriptionStatus != null) {
                    if ((subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID)) || (subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAYMENT_PENDING))) {
                        isProgramSubscribed = true;
                        isProgramSubscribedThroughPackages = true;
                        setExpiryDate = true;
                    }
                    if (subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_EXPIRED)) {
                        isSubscriptionExpired = true;
                    }
                }
                OrderManagement orderManagement = orderManagementRepository.findTop1ByUserAndSubscriptionPackageOrderByCreatedDateDesc(user, packageProgramSubscription.getPackageSubscription().getSubscriptionPackage());
                if (orderManagement != null) {
                    orderStatus = orderManagement.getOrderStatus();
                    if (orderManagement.getOrderStatus() != null && orderManagement.getOrderStatus().equalsIgnoreCase(KeyConstants.KEY_PROCESSING)) {
                        isOrderUnderProcessing = true;
                    }
                    subscribedDate = packageProgramSubscription.getSubscribedDate();
                    if (KeyConstants.KEY_STRIPE.equals(orderManagement.getModeOfPayment())) {
                        StripeSubscriptionAndUserPackageMapping stripeSubscriptionAndUserPackageMapping = stripeSubscriptionAndUserPackageMappingRepository.findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageIdOrderByModifiedDateDesc(user.getUserId(), packageProgramSubscription.getPackageSubscription().getSubscriptionPackage().getSubscriptionPackageId());
                        if (stripeSubscriptionAndUserPackageMapping != null && stripeSubscriptionAndUserPackageMapping.getSubscriptionStatus() != null &&
                                stripeSubscriptionAndUserPackageMapping.getSubscriptionStatus().getSubscriptionStatus().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_ACTIVE)) {
                            programAutoSubscribed = true;
                        }
                        StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
                        if (stripePayment == null) {
                            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERROR_FETCHING_ORDER_TRANSACTION_DETAILS, null);
                        }
                        // Getting the payment status
                        if (stripePayment != null && KeyConstants.KEY_PAID.equals(stripePayment.getTransactionStatus())) {
                            orderStatus = KeyConstants.KEY_SUCCESS;
                        } else {
                            orderStatus = KeyConstants.KEY_FAILURE;
                        }
                    }
                }
            }
            if (subscriptionStatus != null) {
                isTrialOrSubscribed = subscriptionService.isTrialOrSubscribedByUser(subscriptionStatus);
            }
            memberProgramDetailResponseView.setOrderStatus(orderStatus);
            memberProgramDetailResponseView.setIsOrderUnderProcessing(isOrderUnderProcessing);
            if (setExpiryDate && expiryDate != null) {
                memberProgramDetailResponseView.setSubscriptionExpiry(expiryDate.getTime());
                memberProgramDetailResponseView.setSubscriptionExpiryDate(fitwiseUtils.formatDate(expiryDate));
                log.info("Expiry Date {} ", memberProgramDetailResponseView.getSubscriptionExpiryDate());
            }
            memberProgramDetailResponseView.setSubscribedViaPlatform(subscribedViaPlatform);
            if (subscribedDate != null) {
                memberProgramDetailResponseView.setSubscribedDate(subscribedDate);
                memberProgramDetailResponseView.setSubscribedDateFormatted(fitwiseUtils.formatDate(subscribedDate));
            }
            //Setting whether the program is subscribed or not to the response
            memberProgramDetailResponseView.setProgramSubscribed(isProgramSubscribed);
            memberProgramDetailResponseView.setProgramAutoSubscribed(programAutoSubscribed);
            memberProgramDetailResponseView.setProgramSubscribedThroughPackage(isProgramSubscribedThroughPackages);
            if (isSubscriptionExpired) {
                //Program not available for blocked member with expired subscription
                if (isMemberBlocked) {
                    throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_BLOCKED_MEMBER_PROGRAM_NOT_AVAILABLE, MessageConstants.ERROR);
                }
                //For unpublished and blocked programs, if the subscription is expired or trial completed, L2 page is not rendered
                if (isProgramBlockOrUnpublished) {
                    throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_MEMBER_PROGRAM_NOT_AVAILABLE, MessageConstants.ERROR);
                }
            }
            String status = null;
            if (subscriptionStatus != null) {
                if (subscriptionStatus.getSubscriptionStatusName().equals(KeyConstants.KEY_PAYMENT_PENDING) || subscriptionStatus.getSubscriptionStatusName().equals(KeyConstants.KEY_PAID)) {
                    status = KeyConstants.KEY_SUBSCRIBED;
                } else {
                    status = subscriptionStatus.getSubscriptionStatusName();
                }
                if (status.equals(KeyConstants.KEY_TRIAL)) {
                    isTrialSubscription = true;
                }
            }

            memberProgramDetailResponseView.setSubscriptionStatus(status);
        }
        if (isProgramSubscribed) {
            if (expiryDate != null) {
                LocalDate startLocalDate = LocalDate.now();
                LocalDate endLocalDate = expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                Period diff = Period.between(startLocalDate, endLocalDate);
                int validity = diff.getDays();
                if (validity == 0) {
                    subscriptionValidity = "Expires today";
                } else {
                    subscriptionValidity = "Expires in " + validity + " days";
                }
            }
        } else {
            subscriptionValidity = "SUBSCRIBE";
        }
        memberProgramDetailResponseView.setSubscriptionValidity(subscriptionValidity);
        profilingEndTimeInMillis = new Date().getTime();
        log.info("Subscription data construction : Time taken in millis : " + (profilingEndTimeInMillis - profilingStartTimeInMillis));
        List<Equipments> programEquipments = new ArrayList<>();
        List<MemberWorkoutScheduleResponseView> workoutScheduleResponseViews = new ArrayList<>();

        profilingStartTimeInMillis = new Date().getTime();
        int trialWorkoutScheduleCount = fitwiseUtils.getTrialWorkoutsCountForProgram(user, program);
        profilingEndTimeInMillis = new Date().getTime();
        log.info("Trial Workout count : Time taken in millis : " + (profilingEndTimeInMillis - profilingStartTimeInMillis));


        profilingStartTimeInMillis = new Date().getTime();
        List<WorkoutSchedule> workoutSchedules = program.getWorkoutSchedules();
        long totalUniqueWorkouts = workoutSchedules.stream()
                .filter(workoutSchedule -> workoutSchedule.getWorkout() != null)
                .map(workoutSchedule -> workoutSchedule.getWorkout().getWorkoutId())
                .distinct().count();
        memberProgramDetailResponseView.setNoOfWorkouts(Math.toIntExact(totalUniqueWorkouts));
        profilingEndTimeInMillis = new Date().getTime();
        log.info("Unique workouts : Time taken in millis : " + (profilingEndTimeInMillis - profilingStartTimeInMillis));


        profilingStartTimeInMillis = new Date().getTime();
        /*
         * Iterating through the workoutScheduleResponseViews to get the duration, total Exercise count,
         * workout completion status and Equipments list.
         */
        OptionalLong startOrder = workoutSchedules.stream().mapToLong(WorkoutSchedule::getOrder).min();
        for (WorkoutSchedule workoutSchedule : workoutSchedules) {
            MemberWorkoutScheduleResponseView workoutResponseView = new MemberWorkoutScheduleResponseView();
            workoutResponseView.setWorkoutScheduleId(workoutSchedule.getWorkoutScheduleId());
            Long order = workoutSchedule.getOrder();
            workoutResponseView.setOrder(order);
            // Setting trial as true until first two non-rest workouts as trial
            boolean isTrialWorkoutSchedule = false;
            if (order.intValue() <= trialWorkoutScheduleCount) {
                isTrialWorkoutSchedule = true;
            }
            workoutResponseView.setTrial(isTrialWorkoutSchedule);
            //previous workout completed date
            if (user != null) {
                WorkoutSchedule previousWorkoutSchedule = null;
                WorkoutCompletion previousWorkoutCompletion = null;
                if (isTrialOrSubscribed && order.longValue() != startOrder.getAsLong()) {
                    previousWorkoutSchedule = workoutScheduleRepository.findByOrderAndProgramsProgramId(order - 1, programId);
                    if (previousWorkoutSchedule != null) {
                        previousWorkoutCompletion = workoutCompletionRepository.findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdOrderByCompletedDateDesc(user.getUserId(), programId, previousWorkoutSchedule.getWorkoutScheduleId());
                        if (previousWorkoutCompletion != null) {
                            workoutResponseView.setPreviousWorkoutCompletionDate(previousWorkoutCompletion.getCompletedDate());
                        }
                    }
                }
                /*
                 * Workout schedule completion data
                 * Workout schedule - isToday data
                 * */
                //check whether workout is today
                boolean isTodayWorkout = false;
                if (isTrialOrSubscribed && (!(isTrialSubscription && !isTrialWorkoutSchedule))) {
                    //check whether workout to be played today
                    isTodayWorkout = isWorkoutToday(user.getUserId(), programId, order, startOrder.getAsLong(), previousWorkoutCompletion);
                }
                workoutResponseView.setTodayWorkout(isTodayWorkout);
                //if Workout is completed
                boolean isWorkoutCompleted = false;
                if (isTrialOrSubscribed) {
                    WorkoutCompletion workoutCompletion = workoutCompletionRepository.findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdOrderByCompletedDateDesc(user.getUserId(), programId, workoutSchedule.getWorkoutScheduleId());
                    if (workoutCompletion != null) {
                        isWorkoutCompleted = true;
                        workoutResponseView.setWorkoutCompletedOn(fitwiseUtils.formatDate(workoutCompletion.getCompletedDate()));
                        workoutResponseView.setWorkoutCompletionDate(workoutCompletion.getCompletedDate());
                        workoutResponseView.setWorkoutCompletionDateFormatted(fitwiseUtils.formatDate(workoutCompletion.getCompletedDate()));
                    }
                }
                workoutResponseView.setWorkoutCompleted(isWorkoutCompleted);

            }

            if (workoutSchedule.isRestDay()) {
                //Constructing Rest workout related data
                workoutResponseView.setRestDay(true);
                boolean isRestActivity = false;

                //Rest Activity data in workout schedule response
                InstructorRestActivity instructorRestActivity = workoutSchedule.getInstructorRestActivity();
                String title = DBConstants.REST;
                String workoutThumbnail = null;
                if (instructorRestActivity != null) {
                    String restActivityName = instructorRestActivity.getActivityName();
                    if (!DBConstants.REST.equalsIgnoreCase(restActivityName)) {
                        String metric = instructorRestActivity.getRestActivityToMetricMapping().getRestMetric().getRestMetric();
                        title = restActivityName + " - " + instructorRestActivity.getValue() + " " + metric;
                        isRestActivity = true;
                    }

                    RestActivity restActivity = instructorRestActivity.getRestActivityToMetricMapping().getRestActivity();
                    if (restActivity.getImage() != null) {
                        workoutThumbnail = restActivity.getImage().getImagePath();
                    }
                } else {
                    RestActivity restActivity = restActivityRepository.findByRestActivity(DBConstants.REST);
                    if (restActivity.getImage() != null) {
                        workoutThumbnail = restActivity.getImage().getImagePath();
                    }
                }
                workoutResponseView.setRestActivity(isRestActivity);
                workoutResponseView.setWorkoutTitle(title);
                workoutResponseView.setDisplayTitle(Convertions.getDayText(order) + ": " + title);
                workoutResponseView.setWorkoutThumbnail(workoutThumbnail);

            } else {
                //Constructing workout related data
                Workouts workout = workoutSchedule.getWorkout();
                workoutResponseView.setWorkoutId(workout.getWorkoutId());
                workoutResponseView.setWorkoutTitle(workout.getTitle());
                workoutResponseView.setDisplayTitle(Convertions.getDayText(order) + ": " + workout.getTitle());

                if (workout.getImage() != null) {
                    workoutResponseView.setWorkoutThumbnail(workout.getImage().getImagePath());
                }
                //Calculating circuit count and workout duration
                int circuitCount = 0;
                int workoutDuration = 0;
                boolean isVideoProcessingPending = false;
                for (CircuitSchedule circuitSchedule : workout.getCircuitSchedules()) {
                    long circuitDuration = 0;

                    boolean isRestCircuit = circuitSchedule.isRestCircuit();
                    if (isRestCircuit) {
                        circuitDuration = circuitSchedule.getRestDuration();
                    } else if (circuitSchedule.getCircuit() != null && (circuitSchedule.getIsAudio() == null || !circuitSchedule.getIsAudio())) {
                        circuitCount++;
                        Set<ExerciseSchedulers> exerciseSchedules = circuitSchedule.getCircuit().getExerciseSchedules();
                        for (ExerciseSchedulers exerciseScheduler : exerciseSchedules) {
                            long exerciseDuration = 0;
                            if (exerciseScheduler.getExercise() != null) {
                                if (exerciseScheduler.getExercise().getVideoManagement() != null) {
                                    exerciseDuration = exerciseScheduler.getExercise().getVideoManagement().getDuration();

                                    if (exerciseScheduler.getLoopCount() != null && exerciseScheduler.getLoopCount() > 0) {
                                        //Repeat Count Change : Since repeat count is changes as no of times video should play
                                        exerciseDuration = exerciseDuration * exerciseScheduler.getLoopCount();
                                    }

                                    if (fitwiseUtils.isVideoProcessingPending(exerciseScheduler.getExercise().getVideoManagement())) {
                                        isVideoProcessingPending = true;
                                    }
                                }
                                if (exerciseScheduler.getExercise().getEquipments() != null)
                                    programEquipments.addAll(exerciseScheduler.getExercise().getEquipments());
                            } else if (exerciseScheduler.getWorkoutRestVideo() != null) {
                                exerciseDuration = exerciseScheduler.getWorkoutRestVideo().getRestTime();
                            } else if (exerciseScheduler.getVoiceOver() != null) {
                                exerciseDuration = exerciseScheduler.getVoiceOver().getAudios().getDuration();
                            }
                            circuitDuration += exerciseDuration;
                        }
                        Long repeat = circuitSchedule.getRepeat();
                        Long restBetweenRepeat = circuitSchedule.getRestBetweenRepeat();
                        if (repeat != null && repeat > 0) {
                            //Repeat Count Change : Since repeat count is changes as no of times video should play
                            circuitDuration = circuitDuration * repeat;

                            long repeatRestDuration = 0;
                            if (restBetweenRepeat != null && restBetweenRepeat > 0) {
                                //Repeat Count Change : Since repeat count is changes as no of times video should play
                                repeatRestDuration = restBetweenRepeat * (repeat-1);
                            }
                            circuitDuration = circuitDuration  + repeatRestDuration;
                        }
                    } else if (circuitSchedule.getIsAudio() != null && circuitSchedule.getIsAudio()) {
                        for (CircuitAndVoiceOverMapping circuitAndVoiceOverMapping : circuitSchedule.getCircuit().getCircuitAndVoiceOverMappings()) {
                            circuitDuration += circuitAndVoiceOverMapping.getVoiceOver().getAudios().getDuration();
                        }
                    }
                    workoutDuration += circuitDuration;
                }
                workoutResponseView.setTotalCircuits(circuitCount);
                workoutResponseView.setDuration(workoutDuration);
                workoutResponseView.setVideoProcessingPending(isVideoProcessingPending);
            }
            workoutScheduleResponseViews.add(workoutResponseView);
        }
        profilingEndTimeInMillis = new Date().getTime();
        log.info("Workout schedule construction : Time taken in millis : " + (profilingEndTimeInMillis - profilingStartTimeInMillis));


        List<Equipments> programEquipmentsList = programEquipments.stream()
                .collect(collectingAndThen(toCollection(() -> new TreeSet<Equipments>(comparingLong(Equipments::getEquipmentId))), ArrayList::new));
        memberProgramDetailResponseView.setEquipments(programEquipmentsList);
        profilingStartTimeInMillis = new Date().getTime();
        int totalDays = program.getDuration().getDuration().intValue();
        int completedWorkouts = 0;
        String progress = null;
        int progressPercent = 0;
        Date completionDate = null;
        if (user != null) {
            profilingStartTimeInMillis = new Date().getTime();
            List<WorkoutCompletion> workoutCompletionList = workoutCompletionRepository.findByMemberUserIdAndProgramProgramId(user.getUserId(), program.getProgramId());
            completedWorkouts = workoutCompletionList.size();
            if (isTrialSubscription && completedWorkouts >= trialWorkoutScheduleCount) {
                memberProgramDetailResponseView.setTrialCompleted(true);
            }
            if (completedWorkouts == totalDays) {
                progress = KeyConstants.KEY_COMPLETED;
                progressPercent = 100;
                completionDate = workoutCompletionList.get(workoutCompletionList.size() - 1).getCompletedDate();
            } else {
                progress = Convertions.getNumberWithZero(completedWorkouts) + "/" + totalDays;
                progressPercent = (completedWorkouts * 100) / totalDays;
            }
        }
        memberProgramDetailResponseView.setProgress(progress);
        memberProgramDetailResponseView.setProgressPercent(progressPercent);
        memberProgramDetailResponseView.setCompletedWorkouts(completedWorkouts);
        memberProgramDetailResponseView.setProgramCompletionDate(completionDate);
        memberProgramDetailResponseView.setProgramCompletionDateFormatted(fitwiseUtils.formatDate(completionDate));
        profilingEndTimeInMillis = new Date().getTime();
        log.info("Program completion progress and status of completion : Time taken in millis : " + (profilingEndTimeInMillis - profilingStartTimeInMillis));

        if (program.getProgramExpertiseLevel() != null) {
            memberProgramDetailResponseView.setProgramLevel(program.getProgramExpertiseLevel().getExpertiseLevel());
        }
        if (program.getProgramType() != null) {
            memberProgramDetailResponseView.setProgramType(program.getProgramType().getProgramTypeName());
        }

        if (program.getDuration() != null) {
            memberProgramDetailResponseView.setProgramDuration(program.getDuration().getDuration());
        }

        // Sorting exercises based on order
        workoutScheduleResponseViews.sort(Comparator.comparing(MemberWorkoutScheduleResponseView::getOrder));
        memberProgramDetailResponseView.setWorkouts(workoutScheduleResponseViews);

        profilingStartTimeInMillis = new Date().getTime();
        ProgramViewsAudit programViewsAudit = new ProgramViewsAudit();
        programViewsAudit.setProgram(program);
        if (user != null) {
            programViewsAudit.setUser(user);
        }
        programViewsAudit.setDate(new Date());
        programViewsAuditRepository.save(programViewsAudit);
        profilingEndTimeInMillis = new Date().getTime();
        log.info("Program view audit update : Time taken in millis : " + (profilingEndTimeInMillis - profilingStartTimeInMillis));


        /** discount Offers **/
        if (!isViaPackageSubscription) {
            profilingStartTimeInMillis = new Date().getTime();
            List<DiscountOfferMapping> disList = dOfferMappingRepository.findByProgramsProgramIdAndOfferCodeDetailIsInUseAndOfferCodeDetailOfferStatus(programId, true, DiscountsConstants.OFFER_ACTIVE);
            if (disList != null && !disList.isEmpty()) {

                List<ProgramDiscountMappingResponseView> currentDiscounts = new ArrayList<>();
                List<ProgramDiscountMappingResponseView> freeOffers = new ArrayList<>();
                List<ProgramDiscountMappingResponseView> paidOffers = new ArrayList<>();
                List<ProgramDiscountMappingResponseView> upcomingDiscounts = new ArrayList<>();
                List<ProgramDiscountMappingResponseView> expiredDiscounts = new ArrayList<>();
                for (DiscountOfferMapping disOffer : disList) {
                    if (isNewSubscription == disOffer.getOfferCodeDetail().getIsNewUser().booleanValue()) {
                        ProgramDiscountMappingResponseView sResponseView = new ProgramDiscountMappingResponseView();

                        sResponseView.setOfferMappingId(disOffer.getOfferMappingId());
                        sResponseView.setOfferCodeId(disOffer.getOfferCodeDetail().getOfferCodeId());
                        sResponseView.setOfferName(disOffer.getOfferCodeDetail().getOfferName().trim());
                        sResponseView.setOfferCode(disOffer.getOfferCodeDetail().getOfferCode().toUpperCase());
                        sResponseView.setOfferMode(disOffer.getOfferCodeDetail().getOfferMode());
                        sResponseView.setOfferDuration(disOffer.getOfferCodeDetail().getOfferDuration());
                        sResponseView.setOfferStartDate(fitwiseUtils.formatDate(disOffer.getOfferCodeDetail().getOfferStartingDate()));
                        sResponseView.setOfferEndDate(fitwiseUtils.formatDate(disOffer.getOfferCodeDetail().getOfferEndingDate()));

                        sResponseView.setOfferPrice(disOffer.getOfferCodeDetail().getOfferPrice());
                        String formattedPrice;
                        if (disOffer.getOfferCodeDetail().getOfferPrice() != null) {
                            formattedPrice = fitwiseUtils.formatPrice(disOffer.getOfferCodeDetail().getOfferPrice().getPrice());
                        } else {
                            formattedPrice = KeyConstants.KEY_CURRENCY_US_DOLLAR + KeyConstants.KEY_DEFAULT_PRICE_FORMAT;
                        }
                        sResponseView.setFormattedOfferPrice(formattedPrice);
                        if (program.getProgramPrices() != null) {
                            double savingsAmount = program.getProgramPrices().getPrice();
                            if (disOffer.getOfferCodeDetail().getOfferPrice() != null) {
                                savingsAmount = program.getProgramPrices().getPrice() - disOffer.getOfferCodeDetail().getOfferPrice().getPrice();
                            }
                            DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
                            sResponseView.setFormattedSavingsAmount(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(savingsAmount));
                        }
                        sResponseView.setOfferStatus(disOffer.getOfferCodeDetail().getOfferStatus());
                        sResponseView.setIsNewUser(disOffer.getOfferCodeDetail().getIsNewUser());
                        //Offer Validity check
                        Date now = new Date();
                        Date offerStart = disOffer.getOfferCodeDetail().getOfferStartingDate();
                        Date offerEnd = disOffer.getOfferCodeDetail().getOfferEndingDate();
                        if (disOffer.getOfferCodeDetail().getOfferStatus().equalsIgnoreCase(DiscountsConstants.OFFER_ACTIVE) && disOffer.getOfferCodeDetail().isInUse()) {
                            // Current Offers
                            if ((offerStart.equals(now) || offerStart.before(now)) && (offerEnd.equals(now) || offerEnd.after(now))) {
                                if (disOffer.getOfferCodeDetail().getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_FREE)) {
                                    freeOffers.add(sResponseView);
                                } else if (disOffer.getOfferCodeDetail().getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_PAY_AS_YOU_GO)) {
                                    paidOffers.add(sResponseView);
                                }
                            }
                            // UpComing Offers
                            else if (offerStart.after(now) && offerEnd.after(now)) {
                                upcomingDiscounts.add(sResponseView);
                            }
                        } else {
                            // Expired Offers
                            if (offerStart.before(now) && offerEnd.before(now)) {
                                expiredDiscounts.add(sResponseView);
                            }
                        }
                    }
                }
                ProgramDiscountMappingListResponseView discountOffers = new ProgramDiscountMappingListResponseView();
                paidOffers.sort((ProgramDiscountMappingResponseView f1, ProgramDiscountMappingResponseView f2) -> f1.compareTo(f2));
                freeOffers.sort((ProgramDiscountMappingResponseView f1, ProgramDiscountMappingResponseView f2) -> f1.compareTo(f2));
                Collections.sort(freeOffers, Collections.reverseOrder());
                freeOffers.stream().collect(toCollection(() -> currentDiscounts));
                paidOffers.stream().collect(toCollection(() -> currentDiscounts));
                discountOffers.setCurrentDiscounts(currentDiscounts);
                discountOffers.setUpcomingDiscounts(upcomingDiscounts);
                discountOffers.setExpiredDiscounts(expiredDiscounts);
                memberProgramDetailResponseView.setDiscountOffers(discountOffers);
                profilingEndTimeInMillis = new Date().getTime();
                log.info("Offers query and construction : Time taken in millis : " + (profilingEndTimeInMillis - profilingStartTimeInMillis));
            }
        }
        profilingEndTimeInMillis = new Date().getTime();
        log.info("Get Program Details API : Time taken in millis : " + (profilingEndTimeInMillis - startTimeInMllis));
        return memberProgramDetailResponseView;
    }

    /**
     * Check whether the given workout is for today
     *
     * @param userId
     * @param programId
     * @param order
     * @param startOrder
     * @return
     */
    public boolean isWorkoutToday(long userId, long programId, long order, long startOrder, WorkoutCompletion previousWorkoutCompletion) {
        boolean isTodayWorkout;
        boolean populateRestCompletion = false;
        Date restCompletionDate = new Date();
        User user = userComponents.getUser();
        Date today = new Date();
        WorkoutSchedule workoutSchedules = workoutScheduleRepository.findByOrderAndProgramsProgramId(order, programId);
        WorkoutCompletion workoutCompletion = workoutCompletionRepository.findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdOrderByCompletedDateDesc(userId, programId, workoutSchedules.getWorkoutScheduleId());
        if (workoutCompletion != null) {
            isTodayWorkout = fitwiseUtils.isSameDay(workoutCompletion.getCompletedDate(), today) ? true : false;
            return isTodayWorkout;
        }
        if (order == startOrder) {
            isTodayWorkout = true;
            populateRestCompletion = true;
            restCompletionDate = new Date();
        } else {
            //Check if previous workout completed
            if (previousWorkoutCompletion == null) {
                isTodayWorkout = false;
            } else {
                if (fitwiseUtils.isSameDay(previousWorkoutCompletion.getCompletedDate(), today)) {
                    isTodayWorkout = false;
                } else {
                    isTodayWorkout = true;

                    //if previous workout is completed and today is restDay
                    if (workoutSchedules.isRestDay()) {
                        //If rest day was completed today, return true
                        populateRestCompletion = true;
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(previousWorkoutCompletion.getCompletedDate());
                        calendar.add(Calendar.DAY_OF_YEAR, 1);
                        restCompletionDate = calendar.getTime();
                    }
                }
            }
        }
        boolean isRest = isRestDay(workoutSchedules);
        if (populateRestCompletion && isRest) {
            WorkoutCompletion restWorkoutCompletion = workoutCompletionRepository.findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdOrderByCompletedDateDesc(userId, programId, workoutSchedules.getWorkoutScheduleId());
            if (restWorkoutCompletion == null) {
                restWorkoutCompletion = new WorkoutCompletion();
                Programs programs = programRepository.findByProgramId(programId);
                restWorkoutCompletion.setProgram(programs);
                restWorkoutCompletion.setWorkoutScheduleId(workoutSchedules.getWorkoutScheduleId());
                restWorkoutCompletion.setMember(user);
                restWorkoutCompletion.setCompletedDate(restCompletionDate);
                workoutCompletionRepository.save(restWorkoutCompletion);
            }
        }
        return isTodayWorkout;
    }

    private boolean isRestDay(WorkoutSchedule workoutSchedule) {
        boolean isRest = false;
        if (workoutSchedule.isRestDay()) {
            if (workoutSchedule.getInstructorRestActivity() == null) {
                isRest = true;
            } else if (workoutSchedule.getInstructorRestActivity() != null && DBConstants.REST.equals(workoutSchedule.getInstructorRestActivity().getRestActivityToMetricMapping().getRestActivity().getRestActivity())) {
                isRest = true;
            }
        }
        return isRest;
    }

    public MemberWorkoutDetailResponseView getWorkoutDetails(Long workoutId, Long workoutScheduleId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        log.info("Get workout details starts");
        long start = new Date().getTime();
        long profilingStart;
        //Validation for workoutSchedule
        if (workoutScheduleId == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_ID_NULL, MessageConstants.ERROR);
        }
        WorkoutSchedule workoutSchedule = workoutScheduleRepository.findByWorkoutScheduleIdAndWorkoutWorkoutId(workoutScheduleId, workoutId);
        if (workoutSchedule == null) {
            throw new ApplicationException(Constants.NOT_FOUND, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_NOT_FOUND, MessageConstants.ERROR);
        }
        log.info("Basic validations : Time taken in millis : "+(new Date().getTime() - start));
        //Response constructor from overloaded method
        MemberWorkoutDetailResponseView memberWorkoutDetailResponseView = getWorkoutDetails(workoutId);
        profilingStart = new Date().getTime();
        //Display name set in response
        memberWorkoutDetailResponseView.setWorkoutScheduleId(workoutSchedule.getWorkoutScheduleId());
        memberWorkoutDetailResponseView.setDisplayTitle(Convertions.getDayText(workoutSchedule.getOrder()) + ": " + memberWorkoutDetailResponseView.getWorkoutName());
        log.info("Set workout schedule id and display title : Time taken in millis : "+(new Date().getTime() - profilingStart));
        log.info("Get workout details : Total Time taken in millis : "+(new Date().getTime() - start));
        log.info("Get workout details ends");
        return memberWorkoutDetailResponseView;
    }

    /**
     * Method used to get the workout details based on userid and workoutID
     *
     * @param workoutId
     * @return
     * @throws ApplicationException
     */
    public MemberWorkoutDetailResponseView getWorkoutDetails(Long workoutId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        long start = new Date().getTime();
        log.info("Construct member workout details response view started : Time taken in millis : "+start);
        long profilingStart;
        userComponents.getUser();
        if (workoutId == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_ID_NULL, MessageConstants.ERROR);
        }
        //Getting workouts mapped for the given workoutId.
        Workouts workout = workoutRepository.findByWorkoutId(workoutId);
        if (workout == null) {
            throw new ApplicationException(Constants.NOT_FOUND, ValidationMessageConstants.MSG_WORKOUT_NOT_FOUND, MessageConstants.ERROR);
        }
        log.info("Query workout repository : Time taken in millis : " + (new Date().getTime() - start));
        profilingStart = new Date().getTime();
        MemberWorkoutDetailResponseView memberWorkoutDetailResponseView = new MemberWorkoutDetailResponseView();
        memberWorkoutDetailResponseView.setWorkoutName(workout.getTitle());
        if (workout.getImage() != null)
            memberWorkoutDetailResponseView.setWorkoutThumbnail(workout.getImage().getImagePath());
        log.info("Set workout title and thumbnail : Time taken in millis : " + (new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();
        long duration = 0;
        if (workout.getCircuitSchedules() != null && !workout.getCircuitSchedules().isEmpty()) {
            List<CircuitScheduleResponseView> circuitScheduleViewList = new ArrayList<>();
            int circuitCount = 0;
            boolean isWorkoutVideoProcessingPending = false;
            for (CircuitSchedule circuitSchedule : workout.getCircuitSchedules()) {
                CircuitScheduleResponseView circuitScheduleView = new CircuitScheduleResponseView();
                circuitScheduleView.setCircuitScheduleId(circuitSchedule.getCircuitScheduleId());
                circuitScheduleView.setOrder(circuitSchedule.getOrder());
                long circuitDuration = 0;
                boolean isRestCircuit = circuitSchedule.isRestCircuit();
                circuitScheduleView.setRestCircuit(isRestCircuit);
                if (isRestCircuit) {
                    circuitDuration = circuitSchedule.getRestDuration();
                } else if (circuitSchedule.getIsAudio() != null && circuitSchedule.getIsAudio()) {
                    circuitScheduleView.setAudio(circuitSchedule.getIsAudio());
                    List<AudioResponseView> audioResponseViews = new ArrayList<>();
                    for (CircuitAndVoiceOverMapping circuitAndVoiceOverMapping : circuitSchedule.getCircuit().getCircuitAndVoiceOverMappings()) {
                        AudioResponseView audioResponseView = new AudioResponseView();
                        audioResponseView.setAudioId(circuitAndVoiceOverMapping.getVoiceOver().getAudios().getAudioId());
                        audioResponseView.setFilePath(circuitAndVoiceOverMapping.getVoiceOver().getAudios().getFilePath());
                        audioResponseView.setDuration(circuitAndVoiceOverMapping.getVoiceOver().getAudios().getDuration());
                        audioResponseView.setTitle(circuitAndVoiceOverMapping.getVoiceOver().getTitle());
                        audioResponseView.setVoiceOverId(circuitAndVoiceOverMapping.getVoiceOver().getVoiceOverId());
                        audioResponseView.setCircuitAndVoiceOverMappingId(circuitAndVoiceOverMapping.getCircuitAndVoiceOverMappingId());
                        audioResponseViews.add(audioResponseView);
                        circuitDuration += circuitAndVoiceOverMapping.getVoiceOver().getAudios().getDuration();
                    }
                    circuitScheduleView.setCircuitId(circuitSchedule.getCircuit().getCircuitId());
                    circuitScheduleView.setAudioResponseView(audioResponseViews);
                    circuitScheduleView.setDuration(duration);
                } else {
                    circuitCount++;
                    circuitScheduleView.setCircuitId(circuitSchedule.getCircuit().getCircuitId());
                    circuitScheduleView.setCircuitTitle(circuitSchedule.getCircuit().getTitle());
                    Long repeat = circuitSchedule.getRepeat();
                    circuitScheduleView.setRepeat(repeat);
                    Long restBetweenRepeat = circuitSchedule.getRestBetweenRepeat();
                    circuitScheduleView.setRestBetweenRepeat(restBetweenRepeat);
                    Set<ExerciseSchedulers> exerciseSchedules = circuitSchedule.getCircuit().getExerciseSchedules();
                    int exerciseCount = 0;
                    List<String> exerciseThumbnails = new ArrayList<>();
                    boolean isCircuitVideoProcessingPending = false;
                    for (ExerciseSchedulers schedule : exerciseSchedules) {
                        long exerciseDuration = 0;
                        if (schedule.getExercise() != null) {
                            if (schedule.getExercise().getVideoManagement() != null) {
                                exerciseDuration = schedule.getExercise().getVideoManagement().getDuration();
                                if (schedule.getLoopCount() != null && schedule.getLoopCount() > 0) {
                                    //Repeat Count Change : Since repeat count is changes as no of times video should play
                                    exerciseDuration = exerciseDuration * schedule.getLoopCount();
                                }
                                if (schedule.getExercise().getVideoManagement().getThumbnail() != null) {
                                    exerciseThumbnails.add(schedule.getExercise().getVideoManagement().getThumbnail().getImagePath());
                                }
                                if (fitwiseUtils.isVideoProcessingPending(schedule.getExercise().getVideoManagement())) {
                                    isWorkoutVideoProcessingPending = true;
                                    isCircuitVideoProcessingPending = true;
                                }
                            }
                            exerciseCount++;
                        } else if (schedule.getWorkoutRestVideo() != null) {
                            exerciseDuration = schedule.getWorkoutRestVideo().getRestTime();
                        } else if (schedule.getVoiceOver() != null) {
                            exerciseDuration = schedule.getVoiceOver().getAudios().getDuration();
                        }
                        circuitDuration += exerciseDuration;
                    }
                    if (repeat != null && repeat > 0) {
                        //Repeat Count Change : Since repeat count is changes as no of times video should play
                        circuitDuration = circuitDuration * repeat;

                        long repeatRestDuration = 0;
                        if (restBetweenRepeat != null && restBetweenRepeat > 0) {
                            //Repeat Count Change : Since repeat count is changes as no of times video should play
                            repeatRestDuration = restBetweenRepeat * (repeat-1);
                        }
                        circuitDuration = circuitDuration  + repeatRestDuration;
                    }
                    circuitScheduleView.setExerciseCount(exerciseCount);
                    circuitScheduleView.setExerciseThumbnails(exerciseThumbnails);
                    circuitScheduleView.setVideoProcessingPending(isCircuitVideoProcessingPending);
                }

                circuitScheduleView.setDuration(circuitDuration);
                duration += circuitDuration;

                circuitScheduleViewList.add(circuitScheduleView);
            }

            Collections.sort(circuitScheduleViewList, Comparator.comparing(CircuitScheduleResponseView::getOrder));
            memberWorkoutDetailResponseView.setCircuitSchedules(circuitScheduleViewList);
            memberWorkoutDetailResponseView.setWorkoutDuration(duration);
            memberWorkoutDetailResponseView.setCircuitCount(circuitCount);
            memberWorkoutDetailResponseView.setVideoProcessingPending(isWorkoutVideoProcessingPending);
        }
        log.info("Workout response construction : Time taken in millis : " + (new Date().getTime() - profilingStart));

        log.info("Total time taken for member workout details response model construction : Total Time taken in millis : " + (new Date().getTime() - start));

        return memberWorkoutDetailResponseView;
    }

    /**
     * Get expertise and duration filter
     * @return
     */
    public List<MemberFilterView> getProgramsbyTypeFilter() {
        MemberFilterView expertiseFilter = new MemberFilterView();
        expertiseFilter.setFilterName(KeyConstants.KEY_EXPERTISE_FILTER_NAME);
        expertiseFilter.setType(KeyConstants.KEY_EXPERTISE_FILTER_TYPE);
        List<ExpertiseLevels> expertiseLevelList = expertiseLevelRepository.findAll();
        List<Filter> expertiseLevelFilterList = new ArrayList<>();
        for (ExpertiseLevels expertiseLevel : expertiseLevelList) {
            if (!expertiseLevel.getExpertiseLevel().equalsIgnoreCase(KeyConstants.KEY_ALL_LEVELS)) {
                Filter filter = new Filter();
                filter.setFilterId(expertiseLevel.getExpertiseLevelId());
                filter.setFilterName(expertiseLevel.getExpertiseLevel());
                expertiseLevelFilterList.add(filter);
            }
        }
        expertiseFilter.setFilters(expertiseLevelFilterList);
        MemberFilterView durationFilter = new MemberFilterView();
        durationFilter.setFilterName(KeyConstants.KEY_DURATION_FILTER_NAME);
        durationFilter.setType(KeyConstants.KEY_DURATION_FILTER_TYPE);
        List<Duration> durationList = durationRepo.findAllByOrderByDurationAsc();
        List<Filter> durationFilterList = new ArrayList<>();
        for (Duration duration : durationList) {
            Filter filter = new Filter();
            filter.setFilterId(duration.getDurationId());
            filter.setFilterName(duration.getDuration() + " Days");
            durationFilterList.add(filter);
        }
        durationFilter.setFilters(durationFilterList);
        List<MemberFilterView> programsFilterViewList = new ArrayList<>();
        programsFilterViewList.add(expertiseFilter);
        programsFilterViewList.add(durationFilter);
        return programsFilterViewList;
    }

    /**
     * programs by type
     *
     * @param pageNo
     * @param pageSize
     * @param programTypeId
     * @param programFilterModel
     * @param search
     * @return
     */
    public ProgramTypeWithProgramTileModel getProgramsByType(int pageNo, int pageSize, Long programTypeId, MemberProgramsFilterModel programFilterModel, Optional<String> search) {
        long start = new Date().getTime();
        long profilingStartTimeInMillis;
        long profilingEndTimeInMillis;
        RequestParamValidator.pageSetup(pageNo, pageSize);
        ProgramTypeWithProgramTileModel programTypeWithProgramTileModel = new ProgramTypeWithProgramTileModel();
        ProgramTypes programTypes = programTypeRepository.findByProgramTypeId(programTypeId);
        programTypeWithProgramTileModel.setProgramTypeId(programTypeId);
        programTypeWithProgramTileModel.setProgramTypeName(programTypes.getProgramTypeName());
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize);
        profilingStartTimeInMillis = new Date().getTime();
        Specification<Programs> programStatusSpec = ProgramSpecifications.getProgramByStatus(KeyConstants.KEY_PUBLISH);
        Specification<Programs> programTypeSpec = ProgramSpecifications.getProgramByType(programTypeId);
        Specification<Programs> stripeActiveSpec = ProgramSpecifications.getProgramByStripeMapping();
        Specification<Programs> finalSpec;
        Page<Programs> programs;
        if ((programFilterModel.getDuration() == null || programFilterModel.getDuration().isEmpty()) && (programFilterModel.getExpertiseLevel() == null || programFilterModel.getExpertiseLevel().isEmpty())) {
            if (search.isPresent() && !search.get().isEmpty()) {
                Specification<Programs> titleSearchSpec = ProgramSpecifications.getProgramByTitleContains(search.get());
                finalSpec = programStatusSpec.and(programTypeSpec).and(titleSearchSpec).and(stripeActiveSpec);
            } else {
                finalSpec = programStatusSpec.and(programTypeSpec).and(stripeActiveSpec);
            }
        } else {

            List<Long> expertiseLevelIdList;
            if (programFilterModel.getExpertiseLevel() != null && !programFilterModel.getExpertiseLevel().isEmpty()) {
                expertiseLevelIdList = programFilterModel.getExpertiseLevel().stream().map(Filter::getFilterId).collect(Collectors.toList());
                ExpertiseLevels expertiseLevel = expertiseLevelRepository.findByExpertiseLevel(KeyConstants.KEY_ALL_LEVELS);
                expertiseLevelIdList.add(expertiseLevel.getExpertiseLevelId());
            } else {
                expertiseLevelIdList = expertiseLevelRepository.findAll().stream().map(ExpertiseLevels::getExpertiseLevelId).collect(Collectors.toList());
            }
            Specification<Programs> programExpertiseSpec = ProgramSpecifications.getProgramByExpertiseIn(expertiseLevelIdList);

            List<Long> durationIdList;
            if (programFilterModel.getDuration() != null && !programFilterModel.getDuration().isEmpty()) {
                durationIdList = programFilterModel.getDuration().stream().map(Filter::getFilterId).collect(Collectors.toList());
            } else {
                durationIdList = durationRepo.findAllByOrderByDurationAsc().stream().map(Duration::getDurationId).collect(Collectors.toList());
            }
            Specification<Programs> durationSpec = ProgramSpecifications.getProgramByDurationIn(durationIdList);
            if (search.isPresent() && !search.get().isEmpty()) {
                Specification<Programs> titleSearchSpec = ProgramSpecifications.getProgramByTitleContains(search.get());
                finalSpec = programStatusSpec.and(programTypeSpec).and(programExpertiseSpec).and(durationSpec).and(titleSearchSpec).and(stripeActiveSpec);
            } else {
                finalSpec = programStatusSpec.and(programTypeSpec).and(programExpertiseSpec).and(durationSpec).and(stripeActiveSpec);
            }
        }
        programs = programRepository.findAll(finalSpec, pageRequest);
        profilingEndTimeInMillis = new Date().getTime();
        log.info("Querying paginated data : Total time taken in millis : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));

        if (programs.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        programTypeWithProgramTileModel.setProgramsCount(programs.getTotalElements());
        User user = userComponents.getAndValidateUser();
        profilingStartTimeInMillis = new Date().getTime();
        List<Long> programIdList = programs.stream().map(Programs::getProgramId).collect(Collectors.toList());
        Map<Long, Long> offerCountMap = discountsService.getNoOfCurrentAvailableOffersOfProgramsForMember(programIdList, user);
        log.info(StringConstants.LOG_OFFER_COUNT_QUERY + (new Date().getTime() - profilingStartTimeInMillis));
        profilingEndTimeInMillis = new Date().getTime();
        log.info("Constructing program tile models : Time taken in  millis : "+(profilingEndTimeInMillis-profilingStartTimeInMillis));
        programTypeWithProgramTileModel.setPrograms(programDataParsing.constructProgramTileModelWithFreeAccess(programs.getContent(), offerCountMap));
        profilingEndTimeInMillis = new Date().getTime();
        log.info("Get program by type API : Total Time taken in  millis : "+(profilingEndTimeInMillis-start));
        return programTypeWithProgramTileModel;
    }

    /**
     * Used to return my programs list
     *
     * @param subscriptionStatusParam
     * @param pageNo
     * @param pageSize
     * @param searchName
     * @return
     */
    @Transactional
    public Map<String, Object> getMyPrograms(String subscriptionStatusParam, int pageNo, int pageSize, Optional<String> searchName) {
        log.info("MyPrograms starts.");
        long apiStartTimeMillis = System.currentTimeMillis();
        RequestParamValidator.pageSetup(pageNo, pageSize);
        List<String> statusList = new ArrayList<>();
        //subscriptionStatusParam validation
        if (subscriptionStatusParam == null || subscriptionStatusParam.isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_STATUS_PARAM_NULL, null);
        }
        if (!(subscriptionStatusParam.equalsIgnoreCase(KeyConstants.KEY_TRIAL) || subscriptionStatusParam.equalsIgnoreCase(KeyConstants.KEY_PAID) || subscriptionStatusParam.equalsIgnoreCase(KeyConstants.KEY_EXPIRED))) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_STATUS_PARAM_INCORRECT, null);
        }
        User user = userComponents.getUser();
        List<MyProgramView> myProgramViewList = new ArrayList<>();
        Set<Long> programsCollected = freeAccessService.getFreeProductProgramsIds();
        
        // User specific package programIds
        Set<Long> supscriptionPackageProgramIds = freeAccessService.getUserSpecificFreeAccessPackageProgramsIds();    
        if (subscriptionStatusParam.equalsIgnoreCase(KeyConstants.KEY_TRIAL)) {
            statusList.add(KeyConstants.KEY_TRIAL);
        } else if (subscriptionStatusParam.equalsIgnoreCase(KeyConstants.KEY_PAID)) {
            statusList.add(KeyConstants.KEY_PAID);
            statusList.add(KeyConstants.KEY_PAYMENT_PENDING);
            for(Long programId : programsCollected){
            	Programs freeProduct = programRepository.findByProgramId(programId);
                if(freeProduct != null){
                    List<WorkoutCompletionDAO> workoutCompletionDAOList = programJpa.getWorkoutCompletionListByProgramIdList(Arrays.asList(freeProduct.getProgramId()), user.getUserId());
                    MyProgramView myProgramView = new MyProgramView();
                    Long completedWorkouts = 0L;
                    Date lastWorkoutCompletionDate = null;
                    if(!workoutCompletionDAOList.isEmpty() && workoutCompletionDAOList.get(0).getWorkoutCompletionCount() != null && workoutCompletionDAOList.get(0).getLastWorkoutCompletionDate() != null){
                        completedWorkouts = workoutCompletionDAOList.get(0).getWorkoutCompletionCount();
                        lastWorkoutCompletionDate = workoutCompletionDAOList.get(0).getLastWorkoutCompletionDate();
                    }
                    constructMyProgramView(myProgramView, freeProduct, completedWorkouts, lastWorkoutCompletionDate);
                    myProgramView.setFreeToAccess(true);
                    
                    FreeProduct freeProductObj = null;
                    if(supscriptionPackageProgramIds.contains(programId)) { // Program id present in subscription package list
                    	freeProductObj = freeAccessService.getFreeUserSpecAccessByUserAndPackageProgramId(user, programId);
                    }else {
                    	// Checking specific user related
                    	freeProductObj = freeAccessService.getFreeUserSpecAccessByUserAndProgramId(user, programId);
                    	// checking in all type
                    	if(freeProductObj == null) {
                    		FreeAccessProgram freeProgramTypeAll = freeAccessService.getAllUsersFreeProgramsByProgram(freeProduct);
    						if (freeProgramTypeAll != null)
    							freeProductObj = freeProgramTypeAll.getFreeProduct();
                    	}
                    }
					if (freeProductObj != null && freeProductObj.getType()
							.equalsIgnoreCase(DBConstants.FREE_ACCESS_TYPE_INDIVIDUAL)) {
						myProgramView.setSubscribedDate(freeProductObj.getFreeAccessStartDate());
					} else {
						myProgramView.setSubscribedDate(freeProductObj.getUpdatedOn());
					}
					
                    Date startedDate;
                    if (workoutCompletionDAOList.get(0).getFirstWorkoutCompletionDate() != null) {
                        startedDate = workoutCompletionDAOList.get(0).getFirstWorkoutCompletionDate();
                    } else {
                        startedDate = freeProductObj != null ? freeProductObj.getUpdatedOn() : new Date();
                    }
                    myProgramView.setStartDateTimeStamp(startedDate);
                    if (startedDate != null) {
                        myProgramView.setStartDate(fitwiseUtils.formatDate(startedDate));
                    }
                    myProgramViewList.add(myProgramView);
                }
            }
        } else if (subscriptionStatusParam.equalsIgnoreCase(KeyConstants.KEY_EXPIRED)) {
            statusList.add(KeyConstants.KEY_PAID);
            statusList.add(KeyConstants.KEY_PAYMENT_PENDING);
            statusList.add(KeyConstants.KEY_EXPIRED);
        }
        long profilingStartTimeMillis = System.currentTimeMillis();
        List<ProgramSubscriptionDAOWithStripe> programSubscriptionDAOS = subscriptionService.getActiveMemberProgramSubscriptionsDAO(statusList, user.getUserId(), searchName, subscriptionStatusParam);
        long profilingEndTimeMillis = System.currentTimeMillis();
        log.info("ProgramSubscription Query : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        profilingStartTimeMillis = System.currentTimeMillis();
        List<Long> programIdList = programSubscriptionDAOS.stream().map(programSubscriptionDAO -> programSubscriptionDAO.getProgram().getProgramId()).collect(Collectors.toList());
        Map<Long, Long> offerCountMap = discountsService.getNoOfCurrentAvailableOffersOfProgramsForMember(programIdList, user);
        log.info(StringConstants.LOG_OFFER_COUNT_QUERY + (System.currentTimeMillis() - profilingStartTimeMillis));
        profilingStartTimeMillis = System.currentTimeMillis();
        for (ProgramSubscriptionDAOWithStripe programSubscriptionDAO : programSubscriptionDAOS) {
            //TODO Temporarily handled by skipping the deleted programs in expired list. Have to work on proper implementation
            if (!KeyConstants.KEY_DELETED.equalsIgnoreCase(programSubscriptionDAO.getProgram().getStatus()) && !programsCollected.contains(programSubscriptionDAO.getProgram().getProgramId())) {
                MyProgramView myProgram = constructMyProgramView(programSubscriptionDAO.getSubscriptionStatus(), programSubscriptionDAO.getOrderManagement(), programSubscriptionDAO.getSubscribedViaPlatform()
                        , programSubscriptionDAO.getSubscribedDate(), programSubscriptionDAO.getSubscriptionPlan(), programSubscriptionDAO.getProgram(), KeyConstants.KEY_PROGRAM,
                        subscriptionStatusParam, offerCountMap, programSubscriptionDAO.getStripeTransactionStatus(), programSubscriptionDAO.getAppleExpiryDate(),
                        programSubscriptionDAO.getWorkoutCompletionCount(), programSubscriptionDAO.getFirstWorkoutCompletionDate(), programSubscriptionDAO.getLastWorkoutCompletionDate());
                myProgramViewList.add(myProgram);
            }
        }
        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("ProgramSubscription data construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        profilingStartTimeMillis = System.currentTimeMillis();
        List<PackageSubscriptionDAOWithStripe> packageSubscriptionDAOS = subscriptionService.getActivePackageSubscriptionDAOForMember(statusList, user.getUserId(), searchName, subscriptionStatusParam);
        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("PackageSubscription Query : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        profilingStartTimeMillis = System.currentTimeMillis();
        List<Long> idList = packageSubscriptionDAOS.stream().map(packageSubscriptionDAO -> packageSubscriptionDAO.getProgram().getProgramId()).collect(Collectors.toList());
        Map<Long, Long> offerMap = discountsService.getNoOfCurrentAvailableOffersOfProgramsForMember(idList, user);
        log.info(StringConstants.LOG_OFFER_COUNT_QUERY + (System.currentTimeMillis() - profilingStartTimeMillis));
        profilingStartTimeMillis = System.currentTimeMillis();
        for (PackageSubscriptionDAOWithStripe packageSubscriptionDAO : packageSubscriptionDAOS) {
            //TODO Temporarily handled by skipping the deleted programs in expired list. Have to work on proper implementation
            if (!KeyConstants.KEY_DELETED.equalsIgnoreCase(packageSubscriptionDAO.getProgram().getStatus())&& !programsCollected.contains(packageSubscriptionDAO.getProgram().getProgramId())) {
                MyProgramView myProgram = constructMyProgramView(packageSubscriptionDAO.getSubscriptionStatus(), packageSubscriptionDAO.getOrderManagement(), packageSubscriptionDAO.getSubscribedViaPlatform()
                        , packageSubscriptionDAO.getSubscribedDate(), packageSubscriptionDAO.getSubscriptionPlan(), packageSubscriptionDAO.getProgram(), KeyConstants.KEY_SUBSCRIPTION_PACKAGE, subscriptionStatusParam, offerMap,
                        KeyConstants.KEY_PAID, null, packageSubscriptionDAO.getWorkoutCompletionCount(), packageSubscriptionDAO.getFirstWorkoutCompletionDate(),
                        packageSubscriptionDAO.getLastWorkoutCompletionDate());
                myProgramViewList.add(myProgram);
            }
        }
        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("PackageSubscription data construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        profilingStartTimeMillis = System.currentTimeMillis();
        //Removing duplicate programs based on subscription expiry date
        myProgramViewList = myProgramViewList.stream()
                .collect(Collectors.groupingBy(MyProgramView::getProgramId, Collectors.maxBy(Comparator.comparing(MyProgramView::getSubscriptionExpiry, Comparator.nullsFirst(Comparator.naturalOrder())))))
                .values()
                .stream()
                .map(Optional::get)
                .collect(Collectors.toList());
        int fromIndex = (pageNo - 1) * pageSize;
        if (myProgramViewList.isEmpty() || myProgramViewList.size() < fromIndex) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        int totalCount = myProgramViewList.size();
        if (subscriptionStatusParam.equalsIgnoreCase(KeyConstants.KEY_EXPIRED)) {
            //Sorting my program list based on latest expired first.
            myProgramViewList.sort(Comparator.comparing(MyProgramView::getSubscriptionExpiry, Comparator.nullsFirst(Comparator.naturalOrder())).reversed().thenComparing(MyProgramView::getTitle, String.CASE_INSENSITIVE_ORDER));
        } else {
            //Sorting my program list based on latest subscribed first.
            myProgramViewList.sort(Comparator.comparing(MyProgramView::getSubscribedDate).reversed().thenComparing(MyProgramView::getTitle, String.CASE_INSENSITIVE_ORDER));
        }
        myProgramViewList = myProgramViewList.subList(fromIndex, Math.min(fromIndex + pageSize, myProgramViewList.size()));
        if (myProgramViewList.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Sorting and sublist : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        Map<String, Object> respMap = new HashMap<>();
        respMap.put(KeyConstants.KEY_PROGRAMS, myProgramViewList);
        respMap.put(KeyConstants.KEY_TOTAL_COUNT, totalCount);
        long apiEndTimeMillis = System.currentTimeMillis();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("MyPrograms ends.");
        return respMap;
    }


    /**
     * Method to construct My program view
     *
     * @param subscriptionStatus
     * @param orderManagement
     * @param subscribedViaPlatform
     * @param subscriptionDate
     * @param subscriptionPlan
     * @param program
     * @return
     */
    private MyProgramView constructMyProgramView(SubscriptionStatus subscriptionStatus
            , OrderManagement orderManagement, PlatformType subscribedViaPlatform, Date subscriptionDate, SubscriptionPlan subscriptionPlan, Programs program, String subscriptionType
            , String subscriptionStatusParam, Map<Long, Long> offerCountMap, String stripeTransactionStatus, Date appleExpiryDate, Long workoutCompletionCount,
                                                      Date firstWorkoutCompletionDate, Date lastWorkoutCompletionDate) {
        MyProgramView myProgram = new MyProgramView();
        /**
         * Getting the Order status and setting it in the response
         */
        // Subscribed via Apple platform
        if ((subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAYMENT_PENDING)
                || (subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID))) && !subscriptionStatusParam.equalsIgnoreCase(KeyConstants.KEY_EXPIRED)) {
            if (subscribedViaPlatform.getPlatformTypeId() == 2) {
                // Manipulating whether the order is success or failure
                myProgram.setOrderStatus(orderManagement.getOrderStatus());
                if (orderManagement.getOrderStatus().equalsIgnoreCase(KeyConstants.KEY_PROCESSING)) {
                    myProgram.setIsOrderUnderProcessing(true);
                }
            } else {
                if (KeyConstants.KEY_AUTH_NET_PAYMENT_MODE.equals(orderManagement.getModeOfPayment())) {
                    // Subscribed via Authorize.net platform
                    AuthNetPayment authNetPayment = authNetPaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
                    if (authNetPayment != null) {
                        //Setting the order status based on the response from authorize.net
                        if (authNetPayment.getResponseCode() != null && authNetPayment.getResponseCode().equalsIgnoreCase(KeyConstants.ANET_TRANSACTION_SUCCESS_RESPONSE_CODE)) {
                            myProgram.setOrderStatus(KeyConstants.KEY_SUCCESS);
                        } else {
                            myProgram.setOrderStatus(KeyConstants.KEY_FAILURE);
                        }
                    }
                } else {
                    if (KeyConstants.KEY_PAID.equals(stripeTransactionStatus)) {
                        myProgram.setOrderStatus(KeyConstants.KEY_SUCCESS);

                    } else if (KeyConstants.KEY_REFUND.equals(stripeTransactionStatus)) {
                        myProgram.setOrderStatus(KeyConstants.KEY_REFUNDED);
                    } else {
                        myProgram.setOrderStatus(KeyConstants.KEY_FAILURE);
                    }
                }
            }
        }
        if (subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAYMENT_PENDING)
                || (subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID)
                || (subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_EXPIRED)))) {
            if (subscribedViaPlatform.getPlatformTypeId() == 2) {
                if (appleExpiryDate != null){
                    myProgram.setSubscriptionExpiry(appleExpiryDate);
                    myProgram.setSubscriptionExpiryFormatted(fitwiseUtils.formatDateWithTime(appleExpiryDate));
                }
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(subscriptionDate);
                cal.add(Calendar.DAY_OF_YEAR, Math.toIntExact(subscriptionPlan.getDuration()));
                myProgram.setSubscriptionExpiry(cal.getTime());
                myProgram.setSubscriptionExpiryFormatted(fitwiseUtils.formatDateWithTime(cal.getTime()));
            }
        }
        constructMyProgramView(myProgram, program, workoutCompletionCount, lastWorkoutCompletionDate);
        String status;
        if (subscriptionStatusParam.equalsIgnoreCase(KeyConstants.KEY_EXPIRED)){
            status = KeyConstants.KEY_EXPIRED;
        } else {
            if (subscriptionStatus.getSubscriptionStatusId() == 2 || subscriptionStatus.getSubscriptionStatusId() == 3) {
                status = KeyConstants.KEY_SUBSCRIBED;
            } else {
                status = subscriptionStatus.getSubscriptionStatusName();
            }
        }
        myProgram.setSubscriptionStatus(status);
        /** Discount offers **/
        long noOfCurrentAvailableOffers = offerCountMap.get(program.getProgramId());
        myProgram.setNumberOfCurrentAvailableOffers((int) noOfCurrentAvailableOffers);
        Date subscribedDate = subscriptionDate;
        myProgram.setSubscribedDate(subscribedDate);
        myProgram.setSubscribedDateFormatted(fitwiseUtils.formatDate(subscribedDate));
        Date startedDate;
        if (firstWorkoutCompletionDate != null) {
            startedDate = firstWorkoutCompletionDate;
        } else {
            startedDate = subscribedDate;
        }
        myProgram.setStartDateTimeStamp(startedDate);
        if (startedDate != null) {
            myProgram.setStartDate(fitwiseUtils.formatDate(startedDate));
        }
        myProgram.setSubscriptionType(subscriptionType);
        return myProgram;
    }

    public void constructMyProgramView(MyProgramView myProgram, Programs program, Long workoutCompletionCount, Date lastWorkoutCompletionDate){
        myProgram.setProgramId(program.getProgramId());
        myProgram.setTitle(program.getTitle());
        myProgram.setProgramThumbnail(program.getImage().getImagePath());
        int totalDays = program.getDuration().getDuration().intValue();
        myProgram.setDuration(totalDays + " Days");
        int completedWorkouts = Math.toIntExact(workoutCompletionCount);
        Date completedDate = null;
        String progress;
        long progressPercent;
        if (completedWorkouts == totalDays) {
            completedDate = lastWorkoutCompletionDate;
            progress = DBConstants.COMPLETED;
            progressPercent = 100;
        } else {
            progress = Convertions.getNumberWithZero(completedWorkouts) + "/" + totalDays;
            progressPercent = (completedWorkouts * 100) / totalDays;
        }
        myProgram.setProgress(progress);
        myProgram.setProgressPercent(progressPercent);
        myProgram.setCompletedDate(completedDate);
        myProgram.setCompletedDateFormatted(fitwiseUtils.formatDate(completedDate));
    }

    /**
     * Today's program for current user
     * @return
     */
    public List<TodaysProgramView> getTodaysPrograms() {
        User user = userComponents.getUser();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String token = request.getHeader(Constants.X_AUTHORIZATION);
        return getTodaysPrograms(user, token);
    }

    /**
     * Today's program for given user
     *
     * @param user
     * @return
     */
    public List<TodaysProgramView> getTodaysPrograms(User user, String token) {
        log.info("Get today programs starts.");
        long apiStartTimeMillis = new Date().getTime();
        List<String> statusList = Arrays.asList(KeyConstants.KEY_PAID, KeyConstants.KEY_TRIAL, KeyConstants.KEY_PAYMENT_PENDING);
        List<String> programStatusList = Arrays.asList(InstructorConstant.PUBLISH, InstructorConstant.UNPUBLISH, InstructorConstant.BLOCK);
        List<ProgramSubscription> programSubscriptions = programSubscriptionRepo.findByUserUserIdAndProgramStatusInAndSubscriptionStatusSubscriptionStatusNameInOrderBySubscribedDateDesc(user.getUserId(), programStatusList, statusList);
        log.info("Query to get program subscriptions : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        Set<Long> freeProgramsAndPackageProgramIds = freeAccessService.getFreeProductProgramsIds();
        List<TodaysProgramView> todaysProgramViewList = new ArrayList<>();
        for(Long freeProductId : freeProgramsAndPackageProgramIds){
        	Programs program = programRepository.findByProgramId(freeProductId);
            TodaysProgramView todaysProgram = constructTodaysFreeProgramView(token, user, program);
            if (todaysProgram != null) {
                todaysProgramViewList.add(todaysProgram);
            }
        }
        for (ProgramSubscription programSubscription : programSubscriptions) {
            SubscriptionStatus subscriptionStatus = subscriptionService.getMemberProgramSubscriptionStatus(programSubscription);
            TodaysProgramView todaysProgram = constructTodaysProgramView(token, user, subscriptionStatus, programSubscription.getProgram());
            if (todaysProgram != null) {
                todaysProgramViewList.add(todaysProgram);
            }
        }
        log.info("Construct today program view : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        List<PackageProgramSubscription> packageProgramSubscriptions = packageProgramSubscriptionRepository.findByUserUserIdAndProgramStatusInAndSubscriptionStatusSubscriptionStatusNameInOrderBySubscribedDateDesc(user.getUserId(), programStatusList, statusList);
        log.info("Query to get package program subscriptions : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        for (PackageProgramSubscription packageProgramSubscription : packageProgramSubscriptions) {
            SubscriptionStatus subscriptionStatus = subscriptionService.getMemberPackageProgramSubscriptionStatus(packageProgramSubscription);
            TodaysProgramView todaysProgram = constructTodaysProgramView(token, user, subscriptionStatus, packageProgramSubscription.getProgram());
            if (todaysProgram != null) {
                todaysProgramViewList.add(todaysProgram);
            }
        }
        log.info("Construct today program view of package subscription : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        //Removing duplicate programs in todays view
        todaysProgramViewList = todaysProgramViewList.stream()
                .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparingLong(TodaysProgramView::getProgramId))), ArrayList::new));
        if (todaysProgramViewList.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        log.info("Remove duplicated : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get today programs ends.");
        return todaysProgramViewList;
    }

    /**
     * Method to construct todays view
     *
     * @param token
     * @param user
     * @param subscriptionStatus
     * @param program
     * @return
     */
    private TodaysProgramView constructTodaysProgramView(String token, User user, SubscriptionStatus subscriptionStatus, Programs program) {
        TodaysProgramView todaysProgramView = null;
        if (subscriptionStatus != null && !subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_EXPIRED)) {
            /*
             * Restarting program for user.
             * */
            String timeZoneName = userComponents.getTimeZone(token);
            if (timeZoneName != null) {
                checkAndResetProgramCompletion(timeZoneName, user, program);
            }
            TodaysProgramView todaysProgram = new TodaysProgramView();
            todaysProgram.setProgramId(program.getProgramId());
            todaysProgram.setTitle(program.getTitle());
            todaysProgram.setProgramThumbnail(program.getImage().getImagePath());
            List<WorkoutCompletion> workoutCompletionList = workoutCompletionRepository.findByMemberUserIdAndProgramProgramId(user.getUserId(), program.getProgramId());
            //Setting program start date
            Date startedDate = null;
            if (!workoutCompletionList.isEmpty()) {
                startedDate = workoutCompletionList.get(0).getCompletedDate();
            }
            todaysProgram.setStartDateTimeStamp(startedDate);
            if (startedDate != null) {
                todaysProgram.setStartDate(fitwiseUtils.formatDate(startedDate, timeZoneName));
            }
            int completedWorkouts = workoutCompletionList.size();
            int totalDays = program.getDuration().getDuration().intValue();
            int trialWorkoutScheduleCount = fitwiseUtils.getTrialWorkoutsCountForProgram(user, program);
            //Check if all workouts are completed.
            if (completedWorkouts != totalDays) {
                boolean isTodayWorkout = true;
                //In trial program, if completed programs is greater than completed workout schedules, it can not be under today programs
                if ((subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_TRIAL)) && (completedWorkouts >= trialWorkoutScheduleCount)) {
                    isTodayWorkout = false;
                } else {
                    //If previous workout is completed today, it can not be under today programs
                    if (!workoutCompletionList.isEmpty()) {
                        WorkoutCompletion lastcompletedWorkout = workoutCompletionList.get(workoutCompletionList.size() - 1);
                        Date today = new Date();
                        if (fitwiseUtils.isSameDay(lastcompletedWorkout.getCompletedDate(), today, token)) {
                            isTodayWorkout = false;
                        }
                    }
                }
                if (isTodayWorkout) {
                    String progress = Convertions.getNumberWithZero(completedWorkouts) + "/" + totalDays;
                    long progressPercent = (completedWorkouts * 100) / totalDays;
                    todaysProgram.setProgress(progress);
                    todaysProgram.setProgressPercent(progressPercent);
                    if (subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID) || subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAYMENT_PENDING)) {
                        todaysProgram.setSubscriptionStatus(KeyConstants.KEY_SUBSCRIBED);
                    } else {
                        todaysProgram.setSubscriptionStatus(subscriptionStatus.getSubscriptionStatusName());
                    }
                    todaysProgramView = todaysProgram;
                }
            }
        }
        return todaysProgramView;
    }

    private TodaysProgramView constructTodaysFreeProgramView(String token, User user, Programs programs) {
        TodaysProgramView todaysProgramView = null;
        /*
         * Restarting program for user.
         * */
        String timeZoneName = userComponents.getTimeZone(token);
        if (timeZoneName != null) {
            checkAndResetProgramCompletion(timeZoneName, user, programs);
        }
        TodaysProgramView todaysProgram = new TodaysProgramView();
        todaysProgram.setFreeToAccess(true);
        todaysProgram.setProgramId(programs.getProgramId());
        todaysProgram.setTitle(programs.getTitle());
        todaysProgram.setProgramThumbnail(programs.getImage().getImagePath());
        List<WorkoutCompletion> workoutCompletionList = workoutCompletionRepository.findByMemberUserIdAndProgramProgramId(user.getUserId(), programs.getProgramId());
        //Setting program start date
        Date startedDate = null;
        if (!workoutCompletionList.isEmpty()) {
            startedDate = workoutCompletionList.get(0).getCompletedDate();
        }
        todaysProgram.setStartDateTimeStamp(startedDate);
        if (startedDate != null) {
            todaysProgram.setStartDate(fitwiseUtils.formatDate(startedDate, timeZoneName));
        }
        int completedWorkouts = workoutCompletionList.size();
        int totalDays = programs.getDuration().getDuration().intValue();
        //Check if all workouts are completed.
        if (completedWorkouts != totalDays) {
            boolean isTodayWorkout = true;
            //If previous workout is completed today, it can not be under today programs
            if (!workoutCompletionList.isEmpty()) {
                WorkoutCompletion lastcompletedWorkout = workoutCompletionList.get(workoutCompletionList.size() - 1);
                Date today = new Date();
                if (fitwiseUtils.isSameDay(lastcompletedWorkout.getCompletedDate(), today, token)) {
                    isTodayWorkout = false;
                }
            }
            if (isTodayWorkout) {
                String progress = Convertions.getNumberWithZero(completedWorkouts) + "/" + totalDays;
                long progressPercent = (completedWorkouts * 100) / totalDays;
                todaysProgram.setProgress(progress);
                todaysProgram.setProgressPercent(progressPercent);
                todaysProgramView = todaysProgram;
            }
        }
        return todaysProgramView;
    }

    /**
     * posting exercise completed status and voice over in audio circuit completed status
     *
     * @param programId
     * @param workoutScheduleId
     * @param exerciseScheduleId
     * @return
     */
    @Transactional
    public ResponseModel postExerciseCompletedStatus(Long programId, Long workoutScheduleId, Long circuitScheduleId, Long exerciseScheduleId, Long circuitAndVoiceOverMappingId, boolean isAudioCircuit) {
        User user = userComponents.getUser();
        log.info("Post exercise completion status starts");
        long start = new Date().getTime();
        long profilingStart;
        //Validation for params
        if (programId == null || programId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_NULL, MessageConstants.ERROR);
        }
        Programs program = programRepository.findByProgramId(programId);
        if (program == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, MessageConstants.ERROR);
        }
        List<FreeAccessProgram> freeProducts = freeAccessService.getAllUserFreeProgramsList();
        List<Long> freeProgramIds = freeAccessService.getUserSpecificFreeAccessProgramsIds();
        for(FreeAccessProgram freeProduct : freeProducts){
            freeProgramIds.add(freeProduct.getProgram().getProgramId());
        }
        if(!freeProgramIds.contains(programId)){
            boolean isSubscribed = subscriptionService.isProgramTrialOrSubscribedByUser(user.getUserId(), programId);
            if (!isSubscribed) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_SUBSCRIBED, MessageConstants.ERROR);
            }
        }
        if (workoutScheduleId == null || workoutScheduleId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_ID_NULL, MessageConstants.ERROR);
        }
        WorkoutSchedule workoutSchedule = workoutScheduleRepository.findByWorkoutScheduleId(workoutScheduleId);
        if (workoutSchedule == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_NOT_FOUND, MessageConstants.ERROR);
        }
        List<Long> workoutScheduleIdList = program.getWorkoutSchedules().stream().map(WorkoutSchedule::getWorkoutScheduleId).collect(Collectors.toList());
        if (!workoutScheduleIdList.contains(workoutScheduleId)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_INCORRECT, MessageConstants.ERROR);
        }
        if (workoutSchedule.isRestDay()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_REST, MessageConstants.ERROR);
        }
        log.info("Basic user input validation : Time taken in millis : "+(new Date().getTime() - start));
        profilingStart = new Date().getTime();

        //For trial program, can complete Exercise only from trial workout schedules
        PackageProgramSubscription packageProgramSubscription = packageProgramSubscriptionRepository.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), programId);
        if (!freeProgramIds.contains(programId) && packageProgramSubscription != null) {
            SubscriptionStatus packageProgramSubscriptionStatus = subscriptionService.getMemberPackageProgramSubscriptionStatus(packageProgramSubscription);
            if (!KeyConstants.KEY_PAID.equalsIgnoreCase(packageProgramSubscriptionStatus.getSubscriptionStatusName())) {
                SubscriptionStatus subscriptionStatus = subscriptionService.getMemberProgramSubscriptionStatus(user.getUserId(), programId);
                if (subscriptionStatus.getSubscriptionStatusName().equals(KeyConstants.KEY_TRIAL)) {
                    int trialWorkoutScheduleCount = fitwiseUtils.getTrialWorkoutsCountForProgram(user, program);
                    if (workoutSchedule.getOrder().intValue() > trialWorkoutScheduleCount) {
                        throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_TRIAL_PROGRAM_CANT_COMPLETE_WORKOUT, MessageConstants.ERROR);
                    }
                }
            }
        }
        log.info("Query Package program subscription : Time taken in millis : " + (new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();
        Date now = new Date();
        long startOrder = workoutSchedule.getPrograms().getWorkoutSchedules().stream().mapToLong(WorkoutSchedule::getOrder).min().getAsLong();
        long order = workoutSchedule.getOrder().longValue();
        if (startOrder != order) {
            WorkoutSchedule previousWorkoutSchedule = workoutScheduleRepository.findByOrderAndProgramsProgramId(order - 1, programId);
            WorkoutCompletion previousWorkoutCompletion = workoutCompletionRepository.findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdOrderByCompletedDateDesc(user.getUserId(), programId, previousWorkoutSchedule.getWorkoutScheduleId());
            if (previousWorkoutCompletion == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PREVIOUS_WORKOUT_SCHEDULE_INCOMPLETE, MessageConstants.ERROR);
            } else {
                if (fitwiseUtils.isSameDay(previousWorkoutCompletion.getCompletedDate(), now)) {
                    //if previousWorkoutSchedule is completed in the same day provided, current workout schedule can not be completed.
                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PREVIOUS_WORKOUT_SCHEDULE_COMPLETED_TODAY, MessageConstants.ERROR);
                }
            }
        }
        log.info("Check workouts start order : Time taken in millis : " + (new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();
        if (circuitScheduleId == null || circuitScheduleId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_SCHEDULE_ID_NULL, MessageConstants.ERROR);
        }
        Optional<CircuitSchedule> circuitScheduleOptional = circuitScheduleRepository.findById(circuitScheduleId);
        if (!circuitScheduleOptional.isPresent()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_SCHEDULE_NOT_FOUND, MessageConstants.ERROR);
        }
        CircuitSchedule circuitSchedule = circuitScheduleOptional.get();
        if (circuitSchedule.isRestCircuit()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_SCHEDULE_REST, MessageConstants.ERROR);
        }
        List<Long> circuitScheduleIdList = workoutSchedule.getWorkout().getCircuitSchedules().stream().map(CircuitSchedule::getCircuitScheduleId).collect(Collectors.toList());
        if (!circuitScheduleIdList.contains(circuitScheduleId)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_SCHEDULE_INCORRECT, MessageConstants.ERROR);
        }
        log.info("Query Circuit schedule and extract circuit schedule id list from user input : Time taken in millis : " + (new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();
        List<Long> exerciseScheduleIdList = null;
        List<Long> circuitAndVoiceOverMappingIdList = null;
        String responseMsg;
        boolean isAlreadyCompleted = false;
        int noOfExercisesCompletedInCircuit = 0;
        int noOfVoiceOversCompletedInCircuit = 0;
        boolean isworkoutCompletedNow = false;
        if (isAudioCircuit) {
            if (circuitAndVoiceOverMappingId == null || circuitAndVoiceOverMappingId == 0) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_AND_VOICE_OVER_MAPPING_ID_NULL, MessageConstants.ERROR);
            }
            Optional<CircuitAndVoiceOverMapping> circuitAndVoiceOver = circuitAndVoiceOverMappingRepository.findById(circuitAndVoiceOverMappingId);
            CircuitAndVoiceOverMapping circuitAndVoiceOverMapping = circuitAndVoiceOver.orElse(null);
            if (circuitAndVoiceOverMapping == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_AND_VOICE_OVER_MAPPING_NOT_FOUND, MessageConstants.ERROR);
            }
            circuitAndVoiceOverMappingIdList = circuitSchedule.getCircuit().getCircuitAndVoiceOverMappings().stream().map(CircuitAndVoiceOverMapping::getCircuitAndVoiceOverMappingId).collect(Collectors.toList());
            if (!circuitAndVoiceOverMappingIdList.contains(circuitAndVoiceOverMappingId)) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_VOICE_OVER_MAPPING_ID_INCORRECT, MessageConstants.ERROR);
            }
            responseMsg = MessageConstants.MSG_VOICE_OVER_COMPLETED_STATUS_UPDATED;
            log.info("Query Circuit And Voice Over Mapping and extract circuit and voice over mapping id list from circuit schedule : Time taken in millis : " + (new Date().getTime() - profilingStart));
            profilingStart = new Date().getTime();
            CircuitVoiceOverMappingCompletion circuitVoiceOverMappingCompletion = circuitVoiceOverMappingCompletionRepository.findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdAndCircuitScheduleIdAndCircuitVoiceOverMappingIdOrderByCompletedDateDesc(user.getUserId(), programId, workoutScheduleId, circuitScheduleId, circuitAndVoiceOverMappingId);
            if (circuitVoiceOverMappingCompletion == null) {
                circuitVoiceOverMappingCompletion = new CircuitVoiceOverMappingCompletion();
                circuitVoiceOverMappingCompletion.setCircuitVoiceOverMappingId(circuitAndVoiceOverMappingId);
                circuitVoiceOverMappingCompletion.setCircuitScheduleId(circuitScheduleId);
                circuitVoiceOverMappingCompletion.setWorkoutScheduleId(workoutScheduleId);
                circuitVoiceOverMappingCompletion.setMember(user);
                circuitVoiceOverMappingCompletion.setProgram(program);
                circuitVoiceOverMappingCompletion.setCompletedDate(now);
                circuitVoiceOverMappingCompletionRepository.save(circuitVoiceOverMappingCompletion);
            } else {
                isAlreadyCompleted = true;
                responseMsg = MessageConstants.MSG_VOICE_OVER_COMPLETED_ALREADY;
            }
            log.info("Query Update circuit and voice over mapping completion : Time taken in millis : " + (new Date().getTime() - profilingStart));
            profilingStart = new Date().getTime();
            String voiceOverAuditAction = isAlreadyCompleted ? DBConstants.REPEAT : DBConstants.COMPLETED;
            //voice-over completion audit entry
            CircuitVoiceOverMappingCompletionAudit circuitVoiceOverMappingCompletionAudit = new CircuitVoiceOverMappingCompletionAudit();
            circuitVoiceOverMappingCompletionAudit.setMember(user);
            circuitVoiceOverMappingCompletionAudit.setProgram(program);
            circuitVoiceOverMappingCompletionAudit.setWorkoutScheduleId(workoutSchedule.getWorkoutScheduleId());
            circuitVoiceOverMappingCompletionAudit.setCircuitScheduleId(circuitSchedule.getCircuitScheduleId());
            circuitVoiceOverMappingCompletionAudit.setCircuitAndVoiceOverMappingId(circuitAndVoiceOverMapping.getCircuitAndVoiceOverMappingId());
            circuitVoiceOverMappingCompletionAudit.setAction(voiceOverAuditAction);
            circuitVoiceOverMappingCompletionAuditRepo.save(circuitVoiceOverMappingCompletionAudit);
            log.info("Query Update circuit and voice over mapping completion audit : Time taken in millis : " + (new Date().getTime() - profilingStart));
            profilingStart = new Date().getTime();
            List<CircuitVoiceOverMappingCompletion> voiceOversCompletedInCircuitList = circuitVoiceOverMappingCompletionRepository.findByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdAndCircuitScheduleId(user.getUserId(), programId, workoutScheduleId, circuitScheduleId);
            //removing duplicates
            voiceOversCompletedInCircuitList = voiceOversCompletedInCircuitList.stream()
                    .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparingLong(CircuitVoiceOverMappingCompletion::getCircuitVoiceOverMappingId))), ArrayList::new));
            noOfVoiceOversCompletedInCircuit = voiceOversCompletedInCircuitList.size();
            log.info("Query voiceOversCompletedInCircuitList and extract id's : Time taken in millis : " + (new Date().getTime() - profilingStart));
            profilingStart = new Date().getTime();
        } else {
            if (exerciseScheduleId == null || exerciseScheduleId == 0) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_SCHEDULE_ID_NULL, MessageConstants.ERROR);
            }
            ExerciseSchedulers exerciseSchedule = exerciseScheduleRepository.findByExerciseScheduleId(exerciseScheduleId);
            if (exerciseSchedule == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_SCHEDULE_NOT_FOUND, MessageConstants.ERROR);
            }
            exerciseScheduleIdList = circuitSchedule.getCircuit().getExerciseSchedules().stream().map(ExerciseSchedulers::getExerciseScheduleId).collect(Collectors.toList());
            if (!exerciseScheduleIdList.contains(exerciseScheduleId)) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_SCHEDULE_INCORRECT, MessageConstants.ERROR);
            }
            responseMsg = MessageConstants.MSG_EXERCISE_COMPLETED_STATUS_UPDATED;
            log.info("Query exerciseSchedule and extract id's : Time taken in millis : " + (new Date().getTime() - profilingStart));
            profilingStart = new Date().getTime();
            //Exercise completion entry
            //Since exercise repeats is implemented as same video being looped, if the video is played even once, it is considered as completed.
            ExerciseCompletion exerciseCompletion = exerciseCompletionRepository.findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdAndCircuitScheduleIdAndExerciseScheduleIdOrderByCompletedDateDesc(user.getUserId(), programId, workoutScheduleId, circuitScheduleId, exerciseScheduleId);
            if (exerciseCompletion == null) {
                exerciseCompletion = new ExerciseCompletion();
                exerciseCompletion.setMember(user);
                exerciseCompletion.setProgram(program);
                exerciseCompletion.setWorkoutScheduleId(workoutSchedule.getWorkoutScheduleId());
                exerciseCompletion.setCircuitScheduleId(circuitSchedule.getCircuitScheduleId());
                exerciseCompletion.setExerciseScheduleId(exerciseSchedule.getExerciseScheduleId());
                exerciseCompletion.setCompletedDate(now);
                exerciseCompletionRepository.save(exerciseCompletion);
            } else {
                isAlreadyCompleted = true;
                responseMsg = MessageConstants.MSG_EXERCISE_COMPLETED_ALREADY;
            }
            log.info("Query save exercise completion : Time taken in millis : " + (new Date().getTime() - profilingStart));
            profilingStart = new Date().getTime();
            String exerciseAuditAction = isAlreadyCompleted ? DBConstants.REPEAT : DBConstants.COMPLETED;
            //Exercise completion audit entry
            ExerciseCompletionAudit exerciseCompletionAudit = new ExerciseCompletionAudit();
            exerciseCompletionAudit.setMember(user);
            exerciseCompletionAudit.setProgram(program);
            exerciseCompletionAudit.setWorkoutScheduleId(workoutSchedule.getWorkoutScheduleId());
            exerciseCompletionAudit.setCircuitScheduleId(circuitSchedule.getCircuitScheduleId());
            exerciseCompletionAudit.setExerciseScheduleId(exerciseSchedule.getExerciseScheduleId());
            exerciseCompletionAudit.setAction(exerciseAuditAction);
            exerciseCompletionAuditRepository.save(exerciseCompletionAudit);
            log.info("Query save exercise completion audit : Time taken in millis : " + (new Date().getTime() - profilingStart));
            profilingStart = new Date().getTime();
            //Auto populating rest exercise in the circuit. Audit not done for rest exercise.
            Set<ExerciseSchedulers> exerciseSchedules = circuitSchedule.getCircuit().getExerciseSchedules();
            for (ExerciseSchedulers exSchedule : exerciseSchedules) {
                if (exSchedule.getWorkoutRestVideo() != null) {
                    ExerciseCompletion restExerciseCompletion = exerciseCompletionRepository.findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdAndCircuitScheduleIdAndExerciseScheduleIdOrderByCompletedDateDesc(user.getUserId(), programId, workoutScheduleId, circuitScheduleId, exSchedule.getExerciseScheduleId());
                    if (restExerciseCompletion == null) {
                        restExerciseCompletion = new ExerciseCompletion();
                        restExerciseCompletion.setMember(user);
                        restExerciseCompletion.setProgram(program);
                        restExerciseCompletion.setWorkoutScheduleId(workoutSchedule.getWorkoutScheduleId());
                        restExerciseCompletion.setCircuitScheduleId(circuitSchedule.getCircuitScheduleId());
                        restExerciseCompletion.setExerciseScheduleId(exSchedule.getExerciseScheduleId());
                        restExerciseCompletion.setCompletedDate(now);
                        exerciseCompletionRepository.save(restExerciseCompletion);
                    }
                }
            }
            log.info("Query update rest exercise completion : Time taken in millis : " + (new Date().getTime() - profilingStart));
            profilingStart = new Date().getTime();
            //Check whether the circuit is completed or not
            List<ExerciseCompletion> exercisesCompletedInCircuitList = exerciseCompletionRepository.findByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdAndCircuitScheduleId(user.getUserId(), programId, workoutScheduleId, circuitScheduleId);
            //removing duplicates
            exercisesCompletedInCircuitList = exercisesCompletedInCircuitList.stream()
                    .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparingLong(ExerciseCompletion::getExerciseScheduleId))), ArrayList::new));
            noOfExercisesCompletedInCircuit = exercisesCompletedInCircuitList.size();
            log.info("Query Exercises Completed In Circuit : Time taken in millis : " + (new Date().getTime() - profilingStart));
            profilingStart = new Date().getTime();
        }
        //Since Circuit repeats is implemented as duplicate item in playlist, it is completed based on repeats.
        if ((!isAudioCircuit && exerciseScheduleIdList.size() == noOfExercisesCompletedInCircuit) || (isAudioCircuit && circuitAndVoiceOverMappingIdList.size() == noOfVoiceOversCompletedInCircuit)) {
            boolean isAllExInCircuitCompleted = false;
            String circuitAuditAction = DBConstants.COMPLETED;
            CircuitCompletion circuitCompletion = circuitCompletionRepository.findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdAndCircuitScheduleIdOrderByCompletedDateDesc(user.getUserId(), programId, workoutScheduleId, circuitScheduleId);
            if (circuitCompletion == null) {
                circuitCompletion = new CircuitCompletion();
                circuitCompletion.setMember(user);
                circuitCompletion.setProgram(program);
                circuitCompletion.setWorkoutScheduleId(workoutSchedule.getWorkoutScheduleId());
                circuitCompletion.setCircuitScheduleId(circuitSchedule.getCircuitScheduleId());
                circuitCompletion.setCompletedDate(now);
                circuitCompletion.setNoOfTimesPlayed(1);
                // Since there is no repeats for audio circuit
                if (isAudioCircuit) {
                    circuitCompletion.setCompleted(true);
                } else {
                    if (circuitSchedule.getRepeat() == 1) {
                        circuitCompletion.setCompleted(true);
                    } else {
                        //deleting entries in exercise completion table, to mark completion for circuit repeat
                        List<ExerciseCompletion> exerciseCompletionList = exerciseCompletionRepository.findByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdAndCircuitScheduleId(user.getUserId(), programId, workoutScheduleId, circuitScheduleId);
                        for (ExerciseCompletion deleteExerciseCompletion : exerciseCompletionList) {
                            exerciseCompletionRepository.delete(deleteExerciseCompletion);
                        }
                    }
                }
                circuitCompletionRepository.save(circuitCompletion);
                isAllExInCircuitCompleted = true;
                log.info("Query get circuit completion and delete exercise completion : Time taken in millis : " + (new Date().getTime() - profilingStart));
                profilingStart = new Date().getTime();
            } else if (circuitCompletion != null && !circuitCompletion.isCompleted()) {
                int noOfTimesPlayed = circuitCompletion.getNoOfTimesPlayed() + 1;
                circuitCompletion.setNoOfTimesPlayed(noOfTimesPlayed);
                if (noOfTimesPlayed < (circuitSchedule.getRepeat())) {
                    List<ExerciseCompletion> exerciseCompletionList = exerciseCompletionRepository.findByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdAndCircuitScheduleId(user.getUserId(), programId, workoutScheduleId, circuitScheduleId);
                    for (ExerciseCompletion deleteExerciseCompletion : exerciseCompletionList) {
                        exerciseCompletionRepository.delete(deleteExerciseCompletion);
                    }
                }
                if (noOfTimesPlayed == (circuitSchedule.getRepeat())) {
                    circuitCompletion.setCompleted(true);
                }
                isAllExInCircuitCompleted = true;
                circuitAuditAction = DBConstants.REPEAT;
                log.info("Query exercise completion : Time taken in millis : " + (new Date().getTime() - profilingStart));
                profilingStart = new Date().getTime();
            }
            //Circuit completion audit
            if (isAllExInCircuitCompleted) {
                CircuitCompletionAudit circuitCompletionAudit = new CircuitCompletionAudit();
                circuitCompletionAudit.setMember(user);
                circuitCompletionAudit.setProgram(program);
                circuitCompletionAudit.setWorkoutScheduleId(workoutSchedule.getWorkoutScheduleId());
                circuitCompletionAudit.setCircuitScheduleId(circuitSchedule.getCircuitScheduleId());
                circuitCompletionAudit.setAction(circuitAuditAction);
                circuitCompletionAuditRepository.save(circuitCompletionAudit);
                log.info("Query circuit completion audit : Time taken in millis : " + (new Date().getTime() - profilingStart));
                profilingStart = new Date().getTime();
            }
            //Auto populating rest circuits
            CircuitSchedule nextCircuitSchedule = circuitScheduleRepository.findByOrderAndWorkoutWorkoutId(circuitSchedule.getOrder() + 1, workoutSchedule.getWorkout().getWorkoutId());
            if (nextCircuitSchedule != null && nextCircuitSchedule.isRestCircuit()) {
                postRestCircuitCompletedStatus(programId, workoutScheduleId, nextCircuitSchedule.getCircuitScheduleId());
            }
            log.info("Query next circuit schedule : Time taken in millis : " + (new Date().getTime() - profilingStart));
            profilingStart = new Date().getTime();
        }
        //Check whether the workout is completed or not
        int noOfCircuitsCompletedInWorkout = 0;
        List<CircuitCompletion> circuitCompletions = circuitCompletionRepository.findByMemberUserIdAndProgramProgramIdAndWorkoutScheduleId(user.getUserId(), programId, workoutScheduleId);
        for (CircuitCompletion circuitCompletion : circuitCompletions) {
            if (circuitCompletion.isCompleted()) {
                noOfCircuitsCompletedInWorkout += 1;
            }
        }
        log.info("Query check whether the workout is completed or not : Time taken in millis : " + (new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();
        if (circuitScheduleIdList.size() == noOfCircuitsCompletedInWorkout) {
            WorkoutCompletion workoutCompletion = workoutCompletionRepository.findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdOrderByCompletedDateDesc(user.getUserId(), programId, workoutScheduleId);
            log.info("Query get workout completion : Time taken in millis : " + (new Date().getTime() - profilingStart));
            profilingStart = new Date().getTime();
            if (workoutCompletion == null) {
                workoutCompletion = new WorkoutCompletion();
                workoutCompletion.setMember(user);
                workoutCompletion.setProgram(program);
                workoutCompletion.setWorkoutScheduleId(workoutSchedule.getWorkoutScheduleId());
                workoutCompletion.setCompletedDate(now);
                workoutCompletionRepository.save(workoutCompletion);
                log.info("Query save workout completion : Time taken in millis : " + (new Date().getTime() - profilingStart));
                profilingStart = new Date().getTime();
                //Workout completion audit
                WorkoutCompletionAudit workoutCompletionAudit = new WorkoutCompletionAudit();
                workoutCompletionAudit.setMember(user);
                workoutCompletionAudit.setProgram(program);
                workoutCompletionAudit.setWorkoutScheduleId(workoutSchedule.getWorkoutScheduleId());
                workoutCompletionAudit.setAction(DBConstants.COMPLETED);
                workoutCompletionAuditRepository.save(workoutCompletionAudit);
                log.info("Query save workout completion audit : Time taken in millis : " + (new Date().getTime() - profilingStart));
                profilingStart = new Date().getTime();
                if (workoutSchedule.getWorkout() != null) {
                    List<Challenge> challenges = challengeRepository.findByUserUserId(user.getUserId());
                    for (Challenge challenge : challenges) {
                        if (workoutCompletion.getCompletedDate().after(challenge.getChallengeStartedDay()) && workoutCompletion.getCompletedDate().before(challenge.getChallengeEndDate())) {
                            int completedWorkouts = challenge.getCompletedWorkouts() + 1;
                            int remainingWorkouts = challenge.getRemainingWorkouts() - 1;
                            double percentage;
                            if (completedWorkouts <= challenge.getChallengeWorkouts()) {
                                challenge.setCompletedWorkouts(completedWorkouts);
                                challenge.setRemainingWorkouts(remainingWorkouts);
                                percentage = (completedWorkouts * 100.00) / challenge.getChallengeWorkouts();
                                challenge.setPercentage(percentage);
                            }
                            challengeRepository.save(challenge);
                        }
                    }
                    log.info("Query get and update challenges : Time taken in millis : " + (new Date().getTime() - profilingStart));
                    profilingStart = new Date().getTime();
                }
                isworkoutCompletedNow = true;
            }
        }
        ExerciseCompletionResponse exerciseCompletionResponse = new ExerciseCompletionResponse();
        exerciseCompletionResponse.setIsWorkoutCompletedNow(isworkoutCompletedNow);
        boolean isworkoutCompleted = false;
        WorkoutCompletion workoutCompletion = workoutCompletionRepository.findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdOrderByCompletedDateDesc(user.getUserId(), programId, workoutScheduleId);
        log.info("Query get workout completion : Time taken in millis : " + (new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();
        if (workoutCompletion != null) {
            isworkoutCompleted = true;
        }
        exerciseCompletionResponse.setIsWorkoutCompleted(isworkoutCompleted);
        boolean isProgramRatingAllowed = false;
        //Program completion status
        int completedWorkouts = workoutCompletionRepository.countByMemberUserIdAndProgramProgramId(user.getUserId(), programId);
        boolean isProgramCompleted = false;
        if (completedWorkouts == program.getDuration().getDuration().intValue()) {
            isProgramCompleted = true;
        }
        log.info("Query get workout completion for program completion status : Time taken in millis : " + (new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();
        //Program rating submission allowed value in DB
        boolean isProgramRatingSubmissionAllowed = false;
        ProgramRating programRating = programRatingRepository.findByProgramAndUser(program, user);
        log.info("Query get program ratings : Time taken in millis : " + (new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();
        if (programRating == null || programRating.getIsSubmissionAllowed()) {
            isProgramRatingSubmissionAllowed = true;
        }
        if (isProgramCompleted && isProgramRatingSubmissionAllowed) {
            isProgramRatingAllowed = true;
        }
        exerciseCompletionResponse.setIsProgramRatingAllowed(isProgramRatingAllowed);
        Float programRatingValue = null;
        if (programRating != null) {
            programRatingValue = programRating.getProgramRating();
        }
        exerciseCompletionResponse.setProgramRating(programRatingValue);
        //Adding Program completion entry and audit
        if (isProgramCompleted) {
            ProgramCompletion programCompletion = programCompletionRepository.findByMemberAndProgram(user, program);
            log.info("Query get program completion : Time taken in millis : " + (new Date().getTime() - profilingStart));
            profilingStart = new Date().getTime();
            if (programCompletion == null) {
                programCompletion = new ProgramCompletion();
                programCompletion.setMember(user);
                programCompletion.setProgram(program);
                programCompletion.setCompletedDate(now);
                programCompletionRepository.save(programCompletion);
                ProgramCompletionAudit programCompletionAudit = new ProgramCompletionAudit();
                programCompletionAudit.setMember(user);
                programCompletionAudit.setProgram(program);
                programCompletionAudit.setAction(DBConstants.COMPLETED);
                programCompletionAuditRepository.save(programCompletionAudit);
                log.info("Query update program completion and audit : Time taken in millis : " + (new Date().getTime() - profilingStart));
                profilingStart = new Date().getTime();
            }
        }
        ResponseModel responseModel = new ResponseModel();
        responseModel.setPayload(exerciseCompletionResponse);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(responseMsg);
        log.info("Making up response model : Time taken in millis : " + (new Date().getTime() - profilingStart));
        log.info("Post exercise completion status : Total Time taken in millis : "+(new Date().getTime() - start));
        log.info("Post exercise completion status ends");
        return responseModel;
    }

    /**
     * @param programId
     * @param workoutScheduleId
     * @param circuitScheduleId
     * @return
     */
    public ResponseModel postRestCircuitCompletedStatus(Long programId, Long workoutScheduleId, Long circuitScheduleId) {
        User user = userComponents.getUser();
        //Validation for params
        if (programId == null || programId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_NULL, MessageConstants.ERROR);
        }
        Programs program = programRepository.findByProgramId(programId);
        if (program == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, MessageConstants.ERROR);
        }
        List<FreeAccessProgram> freeProducts = freeAccessService.getAllUserFreeProgramsList();
        List<Long> freeProgramIds = freeAccessService.getUserSpecificFreeAccessProgramsIds();
        for(FreeAccessProgram freeProduct : freeProducts){
            freeProgramIds.add(freeProduct.getProgram().getProgramId());
        }
        if(!freeProgramIds.contains(programId)){
            boolean isSubscribed = subscriptionService.isProgramTrialOrSubscribedByUser(user.getUserId(), programId);
            if (!isSubscribed) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_SUBSCRIBED, MessageConstants.ERROR);
            }
        }
        if (workoutScheduleId == null || workoutScheduleId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_ID_NULL, MessageConstants.ERROR);
        }
        WorkoutSchedule workoutSchedule = workoutScheduleRepository.findByWorkoutScheduleId(workoutScheduleId);
        if (workoutSchedule == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_NOT_FOUND, MessageConstants.ERROR);
        }
        if (workoutSchedule.isRestDay()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_REST, MessageConstants.ERROR);
        }
        List<Long> workoutScheduleIdList = program.getWorkoutSchedules().stream().map(WorkoutSchedule::getWorkoutScheduleId).collect(Collectors.toList());
        if (!workoutScheduleIdList.contains(workoutScheduleId)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_INCORRECT, MessageConstants.ERROR);
        }
        if (circuitScheduleId == null || circuitScheduleId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_SCHEDULE_ID_NULL, MessageConstants.ERROR);
        }
        Optional<CircuitSchedule> circuitScheduleOptional = circuitScheduleRepository.findById(circuitScheduleId);
        if (!circuitScheduleOptional.isPresent()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_SCHEDULE_NOT_FOUND, MessageConstants.ERROR);
        }
        List<Long> circuitScheduleIdList = workoutSchedule.getWorkout().getCircuitSchedules().stream().map(CircuitSchedule::getCircuitScheduleId).collect(Collectors.toList());
        if (!circuitScheduleIdList.contains(circuitScheduleId)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_SCHEDULE_INCORRECT, MessageConstants.ERROR);
        }
        CircuitSchedule circuitSchedule = circuitScheduleOptional.get();
        if (!circuitSchedule.isRestCircuit()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_CIRCUIT_SCHEDULE_NOT_REST_CIRCUIT, MessageConstants.ERROR);
        }
        Date now = new Date();
        CircuitCompletion circuitCompletion = circuitCompletionRepository.findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdAndCircuitScheduleIdOrderByCompletedDateDesc(user.getUserId(), programId, workoutScheduleId, circuitScheduleId);
        if (circuitCompletion != null) {
            return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_REST_CIRCUIT_COMPLETED_ALREADY, null);
        } else {
            circuitCompletion = new CircuitCompletion();
            circuitCompletion.setMember(user);
            circuitCompletion.setProgram(program);
            circuitCompletion.setWorkoutScheduleId(workoutSchedule.getWorkoutScheduleId());
            circuitCompletion.setCircuitScheduleId(circuitScheduleId);
            circuitCompletion.setCompletedDate(now);
            circuitCompletion.setCompleted(true);
            circuitCompletionRepository.save(circuitCompletion);
            //Circuit completion audit
            CircuitCompletionAudit circuitCompletionAudit = new CircuitCompletionAudit();
            circuitCompletionAudit.setMember(user);
            circuitCompletionAudit.setProgram(program);
            circuitCompletionAudit.setWorkoutScheduleId(workoutSchedule.getWorkoutScheduleId());
            circuitCompletionAudit.setCircuitScheduleId(circuitScheduleId);
            circuitCompletionAudit.setAction(DBConstants.COMPLETED);
            circuitCompletionAuditRepository.save(circuitCompletionAudit);
        }
        ResponseModel responseModel = new ResponseModel();
        responseModel.setPayload(null);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_REST_CIRCUIT_COMPLETED_STATUS_UPDATED);
        return responseModel;
    }

    /**
     * Get Filter for trending programs
     * @return
     */
    public List<MemberFilterView> getRecommendedAndTrendingFilter() {
        MemberFilterView durationFilter = new MemberFilterView();
        durationFilter.setFilterName(KeyConstants.KEY_DURATION_FILTER_NAME);
        durationFilter.setType(KeyConstants.KEY_DURATION_FILTER_TYPE);
        List<Duration> durationList = durationRepo.findAllByOrderByDurationAsc();
        List<Filter> durationFilterList = new ArrayList<>();
        for (Duration duration : durationList) {
            Filter filter = new Filter();
            filter.setFilterId(duration.getDurationId());
            filter.setFilterName(duration.getDuration() + " Days");
            durationFilterList.add(filter);
        }
        durationFilter.setFilters(durationFilterList);
        List<MemberFilterView> programsFilterViewList = new ArrayList<>();
        programsFilterViewList.add(durationFilter);
        return programsFilterViewList;
    }

    /**
     * Gets the user recommended programs.
     *
     * @param pageNo   the page size
     * @param pageSize the page count
     * @param search
     * @return the user recommended programs
     */
    public Map<String, Object> getRecommendedPrograms(int pageNo, int pageSize, RecommendedAndTrendingFilterModel filterModel, Optional<String> search) {
        long start = new Date().getTime();
        long profilingStart;
        long profilingEnd;
        RequestParamValidator.pageSetup(pageNo, pageSize);
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, Sort.by("programId"));
        User user = userComponents.getUser();
        List<UserProgramGoalsMapping> userProgramGoalsMappings = userProgramGoalsMappingRepository.findByUser(user);
        if (userProgramGoalsMappings.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_ONBOARD_DATA_MISSING, null);
        }
        profilingStart = new Date().getTime();
        //Getting expertise and program type from user goals
        List<Long> programTypeIdList = new ArrayList<>();
        List<Long> expertiseLevelIdList = new ArrayList<>();
        for (UserProgramGoalsMapping userProgramGoalsMapping : userProgramGoalsMappings) {
            programTypeIdList.add(userProgramGoalsMapping.getProgramExpertiseGoalsMapping().getProgramExpertiseMapping().getProgramType().getProgramTypeId());
            expertiseLevelIdList.add(userProgramGoalsMapping.getProgramExpertiseGoalsMapping().getProgramExpertiseMapping().getExpertiseLevel().getExpertiseLevelId());
        }
        //Building criteria based on expertise, program type and stripe mapping
        Specification<Programs> programExpertiseSpec = ProgramSpecifications.getProgramByExpertiseIn(expertiseLevelIdList);
        Specification<Programs> programTypeSpec = ProgramSpecifications.getProgramByTypeIn(programTypeIdList);
        Specification<Programs> programStatusSpec = ProgramSpecifications.getProgramByStatus(KeyConstants.KEY_PUBLISH);
        Specification<Programs> stripeActiveSpec = ProgramSpecifications.getProgramByStripeMapping();
        Specification<Programs> finalSpec = programExpertiseSpec.and(programStatusSpec).and(programTypeSpec).and(stripeActiveSpec);
        Page<Programs> programs;
        if (search.isPresent() && !search.get().isEmpty()) {
            Specification<Programs> titleSearchSpec = ProgramSpecifications.getProgramByTitleContains(search.get());
            finalSpec = finalSpec.and(titleSearchSpec);
        }
        // If filter is Present
        if (filterModel.getDuration() != null && !filterModel.getDuration().isEmpty()) {
            List<Long> durationList = filterModel.getDuration().stream().map(Filter::getFilterId).collect(Collectors.toList());
            Specification<Programs> durationSpec = ProgramSpecifications.getProgramByDurationIn(durationList);
            finalSpec = finalSpec.and(durationSpec);
        }
        programs = programRepository.findAll(finalSpec,pageRequest);
        profilingEnd = new Date().getTime();
        log.info("Query : time taken in millis : "+(profilingEnd-profilingStart));
        if (programs.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        profilingStart = new Date().getTime();
        List<Long> programIdList = programs.stream().map(Programs::getProgramId).collect(Collectors.toList());
        Map<Long, Long> offerCountMap = discountsService.getNoOfCurrentAvailableOffersOfProgramsForMember(programIdList, user);
        log.info(StringConstants.LOG_OFFER_COUNT_QUERY + (new Date().getTime() - profilingStart));
        profilingEnd = new Date().getTime();
        log.info("Construction : time taken in millis : "+(profilingEnd-profilingStart));
        Map<String, Object> recommendedPrograms = new HashMap<>();
        recommendedPrograms.put(KeyConstants.KEY_TOTAL_COUNT, programs.getTotalElements());
        recommendedPrograms.put(KeyConstants.KEY_PROGRAMS, programDataParsing.constructProgramTileModelWithFreeAccess(programs.getContent(), offerCountMap));
        profilingEnd = new Date().getTime();
        log.info("Overall time : time taken in millis : "+(profilingEnd-start));
        return recommendedPrograms;
    }

    /**
     * Gets the user trending programs.
     * @param pageNo   the page size
     * @param pageSize the page count
     * @param search
     * @return the user trending programs
     */
    public Map<String, Object> getUserTrendingPrograms(final int pageNo, final int pageSize, RecommendedAndTrendingFilterModel filterModel, Optional<String> search) {
        log.info("Member Trending programs starts.");
        long apiStartTimeMillis = new Date().getTime();
        RequestParamValidator.pageSetup(pageNo, pageSize);
        long profilingStartTimeMillis = new Date().getTime();
        Specification<Programs> programStatusSpec = ProgramSpecifications.getProgramByStatus(KeyConstants.KEY_PUBLISH);
        Specification<Programs> stripeActiveSpec = ProgramSpecifications.getProgramByStripeMapping();
        Specification<Programs> finalSpec;
        List<Programs> programs;
        if (search.isPresent() && !search.get().isEmpty()) {
            Specification<Programs> titleSearchSpec = ProgramSpecifications.getProgramByTitleContains(search.get());
            if (filterModel.getDuration() == null || filterModel.getDuration().isEmpty()) {
                finalSpec = programStatusSpec.and(titleSearchSpec).and(stripeActiveSpec);
            } else {
                List<Long> durationIdList = filterModel.getDuration().stream().map(Filter::getFilterId).collect(Collectors.toList());
                Specification<Programs> durationSpec = ProgramSpecifications.getProgramByDurationIn(durationIdList);
                finalSpec = programStatusSpec.and(titleSearchSpec).and(durationSpec).and(stripeActiveSpec);
            }
        } else {
            if (filterModel.getDuration() == null || filterModel.getDuration().isEmpty()) {
                finalSpec = programStatusSpec.and(stripeActiveSpec);
            } else {
                List<Long> durationIdList = filterModel.getDuration().stream().map(Filter::getFilterId).collect(Collectors.toList());
                Specification<Programs> durationSpec = ProgramSpecifications.getProgramByDurationIn(durationIdList);
                finalSpec = programStatusSpec.and(durationSpec).and(stripeActiveSpec);
            }
        }
        programs = programRepository.findAll(finalSpec);
        long profilingEndTimeMillis = new Date().getTime();
        log.info("Query : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        if (programs.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, null);
        }
        User user = userComponents.getAndValidateUser();
        profilingStartTimeMillis = new Date().getTime();
        List<Long> programIdList = programs.stream().map(Programs::getProgramId).collect(Collectors.toList());
        Map<Long, Long> offerCountMap = discountsService.getNoOfCurrentAvailableOffersOfProgramsForMember(programIdList, user);
        log.info(StringConstants.LOG_OFFER_COUNT_QUERY + (new Date().getTime() - profilingStartTimeMillis));
        int fromIndex = (pageNo - 1) * pageSize;
        List<ProgramTileModel> programTileModels = new ArrayList<>();
        List<FeaturedPrograms> featuredProgramsList = featuredProgramsRepository.findAllByOrderById();
        profilingStartTimeMillis = new Date().getTime();
        Map<Long, Long> offerMap = discountsService.getNoOfCurrentAvailableOffersOfProgramsForMember(programIdList, user);
        log.info("Offer count query for featured : time taken in millis : "+(new Date().getTime() - profilingStartTimeMillis));
        for (FeaturedPrograms featuredProgram : featuredProgramsList) {
            if (programs.stream().anyMatch(program -> program.getProgramId().equals(featuredProgram.getProgram().getProgramId()))) {
                programTileModels.add(programDataParsing.constructProgramTileModel(featuredProgram.getProgram(), offerMap));
            }
        }
        for (Programs program : programs) {
            if (programTileModels.stream().noneMatch(programTileModel -> programTileModel.getProgramId().equals(program.getProgramId()))) {
                programTileModels.add(programDataParsing.constructProgramTileModel(program, offerCountMap));
            }
        }
        profilingEndTimeMillis = new Date().getTime();
        log.info("Response construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        Map<String, Object> trendingPrograms = new HashMap<>();
        if (fromIndex >= programTileModels.size()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, null);
        }
        profilingStartTimeMillis = new Date().getTime();
        trendingPrograms.put(KeyConstants.KEY_PROGRAMS, programTileModels.subList(fromIndex, Math.min(fromIndex + pageSize, programTileModels.size())));
        trendingPrograms.put(KeyConstants.KEY_TOTAL_COUNT, programs.size());
        profilingEndTimeMillis = new Date().getTime();
        log.info("Pagination sublist : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        long apiEndTimeMillis = new Date().getTime();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("Member Trending programs ends.");

        return trendingPrograms;
    }

    /**
     * Filter for Overall list of programs.
     *
     * @return
     */
    public List<MemberFilterView> getAllProgramsFilter() {
        MemberFilterView expertiseFilter = new MemberFilterView();
        expertiseFilter.setFilterName(KeyConstants.KEY_EXPERTISE_FILTER_NAME);
        expertiseFilter.setType(KeyConstants.KEY_EXPERTISE_FILTER_TYPE);
        List<ExpertiseLevels> expertiseLevelList = expertiseLevelRepository.findAll();
        List<Filter> expertiseLevelFilterList = new ArrayList<>();
        for (ExpertiseLevels expertiseLevel : expertiseLevelList) {
            if (!expertiseLevel.getExpertiseLevel().equalsIgnoreCase(KeyConstants.KEY_ALL_LEVELS)) {
                Filter filter = new Filter();
                filter.setFilterId(expertiseLevel.getExpertiseLevelId());
                filter.setFilterName(expertiseLevel.getExpertiseLevel());
                expertiseLevelFilterList.add(filter);
            }
        }
        expertiseFilter.setFilters(expertiseLevelFilterList);
        MemberFilterView programTypeFilter = new MemberFilterView();
        programTypeFilter.setFilterName(KeyConstants.KEY_PROGRAM_TYPE_FILTER_NAME);
        programTypeFilter.setType(KeyConstants.KEY_PROGRAM_TYPE_FILTER_TYPE);
        List<ProgramTypes> programTypesListList = programTypeRepository.findByOrderByProgramTypeNameAsc();
        List<Filter> programTypesFilterList = new ArrayList<>();
        for (ProgramTypes programType : programTypesListList) {
            Filter filter = new Filter();
            filter.setFilterId(programType.getProgramTypeId());
            filter.setFilterName(programType.getProgramTypeName());
            programTypesFilterList.add(filter);
        }
        programTypeFilter.setFilters(programTypesFilterList);
        MemberFilterView durationFilter = new MemberFilterView();
        durationFilter.setFilterName(KeyConstants.KEY_DURATION_FILTER_NAME);
        durationFilter.setType(KeyConstants.KEY_DURATION_FILTER_TYPE);
        List<Duration> durationList = durationRepo.findAllByOrderByDurationAsc();
        List<Filter> durationFilterList = new ArrayList<>();
        for (Duration duration : durationList) {
            Filter filter = new Filter();
            filter.setFilterId(duration.getDurationId());
            filter.setFilterName(duration.getDuration() + " Days");
            durationFilterList.add(filter);
        }
        durationFilter.setFilters(durationFilterList);
        List<MemberFilterView> programsFilterViewList = new ArrayList<>();
        programsFilterViewList.add(expertiseFilter);
        programsFilterViewList.add(programTypeFilter);
        programsFilterViewList.add(durationFilter);
        return programsFilterViewList;
    }

    /**
     * Gets the overall list of programs.
     *
     * @param pageNo
     * @param pageSize
     * @param filterModel
     * @param search
     * @return
     */
    public Map<String, Object> getAllPrograms(int pageNo, int pageSize, AllProgramsFilterModel filterModel, Optional<String> search) {
        RequestParamValidator.pageSetup(pageNo, pageSize);
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize);
        Specification<Programs> programStatusSpec = ProgramSpecifications.getProgramByStatus(KeyConstants.KEY_PUBLISH);
        Specification<Programs> stripeActiveSpec = ProgramSpecifications.getProgramByStripeMapping();
        Specification<Programs> finalSpec;
        Page<Programs> programs;
        if ((filterModel.getDuration() == null || filterModel.getDuration().isEmpty()) && (filterModel.getExpertiseLevel() == null || filterModel.getExpertiseLevel().isEmpty()) && (filterModel.getProgramType() == null || filterModel.getProgramType().isEmpty())) {
            if (search.isPresent() && !search.get().isEmpty()) {
                Specification<Programs> titleSearchSpec = ProgramSpecifications.getProgramByTitleContains(search.get());
                finalSpec = programStatusSpec.and(titleSearchSpec).and(stripeActiveSpec);
            } else {
                finalSpec = programStatusSpec.and(stripeActiveSpec);
            }
        } else {
            List<Long> expertiseLevelIdList;
            if (filterModel.getExpertiseLevel() != null && !filterModel.getExpertiseLevel().isEmpty()) {
                expertiseLevelIdList = filterModel.getExpertiseLevel().stream().map(Filter::getFilterId).collect(Collectors.toList());
                ExpertiseLevels expertiseLevels = expertiseLevelRepository.findByExpertiseLevel(KeyConstants.KEY_ALL_LEVELS);
                expertiseLevelIdList.add(expertiseLevels.getExpertiseLevelId());
            } else {
                expertiseLevelIdList = expertiseLevelRepository.findAll().stream().map(ExpertiseLevels::getExpertiseLevelId).collect(Collectors.toList());
            }
            Specification<Programs> programExpertiseSpec = ProgramSpecifications.getProgramByExpertiseIn(expertiseLevelIdList);

            List<Long> programTypeIdList;
            if (filterModel.getProgramType() != null && !filterModel.getProgramType().isEmpty()) {
                programTypeIdList = filterModel.getProgramType().stream().map(Filter::getFilterId).collect(Collectors.toList());
            } else {
                programTypeIdList = programTypeRepository.findByOrderByProgramTypeNameAsc().stream().map(ProgramTypes::getProgramTypeId).collect(Collectors.toList());
            }
            Specification<Programs> programTypeSpec = ProgramSpecifications.getProgramByTypeIn(programTypeIdList);

            List<Long> durationIdList;
            if (filterModel.getDuration() != null && !filterModel.getDuration().isEmpty()) {
                durationIdList = filterModel.getDuration().stream().map(Filter::getFilterId).collect(Collectors.toList());
            } else {
                durationIdList = durationRepo.findAllByOrderByDurationAsc().stream().map(Duration::getDurationId).collect(Collectors.toList());
            }
            Specification<Programs> durationSpec = ProgramSpecifications.getProgramByDurationIn(durationIdList);
            if (search.isPresent() && !search.get().isEmpty()) {
                Specification<Programs> titleSearchSpec = ProgramSpecifications.getProgramByTitleContains(search.get());
                finalSpec = programStatusSpec.and(programExpertiseSpec).and(programTypeSpec).and(durationSpec).and(stripeActiveSpec).and(titleSearchSpec);
            } else {
                finalSpec = programStatusSpec.and(programExpertiseSpec).and(programTypeSpec).and(durationSpec).and(stripeActiveSpec);
            }
        }
        programs = programRepository.findAll(finalSpec, pageRequest);
        if (programs.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, null);
        }
        User user = userComponents.getAndValidateUser();
        long temp = System.currentTimeMillis();
        List<Long> programIdList = programs.stream().map(Programs::getProgramId).collect(Collectors.toList());
        Map<Long, Long> offerCountMap = discountsService.getNoOfCurrentAvailableOffersOfProgramsForMember(programIdList, user);
        log.info(StringConstants.LOG_OFFER_COUNT_QUERY + (System.currentTimeMillis() - temp));
        Map<String, Object> allPrograms = new HashMap<>();
        allPrograms.put(KeyConstants.KEY_PROGRAMS, programDataParsing.constructProgramTileModelWithFreeAccess(programs.getContent(), offerCountMap));
        allPrograms.put(KeyConstants.KEY_TOTAL_COUNT, programs.getTotalElements());
        return allPrograms;
    }

    /**
     * @param programId
     */
    public void resetProgramCompletion(Long programId) {
        User user = userComponents.getUser();
        resetProgramCompletion(programId, user, false);
    }

    /**
     * Method to reset completion data of program
     *
     * @param programId
     * @param user
     */
    @Transactional
    public void resetProgramCompletion(Long programId, User user, boolean isAutoRestart) {
        //Validation for params
        if (programId == null || programId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_NULL, MessageConstants.ERROR);
        }
        Programs program = programRepository.findByProgramId(programId);
        if (program == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, MessageConstants.ERROR);
        }
        if (!isAutoRestart) {
            List<FreeAccessProgram> freeProducts = freeAccessService.getAllUserFreeProgramsList();
            List<Long> freeProgramIds = freeAccessService.getUserSpecificFreeAccessProgramsIds();
            for(FreeAccessProgram freeProduct : freeProducts){
                freeProgramIds.add(freeProduct.getProgram().getProgramId());
            }
            if(!freeProgramIds.contains(programId)){
                boolean isSubscribed = subscriptionService.isProgramTrialOrSubscribedByUser(user.getUserId(), programId);
                if (!isSubscribed) {
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_SUBSCRIBED, MessageConstants.ERROR);
                }
            }
        }
        List<WorkoutCompletion> workoutCompletionList = workoutCompletionRepository.findByMemberUserIdAndProgramProgramId(user.getUserId(), programId);
        if (workoutCompletionList.size() != program.getDuration().getDuration().intValue()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PROGRAM_CANT_BE_RESET, MessageConstants.ERROR);
        }
        //Deleting Workout Completion entries
        if (!workoutCompletionList.isEmpty()) {
            //Saving Workout Completion Audit
            List<WorkoutCompletionAudit> workoutCompletionAuditList = new ArrayList<>();
            for (WorkoutCompletion workoutCompletion : workoutCompletionList) {
                WorkoutCompletionAudit workoutCompletionAudit = new WorkoutCompletionAudit();
                workoutCompletionAudit.setMember(user);
                workoutCompletionAudit.setProgram(program);
                workoutCompletionAudit.setWorkoutScheduleId(workoutCompletion.getWorkoutScheduleId());
                workoutCompletionAudit.setAction(DBConstants.RESET);
                workoutCompletionAuditList.add(workoutCompletionAudit);
            }
            workoutCompletionAuditRepository.saveAll(workoutCompletionAuditList);
            workoutCompletionRepository.deleteInBatch(workoutCompletionList);
        }
        //Deleting Circuit Completion entries
        List<CircuitCompletion> circuitCompletionList = circuitCompletionRepository.findByMemberUserIdAndProgramProgramId(user.getUserId(), programId);
        if (!circuitCompletionList.isEmpty()) {
            //Saving Circuit Completion Audit
            List<CircuitCompletionAudit> circuitCompletionAuditList = new ArrayList<>();
            for (CircuitCompletion circuitCompletion : circuitCompletionList) {
                CircuitCompletionAudit circuitCompletionAudit = new CircuitCompletionAudit();
                circuitCompletionAudit.setMember(user);
                circuitCompletionAudit.setProgram(program);
                circuitCompletionAudit.setWorkoutScheduleId(circuitCompletion.getWorkoutScheduleId());
                circuitCompletionAudit.setCircuitScheduleId(circuitCompletion.getCircuitScheduleId());
                circuitCompletionAudit.setAction(DBConstants.RESET);
                circuitCompletionAuditList.add(circuitCompletionAudit);
            }
            circuitCompletionAuditRepository.saveAll(circuitCompletionAuditList);
            circuitCompletionRepository.deleteInBatch(circuitCompletionList);
        }
        //Deleting Exercise Completion entries
        List<ExerciseCompletion> exerciseCompletionList = exerciseCompletionRepository.findByMemberUserIdAndProgramProgramId(user.getUserId(), programId);
        if (!exerciseCompletionList.isEmpty()) {
            //Saving Exercise Completion Audit
            List<ExerciseCompletionAudit> exerciseCompletionAuditList = new ArrayList<>();
            for (ExerciseCompletion exerciseCompletion : exerciseCompletionList) {
                ExerciseCompletionAudit exerciseCompletionAudit = new ExerciseCompletionAudit();
                exerciseCompletionAudit.setMember(user);
                exerciseCompletionAudit.setProgram(program);
                exerciseCompletionAudit.setWorkoutScheduleId(exerciseCompletion.getWorkoutScheduleId());
                exerciseCompletionAudit.setCircuitScheduleId(exerciseCompletion.getCircuitScheduleId());
                exerciseCompletionAudit.setExerciseScheduleId(exerciseCompletion.getExerciseScheduleId());
                exerciseCompletionAudit.setAction(DBConstants.RESET);
                exerciseCompletionAuditList.add(exerciseCompletionAudit);
            }
            exerciseCompletionAuditRepository.saveAll(exerciseCompletionAuditList);
            exerciseCompletionRepository.deleteInBatch(exerciseCompletionList);
        }
        //When program completion is reset, Program rating submission is updated as allowed
        ProgramRating programRating = programRatingRepository.findByProgramAndUser(program, user);
        if (programRating != null) {
            programRating.setIsSubmissionAllowed(true);
            programRatingRepository.save(programRating);
        }
        ProgramCompletion programCompletion = programCompletionRepository.findByMemberAndProgram(user, program);
        if (programCompletion != null) {
            programCompletionRepository.delete(programCompletion);
        }
        ProgramCompletionAudit programCompletionAudit = new ProgramCompletionAudit();
        programCompletionAudit.setMember(user);
        programCompletionAudit.setProgram(program);
        programCompletionAudit.setAction(DBConstants.RESET);
        programCompletionAuditRepository.save(programCompletionAudit);
    }

    /**
     * @param programId
     * @param workoutScheduleId
     */
    @Transactional
    public ExerciseCompletionResponse postRestActivityCompletedStatus(Long programId, Long workoutScheduleId, Long completionDateInMillis) {
        User user = userComponents.getUser();
        //Validation for params
        if (programId == null || programId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_NULL, MessageConstants.ERROR);
        }
        Programs program = programRepository.findByProgramId(programId);
        if (program == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, MessageConstants.ERROR);
        }
        List<FreeAccessProgram> freeProducts = freeAccessService.getAllUserFreeProgramsList();
        List<Long> freeProgramIds = freeAccessService.getUserSpecificFreeAccessProgramsIds();
        for(FreeAccessProgram freeProduct : freeProducts){
            freeProgramIds.add(freeProduct.getProgram().getProgramId());
        }
        if(!freeProgramIds.contains(programId)){
            boolean isSubscribed = subscriptionService.isProgramTrialOrSubscribedByUser(user.getUserId(), programId);
            if (!isSubscribed) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_SUBSCRIBED, MessageConstants.ERROR);
            }
        }
        if (workoutScheduleId == null || workoutScheduleId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_ID_NULL, MessageConstants.ERROR);
        }
        WorkoutSchedule workoutSchedule = workoutScheduleRepository.findByWorkoutScheduleId(workoutScheduleId);
        if (workoutSchedule == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_NOT_FOUND, MessageConstants.ERROR);
        }
        List<Long> workoutScheduleIdList = program.getWorkoutSchedules().stream().map(WorkoutSchedule::getWorkoutScheduleId).collect(Collectors.toList());
        if (!workoutScheduleIdList.contains(workoutScheduleId)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_INCORRECT, MessageConstants.ERROR);
        }
        if (!(workoutSchedule.isRestDay() && !DBConstants.REST.equals(workoutSchedule.getInstructorRestActivity().getRestActivityToMetricMapping().getRestActivity().getRestActivity()))) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_WORKOUT_SCHEDULE_NOT_REST_ACTIVITY, MessageConstants.ERROR);
        }
        if (completionDateInMillis == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_DATE_NULL, MessageConstants.ERROR);
        }
        Date completionDate = new Date(completionDateInMillis);
        Date today = new Date();
        if (completionDate.after(today)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FUTURE_DATE_NOT_ALLOWED, MessageConstants.ERROR);
        }
        ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), programId);
        //Validating completion time for Rest activity completion
        long startOrder = workoutSchedule.getPrograms().getWorkoutSchedules().stream().mapToLong(WorkoutSchedule::getOrder).min().getAsLong();
        long order = workoutSchedule.getOrder().longValue();
        if (startOrder == order) {
            if (completionDate.before(programSubscription.getSubscribedDate())) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_COMPLETION_DATE_BEFORE_SUNSCRIBED_DATE, MessageConstants.ERROR);
            }
        } else {
            //Checking if previousWorkoutSchedule is completed
            WorkoutSchedule previousWorkoutSchedule = workoutScheduleRepository.findByOrderAndProgramsProgramId(order - 1, programId);
            WorkoutCompletion previousWorkoutCompletion = workoutCompletionRepository.findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdOrderByCompletedDateDesc(user.getUserId(), programId, previousWorkoutSchedule.getWorkoutScheduleId());
            if (previousWorkoutCompletion == null) {
                //if previousWorkoutSchedule is not completed, current workout schedule can not be completed.
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PREVIOUS_WORKOUT_SCHEDULE_INCOMPLETE, MessageConstants.ERROR);
            } else {
                if (fitwiseUtils.isSameDay(previousWorkoutCompletion.getCompletedDate(), completionDate)) {
                    //if previousWorkoutSchedule is completed in the same day provided, current workout schedule can not be completed.
                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PREVIOUS_WORKOUT_SCHEDULE_COMPLETED_ON_SAME_DAY, MessageConstants.ERROR);
                }
                if (completionDate.before(previousWorkoutCompletion.getCompletedDate())) {
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_COMPLETION_DATE_BEFORE_PREVIOUS_COMPLETION_DATE, MessageConstants.ERROR);
                }
            }
        }
        boolean isworkoutCompletedNow = false;
        WorkoutCompletion workoutCompletion = workoutCompletionRepository.findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdOrderByCompletedDateDesc(user.getUserId(), programId, workoutScheduleId);
        if (workoutCompletion == null) {
            workoutCompletion = new WorkoutCompletion();
            workoutCompletion.setMember(user);
            workoutCompletion.setProgram(program);
            workoutCompletion.setWorkoutScheduleId(workoutSchedule.getWorkoutScheduleId());
            workoutCompletion.setCompletedDate(completionDate);
            workoutCompletionRepository.save(workoutCompletion);
            //Workout completion audit
            WorkoutCompletionAudit workoutCompletionAudit = new WorkoutCompletionAudit();
            workoutCompletionAudit.setMember(user);
            workoutCompletionAudit.setProgram(program);
            workoutCompletionAudit.setWorkoutScheduleId(workoutSchedule.getWorkoutScheduleId());
            workoutCompletionAudit.setAction(DBConstants.COMPLETED);
            workoutCompletionAuditRepository.save(workoutCompletionAudit);
            isworkoutCompletedNow = true;
        }
        ExerciseCompletionResponse exerciseCompletionResponse = new ExerciseCompletionResponse();
        exerciseCompletionResponse.setIsWorkoutCompletedNow(isworkoutCompletedNow);
        boolean isworkoutCompleted = false;
        workoutCompletion = workoutCompletionRepository.findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdOrderByCompletedDateDesc(user.getUserId(), programId, workoutScheduleId);
        if (workoutCompletion != null) {
            isworkoutCompleted = true;
        }
        exerciseCompletionResponse.setIsWorkoutCompleted(isworkoutCompleted);
        boolean isProgramRatingAllowed = false;
        //Program completion status
        int completedWorkouts = workoutCompletionRepository.countByMemberUserIdAndProgramProgramId(user.getUserId(), programId);
        boolean isProgramCompleted = false;
        if (completedWorkouts == program.getDuration().getDuration().intValue()) {
            isProgramCompleted = true;
        }
        //Program rating submission allowed value in DB
        boolean isProgramRatingSubmissionAllowed = false;
        ProgramRating programRating = programRatingRepository.findByProgramAndUser(program, user);
        if (programRating == null || programRating.getIsSubmissionAllowed()) {
            isProgramRatingSubmissionAllowed = true;
        }
        if (isProgramCompleted && isProgramRatingSubmissionAllowed) {
            isProgramRatingAllowed = true;
        }
        exerciseCompletionResponse.setIsProgramRatingAllowed(isProgramRatingAllowed);
        Float programRatingValue = null;
        if (programRating != null) {
            programRatingValue = programRating.getProgramRating();
        }
        exerciseCompletionResponse.setProgramRating(programRatingValue);
        //Adding Program completion entry and audit
        if (isProgramCompleted) {
            ProgramCompletion programCompletion = programCompletionRepository.findByMemberAndProgram(user, program);
            if (programCompletion == null) {
                programCompletion = new ProgramCompletion();
                programCompletion.setMember(user);
                programCompletion.setProgram(program);
                programCompletion.setCompletedDate(today);
                programCompletionRepository.save(programCompletion);
                ProgramCompletionAudit programCompletionAudit = new ProgramCompletionAudit();
                programCompletionAudit.setMember(user);
                programCompletionAudit.setProgram(program);
                programCompletionAudit.setAction(DBConstants.COMPLETED);
                programCompletionAuditRepository.save(programCompletionAudit);
            }
        }
        return exerciseCompletionResponse;
    }

    /**
     * @return
     */
    public List<FlaggedVideoReason> getExerciseFlaggingReasons() {
        return flaggedVideoReasonsRepository.findAll();
    }

    /**
     * Flag a video
     * @param exerciseId
     * @param reasonId
     */
    public ResponseModel flagExercise(Long exerciseId, Long reasonId) {
        log.info("Flag exercise starts.");
        long apiStartTimeMillis = new Date().getTime();
        User user = userComponents.getUser();
        Exercises exercise = validationService.validateExerciseId(exerciseId);
        log.info("Get user and validate exercise : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        if (reasonId == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FLAG_REASON_ID_NULL, MessageConstants.ERROR);
        }
        Optional<FlaggedVideoReason> flaggedVideoReasonOptional = flaggedVideoReasonsRepository.findById(reasonId);
        if (!flaggedVideoReasonOptional.isPresent()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FLAG_REASON_ID_NOT_FOUND, MessageConstants.ERROR);
        }
        FlaggedExercise flaggedExercise = flaggedExerciseRepository.findByExerciseExerciseIdAndUserUserIdAndFlaggedVideoReasonFeedbackId(exerciseId, user.getUserId(), reasonId);
        if (flaggedExercise != null) {
            throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_EXERCISE_FLAGGED_ALREADY, MessageConstants.ERROR);
        }

        flaggedExercise = new FlaggedExercise();
        flaggedExercise.setExercise(exercise);
        flaggedExercise.setFlaggedVideoReason(flaggedVideoReasonOptional.get());
        flaggedExercise.setUser(user);
        log.info("Basic validation with query DB and creating flag model : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        flaggedExerciseRepository.save(flaggedExercise);
        log.info("Query to save flag exercise : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        int flaggedCount;
        FlaggedExercisesSummary flaggedExercisesSummary = flaggedExercisesSummaryRepository.findByExerciseExerciseId(exerciseId);
        if (flaggedExercisesSummary == null) {
            flaggedExercisesSummary = new FlaggedExercisesSummary();
            flaggedExercisesSummary.setFirstFlaggedDate(new Date());
            flaggedExercisesSummary.setFlagStatus(DBConstants.KEY_REPORTED);
            flaggedExercisesSummary.setExercise(exercise);
            flaggedCount = 1;
        } else {
            flaggedCount = flaggedExercisesSummary.getFlaggedCount() + 1;
        }
        flaggedExercisesSummary.setLatestFlaggedDate(new Date());
        flaggedExercisesSummary.setFlaggedCount(flaggedCount);

        flaggedExercisesSummaryRepository.save(flaggedExercisesSummary);
        log.info("Query to save or update the flagged exercise summary : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setPayload(null);
        response.setMessage(MessageConstants.MSG_EXERCISE_FLAGGED);
        log.info("Response construction : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (profilingEndTimeMillis - apiStartTimeMillis));
        log.info("Flag exercise ends.");
        return response;

    }


    /**
     * video cache sync
     *
     * @param videoCachingRequestModel
     * @return
     */
    @Transactional
    public String videoCacheSync(VideoCachingRequestModel videoCachingRequestModel) {
        log.info("Video cache sync starts");
        long start = new Date().getTime();
        long profilingStart;
        User user = userComponents.getUser();
        Date now = new Date();
        log.info("Get user : Time taken in millis : "+(new Date().getTime() - start));
        for (VideoCacheProgramModel videoCacheProgramModel : videoCachingRequestModel.getPrograms()) {
            profilingStart = new Date().getTime();
            long basicValidation = new Date().getTime();
            if (videoCacheProgramModel.getProgramId() == null || videoCacheProgramModel.getProgramId() == 0) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_NULL, MessageConstants.ERROR);
            }
            Long programId = videoCacheProgramModel.getProgramId();
            Programs program = programRepository.findByProgramId(programId);
            log.info("Query get program from DB : Time taken in millis : " + (new Date().getTime() - profilingStart));
            profilingStart = new Date().getTime();
            if (program == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, MessageConstants.ERROR);
            }
            boolean isFreeProgram = isFreeProgram(programId);
            if(!isFreeProgram){
                boolean isSubscribed = subscriptionService.isProgramTrialOrSubscribedByUser(user.getUserId(), programId);
                log.info("check whether the program Trial or subscribed by user : Time taken in millis : " + (new Date().getTime() - profilingStart));
                profilingStart = new Date().getTime();
                if (!isSubscribed) {
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_SUBSCRIBED, MessageConstants.ERROR);
                }
            }
            ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), programId);
            log.info("Query get program subscription from DB : Time taken in millis : " + (new Date().getTime() - profilingStart));
            if (videoCacheProgramModel.getWorkoutSchedules() == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_ID_NULL, MessageConstants.ERROR);
            }
            List<VideoCacheWorkoutModel> videoCacheWorkoutModelList = videoCacheProgramModel.getWorkoutSchedules();
            log.info("Basic validation : Time taken in millis : " + (new Date().getTime() - basicValidation));
            profilingStart = new Date().getTime();
            List<Long> workoutScheduleIdList = program.getWorkoutSchedules().stream().map(WorkoutSchedule::getWorkoutScheduleId)
                    .collect(Collectors.toList());
            log.info("Extract workout schedule id's : Time taken in millis : " + (new Date().getTime() - profilingStart));
            profilingStart = new Date().getTime();
            for (VideoCacheWorkoutModel videoCacheWorkoutModel1 : videoCacheWorkoutModelList) {
                if (videoCacheWorkoutModel1.getWorkoutScheduleId() == null || videoCacheWorkoutModel1.getWorkoutScheduleId() == 0) {
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_ID_NULL, MessageConstants.ERROR);
                }
                long workoutScheduleId = videoCacheWorkoutModel1.getWorkoutScheduleId();
                WorkoutSchedule workoutSchedule = workoutScheduleRepository.findByWorkoutScheduleId(workoutScheduleId);
                if (workoutSchedule == null) {
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_NOT_FOUND, MessageConstants.ERROR);
                }
                if (!workoutScheduleIdList.contains(videoCacheWorkoutModel1.getWorkoutScheduleId())) {
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_INCORRECT, MessageConstants.ERROR);
                }
                //For trial program, can complete Exercise only from trial workout schedules
                if(!isFreeProgram){
                    SubscriptionStatus subscriptionStatus = subscriptionService.getMemberProgramSubscriptionStatus(user.getUserId(), programId);
                    if (subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_TRIAL)) {
                        int trialWorkoutScheduleCount = fitwiseUtils.getTrialWorkoutScheduleCountForProgram(user, program);
                        if (workoutSchedule.getOrder().intValue() > trialWorkoutScheduleCount) {
                            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_TRIAL_PROGRAM_CANT_COMPLETE_WORKOUT, MessageConstants.ERROR);
                        }
                    }
                }
                Date workoutCompletionDate = null;
                if (videoCacheWorkoutModel1.getWorkoutCompletionDateInMillis() != null && videoCacheWorkoutModel1.getWorkoutCompletionDateInMillis() != 0) {
                    workoutCompletionDate = new Date(videoCacheWorkoutModel1.getWorkoutCompletionDateInMillis());
                }
                long startOrder = workoutSchedule.getPrograms().getWorkoutSchedules().stream().mapToLong(WorkoutSchedule::getOrder).min().getAsLong();
                long order = workoutSchedule.getOrder().longValue();
                if (workoutCompletionDate != null) {
                    if (startOrder != order) {
                        WorkoutSchedule previousWorkoutSchedule = workoutScheduleRepository.findByOrderAndProgramsProgramId(order - 1, programId);
                        WorkoutCompletion previousWorkoutCompletion = workoutCompletionRepository.findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdOrderByCompletedDateDesc(user.getUserId(), programId, previousWorkoutSchedule.getWorkoutScheduleId());
                        if (previousWorkoutCompletion == null) {
                            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PREVIOUS_WORKOUT_SCHEDULE_INCOMPLETE, MessageConstants.ERROR);
                        } else {
                            if (fitwiseUtils.isSameDay(previousWorkoutCompletion.getCompletedDate(), workoutCompletionDate)) {
                                //if previousWorkoutSchedule is completed in the same day provided, current workout schedule can not be completed.
                                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PREVIOUS_WORKOUT_SCHEDULE_COMPLETED_TODAY, MessageConstants.ERROR);
                            }
                        }
                    } else {
                        if (!isFreeProgram && workoutCompletionDate.before(programSubscription.getSubscribedDate())) {
                            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_COMPLETION_DATE_BEFORE_SUNSCRIBED_DATE, MessageConstants.ERROR);
                        }
                    }
                }
                //Rest and Rest Active Day Completion
                if (workoutCompletionDate != null && workoutCompletionDate.after(now)) {
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FUTURE_DATE_NOT_ALLOWED, MessageConstants.ERROR);
                }
                WorkoutCompletion workoutCompletionForRest = workoutCompletionRepository.findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdOrderByCompletedDateDesc(user.getUserId(), programId, workoutScheduleId);
                if (workoutSchedule.isRestDay()) {
                    if (workoutCompletionDate != null && workoutCompletionForRest == null) {
                        workoutCompletionForRest = new WorkoutCompletion();
                        workoutCompletionForRest.setMember(user);
                        workoutCompletionForRest.setProgram(program);
                        workoutCompletionForRest.setWorkoutScheduleId(workoutSchedule.getWorkoutScheduleId());
                        workoutCompletionForRest.setCompletedDate(workoutCompletionDate);
                        workoutCompletionRepository.save(workoutCompletionForRest);
                        //Workout completion audit
                        WorkoutCompletionAudit workoutCompletionAudit = new WorkoutCompletionAudit();
                        workoutCompletionAudit.setMember(user);
                        workoutCompletionAudit.setProgram(program);
                        workoutCompletionAudit.setWorkoutScheduleId(workoutSchedule.getWorkoutScheduleId());
                        workoutCompletionAudit.setAction(DBConstants.COMPLETED);
                        workoutCompletionAuditRepository.save(workoutCompletionAudit);
                    }
                } else {
                    List<VideoCacheExerciseModel> videoCacheExerciseModels = videoCacheWorkoutModel1.getVideoCacheExerciseModels();
                    List<Long> circuitScheduleIdList = workoutSchedule.getWorkout().getCircuitSchedules().stream().map(CircuitSchedule::getCircuitScheduleId).collect(Collectors.toList());
                    for (VideoCacheExerciseModel videoCacheExerciseModel : videoCacheExerciseModels) {
                        if (videoCacheExerciseModel.getCircuitScheduleId() == null || videoCacheExerciseModel.getCircuitScheduleId() == 0) {
                            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_SCHEDULE_ID_NULL, MessageConstants.ERROR);
                        }
                        long circuitScheduleId = videoCacheExerciseModel.getCircuitScheduleId();
                        Optional<CircuitSchedule> circuitScheduleOptional = circuitScheduleRepository.findById(circuitScheduleId);
                        if (!circuitScheduleOptional.isPresent()) {
                            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_SCHEDULE_NOT_FOUND, MessageConstants.ERROR);
                        }
                        CircuitSchedule circuitSchedule = circuitScheduleOptional.get();
                        if (!circuitScheduleIdList.contains(videoCacheExerciseModel.getCircuitScheduleId())) {
                            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_SCHEDULE_INCORRECT, MessageConstants.ERROR);
                        }
                        List<Long> exerciseScheduleIdList = null;
                        List<Long> circuitAndVoiceOverMappingIdList = null;
                        boolean isAlreadyCompleted = false;
                        int noOfExercisesCompletedInCircuit = 0;
                        int noOfVoiceOversCompletedInCircuit = 0;
                        if (videoCacheExerciseModel.getExerciseCompletionDateInMillis() == null || videoCacheExerciseModel.getExerciseCompletionDateInMillis() == 0) {
                            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_COMPLETION_DATE_NULL, MessageConstants.ERROR);
                        }
                        Date exerciseCompletionDate = new Date(videoCacheExerciseModel.getExerciseCompletionDateInMillis());
                        //Check whether the exercise completion date is future date or not
                        if (exerciseCompletionDate.after(now)) {
                            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FUTURE_DATE_NOT_ALLOWED, MessageConstants.ERROR);
                        }
                        //Check whether the exercise completion date is before subscribed date or not
                        if (!isFreeProgram &&  exerciseCompletionDate.before(programSubscription.getSubscribedDate())) {
                            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_COMPLETION_DATE_BEFORE_SUNSCRIBED_DATE, MessageConstants.ERROR);
                        }
                        Boolean isAudioCircuit = videoCacheExerciseModel.isAudio();
                        if (isAudioCircuit) {
                            circuitAndVoiceOverMappingIdList = circuitSchedule.getCircuit().getCircuitAndVoiceOverMappings().stream().map(CircuitAndVoiceOverMapping::getCircuitAndVoiceOverMappingId).collect(Collectors.toList());
                            Long circuitAndVoiceOverMappingId = videoCacheExerciseModel.getCircuitAndVoiceOverMappingId();
                            if (circuitAndVoiceOverMappingId == null || circuitAndVoiceOverMappingId == 0) {
                                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_AND_VOICE_OVER_MAPPING_ID_NULL, MessageConstants.ERROR);
                            }
                            Optional<CircuitAndVoiceOverMapping> circuitAndVoiceOverMappingOptional = circuitAndVoiceOverMappingRepository.findById(circuitAndVoiceOverMappingId);
                            if (!circuitAndVoiceOverMappingOptional.isPresent()) {
                                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_AND_VOICE_OVER_MAPPING_NOT_FOUND, MessageConstants.ERROR);
                            }
                            CircuitAndVoiceOverMapping circuitAndVoiceOverMapping = circuitAndVoiceOverMappingOptional.get();
                            if (!circuitAndVoiceOverMappingIdList.contains(circuitAndVoiceOverMappingId)) {
                                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_VOICE_OVER_MAPPING_ID_INCORRECT, MessageConstants.ERROR);
                            }
                            CircuitVoiceOverMappingCompletion circuitVoiceOverMappingCompletion = circuitVoiceOverMappingCompletionRepository.findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdAndCircuitScheduleIdAndCircuitVoiceOverMappingIdOrderByCompletedDateDesc(user.getUserId(), programId, workoutScheduleId, circuitScheduleId, circuitAndVoiceOverMappingId);
                            if (circuitVoiceOverMappingCompletion == null) {
                                circuitVoiceOverMappingCompletion = new CircuitVoiceOverMappingCompletion();
                                circuitVoiceOverMappingCompletion.setCircuitVoiceOverMappingId(circuitAndVoiceOverMappingId);
                                circuitVoiceOverMappingCompletion.setCircuitScheduleId(circuitScheduleId);
                                circuitVoiceOverMappingCompletion.setWorkoutScheduleId(workoutScheduleId);
                                circuitVoiceOverMappingCompletion.setMember(user);
                                circuitVoiceOverMappingCompletion.setProgram(program);
                                circuitVoiceOverMappingCompletion.setCompletedDate(exerciseCompletionDate);
                                circuitVoiceOverMappingCompletionRepository.save(circuitVoiceOverMappingCompletion);
                            } else {
                                isAlreadyCompleted = true;
                            }
                            String voiceOverAuditAction = isAlreadyCompleted ? DBConstants.REPEAT : DBConstants.COMPLETED;
                            //voice-over completion audit entry
                            CircuitVoiceOverMappingCompletionAudit circuitVoiceOverMappingCompletionAudit = new CircuitVoiceOverMappingCompletionAudit();
                            circuitVoiceOverMappingCompletionAudit.setMember(user);
                            circuitVoiceOverMappingCompletionAudit.setProgram(program);
                            circuitVoiceOverMappingCompletionAudit.setWorkoutScheduleId(workoutSchedule.getWorkoutScheduleId());
                            circuitVoiceOverMappingCompletionAudit.setCircuitScheduleId(circuitSchedule.getCircuitScheduleId());
                            circuitVoiceOverMappingCompletionAudit.setCircuitAndVoiceOverMappingId(circuitAndVoiceOverMapping.getCircuitAndVoiceOverMappingId());
                            circuitVoiceOverMappingCompletionAudit.setAction(voiceOverAuditAction);
                            circuitVoiceOverMappingCompletionAuditRepo.save(circuitVoiceOverMappingCompletionAudit);
                            List<CircuitVoiceOverMappingCompletion> voiceOversCompletedInCircuitList = circuitVoiceOverMappingCompletionRepository.findByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdAndCircuitScheduleId(user.getUserId(), programId, workoutScheduleId, circuitScheduleId);
                            //removing duplicates
                            voiceOversCompletedInCircuitList = voiceOversCompletedInCircuitList.stream()
                                    .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparingLong(CircuitVoiceOverMappingCompletion::getCircuitVoiceOverMappingId))), ArrayList::new));
                            noOfVoiceOversCompletedInCircuit = voiceOversCompletedInCircuitList.size();
                        } else {
                            List<ExerciseSchedulers> exerciseSchedulersList = exerciseScheduleRepository.findByCircuitCircuitId(circuitSchedule.getCircuit().getCircuitId());
                            exerciseScheduleIdList = exerciseSchedulersList.stream().map(ExerciseSchedulers::getExerciseScheduleId).collect(Collectors.toList());
                            Long exerciseScheduleId = videoCacheExerciseModel.getExerciseScheduleId();
                            if (exerciseScheduleId == null || exerciseScheduleId == 0) {
                                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_SCHEDULE_ID_NULL, MessageConstants.ERROR);
                            }
                            ExerciseSchedulers exerciseSchedulers = exerciseScheduleRepository.findByExerciseScheduleId(exerciseScheduleId);
                            if (exerciseSchedulers == null) {
                                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_SCHEDULE_NOT_FOUND, MessageConstants.ERROR);
                            }
                            if (!exerciseScheduleIdList.contains(exerciseScheduleId)) {
                                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_SCHEDULE_INCORRECT, MessageConstants.ERROR);
                            }
                            //Exercise completion entry
                            //Since exercise repeats is implemented as same video being looped, if the video is played even once, it is considered as completed.
                            ExerciseCompletion exerciseCompletion = exerciseCompletionRepository.findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdAndCircuitScheduleIdAndExerciseScheduleIdOrderByCompletedDateDesc(user.getUserId(), programId, workoutScheduleId, circuitScheduleId, exerciseScheduleId);
                            if (exerciseCompletion == null) {
                                exerciseCompletion = new ExerciseCompletion();
                                exerciseCompletion.setMember(user);
                                exerciseCompletion.setProgram(program);
                                exerciseCompletion.setCompletedDate(exerciseCompletionDate);
                                exerciseCompletion.setCircuitScheduleId(circuitSchedule.getCircuitScheduleId());
                                exerciseCompletion.setExerciseScheduleId(exerciseSchedulers.getExerciseScheduleId());
                                exerciseCompletion.setWorkoutScheduleId(workoutSchedule.getWorkoutScheduleId());
                                exerciseCompletionRepository.save(exerciseCompletion);
                            } else {
                                isAlreadyCompleted = true;
                            }
                            String exerciseAuditAction = isAlreadyCompleted ? DBConstants.REPEAT : DBConstants.COMPLETED;
                            //Exercise completion audit entry
                            ExerciseCompletionAudit exerciseCompletionAudit = new ExerciseCompletionAudit();
                            exerciseCompletionAudit.setMember(user);
                            exerciseCompletionAudit.setProgram(program);
                            exerciseCompletionAudit.setWorkoutScheduleId(workoutSchedule.getWorkoutScheduleId());
                            exerciseCompletionAudit.setCircuitScheduleId(circuitSchedule.getCircuitScheduleId());
                            exerciseCompletionAudit.setExerciseScheduleId(exerciseSchedulers.getExerciseScheduleId());
                            exerciseCompletionAudit.setAction(exerciseAuditAction);
                            exerciseCompletionAuditRepository.save(exerciseCompletionAudit);
                            //Autopopulating rest exercise in the circuit. Audit not done for rest exercise.
                            Set<ExerciseSchedulers> exerciseSchedules = circuitSchedule.getCircuit().getExerciseSchedules();
                            for (ExerciseSchedulers exSchedule : exerciseSchedules) {
                                if (exSchedule.getWorkoutRestVideo() != null) {
                                    ExerciseCompletion restExerciseCompletion = exerciseCompletionRepository.findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdAndCircuitScheduleIdAndExerciseScheduleIdOrderByCompletedDateDesc(user.getUserId(), programId, workoutScheduleId, circuitScheduleId, exSchedule.getExerciseScheduleId());
                                    if (restExerciseCompletion == null) {
                                        restExerciseCompletion = new ExerciseCompletion();
                                        restExerciseCompletion.setMember(user);
                                        restExerciseCompletion.setProgram(program);
                                        restExerciseCompletion.setWorkoutScheduleId(workoutSchedule.getWorkoutScheduleId());
                                        restExerciseCompletion.setCircuitScheduleId(circuitSchedule.getCircuitScheduleId());
                                        restExerciseCompletion.setExerciseScheduleId(exSchedule.getExerciseScheduleId());
                                        restExerciseCompletion.setCompletedDate(exerciseCompletionDate);
                                        exerciseCompletionRepository.save(restExerciseCompletion);
                                    }
                                }
                            }
                            //flag exercise
                            if (videoCacheExerciseModel.getFlagReasonId() != null && videoCacheExerciseModel.getFlagReasonId() != 0) {
                                Optional<FlaggedVideoReason> flaggedVideoReasonOptional = flaggedVideoReasonsRepository.findById(videoCacheExerciseModel.getFlagReasonId());
                                if (!flaggedVideoReasonOptional.isPresent()) {
                                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FLAG_REASON_ID_NOT_FOUND, MessageConstants.ERROR);
                                }
                                FlaggedVideoReason flaggedVideoReason = flaggedVideoReasonOptional.get();
                                Exercises exercise = validationService.validateExerciseId(exerciseSchedulers.getExercise().getExerciseId());
                                FlaggedExercise flaggedExercise = flaggedExerciseRepository.findByExerciseExerciseIdAndUserUserIdAndFlaggedVideoReasonFeedbackId(exerciseSchedulers.getExercise().getExerciseId(), user.getUserId(), flaggedVideoReason.getFeedbackId());
                                if (flaggedExercise == null) {
                                    flaggedExercise = new FlaggedExercise();
                                }
                                flaggedExercise.setExercise(exercise);
                                flaggedExercise.setFlaggedVideoReason(flaggedVideoReason);
                                flaggedExercise.setUser(user);
                                flaggedExerciseRepository.save(flaggedExercise);
                                int flaggedCount;
                                FlaggedExercisesSummary flaggedExercisesSummary = flaggedExercisesSummaryRepository.findByExerciseExerciseId(exercise.getExerciseId());
                                if (flaggedExercisesSummary == null) {
                                    flaggedExercisesSummary = new FlaggedExercisesSummary();
                                    flaggedExercisesSummary.setFirstFlaggedDate(exerciseCompletionDate);
                                    flaggedExercisesSummary.setFlagStatus(DBConstants.KEY_REPORTED);
                                    flaggedExercisesSummary.setExercise(exercise);
                                    flaggedCount = 1;
                                } else {
                                    flaggedCount = flaggedExercisesSummary.getFlaggedCount() + 1;
                                    flaggedExercisesSummary.setLatestFlaggedDate(exerciseCompletionDate);
                                }
                                flaggedExercisesSummary.setFlaggedCount(flaggedCount);
                                flaggedExercisesSummaryRepository.save(flaggedExercisesSummary);
                            }
                            //Check whether the circuit is completed or not
                            List<ExerciseCompletion> exercisesCompletedInCircuitList = exerciseCompletionRepository.findByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdAndCircuitScheduleId(user.getUserId(), programId, workoutScheduleId, circuitScheduleId);
                            //removing duplicates
                            exercisesCompletedInCircuitList = exercisesCompletedInCircuitList.stream()
                                    .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparingLong(ExerciseCompletion::getExerciseScheduleId))), ArrayList::new));

                            noOfExercisesCompletedInCircuit = exercisesCompletedInCircuitList.size();
                        }
                        //Since Circuit repeats is implemented as duplicate item in playlist, it is completed based on repeats.
                        if ((!videoCacheExerciseModel.isAudio() && exerciseScheduleIdList.size() == noOfExercisesCompletedInCircuit) || (videoCacheExerciseModel.isAudio() && circuitAndVoiceOverMappingIdList.size() == noOfVoiceOversCompletedInCircuit)) {
                            boolean isAllExInCircuitCompleted = false;
                            String circuitAuditAction = DBConstants.COMPLETED;
                            CircuitCompletion circuitCompletion = circuitCompletionRepository.findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdAndCircuitScheduleIdOrderByCompletedDateDesc(user.getUserId(), programId, workoutScheduleId, circuitScheduleId);
                            if (circuitCompletion == null) {
                                circuitCompletion = new CircuitCompletion();
                                circuitCompletion.setMember(user);
                                circuitCompletion.setProgram(program);
                                circuitCompletion.setWorkoutScheduleId(workoutSchedule.getWorkoutScheduleId());
                                circuitCompletion.setCircuitScheduleId(circuitSchedule.getCircuitScheduleId());
                                circuitCompletion.setCompletedDate(exerciseCompletionDate);
                                circuitCompletion.setNoOfTimesPlayed(1);
                                // Since there is no repeats for audio circuit
                                if (isAudioCircuit) {
                                    circuitCompletion.setCompleted(true);
                                } else {
                                    if (circuitSchedule.getRepeat() == 1) {
                                        circuitCompletion.setCompleted(true);
                                    } else {
                                        //deleting entries in exercise completion table, to mark completion for circuit repeat
                                        List<ExerciseCompletion> exerciseCompletionList = exerciseCompletionRepository.findByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdAndCircuitScheduleId(user.getUserId(), programId, workoutScheduleId, circuitScheduleId);
                                        for (ExerciseCompletion deleteExerciseCompletion : exerciseCompletionList) {
                                            exerciseCompletionRepository.delete(deleteExerciseCompletion);
                                        }
                                    }
                                }
                                circuitCompletionRepository.save(circuitCompletion);
                                isAllExInCircuitCompleted = true;
                            } else if (circuitCompletion != null && !circuitCompletion.isCompleted()) {
                                int noOfTimesPlayed = circuitCompletion.getNoOfTimesPlayed() + 1;
                                circuitCompletion.setNoOfTimesPlayed(noOfTimesPlayed);
                                if (noOfTimesPlayed < (circuitSchedule.getRepeat())) {
                                    List<ExerciseCompletion> exerciseCompletionList = exerciseCompletionRepository.findByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdAndCircuitScheduleId(user.getUserId(), programId, workoutScheduleId, circuitScheduleId);
                                    for (ExerciseCompletion deleteExerciseCompletion : exerciseCompletionList) {
                                        exerciseCompletionRepository.delete(deleteExerciseCompletion);
                                    }
                                }
                                if (noOfTimesPlayed == (circuitSchedule.getRepeat())) {
                                    circuitCompletion.setCompleted(true);
                                }
                                isAllExInCircuitCompleted = true;
                                circuitAuditAction = DBConstants.REPEAT;
                            }
                            //Circuit completion audit
                            if (isAllExInCircuitCompleted) {
                                CircuitCompletionAudit circuitCompletionAudit = new CircuitCompletionAudit();
                                circuitCompletionAudit.setMember(user);
                                circuitCompletionAudit.setProgram(program);
                                circuitCompletionAudit.setWorkoutScheduleId(workoutSchedule.getWorkoutScheduleId());
                                circuitCompletionAudit.setCircuitScheduleId(circuitSchedule.getCircuitScheduleId());
                                circuitCompletionAudit.setAction(circuitAuditAction);
                                circuitCompletionAuditRepository.save(circuitCompletionAudit);
                            }
                            //Auto populating rest circuits
                            CircuitSchedule nextCircuitSchedule = circuitScheduleRepository.findByOrderAndWorkoutWorkoutId(circuitSchedule.getOrder() + 1, workoutSchedule.getWorkout().getWorkoutId());
                            if (nextCircuitSchedule != null && nextCircuitSchedule.isRestCircuit()) {
                                postRestCircuitCompletedStatus(programId, workoutScheduleId, nextCircuitSchedule.getCircuitScheduleId());
                            }
                        }
                    }
                    //Check whether the workout is completed or not
                    int noOfCircuitsCompletedInWorkout = 0;
                    List<CircuitCompletion> circuitCompletions = circuitCompletionRepository.findByMemberUserIdAndProgramProgramIdAndWorkoutScheduleId(user.getUserId(), programId, workoutScheduleId);
                    for (CircuitCompletion circuitCompletion : circuitCompletions) {
                        if (circuitCompletion.isCompleted()) {
                            noOfCircuitsCompletedInWorkout += 1;
                        }
                    }
                    if (circuitScheduleIdList.size() == noOfCircuitsCompletedInWorkout) {
                        WorkoutCompletion workoutCompletion = workoutCompletionRepository.findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdOrderByCompletedDateDesc(user.getUserId(), programId, workoutScheduleId);
                        if (workoutCompletionDate != null && workoutCompletion == null) {
                            workoutCompletion = new WorkoutCompletion();
                            workoutCompletion.setMember(user);
                            workoutCompletion.setProgram(program);
                            workoutCompletion.setWorkoutScheduleId(workoutSchedule.getWorkoutScheduleId());
                            workoutCompletion.setCompletedDate(workoutCompletionDate);
                            workoutCompletionRepository.save(workoutCompletion);
                            //Workout completion audit
                            WorkoutCompletionAudit workoutCompletionAudit = new WorkoutCompletionAudit();
                            workoutCompletionAudit.setMember(user);
                            workoutCompletionAudit.setProgram(program);
                            workoutCompletionAudit.setWorkoutScheduleId(workoutSchedule.getWorkoutScheduleId());
                            workoutCompletionAudit.setAction(DBConstants.COMPLETED);
                            workoutCompletionAuditRepository.save(workoutCompletionAudit);
                            if (workoutSchedule.getWorkout() != null) {
                                List<Challenge> challenges = challengeRepository.findByUserUserId(user.getUserId());
                                for (Challenge challenge : challenges) {
                                    if (workoutCompletion.getCompletedDate().after(challenge.getChallengeStartedDay()) && workoutCompletion.getCompletedDate().before(challenge.getChallengeEndDate())) {
                                        int completedWorkouts = challenge.getCompletedWorkouts() + 1;
                                        int remainingWorkouts = challenge.getRemainingWorkouts() - 1;
                                        double percentage;
                                        if (completedWorkouts <= challenge.getChallengeWorkouts()) {
                                            challenge.setCompletedWorkouts(completedWorkouts);
                                            challenge.setRemainingWorkouts(remainingWorkouts);
                                            percentage = (completedWorkouts * 100.00) / challenge.getChallengeWorkouts();
                                            challenge.setPercentage(percentage);
                                        }
                                        challengeRepository.save(challenge);
                                    }
                                }
                            }
                        }
                    }
                    //Updating the workout discard feedback
                    if (!videoCacheWorkoutModel1.getDiscardReasons().isEmpty()) {
                        WorkoutDiscardFeedback workoutDiscardFeedback = new WorkoutDiscardFeedback();
                        List<WorkoutDiscardFeedbackMapping> discardFeedbackMappingList = new ArrayList<>();
                        for (DiscardReasonModel discardReasonModel : videoCacheWorkoutModel1.getDiscardReasons()) {
                            if (discardReasonModel.getDiscardFeedbackId() == null || discardReasonModel.getDiscardFeedbackId() == 0) {
                                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_DISCARD_FEEDBACK_ID_NULL, MessageConstants.ERROR);
                            }
                            DiscardWorkoutReasons discardReason = discardWorkoutReasonsRepository.findByDiscardFeedbackId(discardReasonModel.getDiscardFeedbackId());
                            if (discardReason == null) {
                                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_DISCARD_FEEDBACK_NOT_FOUND, MessageConstants.ERROR);
                            }
                            boolean isOtherReason = false;
                            if (DBConstants.OTHERS.equals(discardReason.getDiscardReason())) {
                                isOtherReason = true;
                            }
                            if (isOtherReason && (discardReasonModel.getCustomReason() == null || discardReasonModel.getCustomReason().isEmpty())) {
                                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_DISCARD_CUSTOM_FEEDBACK_NOT_FOUND, MessageConstants.ERROR);
                            }
                            WorkoutDiscardFeedbackMapping discardFeedbackMapping = new WorkoutDiscardFeedbackMapping();
                            discardFeedbackMapping.setDiscardWorkoutReason(discardReason);
                            discardFeedbackMapping.setWorkoutDiscardFeedback(workoutDiscardFeedback);
                            if (isOtherReason) {
                                discardFeedbackMapping.setCustomReason(discardReasonModel.getCustomReason());
                            }
                            discardFeedbackMappingList.add(discardFeedbackMapping);
                        }
                        workoutDiscardFeedback.setProgram(program);
                        workoutDiscardFeedback.setWorkoutSchedule(workoutSchedule);
                        workoutDiscardFeedback.setUser(user);
                        workoutDiscardFeedback.setWorkoutDiscardFeedbackMapping(discardFeedbackMappingList);
                        workoutDiscardFeedbackRepository.save(workoutDiscardFeedback);
                    }
                    //Post workout feedback
                    if (videoCacheWorkoutModel1.getWorkoutFeedbackTypeId() != null && videoCacheWorkoutModel1.getWorkoutFeedbackTypeId() != 0) {
                        FeedbackTypes feedbackType = feedbackTypesRepository.findByfeedbackTypeId(videoCacheWorkoutModel1.getWorkoutFeedbackTypeId());
                        if (feedbackType == null) {
                            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FEEDBACK_TYPE_NOT_FOUND, MessageConstants.ERROR);
                        }
                        WorkoutFeedback workoutFeedback = new WorkoutFeedback();
                        workoutFeedback.setProgram(program);
                        workoutFeedback.setWorkoutSchedule(workoutSchedule);
                        workoutFeedback.setFeedbackType(feedbackType);
                        workoutFeedback.setUser(user);
                        workoutFeedbackRepository.save(workoutFeedback);
                    }
                }
            }
            log.info("Update workout, circuit and exercise completion : Time taken in millis : " + (new Date().getTime() - profilingStart));
            profilingStart = new Date().getTime();
            //Program rating submission
            if (videoCacheProgramModel.getProgramRating() != null && videoCacheProgramModel.getProgramRating() >= 0 && videoCacheProgramModel.getProgramRating() <= 5) {
                Float programRating = videoCacheProgramModel.getProgramRating();
                int completedWorkouts = workoutCompletionRepository.countByMemberUserIdAndProgramProgramId(user.getUserId(), programId);
                boolean isProgramCompleted = false;
                if (completedWorkouts == program.getDuration().getDuration().intValue()) {
                    isProgramCompleted = true;
                }
                if (isProgramCompleted) {
                    ProgramRating programRatingFromRepo = programRatingRepository.findByProgramAndUser(program, user);
                    boolean isProgramRatingAllowed = true;
                    if (programRatingFromRepo != null) {
                        if (!programRatingFromRepo.getIsSubmissionAllowed()) {
                            isProgramRatingAllowed = false;
                        }
                    } else {
                        programRatingFromRepo = new ProgramRating();
                        programRatingFromRepo.setProgram(program);
                        programRatingFromRepo.setUser(user);
                        isProgramRatingAllowed = true;
                    }
                    if (isProgramRatingAllowed) {
                        programRatingFromRepo.setProgramRating(programRating);
                        programRatingFromRepo.setModifiedDate(new Date());
                        programRatingFromRepo.setIsSubmissionAllowed(true);
                        programRatingRepository.save(programRatingFromRepo);
                    }
                }
            }
            log.info("Program rating submission : Time taken in millis : " + (new Date().getTime() - profilingStart));
            profilingStart = new Date().getTime();
            //Program promo completion
            if (videoCacheProgramModel.isPromoCompletionStatus()) {
                ProgramPromoViews programPromoViews = new ProgramPromoViews();
                programPromoViews.setProgram(program);
                programPromoViews.setUser(user);
                if (videoCacheProgramModel.getPromoCompletionDateInMillis() == null || videoCacheProgramModel.getPromoCompletionDateInMillis() == 0) {
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROMOTION_COMPLETED_DATE_MISSING, null);
                }
                programPromoViews.setDate(new Date(videoCacheProgramModel.getPromoCompletionDateInMillis()));
                programPromoViews.setStatus(KeyConstants.KEY_WATCHED);
                programPromoViewsRepository.save(programPromoViews);
            }
            log.info("Program promo completion : Time taken in millis : " + (new Date().getTime() - profilingStart));
        }
        log.info("Video cache sync : Total Time taken in millis : "+(new Date().getTime() - start));
        log.info("Video cache sync ends");
        return "Video cache updated Successfully.";
    }

    /**
     * Method to check and auto restart program
     *
     * @param timeZone
     * @param member
     * @param program
     */
    public void checkAndResetProgramCompletion(String timeZone, User member, Programs program) {
        if (timeZone != null) {
            Date now = new Date();
            Long programId = program.getProgramId();
            List<WorkoutCompletion> workoutCompletionList = workoutCompletionRepository.findByMemberUserIdAndProgramProgramId(member.getUserId(), program.getProgramId());
            int completedWorkouts = workoutCompletionList.size();
            int totalDays = program.getDuration().getDuration().intValue();
            if (completedWorkouts == totalDays) {
                WorkoutCompletion lastcompletedWorkout = workoutCompletionList.get(workoutCompletionList.size() - 1);
                Date programCompletionDate = lastcompletedWorkout.getCompletedDate();
                try {
                    if (programCompletionDate.before(now) && !fitwiseUtils.isSameDayInTimeZone(programCompletionDate, now, timeZone)) {
                        log.info("Restarting program with program id : " + programId + " for user id : " + member.getUserId());
                        resetProgramCompletion(programId, member, false);
                    }
                } catch (Exception e) {
                    log.error("Exception occurred while restarting program : " + e.getMessage());
                }
            }
        }
    }

    /**
     * Validate the given programid is free of access
     * @param programId
     * @return
     */
    private boolean isFreeProgram(Long programId){
        boolean isFreeProgram = false;
        List<FreeAccessProgram> freeProductList = freeAccessService.getAllUserFreeProgramsList();
        List<FreeAccessProgram> freeProductForSpecificUser = freeAccessService.getUserSpecificFreeAccessPrograms();
        if(!freeProductForSpecificUser.isEmpty()) {
        	freeProductList.addAll(freeProductForSpecificUser);
        }
        for(FreeAccessProgram freeProduct : freeProductList){
            if(freeProduct.getProgram().getProgramId().equals(programId) ){
                isFreeProgram = true;
                break;
            }
        }
        return isFreeProgram;
    }
}