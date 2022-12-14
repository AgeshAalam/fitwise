package com.fitwise.service.fcm;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.User;
import com.fitwise.entity.UserFcmToken;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.fcm.UserFcmTokenRepository;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.fcm.UserFcmTokenView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class UserFcmTokenService {

    @Autowired
    private UserFcmTokenRepository fcmTokenRepository;

    @Autowired
    private UserComponents userComponents;

    public void saveUserFcmToken(UserFcmTokenView fcmTokenView) {
        log.info("Save user FCM token starts");
        long start = new Date().getTime();
        long profilingStart;
        if (fcmTokenView == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FCM_DATA_NULL, MessageConstants.ERROR);
        }
        if (ValidationUtils.isEmptyString(fcmTokenView.getFcmToken())) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FCM_TOKEN_NULL, MessageConstants.ERROR);
        }
        log.info("Basic validation : Time taken in millis : "+(new Date().getTime() - start));
        profilingStart = new Date().getTime();

        User user = userComponents.getUser();
        String token = fcmTokenView.getFcmToken();

        List<UserFcmToken> fcmTokenList = fcmTokenRepository.findByUserAndFcmtoken(user, token);
        log.info("Get FCM token list and user : Time taken in millis : "+(new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();
        if (fcmTokenList.isEmpty()) {
            UserFcmToken fcmToken = new UserFcmToken();
            fcmToken.setFcmtoken(fcmTokenView.getFcmToken());
            fcmToken.setUser(user);
            fcmTokenRepository.save(fcmToken);
        }
        log.info("Save FCM token : Time taken in millis : "+(new Date().getTime() - profilingStart));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - start));
        log.info("Save user FCM token ends");
    }

    /**
     * Delete user's fcm token
     * @param fcmTokenView
     */
    public void removeFcmToken(UserFcmTokenView fcmTokenView) {

        if (fcmTokenView == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FCM_DATA_NULL, MessageConstants.ERROR);
        }
        if (ValidationUtils.isEmptyString(fcmTokenView.getFcmToken())) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FCM_TOKEN_NULL, MessageConstants.ERROR);
        }

        User user = userComponents.getUser();
        String token = fcmTokenView.getFcmToken();

        List<UserFcmToken> fcmTokenList = fcmTokenRepository.findByUserAndFcmtoken(user, token);

        if (!fcmTokenList.isEmpty()) {
            fcmTokenRepository.deleteInBatch(fcmTokenList);
        }

    }

    /**
     * Remove all fcm tokens of user
     * @param user
     */
    public void removeFcmTokenOfUser(User user) {

        List<UserFcmToken> fcmTokenList = fcmTokenRepository.findByUser(user);
        if (!fcmTokenList.isEmpty()) {
            fcmTokenRepository.deleteInBatch(fcmTokenList);
        }

    }
}
