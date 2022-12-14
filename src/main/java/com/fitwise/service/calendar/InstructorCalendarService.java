package com.fitwise.service.calendar;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.CalendarConstants;
import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.calendar.UserKloudlessAccount;
import com.fitwise.entity.calendar.UserKloudlessCalendar;
import com.fitwise.entity.calendar.UserKloudlessMeeting;
import com.fitwise.entity.calendar.UserKloudlessSchedule;
import com.fitwise.entity.calendar.ZoomAccount;
import com.fitwise.entity.calendar.ZoomMeeting;
import com.fitwise.entity.packaging.PackageKloudlessMapping;
import com.fitwise.entity.subscription.PackageSubscription;
import com.fitwise.exception.ApplicationException;
import com.fitwise.model.DeleteFitwiseSchedulesRequestModel;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.calendar.UserKloudlessMeetingRepository;
import com.fitwise.repository.calendar.UserKloudlessScheduleRepository;
import com.fitwise.repository.packaging.PackageKloudlessMappingRepository;
import com.fitwise.repository.subscription.PackageSubscriptionRepository;
import com.fitwise.response.BookedScheduleView;
import com.fitwise.response.SchedulesDayView;
import com.fitwise.service.InstructorUnavailabilityService;
import com.fitwise.service.cronofy.CronofyService;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.calendar.SubscriptionPackageScheduleAvailability;
import com.fitwise.view.cronofy.CronofyschedulePayload;
import com.fitwise.view.cronofy.RefreshAccessTokenResponse;
import com.fitwise.view.cronofy.SchedulePayloadcreator;
import com.fitwise.view.cronofy.SchedulePayloadcustomproperties;
import com.fitwise.view.cronofy.SchedulePayloadorganizer;
import com.google.gson.Gson;
import com.kloudless.models.ResponseRaw;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

@Service
@Slf4j
public class InstructorCalendarService {

    @Autowired
    private UserKloudlessScheduleRepository userKloudlessScheduleRepository;

    @Autowired
    private UserComponents userComponents;

    @Autowired
    private FitwiseUtils fitwiseUtils;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserKloudlessMeetingRepository userKloudlessMeetingRepository;

    @Autowired
    private KloudLessService kloudLessService;

    @Autowired
    private ZoomService zoomService;

    @Autowired
    private PackageSubscriptionRepository packageSubscriptionRepository;

    @Autowired
    private PackageKloudlessMappingRepository packageKloudlessMappingRepository;

    @Autowired
    private InstructorUnavailabilityService instructorUnavailabilityService;

    @Autowired
    private CalendarService calendarService;
    
    @Autowired
	private CronofyService cronofyService;
    
    /**

     * Getting schedules for a given time period

     * @param startTimeInMillis

     * @param endTimeInMillis

     * @return

     */

     public ResponseModel getSchedules(Long startTimeInMillis, Long endTimeInMillis) throws ParseException {
        
    	 User user = userComponents.getUser();

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
    	   	Calendar calendar = new GregorianCalendar();
    	    calendar.setTime(scheduleDate);
    	    calendar.set(Calendar.HOUR_OF_DAY, 18);
    	    calendar.set(Calendar.MINUTE, 30);
    	    calendar.set(Calendar.SECOND, 0);
    	    calendar.set(Calendar.MILLISECOND, 0);
   	    	Date scheduleDateInUserTimezone = calendar.getTime();
   	    	
   	    	Date scheduleEndDate = fitwiseUtils.convertToUserTimeZone(endDate);
    	   	Calendar ecalendar = new GregorianCalendar();
    	   	ecalendar.setTime(scheduleEndDate);
    	   	ecalendar.set(Calendar.HOUR_OF_DAY, 18);
    	   	ecalendar.set(Calendar.MINUTE, 30);
    	   	ecalendar.set(Calendar.SECOND, 0);
    	   	ecalendar.set(Calendar.MILLISECOND, 0);
   	    	Date scheduleEndDateInUserTimezone = ecalendar.getTime();
   	    	
         List<UserKloudlessSchedule> userKloudlessScheduleList = userKloudlessScheduleRepository.findBySubscriptionPackageOwnerUserId(user.getUserId());
           if(userKloudlessScheduleList.isEmpty()){
             throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS,MessageConstants.MSG_DATA_NOT_AVAILABLE,null);
           }

          List<SchedulesDayView> schedulesDayViews = new ArrayList<>();

             List<UserKloudlessSchedule> userKloudlessSchedules_All = new ArrayList<>();
             List<UserKloudlessSchedule> userKloudlessSchedules = new ArrayList<>();
           
             userKloudlessSchedules_All = userKloudlessScheduleRepository.findBySubscriptionPackageOwnerUserIdAndBookingDateGreaterThanEqualAndBookingDateLessThanEqual(user.getUserId(),scheduleDateInUserTimezone,scheduleEndDateInUserTimezone);
            
            for(UserKloudlessSchedule userKloudlessSchedule_Item : userKloudlessSchedules_All)
            {
            	String startTime = "";
            	if(userKloudlessSchedule_Item.getScheduleStartTime() != null)
            	{
            		startTime = userKloudlessSchedule_Item.getScheduleStartTime();
            	}
            			
                String endTime = "";
                if(userKloudlessSchedule_Item.getScheduleEndTime() != null)
            	{
                	endTime = userKloudlessSchedule_Item.getScheduleEndTime();
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

                    Date scheduleEndTimeInUtc = null;
	     			    try {
	     			    	scheduleEndTimeInUtc = outputsdf.parse(endTime);
	     			     } catch (ParseException e) {
	     				    // TODO Auto-generated catch block
	     				    e.printStackTrace();
	     		    	}

                    if((startDate.before(ScheduleStartTimeInUtc) && endDate.after(ScheduleStartTimeInUtc)) 
                   	|| (startDate.before(scheduleEndTimeInUtc) && endDate.after(scheduleEndTimeInUtc))
                       || (startDate.before(ScheduleStartTimeInUtc) && endDate.after(scheduleEndTimeInUtc))
                       || (startDate.after(ScheduleStartTimeInUtc) && endDate.before(scheduleEndTimeInUtc))){
                    		userKloudlessSchedules.add(userKloudlessSchedule_Item);
                  		  }
                	
                }
            }
             
             
             for(UserKloudlessSchedule userKloudlessSchedule : userKloudlessSchedules){
            	 
            	 SchedulesDayView schedulesDayView = new SchedulesDayView();

//            	 SimpleDateFormat displaysimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
//              	 schedulesDayView.setBookedDate(displaysimpleDateFormat.format(userKloudlessSchedule.getBookingDate())+", "+displaysimpleDateFormat.format(startDate)+", "+displaysimpleDateFormat.format(endDate)+", "+displaysimpleDateFormat.format(scheduleDate)+", "+displaysimpleDateFormat.format(scheduleDateInUserTimezone));
              	 schedulesDayView.setBookedDate(userKloudlessSchedule.getScheduleStartTime());
              	 List<BookedScheduleView> bookedSchedules = new ArrayList<>();

                         BookedScheduleView bookedScheduleView = new BookedScheduleView();
                         bookedScheduleView.setKloudlessScheduleId(userKloudlessSchedule.getUserKloudlessScheduleId());
                         bookedScheduleView.setKloudlessMetingId(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessMeetingId());
                         bookedScheduleView.setSessionTitle(userKloudlessSchedule.getUserKloudlessMeeting().getName());
                         if(userKloudlessSchedule.getPackageKloudlessMapping() != null){
                             bookedScheduleView.setSessionTitleInPackage(userKloudlessSchedule.getPackageKloudlessMapping().getTitle());
                         }
                         if(userKloudlessSchedule.getMeetingTypeId() != null && userKloudlessSchedule.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingTypeId() == CalendarConstants.SESSION_IN_PERSON_OR_VIRTUAL){
                             bookedScheduleView.setMeetingTypeId(userKloudlessSchedule.getMeetingTypeId().getMeetingTypeId());
                             bookedScheduleView.setMeetingType(userKloudlessSchedule.getMeetingTypeId().getMeetingType());
                         }else{
                             bookedScheduleView.setMeetingTypeId(userKloudlessSchedule.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingTypeId());
                             bookedScheduleView.setMeetingType(userKloudlessSchedule.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingType());
                         }
                         
                         
                         
                         CronofyschedulePayload cronofyschedulePayload = new CronofyschedulePayload();
                         
                         cronofyschedulePayload.setAccountId(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar().getUserKloudlessAccount().getAccountId());
                         cronofyschedulePayload.setCalendarId(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar().getCalendarId());
                                       
                           SchedulePayloadcreator schedulePayloadcreator = new SchedulePayloadcreator();
                               schedulePayloadcreator.setEmail(user.getEmail());
                               cronofyschedulePayload.setCreator(schedulePayloadcreator);
                                       
                         SchedulePayloadorganizer schedulePayloadorganizer =new SchedulePayloadorganizer();
                               schedulePayloadorganizer.setEmail(user.getEmail());
                               cronofyschedulePayload.setOrganizer(schedulePayloadorganizer); 
                                         
                               cronofyschedulePayload.setCreated(userKloudlessSchedule.getUserKloudlessMeeting().getStartDateInUtc().toString());
                               cronofyschedulePayload.setModified(userKloudlessSchedule.getUserKloudlessMeeting().getEndDateInUtc().toString());
                                         
                                
                             cronofyschedulePayload.setStart(userKloudlessSchedule.getScheduleStartTime());
                             cronofyschedulePayload.setStartimezone(userKloudlessSchedule.getUserKloudlessMeeting().getTimeZone());

                             cronofyschedulePayload.setEnd(userKloudlessSchedule.getScheduleEndTime());
                             cronofyschedulePayload.setEndtimezone(userKloudlessSchedule.getUserKloudlessMeeting().getTimeZone());
                                  
                             cronofyschedulePayload.setName(userKloudlessSchedule.getPackageKloudlessMapping().getTitle());
                                
                                
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
                                                  		if(userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getLandMark() != null){
                                                  			SPCP_landmark.setValue(userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getLandMark());
                                                     	}else{
                                                     		SPCP_landmark.setValue("");                                     	 
                                                      	}
                                               		}else{
                                               			SPCP_landmark.setValue("");                               	 
                                                	}
                                                  	SPCP_landmark.setPrivate(true);
                                          
                                                  payloadcustomproperties.add(SPCP_landmark);
                                             
                                    SchedulePayloadcustomproperties SPCP_sessionNameInPackage = new SchedulePayloadcustomproperties();        
                                                
                                                   SPCP_sessionNameInPackage.setKey("sessionNameInPackage");
                                                   SPCP_sessionNameInPackage.setValue(userKloudlessSchedule.getPackageKloudlessMapping().getUserKloudlessMeeting().getCalendarMeetingType().getMeetingType());
                                                   SPCP_sessionNameInPackage.setPrivate(true);
                                          
                                                   payloadcustomproperties.add(SPCP_sessionNameInPackage);
                                               
                                     SchedulePayloadcustomproperties SPCP_isFitwiseEvent = new SchedulePayloadcustomproperties();        
                                                
                                                   SPCP_isFitwiseEvent.setKey("isFitwiseEvent");
                                                   SPCP_isFitwiseEvent.setValue("");
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
                           
                                           
                                   cronofyschedulePayload.setCustom_properties(payloadcustomproperties);
                                   Gson gson = new Gson();
                                   bookedScheduleView.setSchedulePayload(gson.toJson(cronofyschedulePayload));

                         
                         UserProfile memberProfile = userProfileRepository.findByUser(userKloudlessSchedule.getUser());
                         bookedScheduleView.setMemberName(memberProfile.getFirstName() + KeyConstants.KEY_SPACE + memberProfile.getLastName());

                         bookedSchedules.add(bookedScheduleView);

                         schedulesDayView.setBookedSchedules(bookedSchedules);
                         if (!schedulesDayView.getBookedSchedules().isEmpty()){
                             schedulesDayViews.add(schedulesDayView);
                         }

             }

         if (schedulesDayViews.isEmpty()){
             throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS,MessageConstants.MSG_DATA_NOT_AVAILABLE,null);
         }
         Map<String,Object> response = new HashMap<>();
         response.put("schedules",schedulesDayViews);
         return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED,response);

    }

     
      private CronofyschedulePayload getschedulepayloaddata(User user, UserKloudlessSchedule userKloudlessSchedule) {
    	  CronofyschedulePayload cronofyschedulePayload = new CronofyschedulePayload();
          
          cronofyschedulePayload.setAccountId(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar().getUserKloudlessAccount().getAccountId());
          cronofyschedulePayload.setCalendarId(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar().getCalendarId());
                        
            SchedulePayloadcreator schedulePayloadcreator = new SchedulePayloadcreator();
                schedulePayloadcreator.setEmail(user.getEmail());
                cronofyschedulePayload.setCreator(schedulePayloadcreator);
                        
          SchedulePayloadorganizer schedulePayloadorganizer =new SchedulePayloadorganizer();
                schedulePayloadorganizer.setEmail(user.getEmail());
                cronofyschedulePayload.setOrganizer(schedulePayloadorganizer); 
                          
                cronofyschedulePayload.setCreated(userKloudlessSchedule.getUserKloudlessMeeting().getStartDateInUtc().toString());
                cronofyschedulePayload.setModified(userKloudlessSchedule.getUserKloudlessMeeting().getEndDateInUtc().toString());
                          
                 
              cronofyschedulePayload.setStart(userKloudlessSchedule.getScheduleStartTime());
              cronofyschedulePayload.setStartimezone(userKloudlessSchedule.getUserKloudlessMeeting().getTimeZone());

              cronofyschedulePayload.setEnd(userKloudlessSchedule.getScheduleEndTime());
              cronofyschedulePayload.setEndtimezone(userKloudlessSchedule.getUserKloudlessMeeting().getTimeZone());
                   
              cronofyschedulePayload.setName(userKloudlessSchedule.getPackageKloudlessMapping().getTitle());
                 
                 
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
                                	   if(userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getLandMark() != null){
                                		   SPCP_landmark.setValue(userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getLandMark());
                                    	}else{
                                    		SPCP_landmark.setValue("");                                    	 
                                     	} 
                                	}else{
                                		SPCP_landmark.setValue("");                                   	 
                                 	}
                                   SPCP_landmark.setPrivate(true);
                           
                                   payloadcustomproperties.add(SPCP_landmark);
                              
                     SchedulePayloadcustomproperties SPCP_sessionNameInPackage = new SchedulePayloadcustomproperties();        
                                 
                                    SPCP_sessionNameInPackage.setKey("sessionNameInPackage");
                                    SPCP_sessionNameInPackage.setValue(userKloudlessSchedule.getPackageKloudlessMapping().getUserKloudlessMeeting().getCalendarMeetingType().getMeetingType());
                                    SPCP_sessionNameInPackage.setPrivate(true);
                           
                                    payloadcustomproperties.add(SPCP_sessionNameInPackage);
                                
                      SchedulePayloadcustomproperties SPCP_isFitwiseEvent = new SchedulePayloadcustomproperties();        
                                 
                                    SPCP_isFitwiseEvent.setKey("isFitwiseEvent");
                                    SPCP_isFitwiseEvent.setValue("");
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



                               
                            
                    cronofyschedulePayload.setCustom_properties(payloadcustomproperties);
                    
                    return cronofyschedulePayload;
                   
      }
    
    
    /**
     * Getting schedules for a given time period
     * @param startTimeInMillis
     * @param endTimeInMillis
     * @return
     */
    public ResponseModel getSchedulesold(Long startTimeInMillis, Long endTimeInMillis) throws ParseException {

        User user = userComponents.getUser();

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

        List<UserKloudlessSchedule> userKloudlessScheduleList = userKloudlessScheduleRepository.findBySubscriptionPackageOwnerUserId(user.getUserId());
        if(userKloudlessScheduleList.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS,MessageConstants.MSG_DATA_NOT_AVAILABLE,null);
        }

        Date scheduleDate = fitwiseUtils.convertToUserTimeZone(startDate);
        Date endDateInUserTimeZone = fitwiseUtils.convertToUserTimeZone(endDate);
        TimeZone userTimeZone = TimeZone.getTimeZone(fitwiseUtils.getUserTimeZone());
        UserKloudlessAccount userKloudlessAccount = calendarService.getActiveAccount(user);
        UserKloudlessCalendar userKloudlessCalendar  = calendarService.getActiveCalendarFromKloudlessAccount(userKloudlessAccount);

          List<SchedulesDayView> schedulesDayViews = new ArrayList<>();
          
          
        while(fitwiseUtils.isSameDay(scheduleDate,endDateInUserTimeZone) || scheduleDate.before(endDateInUserTimeZone)){
             SchedulesDayView schedulesDayView = new SchedulesDayView();
            schedulesDayView.setBookedDate(fitwiseUtils.formatDate(scheduleDate));
            List<UserKloudlessSchedule> userKloudlessSchedules = new ArrayList<>();
            ZonedDateTime scheduleDateTime = scheduleDate.toInstant().atZone(userTimeZone.toZoneId())
                     .with(ChronoField.HOUR_OF_DAY, 0)
                    .with(ChronoField.MINUTE_OF_DAY, 0)
                    .with(ChronoField.SECOND_OF_DAY, 0);
            Date scheduleDateInUtc = Date.from(scheduleDateTime.withZoneSameInstant(ZoneId.systemDefault()).toInstant());
            Date endDateInUtc = Date.from(scheduleDateTime.with(LocalTime.MAX).withZoneSameInstant(ZoneId.systemDefault()).toInstant());
          
            List<BookedScheduleView> bookedSchedules = new ArrayList<>();
          //  userKloudlessSchedules = userKloudlessScheduleRepository.findBySubscriptionPackageOwnerUserIdAndBookingDateGreaterThanEqualAndBookingDateLessThanEqual(user.getUserId(),scheduleDateInUtc,endDateInUtc);
            userKloudlessSchedules = userKloudlessScheduleRepository.findAll();
            
            
            for(UserKloudlessSchedule userKloudlessSchedule : userKloudlessSchedules){
            
            	if(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar().getCalendarId().equals(userKloudlessCalendar.getCalendarId())){
                    
                
            	    String startTime = userKloudlessSchedule.getScheduleStartTime();
                    String endTime = userKloudlessSchedule.getScheduleEndTime();

                    Date ScheduleStartTimeInUtc =Date.from(OffsetDateTime.parse(startTime).toZonedDateTime()
                            .withZoneSameInstant(ZoneOffset.UTC).toInstant()) ;
                    Date scheduleEndTimeInUtc =Date.from(OffsetDateTime.parse(endTime).toZonedDateTime()
                            .withZoneSameInstant(ZoneOffset.UTC).toInstant()) ;

                    //Checking start time of schedule to compare with unavailability endTime
                    if((ScheduleStartTimeInUtc.before(endDate) && ScheduleStartTimeInUtc.after(startDate)) || (scheduleEndTimeInUtc.after(startDate) && scheduleEndTimeInUtc.before(endDate))
                            || (ScheduleStartTimeInUtc.before(startDate) && scheduleEndTimeInUtc.after(endDate))
                            || ((ScheduleStartTimeInUtc.compareTo(startDate) == 0) && scheduleEndTimeInUtc.compareTo(endDate) == 0)){
                        BookedScheduleView bookedScheduleView = new BookedScheduleView();
                        bookedScheduleView.setKloudlessScheduleId(userKloudlessSchedule.getUserKloudlessScheduleId());
                        bookedScheduleView.setKloudlessMetingId(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessMeetingId());
                        bookedScheduleView.setSessionTitle(userKloudlessSchedule.getUserKloudlessMeeting().getName());
                        if(userKloudlessSchedule.getPackageKloudlessMapping() != null){
                            bookedScheduleView.setSessionTitleInPackage(userKloudlessSchedule.getPackageKloudlessMapping().getTitle());
                        }
                        if(userKloudlessSchedule.getMeetingTypeId() != null && userKloudlessSchedule.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingTypeId() == CalendarConstants.SESSION_IN_PERSON_OR_VIRTUAL){
                            bookedScheduleView.setMeetingTypeId(userKloudlessSchedule.getMeetingTypeId().getMeetingTypeId());
                            bookedScheduleView.setMeetingType(userKloudlessSchedule.getMeetingTypeId().getMeetingType());
                        }else{
                            bookedScheduleView.setMeetingTypeId(userKloudlessSchedule.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingTypeId());
                            bookedScheduleView.setMeetingType(userKloudlessSchedule.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingType());
                        }
                        
                        
                        
                        CronofyschedulePayload cronofyschedulePayload = new CronofyschedulePayload();
                        
                        cronofyschedulePayload.setAccountId(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar().getUserKloudlessAccount().getAccountId());
                        cronofyschedulePayload.setCalendarId(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar().getCalendarId());
                                      
                          SchedulePayloadcreator schedulePayloadcreator = new SchedulePayloadcreator();
                              schedulePayloadcreator.setEmail(user.getEmail());
                              cronofyschedulePayload.setCreator(schedulePayloadcreator);
                                      
                        SchedulePayloadorganizer schedulePayloadorganizer =new SchedulePayloadorganizer();
                              schedulePayloadorganizer.setEmail(user.getEmail());
                              cronofyschedulePayload.setOrganizer(schedulePayloadorganizer); 
                                        
                              cronofyschedulePayload.setCreated(userKloudlessSchedule.getUserKloudlessMeeting().getStartDateInUtc().toString());
                              cronofyschedulePayload.setModified(userKloudlessSchedule.getUserKloudlessMeeting().getEndDateInUtc().toString());
                                        
                               
                            cronofyschedulePayload.setStart(userKloudlessSchedule.getScheduleStartTime());
                            cronofyschedulePayload.setStartimezone(userKloudlessSchedule.getUserKloudlessMeeting().getTimeZone());

                            cronofyschedulePayload.setEnd(userKloudlessSchedule.getScheduleEndTime());
                            cronofyschedulePayload.setEndtimezone(userKloudlessSchedule.getUserKloudlessMeeting().getTimeZone());
                                 
                            cronofyschedulePayload.setName(userKloudlessSchedule.getPackageKloudlessMapping().getTitle());
                               
                               
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
                                            		SPCP_state.setValue("'");                                    	 
                                             	}
                                               SPCP_state.setPrivate(true);
                                         
                                              payloadcustomproperties.add(SPCP_state);
                                            
                                   SchedulePayloadcustomproperties SPCP_landmark = new SchedulePayloadcustomproperties();        
                                              
                                                  SPCP_landmark.setKey("landmark");
                                                 if(userKloudlessSchedule.getPackageKloudlessMapping().getLocation() != null){
                                                	 if(userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getLandMark() != null){
                                                		 SPCP_landmark.setValue(userKloudlessSchedule.getPackageKloudlessMapping().getLocation().getLandMark());
                                                     }else{
                                                    	 SPCP_landmark.setValue("");
                                                     }
                                              	}else{
                                              		SPCP_landmark.setValue("");                                     	 
                                               	}
                                                 SPCP_landmark.setPrivate(true);
                                         
                                                 payloadcustomproperties.add(SPCP_landmark);
                                            
                                   SchedulePayloadcustomproperties SPCP_sessionNameInPackage = new SchedulePayloadcustomproperties();        
                                               
                                                  SPCP_sessionNameInPackage.setKey("sessionNameInPackage");
                                                  SPCP_sessionNameInPackage.setValue(userKloudlessSchedule.getPackageKloudlessMapping().getUserKloudlessMeeting().getCalendarMeetingType().getMeetingType());
                                                  SPCP_sessionNameInPackage.setPrivate(true);
                                         
                                                  payloadcustomproperties.add(SPCP_sessionNameInPackage);
                                              
                                    SchedulePayloadcustomproperties SPCP_isFitwiseEvent = new SchedulePayloadcustomproperties();        
                                               
                                                  SPCP_isFitwiseEvent.setKey("isFitwiseEvent");
                                                  SPCP_isFitwiseEvent.setValue("");
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




                                             
                                          
                                  cronofyschedulePayload.setCustom_properties(payloadcustomproperties);
                                  Gson gson = new Gson();
                                  bookedScheduleView.setSchedulePayload(gson.toJson(cronofyschedulePayload));
                        
                        
                       // bookedScheduleView.setSchedulePayload(userKloudlessSchedule.getSchedulePayload());
                       
                        
                        
                        
                        UserProfile memberProfile = userProfileRepository.findByUser(userKloudlessSchedule.getUser());
                        bookedScheduleView.setMemberName(memberProfile.getFirstName() + KeyConstants.KEY_SPACE + memberProfile.getLastName());

                        bookedSchedules.add(bookedScheduleView);
                       }
                  }


            }
            schedulesDayView.setBookedSchedules(bookedSchedules);
           
            if (!schedulesDayView.getBookedSchedules().isEmpty()){
                schedulesDayViews.add(schedulesDayView);
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(scheduleDate);
            calendar.add(Calendar.DATE,1);
            scheduleDate = calendar.getTime();
        }
        if (schedulesDayViews.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS,MessageConstants.MSG_DATA_NOT_AVAILABLE,null);
        }
        Map<String,Object> response = new HashMap<>();
        response.put("schedules",schedulesDayViews);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED,response);

    }

    public void deleteSchedulesFromInstructor(DeleteFitwiseSchedulesRequestModel requestModel){
        //Validating user input
          if (requestModel.getFitwiseScheduleIds() == null || requestModel.getFitwiseScheduleIds().isEmpty()){
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_SCHEDULE_ID_NULL, null);
          }
       
          List<Long> scheduleIds = requestModel.getFitwiseScheduleIds();
          deleteSchedulesFromInstructor(scheduleIds);
    }

        /**
         * Delete the list of schedules from instructor using schedule id's.
         *
         * @param   scheduleIds
         * @return
         */
    public void deleteSchedulesFromInstructor(List<Long> scheduleIds){
        User user = userComponents.getUser();

        if (!fitwiseUtils.isInstructor(user)){
            throw new ApplicationException(Constants.ERROR_STATUS, ValidationMessageConstants.MSG_USER_NOT_INSTRUCTOR, null);
        }

        //Delete the user kloudless schedule's and zoom meetings
        for (Long fitwiseScheduleId : scheduleIds){

            if (fitwiseScheduleId == 0){
                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_SCHEDULE_ID_NULL, null);
            }

            //Delete user kloudless schedule
            UserKloudlessSchedule userKloudlessSchedule = userKloudlessScheduleRepository.findByUserKloudlessScheduleId(fitwiseScheduleId);
            if (userKloudlessSchedule == null){
                throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_SCHEDULE_NOT_FOUND, null);
            }
            UserKloudlessMeeting userKloudlessMeeting = userKloudlessMeetingRepository.findByUserKloudlessMeetingId(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessMeetingId());
            if (userKloudlessMeeting == null){
                log.error("user kloudless meeting is not available");
            }
            if (!userKloudlessMeeting.getUser().getUserId().equals(user.getUserId())){
                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_SCHEDULE_INSTRUCTOR_NOT_MATCH, null);
            }
//            ResponseRaw responseRaw = kloudLessService.deleteKloudlessSchedule(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar(), userKloudlessSchedule.getScheduleId());
//
//            if (responseRaw == null || (responseRaw.getData().getStatusLine().getStatusCode() != 204 && responseRaw.getData().getStatusLine().getStatusCode() != 200)){
//                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_KLOUDLESS_SCHEDULE_DELETE_FAILED, null);
//            }
              RefreshAccessTokenResponse refreshAccessTokenResponse = null;
	        
		       refreshAccessTokenResponse = cronofyService.refreshAccessToken(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar().getUserKloudlessAccount().getRefreshToken());
		        
		        if (refreshAccessTokenResponse == null) {
			 		throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED,null);
			    }
	        
	       
	           if(!cronofyService.deleteevent(refreshAccessTokenResponse.getAccessToken(),userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar().getCalendarId(),userKloudlessSchedule.getUserKloudlessScheduleId().toString())){
	                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_KLOUDLESS_SCHEDULE_DELETE_FAILED, MessageConstants.ERROR);
	           }
	           	 User user_member = userKloudlessSchedule.getUser();
	             String member_Account_Token = "";
		         String member_Calendar_ID = "";
				 UserKloudlessAccount account_member = calendarService.getActiveAccount(user_member);
				 if(account_member != null){
					 UserKloudlessCalendar activeCalendar_member = calendarService.getActiveCalendarFromKloudlessAccount(account_member);
					 if(activeCalendar_member != null){
						 member_Account_Token = account_member.getRefreshToken();
				         member_Calendar_ID = activeCalendar_member.getCalendarId();
				     }
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

            //Delete user zoom meetings
            ZoomAccount zoomAccount = null;
            try {
                zoomAccount = zoomService.getZoomAccount(user);
            } catch (ApplicationException applicationException){
                log.error("get zoom account failed for instructor " + user.getUserId(), applicationException);
            }

            if (zoomAccount == null || userKloudlessSchedule.getZoomMeeting() == null){
                log.info("user do not have an authenticated zoom account, skipping zoom meeting delete");
            }

            if (zoomAccount != null && userKloudlessSchedule.getZoomMeeting() != null){
                ZoomMeeting zoomMeeting = userKloudlessSchedule.getZoomMeeting();
                try {
                    zoomService.deleteMeetingForUser(user, zoomMeeting.getMeetingId(), zoomMeeting.getOccurrenceId());
                } catch (ApplicationException applicationException){
                    log.error("zoom meeting deletion failed for schedule: " + userKloudlessSchedule.getUserKloudlessScheduleId(), applicationException);
                }
            }
        }
    }

    public SubscriptionPackageScheduleAvailability getScheduleInfo(Long fitwiseScheduleId) {
        ValidationUtils.throwException(fitwiseScheduleId == null || fitwiseScheduleId <= 0,
                CalendarConstants.CAL_ERR_SCHEDULE_ID_INVALID, Constants.BAD_REQUEST);

        User user = userComponents.getUser();
        ValidationUtils.throwException(!fitwiseUtils.isInstructor(user),
                ValidationMessageConstants.MSG_USER_NOT_INSTRUCTOR, Constants.BAD_REQUEST);

        Optional<UserKloudlessSchedule> optionalSchedule = userKloudlessScheduleRepository.findById(fitwiseScheduleId);
        ValidationUtils.throwException(!optionalSchedule.isPresent(),
                CalendarConstants.CAL_ERR_SCHEDULE_NOT_FOUND, Constants.BAD_REQUEST);

        UserKloudlessSchedule schedule = optionalSchedule.get();
        ValidationUtils.throwException(schedule.getSubscriptionPackage() == null,
                ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_NOT_FOUND, Constants.BAD_REQUEST);
        ValidationUtils.throwException(!user.getUserId().equals(schedule.getSubscriptionPackage().getOwner().getUserId()),
                CalendarConstants.CAL_ERR_SCHEDULE_INSTRUCTOR_NOT_MATCH, Constants.BAD_REQUEST);

        PackageSubscription packageSubscription = packageSubscriptionRepository
                .findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageIdOrderBySubscribedDateDesc(
                        schedule.getUser().getUserId(),
                        schedule.getSubscriptionPackage().getSubscriptionPackageId());
        ValidationUtils.throwException(packageSubscription == null,
                ValidationMessageConstants.MSG_MEMBER_NOT_SUBSCRIBED_FOR_PACKAGE, Constants.BAD_REQUEST);

        int durationInDays = Math.toIntExact(packageSubscription.getSubscriptionPlan().getDuration());
        Date subscribedDate = fitwiseUtils.convertToUserTimeZone(packageSubscription.getSubscribedDate());
        TimeZone UNAV_userTimeZone = TimeZone.getTimeZone(fitwiseUtils.getUserTimeZone());
        TimeZone userTimeZone = TimeZone.getTimeZone("Etc/UTC");
        int order = 1;
        Date scheduleDate = subscribedDate;
        SubscriptionPackageScheduleAvailability availability = new SubscriptionPackageScheduleAvailability();
        availability.setSubscribedDate(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(subscribedDate.toInstant().atZone(userTimeZone.toZoneId())));
        availability.setExpiryDate(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(subscribedDate.toInstant()
                .plus(durationInDays, ChronoUnit.DAYS).atZone(userTimeZone.toZoneId())));
        availability.setSubscriptionPackageId(schedule.getSubscriptionPackage().getSubscriptionPackageId());

        if (schedule.getPackageKloudlessMapping() != null) {
            availability.setPackageSessionMappingId(schedule.getPackageKloudlessMapping().getSessionMappingId());
        }

        List<Date> bookingRestrictedDates = new ArrayList<>();

        while (order <= durationInDays) {
            ZonedDateTime start = scheduleDate.toInstant().atZone(userTimeZone.toZoneId())
                    .with(ChronoField.HOUR_OF_DAY, 0)
                    .with(ChronoField.MINUTE_OF_DAY, 0)
                    .with(ChronoField.SECOND_OF_DAY, 0);
            ZonedDateTime UNAV_start = scheduleDate.toInstant().atZone(UNAV_userTimeZone.toZoneId())
                    .with(ChronoField.HOUR_OF_DAY, 0)
                    .with(ChronoField.MINUTE_OF_DAY, 0)
                    .with(ChronoField.SECOND_OF_DAY, 0);
            ZonedDateTime UNAV_end = scheduleDate.toInstant().atZone(UNAV_userTimeZone.toZoneId())
                    .with(ChronoField.HOUR_OF_DAY, 23)
                    .with(ChronoField.MINUTE_OF_HOUR, 30)
                    .with(ChronoField.SECOND_OF_MINUTE, 0);

            Date startTimeInUtc = Date.from(start.withZoneSameInstant(ZoneId.systemDefault()).toInstant());
            Date UNAV_startTimeInUtc = Date.from(UNAV_start.withZoneSameInstant(ZoneId.systemDefault()).toInstant());
            Date UNAV_endTimeInUtc = Date.from(UNAV_end.withZoneSameInstant(ZoneId.systemDefault()).toInstant());
            Date endTimeInUtc = Date.from(start.with(LocalTime.MAX).withZoneSameInstant(ZoneId.systemDefault()).toInstant());

            UserKloudlessAccount userKloudlessAccount = calendarService.getActiveAccount(user);
            UserKloudlessCalendar userKloudlessCalendar = calendarService.getActiveCalendarFromKloudlessAccount(userKloudlessAccount);
            boolean isInstructorUnavailableForAWholeday = false;
            if(userKloudlessCalendar != null){
                 isInstructorUnavailableForAWholeday = instructorUnavailabilityService.checkInstructorUnavailabilityForADay(UNAV_startTimeInUtc,UNAV_endTimeInUtc, userKloudlessCalendar);
            }
            boolean isBookingRestrictedForADay = false;

            if (isInstructorUnavailableForAWholeday) {
                isBookingRestrictedForADay = true;
            }

            int bookedSessionsForADay = 0;
            boolean isNewScheduleRestricted = false;
            boolean isRequestedPackageKloudlessMapping = false;

            if (!isBookingRestrictedForADay) {
                List<PackageKloudlessMapping> packageKloudlessMappings = packageKloudlessMappingRepository
                        .findBySubscriptionPackage(schedule.getSubscriptionPackage());
                for (PackageKloudlessMapping packageKloudlessMapping : packageKloudlessMappings) {
                    if (packageKloudlessMapping.getUserKloudlessMeeting().getMeetingWindow() == null) {
                        continue;
                    }

                    if (schedule.getPackageKloudlessMapping() != null) {
                        if (schedule.getPackageKloudlessMapping().getSessionMappingId()
                                .equals(packageKloudlessMapping.getSessionMappingId())) {
                            isRequestedPackageKloudlessMapping = true;
                        }
                    } else {
                        if (schedule.getUserKloudlessMeeting().getUserKloudlessMeetingId()
                                .equals(packageKloudlessMapping.getUserKloudlessMeeting().getUserKloudlessMeetingId())) {
                            isRequestedPackageKloudlessMapping = true;
                        }
                    }

                    boolean isBookingRestricted = false;

                    Set<String> availableDaysList = new HashSet<>();
                    boolean isAvailableDay = false;
                    JSONObject jsonObject = new JSONObject(packageKloudlessMapping.getUserKloudlessMeeting().getMeetingWindow());
                    JSONArray jsonArray = jsonObject.getJSONObject("availability").getJSONArray("available_times");
                    for (Object o : jsonArray) {
                        if (o instanceof JSONObject) {
                            String weekday = ((JSONObject) o).getJSONObject("recurring").getString("weekday");
                            String[] days = weekday.split(",");
                            for (String day : days) {
                                availableDaysList.add(day.trim());
                            }
                        }
                    }

                    DayOfWeek dayOfTheWeek = scheduleDate.toInstant().atZone(ZoneId.of(packageKloudlessMapping.getUserKloudlessMeeting().getTimeZone())).getDayOfWeek();
                    String scheduleday = dayOfTheWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).substring(0, 3).toUpperCase();
                    if (availableDaysList.contains(scheduleday)) {
                        isAvailableDay = true;
                    }

                    List<UserKloudlessSchedule> userKloudlessSchedules = userKloudlessScheduleRepository
                            .findByUserAndPackageKloudlessMappingSessionMappingIdAndBookingDateGreaterThanEqualAndBookingDateLessThanEqual(
                                    schedule.getUser(),
                                    packageKloudlessMapping.getSessionMappingId(),
                                    startTimeInUtc,
                                    endTimeInUtc);
                    if (userKloudlessSchedules.size() >= 1) {
                        isBookingRestricted = true;
                    }

                    int availableSessions = 0;
                    Date meetingStartDateInUtc = packageKloudlessMapping.getUserKloudlessMeeting().getStartDateInUtc();
                    Date meetingEndDateInUtc = packageKloudlessMapping.getUserKloudlessMeeting().getEndDateInUtc();
                    Date meetingStartDate = fitwiseUtils.convertToUserTimeZone(meetingStartDateInUtc);
                    Date meetingEndDate = fitwiseUtils.convertToUserTimeZone(meetingEndDateInUtc);

                    if (isAvailableDay
                            && (fitwiseUtils.isSameDay(scheduleDate, meetingStartDate)
                            || fitwiseUtils.isSameDay(scheduleDate, meetingEndDate)
                            || (scheduleDate.before(meetingEndDate) && scheduleDate.after(meetingStartDate)))) {
                        int bookedSessions = userKloudlessScheduleRepository
                                .countByUserAndPackageKloudlessMappingSessionMappingId(
                                        schedule.getUser(),
                                        packageKloudlessMapping.getSessionMappingId());
                        int totalSessions = packageKloudlessMapping.getTotalSessionCount();
                        availableSessions = totalSessions - bookedSessions;
                        if (availableSessions <= 0) {
                            isBookingRestricted = true;
                        }
                    } else {
                        isBookingRestricted = true;
                    }

                    if (isBookingRestricted) {
                        bookedSessionsForADay++;
                    }

                    if (isRequestedPackageKloudlessMapping && isBookingRestricted) {
                        isNewScheduleRestricted = true;
                    }
                }

                if (bookedSessionsForADay == packageKloudlessMappings.size()) {
                    isBookingRestrictedForADay = true;
                }
            }


            if (isBookingRestrictedForADay || isNewScheduleRestricted) {
                bookingRestrictedDates.add(scheduleDate);
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(scheduleDate);
            cal.add(Calendar.DATE, 1);
            scheduleDate = cal.getTime();
            order++;
        }

        List<Date> restrictedDates = new ArrayList<>();
        ZonedDateTime today = ZonedDateTime.now().withZoneSameInstant(userTimeZone.toZoneId())
                .with(ChronoField.HOUR_OF_DAY, 0)
                .with(ChronoField.MINUTE_OF_DAY, 0)
                .with(ChronoField.SECOND_OF_DAY, 0);
        ZonedDateTime bookedDate = schedule.getBookingDate().toInstant().atZone(userTimeZone.toZoneId());

        for (Date blockedDate : bookingRestrictedDates) {
            ZonedDateTime blockedDateStartOfDay = blockedDate.toInstant().atZone(userTimeZone.toZoneId())
                    .with(ChronoField.HOUR_OF_DAY, 0)
                    .with(ChronoField.MINUTE_OF_DAY, 0)
                    .with(ChronoField.SECOND_OF_DAY, 0);
            if (blockedDateStartOfDay.toInstant().compareTo(bookedDate.toInstant()) == 0) {
                continue;
            }
            if (blockedDateStartOfDay.toInstant().compareTo(today.toInstant()) > 0) {
                restrictedDates.add(blockedDate);
            }
        }

        List<String> dates = new ArrayList<>();
        for (Date date : restrictedDates) {
            ZonedDateTime startOfDay = date.toInstant().atZone(userTimeZone.toZoneId())
                    .with(ChronoField.HOUR_OF_DAY, 0)
                    .with(ChronoField.MINUTE_OF_DAY, 0)
                    .with(ChronoField.SECOND_OF_DAY, 0);
            dates.add(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(startOfDay));
        }
        availability.setRestrictedDates(dates);

        return availability;
    }

    public List<Long> getInstructorSchedules(Long startTimeInMillis, Long endTimeInMillis){

        User user = userComponents.getUser();

        if(startTimeInMillis == null){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_START_TIME_NULL, MessageConstants.ERROR);
        }
        if(endTimeInMillis == null){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_END_TIME_NULL, MessageConstants.ERROR);
        }
        
         Date startDate = new Date(startTimeInMillis);
    	
	     Date endDate = new Date(endTimeInMillis);
	     
		     Date scheduleDate = fitwiseUtils.convertToUserTimeZone(startDate);
	 	   	 Calendar calendar = new GregorianCalendar();
	 	     calendar.setTime(scheduleDate);
	 	     calendar.set(Calendar.HOUR_OF_DAY, 18);
	 	     calendar.set(Calendar.MINUTE, 30);
	 	     calendar.set(Calendar.SECOND, 0);
	 	     calendar.set(Calendar.MILLISECOND, 0);
		     Date scheduleDateInUserTimezone = calendar.getTime();
	     
	     	Date scheduleEndDate = fitwiseUtils.convertToUserTimeZone(endDate);
	 	   	Calendar ecalendar = new GregorianCalendar();
	 	   	ecalendar.setTime(scheduleEndDate);
	 	   	ecalendar.set(Calendar.HOUR_OF_DAY, 18);
	 	   	ecalendar.set(Calendar.MINUTE, 30);
	 	   	ecalendar.set(Calendar.SECOND, 0);
	 	   	ecalendar.set(Calendar.MILLISECOND, 0);
	    	Date scheduleEndDateInUserTimezone = ecalendar.getTime();
	     
	     	List<Long> scheduleIdList = new ArrayList<>();
            List<UserKloudlessSchedule> userKloudlessSchedules_All = new ArrayList<>();
            List<UserKloudlessSchedule> userKloudlessSchedules = new ArrayList<>();
          
            userKloudlessSchedules_All = userKloudlessScheduleRepository.findBySubscriptionPackageOwnerUserIdAndBookingDateGreaterThanEqualAndBookingDateLessThanEqual(user.getUserId(),scheduleDateInUserTimezone,scheduleEndDateInUserTimezone);
           
           for(UserKloudlessSchedule userKloudlessSchedule_Item : userKloudlessSchedules_All)
           {
           	String startTime = "";
           	if(userKloudlessSchedule_Item.getScheduleStartTime() != null)
           	{
           		startTime = userKloudlessSchedule_Item.getScheduleStartTime();
           	}
           			
               String endTime = "";
               if(userKloudlessSchedule_Item.getScheduleEndTime() != null)
           	{
               	endTime = userKloudlessSchedule_Item.getScheduleEndTime();
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

                   Date scheduleEndTimeInUtc = null;
	     			    try {
	     			    	scheduleEndTimeInUtc = outputsdf.parse(endTime);
	     			     } catch (ParseException e) {
	     				    // TODO Auto-generated catch block
	     				    e.printStackTrace();
	     		    	}

                   if((startDate.before(ScheduleStartTimeInUtc) && endDate.after(ScheduleStartTimeInUtc)) 
                  	|| (startDate.before(scheduleEndTimeInUtc) && endDate.after(scheduleEndTimeInUtc))
                      || (startDate.before(ScheduleStartTimeInUtc) && endDate.after(scheduleEndTimeInUtc))
                      || (startDate.after(ScheduleStartTimeInUtc) && endDate.before(scheduleEndTimeInUtc))){
                   		userKloudlessSchedules.add(userKloudlessSchedule_Item);
                 		  }
               	
               }
           }
            
            
            for(UserKloudlessSchedule userKloudlessSchedule : userKloudlessSchedules){
            	
            	scheduleIdList.add(userKloudlessSchedule.getUserKloudlessScheduleId());

                }

        return scheduleIdList;
    }
}
