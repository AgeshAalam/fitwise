package com.fitwise.utils;

import com.fitwise.properties.MobileAppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*
 * Created by Vignesh G on 24/08/20
 */
@Component
public class EmailContentUtil {

    @Autowired
    private MobileAppProperties mobileAppProperties;

    /**
     * Replaces instructor mobile app's store urls
     * @param mailContent
     * @return
     */
    public String replaceInstructorAppUrl(String mailContent) {
        String appStoreUrl = mobileAppProperties.getAppstoreInstructorUrl();
        String playStoreUrl = mobileAppProperties.getPlaystoreInstructorUrl();

        mailContent = mailContent.replace("#APP_STORE_URL#", appStoreUrl);
        mailContent = mailContent.replace("#PLAY_STORE_URL#", playStoreUrl);

        return mailContent;
    }

    /**
     * Replaces member mobile app's store urls
     * @param mailContent
     * @return
     */
    public String replaceMemberAppUrl(String mailContent) {
        String appStoreUrl = mobileAppProperties.getAppstoreMemberUrl();
        String playStoreUrl = mobileAppProperties.getPlaystoreMemberUrl();

        mailContent = mailContent.replace("#APP_STORE_URL#", appStoreUrl);
        mailContent = mailContent.replace("#PLAY_STORE_URL#", playStoreUrl);

        return mailContent;
    }

}
