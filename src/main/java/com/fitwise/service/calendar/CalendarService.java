package com.fitwise.service.calendar;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitwise.components.UserComponents;
import com.fitwise.constants.CalendarConstants;
import com.fitwise.constants.Constants;
import com.fitwise.constants.EmailConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.PushNotificationConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.TimeSpan;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.calendar.CalendarMeetingType;
import com.fitwise.entity.calendar.CronofyAvailabilityRules;
import com.fitwise.entity.calendar.UserKloudlessAccount;
import com.fitwise.entity.calendar.UserKloudlessCalendar;
import com.fitwise.entity.calendar.UserKloudlessMeeting;
import com.fitwise.entity.calendar.UserKloudlessRecurringScheduleMaster;
import com.fitwise.entity.calendar.UserKloudlessSchedule;
import com.fitwise.entity.calendar.UserKloudlessSubscription;
import com.fitwise.entity.calendar.ZoomAccount;
import com.fitwise.entity.calendar.ZoomMeeting;
import com.fitwise.entity.instructor.Location;
import com.fitwise.entity.packaging.PackageKloudlessMapping;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.subscription.PackageSubscription;
import com.fitwise.entity.subscription.SubscriptionStatus;
import com.fitwise.exception.ApplicationException;
import com.fitwise.model.MemberCalendarFilterModel;
import com.fitwise.model.packaging.CancellationDurationModel;
import com.fitwise.properties.KloudlessProperties;
import com.fitwise.repository.TimeSpanRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.calendar.CalendarMeetingTypeRepository;
import com.fitwise.repository.calendar.CronofyAvailabilityRulesRepository;
import com.fitwise.repository.calendar.UserKloudlessCalendarRepository;
import com.fitwise.repository.calendar.UserKloudlessMeetingRepository;
import com.fitwise.repository.calendar.UserKloudlessRecurringScheduleMasterRepository;
import com.fitwise.repository.calendar.UserKloudlessScheduleRepository;
import com.fitwise.repository.calendar.UserKloudlessSubscriptionRepository;
import com.fitwise.repository.calendar.UserKloudlessTokenRepository;
import com.fitwise.repository.packaging.PackageKloudlessMappingRepository;
import com.fitwise.repository.packaging.SubscriptionPackageRepository;
import com.fitwise.repository.subscription.PackageSubscriptionRepository;
import com.fitwise.request.kloudless.CalendarAvailabilityRequest;
import com.fitwise.request.kloudless.TimeWindow;
import com.fitwise.response.AvailablePackageView;
import com.fitwise.response.MemberScheduleView;
import com.fitwise.response.kloudless.Activity;
import com.fitwise.response.kloudless.Attendee;
import com.fitwise.response.kloudless.CustomProperty;
import com.fitwise.response.kloudless.Email;
import com.fitwise.response.kloudless.Event;
import com.fitwise.response.kloudless.MeetingWindow;
import com.fitwise.response.kloudless.Recurrence;
import com.fitwise.response.kloudless.Reminder;
import com.fitwise.response.kloudless.Subscription;
import com.fitwise.response.kloudless.SubscriptionMonitoredResource;
import com.fitwise.response.kloudless.WebhookNotification;
import com.fitwise.response.packaging.MemberSessionView;
import com.fitwise.response.packaging.ScheduleView;
import com.fitwise.service.InstructorUnavailabilityService;
import com.fitwise.service.SubscriptionService;
import com.fitwise.service.UserService;
import com.fitwise.service.cronofy.CronofyService;
import com.fitwise.service.dynamiclink.DynamicLinkService;
import com.fitwise.service.fcm.PushNotificationAPIService;
import com.fitwise.utils.EmailContentUtil;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.utils.mail.AsyncMailer;
import com.fitwise.view.InstructorUnavailabilityMemberView;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.calendar.AvailabilityTimeWindow;
import com.fitwise.view.calendar.FindAvailabilityRequest;
import com.fitwise.view.calendar.KloudlessCalendarModel;
import com.fitwise.view.calendar.KloudlessCalendarResponseView;
import com.fitwise.view.calendar.KloudlessMeetingListModel;
import com.fitwise.view.calendar.KloudlessMeetingModel;
import com.fitwise.view.calendar.KloudlessScheduleModel;
import com.fitwise.view.calendar.KloudlessTokenModel;
import com.fitwise.view.calendar.ZoomMeetingRequest;
import com.fitwise.view.cronofy.CronofyMeetingModel;
import com.fitwise.view.cronofy.CronofyschedulePayload;
import com.fitwise.view.cronofy.RefreshAccessTokenResponse;
import com.fitwise.view.cronofy.SchedulePayloadcreator;
import com.fitwise.view.cronofy.SchedulePayloadcustomproperties;
import com.fitwise.view.cronofy.SchedulePayloadorganizer;
import com.fitwise.view.fcm.NotificationContent;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.kloudless.models.Resource;
import com.kloudless.models.ResourceList;
import com.kloudless.models.ResponseJson;
import com.kloudless.models.ResponseRaw;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

@Service
@Slf4j
@RequiredArgsConstructor
public class CalendarService {

    public static final String MEETING_INVITE_SEPARATOR = "-----------------\r\n";

    public static final String REMINDER_CUSTOM_PROPERTY_SEPERATOR = ";";

    @Autowired
    private KloudLessService kloudLessService;

    @Autowired
    private KloudlessProperties kloudlessProperties;

    @Autowired
    private UserComponents userComponents;

    @Autowired
    private UserKloudlessTokenRepository userKloudlessTokenRepository;

    @Autowired
    private UserKloudlessCalendarRepository userKloudlessCalendarRepository;

    @Autowired
    private UserKloudlessMeetingRepository userKloudlessMeetingRepository;

    @Autowired
    private UserKloudlessScheduleRepository userKloudlessScheduleRepository;

    @Autowired
    private CalendarMeetingTypeRepository calendarMeetingTypeRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SubscriptionPackageRepository subscriptionPackageRepository;

    @Autowired
    private TimeSpanRepository timeSpanRepository;

    @Autowired
    private FitwiseUtils fitwiseUtils;

    @Autowired
    private ZoomService zoomService;

    @Autowired
    private PackageKloudlessMappingRepository packageKloudlessMappingRepository;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private PackageSubscriptionRepository packageSubscriptionRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private InstructorUnavailabilityService instructorUnavailabilityService;

    @Autowired
    private UserKloudlessSubscriptionRepository userKloudlessSubscriptionRepository;

    @Autowired
    private UserKloudlessRecurringScheduleMasterRepository userKloudlessRecurringScheduleMasterRepository;

    @Autowired
    private EmailContentUtil emailContentUtil;

    @Autowired
    private PushNotificationAPIService pushNotificationAPIService;

    @Autowired
    private DynamicLinkService dynamicLinkService;
    
    @Autowired
	private CronofyAvailabilityRulesRepository cronofyAvailabilityRulesRepository;

    private final AsyncMailer asyncMailer;
    
    @Autowired
	private CronofyService cronofyService;
    
    

    public void validateAndSaveToken(final KloudlessTokenModel token) {
        log.info("Validate and save token starts.");
        long apiStartTimeMillis = new Date().getTime();
        if (ValidationUtils.isEmptyString(token.getBearerToken())
                || ValidationUtils.isEmptyString(token.getAccountEmail())) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_INVALID, null);
        }
        Resource account = kloudLessService.validateBearerToken(token.getBearerToken());
        if (account == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED, null);
        }
        log.info("Basic validation with query to get kloudless account : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        User user = userComponents.getUser();
        log.info("Get user : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        List<UserKloudlessAccount> userKloudlessTokens = userKloudlessTokenRepository.findByUserAndAccountId(user, account.getId());
        log.info("Query to user kloudless token list : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        UserKloudlessAccount userKloudlessToken = null;
        if (userKloudlessTokens.isEmpty()) {
            userKloudlessToken = new UserKloudlessAccount();
            userKloudlessToken.setUser(user);
            userKloudlessToken.setAccountId(account.getId());
            userKloudlessToken.setAccountEmail(token.getAccountEmail());
            if (account.getData().has(StringConstants.JSON_PROPERTY_KEY_SERVICE)
                    && !ValidationUtils.isEmptyString(account.getData().get(StringConstants.JSON_PROPERTY_KEY_SERVICE).getAsString())) {
                userKloudlessToken.setService(account.getData().get(StringConstants.JSON_PROPERTY_KEY_SERVICE).getAsString());
            }
        } else {
            userKloudlessToken = userKloudlessTokens.get(0);
        }
        log.info("Get first or new kloudless account : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        List<UserKloudlessAccount> existingKloudlessAccounts = userKloudlessTokenRepository.findByUser(user);
        log.info("Query to get existing kloudless accounts : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        if (existingKloudlessAccounts.isEmpty()){
            userKloudlessToken.setActive(true);
        }
        profilingEndTimeMillis = new Date().getTime();
        userKloudlessToken.setToken(token.getBearerToken());
        userKloudlessTokenRepository.save(userKloudlessToken);
        log.info("Query to save user kloudless token : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Validate and save token ends.");
    }

    public KloudlessTokenModel getActiveAccount(){
        User user = userComponents.getUser();
        UserKloudlessAccount userKloudlessAccount = getActiveAccount(user);
        KloudlessTokenModel kloudlessTokenModel = new KloudlessTokenModel();
        kloudlessTokenModel.setAccountId(userKloudlessAccount.getAccountId());
        kloudlessTokenModel.setBearerToken(userKloudlessAccount.getToken());
        kloudlessTokenModel.setAccountEmail(userKloudlessAccount.getAccountEmail());
        return kloudlessTokenModel;
    }

    public void validateAndSaveCalendar(final KloudlessCalendarModel calendarModel) {
        log.info("Start save or update");
        long tempTime = new Date().getTime();
        if (ValidationUtils.isEmptyString(calendarModel.getCalendarId())) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_CALENDAR_ID_INVALID, null);
        }
        if (ValidationUtils.isEmptyString(calendarModel.getAccountId())) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_ID_INVALID, null);
        }
        log.info("Field validation : " + (new Date().getTime() - tempTime));
        tempTime = new Date().getTime();
        User user = userComponents.getUser();
        log.info("Get User : " + (new Date().getTime() - tempTime));
        tempTime = new Date().getTime();
        List<UserKloudlessAccount> userKloudlessAccounts = userKloudlessTokenRepository.findByUserAndAccountId(user, calendarModel.getAccountId());
        if(userKloudlessAccounts.isEmpty()){
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_NOT_FOUND, null);
        }
        log.info("Check kloudless account : " + (new Date().getTime() - tempTime));
        tempTime = new Date().getTime();
        UserKloudlessAccount userKloudlessAccount = userKloudlessAccounts.get(0);
        if(userKloudlessAccount == null){
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_NOT_FOUND, null);
        }
        if (ValidationUtils.isEmptyString(userKloudlessAccount.getService())) {
            userKloudlessAccount = kloudLessService.updateServiceInfo(userKloudlessAccount);
        }
        log.info("Update service : " + (new Date().getTime() - tempTime));
        tempTime = new Date().getTime();
        Resource calendar = kloudLessService.getKloudlessCalendar(userKloudlessAccount, calendarModel.getCalendarId());
        log.info("Get calendar resource : " + (new Date().getTime() - tempTime));
        tempTime = new Date().getTime();
        List<UserKloudlessCalendar> userKloudlessCalendars = userKloudlessCalendarRepository.findByUserAndUserKloudlessAccountAndCalendarId(user, userKloudlessAccount, calendarModel.getCalendarId());
        UserKloudlessCalendar userKloudlessCalendar = null;
        if (userKloudlessCalendars.isEmpty()) {
            userKloudlessCalendar = new UserKloudlessCalendar();
            userKloudlessCalendar.setUser(user);
            userKloudlessCalendar.setCalendarId(calendar.getId());
            if(calendarModel.getCalendarName() != null){
                userKloudlessCalendar.setCalendarName(calendarModel.getCalendarName());
            }
            userKloudlessCalendar.setPrimaryCalendar(true);
            userKloudlessCalendar.setUserKloudlessAccount(userKloudlessAccount);
        } else {
            userKloudlessCalendar = userKloudlessCalendars.get(0);
            userKloudlessCalendar.setPrimaryCalendar(true);
        }
        userKloudlessCalendarRepository.save(userKloudlessCalendar);
        log.info("Create or update active calendar : " + (new Date().getTime() - tempTime));
        tempTime = new Date().getTime();
        List<UserKloudlessCalendar> existingCalendars = userKloudlessCalendarRepository.findByUserAndUserKloudlessAccount(user, userKloudlessAccount);
        for (UserKloudlessCalendar existingCalendar : existingCalendars) {
            if (userKloudlessCalendar.getUserKloudlessCalendarId().equals(existingCalendar.getUserKloudlessCalendarId())) {
                continue;
            }
            existingCalendar.setPrimaryCalendar(false);
            userKloudlessCalendarRepository.save(existingCalendar);
        }
        log.info("Disable all other calendars as not primary : " + (new Date().getTime() - tempTime));
        /*
         * Updating Subscription for the current account
         * */
        updateKloudlessSubscription(userKloudlessCalendar, userKloudlessAccount);
    }

    /**
     * @param userKloudlessCalendar
     * @param userKloudlessAccount
     */
    private void updateKloudlessSubscription(UserKloudlessCalendar userKloudlessCalendar, UserKloudlessAccount userKloudlessAccount) {
        long tempTime = new Date().getTime();
        ResourceList accountSubscriptions = kloudLessService.getAccountSubscriptions(userKloudlessAccount);
        if (accountSubscriptions == null) {
            log.error("get kloudless subscriptions returned null for account id " + userKloudlessAccount.getAccountId());
            return;
        }
        log.info("Get active subscription : " + (new Date().getTime() - tempTime));
        if (accountSubscriptions.getResources().isEmpty()) {
            createKloudlessSubscription(userKloudlessCalendar, userKloudlessAccount);
            return;
        }

        Gson gson = new Gson();
        tempTime = new Date().getTime();
        for (Resource resource : accountSubscriptions.getResources()) {
            Subscription subscription = gson.fromJson(resource.getData(), Subscription.class);
            deleteKloudlessSubscription(userKloudlessAccount, subscription);
        }
        log.info("Delete all subscription : " + (new Date().getTime() - tempTime));
        createKloudlessSubscription(userKloudlessCalendar, userKloudlessAccount);
    }

    /**
     * @param userKloudlessAccount
     */
    private void deleteKloudlessSubscription(UserKloudlessAccount userKloudlessAccount, Subscription subscription) {
        kloudLessService.deleteSubscription(userKloudlessAccount, String.valueOf(subscription.getId()));

        Optional<UserKloudlessSubscription> optionalSubscription = userKloudlessSubscriptionRepository
                .findByUserKloudlessAccountAndSubscriptionId(userKloudlessAccount, subscription.getId());
        if (!optionalSubscription.isPresent()) {
            return;
        }

        userKloudlessSubscriptionRepository.delete(optionalSubscription.get());
        log.info("deleted kloudless subscription for account id " + userKloudlessAccount.getAccountId());
    }

    /**
     * @param userKloudlessCalendar
     * @param userKloudlessAccount
     */
    private UserKloudlessSubscription createKloudlessSubscription(UserKloudlessCalendar userKloudlessCalendar, UserKloudlessAccount userKloudlessAccount) {
        long tempTime = new Date().getTime();
        Subscription subscription = new Subscription();
        subscription.setActive(true);

        SubscriptionMonitoredResource subscriptionMonitoredResource = new SubscriptionMonitoredResource();
        subscriptionMonitoredResource.setResource(userKloudlessCalendar.getCalendarId());

        List<SubscriptionMonitoredResource> subscriptionMonitoredResourceList = new ArrayList<>();
        subscriptionMonitoredResourceList.add(subscriptionMonitoredResource);

        subscription.setMonitoredResources(subscriptionMonitoredResourceList);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> content = objectMapper.convertValue(subscription, new TypeReference<Map<String, Object>>() {});

        Resource resource = kloudLessService.createSubscription(userKloudlessAccount, content);
        if (resource == null) {
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.MSG_ERR_SUBSCRIPTION_CREATION_FAILED, null);
        }
        Gson gson = new Gson();
        Subscription createdSubscription = gson.fromJson(resource.getData(), Subscription.class);

        UserKloudlessSubscription userKloudlessSubscription = new UserKloudlessSubscription();
        userKloudlessSubscription.setSubscriptionId(createdSubscription.getId());
        userKloudlessSubscription.setCalendarId(userKloudlessCalendar.getCalendarId());
        userKloudlessSubscription.setUserKloudlessAccount(userKloudlessAccount);

        userKloudlessSubscriptionRepository.save(userKloudlessSubscription);
        log.info("Update kloudless subs in fw : " + (new Date().getTime() - tempTime));
        log.info("created kloudless subscription for account id " + userKloudlessAccount.getAccountId());
        return userKloudlessSubscription;
    }

    public List<KloudlessCalendarModel> getMyCalendars(){
        User user = userComponents.getUser();
        UserKloudlessAccount userKloudlessAccount = getActiveAccount(user);
        List<UserKloudlessCalendar> existingCalendars = userKloudlessCalendarRepository.findByUserAndUserKloudlessAccount(user, userKloudlessAccount);
        if(existingCalendars.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, "", null);
        }
        List<KloudlessCalendarModel> kloudlessCalendarModels = new ArrayList<>();
        for(UserKloudlessCalendar userKloudlessCalendar : existingCalendars){
            KloudlessCalendarModel kloudlessCalendarModel = new KloudlessCalendarModel();
            kloudlessCalendarModel.setCalendarId(userKloudlessCalendar.getCalendarId());
            kloudlessCalendarModel.setDefaultCalendar(userKloudlessCalendar.getPrimaryCalendar());
            if(userKloudlessCalendar.getCalendarName() != null){
                kloudlessCalendarModel.setCalendarName(userKloudlessCalendar.getCalendarName());
            }
            kloudlessCalendarModels.add(kloudlessCalendarModel);
        }
        return kloudlessCalendarModels;
    }

    public KloudlessMeetingModel createFitwiseMeeting(KloudlessMeetingModel meetingModel) {
        long startTime = new Date().getTime();
        log.info("Start create fitwise meeting");
        long temp = new Date().getTime();
        if (meetingModel.getMeetingTypeId() <= 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_MEETING_TYPE_INVALID, null);
        }
        log.info("Field validation : " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        User user = userComponents.getUser();
        log.info("Get User : " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        UserKloudlessAccount userKloudlessAccount = getActiveAccount(user);
        log.info("Get and update Active account : " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        UserKloudlessCalendar activeCalendar = getActiveCalendarFromKloudlessAccount(userKloudlessAccount);
        if(activeCalendar == null){
            throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_CALENDAR_NOT_FOUND, null);
        }
        log.info("Get active kloudless calendar : " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        CalendarMeetingType calendarMeetingType = calendarMeetingTypeRepository.findByMeetingTypeId(meetingModel.getMeetingTypeId());
        if(calendarMeetingType == null){
            throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_MEETING_TYPE_NOT_FOUND, null);
        }
        log.info("Get cal types : " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        UserKloudlessMeeting userKloudlessMeeting = new UserKloudlessMeeting();
        if(meetingModel.getFitwiseMeetingId() != null){
            Optional<UserKloudlessMeeting> optionalMeeting = userKloudlessMeetingRepository
                    .findByUserKloudlessCalendarAndUserKloudlessMeetingId(activeCalendar, meetingModel.getFitwiseMeetingId());
            if (optionalMeeting.isPresent()) {
                userKloudlessMeeting = optionalMeeting.get();
            }
        }
        userKloudlessMeeting.setUserKloudlessCalendar(activeCalendar);
        userKloudlessMeeting.setCalendarMeetingType(calendarMeetingType);
        userKloudlessMeeting.setUser(user);
        userKloudlessMeetingRepository.save(userKloudlessMeeting);
        log.info("Create/update kloudless meeting : " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        KloudlessMeetingModel kloudlessMeetingModel = new KloudlessMeetingModel();
        kloudlessMeetingModel.setFitwiseMeetingId(userKloudlessMeeting.getUserKloudlessMeetingId());
        kloudlessMeetingModel.setCalendarId(activeCalendar.getCalendarId());
        kloudlessMeetingModel.setMeetingId(userKloudlessMeeting.getMeetingId());
        kloudlessMeetingModel.setMeetingTypeId(calendarMeetingType.getMeetingTypeId());
        kloudlessMeetingModel.setMeetingType(calendarMeetingType.getMeetingType());
        log.info("Response construction : " + (new Date().getTime() - temp));
        log.info("Create fitwise meeting completed : " + (new Date().getTime() - startTime));
        return kloudlessMeetingModel;
    }

    public KloudlessMeetingModel updateMeeting(KloudlessMeetingModel meetingModel) {
        log.info("Update meeting starts.");
        long apiStartTimeMillis = new Date().getTime();
        if (ValidationUtils.isEmptyString(meetingModel.getName())
                || meetingModel.getMeetingTypeId() <= 0
                || meetingModel.getSessionDuration() < 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_MEETING_INVALID, null);
        }
        if (ValidationUtils.isEmptyString(meetingModel.getTimeZone()) || !fitwiseUtils.isValidTimeZone(meetingModel.getTimeZone())) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_MEETING_TIMEZONE_INVALID, null);
        }
        log.info(StringConstants.LOG_FIELD_VALIDATION + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        User user = userComponents.getUser();
        UserKloudlessAccount account = getActiveAccount(user);
        UserKloudlessCalendar activeCalendar = getActiveCalendarFromKloudlessAccount(account);
        if(activeCalendar == null){
            throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_CALENDAR_NOT_FOUND, null);
        }
        CalendarMeetingType calendarMeetingType = calendarMeetingTypeRepository.findByMeetingTypeId(meetingModel.getMeetingTypeId());
        if(calendarMeetingType == null){
            throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_MEETING_TYPE_NOT_FOUND, null);
        }
        log.info("Query to get user and calendar meeting type : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        Optional<UserKloudlessMeeting> optionalMeeting = userKloudlessMeetingRepository
                .findByUserKloudlessCalendarAndUserKloudlessMeetingId(activeCalendar, meetingModel.getFitwiseMeetingId());
        if (!optionalMeeting.isPresent()) {
            throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_MEETING_NOT_FOUND, null);
        }
        log.info("Query to get user kloudless meeting : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        UserKloudlessMeeting userKloudlessMeeting = optionalMeeting.get();
        MeetingWindow meetingWindow = null;
        if (!ValidationUtils.isEmptyString(meetingModel.getMeetingId())) {
            meetingWindow = kloudLessService.getMeetingWindow(account.getToken(), meetingModel.getMeetingId());
            if (!verifyMeetingWindow(meetingWindow, String.valueOf(meetingModel.getFitwiseMeetingId()))) {
                log.info("kloudless meeting window is not linked with fitwise meeting");
                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_MEETING_NOT_FOUND, null);
            }
        }
        log.info("Get meeting window : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if (userKloudlessMeeting.getStartDate() == null && userKloudlessMeeting.getEndDate() == null) {
            LocalDate currentDate = LocalDate.now();
            Date startDate = Date.from(currentDate
                    .atStartOfDay(ZoneId.of(meetingModel.getTimeZone()))
                    .toInstant());
            Date endDate = Date.from(currentDate
                    .plusDays(meetingModel.getSessionDuration())
                    .atStartOfDay(ZoneId.of(meetingModel.getTimeZone()))
                    .toInstant());
            userKloudlessMeeting.setStartDate(startDate);
            userKloudlessMeeting.setEndDate(endDate);
            userKloudlessMeeting.setTimeZone(meetingModel.getTimeZone());
            userKloudlessMeeting.setStartDateInUtc(Date.from(startDate.toInstant().atZone(ZoneId.systemDefault()).toInstant()));
            userKloudlessMeeting.setEndDateInUtc(Date.from(endDate.toInstant().atZone(ZoneId.systemDefault()).toInstant()));
        }
        userKloudlessMeeting.setCalendarMeetingType(calendarMeetingType);
        userKloudlessMeeting.setMeetingId(meetingModel.getMeetingId());
        userKloudlessMeeting.setName(meetingModel.getName());
        TimeSpan timeSpan = new TimeSpan();
        timeSpan.setMinutes(meetingModel.getDurationInMinutes());
        timeSpanRepository.save(timeSpan);
        userKloudlessMeeting.setDuration(timeSpan);
        userKloudlessMeeting.setUser(user);
        userKloudlessMeeting.setMeetingDurationInDays(meetingModel.getSessionDuration());
        log.info("Query to save time span and construct user klkoudless meeting : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        Gson gson = new Gson();
        if (meetingWindow != null) {
            userKloudlessMeeting.setMeetingWindow(gson.toJson(meetingWindow));
        }
        userKloudlessMeetingRepository.save(userKloudlessMeeting);
        log.info("Query to save user kloudless meeting : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        KloudlessMeetingModel kloudlessMeetingModel = new KloudlessMeetingModel();
        kloudlessMeetingModel.setFitwiseMeetingId(userKloudlessMeeting.getUserKloudlessMeetingId());
        kloudlessMeetingModel.setCalendarId(activeCalendar.getCalendarId());
        kloudlessMeetingModel.setMeetingId(userKloudlessMeeting.getMeetingId());
        kloudlessMeetingModel.setMeetingTypeId(calendarMeetingType.getMeetingTypeId());
        kloudlessMeetingModel.setMeetingType(calendarMeetingType.getMeetingType());
        kloudlessMeetingModel.setName(userKloudlessMeeting.getName());
        kloudlessMeetingModel.setSessionDuration(userKloudlessMeeting.getMeetingDurationInDays());
        kloudlessMeetingModel.setDurationInMinutes(userKloudlessMeeting.getDuration().getMinutes());
        kloudlessMeetingModel.setTimeZone(userKloudlessMeeting.getTimeZone());
        if (!ValidationUtils.isEmptyString(userKloudlessMeeting.getMeetingWindow())) {
            kloudlessMeetingModel.setMeetingWindow(gson.fromJson(userKloudlessMeeting.getMeetingWindow(), Map.class));
        }
        log.info("Response construction : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Update meeting ends.");
        return kloudlessMeetingModel;
    }

    public void deleteMeeting(final Long fitwiseMeetingId) {
        User user = userComponents.getUser();
        UserKloudlessAccount account = getActiveAccount(user);
        UserKloudlessCalendar activeCalendar = getActiveCalendarFromKloudlessAccount(account);
        if(activeCalendar == null){
            throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_CALENDAR_NOT_FOUND, null);
        }
        Optional<UserKloudlessMeeting> optionalMeeting = userKloudlessMeetingRepository
                .findByUserKloudlessCalendarAndUserKloudlessMeetingId(activeCalendar, fitwiseMeetingId);
        UserKloudlessMeeting userKloudlessMeeting = null;
        if (!optionalMeeting.isPresent()) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_MEETING_NOT_FOUND, null);
        }
        userKloudlessMeeting = optionalMeeting.get();

        // if meeting used in any subscription package prevent deletion
        List<PackageKloudlessMapping> packageKloudlessMappings = packageKloudlessMappingRepository.findByUserKloudlessMeeting(userKloudlessMeeting);
        if(packageKloudlessMappings != null && !packageKloudlessMappings.isEmpty()){
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.MSG_CAL_DELETE_USED_MEETING, null);
        }

        if (!ValidationUtils.isEmptyString(userKloudlessMeeting.getMeetingId())) {
            try {
                kloudLessService.deleteMeetingWindow(account.getToken(), userKloudlessMeeting.getMeetingId());
            } catch (ApplicationException ae) {
                log.info("kloudless meeting window deletion failed");
            }
        }
        userKloudlessMeetingRepository.delete(userKloudlessMeeting);
    }

    public KloudlessMeetingListModel getMeetings(final int pageNo, final int pageSize, Optional<Long> meetingType) {
        User user = userComponents.getUser();
        UserKloudlessAccount userKloudlessAccount = getActiveAccount(user);
       
        UserKloudlessCalendar userKloudlessCalendar = getActiveCalendarFromKloudlessAccount(userKloudlessAccount);
       
        if(userKloudlessCalendar == null){
            throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_CALENDAR_NOT_FOUND, null);
        }
       
        PageRequest pageRequest = PageRequest.of(pageNo <= 0 ? 0 : pageNo - 1, pageSize);
       
        Page<UserKloudlessMeeting> userKloudlessMeetings;
      
        if (meetingType.isPresent() && meetingType.get() > 0) {
            CalendarMeetingType calendarMeetingType = calendarMeetingTypeRepository.findByMeetingTypeId(meetingType.get());
            if(calendarMeetingType == null){
                throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_MEETING_TYPE_NOT_FOUND, null);
            }
            userKloudlessMeetings = userKloudlessMeetingRepository
                    .findByMeetingIdNotNullAndUserKloudlessCalendarAndCalendarMeetingType(userKloudlessCalendar, calendarMeetingType, pageRequest);
          } else {
            userKloudlessMeetings = userKloudlessMeetingRepository
                    .findByMeetingIdNotNullAndUserKloudlessCalendar(userKloudlessCalendar, pageRequest);
          }
          if(userKloudlessMeetings.isEmpty()){
             throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, "", null);
          }
         Gson gson = new Gson();

         List<KloudlessMeetingModel> kloudlessMeetingModels = new ArrayList<>();
       
         for(UserKloudlessMeeting userKloudlessMeeting : userKloudlessMeetings){
          
        	KloudlessMeetingModel kloudlessMeetingModel = new KloudlessMeetingModel();
            List<PackageKloudlessMapping> packageKloudlessMappings = packageKloudlessMappingRepository.findByUserKloudlessMeeting(userKloudlessMeeting);
          
            if(!packageKloudlessMappings.isEmpty()){
                kloudlessMeetingModel.setUsedInPackage(true);
            }
           
            kloudlessMeetingModel.setFitwiseMeetingId(userKloudlessMeeting.getUserKloudlessMeetingId());
            kloudlessMeetingModel.setCalendarId(userKloudlessCalendar.getCalendarId());
            kloudlessMeetingModel.setMeetingId(userKloudlessMeeting.getMeetingId());
            kloudlessMeetingModel.setMeetingTypeId(userKloudlessMeeting.getCalendarMeetingType().getMeetingTypeId());
            kloudlessMeetingModel.setMeetingType(userKloudlessMeeting.getCalendarMeetingType().getMeetingType());
            kloudlessMeetingModel.setName(userKloudlessMeeting.getName());
            kloudlessMeetingModel.setSessionDuration(userKloudlessMeeting.getMeetingDurationInDays());
            if(userKloudlessMeeting.getDuration() != null){
                kloudlessMeetingModel.setDurationInMinutes(userKloudlessMeeting.getDuration().getMinutes());
            }
            kloudlessMeetingModel.setTimeZone(userKloudlessMeeting.getTimeZone());
            if (!ValidationUtils.isEmptyString(userKloudlessMeeting.getMeetingWindow())) {
                kloudlessMeetingModel.setMeetingWindow(gson.fromJson(userKloudlessMeeting.getMeetingWindow(), Map.class));
            }
            kloudlessMeetingModels.add(kloudlessMeetingModel);
        }
          KloudlessMeetingListModel meetingsList = new KloudlessMeetingListModel();
          meetingsList.setMeetings(kloudlessMeetingModels);
          meetingsList.setTotalCount(userKloudlessMeetings.getTotalElements());
          return meetingsList;
    }

    public KloudlessScheduleModel createOrUpdateUserSchedule(final KloudlessScheduleModel kloudlessScheduleModel) throws ParseException {
        User user = userComponents.getUser();
        SubscriptionPackage subscriptionPackage;
        if(kloudlessScheduleModel.getSubscriptionPackageId() == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_ID_NULL, null);
        }else{
            subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageId(kloudlessScheduleModel.getSubscriptionPackageId());
        }
        if(subscriptionPackage == null){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_ID_NULL, null);
        }
        UserKloudlessAccount userKloudlessAccount = getActiveAccount(subscriptionPackage.getOwner());
        if(userKloudlessAccount == null){
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_NOT_FOUND, null);
        }
        UserKloudlessCalendar userKloudlessCalendar = getActiveCalendarFromKloudlessAccount(userKloudlessAccount);
        if (ValidationUtils.isEmptyString(userKloudlessAccount.getService())) {
            kloudLessService.updateServiceInfo(userKloudlessAccount);
        }
        Resource resource = kloudLessService.getKloudlessSchedule(userKloudlessCalendar, kloudlessScheduleModel.getScheduleId());
        if(resource == null){
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_SCHEDULE_NOT_FOUND, null);
        }
        UserKloudlessSchedule userKloudlessSchedule = null;
        if(kloudlessScheduleModel.getFitwiseScheduleId() != null && kloudlessScheduleModel.getFitwiseScheduleId() > 0){
            userKloudlessSchedule = userKloudlessScheduleRepository.findByUserAndUserKloudlessScheduleId(user, kloudlessScheduleModel.getFitwiseScheduleId());
        }
        boolean newSchedule = false;
        if(userKloudlessSchedule == null){
            userKloudlessSchedule = new UserKloudlessSchedule();
            newSchedule = true;
        }else{
            Date date = new Date();
            JSONObject jsonObject = new JSONObject(userKloudlessSchedule.getSchedulePayload());
            String startTimeString = jsonObject.getString(StringConstants.JSON_PROPERTY_KEY_START);
            SimpleDateFormat dateFormat = new SimpleDateFormat(StringConstants.PATTERN_DATE_TIME);
            Date startTime = dateFormat.parse(startTimeString);
            TimeSpan timeSpan = userKloudlessSchedule.getSubscriptionPackage().getCancellationDuration();
            if(timeSpan.getDays() != null){
                long differenceInDays = fitwiseUtils.getNumberOfDaysBetweenTwoDates(date,startTime);
                if(differenceInDays < timeSpan.getDays()){
                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_UPDATE_SCHEDULE_NOT_ALLOWED, null);
                }
            }else if(timeSpan.getHours() != null){
                long differenceInHours = fitwiseUtils.getNumberOfHoursBetweenTwoDates(date,startTime);
                if(differenceInHours < timeSpan.getHours()){
                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_UPDATE_SCHEDULE_NOT_ALLOWED, null);
                }
            }
        }
        Long fitwiseScheduleIdFromKloudless = null;
        if(resource.getData().get("custom_properties") != null){
            JsonElement customProperties = resource.getData().get(StringConstants.JSON_PROPERTY_KEY_CUSTOM_PROPERTIES);
            if (customProperties instanceof JsonArray) {
                for(JsonElement property : (JsonArray) customProperties){
                    String key = property.getAsJsonObject().get("key").getAsString();
                    if(key.equalsIgnoreCase(CalendarConstants.CAL_PROP_FITWISE_SCHEDULE_ID)){
                        fitwiseScheduleIdFromKloudless = property.getAsJsonObject().get(StringConstants.JSON_PROPERTY_KEY_VALUE).getAsLong();
                    }
                }
            }
        }
        Optional<UserKloudlessMeeting> userKloudlessMeetings = userKloudlessMeetingRepository.findByUserKloudlessCalendarAndUserKloudlessMeetingId(userKloudlessCalendar, kloudlessScheduleModel.getFitwiseMeetingId());
        if(!userKloudlessMeetings.isPresent()){
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_MEETING_NOT_FOUND, null);
        }
        if(userKloudlessMeetings.get().getCalendarMeetingType().getMeetingTypeId() == CalendarConstants.SESSION_IN_PERSON_OR_VIRTUAL)
        {
            CalendarMeetingType calendarMeetingType;
            if(kloudlessScheduleModel.getFitwiseMeetingTypeId() == null || kloudlessScheduleModel.getFitwiseMeetingTypeId() == 0){
                calendarMeetingType = calendarMeetingTypeRepository.findByMeetingTypeId(1L);
                kloudlessScheduleModel.setFitwiseMeetingTypeId(1L);
            }else {
                calendarMeetingType = calendarMeetingTypeRepository.findByMeetingTypeId(kloudlessScheduleModel.getFitwiseMeetingTypeId());
            }
            userKloudlessSchedule.setMeetingTypeId(calendarMeetingType);
        }
        userKloudlessSchedule.setSubscriptionPackage(subscriptionPackage);
        userKloudlessSchedule.setUser(user);
        userKloudlessSchedule.setUserKloudlessMeeting(userKloudlessMeetings.get());
        userKloudlessSchedule.setScheduleId(resource.getId());
        userKloudlessSchedule.setUser(user);
        Gson gson = new Gson();
        userKloudlessSchedule.setSchedulePayload(gson.toJson(kloudlessScheduleModel.getSchedulePayload()));
        userKloudlessSchedule.setBookingDate(kloudlessScheduleModel.getBookingDate());
        userKloudlessSchedule = userKloudlessScheduleRepository.save(userKloudlessSchedule);
        if(fitwiseScheduleIdFromKloudless == null){
            resource = kloudLessService.updateScheduleProp(userKloudlessSchedule.getUserKloudlessScheduleId(), userKloudlessCalendar, resource.getId());
            if(resource == null){
                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_SCHEDULE_UPDATE_FAILED, null);
            }
        }
        kloudlessScheduleModel.setFitwiseScheduleId(userKloudlessSchedule.getUserKloudlessScheduleId());
        if ((userKloudlessMeetings.get().getCalendarMeetingType().getMeetingTypeId() == CalendarConstants.SESSION_VIRTUAL
                || userKloudlessMeetings.get().getCalendarMeetingType().getMeetingTypeId() == CalendarConstants.SESSION_IN_PERSON_OR_VIRTUAL) && newSchedule) {
            try {
                Optional<PackageKloudlessMapping> optionalPackageSession = packageKloudlessMappingRepository
                        .findBySessionMappingId(kloudlessScheduleModel.getPackageSessionMappingId());
                if (optionalPackageSession.isPresent() && StringUtils.isEmpty(optionalPackageSession.get().getMeetingUrl())) {
                    userKloudlessSchedule.setOnlineMeetingEntryUrl(optionalPackageSession.get().getMeetingUrl());
                    userKloudlessScheduleRepository.save(userKloudlessSchedule);
                }
            } catch (ApplicationException ae) {
                log.error("zoom meeting creation failed for schedule: " + userKloudlessSchedule.getUserKloudlessScheduleId(), ae);
            }
        }
        return kloudlessScheduleModel;
    }

    public List<ZoomMeeting> createZoomMeetingFromEvent(User user, Resource event) {
        if (!event.getData().get("type").getAsString().equals(StringConstants.JSON_PROPERTY_VALUE_EVENT)) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_SCHEDULE_NOT_FOUND, null);
        }
        ZoomMeetingRequest zoomMeetingRequest = new ZoomMeetingRequest();
        zoomMeetingRequest.setTitle(event.getData().get("name").getAsString());
        OffsetDateTime startDateTime;
        OffsetDateTime endDateTime;
        // expected date time format is 2011-12-03T10:15:30+01:00
        try {
            startDateTime = OffsetDateTime.parse(event.getData().get(StringConstants.JSON_PROPERTY_KEY_START).getAsString());
        } catch (DateTimeParseException dpe) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_START_INVALID, null);
        }
        // expected date time format is 2011-12-03T10:15:30+01:00
        try {
            endDateTime = OffsetDateTime.parse(event.getData().get("end").getAsString());
        } catch (DateTimeParseException dpe) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_END_INVALID, null);
        }
        long duration = Duration.between(startDateTime, endDateTime).toMinutes();
        zoomMeetingRequest.setStartTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(startDateTime));
        zoomMeetingRequest.setDuration((int) duration);
        zoomMeetingRequest.setTimeZone(event.getData().get(StringConstants.JSON_PROPERTY_KEY_START_TIME_ZONE).getAsString());
        return zoomService.createMeetingForUser(user, zoomMeetingRequest);
    }

    public Resource updateEventWithZoomMeeting(UserKloudlessCalendar calendar, Resource event, ZoomMeeting zoomMeeting) {
        if (event == null || !event.getData().get("type").getAsString().equals(StringConstants.JSON_PROPERTY_VALUE_EVENT)) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_SCHEDULE_NOT_FOUND, null);
        }
        if (calendar == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_CALENDAR_NOT_FOUND, null);
        }
        if (zoomMeeting == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_INVALID, null);
        }
        String meetingInvitation = zoomService.getMeetingInvitation(zoomMeeting.getZoomAccount(), Long.parseLong(zoomMeeting.getMeetingId()));
        StringBuilder builder = new StringBuilder();
        String description = "";
        if (event.getData().get(StringConstants.JSON_PROPERTY_KEY_DESCRIPTION) != null
                && !event.getData().get(StringConstants.JSON_PROPERTY_KEY_DESCRIPTION).isJsonNull()
                && !ValidationUtils.isEmptyString(event.getData().get(StringConstants.JSON_PROPERTY_KEY_DESCRIPTION).getAsString())) {
            description = event.getData().get(StringConstants.JSON_PROPERTY_KEY_DESCRIPTION).getAsString();
        }
        if (!StringUtils.isEmpty(description) && description.contains(MEETING_INVITE_SEPARATOR)) {
            String eventDescription = description.split(MEETING_INVITE_SEPARATOR)[0];
            builder.append(eventDescription);
        }
        if (!StringUtils.isEmpty(meetingInvitation)) {
            builder.append(MEETING_INVITE_SEPARATOR);
            builder.append(meetingInvitation);
            builder.append(MEETING_INVITE_SEPARATOR);
        }
        Map<String, Object> updateContent = new HashMap<>();
        updateContent.put("location", zoomMeeting.getJoinUrl());
        updateContent.put(StringConstants.JSON_PROPERTY_KEY_DESCRIPTION, builder.toString());
        return kloudLessService.updateKloudlessSchedule(calendar, event.getId(), updateContent);
    }

    public ZoomMeeting updateZoomMeetingFromEvent(User user, String meetingId, Resource event) {
        if (ValidationUtils.isEmptyString(meetingId)) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_SCHEDULE_NOT_FOUND, null);
        }
        if (!event.getData().get("type").getAsString().equals(StringConstants.JSON_PROPERTY_VALUE_EVENT)) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_SCHEDULE_NOT_FOUND, null);
        }
        ZoomMeetingRequest zoomMeetingRequest = new ZoomMeetingRequest();
        zoomMeetingRequest.setTitle(event.getData().get("name").getAsString());
        OffsetDateTime startDateTime;
        OffsetDateTime endDateTime;
        // expected date time format is 2011-12-03T10:15:30+01:00
        try {
            startDateTime = OffsetDateTime.parse(event.getData().get(StringConstants.JSON_PROPERTY_KEY_START).getAsString());
        } catch (DateTimeParseException dpe) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_START_INVALID, null);
        }
        // expected date time format is 2011-12-03T10:15:30+01:00
        try {
            endDateTime = OffsetDateTime.parse(event.getData().get("end").getAsString());
        } catch (DateTimeParseException dpe) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_END_INVALID, null);
        }
        long duration = Duration.between(startDateTime, endDateTime).toMinutes();
        zoomMeetingRequest.setStartTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(startDateTime));
        zoomMeetingRequest.setDuration((int) duration);
        zoomMeetingRequest.setTimeZone(event.getData().get(StringConstants.JSON_PROPERTY_KEY_START_TIME_ZONE).getAsString());
        return zoomService.updateMeetingForUser(user, meetingId, null, zoomMeetingRequest);
    }

    public void deleteUserSchedule(final Long fitwiseScheduleId) throws ParseException {
        User user = userComponents.getUser();
        UserKloudlessSchedule userKloudlessSchedule = userKloudlessScheduleRepository.findByUserAndUserKloudlessScheduleId(user, fitwiseScheduleId);
        if(userKloudlessSchedule == null){
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_SCHEDULE_NOT_FOUND, null);
        }
        Date date = new Date();
        TimeSpan timeSpan = userKloudlessSchedule.getSubscriptionPackage().getCancellationDuration();
        JSONObject jsonObject = new JSONObject(userKloudlessSchedule.getSchedulePayload());
        String startTimeString = jsonObject.getString(StringConstants.JSON_PROPERTY_KEY_START);
        SimpleDateFormat dateFormat = new SimpleDateFormat(StringConstants.PATTERN_DATE_TIME);
        Date startTime = dateFormat.parse(startTimeString);
        if(timeSpan.getDays() != null){
            long differenceInDays = fitwiseUtils.getNumberOfDaysBetweenTwoDates(date,startTime);
            if(differenceInDays < timeSpan.getDays()){
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_DELETE_SCHEDULE_NOT_ALLOWED, null);
            }
        }else if(timeSpan.getHours() != null){
            long differenceInHours = fitwiseUtils.getNumberOfHoursBetweenTwoDates(date,startTime);
            if(differenceInHours < timeSpan.getHours()){
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_DELETE_SCHEDULE_NOT_ALLOWED, null);
            }
        }
        ResponseRaw response = kloudLessService.deleteKloudlessSchedule(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar(), userKloudlessSchedule.getScheduleId());
        if(response.getData().getStatusLine().getStatusCode() != 204 && response.getData().getStatusLine().getStatusCode() != 200){
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_KLOUDLESS_SCHEDULE_DELETE_FAILED, null);
        }
        userKloudlessScheduleRepository.delete(userKloudlessSchedule);
    }

    public KloudlessScheduleModel createOrUpdateSchedule(final KloudlessScheduleModel kloudlessScheduleModel) {
        if (kloudlessScheduleModel.getFitwiseScheduleId() == null || kloudlessScheduleModel.getFitwiseScheduleId() <= 0) {
            return createSchedule(kloudlessScheduleModel);
        }
        return updateSchedule(kloudlessScheduleModel);
    }

    public KloudlessScheduleModel createSchedule(KloudlessScheduleModel kloudlessScheduleModel) {
        log.info("Create schedule starts.");
        long apiStartTimeMillis = new Date().getTime();
        ValidationUtils.throwException(kloudlessScheduleModel.getSchedulePayload() == null,
                CalendarConstants.CAL_ERR_SCHEDULE_EMPTY, Constants.BAD_REQUEST);
        ObjectMapper objectMapper = new ObjectMapper();
        Event event = objectMapper.convertValue(kloudlessScheduleModel.getSchedulePayload(), Event.class);
        ValidationUtils.throwException(ValidationUtils.isEmptyString(event.getName()),
                CalendarConstants.CAL_ERR_NAME_INVALID, Constants.BAD_REQUEST);
        ValidationUtils.throwException(ValidationUtils.isEmptyString(event.getStart()),
                CalendarConstants.CAL_ERR_START_TIME_INVALID, Constants.BAD_REQUEST);
        ValidationUtils.throwException(ValidationUtils.isEmptyString(event.getEnd()),
                CalendarConstants.CAL_ERR_END_TIME_INVALID, Constants.BAD_REQUEST);
        ValidationUtils.throwException(ValidationUtils.isEmptyString(event.getStartTimeZone()),
                CalendarConstants.CAL_ERR_TIME_ZONE_INVALID, Constants.BAD_REQUEST);
        ValidationUtils.throwException(!fitwiseUtils.isValidTimeZone(event.getStartTimeZone()),
                CalendarConstants.CAL_ERR_TIME_ZONE_INVALID, Constants.BAD_REQUEST);
        if (event.getRecurrence() != null) {
            for (Recurrence recurrence : event.getRecurrence()) {
                ValidationUtils.throwException(ValidationUtils.isEmptyString(recurrence.getRrule()),
                        CalendarConstants.CAL_ERR_RRULE_INVALID, Constants.BAD_REQUEST);
            }
        }
        ValidationUtils.throwException(event.getAttendees() == null || event.getAttendees().isEmpty(),
                CalendarConstants.CAL_ERR_ATTENDEE_EMPTY, Constants.BAD_REQUEST);
        for (Attendee attendee : event.getAttendees()) {
            ValidationUtils.throwException(ValidationUtils.isEmptyString(attendee.getName()),
                    CalendarConstants.CAL_ERR_ATTENDEE_NAME_INVALID, Constants.BAD_REQUEST);
            ValidationUtils.throwException(
                    ValidationUtils.isEmptyString(attendee.getEmail()) || !ValidationUtils.emailRegexValidate(attendee.getEmail()),
                    CalendarConstants.CAL_ERR_ATTENDEE_EMAIL_INVALID, Constants.BAD_REQUEST);
        }
        LocalDateTime startDateTime;
        // expected date time format is '2011-12-03T10:15:30'
        try {
            startDateTime = LocalDateTime.parse(event.getStart());
        } catch (DateTimeParseException dpe) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_START_TIME_INVALID, null);
        }
        LocalDateTime endDateTime;
        // expected date time format is '2011-12-03T10:15:30'
        try {
            endDateTime = LocalDateTime.parse(event.getEnd());
        } catch (DateTimeParseException dpe) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_END_TIME_INVALID, null);
        }
        if (startDateTime.compareTo(endDateTime) == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_START_TIME_EQUALS_END_TIME, null);
        }
        if (startDateTime.isAfter(endDateTime)) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_START_TIME_AFTER_END_TIME, null);
        }
        ValidationUtils.throwException(kloudlessScheduleModel.getBookingDate() == null,
                CalendarConstants.CAL_ERR_BOOKING_DATE_INVALID, Constants.BAD_REQUEST);
        User user = userComponents.getUser();
        ValidationUtils.throwException(kloudlessScheduleModel.getSubscriptionPackageId() == null,
                ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_ID_NULL, Constants.BAD_REQUEST);
        log.info("Basic validations with getting user : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        SubscriptionPackage subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageId(kloudlessScheduleModel.getSubscriptionPackageId());
        ValidationUtils.throwException(subscriptionPackage == null,
                ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_ID_NULL, Constants.BAD_REQUEST);
        log.info("Query to get subscription package : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        Optional<PackageKloudlessMapping> optionalPackageSession = packageKloudlessMappingRepository
                .findBySessionMappingId(kloudlessScheduleModel.getPackageSessionMappingId());
        if(!optionalPackageSession.isPresent() || optionalPackageSession.get() == null){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_SESSION_NOT_FOUND, null);
        }
        log.info("Query to get package session : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        UserKloudlessMeeting userKloudlessMeeting = optionalPackageSession.get().getUserKloudlessMeeting();
        UserKloudlessCalendar userKloudlessCalendar = userKloudlessMeeting.getUserKloudlessCalendar();
        UserKloudlessAccount userKloudlessAccount = userKloudlessCalendar.getUserKloudlessAccount();
        ValidationUtils.throwException(userKloudlessAccount == null,
                CalendarConstants.CAL_ERR_ACCOUNT_NOT_FOUND, Constants.BAD_REQUEST);
        Optional<UserKloudlessMeeting> userKloudlessMeetings = userKloudlessMeetingRepository
                .findByUserKloudlessCalendarAndUserKloudlessMeetingId(userKloudlessCalendar, kloudlessScheduleModel.getFitwiseMeetingId());
        if(!userKloudlessMeetings.isPresent() || userKloudlessMeetings.get() == null){
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_MEETING_NOT_FOUND, null);
        }
        log.info("Query to get user kloudless meeting : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        CalendarMeetingType calendarMeetingType;
        if (kloudlessScheduleModel.getFitwiseMeetingTypeId() == null || kloudlessScheduleModel.getFitwiseMeetingTypeId() == 0) {
            // get session type from meeting
            calendarMeetingType = userKloudlessMeetings.get().getCalendarMeetingType();
        } else {
            // find the passed session type
            calendarMeetingType = calendarMeetingTypeRepository.findByMeetingTypeId(kloudlessScheduleModel.getFitwiseMeetingTypeId());
            ValidationUtils.throwException(calendarMeetingType == null,
                    CalendarConstants.CAL_ERR_MEETING_TYPE_NOT_FOUND, Constants.BAD_REQUEST);
        }
        log.info("Query to get calendar meeting type : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        // if session type is "In-Person or Virtual" choose virtual session
        if (CalendarConstants.SESSION_IN_PERSON_OR_VIRTUAL == calendarMeetingType.getMeetingTypeId()) {
            calendarMeetingType = calendarMeetingTypeRepository
                    .findByMeetingTypeId(CalendarConstants.MEETING_TYPE_VIRTUAL);
        }
        log.info("Query to get calendar meeting type : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        kloudlessScheduleModel.setFitwiseMeetingTypeId(calendarMeetingType.getMeetingTypeId());
        String meetingUrl = null;
        if(optionalPackageSession.isPresent() && optionalPackageSession.get() != null){
            if(CalendarConstants.SESSION_VIRTUAL == calendarMeetingType.getMeetingTypeId()){
                StringBuilder builder = new StringBuilder();
                if (!ValidationUtils.isEmptyString(event.getDescription())) {
                    builder.append(event.getDescription());
                }
                meetingUrl = optionalPackageSession.get().getMeetingUrl();
                if (!StringUtils.isEmpty(meetingUrl)) {
                    builder.append(MEETING_INVITE_SEPARATOR);
                        builder.append(meetingUrl);
                        builder.append(MEETING_INVITE_SEPARATOR);
                    }
                    event.setLocation(meetingUrl);
                    event.setDescription(builder.toString());
            } else if(CalendarConstants.SESSION_IN_PERSON == calendarMeetingType.getMeetingTypeId()){
                Location location = optionalPackageSession.get().getLocation();
                if (location != null) {
                    String locationText = buildLocationText(location);
                    if (!ValidationUtils.isEmptyString(locationText)) {
                        event.setLocation(locationText);
                    }
                }
            }
        }
        log.info("Query to get zoom account : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        log.info("Set location : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        Gson gson = new Gson();
        Map<String, Object> content = objectMapper.convertValue(event, new TypeReference<Map<String, Object>>() {});
        if (ValidationUtils.isEmptyString(userKloudlessAccount.getService())) {
            userKloudlessAccount = kloudLessService.updateServiceInfo(userKloudlessAccount);
        }
        log.info("Get kloudless account : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        List<UserKloudlessSchedule> createdSchedules = new ArrayList<>();
        boolean isiCloudRecurringEvent = (userKloudlessAccount.getService().equalsIgnoreCase(KloudLessService.SERVICE_ICLOUD_CALENDAR)
                && event.getRecurrence() != null
                && !event.getRecurrence().isEmpty());
        // for iCloud recurring schedules we're breaking it creating single event because of kloudless limitation
        if (isiCloudRecurringEvent) {
            String rRule = null;
            String exRule = null;
            List<ZonedDateTime> exceptionDates = new ArrayList<>();
            for (Recurrence recurrence : event.getRecurrence()) {
                if (!ValidationUtils.isEmptyString(recurrence.getRrule())
                        && recurrence.getRrule().contains("RRULE")
                        && ValidationUtils.isEmptyString(rRule)) {
                    rRule = recurrence.getRrule();
                }
                if (!ValidationUtils.isEmptyString(recurrence.getRrule())
                        && recurrence.getRrule().contains(StringConstants.RECURRENCE_EXDATE)
                        && ValidationUtils.isEmptyString(exRule)) {
                    exRule = recurrence.getRrule();
                }
            }
            if (!ValidationUtils.isEmptyString(rRule)) {
                RecurrenceRule recurrenceRule;
                try {
                    recurrenceRule = new RecurrenceRule(rRule.split("RRULE:")[1]);
                } catch (InvalidRecurrenceRuleException e) {
                    log.info("create schedule failed: invalid recurrence rule " + rRule);
                    throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_RRULE_INVALID, null);
                }
                if (!ValidationUtils.isEmptyString(exRule)) {
                    List<ZonedDateTime> dateTimeList = zoomService.getExceptionDateTimes(exRule);
                    if (dateTimeList != null && !dateTimeList.isEmpty()) {
                        exceptionDates.addAll(dateTimeList);
                    }
                }

                UserKloudlessRecurringScheduleMaster recurringMaster = new UserKloudlessRecurringScheduleMaster();
                recurringMaster.setStart(Date.from(startDateTime.atZone(ZoneId.of(event.getStartTimeZone())).toInstant()));
                recurringMaster.setEnd(Date.from(endDateTime.atZone(ZoneId.of(event.getStartTimeZone())).toInstant()));
                recurringMaster.setTimeZone(event.getStartTimeZone());
                recurringMaster.setRrule(rRule);
                if (!ValidationUtils.isEmptyString(exRule)) {
                    recurringMaster.setExceptionDates(exRule);
                }
                userKloudlessRecurringScheduleMasterRepository.save(recurringMaster);
                DateTime startDate = new DateTime(TimeZone.getTimeZone(event.getStartTimeZone()),
                        startDateTime.atZone(ZoneId.of(event.getStartTimeZone())).toEpochSecond()*1000);
                Duration duration = Duration.between(startDateTime, endDateTime);
                RecurrenceRuleIterator recurrences = recurrenceRule.iterator(startDate);
                int maxInstances = 100;
                while (recurrences.hasNext() && (!recurrenceRule.isInfinite() || maxInstances-- > 0)) {
                    DateTime nextInstance = recurrences.nextDateTime();
                    ZonedDateTime start = Instant.ofEpochMilli(nextInstance.getTimestamp())
                            .atZone(ZoneId.of(event.getStartTimeZone()));
                    if (!exceptionDates.isEmpty() && exceptionDates.contains(start)) {
                        continue;
                    }
                    DateTime endTime = nextInstance.addDuration(org.dmfs.rfc5545.Duration.parse(duration.toString()));
                    ZonedDateTime end = Instant.ofEpochMilli(endTime.getTimestamp())
                            .atZone(ZoneId.of(event.getStartTimeZone()));
                    Event recurringInstance = new Event();
                    recurringInstance.setName(event.getName());
                    recurringInstance.setDescription(event.getDescription());
                    recurringInstance.setLocation(event.getLocation());
                    recurringInstance.setStart(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(start));
                    recurringInstance.setEnd(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(end));
                    recurringInstance.setStartTimeZone(event.getStartTimeZone());
                    recurringInstance.setAttendees(event.getAttendees());
                    Map<String, Object> instanceContent = objectMapper
                            .convertValue(recurringInstance, new TypeReference<Map<String, Object>>() {});
                    Resource createdInstance = kloudLessService.createKloudlessSchedule(userKloudlessCalendar, instanceContent);
                    if (createdInstance != null) {
                        String dateTime = createdInstance.getData().get(StringConstants.JSON_PROPERTY_KEY_START).getAsString();
                        Instant startTime = OffsetDateTime.parse(dateTime).toZonedDateTime()
                                .with(ChronoField.HOUR_OF_DAY, 0)
                                .with(ChronoField.MINUTE_OF_DAY, 0)
                                .with(ChronoField.SECOND_OF_DAY, 0)
                                .withZoneSameInstant(ZoneOffset.UTC).toInstant();
                        UserKloudlessSchedule schedule = constructUserKloudlessSchedule(createdInstance,
                                Date.from(startTime),
                                CalendarConstants.USER_KLOUDLESS_SCHEDULE_ICLOUD_RECURRING);
                        schedule.setUserKloudlessRecurringScheduleMaster(recurringMaster);
                        if (CalendarConstants.SESSION_VIRTUAL == calendarMeetingType.getMeetingTypeId() && !StringUtils.isEmpty(meetingUrl)) {
                            schedule.setOnlineMeetingEntryUrl(meetingUrl);
                        }
                        createdSchedules.add(schedule);
                    }
                }
            }
        } else {
            // for outlook recurring event with exception, we're create a regular recurring event and then deleting exceptions
            boolean isOutlookRecurringScheduleWithException = false;
            List<ZonedDateTime> exceptions = new ArrayList<>();
            if (KloudLessService.SERVICE_OUTLOOK_CALENDAR.equalsIgnoreCase(userKloudlessAccount.getService())
                    && event.getRecurrence() != null
                    && !event.getRecurrence().isEmpty()) {
                String exRule = null;
                for (Recurrence recurrence : event.getRecurrence()) {
                    if (!ValidationUtils.isEmptyString(recurrence.getRrule())
                            && recurrence.getRrule().contains(StringConstants.RECURRENCE_EXDATE)
                            && ValidationUtils.isEmptyString(exRule)) {
                        isOutlookRecurringScheduleWithException = true;
                        exRule = recurrence.getRrule();
                    }
                }
                List<Recurrence> updatedRecurrenceList = new ArrayList<>();
                if (isOutlookRecurringScheduleWithException && !ValidationUtils.isEmptyString(exRule)) {
                    for (Recurrence recurrence : event.getRecurrence()) {
                        if (recurrence.getRrule().contains(StringConstants.RECURRENCE_EXDATE)) {
                            continue;
                        }
                        updatedRecurrenceList.add(recurrence);
                    }
                    // get exception date times
                    List<ZonedDateTime> dateTimeList = zoomService.getExceptionDateTimes(exRule);
                    if (dateTimeList != null && !dateTimeList.isEmpty()) {
                        exceptions.addAll(dateTimeList);
                    }
                    // set recurrences without EXDATE and update payload
                    if (!updatedRecurrenceList.isEmpty()) {
                        event.setRecurrence(updatedRecurrenceList);
                        content = objectMapper.convertValue(event, new TypeReference<Map<String, Object>>() {});
                    }
                }
            }
            Resource createdEvent = kloudLessService.createKloudlessSchedule(userKloudlessCalendar, content);
            if ("recurring_master".equals(createdEvent.getData().get("recurrence_type").getAsString())) {
                // recurring event
                ResourceList eventList = kloudLessService.getKloudlessScheduleInstances(userKloudlessCalendar, createdEvent.getId());
                Iterator<Resource> resourceIterator = eventList.getPageIterator();
                while (resourceIterator.hasNext()) {
                    Resource instance = resourceIterator.next();
                    String start = instance.getData().get(StringConstants.JSON_PROPERTY_KEY_START).getAsString();
                    ZonedDateTime instanceStartTime = OffsetDateTime.parse(start).toZonedDateTime();
                    // if it's outlook recurring event with exception, delete the exception
                    if (isOutlookRecurringScheduleWithException && !exceptions.isEmpty()) {
                        boolean isException = false;
                        for (ZonedDateTime dateTime : exceptions) {
                            isException = (dateTime.toInstant().compareTo(instanceStartTime.toInstant()) == 0);
                        }
                        if (isException) {
                            kloudLessService.deleteKloudlessSchedule(userKloudlessCalendar, instance.getId());
                            continue;
                        }
                    }
                    Instant bookingDate = instanceStartTime
                            .with(ChronoField.HOUR_OF_DAY, 0)
                            .with(ChronoField.MINUTE_OF_DAY, 0)
                            .with(ChronoField.SECOND_OF_DAY, 0)
                            .withZoneSameInstant(ZoneOffset.UTC).toInstant();
                    UserKloudlessSchedule schedule = constructUserKloudlessSchedule(instance,
                            Date.from(bookingDate),
                            CalendarConstants.USER_KLOUDLESS_SCHEDULE_RECURRING);
                    schedule.setMasterScheduleId(createdEvent.getId());
                    if (CalendarConstants.SESSION_VIRTUAL == calendarMeetingType.getMeetingTypeId() && !StringUtils.isEmpty(meetingUrl)) {
                        schedule.setOnlineMeetingEntryUrl(meetingUrl);
                    }
                    createdSchedules.add(schedule);
                }
            } else {
                // single event
                Date bookingDate = kloudlessScheduleModel.getBookingDate();
                String start = createdEvent.getData().get(StringConstants.JSON_PROPERTY_KEY_START).getAsString();
                Instant startTime = OffsetDateTime.parse(start).toZonedDateTime()
                        .with(ChronoField.HOUR_OF_DAY, 0)
                        .with(ChronoField.MINUTE_OF_DAY, 0)
                        .with(ChronoField.SECOND_OF_DAY, 0)
                        .withZoneSameInstant(ZoneOffset.UTC).toInstant();
                if (startTime.compareTo(kloudlessScheduleModel.getBookingDate().toInstant()) != 0) {
                    bookingDate = Date.from(startTime);
                    log.info("create schedule booking date mismatch: replacing booking date with schedule start date");
                }
                UserKloudlessSchedule schedule = constructUserKloudlessSchedule(createdEvent,
                        bookingDate,
                        CalendarConstants.USER_KLOUDLESS_SCHEDULE_SOLO);
                if (CalendarConstants.SESSION_VIRTUAL == calendarMeetingType.getMeetingTypeId() && !StringUtils.isEmpty(meetingUrl)) {
                    schedule.setOnlineMeetingEntryUrl(meetingUrl);
                }
                createdSchedules.add(schedule);
            }
        }
        log.info("for iCloud recurring schedules we're breaking it creating single event because of kloudless limitation : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        // set common properties
        for (UserKloudlessSchedule userKloudlessSchedule : createdSchedules) {
            userKloudlessSchedule.setUserKloudlessMeeting(userKloudlessMeetings.get());
            userKloudlessSchedule.setUser(user);
            userKloudlessSchedule.setSubscriptionPackage(subscriptionPackage);
            if(optionalPackageSession.isPresent() && optionalPackageSession.get() != null){
                userKloudlessSchedule.setPackageKloudlessMapping(optionalPackageSession.get());
            }
            userKloudlessSchedule.setMeetingTypeId(calendarMeetingType);
        }
        log.info("Set common properties : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        userKloudlessScheduleRepository.saveAll(createdSchedules);
        log.info("Query to save user kloudless schedule : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        try {
            UserKloudlessSchedule schedule = createdSchedules.get(0);
            User instructor = schedule.getUserKloudlessMeeting().getUser();
            String instructorName = fitwiseUtils.getUserFullName(instructor);
            String memberName = fitwiseUtils.getUserFullName(user);
            String scheduleTime = buildScheduleTimeString(schedule);
            String[] scheduleTimes = scheduleTime.split(" ");
            try {
                String calendar = dynamicLinkService.constructCalendarLinkForInstructor();
                String subject;
                String mailBody;
                if(schedule.getScheduleType().equalsIgnoreCase(CalendarConstants.USER_KLOUDLESS_SCHEDULE_SOLO)){
                    subject = EmailConstants.SOLO_SESSION_BOOKING_SUBJECT.replace(EmailConstants.LITERAL_DATE_TIME, scheduleTime);
                    mailBody = EmailConstants.SOLO_SESSION_BOOKING_CONTENT.replace(EmailConstants.LITERAL_MEMBER_NAME, memberName).replace(EmailConstants.LITERAL_SESSION_NAME, schedule.getPackageKloudlessMapping().getTitle()).replace(EmailConstants.LITERAL_DATE, scheduleTimes[0]).replace(EmailConstants.LITERAL_TIME, scheduleTimes[1]).replace(EmailConstants.LITERAL_APP_URL, calendar);
                }else{
                    subject = EmailConstants.RECURRING_SESSION_BOOKING_SUBJECT.replace(EmailConstants.LITERAL_DATE_TIME, scheduleTime);
                    mailBody = EmailConstants.RECURRING_SESSION_BOOKING_CONTENT.replace(EmailConstants.LITERAL_MEMBER_NAME, memberName).replace(EmailConstants.LITERAL_SESSION_NAME, schedule.getPackageKloudlessMapping().getTitle()).replace(EmailConstants.LITERAL_DATE, scheduleTimes[0]).replace(EmailConstants.LITERAL_TIME, scheduleTimes[1] ).replace(EmailConstants.LITERAL_APP_URL, calendar);
                }
                mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + instructorName + ",").replace(EmailConstants.LITERAL_MAIL_BODY, mailBody);
                mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
                asyncMailer.sendHtmlMail(instructor.getEmail(), subject, mailBody);
            } catch (Exception e) {
                log.info("Mail sending failed for session booking by member :" + e.getMessage());
            }
            try {
                NotificationContent notificationContent = new NotificationContent();
                if(schedule.getScheduleType().equalsIgnoreCase(CalendarConstants.USER_KLOUDLESS_SCHEDULE_SOLO)){
                    notificationContent.setTitle(PushNotificationConstants.SOLO_SESSION_BOOKING_TITLE.replace(EmailConstants.LITERAL_DATE_TIME, scheduleTime));
                    notificationContent.setBody(PushNotificationConstants.SOLO_SESSION_BOOKING_MESSAGE.replace(EmailConstants.LITERAL_MEMBER_NAME, memberName).replace(EmailConstants.LITERAL_SESSION_NAME, schedule.getPackageKloudlessMapping().getTitle()).replace(EmailConstants.LITERAL_DATE, scheduleTimes[0]).replace(EmailConstants.LITERAL_TIME, scheduleTimes[1]));
                }else{
                    notificationContent.setTitle(PushNotificationConstants.RECURRING_SESSION_BOOKING_TITLE.replace(EmailConstants.LITERAL_DATE_TIME, scheduleTime));
                    notificationContent.setBody(PushNotificationConstants.RECURRING_SESSION_BOOKING_MESSAGE.replace(EmailConstants.LITERAL_MEMBER_NAME, memberName).replace(EmailConstants.LITERAL_SESSION_NAME, schedule.getPackageKloudlessMapping().getTitle()).replace(EmailConstants.LITERAL_DATE, scheduleTimes[0]).replace(EmailConstants.LITERAL_TIME, scheduleTimes[1]));
                }
                pushNotificationAPIService.sendOnlyNotification(notificationContent, instructor, KeyConstants.KEY_INSTRUCTOR);
            } catch (Exception e) {
                log.error("Notification trigger failed for session booking by member : " + e.getMessage());
            }
        } catch (Exception exception) {
            log.error(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
        }
        log.info("Sending mail : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        // update kloudless event with fitwise properties
        for (UserKloudlessSchedule userKloudlessSchedule : createdSchedules) {
            Resource updatedEvent;
            switch (userKloudlessAccount.getService()) {
                case KloudLessService.SERVICE_ICLOUD_CALENDAR:
                    updatedEvent = updateKloudlessEventReminderProperties(userKloudlessCalendar, userKloudlessSchedule, userKloudlessSchedule.getScheduleId());
                    break;
                case KloudLessService.SERVICE_GOOGLE_CALENDAR:
                case KloudLessService.SERVICE_OUTLOOK_CALENDAR:
                default:
                    updatedEvent = updateKloudlessEventCustomProperties(userKloudlessCalendar, userKloudlessSchedule, userKloudlessSchedule.getScheduleId());
            }
            if (updatedEvent != null) {
                userKloudlessSchedule.setSchedulePayload(gson.toJson(updatedEvent.getData()));
            }
        }
        log.info("Update kloudless event with fitwise properties : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        userKloudlessScheduleRepository.saveAll(createdSchedules);
        log.info("Query to save user knloudless schedule : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        kloudlessScheduleModel.setFitwiseScheduleId(createdSchedules.get(0).getUserKloudlessScheduleId());
        kloudlessScheduleModel.setScheduleId(createdSchedules.get(0).getScheduleId());
        kloudlessScheduleModel.setSchedulePayload(gson.fromJson(createdSchedules.get(0).getSchedulePayload(), Map.class));
        log.info("Construct response model : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Create schedule ends.");
        return kloudlessScheduleModel;
    }

    public KloudlessScheduleModel updateSchedule(KloudlessScheduleModel kloudlessScheduleModel) {
        log.info("Update schedule starts.");
        long apiStartTimeMillis = new Date().getTime();
        ValidationUtils.throwException(kloudlessScheduleModel.getFitwiseScheduleId() == null
                        || kloudlessScheduleModel.getFitwiseScheduleId() <= 0,
                CalendarConstants.CAL_ERR_SCHEDULE_ID_INVALID, Constants.BAD_REQUEST);
        User user = userComponents.getUser();
        UserKloudlessSchedule userKloudlessSchedule;
        userKloudlessSchedule = userKloudlessScheduleRepository
                .findByUserAndUserKloudlessScheduleId(user, kloudlessScheduleModel.getFitwiseScheduleId());
        ValidationUtils.throwException(userKloudlessSchedule == null,
                CalendarConstants.CAL_ERR_SCHEDULE_NOT_FOUND, Constants.BAD_REQUEST);
        String oldScheduleTime = buildScheduleTimeString(userKloudlessSchedule);
        ValidationUtils.throwException(kloudlessScheduleModel.getSchedulePayload() == null,
                CalendarConstants.CAL_ERR_SCHEDULE_EMPTY, Constants.BAD_REQUEST);
        ValidationUtils.throwException(kloudlessScheduleModel.getSubscriptionPackageId() == null,
                ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_ID_NULL, Constants.BAD_REQUEST);
        SubscriptionPackage subscriptionPackage = subscriptionPackageRepository
                .findBySubscriptionPackageId(kloudlessScheduleModel.getSubscriptionPackageId());
        ValidationUtils.throwException(subscriptionPackage == null,
                ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_ID_NULL, Constants.BAD_REQUEST);
        UserKloudlessCalendar userKloudlessCalendar = userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar();
        UserKloudlessAccount userKloudlessAccount = userKloudlessCalendar.getUserKloudlessAccount();
        ValidationUtils.throwException(userKloudlessAccount == null,
                CalendarConstants.CAL_ERR_ACCOUNT_NOT_FOUND, Constants.BAD_REQUEST);
        Optional<UserKloudlessMeeting> userKloudlessMeetings = userKloudlessMeetingRepository
                .findByUserKloudlessCalendarAndUserKloudlessMeetingId(userKloudlessCalendar, kloudlessScheduleModel.getFitwiseMeetingId());
        ValidationUtils.throwException(!userKloudlessMeetings.isPresent(),
                CalendarConstants.CAL_ERR_MEETING_NOT_FOUND, Constants.BAD_REQUEST);
        ObjectMapper objectMapper = new ObjectMapper();
        Gson gson = new Gson();
        Event existingEvent = gson.fromJson(userKloudlessSchedule.getSchedulePayload(), Event.class);
        Event payloadEvent = objectMapper.convertValue(kloudlessScheduleModel.getSchedulePayload(), Event.class);
        Event patchEvent = new Event();
        ValidationUtils.throwException(!ValidationUtils.isEmptyString(payloadEvent.getStart())
                        && ValidationUtils.isEmptyString(payloadEvent.getEnd()),
                CalendarConstants.CAL_ERR_END_TIME_INVALID, Constants.BAD_REQUEST);
        ValidationUtils.throwException(!ValidationUtils.isEmptyString(payloadEvent.getEnd())
                        && ValidationUtils.isEmptyString(payloadEvent.getStart()),
                CalendarConstants.CAL_ERR_START_TIME_INVALID, Constants.BAD_REQUEST);
        log.info("Basic validations with some queries : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        OffsetDateTime existingEventStartDateTime;
        // expected date time format is 2011-12-03T10:15:30+01:00
        try {
            existingEventStartDateTime = OffsetDateTime.parse(existingEvent.getStart());
        } catch (DateTimeParseException dpe) {
            log.error("schedule update failed invalid event start time", dpe);
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_SCHEDULE_UPDATE_FAILED, null);
        }
        Optional<PackageKloudlessMapping> optionalPackageSession = packageKloudlessMappingRepository
                .findBySessionMappingId(kloudlessScheduleModel.getPackageSessionMappingId());
        log.info("Get existing event start date time : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if (!ValidationUtils.isEmptyString(payloadEvent.getStart()) && !ValidationUtils.isEmptyString(payloadEvent.getEnd())) {
            LocalDateTime startDateTime;
            LocalDateTime endDateTime;
            // expected date time format is '2011-12-03T10:15:30'
            try {
                startDateTime = LocalDateTime.parse(payloadEvent.getStart());
            } catch (DateTimeParseException dpe) {
                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_START_TIME_INVALID, null);
            }
            // expected date time format is '2011-12-03T10:15:30'
            try {
                endDateTime = LocalDateTime.parse(payloadEvent.getEnd());
            } catch (DateTimeParseException dpe) {
                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_END_TIME_INVALID, null);
            }
            Date date = new Date();
            TimeSpan timeSpan = userKloudlessSchedule.getSubscriptionPackage().getCancellationDuration();
            if (timeSpan.getDays() != null) {
                long differenceInDays = fitwiseUtils.getNumberOfDaysBetweenTwoDates(date, Date.from(existingEventStartDateTime.toInstant()));
                log.info("Difference in days:" +differenceInDays);
                if (differenceInDays < timeSpan.getDays()) {
                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_UPDATE_SCHEDULE_NOT_ALLOWED, null);
                }
            } else if (timeSpan.getHours() != null) {
                long differenceInHours = fitwiseUtils.getNumberOfHoursBetweenTwoDates(date,Date.from(existingEventStartDateTime.toInstant()));
                log.info("Difference in hours:" +differenceInHours);
                if (differenceInHours < timeSpan.getHours()) {
                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_UPDATE_SCHEDULE_NOT_ALLOWED, null);
                }
            }
            if (startDateTime.compareTo(endDateTime) == 0) {
                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_START_TIME_EQUALS_END_TIME, null);
            }
            if (startDateTime.isAfter(endDateTime)) {
                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_START_TIME_AFTER_END_TIME, null);
            }
            ValidationUtils.throwException(kloudlessScheduleModel.getBookingDate() == null,
                    CalendarConstants.CAL_ERR_BOOKING_DATE_INVALID, Constants.BAD_REQUEST);
            patchEvent.setStart(payloadEvent.getStart());
            patchEvent.setEnd(payloadEvent.getEnd());
        }
        log.info("Validate date time : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if (!ValidationUtils.isEmptyString(payloadEvent.getName())) {
            patchEvent.setName(payloadEvent.getName());
        }
        if (!ValidationUtils.isEmptyString(payloadEvent.getDescription())) {
            patchEvent.setDescription(payloadEvent.getDescription());
        }
        if (payloadEvent.getAttendees() != null && !payloadEvent.getAttendees().isEmpty()) {
            for (Attendee attendee : payloadEvent.getAttendees()) {
                ValidationUtils.throwException(ValidationUtils.isEmptyString(attendee.getName()),
                        CalendarConstants.CAL_ERR_ATTENDEE_NAME_INVALID, Constants.BAD_REQUEST);
                ValidationUtils.throwException(
                        ValidationUtils.isEmptyString(attendee.getEmail()) || !ValidationUtils.emailRegexValidate(attendee.getEmail()),
                        CalendarConstants.CAL_ERR_ATTENDEE_EMAIL_INVALID, Constants.BAD_REQUEST);
            }
            patchEvent.setAttendees(payloadEvent.getAttendees());
        }
        log.info("Set attendees : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        String meetingUrl = null;
        if(optionalPackageSession.isPresent() && optionalPackageSession.get() != null){
            if(CalendarConstants.SESSION_VIRTUAL == userKloudlessSchedule.getMeetingTypeId().getMeetingTypeId()){
                StringBuilder builder = new StringBuilder();
                if (!ValidationUtils.isEmptyString(patchEvent.getDescription())) {
                    builder.append(patchEvent.getDescription());
                }
                meetingUrl = optionalPackageSession.get().getMeetingUrl();
                if (!StringUtils.isEmpty(meetingUrl)) {
                    builder.append(MEETING_INVITE_SEPARATOR);
                    builder.append(meetingUrl);
                    builder.append(MEETING_INVITE_SEPARATOR);
                }
                patchEvent.setLocation(meetingUrl);
                patchEvent.setDescription(builder.toString());
            } else if(CalendarConstants.SESSION_IN_PERSON == userKloudlessSchedule.getMeetingTypeId().getMeetingTypeId()){
                Location location = optionalPackageSession.get().getLocation();
                if (location != null) {
                    String locationText = buildLocationText(location);
                    if (!ValidationUtils.isEmptyString(locationText)) {
                        patchEvent.setLocation(locationText);
                    }
                }
            }
        }
        log.info("Get zoom account : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if (ValidationUtils.isEmptyString(userKloudlessAccount.getService())) {
            userKloudlessAccount = kloudLessService.updateServiceInfo(userKloudlessAccount);
        }
        log.info("Get user kloudless account : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        if (!StringUtils.isEmpty(meetingUrl)) {
            switch (userKloudlessAccount.getService()) {
                case KloudLessService.SERVICE_ICLOUD_CALENDAR:
                    List<Reminder> updatedReminders = new ArrayList<>();
                    if (existingEvent.getReminders() != null && !existingEvent.getReminders().isEmpty()) {
                        for (Reminder existingReminder : existingEvent.getReminders()) {
                            updatedReminders.add(updateReminderProperty(existingReminder,
                                    CalendarConstants.CAL_PROP_ZOOM_MEETING_LINK,
                                    meetingUrl));
                        }
                    }
                    if (!updatedReminders.isEmpty()) {
                        patchEvent.setReminders(updatedReminders);
                    }
                    break;
                case KloudLessService.SERVICE_GOOGLE_CALENDAR:
                case KloudLessService.SERVICE_OUTLOOK_CALENDAR:
                default:
                    CustomProperty customProperty = new CustomProperty();
                    customProperty.setKey(CalendarConstants.CAL_PROP_ZOOM_MEETING_LINK);
                    customProperty.setValue(meetingUrl);
                    customProperty.set_private(true);
                    patchEvent.setCustomProperties(Collections.singletonList(customProperty));
                    break;
            }
        }
        log.info("Set customer properties : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        Map<String, Object> updateContent = objectMapper.convertValue(patchEvent, new TypeReference<Map<String, Object>>() {});
        kloudLessService.updateKloudlessSchedule(userKloudlessCalendar, userKloudlessSchedule.getScheduleId(), updateContent);
        log.info("Update kloudless schedule : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        Resource updatedEvent = kloudLessService.getKloudlessSchedule(userKloudlessCalendar, userKloudlessSchedule.getScheduleId());
        log.info("Get kloudless schedule : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if (!ValidationUtils.isEmptyString(patchEvent.getStart())) {
            String start = updatedEvent.getData().get(StringConstants.JSON_PROPERTY_KEY_START).getAsString();
            Instant startTime = OffsetDateTime.parse(start).toZonedDateTime()
                    .with(ChronoField.HOUR_OF_DAY, 0)
                    .with(ChronoField.MINUTE_OF_DAY, 0)
                    .with(ChronoField.SECOND_OF_DAY, 0)
                    .withZoneSameInstant(ZoneOffset.UTC).toInstant();
            userKloudlessSchedule.setBookingDate(kloudlessScheduleModel.getBookingDate());
            if (startTime.compareTo(kloudlessScheduleModel.getBookingDate().toInstant())  != 0) {
                userKloudlessSchedule.setBookingDate(Date.from(startTime));
                log.info("update schedule booking date mismatch: replacing booking date with updated schedule start date");
            }
        }
        log.info("Validate start date : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        userKloudlessSchedule.setSchedulePayload(gson.toJson(updatedEvent.getData()));
        if (CalendarConstants.SESSION_VIRTUAL == userKloudlessSchedule.getMeetingTypeId().getMeetingTypeId() && !StringUtils.isEmpty(meetingUrl)) {
            userKloudlessSchedule.setOnlineMeetingEntryUrl(meetingUrl);
        }

        userKloudlessScheduleRepository.save(userKloudlessSchedule);
        log.info("Query: save user kloudless schedule : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        kloudlessScheduleModel.setScheduleId(userKloudlessSchedule.getScheduleId());
        kloudlessScheduleModel.setMeetingId(userKloudlessSchedule.getUserKloudlessMeeting().getMeetingId());
        kloudlessScheduleModel.setFitwiseMeetingTypeId(userKloudlessSchedule.getMeetingTypeId().getMeetingTypeId());
        kloudlessScheduleModel.setSchedulePayload(gson.fromJson(userKloudlessSchedule.getSchedulePayload(), Map.class));

        try {
            User instructor = userKloudlessSchedule.getUserKloudlessMeeting().getUser();
            String instructorName = fitwiseUtils.getUserFullName(instructor);
            String memberName = fitwiseUtils.getUserFullName(user);
            String scheduleTime = buildScheduleTimeString(userKloudlessSchedule);
            String[] scheduleTimes = scheduleTime.split(" ");
            String[] oldScheduleTimes = oldScheduleTime.split(" ");
            try {
                String subject = EmailConstants.SESSION_UPDATE_SUBJECT_INSTRUCTOR.replace(EmailConstants.LITERAL_DATE_TIME, scheduleTime);
                String mailBody = EmailConstants.SESSION_UPDATE_CONTENT_INSTRUCTOR.replace(EmailConstants.LITERAL_MEMBER_NAME, memberName).replace(EmailConstants.LITERAL_SESSION_NAME, userKloudlessSchedule.getPackageKloudlessMapping().getTitle()).replace("#OLD_DATE#", oldScheduleTimes[0]).replace("#OLD_TIME#", oldScheduleTimes[1]).
                        replace("#NEW_DATE#",scheduleTimes[0]).replace("#NEW_TIME#",scheduleTimes[1]);
                mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + instructorName + ",").replace(EmailConstants.LITERAL_MAIL_BODY, mailBody);
                mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
                asyncMailer.sendHtmlMail(instructor.getEmail(), subject, mailBody);
            } catch (Exception e) {
                log.info("Mail sending failed for session rescheduling by member :" + e.getMessage());
            }
            try {
                NotificationContent notificationContent = new NotificationContent();
                notificationContent.setTitle(PushNotificationConstants.SESSION_UPDATE_TITLE_INSTRUCTOR.replace(EmailConstants.LITERAL_DATE_TIME, scheduleTime));
                notificationContent.setBody(PushNotificationConstants.SESSION_UPDATE_MESSAGE_INSTRUCTOR.replace(EmailConstants.LITERAL_MEMBER_NAME, memberName).replace(EmailConstants.LITERAL_SESSION_NAME, userKloudlessSchedule.getPackageKloudlessMapping().getTitle()).replace("#OLD_DATE#", oldScheduleTimes[0]).replace("#OLD_TIME#", oldScheduleTimes[1])
                        .replace("#NEW_DATE#",scheduleTimes[0]).replace("#NEW_TIME#",scheduleTimes[1]));

                pushNotificationAPIService.sendOnlyNotification(notificationContent, instructor, KeyConstants.KEY_INSTRUCTOR);
            } catch (Exception exception) {
                log.error("Notification trigger failed for session rescheduling by member : " + exception.getMessage());
            }

        } catch (Exception exception) {
            log.error("Notification trigger failed for session rescheduling by member : " + exception.getMessage());
        }
        log.info("Sending mail : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Update schedule ends.");

        return kloudlessScheduleModel;
    }

    public List<ZoomMeeting> createZoomMeetingFromEvent(User user, Event event) {
        ValidationUtils.throwException(event == null,
                CalendarConstants.CAL_ERR_SCHEDULE_EMPTY, Constants.BAD_REQUEST);
        ZoomMeetingRequest zoomMeetingRequest = new ZoomMeetingRequest();
        zoomMeetingRequest.setTitle(event.getName());
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;
        // expected date time format is '2011-12-03T10:15:30'
        try {
            startDateTime = LocalDateTime.parse(event.getStart());
        } catch (DateTimeParseException dpe) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_START_INVALID, null);
        }
        // expected date time format is '2011-12-03T10:15:30'
        try {
            endDateTime = LocalDateTime.parse(event.getEnd());
        } catch (DateTimeParseException dpe) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_END_INVALID, null);
        }
        long duration = Duration.between(startDateTime, endDateTime).toMinutes();
        zoomMeetingRequest.setStartTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(startDateTime));
        zoomMeetingRequest.setDuration((int) duration);
        zoomMeetingRequest.setTimeZone(event.getStartTimeZone());
        if (event.getRecurrence() != null && !event.getRecurrence().isEmpty()) {
            List<String> rrules = new ArrayList<>();
            for (Recurrence recurrence : event.getRecurrence()) {
                rrules.add(recurrence.getRrule());
            }
            zoomMeetingRequest.setRecurrenceRules(rrules);
        }
        return zoomService.createMeetingForUser(user, zoomMeetingRequest);
    }

    public Resource updateKloudlessEventCustomProperties(UserKloudlessCalendar calendar, UserKloudlessSchedule schedule, String eventId) {
        Map<String, Object> isFitwiseEvent = new HashMap<>();
        isFitwiseEvent.put("key", CalendarConstants.CAL_PROP_IS_FITWISE_EVENT);
        isFitwiseEvent.put(StringConstants.JSON_PROPERTY_KEY_VALUE, "true");
        isFitwiseEvent.put(StringConstants.JSON_PROPERTY_KEY_PRIVATE, true);
        Map<String, Object> scheduleId = new HashMap<>();
        if (schedule.getUserKloudlessScheduleId() != null && schedule.getUserKloudlessScheduleId() > 0) {
            scheduleId.put("key", CalendarConstants.CAL_PROP_FITWISE_SCHEDULE_ID);
            scheduleId.put(StringConstants.JSON_PROPERTY_KEY_VALUE, String.valueOf(schedule.getUserKloudlessScheduleId()));
            scheduleId.put(StringConstants.JSON_PROPERTY_KEY_PRIVATE, true);
        }
        Map<String, Object> meetingId = new HashMap<>();
        if (schedule.getUserKloudlessMeeting() != null
                && schedule.getUserKloudlessMeeting().getUserKloudlessMeetingId() != null
                && schedule.getUserKloudlessMeeting().getUserKloudlessMeetingId() > 0) {
            meetingId.put("key", CalendarConstants.CAL_PROP_FITWISE_MEETING_ID);
            meetingId.put(StringConstants.JSON_PROPERTY_KEY_VALUE, String.valueOf(schedule.getUserKloudlessMeeting().getUserKloudlessMeetingId()));
            meetingId.put(StringConstants.JSON_PROPERTY_KEY_PRIVATE, true);
        }
        Map<String, Object> sessionType = new HashMap<>();
        if (schedule.getMeetingTypeId() != null
                && schedule.getMeetingTypeId().getMeetingTypeId() != null
                && schedule.getMeetingTypeId().getMeetingTypeId() > 0) {
            sessionType.put("key", CalendarConstants.CAL_PROP_SESSION_TYPE);
            sessionType.put(StringConstants.JSON_PROPERTY_KEY_VALUE, String.valueOf(schedule.getMeetingTypeId().getMeetingTypeId()));
            sessionType.put(StringConstants.JSON_PROPERTY_KEY_PRIVATE, true);
        }
        Map<String, Object> meetingWindowId = new HashMap<>();
        if (schedule.getUserKloudlessMeeting() != null
                && !ValidationUtils.isEmptyString(schedule.getUserKloudlessMeeting().getMeetingId())) {
            meetingWindowId.put("key", CalendarConstants.CAL_PROP_MEETING_WINDOW_ID);
            meetingWindowId.put(StringConstants.JSON_PROPERTY_KEY_VALUE, schedule.getUserKloudlessMeeting().getMeetingId());
            meetingWindowId.put(StringConstants.JSON_PROPERTY_KEY_PRIVATE, true);
        }

        Map<String, Object> zoomMeetingLink = new HashMap<>();
        if (!ValidationUtils.isEmptyString(schedule.getOnlineMeetingEntryUrl())) {
            zoomMeetingLink.put("key", CalendarConstants.CAL_PROP_ZOOM_MEETING_LINK);
            zoomMeetingLink.put(StringConstants.JSON_PROPERTY_KEY_VALUE, schedule.getOnlineMeetingEntryUrl());
            zoomMeetingLink.put(StringConstants.JSON_PROPERTY_KEY_PRIVATE, true);
        }
        Map<String, Object> subscriptionPackageId = new HashMap<>();
        Map<String, Object> subscriptionPackageTitle = new HashMap<>();
        if (schedule.getSubscriptionPackage() != null) {
            subscriptionPackageId.put("key", CalendarConstants.CAL_PROP_PACKAGE_ID);
            subscriptionPackageId.put(StringConstants.JSON_PROPERTY_KEY_VALUE, String.valueOf(schedule.getSubscriptionPackage().getSubscriptionPackageId()));
            subscriptionPackageId.put(StringConstants.JSON_PROPERTY_KEY_PRIVATE, true);
            subscriptionPackageTitle.put("key", CalendarConstants.CAL_PROP_PACKAGE_NAME);
            subscriptionPackageTitle.put(StringConstants.JSON_PROPERTY_KEY_VALUE, schedule.getSubscriptionPackage().getTitle());
            subscriptionPackageTitle.put(StringConstants.JSON_PROPERTY_KEY_PRIVATE, true);
        }
        Map<String, Object> sessionName = new HashMap<>();
        if (schedule.getUserKloudlessMeeting() != null
                && schedule.getUserKloudlessMeeting().getName() != null
                && !ValidationUtils.isEmptyString(schedule.getUserKloudlessMeeting().getName())) {
            sessionName.put("key", CalendarConstants.CAL_PROP_SESSION_NAME);
            sessionName.put(StringConstants.JSON_PROPERTY_KEY_VALUE, String.valueOf(schedule.getUserKloudlessMeeting().getName()));
            sessionName.put(StringConstants.JSON_PROPERTY_KEY_PRIVATE, true);
        }
        Map<String, Object> sessionNameInPackage = new HashMap<>();
        if (schedule.getPackageKloudlessMapping() != null
                && schedule.getPackageKloudlessMapping().getTitle() != null
                && !ValidationUtils.isEmptyString(schedule.getPackageKloudlessMapping().getTitle())) {
            sessionNameInPackage.put("key", CalendarConstants.CAL_PROP_SESSION_NAME_PACKAGE);
            sessionNameInPackage.put(StringConstants.JSON_PROPERTY_KEY_VALUE, String.valueOf(schedule.getPackageKloudlessMapping().getTitle()));
            sessionNameInPackage.put(StringConstants.JSON_PROPERTY_KEY_PRIVATE, true);
        }
        Map<String, Object> address = new HashMap<>();
        Map<String, Object> city = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        Map<String, Object> country = new HashMap<>();
        Map<String, Object> zipcode = new HashMap<>();
        Map<String, Object> landmark = new HashMap<>();
        if(schedule.getPackageKloudlessMapping() != null && (schedule.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingTypeId() == CalendarConstants.SESSION_IN_PERSON ||
                (schedule.getMeetingTypeId().getMeetingTypeId() == CalendarConstants.SESSION_IN_PERSON)) && schedule.getPackageKloudlessMapping().getLocation() != null){
            Location instructorLocation = schedule.getPackageKloudlessMapping().getLocation();
            address.put("key", CalendarConstants.CAL_PROP_ADDRESS);
            address.put(StringConstants.JSON_PROPERTY_KEY_VALUE, instructorLocation.getAddress());
            address.put(StringConstants.JSON_PROPERTY_KEY_PRIVATE, true);
            city.put("key", CalendarConstants.CAL_PROP_CITY);
            city.put(StringConstants.JSON_PROPERTY_KEY_VALUE, instructorLocation.getCity());
            city.put(StringConstants.JSON_PROPERTY_KEY_PRIVATE, true);
            state.put("key", CalendarConstants.CAL_PROP_STATE);
            state.put(StringConstants.JSON_PROPERTY_KEY_VALUE, instructorLocation.getState());
            state.put(StringConstants.JSON_PROPERTY_KEY_PRIVATE, true);
            country.put("key", CalendarConstants.CAL_PROP_COUNTRY);
            country.put(StringConstants.JSON_PROPERTY_KEY_VALUE, instructorLocation.getCountry().getCountryName());
            country.put(StringConstants.JSON_PROPERTY_KEY_PRIVATE, true);
            zipcode.put("key", CalendarConstants.CAL_PROP_ZIPCODE);
            zipcode.put(StringConstants.JSON_PROPERTY_KEY_VALUE, instructorLocation.getZipcode());
            zipcode.put(StringConstants.JSON_PROPERTY_KEY_PRIVATE, true);
            landmark.put("key", CalendarConstants.CAL_PROP_LANDMARK);
            if(instructorLocation.getLandMark() != null){
                landmark.put(StringConstants.JSON_PROPERTY_KEY_VALUE, instructorLocation.getLandMark());
            }else{
                landmark.put(StringConstants.JSON_PROPERTY_KEY_VALUE, "");
            }
            landmark.put(StringConstants.JSON_PROPERTY_KEY_PRIVATE, true);
        }
        List<Map<String, Object>> propertiesList = new ArrayList<>();
        propertiesList.add(isFitwiseEvent);
        if (!scheduleId.isEmpty()) {
            propertiesList.add(scheduleId);
        }
        if (!meetingId.isEmpty()) {
            propertiesList.add(meetingId);
        }
        if (!sessionType.isEmpty()) {
            propertiesList.add(sessionType);
        }
        if (!meetingWindowId.isEmpty()) {
            propertiesList.add(meetingWindowId);
        }
        if (!zoomMeetingLink.isEmpty()) {
            propertiesList.add(zoomMeetingLink);
        }
        if(!address.isEmpty()){
            propertiesList.add(address);
        }
        if(!city.isEmpty()){
            propertiesList.add(city);
        }
        if(!state.isEmpty()){
            propertiesList.add(state);
        }
        if(!country.isEmpty()){
            propertiesList.add(country);
        }
        if(!zipcode.isEmpty()){
            propertiesList.add(zipcode);
        }
        if(!landmark.isEmpty()){
            propertiesList.add(landmark);
        }
        if(!subscriptionPackageId.isEmpty()){
            propertiesList.add(subscriptionPackageId);
        }
        if(!subscriptionPackageTitle.isEmpty()){
            propertiesList.add(subscriptionPackageTitle);
        }
        if(!sessionName.isEmpty()){
            propertiesList.add(sessionName);
        }
        if(!sessionNameInPackage.isEmpty()){
            propertiesList.add(sessionNameInPackage);
        }
        Map<String, Object> customProperties = new HashMap<>();
        customProperties.put(StringConstants.JSON_PROPERTY_KEY_CUSTOM_PROPERTIES, propertiesList);
        return kloudLessService.updateKloudlessSchedule(calendar, eventId, customProperties);
    }

    public Resource updateKloudlessEventReminderProperties(UserKloudlessCalendar calendar, UserKloudlessSchedule schedule, String eventId) {
        Map<String, String> properties = new HashMap<>();
        properties.put(CalendarConstants.CAL_PROP_IS_FITWISE_EVENT, "true");
        if (schedule.getUserKloudlessScheduleId() != null && schedule.getUserKloudlessScheduleId() > 0) {
            properties.put(CalendarConstants.CAL_PROP_FITWISE_SCHEDULE_ID, String.valueOf(schedule.getUserKloudlessScheduleId()));
        }
        if (schedule.getUserKloudlessMeeting() != null
                && schedule.getUserKloudlessMeeting().getUserKloudlessMeetingId() != null
                && schedule.getUserKloudlessMeeting().getUserKloudlessMeetingId() > 0) {
            properties.put(CalendarConstants.CAL_PROP_FITWISE_MEETING_ID, String.valueOf(schedule.getUserKloudlessMeeting().getUserKloudlessMeetingId()));
        }
        if (schedule.getMeetingTypeId() != null
                && schedule.getMeetingTypeId().getMeetingTypeId() != null
                && schedule.getMeetingTypeId().getMeetingTypeId() > 0) {
            properties.put(CalendarConstants.CAL_PROP_SESSION_TYPE, String.valueOf(schedule.getMeetingTypeId().getMeetingTypeId()));
        }
        if (schedule.getUserKloudlessMeeting() != null
                && !ValidationUtils.isEmptyString(schedule.getUserKloudlessMeeting().getMeetingId())) {
            properties.put(CalendarConstants.CAL_PROP_MEETING_WINDOW_ID, schedule.getUserKloudlessMeeting().getMeetingId());
        }
        if (!ValidationUtils.isEmptyString(schedule.getOnlineMeetingEntryUrl())) {
            properties.put(CalendarConstants.CAL_PROP_ZOOM_MEETING_LINK, schedule.getOnlineMeetingEntryUrl());
        }
        if(schedule.getSubscriptionPackage() != null){
            properties.put(CalendarConstants.CAL_PROP_PACKAGE_ID,String.valueOf(schedule.getSubscriptionPackage().getSubscriptionPackageId()));
            properties.put(CalendarConstants.CAL_PROP_PACKAGE_NAME,schedule.getSubscriptionPackage().getTitle());
        }
        if(schedule.getUserKloudlessMeeting() != null && schedule.getUserKloudlessMeeting().getName() != null && !ValidationUtils.isEmptyString(schedule.getUserKloudlessMeeting().getName())){
            properties.put(CalendarConstants.CAL_PROP_SESSION_NAME,schedule.getUserKloudlessMeeting().getName());
        }
        if(schedule.getPackageKloudlessMapping() != null && schedule.getPackageKloudlessMapping().getTitle() != null && !ValidationUtils.isEmptyString(schedule.getPackageKloudlessMapping().getTitle())){
            properties.put(CalendarConstants.CAL_PROP_SESSION_NAME_PACKAGE,schedule.getPackageKloudlessMapping().getTitle());
        }
        if (schedule.getPackageKloudlessMapping() != null && (schedule.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingTypeId() == CalendarConstants.SESSION_IN_PERSON ||
                (schedule.getMeetingTypeId().getMeetingTypeId() == CalendarConstants.SESSION_IN_PERSON)) && schedule.getPackageKloudlessMapping().getLocation() != null) {
            Location instructorLocation = schedule.getPackageKloudlessMapping().getLocation();
            properties.put(CalendarConstants.CAL_PROP_ADDRESS, instructorLocation.getAddress());
            properties.put(CalendarConstants.CAL_PROP_CITY, instructorLocation.getCity());
            properties.put(CalendarConstants.CAL_PROP_STATE, instructorLocation.getState());
            properties.put(CalendarConstants.CAL_PROP_COUNTRY, instructorLocation.getCountry().getCountryName());
            properties.put(CalendarConstants.CAL_PROP_ZIPCODE, instructorLocation.getZipcode());
            if(instructorLocation.getLandMark() != null){
                properties.put(CalendarConstants.CAL_PROP_LANDMARK, instructorLocation.getLandMark());
            }else{
                properties.put(CalendarConstants.CAL_PROP_LANDMARK, "");
            }
        }
        List<String> values = new ArrayList<>();
        properties.forEach((name, value) -> values.add(String.format("%s=%s", name, value)));
        String summary = String.join(REMINDER_CUSTOM_PROPERTY_SEPERATOR, values);
        Reminder reminder = new Reminder();
        reminder.setMinutes(30L);
        reminder.setMethod("email");
        Email email = new Email();
        email.setDescription("Fitwise");
        email.setSummary(summary);
        email.setTo(new ArrayList<>());
        reminder.setEmail(email);
        List<Reminder> reminderList = new ArrayList<>();
        reminderList.add(reminder);
        Map<String, Object> reminders = new HashMap<>();
        reminders.put("reminders", reminderList);
        return kloudLessService.updateKloudlessSchedule(calendar, eventId, reminders);
    }

    private Reminder updateReminderProperty(Reminder existingReminder, String key, String value) {
        if (existingReminder == null
                || existingReminder.getEmail() == null
                || ValidationUtils.isEmptyString(existingReminder.getEmail().getSummary())) {
            return  existingReminder;
        }

        if (ValidationUtils.isEmptyString(key) || ValidationUtils.isEmptyString(value)) {
            return existingReminder;
        }

        List <String> updatedProperties = new ArrayList<>();
        String[] properties = existingReminder.getEmail().getSummary().split(REMINDER_CUSTOM_PROPERTY_SEPERATOR);
        for (String property : properties) {
            if (property.split("=")[0].equals(key) && property.split("=").length > 1) {
                property = String.format("%s=%s", property.split("=")[0], value);
            }
            updatedProperties.add(property);
        }

        existingReminder.getEmail().setSummary(String.join(REMINDER_CUSTOM_PROPERTY_SEPERATOR, updatedProperties));
        return existingReminder;
    }

    public ZoomMeeting updateZoomMeetingFromEvent(User user, String meetingId, String occurrenceId, Event event) {
        ValidationUtils.throwException(event == null,
                CalendarConstants.CAL_ERR_SCHEDULE_EMPTY, Constants.BAD_REQUEST);
        ZoomMeetingRequest zoomMeetingRequest = new ZoomMeetingRequest();
        if (!ValidationUtils.isEmptyString(event.getName())) {
            zoomMeetingRequest.setTitle(event.getName());
        }
        if (!ValidationUtils.isEmptyString(event.getStart()) && !ValidationUtils.isEmptyString(event.getEnd())) {
            LocalDateTime startDateTime;
            LocalDateTime endDateTime;
            // expected date time format is '2011-12-03T10:15:30'
            try {
                startDateTime = LocalDateTime.parse(event.getStart());
            } catch (DateTimeParseException dpe) {
                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_START_INVALID, null);
            }
            // expected date time format is '2011-12-03T10:15:30'
            try {
                endDateTime = LocalDateTime.parse(event.getEnd());
            } catch (DateTimeParseException dpe) {
                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_END_INVALID, null);
            }
            long duration = Duration.between(startDateTime, endDateTime).toMinutes();
            zoomMeetingRequest.setStartTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(startDateTime));
            zoomMeetingRequest.setDuration((int) duration);
        }
        if (ValidationUtils.isEmptyString(zoomMeetingRequest.getTitle())
                && ValidationUtils.isEmptyString(zoomMeetingRequest.getStartTime())
                && (zoomMeetingRequest.getDuration() == null || zoomMeetingRequest.getDuration() <= 0)) {
            return null;
        }
        return zoomService.updateMeetingForUser(user, meetingId, occurrenceId, zoomMeetingRequest);
    }

    public String buildLocationText(Location location) {
        if (location == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        if (!ValidationUtils.isEmptyString(location.getAddress())) {
            builder.append(location.getAddress());
            builder.append("\r\n");
        }

        if (!ValidationUtils.isEmptyString(location.getCity())) {
            builder.append(location.getCity());

            if (!ValidationUtils.isEmptyString(location.getZipcode())) {
                builder.append(", ");
                builder.append(location.getZipcode());
            }
            builder.append("\r\n");
        }

        if (!ValidationUtils.isEmptyString(location.getState())) {
            builder.append(location.getState());

            if (location.getCountry() != null && !ValidationUtils.isEmptyString(location.getCountry().getCountryName())) {
                builder.append(", ");
                builder.append(location.getCountry().getCountryName());
            }
            builder.append("\r\n");
        }

        String landmark = "";
        if (location.getLandMark() != null && !ValidationUtils.isEmptyString(location.getLandMark())) {
            landmark = location.getLandMark();
        }
        builder.append("Landmark: ");
        builder.append(landmark);


        return builder.toString();
    }

    public void deleteSchedule(final Long fitwiseScheduleId) throws ParseException {
        User user = userComponents.getUser();
        UserKloudlessSchedule userKloudlessSchedule = userKloudlessScheduleRepository.findByUserAndUserKloudlessScheduleId(user, fitwiseScheduleId);
       
        if (userKloudlessSchedule == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_SCHEDULE_NOT_FOUND, null);
        }
        String scheduleTime = buildScheduleTimeString(userKloudlessSchedule);
        Gson gson = new Gson();
        Event existingEvent = gson.fromJson(userKloudlessSchedule.getSchedulePayload(), Event.class);
        OffsetDateTime startDateTime;
        // expected date time format is 2011-12-03T10:15:30+01:00
        try {
            startDateTime = OffsetDateTime.parse(existingEvent.getStart());
        } catch (DateTimeParseException dpe) {
            log.error("schedule delete failed invalid event start time", dpe);
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_SCHEDULE_DELETE_FAILED, null);
        }
       
        Date date = new Date();
        TimeSpan timeSpan = userKloudlessSchedule.getSubscriptionPackage().getCancellationDuration();
        if (timeSpan.getDays() != null) {
            long differenceInDays = fitwiseUtils.getNumberOfDaysBetweenTwoDates(date, Date.from(startDateTime.toInstant()));
            log.info("Difference in days:" +differenceInDays);
            if (differenceInDays < timeSpan.getDays()) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_DELETE_SCHEDULE_NOT_ALLOWED, null);
            }
        } else if (timeSpan.getHours() != null) {
            long differenceInHours = fitwiseUtils.getNumberOfHoursBetweenTwoDates(date, Date.from(startDateTime.toInstant()));
            log.info("Difference in hours:" +differenceInHours);
            if (differenceInHours < timeSpan.getHours()) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_DELETE_SCHEDULE_NOT_ALLOWED, null);
            }
        }
        
        ResponseRaw response = kloudLessService.deleteKloudlessSchedule(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar(), userKloudlessSchedule.getScheduleId());
        if (response.getData().getStatusLine().getStatusCode() != 204 && response.getData().getStatusLine().getStatusCode() != 200) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_KLOUDLESS_SCHEDULE_DELETE_FAILED, null);
        }
        userKloudlessScheduleRepository.delete(userKloudlessSchedule);
        try {
            User instructor = userKloudlessSchedule.getUserKloudlessMeeting().getUser();
            String instructorName = fitwiseUtils.getUserFullName(instructor);
            String memberName = fitwiseUtils.getUserFullName(user);
            String[] scheduleTimes = scheduleTime.split(" ");
            try {
                String subject = EmailConstants.SESSION_CANCEL_SUBJECT_INSTRUCTOR.replace(EmailConstants.LITERAL_DATE_TIME, scheduleTime);
                String mailBody = EmailConstants.SESSION_CANCEL_CONTENT_INSTRUCTOR.replace(EmailConstants.LITERAL_MEMBER_NAME, memberName).replace(EmailConstants.LITERAL_SESSION_NAME, userKloudlessSchedule.getPackageKloudlessMapping().getTitle()).replace(EmailConstants.LITERAL_DATE, scheduleTimes[0]).replace(EmailConstants.LITERAL_TIME, scheduleTimes[1]);
                mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + instructorName + ",").replace(EmailConstants.LITERAL_MAIL_BODY, mailBody);
                mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
                asyncMailer.sendHtmlMail(instructor.getEmail(), subject, mailBody);
            } catch (Exception e) {
                log.info("Mail sending failed for session cancelling by member :" + e.getMessage());
            }
            try {
                NotificationContent notificationContent = new NotificationContent();
                notificationContent.setTitle(PushNotificationConstants.SESSION_CANCEL_TITLE_INSTRUCTOR.replace(EmailConstants.LITERAL_DATE_TIME, scheduleTime));
                notificationContent.setBody(PushNotificationConstants.SESSION_CANCEL_MESSAGE_INSTRUCTOR.replace(EmailConstants.LITERAL_MEMBER_NAME, memberName).replace(EmailConstants.LITERAL_SESSION_NAME, userKloudlessSchedule.getPackageKloudlessMapping().getTitle()).replace(EmailConstants.LITERAL_DATE, scheduleTimes[0]).replace(EmailConstants.LITERAL_TIME, scheduleTimes[1]));
                pushNotificationAPIService.sendOnlyNotification(notificationContent, instructor, KeyConstants.KEY_INSTRUCTOR);
            } catch (Exception exception) {
                log.error("Notification trigger failed for session cancelling by member : " + exception.getMessage());
            }
        } catch (Exception exception) {
            log.error("Notification trigger failed for session cancelling by member : " + exception.getMessage());
        }
    }

    public KloudlessMeetingListModel getUserMeetings(final int pageNo, final int pageSize, String userId) {
        User user = userService.getUser(Long.parseLong(userId));
        if(user == null){
            throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_USER_NOT_FOUND, null);
        }
        List<UserKloudlessCalendar> userKloudlessCalendars = userKloudlessCalendarRepository.findByUserAndPrimaryCalendar(user, true);
        if(userKloudlessCalendars.isEmpty()){
            throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_CALENDAR_NOT_FOUND, null);
        }
        PageRequest pageRequest = PageRequest.of(pageNo <= 0 ? 0 : pageNo - 1, pageSize);
        Page<UserKloudlessMeeting> userKloudlessMeetings = userKloudlessMeetingRepository.findByMeetingIdNotNullAndUserKloudlessCalendar(userKloudlessCalendars.get(0), pageRequest);
        List<KloudlessMeetingModel> kloudlessMeetingModels = new ArrayList<>();
        if(userKloudlessMeetings.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, "", null);
        }
        Gson gson = new Gson();
        for(UserKloudlessMeeting userKloudlessMeeting : userKloudlessMeetings){
            KloudlessMeetingModel kloudlessMeetingModel = new KloudlessMeetingModel();
            kloudlessMeetingModel.setFitwiseMeetingId(userKloudlessMeeting.getUserKloudlessMeetingId());
            kloudlessMeetingModel.setMeetingId(userKloudlessMeeting.getMeetingId());
            kloudlessMeetingModel.setCalendarId(userKloudlessCalendars.get(0).getCalendarId());
            kloudlessMeetingModel.setMeetingTypeId(userKloudlessMeeting.getCalendarMeetingType().getMeetingTypeId());
            kloudlessMeetingModel.setMeetingType(userKloudlessMeeting.getCalendarMeetingType().getMeetingType());
            kloudlessMeetingModel.setName(userKloudlessMeeting.getName());
            kloudlessMeetingModel.setSessionDuration(userKloudlessMeeting.getMeetingDurationInDays());
            kloudlessMeetingModel.setDurationInMinutes(userKloudlessMeeting.getDuration().getMinutes());
            kloudlessMeetingModel.setTimeZone(userKloudlessMeeting.getTimeZone());
            if (!ValidationUtils.isEmptyString(userKloudlessMeeting.getMeetingWindow())) {
                kloudlessMeetingModel.setMeetingWindow(gson.fromJson(userKloudlessMeeting.getMeetingWindow(), Map.class));
            }
            kloudlessMeetingModels.add(kloudlessMeetingModel);
        }
        KloudlessMeetingListModel meetingsList = new KloudlessMeetingListModel();
        meetingsList.setMeetings(kloudlessMeetingModels);
        meetingsList.setTotalCount(userKloudlessMeetings.getTotalElements());
        return meetingsList;
    }

    public List<KloudlessScheduleModel> getMySchedules() throws ParseException {
         User user = userComponents.getUser();
         List<UserKloudlessSchedule> userKloudlessSchedules = userKloudlessScheduleRepository.findByUser(user);
         if(userKloudlessSchedules.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS,"",null);
         }
         List<KloudlessScheduleModel> kloudlessScheduleModels = new ArrayList<>();
         for(UserKloudlessSchedule userKloudlessSchedule : userKloudlessSchedules){
            KloudlessScheduleModel kloudlessScheduleModel = new KloudlessScheduleModel();
            kloudlessScheduleModel.setMeetingId(userKloudlessSchedule.getUserKloudlessMeeting().getMeetingId());
            kloudlessScheduleModel.setScheduleId(userKloudlessSchedule.getScheduleId());
            Resource resource = kloudLessService.getKloudlessSchedule(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar(), userKloudlessSchedule.getScheduleId());
            if(resource == null){
                continue;
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(StringConstants.PATTERN_DATE);
            kloudlessScheduleModel.setBookingDate(simpleDateFormat.parse(fitwiseUtils.formatDate(userKloudlessSchedule.getBookingDate())));
            kloudlessScheduleModel.setSchedulePayload(resource.getData());
            if(userKloudlessSchedule.getMeetingTypeId() != null){
                kloudlessScheduleModel.setFitwiseMeetingTypeId(userKloudlessSchedule.getMeetingTypeId().getMeetingTypeId());
            }
              kloudlessScheduleModels.add(kloudlessScheduleModel);
          }
          if(kloudlessScheduleModels.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS,"",null);
          }
        return kloudlessScheduleModels;
    }

    public Map<String, Object> findAvailability(FindAvailabilityRequest availabilityRequest) {
        ValidationUtils.throwException(availabilityRequest == null,
                CalendarConstants.CAL_ERR_FIND_AVAILABILITY_REQUEST_INVALID, Constants.BAD_REQUEST);

        ValidationUtils.throwException(availabilityRequest.getFitwiseMeetingId() == 0,
                CalendarConstants.CAL_ERR_MEETING_INVALID, Constants.BAD_REQUEST);

        if (availabilityRequest.getAvailabilityTimeWindows() == null
                || availabilityRequest.getAvailabilityTimeWindows().isEmpty()) {
            ValidationUtils.throwException(true,
                    CalendarConstants.CAL_ERR_FIND_AVAILABILITY_WINDOW_INVALID, Constants.BAD_REQUEST);
        }

        UserKloudlessMeeting kloudlessMeeting = userKloudlessMeetingRepository
                .findByUserKloudlessMeetingId(availabilityRequest.getFitwiseMeetingId());
        ValidationUtils.throwException(kloudlessMeeting == null,
                CalendarConstants.CAL_ERR_MEETING_NOT_FOUND, Constants.BAD_REQUEST);

        if (ValidationUtils.isEmptyString(kloudlessMeeting.getMeetingWindow())) {
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_MEETING_NOT_FOUND, null);
        }

        List<TimeWindow> windowList = new ArrayList<>();
        for (AvailabilityTimeWindow window : availabilityRequest.getAvailabilityTimeWindows()) {
            Instant start = null;
            try {
                // expected date time format is 2011-12-03T10:15:30Z
                start = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(window.getStart()));
            } catch (DateTimeParseException dpe) {
                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_START_TIME_INVALID, null);
            }

            Instant end = null;
            try {
                // expected date time format is 2011-12-03T10:15:30Z
                end = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(window.getEnd()));
            } catch (DateTimeParseException dpe) {
                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_END_TIME_INVALID, null);
            }

            if (start.compareTo(end) == 0) {
                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_START_TIME_EQUALS_END_TIME, null);
            }

            if (start.isAfter(end)) {
                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_START_TIME_AFTER_END_TIME, null);
            }

            TimeWindow timeWindow = new TimeWindow();
            timeWindow.setStart(window.getStart());
            timeWindow.setEnd(window.getEnd());
            windowList.add(timeWindow);
        }

        Gson gson = new Gson();
        MeetingWindow meetingWindow = gson.fromJson(kloudlessMeeting.getMeetingWindow(), MeetingWindow.class);

        CalendarAvailabilityRequest calendarAvailabilityRequest = new CalendarAvailabilityRequest();
        calendarAvailabilityRequest.setCalendars(Collections.singletonList(meetingWindow.getBookingCalendarId()));
        calendarAvailabilityRequest.setMeetingDuration(Duration.ofMinutes(meetingWindow.getDuration()).toString());
        calendarAvailabilityRequest.setTimeWindows(windowList);

        ResponseJson response = kloudLessService
                .findAvailabilityInCalendar(kloudlessMeeting.getUserKloudlessCalendar(), calendarAvailabilityRequest);
        ValidationUtils.throwException(response == null, CalendarConstants.CAL_ERR_FIND_AVAILABILITY_FAILED, Constants.ERROR_STATUS);

        return gson.fromJson(response.getData(), new TypeToken<Map<String, Object>>() {
        }.getType());
    }

    public List<CalendarMeetingType> getMeetingTypes() {
        List<CalendarMeetingType> calendarMeetingTypes = calendarMeetingTypeRepository.findAll();
        if(calendarMeetingTypes.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, "", null);
        }
        return calendarMeetingTypes;
    }

    /**
     * Get active kloudless account and token
     * @param user
     * @return
     */
    public UserKloudlessAccount getActiveAccount(User user){
        List<UserKloudlessAccount> userKloudlessAccounts = userKloudlessTokenRepository.findByUser(user);
        if(userKloudlessAccounts.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, CalendarConstants.CAL_ERR_ACCOUNT_NOT_FOUND, null);
        }
        UserKloudlessAccount userKloudlessAccount = null;
        for (UserKloudlessAccount userKloudlessAccount1 : userKloudlessAccounts){
            if (userKloudlessAccount1.isActive() && userKloudlessAccount1.getAccountId() != null){
                userKloudlessAccount = userKloudlessAccount1;
                break;
            }
        }
        if (userKloudlessAccount == null && !userKloudlessAccounts.isEmpty() && userKloudlessAccounts.get(0).getAccountId() != null){
            userKloudlessAccount = userKloudlessAccounts.get(0);
            userKloudlessAccount.setActive(true);
            userKloudlessTokenRepository.save(userKloudlessAccount);
        }
        return userKloudlessAccount;
    }

    public UserKloudlessCalendar getActiveCalendar(User user){
        List<UserKloudlessCalendar> userKloudlessCalendar = userKloudlessCalendarRepository.findByUserAndPrimaryCalendar(user, true);
        if(userKloudlessCalendar.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, CalendarConstants.CAL_ERR_CALENDAR_NOT_FOUND, null);
        }
        return userKloudlessCalendar.get(0);
    }

    public UserKloudlessCalendar getActiveCalendarFromKloudlessAccount(UserKloudlessAccount userKloudlessAccount){
        List<UserKloudlessCalendar> userKloudlessCalendar = userKloudlessCalendarRepository
                .findByUserKloudlessAccountAndPrimaryCalendar(userKloudlessAccount, true);
        if(userKloudlessCalendar.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, CalendarConstants.CAL_ERR_CALENDAR_NOT_FOUND, null);
        }
        return userKloudlessCalendar.get(0);
    }

    /**
     * Checks whether the passed meeting window has passed event id.
     *
     * @param meetingWindow meeting window
     * @param eventId   event id to verify
     * @return {@code true} if the meeting window has the passed event id in it else {@code false}.
     */
    public boolean verifyMeetingWindow(MeetingWindow meetingWindow, String eventId) {
        if (meetingWindow == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_MEETING_NOT_FOUND, null);
        }
        if (ValidationUtils.isEmptyString(eventId)) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_MEETING_NOT_FOUND, null);
        }
        JsonObject eventMetadata = meetingWindow.getDefaultEventMetadata();
        if (eventMetadata != null && eventMetadata.has(StringConstants.JSON_PROPERTY_KEY_CUSTOM_PROPERTIES)) {
            JsonArray customProperties = eventMetadata.get(StringConstants.JSON_PROPERTY_KEY_CUSTOM_PROPERTIES).getAsJsonArray();
            for (JsonElement element : customProperties) {
                JsonObject property = element.getAsJsonObject();
                if (property.has("key")
                        && property.get("key").getAsString().equals("fitwiseMeetingId")
                        && property.has(StringConstants.JSON_PROPERTY_KEY_VALUE)) {
                    String eId = property.get(StringConstants.JSON_PROPERTY_KEY_VALUE).getAsString();
                    return !ValidationUtils.isEmptyString(eId) && eId.equals(eventId);
                }
            }
        }
        return false;
    }

    public KloudlessScheduleModel updateScheduleFromInstructor(KloudlessScheduleModel kloudlessScheduleModel) {
        ValidationUtils.throwException(kloudlessScheduleModel.getFitwiseScheduleId() == null
                        || kloudlessScheduleModel.getFitwiseScheduleId() <= 0,
                CalendarConstants.CAL_ERR_SCHEDULE_ID_INVALID, Constants.BAD_REQUEST);
        User user = userComponents.getUser();
        ValidationUtils.throwException(!fitwiseUtils.isInstructor(user),
                ValidationMessageConstants.MSG_USER_NOT_INSTRUCTOR, Constants.ERROR_STATUS);
        Optional<UserKloudlessSchedule> optionalSchedule = userKloudlessScheduleRepository
                .findById(kloudlessScheduleModel.getFitwiseScheduleId());
        if(!optionalSchedule.isPresent() || optionalSchedule.get() == null){
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_SCHEDULE_NOT_FOUND, null);
        }
        UserKloudlessSchedule userKloudlessSchedule = optionalSchedule.get();
        ValidationUtils.throwException(!userKloudlessSchedule.getUserKloudlessMeeting().getUser().getUserId().equals(user.getUserId())
                ,CalendarConstants.CAL_ERR_SCHEDULE_INSTRUCTOR_NOT_MATCH, Constants.ERROR_STATUS);
        ValidationUtils.throwException(kloudlessScheduleModel.getSchedulePayload() == null,
                CalendarConstants.CAL_ERR_SCHEDULE_EMPTY, Constants.BAD_REQUEST);
        UserKloudlessCalendar userKloudlessCalendar = userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar();
        UserKloudlessAccount userKloudlessAccount = userKloudlessCalendar.getUserKloudlessAccount();
        ValidationUtils.throwException(userKloudlessAccount == null,
                CalendarConstants.CAL_ERR_ACCOUNT_NOT_FOUND, Constants.BAD_REQUEST);
        ObjectMapper objectMapper = new ObjectMapper();
        Gson gson = new Gson();
        Event existingEvent = gson.fromJson(userKloudlessSchedule.getSchedulePayload(), Event.class);
        Event payloadEvent = objectMapper.convertValue(kloudlessScheduleModel.getSchedulePayload(), Event.class);
        Event patchEvent = new Event();
        ValidationUtils.throwException(!ValidationUtils.isEmptyString(payloadEvent.getStart())
                        && ValidationUtils.isEmptyString(payloadEvent.getEnd()),
                CalendarConstants.CAL_ERR_END_TIME_INVALID, Constants.BAD_REQUEST);
        ValidationUtils.throwException(!ValidationUtils.isEmptyString(payloadEvent.getEnd())
                        && ValidationUtils.isEmptyString(payloadEvent.getStart()),
                CalendarConstants.CAL_ERR_START_TIME_INVALID, Constants.BAD_REQUEST);
        if (!ValidationUtils.isEmptyString(payloadEvent.getStart()) && !ValidationUtils.isEmptyString(payloadEvent.getEnd())) {
            LocalDateTime startDateTime;
            LocalDateTime endDateTime;
            // expected date time format is '2011-12-03T10:15:30'
            try {
                startDateTime = LocalDateTime.parse(payloadEvent.getStart());
            } catch (DateTimeParseException dpe) {
                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_START_TIME_INVALID, null);
            }
            // expected date time format is '2011-12-03T10:15:30'
            try {
                endDateTime = LocalDateTime.parse(payloadEvent.getEnd());
            } catch (DateTimeParseException dpe) {
                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_END_TIME_INVALID, null);
            }
            if (startDateTime.compareTo(endDateTime) == 0) {
                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_START_TIME_EQUALS_END_TIME, null);
            }
            if (startDateTime.isAfter(endDateTime)) {
                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_START_TIME_AFTER_END_TIME, null);
            }
            ValidationUtils.throwException(kloudlessScheduleModel.getBookingDate() == null,
                    CalendarConstants.CAL_ERR_BOOKING_DATE_INVALID, Constants.BAD_REQUEST);
            patchEvent.setStart(payloadEvent.getStart());
            patchEvent.setEnd(payloadEvent.getEnd());
        }
        if (!ValidationUtils.isEmptyString(payloadEvent.getName())) {
            patchEvent.setName(payloadEvent.getName());
        }
        if (!ValidationUtils.isEmptyString(payloadEvent.getDescription())) {
            patchEvent.setDescription(payloadEvent.getDescription());
        }
        if (payloadEvent.getAttendees() != null && !payloadEvent.getAttendees().isEmpty()) {
            for (Attendee attendee : payloadEvent.getAttendees()) {
                ValidationUtils.throwException(ValidationUtils.isEmptyString(attendee.getName()),
                        CalendarConstants.CAL_ERR_ATTENDEE_NAME_INVALID, Constants.BAD_REQUEST);
                ValidationUtils.throwException(
                        ValidationUtils.isEmptyString(attendee.getEmail()) || !ValidationUtils.emailRegexValidate(attendee.getEmail()),
                        CalendarConstants.CAL_ERR_ATTENDEE_EMAIL_INVALID, Constants.BAD_REQUEST);
            }
            patchEvent.setAttendees(payloadEvent.getAttendees());
        }
        Optional<PackageKloudlessMapping> optionalPackageSession = packageKloudlessMappingRepository
                .findBySessionMappingId(kloudlessScheduleModel.getPackageSessionMappingId());
        String meetingUrl = null;
        if(optionalPackageSession.isPresent() && optionalPackageSession.get() != null){
            if(CalendarConstants.SESSION_VIRTUAL == userKloudlessSchedule.getMeetingTypeId().getMeetingTypeId()){
                StringBuilder builder = new StringBuilder();
                if (!ValidationUtils.isEmptyString(patchEvent.getDescription())) {
                    builder.append(patchEvent.getDescription());
                }
                meetingUrl = optionalPackageSession.get().getMeetingUrl();
                if (!StringUtils.isEmpty(meetingUrl)) {
                    builder.append(MEETING_INVITE_SEPARATOR);
                    builder.append(meetingUrl);
                    builder.append(MEETING_INVITE_SEPARATOR);
                }
                patchEvent.setLocation(meetingUrl);
                patchEvent.setDescription(builder.toString());
            } else if(CalendarConstants.SESSION_IN_PERSON == userKloudlessSchedule.getMeetingTypeId().getMeetingTypeId()){
                Location location = optionalPackageSession.get().getLocation();
                if (location != null) {
                    String locationText = buildLocationText(location);
                    if (!ValidationUtils.isEmptyString(locationText)) {
                        patchEvent.setLocation(locationText);
                    }
                }
            }
        }
        if (ValidationUtils.isEmptyString(userKloudlessAccount.getService())) {
            userKloudlessAccount = kloudLessService.updateServiceInfo(userKloudlessAccount);
        }
        if (!StringUtils.isEmpty(meetingUrl)) {
            switch (userKloudlessAccount.getService()) {
                case KloudLessService.SERVICE_ICLOUD_CALENDAR:
                    List<Reminder> updatedReminders = new ArrayList<>();
                    if (existingEvent.getReminders() != null && !existingEvent.getReminders().isEmpty()) {
                        for (Reminder existingReminder : existingEvent.getReminders()) {
                            updatedReminders.add(updateReminderProperty(existingReminder,
                                    CalendarConstants.CAL_PROP_ZOOM_MEETING_LINK,
                                    meetingUrl));
                        }
                    }
                    if (!updatedReminders.isEmpty()) {
                        patchEvent.setReminders(updatedReminders);
                    }
                    break;
                case KloudLessService.SERVICE_GOOGLE_CALENDAR:
                case KloudLessService.SERVICE_OUTLOOK_CALENDAR:
                default:
                    CustomProperty customProperty = new CustomProperty();
                    customProperty.setKey(CalendarConstants.CAL_PROP_ZOOM_MEETING_LINK);
                    customProperty.setValue(meetingUrl);
                    customProperty.set_private(true);
                    patchEvent.setCustomProperties(Collections.singletonList(customProperty));
                    break;
            }
        }
        Map<String, Object> updateContent = objectMapper.convertValue(patchEvent, new TypeReference<Map<String, Object>>() {});
        kloudLessService.updateKloudlessSchedule(userKloudlessCalendar, userKloudlessSchedule.getScheduleId(), updateContent);
        Resource updatedEvent = kloudLessService.getKloudlessSchedule(userKloudlessCalendar, userKloudlessSchedule.getScheduleId());
        if (!ValidationUtils.isEmptyString(patchEvent.getStart())) {
            String start = updatedEvent.getData().get(StringConstants.JSON_PROPERTY_KEY_START).getAsString();
            Instant startTime = OffsetDateTime.parse(start).toZonedDateTime()
                    .with(ChronoField.HOUR_OF_DAY, 0)
                    .with(ChronoField.MINUTE_OF_DAY, 0)
                    .with(ChronoField.SECOND_OF_DAY, 0)
                    .withZoneSameInstant(ZoneOffset.UTC).toInstant();
            userKloudlessSchedule.setBookingDate(kloudlessScheduleModel.getBookingDate());
            if (startTime.compareTo(kloudlessScheduleModel.getBookingDate().toInstant())  != 0) {
                userKloudlessSchedule.setBookingDate(Date.from(startTime));
                log.info("update schedule booking date mismatch: replacing booking date with updated schedule start date");
            }
        }
        userKloudlessSchedule.setSchedulePayload(gson.toJson(updatedEvent.getData()));
        //
        userKloudlessSchedule.setIsRescheduled(true);
        //
        if (CalendarConstants.SESSION_VIRTUAL == userKloudlessSchedule.getMeetingTypeId().getMeetingTypeId() && !StringUtils.isEmpty(meetingUrl)) {
            userKloudlessSchedule.setOnlineMeetingEntryUrl(meetingUrl);
        }
        userKloudlessScheduleRepository.save(userKloudlessSchedule);
        kloudlessScheduleModel.setScheduleId(userKloudlessSchedule.getScheduleId());
        kloudlessScheduleModel.setMeetingId(userKloudlessSchedule.getUserKloudlessMeeting().getMeetingId());
        kloudlessScheduleModel.setFitwiseMeetingId(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessMeetingId());
        kloudlessScheduleModel.setFitwiseMeetingTypeId(userKloudlessSchedule.getMeetingTypeId().getMeetingTypeId());
        kloudlessScheduleModel.setSubscriptionPackageId(userKloudlessSchedule.getSubscriptionPackage().getSubscriptionPackageId());
        kloudlessScheduleModel.setSchedulePayload(gson.fromJson(userKloudlessSchedule.getSchedulePayload(), Map.class));
        if (userKloudlessSchedule.getPackageKloudlessMapping() != null) {
            kloudlessScheduleModel.setPackageSessionMappingId(userKloudlessSchedule.getPackageKloudlessMapping().getSessionMappingId());
        }
        kloudlessScheduleModel.setIsRescheduled(userKloudlessSchedule.getIsRescheduled());
        return kloudlessScheduleModel;
    }

    /**
     * Delete the schedule from instructor using schedule id.
     *
     * @param fitwiseScheduleId Schedule Id
     * @return
     */
    public void deleteScheduleFromInstructor(final Long fitwiseScheduleId) {
        User user = userComponents.getUser();
        if(!fitwiseUtils.isInstructor(user)){
            throw new ApplicationException(Constants.ERROR_STATUS, ValidationMessageConstants.MSG_USER_NOT_INSTRUCTOR, null);
        }
        UserKloudlessSchedule userKloudlessSchedule = userKloudlessScheduleRepository.findById(fitwiseScheduleId)
                .orElseThrow(() -> new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_SCHEDULE_NOT_FOUND, null));
        Optional<UserKloudlessMeeting> userKloudlessMeeting = userKloudlessMeetingRepository.findById(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessMeetingId());
        if(!userKloudlessMeeting.isPresent() || !userKloudlessMeeting.get().getUser().getUserId().equals(user.getUserId())){
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_SCHEDULE_INSTRUCTOR_NOT_MATCH, null);
        }
        RefreshAccessTokenResponse refreshAccessTokenResponse = null;
        
	       refreshAccessTokenResponse = cronofyService.refreshAccessToken(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar().getUserKloudlessAccount().getRefreshToken());
	        
	        if (refreshAccessTokenResponse == null) {
		 		throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED,null);
		    }
     
    
           if(!cronofyService.deleteevent(refreshAccessTokenResponse.getAccessToken(),userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar().getCalendarId(),userKloudlessSchedule.getUserKloudlessScheduleId().toString())){
             throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_KLOUDLESS_SCHEDULE_DELETE_FAILED, MessageConstants.ERROR);
           }
           	 User user_member = userKloudlessSchedule.getUser();
           	 String member_Account_Token = "";
			 String member_Calendar_ID = "";
			 UserKloudlessAccount account_member = getActiveAccount(user_member);
			 if(account_member != null){
				 UserKloudlessCalendar activeCalendar_member = getActiveCalendarFromKloudlessAccount(account_member);
				 if(activeCalendar_member != null){
					 member_Account_Token = account_member.getRefreshToken();
			         member_Calendar_ID = activeCalendar_member.getCalendarId();
			     }
			 }
			 if((member_Account_Token.length() > 0) && (member_Calendar_ID.length() > 0 ))
			 {
			    	RefreshAccessTokenResponse member_refreshAccessTokenResponse = null;
			    
			    	member_refreshAccessTokenResponse = cronofyService.refreshAccessToken(member_Account_Token);
			        
			        if (member_refreshAccessTokenResponse == null) {
				 		throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED,null);
				    }
			    
			   
			       if(!cronofyService.deleteevent(member_refreshAccessTokenResponse.getAccessToken(),member_Calendar_ID,userKloudlessSchedule.getUserKloudlessScheduleId().toString())){
			            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_KLOUDLESS_SCHEDULE_DELETE_FAILED, MessageConstants.ERROR);
			        }
			 }

//        ResponseRaw responseRaw = kloudLessService.deleteKloudlessSchedule(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar(), userKloudlessSchedule.getScheduleId());
//        if(responseRaw.getData().getStatusLine().getStatusCode() != 204 && responseRaw.getData().getStatusLine().getStatusCode() != 200){
//            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_KLOUDLESS_SCHEDULE_DELETE_FAILED, null);
//        }
        userKloudlessScheduleRepository.delete(userKloudlessSchedule);
    }

    /**
     * Get instructors all calendars
     *
     * @parm
     * @return
     */
    public List<KloudlessCalendarResponseView> getMyAllCalendars(){
        User user = userComponents.getUser();

        List<UserKloudlessAccount> userKloudlessAccountList = userKloudlessTokenRepository.findByUser(user);

        if(userKloudlessAccountList.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, CalendarConstants.CAL_ERR_ACCOUNT_NOT_FOUND, null);
        }
        List<KloudlessCalendarResponseView> kloudlessCalendarResponseViews = new ArrayList<>();
        for (UserKloudlessAccount userKloudlessAccount : userKloudlessAccountList){
            KloudlessCalendarResponseView kloudlessCalendarResponseView = new KloudlessCalendarResponseView();
            kloudlessCalendarResponseView.setAccountId(userKloudlessAccount.getAccountId());
            kloudlessCalendarResponseView.setAccountEmail(userKloudlessAccount.getAccountEmail());
            kloudlessCalendarResponseView.setAccountToken(userKloudlessAccount.getToken());
            kloudlessCalendarResponseView.setProfileId(userKloudlessAccount.getProfileId());
            kloudlessCalendarResponseView.setActive(userKloudlessAccount.isActive());

            List<UserKloudlessCalendar> userKloudlessCalendars = userKloudlessCalendarRepository.findByUserAndUserKloudlessAccount(user, userKloudlessAccount);
            List<KloudlessCalendarModel> kloudlessCalendarModels;

            if(!userKloudlessCalendars.isEmpty()){
                kloudlessCalendarModels = new ArrayList<>();
                for (UserKloudlessCalendar userKloudlessCalendar : userKloudlessCalendars){
                    KloudlessCalendarModel kloudlessCalendarModel = new KloudlessCalendarModel();
                    kloudlessCalendarModel.setCalendarId(userKloudlessCalendar.getCalendarId());
                    kloudlessCalendarModel.setDefaultCalendar(userKloudlessCalendar.getPrimaryCalendar());
                    if(userKloudlessCalendar.getCalendarName() != null){
                        kloudlessCalendarModel.setCalendarName(userKloudlessCalendar.getCalendarName());
                       }
                 
                    if (userKloudlessCalendar.getProviderName() != null) {
                 	   kloudlessCalendarModel.setProviderName(userKloudlessCalendar.getProviderName());
		  		       }
		  		  
		  		    if (userKloudlessCalendar.getProfileId() != null) {
		  		        	kloudlessCalendarModel.setProfileId(userKloudlessCalendar.getProfileId());
		  		         }
		  		  
		  		    if (userKloudlessCalendar.getProfileName() != null) {
		  		    	   kloudlessCalendarModel.setProfileName(userKloudlessCalendar.getProfileName());
		  		    	   kloudlessCalendarModel.setCalendarName(userKloudlessCalendar.getProfileName());
		  		         }
		  		  
		  		     if (userKloudlessCalendar.getCalendarId() != null) {
		  		    	  kloudlessCalendarModel.setCalendarId(userKloudlessCalendar.getCalendarId());
		  		        }
		  		  
//		  		     if (userKloudlessCalendar.getCalendarName() != null) {
//		  		    	  kloudlessCalendarModel.setCalendarName(userKloudlessCalendar.getCalendarName());
//		  		        }
		  		  
		  		      kloudlessCalendarModel.setCalendarReadonly(userKloudlessCalendar.getCalendarReadonly());
		  		   
		  		   
		  		      kloudlessCalendarModel.setCalendarDeleted(userKloudlessCalendar.getCalendarDeleted());
		  		 
		  		      kloudlessCalendarModel.setCalendarIntegratedConferencingAvailable(userKloudlessCalendar.getCalendarIntegratedConferencingAvailable());
				    
				    
		  		      kloudlessCalendarModel.setCalendarPrimary(userKloudlessCalendar.getPrimaryCalendar());
			       
			    
		  		      kloudlessCalendarModel.setPermissionLevel(userKloudlessCalendar.getPermissionLevel());
                    kloudlessCalendarModels.add(kloudlessCalendarModel);
                }
                kloudlessCalendarResponseView.setKloudlessCalendars(kloudlessCalendarModels);
            }
            kloudlessCalendarResponseViews.add(kloudlessCalendarResponseView);
        }

        return kloudlessCalendarResponseViews;
    }

    /**
     * API to change the calendar account status
     *
     * @parm userKloudlessAccountId, setActive
     * @return
     */
    public void changeCalendarActiveStatus(String userKloudlessAccountId, boolean setActive){
        
    	User user = userComponents.getUser();

        //Validating the userKloudlessAccountId
         if(userKloudlessAccountId == null){
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_ID_NULL, MessageConstants.ERROR);
         }
         List<UserKloudlessAccount> userKloudlessAccountList = userKloudlessTokenRepository.findByUserAndAccountId(user, userKloudlessAccountId);
         if (userKloudlessAccountList.isEmpty()){
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_ID_INVALID, MessageConstants.ERROR);
         }
         UserKloudlessAccount userKloudlessAccount = userKloudlessAccountList.get(0);

         if(setActive){
             userKloudlessAccount.setActive(true);
            
             List<UserKloudlessAccount> existingKloudlessAccounts = userKloudlessTokenRepository.findByUser(user);
             
             for (UserKloudlessAccount existingAccount : existingKloudlessAccounts) {
               
            	 if (existingAccount.getUserKloudlessTokenId().equals(userKloudlessAccount.getUserKloudlessTokenId())) {
                    continue;
                 }
                
            	 existingAccount.setActive(false);
                 userKloudlessTokenRepository.save(existingAccount);
               }
           } else {
            userKloudlessAccount.setActive(false);
        }
        userKloudlessTokenRepository.save(userKloudlessAccount);
    }

    public ResponseModel getSchedules(Date startDate, Date endDate, MemberCalendarFilterModel memberCalendarFilterModel) {
        log.info("Get schedules starts.");
        long apiStartTimeMillis = new Date().getTime();
        User user = userComponents.getUser();
        String member_Account_Token = "";
        String member_Calendar_ID = "";
		 UserKloudlessAccount account_member = getActiveAccount(user);
		 if(account_member != null){
			 UserKloudlessCalendar activeCalendar_member = getActiveCalendarFromKloudlessAccount(account_member);
			 if(activeCalendar_member != null){
				 member_Account_Token = account_member.getRefreshToken();
		         member_Calendar_ID = activeCalendar_member.getCalendarId();
		     }
	     }
        if(!fitwiseUtils.isSameDay(startDate,endDate) && startDate.after(endDate)){
            throw new ApplicationException(Constants.BAD_REQUEST,MessageConstants.MSG_START_DATE_GREATER_END_DATE,MessageConstants.ERROR);
        }
        log.info("Basic validation : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        List<PackageSubscription> packageSubscriptions = packageSubscriptionRepository.findByUserUserIdOrderBySubscribedDateDesc(user.getUserId());
        log.info("Query to get pacakge subscriptions : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        List<PackageSubscription> paidPackageSubscriptions = new ArrayList<>();
       
        for(PackageSubscription packageSubscription : packageSubscriptions) {
            SubscriptionStatus subscriptionStatus = subscriptionService.getMemberPackageSubscriptionStatus(packageSubscription);
            if (subscriptionStatus != null && (subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID) || subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAYMENT_PENDING))) {
                paidPackageSubscriptions.add(packageSubscription);
            }
        }
        log.info("Filter based on active subscriptions : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if(packageSubscriptions == null || packageSubscriptions.isEmpty()){
            throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_MEMBER_NOT_SUBSCRIBED_FOR_ANY_PACKAGE, MessageConstants.ERROR);
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(StringConstants.PATTERN_DATE);
       
        Date scheduleDate = fitwiseUtils.convertToUserTimeZone(startDate);
        
        Date endDateInUserTimezone = fitwiseUtils.convertToUserTimeZone(endDate);
        
        int order = 1;
        TimeZone UNAV_userTimeZone = TimeZone.getTimeZone(fitwiseUtils.getUserTimeZone());
        TimeZone userTimeZone = TimeZone.getTimeZone("Etc/UTC");
        List<MemberScheduleView> memberPackageScheduleDayViews = new ArrayList<>();
       
        log.info("Time zone conversions : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        while (fitwiseUtils.isSameDay(scheduleDate,endDateInUserTimezone) || scheduleDate.before(endDateInUserTimezone)){

            MemberScheduleView memberScheduleDayView = new MemberScheduleView();
            memberScheduleDayView.setDate(simpleDateFormat.format(scheduleDate));
            memberScheduleDayView.setOrder(order);
            ZonedDateTime start = scheduleDate.toInstant().atZone(userTimeZone.toZoneId())
                    .with(ChronoField.HOUR_OF_DAY, 0)
                    .with(ChronoField.MINUTE_OF_DAY, 0)
                    .with(ChronoField.SECOND_OF_DAY, 0);
            ZonedDateTime UNAV_start = scheduleDate.toInstant().atZone(UNAV_userTimeZone.toZoneId())
                    .with(ChronoField.HOUR_OF_DAY, 0)
                    .with(ChronoField.MINUTE_OF_DAY, 0)
                    .with(ChronoField.SECOND_OF_DAY, 0);
            ZonedDateTime UNAV_end = scheduleDate.toInstant().atZone(UNAV_userTimeZone.toZoneId())
                    .with(ChronoField.HOUR_OF_DAY, 23)
                    .with(ChronoField.MINUTE_OF_HOUR, 30)
                    .with(ChronoField.SECOND_OF_MINUTE, 0);


            Date startTimeInUtc = Date.from(start.withZoneSameInstant(ZoneId.systemDefault()).toInstant());
            Date UNAV_startTimeInUtc = Date.from(UNAV_start.withZoneSameInstant(ZoneId.systemDefault()).toInstant());
            Date UNAV_endTimeInUtc = Date.from(UNAV_end.withZoneSameInstant(ZoneId.systemDefault()).toInstant());
            Date endTimeInUtc = Date.from(start.with(LocalTime.MAX).withZoneSameInstant(ZoneId.systemDefault()).toInstant());
            boolean isBookingRestrictedForADay = false;

            int noOfSessionsBookedInADay = 0;
            List<AvailablePackageView> availablePackages =  new ArrayList<>();
            List<SubscriptionPackage> availableSubscriptionPackages = new ArrayList<>();

            for(PackageSubscription packageSubscription : paidPackageSubscriptions){
                Date subscribedDate = packageSubscription.getSubscribedDate();
                Date expiryDate = subscriptionService.getPackageSubscriptionExpiry(packageSubscription);
                Date scheduleDateInUtc = Date.from(scheduleDate.toInstant().atZone(ZoneId.systemDefault()).toInstant());
                if(fitwiseUtils.isSameDay(subscribedDate,scheduleDateInUtc) || (scheduleDateInUtc.after(subscribedDate) && scheduleDate.before(expiryDate))){
                    if(memberCalendarFilterModel.getSubscriptionPackageIds() != null && !memberCalendarFilterModel.getSubscriptionPackageIds().isEmpty()){
                        if(memberCalendarFilterModel.getSubscriptionPackageIds().contains(packageSubscription.getSubscriptionPackage().getSubscriptionPackageId())){
                            availableSubscriptionPackages.add(packageSubscription.getSubscriptionPackage());
                        }
                    }
                }
            }

            int restrictedPackagesForADay = 0;
            int InstructorUnavailableForAWholeDay_count = 0;
            for(SubscriptionPackage subscriptionPackage : availableSubscriptionPackages){
                List<MemberSessionView> memberSessionViews = new ArrayList<>();
                List<PackageKloudlessMapping> packageKloudlessMappingList = subscriptionPackage.getPackageKloudlessMapping();
                boolean isBookingRestrictedForAPackage = false;

                int bookedSessionsForAPackageOnADay = 0;
                AvailablePackageView availablePackageView = new AvailablePackageView();
                availablePackageView.setSubscriptionPackageId(subscriptionPackage.getSubscriptionPackageId());
                availablePackageView.setTitle(subscriptionPackage.getTitle());
                UserProfile instructorProfile = userProfileRepository.findByUser(subscriptionPackage.getOwner());
                availablePackageView.setInstructorName(instructorProfile.getFirstName() + KeyConstants.KEY_SPACE + instructorProfile.getLastName());
                List<InstructorUnavailabilityMemberView> instructorUnavailabilityMemberViews = new ArrayList<>();
                boolean isInstructorUnavailableForAWholeDay = false;
                int unavialabilityCount = 0;
                for(PackageKloudlessMapping packageKloudlessMapping : subscriptionPackage.getPackageKloudlessMapping()){
                    UserKloudlessCalendar userKloudlessCalendar = packageKloudlessMapping.getUserKloudlessMeeting().getUserKloudlessCalendar();
                    if(userKloudlessCalendar != null){
                        isInstructorUnavailableForAWholeDay = instructorUnavailabilityService.checkInstructorUnavailabilityForADay(UNAV_startTimeInUtc,UNAV_endTimeInUtc,userKloudlessCalendar);
                        if (isInstructorUnavailableForAWholeDay) {
                            unavialabilityCount++;
                        }
                        List<InstructorUnavailabilityMemberView> instructorUnavailabilityMemberViewsTemp = instructorUnavailabilityService.constructInstructorUnavailability(UNAV_startTimeInUtc,UNAV_endTimeInUtc,userKloudlessCalendar);
                        instructorUnavailabilityMemberViews.addAll(instructorUnavailabilityMemberViewsTemp);
                    }
                }
                
                if (unavialabilityCount > 0) {
                    isInstructorUnavailableForAWholeDay = true;
                    InstructorUnavailableForAWholeDay_count++;
                }


                availablePackageView.setInstructorUnavailableForAWholeDay(isInstructorUnavailableForAWholeDay);
                List<Long> memberViewIdList = new ArrayList<>();
                List<InstructorUnavailabilityMemberView> memberViewList = new ArrayList<>();
                for (InstructorUnavailabilityMemberView memberView : instructorUnavailabilityMemberViews) {
                    if (!memberViewIdList.contains(memberView.getInstructorUnavailabilityId())) {
                        memberViewIdList.add(memberView.getInstructorUnavailabilityId());
                        memberViewList.add(memberView);
                    }
                 }

                availablePackageView.setInstructorUnavailabilities(memberViewList);


                PackageSubscription packageSubscription = packageSubscriptionRepository.findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageIdOrderBySubscribedDateDesc(user.getUserId(),subscriptionPackage.getSubscriptionPackageId());
                availablePackageView.setSubscribedDate(packageSubscription.getSubscribedDate());
                availablePackageView.setSubscribedDateFormatted(fitwiseUtils.formatDate(packageSubscription.getSubscribedDate()));
                Date expiryDate = subscriptionService.getPackageSubscriptionExpiry(packageSubscription);
                availablePackageView.setExpiryDate(expiryDate);
                availablePackageView.setExpiryDateFormatted(fitwiseUtils.formatDate(expiryDate));
                CancellationDurationModel cancellationDurationModel = fitwiseUtils.constructCancellationDurationModel(subscriptionPackage.getCancellationDuration());
                if(cancellationDurationModel != null){
                    availablePackageView.setCancellationDuration(cancellationDurationModel);
                }
                if(isInstructorUnavailableForAWholeDay){
                    isBookingRestrictedForAPackage = true;
                    restrictedPackagesForADay++;
                }

                if(!isBookingRestrictedForAPackage){
                    for(PackageKloudlessMapping packageKloudlessMapping : packageKloudlessMappingList){
                    	
                    	 log.info("Query to get cronofy availabilityrules : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
          		         profilingEndTimeMillis = new Date().getTime();
          			  
          		         List<CronofyAvailabilityRules> cronofyAvailabilityRules = cronofyAvailabilityRulesRepository
          	        		   .findByUserKloudlessMeeting(packageKloudlessMapping.getUserKloudlessMeeting());
          			  
          			     log.info("Query to get cronofy availabilityrules : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
          		         profilingEndTimeMillis = new Date().getTime();
          			  
          		       
          		         CronofyMeetingModel cronofyMeetingModel = new CronofyMeetingModel();
                       
          			   
          			      for(CronofyAvailabilityRules cronofyAvailability : cronofyAvailabilityRules){
                    	
                    	      cronofyMeetingModel.setCronofyavailabilityrulesid(cronofyAvailability.getUserCronofyAvailabilityRulesId());
          		           }
          			   
          			      CronofyAvailabilityRules cronofyAvailabilityRule = cronofyAvailabilityRulesRepository
    		                    .findByUserCronofyAvailabilityRulesId(cronofyMeetingModel.getCronofyavailabilityrulesid());
          			      if(cronofyAvailabilityRule.getWeeklyPeriods() == null){
                                  continue;
                          }
                       
                    	
                      
                        boolean isBookingRestricted = false;
                        MemberSessionView memberSessionView = new MemberSessionView();
                        memberSessionView.setPackageSessionMappingId(packageKloudlessMapping.getSessionMappingId());
                        memberSessionView.setUserKloudlessMeetingId(packageKloudlessMapping.getUserKloudlessMeeting().getUserKloudlessMeetingId());
                        memberSessionView.setMeetingTitle(packageKloudlessMapping.getTitle());
                        memberSessionView.setMeetingTypeId(packageKloudlessMapping.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingTypeId());
                        memberSessionView.setMeetingType(packageKloudlessMapping.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingType());
                        memberSessionView.setMeetingId(packageKloudlessMapping.getUserKloudlessMeeting().getMeetingId());
                     
                         Set<String> availableDaysList = new HashSet<>();
                         boolean isAvailableDay = false;


                         JSONArray jsonArray = new JSONArray(cronofyAvailabilityRule.getWeeklyPeriods());
                         
                         for (int i = 0; i < jsonArray.length(); i++) {
           		    	   
          		    	     JSONObject jsonObject = jsonArray.getJSONObject(i);
         		    	   
          		    	     String  weeklyDay = jsonObject.getString("day");

                             availableDaysList.add(weeklyDay.trim().substring(0,3).toUpperCase());
                  			  
          		    	  }

                        DayOfWeek dayOfTheWeek = scheduleDate.toInstant().atZone(ZoneId.of(packageKloudlessMapping.getUserKloudlessMeeting().getTimeZone())).getDayOfWeek();
                       
                        String scheduleday = dayOfTheWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).substring(0,3).toUpperCase();
                        
                        if(availableDaysList.contains(scheduleday)){
                            isAvailableDay = true;
                        }
                        
                        
                       List<UserKloudlessSchedule> userKloudlessSchedules = userKloudlessScheduleRepository.findByUserAndPackageKloudlessMappingSessionMappingIdAndBookingDateGreaterThanEqualAndBookingDateLessThanEqual(
                                user,packageKloudlessMapping.getSessionMappingId(),startTimeInUtc,endTimeInUtc);
                     
                       for(UserKloudlessSchedule userKloudlessSchedule : userKloudlessSchedules) {
                    	   
                    	    if(userKloudlessSchedule.getScheduleStartTime() == null) {
                    	    	RefreshAccessTokenResponse refreshAccessTokenResponse = null;
                		        
                 		        refreshAccessTokenResponse = cronofyService.refreshAccessToken(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar().getUserKloudlessAccount().getRefreshToken());
                 		        
                 		        if (refreshAccessTokenResponse == null) {
                 			 		throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED,null);
                 			    }
                 	        
                 	       
                 	           if(!cronofyService.deleteevent(refreshAccessTokenResponse.getAccessToken(),userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar().getCalendarId(),userKloudlessSchedule.getUserKloudlessScheduleId().toString())){
                 	                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_KLOUDLESS_SCHEDULE_DELETE_FAILED, MessageConstants.ERROR);
                 	            }
                 	        
                 	           if((member_Account_Token.length() > 0) && (member_Calendar_ID.length() > 0 ))
	  	                        {
	  	               	        	RefreshAccessTokenResponse member_refreshAccessTokenResponse = null;
	  	              		        
	  	               	        	member_refreshAccessTokenResponse = cronofyService.refreshAccessToken(member_Account_Token);
	  	               		        
	  	               		        if (member_refreshAccessTokenResponse == null) {
	  	               			 		throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED,null);
	  	               			    }
	  	               	        
	  	               	       
	  	               	           if(!cronofyService.deleteevent(member_refreshAccessTokenResponse.getAccessToken(),member_Calendar_ID,userKloudlessSchedule.getUserKloudlessScheduleId().toString())){
	  	               	                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_KLOUDLESS_SCHEDULE_DELETE_FAILED, MessageConstants.ERROR);
	  	               	            }
	  	                        }

                 	        
                 	           userKloudlessScheduleRepository.delete(userKloudlessSchedule);
                    	    }
                       }
                       
                       List<UserKloudlessSchedule> userKloudlessSchedulesafterdelete = userKloudlessScheduleRepository.findByUserAndPackageKloudlessMappingSessionMappingIdAndBookingDateGreaterThanEqualAndBookingDateLessThanEqual(
                               user,packageKloudlessMapping.getSessionMappingId(),startTimeInUtc,endTimeInUtc);
                      
                       List<ScheduleView> scheduleViews = new ArrayList<>();
                      
                       for(UserKloudlessSchedule userKloudlessSchedule : userKloudlessSchedulesafterdelete) {
                            ScheduleView scheduleView = new ScheduleView();
                            if(packageKloudlessMapping.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingTypeId() == 3 && userKloudlessSchedule.getMeetingTypeId() != null){
                                scheduleView.setSelectedMeetingTypeIdInSchedule(userKloudlessSchedule.getMeetingTypeId().getMeetingTypeId());
                                scheduleView.setMeetingType(userKloudlessSchedule.getMeetingTypeId().getMeetingType());
                            }
                            boolean isCompleted = false;
                            scheduleView.setUserKloudlessScheduleId(userKloudlessSchedule.getUserKloudlessScheduleId());
                           
                            CronofyschedulePayload cronofyschedulePayload = new CronofyschedulePayload();
                            
                            cronofyschedulePayload.setAccountId(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar().getUserKloudlessAccount().getAccountId());
                            cronofyschedulePayload.setCalendarId(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar().getCalendarId());
                                          
                            SchedulePayloadcreator schedulePayloadcreator = new SchedulePayloadcreator();
                              schedulePayloadcreator.setEmail(user.getEmail());
                              cronofyschedulePayload.setCreator(schedulePayloadcreator);
                                          
                            SchedulePayloadorganizer schedulePayloadorganizer =new SchedulePayloadorganizer();
                               schedulePayloadorganizer.setEmail(user.getEmail());
                               cronofyschedulePayload.setOrganizer(schedulePayloadorganizer); 
                                            
                               cronofyschedulePayload.setCreated(userKloudlessSchedule.getUserKloudlessMeeting().getStartDateInUtc().toString());
                               cronofyschedulePayload.setModified(userKloudlessSchedule.getUserKloudlessMeeting().getEndDateInUtc().toString());
                                            
                                   
                                cronofyschedulePayload.setStart(userKloudlessSchedule.getScheduleStartTime());
                                cronofyschedulePayload.setStartimezone(userKloudlessSchedule.getUserKloudlessMeeting().getTimeZone());

                                cronofyschedulePayload.setEnd(userKloudlessSchedule.getScheduleEndTime());
                                cronofyschedulePayload.setEndtimezone(userKloudlessSchedule.getUserKloudlessMeeting().getTimeZone());
                                     
                                cronofyschedulePayload.setName(packageKloudlessMapping.getTitle());
                               
                                if(packageKloudlessMapping.getLocation() != null){
                                    cronofyschedulePayload.setLocation(packageKloudlessMapping.getLocation().getCity()+","+packageKloudlessMapping.getLocation().getState()+","+packageKloudlessMapping.getLocation().getCountry().getCountryName());  
                             	}else{
                             		cronofyschedulePayload.setLocation("");                                    	 
                              	}
                                   
                                List<SchedulePayloadcustomproperties> payloadcustomproperties = new ArrayList<>();
                                           
                                SchedulePayloadcustomproperties SPCP_country = new SchedulePayloadcustomproperties();
                                             
                                       SPCP_country.setKey("country");
                                       if(packageKloudlessMapping.getLocation() != null){
                                           SPCP_country.setValue(packageKloudlessMapping.getLocation().getCountry().getCountryName());
                                    	}else{
                                            SPCP_country.setValue("");                                	 
                                     	}
                                       SPCP_country.setPrivate(true);
                                                    
                                      payloadcustomproperties.add(SPCP_country);
                                       
                                SchedulePayloadcustomproperties SPCP_address = new SchedulePayloadcustomproperties();        
                                       
                                              SPCP_address.setKey("address");
                                              	if(packageKloudlessMapping.getLocation() != null){
                                                    SPCP_address.setValue(packageKloudlessMapping.getLocation().getAddress());
                                           		}else{
                                                    SPCP_address.setValue("");                                  	 
                                            	}
                                              SPCP_address.setPrivate(true);
                                            
                                     payloadcustomproperties.add(SPCP_address);
                                      
                               SchedulePayloadcustomproperties SPCP_fitwiseMeetingId = new SchedulePayloadcustomproperties();        
                                      
                                               SPCP_fitwiseMeetingId.setKey("fitwiseMeetingId");
                                               SPCP_fitwiseMeetingId.setValue(packageKloudlessMapping.getUserKloudlessMeeting().getUserKloudlessMeetingId().toString());
                                               SPCP_fitwiseMeetingId.setPrivate(true);
                                         
                                               payloadcustomproperties.add(SPCP_fitwiseMeetingId);
                                   
                                  SchedulePayloadcustomproperties SPCP_city = new SchedulePayloadcustomproperties();        
                                   
                                               SPCP_city.setKey("city");
                                               if(packageKloudlessMapping.getLocation() != null){
                                                   SPCP_city.setValue(packageKloudlessMapping.getLocation().getCity());
                                            	}else{
                                                    SPCP_city.setValue("");                                     	 
                                             	}
                                               SPCP_city.setPrivate(true);
                                      
                                                payloadcustomproperties.add(SPCP_city);
                                 
                                   SchedulePayloadcustomproperties SPCP_meetingWindowId = new SchedulePayloadcustomproperties();        
                                
                                                 SPCP_meetingWindowId.setKey("meetingWindowId");
                                                 SPCP_meetingWindowId.setValue("");
                                                 SPCP_meetingWindowId.setPrivate(true);
                                                 payloadcustomproperties.add(SPCP_meetingWindowId);
                                           
                                   SchedulePayloadcustomproperties SPCP_sessionName = new SchedulePayloadcustomproperties();        
                                           
                                                 SPCP_sessionName.setKey("sessionName");
                                                 SPCP_sessionName.setValue(packageKloudlessMapping.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingType());
                                                 SPCP_sessionName.setPrivate(true);
                                                 payloadcustomproperties.add(SPCP_sessionName);
                                               
                                   SchedulePayloadcustomproperties SPCP_packageId = new SchedulePayloadcustomproperties();        
                                               
                                                     SPCP_packageId.setKey("packageId");
                                                     SPCP_packageId.setValue(packageKloudlessMapping.getSubscriptionPackage().getSubscriptionPackageId().toString());
                                                     SPCP_packageId.setPrivate(true);
                                                 payloadcustomproperties.add(SPCP_packageId);
                                               
                                   SchedulePayloadcustomproperties SPCP_fitwiseScheduleId = new SchedulePayloadcustomproperties();        
                                               
                                                     SPCP_fitwiseScheduleId.setKey("fitwiseScheduleId");
                                                     SPCP_fitwiseScheduleId.setValue(userKloudlessSchedule.getUserKloudlessScheduleId().toString());
                                                     SPCP_fitwiseScheduleId.setPrivate(true);
                                                   payloadcustomproperties.add(SPCP_fitwiseScheduleId);
                                               
                                    SchedulePayloadcustomproperties SPCP_packageTitle = new SchedulePayloadcustomproperties();        
                                               
                                                     SPCP_packageTitle.setKey("packageTitle");
                                                     SPCP_packageTitle.setValue(packageKloudlessMapping.getTitle());
                                                     SPCP_packageTitle.setPrivate(true);
                                              
                                                   payloadcustomproperties.add(SPCP_packageTitle);
                                                
                                     SchedulePayloadcustomproperties SPCP_zipcode = new SchedulePayloadcustomproperties();        
                                               
                                                       SPCP_zipcode.setKey("zipcode");
                                                       if(packageKloudlessMapping.getLocation() != null){
                                                           SPCP_zipcode.setValue(packageKloudlessMapping.getLocation().getZipcode());
                                                    	}else{
                                                            SPCP_zipcode.setValue("");                                      	 
                                                     	}
                                                       SPCP_zipcode.setPrivate(true);
                                              
                                                   payloadcustomproperties.add(SPCP_zipcode);
                                               
                                     SchedulePayloadcustomproperties SPCP_sessionType = new SchedulePayloadcustomproperties();        
                                                 
                                                           SPCP_sessionType.setKey("sessionType");
                                                           SPCP_sessionType.setValue(packageKloudlessMapping.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingTypeId().toString());
                                                           SPCP_sessionType.setPrivate(true);
                                             
                                                   payloadcustomproperties.add(SPCP_sessionType);
                                                  
                                      SchedulePayloadcustomproperties SPCP_state = new SchedulePayloadcustomproperties();        
                                                  
                                                        SPCP_state.setKey("state");
                                                        if(packageKloudlessMapping.getLocation() != null){
                                                            SPCP_state.setValue(packageKloudlessMapping.getLocation().getState());
                                                     	}else{
                                                            SPCP_state.setValue("");                                     	 
                                                      	}
                                                        SPCP_state.setPrivate(true);
                                             
                                                   payloadcustomproperties.add(SPCP_state);
                                                
                                       SchedulePayloadcustomproperties SPCP_landmark = new SchedulePayloadcustomproperties();        
                                                  
                                                         SPCP_landmark.setKey("landmark");
                                                         if(packageKloudlessMapping.getLocation() != null){
                                                        	 if(packageKloudlessMapping.getLocation().getLandMark() != null){
                                                            	   SPCP_landmark.setValue(packageKloudlessMapping.getLocation().getLandMark());
                                                          	}else{
                                                           	   SPCP_landmark.setValue("");                                        	 
                                                           	}
                                                         }else{
                                                      	   SPCP_landmark.setValue("");
                                                         }
                                                         SPCP_landmark.setPrivate(true);
                                             
                                                    payloadcustomproperties.add(SPCP_landmark);
                                                
                                       SchedulePayloadcustomproperties SPCP_sessionNameInPackage = new SchedulePayloadcustomproperties();        
                                                   
                                                         SPCP_sessionNameInPackage.setKey("sessionNameInPackage");
                                                         SPCP_sessionNameInPackage.setValue(packageKloudlessMapping.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingType());
                                                         SPCP_sessionNameInPackage.setPrivate(true);
                                             
                                                    payloadcustomproperties.add(SPCP_sessionNameInPackage);
                                                  
                                        SchedulePayloadcustomproperties SPCP_isFitwiseEvent = new SchedulePayloadcustomproperties();        
                                                   
                                                        SPCP_isFitwiseEvent.setKey("isFitwiseEvent");
                                                        SPCP_isFitwiseEvent.setValue("");
                                                        SPCP_isFitwiseEvent.setPrivate(true);
                                             
                                                    payloadcustomproperties.add(SPCP_isFitwiseEvent);
                                                 
                                        SchedulePayloadcustomproperties SPCP_zoomMeetingLink = new SchedulePayloadcustomproperties();        
                                                    
                                      				SPCP_zoomMeetingLink.setKey("zoomMeetingLink");
                                                    if(packageKloudlessMapping.getMeetingUrl() != null){
                                                    	SPCP_zoomMeetingLink.setValue(packageKloudlessMapping.getMeetingUrl());
                                                    }else{
                                                    	SPCP_zoomMeetingLink.setValue("");
                                                    }
                                                    SPCP_zoomMeetingLink.setPrivate(true);
                                        
                                                    payloadcustomproperties.add(SPCP_zoomMeetingLink);      
                                                 
                                              
                                      cronofyschedulePayload.setCustom_properties(payloadcustomproperties);
                                      Gson gson = new Gson();
                                      scheduleView.setSchedulePayload(gson.toJson(cronofyschedulePayload));
                                     

                            scheduleView.setIsRescheduled(userKloudlessSchedule.getIsRescheduled());
                            
                            scheduleViews.add(scheduleView);
                            String now = fitwiseUtils.formatDate(new Date());
                            Date currentDateInUserTimeZone = fitwiseUtils.constructDate(now);
                            if (scheduleDate.before(currentDateInUserTimeZone)) {
                                isCompleted = true;
                            }
                            memberSessionView.setCompleted(isCompleted);
                            noOfSessionsBookedInADay++;
                        }
                        if(userKloudlessSchedules.size() > 1){
                            isBookingRestricted = true;
                        }
                        memberSessionView.setSchedules(scheduleViews);
                        int availableSessions = 0;
                        Date meetingStartDateInUtc = packageKloudlessMapping.getUserKloudlessMeeting().getStartDateInUtc();
                        Date meetingEndDateInUtc = packageKloudlessMapping.getUserKloudlessMeeting().getEndDateInUtc();
                        Date meetingStartDate = fitwiseUtils.convertToUserTimeZone(meetingStartDateInUtc);
                        Date meetingEndDate = fitwiseUtils.convertToUserTimeZone(meetingEndDateInUtc);

                        if(isAvailableDay && (fitwiseUtils.isSameDay(scheduleDate,meetingStartDate) || fitwiseUtils.isSameDay(scheduleDate,meetingEndDate) || (scheduleDate.before(meetingEndDate) && scheduleDate.after(meetingStartDate)))){
                            int bookedSessions = userKloudlessScheduleRepository.countByUserAndPackageKloudlessMappingSessionMappingId(
                                    user,packageKloudlessMapping.getSessionMappingId());
                            int totalSessions = packageKloudlessMapping.getTotalSessionCount();
                            availableSessions = totalSessions-bookedSessions;
                            if(availableSessions <= 0){
                                isBookingRestricted = true;
                            }
                        }else{
                            isBookingRestricted = true;
                        }

                        if(isBookingRestricted){
                            bookedSessionsForAPackageOnADay++;
                        }
                        memberSessionView.setBookingRestricted(isBookingRestricted);
                        memberSessionView.setNoOfAvailableSessions(availableSessions);
                        memberSessionViews.add(memberSessionView);
                    }
                    availablePackageView.setSessions(memberSessionViews);
                    if(bookedSessionsForAPackageOnADay == packageKloudlessMappingList.size()){
                        isBookingRestrictedForAPackage = true;
                        restrictedPackagesForADay++;
                    }
                }
                availablePackageView.setBookingRestrictedForAPackage(isBookingRestrictedForAPackage);
                availablePackages.add(availablePackageView);
            }
            if(restrictedPackagesForADay == availableSubscriptionPackages.size()){
                isBookingRestrictedForADay = true;
            }
            memberScheduleDayView.setAvailablePackages(availablePackages);
            memberScheduleDayView.setBookingRestrictedForADay(isBookingRestrictedForADay);
            memberScheduleDayView.setNoOfSessionsBookedInADay(noOfSessionsBookedInADay);
            if(InstructorUnavailableForAWholeDay_count == availableSubscriptionPackages.size()) {
                memberScheduleDayView.setInstructorUnavailableForAWholeDay(true);
             }else {
                 memberScheduleDayView.setInstructorUnavailableForAWholeDay(false);
 
             }
            memberPackageScheduleDayViews.add(memberScheduleDayView);
            Calendar cal = Calendar.getInstance();
            cal.setTime(scheduleDate);
            cal.add(Calendar.DATE, 1);
            scheduleDate = cal.getTime();
            order++;
        }
        log.info("Construct member package schedule day views : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get schedules ends.");
        return  new ResponseModel(Constants.SUCCESS_STATUS,MessageConstants.MSG_DATA_RETRIEVED,memberPackageScheduleDayViews);
    }

    @Transactional
    public void deleteInstructorCalendarAccount(String accountId){
        log.info("Delete instructor calendar account starts.");
        long apiStartTimeMillis = new Date().getTime();
        User user = userComponents.getUser();


        if (!fitwiseUtils.isInstructor(user)){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_INSTRUCTOR, MessageConstants.ERROR);
        }

        //Validating the inputs
        if (accountId == null || ValidationUtils.isEmptyString(accountId)){
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_ID_NULL, MessageConstants.ERROR);
        }
        List<UserKloudlessAccount> userKloudlessAccountList = userKloudlessTokenRepository.findByUserAndAccountId(user, accountId);
        if (userKloudlessAccountList.isEmpty()){
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_ID_INVALID, MessageConstants.ERROR);
        }
        UserKloudlessAccount userKloudlessAccount = userKloudlessAccountList.get(0);

        if (userKloudlessAccount.isActive()){
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.MSG_ERR_DELETE_ACTIVE_CALENDAR_ACCOUNT, MessageConstants.ERROR);
        }
        log.info(StringConstants.LOG_FIELD_VALIDATION + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        List<UserKloudlessCalendar> userKloudlessCalendarList = userKloudlessCalendarRepository.findByUserAndUserKloudlessAccount(user, userKloudlessAccount);
        log.info("Query to get user kloudless calendar list : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        boolean isCalendarAccountUsedInPackages = true;
        int noOfTimesUsed = 0;
        if (userKloudlessCalendarList.isEmpty()){
            isCalendarAccountUsedInPackages = false;
        } else {
            for (UserKloudlessCalendar userKloudlessCalendar : userKloudlessCalendarList){

                List<UserKloudlessMeeting> userKloudlessMeetingList = userKloudlessMeetingRepository.findByUserAndUserKloudlessCalendar(user, userKloudlessCalendar);
                if (userKloudlessMeetingList.isEmpty()){
                    isCalendarAccountUsedInPackages = false;
                } else {

                    for (UserKloudlessMeeting userKloudlessMeeting: userKloudlessMeetingList){
                        List<UserKloudlessSchedule> userKloudlessScheduleList = userKloudlessScheduleRepository.findByUserKloudlessMeeting(userKloudlessMeeting);

                        if (userKloudlessScheduleList.isEmpty()){
                            isCalendarAccountUsedInPackages = false;
                        } else {
                            noOfTimesUsed++;
                        }
                    }
                }
            }
        }
        log.info("Check whether the calendar used in packages or not : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if (!isCalendarAccountUsedInPackages && noOfTimesUsed == 0){

            UserKloudlessSubscription userKloudlessSubscription = userKloudlessSubscriptionRepository.findByUserKloudlessAccountUserKloudlessTokenId(userKloudlessAccount.getUserKloudlessTokenId());
            log.info("Query to get user kloudless subscription : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            if (userKloudlessSubscription != null){
                userKloudlessSubscriptionRepository.delete(userKloudlessSubscription);
                log.info("Query to delete user kloudless subscription : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                profilingEndTimeMillis = new Date().getTime();
            }

            List<UserKloudlessCalendar> userKloudlessCalendarListDelete = userKloudlessCalendarRepository.findByUserAndUserKloudlessAccount(user, userKloudlessAccount);
            log.info("Query to user kloudless calendar list : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();

            if (!userKloudlessCalendarListDelete.isEmpty()){
                for (UserKloudlessCalendar userKloudlessCalendar : userKloudlessCalendarListDelete){
                    List<UserKloudlessMeeting> userKloudlessMeetingList = userKloudlessMeetingRepository.findByUserAndUserKloudlessCalendar(user, userKloudlessCalendar);

                    if (!userKloudlessMeetingList.isEmpty()){
                        for (UserKloudlessMeeting userKloudlessMeeting : userKloudlessMeetingList){
                            //delete user kloudless meeting from kloudless
                            kloudLessService.deleteMeetingWindow(userKloudlessAccount.getToken(), userKloudlessMeeting.getMeetingId());

                            userKloudlessMeetingRepository.delete(userKloudlessMeeting);
                        }
                    }
                    //delete the user kloudless calendar
                    userKloudlessCalendarRepository.delete(userKloudlessCalendar);
                }
            }
            log.info("Query to delete user kloudless calendars : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            if(!kloudLessService.deleteAccount(userKloudlessAccount)){
                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.MSG_ERR_DELETE_FAILED, MessageConstants.ERROR);
            }
            userKloudlessTokenRepository.deleteById(userKloudlessAccount.getUserKloudlessTokenId());
            log.info("Delete delete user kloudless account from kloudless service and DB : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        } else {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.MSG_CAL_DELETE_PACKAGE_USER_ACCOUNT, MessageConstants.ERROR);
        }
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Delete instructor calendar account ends.");

    }

    /**
     * Verifies payload with signature and returns true if payload matches the signature else false.
     * @param payload   webhook payload
     * @param signature signature passed on header
     * @param verifier  kloudless api key
     * @return true if payload matches the signature else false.
     */
    private boolean verifyKloudlessWebhook(String payload, String signature, String verifier) {
        if (ValidationUtils.isEmptyString(payload)
                || ValidationUtils.isEmptyString(signature)
                || ValidationUtils.isEmptyString(verifier)) {
            return false;
        }
        try {
            SecretKeySpec secretKey = new SecretKeySpec(verifier.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            String hash = Base64.getEncoder().encodeToString(mac.doFinal(payload.getBytes()));
            return hash.equals(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return false;
        }
    }

    /**
     * Handles kloudless webhook.
     * @param payload   webhook payload
     * @param signature signature
     * @return
     */
    public String processWebhook(String payload, String signature) {
        log.info("Process webhook starts.");
        long apiStartTimeMillis = new Date().getTime();
        if (ValidationUtils.isEmptyString(payload)) {
            log.error("kloudless webhook handler: payload is empty");
            return "";
        }
        if (ValidationUtils.isEmptyString(signature)) {
            log.error("kloudless webhook handler: signature is empty");
            return "";
        }
        if (!verifyKloudlessWebhook(payload, signature, kloudlessProperties.getKloudlessAPIKey())) {
            log.error("kloudless webhook handler: signature verification failed");
            return "";
        }
        log.info(StringConstants.LOG_FIELD_VALIDATION + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        Gson gson = new Gson();
        WebhookNotification webhookNotification = gson.fromJson(payload, WebhookNotification.class);
        // if it's a test webhook event from kloudless send the application id
        if (webhookNotification == null
                || (webhookNotification.getAccount() == null && webhookNotification.getSubscription() == null)) {
            return kloudlessProperties.getApplicationId();
        }
        log.info("Convert payload to  : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        UserKloudlessAccount userKloudlessAccount = userKloudlessTokenRepository
                .findTop1ByAccountId(String.valueOf(webhookNotification.getAccount()));
        log.info("Query to get user kloudless account  : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if (userKloudlessAccount == null) {
            log.info("kloudless webhook handler: account not found for id " + webhookNotification.getAccount());
            log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
            return "OK";
        }
        if (!userKloudlessAccount.isActive()) {
            log.info("kloudless webhook handler: account id " + webhookNotification.getAccount() + "is inactive ");
            log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
            return "OK";
        }
        Optional<UserKloudlessSubscription> optionalSubscription = userKloudlessSubscriptionRepository
                .findByUserKloudlessAccountAndSubscriptionId(userKloudlessAccount, webhookNotification.getSubscription());
        log.info("Query to get user kloudless subscription  : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if (!optionalSubscription.isPresent()) {
            log.info("kloudless webhook handler: subscription not found for account id " + userKloudlessAccount.getAccountId());
            log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
            return "OK";
        }
        if (ValidationUtils.isEmptyString(userKloudlessAccount.getService())) {
            userKloudlessAccount = kloudLessService.updateServiceInfo(userKloudlessAccount);
            log.info("Query to get user kloudless account  : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        }
        if (KloudLessService.SERVICE_OUTLOOK_CALENDAR.equals(userKloudlessAccount.getService())) {
            log.info("kloudless webhook handler: skipping get activity since it's an outlook account");
            log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
            return "OK";
        }
        profilingEndTimeMillis = new Date().getTime();
        UserKloudlessSubscription subscription = optionalSubscription.get();
        ResponseJson response = kloudLessService.getSubscriptionActivities(userKloudlessAccount,
                String.valueOf(subscription.getSubscriptionId()),
                subscription.getLastCursor());
        log.info("Get subscription activities  : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        JsonObject data = response.getData();
        String cursor = "";
        List<Activity> activities = new ArrayList<>();
        if (data.has("type") && "object_list".equals(data.get("type").getAsString())) {
            JsonArray array = data.get("objects").getAsJsonArray();
            log.info("kloudless webhook handler: fetched activity size: " + array.size());
            cursor = data.get(StringConstants.JSON_PROPERTY_KEY_CURSOR) == null || data.get(StringConstants.JSON_PROPERTY_KEY_CURSOR).isJsonNull() ? ""
                    : data.get(StringConstants.JSON_PROPERTY_KEY_CURSOR).getAsString();
            for (int i = 0; i < array.size(); i++) {
                JsonObject element = array.get(i).getAsJsonObject();
                activities.add(gson.fromJson(element, Activity.class));
            }
        }
        log.info("Adding kloudless activities : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        for (Activity activity : activities) {
            boolean isValid = true;
            if (activity.getAccount() == null
                    || !String.valueOf(activity.getAccount()).equals(userKloudlessAccount.getAccountId())
                    || activity.getSubscription() == null
                    || !activity.getSubscription().equals(subscription.getSubscriptionId())) {
                log.info("kloudless webhook handler: account or subscription id mismatch");
                isValid = false;
            }
            Event event = gson.fromJson(activity.getMetadata(), Event.class);
            Optional<UserKloudlessSchedule> optionalSchedule = userKloudlessScheduleRepository.findByScheduleId(event.getId());
            if (isValid && !optionalSchedule.isPresent()) {
                log.info("kloudless webhook handler: non fitwise event: schedule not found");
                isValid = false;
            }
            if(!isValid){
                continue;
            }
            try {
                UserKloudlessSchedule schedule = optionalSchedule.get();
                switch (activity.getType()) {
                    case "update":
                        updateUserKloudlessSchedule(schedule, activity.getMetadata());
                        break;
                    case "delete":
                        deleteUserKloudlessSchedule(schedule);
                        break;
                    default:
                        log.info("kloudless webhook handler: skipping activity type " + activity.getType());
                        break;
                }
            } catch (Exception e) {
                log.error("kloudless webhook handler failed", e);
            }
        }
        log.info("Update or delete user kloudless schedule  : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if (!StringUtils.isEmpty(cursor) && !cursor.equals(subscription.getLastCursor())) {
            subscription.setLastCursor(cursor);
            userKloudlessSubscriptionRepository.save(subscription);
            log.info("query to save user kloudless subscription  : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        }
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        return "OK";
    }

    /**
     * Updates the schedule entry if needed updates associated zoom meeting.
     * @param schedule fitwise schedule
     * @param event    updated upstream event
     */
    private void updateUserKloudlessSchedule(UserKloudlessSchedule schedule, JsonObject event) {
        if (schedule == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, "schedule is null", null);
        }
        if (event == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, "event is null", null);
        }

        Gson gson = new Gson();
        Event existingEvent = gson.fromJson(schedule.getSchedulePayload(), Event.class);
        Event upstreamEvent = gson.fromJson(event, Event.class);

        boolean isUpstreamEventTimeChanged = (!existingEvent.getStart().equals(upstreamEvent.getStart())
                || !existingEvent.getEnd().equals(upstreamEvent.getEnd()));

        User instructor = schedule.getSubscriptionPackage().getOwner();
        UserKloudlessCalendar userKloudlessCalendar = schedule.getUserKloudlessMeeting().getUserKloudlessCalendar();
        UserKloudlessAccount userKloudlessAccount = userKloudlessCalendar.getUserKloudlessAccount();
        ValidationUtils.throwException(userKloudlessAccount == null,
                CalendarConstants.CAL_ERR_ACCOUNT_NOT_FOUND, Constants.BAD_REQUEST);

        if (isUpstreamEventTimeChanged) {
            boolean isVirtualSchedule = (CalendarConstants.SESSION_VIRTUAL == schedule.getMeetingTypeId().getMeetingTypeId());

            ZoomMeeting updatedMeeting = null;
            Event patchEvent = new Event();

            if (isVirtualSchedule && schedule.getZoomMeeting() != null) {
                ZoomAccount zoomAccount = null;
                try {
                    zoomAccount = zoomService.getZoomAccount(instructor);
                } catch (ApplicationException ae) {
                    log.error("get zoom account failed for instructor " + instructor.getUserId(), ae);
                }

                if (zoomAccount == null) {
                    log.info("instructor do not have an authenticated zoom account, skipping zoom meeting update");
                }

                if (zoomAccount != null && schedule.getZoomMeeting() != null && userKloudlessCalendar != null) {
                    try {
                        Event updateEvent = new Event();
                        updateEvent.setName(upstreamEvent.getName());
                        OffsetDateTime start = OffsetDateTime.parse(upstreamEvent.getStart());
                        updateEvent.setStart(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(start));
                        OffsetDateTime end = OffsetDateTime.parse(upstreamEvent.getEnd());
                        updateEvent.setEnd(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(end));

                        ZoomMeeting zoomMeeting = schedule.getZoomMeeting();
                        updatedMeeting = updateZoomMeetingFromEvent(userKloudlessCalendar.getUser(),
                                zoomMeeting.getMeetingId(), zoomMeeting.getOccurrenceId(), updateEvent);
                    } catch (ApplicationException ae) {
                        log.error("zoom meeting update failed for instructor " + instructor.getUserId(), ae);
                    }

                    String updatedInvitation = null;
                    try {
                        if (updatedMeeting != null) {
                            updatedInvitation = zoomService
                                    .getMeetingInvitation(zoomAccount, Long.parseLong(updatedMeeting.getMeetingId()));
                        }
                    } catch (ApplicationException ae) {
                        log.error("zoom meeting get invitation failed for meeting " + updatedMeeting.getMeetingId(), ae);
                    }

                    if (updatedMeeting != null) {
                        StringBuilder builder = new StringBuilder();
                        if (!ValidationUtils.isEmptyString(upstreamEvent.getDescription())) {
                            builder.append(upstreamEvent.getDescription().split(MEETING_INVITE_SEPARATOR)[0]);
                        }

                        if (!ValidationUtils.isEmptyString(updatedInvitation)) {
                            builder.append(MEETING_INVITE_SEPARATOR);
                            builder.append(updatedInvitation);
                            builder.append(MEETING_INVITE_SEPARATOR);
                        }

                        patchEvent.setLocation(updatedMeeting.getJoinUrl());
                        patchEvent.setDescription(builder.toString());
                    }
                }

                if (ValidationUtils.isEmptyString(userKloudlessAccount.getService())) {
                    userKloudlessAccount = kloudLessService.updateServiceInfo(userKloudlessAccount);
                }

                if (updatedMeeting != null) {
                    switch (userKloudlessAccount.getService()) {
                        case KloudLessService.SERVICE_ICLOUD_CALENDAR:
                            List<Reminder> updatedReminders = new ArrayList<>();
                            if (existingEvent.getReminders() != null && !existingEvent.getReminders().isEmpty()) {
                                for (Reminder existingReminder : existingEvent.getReminders()) {
                                    updatedReminders.add(updateReminderProperty(existingReminder,
                                            CalendarConstants.CAL_PROP_ZOOM_MEETING_LINK,
                                            updatedMeeting.getJoinUrl()));
                                }
                            }
                            if (!updatedReminders.isEmpty()) {
                                patchEvent.setReminders(updatedReminders);
                            }
                            break;
                        case KloudLessService.SERVICE_GOOGLE_CALENDAR:
                        case KloudLessService.SERVICE_OUTLOOK_CALENDAR:
                        default:
                            CustomProperty customProperty = new CustomProperty();
                            customProperty.setKey(CalendarConstants.CAL_PROP_ZOOM_MEETING_LINK);
                            customProperty.setValue(updatedMeeting.getJoinUrl());
                            customProperty.set_private(true);
                            patchEvent.setCustomProperties(Collections.singletonList(customProperty));
                            break;
                    }
                }
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> updateContent = objectMapper.convertValue(patchEvent, new TypeReference<Map<String, Object>>() {});
                Resource updatedEvent = kloudLessService
                        .updateKloudlessSchedule(userKloudlessCalendar, schedule.getScheduleId(), updateContent);
                if (updatedEvent == null) {
                    throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_SCHEDULE_UPDATE_FAILED, null);
                }
            }
        }
        // get full event details from api
        Resource updatedSchedule = kloudLessService.getKloudlessSchedule(userKloudlessCalendar, schedule.getScheduleId());
        Event updatedEvent = gson.fromJson(updatedSchedule.getData(), Event.class);
        schedule.setSchedulePayload(gson.toJson(updatedSchedule.getData()));
        if (!ValidationUtils.isEmptyString(updatedEvent.getStart())) {
            String start = updatedEvent.getStart();
            Instant startTime = OffsetDateTime.parse(start).toZonedDateTime()
                    .with(ChronoField.HOUR_OF_DAY, 0)
                    .with(ChronoField.MINUTE_OF_DAY, 0)
                    .with(ChronoField.SECOND_OF_DAY, 0)
                    .withZoneSameInstant(ZoneOffset.UTC).toInstant();
            schedule.setBookingDate(Date.from(startTime));
        }
        userKloudlessScheduleRepository.save(schedule);
        log.info("kloudless webhook: updated schedule with id " + schedule.getUserKloudlessScheduleId());
    }

    /**
     * Delete fitwise schedule if needed delete associated zoom meeting.
     * @param schedule fitwise schedule
     */
    private void deleteUserKloudlessSchedule(UserKloudlessSchedule schedule) {
        if (schedule == null) {
            return;
        }
        userKloudlessScheduleRepository.delete(schedule);
        boolean isVirtualSchedule = (CalendarConstants.SESSION_VIRTUAL == schedule.getMeetingTypeId().getMeetingTypeId());
        if (isVirtualSchedule && schedule.getZoomMeeting() != null) {
            User instructor = schedule.getSubscriptionPackage().getOwner();
            ZoomAccount zoomAccount = null;
            try {
                zoomAccount = zoomService.getZoomAccount(instructor);
            } catch (ApplicationException ae) {
                log.error("get zoom account failed for instructor " + instructor.getUserId(), ae);
            }
            if (zoomAccount == null || schedule.getZoomMeeting() == null) {
                log.info("instructor do not have an authenticated zoom account, skipping zoom meeting delete");
            }
            if (zoomAccount != null && schedule.getZoomMeeting() != null) {
                ZoomMeeting zoomMeeting = schedule.getZoomMeeting();
                try {
                    zoomService.deleteMeetingForUser(instructor, zoomMeeting.getMeetingId(), zoomMeeting.getOccurrenceId());
                } catch (ApplicationException ae) {
                    log.error("zoom meeting deletion failed for schedule: " + schedule.getUserKloudlessScheduleId(), ae);
                }
            }
        }
        log.info("kloudless webhook: deleted schedule");
    }

    private UserKloudlessSchedule constructUserKloudlessSchedule(Resource eventResource, Date bookingDate, String type) {
        Gson gson = new Gson();
        UserKloudlessSchedule schedule = new UserKloudlessSchedule();
        schedule.setScheduleId(eventResource.getId());
        schedule.setScheduleType(type);
        schedule.setSchedulePayload(gson.toJson(eventResource.getData()));
        schedule.setBookingDate(bookingDate);
        return schedule;
    }

    private  String buildScheduleTimeString(UserKloudlessSchedule schedule){
        String scheduleTime = "";
        try{
            JSONObject jsonObject = new JSONObject(schedule.getSchedulePayload());
            String start = jsonObject.getString(StringConstants.JSON_PROPERTY_KEY_START);
            String end = jsonObject.getString("end");
            String timeZone = jsonObject.getString(StringConstants.JSON_PROPERTY_KEY_START_TIME_ZONE);
            SimpleDateFormat dateFormat = new SimpleDateFormat(StringConstants.PATTERN_DATE_TIME);
            dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
            Date startTimeInPayload = dateFormat.parse(start);
            Date endTimeInPayload = dateFormat.parse(end);
            log.info("start time in payload: " +startTimeInPayload);
            log.info("end time in payload:" +endTimeInPayload);
            //onvert time to instrutor time zone
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(schedule.getUserKloudlessMeeting().getTimeZone()));
            String startTimeInInstructorTimeZone = simpleDateFormat.format(startTimeInPayload);
            String endTimeInInstructorTimeZone = simpleDateFormat.format(endTimeInPayload);
            log.info("start time in instructor timezone:" +startTimeInInstructorTimeZone);
            log.info("end time in instructor timezone:" +endTimeInInstructorTimeZone);
            String[] startTime = startTimeInInstructorTimeZone.split(" ");
            String[] endTime = endTimeInInstructorTimeZone.split(" ");
            scheduleTime = startTime[0] + " " + startTime[1] +"-" + endTime[1];
        }catch (Exception exception){
            log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
        }
        return  scheduleTime;
    }

}
