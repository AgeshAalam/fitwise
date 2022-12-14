package com.fitwise.rest;


import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.model.InstructorUnavailabilityRequestModel;
import com.fitwise.service.InstructorUnavailabilityService;
import com.fitwise.view.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Date;

/**
 * The Class InstructorUnavailabilityController.
 */
@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/instructor")
public class InstructorUnavailabilityController {

    @Autowired
    private InstructorUnavailabilityService instructorUnavailabilityService;

    @PostMapping(value = "/unavailability")
    public ResponseModel saveInstructorUnavailability(@RequestBody InstructorUnavailabilityRequestModel requestModel) throws ParseException {
        return instructorUnavailabilityService.saveInstructorUnavailability(requestModel);
    }

    @GetMapping(value = "/unavailability")
    public ResponseModel getInstructorUnavailabilities(@RequestParam Long startDate, @RequestParam Long endDate) throws ParseException {
        return instructorUnavailabilityService.getInstructorUnavailability(startDate, endDate);
    }

    @DeleteMapping(value = "/unavailability")
    public ResponseModel deleteInstructorUnavailability(@RequestParam Long instructorUnavailabilityId){
        return instructorUnavailabilityService.deleteInstructorUnavailability(instructorUnavailabilityId);
    }

    @GetMapping(value = "/unavailableTimes")
    public ResponseModel getUnavailableTimes(){
        return instructorUnavailabilityService.getUnavailableTimeList();
    }

    @GetMapping(value = "/populateCalendar")
    public ResponseModel populateCalendarData(){
       return instructorUnavailabilityService.populateCalendarData();
    }
}
