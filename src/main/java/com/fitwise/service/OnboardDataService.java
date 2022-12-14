package com.fitwise.service;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.AppConfigKeyValue;
import com.fitwise.entity.Duration;
import com.fitwise.entity.Equipments;
import com.fitwise.entity.ExpertiseLevels;
import com.fitwise.entity.ProgramTypes;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.UserRole;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.AppConfigKeyValueRepository;
import com.fitwise.repository.DurationRepo;
import com.fitwise.repository.EquipmentsRepository;
import com.fitwise.repository.ExpertiseLevelRepository;
import com.fitwise.repository.ProgramTypeRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.request.UserDeviceDetails;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.AppConfigResponseView;
import com.fitwise.view.ResponseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * The Class OnboardDataService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardDataService {

    private static final Set<String> TIMEZONES = new HashSet<>(Arrays.asList(TimeZone.getAvailableIDs()));

    private final ProgramTypeRepository programTypeRepository;

    private final ExpertiseLevelRepository expertiseLevelRepository;

    private final DurationRepo durationRepo;

    @Autowired
    private EquipmentsRepository equipmentsRepository;

    @Autowired
    private UserComponents userComponents;

    /**
     * The validationService
     */
    @Autowired
    private ValidationService validationService;

    @Autowired
    private AppUpdateService appUpdateService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private RedisService redisService;

    private final FitwiseUtils fitwiseUtils;
    private final AppConfigKeyValueRepository appConfigKeyValueRepository;

    /**
     * Gets the program plan data.
     *
     * @return the program plan data
     */
    public Map<String, Object> getProgramPlanData() {
        log.info("Get program plan data starts.");
        long apiStartTimeMillis = new Date().getTime();

        fitwiseUtils.validateCurrentInstructorBlocked();
        Map<String, Object> planData = new HashMap<>();
        List<ProgramTypes> programTypes = programTypeRepository.findByOrderByProgramTypeNameAsc();
        if (programTypes.isEmpty()) {
            programTypes = new ArrayList<>();
        }
        List<ExpertiseLevels> expertiseLevels = expertiseLevelRepository.findAll();
        log.info("Query to get program types and expertise levels : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        if(userComponents.getRole().equalsIgnoreCase(KeyConstants.KEY_MEMBER)){
            expertiseLevels.removeIf(expertiseLevels1 -> expertiseLevels1.getExpertiseLevel().equalsIgnoreCase(KeyConstants.KEY_ALL_LEVELS));
        }
        if (expertiseLevels.isEmpty()) {
            expertiseLevels = new ArrayList<>();
        }
        List<Duration> durations = durationRepo.findAllByOrderByDurationAsc();
        if (durations.isEmpty()) {
            durations = new ArrayList<>();
        }
        planData.put(KeyConstants.KEY_PROGRAM_TYPES, programTypes);
        planData.put(KeyConstants.KEY_EXPERTISE_LEVELS, expertiseLevels);
        planData.put(KeyConstants.KEY_DURATIONS, durations);
        log.info("Query to get duration and response construction : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get program plan data ends.");
        return planData;
    }

    public List<Equipments> getEquipments() {
        List<Equipments> equipments = equipmentsRepository.findAll();
        if (equipments.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        return equipments;
    }

    /**
     * Method used to retrieve the app config data of the user!
     *
     * @param deviceDetails
     * @return
     */
    public ResponseModel getAppConfigData(UserDeviceDetails deviceDetails) {
        log.info("Get app config data starts");
        long start = new Date().getTime();
        long profilingStart;
        /*
         * Validating and getting User role
         */
        UserRole userRole = validationService.validateUserRole(deviceDetails.getRole());
        /*
         * User activeness data is captured here and saved in database
         */
        User user = userComponents.getAndValidateUser();
        log.info("Basic validation : Time taken in millis : "+(new Date().getTime() - start));
        profilingStart = new Date().getTime();
        /*
         * Check whether the user has given the contact number and return the result in response
         * This will be used to show anonymous ticket warning of ZenDesk in App side
         */
        AppConfigResponseView response = appUpdateService.checkAppUpdateService(deviceDetails, user, userRole.getName());
        log.info("Check app update service : Time taken in millis : " + (new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();
        UserProfile userProfile = new UserProfile();
        if(user != null){
            userProfile = userProfileRepository.findByUser(user);
            response.setOnboardingCompleted(true);
        }
        if (userProfile != null && userProfile.getContactNumber() != null && !userProfile.getContactNumber().isEmpty()) {
            response.setUserPhoneNumberExists(true);
        }
        //Set user Time Zone In AuthToken
        if (user != null) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String token = request.getHeader(Constants.X_AUTHORIZATION);
            setTimeZoneForUser(deviceDetails.getTimeZone(), token);
            log.info("Set user time zone in auth token : Time taken in millis : " + (new Date().getTime() - profilingStart));
        }
        log.info("Get app config data : Total Time taken in millis : "+(new Date().getTime() - start));
        log.info("Get app config data ends");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_APP_CONFIF_DATA_RETRIEVED, response);
    }

    /**
     * Setting time zone of user in cache
     * @param timeZone
     */
    private void setTimeZoneForUser(String timeZone, String token) {
        if (!ValidationUtils.isEmptyString(timeZone)) {
            if (!TIMEZONES.contains(timeZone)) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_TIME_ZONE_INVALID, MessageConstants.ERROR);
            }
            redisService.set(token + "_" + KeyConstants.KEY_TIME_ZONE, timeZone);
        }
    }

    /**
     * Get information video in vimeo
     * @return vimeo video id map
     */
    public Map<String, String> getInfoVideos(){
        Map<String, String> responseMap = new HashMap<>();
        AppConfigKeyValue introVideoVimeo = appConfigKeyValueRepository.findByKeyString(Constants.INTRO_VIDEO_VIMEO);
        AppConfigKeyValue transitionVideoVimeo = appConfigKeyValueRepository.findByKeyString(Constants.TRANSITION_VIDEO_VIMEO);
        try {
            responseMap.put("introVideoVimeoId", introVideoVimeo.getValueString());
            responseMap.put("transitionVideoVimeoId", transitionVideoVimeo.getValueString());
        }catch (Exception exception){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        return responseMap;
    }

}