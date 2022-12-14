package com.fitwise.rest.calendar;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.service.calendar.CalendarService;
import com.fitwise.view.ResponseModel;
import com.fitwise.response.kloudless.WebhookNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/cal")
public class CalendarController {

    @Autowired
    private CalendarService calendarService;

    @GetMapping(value = "/meeting/type/all")
    public ResponseModel getMeetingsTypes(){
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_MEETING_AVAILABLE, calendarService.getMeetingTypes());
    }

    @GetMapping(value = "/meetings")
    public ResponseModel getUserMeetings(@RequestParam final int pageNo, @RequestParam final int pageSize, final @RequestParam String userId){
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CAL_MEETING_RETRIEVED, calendarService.getUserMeetings(pageNo, pageSize, userId));
    }

    @PostMapping(value = "/webhook")
    public String processWebhook(@RequestHeader("X-Kloudless-Signature") String signature, @RequestBody String payload) {
        return calendarService.processWebhook(payload, signature);
    }

}
