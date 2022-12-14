package com.fitwise.rest.cronofy;

import java.text.ParseException;

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

import com.fitwise.constants.CalendarConstants;
import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.service.cronofy.CronofyCalenderService;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.calendar.KloudlessScheduleModel;
import com.fitwise.view.cronofy.DefaultCalendarModel;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/Cronofy/member")
public class MemberCronofyController {
     
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
    public ResponseModel deleteMemberCalendar(final @RequestParam String userKloudlessAccountId) {
    	  cronofycalendarservice.deleteMemberCalendarAccount(userKloudlessAccountId);
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
	
	@PostMapping(value = "/scheduleinstance")
    public ResponseModel scheduleEvent(final @RequestBody KloudlessScheduleModel scheduleModel) throws ParseException {
       return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_SCH_SAVED, cronofycalendarservice.CreateScheduleInstance(scheduleModel));
    }
	
	@PostMapping(value = "/saveschedule")
    public ResponseModel expScheduleEvent(@RequestParam final String token) {
	       cronofycalendarservice.saveSchedule(token);
	       return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_SCH_SAVED, null);
    }
	
	@DeleteMapping(value = "/schedule")
    public ResponseModel expDeleteSchedule(final @RequestParam Long fitwiseScheduleId) throws ParseException {
 	       cronofycalendarservice.deleteSchedule(fitwiseScheduleId);
       return new ResponseModel(Constants.SUCCESS_STATUS, CalendarConstants.CAL_SCS_SCHEDULE_DELETED, null);
    }
	
}
