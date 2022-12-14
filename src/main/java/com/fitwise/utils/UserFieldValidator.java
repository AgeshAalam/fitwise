package com.fitwise.utils;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.properties.GeneralProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserFieldValidator {

    @Autowired
    private GeneralProperties generalProperties;

    public void isTestBotEmail(String email){
        String[] emailDomains = generalProperties.getTestBotEmailDomains().split(",");
        for(String domain : emailDomains){
            if(email.endsWith("@" + domain)){
                log.info("Bot register triggered : " + email);
                throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_ERR_USR_EMAIL_NOT_ALLOWED, null);
            }
        }
    }
}
