package com.fitwise.service.dynamiclink;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.exception.ApplicationException;
import com.fitwise.properties.GeneralProperties;
import com.fitwise.properties.MobileAppProperties;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.packaging.SubscriptionPackageRepository;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.utils.APIBuilder;
import com.fitwise.utils.APIService;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.view.dynamiclink.AndroidInfo;
import com.fitwise.view.dynamiclink.DynamicLinkInfo;
import com.fitwise.view.dynamiclink.DynamicLinkRequest;
import com.fitwise.view.dynamiclink.IOSInfo;
import com.fitwise.view.dynamiclink.SocialMetaTagInfo;
import com.fitwise.view.dynamiclink.Suffix;
import com.google.gson.internal.LinkedTreeMap;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Response;

/*
 * Created by Vignesh G on 25/08/20
 */
@Service
@Slf4j
public class DynamicLinkService {

    @Value("${member.dynamiclink.domain}")
    private String memberDynamicLinkDomain;

    @Value("${member.dynamiclink.webapi.key}")
    private String memberWebApiKey;

    @Value("${instructor.dynamiclink.domain}")
    private String instructorDynamicLinkDomain;

    @Value("${instructor.dynamiclink.webapi.key}")
    private String instructorWebApiKey;

    @Autowired
    private UserComponents userComponents;
    @Autowired
    private ProgramRepository programRepository;
    @Autowired
    private ValidationService validationService;
    @Autowired
    FitwiseUtils fitwiseUtils;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private GeneralProperties generalProperties;
    @Autowired
    private MobileAppProperties mobileAppProperties;
    @Autowired
    private SubscriptionPackageRepository subscriptionPackageRepository;

    /**
     * Method to invoke Firebase dynamic link API
     * @param dynamicLinkRequest
     * @return
     */
    private String getShortLink(DynamicLinkRequest dynamicLinkRequest, String webApiKey) {

        String dynamicLinkBaseUrl = "https://firebasedynamiclinks.googleapis.com";
        APIService webService = APIBuilder.builder(dynamicLinkBaseUrl);
        LinkedTreeMap<String, Object> responseBody = null;
        ResponseBody errorBody = null;
        try {
            Response<?> response = webService.createShortLink(dynamicLinkRequest, "application/json", webApiKey).execute();
            responseBody = (LinkedTreeMap) response.body();
            errorBody = response.errorBody();
        } catch (Exception e) {
            log.error("Exception occurred while ");
            e.printStackTrace();
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_DYNAMIC_LINK_SHORT_LINK_CREATION_FAILED, MessageConstants.ERROR);
        }

        if (responseBody == null) {
            if (errorBody != null) {
                try {
                    String errorMsg = errorBody.string();
                    log.error("Dynamic link API error response : ");
                    log.error(errorMsg);
                } catch (Exception e) {
                }
            }
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_DYNAMIC_LINK_SHORT_LINK_NOT_CREATED, MessageConstants.ERROR);
        }
        String shortLink = responseBody.get("shortLink").toString();

        return shortLink;
    }

    /**
     * Method to get a short link based on request body
     * @param dynamicLinkRequest
     * @return
     */
    public String getShortLinkForParams(DynamicLinkRequest dynamicLinkRequest) {

        DynamicLinkInfo dynamicLinkInfo = dynamicLinkRequest.getDynamicLinkInfo();
        if (dynamicLinkInfo == null) {
            throw new ApplicationException(Constants.ERROR_STATUS, ValidationMessageConstants.MSG_DYNAMIC_LINK_INFO_MISSING, MessageConstants.ERROR);
        }
        if (dynamicLinkInfo.getDomainUriPrefix() == null) {
            throw new ApplicationException(Constants.ERROR_STATUS, ValidationMessageConstants.MSG_DYNAMIC_LINK_DOMAIN_MISSING, MessageConstants.ERROR);
        }
        if (dynamicLinkInfo.getLink() == null) {
            throw new ApplicationException(Constants.ERROR_STATUS, ValidationMessageConstants.MSG_DYNAMIC_LINK_MISSING, MessageConstants.ERROR);
        }

        String shortLink = getShortLink(dynamicLinkRequest, memberWebApiKey);
        return shortLink;
    }

    /**
     * Method to get short link for program - to be shared with members
     * @param programId
     * @return
     */
    public String constructProgramLinkForMember(Long programId) {
        User user = userComponents.getUser();
        return constructProgramLinkForMember(programId, user);
    }

    public String constructProgramLinkForMember(Long programId, User user) {

        if (programId == null || programId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_NULL, MessageConstants.ERROR);
        }
        Programs program = programRepository.findByProgramIdAndOwnerUserId(programId, user.getUserId());
        if (program == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, null);
        }

        String memberProgramL2UrlSuffix = "/app/discover/program/" + programId.toString();

        String link = generalProperties.getMemberBaseUrl() + memberProgramL2UrlSuffix;

        DynamicLinkInfo dynamicLinkInfo = new DynamicLinkInfo();
        dynamicLinkInfo.setDomainUriPrefix(memberDynamicLinkDomain);
        dynamicLinkInfo.setLink(link);

        AndroidInfo androidInfo = new AndroidInfo();
        androidInfo.setAndroidPackageName(mobileAppProperties.getMemberAndroidPackage());
        dynamicLinkInfo.setAndroidInfo(androidInfo);

        IOSInfo iosInfo = new IOSInfo();
        iosInfo.setIosBundleId(mobileAppProperties.getMemberiOSPackage());
        iosInfo.setIosFallbackLink(mobileAppProperties.getAppstoreMemberUrl());
        dynamicLinkInfo.setIosInfo(iosInfo);

        SocialMetaTagInfo socialMetaTagInfo = new SocialMetaTagInfo();
        socialMetaTagInfo.setSocialTitle(program.getTitle());
        socialMetaTagInfo.setSocialDescription(program.getShortDescription());
        if (program.getImage() != null) {
            socialMetaTagInfo.setSocialImageLink(program.getImage().getImagePath());
        }
        dynamicLinkInfo.setSocialMetaTagInfo(socialMetaTagInfo);

        DynamicLinkRequest dynamicLinkRequest = new DynamicLinkRequest();
        dynamicLinkRequest.setDynamicLinkInfo(dynamicLinkInfo);

        Suffix suffix = new Suffix();
        suffix.setOption("UNGUESSABLE");

        dynamicLinkRequest.setSuffix(suffix);

        String shortLink = getShortLink(dynamicLinkRequest, memberWebApiKey);

        return shortLink;
    }

    /**
     * Method to get short link for current instructor profile - to be shared with members
     * @return
     */
    public String constructInstructorProfileLinkForMember() {

        User user = userComponents.getUser();

        User instructor = validationService.validateInstructorId(user.getUserId());
        fitwiseUtils.validateCurrentInstructorBlocked();

        UserProfile userProfile = userProfileRepository.findByUser(instructor);

        String instructorProfileUrlSuffix = "/app/discover/detail/" + user.getUserId().toString();

        String link = generalProperties.getMemberBaseUrl() + instructorProfileUrlSuffix;

        DynamicLinkInfo dynamicLinkInfo = new DynamicLinkInfo();
        dynamicLinkInfo.setDomainUriPrefix(memberDynamicLinkDomain);
        dynamicLinkInfo.setLink(link);

        AndroidInfo androidInfo = new AndroidInfo();
        androidInfo.setAndroidPackageName(mobileAppProperties.getMemberAndroidPackage());
        dynamicLinkInfo.setAndroidInfo(androidInfo);

        IOSInfo iosInfo = new IOSInfo();
        iosInfo.setIosBundleId(mobileAppProperties.getMemberiOSPackage());
        iosInfo.setIosFallbackLink(mobileAppProperties.getAppstoreMemberUrl());
        dynamicLinkInfo.setIosInfo(iosInfo);

        SocialMetaTagInfo socialMetaTagInfo = new SocialMetaTagInfo();
        String title = fitwiseUtils.getUserFullName(userProfile);
        socialMetaTagInfo.setSocialTitle(title);
        socialMetaTagInfo.setSocialDescription(userProfile.getBiography());
        if (userProfile.getProfileImage() != null) {
            socialMetaTagInfo.setSocialImageLink(userProfile.getProfileImage().getImagePath());
        }
        dynamicLinkInfo.setSocialMetaTagInfo(socialMetaTagInfo);

        DynamicLinkRequest dynamicLinkRequest = new DynamicLinkRequest();
        dynamicLinkRequest.setDynamicLinkInfo(dynamicLinkInfo);

        Suffix suffix = new Suffix();
        suffix.setOption("UNGUESSABLE");

        dynamicLinkRequest.setSuffix(suffix);

        String shortLink = getShortLink(dynamicLinkRequest, memberWebApiKey);

        return shortLink;

    }


    /**
     * Method to get short link for SubscriptionPackage - to be shared with members
     * @param subscriptionPackageId
     * @return
     */
    public String constructPackageLinkForMember(Long subscriptionPackageId) {
        User user = userComponents.getUser();
        return constructPackageLinkForMember(subscriptionPackageId,null,user);
    }

    public String constructPackageLinkForMember(Long subscriptionPackageId, String token,User user) {

        if (subscriptionPackageId == null || subscriptionPackageId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_ID_NULL, MessageConstants.ERROR);
        }
        SubscriptionPackage subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageIdAndOwnerUserId(subscriptionPackageId, user.getUserId());
        if (subscriptionPackage == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_NOT_FOUND, null);
        }

        String memberPackageL2UrlSuffix;
        if (token == null) {
            memberPackageL2UrlSuffix = "/app/discover/subscription-package/" + subscriptionPackageId.toString();
        } else {
            memberPackageL2UrlSuffix = "/app/discover/subscription-package/" + subscriptionPackageId.toString() + "?token=" + token;
        }

        String link = generalProperties.getMemberBaseUrl() + memberPackageL2UrlSuffix;

        DynamicLinkInfo dynamicLinkInfo = new DynamicLinkInfo();
        dynamicLinkInfo.setDomainUriPrefix(memberDynamicLinkDomain);
        dynamicLinkInfo.setLink(link);

        //TODO : uncomment below code when Subscription Package is available in android and iOS
        /*
        AndroidInfo androidInfo = new AndroidInfo();
        androidInfo.setAndroidPackageName(mobileAppProperties.getMemberAndroidPackage());
        dynamicLinkInfo.setAndroidInfo(androidInfo);

        IOSInfo iosInfo = new IOSInfo();
        iosInfo.setIosBundleId(mobileAppProperties.getMemberiOSPackage());
        iosInfo.setIosFallbackLink(mobileAppProperties.getAppstoreMemberUrl());
        dynamicLinkInfo.setIosInfo(iosInfo);
        */

        SocialMetaTagInfo socialMetaTagInfo = new SocialMetaTagInfo();
        socialMetaTagInfo.setSocialTitle(subscriptionPackage.getTitle());
        socialMetaTagInfo.setSocialDescription(subscriptionPackage.getShortDescription());
        if (subscriptionPackage.getImage() != null) {
            socialMetaTagInfo.setSocialImageLink(subscriptionPackage.getImage().getImagePath());
        }
        dynamicLinkInfo.setSocialMetaTagInfo(socialMetaTagInfo);

        DynamicLinkRequest dynamicLinkRequest = new DynamicLinkRequest();
        dynamicLinkRequest.setDynamicLinkInfo(dynamicLinkInfo);

        Suffix suffix = new Suffix();
        suffix.setOption("UNGUESSABLE");

        dynamicLinkRequest.setSuffix(suffix);

        String shortLink = getShortLink(dynamicLinkRequest, memberWebApiKey);

        return shortLink;
    }

    public String constructCalendarLinkForInstructor() {

        String instructorCalendarUrlSuffix = "/app/calendar";

        String link = generalProperties.getInstructorBaseUrl() + instructorCalendarUrlSuffix;

        DynamicLinkInfo dynamicLinkInfo = new DynamicLinkInfo();
        dynamicLinkInfo.setDomainUriPrefix(instructorDynamicLinkDomain);
        dynamicLinkInfo.setLink(link);

        AndroidInfo androidInfo = new AndroidInfo();
        androidInfo.setAndroidPackageName(mobileAppProperties.getInstructorAndroidPackage());
        dynamicLinkInfo.setAndroidInfo(androidInfo);

        IOSInfo iosInfo = new IOSInfo();
        iosInfo.setIosBundleId(mobileAppProperties.getInstructoriOSPackage());
        iosInfo.setIosFallbackLink(mobileAppProperties.getAppstoreInstructorUrl());
        dynamicLinkInfo.setIosInfo(iosInfo);


        DynamicLinkRequest dynamicLinkRequest = new DynamicLinkRequest();
        dynamicLinkRequest.setDynamicLinkInfo(dynamicLinkInfo);

        Suffix suffix = new Suffix();
        suffix.setOption("UNGUESSABLE");

        dynamicLinkRequest.setSuffix(suffix);

        String shortLink = getShortLink(dynamicLinkRequest, instructorWebApiKey);

        return shortLink;
    }
}
