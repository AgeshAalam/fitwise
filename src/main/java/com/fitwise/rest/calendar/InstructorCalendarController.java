package com.fitwise.rest.calendar;

import com.fitwise.constants.CalendarConstants;
import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.model.DeleteFitwiseSchedulesRequestModel;
import com.fitwise.service.calendar.CalendarService;
import com.fitwise.service.calendar.InstructorCalendarService;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.calendar.KloudlessCalendarModel;
import com.fitwise.view.calendar.KloudlessMeetingModel;
import com.fitwise.view.calendar.KloudlessScheduleModel;
import com.fitwise.view.calendar.KloudlessTokenModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Date;
import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/cal/instructor")
public class InstructorCalendarController {

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private InstructorCalendarService instructorCalendarService;

    @PostMapping(value = "/token")
    public @ResponseBody ResponseModel validateAndSaveToken(@RequestBody final KloudlessTokenModel token){
        calendarService.validateAndSaveToken(token);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_TOKEN_SAVED,null);
    }

    @GetMapping(value = "/token")
    public @ResponseBody ResponseModel getToken(){
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_TOKEN_AVAILABLE,calendarService.getActiveAccount());
    }

    @PostMapping(value = "/calendar")
    public @ResponseBody ResponseModel validateAndSaveToken(@RequestBody final KloudlessCalendarModel calendarModel){
        calendarService.validateAndSaveCalendar(calendarModel);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_ID_SAVED,null);
    }

    @GetMapping(value = "/calendars")
    public @ResponseBody ResponseModel getMyCalendars(){
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_LIST_RETRIEVED,calendarService.getMyCalendars());
    }

    @PostMapping(value = "/fitwiseMeetingId")
    public @ResponseBody ResponseModel createFitwiseMeetingId(@RequestBody final KloudlessMeetingModel meetingModel){
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_MEETING_SAVED,calendarService.createFitwiseMeeting(meetingModel));
    }

    @PutMapping(value = "/meeting")
    public @ResponseBody ResponseModel updateMeeting(@RequestBody final KloudlessMeetingModel meetingModel) {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_MEETING_SAVED, calendarService.updateMeeting(meetingModel));
    }

    @GetMapping(value = "/meetings")
    public @ResponseBody ResponseModel getMyMeetings(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam final Optional<Long> meetingType){
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_MEETING_RETRIEVED,calendarService.getMeetings(pageNo, pageSize, meetingType));
    }

    @DeleteMapping(value = "/meeting")
    public @ResponseBody ResponseModel deleteMeeting(@RequestParam final Long fitwiseMeetingId) {
        calendarService.deleteMeeting(fitwiseMeetingId);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_MEETING_DELETED, null);
    }

    @PutMapping(value = "/schedule")
    public ResponseModel updateSchedule(final @RequestBody KloudlessScheduleModel scheduleModel) {
        return new ResponseModel(Constants.SUCCESS_STATUS, CalendarConstants.CAL_SCS_SCHEDULE_UPDATED, calendarService.updateScheduleFromInstructor(scheduleModel));
    }

    @DeleteMapping(value = "/schedule")
    public ResponseModel deleteSchedule(final @RequestParam Long fitwiseScheduleId) throws ParseException {
          calendarService.deleteScheduleFromInstructor(fitwiseScheduleId);
          return new ResponseModel(Constants.SUCCESS_STATUS, CalendarConstants.CAL_SCS_SCHEDULE_DELETED, null);
    }

    @GetMapping(value = "/allCalendars")
    public @ResponseBody ResponseModel getMyAllCalendars(){
           return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_ACCOUNT_LIST_RETRIEVED,calendarService.getMyAllCalendars());
    }

    @PutMapping(value = "/calendar/changeActiveStatus")
    public ResponseModel changeCalendarActiveStatus(final @RequestParam String userKloudlessAccountId, final @RequestParam boolean setActive){
           calendarService.changeCalendarActiveStatus(userKloudlessAccountId, setActive);
           return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_ACTIVE_STATUS_CHANGED, null);
    }


    @DeleteMapping(value = "/calendar")
    public ResponseModel deleteInstructorCalendar(final @RequestParam String userKloudlessAccountId) {
           calendarService.deleteInstructorCalendarAccount(userKloudlessAccountId);
           return new ResponseModel(Constants.SUCCESS_STATUS, CalendarConstants.MSG_CAL_DELETE_CALENDAR_ACCOUNT, null);
    }
   
     @GetMapping("/schedules")
     public ResponseModel getSchedules(@RequestParam Long startTime,@RequestParam Long endTime) throws ParseException {
           return instructorCalendarService.getSchedules(startTime,endTime);
     }
    


    @DeleteMapping(value = "/schedules")
    public ResponseModel deleteSchedules(@RequestBody DeleteFitwiseSchedulesRequestModel fitwiseSchedules) {
           instructorCalendarService.deleteSchedulesFromInstructor(fitwiseSchedules);
          return new ResponseModel(Constants.SUCCESS_STATUS, CalendarConstants.CAL_SCS_SCHEDULE_LIST_DELETED, null);

    }

    @GetMapping(value = "/schedule-availability")
    public ResponseModel getScheduleInfo(final Long fitwiseScheduleId) {
        return new ResponseModel(Constants.SUCCESS_STATUS, "", instructorCalendarService.getScheduleInfo(fitwiseScheduleId));
    }

}
