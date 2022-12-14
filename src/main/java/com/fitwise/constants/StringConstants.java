package com.fitwise.constants;

public class StringConstants {

    private StringConstants(){
    }

    /**
     * Logger string constants
     */
    public static final String LOG_FIELD_VALIDATION = "Validation :";
    public static final String LOG_API_DURATION_TEXT = "API duration : Time taken in millis : ";
    public static final String LOG_OFFER_COUNT_QUERY = "Offer count query(Milliseconds) : ";

    /**
     * Pattern constants
     */
    public static final String PATTERN_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String PATTERN_DATE = "MM/dd/yyyy";
    public static final String PATTERN_DATE_YYYY_MM_DD = "yyyy-MM-dd";

    /**
     * JSON key constants
     */
    public static final String JSON_PROPERTY_KEY_CUSTOM_PROPERTIES = "custom_properties";
    public static final String JSON_PROPERTY_KEY_SERVICE = "service";
    public static final String JSON_PROPERTY_KEY_START = "start";
    public static final String JSON_PROPERTY_KEY_VALUE = "value";
    public static final String JSON_PROPERTY_KEY_START_TIME_ZONE = "start_time_zone";
    public static final String JSON_PROPERTY_KEY_PRIVATE = "private";
    public static final String JSON_PROPERTY_KEY_DESCRIPTION = "description";
    public static final String JSON_PROPERTY_KEY_CURSOR = "cursor";
    public static final String JSON_PROPERTY_KEY_PROGRAM_EXPERTISE_LEVEL = "programExpertiseLevel";
    public static final String JSON_PROPERTY_KEY_PROGRAM_TYPE = "programType";
    public static final String JSON_PROPERTY_KEY_PROGRAM_ID = "programId";

    /**
     * JSON value constants
     */
    public static final String JSON_PROPERTY_VALUE_EVENT = "event";

    /**
     * Recurrence rule value constants
     */
    public static final String RECURRENCE_EXDATE = "EXDATE";

    /**
     * Literals to process strings
     */
    public static final String LITERAL_COUNT = "#COUNT#";
    public static final String EMAIL = "Email";
    public static final String MEMBER_NAME = "Member Name";
    public static final String INVOICE = "Invoice";
}
