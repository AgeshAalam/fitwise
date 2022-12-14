package com.fitwise.rest.calendar;

import com.fitwise.constants.CalendarConstants;
import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.model.MemberCalendarFilterModel;
import com.fitwise.service.calendar.CalendarService;
import com.fitwise.service.member.MemberPackageService;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.calendar.FindAvailabilityRequest;
import com.fitwise.view.calendar.KloudlessScheduleModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Date;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/cal/member")
public class MemberCalendarController {

    @Autowired
    private CalendarService calendarService;

    @PostMapping(value = "/schedule")
    public ResponseModel scheduleEvent(final @RequestBody KloudlessScheduleModel scheduleModel) throws ParseException {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_SCH_SAVED, calendarService.createOrUpdateUserSchedule(scheduleModel));
    }

    @DeleteMapping(value = "/schedule")
    public ResponseModel deleteSchedule(final @RequestParam Long fitwiseScheduleId) throws ParseException {
        calendarService.deleteUserSchedule(fitwiseScheduleId);
        return new ResponseModel(Constants.SUCCESS_STATUS, CalendarConstants.CAL_SCS_SCHEDULE_DELETED, null);
    }

    @GetMapping(value = "/schedules")
    public ResponseModel getMySchedules() throws ParseException {
         return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_SCH_RETRIEVED, calendarService.getMySchedules());
    }

    @PostMapping("/schedule/availability")
    public ResponseModel findAvailability(final @RequestBody FindAvailabilityRequest availabilityRequest) {
         return new ResponseModel(Constants.SUCCESS_STATUS, CalendarConstants.CAL_SCS_AVAILABILITY, calendarService.findAvailability(availabilityRequest));
    }

    @PostMapping(value = "/exp-schedule")
    public ResponseModel expScheduleEvent(final @RequestBody KloudlessScheduleModel scheduleModel) {
         return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_SCH_SAVED, calendarService.createOrUpdateSchedule(scheduleModel));
    }

    @DeleteMapping(value = "/exp-schedule")
    public ResponseModel expDeleteSchedule(final @RequestParam Long fitwiseScheduleId) throws ParseException {
         calendarService.deleteSchedule(fitwiseScheduleId);
         return new ResponseModel(Constants.SUCCESS_STATUS, CalendarConstants.CAL_SCS_SCHEDULE_DELETED, null);
    }


    @PutMapping
    public ResponseModel getMySchedules(@RequestParam Date startDate, @RequestParam  Date endDate, @RequestBody MemberCalendarFilterModel memberCalendarFilterModel) throws ParseException {
          return calendarService.getSchedules(startDate,endDate,memberCalendarFilterModel);
    }

}
