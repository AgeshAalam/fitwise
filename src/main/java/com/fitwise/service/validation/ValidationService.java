package com.fitwise.service.validation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.Exercises;
import com.fitwise.entity.PlatformType;
import com.fitwise.entity.Programs;
import com.fitwise.entity.Promotions;
import com.fitwise.entity.User;
import com.fitwise.entity.UserRole;
import com.fitwise.entity.VoiceOver;
import com.fitwise.entity.Workouts;
import com.fitwise.entity.instructor.Tier;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.payments.common.RefundReasons;
import com.fitwise.entity.subscription.SubscriptionPlan;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.BlockedProgramsRepository;
import com.fitwise.repository.ExerciseRepository;
import com.fitwise.repository.PlatformTypeRepository;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.PromotionRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.repository.UserRoleRepository;
import com.fitwise.repository.VoiceOverRepository;
import com.fitwise.repository.WorkoutRepository;
import com.fitwise.repository.instructor.TierRepository;
import com.fitwise.repository.order.OrderManagementRepository;
import com.fitwise.repository.packaging.SubscriptionPackageRepository;
import com.fitwise.repository.payments.authnet.RefundReasonsRepository;
import com.fitwise.repository.subscription.SubscriptionPlansRepo;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.CalendarView;

/**
 * Class to validate the general data from the user
 */
@Service
public class ValidationService {

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public ProgramRepository programRepository;

    @Autowired
    public WorkoutRepository workoutRepository;

    @Autowired
    public ExerciseRepository exerciseRepository;

    @Autowired
    public UserRoleRepository userRoleRepository;

    @Autowired
    public SubscriptionPlansRepo subscriptionPlansRepo;

    @Autowired
    private BlockedProgramsRepository blockedProgramsRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private PlatformTypeRepository platformTypeRepository;

    @Autowired
    private OrderManagementRepository orderManagementRepository;

    @Autowired
    FitwiseUtils fitwiseUtils;

    @Autowired
    RefundReasonsRepository refundReasonsRepository;
    @Autowired
    private SubscriptionPackageRepository subscriptionPackageRepository;

    @Autowired
    private VoiceOverRepository voiceOverRepository;
    
    @Autowired
    private TierRepository tierRepository;

    private static final String[] formats = {
            "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd HH:mm:ss",
            "MM/dd/yyyy HH:mm:ss", "MM/dd/yyyy'T'HH:mm:ss.SSS'Z'",
            "MM/dd/yyyy'T'HH:mm:ss.SSSZ", "MM/dd/yyyy'T'HH:mm:ss.SSS",
            "MM/dd/yyyy'T'HH:mm:ssZ", "MM/dd/yyyy'T'HH:mm:ss",
            "yyyy:MM:dd HH:mm:ss", "yyyyMMdd", "E MMM dd HH:mm:ss Z yyyy"};
    /**
     * Method to check whether the instructor id from client is valid
     *
     * @param instructorId
     * @return
     * @throws ApplicationException
     */
    public User validateInstructorId(Long instructorId) throws ApplicationException {
        if (instructorId == null || instructorId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_INSTRUCTOR_ID_NULL, MessageConstants.ERROR);
        }
        User instructor = userRepository.findByUserId(instructorId);
        if (instructor == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_FOUND, MessageConstants.ERROR);
        }
        if (!fitwiseUtils.isInstructor(instructor)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_INSTRUCTOR, MessageConstants.ERROR);
        }
        return instructor;
    }

    /**
     * Method to check whether a program belongs to the instructor
     *
     * @param instructorId
     * @param programId
     * @return
     */
    public void validateIfProgramBelongsToInstructor(Long instructorId, Long programId) throws ApplicationException {
        if (programId == null || programId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_NULL, MessageConstants.ERROR);
        }
        if (instructorId == null || instructorId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_INSTRUCTOR_ID_NULL, MessageConstants.ERROR);
        }
        Programs program = programRepository.findByProgramIdAndOwnerUserId(programId, instructorId);
        if (program == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_INSTRUCTOR_PROGRAM_ERROR, MessageConstants.ERROR);
        }
    }

    /**
     * Validate the workout for delete workout from program
     *
     * @param userId
     * @param programId
     * @param workoutId
     */
    public void validateRemoveWorkout(Long userId, Long programId, Long workoutId) {
        validateIfProgramBelongsToInstructor(userId, programId);
        validateIfWorkoutBelongsToInstructor(userId, workoutId);
        Programs program = programRepository.findByProgramIdAndOwnerUserId(programId, userId);
        if (program.getWorkoutSchedules().size() > 0) {
            throw new ApplicationException(Constants.RESET_CONTENT, MessageConstants.MSG_ERR_PGM_WKT_SCHEDULED, null);
        }
    }

    /**
     * Method to check if a program is valid
     *
     * @param programId
     * @throws ApplicationException
     */
    public Programs validateProgramIdBlocked(Long programId) throws ApplicationException {
        if (programId == null || programId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_NULL, MessageConstants.ERROR);
        }
        if (blockedProgramsRepository.existsByProgramProgramId(programId)) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PROGRAM_BLOCKED, null);
        }
        Programs program = programRepository.findByProgramId(programId);
        if (program == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, MessageConstants.ERROR);
        }
        return program;
    }

    public Programs validateProgramId(Long programId) throws ApplicationException {
        if (programId == null || programId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_NULL, MessageConstants.ERROR);
        }
        Programs program = programRepository.findByProgramId(programId);
        if (program == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, MessageConstants.ERROR);
        }
        return program;
    }

    public SubscriptionPackage validateSubscriptionPackageId(Long subscriptionPackageId) throws ApplicationException {
        if (subscriptionPackageId == null || subscriptionPackageId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_ID_NULL, MessageConstants.ERROR);
        }
        SubscriptionPackage subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageId(subscriptionPackageId);
        if (subscriptionPackage == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_NOT_FOUND, MessageConstants.ERROR);
        }
        return subscriptionPackage;
    }

    /**
     * Method to validate workout Id
     *
     * @param workoutId
     * @throws ApplicationException
     */
    public Workouts validateWorkoutId(Long workoutId) throws ApplicationException {
        if (workoutId == null || workoutId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_ID_NULL, MessageConstants.ERROR);
        }
        Workouts workout = workoutRepository.findByWorkoutId(workoutId);
        if (workout == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_NOT_FOUND, MessageConstants.ERROR);
        }
        return workout;
    }

    /**
     * Method to check whether  the email is valid
     *
     * @param email
     * @return
     */
    public static boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

    /**
     * Method to validate workout Id
     *
     * @param exerciseId
     * @throws ApplicationException
     */
    public Exercises validateExerciseId(Long exerciseId) throws ApplicationException {
        if (exerciseId == null || exerciseId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_ID_NULL, MessageConstants.ERROR);
        }
        Exercises exercise = exerciseRepository.findByExerciseId(exerciseId);
        if (exercise == null) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, ValidationMessageConstants.MSG_EXERCISE_NOT_FOUND, MessageConstants.ERROR);
        }
        return exercise;
    }

    /**
     * Method to validate password
     * <p>
     * Constraints : Should contains 8 characters.
     * Should have lower and upper case letters.
     * Should contains at-least a number or symbol.
     *
     * @param password
     * @throws ApplicationException
     */
    public void validatePassword(String password) throws ApplicationException {
        if (!checkPasswordConstraint(password)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_INVALID_SECRET, MessageConstants.ERROR);
        }
    }

    /**
     * Method returns whether the password falls within given constraints
     * Constraints : Should contains 8 characters.
     * Should have lower and upper case letters.
     * Should contains at-least a number or symbol.
     *
     * @param password
     * @return
     */
    public static boolean checkPasswordConstraint(String password) {
        /*
         * Password length should be greater than 8 and lesser than 16!
         */
        if (password.length() >= 8 && password.length() <= 16) {
            /*
             * Pattern for lower case
             */
            Pattern lowerCase = Pattern.compile("[a-z]");
            /*
             * Pattern for upper case
             */
            Pattern upperCase = Pattern.compile("[A-Z]");
            /*
             * Pattern for number
             */
            Pattern digit = Pattern.compile("[0-9]");
            /*
             * Pattern for special characters
             */
            Pattern special = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]");
            Matcher hasLowercase = lowerCase.matcher(password);
            Matcher hasUppercase = upperCase.matcher(password);
            Matcher hasDigit = digit.matcher(password);
            Matcher hasSpecial = special.matcher(password);
            return hasUppercase.find() && hasLowercase.find() && (hasDigit.find() || hasSpecial.find());
        } else
            return false;
    }

    /**
     * Method used to check whether the user role is valid and exists in DB
     *
     * @param userRole
     * @throws ApplicationException
     */
    public UserRole validateUserRole(String userRole) throws ApplicationException {
        if (ValidationUtils.isEmptyString(userRole)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ROLE_NULL, MessageConstants.ERROR);
        }
        UserRole role = userRoleRepository.findByName(userRole);
        if (role == null) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, ValidationMessageConstants.MSG_ROLE_NOT_FOUND, MessageConstants.ERROR);
        }
        return role;
    }

    /**
     * Method used to validate date and returns the Year specified in the date
     *
     * @param date Should be in MM/dd/yyyy format
     * @return
     */
    public CalendarView validateAndGetDateMonthYearFromDate(Date date) throws ApplicationException, ParseException {
        if (date == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_INVALID_DATE, MessageConstants.ERROR);
        }
        String dateStr = date.toString();
        DateFormat formatter = new SimpleDateFormat(parse(dateStr));
        Date mDate = formatter.parse(dateStr);
        Calendar cal = Calendar.getInstance();
        cal.setTime(mDate);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DATE);
        /*
         * Getting current date to check whether the date entered by user is in future
         */
        Date currentDate = new Date();
        LocalDate localCurrentDate = currentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int currentYear = localCurrentDate.getYear();
        /*
         * Fitwise is launched on 2020. So, filtering year and if the year is less than 2020,
         * throwing error as Invalid date. Also, checking if the year is greater than current year
         */
        if (year > currentYear) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FUTURE_DATE_NOT_ALLOWED, MessageConstants.ERROR);
        }
        if (year < 2020) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_DATE_BEFORE_2020_NOT_ALLOWED, MessageConstants.ERROR);
        }
        return new CalendarView(day, month, year);
    }

    /**
     * Method to parse date format
     *
     * @param date
     * @return the format of the date
     */
    public String parse(String date) {
        if (date != null) {
            for (String parse : formats) {
                SimpleDateFormat sdf = new SimpleDateFormat(parse);
                try {
                    sdf.parse(date);
                    return parse;
                } catch (ParseException e) {

                }
            }
        }
        return "";
    }

    /**
     * Method to check whether a string contains number
     *
     * @param data - String
     * @return - boolean
     */
    public boolean isStringContainsOnlyAlphabets(String data) {
        /*
         * Pattern for only alphabets
         */
        Pattern onlyAlphabets = Pattern.compile("[a-zA-Z]");
        Matcher alphabets = onlyAlphabets.matcher(data);
        Pattern onlyNumbers = Pattern.compile("[0-9]");
        Matcher numbers = onlyNumbers.matcher(data);
        /*
         * Pattern for special characters
         */
        Pattern special = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]");
        Matcher specialCharacter = special.matcher(data);
        return alphabets.find() && !specialCharacter.find() && !numbers.find();
    }

    /**
     * Method to check if the subscription plan id is valid and returns the subscription plan object for the given Plan Id
     *
     * @param subscriptionPlanId
     * @throws ApplicationException
     */
    public SubscriptionPlan validateSubscriptionPlanId(Long subscriptionPlanId) throws ApplicationException {
        if (subscriptionPlanId == null || subscriptionPlanId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PLAN_ID_NULL, MessageConstants.ERROR);
        }
        SubscriptionPlan subscriptionPlan = subscriptionPlansRepo.findByDuration(subscriptionPlanId);
        if (subscriptionPlan == null) {
            throw new ApplicationException(Constants.NOT_EXIST_STATUS, MessageConstants.MSG_NO_SUBSCRIPTION_PLANS_FOUND, MessageConstants.ERROR);
        }
        return subscriptionPlan;
    }

    public boolean isStringContainsOnlyNumbers(String text) {
        if (text.matches("^[0-9]*$")) {
            return true;
        }
        return false;
    }

    public void validateYear(int year) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int futureYear = currentYear + 1;
        if (year < 2020 || year > futureYear) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_INVALID_YEAR_FORMAT, MessageConstants.ERROR);
        }

    }

    public void validateMonth(int month) {
        if (month < 1 || month > 12) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_INVALID_MONTH_FORMAT, MessageConstants.ERROR);
        }
    }

    /**
     * Check whether the user have member role
     *
     * @param memberId
     * @throws ApplicationException
     */
    public User validateMemberId(Long memberId) throws ApplicationException {
        if (memberId == null || memberId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_ID_NULL, MessageConstants.ERROR);
        }
        User member = userRepository.findByUserId(memberId);
        if (member == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_FOUND, MessageConstants.ERROR);
        }
        if (!fitwiseUtils.isMember(member)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_MEMBER, MessageConstants.ERROR);
        }
        return member;
    }

    /**
     * Validate image extension
     *
     * @param image
     * @return
     */
    public boolean validateImage(String image) {
        String imagePattern = "([^\\s]+(\\.(?i)(jpg|png|jpeg))$)";
        Pattern pattern = Pattern.compile(imagePattern);
        Matcher imageType = pattern.matcher(image);
        return imageType.find();
    }

    /**
     * Method to check whether a workout belongs to the instructor
     *
     * @param instructorId
     * @param workoutId
     * @return
     */
    public void validateIfWorkoutBelongsToInstructor(Long instructorId, Long workoutId) throws ApplicationException {
        if (workoutId == null || workoutId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_ID_NULL, MessageConstants.ERROR);
        }
        if (instructorId == null || instructorId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_INSTRUCTOR_ID_NULL, MessageConstants.ERROR);
        }
        List<Workouts> workouts = workoutRepository.findByWorkoutIdAndOwnerUserId(workoutId, instructorId);
        if (workouts.isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_INSTRUCTOR_WORKOUT_ERROR, MessageConstants.ERROR);
        }
    }

    /**
     * To validate year
     *
     * @param year
     */
    public void validateYear(String year) {
        if (!year.matches("^(19|20)\\d\\d$")) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_YEAR_INVALID, MessageConstants.ERROR);
        }
    }

    /**
     * To validate month
     *
     * @param month
     */
    public void validateMonth(String month) {
        if (!month.matches("^0[1-9]|1[012]$")) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_MONTH_INVALID, MessageConstants.ERROR);
        }
    }

    /**
     * Validate and get platform type
     * @param platformId platform type id
     * @return platformType
     * @throws ApplicationException exception
     */
    public PlatformType validateAndGetPlatform(final Long platformId) throws ApplicationException {
        if (platformId == null || platformId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_DEVICE_PLATFORM_EMPTY, MessageConstants.ERROR);
        }
        PlatformType platformType = platformTypeRepository.findByPlatformTypeId(platformId);
        if (platformType == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_NO_PLATFORMS_FOUND, MessageConstants.ERROR);
        }
        return platformType;
    }

    /**
     * To check whether a failed order id is valid
     *
     * @param orderId
     * @return
     */
    public OrderManagement isValidOrder(String orderId) {
        OrderManagement orderManagement = orderManagementRepository.findTop1ByOrderId(orderId);
        if (orderManagement == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_INCORRECT_ORDER_ID, MessageConstants.ERROR);
        }
        Calendar c = Calendar.getInstance(); // starts with today's date and time
        c.add(Calendar.DAY_OF_YEAR, 2);  // advances day by 2
        Date dayAfterTwoDays = c.getTime();
        // User can re-try purchasing a failed order only within two days. Else return error
        if (orderManagement.getCreatedDate().after(dayAfterTwoDays)) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_REORDER_TIME_PERIOD_VALIDATION, MessageConstants.ERROR);
        }
        return orderManagement;
    }

    /**
     * Used to validate whether facebook user profile id.
     *
     * @param fbUserProfileId
     * @return
     */
    public String isValidFbUserProfileId(String fbUserProfileId) {
        if (fbUserProfileId.isEmpty() || Long.parseLong(fbUserProfileId) == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_INCORRECT_FACEBOOK_USER_PROFILE_ID, MessageConstants.ERROR);
        }
        return fbUserProfileId;
    }

    /**
     * Used to validate and return refund reason
     *
     * @param id
     * @return
     */
    public RefundReasons validateRefundReason(Long id) {
        if (id == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_WRONG_REFUND_REASON_ID, null);
        }
        return refundReasonsRepository.findByRefundReasonId(id);
    }

    /* Returns the validated date from date String
     *
     * @param dateString
     * @return
     */
    public Date validateAndConstructDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(StringConstants.PATTERN_DATE);
        dateFormat.setLenient(false);
        String userTimeZone = fitwiseUtils.getUserTimeZone();
        if (userTimeZone != null) {
            TimeZone timeZone = TimeZone.getTimeZone(userTimeZone);
            dateFormat.setTimeZone(timeZone);
        }
        Date date;
        try {
            date = dateFormat.parse(dateString);
        } catch (ParseException exception) {
            throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, ValidationMessageConstants.MSG_INVALID_DATE, null);
        }
        if (date.after(new Date())) {
            throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, ValidationMessageConstants.MSG_FUTURE_DATE_NOT_ALLOWED, null);
        }
        return date;
    }

    @Transactional
    public OrderManagement validateOrderId(String orderId) {
        OrderManagement orderManagement = orderManagementRepository.findTop1ByOrderId(orderId);
        if (orderManagement == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_INCORRECT_ORDER_ID, MessageConstants.ERROR);
        }
        return orderManagement;
    }

    /**
     * Validate audio extension
     * @param filePath
     * @return
     */
    public boolean validateAudio(String filePath) {
        String audioPattern = "([^\\s]+(\\.(?i)(mp3|wav|mp4))$)";
        Pattern pattern = Pattern.compile(audioPattern);
        Matcher audioType = pattern.matcher(filePath);
        return audioType.find();
    }

    /**
     * Method to check whether a voice over belongs to the instructor
     *
     * @param instructorId
     * @param voiceOverId
     * @return
     */
    public void validateIfVoiceOverBelongsToInstructor(Long instructorId, Long voiceOverId)
    {
        if(instructorId == null || instructorId == 0)
        {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_INSTRUCTOR_ID_NULL, MessageConstants.ERROR);
        }
        if(voiceOverId == null || voiceOverId == 0)
        {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VOICE_OVER_ID_NULL, MessageConstants.ERROR);
        }
        VoiceOver voiceOver = voiceOverRepository.findByVoiceOverIdAndUserUserId(voiceOverId, instructorId);
        if(voiceOver == null)
        {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_WRONG_INSTRUCTOR_VOICE_OVER_ID, MessageConstants.ERROR);
        }
    }

    /**
     * Validate tier id and get tier data
     * @param tierId tier id
     * @return tier
     * @throws ApplicationException exception
     */
	public Tier validateAndGetTier(final Long tierId) throws ApplicationException {
		if (tierId == null || tierId == 0) {
			throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_TIER_ID_NULL,
					MessageConstants.ERROR);
		}
		Tier tier = tierRepository.findByTierId(tierId);
		if (tier == null) {
			throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_TIER_NOT_FOUND,
					MessageConstants.ERROR);
		} else if (!tier.getIsActive()){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ERR_TIER_NOT_AVAILABLE,
                    MessageConstants.ERROR);
        }
		return tier;
	}

}
