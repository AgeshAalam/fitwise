package com.fitwise.service.fcm;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.User;
import com.fitwise.entity.UserFcmToken;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.fcm.UserFcmTokenRepository;
import com.fitwise.view.fcm.NotificationContent;
import com.fitwise.view.fcm.PushNotificationApiRequest;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

/*
 * Created by Vignesh G on 22/07/20
 */
@Service
public class PushNotificationAPIService {

    @Value("${fcm.push.serverkey.member}")
    private String memberServerKey;

    @Value("${fcm.push.serverkey.instructor}")
    private String instructorServerKey;

    @Autowired
    private UserComponents userComponents;
    @Autowired
    private UserFcmTokenRepository userFcmTokenRepository;

    /**
     * Send notification for a role
     * @param pushNotificationApiRequest
     * @param role
     * @return
     */
    public boolean sendPushNotificationWithoutData(PushNotificationApiRequest pushNotificationApiRequest, String role) {

        //Validations
        if (role == null || role.isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ROLE_NULL, null);
        }
        if (!(role.equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR) || role.equalsIgnoreCase(KeyConstants.KEY_MEMBER))) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ROLE_INCORRECT, null);
        }

        if (pushNotificationApiRequest == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PUSH_NOTIFICATION_DATA_NULL, null);
        }
        if (pushNotificationApiRequest.getRegistration_ids() == null || pushNotificationApiRequest.getRegistration_ids().isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PUSH_NOTIFICATION_TOKEN_NOT_FOUND, null);
        }

        //Setting server key to select the app to which notifications are relayed
        String serverKey;
        if (role.equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR)) {
            serverKey = instructorServerKey;
        } else {
            serverKey = memberServerKey;
        }

        boolean httpCallSuccess = false;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "key=" + serverKey);

        HttpEntity<?> request = new HttpEntity<>(pushNotificationApiRequest, headers);
        String url = "https://fcm.googleapis.com/fcm/send";

        ResponseEntity<?> response = new RestTemplate().postForEntity(url, request, String.class);
        JSONObject jsonObj = new JSONObject(response.getBody().toString());
        int success = (int) jsonObj.get("success");
        if (success > 0) {
            httpCallSuccess = true;
        }
        return httpCallSuccess;
    }

    /**
     * Send notification to current user
     * @param notificationContent
     * @return
     */
    public boolean sendSampleNotification(NotificationContent notificationContent) {
        User currentUser = userComponents.getUser();
        String role = userComponents.getRole();

        return sendOnlyNotification(notificationContent, currentUser, role);
    }

    /**
     * Sending Push notification with only notification and no actual data
     * @param notificationContent
     * @param user
     * @param role
     * @return
     */
    public boolean sendOnlyNotification(NotificationContent notificationContent, User user, String role) {

        List<UserFcmToken> userFcmTokenList = userFcmTokenRepository.findByUser(user);
        List<String> tokenList = userFcmTokenList.stream().map(userFcmToken -> userFcmToken.getFcmtoken()).distinct().collect(Collectors.toList());

        //Push notifications sent only to users with fcm token
        if (!tokenList.isEmpty()) {

            PushNotificationApiRequest pushNotificationApiRequest = new PushNotificationApiRequest();
            pushNotificationApiRequest.setRegistration_ids(tokenList);
            pushNotificationApiRequest.setNotification(notificationContent);
            pushNotificationApiRequest.setData(notificationContent);

            return sendPushNotificationWithoutData(pushNotificationApiRequest, role);
        }
        return false;
    }
}
