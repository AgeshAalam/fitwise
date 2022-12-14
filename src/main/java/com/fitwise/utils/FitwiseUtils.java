package com.fitwise.utils;

/*
 * Created by Vignesh G on 31/03/20
 */

import com.fitwise.components.UserComponents;
import com.fitwise.constants.AppConfigConstants;
import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SecurityFilterConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.constants.VideoUploadStatus;
import com.fitwise.entity.AppConfigKeyValue;
import com.fitwise.entity.ChatConversation;
import com.fitwise.entity.Circuit;
import com.fitwise.entity.Exercises;
import com.fitwise.entity.Programs;
import com.fitwise.entity.TimeSpan;
import com.fitwise.entity.User;
import com.fitwise.entity.UserActiveInactiveTracker;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.UserRole;
import com.fitwise.entity.VideoManagement;
import com.fitwise.entity.WorkoutSchedule;
import com.fitwise.entity.Workouts;
import com.fitwise.exception.ApplicationException;
import com.fitwise.model.packaging.CancellationDurationModel;
import com.fitwise.repository.AppConfigKeyValueRepository;
import com.fitwise.repository.BlockedUserRepository;
import com.fitwise.repository.UserActiveInactiveTrackerRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.messaging.ChatConversationRepository;
import com.fitwise.service.payment.stripe.StripeTierService;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.specifications.jpa.ProgramRatingJPA;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class FitwiseUtils {

    @Autowired
    private UserComponents userComponents;

    @Autowired
    private ValidationService validationService;

    @Autowired
    UserActiveInactiveTrackerRepository userActiveInactiveTrackerRepository;

    @Autowired
    private ChatConversationRepository chatConversationRepository;

    private final ProgramRatingJPA programRatingJPA;

    @Autowired
    private BlockedUserRepository blockedUserRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
	private AppConfigKeyValueRepository appConfigKeyValueRepository;
    /**
     * Check If user if active. User is active if he has used application in the past 5 days.
     *
     * @param user
     * @param role
     * @return
     */
    public boolean isUserActive(User user, UserRole role) {
        boolean isActive = false;
        List<UserActiveInactiveTracker> userActiveInactiveTracker = userActiveInactiveTrackerRepository.findByUserUserIdAndUserRoleRoleIdOrderByIdDesc(user.getUserId(), role.getRoleId());
        if (!userActiveInactiveTracker.isEmpty()) {
            isActive = userActiveInactiveTracker.get(0).isActive();
        }
        return isActive;
    }

    /**
     * Get user's last active date
     * @param user
     * @param role
     * @return
     */
    public Date getLastActiveDate(User user, UserRole role) {
        Date lastActiveDate = null;
        List<UserActiveInactiveTracker> userActiveInactiveTracker = userActiveInactiveTrackerRepository.findByUserUserIdAndUserRoleRoleIdOrderByIdDesc(user.getUserId(), role.getRoleId());
        if (!userActiveInactiveTracker.isEmpty()) {
            lastActiveDate = userActiveInactiveTracker.get(0).getModifiedDate();
        }
        return lastActiveDate;
    }

    public ChatConversation validateChatConversation(Long conversationId) {
        if (conversationId == null || conversationId.equals(0L)) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_CONVERSATION_ID_NULL, MessageConstants.ERROR);
        }

        User user = userComponents.getUser();
        UserRole userRole = validationService.validateUserRole(userComponents.getRole());

        ChatConversation chatConversation = null;
        if (userRole.getName().equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR)) {
            chatConversation = chatConversationRepository.findByConversationIdAndPrimaryUserUserUserId(conversationId, user.getUserId());
        } else {
            chatConversation = chatConversationRepository.findByConversationIdAndSecondaryUserUserUserId(conversationId, user.getUserId());
        }

        if (chatConversation == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_CONVERSATION_NOT_FOUND, MessageConstants.ERROR);
        }

        return chatConversation;
    }

    /**
     * @param programId
     * @return
     */
    public BigDecimal getProgramRating(long programId) {

        Double programRating = programRatingJPA.getAverageProgramRating(programId);

        double rating = programRating != null ? programRating : 0.0;
        BigDecimal overAllRating = new BigDecimal(rating).setScale(2, RoundingMode.HALF_UP);

        return overAllRating;
    }

    public Workouts makeWorkoutAnonymous(Workouts workout) {

        workout.setTitle(KeyConstants.KEY_ANONYMOUS);
        workout.setDescription(KeyConstants.KEY_ANONYMOUS);
        workout.setImage(null);
        workout.setFlag(false);
        workout.setOwner(null);
        workout.setExerciseScheduler(null);
        workout.setCircuitSchedules(null);

        return workout;

    }

    public Exercises makeExerciseAnonymous(Exercises exercise) {

        exercise.setTitle(KeyConstants.KEY_ANONYMOUS);
        exercise.setDescription(KeyConstants.KEY_ANONYMOUS);
        exercise.setFlag(false);
        exercise.setVideoManagement(null);
        exercise.setLoopCount(null);
        exercise.setOwner(null);
        exercise.setEquipments(null);
        exercise.setSupportVideoManagement(null);

        return exercise;

    }

    public Circuit makeCircuitAnonymous(Circuit circuit) {

        circuit.setTitle(KeyConstants.KEY_ANONYMOUS);
        circuit.setOwner(null);
        circuit.setExerciseSchedules(null);

        return circuit;
    }

    /**
     * Defalut number of trial workouts in a program
     *
     * @return
     */
    public int getDefaultTrialWorkoutsCount() {
        return 2;
    }

    /**
     * How many trial workouts for a user in a program. In future, logic will be included for coupons and offers
     *
     * @param user
     * @param program
     * @return
     */
    public int getTrialWorkoutsCountForProgram(User user, Programs program) {
        return getDefaultTrialWorkoutsCount();
    }

    /**
     * To get number of workout schedules available for trial subscription in a program
     *
     * @param user
     * @param program
     * @return
     */
    public int getTrialWorkoutScheduleCountForProgram(User user, Programs program) {
        int trialWorkouts = getTrialWorkoutsCountForProgram(user, program);
        List<WorkoutSchedule> workoutSchedules = program.getWorkoutSchedules();
        workoutSchedules.sort(Comparator.comparing(WorkoutSchedule::getOrder));
        int trialWorkoutCount = 0;
        int trialWorkoutScheduleCount = 0;
        for (WorkoutSchedule workoutSchedule : workoutSchedules) {
            trialWorkoutScheduleCount++;
            if (!workoutSchedule.isRestDay()) {
                trialWorkoutCount++;
            }
            if (trialWorkoutCount == trialWorkouts) {
                break;
            }
        }
        return trialWorkoutScheduleCount;
    }

    public boolean isMember(User user) {
        boolean isMember = false;
        Optional<UserRole> userRole = AppUtils.getUserRoles(user).stream().filter(role -> role.getName().equalsIgnoreCase(KeyConstants.KEY_MEMBER)).findAny();
        if (userRole.isPresent()) {
            isMember = true;
        }
        return isMember;
    }

    public boolean isInstructor(User user) {
        boolean isInstructor = false;
        Optional<UserRole> userRole = AppUtils.getUserRoles(user).stream().filter(role -> role.getName().equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR)).findAny();
        if (userRole.isPresent()) {
            isInstructor = true;
        }
        return isInstructor;
    }

    public boolean isAdmin(User user) {
        boolean isAdmin = false;
        Optional<UserRole> userRole = AppUtils.getUserRoles(user).stream().filter(role -> role.getName().equalsIgnoreCase(KeyConstants.KEY_ADMIN)).findAny();
        if (userRole.isPresent()) {
            isAdmin = true;
        }
        return isAdmin;
    }

    public void validateCurrentInstructorBlocked() {
        User user = userComponents.getUser();
        if (blockedUserRepository.existsByUserUserIdAndUserRoleName(user.getUserId(), KeyConstants.KEY_INSTRUCTOR)) {
            throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_CURRENT_USER_BLOCKED, null);
        }
    }

    /**
     * @return
     */
    public boolean isCurrentMemberBlocked() {
        boolean isMemberBlocked = false;
        User user = userComponents.getUser();
        if (blockedUserRepository.existsByUserUserIdAndUserRoleName(user.getUserId(), KeyConstants.KEY_MEMBER)) {
            isMemberBlocked = true;
        }
        return isMemberBlocked;
    }

    /**
     * Find the user is blocked or not
     * @param user
     * @return
     */
    public boolean isUserBlocked(User user) {
        boolean isMemberBlocked = false;
        if (blockedUserRepository.existsByUserUserIdAndUserRoleName(user.getUserId(), KeyConstants.KEY_MEMBER)) {
            isMemberBlocked = true;
        }
        return isMemberBlocked;
    }

    public boolean isVideoProcessingPending(VideoManagement videoManagement) {
        boolean isVideoProcessingPending = false;
        if (videoManagement != null) {
            List<String> statusList = Arrays.asList(new String[]{VideoUploadStatus.COMPLETED, VideoUploadStatus.REUPLOAD, VideoUploadStatus.REUPLOAD_INPROGRESS,
                     VideoUploadStatus.VIMEO_REUPLOAD_PROCESSING_FAILED});
            isVideoProcessingPending = statusList.stream().noneMatch(videoManagement.getUploadStatus()::equalsIgnoreCase);
        }
        return isVideoProcessingPending;
    }

    /**
     * @param user
     * @return
     */
    public String getUserFullName(User user) {
        String fullName = null;
        if (user != null) {
            UserProfile userProfile = userProfileRepository.findByUser(user);
            fullName = getUserFullName(userProfile);
        }
        return fullName;
    }

    /**
     * @param userProfile
     * @return
     */
    public String getUserFullName(UserProfile userProfile) {
        String fullName = null;
        if (userProfile != null) {
            if (!ValidationUtils.isEmptyString(userProfile.getFirstName())) {
                fullName = userProfile.getFirstName();
            }
            if (!ValidationUtils.isEmptyString(userProfile.getLastName())) {
                fullName += " " + userProfile.getLastName();
            }
        }
        return fullName;
    }

    /**
     * Util to check if a two local dates are the same day
     *
     * @param date1
     * @param date2
     * @return
     */
    public boolean isSameDay(Date date1, Date date2) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String token = request.getHeader(Constants.X_AUTHORIZATION);

        return isSameDay(date1, date2, token);
    }

    /**
     * Util to check if a two local dates are the same day based on time zone taken from token
     *
     * @param date1
     * @param date2
     * @param token
     * @return
     */
    public boolean isSameDay(Date date1, Date date2, String token) {
        String timeZoneName = userComponents.getTimeZone(token);
        return isSameDayInTimeZone(date1, date2, timeZoneName);
    }

    /**
     * Util to check if a two local dates are the same day based on time zone
     * @param date1
     * @param date2
     * @param timeZoneName
     * @return
     */
    public boolean isSameDayInTimeZone(Date date1, Date date2, String timeZoneName) {
        TimeZone timeZone = null;
        if (timeZoneName != null) {
            timeZone = TimeZone.getTimeZone(timeZoneName);
        }

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        if (timeZone != null) {
            cal1.setTimeZone(timeZone);
        }

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        if (timeZone != null) {
            cal2.setTimeZone(timeZone);
        }

        return DateUtils.isSameDay(cal1, cal2);
    }

    public boolean isValidTimeZone(String timeZone) {
        return Arrays.asList(TimeZone.getAvailableIDs()).contains(timeZone);
    }

    public String getUserTimeZone() {
        String token = null;
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            token = request.getHeader(Constants.X_AUTHORIZATION);
        } catch (Exception e) {
            log.warn("Exception occurred in FitwiseUtils.getUserTimeZone() : " + e.getMessage());
        }
        String timeZoneName = userComponents.getTimeZone(token);
        return timeZoneName;
    }

    /**
     * Returns the Date from string.
     * @param dateString
     * @return
     */
    public Date constructDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(StringConstants.PATTERN_DATE);
        dateFormat.setLenient(false);
        String userTimeZone = getUserTimeZone();

        if (userTimeZone != null) {
            TimeZone timeZone = TimeZone.getTimeZone(userTimeZone);
            dateFormat.setTimeZone(timeZone);
        }

        Date date = null;
        try {
            date = dateFormat.parse(dateString);
        } catch (ParseException exception) {
            throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, ValidationMessageConstants.MSG_INVALID_DATE, null);
        }
        return date;
    }

    public Date constructDate(String dateString, String timezoneString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(StringConstants.PATTERN_DATE);
        dateFormat.setLenient(false);

        if (!ValidationUtils.isEmptyString(timezoneString)) {
            TimeZone timeZone = TimeZone.getTimeZone(timezoneString);
            dateFormat.setTimeZone(timeZone);
        }

        Date date = null;
        try {
            date = dateFormat.parse(dateString);
        } catch (ParseException exception) {
            throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, ValidationMessageConstants.MSG_INVALID_DATE, null);
        }
        return date;
    }

    public String formatDate(Date date, String timeZoneName) {
        String formattedDate = null;
        if (date != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(StringConstants.PATTERN_DATE);
            if (timeZoneName != null) {
                TimeZone timeZone = TimeZone.getTimeZone(timeZoneName);
                simpleDateFormat.setTimeZone(timeZone);
            }
            formattedDate = simpleDateFormat.format(date);
        }
        return formattedDate;
    }

    public String formatDate(Date date) {
        String formattedDate = null;
        if (date != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(StringConstants.PATTERN_DATE);
            if (getUserTimeZone() != null) {
                TimeZone timeZone = TimeZone.getTimeZone(getUserTimeZone());
                simpleDateFormat.setTimeZone(timeZone);
            }
            formattedDate = simpleDateFormat.format(date);
        }
        return formattedDate;
    }

    public String formatDateWithTime(Date date) {
        String formattedDate = null;
        if (date != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            if (getUserTimeZone() != null) {
                TimeZone timeZone = TimeZone.getTimeZone(getUserTimeZone());
                simpleDateFormat.setTimeZone(timeZone);
            }
            formattedDate = simpleDateFormat.format(date);
        }
        return formattedDate;
    }
    
    public Date getFitwiseLaunchDate() {
    	Date launchDate=null;
    	try {
    		AppConfigKeyValue appConfig= appConfigKeyValueRepository.findByKeyString(Constants.FITWISE_LAUNCH_DATE);
            String date=appConfig.getValueString();
            try {               
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.s");
                String dateStr=sdf2.format(sdf.parse(date));
                launchDate=sdf2.parse(dateStr);
                
            } catch (ParseException e) {
                e.printStackTrace();
            }
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	return launchDate;
    }

    /**
     * Get the welcome message for the user by user role
     * @param role
     * @return
     */
    public String getWelcomeMessage(final String role){
        String message = MessageConstants.MSG_DEFAULT_MESSAGE;
        AppConfigKeyValue appConfig = null;
        if(role.equalsIgnoreCase(SecurityFilterConstants.ROLE_INSTRUCTOR)){
            appConfig = appConfigKeyValueRepository.findByKeyString(AppConfigConstants.MSG_INSTRUCTOR_WELCOME);
        }else if(role.equalsIgnoreCase(SecurityFilterConstants.ROLE_MEMBER)){
            appConfig = appConfigKeyValueRepository.findByKeyString(AppConfigConstants.MSG_MEMBER_WELCOME);
        }
        if(appConfig != null){
            message = appConfig.getValueString();
        }
        return message;
    }

    /**
     * Get the formatted price
     * @param price
     * @return
     */
    public String formatPrice(double price)
    {
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        return KeyConstants.KEY_CURRENCY_US_DOLLAR+decimalFormat.format(price);
    }

    public Date convertToUserTimeZone(Date date) {
        Date convertedDate = null;
        if (getUserTimeZone() != null) {
            String dt = formatDateWithTime(date);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            dateFormat.setLenient(false);
            try {
                return dateFormat.parse(dt);
            } catch (ParseException e) {
                throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, ValidationMessageConstants.MSG_INVALID_DATE, null);
            }
        }
        return convertedDate;
    }

    public long getNumberOfDaysBetweenTwoDates(Date date1, Date date2){
        long difference = date2.getTime() - date1.getTime();
        long differenceInDays = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS);
        return  differenceInDays;
    }

    public long getNumberOfHoursBetweenTwoDates(Date date1, Date date2){
        long difference = date2.getTime() - date1.getTime();
        long differenceInDays = TimeUnit.HOURS.convert(difference, TimeUnit.MILLISECONDS);
        return  differenceInDays;
    }

    /**
     * Get header for file to download
     * @param filename
     * @return
     */
    public HttpHeaders getHttpDownloadHeaders(String filename) {
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(MediaType.parseMediaType("application/csv"));
        respHeaders.setContentDispositionFormData("attachment", filename);
        return respHeaders;
    }

    /**
     * Construct the name with proper convention
     * @param firstName First name
     * @param lastName Last name
     * @return Valid name
     */
    public String constructUserName(final String firstName, final String lastName){
        String name = "";
        if (firstName != null && lastName != null) {
            name = firstName + KeyConstants.KEY_SPACE + lastName;
        } else if (firstName != null) {
            name = firstName;
        } else if (lastName != null) {
            name = lastName;
        }
        return name;
    }

    /**
     * Construct cancellation duration model
     * @param cancellationDuration Cancellation duration
     * @return CancellationDurationModel
     */
    public CancellationDurationModel constructCancellationDurationModel(final TimeSpan cancellationDuration){
        CancellationDurationModel cancellationDurationModel = null;
        if (cancellationDuration != null) {
            cancellationDurationModel = new CancellationDurationModel();
            boolean isDays = false;
            if (cancellationDuration.getDays() != null) {
                isDays = true;
                cancellationDurationModel.setDays(cancellationDuration.getDays());
            } else {
                cancellationDurationModel.setHours(cancellationDuration.getHours());
            }
            cancellationDurationModel.setIsDays(isDays);
        }
        return cancellationDurationModel;
    }
    
	/**
	 * Convert date string to date.
	 *
	 * @param dateStr the date str
	 * @return the date
	 * @throws ParseException the parse exception
	 */
	public Date convertDateStringToDate(String dateStr) throws ParseException {
		Date formattedDate = null;
		if (dateStr != null && !dateStr.isEmpty()) {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(StringConstants.PATTERN_DATE_YYYY_MM_DD);
			formattedDate = simpleDateFormat.parse(dateStr);
		}
		return formattedDate;
	}
	
	/**
	 * Construct date without time zone.
	 *
	 * @param dateString the date string
	 * @return the date
	 */
	public Date constructDateWithoutTimeZone(String dateString) {
		Date date = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat(StringConstants.PATTERN_DATE);
		dateFormat.setLenient(false);
		try {
			date = dateFormat.parse(dateString);
		} catch (ParseException exception) {
			throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, ValidationMessageConstants.MSG_INVALID_DATE,
					null);
		}
		return date;
	}

}
