package com.fitwise.rest.messaging;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.response.messaging.ChatConversationView;
import com.fitwise.service.messaging.MessagingService;
import com.fitwise.view.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Optional;

/*
 * Created by Vignesh G on 03/04/20
 */

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/messaging")
public class MessagingController {

    private static final String CONVERSATION_ID_KEY = "ConversationId";

    @Autowired
    MessagingService messagingService;

    @GetMapping("/getInboxConversations")
    public ResponseModel getInboxConversations(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam Optional<String> search) throws ApplicationException {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(messagingService.getInboxConversations(pageNo, pageSize, search));

        return response;
    }

    @GetMapping("/getConversationMessages")
    public ResponseModel getConversationMessages(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam Long conversationId) throws ApplicationException {
        ChatConversationView chatConversationView = messagingService.getConversationMessages(pageNo, pageSize, conversationId);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(chatConversationView);

        return response;
    }

    @PostMapping("/blockConversation")
    public ResponseModel blockConversation(@RequestParam Long conversationId) throws ApplicationException {
        String responseMsg = messagingService.blockConversation(conversationId);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setPayload(null);
        response.setMessage(responseMsg);
        return response;
    }

    @PostMapping("/unblockConversation")
    public ResponseModel unblockConversation(@RequestParam Long conversationId) throws ApplicationException {
        String responseMsg = messagingService.unblockConversation(conversationId);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setPayload(null);
        response.setMessage(responseMsg);
        return response;
    }

    @PostMapping("/sendMessage")
    public ResponseModel sendMessage(@RequestParam Long conversationId, @RequestParam String messageContent) throws ApplicationException {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setPayload(messagingService.sendMessage(conversationId, messageContent));
        response.setMessage(MessageConstants.MSG_MESSAGE_SENT);
        return response;
    }

    @PostMapping("/startConversation")
    public ResponseModel startConversation(@RequestParam Long recipientUserId, @RequestParam String messageContent) throws ApplicationException {
        Long conversationId = messagingService.startConversation(recipientUserId, messageContent);

        HashMap<String, Long> respMap = new HashMap<>();
        respMap.put(CONVERSATION_ID_KEY, conversationId);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setPayload(respMap);
        response.setMessage(MessageConstants.MSG_MESSAGE_SENT);
        return response;
    }

    @GetMapping("/getConversationId")
    public ResponseModel getConversationId(@RequestParam Long recipientUserId) throws ApplicationException {
        Long conversationId = messagingService.getConversationId(recipientUserId);

        HashMap<String, Long> respMap = new HashMap<>();
        if (conversationId != null) {
            respMap.put(CONVERSATION_ID_KEY, conversationId);
        }

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(respMap);

        return response;
    }

    @PostMapping("/blockConversationByAdmin")
    public ResponseModel blockConversationByAdmin(@RequestParam Long conversationId) throws ApplicationException {
        String responseMsg = messagingService.blockConversationByAdmin(conversationId);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setPayload(null);
        response.setMessage(responseMsg);
        return response;
    }

    @PostMapping("/unblockConversationByAdmin")
    public ResponseModel unblockConversationByAdmin(@RequestParam Long conversationId) throws ApplicationException {
        String responseMsg = messagingService.unblockConversationByAdmin(conversationId);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setPayload(null);
        response.setMessage(responseMsg);
        return response;
    }

    /**
     * API to get Messaging NotificationDetails
     * @return
     * @throws ApplicationException
     */
    @GetMapping("/notification")
    public ResponseModel getNotificationDetails() throws ApplicationException {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(messagingService.getNotificationDetails());

        return response;
    }

}
