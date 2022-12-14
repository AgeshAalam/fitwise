package com.fitwise.service.calendar;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitwise.constants.CalendarConstants;
import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.entity.calendar.UserKloudlessAccount;
import com.fitwise.entity.calendar.UserKloudlessCalendar;
import com.fitwise.exception.ApplicationException;
import com.fitwise.properties.KloudlessProperties;
import com.fitwise.repository.calendar.UserKloudlessTokenRepository;
import com.fitwise.request.kloudless.CalendarAvailabilityRequest;
import com.fitwise.response.kloudless.ErrorResponse;
import com.fitwise.response.kloudless.MeetingWindow;
import com.fitwise.response.kloudless.VerifyTokenResponse;
import com.fitwise.utils.APIBuilder;
import com.fitwise.utils.APIService;
import com.fitwise.utils.ValidationUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kloudless.Account;
import com.kloudless.exceptions.ApiException;
import com.kloudless.exceptions.InvalidArgumentException;
import com.kloudless.models.Resource;
import com.kloudless.models.ResourceList;
import com.kloudless.models.ResponseJson;
import com.kloudless.models.ResponseRaw;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import retrofit2.Response;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class KloudLessService {

    @Autowired
    private UserKloudlessTokenRepository userKloudlessTokenRepository;

    public static final String API_BASE_URL = "https://api.kloudless.com/";
    public static final String API_ENDPOINT_CALENDARS = "/cal/calendars/";
    public static final String API_ENDPOINT_EVENTS = "/events/";

    public static final String SERVICE_GOOGLE_CALENDAR = "google_calendar";
    public static final String SERVICE_OUTLOOK_CALENDAR = "outlook_calendar";
    public static final String SERVICE_ICLOUD_CALENDAR = "icloud_calendar";


    @Autowired
    private KloudlessProperties kloudlessProperties;

    public Resource validateBearerToken(final String token) {
        if (ValidationUtils.isEmptyString(token)) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_INVALID, null);
        }
        APIService apiService = APIBuilder.builder(API_BASE_URL);
        VerifyTokenResponse verifyTokenResponse = null;
        try {
            Response<VerifyTokenResponse> response = apiService.verifyOauthToken(token).execute();
            if (!response.isSuccessful()) {
                Gson gson = new Gson();
                ErrorResponse errorResponse = gson.fromJson(response.errorBody().string(), ErrorResponse.class);
                log.error("kloudless access token verification failed, error code: " + errorResponse.getErrorCode());
                throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED, null);
            }
            verifyTokenResponse = response.body();
        } catch (IOException e) {
            log.error("kloudless access token verification api error", e);
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED, null);
        }
        if (ValidationUtils.isEmptyString(kloudlessProperties.getApplicationId())) {
            log.error("kloudless application id is empty in properties");
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED, null);
        }
        if (!kloudlessProperties.getApplicationId().equals(verifyTokenResponse.getClientId())) {
            log.error("kloudless access token verification failed, invalid app id");
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_INVALID, null);
        }
        Resource resource = null;
        try {
            Account account = new Account(token);
            resource = (Resource) account.get("");
        } catch (Exception exception) {
            log.error("kloudless get account failed", exception);
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED, null);
        }
        return resource;
    }

    public boolean deleteAccount(final UserKloudlessAccount userKloudlessAccount){
        boolean isDeleted = false;
        try {
            Account account = new Account(userKloudlessAccount.getToken());
            account.delete("");
            isDeleted = true;
        } catch (InvalidArgumentException exception) {
            log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
        } catch (ApiException apiException){
            log.info(MessageConstants.MSG_ERR_EXCEPTION + apiException.getMessage());
        }
        return isDeleted;
    }

    public Resource getKloudlessCalendar(final UserKloudlessAccount userKloudlessAccount, final String calendarId) {
        long tempTime = new Date().getTime();
        if (userKloudlessAccount == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_NOT_FOUND, null);
        }
        if (ValidationUtils.isEmptyString(calendarId)) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_CALENDAR_ID_INVALID, null);
        }
        log.info("Validate fields : " + (new Date().getTime() - tempTime));
        Resource resource = null;
        try {
            tempTime = new Date().getTime();
            Account account = new Account(userKloudlessAccount.getToken());
            resource = (Resource) account.get(API_ENDPOINT_CALENDARS + calendarId);
            log.info("Get kloudless calendar : " + (new Date().getTime() - tempTime));
        } catch (Exception exception) {
            log.error("Kloudless get calendar failed", exception);
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_EER_CALENDAR_LINKING_FAILED, null);
        }
        return resource;
    }

    public Resource getKloudlessSchedule(final UserKloudlessCalendar userKloudlessCalendar, final String eventId) {
        Resource resource = null;
        try {
            Account account = new Account(userKloudlessCalendar.getUserKloudlessAccount().getAccountId(), kloudlessProperties.getKloudlessAPIKey());
            resource = (Resource) account.get(API_ENDPOINT_CALENDARS + userKloudlessCalendar.getCalendarId() + API_ENDPOINT_EVENTS + eventId);
        } catch (Exception exception) {
            log.error("Kloudless get calendar event failed for id: " + eventId, exception);
        }
        return resource;
    }

    public ResourceList getKloudlessScheduleInstances(final UserKloudlessCalendar userKloudlessCalendar, final String eventId) {
        ResourceList resources = null;
        try {
            Account account = new Account(userKloudlessCalendar.getUserKloudlessAccount().getAccountId(), kloudlessProperties.getKloudlessAPIKey());
            resources = (ResourceList) account.get(API_ENDPOINT_CALENDARS + userKloudlessCalendar.getCalendarId() + "/events?instances=" + eventId);
        } catch (Exception exception) {
            log.error("Kloudless get calendar event instances failed for id: " + eventId, exception);
        }
        return resources;
    }

    public Resource createKloudlessSchedule(final UserKloudlessCalendar userKloudlessCalendar, Map<String,Object> content) {
        Resource resource = null;
        try {
            Account account = new Account(userKloudlessCalendar.getUserKloudlessAccount().getAccountId(), kloudlessProperties.getKloudlessAPIKey());
            resource = (Resource) account.post(API_ENDPOINT_CALENDARS + userKloudlessCalendar.getCalendarId() + "/events", content);
        } catch (Exception exception) {
            log.error("Kloudless create calendar event failed", exception);
        }
        return resource;
    }

    public ResponseRaw deleteKloudlessSchedule(final UserKloudlessCalendar userKloudlessCalendar, final String eventId) {
        ResponseRaw resource = null;
        try {
            Account account = new Account(userKloudlessCalendar.getUserKloudlessAccount().getAccountId(), kloudlessProperties.getKloudlessAPIKey());
            resource = (ResponseRaw) account.delete(API_ENDPOINT_CALENDARS + userKloudlessCalendar.getCalendarId() + API_ENDPOINT_EVENTS + eventId);
            log.info(new Gson().toJson(resource));
        } catch (Exception exception) {
            log.error("Kloudless delete calendar event failed for id: " + eventId, exception);
        }
        return resource;
    }

    /**
     * Returns the meeting window by meeting window id.
     *
     * @param token           access token
     * @param meetingWindowId window id
     * @return Scheduled meeting window
     */
    public MeetingWindow getMeetingWindow(String token, String meetingWindowId) {
        if (ValidationUtils.isEmptyString(token)) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_INVALID, null);
        }
        if (ValidationUtils.isEmptyString(meetingWindowId)) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_MEETING_INVALID, null);
        }

        APIService apiService = APIBuilder.builder(API_BASE_URL);
        MeetingWindow window;
        try {
            Response<MeetingWindow> response = apiService.getMeetingWindow("Bearer " + token, meetingWindowId)
                    .execute();
            if (!response.isSuccessful()) {
                Gson gson = new Gson();
                ErrorResponse errorResponse = gson.fromJson(response.errorBody().string(), ErrorResponse.class);
                log.error("kloudless get meeting window failed for id " + meetingWindowId + ", error code: " + errorResponse.getErrorCode());
                throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_MEETING_LINKING_FAILED, null);
            }
            window = response.body();
        } catch (IOException e) {
            log.error("kloudless get meeting window api failed for id " + meetingWindowId, e);
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_MEETING_LINKING_FAILED, null);
        }
        return window;
    }

    /**
     * Delete the meeting window by meeting window id.
     * @param token           access token
     * @param meetingWindowId window id
     */
    public void deleteMeetingWindow(String token, String meetingWindowId) {
        if (ValidationUtils.isEmptyString(token)) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_INVALID, null);
        }
        if (ValidationUtils.isEmptyString(meetingWindowId)) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_MEETING_INVALID, null);
        }

        // using RestTemplate because retrofit has trouble in handling 204 with non empty body
        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        String url = String.format(API_BASE_URL + "/v1/meetings/windows/%s/", meetingWindowId);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + token);
        HttpEntity<Object> requestEntity = new HttpEntity<>(null, httpHeaders);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
            if (!responseEntity.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
                Gson gson = new Gson();
                ErrorResponse errorResponse = gson.fromJson(responseEntity.getBody(), ErrorResponse.class);
                log.error("kloudless delete meeting window failed for id " + meetingWindowId + ", error code: " + errorResponse.getErrorCode());
                throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_MEETING_DELETION_FAILED, null);
            }
        } catch (RestClientException rce) {
            log.error("kloudless delete meeting window api failed for id " + meetingWindowId, rce);
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_MEETING_DELETION_FAILED, null);
        }
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        int timeout = 8000;
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory
                = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(timeout);
        return clientHttpRequestFactory;
    }

    public Resource updateScheduleProp(final Long userKloudlessScheduleId, final UserKloudlessCalendar userKloudlessCalendar, final String eventId) {
        Resource resource = null;
        try {
            Map<String, Object> properties = new HashMap<>();
            properties.put("key", CalendarConstants.CAL_PROP_FITWISE_SCHEDULE_ID);
            properties.put(StringConstants.JSON_PROPERTY_KEY_VALUE, String.valueOf(userKloudlessScheduleId));
            properties.put(StringConstants.JSON_PROPERTY_KEY_PRIVATE, true);
            List<Map<String, Object>> propList = new ArrayList<>();
            propList.add(properties);
            Map<String, Object> propMap = new HashMap<>();
            propMap.put("custom_properties", propList);
            Account account = new Account(userKloudlessCalendar.getUserKloudlessAccount().getAccountId(), kloudlessProperties.getKloudlessAPIKey());
            resource = (Resource) account.patch(API_ENDPOINT_CALENDARS + userKloudlessCalendar.getCalendarId() + API_ENDPOINT_EVENTS + eventId, propMap);
        } catch (Exception exception) {
            log.error("Kloudless delete calendar event failed for id: " + eventId, exception);
        }
        return resource;
    }

    public Resource updateKloudlessSchedule(final UserKloudlessCalendar userKloudlessCalendar, final String eventId, Map<String,Object> update) {
        Resource resource = null;
        try {
            Account account = new Account(userKloudlessCalendar.getUserKloudlessAccount().getAccountId(), kloudlessProperties.getKloudlessAPIKey());
            resource = (Resource) account.patch(API_ENDPOINT_CALENDARS + userKloudlessCalendar.getCalendarId() + API_ENDPOINT_EVENTS + eventId, update);
        } catch (Exception exception) {
            log.error("Kloudless update calendar event failed for id: " + eventId, exception);
        }
        return resource;
    }

    /**
     * Returns the available time window from the requested time window.
     * @param calendar     kloudless calendar
     * @param availability time windows
     * @return available time windows
     */
    public ResponseJson findAvailabilityInCalendar(UserKloudlessCalendar calendar, CalendarAvailabilityRequest availability) {
        if (calendar == null || ValidationUtils.isEmptyString(calendar.getCalendarId())) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_CALENDAR_ID_INVALID, null);
        }

        if (calendar.getUserKloudlessAccount() == null
                || ValidationUtils.isEmptyString(calendar.getUserKloudlessAccount().getAccountId())) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_NOT_FOUND, null);
        }

        ResponseJson response = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> findRequest = mapper.convertValue(availability, new TypeReference<Map<String, Object>>() {});
            Account account = new Account(calendar.getUserKloudlessAccount().getAccountId(),
                    kloudlessProperties.getKloudlessAPIKey());
            response = (ResponseJson) account.post("/cal/availability", findRequest);
        } catch (Exception e) {
            log.error("find availability failed for kloudless account id " + calendar.getUserKloudlessAccount().getAccountId(), e);
        }

        return response;
    }

    /**
     * Retrieves passed kloudless account subscriptions.
     * @param userKloudlessAccount kloudless account
     * @return kloudless subscriptions
     */
    public ResourceList getAccountSubscriptions(UserKloudlessAccount userKloudlessAccount) {
        long tempTime = new Date().getTime();
        if (userKloudlessAccount == null) {
            return null;
        }
        if (ValidationUtils.isEmptyString(userKloudlessAccount.getToken())) {
            log.info("get kloudless account subscriptions failed: account token is empty");
            return null;
        }
        log.info("Validate fields : " + (new Date().getTime() - tempTime));
        ResourceList subscriptionResourceList = null;
        try {
            tempTime = new Date().getTime();
            Account account = new Account(userKloudlessAccount.getToken());
            subscriptionResourceList = (ResourceList) account.get("/subscriptions");
            log.info("Get kloudless subscriptions from kloudless : " + (new Date().getTime() - tempTime));
        } catch (Exception exception) {
            log.error("Kloudless get subscriptions failed", exception);
        }
        return subscriptionResourceList;
    }

    /**
     * Creates kloudless subscription for passed account.
     * @param userKloudlessAccount kloudless account
     * @param content              subscription payload
     * @return created subscription
     */
    public Resource createSubscription(UserKloudlessAccount userKloudlessAccount, Map<String,Object> content) {
        if (userKloudlessAccount == null) {
            return null;
        }
        if (ValidationUtils.isEmptyString(userKloudlessAccount.getToken())) {
            log.info("create kloudless account subscription failed: account token is empty");
            return null;
        }
        if (content == null || content.isEmpty()) {
            log.info("create kloudless account subscription failed: content is empty");
            return null;
        }

        Resource subscriptionResource = null;
        try {
            long tempTime = new Date().getTime();
            Account account = new Account(userKloudlessAccount.getToken());
            subscriptionResource = (Resource) account.post("/subscriptions", content);
            log.info("Create kloudless sucription : " + (new Date().getTime() - tempTime));
        } catch (Exception exception) {
            log.error("Kloudless create subscription failed", exception);
        }
        return subscriptionResource;
    }

    /**
     * Delete kloudless subscription from passed account.
     * @param userKloudlessAccount kloudless account
     * @param subscriptionId       subscription identifier
     */
    public void deleteSubscription(UserKloudlessAccount userKloudlessAccount, String subscriptionId) {
        if (userKloudlessAccount == null) {
            log.info("delete kloudless account subscription failed: account is null");
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.MSG_ERR_SUBSCRIPTION_DELETE_FAILED, null);
        }
        if (ValidationUtils.isEmptyString(userKloudlessAccount.getToken())) {
            log.info("delete kloudless account subscription failed: account token is empty");
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.MSG_ERR_SUBSCRIPTION_DELETE_FAILED, null);
        }
        if (ValidationUtils.isEmptyString(subscriptionId)) {
            log.info("delete kloudless account subscription failed: subscription id is empty");
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.MSG_ERR_SUBSCRIPTION_DELETE_FAILED, null);
        }

        try {
            Account account = new Account(userKloudlessAccount.getToken());
            account.delete("/subscriptions/" + subscriptionId);
        } catch (Exception exception) {
            log.error("Kloudless delete subscription failed", exception);
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.MSG_ERR_SUBSCRIPTION_DELETE_FAILED, null);
        }
    }

    /**
     * Retrieves activites for the kloudless subscription.
     * @param userKloudlessAccount kloudless account
     * @param subscriptionId       subscription identifier
     * @param cursor               optional cursor to retrieve events from
     * @return activities
     */
    public ResponseJson getSubscriptionActivities(UserKloudlessAccount userKloudlessAccount, String subscriptionId, String cursor) {
        if (userKloudlessAccount == null) {
            log.info("get kloudless account subscription activities failed: account is null");
            return null;
        }
        if (ValidationUtils.isEmptyString(userKloudlessAccount.getToken())) {
            log.info("get kloudless account subscription activities failed: account token is empty");
            return null;
        }
        if (ValidationUtils.isEmptyString(subscriptionId)) {
            log.info("get kloudless account subscription activities failed: subscription id is empty");
            return null;
        }

        String path = String.format("v1/accounts/%s/subscriptions/%s/activity", userKloudlessAccount.getAccountId(), subscriptionId);
        if (!ValidationUtils.isEmptyString(cursor)) {
            path = path.concat("?cursor=" + cursor);
        }

        // using RestTemplate because kloudless sdk has parsing problem when id is null
        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        String url = API_BASE_URL + path;

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(userKloudlessAccount.getToken());
        HttpEntity<Object> requestEntity = new HttpEntity<>(null, httpHeaders);

        ResponseJson activityResourceList;
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
            Gson gson = new Gson();
            activityResourceList = new ResponseJson(gson.fromJson(responseEntity.getBody(), JsonObject.class), url, null);
        } catch (HttpStatusCodeException hce) {
            log.error("Kloudless get subscription activities failed: " + hce.getResponseBodyAsString());
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.MSG_ERR_GET_SUBSCRIPTION_ACTIVITY_FAILED, null);
        } catch (RestClientException rce) {
            log.error("Kloudless get subscription activities failed", rce);
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.MSG_ERR_GET_SUBSCRIPTION_ACTIVITY_FAILED, null);
        } catch (URISyntaxException use) {
            log.error("Kloudless get subscription activities failed: response json parsing failed", use);
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.MSG_ERR_GET_SUBSCRIPTION_ACTIVITY_FAILED, null);
        }

        return activityResourceList;
    }

    /**
     * Updates kloudless account's service info and returns.
     * @param userKloudlessAccount kloudess account
     * @return updated account with service info
     */
    public UserKloudlessAccount updateServiceInfo(UserKloudlessAccount userKloudlessAccount) {
        long startTime = new Date().getTime();
        log.info("Update service info");
        if (userKloudlessAccount == null
                || ValidationUtils.isEmptyString(userKloudlessAccount.getAccountId())
                || ValidationUtils.isEmptyString(userKloudlessAccount.getToken())) {
            return userKloudlessAccount;
        }

        UserKloudlessAccount existingAccount = userKloudlessTokenRepository
                .findByAccountId(userKloudlessAccount.getAccountId());
        if (existingAccount == null) {
            return userKloudlessAccount;
        }
        log.info("Check existing account : " + (new Date().getTime() - startTime));
        try {
            startTime = new Date().getTime();
            Account account = new Account(existingAccount.getToken());
            Resource resource = (Resource) account.get("");
            if (resource == null
                    || resource.getData() == null
                    || !resource.getData().has(StringConstants.JSON_PROPERTY_KEY_SERVICE)
                    || ValidationUtils.isEmptyString(resource.getData().get(StringConstants.JSON_PROPERTY_KEY_SERVICE).getAsString())) {
                log.error("update kloudless account service info failed: service info is empty or null");
                return userKloudlessAccount;
            }
            log.info("Get account from kloudless : " + (new Date().getTime() - startTime));
            startTime = new Date().getTime();
            existingAccount.setService(resource.getData().get(StringConstants.JSON_PROPERTY_KEY_SERVICE).getAsString());
            userKloudlessTokenRepository.save(existingAccount);
            log.info("Update service in db : " + (new Date().getTime() - startTime));
            log.info("updated kloudless account service info for account id " + existingAccount.getAccountId());
            return existingAccount;
        } catch (Exception e) {
            log.error("update kloudless account service info failed", e);
            return userKloudlessAccount;
        }
    }

}
