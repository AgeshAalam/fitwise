package com.fitwise.constants;

import java.util.Arrays;
import java.util.List;

/**
 * The Class SecurityFilterConstants.
 */
public class SecurityFilterConstants {

    private SecurityFilterConstants() {
    }

    public static final String[] URL_FILTER_NO_AUTH = {"/v1/user/register", "/v1/user/login",
            "/v1/social/postFacebookSignIn", "/v1/social/postGoogleSignIn", "/swagger-ui.html", "/v1/user/forgotPassword",
            "/v1/user/validateEmail", "/v1/user/authenticateAndAddNewRole", "/v1/user/generateOtp", "/v1/user/savePasswordAndNewRole",
            "/v1/user/validateOtp",
            "/v1/user/common/notifyMe", "/v1/zenDesk/zenDeskTicketWebHook", "/v1/payment/initiateOneTimePaymentTransactionByCard",
            "/v1/payment/initiateRecurringProgramSubscription", "/v1/payment/cancelRecurringSubscription", "/v1/payment/getWebHookDataFromAuthNet","/v1/payment/stripe/notification",
            "/v1/payment/getANetCustomerProfile", "/v1/payment/initiateOneTimePaymentTransactionByPaymentProfile",
            "/v1/payment/initiateRecurringSubscriptionByCard", "/v1/payment/initiateRecurringSubscriptionByPaymentProfile",
            "/v1/payment/iap/uploadmetadata", "/v1/payment/iap/notification/validateReceipt", "/v1/payment/iap/notification/initialPurchaseNotification",
            "/v1/payment/iap/notification/getNotificationfromAppstore", "/v1/member/program/all", "/v1/member/program/all/filter", "/v1/member/program/all/byType", "/v1/member/program/trending/filter", "/v1/member/program/trending", "/v1/member/getInstructors", "/v1/member/instructorFilter", "/v1/member/getInstructorProfileForMember",
            "/v1/member/program/programsbyTypeFilter", "/v1/member/program/getProgramsByType", "/v1/member/program/getProgramDetails", "/v1/payment/iap/unPublishMetadata",
            "/qbo/connectToQuickbooks", "/qbo/getCompanyInfo", "/qbo/oauth2redirect", "/qbo/webhookevents", "/v1/social/validateFbUserProfileId", "/v1/social/validateAndLoginUsingUserEnteredEmail", "/v1/user/validateEmailForSocialLogin", "/v1/social/validateAppleLogin", "/v1/onboard/getAppConfigData", "/v1/zenDesk/createZenDeskTicketFromEmail", "/v1/order/getOrderReceipt",
            "/v1/googlesignin", "/v1/gauth","/v1/program/programPromoCompletionStatus",
            "/v1/payment/iap/notification/redirectNotificationfromAppstore","/v1/discounts/saveOfferCode"
            ,"/v1/discounts/getAllOfferCodes","/v1/discounts/getOfferCodeDetail","/v1/discounts/RemoveOfferCode","/v1/discounts/getAllOfferDuration",
            "/v1/discounts/getPriceList","/v1/discounts/generateOfferCode","/v1/discounts/validateOfferName","/v1/discounts/updateOfferCode","/v1/discounts/getProgramOffers","/v1/payment/iap/notification/generateSignature","/v1/calendar/instructor/saveToken", "/v1/member/package/getSubscriptionPackages", "/v1/member/package/getPackageFilters","/v1/member/package/getSubscriptionPackage",
            "/v1/cal/instructor/zoom/deauth","/fw/v2/user/login", "/v1/cal/webhook", "/v1/member/package/getInstructorPackages", "/fw/v2/member/instructors", "/v1/onboard/getProgramTypes", "/fw/v2/member/programs/trending", "/fw/v2/member/program", "/actuator/health", "/fw/v2/member/instructor", "/fw/v2/member/instructor/program/all", "/fw/v2/member/program/featured/all", "/v1/onboard/transitionvideos", "/v1/onboard/freeaccess"};

    /**
     * The Constant URL_NO_AUTH.
     */
    public static final List<String> URL_NO_AUTH = Arrays.asList(URL_FILTER_NO_AUTH);
    /**
     * The Constant ROLE_MEMBER.
     */
    public static final String ROLE_MEMBER = "Member";

    /**
     * The Constant ROLE_INSTRUCTOR.
     */
    public static final String ROLE_INSTRUCTOR = "Instructor";

    /**
     * The Constant ROLE_INSTRUCTOR.
     */
    public static final String ROLE_ADMIN = "Admin";
}
