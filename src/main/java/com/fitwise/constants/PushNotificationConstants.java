package com.fitwise.constants;

/*
 * Created by Vignesh G on 20/07/20
 */
public class PushNotificationConstants {

    private PushNotificationConstants() {
    }

    public static final String NEW_MESSAGE_TITLE = "You've got a new Message";

    public static final String NEW_MESSAGE_INSTRUCTOR_MESSAGE = "Your client, #MEMBER_NAME# has just sent you a message. #MESSAGE#";
    public static final String NEW_MESSAGE_MEMBER_MESSAGE = "Your Instructor, #INSTRUCTOR_NAME# has just sent you a message. #MESSAGE#";
    public static final String NEW_MESSAGE_ADMIN_MESSAGE = "Your Admin, #ADMIN_NAME# has just sent you a message. #MESSAGE#";

    public static final String INSTRUCTOR_INACTIVE_TITLE = "Inactive";

    public static final String INSTRUCTOR_3_DAYS_INACTIVE_MESSAGE = "It's been a few days since you've checked your profile. Swipe now to see your latest reports!";
    public static final String INSTRUCTOR_7_DAYS_INACTIVE_MESSAGE = "Don't forget to check in on the business. You may have some clients waiting on you!";

    public static final String MEMBER_INACTIVE_TITLE = "Come Back!";

    public static final String MEMBER_7_DAYS_INACTIVE_MESSAGE = "It’s been a while since we’ve seen you! Let’s change that today and pick back up where we left off!";
    public static final String MEMBER_14_DAYS_INACTIVE_MESSAGE = "We're not giving up on you yet! Swipe now to get right back to where you left off. You can do it!";
    public static final String MEMBER_30_DAYS_INACTIVE_MESSAGE = "Hey remember us? We're the ones that are trying to make you live your best and healthiest life swipe now to get back to it!";

    public static final String WORKOUT_SCHEDULED_MEMBER_TITLE = "Workout Scheduled";
    public static final String WORKOUT_SCHEDULED_MEMBER_MESSAGE = "Get Ready! You have a workout today!";

    public static final String MISSED_WORKOUT_MEMBER_TITLE = "Missed Workout";
    public static final String MISSED_WORKOUT_MEMBER_MESSAGE = "Whoops! Looks like you missed a workout. Don’t worry though, we can get back on track today!";

    public static final String FIRST_CLIENT_SUBSCRIPTION = "First Client!";
    public static final String NEW_SUBSCRIPTION_TITLE = "New Client!";
    public static final String SUBJECT_TRENDING_PROGRAM = "Trending Program!";
    public static final String SUBJECT_100_CLIENTS = "100 Clients!";

    public static final String FIRST_SUBSCRIPTION_FOR_PROGRAM = "You just got your first client for your #PROGRAM_NAME# program! Money is on the way!";
    public static final String NEW_SUBSCRIPTION_FOR_PROGRAM = "Another client has subscribed to your #PROGRAM_NAME#! Money is on the way!";
    public static final String BODY_POPULAR_PROGRAM_IN_10_DAYS = "Your #PROGRAM_NAME# is very popular today! Swipe now to see more details!";
    public static final String HUNDRED_SUBSCRIPTION_FOR_PROGRAM_MESSAGE = "You just hit the 100th client mark for your one program. Congrats! Swipe to see more details!";

    public static final String SOLO_SESSION_BOOKING_TITLE = "New Scheduled Trainnr Session - #DATE_TIME#";
    public static final String SOLO_SESSION_BOOKING_MESSAGE = "#MEMBER_NAME# booked the #SESSION_NAME# session with you on #DATE# at #TIME#. This booked session will be reflected in your linked calendar. You can also view the scheduled session directly on the Trainnr instructor website in “My Calendar”.";

    public static final String RECURRING_SESSION_BOOKING_TITLE = "New Scheduled Trainnr Recurring Session - #DATE_TIME#";
    public static final String RECURRING_SESSION_BOOKING_MESSAGE = "#MEMBER_NAME# booked the #SESSION_NAME# recurring session with you on #DATE# at #TIME#. This booked session will be reflected in your linked calendar. You can also view the scheduled session directly on the Trainnr instructor website in in “My Calendar”.";

    public static final String SESSION_UPDATE_TITLE_INSTRUCTOR = "Rescheduled Trainnr session - #DATE_TIME#";
    public static final String SESSION_UPDATE_MESSAGE_INSTRUCTOR = "#MEMBER_NAME# has rescheduled the #SESSION_NAME# session with you from #OLD_DATE# at #OLD_TIME# to #NEW_DATE# at #NEW_TIME#.";

    public static final String SESSION_CANCEL_TITLE_INSTRUCTOR = "Canceled Trainnr session - #DATE_TIME#";
    public static final String SESSION_CANCEL_MESSAGE_INSTRUCTOR = "#MEMBER_NAME# has canceled the #SESSION_NAME# session with you on #DATE# at #TIME#.";

}