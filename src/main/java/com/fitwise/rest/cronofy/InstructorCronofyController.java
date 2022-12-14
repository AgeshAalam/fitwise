package com.fitwise.rest.cronofy;

import java.text.ParseException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fitwise.constants.Constants;
import com.fitwise.constants.CalendarConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.service.cronofy.CronofyCalenderService;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.calendar.KloudlessScheduleModel;
import com.fitwise.view.cronofy.CronofyCalendarModel;
import com.fitwise.view.cronofy.CronofyMeetingModel;
import com.fitwise.view.cronofy.CronofyTokenModel;
import com.fitwise.view.cronofy.DefaultCalendarModel;
import com.fitwise.view.cronofy.InstructorSchedulePayload;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/cronofy/instructor")
public class InstructorCronofyController {
	
	@Autowired
	private CronofyCalenderService cronofycalendarservice;
	
	
	
	   
	    @PostMapping(value = "/token")
        public @ResponseBody ResponseModel validateAndSaveToken(final @RequestParam String  code,final @RequestParam String  redirectUri) {
	           return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_TOKEN_SAVED, cronofycalendarservice.validateAndSaveToken(code,redirectUri));
        }
	 
	   
	    @GetMapping(value = "/token")
        public @ResponseBody ResponseModel getToken(){
               return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_TOKEN_AVAILABLE,cronofycalendarservice.getActiveAccount());
        } 
      
	  
	   @PostMapping(value = "/calendar")
       public @ResponseBody ResponseModel validateAndSaveToken(final @RequestParam String  profileId,final @RequestParam String profilename,final @RequestParam String accessToken,final @RequestParam String refreshToken){
		      cronofycalendarservice.validateAndSaveCalendar(profileId,profilename,accessToken,refreshToken);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_ID_SAVED,null);
       }
	   
	   @DeleteMapping(value = "/calendar")
	    public ResponseModel deleteInstructorCalendar(final @RequestParam String userKloudlessAccountId) {
	    	  cronofycalendarservice.deleteInstructorCalendarAccount(userKloudlessAccountId);
	          return new ResponseModel(Constants.SUCCESS_STATUS, CalendarConstants.MSG_CAL_DELETE_CALENDAR_ACCOUNT, null);
	     }
	  
	   @PostMapping(value = "/defaultcalendar")
	   public @ResponseBody ResponseModel validateAndSaveToken(@RequestBody final DefaultCalendarModel calendarModel){
	    	   cronofycalendarservice.validateAndSaveDefaultCalendar(calendarModel);
	           return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_ID_SAVED,null);
	   }
      
	  
	   @PutMapping(value = "/calendar/changeActiveStatus")
       public ResponseModel changeCalendarActiveStatus(final @RequestParam String userCronofyAccountId, final @RequestParam boolean setActive){
         	 cronofycalendarservice.changeCalendarActiveStatus(userCronofyAccountId, setActive);
             return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_ACTIVE_STATUS_CHANGED, null);
        }
   
       
	   @GetMapping(value = "/allCalendars")
       public @ResponseBody ResponseModel getMyAllCalendars(){
             return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_ACCOUNT_LIST_RETRIEVED,cronofycalendarservice.getMyAllCalendars());
       }	
      
       
       @PostMapping(value = "/fitwiseMeetingId")
	   public @ResponseBody ResponseModel createFitwiseMeetingId(@RequestBody final CronofyMeetingModel meetingModel){
	         return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_MEETING_SAVED,cronofycalendarservice.createFitwiseMeeting(meetingModel));
	   }
	   
	  
       @PutMapping(value = "/meeting")
	   public @ResponseBody ResponseModel updateMeeting(@RequestBody final CronofyMeetingModel meetingModel) {
	         return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_MEETING_SAVED, cronofycalendarservice.updateMeeting(meetingModel));
	   }
	     
	  
	   @DeleteMapping(value = "/meeting")
	   public @ResponseBody ResponseModel deleteMeeting(@RequestParam final Long fitwiseMeetingId,@RequestParam final Long availabilityRuleId) {
	    	 cronofycalendarservice.deleteMeeting(fitwiseMeetingId,availabilityRuleId);
	         return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_MEETING_DELETED, null);
	   }
	   
	   @GetMapping(value = "/meetings")
	   public @ResponseBody ResponseModel getMyMeetings(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam final Optional<Long> meetingType){
	         return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_MEETING_RETRIEVED,cronofycalendarservice.getMeetings(pageNo, pageSize, meetingType));
	   }
	   
	   @GetMapping(value = "/zones")
	   public @ResponseBody ResponseModel getZones(){
	         return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_ZONE_RETRIEVED,cronofycalendarservice.getZones());
	   }
	   
	   @PutMapping(value = "/schedule")
       public ResponseModel updateSchedule(final @RequestBody KloudlessScheduleModel scheduleModel) {
             return new ResponseModel(Constants.SUCCESS_STATUS, CalendarConstants.CAL_SCS_SCHEDULE_UPDATED, cronofycalendarservice.updateScheduleFromInstructor(scheduleModel));
       }
	   
	   @PostMapping(value = "/saveschedule")
       public ResponseModel expScheduleEvent(@RequestParam final String token) {
               cronofycalendarservice.saveSchedule(token);
               return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_SCH_SAVED, null);
        }
	     
	    @GetMapping("/schedulescronofy")
	    public ResponseModel getSchedulescronofy(@RequestParam Long startTime,@RequestParam Long endTime) throws ParseException {
	           return cronofycalendarservice.getSchedulescronofy(startTime,endTime);
	    }
	   
}
