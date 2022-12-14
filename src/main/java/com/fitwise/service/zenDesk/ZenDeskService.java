package com.fitwise.service.zenDesk;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.ZenDeskTicket;
import com.fitwise.exception.ApplicationException;
import com.fitwise.properties.ZenDeskProperties;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.repository.ZenDeskTicketRepository;
import com.fitwise.request.zenDesk.ZenDeskClientView;
import com.fitwise.request.zenDesk.ZenDeskMailModel;
import com.fitwise.request.zenDesk.ZenDeskRequest;
import com.fitwise.request.zenDesk.ZenDeskTicketRequest;
import com.fitwise.request.zenDesk.ZenDeskUserData;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.ZenDeskWebHookResponseView;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ZenDeskService {

    @Autowired
    private ZenDeskTicketRepository zenDeskTicketRepository;

    @Autowired
    UserComponents userComponents;

    @Autowired
    UserProfileRepository userProfileRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ZenDeskProperties zenDeskProperties;

    public ResponseModel zenDeskTicketWebHook(ZenDeskWebHookResponseView zenDeskResponse) {
        log.info("Zendesk Ticket webhook starts");
        long start = new Date().getTime();
        ZenDeskTicket zenDeskTicket = new ZenDeskTicket();
        zenDeskTicket.setTicketId(zenDeskResponse.getId());
        zenDeskTicket.setTicketStatus(zenDeskResponse.getTicketStatus());
        zenDeskTicket.setUserEmail(zenDeskResponse.getRequesterEmail());
        zenDeskTicket.setUserPhoneNumber(zenDeskResponse.getUserPhoneNumber());

        zenDeskTicketRepository.save(zenDeskTicket);
        ResponseModel responseModel = new ResponseModel();
        log.info("Response from ZenDesk Trigger ===========> " + zenDeskResponse);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        log.info("Zendesk ticket webhook : Total time taken in millis : "+(new Date().getTime()-start));
        log.info("Zendesk Ticket webhook ends");
        return responseModel;
    }


    /**
     * Creating a zen desk ticket through support link on email
     *
     * @param zenDeskMailModel
     * @return
     */
    public ResponseModel createZenDeskTicketFromEmail(ZenDeskMailModel zenDeskMailModel) {
        if (ValidationUtils.isEmptyString(zenDeskMailModel.getEmail())) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EMAIL_EMPTY, Constants.RESPONSE_INVALID_DATA);
        }
        User user = userRepository.findByEmail(zenDeskMailModel.getEmail());
        if (user == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EMAIL_ERROR, Constants.RESPONSE_INVALID_DATA);
        }
        ZenDeskClientView zenDeskClientView = new ZenDeskClientView();
        zenDeskClientView.setBody(zenDeskMailModel.getBody());
        zenDeskClientView.setSubject(zenDeskMailModel.getSubject());
        return createZenDeskTicket(zenDeskClientView, user);
    }

    /**
     * Method used to create ticket in ZenDesk ticket using Email
     *
     * @param zenDeskClientView
     * @return
     */
    public ResponseModel createZenDeskTicket(ZenDeskClientView zenDeskClientView) {
        log.info("Create zen desk ticket starts.");
        long apiStartTimeMillis = new Date().getTime();
        User user = userRepository.findByEmail(userComponents.getUser().getEmail());
        log.info("Query to get user : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        ResponseModel responseModel = createZenDeskTicket(zenDeskClientView, user);
        log.info("Create zen desk ticker : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Create zen desk ticket ends.");
        return responseModel;
    }


    private ResponseModel createZenDeskTicket(ZenDeskClientView zenDeskClientView, User user) {
        log.info("Create zen desk ticket - Zen desk service method starts.");
        long apiStartTimeMillis = new Date().getTime();

        ZenDeskRequest zenDeskRequest = new ZenDeskRequest();

        UserProfile userProfile = userProfileRepository.findByUser(user);
        log.info("Query to get user profile : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();

        ZenDeskTicketRequest zenDeskTicketCreationRequest = new ZenDeskTicketRequest();
        zenDeskTicketCreationRequest.setSubject(zenDeskClientView.getSubject());

        Map<String, String> zenDeskMap = new HashMap<>();
        zenDeskMap.put(KeyConstants.KEY_BODY, zenDeskClientView.getBody());
        zenDeskTicketCreationRequest.setComment(zenDeskMap);

        ZenDeskUserData zenDeskUserData = new ZenDeskUserData();
        zenDeskUserData.setEmail(user.getEmail());
        zenDeskUserData.setName(userProfile.getFirstName() + " " + userProfile.getLastName());
        zenDeskTicketCreationRequest.setRequester(zenDeskUserData);

        zenDeskRequest.setTicket(zenDeskTicketCreationRequest);

        String apiKey = zenDeskProperties.getApiKey();

        String plainCreds = "techteam@fitwise.pro/token:" + apiKey;
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Basic " + base64Creds);

        HttpEntity<?> request = new HttpEntity<>(zenDeskRequest, headers);
        String domainUrl = zenDeskProperties.getDomainUrl();
        String url = domainUrl + "api/v2/tickets.json";
        log.info("Create url and request for zen desk api call : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        ResponseEntity<?> response = new RestTemplate().postForEntity(url, request, String.class);
        System.out.println(response.getBody());
        log.info("Zen desk ticket creation api completed : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        JSONObject jsonObj = new JSONObject(response.getBody().toString());
        JSONObject ticket = jsonObj.getJSONObject("ticket");

        Map<String, String> createdTicketData = null;

        if (ticket != null) {
            int ticketId = (int) ticket.get("id");
            if (ticketId == 0) {
                throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_CREATING_TICKET, MessageConstants.ERROR);
            }
            String ticketSubject = (String) ticket.get("subject");
            String ticketBody = (String) ticket.get(StringConstants.JSON_PROPERTY_KEY_DESCRIPTION);
            String ticketStatus = (String) ticket.get("status");

            createdTicketData = new HashMap<>();
            createdTicketData.put(KeyConstants.KEY_TICKET_ID, String.valueOf(ticketId));
            createdTicketData.put(KeyConstants.KEY_TICKET_SUBJECT, ticketSubject);
            createdTicketData.put(KeyConstants.KEY_TICKET_BODY, ticketBody);
            createdTicketData.put(KeyConstants.KEY_TICKET_STATUS, ticketStatus);
            log.info("Created ticket date response construction : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        } else {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_CREATING_TICKET, MessageConstants.ERROR);
        }
        log.info("Method total execution duration : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        log.info("Create zen desk ticket ends.");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_TICKET_CREATED, createdTicketData);
    }
}
