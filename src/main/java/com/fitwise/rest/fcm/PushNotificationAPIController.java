package com.fitwise.rest.fcm;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.service.fcm.PushNotificationAPIService;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.fcm.NotificationContent;
import com.fitwise.view.fcm.PushNotificationApiRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/*
 * Created by Vignesh G on 22/07/20
 */
@RestController
@RequestMapping(value = "/v1/notification/api")
public class PushNotificationAPIController {

    @Autowired
    PushNotificationAPIService pushNotificationAPIService;

    /**
     * Send notification for a role
     * @param request
     * @param role
     * @return
     */
    @PostMapping("/sendNotification")
    public ResponseModel sendNotification(@RequestBody PushNotificationApiRequest request, @RequestParam String role) {
        boolean httpCallSuccess = pushNotificationAPIService.sendPushNotificationWithoutData(request, role);

        Map<String, Boolean> responseMap = new HashMap<>();
        responseMap.put("NotificationSent", httpCallSuccess);

        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_PUSH_NOTIFICATION_INVOKED);
        responseModel.setPayload(responseMap);
        return responseModel;
    }

    /**
     * Send sample notification to current user
     * @param notificationContent
     * @return
     */
    @PostMapping("/sendSampleNotification")
    public ResponseModel sendSampleNotification(@RequestBody NotificationContent notificationContent) {
        boolean httpCallSuccess = pushNotificationAPIService.sendSampleNotification(notificationContent);

        Map<String, Boolean> responseMap = new HashMap<>();
        responseMap.put("NotificationSent", httpCallSuccess);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_PUSH_NOTIFICATION_INVOKED);
        response.setPayload(responseMap);
        return response;
    }

}
