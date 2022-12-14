package com.fitwise.constants;

public class CalendarConstants {

    private CalendarConstants() {
    }

    /**
     * Calendar error messages
     */
    public static final String CAL_ERR_USER_NOT_FOUND ="User not found.";
    public static final String CAL_ERR_ACCOUNT_NOT_FOUND = "User is not authenticated with calendar account.";
    public static final String CAL_ERR_CALENDAR_NOT_FOUND = "User is not mapped with any calendar.";
    public static final String CAL_ERR_SCHEDULE_NOT_FOUND = "Requested schedule is not available.";
    public static final String CAL_ERR_SCHEDULE_ID_INVALID = "Requested schedule id is invalid.";
    public static final String CAL_ERR_KLOUDLESS_SCHEDULE_DELETE_FAILED = "Kloudless schedule deletion failed.";
    public static final String CAL_ERR_SCHEDULE_UPDATE_FAILED = "Requested schedule update is failed, Try again.";
    public static final String CAL_ERR_SCHEDULE_DELETE_FAILED = "Requested schedule delete is failed, Try again.";
    public static final String CAL_ERR_MEETING_NOT_FOUND = "Meeting is not available.";
    public static final String CAL_ERR_SUBCRIPTION_PACKAGE_NOT_FOUND = "Subscription package is not available.";

    public static final String CAL_ERR_MEETING_TYPE_NOT_FOUND = "Meeting type is not available.";
    public static final String CAL_ERR_ACCOUNT_LINKING_FAILED = "Account linking failed.";
    public static final String CAL_ERR_CALENDAR_ID_INVALID = "Calendar id is invalid.";
    public static final String CAL_ERR_ACCOUNT_ID_INVALID = "Account id is invalid.";
    public static final String CAL_EER_CALENDAR_LINKING_FAILED = "Calendar linking failed.";
    public static final String CAL_ERR_MEETING_LINKING_FAILED = "Meeting linking failed.";
    public static final String CAL_ERR_CALENDER_CREATE_FAILED = "Calender create failed.";
    

    
    public static final String CAL_ERR_AVAILABILITY_RULES_NOT_FOUND = "Schedule availability time slot is not available.";
    
    public static final String CAL_ERR_REAlTIME_SCHEDULE_LINKING_FAILED = "Realtime schedule linking failed.";
    
    public static final String CAL_ERR_GET_FREE_BUSY_EVENT_FAILED = "Get free busy event failed.";
   
    public static final String CAL_ERR_MEETING_DELETION_FAILED = "Meeting deletion failed.";
    
    public static final String CAL_ERR_ACCOUNT_DELETION_FAILED = "Account deletion failed.";
    
    public static final String CAL_ERR_SCHEDULE_DELETION_FAILED = "Schedule deletion failed.";
    
    public static final String CAL_ERR_UNAVAILABLE_EVENT_FAILED = "Create unavailable event failed.";
    
    public static final String CAL_ERR_MEETING_RUELS_NOT_FOUND = "Meeting rules is not available.";
    public static final String CAL_EER_AVAILABILITY_LINKING_FAILED = "Availability linking failed.";
   
    public static final String CAL_ERR_MEETING_TIMEZONE_INVALID = "Timezone is invalid";
    public static final String CAL_ERR_ACCOUNT_INVALID = "Account is invalid";
    public static final String CAL_ERR_MEETING_INVALID = "Meeting is invalid";
    public static final String CAL_ERR_MEETING_TYPE_INVALID = "Meeting type is invalid";
    public static final String CAL_ERR_SCHEDULE_INSTRUCTOR_NOT_MATCH = "Given schedule id doesn't belongs to this instructor";
    public static final String CAL_ERR_FIND_AVAILABILITY_FAILED = "Find availability failed.";
    public static final String CAL_ERR_FIND_AVAILABILITY_REQUEST_INVALID = "Availability request is invalid.";
    public static final String CAL_ERR_FIND_AVAILABILITY_WINDOW_INVALID = "Availability window is invalid.";
    public static final String CAL_ERR_SCHEDULE_EMPTY = "Schedule information is empty.";
    public static final String CAL_ERR_START_TIME_INVALID = "Start time is invalid.";
    public static final String CAL_ERR_END_TIME_INVALID = "End time is invalid.";
    public static final String CAL_ERR_BOOKING_DATE_INVALID = "Booking date is invalid.";
    public static final String CAL_ERR_START_TIME_EQUALS_END_TIME = "Start time and end time must not be same.";
    public static final String CAL_ERR_START_TIME_AFTER_END_TIME = "Start time must be earlier than end time.";
    public static final String CAL_ERR_NAME_INVALID = "Name is invalid.";
    public static final String CAL_ERR_TIME_ZONE_INVALID = "Time zone is invalid.";
    public static final String CAL_ERR_RRULE_INVALID = "Recurrence rule is invalid.";
    public static final String CAL_ERR_ATTENDEE_EMPTY = "Attendee list is empty.";
    public static final String CAL_ERR_ATTENDEE_NAME_INVALID = "Attendee name is invalid.";
    public static final String CAL_ERR_ATTENDEE_EMAIL_INVALID = "Attendee email is invalid.";
    public static final String CAL_ERR_ACCOUNT_ID_NULL = "Account id missing.";
    public static final String MSG_CAL_ACTIVE_STATUS_CHANGED = "Active account switched.";
    public static final String MSG_ERR_SUBSCRIPTION_CREATION_FAILED = "Kloudless Subscription Creation for primary calendar failed";
    public static final String MSG_ERR_SUBSCRIPTION_DELETE_FAILED = "Kloudless Subscription Deletion for primary calendar failed";
    public static final String MSG_ERR_GET_SUBSCRIPTION_ACTIVITY_FAILED = "Kloudless Subscription get activity failed";
    public static final String MSG_CAL_DELETE_PACKAGE_USER_ACCOUNT = "Requested calendar account used in package scheduling. So can't be deleted.";
    public static final String MSG_CAL_DELETE_CALENDAR_ACCOUNT = "Calendar account deleted successfully.";
    public static final String MSG_ERR_DELETE_ACTIVE_CALENDAR_ACCOUNT = "You can't delete active calendar account.";
    public static final String MSG_ERR_DELETE_FAILED = "Account deletion is failed. Try again later. If persist again contact support.";
    public static final String MSG_CAL_DELETE_USED_MEETING = "Requested schedule availability is used in subscription package. So can't be deleted.";
    public static final String CAL_ERR_SCHEDULE_ID_NULL = "Schedule id missing.";

    /**
     * Calendar success messages
     */
    public static final String CAL_SCS_SCHEDULE_DELETED = "User schedule deleted.";
    public static final String CAL_SCS_SCHEDULE_UPDATED = "User schedule updated.";
    public static final String CAL_SCS_AVAILABILITY = "Calendar availability.";
    public static final String CAL_SCS_SCHEDULE_LIST_DELETED = "User schedules deleted.";

    /**
     * Calendar properties
     */
    public static final String CAL_PROP_IS_FITWISE_EVENT = "isFitwiseEvent";
    public static final String CAL_PROP_FITWISE_SCHEDULE_ID = "fitwiseScheduleId";
    public static final String CAL_PROP_FITWISE_MEETING_ID = "fitwiseMeetingId";
    public static final String CAL_PROP_SESSION_TYPE = "sessionType";
    public static final String CAL_PROP_MEETING_WINDOW_ID = "meetingWindowId";
    public static final String CAL_PROP_ZOOM_MEETING_LINK = "zoomMeetingLink";
    public static final String CAL_PROP_ADDRESS = "address";
    public static final String CAL_PROP_CITY = "city";
    public static final String CAL_PROP_STATE = "state";
    public static final String CAL_PROP_COUNTRY = "country";
    public static final String CAL_PROP_ZIPCODE = "zipcode";
    public static final String CAL_PROP_LANDMARK = "landmark";
    public static final String CAL_PROP_INSTRUCTOR_UNAVAILABILITY_ID = "instructorUnavailabilityId";
    public static final String CAL_PROP_SESSION_NAME = "sessionName";
    public static final String CAL_PROP_SESSION_NAME_PACKAGE = "sessionNameInPackage";
    public static final String CAL_PROP_PACKAGE_NAME = "packageTitle";
    public static final String CAL_PROP_PACKAGE_ID = "packageId";

    /**
     * Zoom integration message constants
     */
    public static final String CAL_SCS_ZOOM_AUTH = "Zoom authorization Url.";
    public static final String CAL_SCS_ZOOM_AUTHORIZED = "Zoom authorization success.";

    public static final String CAL_ERR_ZOOM_AUTH_FAILED = "Zoom account authorization failed.";
    public static final String CAL_ERR_ZOOM_AUTH_CODE_INVALID = "Zoom authorization code is invalid.";
    public static final String CAL_ERR_ZOOM_STATE_INVALID = "Zoom authorization state is invalid.";
    public static final String CAL_ERR_ZOOM_STATE_NOT_FOUND = "Zoom authorization state not found .";
    public static final String CAL_ERR_ZOOM_ACCESS_TOKEN_INVALID = "Zoom access token is invalid.";
    public static final String CAL_ERR_ZOOM_ACCOUNT_NOT_FOUND = "User is not authenticated with zoom account.";
    public static final String CAL_ERR_ZOOM_ACCOUNT_DUPLICATE = "This Zoom account have been added already.";
    public static final String CAL_ERR_ZOOM_GET_USER_FAILED = "Zoom get user details failed.";
    public static final String CAL_ERR_ZOOM_GET_MEETING_INVITATION_FAILED = "Zoom get meeting invitation failed.";
    public static final String CAL_ERR_ZOOM_REFRESH_TOKEN_FAILED = "Zoom account refreshing token failed.";
    public static final String CAL_ERR_ZOOM_MEETING_INVALID = "Zoom meeting is invalid.";
    public static final String CAL_ERR_ZOOM_MEETING_NOT_FOUND = "Zoom meeting not found.";
    public static final String CAL_ERR_ZOOM_MEETING_TITLE_INVALID = "Zoom meeting title is invalid.";
    public static final String CAL_ERR_ZOOM_MEETING_START_INVALID = "Zoom meeting start time is invalid.";
    public static final String CAL_ERR_ZOOM_MEETING_END_INVALID = "Zoom meeting end time is invalid.";
    public static final String CAL_ERR_ZOOM_MEETING_DURATION_INVALID = "Zoom meeting duration is invalid.";
    public static final String CAL_ERR_ZOOM_MEETING_TIMEZONE_INVALID = "Zoom meeting timezone is invalid.";
    public static final String CAL_ERR_ZOOM_MEETING_RECURRENCE_INVALID = "Zoom meeting recurrence is invalid.";
    public static final String CAL_ERR_ZOOM_MEETING_CREATE_FAILED = "Zoom meeting creation failed.";
    public static final String CAL_ERR_ZOOM_MEETING_UPDATE_FAILED = "Zoom meeting update failed.";
    public static final String CAL_ERR_ZOOM_MEETING_DELETE_FAILED = "Zoom meeting delete failed.";
    public static final String CAL_ERR_ZOOM_MEETING_GET_FAILED = "Zoom meeting fetch failed.";
    public static final String CAL_SCS_ZOOM_ACCOUNTS = "Zoom accounts retrieved..";
    public static final String CAL_SCS_ZOOM_ACCOUNTS_EMPTY = "Zoom accounts not available..";
    public static final String MSG_ERR_ZOOM_DELETE_PACKAGE_USER_ACCOUNT = "Requested zoom account used in package scheduling. So can't be deleted.";
    public static final String MSG_SCS_ZOOM_ACCOUNT_DELETE = "Zoom account deleted successfully.";
    public static final String MSG_ERR_DELETE_ACTIVE_ZOOM_ACCOUNT = "You can't delete active zoom account.";
    public static final String CAL_ERR_INSTRUCTOR_MEETING_REQUIRED = "In order to create a Virtual / Virtual or In-Person session, you need to give the meeting url.";
    public static final String CAL_ERR_INSTRUCTOR_INVALID_MEETING_URL = "Given meeting url is not valid. Please enter a valid meeting url.";

    /**
     * Schedule types
     */
    public static final String USER_KLOUDLESS_SCHEDULE_SOLO = "solo";
    public static final String USER_KLOUDLESS_SCHEDULE_RECURRING = "recurring";
    public static final String USER_KLOUDLESS_SCHEDULE_ICLOUD_RECURRING = "icloud_recurring_event";

    /**
     * Session types
     */
    public static final int SESSION_VIRTUAL = 1;
    public static final int SESSION_IN_PERSON = 2;
    public static final int SESSION_IN_PERSON_OR_VIRTUAL = 3;

    /**
     * Meeting types
     */
    public static final Long MEETING_TYPE_VIRTUAL = 1l;

}
