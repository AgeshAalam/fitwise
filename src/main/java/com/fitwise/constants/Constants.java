package com.fitwise.constants;

/**
 * * Application constants.
 */
public class Constants {

    private Constants() {
    }

    /**
     * Error Msg for Response Model.
     */
    public static final String RESPONSE_FAILURE = "Internal Error";

    /**
     * Error Msg for Response Model.
     */
    public static final String RESPONSE_INVALID_DATA = "Invalid Data";

    /**
     * Success response message
     */
    public static final String RESPONSE_SUCCESS = "Success";
    
    public static final String RESPONSE_ERROR = "Error";

    /**
     * Success Status.
     */
    public static final long SUCCESS_STATUS = 2000;

    /**
     * Created Status.
     */
    public static final long CREATED_STATUS = 2001;

    /**
     * Error Status.
     */
    public static final long ERROR_STATUS = 5000;

    /**
     * Error Status.
     */
    public static final long BAD_REQUEST = 4000;

    /**
     * Error Status.
     */
    public static final long EMPTY_RESPONSE_STATUS = 2004;

    /**
     * Success reset content
     */
    public static final long RESET_CONTENT = 2005;

    /**
     * Error Status.
     */
    public static final long CONTENT_NEEDS_TO_BE_VALIDATE = 2040;

    /**
     * Error Status.
     */
    public static final long UNPROCESSABLE_ENTITY = 4022;

    /**
     * Exist Status.
     */
    public static final long NOT_EXIST_STATUS = 5003;

    /**
     * The Constant CONFLICT.
     */
    public static final long CONFLICT = 4009;

    /**
     * The Constant GONE.
     */
    public static final long GONE = 4010;

    /**
     * The Constant PRECONDITION_FAILED.
     */
    public static final long PRECONDITION_FAILED = 4012;

    /**
     * The Constant UNAUTHORIZED.
     */
    public static final long UNAUTHORIZED = 4001;

    /**
     * The Constant FORBIDDEN.
     */
    public static final long FORBIDDEN = 4003;

    /**
     * The Constant NOT_FOUND.
     */
    public static final long NOT_FOUND = 4004;

    public static final long CAN_EDIT = 4090;
    public static final long CAPCHA_FAILURE = 4091;

    /**
     * The Constant UUID.
     */
    public static final String UUID = "uuid";

    /**
     * The Constant X_AUTHORIZATION.
     */
    public static final String X_AUTHORIZATION = "x-authorization";

    /**
     * The Constant ENCRYPTION_KEY.
     */
    public static final String ENCRYPTION_KEY = "${encryption_key}";

    /**
     * The Constant AUTHORITIES.
     */
    public static final String AUTHORITIES = "authorities";

    /**
     * The Constant AUTHORITY.
     */
    public static final String AUTHORITY = "authority";

    /**
     * The Constant PRINCIPAL.
     */
    public static final String PRINCIPAL = "principal";

    /**
     * The Constant OTP_CHECK.
     */
    public static final String KEY_USER_ID = "userId";

    /**
     * The Constant FORCE_UPDATE.
     */
    public static final String FORCE_UPDATE = "Force update";


    /**
     * The Constant FORCE_LOGOUT.
     */
    public static final String FORCE_LOGOUT = "Force logout";

    /**
     * The Constant PRIVACY_POLICY_URL.
     */
    public static final String PRIVACY_POLICY_URL = "Privacy policy url";

    /**
     * The Constant TERMS_AND_CONDITION_URL.
     */
    public static final String TERMS_AND_CONDITION_URL = "Terms and condition url";

    /**
     * The Constant FAQ_URL.
     */
    public static final String FAQ_URL = "Faq url";

    /**
     * The Constant SUPPORT_EMAIL
     */
    public static final String SUPPORT_EMAIL = "SupportEmail";

    /**
     * The Constant INTRO_VIDEO
     */
    public static final String INTRO_VIDEO = "IntroVideo";

    /**
     * The Constant TRANSITION_VIDEO
     */
    public static final String TRANSITION_VIDEO = "TransitionVideo";

    public static final String FITWISE_LAUNCH_DATE = "FitwiseLaunchDate";
    public static final String USER_SIGN_UP_DATE = "signUpDate";

    public static final int LISTENER_BLOCK_OPERATION = 1;
    public static final int LISTENER_UNBLOCK_OPERATION = 2;

    public static final String TIME_FORMAT = "HH:mm:ss";

    /**
     * Info video constants
     */
    public static final String INTRO_VIDEO_VIMEO = "IntroVideoVimeoId";
    public static final String TRANSITION_VIDEO_VIMEO = "TransitionVideoVimeoId";
    
    /**
     * Workout duration calculates for Sets and Reps workouts
     */
    public static final int SET_REST_DURATION_IN_SEC = 60;
    public static final int REPS_REST_DURATION_IN_SEC = 2;



}
