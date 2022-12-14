package com.fitwise.rest.subscription;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.service.SubscriptionService;
import com.fitwise.service.instructor.InstructorSubscriptionService;
import com.fitwise.view.InstructorSubscriptionView;
import com.fitwise.view.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/instructor")
public class InstructorSubscriptionController {

    @Autowired
    SubscriptionService subscriptionService;

    @Autowired
    InstructorSubscriptionService instructorSubscriptionService;

    @GetMapping("/getLapsedClientsOfAnInstructor")
    public ResponseModel getLapseClientsOfAnInstructor(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam Optional<String> searchName,@RequestParam Optional<String> type) throws ApplicationException, ParseException {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_LAPSE_CLIENTS_FETCHED);
        response.setPayload(instructorSubscriptionService.getLapseClientsOfAnInstructor(pageNo, pageSize, searchName,type));

        return response;
    }

    @GetMapping("/getSubscribedClientsOfAnInstructor")
    public ResponseModel getSubscribedClientsOfAnInstructor(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam Optional<String> searchName,@RequestParam Optional<String> type) throws ApplicationException {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_SUBSCRIBED_CLIENTS_FETCHED);
        response.setPayload(instructorSubscriptionService.getSubscribedClientsOfAnInstructor(pageNo, pageSize, searchName,type));

        return response;
    }

    @GetMapping("/getTrialClientsOfAnInstructor")
    public ResponseModel getTrialClientsOfAnInstructor(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam Optional<String> searchName) throws ApplicationException, ParseException {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_TRIAL_CLIENTS_FETCHED);
        response.setPayload(instructorSubscriptionService.getTrialClientsOfAnInstructor(pageNo, pageSize, searchName));
        return response;
    }

    @PostMapping("/subscribeInstructor")
    public ResponseModel subscribeToInstructor(@RequestBody InstructorSubscriptionView subscriptionView) throws ApplicationException {
        return subscriptionService.subscribeInstructor(subscriptionView);
    }

    /**
     * API to get clients of an instructor
     * @param search
     * @return
     * @throws ApplicationException
     */
    @GetMapping("/clients")
    public ResponseModel getClients(@RequestParam Optional<String> search) throws ApplicationException {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_SUBSCRIBED_CLIENTS_FETCHED);
        response.setPayload(instructorSubscriptionService.getClients(search));

        return response;
    }

}
