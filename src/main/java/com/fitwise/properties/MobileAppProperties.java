package com.fitwise.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/*
 * Created by Vignesh G on 24/08/20
 */
@Component
@Getter
public class MobileAppProperties {

    @Value("${appstore.instructor.url}")
    private String appstoreInstructorUrl;

    @Value("${appstore.member.url}")
    private String appstoreMemberUrl;

    @Value("${playstore.instructor.url}")
    private String playstoreInstructorUrl;

    @Value("${playstore.member.url}")
    private String playstoreMemberUrl;

    @Value("${member.android.package}")
    private String memberAndroidPackage;

    @Value("${member.ios.bundle}")
    private String memberiOSPackage;

    @Value("${instructor.android.package}")
    private String instructorAndroidPackage;

    @Value("${instructor.ios.bundle}")
    private String instructoriOSPackage;

}
