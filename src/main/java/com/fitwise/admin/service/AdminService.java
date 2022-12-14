package com.fitwise.admin.service;

import com.fitwise.admin.model.DeviceWisePercentageModel;
import com.fitwise.admin.model.TaxPercentageModel;
import com.fitwise.block.service.AdminBlockedService;
import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.EmailConstants;
import com.fitwise.constants.InstructorConstant;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.encryption.AESEncryption;
import com.fitwise.entity.BlockedPrograms;
import com.fitwise.entity.CircuitSchedule;
import com.fitwise.entity.DeviceWiseTaxPercentage;
import com.fitwise.entity.ExerciseSchedulers;
import com.fitwise.entity.Exercises;
import com.fitwise.entity.FlaggedExercise;
import com.fitwise.entity.FlaggedExercisesSummary;
import com.fitwise.entity.FlaggedVideoReason;
import com.fitwise.entity.InstructorAwards;
import com.fitwise.entity.InstructorCertification;
import com.fitwise.entity.InstructorPayment;
import com.fitwise.entity.InstructorProgramExperience;
import com.fitwise.entity.OtherExpertise;
import com.fitwise.entity.PlatformType;
import com.fitwise.entity.ProgramExpertiseGoalsMapping;
import com.fitwise.entity.ProgramExpertiseMapping;
import com.fitwise.entity.ProgramGoals;
import com.fitwise.entity.ProgramPriceTaxPercentage;
import com.fitwise.entity.ProgramSubscriptionPaymentHistory;
import com.fitwise.entity.ProgramTypes;
import com.fitwise.entity.Programs;
import com.fitwise.entity.TaxId;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.UserProgramGoalsMapping;
import com.fitwise.entity.UserRole;
import com.fitwise.entity.UserRoleMapping;
import com.fitwise.entity.VideoManagement;
import com.fitwise.entity.WorkoutCompletion;
import com.fitwise.entity.WorkoutMapping;
import com.fitwise.entity.WorkoutSchedule;
import com.fitwise.entity.YearsOfExpertise;
import com.fitwise.entity.packaging.PackageProgramMapping;
import com.fitwise.entity.payments.authNet.AuthNetArbSubscription;
import com.fitwise.entity.payments.authNet.AuthNetPayment;
import com.fitwise.entity.payments.authNet.AuthNetSubscriptionStatus;
import com.fitwise.entity.payments.authNet.Countries;
import com.fitwise.entity.payments.authNet.TestMemberSubscription;
import com.fitwise.entity.payments.common.InvoiceManagement;
import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.subscription.ProgramSubscription;
import com.fitwise.entity.subscription.SubscriptionAudit;
import com.fitwise.entity.subscription.SubscriptionPlan;
import com.fitwise.entity.subscription.SubscriptionStatus;
import com.fitwise.entity.subscription.SubscriptionType;
import com.fitwise.exception.ApplicationException;
import com.fitwise.program.daoImpl.ProgramsRepoImpl;
import com.fitwise.program.model.ProgramDetailsResponseModel;
import com.fitwise.program.model.ProgramResponseModel;
import com.fitwise.program.service.ProgramService;
import com.fitwise.repository.AuthNetSubscriptionStatusRepository;
import com.fitwise.repository.AwardsRepository;
import com.fitwise.repository.BlockedProgramsRepository;
import com.fitwise.repository.BlockedUserRepository;
import com.fitwise.repository.CertificateRepository;
import com.fitwise.repository.ExerciseRepository;
import com.fitwise.repository.ExerciseScheduleRepository;
import com.fitwise.repository.FlaggedExerciseRepository;
import com.fitwise.repository.FlaggedExercisesSummaryRepository;
import com.fitwise.repository.FlaggedVideoReasonsRepository;
import com.fitwise.repository.GoalsRepository;
import com.fitwise.repository.InstructorExperienceRepository;
import com.fitwise.repository.OtherExpertiseRepository;
import com.fitwise.repository.ProgramExpertiseGoalsMappingRepository;
import com.fitwise.repository.ProgramExpertiseMappingRepository;
import com.fitwise.repository.ProgramPriceTaxPercentageRepo;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.ProgramTypeRepository;
import com.fitwise.repository.SubscriptionPaymentHistoryRepository;
import com.fitwise.repository.TaxRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.UserProgramGoalsMappingRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.repository.UserRoleMappingRepository;
import com.fitwise.repository.UserRoleRepository;
import com.fitwise.repository.WorkoutMappingRepository;
import com.fitwise.repository.WorkoutRepository;
import com.fitwise.repository.WorkoutScheduleRepository;
import com.fitwise.repository.circuit.CircuitScheduleRepository;
import com.fitwise.repository.member.WorkoutCompletionRepository;
import com.fitwise.repository.order.InstructorPaymentRepository;
import com.fitwise.repository.order.InvoiceManagementRepository;
import com.fitwise.repository.order.OrderManagementRepository;
import com.fitwise.repository.packaging.PackageProgramMappingRepository;
import com.fitwise.repository.payments.TestMemberSubscriptionRepository;
import com.fitwise.repository.payments.authnet.AuthNetArbSubscriptionRepository;
import com.fitwise.repository.payments.authnet.AuthNetPaymentRepository;
import com.fitwise.repository.payments.authnet.CountriesRepository;
import com.fitwise.repository.subscription.ProgramSubscriptionRepo;
import com.fitwise.repository.subscription.SubscriptionAuditRepo;
import com.fitwise.repository.subscription.SubscriptionPlansRepo;
import com.fitwise.repository.subscription.SubscriptionStatusRepo;
import com.fitwise.repository.subscription.SubscriptionTypesRepo;
import com.fitwise.response.AdminListResponse;
import com.fitwise.response.ProgramAnalytics;
import com.fitwise.response.SpecializationResponse;
import com.fitwise.response.UserNameProfileImgView;
import com.fitwise.response.flaggedvideo.FlaggedReasonDetailsView;
import com.fitwise.response.flaggedvideo.FlaggedVideoAffectedProgram;
import com.fitwise.response.flaggedvideo.FlaggedVideoDetailsView;
import com.fitwise.response.flaggedvideo.FlaggedVideosResponse;
import com.fitwise.response.flaggedvideo.ReasonCountView;
import com.fitwise.service.SubscriptionService;
import com.fitwise.service.instructor.InstructorAnalyticsService;
import com.fitwise.service.v2.instructor.UserLinkService;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.specifications.UserProfileSpecifications;
import com.fitwise.specifications.jpa.SubscriptionAuditJPA;
import com.fitwise.specifications.jpa.dao.InstructorSubscriptionCount;
import com.fitwise.utils.Convertions;
import com.fitwise.utils.EmailContentUtil;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.RequestParamValidator;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.utils.mail.AsyncMailer;
import com.fitwise.view.CalendarView;
import com.fitwise.view.ExperienceView;
import com.fitwise.view.MemberProgramTileView;
import com.fitwise.view.MonthlyEnrollmentResponseView;
import com.fitwise.view.MonthlyEnrollmentView;
import com.fitwise.view.OtherExpertiseResponseView;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.SubscriptionPackagePackageIdAndTitleView;
import com.fitwise.view.TaxView;
import com.fitwise.view.WorkoutCompletionView;
import com.fitwise.view.YearlyUsersEnrollmentView;
import com.fitwise.view.YearsEnrollmentResponseView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
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
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {

    @Autowired
    private ProgramPriceTaxPercentageRepo programRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserProgramGoalsMappingRepository userProgramGoalsMappingRepository;

    @Autowired
    private ProgramExpertiseGoalsMappingRepository programExpertiseGoalsMappingRepository;

    @Autowired
    private GoalsRepository goalsRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private ProgramExpertiseMappingRepository programExpertiseMappingRepository;

    @Autowired
    private UserRoleMappingRepository userRoleMappingRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private TaxRepository taxRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private AwardsRepository awardsRepository;

    @Autowired
    private InstructorExperienceRepository instructorExperienceRepository;

    @Autowired
    private ProgramTypeRepository programTypeRepository;

    @Autowired
    private ProgramSubscriptionRepo programSubscriptionRepo;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private ProgramService programService;

    @Autowired
    private ProgramsRepoImpl programImpl;

    @Autowired
    private WorkoutScheduleRepository workoutScheduleRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    FlaggedExercisesSummaryRepository flaggedExercisesSummaryRepository;

    @Autowired
    private FlaggedExerciseRepository flaggedExerciseRepository;
    @Autowired
    FlaggedVideoReasonsRepository flaggedVideoReasonsRepository;

    @Autowired
    private ExerciseScheduleRepository exerciseScheduleRepository;

    @Autowired
    private WorkoutMappingRepository workoutMappingRepository;

    @Autowired
    BlockedUserRepository blockedUserRepository;

    @Autowired
    private OtherExpertiseRepository otherExpertiseRepository;

    @Autowired
    private UserComponents userComponents;

    @Autowired
    CircuitScheduleRepository circuitScheduleRepository;

    @Autowired
    WorkoutCompletionRepository workoutCompletionRepository;

    @Autowired
    FitwiseUtils fitwiseUtils;

    @Autowired
    private AdminBlockedService adminBlockedService;

    @Autowired
    private OrderManagementRepository orderManagementRepository;

    @Autowired
    private AuthNetArbSubscriptionRepository authNetArbSubscriptionRepository;

    @Autowired
    private AuthNetPaymentRepository authNetPaymentRepository;

    @Autowired
    private AsyncMailer asyncMailer;

    @Autowired
    private BlockedProgramsRepository blockedProgramsRepository;

    @Autowired
    private SubscriptionAuditRepo subscriptionAuditRepo;
    @Autowired
    private EmailContentUtil emailContentUtil;
    @Autowired
    private InvoiceManagementRepository invoiceManagementRepository;
    @Autowired
    private SubscriptionPlansRepo subscriptionPlansRepo;
    @Autowired
    private SubscriptionStatusRepo subscriptionStatusRepo;
    @Autowired
    private SubscriptionPaymentHistoryRepository subscriptionPaymentHistoryRepository;
    @Autowired
    private SubscriptionTypesRepo subscriptionTypesRepo;
    @Autowired
    private AuthNetSubscriptionStatusRepository authNetSubscriptionStatusRepository;
    @Autowired
    private TestMemberSubscriptionRepository testMemberSubscriptionRepository;

    @Autowired
    private InstructorAnalyticsService instructorAnalyticsService;

    @Autowired
    private InstructorPaymentRepository instructorPaymentRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;
    @Autowired
    private PackageProgramMappingRepository packageProgramMappingRepository;
    @Autowired
    private CountriesRepository countriesRepository;

    @Autowired
    private AESEncryption aesEncryption;

    @Autowired
    private SubscriptionAuditJPA subscriptionAuditJPA;

    private final UserLinkService userLinkService;
    private final ProgramPriceTaxPercentageRepo programPriceTaxPercentageRepo;

    public ResponseModel storeTaxPercentageDetails(final TaxPercentageModel taxModel) {
        ValidationUtils.throwException(taxModel == null, "Tax percentage model cant be null", Constants.BAD_REQUEST);
        ValidationUtils.throwException(taxModel.getDeviceWisePercentageList().isEmpty(), "Device Wise percentage list cant be empty", Constants.BAD_REQUEST);
        List<DeviceWisePercentageModel> deviceWisePercentageList = taxModel.getDeviceWisePercentageList();
        ProgramPriceTaxPercentage pricePercentage = null;
        if (taxModel.getPricePercentageId() == null || taxModel.getPricePercentageId() == 0) {
            pricePercentage = new ProgramPriceTaxPercentage();
        }else{
            pricePercentage = programPriceTaxPercentageRepo.findByPricePercentageId(taxModel.getPricePercentageId());
        }
        List<DeviceWiseTaxPercentage> taxWisePercentageList = new ArrayList<>();
        for (DeviceWisePercentageModel model : deviceWisePercentageList) {
            doValidateModel(model);
            DeviceWiseTaxPercentage percentageData = new DeviceWiseTaxPercentage();
            percentageData.setAppStoreChargePercentage(model.getAppStoreChargePercentage());
            percentageData.setDeviceType(model.getDeviceType());
            percentageData.setTrainnrPlatformPercentage(model.getTrainnrPlatformPercentage());
            percentageData.setTaxPercentage(model.getTaxPercentage());
            taxWisePercentageList.add(percentageData);
        }
        pricePercentage.setDeviceWiseTaxPercentage(taxWisePercentageList);
        pricePercentage = programRepo.save(pricePercentage);
        taxModel.setPricePercentageId(pricePercentage.getPricePercentageId());
        ResponseModel responseModel = new ResponseModel();
        responseModel.setPayload(taxModel);
        responseModel.setMessage("Successfully Stored Tax percentage ");
        responseModel.setStatus(Constants.CREATED_STATUS);
        return responseModel;
    }

    private void doValidateModel(DeviceWisePercentageModel model) {
        ValidationUtils.throwException(model.getDeviceType() == null || model.getDeviceType().isEmpty(), "Device type can't be null", Constants.BAD_REQUEST);
        ValidationUtils.throwException(model.getAppStoreChargePercentage() == null || model.getAppStoreChargePercentage() == 0, "App Store percent can't be null", Constants.BAD_REQUEST);
        ValidationUtils.throwException(model.getTaxPercentage() == null || model.getTaxPercentage() == 0, "Tax percent can't be null", Constants.BAD_REQUEST);
        ValidationUtils.throwException(model.getTrainnrPlatformPercentage() == null || model.getTrainnrPlatformPercentage() == 0, "Trainnr percent can't be null", Constants.BAD_REQUEST);
    }

    /**
     * To retrieve memberDetails in admin web app
     *
     * @param memberId
     * @return
     * @throws ApplicationException
     * @throws ParseException
     */
    public ResponseModel getMemberDetails(Long memberId) {
        User user = userRepository.findByUserId(memberId);
        if (user == null) {
            throw new ApplicationException(Constants.NOT_FOUND, ValidationMessageConstants.MSG_USER_NOT_FOUND, MessageConstants.ERROR);
        }

        Map<String, Object> memberDetails = new HashMap<>();
        List<UserProgramGoalsMapping> userProgramGoalsMappingList = userProgramGoalsMappingRepository.findByUserUserId(memberId);

        UserProfile userProfile = userProfileRepository.findByUser(user);
        UserRole userRole = userRoleRepository.findByName(KeyConstants.KEY_MEMBER);
        boolean isActive = fitwiseUtils.isUserActive(userProfile.getUser(), userRole);
        memberDetails.put(KeyConstants.KEY_USER_STATUS, isActive);
        Date lastAccessDate = fitwiseUtils.getLastActiveDate(userProfile.getUser(), userRole);
        String lastAccessDateFormatted = fitwiseUtils.formatDate(lastAccessDate);
        memberDetails.put(KeyConstants.KEY_LAST_ACTIVE_DATE, lastAccessDate);
        memberDetails.put(KeyConstants.KEY_LAST_ACTIVE_DATE_FORMATTED, lastAccessDateFormatted);


        memberDetails.put(KeyConstants.KEY_USER_FIRST_NAME, userProfile.getFirstName());
        memberDetails.put(KeyConstants.KEY_USER_LAST_NAME, userProfile.getLastName());
        memberDetails.put(KeyConstants.KEY_USER_BIO, userProfile.getBiography());
        memberDetails.put(KeyConstants.KEY_CONTACT_NUMBER, userProfile.getContactNumber());
        memberDetails.put(KeyConstants.KEY_PROFILE_IMAGE, userProfile.getProfileImage());
        memberDetails.put(KeyConstants.KEY_ISD_CODE, userProfile.getIsdCode());
        if (userProfile.getCountryCode() != null) {
            memberDetails.put(KeyConstants.KEY_COUNTRY_CODE, userProfile.getCountryCode());

            Countries country = countriesRepository.findByCountryCode(userProfile.getCountryCode());
            if (country != null)
                memberDetails.put(KeyConstants.KEY_COUNTRY_NAME, country.getCountryName());
        }
        memberDetails.put(KeyConstants.KEY_EMAIL, user.getEmail());
        List<ProgramGoals> programGoalsList = new ArrayList<>();
        List<ProgramExpertiseMapping> programExpertiseMappings = new ArrayList<>();

            /*
            Getting Selected program types,expertise levels and goals
             */
        for (UserProgramGoalsMapping userProgramGoalsMapping : userProgramGoalsMappingList) {
            ProgramExpertiseGoalsMapping programExpertiseGoalsMapping = programExpertiseGoalsMappingRepository.findByProgramExpertiseGoalsMappingId(userProgramGoalsMapping.getProgramExpertiseGoalsMapping().getProgramExpertiseGoalsMappingId());
            ProgramGoals programGoals = goalsRepository.findByProgramGoalId(programExpertiseGoalsMapping.getProgramGoals().getProgramGoalId());
            programGoalsList.add(programGoals);
            ProgramExpertiseMapping programExpertiseMapping = programExpertiseMappingRepository.findByProgramExpertiseMappingId(programExpertiseGoalsMapping.getProgramExpertiseMapping().getProgramExpertiseMappingId());
            programExpertiseMappings.add(programExpertiseMapping);

        }

        //Goals of client
        List<ProgramGoals> goals = userProgramGoalsMappingList.stream()
                .map(userProgramGoalsMapping -> userProgramGoalsMapping.getProgramExpertiseGoalsMapping().getProgramGoals())
                .distinct()
                .collect(Collectors.toList());


        List<ProgramExpertiseMapping> programExpertiseMappingList = programExpertiseMappings.stream()
                .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparingLong(ProgramExpertiseMapping::getProgramExpertiseMappingId))),
                        ArrayList::new));

        memberDetails.put(KeyConstants.KEY_SELECTED_USER_GOALS, goals);
        memberDetails.put(KeyConstants.KEY_SELECTED_PROGRAM_EXPERTISE_LEVELS, programExpertiseMappingList);

        boolean isUserBlocked = blockedUserRepository.existsByUserUserIdAndUserRoleName(memberId, KeyConstants.KEY_MEMBER);
        memberDetails.put(KeyConstants.KEY_BLOCKED, isUserBlocked);

        ResponseModel responseModel = new ResponseModel();
        responseModel.setPayload(memberDetails);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_USER_DETAILS_FETCHED);

        return responseModel;
    }


    /**
     * Check if particular workout completed or not
     *
     * @param memberId
     * @param programId
     * @param workoutScheduleId
     * @return
     */
    public WorkoutCompletionView checkIfWorkoutCompleted(Long memberId, Long programId, Long workoutScheduleId) {

        //Validation for params
        if (memberId == null || memberId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_ID_NULL, MessageConstants.ERROR);
        }
        User member = userRepository.findByUserId(memberId);
        if(member == null){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_FOUND, MessageConstants.ERROR);
        }
        if (!fitwiseUtils.isMember(member)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_MEMBER, MessageConstants.ERROR);
        }

        if (programId == null || programId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_NULL, MessageConstants.ERROR);
        }
        Programs program = programRepository.findByProgramId(programId);
        if (program == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, MessageConstants.ERROR);
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

        WorkoutCompletionView workoutCompletionView = new WorkoutCompletionView();

        WorkoutCompletion workoutCompletion = workoutCompletionRepository.findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdOrderByCompletedDateDesc(memberId, programId, workoutScheduleId);
        if (workoutCompletion != null) {
            workoutCompletionView.setCompletedDate(fitwiseUtils.formatDate(workoutCompletion.getCompletedDate()));
            workoutCompletionView.setCompletedDateTimeStamp(workoutCompletion.getCompletedDate());
            workoutCompletionView.setWorkoutCompleted(true);
        }

        return workoutCompletionView;
    }


    /**
     * Method returns the number of instructors enrolled for an year
     *
     * @param role - User role for which enrollment count needs to be taken
     * @param date - Used to get the year for which enrollment count needs to be taken
     * @return
     * @throws ApplicationException
     */
    public ResponseModel getUsersEnrollmentCountForAYear(String role, Date date) throws ParseException {
        log.info("getUsersEnrollmentCountForAYear starts.");
        long apiStartTimeMillis = System.currentTimeMillis();

        long profilingStartTimeMillis = System.currentTimeMillis();
        CalendarView mCalendar = validationService.validateAndGetDateMonthYearFromDate(date);
        int userRequestedYear = mCalendar.getYear();

        UserRole userRole = validationService.validateUserRole(role);
        List<UserRoleMapping> filteredUsersBasedOnRole = userRoleMappingRepository.findByUserRoleRoleId(userRole.getRoleId());

        long profilingEndTimeMillis = System.currentTimeMillis();
        log.info(" Validation and Query : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        /*
         * Iterating through the users list which was obtained based on role
         * and filtering it based on the user's enrolled year
         */
        profilingStartTimeMillis = System.currentTimeMillis();
        List<UserRoleMapping> usersMappingEnrolledForTheYear = new ArrayList<>();
        for (UserRoleMapping userRoleMapping : filteredUsersBasedOnRole) {
            if (userRoleMapping.getCreatedDate() != null) {
                /*
                 * Parsing and getting requested year from given date
                 */
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(userRoleMapping.getCreatedDate());
                int createdYear = calendar.get(Calendar.YEAR);
                if (createdYear == userRequestedYear && userRoleMapping.getUserRole().getName().equalsIgnoreCase(userRole.getName())) {
                    usersMappingEnrolledForTheYear.add(userRoleMapping);
                }
            }
        }
        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Filtering UserRoleMapping based on year : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        /*
         * Setting the months and respective id's to object and adding it to list
         */
        profilingStartTimeMillis = System.currentTimeMillis();
        List<YearsEnrollmentResponseView> yearsEnrollmentResponseViews = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : ValidationUtils.monthsMap.entrySet()) {
            YearsEnrollmentResponseView yearsEnrollmentResponseView = new YearsEnrollmentResponseView();
            Integer monthNumber = entry.getKey();
            String monthName = entry.getValue();

            yearsEnrollmentResponseView.setMonthId(monthNumber);
            yearsEnrollmentResponseView.setEnrollmentCount(0);
            yearsEnrollmentResponseView.setMonthName(monthName);
            yearsEnrollmentResponseViews.add(yearsEnrollmentResponseView);
        }
        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Month segregation : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        /*
         * Iterating through the list and increasing enrollment count
         * if month matches. ie., If Jan already has enrollment count 2,
         * it will be increased as 3.
         */
        profilingStartTimeMillis = System.currentTimeMillis();
        for (YearsEnrollmentResponseView responseView : yearsEnrollmentResponseViews) {
            for (UserRoleMapping userEnrolled : usersMappingEnrolledForTheYear) {
                if (userEnrolled.getCreatedDate() != null) {

                    /*
                     * Parsing and getting requested month from given date
                     */
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(userEnrolled.getCreatedDate());
                    int createdMonth = calendar.get(Calendar.MONTH);

                    if (createdMonth == responseView.getMonthId()) {
                        int enrollmentCount = responseView.getEnrollmentCount();
                        enrollmentCount++;
                        responseView.setEnrollmentCount(enrollmentCount);
                    }
                }
            }
        }
        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Data construction for each month : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        YearlyUsersEnrollmentView yearlyUsersEnrollmentView = new YearlyUsersEnrollmentView();
        yearlyUsersEnrollmentView.setTotalEnrollmentCount(usersMappingEnrolledForTheYear.size()); // Setting the total users enrollment count
        yearlyUsersEnrollmentView.setEnrollmentPerMonth(yearsEnrollmentResponseViews);

        // Constructing response object
        ResponseModel res = new ResponseModel();
        res.setStatus(Constants.SUCCESS_STATUS);
        res.setMessage(MessageConstants.MSG_USERS_FETCHED);
        res.setPayload(yearlyUsersEnrollmentView);
        long apiEndTimeMillis = System.currentTimeMillis();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("getUsersEnrollmentCountForAYear ends.");
        return res;
    }


    /**
     * Method returns the count of instructors enrolled for a month
     *
     * @return
     */
    public ResponseModel getUsersEnrollmentCountForAMonth(String role, Date date) throws ParseException {
        log.info("getUsersEnrollmentCountForAMonth starts.");
        long apiStartTimeMillis = System.currentTimeMillis();
        /*
         * Validating date from user
         */
        long profilingStartTimeMillis = System.currentTimeMillis();
        CalendarView mCalendar = validationService.validateAndGetDateMonthYearFromDate(date);
        int userRequestedYear = mCalendar.getYear();
        int userRequestedMonth = mCalendar.getMonth();
        UserRole userRole = validationService.validateUserRole(role);
        List<UserRoleMapping> filteredUsersBasedOnRole = userRoleMappingRepository.findByUserRoleRoleId(userRole.getRoleId());
        long profilingEndTimeMillis = System.currentTimeMillis();
        log.info(" Validation and Query : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        /*
         * Iterating through the users list which was obtained based on role
         * and filtering it based on the user's enrolled year
         */
        profilingStartTimeMillis = System.currentTimeMillis();
        List<UserRoleMapping> usersMappingEnrolledForTheYear = new ArrayList<>();
        for (UserRoleMapping userRoleMapping : filteredUsersBasedOnRole) {
            if (userRoleMapping.getCreatedDate() != null) {

                /*
                 * Parsing and getting requested year from given date
                 */
                CalendarView calendarView = validationService.validateAndGetDateMonthYearFromDate(userRoleMapping.getCreatedDate());
                int createdYear = calendarView.getYear();
                int createdMonth = calendarView.getMonth();

                if (createdYear == userRequestedYear && userRequestedMonth == createdMonth) {
                    usersMappingEnrolledForTheYear.add(userRoleMapping);
                }
            }
        }
        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Filtering UserRoleMapping based on month : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        /*
         * Iterating through the months list and constructing the object only if the month
         * from the list is equal to user requested month
         */
        profilingStartTimeMillis = System.currentTimeMillis();
        List<MonthlyEnrollmentView> monthlyEnrollmentViews = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : ValidationUtils.weeksMap.entrySet()) {
            MonthlyEnrollmentView monthlyEnrollmentView = new MonthlyEnrollmentView();
            Integer weekNumber = entry.getKey();
            String weekName = entry.getValue();

            monthlyEnrollmentView.setWeekId(weekNumber);
            monthlyEnrollmentView.setEnrollmentCount(0);
            monthlyEnrollmentView.setWeekName(weekName);
            monthlyEnrollmentViews.add(monthlyEnrollmentView);
        }
        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Week segregation : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        /*
         * Getting the number of days in the month using which sectioning of weeks are done
         * 30 days ---> 7days 7days 8days 8days
         * 31 days ---> 7days 8days 8days 8days
         * 28 days ---> 7days 7days 7days 7days
         * 29 days ---> 7days 7days 7days 8days
         */
        profilingStartTimeMillis = System.currentTimeMillis();
        int noOfDaysInTheMonth = ValidationUtils.getNumberOfDaysInTheMonth(userRequestedYear, userRequestedMonth);

        String[] weekSplit = null;

        /*
         * Sectioning weeks array based on number of days in month
         */
        if (noOfDaysInTheMonth == 28) {
            weekSplit = ValidationUtils.monthSplitWithTotal28Days;
        } else if (noOfDaysInTheMonth == 29) {
            weekSplit = ValidationUtils.monthSplitWithTotal29Days;
        } else if (noOfDaysInTheMonth == 30) {
            weekSplit = ValidationUtils.monthSplitWithTotal30Days;
        } else if (noOfDaysInTheMonth == 31) {
            weekSplit = ValidationUtils.monthSplitWithTotal31Days;
        }

        int firstWeekStartDate = 0;
        int firstWeekEndDate = 0;
        int secondWeekStartDate = 0;
        int secondWeekEndDate = 0;
        int thirdWeekStartDate = 0;
        int thirdWeekEndDate = 0;
        int fourthWeekStartDate = 0;
        int fourthWeekEndDate = 0;

        /*
         * Setting the date limits to the weeks by parsing through the weekSplit array
         */
        for (int i = 0; i < weekSplit.length; i++) {
            if (i == 0) {
                String dayLimit = weekSplit[i];
                String[] dates = dayLimit.split("-");
                firstWeekStartDate = Integer.parseInt(dates[0]);
                firstWeekEndDate = Integer.parseInt(dates[1]);
            } else if (i == 1) {
                String dayLimit = weekSplit[i];
                String[] dates = dayLimit.split("-");
                secondWeekStartDate = Integer.parseInt(dates[0]);
                secondWeekEndDate = Integer.parseInt(dates[1]);
            } else if (i == 2) {
                String dayLimit = weekSplit[i];
                String[] dates = dayLimit.split("-");
                thirdWeekStartDate = Integer.parseInt(dates[0]);
                thirdWeekEndDate = Integer.parseInt(dates[1]);
            } else if (i == 3) {
                String dayLimit = weekSplit[i];
                String[] dates = dayLimit.split("-");
                fourthWeekStartDate = Integer.parseInt(dates[0]);
                fourthWeekEndDate = Integer.parseInt(dates[1]);
            }
        }
        int[] startEndDateArrays = {firstWeekStartDate, firstWeekEndDate, secondWeekStartDate,
                secondWeekEndDate, thirdWeekStartDate, thirdWeekEndDate, fourthWeekStartDate, fourthWeekEndDate};
        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("No of weeks calculation : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        /*
         * If number of users enrolled for a particular time period is empty then
         * just add week duration to the response. For example 1-7, 8-14, 15-21, 22-30 like wise
         */
        profilingStartTimeMillis = System.currentTimeMillis();
        if (usersMappingEnrolledForTheYear.isEmpty()) {
            for (MonthlyEnrollmentView responseView : monthlyEnrollmentViews) {
                addDataToResponseBasedOnMonthDays(0, responseView, startEndDateArrays);
            }
        } else {
            /*
             * Iterating through the list and increasing enrollment count only if
             * month matches. ie., If Jan already has enrollment count 2,
             * it will be increased as 3.
             */
            for (UserRoleMapping userEnrolled : usersMappingEnrolledForTheYear) {
                for (MonthlyEnrollmentView responseView : monthlyEnrollmentViews) {
                    /*
                     * Parsing and getting requested month from given date
                     */
                    CalendarView calendarView = validationService.validateAndGetDateMonthYearFromDate(userEnrolled.getCreatedDate());
                    int createdDate = calendarView.getDate();
                    addDataToResponseBasedOnMonthDays(createdDate, responseView, startEndDateArrays);
                }
            }
        }
        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Data construction for each week : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        MonthlyEnrollmentResponseView monthlyEnrollmentResponseView = new MonthlyEnrollmentResponseView();
        monthlyEnrollmentResponseView.setTotalCount(usersMappingEnrolledForTheYear.size()); // Setting the total users enrollment count
        monthlyEnrollmentResponseView.setEnrollmentPerWeek(monthlyEnrollmentViews);
        // Constructing response object
        ResponseModel res = new ResponseModel();
        res.setStatus(Constants.SUCCESS_STATUS);
        res.setMessage(MessageConstants.MSG_USERS_FETCHED);
        res.setPayload(monthlyEnrollmentResponseView);
        long apiEndTimeMillis = System.currentTimeMillis();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("getUsersEnrollmentCountForAMonth ends.");
        return res;
    }

    /**
     * Sectioning of weeks will be done based on the number of days in a month.
     * Week division based on days are as below
     * 30 days - 7days 7days 8days 8days
     * 31 days - 7days 8days 8days 8days
     * 28 days - 7days 7days 7days 7days
     * 29 days - 7days 7days 7days 8days
     *
     * @param createdDate
     * @param responseView
     */
    private void addDataToResponseBasedOnMonthDays(int createdDate, MonthlyEnrollmentView responseView, int[] startEndDateArrays) {

        /*
         * Increasing the enrollment count if the start date and end date falls within the week's criteria.
         * Weeks duration is set irrespective of this check!
         */
        if (responseView.getWeekId() == 0) {
            if (createdDate != 0 && createdDate >= startEndDateArrays[0] && createdDate <= startEndDateArrays[1]) {
                int enrollmentCount = responseView.getEnrollmentCount();
                enrollmentCount++;
                responseView.setEnrollmentCount(enrollmentCount);
            }
            responseView.setWeekDuration(startEndDateArrays[0] + "-" + startEndDateArrays[1]);
        } else if (responseView.getWeekId() == 1) {
            if (createdDate != 0 && createdDate >= startEndDateArrays[2] && createdDate <= startEndDateArrays[3]) {
                int enrollmentCount = responseView.getEnrollmentCount();
                enrollmentCount++;
                responseView.setEnrollmentCount(enrollmentCount);
            }
            responseView.setWeekDuration(startEndDateArrays[2] + "-" + startEndDateArrays[3]);
        } else if (responseView.getWeekId() == 2) {
            if (createdDate != 0 && createdDate >= startEndDateArrays[4] && createdDate <= startEndDateArrays[5]) {
                int enrollmentCount = responseView.getEnrollmentCount();
                enrollmentCount++;
                responseView.setEnrollmentCount(enrollmentCount);
            }
            responseView.setWeekDuration(startEndDateArrays[4] + "-" + startEndDateArrays[5]);
        } else if (responseView.getWeekId() == 3) {
            if (createdDate != 0 && createdDate >= startEndDateArrays[6] && createdDate <= startEndDateArrays[7]) {
                int enrollmentCount = responseView.getEnrollmentCount();
                enrollmentCount++;
                responseView.setEnrollmentCount(enrollmentCount);
            }
            responseView.setWeekDuration(startEndDateArrays[6] + "-" + startEndDateArrays[7]);
        }
    }

    /**
     * To get instructor details
     *
     * @param instructorId
     * @return
     */
    public ResponseModel getInstructorDetails(Long instructorId) {
        log.info("Get instructor details starts.");
        long start = new Date().getTime();
        ResponseModel responseModel = new ResponseModel();
        Map<String, Object> instructorDetails = new HashMap<>();
        validationService.validateInstructorId(instructorId);
        User user = userRepository.findByUserId(instructorId);
        UserProfile userProfile = userProfileRepository.findByUser(user);
        UserRole userRole = userRoleRepository.findByName(KeyConstants.KEY_INSTRUCTOR);
        boolean isActive = fitwiseUtils.isUserActive(userProfile.getUser(), userRole);
        String status = Boolean.toString(isActive);
        instructorDetails.put(KeyConstants.KEY_USER_STATUS, status);
        Date lastAccessDate = fitwiseUtils.getLastActiveDate(user, userRole);
        String lastAccessDateFormatted = fitwiseUtils.formatDate(lastAccessDate);
        instructorDetails.put(KeyConstants.KEY_LAST_ACTIVE_DATE, lastAccessDate);
        instructorDetails.put(KeyConstants.KEY_LAST_ACTIVE_DATE_FORMATTED, lastAccessDateFormatted);
        instructorDetails.put(KeyConstants.KEY_USER_FIRST_NAME, userProfile.getFirstName());
        instructorDetails.put(KeyConstants.KEY_USER_LAST_NAME, userProfile.getLastName());
        Map<String, Object> contactInformation = new HashMap<>();
        contactInformation.put(KeyConstants.KEY_EMAIL, user.getEmail());
        contactInformation.put(KeyConstants.KEY_CONTACT_NUMBER, userProfile.getContactNumber());
        contactInformation.put(KeyConstants.KEY_ISD_CODE, userProfile.getIsdCode());
        if (userProfile.getCountryCode() != null) {
            contactInformation.put(KeyConstants.KEY_COUNTRY_CODE, userProfile.getCountryCode());
            Countries country = countriesRepository.findByCountryCode(userProfile.getCountryCode());
            if (country != null)
                contactInformation.put(KeyConstants.KEY_COUNTRY_NAME, country.getCountryName());
        }
        instructorDetails.put(KeyConstants.KEY_CONTACT_INFORMATION, contactInformation);
        instructorDetails.put(KeyConstants.KEY_PROFILE_IMAGE, userProfile.getProfileImage());
        instructorDetails.put(KeyConstants.KEY_USER_BIO , userProfile.getBiography());
        UserRoleMapping userRoleMapping = userRoleMappingRepository.findTop1ByUserUserIdAndUserRoleName(user.getUserId(), KeyConstants.KEY_INSTRUCTOR);
        instructorDetails.put(KeyConstants.KEY_ONBOARDED_DATE,userRoleMapping.getCreatedDate());
        instructorDetails.put(KeyConstants.KEY_FORMATTED_ONBOARDED_DATE,fitwiseUtils.formatDate(userRoleMapping.getCreatedDate()));
        String promoUrl = null;
        String promoThumbnailUrl = null;
        if(userProfile.getPromotion() != null && userProfile.getPromotion().getVideoManagement() != null && userProfile.getPromotion().getVideoManagement().getUploadStatus().equalsIgnoreCase(KeyConstants.KEY_COMPLETED)){
            promoUrl = userProfile.getPromotion().getVideoManagement().getUrl();
            promoThumbnailUrl = userProfile.getPromotion().getVideoManagement().getThumbnail().getImagePath();
        }
        instructorDetails.put(KeyConstants.KEY_PROMO_URL,promoUrl);
        instructorDetails.put(KeyConstants.KEY_PROMO_THUMBNAIL_URL,promoThumbnailUrl);
        log.info("Basic Details : Time taken in millis : "+(new Date().getTime()-start));
        List<String> statusList = Arrays.asList(KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING);
        //Upcoming payment - from month's first day to till date - instructor share
        long temp = new Date().getTime();
        double upcomingPayment;
        double netRevenue = 0.0;
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        upcomingPayment = instructorAnalyticsService.calculateOutstandingPaymentOfAnInstructor(instructorId, null);
        instructorDetails.put(KeyConstants.KEY_UPCOMING_PAYMENT, KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(upcomingPayment));
        //Upcoming payment for Package subscriptions
        instructorDetails.put(KeyConstants.KEY_PACKAGE_OUTSTANDING_PAYMENT, KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(upcomingPayment));
        instructorDetails.put(KeyConstants.KEY_PROGRAM_OUTSTANDING_PAYMENT, KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(upcomingPayment));
        log.info("Upcoming payment for program,package : Time taken in millis : "+(new Date().getTime()-temp));
        //Getting current month's last date
        temp = new Date().getTime();
        Calendar cal = Calendar.getInstance();
        String monthName = null;
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DATE);
        DateFormatSymbols dateFormatSymbols = new DateFormatSymbols();
        String[] months = dateFormatSymbols.getMonths();
        if (month >= 0 && month <= 11 ) {
            monthName = months[month];
        }
        instructorDetails.put(KeyConstants.KEY_UPCOMING_PAYMENT_DATE, monthName + KeyConstants.KEY_SPACE + day);
        log.info("Upcoming payment date : Time taken in millis : "+(new Date().getTime()-temp));
        //Net revenue for an instructor
        temp = new Date().getTime();
        Double instructorShareByOwnerUserId = subscriptionAuditJPA.getInstructorShareByOwnerUserId(KeyConstants.KEY_PROGRAM, statusList, userProfile.getUser().getUserId());
        if (instructorShareByOwnerUserId != null)
            netRevenue = instructorShareByOwnerUserId;
        netRevenue = Math.round(netRevenue * 100.0) / 100.0;
        instructorDetails.put(KeyConstants.KEY_NET_REVENUE, KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(netRevenue));
        log.info("Net Revenue : Time taken in millis : "+(new Date().getTime()-temp));
        temp = new Date().getTime();
        TaxId taxId = taxRepository.findByUserUserId(user.getUserId());
        if (taxId != null) {
            TaxView taxView = new TaxView();
            String taxNo = null;
            if(taxId.getTaxNumber() != null){
                try{
                    taxNo = aesEncryption.decrypt(taxId.getTaxNumber());
                }catch (Exception e){
                    log.info("Decryption failed");
                }
            }
            taxView.setTaxNumber(taxNo);
            taxView.setTaxNumberType(taxId.getTaxTypes().getTaxNumberType());
            taxView.setTaxTypeId(taxId.getTaxTypes().getTaxTypeId());
            instructorDetails.put(KeyConstants.KEY_TAX_ID, taxView);
        } else {
            instructorDetails.put(KeyConstants.KEY_TAX_ID, null);
        }
        log.info("Tax Id Details : Time taken in millis : "+(new Date().getTime()-temp));
        temp = new Date().getTime();
        List<ExperienceView> instructorExperienceList = new ArrayList<>();
        List<InstructorProgramExperience> instructorProgramExperienceList = instructorExperienceRepository.findByUserUserId(user.getUserId());
        for (int i = 0; i < instructorProgramExperienceList.size(); i++) {
            ExperienceView experienceView = new ExperienceView();
            ProgramTypes programTypes = instructorProgramExperienceList.get(i).getProgramType();
            experienceView.setProgramTypeId(programTypes.getProgramTypeId());
            experienceView.setProgramType(programTypes.getProgramTypeName());
            YearsOfExpertise yearsOfExpertise = instructorProgramExperienceList.get(i).getExperience();
            experienceView.setYearsOfExperienceId(yearsOfExpertise.getExperienceId());
            experienceView.setNumberOfYears(yearsOfExpertise.getNumberOfYears());
            instructorExperienceList.add(experienceView);
        }
        instructorDetails.put(KeyConstants.KEY_INSTRUCTOR_EXPERIENCE, instructorExperienceList);
        log.info("Experience Details : Time taken in millis : "+(new Date().getTime()-temp));
        temp = new Date().getTime();
        List<OtherExpertise> otherExpertiseList = otherExpertiseRepository.findByUserUserId(user.getUserId());
        List<OtherExpertiseResponseView> otherExpertiseResponseViews = new ArrayList<>();
        for (OtherExpertise otherExpertise : otherExpertiseList) {
            OtherExpertiseResponseView otherExpertiseResponseView = new OtherExpertiseResponseView();
            otherExpertiseResponseView.setProgramType(otherExpertise.getExpertiseType());
            otherExpertiseResponseView.setNumberOfYears(otherExpertise.getExperience().getNumberOfYears());
            otherExpertiseResponseViews.add(otherExpertiseResponseView);
        }
        instructorDetails.put(KeyConstants.KEY_OTHER_EXPERTISE, otherExpertiseResponseViews);
        List<InstructorCertification> instructorCertificationList = certificateRepository.findByUser(user);
        instructorDetails.put(KeyConstants.KEY_CERTIFICATION, instructorCertificationList);
        List<InstructorAwards> instructorAwardsList = awardsRepository.findByUser(user);
        instructorDetails.put(KeyConstants.KEY_AWARDS, instructorAwardsList);
        List<Programs> programsList = programRepository.findByOwnerUserIdAndStatus(user.getUserId(), KeyConstants.KEY_PUBLISH);
        instructorDetails.put(KeyConstants.KEY_PROGRAM_COUNT, programsList.size());
        log.info("Other expertise, awards,certifications and programs : Time taken in millis : "+(new Date().getTime()-temp));
        /*
        To find total subscriptions of instructor published programs
         */
        temp = new Date().getTime();
        instructorDetails.put(KeyConstants.KEY_TOTAL_SUBSCRIPTIONS, subscriptionService.
                getActiveSubscripionCountOfAnInstructor(instructorId));
        log.info("Total Subscriptions : Time taken in millis : "+(new Date().getTime()-temp));
        boolean isUserBlocked = blockedUserRepository.existsByUserUserIdAndUserRoleName(instructorId, KeyConstants.KEY_INSTRUCTOR);
        instructorDetails.put(KeyConstants.KEY_BLOCKED, isUserBlocked);
        instructorDetails.put("totalExercises", exerciseRepository.countByOwnerUserId(instructorId));
        instructorDetails.put("totalWorkouts", workoutRepository.countByOwnerUserId(instructorId));
        instructorDetails.put("signUpDate", user.getCreatedDate());
        instructorDetails.put(KeyConstants.SOCIAL_LINKS, userLinkService.getSocialLinks(user));
        instructorDetails.put(KeyConstants.EXTERNAL_LINKS, userLinkService.getExternalLinks(user));
        responseModel.setPayload(instructorDetails);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_INSTRUCTOR_DETAILS_FETCHED);
        log.info("Get Instructor details : Time taken in millis : "+(new Date().getTime()-start));
        return responseModel;
    }

    /**
     * Gets the Program Analytics For Month.
     *
     * @param startDate
     * @return response model
     * @throws ParseException
     * @throws ApplicationException
     */
    public ResponseModel getProgramAnalyticsForMonth(Date startDate) throws ParseException {
        log.info("getProgramAnalyticsForMonth starts.");
        long apiStartTimeMillis = System.currentTimeMillis();

        long profilingStartTimeMillis = System.currentTimeMillis();
        validationService.validateAndGetDateMonthYearFromDate(startDate);
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);

        Calendar calendarStart=Calendar.getInstance();
        calendarStart.setTime(startDate);
        calendarStart.set(Calendar.DATE, calendarStart.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendarStart.set(Calendar.HOUR_OF_DAY, 0);
        calendarStart.set(Calendar.MINUTE, 0);
        calendarStart.set(Calendar.SECOND, 0);
        // returning the first date
        Date startTime=calendarStart.getTime();

        Calendar calendarEnd=Calendar.getInstance();
        calendarEnd.setTime(startDate);
        calendarEnd.set(Calendar.DATE, calendarEnd.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendarEnd.set(Calendar.HOUR_OF_DAY, 23);
        calendarEnd.set(Calendar.MINUTE, 59);
        calendarEnd.set(Calendar.SECOND, 59);
        // returning the last date
        Date endTime=calendarEnd.getTime();
        long profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Date manipulation : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));


        profilingStartTimeMillis = System.currentTimeMillis();
        List<ProgramAnalytics> programAnalytics = new ArrayList<>();
        List<InstructorSubscriptionCount> instructorSubscriptionCountList = subscriptionAuditJPA.getTotalRevenueAndSubscriptionCountByProgramTypeId(startTime, endTime);
        for (InstructorSubscriptionCount instructorSubscriptionCount : instructorSubscriptionCountList) {
            double revenue = 0.0;
            long subscriptionCount = 0;
            ProgramAnalytics programAnalytic = new ProgramAnalytics();

            if (instructorSubscriptionCount.getProgramType() != null && !instructorSubscriptionCount.getProgramType().isEmpty())
                programAnalytic.setProgramType(instructorSubscriptionCount.getProgramType());

            if (instructorSubscriptionCount.getProgramCount() != null)
                programAnalytic.setNumberOfPrograms(instructorSubscriptionCount.getProgramCount());

            if (instructorSubscriptionCount.getRevenue() != null)
                revenue = instructorSubscriptionCount.getRevenue();

            if (instructorSubscriptionCount.getSubscriptionCount() != null)
                subscriptionCount = instructorSubscriptionCount.getSubscriptionCount();

            revenue = Math.round(revenue * 100.0) / 100.0;
            programAnalytic.setRevenue(revenue);
            programAnalytic.setFormattedRevenue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(revenue));
            programAnalytic.setSubscriptions(subscriptionCount);
            programAnalytics.add(programAnalytic);
        }

        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Query and data construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        long apiEndTimeMillis = System.currentTimeMillis();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("getProgramAnalyticsForMonth ends.");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, programAnalytics);
    }

    /**
     * Gets the Program Analytics For year.
     *
     * @param startDate
     * @param startDate return response model
     */
    public ResponseModel getProgramAnalyticsForYear(Date startDate, Date endDate) throws ParseException {
        log.info("getProgramAnalyticsForYear starts.");
        long apiStartTimeMillis = System.currentTimeMillis();
        long profilingStartTimeMillis = System.currentTimeMillis();
        CalendarView mCalendar = validationService.validateAndGetDateMonthYearFromDate(startDate);
        int startRequestedYear = mCalendar.getYear();
        validationService.validateAndGetDateMonthYearFromDate(endDate);
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        int year = startRequestedYear;
        Calendar calendarStart=Calendar.getInstance();
        calendarStart.setTime(startDate);
        calendarStart.set(Calendar.YEAR,year);
        calendarStart.set(Calendar.MONTH,0);
        calendarStart.set(Calendar.DAY_OF_MONTH,1);
        calendarStart.set(Calendar.HOUR_OF_DAY, 0);
        calendarStart.set(Calendar.MINUTE, 0);
        calendarStart.set(Calendar.SECOND, 0);
        // returning the first date
        Date startTime=calendarStart.getTime();
        Calendar calendarEnd=Calendar.getInstance();
        calendarStart.setTime(endDate);
        calendarEnd.set(Calendar.YEAR,year);
        calendarEnd.set(Calendar.MONTH,11);
        calendarEnd.set(Calendar.DAY_OF_MONTH,31);
        calendarEnd.set(Calendar.HOUR_OF_DAY, 23);
        calendarEnd.set(Calendar.MINUTE, 59);
        calendarEnd.set(Calendar.SECOND, 59);
        // returning the last date
        Date endTime=calendarEnd.getTime();
        long profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Date manipulation : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        profilingStartTimeMillis = System.currentTimeMillis();
        List<ProgramAnalytics> programAnalytics = new ArrayList<>();
        List<InstructorSubscriptionCount> instructorSubscriptionCountList = subscriptionAuditJPA.getTotalRevenueAndSubscriptionCountByProgramTypeId(startTime, endTime);
        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Query to get instructor subscription count list : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        profilingStartTimeMillis = System.currentTimeMillis();
        for (InstructorSubscriptionCount instructorSubscriptionCount : instructorSubscriptionCountList) {
            double revenue = 0.0;
            long subscriptionCount = 0;
            ProgramAnalytics programAnalytic = new ProgramAnalytics();

            if (instructorSubscriptionCount.getProgramType() != null && !instructorSubscriptionCount.getProgramType().isEmpty())
                programAnalytic.setProgramType(instructorSubscriptionCount.getProgramType());

            if (instructorSubscriptionCount.getProgramCount() != null)
                programAnalytic.setNumberOfPrograms(instructorSubscriptionCount.getProgramCount());

            if (instructorSubscriptionCount.getRevenue() != null)
                revenue = instructorSubscriptionCount.getRevenue();

            if (instructorSubscriptionCount.getSubscriptionCount() != null)
                subscriptionCount = instructorSubscriptionCount.getSubscriptionCount();

            revenue = Math.round(revenue * 100.0) / 100.0;

            programAnalytic.setSubscriptions(subscriptionCount);
            programAnalytic.setRevenue(revenue);
            programAnalytic.setFormattedRevenue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(revenue));
            programAnalytics.add(programAnalytic);
        }
        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Data construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        long apiEndTimeMillis = System.currentTimeMillis();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("getProgramAnalyticsForYear ends.");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, programAnalytics);
    }

    /**
     * Gets the Program specialization For year and month.
     *
     * @param startDate
     * @param startDate return response model
     */
    public ResponseModel getSpecializationForMonth(Date startDate, Date endDate) throws ParseException {
        CalendarView mCalendar = validationService.validateAndGetDateMonthYearFromDate(startDate);
        int startRequestedYear = mCalendar.getYear();
        int startRequestedMonth = mCalendar.getMonth();
        validationService.validateAndGetDateMonthYearFromDate(endDate);
        List<SpecializationResponse> specializationResponses = new ArrayList<>();
        List<ProgramTypes> programTypes = programTypeRepository.findByOrderByProgramTypeNameAsc();
        float total = 0.0f;
        for (ProgramTypes programType : programTypes) {
            int year = startRequestedYear;
            int month = startRequestedMonth;
            float count = 0.0f;
            SpecializationResponse specializationResponse = new SpecializationResponse();
            int noOfDaysInTheMonth = ValidationUtils.getNumberOfDaysInTheMonth(year, month);
            LocalDateTime startLocalDate = LocalDateTime.of(year, month, 1, 0, 0, 0);
            LocalDateTime endLocalDate = LocalDateTime.of(year, month, noOfDaysInTheMonth, 23, 59, 59);
            Date startTime = Date.from(startLocalDate.atZone(ZoneId.systemDefault()).toInstant());
            Date endTime = Date.from(endLocalDate.atZone(ZoneId.systemDefault()).toInstant());
            count = count + programRepository.countByProgramTypeAndStatusAndCreatedDateBetween(programType, KeyConstants.KEY_PUBLISH, startTime, endTime);
            total = total + count;
            specializationResponse.setProgramType(programType.getProgramTypeName());
            specializationResponse.setPercentage(count);
            specializationResponses.add(specializationResponse);
        }
        if (total == 0) {
            return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, specializationResponses);
        }
        for (SpecializationResponse specializationResponse : specializationResponses) {
            float percentage = (specializationResponse.getPercentage() / total) * 100;
            percentage = (float) (Math.round(percentage * 100.0) / 100.0);
            specializationResponse.setPercentage(percentage);
        }
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, specializationResponses);
    }

    public ResponseModel getSpecializationForYear(Date startDate, Date endDate) throws ParseException {
        CalendarView mCalendar = validationService.validateAndGetDateMonthYearFromDate(startDate);
        int startRequestedYear = mCalendar.getYear();
        validationService.validateAndGetDateMonthYearFromDate(endDate);
        List<SpecializationResponse> specializationResponses = new ArrayList<>();
        List<ProgramTypes> programTypes = programTypeRepository.findByOrderByProgramTypeNameAsc();
        float total = 0.0f;
        int year = startRequestedYear;
        int month = 1;
        for (ProgramTypes programType : programTypes) {
            float count = 0.0f;
            SpecializationResponse specializationResponse = new SpecializationResponse();
            for (int index = 0; index <= 11; index++) {
                int noOfDaysInTheMonth = ValidationUtils.getNumberOfDaysInTheMonth(year , month);
                LocalDateTime startLocalDate = LocalDateTime.of(year, month,1,0,0,0);
                LocalDateTime endLocalDate = LocalDateTime.of(year,month,noOfDaysInTheMonth,23,59,59);
                Date startTime = Date.from(startLocalDate.atZone(ZoneId.systemDefault()).toInstant());
                Date endTime = Date.from(endLocalDate.atZone(ZoneId.systemDefault()).toInstant());

                count = count + programRepository.countByProgramTypeAndStatusAndCreatedDateBetween(programType,KeyConstants.KEY_PUBLISH,startTime,endTime);
                if (month == 12) {
                    month = 1;
                    year = 1 + year;
                    continue;
                }
                month++;
            }
            total = total + count;
            specializationResponse.setProgramType(programType.getProgramTypeName());
            specializationResponse.setPercentage(count);
            specializationResponses.add(specializationResponse);
            month = 1;
            year = startRequestedYear;
        }
        if (total == 0) {
            return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, specializationResponses);
        }

        for (SpecializationResponse specializationResponse : specializationResponses) {
            float percentage = 0;
            percentage = (specializationResponse.getPercentage() / total) * 100;
            percentage = (float) (Math.round(percentage * 100.0) / 100.0);
            specializationResponse.setPercentage(percentage);
        }
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, specializationResponses);
    }

    /**
     * To get program details
     *
     * @param programId
     * @return
     * @throws ParseException
     */
    public ProgramDetailsResponseModel getProgram(final Long programId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ParseException {
        log.info("Admin get program starts.");
        long apiStartTimeMillis = new Date().getTime();
        Programs program = programImpl.getProgram(programId);
        log.info("Query: Get program : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        if (program == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, null);
        }
        ProgramResponseModel programResponseModel = programService.constructProgramModel(program);
        log.info("Construct program model : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        ProgramDetailsResponseModel programDetailsResponseModel = new ProgramDetailsResponseModel();
        BeanUtils.copyProperties(programResponseModel, programDetailsResponseModel);
        log.info("Copy bean properties : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        List<String> statusList = Arrays.asList(KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING);
        int totalSubscriptions = subscriptionAuditRepo.countBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndProgramSubscriptionProgramProgramId(KeyConstants.KEY_PROGRAM, statusList, programId);
        programDetailsResponseModel.setTotalSubscriptions(totalSubscriptions);
        log.info("Query; get subscription count : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Admin get program ends.");
        return programDetailsResponseModel;
    }

    /**
     * Gets the flagged videos.
     *
     * @param pageNo
     * @param pageSize
     * @param sortOrder
     * @param sortBy
     * @param searchName
     * @param blockStatusOptional
     * @return
     */
    public AdminListResponse getFlaggedVideos(final int pageNo, final int pageSize, String sortOrder, String sortBy, Optional<String> searchName, Optional<String> blockStatusOptional) {
        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ERROR, null);
        }
        if (!(sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC) || sortOrder.equalsIgnoreCase(SearchConstants.ORDER_ASC))) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ORDER_ERROR, null);
        }
        if (!(sortBy.equalsIgnoreCase(SearchConstants.FLAGGED_EXERCISE) || sortBy.equalsIgnoreCase(SearchConstants.FLAGGED_COUNT) ||
                sortBy.equalsIgnoreCase(SearchConstants.LATEST_FLAGGED_TIME) || sortBy.equalsIgnoreCase(SearchConstants.FIRST_FLAGGED_TIME))) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_SORT_BY, null);
        }
        String blockStatus;
        if (blockStatusOptional.isPresent() && !blockStatusOptional.get().isEmpty()) {
            blockStatus = blockStatusOptional.get();
            if (!(blockStatus.equalsIgnoreCase(KeyConstants.KEY_ALL) || blockStatus.equalsIgnoreCase(KeyConstants.KEY_OPEN) || blockStatus.equalsIgnoreCase(KeyConstants.KEY_BLOCKED))) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_BLOCK_STATUS_PARAM_INCORRECT, null);
            }
        } else {
            blockStatus = KeyConstants.KEY_ALL;
        }

        List<String> flagStatusList = new ArrayList<>();
        if (blockStatus.equalsIgnoreCase(KeyConstants.KEY_BLOCKED)) {
            flagStatusList.add(KeyConstants.KEY_BLOCK);
        } else if (blockStatus.equalsIgnoreCase(KeyConstants.KEY_OPEN)) {
            flagStatusList.add(DBConstants.KEY_IGNORE);
            flagStatusList.add(DBConstants.KEY_REPORTED);
        } else {
            flagStatusList.add(KeyConstants.KEY_BLOCK);
            flagStatusList.add(DBConstants.KEY_IGNORE);
            flagStatusList.add(DBConstants.KEY_REPORTED);
        }

        Page<FlaggedExercisesSummary> flaggedExercisesSummaryPage;

        Sort sort = getFlaggedVideosSortCriteria(sortBy);
        if (sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC)) {
            sort = sort.descending();
        }
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, sort);

        if (searchName.isPresent() && !searchName.get().isEmpty()) {
            flaggedExercisesSummaryPage = flaggedExercisesSummaryRepository.findByExerciseTitleIgnoreCaseContainingAndFlagStatusIn(searchName.get(), flagStatusList, pageRequest);
        } else {
            flaggedExercisesSummaryPage = flaggedExercisesSummaryRepository.findByFlagStatusIn(flagStatusList, pageRequest);
        }

        if (flaggedExercisesSummaryPage.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }

        List<FlaggedVideosResponse> flaggedVideosResponses = constructFlaggedVideosResponse(flaggedExercisesSummaryPage.getContent());

        AdminListResponse<FlaggedVideosResponse> adminListResponse = new AdminListResponse<>();
        adminListResponse.setTotalSizeOfList(flaggedExercisesSummaryPage.getTotalElements());
        adminListResponse.setPayloadOfAdmin(flaggedVideosResponses);
        return adminListResponse;
    }

    private List<FlaggedVideosResponse> constructFlaggedVideosResponse(List<FlaggedExercisesSummary> flaggedExercisesSummaryList) {
        if (flaggedExercisesSummaryList == null) {
            return Collections.emptyList();
        }
        List<FlaggedVideosResponse> flaggedVideosResponseList = new ArrayList<>();
        for (FlaggedExercisesSummary flaggedExercisesSummary : flaggedExercisesSummaryList) {
            FlaggedVideosResponse flaggedVideosResponse = new FlaggedVideosResponse();
            Exercises exercises = flaggedExercisesSummary.getExercise();
            flaggedVideosResponse.setExerciseId(exercises.getExerciseId());
            flaggedVideosResponse.setExerciseTitle(exercises.getTitle());
            flaggedVideosResponse.setFlaggedCount(flaggedExercisesSummary.getFlaggedCount());
            flaggedVideosResponse.setLatestFlaggedtime(flaggedExercisesSummary.getLatestFlaggedDate());
            String latestDateString = fitwiseUtils.formatDateWithTime(flaggedExercisesSummary.getLatestFlaggedDate());
            flaggedVideosResponse.setLatestFlaggedtimeFormatted(latestDateString);
            flaggedVideosResponse.setFirstFlaggedtime(flaggedExercisesSummary.getFirstFlaggedDate());
            String firstDateString = fitwiseUtils.formatDateWithTime(flaggedExercisesSummary.getFirstFlaggedDate());
            flaggedVideosResponse.setFirstFlaggedtimeFormatted(firstDateString);
            VideoManagement videoManagement = exercises.getVideoManagement();
            if (videoManagement != null && videoManagement.getThumbnail() != null) {
                flaggedVideosResponse.setExerciseThumbnailUrl(videoManagement.getThumbnail().getImagePath());
                flaggedVideosResponse.setExerciseVideoId(videoManagement.getUrl());
            }
            flaggedVideosResponse.setFlagStatus(flaggedExercisesSummary.getFlagStatus());
            flaggedVideosResponseList.add(flaggedVideosResponse);
        }
        return flaggedVideosResponseList;
    }

    private Sort getFlaggedVideosSortCriteria(String sortBy) {
        Sort sort = Sort.by("exercise.title");
        if (sortBy.equalsIgnoreCase(SearchConstants.FLAGGED_COUNT)) {
            sort = Sort.by("flaggedCount");
        } else if (sortBy.equalsIgnoreCase(SearchConstants.LATEST_FLAGGED_TIME)) {
            sort = Sort.by("latestFlaggedDate");
        } else if (sortBy.equalsIgnoreCase(SearchConstants.FIRST_FLAGGED_TIME)) {
            sort = Sort.by("firstFlaggedDate");
        }
        return sort;
    }

    /**
     * @param exerciseId
     * @return
     */
    @Transactional
    public String blockFlaggedVideo(Long exerciseId) {
        String responseMsg = MessageConstants.MSG_EXERCISE_FLAG_STATUS_BLOCKED;
        boolean isAlreadyBlocked = false;
        if (exerciseId == null || exerciseId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_ID_NULL, MessageConstants.ERROR);
        }
        FlaggedExercisesSummary flaggedExercise = flaggedExercisesSummaryRepository.findByExerciseExerciseId(exerciseId);
        if (flaggedExercise == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FLAGGED_EXERCISE_NOT_FOUND, MessageConstants.ERROR);
        }
        if (KeyConstants.KEY_BLOCK.equals(flaggedExercise.getFlagStatus())) {
            responseMsg = ValidationMessageConstants.MSG_FLAGGED_EXERCISE_ALREADY_BLOCKED;
            isAlreadyBlocked = true;
        }

        if (!isAlreadyBlocked) {
            flaggedExercise.setFlagStatus(KeyConstants.KEY_BLOCK);
            flaggedExercisesSummaryRepository.save(flaggedExercise);

            //Block affected programs
            List<ExerciseSchedulers> exerciseSchedulerList = exerciseScheduleRepository.findByExerciseExerciseId(exerciseId);
            List<Long> circuitIdList = exerciseSchedulerList.stream().map(exerciseScheduler -> exerciseScheduler.getCircuit().getCircuitId()).distinct().collect(Collectors.toList());
            List<CircuitSchedule> circuitScheduleList = circuitScheduleRepository.findByCircuitCircuitIdIn(circuitIdList);
            List<Long> workoutIdList = circuitScheduleList.stream().map(circuitSchedule -> circuitSchedule.getWorkout().getWorkoutId()).distinct().collect(Collectors.toList());
            List<WorkoutMapping> workoutMappingList = workoutMappingRepository.findByWorkoutWorkoutIdIn(workoutIdList);
            List<Programs> programsList = workoutMappingList.stream().map(WorkoutMapping::getPrograms).collect(Collectors.toList());

            //removing duplicated
            programsList = programsList.stream()
                    .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparingLong(Programs::getProgramId))), ArrayList::new));

            //Getting only published programs
            programsList = programsList.stream().filter(program -> KeyConstants.KEY_PUBLISH.equals(program.getStatus())).collect(Collectors.toList());

            //blocking only published programs related to flagged video
            List<Long> programIdList = programsList.stream().map(Programs::getProgramId).distinct().collect(Collectors.toList());

            for (Long programId : programIdList) {
                adminBlockedService.blockProgram(programId, KeyConstants.KEY_FLAGGED_VIDEO_BLOCK);
            }

            //Sending mail to instructor
            String subject = EmailConstants.EXERCISE_BLOCK_SUBJECT.replace(EmailConstants.LITERAL_EXERCISE_NAME, "'" + flaggedExercise.getExercise().getTitle() + "'");
            String supportLink = EmailConstants.TRAINNR_SUPPORT_LINK.replace(EmailConstants.EMAIL_EMAIL_ADDRESS, flaggedExercise.getExercise().getOwner().getEmail());
            String mailBody = EmailConstants.EXERCISE_BLOCK_CONTENT.replace(EmailConstants.LITERAL_EXERCISE_NAME, "<b>" + flaggedExercise.getExercise().getTitle() + "</b>");
            String userName = fitwiseUtils.getUserFullName(flaggedExercise.getExercise().getOwner());
            mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON
                    .replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
                    .replace(EmailConstants.EMAIL_BODY, mailBody)
                    .replace(EmailConstants.EMAIL_SUPPORT_URL, supportLink);
            mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
            asyncMailer.sendHtmlMail(flaggedExercise.getExercise().getOwner().getEmail(), subject, mailBody);
        }
        return responseMsg;
    }

    /**
     * @param exerciseId
     */
    @Transactional
    public void unblockFlaggedVideo(Long exerciseId) {
        if (exerciseId == null || exerciseId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_ID_NULL, MessageConstants.ERROR);
        }
        FlaggedExercisesSummary flaggedExercise = flaggedExercisesSummaryRepository.findByExerciseExerciseId(exerciseId);
        if (flaggedExercise == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FLAGGED_EXERCISE_NOT_FOUND, MessageConstants.ERROR);
        }
        if (!(KeyConstants.KEY_BLOCK.equals(flaggedExercise.getFlagStatus()))) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FLAGGED_EXERCISE_NOT_BLOCKED, MessageConstants.ERROR);
        }

        //Block affected programs
        List<ExerciseSchedulers> exerciseSchedulerList = exerciseScheduleRepository.findByExerciseExerciseId(exerciseId);
        List<Long> circuitIdList = exerciseSchedulerList.stream().map(exerciseScheduler -> exerciseScheduler.getCircuit().getCircuitId()).distinct().collect(Collectors.toList());
        List<CircuitSchedule> circuitScheduleList = circuitScheduleRepository.findByCircuitCircuitIdIn(circuitIdList);
        List<Long> workoutIdList = circuitScheduleList.stream().map(circuitSchedule -> circuitSchedule.getWorkout().getWorkoutId()).distinct().collect(Collectors.toList());
        List<WorkoutMapping> workoutMappingList = workoutMappingRepository.findByWorkoutWorkoutIdIn(workoutIdList);
        List<Long> programIdList = workoutMappingList.stream().map(workoutMapping -> workoutMapping.getPrograms().getProgramId()).distinct().collect(Collectors.toList());

        List<BlockedPrograms> blockedProgramList = blockedProgramsRepository.findByProgramProgramIdInAndBlockType(programIdList, KeyConstants.KEY_FLAGGED_VIDEO_BLOCK);

        //Getting blocked flagged video programs
        programIdList = blockedProgramList.stream().map(blockedProgram -> blockedProgram.getProgram().getProgramId()).collect(Collectors.toList());

        long subscriptionCount = subscriptionService.getOverallActiveSubscriptionCountForProgramsList(programIdList);
        if (subscriptionCount > 0) {
            throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_CANT_UNBLOCK_EXERCISE_SUBSCRIBED_PROGRAMS, MessageConstants.ERROR);
        }

        flaggedExercise.setFlagStatus(DBConstants.KEY_REPORTED);
        flaggedExercisesSummaryRepository.save(flaggedExercise);

        for (Long programId : programIdList) {
            adminBlockedService.unBlockProgram(programId, KeyConstants.KEY_FLAGGED_VIDEO_BLOCK);
        }

        //Sending mail to instructor
        String subject = EmailConstants.EXERCISE_UNBLOCK_SUBJECT.replace(EmailConstants.LITERAL_EXERCISE_NAME, "'" + flaggedExercise.getExercise().getTitle() + "'");
        String mailBody = EmailConstants.EXERCISE_UNBLOCK_CONTENT.replace(EmailConstants.LITERAL_EXERCISE_NAME, "<b>" + flaggedExercise.getExercise().getTitle() + "</b>");
        String userName = fitwiseUtils.getUserFullName(flaggedExercise.getExercise().getOwner());
        mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, mailBody);
        mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
        asyncMailer.sendHtmlMail(flaggedExercise.getExercise().getOwner().getEmail(), subject, mailBody);

    }

    public void ignoreFlaggedVideo(Long exerciseId) {
        if (exerciseId == null || exerciseId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_ID_NULL, MessageConstants.ERROR);
        }
        FlaggedExercisesSummary flaggedExercise = flaggedExercisesSummaryRepository.findByExerciseExerciseId(exerciseId);
        if (flaggedExercise == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FLAGGED_EXERCISE_NOT_FOUND, MessageConstants.ERROR);
        }
        if (KeyConstants.KEY_BLOCK.equals(flaggedExercise.getFlagStatus())) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FLAGGED_EXERCISE_CANT_IGNORE, MessageConstants.ERROR);
        }
        flaggedExercise.setFlagStatus(DBConstants.KEY_IGNORE);
        flaggedExercisesSummaryRepository.save(flaggedExercise);
    }

    public FlaggedVideoDetailsView getFlaggedVideoDetails(Long exerciseId) {
        log.info("Get flagged video details starts.");
        long apiStartTimeMillis = new Date().getTime();
        if (exerciseId == null || exerciseId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_ID_NULL, MessageConstants.ERROR);
        }
        FlaggedVideoDetailsView flaggedVideoDetailsView = new FlaggedVideoDetailsView();

        FlaggedExercisesSummary flaggedExerciseSummary = flaggedExercisesSummaryRepository.findByExerciseExerciseId(exerciseId);
        if (flaggedExerciseSummary == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FLAGGED_EXERCISE_NOT_FOUND, MessageConstants.ERROR);
        }
        log.info("Basic validations : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        validationService.validateExerciseId(exerciseId);
        log.info("Validate exercise id : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        Exercises exercise = flaggedExerciseSummary.getExercise();
        flaggedVideoDetailsView.setExerciseId(exercise.getExerciseId());
        flaggedVideoDetailsView.setTitle(exercise.getTitle());

        VideoManagement videoManagement = exercise.getVideoManagement();
        if (videoManagement != null) {
            flaggedVideoDetailsView.setVideoId(videoManagement.getUrl());
            flaggedVideoDetailsView.setThumbnailUrl(videoManagement.getThumbnail().getImagePath());
        }

        UserProfile userProfile = userProfileRepository.findByUser(exercise.getOwner());
        if (userProfile == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_FOUND, MessageConstants.ERROR);
        }

        String ownerName = userProfile.getFirstName();
        if (userProfile.getLastName() != null) {
            ownerName = ownerName + " " + userProfile.getLastName();
        }
        flaggedVideoDetailsView.setOwner(ownerName);

        if (userProfile.getProfileImage() != null) {
            flaggedVideoDetailsView.setOwnerProfileImage(userProfile.getProfileImage().getImagePath());
        }
        flaggedVideoDetailsView.setFlaggedCount(flaggedExerciseSummary.getFlaggedCount());

        flaggedVideoDetailsView.setFirstFlaggedtime(fitwiseUtils.formatDate(flaggedExerciseSummary.getFirstFlaggedDate()));
        flaggedVideoDetailsView.setLatestFlaggedtime(fitwiseUtils.formatDate(flaggedExerciseSummary.getLatestFlaggedDate()));
        flaggedVideoDetailsView.setFlagStatus(flaggedExerciseSummary.getFlagStatus());
        log.info("Query to get user profile and construct flagged video details view : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        //Gettting reason count
        List<FlaggedExercise> flaggedExerciseList = flaggedExerciseRepository.findByExerciseExerciseId(exerciseId);
        List<FlaggedVideoReason> flaggedVideoReasonList = flaggedVideoReasonsRepository.findAll();
        log.info("Query to get reason count : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        List<ReasonCountView> reasonForFlagging = new ArrayList<>();
        for (FlaggedVideoReason flaggedVideoReason : flaggedVideoReasonList) {
            ReasonCountView reasonCountView = new ReasonCountView();
            long count = flaggedExerciseList.stream()
                    .filter(flaggedExercise -> flaggedExercise.getFlaggedVideoReason().getFeedbackId() == flaggedVideoReason.getFeedbackId())
                    .count();
            reasonCountView.setReasonId(flaggedVideoReason.getFeedbackId());
            reasonCountView.setReason(flaggedVideoReason.getFeedbackReason());
            reasonCountView.setCount(count);
            reasonForFlagging.add(reasonCountView);
        }
        log.info("Construct reason for flagging : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        //Reasons sorted by count in descending order
        reasonForFlagging.sort(Comparator.comparingLong(ReasonCountView::getCount).reversed().thenComparing(ReasonCountView::getReason));
        flaggedVideoDetailsView.setReasonForFlagging(reasonForFlagging);
        List<ExerciseSchedulers> exerciseSchedulerList = exerciseScheduleRepository.findByExerciseExerciseId(exerciseId);
        List<Long> circuitIdList = exerciseSchedulerList.stream().map(exerciseScheduler -> exerciseScheduler.getCircuit().getCircuitId()).distinct().collect(Collectors.toList());
        List<CircuitSchedule> circuitScheduleList = circuitScheduleRepository.findByCircuitCircuitIdIn(circuitIdList);
        List<Long> workoutIdList = circuitScheduleList.stream().map(circuitSchedule -> circuitSchedule.getWorkout().getWorkoutId()).distinct().collect(Collectors.toList());
        List<WorkoutMapping> workoutMappingList = workoutMappingRepository.findByWorkoutWorkoutIdIn(workoutIdList);
        List<Programs> programs = workoutMappingList.stream().map(WorkoutMapping::getPrograms).collect(Collectors.toList());
        log.info("Query to get programs : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        //removing duplicated
        programs = programs.stream()
                .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparingLong(Programs::getProgramId))), ArrayList::new));
        //Getting only published/blocked programs
        List<String> statusList = Arrays.asList(KeyConstants.KEY_PUBLISH, InstructorConstant.BLOCK, DBConstants.BLOCK_EDIT);
        programs = programs.stream().filter(program -> statusList.contains(program.getStatus())).collect(Collectors.toList());
        List<FlaggedVideoAffectedProgram> affectedPrograms = constructFlaggedVideoAffectedPrograms(programs);
        log.info("Construct response model : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get flagged video details ends.");
        flaggedVideoDetailsView.setImpactedPrograms(affectedPrograms);
        return flaggedVideoDetailsView;
    }

    /**
     * Constructing affected programs view
     * @param programs
     * @return
     */
    private List<FlaggedVideoAffectedProgram> constructFlaggedVideoAffectedPrograms(List<Programs> programs) {

        List<FlaggedVideoAffectedProgram> flaggedVideoAffectedPrograms = new ArrayList<>();
        for (Programs program : programs) {
            FlaggedVideoAffectedProgram affectedProgram = new FlaggedVideoAffectedProgram();
            affectedProgram.setProgramId(program.getProgramId());
            affectedProgram.setProgramTitle(program.getTitle());

            //Constructing associated packages
            List<SubscriptionPackagePackageIdAndTitleView> associatedPackages = new ArrayList<>();
            List<PackageProgramMapping> packageProgramMappingList = packageProgramMappingRepository.findByProgramAndSubscriptionPackageStatus(program, KeyConstants.KEY_PUBLISH);
            if(!packageProgramMappingList.isEmpty()){
                for(PackageProgramMapping packageProgramMapping : packageProgramMappingList){
                    SubscriptionPackagePackageIdAndTitleView associatedPackageView = new SubscriptionPackagePackageIdAndTitleView();
                    associatedPackageView.setSubscriptionPackageId(packageProgramMapping.getSubscriptionPackage().getSubscriptionPackageId());
                    associatedPackageView.setTitle(packageProgramMapping.getSubscriptionPackage().getTitle());
                    associatedPackages.add(associatedPackageView);
                }
            }
            affectedProgram.setAssociatedPackages(associatedPackages);

            flaggedVideoAffectedPrograms.add(affectedProgram);
        }
        return flaggedVideoAffectedPrograms;
    }

    /**
     * Method for MemberProgramHistory under admin -> member tab
     * @param memberId Member ID
     * @param pageNo Page number
     * @param pageSize Page size
     * @return Map Object
     */
    public Map<String, Object> getMemberProgramHistory(Long memberId, int pageNo, final int pageSize) {
        log.info("Get member program history starts.");
        long apiStartTimeMillis = new Date().getTime();
        RequestParamValidator.pageSetup(pageNo, pageSize);
        User member = validationService.validateMemberId(memberId);
        log.info("Validate member id : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize);
        List<String> statusList = Arrays.asList(KeyConstants.KEY_PAID, KeyConstants.KEY_TRIAL, KeyConstants.KEY_PAYMENT_PENDING, KeyConstants.KEY_EXPIRED);
        Page<ProgramSubscription> programSubscriptions = programSubscriptionRepo.findByUserUserIdAndSubscriptionStatusSubscriptionStatusNameIn(memberId, statusList, pageRequest);
        log.info("Query: get program subscription list from DB : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        List<MemberProgramTileView> programTileViewList = new ArrayList<>();
        for (ProgramSubscription programSubscription : programSubscriptions) {
            MemberProgramTileView memberProgramTileView = new MemberProgramTileView();
            Programs program = programSubscription.getProgram();
            memberProgramTileView.setProgramId(program.getProgramId());
            memberProgramTileView.setProgramTitle(program.getTitle());
            memberProgramTileView.setDuration(program.getDuration().getDuration());
            memberProgramTileView.setThumbnail(program.getImage());
            SubscriptionStatus subscriptionStatus = subscriptionService.getMemberProgramSubscriptionStatus(programSubscription);
            if (subscriptionStatus != null) {
                if (subscriptionStatus.getSubscriptionStatusId() == 2 || subscriptionStatus.getSubscriptionStatusId() == 3) {
                    // Program is subscribed
                    memberProgramTileView.setSubscriptionStatus(KeyConstants.KEY_SUBSCRIBED);
                } else if (subscriptionStatus.getSubscriptionStatusId() == 5) {
                    // Program subscription is expired
                    memberProgramTileView.setSubscriptionStatus(KeyConstants.KEY_EXPIRED);

                } else if (subscriptionStatus.getSubscriptionStatusId() == 1) {
                    memberProgramTileView.setSubscriptionStatus(KeyConstants.KEY_TRIAL);
                } else {
                    memberProgramTileView.setSubscriptionStatus(subscriptionStatus.getSubscriptionStatusName());
                }
            }
            Date subscribedDate = programSubscription.getSubscribedDate();
            memberProgramTileView.setSubscribedDate(subscribedDate);
            memberProgramTileView.setSubscribedDateFormatted(fitwiseUtils.formatDate(subscribedDate));
            Date initialStartDate = programSubscription.getCreatedDate();
            memberProgramTileView.setInitialStartDate(initialStartDate);
            memberProgramTileView.setInitialStartDateFormatted(fitwiseUtils.formatDate(initialStartDate));
            List<WorkoutCompletion> workoutCompletionList = workoutCompletionRepository.findByMemberUserIdAndProgramProgramId(member.getUserId(), program.getProgramId());
            int completedWorkouts = workoutCompletionList.size();
            int totalDays = program.getDuration().getDuration().intValue();
            if (completedWorkouts > 0) {
                Date startedDate = workoutCompletionList.get(0).getCompletedDate();
                memberProgramTileView.setStartDate(startedDate);
                memberProgramTileView.setStartDateFormatted(fitwiseUtils.formatDate(startedDate));
            }
            String progress;
            long progressPercent;
            if (completedWorkouts == totalDays) {
                progress = "Completed";
                progressPercent = 100;
                Date completionDate = workoutCompletionList.get(workoutCompletionList.size() - 1).getCompletedDate();
                memberProgramTileView.setCompletedDate(completionDate);
                memberProgramTileView.setCompletedDateFormatted(fitwiseUtils.formatDate(completionDate));
            } else {
                progress = Convertions.getNumberWithZero(completedWorkouts) + "/" + totalDays;
                progressPercent = (completedWorkouts * 100) / totalDays;
            }
            memberProgramTileView.setProgress(progress);
            memberProgramTileView.setProgressPercent(progressPercent);
            programTileViewList.add(memberProgramTileView);
        }
        log.info("Construct member program tile view : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        Map<String, Object> memberDetails = new HashMap<>();
        memberDetails.put(KeyConstants.KEY_PROGRAM, programTileViewList);
        memberDetails.put(KeyConstants.KEY_COMPLETED_PROGRAMS_COUNT, getCompletedProgramCountByMember(memberId));
        List<ProgramSubscription> programSubscriptionList = subscriptionService.getPaidProgramSubscriptionsByAnUser(memberId);
        memberDetails.put(KeyConstants.KEY_USER_SUBSCRIBED_PROGRAMS_COUNT, programSubscriptionList.size());
        memberDetails.put(KeyConstants.KEY_TOTAL_COUNT, programSubscriptions.getTotalElements());

        List<String> subscriptionTypeList = Arrays.asList(KeyConstants.KEY_PROGRAM);
        double amountSpentByMember = getAmountSpentByMember(member, subscriptionTypeList);
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        memberDetails.put(KeyConstants.KEY_AMOUNT_SPENT_BY_MEMBER, KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(amountSpentByMember));
        log.info("Query: get subscribed program count and construct response model : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get member program history ends.");
        return memberDetails;
    }

    public FlaggedReasonDetailsView getFlaggedReasonDetails(Long exerciseId, Long reasonId, int pageNo, int pageSize) {
        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ERROR, null);
        }
        validationService.validateExerciseId(exerciseId);
        if (reasonId == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FLAG_REASON_ID_NULL, MessageConstants.ERROR);
        }
        Optional<FlaggedVideoReason> flaggedVideoReasonOptional = flaggedVideoReasonsRepository.findById(reasonId);
        if (!flaggedVideoReasonOptional.isPresent()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FLAG_REASON_ID_NOT_FOUND, MessageConstants.ERROR);
        }
        Specification<UserProfile> specification = UserProfileSpecifications.getUserProfileByFlaggedVideoAndReasonId(exerciseId, reasonId);
        Page<UserProfile> userProfilePage = userProfileRepository.findAll(specification, PageRequest.of(pageNo - 1, pageSize));
        if (userProfilePage.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        FlaggedReasonDetailsView flaggedReasonDetailsView = new FlaggedReasonDetailsView();
        flaggedReasonDetailsView.setReason(flaggedVideoReasonOptional.get().getFeedbackReason());
        flaggedReasonDetailsView.setTotalCount(userProfilePage.getTotalElements());
        List<UserNameProfileImgView> userNameProfileImgViewList = new ArrayList<>();
        for (UserProfile userProfile : userProfilePage) {
            UserNameProfileImgView userNameProfileImgView = new UserNameProfileImgView();
            userNameProfileImgView.setUserId(userProfile.getUser().getUserId());
            userNameProfileImgView.setUserName(userProfile.getFirstName() + " " + userProfile.getLastName());
            if (userProfile.getProfileImage() != null) {
                userNameProfileImgView.setProfileImageUrl(userProfile.getProfileImage().getImagePath());
            }
            userNameProfileImgViewList.add(userNameProfileImgView);
        }
        flaggedReasonDetailsView.setUsers(userNameProfileImgViewList);
        return flaggedReasonDetailsView;
    }

    /**
     * To get completed program count by member
     * @param memberId
     * @return
     */
    public int getCompletedProgramCountByMember(long memberId) {
        int count = 0;
        List<WorkoutCompletion> workoutCompletionList = workoutCompletionRepository.findByMemberUserId(memberId);
        Map<Long, ProgramCompletionCount> workoutCompletionMap = new HashMap<>();
        for (WorkoutCompletion workoutCompletion : workoutCompletionList) {
            if (!KeyConstants.KEY_DELETED.equalsIgnoreCase(workoutCompletion.getProgram().getStatus())) {
                Long programId = workoutCompletion.getProgram().getProgramId();
                if (!workoutCompletionMap.containsKey(programId)) {
                    workoutCompletionMap.put(programId, new ProgramCompletionCount(workoutCompletion.getProgram(), 1));
                } else {
                    ProgramCompletionCount programCompletionCount = workoutCompletionMap.get(programId);
                    int updatedCount = programCompletionCount.getCompletionCount() + 1;
                    programCompletionCount.setCompletionCount(updatedCount);
                    workoutCompletionMap.put(programId, programCompletionCount);
                }
            }
        }

        for (Map.Entry<Long, ProgramCompletionCount> entry : workoutCompletionMap.entrySet()) {
            ProgramCompletionCount programCompletionCount = entry.getValue();
            Programs program = programCompletionCount.getProgram();
            if (programCompletionCount.getCompletionCount() == program.getDuration().getDuration().intValue()) {
                count++;
            }
        }

        return count;
    }

    @Data
    @AllArgsConstructor
    private class ProgramCompletionCount {
        private Programs program;
        int completionCount;
    }

    /**
     * to get total amount spent by member
     * @param member
     * @return
     */
    public double getAmountSpentByMember(User member, List<String> subscriptionTypeList) {
        double amountSpentByMember = 0.0;
        Long userId = member.getUserId();
        List<String> statusValues = Arrays.asList(KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING);
        if (subscriptionTypeList != null) {
            for (String subscriptionType : subscriptionTypeList) {
                List<SubscriptionAudit> subscriptionAudits = subscriptionAuditRepo.findBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndUserUserId(subscriptionType, statusValues, userId);
                for (SubscriptionAudit subscriptionAudit : subscriptionAudits) {
                    if (subscriptionAudit.getProgramSubscriptionPaymentHistory() != null && subscriptionAudit.getProgramSubscriptionPaymentHistory().getOrderManagement() != null) {
                        InstructorPayment instructorPayment = instructorPaymentRepository.findByOrderManagement(subscriptionAudit.getProgramSubscriptionPaymentHistory().getOrderManagement());
                        if (instructorPayment != null) {
                            amountSpentByMember += instructorPayment.getTotalAmt();
                        }
                    }
                }
            }
        }
        amountSpentByMember = Math.round(amountSpentByMember * 100.0) / 100.0;
        return amountSpentByMember;
    }

    /**
     * method to populate dummy subscription
     * @param programId
     * @param memberId
     */
    @Transactional
    public void populateTestSubscription(Long programId, Long memberId) {
        User currentUser = userComponents.getUser();
        boolean isAdmin = fitwiseUtils.isAdmin(currentUser);
        if (!isAdmin) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_ADMIN, MessageConstants.ERROR);
        }

        Programs program = validationService.validateProgramIdBlocked(programId);
        User user = validationService.validateMemberId(memberId);

        OrderManagement orderManagement = orderManagementRepository.findTop1ByUserAndProgramOrderByCreatedDateDesc(user, program);
        if (orderManagement != null) {
            throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_PROGRAM_ALREADY_SUBSCRIBED, null);
        }
        PlatformType platformType = validationService.validateAndGetPlatform(3L);
        SubscriptionType subscriptionType = subscriptionTypesRepo.findByNameIgnoreCase(KeyConstants.KEY_PROGRAM);

        //Populating orderManagement
        orderManagement = new OrderManagement();
        orderManagement.setOrderId("ORDFREE" + (System.currentTimeMillis() / 1000));
        orderManagement.setUser(user);
        orderManagement.setProgram(program);
        orderManagement.setModeOfPayment(KeyConstants.KEY_AUTH_NET_PAYMENT_MODE);
        orderManagement.setIsAutoRenewable(false);
        orderManagement.setSubscribedViaPlatform(platformType);
        orderManagement.setSubscriptionType(subscriptionType);
        orderManagement.setCreatedDate(new Date());
        orderManagement = orderManagementRepository.save(orderManagement);

        //Populating InvoiceManagement
        InvoiceManagement invoiceManagement = new InvoiceManagement();
        invoiceManagement.setInvoiceNumber("#FITIVFR" + (System.currentTimeMillis() / 1000));
        invoiceManagement.setOrderManagement(orderManagement);
        invoiceManagementRepository.save(invoiceManagement);

        ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), program.getProgramId());
        if (programSubscription != null) {
            SubscriptionStatus subscriptionStatus = subscriptionService.getMemberProgramSubscriptionStatus(programSubscription);
            if (subscriptionStatus != null) {
                String statusName = subscriptionStatus.getSubscriptionStatusName();
                if (statusName.equals(KeyConstants.KEY_PAID) || statusName.equals(KeyConstants.KEY_PAYMENT_PENDING) || statusName.equals(KeyConstants.KEY_EXPIRED)) {
                    throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_PROGRAM_ALREADY_SUBSCRIBED, null);
                }
            }
        }

        SubscriptionStatus subscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_PAID);
        SubscriptionPlan subscriptionPlan = subscriptionPlansRepo.findByDuration(program.getDuration().getDuration());

        //Populating programSubscription
        if (programSubscription == null) {
            programSubscription = new ProgramSubscription();
        }
        programSubscription.setUser(user);
        programSubscription.setProgram(program);
        programSubscription.setSubscribedDate(new Date());
        programSubscription.setSubscriptionPlan(subscriptionPlan);
        programSubscription.setSubscriptionStatus(subscriptionStatus);
        programSubscription.setAutoRenewal(false);
        programSubscription.setSubscribedViaPlatform(platformType);
        programSubscription = programSubscriptionRepo.save(programSubscription);

        //Populating ProgramSubscriptionPaymentHistory
        ProgramSubscriptionPaymentHistory programSubscriptionPaymentHistory = new ProgramSubscriptionPaymentHistory();
        programSubscriptionPaymentHistory.setCreatedDate(new Date());
        programSubscriptionPaymentHistory.setInstructorShare(0);
        programSubscriptionPaymentHistory.setTrainnrRevenue(0);
        programSubscriptionPaymentHistory.setTaxCharges(0);
        programSubscriptionPaymentHistory.setProgramPrice(0);
        programSubscriptionPaymentHistory.setOrderManagement(orderManagement);
        programSubscriptionPaymentHistory = subscriptionPaymentHistoryRepository.save(programSubscriptionPaymentHistory);

        //Populating SubscriptionAudit
        SubscriptionAudit subscriptionAudit = new SubscriptionAudit();
        subscriptionAudit.setUser(user);
        subscriptionAudit.setSubscriptionType(subscriptionType);
        subscriptionAudit.setProgramSubscription(programSubscription);
        subscriptionAudit.setSubscriptionPlan(subscriptionPlan);
        subscriptionAudit.setSubscriptionStatus(subscriptionStatus);
        subscriptionAudit.setSubscribedViaPlatform(platformType);
        subscriptionAudit.setSubscriptionDate(new Date());
        subscriptionAudit.setCreatedDate(new Date());
        subscriptionAudit.setAutoRenewal(false);
        subscriptionAudit.setRenewalStatus(KeyConstants.KEY_NEW);
        subscriptionAudit.setProgramSubscriptionPaymentHistory(programSubscriptionPaymentHistory);
        subscriptionAudit = subscriptionAuditRepo.save(subscriptionAudit);

        //Populating AuthNetArbSubscription
        String aNetSubscriptionId = String.valueOf(System.currentTimeMillis());
        AuthNetSubscriptionStatus authNetSubscriptionStatus = authNetSubscriptionStatusRepository.findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_CANCELLED);

        AuthNetArbSubscription authNetARBSubscription = new AuthNetArbSubscription();
        authNetARBSubscription.setUser(user);
        authNetARBSubscription.setProgram(program);
        authNetARBSubscription.setANetSubscriptionId(aNetSubscriptionId);
        authNetARBSubscription.setAuthNetSubscriptionStatus(authNetSubscriptionStatus);
        authNetARBSubscription.setSubscribedViaPlatform(platformType);
        authNetArbSubscriptionRepository.save(authNetARBSubscription);

        //Populating AuthNetPayment
        AuthNetPayment authNetPayment = new AuthNetPayment();
        authNetPayment.setOrderManagement(orderManagement);
        authNetPayment.setResponseCode("1");
        authNetPayment.setTransactionId("Promotion");
        authNetPayment.setTransactionStatus(KeyConstants.KEY_TRANSACTION_SETTLED_SUCCESSFULLY);
        authNetPayment.setReceiptNumber("#FITRNFR" + (System.currentTimeMillis() / 1000));
        authNetPayment.setIsARB(false);
        authNetPayment.setArbSubscriptionId(aNetSubscriptionId);
        authNetPayment.setIsARBUnderProcessing(null);
        authNetPayment.setRefundTransactionId(null);
        authNetPayment.setAmountPaid(0d);
        authNetPaymentRepository.save(authNetPayment);

        TestMemberSubscription testMemberSubscription = new TestMemberSubscription();
        testMemberSubscription.setMemberId(user.getUserId());
        testMemberSubscription.setProgramId(program.getProgramId());
        testMemberSubscription.setProgramSubscriptionId(programSubscription.getProgramSubscriptionId());
        testMemberSubscription.setSubscriptionAudit(subscriptionAudit.getAuditId());
        testMemberSubscription.setOrderId(orderManagement.getId());
        testMemberSubscriptionRepository.save(testMemberSubscription);
    }
    
}