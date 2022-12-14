package com.fitwise.service.payment.paypal;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.entity.User;
import com.fitwise.entity.payments.paypal.UserAccountAndPayPalIdMapping;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.payments.stripe.paypal.UserAccountAndPayPalIdMappingRepository;
import com.fitwise.view.ResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PayPalService {

    @Autowired
    private UserAccountAndPayPalIdMappingRepository userAccountAndPayPalIdMappingRepository;

    @Autowired
    private UserComponents userComponents;

    /**
     * Used to save paypal id against an user
     *
     * @param payPalId
     * @return
     */
    @Transactional
    public ResponseModel savePayPalId(String payPalId) {
        log.info("Save pay pal id starts.");
        long apiStartTimeMillis = new Date().getTime();
        if (payPalId == null || payPalId.isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAYPAL_ID_INVALID, null);
        }
        User user = userComponents.getUser();
        UserAccountAndPayPalIdMapping userAccountAndPayPalIdMapping = userAccountAndPayPalIdMappingRepository.findByUserUserId(user.getUserId());
        if (userAccountAndPayPalIdMapping == null) {
            userAccountAndPayPalIdMapping = new UserAccountAndPayPalIdMapping();
            userAccountAndPayPalIdMapping.setUser(user);
        }
        log.info("Query to get user and UserAccountAndPayPalIdMapping : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        userAccountAndPayPalIdMapping.setPayPalId(payPalId);
        userAccountAndPayPalIdMappingRepository.save(userAccountAndPayPalIdMapping);
        log.info("Query to save UserAccountAndPayPalIdMapping : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        Map<String, Object> payPalObject = new HashMap<>();
        payPalObject.put(KeyConstants.KEY_PAYPAL_ID, userAccountAndPayPalIdMapping.getPayPalId());
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Save pay pal id ends.");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PAYPAL_ID_SAVED, payPalObject);
    }

}
