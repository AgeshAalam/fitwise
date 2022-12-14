package com.fitwise.service;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.AppConfigKeyValue;
import com.fitwise.entity.AppForceLogoutAudit;
import com.fitwise.entity.AppUpdateInfo;
import com.fitwise.entity.AppVersionInfo;
import com.fitwise.entity.DeviceInfoDetails;
import com.fitwise.entity.User;
import com.fitwise.exception.ApplicationException;
import com.fitwise.properties.ZenDeskProperties;
import com.fitwise.repository.AppConfigKeyValueRepository;
import com.fitwise.repository.AppForceLogoutAuditRepository;
import com.fitwise.repository.AppUpdateInfoRepository;
import com.fitwise.repository.AppVersionInfoRepository;
import com.fitwise.repository.DeviceInfoDetailsRepository;
import com.fitwise.request.UserDeviceDetails;
import com.fitwise.view.AppConfigResponseView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * The Class AppUpdateService.
 */
@Service
public class AppUpdateService {

    /**
     * The app update info repository.
     */
    @Autowired
    private AppUpdateInfoRepository appUpdateInfoRepository;

    /**
     * The device info details repository.
     */
    @Autowired
    private DeviceInfoDetailsRepository deviceInfoDetailsRepository;

    /**
     * The app version info repository.
     */
    @Autowired
    private AppVersionInfoRepository appVersionInfoRepository;

    /**
     * The app force logout audit repository.
     */
    @Autowired
    private AppForceLogoutAuditRepository appForceLogoutAuditRepository;

    /**
     * The app config key value repository.
     */
    @Autowired
    private AppConfigKeyValueRepository appConfigKeyValueRepository;

    @Autowired
    private ZenDeskProperties zenDeskProperties;

    /**
     * Check app update service.
     *
     * @param userDeviceDetails the user device details
     * @param user              the user
     * @return the app config response view
     */
    @Transactional
    public AppConfigResponseView checkAppUpdateService(final UserDeviceDetails userDeviceDetails, final User user, final String role) {
        AppConfigResponseView responseView = setDefaultAppConfig(userDeviceDetails.getDevicePlatform(), role);
        validateDeviceInfoDetails(userDeviceDetails);
        DeviceInfoDetails userDeviceInfo = getDeviceDetail(userDeviceDetails, user);
        if (userDeviceInfo == null) {
            userDeviceInfo = saveUserDeviceInfoDetails(userDeviceDetails, user);
        }
        AppVersionInfo latestAppVersion = getLatestAppVersion(userDeviceDetails.getDevicePlatform(), role);
        AppVersionInfo userAppVersion = getAppInfoByAppVersion(userDeviceDetails.getAppVersion(),
                userDeviceDetails.getDevicePlatform(), role);
        AppUpdateInfo appupdate = checkAnyAppLaunchActionAvailabilityForUserAppVersion(latestAppVersion);
        if(userAppVersion != null && appupdate != null && !latestAppVersion.getAppVersion().equalsIgnoreCase(userAppVersion.getAppVersion()) && latestAppVersion.getAppVersionId() > userAppVersion.getAppVersionId()){
            if (Constants.FORCE_UPDATE.equalsIgnoreCase(appupdate.getAppLaunchActionId().getActionName())) {
                responseView = setAppUpdateValues(responseView, appupdate.getAppLaunchActionId().getActionName());
            } else if (Constants.FORCE_LOGOUT.equalsIgnoreCase(appupdate.getAppLaunchActionId().getActionName())) {
                String userIds = appupdate.getUserIds();
                if (userIds != null && !userIds.isEmpty()) {
                    String[] userIdList = userIds.split(",");
                    for (String userId : userIdList) {
                        if (user != null && userId.equals(user.getUserId().toString())) {
                            if (!checkTheUserHasAlreadyLogoutOrNot(userDeviceInfo, userAppVersion, appupdate)) {
                                createNewForceLogoutAuditRecord(userDeviceInfo, userAppVersion, appupdate);
                                responseView = setAppUpdateValues(responseView,
                                        appupdate.getAppLaunchActionId().getActionName());
                            }
                            break;
                        }
                    }
                } else {
                    if (!checkTheUserHasAlreadyLogoutOrNot(userDeviceInfo, userAppVersion, appupdate)) {
                        createNewForceLogoutAuditRecord(userDeviceInfo, userAppVersion, appupdate);
                        responseView = setAppUpdateValues(responseView,
                                appupdate.getAppLaunchActionId().getActionName());
                    }
                }
            }
        }
        return responseView;
    }

    /**
     * Gets the device detail.
     *
     * @param deviceDetails the device details
     * @param user          the user
     * @return the device detail
     */
    private DeviceInfoDetails getDeviceDetail(final UserDeviceDetails deviceDetails, final User user) {
        DeviceInfoDetails deviceInfo = null;
        deviceInfo = deviceInfoDetailsRepository
                .findByDeviceNameAndDeviceModelNameAndDevicePlatformAndDevicePlatformVersionAndDeviceUuidAndAppVersionAndUserId(
                        deviceDetails.getDeviceName(), deviceDetails.getDeviceModel(),
                        deviceDetails.getDevicePlatform(), deviceDetails.getDevicePlatformVersion(),
                        deviceDetails.getDeviceUuid(), deviceDetails.getAppVersion(), user);
        return deviceInfo;
    }

    /**
     * Save user device info details.
     *
     * @param deviceDetails the device details
     * @param user          the user
     * @return the device info details
     */
    private DeviceInfoDetails saveUserDeviceInfoDetails(final UserDeviceDetails deviceDetails, final User user) {
        DeviceInfoDetails details = new DeviceInfoDetails();
        details.setAppVersion(deviceDetails.getAppVersion());
        details.setDeviceName(deviceDetails.getDeviceName());
        details.setDeviceModelName(deviceDetails.getDeviceModel());
        details.setDevicePlatform(deviceDetails.getDevicePlatform());
        details.setDevicePlatformVersion(deviceDetails.getDevicePlatformVersion());
        details.setDeviceUuid(deviceDetails.getDeviceUuid());
        details.setCreatedDate(new Date());
        if (user != null) {
            details.setUserId(user);
        }
        details = deviceInfoDetailsRepository.save(details);
        return details;
    }

    /**
     * Gets the app info by app version.
     *
     * @param appVersion  the app version
     * @param appPlatform the app platform
     * @return the app info by app version
     */
    private AppVersionInfo getAppInfoByAppVersion(final String appVersion, final String appPlatform, final String role) {
        return appVersionInfoRepository.findByAppVersionAndAppPlatformAndApplication(appVersion, appPlatform, role);
    }

    /**
     * Gets the latest app version.
     *
     * @param appPlatform the app platform
     * @return the latest app version
     */
    private AppVersionInfo getLatestAppVersion(final String appPlatform, String role) {
        return appVersionInfoRepository.findByIsLatestVersionAndAppPlatformAndApplication(true, appPlatform, role);
    }

    /**
     * Check any app launch action availability for user app version.
     *
     * @param appInfo the app info
     * @return the app update info
     */
    private AppUpdateInfo checkAnyAppLaunchActionAvailabilityForUserAppVersion(final AppVersionInfo appInfo) {
        return appUpdateInfoRepository.findTopByAppVersionIdOrderByAppUpdateInfoIdDesc(appInfo);
    }

    /**
     * Sets the default app config.
     *
     * @param devicePlatform the device platform
     * @return the app config response view
     */
    private AppConfigResponseView setDefaultAppConfig(final String devicePlatform, String role) {
        AppVersionInfo latestAppVersion = getLatestAppVersion(devicePlatform, role);
        AppConfigResponseView responsiveView = new AppConfigResponseView();
        responsiveView.setAppForceUpdate(false);
        responsiveView.setAppForceLogout(false);
        if (latestAppVersion != null) {
            responsiveView.setAppVersion(latestAppVersion.getAppVersion());
        }
        AppConfigKeyValue keyValue = getValueUsingKeyName(Constants.PRIVACY_POLICY_URL);
        if (keyValue != null) {
            responsiveView.setPrivacyPolicyUrl(keyValue.getValueString());
        }
        keyValue = getValueUsingKeyName(Constants.TERMS_AND_CONDITION_URL);
        if (keyValue != null) {
            responsiveView.setTermsAndCondUrl(keyValue.getValueString());
        }
        keyValue = getValueUsingKeyName(Constants.FAQ_URL);
        if (keyValue != null) {
            responsiveView.setFaqUrl(keyValue.getValueString());
        }

        // Setting ZenDesk's Support phone number
        responsiveView.setSupportPhone(zenDeskProperties.getSupportPhoneNumber());

        keyValue = getValueUsingKeyName(Constants.SUPPORT_EMAIL);
        if (keyValue != null) {
            responsiveView.setSupportEmail(keyValue.getValueString());
        }
        keyValue = getValueUsingKeyName(Constants.INTRO_VIDEO);
        if (keyValue != null) {
            responsiveView.setIntroVideoUrl(keyValue.getValueString());
        }
        keyValue = getValueUsingKeyName(Constants.TRANSITION_VIDEO);
        if (keyValue != null) {
            responsiveView.setTransitionVideoUrl(keyValue.getValueString());
        }
        keyValue = getValueUsingKeyName(Constants.FITWISE_LAUNCH_DATE);
        if (keyValue != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(StringConstants.PATTERN_DATE);
            try {
                Date appLaunchDate = simpleDateFormat.parse(keyValue.getValueString());
                responsiveView.setFitwiseLaunchDate(appLaunchDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return responsiveView;
    }

    /**
     * Sets the app update values.
     *
     * @param responsiveView the responsive view
     * @param appAction      the app action
     * @return the app config response view
     */
    private AppConfigResponseView setAppUpdateValues(final AppConfigResponseView responsiveView, final String appAction) {
        switch (appAction) {
            case Constants.FORCE_UPDATE:
                responsiveView.setAppForceUpdate(true);
                break;
            case Constants.FORCE_LOGOUT:
                responsiveView.setAppForceLogout(true);
                break;
            default:
                break;
        }
        return responsiveView;
    }

    /**
     * Check the user has already logout or not.
     *
     * @param userDeviceInfo the user device info
     * @param userAppVersion the user app version
     * @param appUpdateInfo  the app update info
     * @return true, if successful
     */
    private boolean checkTheUserHasAlreadyLogoutOrNot(final DeviceInfoDetails userDeviceInfo, final AppVersionInfo userAppVersion,
                                                      AppUpdateInfo appUpdateInfo) {
        boolean userIsalreadyLogout = false;
        List<AppForceLogoutAudit> appForceLogoutAuditList = appForceLogoutAuditRepository
                .findByAppVersionIdAndDeviceInfoDetailsIdAndAppUpdateInfoId(userAppVersion, userDeviceInfo,
                        appUpdateInfo);
        if (!appForceLogoutAuditList.isEmpty()) {
            userIsalreadyLogout = true;
        }
        return userIsalreadyLogout;
    }

    /**
     * Creates the new force logout audit record.
     *
     * @param userDeviceInfo the user device info
     * @param userAppVersion the user app version
     * @param appUpdateInfo  the app update info
     */
    private void createNewForceLogoutAuditRecord(final DeviceInfoDetails userDeviceInfo, final AppVersionInfo userAppVersion,
                                                 AppUpdateInfo appUpdateInfo) {
        AppForceLogoutAudit audit = new AppForceLogoutAudit();
        audit.setDeviceInfoDetailsId(userDeviceInfo);
        audit.setAppVersionId(userAppVersion);
        audit.setAppUpdateInfoId(appUpdateInfo);
        audit.setAppLaunchActionId(appUpdateInfo.getAppLaunchActionId());
        if (userDeviceInfo.getUserId() != null) {
            audit.setUserId(userDeviceInfo.getUserId());
        }
        audit.setCreatedDate(new Date());
        appForceLogoutAuditRepository.save(audit);
    }

    /**
     * Validate device info details.
     *
     * @param deviceDetails the device details
     */
    private void validateDeviceInfoDetails(final UserDeviceDetails deviceDetails) {
        if (deviceDetails.getAppVersion() == null || deviceDetails.getAppVersion().isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_APP_VERSION_EMPTY,
                    MessageConstants.ERROR);
        } else if (deviceDetails.getDevicePlatform() == null || deviceDetails.getDevicePlatform().isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_DEVICE_PLATFORM_EMPTY,
                    MessageConstants.ERROR);
        } else if (deviceDetails.getDeviceUuid() == null || deviceDetails.getDeviceUuid().isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_DEVICE_UUID_EMPTY,
                    MessageConstants.ERROR);
        }
    }

    /**
     * Gets the value using key name.
     *
     * @param keyName the key name
     * @return the value using key name
     */
    private AppConfigKeyValue getValueUsingKeyName(String keyName) {
        AppConfigKeyValue keyValue = appConfigKeyValueRepository.findByKeyString(keyName);
        return keyValue;
    }

}
