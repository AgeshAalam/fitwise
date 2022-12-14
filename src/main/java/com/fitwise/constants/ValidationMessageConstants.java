package com.fitwise.constants;

/*
 * Created by Vignesh G on 09/04/20
 */
public class ValidationMessageConstants {

    private ValidationMessageConstants() {
    }

    /**
     * PROGRAM related validation constants
     */
    //If program id is zero or null
    public static final String MSG_PROGRAM_ID_NULL = "Program id missing";
    //If program is not available for id
    public static final String MSG_PROGRAM_NOT_FOUND = "Program not found";
    //If id does not provide expected program.
    public static final String MSG_PROGRAM_ID_INCORRECT = "Program id is incorrect";

    public static final String MSG_TITLE_NULL = "Title missing";
    public static final String MSG_PROMOTION_ID_NULL = "Promotion id is invalid";
    public static final String MSG_PROMOTION_NOT_FOUND = "Promotion id not found";
    public static final String MSG_PROGRAM_PRICE_INVALID = "Please enter valid program price.";
    public static final String MSG_PROGRAM_PRICE_NOT_FOUND = "Program price not found";
    public static final String MSG_INVALID_PROGRAM_EXPERIENCE_ID = "Invalid Program type or experience id ";
    public static final String MSG_SAMPLE_PROGRAMS_NOT_FOUND = "Sample program not found";
    public static final String MSG_PROGRAM_STATUS_NULL = "Program Status missing";
    public static final String MSG_PROGRAM_STATUS_INCORRECT = "Program Status is incorrect";
    public static final String MSG_PROGRAMS_NOT_RESTRICTED = "Program is not restricted";
    public static final String MSG_PROGRAMS_NOT_RESTRICTED_EDIT = "Program is not restricted edit status";
    public static final String MSG_RESTRICTED_PROGRAM_TITLE_CHANGE = "Cannot change title for Unpublished/Blocked program .";
    public static final String MSG_RESTRICTED_PROGRAM_SUBSCRIBED = "Program has #COUNT# Active Subscription(s). It cannot be edited.";
    public static final String MSG_PROGRAM_NOT_SUBSCRIBED = "Program is not subscribed by the user";
    public static final String MSG_CANT_UNBLOCK_SUBSCRIBED_PROGRAM = "Program has #COUNT# Active Subscription(s). It cannot be unblocked.";
    public static final String MSG_CANT_DELETE_SUBSCRIBED_PROGRAM = "Program has #COUNT# Active Subscription(s). It cannot be deleted.";

    public static final String MSG_PROGRAM_NOT_PUBLISHED_CANT_SUBSCRIBE = "This program is not available for subscription currently. Please come back later";
    public static final String MSG_MEMBER_BLOCKED_CANT_SUBSCRIBE = "You are not allowed to Subscribe this program as you are blocked by the Admin. Please contact Support for Further assistance";
    public static final String MSG_MEMBER_PROGRAM_NOT_AVAILABLE = "This program is currently not available, Please come back later.";
    public static final String MSG_BLOCKED_MEMBER_PROGRAM_NOT_AVAILABLE = "You are not allowed to View this program as you are blocked by the Admin. Please contact Support for Further assistance";
    public static final String MSG_PROMOTION_COMPLETED_DATE_MISSING = "Promotion completion date missing";
    public static final String MSG_ERR_PGM_NOT_ACCESSIBLE = "One of the program given in the list is not published. Program : ";

    /**
     * WORKOUT related validation constants
     */
    //If Workout id is zero or null
    public static final String MSG_WORKOUT_ID_NULL = "Workout id missing";
    //If Workout is not available for id
    public static final String MSG_WORKOUT_NOT_FOUND = "Workout not found";
    //If id does not provide expected program.
    public static final String MSG_WORKOUT_ID_INCORRECT = "Workout id is incorrect";
    public static final String MSG_WORKOUT_REST_ID_INVALID = "Workout rest video id is invalid";
    public static final String MSG_WORKOUT_SCHEDULE_ID_NULL = "Workout schedule id missing";
    public static final String MSG_WORKOUT_SCHEDULE_NOT_FOUND = "Workout schedule not found";
    public static final String MSG_WORKOUT_SCHEDULE_INCORRECT = "Workout schedule id is incorrect";
    public static final String MSG_WORKOUT_SCHEDULE_REST = "Workout schedule is a rest schedule";
    public static final String MSG_WORKOUT_MODEL_EMPTY = "Workout data missing";

    /**
     * PROGRAM and WORKOUT validation constants
     */
    public static final String MSG_FEEDBACK_TYPE_ID_NULL = "Feedback type id missing";
    public static final String MSG_FEEDBACK_TYPE_NOT_FOUND = "Feedback type not found";
    public static final String MSG_DISCARD_FEEDBACK_ID_NULL = "Discard Feedback id missing";
    public static final String MSG_DISCARD_FEEDBACK_NOT_FOUND = "Discard Feedback not found";
    public static final String MSG_DISCARD_CUSTOM_FEEDBACK_NOT_FOUND = "Custom Reason for Discard Feedback  not found";
    public static final String MSG_CANT_ADD_WORKOUT_TO_SCHEDULE = "Workout does not belong to this program. Cannot add workout to schedule.";

    //Constants related to schedule order validation
    public static final String MSG_SCHEDULE_ORDER_MISSING = "Schedule Order missing";
    public static final String MSG_SCHEDULE_ORDER_CANT_BE_ZERO = "Schedule Order cannot be zero or less";
    public static final String MSG_SCHEDULE_ORDER_INCORRECT = "Schedule Order incorrect";
    public static final String MSG_RATING_INPUT_INCORRECT = "Rating shouldn't be zero or more than five";
    public static final String MSG_PROGRAM_RATING_CANT_SUBMIT = "Program not completed. Cannot submit Program Rating.";
    public static final String MSG_PROGRAM_RATING_CANT_RESUBMIT = "Program Rating already submitted. Cannot resubmit without program completion";

    /**
     * CIRCUIT related validation constants
     */

    //If Circuit model is empty
    public static final String MSG_CIRCUIT_MODEL_EMPTY = "Circuit data missing";
    //If Circuit id is zero or null
    public static final String MSG_CIRCUIT_ID_NULL = "Circuit id missing";
    //If Circuit is not available for id
    public static final String MSG_CIRCUIT_NOT_FOUND = "Circuit not found";
    public static final String MSG_CIRCUIT_SCHEDULE_ID_NULL = "Circuit schedule id missing";
    public static final String MSG_CIRCUIT_SCHEDULE_NOT_FOUND = "Circuit Schedule not found";
    public static final String MSG_CIRCUIT_SCHEDULE_INCORRECT = "Circuit schedule id is incorrect";
    public static final String MSG_CIRCUITS_EMPTY = "Circuits cannot be empty";
    public static final String MSG_CIRCUITS_REPEAT_INCORRECT = "Circuit repeat value is not valid";
    public static final String MSG_CIRCUITS_REST_DURATION_MISSING = "Circuit rest duration missing";
    public static final String MSG_CIRCUIT_EXERCISE_SCHEDULE_EMPTY = "Circuit's exercise schedule cannot be empty";
    public static final String MSG_CIRCUIT_SCHEDULE_REST = "Circuit schedule is a rest circuit";

    /**
     * EXERCISE related validation constants
     */
    //If Exercise id is zero or null
    public static final String MSG_EXERCISE_ID_NULL = "Exercise id missing";
    //If Exercise is not available for id
    public static final String MSG_EXERCISE_NOT_FOUND = "Exercise not found";
    //If Flagged Exercise is not available for id
    public static final String MSG_FLAGGED_EXERCISE_NOT_FOUND = "Flagged Exercise not found";
    public static final String MSG_FLAGGED_EXERCISE_ALREADY_BLOCKED = "Flagged Exercise already blocked";
    public static final String MSG_FLAGGED_EXERCISE_NOT_BLOCKED = "Cannot unblock Flagged Exercise. It is not blocked";
    public static final String MSG_FLAGGED_EXERCISE_CANT_IGNORE = "Flagged Exercise is in blocked state. Cannot change status to ignore";
    //If Exercise schedule id is zero or null
    public static final String MSG_EXERCISE_SCHEDULE_ID_NULL = "Exercise schedule id missing";
    public static final String MSG_EXERCISE_SCHEDULE_NOT_FOUND = "Exercise Schedule not found";
    public static final String MSG_EXERCISE_SCHEDULE_INCORRECT = "Exercise schedule id is incorrect";
    public static final String MSG_EXERCISE_SCHEDULE_ORDER_INVALID = "Exercise schedule order is invalid";
    public static final String MSG_EXERCISE_EMPTY = "Exercises cannot be empty";
    public static final String MSG_FLAG_REASON_ID_NULL = "Exercise Flagging Reason id missing";
    public static final String MSG_FLAG_REASON_ID_NOT_FOUND = "Exercise Flagging Reason not found";
    public static final String MSG_EXERCISE_COMPLETION_DATE_NULL = "Exercise completion date shouldn't not be null or zero";

    /**
     * IMAGE related validation constants
     */
    //If image id is zero or null
    public static final String MSG_IMAGE_ID_NULL = "Image id missing";
    //If image is not available for id
    public static final String MSG_IMAGE_NOT_FOUND = "Image not found";

    public static final String MSG_WRONG_IMAGE_TYPE = "Image type is invalid";
    /**
     * Thumbnail related validation constants
     */
    public static final String MSG_THUMBNAIL_ID_NULL = "Thumbnail id missing";
    public static final String MSG_THUMBNAIL_MAIN_TAG_NAME_INCORRECT = "Thumbnail main tag name incorrect";
    public static final String MSG_THUMBNAIL_SUB_TAG_NAME_INCORRECT = "Thumbnail sub tag name incorrect";
    public static final String MSG_DRIVE_LINK_DOWNLOAD_FAILED = "Download from Google drive link failed";
    public static final String MSG_DRIVE_FOLDER_LINK_INVALID = "Enter a valid link for Google drive folder";
    public static final String MSG_GDRIVE_DOWNLOAD_FILE_MISSING = "File is missing in Google Drive or file download failed.";

    public static final String MSG_BULK_UPLOAD_CSV_NULL = "Csv file missing";
    public static final String MSG_BULK_UPLOAD_CSV_INVALID = "Csv file is not valid";
    public static final String MSG_BULK_UPLOAD_CSV_EMPTY = "Csv file does not have data";
    public static final String MSG_BULK_UPLOAD_CSV_WRONG_STRUCTURE = "Csv file is not as per accepted structure";

    public static final String MSG_BULK_UPLOAD_ID_NULL = "Bulk upload id missing";
    public static final String MSG_BULK_UPLOAD_NOT_FOUND = "Bulk Upload details not found";
    public static final String MSG_BULK_UPLOAD_FAILURE_CSV_GENERATION_FAILED = "Bulk Upload failures csv generation failed";
    public static final String MSG_BULK_UPLOAD_SAMPLE_CSV_FAILED = "Bulk Upload sample csv download failed";

    /**
     * Subscription related constants
     */
    public static final String MSG_SUBSCRIPTION_PLAN_ID_NULL = "Subscription Plan id missing";

    /**
     * USER related validation constants
     */
    //If User id is zero or null
    public static final String MSG_USER_ID_NULL = "User id missing";
    //If User is not available for id
    public static final String MSG_USER_NOT_FOUND = "User not found";
    public static final String MSG_USER_NOT_MEMBER = "User is not a member";
    public static final String MSG_USER_NOT_INSTRUCTOR = "User is not an instructor";

    public static final String MSG_FIRST_NAME_ERROR = "First Name is invalid";
    public static final String MSG_LAST_NAME_ERROR = "Last Name is invalid";
    public static final String MSG_SECRET_NULL = "Password missing";
    public static final String MSG_EMAIL_EMPTY = "Email can't be empty";
    public static final String MSG_ERR_EMAIL_MAX_LENGTH = "Email should be less than or equal to 100 characters";
    public static final String MSG_EMAIL_ERROR = "Please enter valid email id";
    public static final String MSG_INVALID_SECRET = "Invalid password";
    public static final String MSG_FCM_DATA_NULL = "User FCM token data is missing";
    public static final String MSG_FCM_TOKEN_NULL = "User FCM token is invalid";
    public static final String MSG_LOCATION_NULL = "Location value missing";
    public static final String MSG_GYM_NULL = "Gym value missing";

    



    //If role/name is null
    public static final String MSG_ROLE_NULL = "User role missing";
    //If role is not found for name/id
    public static final String MSG_ROLE_NOT_FOUND = "User role not found";
    //If expected role is not found
    public static final String MSG_ROLE_INCORRECT = "User role incorrect";

    /**
     * Sign in related constants
     */
    public static final String MSG_CLIENT_ID_NULL = "Client id missing";
    public static final String MSG_FACEBOOK_ACCESS_TOKEN_VALID = "Facebook access token is valid";
    public static final String MSG_FACEBOOK_ACCESS_TOKEN_INVALID = "Facebook access token is invalid";

    public static final String MSG_OTP_INVALID = "The OTP is invalid. Please enter the correct OTP";
    public static final String MSG_OTP_VALID = "Given OTP is valid.";
    public static final String MSG_VALIDATE_OTP = "User needs to validate the OTP.";
    public static final String MSG_CURRENT_SECRET_INVALID = "Enter a valid current password.";
    public static final String MSG_NEW_SECRET_INVALID = "Enter a valid new password.";

    public static final String MSG_TOKEN_INVALID = "Invalid access token.";

    /**
     * Instructor related constants
     */
    public static final String MSG_MEMBER_IS_NOT_CLIENT = "The selected member is not a client";
    //If Instructor id is zero or null
    public static final String MSG_INSTRUCTOR_ID_NULL = "Instructor id missing";
    //If Instructor is not available for id

    public static final String MSG_CERTIFICATE_ID_NULL = "Certificate id missing";
    public static final String MSG_CERTIFICATE_NOT_FOUND = "Certificate not found";
    public static final String MSG_CERTIFICATE_TITLE_NULL = "Certificate Title missing";
    public static final String MSG_AWARD_ID_NULL = "Award id missing";
    public static final String MSG_AWARD_NOT_FOUND = "Award not found";
    public static final String MSG_AWARD_TITLE_NULL = "AwardTitle can't be null";

    public static final String MSG_ISSUED_DATE_NULL = "Issued Date missing";
    public static final String MSG_ACADEMY_NAME_NULL = "Academy Name missing";
    public static final String MSG_ORGANIZATION_NULL = "Organization Name missing";

    public static final String MSG_TAX_NUMBER_NULL = "Tax Number missing";

    public static final String MSG_PHONENUMBER_EMPTY = "Phone number cannot be empty";
    public static final String MSG_PHONENUMBER_INVALID = "Phone number should be valid.";
    public static final String MSG_GENDER_ERROR = "Gender is incorrect";
    public static final String MSG_NULL_INSTRUCTOR_EXPERIENCE_LIST = "Instructor experience list is null";

    /**
     * Platform related constants
     */
    public static final String MSG_DEVICE_UUID_EMPTY = "Device uuid can't be null or empty";
    public static final String MSG_DEVICE_PLATFORM_EMPTY = "Device platform can't be null or empty";
    public static final String MSG_APP_VERSION_EMPTY = "App version can't be null or empty";

    /**
     * Date related constants
     */
    public static final String MSG_DATE_NULL = "Date is missing";
    public static final String MSG_INVALID_DATE = "Invalid date";
    public static final String MSG_INVALID_YEAR_FORMAT = "Invalid year format";
    public static final String MSG_INVALID_MONTH_FORMAT = "Invalid month format";
    public static final String MSG_YEAR_INVALID = "Given year is invalid";
    public static final String MSG_MONTH_INVALID = "Given month is invalid";
    public static final String MSG_PROVIDE_DATES_WITHIN_SAME_MONTH = "Provide dates within the same month";
    public static final String MSG_START_END_DATE_FORMAT_DIFFERENT = "Provide same format for both start date and end date";
    public static final String MSG_START_DATE_AFTER_END_DATE = "Start date is after end date";
    public static final String MSG_PROVIDE_START_DATE_AND_END_DATE = "Provide Start date and End date";
    public static final String MSG_DATE_BEFORE_2020_NOT_ALLOWED = "Date prior to 2020 is not allowed.";
    public static final String MSG_FUTURE_DATE_NOT_ALLOWED = "Future date is not allowed.";
    public static final String MSG_TIME_ZONE_INVALID = "Enter a valid time zone.";

    public static final String MSG_COMPLETION_DATE_BEFORE_SUNSCRIBED_DATE = "Completion date cannot be before subscribed date";
    public static final String MSG_COMPLETION_DATE_BEFORE_PREVIOUS_COMPLETION_DATE = "Completion date cannot be before previous workout completion date";
    public static final String MSG_TRIAL_PROGRAM_CANT_COMPLETE_WORKOUT = "Cannot complete this workout for trial subscription";

    /**
     * The Constant MSG_PROGRAM_DESCRIPTION_ERROR.
     */
    public static final String MSG_NO_PLATFORMS_FOUND = "No platforms found";

    //API param validation
    public static final String MSG_BLOCK_STATUS_PARAM_NULL = "Blocked status param missing";
    public static final String MSG_BLOCK_STATUS_PARAM_INCORRECT = "Blocked status param incorrect";

    public static final String MSG_SUBSCRIPTION_STATUS_PARAM_NULL = "Subscription status param missing";
    public static final String MSG_SUBSCRIPTION_STATUS_PARAM_INCORRECT = "Subscription status param incorrect";

    //Award/Certificate image required error message
    public static final String MSG_ERR_CERTIFICATE_IMAGE_MISSING = "Please Upload Your Certificate Image.";
    public static final String MSG_ERR_AWARD_IMAGE_MISSING = "Please Upload Your Award Image.";

    //Rest Schedule activity related constants
    public static final String MSG_REST_ACTIVITY_DATA_MISSING = "Rest Activity data missing";
    public static final String MSG_REST_ACTIVITY_NOT_FOUND = "Rest Activity not found";
    public static final String MSG_REST_ACTIVITY_METRIC_INCORRECT = "Incorrect metric selected from Rest Activity";
    public static final String MSG_REST_ACTIVITY_VALUE_EXCEED_LIMIT = "Rest Activity distance/duration value exceeded limit";

    public static final String MSG_USER_NOT_ADMIN = "User is not an admin";

    public static final String MSG_THUMBNAIL_MAIN_TAG_ID_INCORRECT = "Thumbnail main tag Id incorrect";

    public static final String MSG_MULTIPLE_SUB_TAGS_NOT_ALLOWED = "Multiple sub tags are not allowed for a main tag which should have single sub tag";


    public static final String MSG_EXERCISE_TITLE_CHANGE = "Cannot change title for exercise .";

    public static final String MSG_PUSH_NOTIFICATION_DATA_NULL = "Push notification data is missing";
    public static final String MSG_PUSH_NOTIFICATION_TOKEN_NOT_FOUND = "FCM tokens are missing";

    public static final String MSG_MULTIPLE_EXPERTISE_LEVELS_NOT_ALLOWED = "Multiple expertise levels for a program type is not allowed";

    /**
     * Constants related to Fire base dynamic links
     */
    public static final String MSG_DYNAMIC_LINK_INFO_MISSING = "Dynamic Link info missing";
    public static final String MSG_DYNAMIC_LINK_DOMAIN_MISSING = "Domain missing for dynamic link";
    public static final String MSG_DYNAMIC_LINK_MISSING = "Link missing for dynamic link";

    public static final String MSG_TOUR_VIDEO_NOT_FOUND = "Quick tour video not found";

    /**
     * Subscription Package related Messages
     */
    public static final String MSG_SUBSCRIPTION_PACKAGE_ID_NULL = "Subscription Package id missing.";
    public static final String MSG_PACKAGE_ID_INCORRECT = "Package id is incorrect";
    public static final String MSG_SUBSCRIPTION_PACKAGE_NOT_FOUND = "Subscription Package not found.";
    public static final String MSG_SUBSCRIPTION_PACKAGE_SESSION_NOT_FOUND = "Subscription Package session not found.";
    public static final String MSG_SUBSCRIPTION_PACKAGE_TITLE_CHANGE = "Cannot change title for Unpublished/Blocked Subscription Package.";
    public static final String MSG_SUBSCRIPTION_PACKAGE_ACCESS_MEMBERS_NOT_SET = "Clients not selected for Restricted Access for Subscription Package";
    public static final String MSG_SUBSCRIPTION_PACKAGE_NO_CLIENTS = "Instructor does not have any clients. Can not select clients for Restricted Access for Subscription Package";
    public static final String MSG_SUBSCRIPTION_PACKAGE_BLOCKED = "Subscription Package is blocked.";
    public static final String MSG_SUBSCRIPTION_PACKAGE_CANT_UNPUBLISH_NOT_PUBLISHED = "Can not un-publish Subscription Package. It is not published.";
    public static final String MSG_SUBSCRIPTION_PACKAGE_PUBLISHED_CANT_REMOVE_PROGRAM = "Subscription Package is published. Can not remove program from Subscription Package.";
    public static final String MSG_SUBSCRIPTION_PACKAGE_STATUS_NULL = "Subscription Package Status missing.";
    public static final String MSG_SUBSCRIPTION_PACKAGE_NOT_RESTRICTED = "Subscription Package is not restricted.";
    public static final String MSG_CANT_DELETE_SUBSCRIBED_PACKAGE = "Subscription Package has #COUNT# Active Subscription(s). It cannot be deleted.";
    public static final String MSG_RESTRICTED_PACKAGE_SUBSCRIBED = "Subscription Package has #COUNT# Active Subscription(s). It cannot be edited.";
    public static final String MSG_SESSION_PACKAGE_MAPPING_NOT_FOUND = "Subscription Package and session mapping not found.";
    public static final String MSG_PACKAGE_PRICE_MAX_LIMIT_REACHED = "The maximum price allowed for a package is $999999.99. Please enter a price within the given range";

    /**
     * Package configuration error messages
     */
    public static final String MSG_MEETING_NOT_FOUND = "Meeting not found";
    public static final String MSG_MEETING_ORDER_MISSING = "Meeting Order missing";
    public static final String MSG_MEETING_ORDER_CANT_BE_ZERO = "Meeting Order cannot be zero or less";
    public static final String MSG_MEETING_COUNT_CANT_BE_ZERO = "Meeting session count cannot be zero or less";
    public static final String MSG_MEETING_ORDER_INCORRECT = "Meeting Order incorrect";
    public static final String MSG_ERR_PACKAGE_OR_MEETING_REQUIRED = "Package should have at-least a program or a meeting.";
    public static final String MSG_ZOOM_ID_SECRET_EMPTY = "Zoom ID or password can not be empty";

    public static final String MSG_VIDEO_QUALITY_NOT_FOUND= "Video quality not found";
    public static final String MSG_OFFER_MODE_NULL = "Offer mode missing";
    public static final String MSG_OFFER_APPLICABLE_USERS_NULL = "Offer applicable users missing";
    public static final String MSG_OFFER_CODE_DOESNT_EXIST = "Offer Code does not exist";
    public static final String MSG_OFFER_CODE_ID_INVALID = "Invalid Offer Code Id";
    public static final String MSG_USER_OFFER_CODE_ID_INVALID = "Invalid Offer Code Id for the user.";
    public static final String MSG_OFFER_CODE_ID_NULL = "Offer Code Id missing.";
    public static final String MSG_OFFER_NOT_FOUND = "Offer not found";
    public static final String MSG_PRICE_ID_INVALID = "Invalid Price Id";
    public static final String MSG_OFFER_PRICE_ID_NULL = "Offer price is missing";
    public static final String MSG_OFFER_PRICE_NOT_FOUND = "Offer price not found";
    public static final String MSG_OFFER_NOT_FOUND_STRIPE_COUPON_FAILED = "Offer not found. Can not create coupon.";
    public static final String MSG_OFFER_DURATION_MISSING = "Offer duration missing";
    public static final String MSG_OFFER_END_DATE_MISSING = "Offer end date missing";
    public static final String MSG_STRIPE_COUPON_EXISTS_FOR_OFFER = "Coupon already created for Offer. Can not create again.";
    public static final String MSG_OFFER_CODE_NOT_VALID = "Offer Code is not valid";
    public static final String MSG_OFFER_CODE_ALREADY_MAPPED_TO_PROGRAM = "Offer Code already mapped to another program.";
    public static final String MSG_OFFER_CODE_NOT_ACCEPTED_FOR_PROGRAM = "Program does not accept the Offer Code requested";
    public static final String MSG_STRIPE_COUPON_NOT_FOUND_FOR_OFFER = "Coupon not available for requested Offer code";
    public static final String MSG_OFFER_CODE_NOT_APPLICABLE_FOR_USER = "Requested offer code is not applicable for the current user.";
    public static final String MSG_OFFER_CODE_NOT_AVAILABLE = "Offer Code is not available";
    public static final String MSG_OFFER_REACHED_MAX_COUNT_EXISTING_USERS ="You have reached the maximum number of offers for existing users that you can add to this program. To add new offer(s), you can either delete an existing offer or wait until the existing offer(s) expire.";
    public static final String MSG_OFFER_REACHED_MAX_COUNT_NEW_USERS ="You have reached the maximum number of offers for new users that you can add to this program. To add new offer(s), you can either delete an existing offer or wait until the existing offer(s) expire.";
    public static final String MSG_OFFER_SAME_DATES_NEW_USER = "Offer start and end date should not be same for new user offer";

    public static final String MSG_WRONG_AUDIO_TYPE = "The File format that you are trying to upload is not supported, please try uploading in the following formats - wav, MP3.";
    public static final String MSG_STRIPE_COUPON_PRICE_NOT_FOUND = "Price missing. Can not create coupon.";
    public static final String MSG_STRIPE_PAYMENT_ID_MISSING = "Payment method id missing";
    public static final String MSG_STRIPE_PAYMENT_ID_INVALID = "Payment method id is not valid";

    public static final String MSG_VOICE_OVER_ID_INVALID = "Invalid voice over id";

    public static final String MSG_AUDIO_NOT_FOUND = "Audio not found";

    public static final String MSG_VOICE_OVER_TAG_NOT_FOUND = "Voice over tag not found";

    public static final String MSG_VOICE_OVER_TITLE_CANT_BE_EMPTY = "Voice over title can't be empty";

    public static final String MSG_VOICE_OVER_NOT_FOUND = "Voice over not found";

    public static final String MSG_AUDIO_ID_INVALID = "Invalid audio id";

    public static final String MSG_VOICE_OVER_ID_NULL = "Voice over id missing";

    public static final String MSG_CIRCUIT_AND_VOICE_OVER_MAPPING_ID_NULL = "Circuit and voice over mapping id missing";

    public static final String MSG_CIRCUIT_AND_VOICE_OVER_MAPPING_NOT_FOUND = "Circuit and voice over mapping not found";

    public static final String MSG_CIRCUIT_VOICE_OVER_MAPPING_ID_INCORRECT = "Circuit and voice over mapping id is incorrect";

    /**
     * Subscription Package for member related Messages
     */
    public static final String MSG_MEMBER_NO_ACCESS_FOR_PACKAGE = "This user doesn't have access for this subscription package";
    public static final String MSG_MEMBER_PACKAGE_NOT_AVAILABLE = "This subscription package is currently not available, Please come back later.";
    public static final String MSG_BLOCKED_MEMBER_PACKAGE_NOT_AVAILABLE = "You are not allowed to View this subscription package as you are blocked by the Admin. Please contact Support for Further assistance";
    public static final String MSG_MEMBER_NOT_SUBSCRIBED_FOR_PACKAGE = "This user is not subscribed to this subscription package";
    public static final String MSG_GUEST_USER_NO_ACCESS_FOR_PACKAGE = "Guest user doesn't have access for this subscription package";
    public static final String MSG_MEMBER_NOT_SUBSCRIBED_FOR_ANY_PACKAGE = "This user doesn't have any active package subscription";

    public static final String MSG_OFFER_CODE_NOT_ACCEPTED_FOR_PACKAGE = "Subscription Package does not accept the Offer Code requested";
    public static final String MSG_OFFER_CODE_ALREADY_MAPPED_TO_PACKAGE = "Offer Code already mapped to another subscription package.";
    public static final String MSG_PACKAGE_OFFER_REACHED_MAX_COUNT_EXISTING_USERS ="You have reached the maximum number of offers for existing users that you can add to this subscription package. To add new offer(s), you can either delete an existing offer or wait until the existing offer(s) expire.";
    public static final String MSG_PACKAGE_OFFER_REACHED_MAX_COUNT_NEW_USERS ="You have reached the maximum number of offers for new users that you can add to this subscription package. To add new offer(s), you can either delete an existing offer or wait until the existing offer(s) expire.";

    public static final String MSG_LOCATION_NOT_FOUND = "Location not found";
    public static final String MSG_LOCATION_TYPE_NOT_FOUND = "Location type not found";
    public static final String MSG_COUNTRY_NOT_FOUND = "Country not found";
    public static final String MSG_LOCATION_ID_INCORRECT = "Location id incorrect";

    /**
     * Instructor Unavailability related messages
     */
    public static final String MSG_START_TIME_NULL = "Start time missing";
    public static final String MSG_END_TIME_NULL = "END time missing";
    public static final String MSG_INSTRUCTOR_UNAVAILABILITY_ID_INCORRECT = "Invalid instructor unavailability id";
    public static final String MSG_INSTRUCTOR_NOT_ALLOWED_TO_EDIT = "Instructor unavailability doesn't belongs to this user";
    public static final String MSG_PAST_DATE_NOT_ALLOWED = "Past date is not allowed.";
    public static final String MSG_SAME_END_DATE_SAME = "Start and end date shouldn't be same.";
    public static final String MSG_UNAVAILABLE_DUPLICATION = "You already marked this period as unavailable, If you need to extend please edit already marked unavailability";
    public static final String MSG_INSTRUCTOR_UNAVAILABILITY_ID_NULL = "Instructor unavailability id missing";
    public static final String MSG_INVALID_PAYOUT_FILTER_TYPE = "Payout filter type incorrect";
    public static final String MSG_INVALID_PAYOUT_PLATFORM = "Payout platform incorrect";

    public static final String MSG_VIMEO_ID_NULL= "Vimeo id can't be null";

    public static final String MSG_FEATURED_PROGRAM_NOT_FOUND = "Featured programs not found";

    public static final String MSG_EXERCISE_CATEGORY_NOT_FOUND = "Exercise category not found";
    public static final String MSG_EXERCISE_CATEGORY_NULL = "Exercise categories can't be null or empty";
    public static final String MSG_STOCK_EXERCISE_CANNOT_EDITED_BY_INSTRUCTOR = " Stock exercise cannot be edited";

    public static final String MSG_EQUIPMENT_PRESENT = "Equipment already present";
    public static final String MSG_EQUIPMENT_NULL = "Equipment should not be null";
    public static final String MSG_EQUIPMENT_NOT_FOUND = "Equipment not found.";

    public static final String MSG_PROMOTION_NOT_BELONGS_INSTRUCTOR = "Promotion not belongs to current user";

    public static final String MSG_EXPERTISE_LEVEL_ID_INVALID = "Invalid expertise level id";
    
    /**
     * Free Access
     */
    public static final String MSG_FREE_ACCESS_PROGRAM_NOT_FOUND = "Free access program not found";
    public static final String MSG_FREE_ACCESS_PROGRAM_IS_EXITS = "Program already added in free access";
    public static final String MSG_FREE_ACCESS_PROGRAM_IS_REMOVED = "Program already removed in free access";
    public static final String MSG_FREE_ACCESS_MEMBER_ID_LIST_NOT_EMPTY = "Members Id List should not be empty";
    public static final String MSG_FREE_ACCESS_PROGRAM_PACKAGE_ID_LIST_NOT_EMPTY = "Program or Package Id List should not be empty";
    public static final String MSG_FREE_ACCESS_START_DATE_NOT_NULL_EMPTY = "Free Access start date should not be null or empty";
    public static final String MSG_FREE_ACCESS_END_DATE_NOT_NULL_EMPTY = "Free Access end date should not be null or empty";
    public static final String MSG_FREE_ACCESS_END_DATE_AFTER_START_DATE = "Free Access end date should be in after the start date";
    public static final String MSG_FREE_ACCESS_INVALID_FREE_PRODUCT_USER_SPECIFIC_ID = "Invalid free product user specific Id";
    public static final String MSG_FREE_ACCESS_PROGRAM_START_OR_END_DATE_IS_CONFLICT = "Given start date or end date is conflict with existing free program";
    public static final String MSG_FREE_ACCESS_PACKAGE_START_OR_END_DATE_IS_CONFLICT = "Given start date or end date is conflict with existing free package";

    
    // Tier
    public static final String MSG_TIER_ID_NULL = "Tier id missing";
    public static final String MSG_TIER_NOT_FOUND = "Tier not found";
    public static final String MSG_ERR_TIER_NOT_AVAILABLE = "Requested tier not active at this moment.";
    public static final String MSG_FREQUENCY_NOT_FOUND = "Frequency not found";

}