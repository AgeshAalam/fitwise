package com.fitwise.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitwise.components.UserComponents;
import com.fitwise.constants.CalendarConstants;
import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.InstructorUnavailability;
import com.fitwise.entity.Programs;
import com.fitwise.entity.UnavailableTimes;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.calendar.CronofyAvailabilityRules;
import com.fitwise.entity.calendar.UserKloudlessAccount;
import com.fitwise.entity.calendar.UserKloudlessCalendar;
import com.fitwise.entity.calendar.UserKloudlessMeeting;
import com.fitwise.entity.calendar.UserKloudlessSchedule;
import com.fitwise.entity.packaging.PackageKloudlessMapping;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.exception.ApplicationException;
import com.fitwise.model.InstructorUnavailabilityRequestModel;
import com.fitwise.repository.InstructorUnavailabilityRepository;
import com.fitwise.repository.UnavailableTimeRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.calendar.CronofyAvailabilityRulesRepository;
import com.fitwise.repository.calendar.UserKloudlessCalendarRepository;
import com.fitwise.repository.calendar.UserKloudlessMeetingRepository;
import com.fitwise.repository.calendar.UserKloudlessScheduleRepository;
import com.fitwise.repository.packaging.PackageKloudlessMappingRepository;
import com.fitwise.repository.packaging.SubscriptionPackageRepository;
import com.fitwise.response.kloudless.Email;
import com.fitwise.response.kloudless.Event;
import com.fitwise.response.kloudless.Reminder;
import com.fitwise.service.calendar.CalendarService;
import com.fitwise.service.calendar.InstructorCalendarService;
import com.fitwise.service.calendar.KloudLessService;
import com.fitwise.service.calendar.SchedulePayload;
import com.fitwise.service.cronofy.CronofyService;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.InstructorUnavailabilityMemberView;
import com.fitwise.view.InstructorUnavailabilityResponseView;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.cronofy.CreateRealtimeScheduleResponse;
import com.fitwise.view.cronofy.CronofyMeetingModel;
import com.fitwise.view.cronofy.CronofyschedulePayload;
import com.fitwise.view.cronofy.InstructorUnavailabilityschedulepayload;
import com.fitwise.view.cronofy.RealtimeScheduleResponse;
import com.fitwise.view.cronofy.RealtimeScheduleavailability;
import com.fitwise.view.cronofy.RealtimeSchedulebuffer;
import com.fitwise.view.cronofy.RealtimeSchedulebufferafter;
import com.fitwise.view.cronofy.RealtimeSchedulebufferbefore;
import com.fitwise.view.cronofy.RealtimeScheduleevent;
import com.fitwise.view.cronofy.RealtimeSchedulemembers;
import com.fitwise.view.cronofy.RealtimeScheduleoauth;
import com.fitwise.view.cronofy.RealtimeScheduleparticipants;
import com.fitwise.view.cronofy.RealtimeSchedulequeryperiods;
import com.fitwise.view.cronofy.RealtimeScheduleredirecturls;
import com.fitwise.view.cronofy.RealtimeSchedulerequiredduration;
import com.fitwise.view.cronofy.RealtimeSchedulestartinterval;
import com.fitwise.view.cronofy.RealtimeScheduletargetcalendars;
import com.fitwise.view.cronofy.RefreshAccessTokenResponse;
import com.fitwise.view.cronofy.SchedulePayloadattachments;
import com.fitwise.view.cronofy.SchedulePayloadattendees;
import com.fitwise.view.cronofy.SchedulePayloadcreator;
import com.fitwise.view.cronofy.SchedulePayloadcustomproperties;
import com.fitwise.view.cronofy.SchedulePayloadorganizer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kloudless.models.Resource;
import com.kloudless.models.ResponseRaw;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

@Service
@Slf4j
public class InstructorUnavailabilityService {

    @Autowired
    InstructorUnavailabilityRepository instructorUnavailabilityRepository;

    @Autowired
    private UserComponents userComponents;

    @Autowired
    private FitwiseUtils fitwiseUtils;

    @Autowired
    private UnavailableTimeRepository unavailableTimeRepository;

    @Autowired
    private KloudLessService kloudLessService;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private InstructorCalendarService instructorCalendarService;

    @Autowired
    private UserKloudlessCalendarRepository userKloudlessCalendarRepository;
    
    @Autowired
	private UserKloudlessScheduleRepository userKloudlessScheduleRepository;
    
    @Autowired
	private CronofyService cronofyService;
    
    @Autowired
	private CronofyAvailabilityRulesRepository cronofyAvailabilityRulesRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
	private UserKloudlessMeetingRepository userKloudlessMeetingRepository;
    
    @Autowired
    private SubscriptionPackageRepository subscriptionPackageRepository;
    
    @Autowired
	private PackageKloudlessMappingRepository packageKloudlessMappingRepository;
    
    public static final String REDIRECT_URI = "https://stg-cln-instructor.trainnr.com/app/calendar";
    /**
     * API to save instructor unavailability
     *
     * @parm requestModel
     * @return
     */
    public ResponseModel saveInstructorUnavailability(InstructorUnavailabilityRequestModel requestModel) {
       User user = userComponents.getUser();

        //check whether the current user is instructor or not
        if (!fitwiseUtils.isInstructor(user)){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_INSTRUCTOR, null);
        }

        boolean isNewUnavailability = false;
        Long ExistingInstructorUnavailabilityId = (long) 0;

        //Validating the time periods
        Date startDate = new Date(requestModel.getStartDate());
        Date endDate = new Date(requestModel.getEndDate());
        Date now = new Date();
        if (startDate.before(now)){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PAST_DATE_NOT_ALLOWED, null);
        }
        if (startDate.after(endDate)){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_START_DATE_AFTER_END_DATE, null);
        }
        if (startDate.equals(endDate)){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SAME_END_DATE_SAME, null);
        }
        
         Date startDateSchedule = new Date(requestModel.getStartDate());
    	
	     Date endDateSchedule = new Date(requestModel.getEndDate());
	     
	     Date scheduleDate = getTimeInUTC(startDateSchedule);
         
   	     Date endDateInUserTimezone = getTimeInUTC(endDateSchedule);
   	     
	   	  List<Long> scheduleIdsList = instructorCalendarService.getInstructorSchedules(requestModel.getStartDate(), requestModel.getEndDate());
	
	      if (scheduleIdsList != null && !scheduleIdsList.isEmpty()) {
	
	          //Deleting user schedules
	          instructorCalendarService.deleteSchedulesFromInstructor(scheduleIdsList);
	      }

        try{

            UserKloudlessAccount userKloudlessAccount = calendarService.getActiveAccount(user);
            UserKloudlessCalendar userKloudlessCalendar = calendarService.getActiveCalendarFromKloudlessAccount(userKloudlessAccount);

            InstructorUnavailability instructorUnavailability;
           
            if (requestModel.getInstructorUnavailabilityId() != null && requestModel.getInstructorUnavailabilityId() != 0){

                instructorUnavailability = instructorUnavailabilityRepository.findByInstructorUnavailabilityId(requestModel.getInstructorUnavailabilityId());

                if (instructorUnavailability == null){
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_INSTRUCTOR_UNAVAILABILITY_ID_INCORRECT, null);
                }
                if (!user.getUserId().equals(instructorUnavailability.getUser().getUserId())){
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_INSTRUCTOR_NOT_ALLOWED_TO_EDIT, null);
                }
                isNewUnavailability = false;
                ExistingInstructorUnavailabilityId = instructorUnavailability.getInstructorUnavailabilityId();
            } else {
                instructorUnavailability = new InstructorUnavailability();

                InstructorUnavailability instructorUnavailability1 = instructorUnavailabilityRepository.findByStartDateAndEndDateAndUserKloudlessCalendar(startDate, endDate, userKloudlessCalendar);

                if (instructorUnavailability1 != null){
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_UNAVAILABLE_DUPLICATION, null);
                }
                isNewUnavailability = true;
            }
            
            boolean isInstructorUnavailableOverlapping = checkInstructorUnavailabilityOverlapping(startDate,endDate,userKloudlessCalendar,ExistingInstructorUnavailabilityId);
         
            if (isInstructorUnavailableOverlapping){
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_UNAVAILABLE_DUPLICATION, null);
            }

            instructorUnavailability.setUser(user);
            instructorUnavailability.setStartDate(startDate);
            instructorUnavailability.setEndDate(endDate);

            scheduleUnavailability(scheduleDate,endDateInUserTimezone,user,instructorUnavailability,isNewUnavailability);

            InstructorUnavailabilityResponseView instructorUnavailabilityResponseView = new InstructorUnavailabilityResponseView();
            instructorUnavailabilityResponseView.setInstructorUnavailabilityId(instructorUnavailability.getInstructorUnavailabilityId());
            instructorUnavailabilityResponseView.setStartDate(fitwiseUtils.formatDateWithTime(instructorUnavailability.getStartDate()));
            instructorUnavailabilityResponseView.setEndDate(fitwiseUtils.formatDateWithTime(instructorUnavailability.getEndDate()));
            if(instructorUnavailability.getSchedulePayload() != null){
                instructorUnavailabilityResponseView.setSchedulePayload(instructorUnavailability.getSchedulePayload());
            }

            return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_INSTRUCTOR_UNAVAILABILITY_SAVED, instructorUnavailabilityResponseView);

        } finally {

            //Getting list of schedules which scheduled between the instructor unavailability start and end time
//            List<Long> scheduleIdsList = instructorCalendarService.getInstructorSchedules(requestModel.getStartDate(), requestModel.getEndDate());
//
//            if (scheduleIdsList != null && !scheduleIdsList.isEmpty()) {
//
//                //Deleting user schedules
//                instructorCalendarService.deleteSchedulesFromInstructor(scheduleIdsList);
//            }

        }

    }
    
      private void scheduleUnavailability(Date scheduleDate, Date endDateInUserTimezone, User user,
		
            InstructorUnavailability instructorUnavailability, boolean isNewUnavailability) {
		   // TODO Auto-generated method stub
    	  UserKloudlessAccount userKloudlessAccount = calendarService.getActiveAccount(user);
          
          ValidationUtils.throwException(userKloudlessAccount == null,
               CalendarConstants.CAL_ERR_ACCOUNT_NOT_FOUND, Constants.BAD_REQUEST);
       
          UserKloudlessCalendar userKloudlessCalendar = calendarService.getActiveCalendarFromKloudlessAccount(userKloudlessAccount);
          
          	UserKloudlessSchedule userKloudlessSchedule = new UserKloudlessSchedule();
          	 if(instructorUnavailability.getEventId() != null){
          		 userKloudlessSchedule = userKloudlessScheduleRepository.findByUserKloudlessScheduleId(Long.valueOf(instructorUnavailability.getEventId()));
          	 }
          	
          	userKloudlessSchedule.setUser(user);
 	        if(scheduleDate != null){

 	            SimpleDateFormat outputsdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);   
	 		     
				String setbookingDate = outputsdf.format(scheduleDate);
			    String setbookingTime = "18:30:00";
			    String setbookingDateTime = setbookingDate +" "+ setbookingTime; 
	   	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
			    sdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
 		        Date date = null;
 				    try {
 				    	date = sdf.parse(setbookingDateTime);
 				           } catch (ParseException e) {
 					       // TODO Auto-generated catch block
 					   e.printStackTrace();
 			         }
 		           userKloudlessSchedule.setBookingDate(date);
 		           SimpleDateFormat start_end_sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
 		           userKloudlessSchedule.setScheduleStartTime(start_end_sdf.format(scheduleDate));
 		           userKloudlessSchedule.setScheduleEndTime(start_end_sdf.format(endDateInUserTimezone));
 		        
 	            }
 	        UserKloudlessSchedule userKloudlessScheduleSaved = userKloudlessScheduleRepository.save(userKloudlessSchedule);
 	        instructorUnavailability.setEventId(userKloudlessSchedule.getUserKloudlessScheduleId().toString());
 	        instructorUnavailability.setUserKloudlessCalendar(userKloudlessCalendar);
 	        InstructorUnavailability instructorUnavailabilitySaved = instructorUnavailabilityRepository.save(instructorUnavailability);
            setUnavailabilityForIntegratedCalendar(instructorUnavailabilitySaved,userKloudlessAccount,userKloudlessCalendar);
		 
	}
      
    private void setUnavailabilityForIntegratedCalendar(InstructorUnavailability instructorUnavailability, UserKloudlessAccount userKloudlessAccount, UserKloudlessCalendar userKloudlessCalendar) {
      	
    		RefreshAccessTokenResponse refreshAccessTokenResponse = null;
        
    		refreshAccessTokenResponse = cronofyService.refreshAccessToken(userKloudlessAccount.getRefreshToken());
	        
	        if (refreshAccessTokenResponse == null) {
		 		throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED,null);
		    }
	        
	        SimpleDateFormat realtimeSchedulequeryperiodssdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszzz");
            realtimeSchedulequeryperiodssdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
            String startDate = realtimeSchedulequeryperiodssdf.format(instructorUnavailability.getStartDate());
            String endDate = realtimeSchedulequeryperiodssdf.format(instructorUnavailability.getEndDate());
    
	        if(!cronofyService.createUnavailableEvent(refreshAccessTokenResponse.getAccessToken(),userKloudlessCalendar.getCalendarId(),instructorUnavailability.getInstructorUnavailabilityId().toString(),startDate,endDate)){
	             throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED, MessageConstants.ERROR);
	        }
	
        }
      


	public Date getTimeInUTC(Date startDate) {
    	
    	
    	SimpleDateFormat outputsdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);   
		     
	          String setbookingDate = outputsdf.format(startDate);
	         
	          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
	         
	          Date date = null;
			    try {
			    	date = sdf.parse(setbookingDate);
			     } catch (ParseException e) {
				    // TODO Auto-generated catch block
				    e.printStackTrace();
		    	}
			    return date;
      }


    /**
     * API to get instructor unavailability based in start and end time
     *
     * @parm startDateInMillis, endDateInMillis
     * @return
     */
    public ResponseModel getInstructorUnavailability(Long startTime, Long endTime) throws ParseException {
        User user = userComponents.getUser();

        //check whether the start date is after the end date or not
        Date startDate = new Date(startTime);
        Date endDate = new Date(endTime);
        if (startDate.after(endDate)){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_START_DATE_AFTER_END_DATE, null);
        }

        List<InstructorUnavailabilityResponseView> instructorUnavailabilitieRes = new ArrayList<>();
        UserKloudlessAccount userKloudlessAccount = calendarService.getActiveAccount(user);
        UserKloudlessCalendar userKloudlessCalendar = calendarService.getActiveCalendarFromKloudlessAccount(userKloudlessAccount);
        List<InstructorUnavailability> instructorUnavailabilities = instructorUnavailabilityRepository.findByUserKloudlessCalendar(userKloudlessCalendar);
        //getting list of unavailabilities
        for (InstructorUnavailability instructorUnavailability : instructorUnavailabilities){
            if ((startDate.after(instructorUnavailability.getStartDate()) && startDate.before(instructorUnavailability.getEndDate()))
                    || (endDate.after(instructorUnavailability.getStartDate()) && endDate.before(instructorUnavailability.getEndDate()))
                    || (startDate.before(instructorUnavailability.getStartDate()) && endDate.after(instructorUnavailability.getEndDate()))){

                InstructorUnavailabilityResponseView instructorUnavailabilityResponseView = new InstructorUnavailabilityResponseView();
                instructorUnavailabilityResponseView.setInstructorUnavailabilityId(instructorUnavailability.getInstructorUnavailabilityId());
                instructorUnavailabilityResponseView.setStartDate(fitwiseUtils.formatDateWithTime(instructorUnavailability.getStartDate()));
                instructorUnavailabilityResponseView.setEndDate(fitwiseUtils.formatDateWithTime(instructorUnavailability.getEndDate()));
                instructorUnavailabilitieRes.add(instructorUnavailabilityResponseView);
            }
        }

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_INSTRUCTOR_UNAVAILABILITY_RETRIEVED, instructorUnavailabilitieRes);
    }

    /**
     * API to delete instructor unavailability
     *
     * @parm instructorUnavailabilityId
     * @return
     */
    public ResponseModel deleteInstructorUnavailability(Long instructorUnavailabilityId){
        User user = userComponents.getUser();

        //Validating the instructorUnavailabilityId
        if (instructorUnavailabilityId == null ||instructorUnavailabilityId ==0){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_INSTRUCTOR_UNAVAILABILITY_ID_NULL, null);
        }

        InstructorUnavailability instructorUnavailability = instructorUnavailabilityRepository.findByInstructorUnavailabilityId(instructorUnavailabilityId);
        if (instructorUnavailability == null){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_INSTRUCTOR_UNAVAILABILITY_ID_INCORRECT, null);
        }
        if (!user.getUserId().equals(instructorUnavailability.getUser().getUserId())){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_INSTRUCTOR_NOT_ALLOWED_TO_EDIT, null);
        }
        //delete unavailability from Cronofy
        	UserKloudlessAccount inst_account = calendarService.getActiveAccount(user);
        	UserKloudlessCalendar inst_activeCalendar = calendarService.getActiveCalendarFromKloudlessAccount(inst_account);
        	String UNAV_EventId = "Unavailable_"+instructorUnavailability.getInstructorUnavailabilityId().toString();
	        RefreshAccessTokenResponse refreshAccessTokenResponse = null;
	        
	        refreshAccessTokenResponse = cronofyService.refreshAccessToken(inst_account.getRefreshToken());
	        
	        if (refreshAccessTokenResponse == null) {
		 		throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED,null);
		    }
	     
	    
	        if(!cronofyService.deleteevent(refreshAccessTokenResponse.getAccessToken(),inst_activeCalendar.getCalendarId(),UNAV_EventId)){
	             throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_KLOUDLESS_SCHEDULE_DELETE_FAILED, MessageConstants.ERROR);
	         }

        //delete unavailability from Schedule table
        UserKloudlessSchedule UNAV_userKloudlessSchedule = userKloudlessScheduleRepository.findByUserKloudlessScheduleId(Long.valueOf(instructorUnavailability.getEventId()));
        userKloudlessScheduleRepository.delete(UNAV_userKloudlessSchedule);
        
        //delete unavailability from instructorUnavailability table
        instructorUnavailabilityRepository.delete(instructorUnavailability);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_INSTRUCTOR_UNAVAILABILITY_DELETED, null);
    }

    /**
     * Check Instructor unavailabilty for a whole day
     * @param startTimeInUtc
     * @param endTimeInUtc
     * @param userKloudlessCalendar
     * @return
     */
    public boolean checkInstructorUnavailabilityForADay(Date startTimeInUtc, Date endTimeInUtc,UserKloudlessCalendar userKloudlessCalendar){
        boolean isInstructorUnavailableForADay = false;
        if(userKloudlessCalendar != null){
         List<InstructorUnavailability> instructorUnavailabilities = instructorUnavailabilityRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndUserKloudlessCalendar(
                   startTimeInUtc,endTimeInUtc,userKloudlessCalendar);
         if(!instructorUnavailabilities.isEmpty()){
                isInstructorUnavailableForADay = true;
         }
        }
        return  isInstructorUnavailableForADay;
    }
    /**
     * Check Instructor unavailabilty for Overlapping
     * @param startTimeInUtc
     * @param endTimeInUtc
     * @param userKloudlessCalendar
     * @return
     */
    public boolean checkInstructorUnavailabilityOverlapping(Date startDate, Date endDate, UserKloudlessCalendar userKloudlessCalendar, Long instructorUnavailabilityID){
        boolean isInstructorUnavailableOverlapping = false;
        if(userKloudlessCalendar != null){
        	 List<InstructorUnavailability> instructorUnavailabilities = instructorUnavailabilityRepository.findByUserKloudlessCalendar(
                     userKloudlessCalendar);
        	 if(!instructorUnavailabilities.isEmpty()){
                 for(InstructorUnavailability instructorUnavailability : instructorUnavailabilities){
                	 if(!instructorUnavailabilityID.equals(instructorUnavailability.getInstructorUnavailabilityId())) {
                     if ((startDate.after(instructorUnavailability.getStartDate()) && startDate.before(instructorUnavailability.getEndDate()))
                             || (endDate.after(instructorUnavailability.getStartDate()) && endDate.before(instructorUnavailability.getEndDate()))
                             || (endDate.after(instructorUnavailability.getEndDate()) && startDate.equals(instructorUnavailability.getStartDate()))
                             || (startDate.before(instructorUnavailability.getStartDate()) && endDate.equals(instructorUnavailability.getEndDate()))
                             || (startDate.before(instructorUnavailability.getStartDate()) && endDate.after(instructorUnavailability.getEndDate()))){
                         
                    	 isInstructorUnavailableOverlapping = true;
                     }
                   }
                 }
             }
         
        }
        return  isInstructorUnavailableOverlapping;
    }

    /**
     * Construct Instructor unavailabilities for member
     * @param startDate
     * @param endDate
     * @param userKloudlessCalendar
     * @return
     */
    public List<InstructorUnavailabilityMemberView> constructInstructorUnavailability(Date startDate, Date endDate, UserKloudlessCalendar userKloudlessCalendar){
        List<InstructorUnavailability> instructorUnavailabilities = instructorUnavailabilityRepository.findByUserKloudlessCalendar(
                userKloudlessCalendar);

        List<InstructorUnavailabilityMemberView> instructorUnavailabilityMemberViews = new ArrayList<>();
        if(!instructorUnavailabilities.isEmpty()){
            for(InstructorUnavailability instructorUnavailability : instructorUnavailabilities){
                if ((startDate.after(instructorUnavailability.getStartDate()) && startDate.before(instructorUnavailability.getEndDate()))
                        || (endDate.after(instructorUnavailability.getStartDate()) && endDate.before(instructorUnavailability.getEndDate()))
                        || (startDate.before(instructorUnavailability.getStartDate()) && endDate.after(instructorUnavailability.getEndDate()))){
                    InstructorUnavailabilityMemberView instructorUnavailabilityMemberView = new InstructorUnavailabilityMemberView();
                    instructorUnavailabilityMemberView.setStartDate(fitwiseUtils.formatDateWithTime(instructorUnavailability.getStartDate()));
                    instructorUnavailabilityMemberView.setEndDate(fitwiseUtils.formatDateWithTime(instructorUnavailability.getEndDate()));
                    instructorUnavailabilityMemberView.setInstructorUnavailabilityId(instructorUnavailability.getInstructorUnavailabilityId());
                    instructorUnavailabilityMemberViews.add(instructorUnavailabilityMemberView);
                }

            }
        }
        return instructorUnavailabilityMemberViews;

    }

    public ResponseModel getUnavailableTimeList(){

        List<UnavailableTimes> unavailableTimes = unavailableTimeRepository.findAll();
        if(unavailableTimes.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS,MessageConstants.MSG_DATA_NOT_AVAILABLE,null);
        }
        Map<String,Object> responseMap = new HashMap<>();
        responseMap.put(KeyConstants.KEY_UNAVAILABLE_TIMES,unavailableTimes);
        return  new ResponseModel(Constants.SUCCESS_STATUS,MessageConstants.MSG_DATA_RETRIEVED,responseMap);

    }

    private SchedulePayload constructScedulePayload(String startTime,String endTime){

        SchedulePayload schedulePayload = new SchedulePayload();
        schedulePayload.setName("unavailable");
        schedulePayload.setStart(startTime);
        schedulePayload.setEnd(endTime);
        schedulePayload.setStart_time_zone(fitwiseUtils.getUserTimeZone());

        return schedulePayload;

    }

    private void scheduleUnavailability(SchedulePayload schedulePayload,User user,InstructorUnavailability instructorUnavailability,boolean isNewBlock){

        UserKloudlessAccount userKloudlessAccount = calendarService.getActiveAccount(user);
        ValidationUtils.throwException(userKloudlessAccount == null,
                CalendarConstants.CAL_ERR_ACCOUNT_NOT_FOUND, Constants.BAD_REQUEST);

       if (ValidationUtils.isEmptyString(userKloudlessAccount.getService())) {
            userKloudlessAccount = kloudLessService.updateServiceInfo(userKloudlessAccount);
        }

        UserKloudlessCalendar userKloudlessCalendar = calendarService.getActiveCalendarFromKloudlessAccount(userKloudlessAccount);

        ObjectMapper objectMapper = new ObjectMapper();
        Event event = objectMapper.convertValue(schedulePayload, Event.class);
        Map<String, Object> content = objectMapper.convertValue(event, new TypeReference<Map<String, Object>>() {});
        Resource createdEvent;
        if(isNewBlock){
            createdEvent =kloudLessService.createKloudlessSchedule(userKloudlessCalendar, content);
        }else{
            createdEvent = kloudLessService.updateKloudlessSchedule(userKloudlessCalendar,instructorUnavailability.getEventId(),content);
        }
        Gson gson = new Gson();
        instructorUnavailability.setSchedulePayload(gson.toJson(createdEvent.getData()));
        instructorUnavailability.setEventId(createdEvent.getId());
        instructorUnavailability.setUserKloudlessCalendar(userKloudlessCalendar);
        instructorUnavailabilityRepository.save(instructorUnavailability);

        switch (userKloudlessAccount.getService()) {
            case KloudLessService.SERVICE_ICLOUD_CALENDAR:
                updateKloudlessEventReminderProperties(userKloudlessCalendar, instructorUnavailability, createdEvent.getId());
                break;
            case KloudLessService.SERVICE_GOOGLE_CALENDAR:
            case KloudLessService.SERVICE_OUTLOOK_CALENDAR:
            default:
                updateKloudlessEventCustomProperties(userKloudlessCalendar,instructorUnavailability,createdEvent.getId());
                break;
        }

    }


    public void updateKloudlessEventCustomProperties(UserKloudlessCalendar calendar, InstructorUnavailability instructorUnavailability, String eventId) {


        Map<String, Object> instructorUnavailabilityId = new HashMap<String, Object>();

        instructorUnavailabilityId.put("key", CalendarConstants.CAL_PROP_INSTRUCTOR_UNAVAILABILITY_ID);
        instructorUnavailabilityId.put(StringConstants.JSON_PROPERTY_KEY_VALUE, instructorUnavailability.getInstructorUnavailabilityId());
        instructorUnavailabilityId.put(StringConstants.JSON_PROPERTY_KEY_PRIVATE, true);

        List<Map<String, Object>> propertiesList = new ArrayList<>();

        if (!instructorUnavailabilityId.isEmpty()) {
            propertiesList.add(instructorUnavailabilityId);
        }


        Map<String, Object> customProperties = new HashMap<>();
        customProperties.put("custom_properties", propertiesList);
        Resource resource =  kloudLessService.updateKloudlessSchedule(calendar, eventId, customProperties);
        Gson gson = new Gson();
        instructorUnavailability.setSchedulePayload(gson.toJson(resource.getData()));
        instructorUnavailabilityRepository.save(instructorUnavailability);
    }

    private void updateKloudlessEventReminderProperties(UserKloudlessCalendar calendar, InstructorUnavailability instructorUnavailability, String eventId) {
        Map<String, String> properties = new HashMap<>();
        if (instructorUnavailability.getInstructorUnavailabilityId() > 0) {
            properties.put(CalendarConstants.CAL_PROP_INSTRUCTOR_UNAVAILABILITY_ID, String.valueOf(instructorUnavailability.getInstructorUnavailabilityId()));
        }

        List<String> values = new ArrayList<>();
        properties.forEach((name, value) -> values.add(String.format("%s=%s", name, value)));
        String summary = String.join(CalendarService.REMINDER_CUSTOM_PROPERTY_SEPERATOR, values);

        Reminder reminder = new Reminder();
        reminder.setMinutes(30L);
        reminder.setMethod("email");

        Email email = new Email();
        email.setDescription("Fitwise");
        email.setSummary(summary);
        email.setTo(new ArrayList<>());
        reminder.setEmail(email);

        List<Reminder> reminderList = new ArrayList<>();
        reminderList.add(reminder);

        Map<String, Object> reminders = new HashMap<>();
        reminders.put("reminders", reminderList);

        Resource resource =  kloudLessService.updateKloudlessSchedule(calendar, eventId, reminders);
        Gson gson = new Gson();
        instructorUnavailability.setSchedulePayload(gson.toJson(resource.getData()));
        instructorUnavailabilityRepository.save(instructorUnavailability);
    }

    public void deleteScheduleFromKloudless(User user, InstructorUnavailability instructorUnavailability){

        JSONObject jsonObject = new JSONObject(instructorUnavailability.getSchedulePayload());
        String eventId = jsonObject.getString("id");

        UserKloudlessCalendar userKloudlessCalendar = instructorUnavailability.getUserKloudlessCalendar();
        ResponseRaw responseRaw = null;
        if (eventId != null){

            responseRaw = kloudLessService.deleteKloudlessSchedule(userKloudlessCalendar, eventId);
        }
        if(responseRaw.getData().getStatusLine().getStatusCode() != 204 && responseRaw.getData().getStatusLine().getStatusCode() != 200){
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_KLOUDLESS_SCHEDULE_DELETE_FAILED, null);
        }
    }

    public ResponseModel populateCalendarData(){
        List<InstructorUnavailability> instructorUnavailabilities = instructorUnavailabilityRepository.findAll();
        for(InstructorUnavailability instructorUnavailability : instructorUnavailabilities){
            if(instructorUnavailability.getSchedulePayload() != null){
                JSONObject jsonObject = new JSONObject(instructorUnavailability.getSchedulePayload());
                String calendarId = jsonObject.getString("calendar_id");
                List<UserKloudlessCalendar> userKloudlessCalendars = userKloudlessCalendarRepository.findByUserAndCalendarId(instructorUnavailability.getUser(),calendarId);
                if(!userKloudlessCalendars.isEmpty()){
                    instructorUnavailability.setUserKloudlessCalendar(userKloudlessCalendars.get(0));
                    instructorUnavailabilityRepository.save(instructorUnavailability);
                }
            }

        }

        return new ResponseModel(Constants.SUCCESS_STATUS,"calendar data populated",null);

    }

}
