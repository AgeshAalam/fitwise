package com.fitwise.constants;

/*
 * Created by Vignesh G on 18/05/20
 */
public class DBConstants {

    private DBConstants() {
    }

    /**
     * RestActivity Table constants
     */
    public static final String REST = "Rest";
    public static final String OTHERS = "Others";

    /**
     * Program Table constants
     */
    public static final String UNPUBLISH_EDIT = "UnPublish_Edit";
    public static final String PUBLISH_EDIT = "Publish_Edit";
    public static final String PUBLISH = "Publish";
    public static final String BLOCK_EDIT = "Block_Edit";

    /**
     * Exercise, Circuit, Workout Completion Table constants
     */
    public static final String COMPLETED = "Completed";
    public static final String REPEAT = "Repeat";
    public static final String RESET = "Reset";

    /**
     * FlaggedExercisesSummary Table constants
     */
    public static final String KEY_IGNORE = "ignore";
    public static final String KEY_REPORTED = "reported";

    /**
     * Gender Table constants
     */
    public static final String PREFER_NOT_TO_SAY = "Prefer not to say";

    /**
     * PlatformType Table constants
     */
    public static final String ANDROID = "Android";
    public static final String IOS = "iOS";
    public static final String WEB = "Web";

    /**
     * BulkUploadFailure Table constants
     */
    public static final String DUPLICATE_IMAGE_TITLE = "Already mapped Image title";

    /**
     * SubscriptionPackage Table constants
     */
    public static final String CONFIGURE = "Configure";
    public static final String ACCESS_CONTROL = "AccessControl";

    /**
     * SessionType Table constants
     */
    public static final String VIRTUAL_SESSION = "Virtual";
    public static final String INPERSON_SESSION = "In person";
    public static final String INPERSON_OR_VIRTUAL_SESSION = "Both";

    /**
     * ItmsPublish Table constants
     */
    public static final String ITMS_UPDATED = "Updated.";
    public static final String ITMS_AWAITING_CLEAR_FOR_SALE = "Awaiting clear for sale";

    /**
     * InstructorPayment Table constants
     */
    public static final String INSTRUCTOR_PAYMENT_ID = "instructorPaymentId";

    /**
     * Free access types
     */
    public static final String FREE_ACCESS_TYPE_ALL = "All";
    public static final String FREE_ACCESS_TYPE_INDIVIDUAL = "Individual";
    public static final String FREE_ACCESS_DURATION_START = "Start";
    public static final String FREE_ACCESS_DURATION_END = "End";
    public static final String FREE_ACCESS_TYPE_PACKAGE = "Package";
    
    /**
     * Admin Invite Member
     */
    public static final String INVITE_MEMBER_STATUS_PENDING = "Invite sent";
    public static final String INVITE_MEMBER_STATUS_REGISTERED = "Registered";

    public static final String TIER_FREE = "Free Tier";

    public static final String TIER_SILVER = "Silver Tier";

    public static final String TIER_GOLDEN = "Golden Tier";

}
