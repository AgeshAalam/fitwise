package com.fitwise.service.cronofy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.Comparator;
import javax.transaction.Transactional;

import org.biacode.jcronofy.api.model.common.CronofyResponse;
import org.biacode.jcronofy.api.model.response.UpdateAccessTokenResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.CalendarConstants;
import com.fitwise.constants.Constants;
import com.fitwise.constants.EmailConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.PushNotificationConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.InstructorUnavailability;
import com.fitwise.entity.TimeSpan;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.calendar.CalendarMeetingType;
import com.fitwise.entity.calendar.CronofyAvailabilityRules;
import com.fitwise.entity.calendar.UserKloudlessAccount;
import com.fitwise.entity.calendar.UserKloudlessCalendar;
import com.fitwise.entity.calendar.UserKloudlessMeeting;
import com.fitwise.entity.calendar.UserKloudlessSchedule;
import com.fitwise.entity.calendar.UserKloudlessSubscription;
import com.fitwise.entity.calendar.Zone;
import com.fitwise.entity.packaging.PackageKloudlessMapping;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.subscription.PackageSubscription;
import com.fitwise.exception.ApplicationException;
import com.fitwise.properties.CronofyProperties;
import com.fitwise.repository.InstructorUnavailabilityRepository;
import com.fitwise.repository.TimeSpanRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.calendar.CalendarMeetingTypeRepository;
import com.fitwise.repository.calendar.CronofyAvailabilityRulesRepository;
import com.fitwise.repository.calendar.UserKloudlessCalendarRepository;
import com.fitwise.repository.calendar.UserKloudlessMeetingRepository;
import com.fitwise.repository.calendar.UserKloudlessScheduleRepository;
import com.fitwise.repository.calendar.UserKloudlessSubscriptionRepository;
import com.fitwise.repository.calendar.UserKloudlessTokenRepository;
import com.fitwise.repository.calendar.ZoneRepository;
import com.fitwise.repository.packaging.PackageKloudlessMappingRepository;
import com.fitwise.repository.packaging.SubscriptionPackageRepository;
import com.fitwise.repository.subscription.PackageSubscriptionRepository;
import com.fitwise.response.packaging.SubscriptionPackageMemberView;
import com.fitwise.service.fcm.PushNotificationAPIService;
import com.fitwise.utils.EmailContentUtil;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.utils.mail.AsyncMailer;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.calendar.KloudlessMeetingListModel;
import com.fitwise.view.calendar.KloudlessMeetingModel;
import com.fitwise.view.calendar.KloudlessScheduleModel;
import com.fitwise.view.cronofy.AccessTokenModel;
import com.fitwise.view.cronofy.AccessTokenResponse;
import com.fitwise.view.cronofy.CreateCalendarResponse;
import com.fitwise.view.cronofy.CreateRealtimeScheduleResponse;
import com.fitwise.view.cronofy.CreateavailabilityrulesResponse;
import com.fitwise.view.cronofy.CronofyCalendarModel;
import com.fitwise.view.cronofy.CronofyCalendarResponseView;
import com.fitwise.view.cronofy.CronofyMeetingListModel;
import com.fitwise.view.cronofy.CronofyMeetingModel;
import com.fitwise.view.cronofy.CronofyTokenModel;
import com.fitwise.view.cronofy.CronofyschedulePayload;
import com.fitwise.view.cronofy.DefaultCalendarModel;
import com.fitwise.view.cronofy.FreeBusyResponse;
import com.fitwise.view.cronofy.FreeBusyEvent;
import com.fitwise.view.cronofy.GetScheduleStatusResponse;
import com.fitwise.view.cronofy.InstructorSchedulePayload;
import com.fitwise.view.cronofy.RealtimeScheduleResponse;
import com.fitwise.view.cronofy.RealtimeScheduleavailability;
import com.fitwise.view.cronofy.RealtimeSchedulebuffer;
import com.fitwise.view.cronofy.RealtimeSchedulebufferafter;
import com.fitwise.view.cronofy.RealtimeSchedulebufferbefore;
import com.fitwise.view.cronofy.RealtimeScheduleevent;
import com.fitwise.view.cronofy.RealtimeScheduleConferencing;
import com.fitwise.view.cronofy.RealtimeScheduleLocation;
import com.fitwise.view.cronofy.RealtimeSchedulemembers;
import com.fitwise.view.cronofy.RealtimeScheduleoauth;
import com.fitwise.view.cronofy.RealtimeScheduleparticipants;
import com.fitwise.view.cronofy.RealtimeSchedulequeryperiods;
import com.fitwise.view.cronofy.RealtimeScheduleredirecturls;
import com.fitwise.view.cronofy.RealtimeSchedulerequiredduration;
import com.fitwise.view.cronofy.RealtimeSchedulestartinterval;
import com.fitwise.view.cronofy.RealtimeScheduletargetcalendars;
import com.fitwise.view.cronofy.RefreshAccessTokenResponse;
import com.fitwise.view.cronofy.RequestavailabilityrulesResponse;
import com.fitwise.view.cronofy.SchedulePayloadattendees;
import com.fitwise.view.cronofy.SchedulePayloadcreator;
import com.fitwise.view.cronofy.SchedulePayloadcustomproperties;
import com.fitwise.view.cronofy.SchedulePayloadorganizer;
import com.fitwise.view.fcm.NotificationContent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CronofyCalenderService {
	
	@Autowired
	private CronofyService cronofyService;

	@Autowired
	private CronofyProperties cronofyProperties;
	
	@Autowired
	private UserComponents userComponents;
	
	@Autowired
	private PackageSubscriptionRepository packageSubscriptionRepository;


	@Autowired
	private UserKloudlessTokenRepository userKloudlessTokenRepository;
	
	@Autowired
    private UserKloudlessCalendarRepository userKloudlessCalendarRepository;
	
	@Autowired
	private CronofyAvailabilityRulesRepository cronofyAvailabilityRulesRepository;
	  
	@Autowired
	private TimeSpanRepository timeSpanRepository;
	  
	@Autowired
	private PackageKloudlessMappingRepository packageKloudlessMappingRepository;
	
	@Autowired
	private FitwiseUtils fitwiseUtils;
	
    @Autowired
	private UserKloudlessMeetingRepository userKloudlessMeetingRepository;

	@Autowired
	private UserKloudlessScheduleRepository userKloudlessScheduleRepository;

	@Autowired
	private CalendarMeetingTypeRepository calendarMeetingTypeRepository;
	
	@Autowired
	private ZoneRepository zoneRepository;
    @Autowired
	private UserKloudlessSubscriptionRepository userKloudlessSubscriptionRepository;
	
    @Autowired
    private SubscriptionPackageRepository subscriptionPackageRepository;
    
    @Autowired
    EmailContentUtil emailContentUtil;
    @Autowired
    
    PushNotificationAPIService pushNotificationAPIService;
   
    private final AsyncMailer asyncMailer;
	
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    InstructorUnavailabilityRepository instructorUnavailabilityRepository;
   
	
	public CronofyTokenModel validateAndSaveToken(final String code,final String redirectUri) {

		 
		  log.info("Validate and save token starts.");
		  long apiStartTimeMillis = new Date().getTime();

		   if (ValidationUtils.isEmptyString(code)) {
				throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_INVALID, null);
		   }
		   if (ValidationUtils.isEmptyString(redirectUri)) {
				throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_INVALID, null);
		   }
		   
		   AccessTokenResponse accessTokenResponse = null;
		  
		     accessTokenResponse = cronofyService.getaccesstoken(code,redirectUri);
		 
		   if (accessTokenResponse == null) {
		 		throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED,null);
		   }
		   
		   
		   log.info("Basic validation with query to get cronofy account : Time taken in millis : "
				+ (new Date().getTime() - apiStartTimeMillis));
		   long profilingEndTimeMillis = new Date().getTime();

		   User user = userComponents.getUser();
		   log.info("Get user : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
		   profilingEndTimeMillis = new Date().getTime();

		   List<UserKloudlessAccount> userKloudlessTokens = userKloudlessTokenRepository.findByUserAndAccountId(user,
				accessTokenResponse.getAccountId());
		   
		   log.info("Query to user Cronofy token list : Time taken in millis : "
				+ (new Date().getTime() - profilingEndTimeMillis));

		   profilingEndTimeMillis = new Date().getTime();

		   UserKloudlessAccount userKloudlessToken = null;

		   if (userKloudlessTokens.isEmpty()) {
			  userKloudlessToken = new UserKloudlessAccount();
			  userKloudlessToken.setUser(user);
			  userKloudlessToken.setAccountId(accessTokenResponse.getAccountId());
			  userKloudlessToken.setTokentype(accessTokenResponse.getTokenType());
			  userKloudlessToken.setExpiresin(accessTokenResponse.getExpiresIn());
			  userKloudlessToken.setRefreshToken(accessTokenResponse.getRefreshToken());
			  userKloudlessToken.setScope(accessTokenResponse.getScope());
			  userKloudlessToken.setSub(accessTokenResponse.getSub());
			  userKloudlessToken.setService(accessTokenResponse.getLinkingProfile().getProviderService());
			  userKloudlessToken.setProfileId(accessTokenResponse.getLinkingProfile().getProfileId());
			  userKloudlessToken.setAccountEmail(accessTokenResponse.getLinkingProfile().getProfileName());
			  userKloudlessToken.setProviderName(accessTokenResponse.getLinkingProfile().getProviderName());

		   } 
		 else 
		 {
			
			   userKloudlessToken = userKloudlessTokens.get(0);
		 }

		 log.info("Get first or new cronofy account : Time taken in millis : "
				+ (new Date().getTime() - profilingEndTimeMillis));
		 profilingEndTimeMillis = new Date().getTime();

		
		 List<UserKloudlessAccount> existingKloudlessAccounts = userKloudlessTokenRepository.findByUser(user);
		  log.info("Query to get existing cronofy accounts : Time taken in millis : "
				+ (new Date().getTime() - profilingEndTimeMillis));

		 if (existingKloudlessAccounts.isEmpty()) {
			userKloudlessToken.setActive(true);
		 }

		 profilingEndTimeMillis = new Date().getTime();

		 userKloudlessToken.setToken(accessTokenResponse.getAccessToken());

		 userKloudlessTokenRepository.save(userKloudlessToken);
		 
		 log.info("Query to save user cronofy token : Time taken in millis : "
					+ (new Date().getTime() - profilingEndTimeMillis));
	     log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
		 log.info("Validate and save token ends.");
		 
		 CronofyTokenModel cronofyTokenModel = new CronofyTokenModel();
	      
          cronofyTokenModel.setTokentype(userKloudlessToken.getTokentype());
        
          cronofyTokenModel.setAccesstoken(userKloudlessToken.getToken());
    
          cronofyTokenModel.setExpiresin(userKloudlessToken.getExpiresin().toString());
     
          cronofyTokenModel.setRefreshtoken(userKloudlessToken.getRefreshToken());
      
          cronofyTokenModel.setScope(userKloudlessToken.getScope());
         
          cronofyTokenModel.setAccountid(userKloudlessToken.getAccountId());
     
          cronofyTokenModel.setSub(userKloudlessToken.getSub());
     
          cronofyTokenModel.setProvidername(userKloudlessToken.getService());
      
          cronofyTokenModel.setProfileid(userKloudlessToken.getProfileId());
        
          cronofyTokenModel.setProfilename(userKloudlessToken.getAccountEmail());
      
          cronofyTokenModel.setProviderservice(userKloudlessToken.getProviderName());
    
         return cronofyTokenModel;
        }
	  
	
	  public void validateAndSaveCalendar(final String profileId, final String profilename,final String accessToken,final String refreshToken) {
		
	       log.info("Start save or update");
	       long tempTime = new Date().getTime();
		
	        tempTime = new Date().getTime();
		    User user = userComponents.getUser();
		
		    log.info("Get User : " + (new Date().getTime() - tempTime));
		    tempTime = new Date().getTime();
		  
		    List<UserKloudlessAccount> userKloudlessAccounts = userKloudlessTokenRepository.findByUserAndProfileId(user,
		    		profileId);
		  
		    if (userKloudlessAccounts.isEmpty()) {
			 throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_NOT_FOUND, null);
		    }
		  
		    log.info("Check cronofy account : " + (new Date().getTime() - tempTime));
		    tempTime = new Date().getTime();
		 
		    UserKloudlessAccount userKloudlessAccount = userKloudlessAccounts.get(0);
		  
		    if (userKloudlessAccount == null) {
		 	 throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_NOT_FOUND, null);
		   }
		
		
		    List<UserKloudlessCalendar> userKloudlessCalendars = userKloudlessCalendarRepository
				.findByUserAndUserKloudlessAccountAndCalendarName(user, userKloudlessAccount,
						profilename);
		
		    UserKloudlessCalendar userKloudlessCalendar = null;
		   
		    
		    if (userKloudlessCalendars.isEmpty()) {
		    	 CronofyResponse<UpdateAccessTokenResponse> updateAccessTokenResponse = cronofyService.updateAccessToken(refreshToken);

				   if (updateAccessTokenResponse == null) {
					   throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED,
							null);
				    }
			       

			       CreateCalendarResponse createCalendarResponse = null;
				  
			       createCalendarResponse = cronofyService.createCalendar(profileId,profilename,accessToken);
			       
			       if (createCalendarResponse == null) {
				 		throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED,null);
				    }
			     
			       if (ValidationUtils.isEmptyString(createCalendarResponse.getCalendar().getCalendarId())) {
					throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_CALENDAR_ID_INVALID, null);
				   }
			   
			       if (ValidationUtils.isEmptyString(createCalendarResponse.getCalendar().getProfileId())) {
					throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_ID_INVALID, null);
				   }
			     
			       log.info("Field validation : " + (new Date().getTime() - tempTime));
		    	
		      userKloudlessCalendar = new UserKloudlessCalendar();
		    	
		      userKloudlessCalendar.setUser(user);
			   
		      userKloudlessCalendar.setCalendarId(createCalendarResponse.getCalendar().getCalendarId());
		  
		      if (createCalendarResponse.getCalendar().getProviderName() != null) {
				userKloudlessCalendar.setProviderName(createCalendarResponse.getCalendar().getProviderName());
		      }
		  
		      if (createCalendarResponse.getCalendar().getProfileId() != null) {
				userKloudlessCalendar.setProfileId(createCalendarResponse.getCalendar().getProfileId());
		      }
		  
		      if (createCalendarResponse.getCalendar().getProfileName() != null) {
				userKloudlessCalendar.setProfileName(createCalendarResponse.getCalendar().getProfileName());
		      }
		  
		      if (createCalendarResponse.getCalendar().getCalendarId() != null) {
				userKloudlessCalendar.setCalendarId(createCalendarResponse.getCalendar().getCalendarId());
		      }
		  
		      if (createCalendarResponse.getCalendar().getCalendarName() != null) {
				userKloudlessCalendar.setCalendarName(createCalendarResponse.getCalendar().getCalendarName());
		      }
		    
		      userKloudlessCalendar.setCalendarReadonly(createCalendarResponse.getCalendar().isCalendarReadOnly());
		   
		    
		      userKloudlessCalendar.setCalendarIntegratedConferencingAvailable(createCalendarResponse.getCalendar().isCalendarIntegratedConferencingAvailable());
		    
		    
		       userKloudlessCalendar.setCalendarPrimary(createCalendarResponse.getCalendar().isCalendarPrimary());
		       
		       userKloudlessCalendar.setCalendarDeleted(createCalendarResponse.getCalendar().isCalendarDeleted());
		    
		       userKloudlessCalendar.setPermissionLevel(createCalendarResponse.getCalendar().getPermissionLevel());
		   
		       userKloudlessCalendar.setPrimaryCalendar(true);
		       userKloudlessCalendar.setUserKloudlessAccount(userKloudlessAccount);
		     
		     } 
		   
		    else 
		  
		    {
			    userKloudlessCalendar = userKloudlessCalendars.get(0);
			    userKloudlessCalendar.setPrimaryCalendar(true);
		    }
		   
		     userKloudlessCalendarRepository.save(userKloudlessCalendar);
		   
		   
		     log.info("Create or update active calendar : " + (new Date().getTime() - tempTime));
		     tempTime = new Date().getTime();
		  
		     List<UserKloudlessCalendar> existingCalendars = userKloudlessCalendarRepository
				.findByUserAndUserKloudlessAccount(user, userKloudlessAccount);
		     for (UserKloudlessCalendar existingCalendar : existingCalendars) {
			
		     if (userKloudlessCalendar.getUserKloudlessCalendarId()
					.equals(existingCalendar.getUserKloudlessCalendarId())) {
				continue;
			 }
		     existingCalendar.setPrimaryCalendar(false);
		     userKloudlessCalendarRepository.save(existingCalendar);
		   }
		   log.info("Disable all other calendars as not primary : " + (new Date().getTime() - tempTime));
	    }
	 
	   public void validateAndSaveDefaultCalendar(final DefaultCalendarModel calendarModel) {
		      
	    	 log.info("Start save or update");
	         long tempTime = new Date().getTime();
	        
	         if (ValidationUtils.isEmptyString(calendarModel.getCalendarId())) {
	            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_CALENDAR_ID_INVALID, null);
	         }
	         if (ValidationUtils.isEmptyString(calendarModel.getAccountId())) {
	            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_ID_INVALID, null);
	         }
	         log.info("Field validation : " + (new Date().getTime() - tempTime));
	         tempTime = new Date().getTime();
	       
	         User user = userComponents.getUser();
	         log.info("Get User : " + (new Date().getTime() - tempTime));
	         tempTime = new Date().getTime();
	       
	         List<UserKloudlessAccount> userKloudlessAccounts = userKloudlessTokenRepository.findByUserAndAccountId(user, calendarModel.getAccountId());
	       
	         if(userKloudlessAccounts.isEmpty()){
	            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_NOT_FOUND, null);
	         }
	         log.info("Check kloudless account : " + (new Date().getTime() - tempTime));
	         tempTime = new Date().getTime();
	       
	         UserKloudlessAccount userKloudlessAccount = userKloudlessAccounts.get(0);
	        
	         if(userKloudlessAccount == null){
	            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_NOT_FOUND, null);
	        }
	        
	        log.info("Update service : " + (new Date().getTime() - tempTime));
	        tempTime = new Date().getTime();
	        log.info("Get calendar resource : " + (new Date().getTime() - tempTime));
	        tempTime = new Date().getTime();
	       
	       
	          List<UserKloudlessCalendar> userKloudlessCalendars = userKloudlessCalendarRepository.findByUserAndUserKloudlessAccountAndCalendarId(user, userKloudlessAccount, calendarModel.getCalendarId());
	          UserKloudlessCalendar userKloudlessCalendar = userKloudlessCalendars.get(0);
	           if (!userKloudlessCalendars.isEmpty()) {
	                userKloudlessCalendar.setPrimaryCalendar(true);
	           } 
	          userKloudlessCalendarRepository.save(userKloudlessCalendar);
	        
	          log.info("Create or update active calendar : " + (new Date().getTime() - tempTime));
	          tempTime = new Date().getTime();
	        
	          List<UserKloudlessCalendar> existingCalendars = userKloudlessCalendarRepository.findByUserAndUserKloudlessAccount(user, userKloudlessAccount);
	           for (UserKloudlessCalendar existingCalendar : existingCalendars) {
	             if (userKloudlessCalendar.getUserKloudlessCalendarId().equals(existingCalendar.getUserKloudlessCalendarId())) {
	                continue;
	             }
	             existingCalendar.setPrimaryCalendar(false);
	             userKloudlessCalendarRepository.save(existingCalendar);
	           }
	          log.info("Disable all other calendars as not primary : " + (new Date().getTime() - tempTime));
	        
	    }
	  
	  
	  
	  public CronofyTokenModel getActiveAccount(){
	       
	    	User user = userComponents.getUser();
	       
	        UserKloudlessAccount userKloudlessAccount = getActiveAccount(user);

	       
	        CronofyResponse<UpdateAccessTokenResponse> updateAccessTokenResponse = cronofyService.updateAccessToken(userKloudlessAccount.getRefreshToken());

			   if (updateAccessTokenResponse == null) {
				   throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED,null);
			 } 

			CronofyTokenModel cronofyTokenModel = new CronofyTokenModel();
	      
	            cronofyTokenModel.setTokentype(userKloudlessAccount.getTokentype());
	           
	            cronofyTokenModel.setAccesstoken(userKloudlessAccount.getToken());
	       
	            cronofyTokenModel.setExpiresin(userKloudlessAccount.getExpiresin().toString());
	        
	            cronofyTokenModel.setRefreshtoken(userKloudlessAccount.getRefreshToken());
	         
	            cronofyTokenModel.setScope(userKloudlessAccount.getScope());
	            
	            cronofyTokenModel.setAccountid(userKloudlessAccount.getAccountId());
	        
	            cronofyTokenModel.setSub(userKloudlessAccount.getSub());
	        
	            cronofyTokenModel.setProvidername(userKloudlessAccount.getService());
	         
	            cronofyTokenModel.setProfileid(userKloudlessAccount.getProfileId());
	           
	            cronofyTokenModel.setProfilename(userKloudlessAccount.getAccountEmail());
	         
	            cronofyTokenModel.setProviderservice(userKloudlessAccount.getProviderName());
	       
	          return cronofyTokenModel;
	        
	      }
	      

              
	    /**
	     * API to change the calendar account status
	     *
	     * @parm userKloudlessAccountId, setActive
	     * @return
	     */
	     public void changeCalendarActiveStatus(String userCronofyAccountId, boolean setActive){
	       
	    	 User user = userComponents.getUser();

	        //Validating the userCronofyAccountId
	        if(userCronofyAccountId == null || userCronofyAccountId == ""){
	            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_ID_NULL, MessageConstants.ERROR);
	        }
	        List<UserKloudlessAccount> userKloudlessAccountList = userKloudlessTokenRepository.findByUserAndAccountId(user, userCronofyAccountId);
	        if (userKloudlessAccountList.isEmpty()){
	            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_ID_INVALID, MessageConstants.ERROR);
	        }
	        UserKloudlessAccount userKloudlessAccount = userKloudlessAccountList.get(0);

	        if(setActive){
	            userKloudlessAccount.setActive(true);
	            List<UserKloudlessAccount> existingKloudlessAccounts = userKloudlessTokenRepository.findByUser(user);
	            for (UserKloudlessAccount existingAccount : existingKloudlessAccounts) {
	                if (existingAccount.getUserKloudlessTokenId().equals(userKloudlessAccount.getUserKloudlessTokenId())) {
	                    continue;
	                }
	                existingAccount.setActive(false);
	                userKloudlessTokenRepository.save(existingAccount);
	            }
	        } else {
	            userKloudlessAccount.setActive(false);
	        }
	        userKloudlessTokenRepository.save(userKloudlessAccount);
	      }
	  
	
	     
	   
	     /**
	     * Get active cronofy account and token
	     * @param user
	     * @return
	     */
	     
	      public UserKloudlessAccount getActiveAccount(User user){
	       
		  
		   List<UserKloudlessAccount> userKloudlessAccounts = userKloudlessTokenRepository.findByUser(user);
	       
	        if(userKloudlessAccounts.isEmpty()){
	            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, CalendarConstants.CAL_ERR_ACCOUNT_NOT_FOUND, null);
	        }
	       
	        UserKloudlessAccount userKloudlessAccount = null;
	       
	        for (UserKloudlessAccount userKloudlessAccount1 : userKloudlessAccounts){
	         
	        	if (userKloudlessAccount1.isActive() && userKloudlessAccount1.getAccountId() != null){
	                userKloudlessAccount = userKloudlessAccount1;
	                break;
	             }
	          }
	        
	          if (userKloudlessAccount == null && !userKloudlessAccounts.isEmpty() && userKloudlessAccounts.get(0).getAccountId() != null){
	          
	        	userKloudlessAccount = userKloudlessAccounts.get(0);
	            userKloudlessAccount.setActive(true);
	            userKloudlessTokenRepository.save(userKloudlessAccount);
	          }
	          
	          return userKloudlessAccount;
	    }
	   
	  
	     /**
	      * Get instructors all calendars
	      *
	      * @parm
	      * @return
	     */
	   
	    public List<CronofyCalendarResponseView> getMyAllCalendars(){
	         
	    	
	    	User user = userComponents.getUser();

	       
	    	List<UserKloudlessAccount> userKloudlessAccountList = userKloudlessTokenRepository.findByUser(user);

	        if(userKloudlessAccountList.isEmpty()){
	              throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, CalendarConstants.CAL_ERR_ACCOUNT_NOT_FOUND, null);
	        }
	       
	        
	        List<CronofyCalendarResponseView> cronofyCalendarResponseViews = new ArrayList<>();
	        
	        for (UserKloudlessAccount userKloudlessAccount : userKloudlessAccountList){
	             
	        	
	        	CronofyCalendarResponseView cronofyCalendarResponseView = new CronofyCalendarResponseView();
	              
	        	cronofyCalendarResponseView.setAccountId(userKloudlessAccount.getAccountId());
	        	cronofyCalendarResponseView.setAccountEmail(userKloudlessAccount.getAccountEmail());
	        	cronofyCalendarResponseView.setAccountToken(userKloudlessAccount.getToken());
	        	cronofyCalendarResponseView.setProfileId(userKloudlessAccount.getProfileId());
	        	cronofyCalendarResponseView.setActive(userKloudlessAccount.isActive());
	        	  
	            List<UserKloudlessCalendar> userKloudlessCalendars = userKloudlessCalendarRepository.findByUserAndUserKloudlessAccount(user, userKloudlessAccount);
	           
	            List<CronofyCalendarModel> cronofyCalendarModels;

	            if(!userKloudlessCalendars.isEmpty()){
	               
	            	cronofyCalendarModels = new ArrayList<>();
	            
	             for (UserKloudlessCalendar userKloudlessCalendar : userKloudlessCalendars){
	                  
	            	CronofyCalendarModel cronofyCalendarModel = new CronofyCalendarModel();
		        	
		        	cronofyCalendarModel.setCalendarId(userKloudlessCalendar.getCalendarId());
		        	
		        	cronofyCalendarModel.setDefaultCalendar(userKloudlessCalendar.getPrimaryCalendar());
		        	
		   		  
		  		    if (userKloudlessCalendar.getProviderName() != null) {
		  			 cronofyCalendarModel.setProviderName(userKloudlessCalendar.getProviderName());
		  		    }
		  		  
		  		    if (userKloudlessCalendar.getProfileId() != null) {
		  			 cronofyCalendarModel.setProfileId(userKloudlessCalendar.getProfileId());
		  		    }
		  		  
		  		    if (userKloudlessCalendar.getProfileName() != null) {
		  			 cronofyCalendarModel.setProfileName(userKloudlessCalendar.getProfileName());
		  			 cronofyCalendarModel.setCalendarName(userKloudlessCalendar.getProfileName());
		  		    }
		  		  
		  		    if (userKloudlessCalendar.getCalendarId() != null) {
		  			  cronofyCalendarModel.setCalendarId(userKloudlessCalendar.getCalendarId());
		  		    }
		  		  
//		  		    if (userKloudlessCalendar.getCalendarName() != null) {
//		  			 cronofyCalendarModel.setCalendarName(userKloudlessCalendar.getCalendarName());
//		  		    }
		  		  
		  		    cronofyCalendarModel.setCalendarReadonly(userKloudlessCalendar.getCalendarReadonly());
		  		   
		  		   
		  		    cronofyCalendarModel.setCalendarDeleted(userKloudlessCalendar.getCalendarDeleted());
		  		 
		  		    cronofyCalendarModel.setCalendarIntegratedConferencingAvailable(userKloudlessCalendar.getCalendarIntegratedConferencingAvailable());
				    
				    
		  		    cronofyCalendarModel.setCalendarPrimary(userKloudlessCalendar.getCalendarPrimary());
			       
			    
		  		    cronofyCalendarModel.setPermissionLevel(userKloudlessCalendar.getPermissionLevel());
		           
		            
		  		    cronofyCalendarModels.add(cronofyCalendarModel);
		             
	               }
	         
	                cronofyCalendarResponseView.setCronofyCalendars(cronofyCalendarModels);
	             }
	            cronofyCalendarResponseViews.add(cronofyCalendarResponseView);
	          }
	         
	           
	        return cronofyCalendarResponseViews;
	      }		
	    
	    
	    public CronofyMeetingModel createFitwiseMeeting(CronofyMeetingModel meetingModel) {
		       
	    	  long startTime = new Date().getTime();
	          log.info("Start create fitwise meeting");
	          long temp = new Date().getTime();
	       
	          if (meetingModel.getMeetingTypeId() <= 0) {
	            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_MEETING_TYPE_INVALID, null);
	          }
	          log.info("Field validation : " + (new Date().getTime() - temp));
	       
	          temp = new Date().getTime();
	          User user = userComponents.getUser();
	          log.info("Get User : " + (new Date().getTime() - temp));
	          temp = new Date().getTime();
	        
	         UserKloudlessAccount userKloudlessAccount = getActiveAccount(user);
	         log.info("Get and update Active account : " + (new Date().getTime() - temp));
	         temp = new Date().getTime();
	       
	         UserKloudlessCalendar activeCalendar = getActiveCalendarFromKloudlessAccount(userKloudlessAccount);
	       
	         if(activeCalendar == null){
	            throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_CALENDAR_NOT_FOUND, null);
	         }
	        
	         log.info("Get active kloudless calendar : " + (new Date().getTime() - temp));
	         temp = new Date().getTime();
	      
	          CalendarMeetingType calendarMeetingType = calendarMeetingTypeRepository.findByMeetingTypeId(meetingModel.getMeetingTypeId());
	      
	          if(calendarMeetingType == null){
	           
	        	throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_MEETING_TYPE_NOT_FOUND, null);
	          }
	          log.info("Get cal types : " + (new Date().getTime() - temp));
	          temp = new Date().getTime();
	       
	          
	          UserKloudlessMeeting userKloudlessMeeting = new UserKloudlessMeeting();
	      
	          if(meetingModel.getFitwiseMeetingId() != null){
	             Optional<UserKloudlessMeeting> optionalMeeting = userKloudlessMeetingRepository
	                    .findByUserKloudlessCalendarAndUserKloudlessMeetingId(activeCalendar, meetingModel.getFitwiseMeetingId());
	               if (optionalMeeting.isPresent()) {
	                  userKloudlessMeeting = optionalMeeting.get();
	               }
	           }
	       
	        
	          userKloudlessMeeting.setUserKloudlessCalendar(activeCalendar);
	        
	          userKloudlessMeeting.setCalendarMeetingType(calendarMeetingType);
	       
	          userKloudlessMeeting.setUser(user);
	       
	          userKloudlessMeetingRepository.save(userKloudlessMeeting);
	         
	          CronofyAvailabilityRules cronofyAvailabilityRules = new CronofyAvailabilityRules();
	           
	          if(meetingModel.getCronofyavailabilityrulesid() != null){
		            Optional<CronofyAvailabilityRules> optionalRules = cronofyAvailabilityRulesRepository
		                    .findByUserKloudlessMeetingAndUserCronofyAvailabilityRulesId(userKloudlessMeeting, meetingModel.getCronofyavailabilityrulesid());
		               if (optionalRules.isPresent()) {
		            	   cronofyAvailabilityRules = optionalRules.get();
		              }
		      }
	           
	          cronofyAvailabilityRules.setUserKloudlessMeeting(userKloudlessMeeting);
	           
	          cronofyAvailabilityRulesRepository.save(cronofyAvailabilityRules);
	         
	         
	          log.info("Create/update kloudless meeting : " + (new Date().getTime() - temp));
	          temp = new Date().getTime();
	       
	          
	          CronofyMeetingModel cronofyMeetingModel = new CronofyMeetingModel();
	      
	          cronofyMeetingModel.setFitwiseMeetingId(userKloudlessMeeting.getUserKloudlessMeetingId());
	         
	          cronofyMeetingModel.setCalendarId(activeCalendar.getCalendarId());
	         
	          cronofyMeetingModel.setMeetingId(userKloudlessMeeting.getMeetingId());
	         
	          cronofyMeetingModel.setMeetingTypeId(calendarMeetingType.getMeetingTypeId());
	          
	          cronofyMeetingModel.setMeetingType(calendarMeetingType.getMeetingType());
	          
	          cronofyMeetingModel.setCronofyavailabilityrulesid(cronofyAvailabilityRules.getUserCronofyAvailabilityRulesId());
	       
	          log.info("Response construction : " + (new Date().getTime() - temp));
	          log.info("Create fitwise meeting completed : " + (new Date().getTime() - startTime));
	        
	          return cronofyMeetingModel;
	 }
	  
	 
	  public UserKloudlessCalendar getActiveCalendarFromKloudlessAccount(UserKloudlessAccount userKloudlessAccount){
	        List<UserKloudlessCalendar> userKloudlessCalendar = userKloudlessCalendarRepository
	                .findByUserKloudlessAccountAndPrimaryCalendar(userKloudlessAccount, true);
	        if(userKloudlessCalendar.isEmpty()){
	            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, CalendarConstants.CAL_ERR_CALENDAR_NOT_FOUND, null);
	        }
	        return userKloudlessCalendar.get(0);
	 }
	 
	  public CronofyMeetingModel updateMeeting(CronofyMeetingModel meetingModel) {
	      
		    log.info("Update meeting starts.");
	        long apiStartTimeMillis = new Date().getTime();
	       
	        if (ValidationUtils.isEmptyString(meetingModel.getName())
	                || meetingModel.getMeetingTypeId() <= 0
	                || meetingModel.getSessionDuration() < 0) {
	            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_MEETING_INVALID, null);
	        }
	       
	        if (ValidationUtils.isEmptyString(meetingModel.getTimeZone()) || !fitwiseUtils.isValidTimeZone(meetingModel.getTimeZone())) {
	            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_MEETING_TIMEZONE_INVALID, null);
	        }
	       
	        log.info(StringConstants.LOG_FIELD_VALIDATION + (new Date().getTime() - apiStartTimeMillis));
	        long profilingEndTimeMillis = new Date().getTime();
	      
	        User user = userComponents.getUser();
	      
	        UserKloudlessAccount account = getActiveAccount(user);
	       
	        UserKloudlessCalendar activeCalendar = getActiveCalendarFromKloudlessAccount(account);
	        
	        if(activeCalendar == null){
	            throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_CALENDAR_NOT_FOUND, null);
	        }
	      
	        CalendarMeetingType calendarMeetingType = calendarMeetingTypeRepository.findByMeetingTypeId(meetingModel.getMeetingTypeId());
	       
	        if(calendarMeetingType == null){
	            throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_MEETING_TYPE_NOT_FOUND, null);
	        }
	      
	            log.info("Query to get user and calendar meeting type : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	            profilingEndTimeMillis = new Date().getTime();
	       
	            Optional<UserKloudlessMeeting> optionalMeeting = userKloudlessMeetingRepository
	                .findByUserKloudlessCalendarAndUserKloudlessMeetingId(activeCalendar, meetingModel.getFitwiseMeetingId());
	       
	            if (!optionalMeeting.isPresent()) {
	             throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_MEETING_NOT_FOUND, null);
	            }
	       
	            log.info("Query to get user kloudless meeting : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	            profilingEndTimeMillis = new Date().getTime();
	       
	            UserKloudlessMeeting userKloudlessMeeting = optionalMeeting.get();
	         
	            log.info("Query to get accesstoken : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	            profilingEndTimeMillis = new Date().getTime();
	          
	            String refreshtokn = account.getRefreshToken();
	           
	            if(refreshtokn == null) {
				   throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED,null);
				} else 
				  {
					log.info("Refresh Token : " +refreshtokn);
				  }
	           
		       RefreshAccessTokenResponse refreshAccessTokenResponse = null;
		        
		       refreshAccessTokenResponse = cronofyService.refreshAccessToken(refreshtokn);
		        
		        if (refreshAccessTokenResponse == null) {
			 		throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED,null);
			    }
	         
	            log.info("Query to get accesstoken : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	            profilingEndTimeMillis = new Date().getTime();
			  
	            CreateavailabilityrulesResponse createavailabilityrulesResponse = null;
			  
			    RequestavailabilityrulesResponse requestavailabilityrulesResponse = new RequestavailabilityrulesResponse();
			    
			    Gson gson = new Gson();
			    
			    requestavailabilityrulesResponse.setAvailabilityRuleId(meetingModel.getCronofyavailabilityrulesid().toString());
			  
			    requestavailabilityrulesResponse.setTzid(meetingModel.getTimeZone());
			    
			    List<String> calendarIds = new ArrayList<>();
			       
			    calendarIds.add(meetingModel.getCalendarId());
			    
			    requestavailabilityrulesResponse.setCalendarIds(calendarIds);

			    requestavailabilityrulesResponse.setWeeklyPeriods(meetingModel.getWeeklyperiods());
			  
			    log.info("Query to create availability Rules : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	            profilingEndTimeMillis = new Date().getTime();
			  
	           createavailabilityrulesResponse = cronofyService.createavailabilityrules(requestavailabilityrulesResponse,refreshAccessTokenResponse.getAccessToken());
	          
	           if (createavailabilityrulesResponse == null) {
				        throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED,
						null);
				     } 
			     
	           log.info("Query to create availability Rules Response : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	           profilingEndTimeMillis = new Date().getTime();
	        
	           if (userKloudlessMeeting.getStartDate() == null && userKloudlessMeeting.getEndDate() == null) {
	            
	        	 LocalDate currentDate = LocalDate.now();
	            
	             Date startDate = Date.from(currentDate
	                    .atStartOfDay(ZoneId.of(meetingModel.getTimeZone()))
	                    .toInstant());
	           
	             Date endDate = Date.from(currentDate
	                    .plusDays(meetingModel.getSessionDuration())
	                    .atStartOfDay(ZoneId.of(meetingModel.getTimeZone()))
	                    .toInstant());
	             
	             	SimpleDateFormat end_date_sdf = new SimpleDateFormat("yyyy-MM-dd");   
					String set_end_Date = end_date_sdf.format(endDate);
					
					SimpleDateFormat end_time_sdf = new SimpleDateFormat("HH:mm:ss");   
					String set_end_Time = end_time_sdf.format(startDate);
					
					String set_end_Date_Time =  set_end_Date +" "+ set_end_Time;
					
					SimpleDateFormat end_Date_Time_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	   		        Date endDate_Final = null;
	   				    try {
	   				    	endDate_Final = end_Date_Time_sdf.parse(set_end_Date_Time);
	   				           } catch (ParseException e) {
	   					       // TODO Auto-generated catch block
	   					   e.printStackTrace();
	   			         }
	           
	            userKloudlessMeeting.setStartDate(startDate);
	            userKloudlessMeeting.setEndDate(endDate_Final);
	            userKloudlessMeeting.setStartDateInUtc(Date.from(startDate.toInstant().atZone(ZoneId.systemDefault()).toInstant()));
	            userKloudlessMeeting.setEndDateInUtc(Date.from(endDate_Final.toInstant().atZone(ZoneId.systemDefault()).toInstant()));
	            
	            }
	            userKloudlessMeeting.setTimeZone(meetingModel.getTimeZone());
	            userKloudlessMeeting.setCalendarMeetingType(calendarMeetingType);
	            userKloudlessMeeting.setMeetingId(meetingModel.getMeetingId());
	            userKloudlessMeeting.setName(meetingModel.getName());
	            userKloudlessMeeting.setEventDescription(meetingModel.getEventDescription());
	            
	            if(meetingModel.getAvailability() != null) {
		          
	            	userKloudlessMeeting.setAvailability(gson.toJson(meetingModel.getAvailability()));
		        }

	            
	            
	            
	       
	           TimeSpan timeSpan = new TimeSpan();
	           	       
	           timeSpan.setMinutes(meetingModel.getDurationInMinutes());
	       
	           timeSpanRepository.save(timeSpan);
	      
	           userKloudlessMeeting.setDuration(timeSpan);
	          
	           userKloudlessMeeting.setUser(user);
	          
	           userKloudlessMeeting.setMeetingDurationInDays(meetingModel.getSessionDuration());
	       
	           log.info("Query to save time span and construct user Cronofy meeting : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	           profilingEndTimeMillis = new Date().getTime();
	          
	           userKloudlessMeetingRepository.save(userKloudlessMeeting);
	           
	           log.info("Query to save user cronofy meeting : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	           profilingEndTimeMillis = new Date().getTime();
		         
	             
			  
	           Optional<CronofyAvailabilityRules> optionalRules = cronofyAvailabilityRulesRepository
	                    .findByUserKloudlessMeetingAndUserCronofyAvailabilityRulesId(userKloudlessMeeting, meetingModel.getCronofyavailabilityrulesid());
		       
		       if (!optionalRules.isPresent()) {
		            throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_MEETING_NOT_FOUND, null);
		        }
		       log.info("Query to get availibility rules : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	           profilingEndTimeMillis = new Date().getTime();
		         
		       CronofyAvailabilityRules cronofyAvailabilityRules = optionalRules.get();
		         
		        cronofyAvailabilityRules.setCalenderId(meetingModel.getCalendarId());
		        cronofyAvailabilityRules.setBufferAfter(meetingModel.getBufferafter());
		        cronofyAvailabilityRules.setBufferBefore(meetingModel.getBufferbefore());
		        cronofyAvailabilityRules.setStartInterval(meetingModel.getStartinterval());
		         
		        if (createavailabilityrulesResponse.getAvailabilityrule().getWeeklyPeriods() != null) {
		        	  cronofyAvailabilityRules.setWeeklyPeriods(gson.toJson(createavailabilityrulesResponse.getAvailabilityrule().getWeeklyPeriods()));
		        }
		        
		        cronofyAvailabilityRulesRepository.save(cronofyAvailabilityRules);
	          
	            log.info("Query to save user Cronofy meeting : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	            profilingEndTimeMillis = new Date().getTime();
	      
	          
	           CronofyMeetingModel cronofyMeetingModel = new CronofyMeetingModel();
	        
	           cronofyMeetingModel.setFitwiseMeetingId(userKloudlessMeeting.getUserKloudlessMeetingId());
	          
	           cronofyMeetingModel.setCalendarId(activeCalendar.getCalendarId());
	           
	           cronofyMeetingModel.setMeetingId(userKloudlessMeeting.getMeetingId());
	           
	           cronofyMeetingModel.setMeetingTypeId(calendarMeetingType.getMeetingTypeId());
	          
	           cronofyMeetingModel.setMeetingType(calendarMeetingType.getMeetingType());
	           
	           cronofyMeetingModel.setName(userKloudlessMeeting.getName());
	          
	           cronofyMeetingModel.setEventDescription(userKloudlessMeeting.getEventDescription());
	          
	           cronofyMeetingModel.setSessionDuration(userKloudlessMeeting.getMeetingDurationInDays());
	           
	           if(userKloudlessMeeting.getDuration().getMinutes() != null) {
		           cronofyMeetingModel.setDurationInMinutes(userKloudlessMeeting.getDuration().getMinutes());
		       }
	          
	           cronofyMeetingModel.setTimeZone(userKloudlessMeeting.getTimeZone());
	           
	           cronofyMeetingModel.setCronofyavailabilityrulesid(cronofyAvailabilityRules.getUserCronofyAvailabilityRulesId());
	          
	           cronofyMeetingModel.setBufferafter(cronofyAvailabilityRules.getBufferAfter());
	           
              cronofyMeetingModel.setBufferbefore(cronofyAvailabilityRules.getBufferBefore());
          
              cronofyMeetingModel.setStartinterval(cronofyAvailabilityRules.getStartInterval());
	           
	           if(cronofyAvailabilityRules.getWeeklyPeriods() != null) {
	        	   List<Object> list = (List<Object>) Arrays.asList(new GsonBuilder().create().fromJson(cronofyAvailabilityRules.getWeeklyPeriods(), Object[].class));
                   cronofyMeetingModel.setWeeklyperiods(list); 
	           }
	           if(userKloudlessMeeting.getAvailability() != null) {
	        	   List<Object> list = (List<Object>) Arrays.asList(new GsonBuilder().create().fromJson(userKloudlessMeeting.getAvailability(), Object[].class));
	        	   cronofyMeetingModel.setAvailability(list); 
	          }
           
	       
	           log.info("Response construction : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	          
	           log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
	          
	           log.info("Update meeting ends.");
	        
	           return cronofyMeetingModel;
	    }
	  
	  
	  public void deleteMeeting(final Long fitwiseMeetingId,final Long availabilityRuleId) {
	      
    	  User user = userComponents.getUser();
        
    	 
    	  UserKloudlessAccount account = getActiveAccount(user);
        
    	  
    	  UserKloudlessCalendar activeCalendar = getActiveCalendarFromKloudlessAccount(account);
       
    	 
    	  if(activeCalendar == null){
             throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_CALENDAR_NOT_FOUND, null);
          }
        
    	
    	  Optional<UserKloudlessMeeting> optionalMeeting = userKloudlessMeetingRepository
                 .findByUserKloudlessCalendarAndUserKloudlessMeetingId(activeCalendar, fitwiseMeetingId);
    	   
    	   if (!optionalMeeting.isPresent()) {
               throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_MEETING_NOT_FOUND, null);
           }
        
           UserKloudlessMeeting userKloudlessMeeting = optionalMeeting.get();
         
	       List<CronofyAvailabilityRules> cronofyAvailabilityRules = cronofyAvailabilityRulesRepository
        		   .findByUserKloudlessMeeting(userKloudlessMeeting);
	      
           if (cronofyAvailabilityRules == null) {
        	  
        	   throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_MEETING_RUELS_NOT_FOUND, null);
           }
	      

            // if meeting used in any subscription package prevent deletion
            List<PackageKloudlessMapping> packageKloudlessMappings = packageKloudlessMappingRepository.findByUserKloudlessMeeting(userKloudlessMeeting);
       
          
            if(packageKloudlessMappings != null && !packageKloudlessMappings.isEmpty()){
            
        	  throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.MSG_CAL_DELETE_USED_MEETING, null);
            }
            
             RefreshAccessTokenResponse refreshAccessTokenResponse = null;
	        
	         refreshAccessTokenResponse = cronofyService.refreshAccessToken(account.getRefreshToken());
	        
	          if (refreshAccessTokenResponse == null) {
		 		throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED,null);
		      }
		   
		     if (!ValidationUtils.isEmptyString(availabilityRuleId.toString())) {
			 
			    try {
				      
			    	   cronofyService.deleteavailabilityrules(refreshAccessTokenResponse.getAccessToken(), availabilityRuleId.toString());
	               
			        } catch (ApplicationException ae) {
	                  
			        	log.info("Cronofy meeting window deletion failed");
	                }  
		      }
		       else
		       {
			   throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_EER_AVAILABILITY_LINKING_FAILED, null);
		       }
		   
		   cronofyAvailabilityRulesRepository.deleteAll(cronofyAvailabilityRules);
           
		   userKloudlessMeetingRepository.delete(userKloudlessMeeting);
         
    }
	  
       public List<Zone> getZones(){
		  
		  List<Zone> zones = zoneRepository.findAll();
	       
		  if (zones.isEmpty()) {
	            throw new ApplicationException(Constants.NOT_FOUND, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
	        }
		 
		  
		  return zones;
	   }
      
       public CronofyMeetingListModel getMeetings(final int pageNo, final int pageSize, Optional<Long> meetingType) {
	        
	        User user = userComponents.getUser();
	      
	        UserKloudlessAccount userKloudlessAccount = getActiveAccount(user);
	       
	        UserKloudlessCalendar userKloudlessCalendar = getActiveCalendarFromKloudlessAccount(userKloudlessAccount);
	       
	        if(userKloudlessCalendar == null){
	            throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_CALENDAR_NOT_FOUND, null);
	        }
	        PageRequest pageRequest = PageRequest.of(pageNo <= 0 ? 0 : pageNo - 1, pageSize);
	       
	        Page<UserKloudlessMeeting> userKloudlessMeetings;
	       
	        if (meetingType.isPresent() && meetingType.get() > 0) {
	            CalendarMeetingType calendarMeetingType = calendarMeetingTypeRepository.findByMeetingTypeId(meetingType.get());
	            
	            if(calendarMeetingType == null){
	                throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_MEETING_TYPE_NOT_FOUND, null);
	            }
	            userKloudlessMeetings = userKloudlessMeetingRepository
	                    .findByNameNotNullAndUserKloudlessCalendarAndCalendarMeetingType(userKloudlessCalendar, calendarMeetingType, pageRequest);
	         } else {
	            userKloudlessMeetings = userKloudlessMeetingRepository
	                    .findByNameNotNullAndUserKloudlessCalendar(userKloudlessCalendar, pageRequest);
	         }
	         if(userKloudlessMeetings.isEmpty()){
	            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, "", null);
	         }
	        // Gson gson = new Gson();
	        
	        
		      
	         
	          List<CronofyMeetingModel> CronofyMeetingModels = new ArrayList<>();
	         
	          for(UserKloudlessMeeting userKloudlessMeeting : userKloudlessMeetings){
	        	  CronofyMeetingModel cronofyMeetingModel = new CronofyMeetingModel();
	           
	        	  List<PackageKloudlessMapping> packageKloudlessMappings = packageKloudlessMappingRepository.findByUserKloudlessMeeting(userKloudlessMeeting);
	          
	            if(!packageKloudlessMappings.isEmpty()){
	            	cronofyMeetingModel.setUsedInPackage(true);
	            }
	            cronofyMeetingModel.setFitwiseMeetingId(userKloudlessMeeting.getUserKloudlessMeetingId());
	            cronofyMeetingModel.setCalendarId(userKloudlessCalendar.getCalendarId());
	           // cronofyMeetingModel.setDurationInMinutes(userKloudlessMeeting);
	            cronofyMeetingModel.setMeetingTypeId(userKloudlessMeeting.getCalendarMeetingType().getMeetingTypeId());
	            cronofyMeetingModel.setMeetingType(userKloudlessMeeting.getCalendarMeetingType().getMeetingType());
	            cronofyMeetingModel.setName(userKloudlessMeeting.getName());
	            cronofyMeetingModel.setSessionDuration(userKloudlessMeeting.getMeetingDurationInDays());
	            if(userKloudlessMeeting.getDuration() != null){
	            	cronofyMeetingModel.setDurationInMinutes(userKloudlessMeeting.getDuration().getMinutes());
	            }
	            cronofyMeetingModel.setTimeZone(userKloudlessMeeting.getTimeZone());
	            cronofyMeetingModel.setEventDescription(userKloudlessMeeting.getEventDescription());
	            if(userKloudlessMeeting.getAvailability() != null) {
		        	   List<Object> list = (List<Object>) Arrays.asList(new GsonBuilder().create().fromJson(userKloudlessMeeting.getAvailability(), Object[].class));
		        	   cronofyMeetingModel.setAvailability(list); 
		        }
	            List<CronofyAvailabilityRules> cronofyAvailabilityRules = cronofyAvailabilityRulesRepository
		        		   .findByUserKloudlessMeeting(userKloudlessMeeting);
	            
	            for(CronofyAvailabilityRules cronofyAvailability : cronofyAvailabilityRules){
	            	cronofyMeetingModel.setCronofyavailabilityrulesid(cronofyAvailability.getUserCronofyAvailabilityRulesId());
			          
			        cronofyMeetingModel.setBufferafter(cronofyAvailability.getBufferAfter());
			           
			        cronofyMeetingModel.setBufferbefore(cronofyAvailability.getBufferBefore());
			          
			        cronofyMeetingModel.setStartinterval(cronofyAvailability.getStartInterval());
			        
			        if(cronofyAvailability.getWeeklyPeriods() != null) {
				        List<Object> list = (List<Object>) Arrays.asList(new GsonBuilder().create().fromJson(cronofyAvailability.getWeeklyPeriods(), Object[].class));
				        cronofyMeetingModel.setWeeklyperiods(list);
			        }
			           

			        
	            }
	            CronofyMeetingModels.add(cronofyMeetingModel);
	        }
	          CronofyMeetingListModel meetingsList = new CronofyMeetingListModel();
	          meetingsList.setMeetings(CronofyMeetingModels);
	          meetingsList.setTotalCount(userKloudlessMeetings.getTotalElements());
	        return meetingsList;
	    }
       
       
       
       public RealtimeScheduleResponse CreateScheduleInstance(final KloudlessScheduleModel kloudlessScheduleModel) {
	    	 
	    	 
	    	 log.info("Create schedule starts.");
	         long apiStartTimeMillis = new Date().getTime();
	         
	        
	         User user = userComponents.getUser();
	         
	         String member_Account_ID = "";
	         String member_Calendar_ID = "";
			 UserKloudlessAccount account_member = getActiveAccount(user);
			 if(account_member != null){
				 UserKloudlessCalendar activeCalendar_member = getActiveCalendarFromKloudlessAccount(account_member);
				 if(activeCalendar_member != null){
					 member_Account_ID = account_member.getAccountId();
			         member_Calendar_ID = activeCalendar_member.getCalendarId();
			     }
		     }
			 
			   String redirectUri = kloudlessScheduleModel.getRedirectUri();
		          String toRemove = "/events";
		          if (redirectUri.contains(toRemove)) {
		        	  redirectUri=redirectUri.replaceAll(toRemove, "");
		        	}
	         
	         log.info("Basic validations with getting user : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
	         long profilingEndTimeMillis = new Date().getTime();
	    	 
	         
	         SubscriptionPackage subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageId(kloudlessScheduleModel.getSubscriptionPackageId());
	        
	         
	         ValidationUtils.throwException(subscriptionPackage == null,
	                 ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_ID_NULL, Constants.BAD_REQUEST);
	        
	         
	         log.info("Query to get subscription package : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	         profilingEndTimeMillis = new Date().getTime();
	        
	        
	         Optional<PackageKloudlessMapping> optionalPackageSession = packageKloudlessMappingRepository
	                 .findBySessionMappingId(kloudlessScheduleModel.getPackageSessionMappingId());
       
          
	         if(!optionalPackageSession.isPresent() || optionalPackageSession.get() == null){
	             throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_SESSION_NOT_FOUND, null);
	         }
	     	 
           log.info("Query to get package session : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	         profilingEndTimeMillis = new Date().getTime();
	         
	        
	         UserKloudlessMeeting  userKloudlessMeeting = optionalPackageSession.get().getUserKloudlessMeeting();
	        
	        
	         UserKloudlessCalendar userKloudlessCalendar = userKloudlessMeeting.getUserKloudlessCalendar();
	         
	         
	         UserKloudlessAccount  userKloudlessAccount = userKloudlessCalendar.getUserKloudlessAccount();
	         
	         
	         Optional<UserKloudlessMeeting> userKloudlessMeetings = userKloudlessMeetingRepository
	                 .findByUserKloudlessCalendarAndUserKloudlessMeetingId(userKloudlessCalendar, kloudlessScheduleModel.getFitwiseMeetingId());
	        
	         if(!userKloudlessMeetings.isPresent() || userKloudlessMeetings.get() == null){
	             throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_MEETING_NOT_FOUND, null);
	         }
	         log.info("Query to get user kloudless meeting : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	         profilingEndTimeMillis = new Date().getTime();
	       
	         CalendarMeetingType calendarMeetingType;
	         if (kloudlessScheduleModel.getFitwiseMeetingTypeId() == null || kloudlessScheduleModel.getFitwiseMeetingTypeId() == 0) {
	             // get session type from meeting
	             calendarMeetingType = userKloudlessMeetings.get().getCalendarMeetingType();
	         } else {
	             // find the passed session type
	             calendarMeetingType = calendarMeetingTypeRepository.findByMeetingTypeId(kloudlessScheduleModel.getFitwiseMeetingTypeId());
	             ValidationUtils.throwException(calendarMeetingType == null,
	                     CalendarConstants.CAL_ERR_MEETING_TYPE_NOT_FOUND, Constants.BAD_REQUEST);
	         }
	       
	         log.info("Query to get calendar meeting type : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	         profilingEndTimeMillis = new Date().getTime();
	         // if session type is "In-Person or Virtual" choose virtual session
	         if (CalendarConstants.SESSION_IN_PERSON_OR_VIRTUAL == calendarMeetingType.getMeetingTypeId()) {
	             calendarMeetingType = calendarMeetingTypeRepository
	                     .findByMeetingTypeId(CalendarConstants.MEETING_TYPE_VIRTUAL);
	         }
	        
	         log.info("Query to get calendar meeting type : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	         profilingEndTimeMillis = new Date().getTime();
	       
	         List<InstructorUnavailability> instructorUnavailabilities = new ArrayList<>();
             
	         
	         List<InstructorUnavailability> instructorUnavailabilitiesApp = instructorUnavailabilityRepository.findByUserKloudlessCalendar(userKloudlessCalendar);
	         if (!instructorUnavailabilitiesApp.isEmpty()) 
             {
	        	 for(InstructorUnavailability instructorUnavailabilityApp : instructorUnavailabilitiesApp){
	        		 InstructorUnavailability instructorUnavailability = new InstructorUnavailability();
	        		 instructorUnavailability.setStartDate(instructorUnavailabilityApp.getStartDate());
	        		 instructorUnavailability.setEndDate(instructorUnavailabilityApp.getEndDate());
	        		 instructorUnavailabilities.add(instructorUnavailability);
	        	 }
             }
	         List<InstructorUnavailability> instructorUnavailabilitiesFreeBusy = getFreeBusyUnavailabilities(kloudlessScheduleModel,userKloudlessAccount,userKloudlessCalendar,userKloudlessMeeting);
	         if (!instructorUnavailabilitiesFreeBusy.isEmpty()) 
             {
	        	 for(InstructorUnavailability instructorUnavailabilityFreeBusy : instructorUnavailabilitiesFreeBusy){
	        		 InstructorUnavailability instructorUnavailability = new InstructorUnavailability();
	        		 instructorUnavailability.setStartDate(instructorUnavailabilityFreeBusy.getStartDate());
	        		 instructorUnavailability.setEndDate(instructorUnavailabilityFreeBusy.getEndDate());
	        		 instructorUnavailabilities.add(instructorUnavailability);
	        	 }
             }
	         List<InstructorUnavailability> memberUnavailabilitiesApp = getMemberUnavailabilities(kloudlessScheduleModel,userKloudlessAccount,user,userKloudlessMeeting);
	         if (!memberUnavailabilitiesApp.isEmpty()) 
             {
	        	 for(InstructorUnavailability memberUnavailabilityApp : memberUnavailabilitiesApp){
	        		 InstructorUnavailability memberUnavailability = new InstructorUnavailability();
	        		 memberUnavailability.setStartDate(memberUnavailabilityApp.getStartDate());
	        		 memberUnavailability.setEndDate(memberUnavailabilityApp.getEndDate());
	        		 instructorUnavailabilities.add(memberUnavailability);
	        	 }
             }
	         instructorUnavailabilities.sort(Comparator.comparing(InstructorUnavailability::getStartDate));
             log.info("Query to get cronofy availabilityrules : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
		       profilingEndTimeMillis = new Date().getTime();
			  
		       List<CronofyAvailabilityRules> cronofyAvailabilityRules = cronofyAvailabilityRulesRepository
	        		   .findByUserKloudlessMeeting(userKloudlessMeeting);
			  
			   log.info("Query to get cronofy availabilityrules : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
		       profilingEndTimeMillis = new Date().getTime();
			  
		       
		      CronofyMeetingModel cronofyMeetingModel = new CronofyMeetingModel();
              
			   
			     for(CronofyAvailabilityRules cronofyAvailability : cronofyAvailabilityRules){
           	
           	      cronofyMeetingModel.setCronofyavailabilityrulesid(cronofyAvailability.getUserCronofyAvailabilityRulesId());
		          
		          cronofyMeetingModel.setBufferafter(cronofyAvailability.getBufferAfter());
		           
		          cronofyMeetingModel.setBufferbefore(cronofyAvailability.getBufferBefore());
		          
		          cronofyMeetingModel.setStartinterval(cronofyAvailability.getStartInterval());
		        
		          if(cronofyAvailability.getWeeklyPeriods() != null) {
			        List<Object> list = (List<Object>) Arrays.asList(new GsonBuilder().create().fromJson(cronofyAvailability.getWeeklyPeriods(), Object[].class));
			         cronofyMeetingModel.setWeeklyperiods(list);
		           }
		         }
			   
			   
			     CronofyAvailabilityRules cronofyAvailabilityRule = cronofyAvailabilityRulesRepository
                  .findByUserCronofyAvailabilityRulesId(cronofyMeetingModel.getCronofyavailabilityrulesid());
    
                 if (cronofyAvailabilityRule==null) 
	              {
	                throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_MEETING_NOT_FOUND, null);
	               }
             
                 List<RealtimeSchedulequeryperiods> Schedulequeryperiods = new ArrayList<>();
              
                 Date bookingDate = kloudlessScheduleModel.getBookingDate();
			  
	             SimpleDateFormat df = new SimpleDateFormat( "EEEE" ); 
			 
                 String day= df.format( bookingDate ); 
	            
	             SimpleDateFormat dfymd = new SimpleDateFormat( "yyyy-MM-dd" );
              
	             String ymd = dfymd.format( bookingDate ); 
           
	             JSONArray jsonArray = new JSONArray(cronofyAvailabilityRule.getWeeklyPeriods());
    
                 for (int i = 0; i < jsonArray.length(); i++) {
	    
	                  JSONObject jsonObject = jsonArray.getJSONObject(i);
	   
	                  String  weeklyDay = jsonObject.getString("day");
	   
	   
	                 if(weeklyDay.toLowerCase().equals(day.toLowerCase())) {
	        
	    	          String  starttime = jsonObject.getString("start_time");
	    	      
	    	          String  endttime = jsonObject.getString("end_time");
	    	     
	    	          String  startdaytime = ymd+" "+starttime;

                      String  enddaytime = ymd+" "+endttime;
             
                    //String  start
                    RealtimeSchedulequeryperiods realtimeSchedulequeryperiods = new RealtimeSchedulequeryperiods();
                        realtimeSchedulequeryperiods.setStart(GetFormatDate(startdaytime,userKloudlessMeeting.getTimeZone()));
			            realtimeSchedulequeryperiods.setEnd(GetFormatDate(enddaytime,userKloudlessMeeting.getTimeZone()));
			            Schedulequeryperiods.add(realtimeSchedulequeryperiods);
			          }
	             }
                 
                 if(Schedulequeryperiods.isEmpty()) {
 		        	
		        	 throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_AVAILABILITY_RULES_NOT_FOUND, null);
		         }
                 PackageSubscription packageSubscription = packageSubscriptionRepository.findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageIdOrderBySubscribedDateDesc(user.getUserId(), kloudlessScheduleModel.getSubscriptionPackageId());
                 Schedulequeryperiods.addAll(GetSchedulePeriodsForNext7Days(bookingDate,userKloudlessMeeting,cronofyAvailabilityRule,packageSubscription,user,subscriptionPackage));
                 
                 List<RealtimeSchedulequeryperiods> Schedulequeryperiodsforcronofy = new ArrayList<>();
                 
                 if (!instructorUnavailabilities.isEmpty()) 
	              {
          	         Schedulequeryperiodsforcronofy = GetScheduleQueryPeriods(Schedulequeryperiods,instructorUnavailabilities,userKloudlessMeeting);
	              }
                 
                 List<RealtimeSchedulequeryperiods> schedulequeryperiodsinCurrentDate= new ArrayList<>(); 
                 
                 
                 SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    Date datelocal = new Date();
                    String ymdcurrent = formatter.format( datelocal );
               
                 SimpleDateFormat sdfbookingday = new SimpleDateFormat("yyyy-MM-dd");
                
                  Date datebooking = null;
                  try {
                	  datebooking = sdfbookingday.parse(ymd);
 			         
 	                } catch (ParseException e) {
 			          // TODO Auto-generated catch block
 			         e.printStackTrace();
 		            }
                 
                 SimpleDateFormat sdfcurrentday = new SimpleDateFormat("yyyy-MM-dd");
                
                  Date datecurrent = null;
                      try {
                    	  datecurrent = sdfcurrentday.parse(ymdcurrent);
 			         
 	                       } catch (ParseException e) {
 			               // TODO Auto-generated catch block
 			                e.printStackTrace();
 		                   }

                 
                  if(datebooking.equals(datecurrent)) {
                    	
                	   if (!Schedulequeryperiodsforcronofy.isEmpty()) {
                           	  schedulequeryperiodsinCurrentDate = getisdayqueryPeriods(Schedulequeryperiodsforcronofy,userKloudlessMeeting);
                         }
                       else
                         {
                        	  schedulequeryperiodsinCurrentDate = getisdayqueryPeriods(Schedulequeryperiods,userKloudlessMeeting);
                         } 
                   }
                 
                 
                 
                 
	        
	             UserKloudlessSchedule userKloudlessSchedule = new UserKloudlessSchedule();
	        
	             if (kloudlessScheduleModel.getFitwiseScheduleId() == null || kloudlessScheduleModel.getFitwiseScheduleId() <= 0) {
	        	
	        	 if(kloudlessScheduleModel.getFitwiseMeetingId() != null){
		        	  
	   	          Optional<UserKloudlessSchedule> optionalSchedule = userKloudlessScheduleRepository.findByUserKloudlessMeetingAndUserKloudlessScheduleId(userKloudlessMeeting,kloudlessScheduleModel.getFitwiseScheduleId());   
	   	        
	   	          if (optionalSchedule.isPresent()) {
	   	            	     userKloudlessSchedule = optionalSchedule.get();
	   	                }
	   	          }    
	   	          userKloudlessSchedule.setUserKloudlessMeeting(userKloudlessMeeting);
	   	           
	   	          userKloudlessSchedule.setUser(user);
	   	           
	   	          userKloudlessSchedule.setSubscriptionPackage(subscriptionPackage);
	   	            
	   	          if(optionalPackageSession.isPresent() && optionalPackageSession.get() != null){
	   	                userKloudlessSchedule.setPackageKloudlessMapping(optionalPackageSession.get());
	   	            }
	   	          userKloudlessSchedule.setMeetingTypeId(calendarMeetingType);
	   	         
	   	          if(kloudlessScheduleModel.getBookingDate() != null){
	   	        	 

	   	        	  SimpleDateFormat outputsdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);   
		 		     
					  String setbookingDate = outputsdf.format(kloudlessScheduleModel.getBookingDate());
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
	   	            }
	   	          
	   	          
	   	             userKloudlessScheduleRepository.save(userKloudlessSchedule);
	               }else {
	                   Optional<UserKloudlessSchedule> optionalSchedule = userKloudlessScheduleRepository.findById(kloudlessScheduleModel.getFitwiseScheduleId());
	                   userKloudlessSchedule = optionalSchedule.get();
	               }
		      
	         
	          
	          
	           
	          
	          ValidationUtils.throwException(userKloudlessAccount == null,
	                 CalendarConstants.CAL_ERR_ACCOUNT_NOT_FOUND, Constants.BAD_REQUEST);

	          RealtimeScheduleResponse realtimeScheduleResponse = null;
	    	   
	           
	          CreateRealtimeScheduleResponse createRealtimeScheduleResponse = new CreateRealtimeScheduleResponse();
	          
	          RealtimeScheduleoauth realtimeScheduleoauth = new RealtimeScheduleoauth();
	              realtimeScheduleoauth.setRedirectUri(redirectUri);
	              createRealtimeScheduleResponse.setRealtimeScheduleoauth(realtimeScheduleoauth);
	           
	          
	          RealtimeScheduleevent realtimeScheduleevent = new RealtimeScheduleevent();
	        		  
	                 //  realtimeScheduleevent.setEventId(userKloudlessSchedule.getUserKloudlessScheduleId().toString());
	                   if (kloudlessScheduleModel.getFitwiseScheduleId() == null || kloudlessScheduleModel.getFitwiseScheduleId() <= 0) {
	                	  
	                
	        			   
	        	            realtimeScheduleevent.setEventId(userKloudlessSchedule.getUserKloudlessScheduleId().toString());
	                      }else {
		        	         realtimeScheduleevent.setEventId(kloudlessScheduleModel.getFitwiseScheduleId().toString());

	                    }
	        		  
	                   UserProfile userProfilemember = userProfileRepository.findByUser(userKloudlessSchedule.getUser());
        			   String memberFirstName = userProfilemember.getFirstName();
        			   String memberlastName = userProfilemember.getLastName();
        			   String memberFullName = memberFirstName +" "+ memberlastName;
        			   
        			   User instructor = userKloudlessSchedule.getUserKloudlessMeeting().getUser();
    	               String instructorFullName = fitwiseUtils.getUserFullName(instructor);
        			   
	                   realtimeScheduleevent.setSummary(userKloudlessMeeting.getName()+" with "+instructorFullName+" for "+ memberFullName);
	        		   realtimeScheduleevent.setTzid(userKloudlessMeeting.getTimeZone());
	        		   
	        		   if (userKloudlessMeeting.getEventDescription() != null) {
	        			   
	        			 
	        			   
	                       realtimeScheduleevent.setDescription(userKloudlessMeeting.getEventDescription());
	        		   }else {
	                         realtimeScheduleevent.setDescription("N/A");
	        		   }
	            
	        		   if(optionalPackageSession.isPresent() && optionalPackageSession.get() != null){
	        			   if (optionalPackageSession.get().getMeetingUrl() != null) {
	        				   RealtimeScheduleConferencing realtimeScheduleConferencing = new RealtimeScheduleConferencing();
	        				   realtimeScheduleConferencing.setProfile_id("explicit");
	        				   realtimeScheduleConferencing.setProvider_description("Virtual Session");
	        				   realtimeScheduleConferencing.setJoin_url(optionalPackageSession.get().getMeetingUrl());
	        				   realtimeScheduleevent.setRealtimeScheduleConferencing(realtimeScheduleConferencing);
	                         }
	                   }
	        		   
	        		   if(userKloudlessSchedule.getPackageKloudlessMapping().getLocation() != null){
	        			   String full_Address = "";
	        			   if(userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getAddress() != null){
	        				   full_Address = userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getAddress()+", ";
		        		   }
	        			   if(userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getCity() != null){
	        				   full_Address = full_Address+userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getCity()+", ";
		        		   }
	        			   if(userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getZipcode() != null){
	        				   full_Address = full_Address+userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getZipcode()+", ";
		        		   }
	        			   if(userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getState() != null){
	        				   full_Address = full_Address+userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getState()+", ";
		        		   }
	        			   if(userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getCountry().getCountryName() != null){
	        				   full_Address = full_Address+userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getCountry().getCountryName();
		        		   }
	        			   if(userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getLandMark() != null){
	        				   full_Address = full_Address+", "+userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getLandMark();
		        		   }
	        			   RealtimeScheduleLocation realtimeScheduleLocation = new RealtimeScheduleLocation(); 
        				   realtimeScheduleLocation.setDescription(full_Address);
        				   realtimeScheduleevent.setRealtimeScheduleLocation(realtimeScheduleLocation);
	        		   }
	          
	          createRealtimeScheduleResponse.setRealtimeScheduleevent(realtimeScheduleevent);
	           
	          
	          RealtimeSchedulemembers realtimeSchedulemembers = new RealtimeSchedulemembers();
	          
	          List<String> calendarIds = new ArrayList<>();
			 
	          calendarIds.add(userKloudlessCalendar.getCalendarId());
			  
	          realtimeSchedulemembers.setSub(userKloudlessAccount.getSub());
			 
	          realtimeSchedulemembers.setCalendarids(calendarIds);
	          
	          
	          List<RealtimeSchedulemembers> Schedulemembers = new ArrayList<>();
	          
              Schedulemembers.add(realtimeSchedulemembers);
	          
              if((member_Account_ID.length() > 0) && (member_Calendar_ID.length() > 0 ))
              {
            	  RealtimeSchedulemembers member_realtimeSchedulemembers = new RealtimeSchedulemembers();
            	  
            	  List<String> member_calendarIds = new ArrayList<>();
     			 
            	  member_calendarIds.add(member_Calendar_ID);
    			  
    	          member_realtimeSchedulemembers.setSub(member_Account_ID);
    			 
    	          member_realtimeSchedulemembers.setCalendarids(member_calendarIds);
    	          
                  Schedulemembers.add(member_realtimeSchedulemembers);
              }
	          
              RealtimeScheduleparticipants realtimeScheduleparticipants = new RealtimeScheduleparticipants();
	             
			  realtimeScheduleparticipants.setMembers(Schedulemembers);
			   
			  realtimeScheduleparticipants.setRequired("all");
			  
			  
			  RealtimeSchedulerequiredduration realtimeSchedulerequiredduration = new RealtimeSchedulerequiredduration();
			    
			  realtimeSchedulerequiredduration.setMinutes(userKloudlessMeeting.getDuration().getMinutes().toString());
			 
			 
			   

		         
		      
		   
	           List<RealtimeScheduleparticipants> Scheduleparticipants = new ArrayList<>();  
			      Scheduleparticipants.add(realtimeScheduleparticipants);
			      
			   RealtimeSchedulestartinterval realtimeSchedulestartinterval = new RealtimeSchedulestartinterval();
			   realtimeSchedulestartinterval.setMinutes("15");
			   
			   RealtimeSchedulebuffer RealtimeSchedulebuffer = new RealtimeSchedulebuffer();
			    
			  
			   RealtimeSchedulebufferbefore realtimeSchedulebufferbefore = new RealtimeSchedulebufferbefore();
			  
			    if(cronofyMeetingModel.getBufferbefore() != null) {
				   realtimeSchedulebufferbefore.setMinutes(cronofyMeetingModel.getBufferbefore());
			     }else{
				   realtimeSchedulebufferbefore.setMinutes("0");
				}
			    
              RealtimeSchedulebufferafter realtimeSchedulebufferafter = new RealtimeSchedulebufferafter();
			    if(cronofyMeetingModel.getBufferafter() != null) {
			    	realtimeSchedulebufferafter.setMinutes(cronofyMeetingModel.getBufferafter());
			    }else {
			    	realtimeSchedulebufferafter.setMinutes("0");
			    }
			    
			    
			    
			  
			  
			   RealtimeSchedulebuffer.setBufferafter(realtimeSchedulebufferafter);
			 
			   RealtimeSchedulebuffer.setBufferbefore(realtimeSchedulebufferbefore);
			  
			  
			   RealtimeScheduleavailability realtimeScheduleavailability = new RealtimeScheduleavailability();
	           
	          
			      realtimeScheduleavailability.setParticipants(Scheduleparticipants);		    
	          
	              realtimeScheduleavailability.setRequiredduration(realtimeSchedulerequiredduration);	
	             
	              if(datebooking.equals(datecurrent)) {
	            	  realtimeScheduleavailability.setQueryperiods(schedulequeryperiodsinCurrentDate);
	            	  realtimeScheduleavailability.setMaxresults(String.valueOf(schedulequeryperiodsinCurrentDate.size()));
	               }else {
	            	   if (!instructorUnavailabilities.isEmpty()) {
	 	            	  if (!Schedulequeryperiodsforcronofy.isEmpty()) {
	 	            	       realtimeScheduleavailability.setQueryperiods(Schedulequeryperiodsforcronofy);
	 	            	      realtimeScheduleavailability.setMaxresults(String.valueOf(Schedulequeryperiodsforcronofy.size()));
	 	            	  }else {
	 	            		   realtimeScheduleavailability.setQueryperiods(Schedulequeryperiods); 
	 	            		  realtimeScheduleavailability.setMaxresults(String.valueOf(Schedulequeryperiods.size()));
	 	            	  }
	 	            	  
	 	              }else {
        					  if(Schedulequeryperiodsforcronofy.isEmpty()) {
        						  realtimeScheduleavailability.setQueryperiods(Schedulequeryperiods);
        						  realtimeScheduleavailability.setMaxresults(String.valueOf(Schedulequeryperiods.size()));
        					  
        					  }
	 	            	       
	 	            	         
	 	              }  
	               }
	             
	         
	            
	              realtimeScheduleavailability.setStartinterval(realtimeSchedulestartinterval);
   	              realtimeScheduleavailability.setBuffer(RealtimeSchedulebuffer);	
   	              
   	              
   	              
   	     
	        		  
	          
	           
	           createRealtimeScheduleResponse.setRealtimeScheduleavailability(realtimeScheduleavailability);
	          
	           RealtimeScheduletargetcalendars realtimeScheduletargetcalendars = new  RealtimeScheduletargetcalendars();
	          
	           realtimeScheduletargetcalendars.setSub(userKloudlessAccount.getSub());
	          
	           realtimeScheduletargetcalendars.setCalendarId(userKloudlessCalendar.getCalendarId());
	          
	           List<RealtimeScheduletargetcalendars> Scheduletargetcalendars = new ArrayList<>();
	          
	           Scheduletargetcalendars.add(realtimeScheduletargetcalendars);
	           
	           if((member_Account_ID.length() > 0) && (member_Calendar_ID.length() > 0 ))
	           {	                  
	                  RealtimeScheduletargetcalendars member_realtimeScheduletargetcalendars = new  RealtimeScheduletargetcalendars();
	    	          
	                  member_realtimeScheduletargetcalendars.setSub(member_Account_ID);
	   	          
	                  member_realtimeScheduletargetcalendars.setCalendarId(member_Calendar_ID);
	                  
	                  Scheduletargetcalendars.add(member_realtimeScheduletargetcalendars);
	            }
	           
	           createRealtimeScheduleResponse.setTargetcalendars(Scheduletargetcalendars);
	           
	           createRealtimeScheduleResponse.setCallbackUrl(redirectUri);
	           
	           
	           RealtimeScheduleredirecturls realtimeScheduleredirecturls= new RealtimeScheduleredirecturls();
	           
	            realtimeScheduleredirecturls.setCompletedUrl(redirectUri);
	         
	            createRealtimeScheduleResponse.setRealtimeScheduleredirecturls(realtimeScheduleredirecturls);
	         
	           
	            realtimeScheduleResponse = cronofyService.createscheduleInstance(createRealtimeScheduleResponse);
		          
	        
	           
	            if (realtimeScheduleResponse == null) {
				        throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED,
						null);
		         } 
			     
	            log.info("Query to create availability Rules Response : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	            profilingEndTimeMillis = new Date().getTime();
	        
	          
	          
	    	
	    	  
	            return realtimeScheduleResponse;
	    	   
	    }
       
       private List<RealtimeSchedulequeryperiods> getisdayqueryPeriods(List<RealtimeSchedulequeryperiods> schedulequeryperiods, UserKloudlessMeeting userKloudlessMeeting) {
   	      
         Date date = new Date();
         OffsetDateTime currentdatetime = date.toInstant().atOffset(ZoneOffset.UTC);
         String currentdatestring = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(currentdatetime);
        
         Date datecurent = null;
      
         SimpleDateFormat sdfcurent = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
       
          try {
     	 datecurent = sdfcurent.parse(currentdatestring);
	         
           } catch (ParseException e) {
	          // TODO Auto-generated catch block
	           e.printStackTrace();
           }


        List<RealtimeSchedulequeryperiods>  schedulequeryperiodsForPass = new ArrayList<>();
        
   	    for(RealtimeSchedulequeryperiods schedulequeryperiod: schedulequeryperiods) {
      	             Date datestart = null;
	 	             SimpleDateFormat sdfstart = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszzz");
	 	             
	 	             try {
	 	            	datestart = sdfstart.parse(schedulequeryperiod.getStart());
	 			     } catch (ParseException e) {
	 			     // TODO Auto-generated catch block
	 			         e.printStackTrace();
	 		         }
	 	             long ScheduleStartTime = datestart.getTime();
	 	             
	 	             Date dateend = null;
	 	             SimpleDateFormat sdfend = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszzz");
	 	             
	 	             try {
	 	            	dateend = sdfend.parse(schedulequeryperiod.getEnd());
	 			     } catch (ParseException e) {
	 			     // TODO Auto-generated catch block
	 			         e.printStackTrace();
	 		         }
	 	            
	 	           long ScheduleEndTime = dateend.getTime();
	 	           
	 	         Date datecurentExact = getExactUnavailibilityDate(datestart,datecurent,userKloudlessMeeting.getDuration().getMinutes());

	 	          long ScheduleCurenttime = datecurentExact.getTime();
	 	             
 	               if(ScheduleCurenttime < ScheduleStartTime) {
 	            	   		schedulequeryperiodsForPass.add(schedulequeryperiod);
 	                }else {
	 	            	 if(ScheduleCurenttime <= ScheduleEndTime) {
	 	            		RealtimeSchedulequeryperiods realtimeSchedulequeryperiods = new RealtimeSchedulequeryperiods();
	 	            		SimpleDateFormat outputsdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszzz");
	 	  		            String strDate = outputsdf.format(datecurentExact);
		 	            	realtimeSchedulequeryperiods.setStart(strDate); 
		 	            	if(strDate.equals(schedulequeryperiod.getEnd()))
		 	            	{
		 	            		long ScheduleFullEndTime = datecurentExact.getTime() + (300000);
		                      	Date ScheduleFullEndDate = new Date(ScheduleFullEndTime);
		                      	realtimeSchedulequeryperiods.setEnd(outputsdf.format(ScheduleFullEndDate));
		 	            	}else {
		 	            		realtimeSchedulequeryperiods.setEnd(schedulequeryperiod.getEnd());
		 	            	}
		 	            	schedulequeryperiodsForPass.add(realtimeSchedulequeryperiods);
	 	            	 }
 	              }
   	         }
   	    
	   	 if (schedulequeryperiodsForPass.isEmpty()) {
	   		RealtimeSchedulequeryperiods realtimeSchedulequeryperiods = new RealtimeSchedulequeryperiods();
     		SimpleDateFormat outputsdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszzz");
            String strDate = outputsdf.format(datecurent);
            realtimeSchedulequeryperiods.setStart(strDate); 
     		long ScheduleFullEndTime = datecurent.getTime() + (300000);
          	Date ScheduleFullEndDate = new Date(ScheduleFullEndTime);
          	realtimeSchedulequeryperiods.setEnd(outputsdf.format(ScheduleFullEndDate));
         	schedulequeryperiodsForPass.add(realtimeSchedulequeryperiods);
	   	 }
   	        
   	    return schedulequeryperiodsForPass;
     }
       
       private boolean isSameDayInstructorUnavailability(Date dateSchedule, Date dateUnavailability) {
    	    SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
    	    return fmt.format(dateSchedule).equals(fmt.format(dateUnavailability));
    	}
       
       private List<RealtimeSchedulequeryperiods> GetScheduleQueryPeriods(List<RealtimeSchedulequeryperiods> schedulequeryperiods, List<InstructorUnavailability> instructorUnavailabilities, final UserKloudlessMeeting userKloudlessMeeting) {
  	    	   
    	   		List<RealtimeSchedulequeryperiods>  schedulequeryperiodsFinal = new ArrayList<>();
  	    	 // TODO Auto-generated method stub
  	    	    for(RealtimeSchedulequeryperiods Schedulequeryperiod: schedulequeryperiods) {
  	    	    	
    	    		 int scheduleIncrement = 0;
    	    		 List<RealtimeSchedulequeryperiods>  schedulequeryperiodsForPass = new ArrayList<>();
  	    	    	 Date dateStart = null;
  		 	         SimpleDateFormat sdfStart = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszzz");
  		 	             try {
  		 			        dateStart = sdfStart.parse(Schedulequeryperiod.getStart());
  		 			         } catch (ParseException e) {
  		 			     // TODO Auto-generated catch block
  		 			       e.printStackTrace();
  		 		         }
  		    	       
  		    	     long ScheduleStartTime = dateStart.getTime();
  		    	     String ScheduleStartTime_new = Schedulequeryperiod.getStart();
  		    	     
  		    	     Date dateEnd = null;
  		 	         SimpleDateFormat sdfEnd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszzz");
  		 	             
  		 	            try {
  		 	            	dateEnd = sdfEnd.parse(Schedulequeryperiod.getEnd());
  		 			        } catch (ParseException e) {
  		 			     // TODO Auto-generated catch block
  		 			        e.printStackTrace();
  		 		         }
  		 	           
  		    	    long ScheduleEndTime = dateEnd.getTime();
  		    	    String ScheduleEndTime_new = Schedulequeryperiod.getEnd();
  		    	  
  		    	    for(InstructorUnavailability instructorUnavailability:instructorUnavailabilities)
                 
  		    	    {
  	    			SimpleDateFormat outputsdfstrDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  	    			outputsdfstrDate.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));  
  			         
  			         
  			         String strDate = outputsdfstrDate.format(instructorUnavailability.getStartDate());
  		    		 Date dateunavstrDate = null;
  		 	          
  		    		SimpleDateFormat sdfunavstrDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  		 	             try {
  		 	            	dateunavstrDate = sdfunavstrDate.parse(strDate);
  		 			       } catch (ParseException e) {
  		 			     // TODO Auto-generated catch block
  		 			       e.printStackTrace();
  		 		         }
  		    		 
  		    	       long unavailabilityStartTime = dateunavstrDate.getTime();
  		    	       
  		    	       SimpleDateFormat outputsdfEndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  		    	       outputsdfEndDate.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));  
  			           String EndDate = outputsdfEndDate.format(instructorUnavailability.getEndDate());
  		    		   Date dateunavEndDate = null;
  		 	           SimpleDateFormat sdfunavEndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  		 	             try {
  		 	            	dateunavEndDate = sdfunavEndDate.parse(EndDate);
  		 			       } catch (ParseException e) {
  		 			     // TODO Auto-generated catch block
  		 			       e.printStackTrace();
  		 		         }
  		    	     
  		 	            long unavailabilityEndTime = dateunavEndDate.getTime();

  		    	     
  		    	     
  		 	        
	    		    if((ScheduleStartTime >= unavailabilityStartTime) && (ScheduleEndTime <= unavailabilityEndTime)){
		    		    	if(schedulequeryperiodsForPass.size() > 0 && scheduleIncrement != 0) {
		  		    	    	schedulequeryperiodsForPass.remove(schedulequeryperiodsForPass.size()-1);
		  		    	     }
	    		    		RealtimeSchedulequeryperiods realtimeSchedulequeryperiods = new RealtimeSchedulequeryperiods();
	                      	realtimeSchedulequeryperiods.setStart(ScheduleStartTime_new);
	                      	SimpleDateFormat outputsdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszzz");
	                      	long ScheduleFullEndTime = ScheduleStartTime + (300000);
	                      	Date ScheduleFullEndDate = new Date(ScheduleFullEndTime);
	      	                String strDatefirstCondition = outputsdf.format(ScheduleFullEndDate);
	      	                realtimeSchedulequeryperiods.setEnd(strDatefirstCondition);
	      	                schedulequeryperiodsForPass.add(realtimeSchedulequeryperiods);
	      	                continue;
	    		    }
	    		    if(unavailabilityStartTime < ScheduleStartTime && unavailabilityEndTime <= ScheduleStartTime)
	    		    {
	    		    	continue;
	    		    }
	    		    if(unavailabilityStartTime >= ScheduleEndTime && unavailabilityEndTime > ScheduleEndTime)
	    		    {
	    		    	continue;
	    		    }
	    		    if(!(isSameDayInstructorUnavailability(dateStart,dateunavstrDate) || isSameDayInstructorUnavailability(dateStart,dateunavEndDate))) {
	    		    	continue;
  		    	     }
	    		    if(schedulequeryperiodsForPass.size() > 0 && scheduleIncrement != 0) {
  		    	    	schedulequeryperiodsForPass.remove(schedulequeryperiodsForPass.size()-1);
  		    	     }
	    		    scheduleIncrement++;
	    		    
  		    	   if((ScheduleStartTime < unavailabilityStartTime) && (ScheduleEndTime > unavailabilityStartTime) && (ScheduleEndTime <= unavailabilityEndTime)){
  		    	    	  	RealtimeSchedulequeryperiods realtimeSchedulequeryperiods = new RealtimeSchedulequeryperiods();
  	                      	realtimeSchedulequeryperiods.setStart(ScheduleStartTime_new);
  	                      	SimpleDateFormat outputsdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszzz");
  	                      	outputsdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));  
  	      	                String strDatesecondCondition = outputsdf.format(instructorUnavailability.getStartDate());
  	      	                realtimeSchedulequeryperiods.setEnd(strDatesecondCondition);
  	      	                schedulequeryperiodsForPass.add(realtimeSchedulequeryperiods);
  	      	                
	  	      	            Date dateNew = null;
	       		 	         SimpleDateFormat sdfnew = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszzz");
	       		 	             try {
	       		 	            dateNew = sdfnew.parse(strDatesecondCondition);
	       		 			         } catch (ParseException e) {
	       		 			     // TODO Auto-generated catch block
	       		 			       e.printStackTrace();
	       		 		         }
	       		    	       
	       		 	         ScheduleEndTime = dateNew.getTime();
	       		 	         ScheduleEndTime_new = strDatesecondCondition;
  		    	      } 
  		
  		    	      
  		    	       if((ScheduleStartTime < unavailabilityEndTime) && (ScheduleEndTime > unavailabilityEndTime) && (ScheduleStartTime >= unavailabilityStartTime)) {
  		    	    	  RealtimeSchedulequeryperiods realtimeSchedulequeryperiods = new RealtimeSchedulequeryperiods();
  	                      SimpleDateFormat outputsdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszzz");
  	                      outputsdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));  
  	                      Date exactUnavailibilityDate = getExactUnavailibilityDate(dateStart,instructorUnavailability.getEndDate(),userKloudlessMeeting.getDuration().getMinutes());
  	                      String strDatethirdCondition = outputsdf.format(exactUnavailibilityDate);
  	                      realtimeSchedulequeryperiods.setStart(strDatethirdCondition);
  	                      realtimeSchedulequeryperiods.setEnd(ScheduleEndTime_new);
  	                      if(exactUnavailibilityDate.getTime() < ScheduleEndTime)
  	                      {
      	                	schedulequeryperiodsForPass.add(realtimeSchedulequeryperiods);
  	                      }
  	                      
	  	                    Date dateNew = null;
	       		 	         SimpleDateFormat sdfnew = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszzz");
	       		 	             try {
	       		 	            dateNew = sdfnew.parse(strDatethirdCondition);
	       		 			         } catch (ParseException e) {
	       		 			     // TODO Auto-generated catch block
	       		 			       e.printStackTrace();
	       		 		         }
	       		    	       
	       		    	     ScheduleStartTime = dateNew.getTime();
	       		    	     ScheduleStartTime_new = strDatethirdCondition;
  		    	       }

  		    	      

  		    	                        
  	                   if ((ScheduleStartTime < unavailabilityStartTime) && (ScheduleEndTime > unavailabilityEndTime)) {
  	                	    RealtimeSchedulequeryperiods realtimeSchedulequeryperiods = new RealtimeSchedulequeryperiods();
  	                                realtimeSchedulequeryperiods.setStart(ScheduleStartTime_new);
  	                        SimpleDateFormat outputsdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszzz");
  	                                outputsdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));  
  	               	                String strDatefourthCondition = outputsdf.format(instructorUnavailability.getStartDate());
  	                                realtimeSchedulequeryperiods.setEnd(strDatefourthCondition);
  			                        schedulequeryperiodsForPass.add(realtimeSchedulequeryperiods);
  			              
  			                RealtimeSchedulequeryperiods realtimeSchedulequeryperiods_Unav = new RealtimeSchedulequeryperiods();
		                           SimpleDateFormat outputsdf_Unav = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszzz");
		                           outputsdf_Unav.setTimeZone(TimeZone.getTimeZone("Etc/UTC")); 
		  	                       Date exactUnavailibilityDate = getExactUnavailibilityDate(dateStart,instructorUnavailability.getEndDate(),userKloudlessMeeting.getDuration().getMinutes());
        	                       String strDate_Unav = outputsdf_Unav.format(exactUnavailibilityDate);
        	                       realtimeSchedulequeryperiods_Unav.setStart(strDate_Unav);
        	                       realtimeSchedulequeryperiods_Unav.setEnd(ScheduleEndTime_new);
        	                       if(exactUnavailibilityDate.getTime() < ScheduleEndTime)
       	                      	   {
        	                    	   schedulequeryperiodsForPass.add(realtimeSchedulequeryperiods_Unav);
       	                      	   }
  	        	                     
  	        	                 Date dateNew = null;
  	          		 	         SimpleDateFormat sdfnew = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszzz");
  	          		 	             try {
  	          		 	            dateNew = sdfnew.parse(strDate_Unav);
  	          		 			         } catch (ParseException e) {
  	          		 			     // TODO Auto-generated catch block
  	          		 			       e.printStackTrace();
  	          		 		         }
  	          		    	       
  	          		    	     ScheduleStartTime = dateNew.getTime();
  	          		    	     ScheduleStartTime_new = strDate_Unav;
  			              }
  	    		      }
  	              
		  		    	if (schedulequeryperiodsForPass.isEmpty()) {
		  			   		RealtimeSchedulequeryperiods realtimeSchedulequeryperiods = new RealtimeSchedulequeryperiods();
		  		            realtimeSchedulequeryperiods.setStart(Schedulequeryperiod.getStart()); 
		  		          	realtimeSchedulequeryperiods.setEnd(Schedulequeryperiod.getEnd());
		  		         	schedulequeryperiodsForPass.add(realtimeSchedulequeryperiods);
		  			   	 }
  		    	    	schedulequeryperiodsFinal.addAll(schedulequeryperiodsForPass);
                    }  
  	    	    
  	    	      return schedulequeryperiodsFinal;
  		  }
       
	    
       private String GetFormatDate(String startdaytime, String timezone) {
    	   
	    	 SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	    	 formatter.setTimeZone(TimeZone.getTimeZone(timezone));
	    	 Date datetime = null;
	    	  try {
	    		    datetime = formatter.parse(startdaytime);
			       } catch (ParseException e) {
				   // TODO Auto-generated catch block
				   e.printStackTrace();
			      }
	  
	         SimpleDateFormat outputsdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszzz");
	         outputsdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));  
	         
	         
	         String strDate = outputsdf.format(datetime);
	    	
	    	
	    	return strDate;
	    	
	    }
	   
	    public void saveSchedule(final String token) {
	    	  
	    	log.info("Create schedule starts.");
	        long apiStartTimeMillis = new Date().getTime();
	        
	        User user = userComponents.getUser();
	        
	          
	        log.info("Basic validations with getting user : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
	        long profilingEndTimeMillis = new Date().getTime();
	    	
	        GetScheduleStatusResponse getScheduleStatusResponse = null;
	    	
	    	getScheduleStatusResponse = cronofyService.getscheduleStatus(token);
	          
	        if (getScheduleStatusResponse == null) {
			 	      throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED,null);
		       } 
			     
	        log.info("Query to create availability Rules Response : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	           
	        profilingEndTimeMillis = new Date().getTime();
	       
	        String oldScheduleTime = null; 
	           
	        UserKloudlessSchedule userKloudlessSchedule = new UserKloudlessSchedule();
			      
		         
	        if(getScheduleStatusResponse.getRealtimeschedulingStatusResponse().getEventStatusResponse().getEventId() != null){
	        	  
	        	   String  ScheduleId = getScheduleStatusResponse.getRealtimeschedulingStatusResponse().getEventStatusResponse().getEventId();
	        	   
	        	   long number = Long.parseLong(ScheduleId);
		        	  
		           userKloudlessSchedule = userKloudlessScheduleRepository.findByUserKloudlessScheduleId(number);  
		           oldScheduleTime = buildScheduleTimeString(userKloudlessSchedule);
		           if (userKloudlessSchedule != null) {
		             
		        	   userKloudlessSchedule.setScheduleStartTime(getScheduleStatusResponse.getRealtimeschedulingStatusResponse().getEventStatusResponse().getEventschedulingstarttime().getTime());
		        	   userKloudlessSchedule.setScheduleEndTime(getScheduleStatusResponse.getRealtimeschedulingStatusResponse().getEventStatusResponse().getEventschedulingendtime().getTime());
		        	   userKloudlessSchedule.setRealtimeschedulingId(getScheduleStatusResponse.getRealtimeschedulingStatusResponse().getRealtimeschedulingId());
		        	   userKloudlessSchedule.setSrealtimeschedulingToken(token);

			           	String startDate = getScheduleStatusResponse.getRealtimeschedulingStatusResponse().getEventStatusResponse().getEventschedulingstarttime().getTime();
			       		String setbookingDate = startDate.substring(0,10);
			       		String setbookingTime = "18:30:00";
			       		String setbookingDateTime = setbookingDate +" "+ setbookingTime;
			            
			       		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        
	          			Date bookingDateFinal = null;
	          			try {
	          				bookingDateFinal = sdf.parse(setbookingDateTime);
	           			} catch (ParseException e) {
	           				// TODO Auto-generated catch block
	           				e.printStackTrace();
	           			}
	          			userKloudlessSchedule.setBookingDate(bookingDateFinal);

		           	}
		           userKloudlessScheduleRepository.save(userKloudlessSchedule);
		     }else {
		    	  throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED,null);
		     }
	         try {
	              User instructor = userKloudlessSchedule.getUserKloudlessMeeting().getUser();
	               String instructorName = fitwiseUtils.getUserFullName(instructor);
	               String memberName = fitwiseUtils.getUserFullName(user);
	               String scheduleTime = buildScheduleTimeString(userKloudlessSchedule);
	               String[] scheduleTimes = scheduleTime.split(" ");
	               String[] oldScheduleTimes = oldScheduleTime.split(" ");
	            try {
	                String subject = EmailConstants.SESSION_UPDATE_SUBJECT_INSTRUCTOR.replace(EmailConstants.LITERAL_DATE_TIME, scheduleTime);
	                String mailBody = EmailConstants.SESSION_UPDATE_CONTENT_INSTRUCTOR.replace(EmailConstants.LITERAL_MEMBER_NAME, memberName).replace(EmailConstants.LITERAL_SESSION_NAME, userKloudlessSchedule.getPackageKloudlessMapping().getTitle()).replace("#OLD_DATE#", oldScheduleTimes[0]).replace("#OLD_TIME#", oldScheduleTimes[1]).
	                        replace("#NEW_DATE#",scheduleTimes[0]).replace("#NEW_TIME#",scheduleTimes[1]);
	                mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + instructorName + ",").replace(EmailConstants.LITERAL_MAIL_BODY, mailBody);
	                mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
	                asyncMailer.sendHtmlMail(instructor.getEmail(), subject, mailBody);
	            } catch (Exception e) {
	                log.info("Mail sending failed for session rescheduling by member :" + e.getMessage());
	            }
	            try {
	                NotificationContent notificationContent = new NotificationContent();
	                notificationContent.setTitle(PushNotificationConstants.SESSION_UPDATE_TITLE_INSTRUCTOR.replace(EmailConstants.LITERAL_DATE_TIME, scheduleTime));
	                notificationContent.setBody(PushNotificationConstants.SESSION_UPDATE_MESSAGE_INSTRUCTOR.replace(EmailConstants.LITERAL_MEMBER_NAME, memberName).replace(EmailConstants.LITERAL_SESSION_NAME, userKloudlessSchedule.getPackageKloudlessMapping().getTitle()).replace("#OLD_DATE#", oldScheduleTimes[0]).replace("#OLD_TIME#", oldScheduleTimes[1])
	                        .replace("#NEW_DATE#",scheduleTimes[0]).replace("#NEW_TIME#",scheduleTimes[1]));

	                pushNotificationAPIService.sendOnlyNotification(notificationContent, instructor, KeyConstants.KEY_INSTRUCTOR);
	            } catch (Exception exception) {
	                log.error("Notification trigger failed for session rescheduling by member : " + exception.getMessage());
	            }

	        } catch (Exception exception) {
	            log.error("Notification trigger failed for session rescheduling by member : " + exception.getMessage());
	        }
	    	
	         
	        
	   }
	    
	    public void deleteSchedule(final Long fitwiseScheduleId) throws ParseException {
		       
	    	   User user = userComponents.getUser();
	    	   
	    	   String member_Account_Token = "";
		         String member_Calendar_ID = "";
				 UserKloudlessAccount account_member = getActiveAccount(user);
				 if(account_member != null){
					 UserKloudlessCalendar activeCalendar_member = getActiveCalendarFromKloudlessAccount(account_member);
					 if(activeCalendar_member != null){
						 member_Account_Token = account_member.getRefreshToken();
				         member_Calendar_ID = activeCalendar_member.getCalendarId();
				     }
			     }
	        
	           UserKloudlessSchedule userKloudlessSchedule = userKloudlessScheduleRepository.findByUserAndUserKloudlessScheduleId(user, fitwiseScheduleId);
	      
	           if (userKloudlessSchedule == null) {
	            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_SCHEDULE_NOT_FOUND, null);
	           }
	      
	           String scheduleTime = buildScheduleTimeString(userKloudlessSchedule);
	         // Gson gson = new Gson();
	        //  Event existingEvent = gson.fromJson(userKloudlessSchedule.getSchedulePayload(), Event.class);
	           OffsetDateTime startDateTime;
	          // expected date time format is 2011-12-03T10:15:30+01:00
	           try {
	              startDateTime = OffsetDateTime.parse(userKloudlessSchedule.getScheduleStartTime());
	              } catch (DateTimeParseException dpe) {
	              log.error("schedule delete failed invalid event start time", dpe);
	              throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_SCHEDULE_DELETE_FAILED, null);
	           }
	       
	           Date date = new Date();
	       
	           TimeSpan timeSpan = userKloudlessSchedule.getSubscriptionPackage().getCancellationDuration();
	       
	           if (timeSpan.getDays() != null) {
	            long differenceInDays = fitwiseUtils.getNumberOfDaysBetweenTwoDates(date, Date.from(startDateTime.toInstant()));
	            log.info("Difference in days:" +differenceInDays);
	            if (differenceInDays < timeSpan.getDays()) {
	                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_DELETE_SCHEDULE_NOT_ALLOWED, null);
	            }
	             } else if (timeSpan.getHours() != null) {
	            long differenceInHours = fitwiseUtils.getNumberOfHoursBetweenTwoDates(date, Date.from(startDateTime.toInstant()));
	            log.info("Difference in hours:" +differenceInHours);
	            if (differenceInHours < timeSpan.getHours()) {
	                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_DELETE_SCHEDULE_NOT_ALLOWED, null);
	            }
	           }
	           
	           RefreshAccessTokenResponse refreshAccessTokenResponse = null;
		        
		       refreshAccessTokenResponse = cronofyService.refreshAccessToken(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar().getUserKloudlessAccount().getRefreshToken());
		        
		        if (refreshAccessTokenResponse == null) {
			 		throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED,null);
			    }
	        
	       
	           if(!cronofyService.deleteevent(refreshAccessTokenResponse.getAccessToken(),userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar().getCalendarId(),userKloudlessSchedule.getUserKloudlessScheduleId().toString())){
	                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_KLOUDLESS_SCHEDULE_DELETE_FAILED, MessageConstants.ERROR);
	           }
	        
	           if((member_Account_Token.length() > 0) && (member_Calendar_ID.length() > 0 ))
               {
      	        	RefreshAccessTokenResponse member_refreshAccessTokenResponse = null;
     		        
      	        	member_refreshAccessTokenResponse = cronofyService.refreshAccessToken(member_Account_Token);
      		        
      		        if (member_refreshAccessTokenResponse == null) {
      			 		throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED,null);
      			    }
      	        
      	       
      	           if(!cronofyService.deleteevent(member_refreshAccessTokenResponse.getAccessToken(),member_Calendar_ID,userKloudlessSchedule.getUserKloudlessScheduleId().toString())){
      	                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_KLOUDLESS_SCHEDULE_DELETE_FAILED, MessageConstants.ERROR);
      	            }
               }
	           userKloudlessScheduleRepository.delete(userKloudlessSchedule);
	       
	        try {
	            User instructor = userKloudlessSchedule.getUserKloudlessMeeting().getUser();
	            String instructorName = fitwiseUtils.getUserFullName(instructor);
	            String memberName = fitwiseUtils.getUserFullName(user);
	            String[] scheduleTimes = scheduleTime.split(" ");
	            try {
	                  String subject = EmailConstants.SESSION_CANCEL_SUBJECT_INSTRUCTOR.replace(EmailConstants.LITERAL_DATE_TIME, scheduleTime);
	                  String mailBody = EmailConstants.SESSION_CANCEL_CONTENT_INSTRUCTOR.replace(EmailConstants.LITERAL_MEMBER_NAME, memberName).replace(EmailConstants.LITERAL_SESSION_NAME, userKloudlessSchedule.getPackageKloudlessMapping().getTitle()).replace(EmailConstants.LITERAL_DATE, scheduleTimes[0]).replace(EmailConstants.LITERAL_TIME, scheduleTimes[1]);
	                  mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + instructorName + ",").replace(EmailConstants.LITERAL_MAIL_BODY, mailBody);
	                  mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
	                  asyncMailer.sendHtmlMail(instructor.getEmail(), subject, mailBody);
	            } catch (Exception e) {
	                log.info("Mail sending failed for session cancelling by member :" + e.getMessage());
	            }
	            try {
	                NotificationContent notificationContent = new NotificationContent();
	                notificationContent.setTitle(PushNotificationConstants.SESSION_CANCEL_TITLE_INSTRUCTOR.replace(EmailConstants.LITERAL_DATE_TIME, scheduleTime));
	                notificationContent.setBody(PushNotificationConstants.SESSION_CANCEL_MESSAGE_INSTRUCTOR.replace(EmailConstants.LITERAL_MEMBER_NAME, memberName).replace(EmailConstants.LITERAL_SESSION_NAME, userKloudlessSchedule.getPackageKloudlessMapping().getTitle()).replace(EmailConstants.LITERAL_DATE, scheduleTimes[0]).replace(EmailConstants.LITERAL_TIME, scheduleTimes[1]));
	                pushNotificationAPIService.sendOnlyNotification(notificationContent, instructor, KeyConstants.KEY_INSTRUCTOR);
	            } catch (Exception exception) {
	                log.error("Notification trigger failed for session cancelling by member : " + exception.getMessage());
	            }
	        } catch (Exception exception) {
	            log.error("Notification trigger failed for session cancelling by member : " + exception.getMessage());
	        }
	    }
	    
	    private  String buildScheduleTimeString(UserKloudlessSchedule schedule){
	        String scheduleTime = "";
	        try{
	              String start = schedule.getScheduleStartTime();
	        	  String end = schedule.getScheduleEndTime();
	          
	           
	              SimpleDateFormat dateFormat = new SimpleDateFormat(StringConstants.PATTERN_DATE_TIME);
	              dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	              Date startTimeInPayload = dateFormat.parse(start);
	              Date endTimeInPayload = dateFormat.parse(end);
	              log.info("start time in payload: " +startTimeInPayload);
	              log.info("end time in payload:" +endTimeInPayload);
	              //onvert time to instrutor time zone
	              SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	              simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	              String startTimeInInstructorTimeZone = simpleDateFormat.format(startTimeInPayload);
	              String endTimeInInstructorTimeZone = simpleDateFormat.format(endTimeInPayload);
	              log.info("start time in instructor timezone:" +startTimeInInstructorTimeZone);
	              log.info("end time in instructor timezone:" +endTimeInInstructorTimeZone);
	              String[] startTime = startTimeInInstructorTimeZone.split(" ");
	              String[] endTime = endTimeInInstructorTimeZone.split(" ");
	              scheduleTime = startTime[0] + " " + startTime[1] +"-" + endTime[1];
	        }catch (Exception exception){
	            log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
	        }
	        return  scheduleTime;
	    }
	    public void deleteMemberCalendarAccount(String accountId){
		       
	    	   log.info("Delete instructor calendar account starts.");
	        
	    	   long apiStartTimeMillis = new Date().getTime();
	         
	    	   User user = userComponents.getUser();

	          //Validating the inputs
	          if (accountId == null || ValidationUtils.isEmptyString(accountId)){
	            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_ID_NULL, MessageConstants.ERROR);
	          }
	          List<UserKloudlessAccount> userKloudlessAccountList = userKloudlessTokenRepository.findByUserAndAccountId(user, accountId);
	        
	          if (userKloudlessAccountList.isEmpty()){
	            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_ID_INVALID, MessageConstants.ERROR);
	          }
	      
	         UserKloudlessAccount userKloudlessAccount = userKloudlessAccountList.get(0);

	         if (userKloudlessAccount.isActive()){
	            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.MSG_ERR_DELETE_ACTIVE_CALENDAR_ACCOUNT, MessageConstants.ERROR);
	         }
	         log.info(StringConstants.LOG_FIELD_VALIDATION + (new Date().getTime() - apiStartTimeMillis));
	         long profilingEndTimeMillis = new Date().getTime();
	       
	         RefreshAccessTokenResponse refreshAccessTokenResponse = null;
		        
	          refreshAccessTokenResponse = cronofyService.refreshAccessToken(userKloudlessAccount.getRefreshToken());
	        
	          if (refreshAccessTokenResponse == null) {
		 		
	        	   throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED,null);
		      }
	           
	            List<UserKloudlessCalendar> userKloudlessCalendarListDelete = userKloudlessCalendarRepository.findByUserAndUserKloudlessAccount(user, userKloudlessAccount);
	            log.info("Query to user kloudless calendar list : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	            profilingEndTimeMillis = new Date().getTime();

	           
	           if (!userKloudlessCalendarListDelete.isEmpty()){
	               
	        	   for (UserKloudlessCalendar userKloudlessCalendar : userKloudlessCalendarListDelete){
	                  
	                            userKloudlessCalendarRepository.delete(userKloudlessCalendar);
	                }
	             }
	           
	            log.info("Query to delete user kloudless calendars : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	            profilingEndTimeMillis = new Date().getTime(); 
	         
	            if(!cronofyService.deleteprofile(refreshAccessTokenResponse.getAccessToken(),userKloudlessAccount.getProfileId())){
	                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.MSG_ERR_DELETE_FAILED, MessageConstants.ERROR);
	            }
	          
	            userKloudlessTokenRepository.deleteById(userKloudlessAccount.getUserKloudlessTokenId());
	            log.info("Delete delete user Cronofy account from cronofy service and DB : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	       
	   
	        
	            log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
	            log.info("Delete instructor calendar account ends.");
	     }
	     
	    
	    @Transactional
	    public void deleteInstructorCalendarAccount(String accountId){
	       
	    	   log.info("Delete instructor calendar account starts.");
	        
	    	   long apiStartTimeMillis = new Date().getTime();
	         
	    	   User user = userComponents.getUser();


	           if (!fitwiseUtils.isInstructor(user)){
	            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_INSTRUCTOR, MessageConstants.ERROR);
	           }

	          //Validating the inputs
	          if (accountId == null || ValidationUtils.isEmptyString(accountId)){
	            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_ID_NULL, MessageConstants.ERROR);
	          }
	          List<UserKloudlessAccount> userKloudlessAccountList = userKloudlessTokenRepository.findByUserAndAccountId(user, accountId);
	        
	          if (userKloudlessAccountList.isEmpty()){
	            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_ID_INVALID, MessageConstants.ERROR);
	          }
	      
	         UserKloudlessAccount userKloudlessAccount = userKloudlessAccountList.get(0);

	         if (userKloudlessAccount.isActive()){
	            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.MSG_ERR_DELETE_ACTIVE_CALENDAR_ACCOUNT, MessageConstants.ERROR);
	         }
	         log.info(StringConstants.LOG_FIELD_VALIDATION + (new Date().getTime() - apiStartTimeMillis));
	         long profilingEndTimeMillis = new Date().getTime();
	       
	         List<UserKloudlessCalendar> userKloudlessCalendarList = userKloudlessCalendarRepository.findByUserAndUserKloudlessAccount(user, userKloudlessAccount);
	         log.info("Query to get user kloudless calendar list : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	         profilingEndTimeMillis = new Date().getTime();
	         
	         boolean isCalendarAccountUsedInPackages = true;
	       
	         int noOfTimesUsed = 0;
	         
	         if (userKloudlessCalendarList.isEmpty()){
	            isCalendarAccountUsedInPackages = false;
	           } else {
	             for (UserKloudlessCalendar userKloudlessCalendar : userKloudlessCalendarList){

	                List<UserKloudlessMeeting> userKloudlessMeetingList = userKloudlessMeetingRepository.findByUserAndUserKloudlessCalendar(user, userKloudlessCalendar);
	               
	                 if (userKloudlessMeetingList.isEmpty()){
	                    isCalendarAccountUsedInPackages = false;
	                    } else {

	                    for (UserKloudlessMeeting userKloudlessMeeting: userKloudlessMeetingList){
	                        List<UserKloudlessSchedule> userKloudlessScheduleList = userKloudlessScheduleRepository.findByUserKloudlessMeeting(userKloudlessMeeting);

	                        if (userKloudlessScheduleList.isEmpty()){
	                            isCalendarAccountUsedInPackages = false;
	                        } else {
	                            noOfTimesUsed++;
	                        }
	                    }
	                }
	            }
	          }
	        
	         
	         log.info("Check whether the calendar used in packages or not : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	         profilingEndTimeMillis = new Date().getTime();
	         
	         RefreshAccessTokenResponse refreshAccessTokenResponse = null;
		        
	          refreshAccessTokenResponse = cronofyService.refreshAccessToken(userKloudlessAccount.getRefreshToken());
	        
	          if (refreshAccessTokenResponse == null) {
		 		
	        	   throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED,null);
		      }
	         
	        
	         if (!isCalendarAccountUsedInPackages && noOfTimesUsed == 0){

	            UserKloudlessSubscription userKloudlessSubscription = userKloudlessSubscriptionRepository.findByUserKloudlessAccountUserKloudlessTokenId(userKloudlessAccount.getUserKloudlessTokenId());
	            log.info("Query to get user kloudless subscription : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	            profilingEndTimeMillis = new Date().getTime();
	           
	            if (userKloudlessSubscription != null){
	                userKloudlessSubscriptionRepository.delete(userKloudlessSubscription);
	                log.info("Query to delete user kloudless subscription : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	                profilingEndTimeMillis = new Date().getTime();
	            }

	            List<UserKloudlessCalendar> userKloudlessCalendarListDelete = userKloudlessCalendarRepository.findByUserAndUserKloudlessAccount(user, userKloudlessAccount);
	            log.info("Query to user kloudless calendar list : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	            profilingEndTimeMillis = new Date().getTime();

	           
	           if (!userKloudlessCalendarListDelete.isEmpty()){
	               
	        	   for (UserKloudlessCalendar userKloudlessCalendar : userKloudlessCalendarListDelete){
	                
	        		 List<UserKloudlessMeeting> userKloudlessMeetingList = userKloudlessMeetingRepository.findByUserAndUserKloudlessCalendar(user, userKloudlessCalendar);

	                 if (!userKloudlessMeetingList.isEmpty()){
	                    
	                 for (UserKloudlessMeeting userKloudlessMeeting : userKloudlessMeetingList) {
	                	 
	                          List<CronofyAvailabilityRules> cronofyAvailabilityRules = cronofyAvailabilityRulesRepository
	      	        		   .findByUserKloudlessMeeting(userKloudlessMeeting);
	                          
	                          if (!cronofyAvailabilityRules.isEmpty()){
	                        	  log.info("Query to get cronofy availabilityrules : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	       	      		       profilingEndTimeMillis = new Date().getTime();
	       	      			  
	       	      		       
	       	      		       CronofyMeetingModel cronofyMeetingModel = new CronofyMeetingModel();
	       	                     
	       	      			   
	       	      			   for(CronofyAvailabilityRules cronofyAvailability : cronofyAvailabilityRules){
	       	                  	
	       	                  	  cronofyMeetingModel.setCronofyavailabilityrulesid(cronofyAvailability.getUserCronofyAvailabilityRulesId());
	       	      		          
	       	      		       }
	       	                	 
	       	      			   CronofyAvailabilityRules cronofyAvailabilityRule = cronofyAvailabilityRulesRepository
	       	  		                    .findByUserCronofyAvailabilityRulesId(cronofyMeetingModel.getCronofyavailabilityrulesid());
	       		            
	       			          if (cronofyAvailabilityRule != null) 
	       				      {
	       			        	if (!ValidationUtils.isEmptyString(cronofyAvailabilityRule.getUserCronofyAvailabilityRulesId().toString())) {
		       	                	if (!cronofyAvailabilityRule.getWeeklyPeriods().isEmpty()) {
		       	                		try {
		       	           		               
	     	           					   cronofyService.deleteavailabilityrules(refreshAccessTokenResponse.getAccessToken(), cronofyAvailabilityRule.getUserCronofyAvailabilityRulesId().toString());
	       	           		               
	       	           				   } catch (ApplicationException ae) {
	       	           		                  
	       	           				        	log.info("Cronofy meeting window deletion failed");
	       	           		           }   
		           		               }
		       	           		      
		       	           			 }
		       	           		   else
		       	           		    {
		       	           				   throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_EER_AVAILABILITY_LINKING_FAILED, null);
		       	           		    }
	       				         
	       				      }
	       	                	 
	       	                     cronofyAvailabilityRulesRepository.deleteAll(cronofyAvailabilityRules); 
	                       }
	      			  
	                             userKloudlessMeetingRepository.delete(userKloudlessMeeting);
	                     }
	                 }
	                  
	                            userKloudlessCalendarRepository.delete(userKloudlessCalendar);
	                }
	             }
	           
	            log.info("Query to delete user kloudless calendars : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	            profilingEndTimeMillis = new Date().getTime(); 
	         
	            if(!cronofyService.deleteprofile(refreshAccessTokenResponse.getAccessToken(),userKloudlessAccount.getProfileId())){
	                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.MSG_ERR_DELETE_FAILED, MessageConstants.ERROR);
	            }
	          
	            userKloudlessTokenRepository.deleteById(userKloudlessAccount.getUserKloudlessTokenId());
	            log.info("Delete delete user Cronofy account from cronofy service and DB : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	       
	            }
	            else 
	            {
	             throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.MSG_CAL_DELETE_PACKAGE_USER_ACCOUNT, MessageConstants.ERROR);
	            }
	        
	            log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
	            log.info("Delete instructor calendar account ends.");
	     }
	     
	    
	    public RealtimeScheduleResponse updateScheduleFromInstructor(KloudlessScheduleModel kloudlessScheduleModel) {
	    	
	    	  log.info("Create schedule starts.");
	          long apiStartTimeMillis = new Date().getTime();
	    	 
	          ValidationUtils.throwException(kloudlessScheduleModel.getFitwiseScheduleId() == null
                   || kloudlessScheduleModel.getFitwiseScheduleId() <= 0,
              CalendarConstants.CAL_ERR_SCHEDULE_ID_INVALID, Constants.BAD_REQUEST);
           
	          User user = userComponents.getUser();
         
              ValidationUtils.throwException(!fitwiseUtils.isInstructor(user),
           
              ValidationMessageConstants.MSG_USER_NOT_INSTRUCTOR, Constants.ERROR_STATUS);
         
           
            Optional<UserKloudlessSchedule> optionalSchedule = userKloudlessScheduleRepository.findById(kloudlessScheduleModel.getFitwiseScheduleId());
             
             if(!optionalSchedule.isPresent() || optionalSchedule.get() == null){
                 throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_SCHEDULE_NOT_FOUND, null);
               }
           
            UserKloudlessSchedule userKloudlessSchedule = optionalSchedule.get();
         
           
            ValidationUtils.throwException(!userKloudlessSchedule.getUserKloudlessMeeting().getUser().getUserId().equals(user.getUserId())
            ,CalendarConstants.CAL_ERR_SCHEDULE_INSTRUCTOR_NOT_MATCH, Constants.ERROR_STATUS);
          
            UserKloudlessCalendar userKloudlessCalendar = userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar();
          
           
             UserKloudlessAccount userKloudlessAccount = userKloudlessCalendar.getUserKloudlessAccount();
           
             User user_member = userKloudlessSchedule.getUser();
             String member_Account_ID = "";
	         String member_Calendar_ID = "";
			 UserKloudlessAccount account_member = getActiveAccount(user_member);
			 if(account_member != null){
				 UserKloudlessCalendar activeCalendar_member = getActiveCalendarFromKloudlessAccount(account_member);
				 if(activeCalendar_member != null){
					 member_Account_ID = account_member.getAccountId();
			         member_Calendar_ID = activeCalendar_member.getCalendarId();
			     }
		     }

             ValidationUtils.throwException(userKloudlessAccount == null, CalendarConstants.CAL_ERR_ACCOUNT_NOT_FOUND, Constants.BAD_REQUEST);
                      log.info("Basic validations with getting user : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
         	 
             long profilingEndTimeMillis = new Date().getTime();
         	    	 
         	         
         	 SubscriptionPackage subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageId(kloudlessScheduleModel.getSubscriptionPackageId());
         	        
         	         
         	 ValidationUtils.throwException(subscriptionPackage == null,
         	                 ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_ID_NULL, Constants.BAD_REQUEST);
         	        
         	         
         	 log.info("Query to get subscription package : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
         	 profilingEndTimeMillis = new Date().getTime();
         	        
         	        
         	 Optional<PackageKloudlessMapping> optionalPackageSession = packageKloudlessMappingRepository
         	                 .findBySessionMappingId(kloudlessScheduleModel.getPackageSessionMappingId());
                  
                     
         	 if(!optionalPackageSession.isPresent() || optionalPackageSession.get() == null)
         	    {
         	             throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_SESSION_NOT_FOUND, null);
         	    }
         	     	 
              log.info("Query to get package session : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
         	        profilingEndTimeMillis = new Date().getTime();
         	         
         	        
         	  UserKloudlessMeeting  userKloudlessMeeting = optionalPackageSession.get().getUserKloudlessMeeting();
         	        
         	  Optional<UserKloudlessMeeting> userKloudlessMeetings = userKloudlessMeetingRepository
         	                 .findByUserKloudlessCalendarAndUserKloudlessMeetingId(userKloudlessCalendar, kloudlessScheduleModel.getFitwiseMeetingId());
         	        
         	  if(!userKloudlessMeetings.isPresent() || userKloudlessMeetings.get() == null){
         	             throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_MEETING_NOT_FOUND, null);
         	  }
         	         
         	  log.info("Query to get user kloudless meeting : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
         	  profilingEndTimeMillis = new Date().getTime();
         	      
         	  CalendarMeetingType calendarMeetingType;
         	         
         	  if (kloudlessScheduleModel.getFitwiseMeetingTypeId() == null || kloudlessScheduleModel.getFitwiseMeetingTypeId() == 0) {
         	             // get session type from meeting
         	             calendarMeetingType = userKloudlessMeetings.get().getCalendarMeetingType();
         	  } else {
         	             // find the passed session type
         	             calendarMeetingType = calendarMeetingTypeRepository.findByMeetingTypeId(kloudlessScheduleModel.getFitwiseMeetingTypeId());
         	             ValidationUtils.throwException(calendarMeetingType == null,
         	                     CalendarConstants.CAL_ERR_MEETING_TYPE_NOT_FOUND, Constants.BAD_REQUEST);
         	    }
         	       
         	         
         	 log.info("Query to get calendar meeting type : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
         	 profilingEndTimeMillis = new Date().getTime();
         	        
         	 // if session type is "In-Person or Virtual" choose virtual session
         	 if (CalendarConstants.SESSION_IN_PERSON_OR_VIRTUAL == calendarMeetingType.getMeetingTypeId()) {
         	             calendarMeetingType = calendarMeetingTypeRepository
         	                     .findByMeetingTypeId(CalendarConstants.MEETING_TYPE_VIRTUAL);
         	  }
         	        
         	  log.info("Query to get calendar meeting type : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
         	  profilingEndTimeMillis = new Date().getTime();          
         	  log.info("Query to get cronofy availabilityrules : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
		      profilingEndTimeMillis = new Date().getTime();
			  
		      	 List<InstructorUnavailability> instructorUnavailabilities = new ArrayList<>();
	             
		         List<InstructorUnavailability> instructorUnavailabilitiesApp = instructorUnavailabilityRepository.findByUserKloudlessCalendar(userKloudlessCalendar);
		         if (!instructorUnavailabilitiesApp.isEmpty()) 
	             {
		        	 for(InstructorUnavailability instructorUnavailabilityApp : instructorUnavailabilitiesApp){
		        		 InstructorUnavailability instructorUnavailability = new InstructorUnavailability();
		        		 instructorUnavailability.setStartDate(instructorUnavailabilityApp.getStartDate());
		        		 instructorUnavailability.setEndDate(instructorUnavailabilityApp.getEndDate());
		        		 instructorUnavailabilities.add(instructorUnavailability);
		        	 }
	             }
		         List<InstructorUnavailability> instructorUnavailabilitiesFreeBusy = getFreeBusyUnavailabilities(kloudlessScheduleModel,userKloudlessAccount,userKloudlessCalendar,userKloudlessMeeting);
		         if (!instructorUnavailabilitiesFreeBusy.isEmpty()) 
	             {
		        	 for(InstructorUnavailability instructorUnavailabilityFreeBusy : instructorUnavailabilitiesFreeBusy){
		        		 InstructorUnavailability instructorUnavailability = new InstructorUnavailability();
		        		 instructorUnavailability.setStartDate(instructorUnavailabilityFreeBusy.getStartDate());
		        		 instructorUnavailability.setEndDate(instructorUnavailabilityFreeBusy.getEndDate());
		        		 instructorUnavailabilities.add(instructorUnavailability);
		        	 }
	             }
		         List<InstructorUnavailability> memberUnavailabilitiesApp = getMemberUnavailabilities(kloudlessScheduleModel,userKloudlessAccount,user_member,userKloudlessMeeting);
		         if (!memberUnavailabilitiesApp.isEmpty()) 
	             {
		        	 for(InstructorUnavailability memberUnavailabilityApp : memberUnavailabilitiesApp){
		        		 InstructorUnavailability memberUnavailability = new InstructorUnavailability();
		        		 memberUnavailability.setStartDate(memberUnavailabilityApp.getStartDate());
		        		 memberUnavailability.setEndDate(memberUnavailabilityApp.getEndDate());
		        		 instructorUnavailabilities.add(memberUnavailability);
		        	 }
	             }
		         instructorUnavailabilities.sort(Comparator.comparing(InstructorUnavailability::getStartDate));
		      List<CronofyAvailabilityRules> cronofyAvailabilityRules = cronofyAvailabilityRulesRepository
	        		   .findByUserKloudlessMeeting(userKloudlessMeeting);
			  
			  log.info("Query to get cronofy availabilityrules : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
		      profilingEndTimeMillis = new Date().getTime();
		     
		      CronofyMeetingModel cronofyMeetingModel = new CronofyMeetingModel();
          
			   
			  for(CronofyAvailabilityRules cronofyAvailability : cronofyAvailabilityRules){
       	
       	          cronofyMeetingModel.setCronofyavailabilityrulesid(cronofyAvailability.getUserCronofyAvailabilityRulesId());
		          
		          cronofyMeetingModel.setBufferafter(cronofyAvailability.getBufferAfter());
		           
		          cronofyMeetingModel.setBufferbefore(cronofyAvailability.getBufferBefore());
		          
		          cronofyMeetingModel.setStartinterval(cronofyAvailability.getStartInterval());
		        
		           if(cronofyAvailability.getWeeklyPeriods() != null) {
			          List<Object> list = (List<Object>) Arrays.asList(new GsonBuilder().create().fromJson(cronofyAvailability.getWeeklyPeriods(), Object[].class));
			          cronofyMeetingModel.setWeeklyperiods(list);
		           }
		           
             }
			  
			  List<RealtimeSchedulequeryperiods> Schedulequeryperiods = new ArrayList<>();
		         
	           
              Date bookingDate = kloudlessScheduleModel.getBookingDate();
		  
              SimpleDateFormat df = new SimpleDateFormat( "EEEE" ); 
		 
              String day= df.format( bookingDate ); 
            
              SimpleDateFormat dfymd = new SimpleDateFormat( "yyyy-MM-dd" );
        
              String ymd = dfymd.format( bookingDate ); 
             
              
              CronofyAvailabilityRules cronofyAvailabilityRule = cronofyAvailabilityRulesRepository
	                    .findByUserCronofyAvailabilityRulesId(cronofyMeetingModel.getCronofyavailabilityrulesid());
            
	          if (cronofyAvailabilityRule==null) 
		          {
		            throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_MEETING_NOT_FOUND, null);
		          }
	   
	           JSONArray jsonArray = new JSONArray(cronofyAvailabilityRule.getWeeklyPeriods());
	        
	            for (int i = 0; i < jsonArray.length(); i++) {
	    	   
	    	       JSONObject jsonObject = jsonArray.getJSONObject(i);
	    	   
	    	       String  weeklyDay = jsonObject.getString("day");
	    	   
	    	   
	    	       if(weeklyDay.toLowerCase().equals(day.toLowerCase())) {
	    	        
	    	    	  String  starttime = jsonObject.getString("start_time");
		    	      
		    	      String  endttime = jsonObject.getString("end_time");
		    	     
		    	      String  startdaytime = ymd+" "+starttime;

                      String  enddaytime = ymd+" "+endttime;
                  
                     //String  start
                      RealtimeSchedulequeryperiods realtimeSchedulequeryperiods = new RealtimeSchedulequeryperiods();
                         realtimeSchedulequeryperiods.setStart(GetFormatDate(startdaytime,userKloudlessMeeting.getTimeZone()));
   			             realtimeSchedulequeryperiods.setEnd(GetFormatDate(enddaytime,userKloudlessMeeting.getTimeZone()));
   			             Schedulequeryperiods.add(realtimeSchedulequeryperiods);
   			             
               
   			  
	    	      }
	    	    

	             }
	            
	              if(Schedulequeryperiods.isEmpty()) {
 		        	
		        	 throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_AVAILABILITY_RULES_NOT_FOUND, null);
		          }
	              PackageSubscription packageSubscription = packageSubscriptionRepository.findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageIdOrderBySubscribedDateDesc(user_member.getUserId(), kloudlessScheduleModel.getSubscriptionPackageId());
	              Schedulequeryperiods.addAll(GetSchedulePeriodsForNext7Days(bookingDate,userKloudlessMeeting,cronofyAvailabilityRule,packageSubscription,user_member,subscriptionPackage));
	              
                  List<RealtimeSchedulequeryperiods> Schedulequeryperiodsforcronofy = new ArrayList<>();
                
                  if (!instructorUnavailabilities.isEmpty()) 
	               {
         	         Schedulequeryperiodsforcronofy = GetScheduleQueryPeriods(Schedulequeryperiods,instructorUnavailabilities,userKloudlessMeeting);
	               }
	                
                  List<RealtimeSchedulequeryperiods> schedulequeryperiodsinCurrentDate= new ArrayList<>(); 
                  
                  
                  SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                     Date datelocal = new Date();
                     String ymdcurrent = formatter.format( datelocal );
                
                  SimpleDateFormat sdfbookingday = new SimpleDateFormat("yyyy-MM-dd");
                 
                   Date datebooking = null;
                   try {
                 	  datebooking = sdfbookingday.parse(ymd);
	 			         
	 	                } catch (ParseException e) {
	 			          // TODO Auto-generated catch block
	 			         e.printStackTrace();
	 		            }
                  
                  SimpleDateFormat sdfcurrentday = new SimpleDateFormat("yyyy-MM-dd");
                 
                   Date datecurrent = null;
                       try {
                     	  datecurrent = sdfcurrentday.parse(ymdcurrent);
	 			         
	 	                       } catch (ParseException e) {
	 			               // TODO Auto-generated catch block
	 			                e.printStackTrace();
	 		                   }

                  
                   if(datebooking.equals(datecurrent)) {
                     	
                 	   if (!Schedulequeryperiodsforcronofy.isEmpty()) {
                            	  schedulequeryperiodsinCurrentDate = getisdayqueryPeriods(Schedulequeryperiodsforcronofy,userKloudlessMeeting);
                          }
                        else
                          {
                         	      schedulequeryperiodsinCurrentDate = getisdayqueryPeriods(Schedulequeryperiods,userKloudlessMeeting);
                          } 
                    }
             
         	 if (kloudlessScheduleModel.getFitwiseScheduleId() != null) {
	        	
		           if(kloudlessScheduleModel.getBookingDate() != null){

		             SimpleDateFormat outputsdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);   
			 		     
						  String setbookingDate = outputsdf.format(kloudlessScheduleModel.getBookingDate());
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
		          }
		          
		          userKloudlessScheduleRepository.save(userKloudlessSchedule);
	         }
	         
	        
              ValidationUtils.throwException(userKloudlessAccount == null,
	                 CalendarConstants.CAL_ERR_ACCOUNT_NOT_FOUND, Constants.BAD_REQUEST);

	          RealtimeScheduleResponse realtimeScheduleResponse = null;
	    	   
	           
	          CreateRealtimeScheduleResponse createRealtimeScheduleResponse = new CreateRealtimeScheduleResponse();
	          
	          RealtimeScheduleoauth realtimeScheduleoauth = new RealtimeScheduleoauth();
	                 realtimeScheduleoauth.setRedirectUri(kloudlessScheduleModel.getRedirectUri());
	                 createRealtimeScheduleResponse.setRealtimeScheduleoauth(realtimeScheduleoauth);
	           
	          
	          RealtimeScheduleevent realtimeScheduleevent = new RealtimeScheduleevent();
	           
	          
	          if (kloudlessScheduleModel.getFitwiseScheduleId() == null || kloudlessScheduleModel.getFitwiseScheduleId() <= 0) {
	        	  
	        	  
	        	     realtimeScheduleevent.setEventId(userKloudlessSchedule.getUserKloudlessScheduleId().toString());
	             
	           }
	             else 
	          {
		        	  
	            	 realtimeScheduleevent.setEventId(kloudlessScheduleModel.getFitwiseScheduleId().toString());

	           }
	          
	          		UserProfile userProfilemember = userProfileRepository.findByUser(userKloudlessSchedule.getUser());
						   String memberFirstName = userProfilemember.getFirstName();
						   String memberlastName = userProfilemember.getLastName();
						   String memberFullName = memberFirstName +" "+ memberlastName;
				   
	                  
	        	     realtimeScheduleevent.setSummary(userKloudlessMeeting.getName()+" with "+ memberFullName);
	        		 realtimeScheduleevent.setTzid(userKloudlessMeeting.getTimeZone());
	        		 
	        		 if (userKloudlessMeeting.getEventDescription() != null) {
	        			 
	        			 	realtimeScheduleevent.setDescription(userKloudlessMeeting.getEventDescription());
	        		 }else {
	                       realtimeScheduleevent.setDescription("N/A");
	        		 }
	          
	        		 if(optionalPackageSession.isPresent() && optionalPackageSession.get() != null){
	        			 if (optionalPackageSession.get().getMeetingUrl() != null) {
	        				 RealtimeScheduleConferencing realtimeScheduleConferencing = new RealtimeScheduleConferencing();
	        				 realtimeScheduleConferencing.setProfile_id("explicit");
	        				 realtimeScheduleConferencing.setProvider_description("Virtual Session");
	        				 realtimeScheduleConferencing.setJoin_url(optionalPackageSession.get().getMeetingUrl());
	        				 realtimeScheduleevent.setRealtimeScheduleConferencing(realtimeScheduleConferencing);
	                       }
	                 }
	        		 if(userKloudlessSchedule.getPackageKloudlessMapping().getLocation() != null){
	        			   String full_Address = "";
	        			   if(userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getAddress() != null){
	        				   full_Address = userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getAddress()+", ";
		        		   }
	        			   if(userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getCity() != null){
	        				   full_Address = full_Address+userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getCity()+", ";
		        		   }
	        			   if(userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getZipcode() != null){
	        				   full_Address = full_Address+userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getZipcode()+", ";
		        		   }
	        			   if(userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getState() != null){
	        				   full_Address = full_Address+userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getState()+", ";
		        		   }
	        			   if(userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getCountry().getCountryName() != null){
	        				   full_Address = full_Address+userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getCountry().getCountryName();
		        		   }
	        			   if(userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getLandMark() != null){
	        				   full_Address = full_Address+", "+userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getLandMark();
		        		   }
	        			   RealtimeScheduleLocation realtimeScheduleLocation = new RealtimeScheduleLocation(); 
	      				   realtimeScheduleLocation.setDescription(full_Address);
	      				   realtimeScheduleevent.setRealtimeScheduleLocation(realtimeScheduleLocation);
	        		  }
	          
	          createRealtimeScheduleResponse.setRealtimeScheduleevent(realtimeScheduleevent);
	           
	          
	          RealtimeSchedulemembers realtimeSchedulemembers = new RealtimeSchedulemembers();
	          
	          List<String> calendarIds = new ArrayList<>();
			 
	          calendarIds.add(userKloudlessCalendar.getCalendarId());
			  
	          realtimeSchedulemembers.setSub(userKloudlessAccount.getSub());
			 
	          realtimeSchedulemembers.setCalendarids(calendarIds);
	          
	          
	          List<RealtimeSchedulemembers> Schedulemembers = new ArrayList<>();
	          
              Schedulemembers.add(realtimeSchedulemembers);
              
              if((member_Account_ID.length() > 0) && (member_Calendar_ID.length() > 0 ))
              {
            	  RealtimeSchedulemembers member_realtimeSchedulemembers = new RealtimeSchedulemembers();
            	  
            	  List<String> member_calendarIds = new ArrayList<>();
     			 
            	  member_calendarIds.add(member_Calendar_ID);
    			  
    	          member_realtimeSchedulemembers.setSub(member_Account_ID);
    			 
    	          member_realtimeSchedulemembers.setCalendarids(member_calendarIds);
    	          
                  Schedulemembers.add(member_realtimeSchedulemembers);
              }
	          
              RealtimeScheduleparticipants realtimeScheduleparticipants = new RealtimeScheduleparticipants();
	             
			  realtimeScheduleparticipants.setMembers(Schedulemembers);
			   
			  realtimeScheduleparticipants.setRequired("all");
			  
			  
			  RealtimeSchedulerequiredduration realtimeSchedulerequiredduration = new RealtimeSchedulerequiredduration();
			    
			  realtimeSchedulerequiredduration.setMinutes(userKloudlessMeeting.getDuration().getMinutes().toString());
			 
			   List<RealtimeScheduleparticipants> Scheduleparticipants = new ArrayList<>();  
			   Scheduleparticipants.add(realtimeScheduleparticipants);
			  
			  
			   RealtimeSchedulestartinterval realtimeSchedulestartinterval = new RealtimeSchedulestartinterval();
			   realtimeSchedulestartinterval.setMinutes("15");
			  
			   RealtimeSchedulebuffer RealtimeSchedulebuffer = new RealtimeSchedulebuffer();
			    
			  
			   RealtimeSchedulebufferbefore realtimeSchedulebufferbefore = new RealtimeSchedulebufferbefore();
			   
			   if(cronofyMeetingModel.getBufferbefore() != null) {
				   realtimeSchedulebufferbefore.setMinutes(cronofyMeetingModel.getBufferbefore());
			   }else {
				   realtimeSchedulebufferbefore.setMinutes("0");
			   }
			   
			   
			   RealtimeSchedulebufferafter realtimeSchedulebufferafter = new RealtimeSchedulebufferafter();
			   
			   if(cronofyMeetingModel.getBufferafter() != null) {
				   realtimeSchedulebufferafter.setMinutes(cronofyMeetingModel.getBufferafter()); 
			   }else {
				   realtimeSchedulebufferafter.setMinutes("0");
			   }
			   
			  
			  
			   RealtimeSchedulebuffer.setBufferafter(realtimeSchedulebufferafter);
			 
			   RealtimeSchedulebuffer.setBufferbefore(realtimeSchedulebufferbefore);
			  
			  
			   RealtimeScheduleavailability realtimeScheduleavailability = new RealtimeScheduleavailability();
	           
	          
			      realtimeScheduleavailability.setParticipants(Scheduleparticipants);		    
	          
	              realtimeScheduleavailability.setRequiredduration(realtimeSchedulerequiredduration);
	              if(datebooking.equals(datecurrent)) {
	            	  realtimeScheduleavailability.setQueryperiods(schedulequeryperiodsinCurrentDate);
	            	  realtimeScheduleavailability.setMaxresults(String.valueOf(schedulequeryperiodsinCurrentDate.size()));
	               }else {
	            	   if (!instructorUnavailabilities.isEmpty()) {
	 	            	  if (!Schedulequeryperiodsforcronofy.isEmpty()) {
	 	            	       realtimeScheduleavailability.setQueryperiods(Schedulequeryperiodsforcronofy);
	 	            	      realtimeScheduleavailability.setMaxresults(String.valueOf(Schedulequeryperiodsforcronofy.size()));
	 	            	  }else {
	 	            		   realtimeScheduleavailability.setQueryperiods(Schedulequeryperiods); 
	 	            		  realtimeScheduleavailability.setMaxresults(String.valueOf(Schedulequeryperiods.size()));
	 	            	  }
	 	            	  
	 	              }else {
        					  if(Schedulequeryperiodsforcronofy.isEmpty()) {
	 	            	         realtimeScheduleavailability.setQueryperiods(Schedulequeryperiods);
	 	            	        realtimeScheduleavailability.setMaxresults(String.valueOf(Schedulequeryperiods.size()));
        					  }
	 	              }  
	               }

	         
	              realtimeScheduleavailability.setStartinterval(realtimeSchedulestartinterval);
  	              realtimeScheduleavailability.setBuffer(RealtimeSchedulebuffer);	   
  	              
	        		  
	          
	           
	           createRealtimeScheduleResponse.setRealtimeScheduleavailability(realtimeScheduleavailability);
	          
	           RealtimeScheduletargetcalendars realtimeScheduletargetcalendars = new  RealtimeScheduletargetcalendars();
	          
	           realtimeScheduletargetcalendars.setSub(userKloudlessAccount.getSub());
	          
	           realtimeScheduletargetcalendars.setCalendarId(userKloudlessCalendar.getCalendarId());
	          
	           List<RealtimeScheduletargetcalendars> Scheduletargetcalendars = new ArrayList<>();
	          
	           Scheduletargetcalendars.add(realtimeScheduletargetcalendars);
	           
	           if((member_Account_ID.length() > 0) && (member_Calendar_ID.length() > 0 ))
	           {	                  
	                  RealtimeScheduletargetcalendars member_realtimeScheduletargetcalendars = new  RealtimeScheduletargetcalendars();
	    	          
	                  member_realtimeScheduletargetcalendars.setSub(member_Account_ID);
	   	          
	                  member_realtimeScheduletargetcalendars.setCalendarId(member_Calendar_ID);
	                  
	                  Scheduletargetcalendars.add(member_realtimeScheduletargetcalendars);
	            }
	           
	           createRealtimeScheduleResponse.setTargetcalendars(Scheduletargetcalendars);
	           
	           createRealtimeScheduleResponse.setCallbackUrl(kloudlessScheduleModel.getRedirectUri());
	           
	           
	           RealtimeScheduleredirecturls realtimeScheduleredirecturls= new RealtimeScheduleredirecturls();
	           
	           realtimeScheduleredirecturls.setCompletedUrl(kloudlessScheduleModel.getRedirectUri());
	         
	           createRealtimeScheduleResponse.setRealtimeScheduleredirecturls(realtimeScheduleredirecturls);
	         
	           
	           realtimeScheduleResponse = cronofyService.createscheduleInstance(createRealtimeScheduleResponse);
		          
	        
	           
	           if (realtimeScheduleResponse == null) {
				        throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED,
						null);
		        } 
			     
	            log.info("Query to create availability Rules Response : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
	            profilingEndTimeMillis = new Date().getTime();
	        
	            return realtimeScheduleResponse;
           
        }
	 
	    /**
	     * Getting schedules for a given time period
	     * @param startTimeInMillis
	     * @param endTimeInMillis
	     * @return
	     */
	    public ResponseModel getSchedulescronofy(Long startTimeInMillis, Long endTimeInMillis) throws ParseException {
	           
	    	    User user = userComponents.getUser();
                UserKloudlessAccount inst_account = getActiveAccount(user);
    			UserKloudlessCalendar inst_activeCalendar = getActiveCalendarFromKloudlessAccount(inst_account);
	    	
	            if(startTimeInMillis == null){
	    	
	    	            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_START_TIME_NULL, MessageConstants.ERROR);
	    	
	    	     }
	   
	    	    if(endTimeInMillis == null){
	    	
	    	            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_END_TIME_NULL, MessageConstants.ERROR);
	    	
	    	      }
	    	
	    	
	    	     Date startDate = new Date(startTimeInMillis);
	    	
	    	     Date endDate = new Date(endTimeInMillis);
	    	     

	    	    
	    	    if(!fitwiseUtils.isSameDay(startDate,endDate) && startDate.after(endDate)){

	                throw new ApplicationException(Constants.BAD_REQUEST,MessageConstants.MSG_START_DATE_GREATER_END_DATE,MessageConstants.ERROR);

	            }
	   
	    	    Date scheduleDate = fitwiseUtils.convertToUserTimeZone(startDate);
	          
	    	    Date endDateInUserTimezone = fitwiseUtils.convertToUserTimeZone(endDate);
	            
	         
	
	    

	    	
	    	     
		    	   List<UserKloudlessSchedule> userKloudlessSchedules = new ArrayList<>();
	    	
	    	        userKloudlessSchedules = userKloudlessScheduleRepository.findBySubscriptionPackageOwnerUserIdAndBookingDateGreaterThanEqualAndBookingDateLessThanEqual(user.getUserId(),scheduleDate,endDateInUserTimezone);
	    	
	    	       
                 
                   InstructorSchedulePayload instructorSchedulePayload= new InstructorSchedulePayload();
               
                   List<CronofyschedulePayload> schedulePayload = new ArrayList<>();
                
                 if (!userKloudlessSchedules.isEmpty()){
                 for(UserKloudlessSchedule userKloudlessSchedule : userKloudlessSchedules){
                	
                	 if(!inst_activeCalendar.getCalendarId().equals(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar().getCalendarId())) {
                 		continue;
                 	}
            	
                 CronofyschedulePayload cronofyschedulePayload = new CronofyschedulePayload();
                             cronofyschedulePayload.setApi("calendar");
                             cronofyschedulePayload.setType("event");
                             cronofyschedulePayload.setId(userKloudlessSchedule.getUserKloudlessScheduleId().toString());
                             cronofyschedulePayload.setAccountId(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar().getUserKloudlessAccount().getAccountId());
                             cronofyschedulePayload.setCalendarId(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar().getCalendarId());
                             UserProfile userProfile = userProfileRepository.findByUser(userKloudlessSchedule.getUserKloudlessMeeting().getUser());
                      SchedulePayloadcreator schedulePayloadcreator = new SchedulePayloadcreator();
                             schedulePayloadcreator.setId(userKloudlessSchedule.getUserKloudlessMeeting().getUser().getUserId().toString());
                             schedulePayloadcreator.setName(userProfile.getFirstName()+" "+userProfile.getLastName());
                             schedulePayloadcreator.setEmail(user.getEmail());
                             cronofyschedulePayload.setCreator(schedulePayloadcreator);
                                
                      SchedulePayloadorganizer schedulePayloadorganizer =new SchedulePayloadorganizer();
                             schedulePayloadorganizer.setId(userKloudlessSchedule.getUserKloudlessMeeting().getUser().getUserId().toString());
                             schedulePayloadorganizer.setName(userProfile.getFirstName()+" "+userProfile.getLastName());
                            
                             schedulePayloadorganizer.setEmail(user.getEmail());
                             cronofyschedulePayload.setOrganizer(schedulePayloadorganizer); 
                             UserProfile userProfilemember = userProfileRepository.findByUser(userKloudlessSchedule.getUser());
                             List<SchedulePayloadattendees> payloadattendees = new ArrayList<>();
                     SchedulePayloadattendees schedulePayloadattendees = new SchedulePayloadattendees();
                              
                              schedulePayloadattendees.setId(userKloudlessSchedule.getUser().getUserId().toString());
                              schedulePayloadattendees.setName(userProfilemember.getFirstName()+" "+userProfilemember.getLastName());
                              schedulePayloadattendees.setEmail(userKloudlessSchedule.getUser().getEmail());
                              schedulePayloadattendees.setStatus("pending");
                              schedulePayloadattendees.setRequired("true");
                              schedulePayloadattendees.setResource("false");
                              payloadattendees.add(schedulePayloadattendees);
                              cronofyschedulePayload.setAttendees(payloadattendees);
                             
                              cronofyschedulePayload.setCreated(userKloudlessSchedule.getUserKloudlessMeeting().getStartDateInUtc().toString());
                              cronofyschedulePayload.setModified(userKloudlessSchedule.getUserKloudlessMeeting().getEndDateInUtc().toString());
                                         
                              cronofyschedulePayload.setStart(userKloudlessSchedule.getScheduleStartTime());
                                                     
                              cronofyschedulePayload.setStartimezone(userKloudlessSchedule.getUserKloudlessMeeting().getTimeZone());

                              cronofyschedulePayload.setEnd(userKloudlessSchedule.getScheduleEndTime());
                              cronofyschedulePayload.setEndtimezone(userKloudlessSchedule.getUserKloudlessMeeting().getTimeZone());
                           
                              cronofyschedulePayload.setName(userKloudlessSchedule.getPackageKloudlessMapping().getTitle());
                              	if(userKloudlessSchedule.getPackageKloudlessMapping().getLocation() != null){
                                    cronofyschedulePayload.setLocation(userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getCity()+","+userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getState()+","+userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getCountry().getCountryName());
                           		}else{
                                    cronofyschedulePayload.setLocation("");                                   	 
                            	}
                         
                     List<SchedulePayloadcustomproperties> payloadcustomproperties = new ArrayList<>();
                                 
                     SchedulePayloadcustomproperties SPCP_country = new SchedulePayloadcustomproperties();
                                   
                              SPCP_country.setKey("country");
                              	if(userKloudlessSchedule.getPackageKloudlessMapping().getLocation() != null){
                                  	SPCP_country.setValue(userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getCountry().getCountryName());
                         		}else{
                                    SPCP_country.setValue("");
                         		}
                              SPCP_country.setPrivate(true);
                                          
                               payloadcustomproperties.add(SPCP_country);
                             
                       SchedulePayloadcustomproperties SPCP_address = new SchedulePayloadcustomproperties();        
                             
                               SPCP_address.setKey("address");
                               if(userKloudlessSchedule.getPackageKloudlessMapping().getLocation() != null){
                                   SPCP_address.setValue(userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getAddress());
                        		}else{
                                    SPCP_address.setValue("");
                        		}
                               SPCP_address.setPrivate(true);
                                  
                               payloadcustomproperties.add(SPCP_address);
                            
                       SchedulePayloadcustomproperties SPCP_fitwiseMeetingId = new SchedulePayloadcustomproperties();        
                            
                                SPCP_fitwiseMeetingId.setKey("fitwiseMeetingId");
                                SPCP_fitwiseMeetingId.setValue(userKloudlessSchedule.getPackageKloudlessMapping().getUserKloudlessMeeting().getUserKloudlessMeetingId().toString());
                                SPCP_fitwiseMeetingId.setPrivate(true);
                               
                                payloadcustomproperties.add(SPCP_fitwiseMeetingId);
                         
                       SchedulePayloadcustomproperties SPCP_city = new SchedulePayloadcustomproperties();        
                         
                                SPCP_city.setKey("city");
                                if(userKloudlessSchedule.getPackageKloudlessMapping().getLocation() != null){
                                    SPCP_city.setValue(userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getCity());
                         		}else{
                                    SPCP_city.setValue("");
                         		}
                                SPCP_city.setPrivate(true);
                            
                               payloadcustomproperties.add(SPCP_city);
                       
                        SchedulePayloadcustomproperties SPCP_meetingWindowId = new SchedulePayloadcustomproperties();        
                      
                                       SPCP_meetingWindowId.setKey("meetingWindowId");
                                       SPCP_meetingWindowId.setValue("");
                                       SPCP_meetingWindowId.setPrivate(true);
                                payloadcustomproperties.add(SPCP_meetingWindowId);
                                 
                        SchedulePayloadcustomproperties SPCP_sessionName = new SchedulePayloadcustomproperties();        
                                 
                                       SPCP_sessionName.setKey("sessionName");
                                       SPCP_sessionName.setValue(userKloudlessSchedule.getPackageKloudlessMapping().getUserKloudlessMeeting().getCalendarMeetingType().getMeetingType());
                                       SPCP_sessionName.setPrivate(true);
                                 payloadcustomproperties.add(SPCP_sessionName);
                                     
                        SchedulePayloadcustomproperties SPCP_packageId = new SchedulePayloadcustomproperties();        
                                     
                                       SPCP_packageId.setKey("packageId");
                                       SPCP_packageId.setValue(userKloudlessSchedule.getPackageKloudlessMapping().getSubscriptionPackage().getSubscriptionPackageId().toString());
                                       SPCP_packageId.setPrivate(true);
                                  payloadcustomproperties.add(SPCP_packageId);
                                     
                        SchedulePayloadcustomproperties SPCP_fitwiseScheduleId = new SchedulePayloadcustomproperties();        
                                     
                                         SPCP_fitwiseScheduleId.setKey("fitwiseScheduleId");
                                         SPCP_fitwiseScheduleId.setValue(userKloudlessSchedule.getUserKloudlessScheduleId().toString());
                                         SPCP_fitwiseScheduleId.setPrivate(true);
                                   payloadcustomproperties.add(SPCP_fitwiseScheduleId);
                                     
                         SchedulePayloadcustomproperties SPCP_packageTitle = new SchedulePayloadcustomproperties();        
                                     
                                        SPCP_packageTitle.setKey("packageTitle");
                                        SPCP_packageTitle.setValue(userKloudlessSchedule.getPackageKloudlessMapping().getTitle());
                                        SPCP_packageTitle.setPrivate(true);
                                    
                                   payloadcustomproperties.add(SPCP_packageTitle);
                                      
                          SchedulePayloadcustomproperties SPCP_zipcode = new SchedulePayloadcustomproperties();        
                                     
                                         SPCP_zipcode.setKey("zipcode");
                                         if(userKloudlessSchedule.getPackageKloudlessMapping().getLocation() != null){
                                             SPCP_zipcode.setValue(userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getZipcode());
                                  		}else{
                                            SPCP_zipcode.setValue("");
                                  		}
                                        SPCP_zipcode.setPrivate(true);
                                    
                                    payloadcustomproperties.add(SPCP_zipcode);
                                     
                           SchedulePayloadcustomproperties SPCP_sessionType = new SchedulePayloadcustomproperties();        
                                       
                                         SPCP_sessionType.setKey("sessionType");
                                         SPCP_sessionType.setValue(userKloudlessSchedule.getPackageKloudlessMapping().getUserKloudlessMeeting().getCalendarMeetingType().getMeetingTypeId().toString());
                                         SPCP_sessionType.setPrivate(true);
                                   
                                     payloadcustomproperties.add(SPCP_sessionType);
                                        
                            SchedulePayloadcustomproperties SPCP_state = new SchedulePayloadcustomproperties();        
                                        
                                         SPCP_state.setKey("state");
                                         if(userKloudlessSchedule.getPackageKloudlessMapping().getLocation() != null){
                                             SPCP_state.setValue(userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getState());
                                  		}else{
                                            SPCP_state.setValue("");
                                  		}
                                         SPCP_state.setPrivate(true);
                                   
                                     payloadcustomproperties.add(SPCP_state);
                                      
                             SchedulePayloadcustomproperties SPCP_landmark = new SchedulePayloadcustomproperties();        
                                        
                                           SPCP_landmark.setKey("landmark");
                                           if(userKloudlessSchedule.getPackageKloudlessMapping().getLocation() != null){
                                        	   SPCP_landmark.setValue(userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getLandMark());
                                           }else{
                                        	   SPCP_landmark.setValue("");
                                           }
                                          // SPCP_landmark.setValue("");
                                           SPCP_landmark.setPrivate(true);
                                   
                                      payloadcustomproperties.add(SPCP_landmark);
                                      
                             SchedulePayloadcustomproperties SPCP_sessionNameInPackage = new SchedulePayloadcustomproperties();        
                                         
                                            SPCP_sessionNameInPackage.setKey("sessionNameInPackage");
                                            SPCP_sessionNameInPackage.setValue(userKloudlessSchedule.getPackageKloudlessMapping().getUserKloudlessMeeting().getCalendarMeetingType().getMeetingType());
                                            SPCP_sessionNameInPackage.setPrivate(true);
                                   
                                       payloadcustomproperties.add(SPCP_sessionNameInPackage);
                                        
                              SchedulePayloadcustomproperties SPCP_isFitwiseEvent = new SchedulePayloadcustomproperties();        
                                         
                                            SPCP_isFitwiseEvent.setKey("isFitwiseEvent");
                                            SPCP_isFitwiseEvent.setValue("true");
                                            SPCP_isFitwiseEvent.setPrivate(true);
                                   
                                        payloadcustomproperties.add(SPCP_isFitwiseEvent);
                                        
                              SchedulePayloadcustomproperties SPCP_zoomMeetingLink = new SchedulePayloadcustomproperties();        
                                        
                          				SPCP_zoomMeetingLink.setKey("zoomMeetingLink");
                                        if(userKloudlessSchedule.getPackageKloudlessMapping().getMeetingUrl() != null){
                                        	SPCP_zoomMeetingLink.setValue(userKloudlessSchedule.getPackageKloudlessMapping().getMeetingUrl());
                                        }else{
                                        	SPCP_zoomMeetingLink.setValue("");
                                        }
                                        SPCP_zoomMeetingLink.setPrivate(true);
                            
                                        payloadcustomproperties.add(SPCP_zoomMeetingLink);



                                   
	                            if (userKloudlessSchedule.getScheduleStartTime() != null && userKloudlessSchedule.getScheduleEndTime() != null){      	
	                                
	                              SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	               		          Date Schedulestartdate = null;
	               				    try {
	               				    	Schedulestartdate = sdf.parse(userKloudlessSchedule.getScheduleStartTime());
	               				     } catch (ParseException e) {
	               					    // TODO Auto-generated catch block
	               					    e.printStackTrace();
	               			    	}
	               				   Date ScheduleEnddate = null;
	              				    try {
	              				    	ScheduleEnddate = sdf.parse(userKloudlessSchedule.getScheduleEndTime());
	              				     } catch (ParseException e) {
	              					    // TODO Auto-generated catch block
	              					    e.printStackTrace();
	              			    	}
	              				    Date scheduleDateafterformat = getTimeInUTC(Schedulestartdate);
	              			          
	              			        Date endDateafterformat = getTimeInUTC(ScheduleEnddate);
	               	              
	              			        List<InstructorUnavailability> instructorUnavailabilities_inner = instructorUnavailabilityRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndUserKloudlessCalendar(scheduleDateafterformat, endDateafterformat, userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar());
	                            	
	                            	if (instructorUnavailabilities_inner != null){
	                            		for(InstructorUnavailability instructorUnavailability_inner : instructorUnavailabilities_inner){
	                            			SchedulePayloadcustomproperties SPCP_instructorUnavailabilityId = new SchedulePayloadcustomproperties();        
		                                    
		                                    SPCP_instructorUnavailabilityId.setKey("instructorUnavailabilityId");
		                                    SPCP_instructorUnavailabilityId.setValue(instructorUnavailability_inner.getInstructorUnavailabilityId().toString());
		                                    SPCP_instructorUnavailabilityId.setPrivate(true);  
		                                    payloadcustomproperties.add(SPCP_instructorUnavailabilityId); 
		                                    break;
		                                }
	                               	 
	                                }else {
	                                    SchedulePayloadcustomproperties SPCP_instructorUnavailabilityId = new SchedulePayloadcustomproperties();        
	                                    
	                                    SPCP_instructorUnavailabilityId.setKey("instructorUnavailabilityId");
	                                    SPCP_instructorUnavailabilityId.setValue("");
	                                    SPCP_instructorUnavailabilityId.setPrivate(false); 
	                                    payloadcustomproperties.add(SPCP_instructorUnavailabilityId);    
	                                }
	                            } 
	                        
                                cronofyschedulePayload.setCustom_properties(payloadcustomproperties);
                               
                                schedulePayload.add(cronofyschedulePayload);
							      
							    
                       }
                 }
	                 
	                long oneWeekBeforeStartTime = scheduleDate.getTime() - (7 * 24 * 3600 * 1000);
	                Date oneWeekBeforeStartDate = new Date(oneWeekBeforeStartTime);
	                long oneWeekAfterEndTime = endDateInUserTimezone.getTime() + (7 * 24 * 3600 * 1000);
	                Date oneWeekAfterEndDate = new Date(oneWeekAfterEndTime);
                 	List<UserKloudlessSchedule> UNAV_userKloudlessSchedules = new ArrayList<>();
	    	        UNAV_userKloudlessSchedules = userKloudlessScheduleRepository.findByUserAndBookingDateGreaterThanEqualAndBookingDateLessThanEqualAndMeetingTypeIdIsNull(user,oneWeekBeforeStartDate,oneWeekAfterEndDate);
	    	        if (!UNAV_userKloudlessSchedules.isEmpty()){

	                    for(UserKloudlessSchedule UNAV_userKloudlessSchedule : UNAV_userKloudlessSchedules){
	                    	
	                    	CronofyschedulePayload cronofyschedulePayload = new CronofyschedulePayload();
                            cronofyschedulePayload.setApi("calendar");
                            cronofyschedulePayload.setType("event");
                            if(inst_account != null) 
                            {
                            	cronofyschedulePayload.setAccountId(inst_account.getAccountId());	
                            }
                            if(inst_activeCalendar != null)
                            {
                            	cronofyschedulePayload.setCalendarId(inst_activeCalendar.getCalendarId());
                            }
                            cronofyschedulePayload.setId(UNAV_userKloudlessSchedule.getUserKloudlessScheduleId().toString());
                            cronofyschedulePayload.setStart(UNAV_userKloudlessSchedule.getScheduleStartTime());
                            cronofyschedulePayload.setEnd(UNAV_userKloudlessSchedule.getScheduleEndTime());                         
                            cronofyschedulePayload.setName("Unavailable");
	                        
                            List<SchedulePayloadcustomproperties> payloadcustomproperties = new ArrayList<>();
                            SchedulePayloadcustomproperties SPCP_fitwiseMeetingId = new SchedulePayloadcustomproperties();        
                            SPCP_fitwiseMeetingId.setKey("fitwiseMeetingId");
                            SPCP_fitwiseMeetingId.setValue(UNAV_userKloudlessSchedule.getUserKloudlessScheduleId().toString());
                            SPCP_fitwiseMeetingId.setPrivate(true);
                            payloadcustomproperties.add(SPCP_fitwiseMeetingId);
                            
                            SchedulePayloadcustomproperties SPCP_isFitwiseEvent = new SchedulePayloadcustomproperties();              
                            SPCP_isFitwiseEvent.setKey("isFitwiseEvent");
                            SPCP_isFitwiseEvent.setValue("true");
                            SPCP_isFitwiseEvent.setPrivate(true);
                            payloadcustomproperties.add(SPCP_isFitwiseEvent);
                            
                            if (UNAV_userKloudlessSchedule.getScheduleStartTime() != null && UNAV_userKloudlessSchedule.getScheduleEndTime() != null && inst_activeCalendar != null){      	
                                
	                              SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	               		          Date Schedulestartdate = null;
	               				    try {
	               				    	Schedulestartdate = sdf.parse(UNAV_userKloudlessSchedule.getScheduleStartTime());
	               				     } catch (ParseException e) {
	               					    // TODO Auto-generated catch block
	               					    e.printStackTrace();
	               			    	}
	               				   Date ScheduleEnddate = null;
	              				    try {
	              				    	ScheduleEnddate = sdf.parse(UNAV_userKloudlessSchedule.getScheduleEndTime());
	              				     } catch (ParseException e) {
	              					    // TODO Auto-generated catch block
	              					    e.printStackTrace();
	              			    	}
	              				    Date scheduleDateafterformat = getTimeInUTC(Schedulestartdate);
	              			          
	              			        Date endDateafterformat = getTimeInUTC(ScheduleEnddate);
	               	              
	              			        List<InstructorUnavailability> instructorUnavailabilities_inner = instructorUnavailabilityRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndUserKloudlessCalendar(scheduleDateafterformat, endDateafterformat, inst_activeCalendar);
	                            	
	                            	if (instructorUnavailabilities_inner != null){
	                            		for(InstructorUnavailability instructorUnavailability_inner : instructorUnavailabilities_inner){
	                            			SchedulePayloadcustomproperties SPCP_instructorUnavailabilityId = new SchedulePayloadcustomproperties();        
		                                    
		                                    SPCP_instructorUnavailabilityId.setKey("instructorUnavailabilityId");
		                                    SPCP_instructorUnavailabilityId.setValue(instructorUnavailability_inner.getInstructorUnavailabilityId().toString());
		                                    SPCP_instructorUnavailabilityId.setPrivate(true);  
		                                    payloadcustomproperties.add(SPCP_instructorUnavailabilityId); 
		                                    if(instructorUnavailability_inner.getEventId().toString().equals(UNAV_userKloudlessSchedule.getUserKloudlessScheduleId().toString()))
		                                    {
		                                    	cronofyschedulePayload.setCustom_properties(payloadcustomproperties);
				                                schedulePayload.add(cronofyschedulePayload);
		                                    }
		                                    break;
		                                }
	                                }
	                            } 
                           }
	                    }
             
                           
			                 instructorSchedulePayload.setCronofyschedulePayload(schedulePayload);
			                 instructorSchedulePayload.setCount(schedulePayload.size());
	                         instructorSchedulePayload.setPage("1");
	                         instructorSchedulePayload.setType("object_list");
	                         instructorSchedulePayload.setApi("calendar");
                           
                           List<String> instructorSchedule_Unavailable_Days = new ArrayList<>();
                           List<InstructorUnavailability> instructorUnavailabilities = instructorUnavailabilityRepository.findByUserKloudlessCalendar(inst_activeCalendar);
                           for(InstructorUnavailability InstructorUnavailability : instructorUnavailabilities){
                        	   
                        	   	Date DateInUserTimezone_Start = fitwiseUtils.convertToUserTimeZone(InstructorUnavailability.getStartDate());
                        	   	Date DateInUserTimezone_End = fitwiseUtils.convertToUserTimeZone(InstructorUnavailability.getEndDate());
                        	   	Calendar calendar = new GregorianCalendar();
                        	    calendar.setTime(DateInUserTimezone_Start);
                        	    calendar.set(Calendar.HOUR_OF_DAY, 0);
                        	    calendar.set(Calendar.MINUTE, 0);
                        	    calendar.set(Calendar.SECOND, 0);
                        	    calendar.set(Calendar.MILLISECOND, 0);
                        	    
                        	   while (calendar.getTime().before(DateInUserTimezone_End))
                        	    {
                        	        Date result = calendar.getTime();
                        	        SimpleDateFormat un_avl_sdf = new SimpleDateFormat("yyyy-MM-dd");
                        	        String un_avl_date = un_avl_sdf.format(result);
                        	        instructorSchedule_Unavailable_Days.add(un_avl_date);
                        	        calendar.add(Calendar.DATE, 1);
                        	    }
                        	   
                           }
                           
                           instructorSchedulePayload.setUnavailable_days(instructorSchedule_Unavailable_Days);
                         
                           return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED,instructorSchedulePayload);
  
	     }
	    
	        public Date getTimeInUTC(Date startDate) {
	        	
	        	
	        	SimpleDateFormat outputsdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   
  	 		     
 		          String setbookingDate = outputsdf.format(startDate);
 		         
 		          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		         
 		          Date date = null;
 				    try {
 				    	date = sdf.parse(setbookingDate);
 				     } catch (ParseException e) {
 					    // TODO Auto-generated catch block
 					    e.printStackTrace();
 			    	}
 				    return date;
	        }
	        
	        public Date getExactUnavailibilityDate(Date startDate, Date endDate, int duration){

 		          	Date exactDate = startDate;
 		          	Calendar calendar = new GregorianCalendar();
 		          	calendar.setTime(exactDate);
 		          	while (calendar.getTime().before(endDate))
	           	    {
	           	        calendar.add(Calendar.MINUTE, duration);
	           	        exactDate = calendar.getTime();
	           	    }
 				    return exactDate;
	        }
	        
	        public List<InstructorUnavailability> getFreeBusyUnavailabilities(final KloudlessScheduleModel kloudlessScheduleModel, final UserKloudlessAccount userKloudlessAccount, final UserKloudlessCalendar userKloudlessCalendar, final UserKloudlessMeeting userKloudlessMeeting){
	        	
	        	List<InstructorUnavailability> instructorUnavailabilities = new ArrayList<>();
	        	RefreshAccessTokenResponse refreshAccessTokenResponse = null;
 		        
  	        	refreshAccessTokenResponse = cronofyService.refreshAccessToken(userKloudlessAccount.getRefreshToken());
  		        
  		        if (refreshAccessTokenResponse == null) {
  		        	return instructorUnavailabilities;
  			    }

		        Date bookingDate = kloudlessScheduleModel.getBookingDate();
		        long minBookingDateTime = bookingDate.getTime() - (7 * 24 * 3600 * 1000);
		        Date minBookingDate = new Date(minBookingDateTime);
		        long maxBookingDateTime = bookingDate.getTime() + (14 * 24 * 3600 * 1000);
		        Date maxBookingDate = new Date(maxBookingDateTime);
		        SimpleDateFormat outputsdf = new SimpleDateFormat("yyyy-MM-dd");   
		        String startDate = outputsdf.format(minBookingDate);
		        String endDate = outputsdf.format(maxBookingDate);
		          
  		        FreeBusyResponse getFreeBusyResponse = cronofyService.getFreeBusyEvents(refreshAccessTokenResponse.getAccessToken(), userKloudlessCalendar.getCalendarId(), startDate, endDate);
  		        if (getFreeBusyResponse == null) {
		        	return instructorUnavailabilities;
			    }

  		        List<FreeBusyEvent> freebusyevents = getFreeBusyResponse.getFreebusyevents();
  		        if (!freebusyevents.isEmpty()) 
  		        {
  		        	for(FreeBusyEvent freebusyevent : freebusyevents)
  		        	{
  		        		InstructorUnavailability instructorUnavailability = new InstructorUnavailability();
  		        		String start_end_format = "yyyy-MM-dd";
  		        		if(freebusyevent.getStart().length() > 11)
  		        		{
  		        			start_end_format = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  		        		}
  		        		SimpleDateFormat start_end_sdf = new SimpleDateFormat(start_end_format);
  		        		Date start_date = null;
  	 				    try {
  	 				    	start_date = start_end_sdf.parse(freebusyevent.getStart());
  	 				     } catch (ParseException e) {
  	 					    // TODO Auto-generated catch block
  	 					    e.printStackTrace();
  	 			    	}
  	 				    OffsetDateTime start_offsetDate = start_date.toInstant().atOffset(ZoneOffset.UTC);
  	 				    int schedule_duration = userKloudlessMeeting.getDuration().getMinutes();
  	 				    if(schedule_duration >= 30 && schedule_duration < 45 )
  	 				    {
  	 				    	schedule_duration = 30;
  	 				    }
  	 				    if(schedule_duration >= 45 && schedule_duration < 60) {
				    		schedule_duration = 15;
  	 				    }
  	 				    if(schedule_duration>=60) {
  	 				    	schedule_duration = 30;
  	 				    }
  	 				    
  	 				    if(start_offsetDate.getMinute() > 0) {
  	 				    	int start_minute_differ = (start_offsetDate.getMinute() % schedule_duration);
		  	 				OffsetDateTime start_offsetDateTime = start_offsetDate.minusMinutes(start_minute_differ);
		  	 				start_date = Date.from(start_offsetDateTime.toInstant());    	
  	 				    }

  	 				    Date end_date = null;
	  				    try {
	  				    	end_date = start_end_sdf.parse(freebusyevent.getEnd());
	  				     } catch (ParseException e) {
	  					    // TODO Auto-generated catch block
	  					    e.printStackTrace();
	  			    	}
	  				    OffsetDateTime end_offsetDate = end_date.toInstant().atOffset(ZoneOffset.UTC);
	 				    if(end_offsetDate.getMinute() > 0) {
	 				    	int end_minute_differ = (end_offsetDate.getMinute() % schedule_duration);
	 				    	if(end_minute_differ != 0)
	 				    	{
			  	 				OffsetDateTime end_offsetDateTime = end_offsetDate.plusMinutes(schedule_duration - end_minute_differ);
			  	 				end_date = Date.from(end_offsetDateTime.toInstant()); 
			  	 			}
	 				    }

  		        		instructorUnavailability.setStartDate(start_date);
  		        		instructorUnavailability.setEndDate(end_date);
  		        		instructorUnavailabilities.add(instructorUnavailability);
  		        	}
			    }
  		        
  		        return instructorUnavailabilities;
	        	
	        }
	        public List<InstructorUnavailability> getMemberUnavailabilities(final KloudlessScheduleModel kloudlessScheduleModel, final UserKloudlessAccount userKloudlessAccount, final User member, final UserKloudlessMeeting userKloudlessMeeting){
	        	
	        	List<InstructorUnavailability> memberUnavailabilities = new ArrayList<>();
	        	
		        Date bookingDate = kloudlessScheduleModel.getBookingDate();
		        TimeZone defaultTimeZone = TimeZone.getTimeZone("Etc/UTC");
		        ZonedDateTime bookingDay = bookingDate.toInstant().atZone(defaultTimeZone.toZoneId())
	                    .with(ChronoField.HOUR_OF_DAY, 0)
	                    .with(ChronoField.MINUTE_OF_DAY, 0)
	                    .with(ChronoField.SECOND_OF_DAY, 0);
	            Date startTimeInUtc = Date.from(bookingDay.withZoneSameInstant(ZoneId.systemDefault()).toInstant());
	            Date maxBookingEndDate = Date.from(bookingDay.with(LocalTime.MAX).withZoneSameInstant(ZoneId.systemDefault()).toInstant());
	            long maxBookingEndDateTime = maxBookingEndDate.getTime() + (7 * 24 * 3600 * 1000);
		        Date  endTimeInUtc = new Date(maxBookingEndDateTime);

	            List<UserKloudlessSchedule> member_userKloudlessSchedules = new ArrayList<>();
    	        member_userKloudlessSchedules = userKloudlessScheduleRepository.findByUserAndBookingDateGreaterThanEqualAndBookingDateLessThanEqual(member,startTimeInUtc,endTimeInUtc);
	            List<UserKloudlessSchedule> instructor_userKloudlessSchedules = new ArrayList<>();
	            instructor_userKloudlessSchedules = userKloudlessScheduleRepository.findBySubscriptionPackageOwnerUserIdAndBookingDateGreaterThanEqualAndBookingDateLessThanEqual(userKloudlessAccount.getUser().getUserId(),startTimeInUtc,endTimeInUtc);
	            
	            if (!instructor_userKloudlessSchedules.isEmpty()){
    	        	for(UserKloudlessSchedule instructor_userKloudlessSchedule : instructor_userKloudlessSchedules)
  		        	{
    	        		if(!member_userKloudlessSchedules.contains(instructor_userKloudlessSchedule))
    	        		{
    	        			member_userKloudlessSchedules.add(instructor_userKloudlessSchedule);
    	        		}
  		        	}
    	        }
	            
	            int schedule_duration = userKloudlessMeeting.getDuration().getMinutes();
    	        if (!member_userKloudlessSchedules.isEmpty()){
    	        	for(UserKloudlessSchedule member_userKloudlessSchedule : member_userKloudlessSchedules)
  		        	{
    	        		String startTime = "";
    	               	if(member_userKloudlessSchedule.getScheduleStartTime() != null)
    	               	{
    	               		startTime = member_userKloudlessSchedule.getScheduleStartTime();
    	               	}
    	               			
	                   String endTime = "";
	                   if(member_userKloudlessSchedule.getScheduleEndTime() != null)
    	               	{
    	                   	endTime = member_userKloudlessSchedule.getScheduleEndTime();
    	               	}
	                   if(!startTime.isEmpty() || !endTime.isEmpty())
	                   {
	                   		SimpleDateFormat outputsdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

                  	 		Date ScheduleStartTimeInUtc = null;
    	     			    try {
    	     			    	ScheduleStartTimeInUtc = outputsdf.parse(startTime);
    	     			     } catch (ParseException e) {
    	     				    // TODO Auto-generated catch block
    	     				    e.printStackTrace();
    	     		    	}
    	     			   OffsetDateTime start_offsetDate = ScheduleStartTimeInUtc.toInstant().atOffset(ZoneOffset.UTC);
//     	 				    if(schedule_duration >= 30)
//     	 				    {
//     	 				    	schedule_duration = 30;
//     	 				    }
    	     			   
    	     			  if(schedule_duration >= 30 && schedule_duration < 45 )
    	 				    {
    	 				    	schedule_duration = 30;
    	 				    }
    	 				    if(schedule_duration >= 45 && schedule_duration < 60) {
  				    		schedule_duration = 15;
    	 				    }
    	 				    if(schedule_duration>=60) {
    	 					schedule_duration = 30;
    	 				    }

     	 				    if(start_offsetDate.getMinute() > 0) {
     	 				    int start_minute_differ = (start_offsetDate.getMinute() % schedule_duration);
   		  	 				OffsetDateTime start_offsetDateTime = start_offsetDate.minusMinutes(start_minute_differ);
   		  	 				ScheduleStartTimeInUtc = Date.from(start_offsetDateTime.toInstant());    	
     	 				    }
    	     			    Date scheduleEndTimeInUtc = null;
    	     			    try {
    	     			    	scheduleEndTimeInUtc = outputsdf.parse(endTime);
    	     			     } catch (ParseException e) {
    	     				    // TODO Auto-generated catch block
    	     				    e.printStackTrace();
    	     		    	}
    	     			   OffsetDateTime end_offsetDate = scheduleEndTimeInUtc.toInstant().atOffset(ZoneOffset.UTC);
   	 				    if(end_offsetDate.getMinute() > 0) {
   	 				    	int end_minute_differ = (end_offsetDate.getMinute() % schedule_duration);
   	 				    	if(end_minute_differ != 0)
   	 				    	{
   			  	 				OffsetDateTime end_offsetDateTime = end_offsetDate.plusMinutes(schedule_duration - end_minute_differ);
   			  	 				scheduleEndTimeInUtc = Date.from(end_offsetDateTime.toInstant()); 
   			  	 			}
   	 				    }

    	     			   InstructorUnavailability memberUnavailability = new InstructorUnavailability();
    	     			   memberUnavailability.setStartDate(ScheduleStartTimeInUtc);
    	     			   memberUnavailability.setEndDate(scheduleEndTimeInUtc);
    	     			   memberUnavailabilities.add(memberUnavailability);
	                   }
  		        	}
    	        	
    	        }
		          
  		        return memberUnavailabilities;
	        }
	        
	       
	      private List<RealtimeSchedulequeryperiods> GetSchedulePeriodsForNext7Days(Date firstBookingDate, UserKloudlessMeeting  userKloudlessMeeting, CronofyAvailabilityRules cronofyAvailabilityRules, PackageSubscription packageSubscription,User user,SubscriptionPackage subscriptionPackage) {
	  	    	   

    	   		List<RealtimeSchedulequeryperiods>  schedulequeryperiodsFinal = new ArrayList<>();
    	   		List<UserKloudlessSchedule> DiscardSchedule = new ArrayList<>();

    	   		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszzz");
  	    	 
    	   		for(int dayCount=1; dayCount<=7;dayCount++){
    	   			 DiscardSchedule.clear();
  		 	         Calendar fcal = Calendar.getInstance();
  		 	         fcal.setTime(firstBookingDate);
  		 	         fcal.add(Calendar.HOUR, 24*dayCount);
  		 	         Date bookingDate = fcal.getTime();
  		 	         
  		 	         SimpleDateFormat outputsdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);   
		 		     
				     String setbookingDate = outputsdf.format(bookingDate);
			         String setbookingTime = "18:30:00";
			         String setbookingDateTime = setbookingDate +" "+ setbookingTime; 
	   	        
			         
			         
			         SimpleDateFormat sdffind = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
			         sdffind.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
   		         
   		             Date date = null;
   				     try {
   				    	date = sdffind.parse(setbookingDateTime);
   				           } catch (ParseException e) {
   					       // TODO Auto-generated catch block
   					   e.printStackTrace();
   			         }
   		           
  		 	         
  		    	      DiscardSchedule = userKloudlessScheduleRepository.findByUserAndBookingDateAndSubscriptionPackageSubscriptionPackageId(user, date,subscriptionPackage.getSubscriptionPackageId());
  		    	     
  		    	     if (DiscardSchedule.isEmpty()) {
  		    	      
  		    	    	 SimpleDateFormat df = new SimpleDateFormat( "EEEE" ); 
  		 			 
    		 	         String day= df.format( bookingDate ); 
    		 	         
    		 	         SimpleDateFormat dfymd = new SimpleDateFormat( "yyyy-MM-dd" );
    	              
    		 	         String ymd = dfymd.format( bookingDate ); 
    		 	         
    		 	         JSONArray jsonArray = new JSONArray(cronofyAvailabilityRules.getWeeklyPeriods());
    		        
    		 	         for (int i = 0; i < jsonArray.length(); i++) {
  	  	    	   
  	  	    	       JSONObject jsonObject = jsonArray.getJSONObject(i);
  	  	    	   
  	  	    	       String  weeklyDay = jsonObject.getString("day");
  	  	    	   
  	  	    	   
  	  	    	       if(weeklyDay.toLowerCase().equals(day.toLowerCase())) {
  	  	    	    	   
  	  	    	    	   	String  starttime = jsonObject.getString("start_time");
  	  		    	      
  	  	    	    	   	String  endttime = jsonObject.getString("end_time");
  		    	     
  	  	    	    	   	String  startdaytime = ymd+" "+starttime;

  	  	    	    	   	String  enddaytime = ymd+" "+endttime;
  	  	    	    	   	
  	  	    	    	  
  	  	    	    	   	Calendar cal = Calendar.getInstance();
  	                        cal.setTime(packageSubscription.getSubscribedDate());
  	                        cal.add(Calendar.DAY_OF_YEAR, Math.toIntExact(packageSubscription.getSubscriptionPlan().getDuration())-1);
       			 
  	  	    	    	   	if(bookingDate.before(cal.getTime())) {
  				            
  				            	RealtimeSchedulequeryperiods realtimeSchedulequeryperiods = new RealtimeSchedulequeryperiods();
  		                        realtimeSchedulequeryperiods.setStart(GetFormatDate(startdaytime,userKloudlessMeeting.getTimeZone()));
  					            realtimeSchedulequeryperiods.setEnd(GetFormatDate(enddaytime,userKloudlessMeeting.getTimeZone()));
  				            	schedulequeryperiodsFinal.add(realtimeSchedulequeryperiods);  
  	  	    	       		} 

  	  	             	}
      	   		       }
  		    		   
  		    	     }

  		 	     
    	   		}
    	   		return schedulequeryperiodsFinal;
  		  }


	    
}
