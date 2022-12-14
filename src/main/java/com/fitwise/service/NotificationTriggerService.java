package com.fitwise.service;

import com.fitwise.constants.EmailConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.PushNotificationConstants;
import com.fitwise.constants.SecurityFilterConstants;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.UserActiveInactiveTracker;
import com.fitwise.entity.UserRole;
import com.fitwise.entity.WorkoutCompletion;
import com.fitwise.entity.subscription.ProgramSubscription;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.UserActiveInactiveTrackerRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.repository.UserRoleRepository;
import com.fitwise.repository.member.WorkoutCompletionRepository;
import com.fitwise.repository.subscription.ProgramSubscriptionRepo;
import com.fitwise.service.fcm.PushNotificationAPIService;
import com.fitwise.service.member.MemberProgramService;
import com.fitwise.utils.EmailContentUtil;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.mail.AsyncMailer;
import com.fitwise.utils.mail.AsyncMailerUtil;
import com.fitwise.utils.mail.MailSender;
import com.fitwise.view.fcm.NotificationContent;
import com.fitwise.view.member.TodaysProgramView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/*
 * Created by Vignesh G on 20/07/20
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationTriggerService {

    private static final long FOURTEEN_DAYS_IN_MILLISECONDS = TimeUnit.DAYS.toMillis(14);
    private static final long FIFTEEN_DAYS_IN_MILLISECONDS = TimeUnit.DAYS.toMillis(15);
    private static final long THIRTY_DAYS_IN_MILLISECONDS = TimeUnit.DAYS.toMillis(30);
    private static final long THIRTY_ONE_DAYS_IN_MILLISECONDS = TimeUnit.DAYS.toMillis(31);

    @Autowired
    UserRepository userRepository;
    @Autowired
    ProgramRepository programRepository;
    @Autowired
    UserActiveInactiveTrackerRepository userActiveInactiveTrackerRepository;
    @Autowired
    private MailSender mailSender;
    @Autowired
    public UserRoleRepository userRoleRepository;
    @Autowired
    FitwiseUtils fitwiseUtils;
    @Autowired
    PushNotificationAPIService pushNotificationAPIService;
    @Autowired
    MemberProgramService memberProgramService;
    @Autowired
    WorkoutCompletionRepository workoutCompletionRepository;
    @Autowired
    ProgramSubscriptionRepo programSubscriptionRepo;
    @Autowired
    private EmailContentUtil emailContentUtil;

    private final AsyncMailer asyncMailer;
    /**
     * Invoked notifications
     */
    public void invokeScheduledNotifications() {
        log.info("Scheduled notifications started..");
        triggerScheduledMails();
        triggerScheduledPushNotifications();
        log.info("Scheduled notifications ended..");
    }

    private void triggerScheduledMails() {
        triggerProgramUploadMailsForInstructor();
    }

    /**
     * Below codes are used to send mails if an instructor has not uploaded a program
     */
    private void triggerProgramUploadMailsForInstructor() {
        Date today = new Date();
        UserRole roleInstructor = userRoleRepository.findByName(SecurityFilterConstants.ROLE_INSTRUCTOR);
        List<User> instructors = userRepository.findByUserRoleMappingsUserRole(roleInstructor);
        for (User instructor : instructors) {
            Programs programs = programRepository.findTop1ByOwnerUserIdOrderByModifiedDateDesc(instructor.getUserId());
            if(programs != null){
                long lastProgramPublishedDate = programs.getModifiedDate().getTime();
                long currentTimeStamp = today.getTime();
                // MAIL NOTIFICATION IF ITS BEEN 14 DAYS SINCE PROGRAM UPLOAD
                if (currentTimeStamp - lastProgramPublishedDate >= FOURTEEN_DAYS_IN_MILLISECONDS && currentTimeStamp - lastProgramPublishedDate < FIFTEEN_DAYS_IN_MILLISECONDS) {
                    String subject = EmailConstants.INSTRUCTOR_14_DAYS_SINCE_PROGRAM_UPLOAD_SUBJECT;
                    String mailBody = EmailConstants.INSTRUCTOR_14_DAYS_SINCE_PROGRAM_UPLOAD_CONTENT;
                    String userName = fitwiseUtils.getUserFullName(instructor);
                    mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, mailBody);
                    mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
                    mailSender.sendHtmlReminderMail(instructor.getEmail(), subject, mailBody);
                }
                // MAIL NOTIFICATION IF ITS BEEN 30 DAYS SINCE PROGRAM UPLOAD
                if (currentTimeStamp - lastProgramPublishedDate >= THIRTY_DAYS_IN_MILLISECONDS && currentTimeStamp - lastProgramPublishedDate < THIRTY_ONE_DAYS_IN_MILLISECONDS) {
                    String subject = EmailConstants.INSTRUCTOR_30_DAYS_SINCE_PROGRAM_UPLOAD_SUBJECT;
                    String mailBody = EmailConstants.INSTRUCTOR_30_DAYS_SINCE_PROGRAM_UPLOAD_CONTENT;
                    String userName = fitwiseUtils.getUserFullName(instructor);
                    mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, mailBody);
                    mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
                    mailSender.sendHtmlReminderMail(instructor.getEmail(), subject, mailBody);
                }
            }
        }
    }

    private void triggerScheduledPushNotifications() {
        triggerActivityPushNotificationForInstructor();
        triggerActivityPushNotificationForMember();
        triggerWorkoutReminderPushNotificationForMember();
        triggerWorkoutMissedPushNotificationForMember();
    }

    /**
     * Util to send push notifications to all devices of user
     *
     * @param user
     * @param title
     * @param message
     */
    private void sendPushNotificationsToUser(User user, String title, String message, String role) {
        try {
            NotificationContent notificationContent = new NotificationContent();
            notificationContent.setTitle(title);
            notificationContent.setBody(message);
            pushNotificationAPIService.sendOnlyNotification(notificationContent, user, role);
        } catch (Exception e) {
            log.error("Exception while sending push notification : " + e.getMessage());
        }
    }

    /**
     * Method to trigger scheduled push notification for being Member inactive
     */
    private void triggerActivityPushNotificationForMember() {
        Sort sort = Sort.by("modifiedDate").descending();
        List<UserActiveInactiveTracker> memberList = userActiveInactiveTrackerRepository.findByUserRoleName(KeyConstants.KEY_MEMBER, sort);
        //user id stored in set to not show multiple notifications for the same user.
        Set<Long> memberInstructorIdSet = new HashSet<>();
        //List of members sorted by last app visit time in descending order
        for (UserActiveInactiveTracker memberTracker : memberList) {
            LocalDateTime lastActiveDate = memberTracker.getModifiedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime todayLocalDate = LocalDateTime.now();
            long days = lastActiveDate.until(todayLocalDate, ChronoUnit.DAYS);
            int inactiveDays = Math.toIntExact(days);
            //7 day not active push notification
            if (inactiveDays == 7 && !memberInstructorIdSet.contains(memberTracker.getUser().getUserId())) {
                sendPushNotificationsToUser(memberTracker.getUser(), PushNotificationConstants.MEMBER_INACTIVE_TITLE, PushNotificationConstants.MEMBER_7_DAYS_INACTIVE_MESSAGE, KeyConstants.KEY_MEMBER);
                memberInstructorIdSet.add(memberTracker.getUser().getUserId());
            }
            //14 day not active push notification
            if (inactiveDays == 14 && !memberInstructorIdSet.contains(memberTracker.getUser().getUserId())) {
                sendPushNotificationsToUser(memberTracker.getUser(), PushNotificationConstants.MEMBER_INACTIVE_TITLE, PushNotificationConstants.MEMBER_14_DAYS_INACTIVE_MESSAGE, KeyConstants.KEY_MEMBER);
                memberInstructorIdSet.add(memberTracker.getUser().getUserId());
            }
            //30 day not active push notification
            if (inactiveDays == 30 && !memberInstructorIdSet.contains(memberTracker.getUser().getUserId())) {
                sendPushNotificationsToUser(memberTracker.getUser(), PushNotificationConstants.MEMBER_INACTIVE_TITLE, PushNotificationConstants.MEMBER_30_DAYS_INACTIVE_MESSAGE, KeyConstants.KEY_MEMBER);
                memberInstructorIdSet.add(memberTracker.getUser().getUserId());
            }
        }
    }

    /**
     * Method to trigger scheduled push notification for being Instructor inactive
     */
    private void triggerActivityPushNotificationForInstructor() {
        Sort sort = Sort.by("modifiedDate").descending();
        List<UserActiveInactiveTracker> instructorUserList = userActiveInactiveTrackerRepository.findByUserRoleName(KeyConstants.KEY_INSTRUCTOR, sort);
        //user id stored in set to not show multiple notifications for the same user.
        Set<Long> notifiedInstructorIdSet = new HashSet<>();
        //List of instructors sorted by last app visit time in descending order
        for (UserActiveInactiveTracker instructorTracker : instructorUserList) {
            LocalDateTime lastActiveDate = instructorTracker.getModifiedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime todayLocalDate = LocalDateTime.now();
            long days = lastActiveDate.until(todayLocalDate, ChronoUnit.DAYS);
            int inactiveDays = Math.toIntExact(days);
            //3 day not active push notification
            if (inactiveDays == 3 && !notifiedInstructorIdSet.contains(instructorTracker.getUser().getUserId())) {
                sendPushNotificationsToUser(instructorTracker.getUser(), PushNotificationConstants.INSTRUCTOR_INACTIVE_TITLE, PushNotificationConstants.INSTRUCTOR_3_DAYS_INACTIVE_MESSAGE, KeyConstants.KEY_INSTRUCTOR);
                notifiedInstructorIdSet.add(instructorTracker.getUser().getUserId());
            }
            //7 day not active push notification
            if (inactiveDays == 7 && !notifiedInstructorIdSet.contains(instructorTracker.getUser().getUserId())) {
                sendPushNotificationsToUser(instructorTracker.getUser(), PushNotificationConstants.INSTRUCTOR_INACTIVE_TITLE, PushNotificationConstants.INSTRUCTOR_7_DAYS_INACTIVE_MESSAGE, KeyConstants.KEY_INSTRUCTOR);
                notifiedInstructorIdSet.add(instructorTracker.getUser().getUserId());
            }
        }
    }

    /**
     * Push notification to remind member about Scheduled Workout for the Day
     */
    private void triggerWorkoutReminderPushNotificationForMember() {
        UserRole memberRole = userRoleRepository.findByName(SecurityFilterConstants.ROLE_MEMBER);
        List<User> members = userRepository.findByUserRoleMappingsUserRole(memberRole);
        for (User member : members) {
            try {
                List<TodaysProgramView> todayPrograms = memberProgramService.getTodaysPrograms(member, null);
                if (!todayPrograms.isEmpty()) {
                    sendPushNotificationsToUser(member, PushNotificationConstants.WORKOUT_SCHEDULED_MEMBER_TITLE, PushNotificationConstants.WORKOUT_SCHEDULED_MEMBER_MESSAGE, KeyConstants.KEY_MEMBER);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    /**
     * Push notification to remind member about Missed Workout for the Day
     */
    private void triggerWorkoutMissedPushNotificationForMember() {
        LocalDateTime todayLocalDate = LocalDateTime.now();
        UserRole memberRole = userRoleRepository.findByName(SecurityFilterConstants.ROLE_MEMBER);
        List<User> members = userRepository.findByUserRoleMappingsUserRole(memberRole);
        for (User member : members) {
            boolean memberMissed = false;
            try {
                List<TodaysProgramView> todayPrograms = memberProgramService.getTodaysPrograms(member, null);
                for (TodaysProgramView todaysProgramView : todayPrograms) {
                    List<WorkoutCompletion> workoutCompletionList = workoutCompletionRepository.findByMemberUserIdAndProgramProgramId(member.getUserId(), todaysProgramView.getProgramId());
                    if (!workoutCompletionList.isEmpty()) {
                        WorkoutCompletion workoutCompletion = workoutCompletionList.get(workoutCompletionList.size() - 1);
                        LocalDateTime completedDateTime = workoutCompletion.getCompletedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                        long days = completedDateTime.until(todayLocalDate, ChronoUnit.DAYS);
                        int diffDays = Math.toIntExact(days);
                        if (diffDays > 0) {
                            memberMissed = true;
                        }
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            if (memberMissed) {
                sendPushNotificationsToUser(member, PushNotificationConstants.MISSED_WORKOUT_MEMBER_TITLE, PushNotificationConstants.MISSED_WORKOUT_MEMBER_MESSAGE, KeyConstants.KEY_MEMBER);
            }
        }
    }

    /**
     * Push notification for subscription milestone notification
     *
     * @param programSubscription
     */
    public void invokeSubscriptionPushNotification(ProgramSubscription programSubscription) {
        Long programId = programSubscription.getProgram().getProgramId();
        User instructor = programSubscription.getProgram().getOwner();
        int subscriptionCount = programSubscriptionRepo.countByProgramProgramId(programId);
        String pushContent = PushNotificationConstants.FIRST_SUBSCRIPTION_FOR_PROGRAM.replace(EmailConstants.LITERAL_PROGRAM_NAME, programSubscription.getProgram().getTitle());
        if (subscriptionCount == 1) {
            sendPushNotificationsToUser(instructor, PushNotificationConstants.FIRST_CLIENT_SUBSCRIPTION, pushContent, KeyConstants.KEY_INSTRUCTOR);
            String subject = EmailConstants.SUBJECT_FIRST_CLIENT_SUBSCRIPTION;
            String mailBody = pushContent;
            String userName = fitwiseUtils.getUserFullName(instructor);
            mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
                    .replace(EmailConstants.EMAIL_BODY, mailBody);
            mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
            asyncMailer.sendHtmlMail(instructor.getEmail(), subject, mailBody);
        } else if (subscriptionCount == 100) {
            sendPushNotificationsToUser(instructor, PushNotificationConstants.SUBJECT_100_CLIENTS, PushNotificationConstants.HUNDRED_SUBSCRIPTION_FOR_PROGRAM_MESSAGE, KeyConstants.KEY_INSTRUCTOR);
        } else {
            pushContent = PushNotificationConstants.NEW_SUBSCRIPTION_FOR_PROGRAM.replace(EmailConstants.LITERAL_PROGRAM_NAME, programSubscription.getProgram().getTitle());
            sendPushNotificationsToUser(instructor, PushNotificationConstants.NEW_SUBSCRIPTION_TITLE, pushContent, KeyConstants.KEY_INSTRUCTOR);
        }
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Date yesterday = calendar.getTime();
        int subscriptionCountToday = programSubscriptionRepo.countByProgramProgramIdAndModifiedDateGreaterThan(programId, yesterday);
        if (subscriptionCountToday == 10) {
            sendPushNotificationsToUser(instructor, PushNotificationConstants.SUBJECT_TRENDING_PROGRAM, PushNotificationConstants.BODY_POPULAR_PROGRAM_IN_10_DAYS, KeyConstants.KEY_INSTRUCTOR);
        }
    }

}