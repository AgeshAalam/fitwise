package com.fitwise.service.calendar;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.CalendarConstants;
import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.constants.ZoomConstants;
import com.fitwise.entity.User;
import com.fitwise.entity.calendar.ZoomAccount;
import com.fitwise.entity.calendar.ZoomMeeting;
import com.fitwise.exception.ApplicationException;
import com.fitwise.properties.ZoomProperties;
import com.fitwise.repository.calendar.ZoomAccountRepository;
import com.fitwise.repository.calendar.ZoomMeetingRepository;
import com.fitwise.request.zoom.DataCompliance;
import com.fitwise.request.zoom.Meeting;
import com.fitwise.request.zoom.Occurrence;
import com.fitwise.request.zoom.Recurrence;
import com.fitwise.response.zoom.Deauthorization;
import com.fitwise.response.zoom.DeauthorizationEventNotification;
import com.fitwise.response.zoom.InvitationResponse;
import com.fitwise.response.zoom.TokenResponse;
import com.fitwise.response.zoom.UserResponse;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.calendar.ZoomAccountsResponseView;
import com.fitwise.view.calendar.ZoomAuthorization;
import com.fitwise.view.calendar.ZoomCredentials;
import com.fitwise.view.calendar.ZoomMeetingRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class ZoomService {

    public static final String BASE_AUTH_URL = "https://zoom.us/oauth/authorize";
    public static final String BASE_AUTH_TOKEN_URL = "https://zoom.us/oauth/token";
    public static final String BASE_URL = "https://api.zoom.us";
    public static final String DATA_COMPLIANCE_URL = "https://api.zoom.us/oauth/data/compliance";

    private static final Integer TOKEN_EXPIRATION_OFFSET = 10;

    @Autowired
    private ZoomProperties zoomProperties;

    @Autowired
    private ZoomAccountRepository zoomAccountRepository;

    @Autowired
    private ZoomMeetingRepository zoomMeetingRepository;

    @Autowired
    private UserComponents userComponents;

    @Autowired
    private FitwiseUtils fitwiseUtils;

    /**
     * Returns authenticated Zoom account else returns Zoom authorization url.
     *
     * @return zoom account or authorization url
     */
    public ZoomAuthorization getAccount() {
        User user = userComponents.getUser();
        ZoomAccount authorizedAccount = getZoomAccount(user);
        if (authorizedAccount != null) {
            ZoomAuthorization zoomAuthorization = new ZoomAuthorization();
            zoomAuthorization.setAccountEmail(authorizedAccount.getAccountEmail());
            zoomAuthorization.setAuthenticated(true);
            zoomAuthorization.setAccountId(authorizedAccount.getAccountId());
            zoomAuthorization.setActive(authorizedAccount.isActive());
            return zoomAuthorization;
        }

        if (ValidationUtils.isEmptyString(zoomProperties.getClientId())
                || ValidationUtils.isEmptyString(zoomProperties.getClientSecret())) {
            log.error("zoom application credentials is invalid");
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_AUTH_FAILED, null);
        }
        if (ValidationUtils.isEmptyString(zoomProperties.getRedirectUrl())) {
            log.error("zoom application redirection url is invalid");
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_AUTH_FAILED, null);
        }

        ZoomAccount zoomAccount = null;
        List<ZoomAccount> zoomAccountList = zoomAccountRepository.findByUser(user);
        if(zoomAccountList.isEmpty()){
            zoomAccount = new ZoomAccount();
            zoomAccount.setUser(user);
            zoomAccount.setState(UUID.randomUUID().toString());
            zoomAccountRepository.save(zoomAccount);
        } else {
            for (ZoomAccount zoomAccount1 : zoomAccountList){
                if(zoomAccount1.getAccountId() == null){
                    zoomAccount = zoomAccount1;
                }
            }
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(BASE_AUTH_URL);
        uriBuilder.queryParam("response_type", "code");
        uriBuilder.queryParam("client_id", zoomProperties.getClientId());
        uriBuilder.queryParam("redirect_uri", zoomProperties.getRedirectUrl());
        uriBuilder.queryParam("state", zoomAccount.getState());

        ZoomAuthorization zoomAuthorization = new ZoomAuthorization();
        zoomAuthorization.setAuthorizationUrl(uriBuilder.toUriString());
        zoomAuthorization.setAuthenticated(false);
        return zoomAuthorization;
    }

    /**
     * Exchanges authorization code for access token and saves zoom user details.
     *
     * @param zoomCredentials Zoom authorized code.
     */
    public ZoomAuthorization saveAccount(ZoomCredentials zoomCredentials) {
        if (ValidationUtils.isEmptyString(zoomProperties.getClientId())
                || ValidationUtils.isEmptyString(zoomProperties.getClientSecret())) {
            log.error("zoom application credentials is invalid");
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_AUTH_FAILED, null);
        }
        if (ValidationUtils.isEmptyString(zoomProperties.getRedirectUrl())) {
            log.error("zoom application redirection url is invalid");
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_AUTH_FAILED, null);
        }
        if (ValidationUtils.isEmptyString(zoomCredentials.getCode())) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_AUTH_CODE_INVALID, null);
        }
        if (ValidationUtils.isEmptyString(zoomCredentials.getState())) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_STATE_INVALID, null);
        }
        User user = userComponents.getUser();
        List<ZoomAccount> accounts = zoomAccountRepository.findByUser(user);
        if (accounts.isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_ACCOUNT_NOT_FOUND, null);
        }
        ZoomAccount zoomAccount = null;
        for (ZoomAccount account : accounts) {
            if (!account.isActive() && account.getState().equals(zoomCredentials.getState())) {
                zoomAccount = account;
            }
        }
        if (zoomAccount == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_STATE_NOT_FOUND, null);
        }
        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(BASE_AUTH_TOKEN_URL);
        uriBuilder.queryParam("grant_type", "authorization_code");
        uriBuilder.queryParam("code", zoomCredentials.getCode());
        uriBuilder.queryParam("redirect_uri", zoomProperties.getRedirectUrl());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(zoomProperties.getClientId(), zoomProperties.getClientSecret());
        HttpEntity<Object> requestEntity = new HttpEntity<>(null, httpHeaders);
        TokenResponse tokenResponse;
        try {
            ResponseEntity<TokenResponse> responseEntity = restTemplate
                    .exchange(uriBuilder.toUriString(), HttpMethod.POST, requestEntity, TokenResponse.class);
            tokenResponse = responseEntity.getBody();
        } catch (HttpStatusCodeException hse) {
            log.error("zoom get access token failed: " + hse.getResponseBodyAsString());
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_AUTH_FAILED, null);
        } catch (RestClientException rce) {
            log.error("zoom get access token api failed", rce);
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_AUTH_FAILED, null);
        }
        if (tokenResponse == null) {
            log.error("zoom get access token failed, api response is empty");
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_AUTH_FAILED, null);
        }
        zoomAccount.setAccessToken(tokenResponse.getAccessToken());
        zoomAccount.setRefreshToken(tokenResponse.getRefreshToken());
        // access token expires in 1 hour, but decreasing expiration time to refresh token proactively
        LocalDateTime expirationTime = LocalDateTime.now()
                .plusSeconds(tokenResponse.getExpiresIn() - TOKEN_EXPIRATION_OFFSET);
        zoomAccount.setTokenExpirationTime(Date.from(expirationTime.atZone(ZoneId.systemDefault()).toInstant()));

        UserResponse zoomUser = getUser(tokenResponse.getAccessToken());
        if (zoomUser == null) {
            log.error("zoom get user returned null for state: " + zoomCredentials.getState());
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_GET_USER_FAILED, null);
        }
        List<ZoomAccount> existingAccounts = zoomAccountRepository.findByUser(zoomAccount.getUser());
        for (ZoomAccount account : existingAccounts) {
            if (!ValidationUtils.isEmptyString(account.getAccountId())
                    && account.getAccountId().equals(zoomUser.getAccountId())) {
                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_ACCOUNT_DUPLICATE, null);
            }
        }
        zoomAccount.setUserId(zoomUser.getId());
        zoomAccount.setAccountId(zoomUser.getAccountId());
        zoomAccount.setAccountEmail(zoomUser.getEmail());
        zoomAccount.setActive(existingAccounts.size() <= 1);
        //Set other zoom accounts inactive
        if(zoomAccount.isActive()){
            for (ZoomAccount existingAccount : existingAccounts) {
                if (existingAccount.getZoomAccountId().equals(zoomAccount.getZoomAccountId())) {
                    continue;
                }
                existingAccount.setActive(false);
                zoomAccountRepository.save(existingAccount);
            }
        }
        zoomAccountRepository.save(zoomAccount);
        ZoomAuthorization zoomAuthorization = new ZoomAuthorization();
        zoomAuthorization.setAccountEmail(zoomAccount.getAccountEmail());
        zoomAuthorization.setAuthenticated(true);
        zoomAuthorization.setAccountId(zoomAccount.getAccountId());
        zoomAuthorization.setActive(zoomAccount.isActive());
        return zoomAuthorization;
    }

    /**
     * Fetches zoom user profile.
     *
     * @param accessToken zoom user access token
     * @return user profile
     */
    public UserResponse getUser(String accessToken) {
        if (ValidationUtils.isEmptyString(accessToken)) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_ACCESS_TOKEN_INVALID, null);
        }
        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        String url = String.format("%s/v2/users/me", BASE_URL);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(accessToken);
        HttpEntity<Object> requestEntity = new HttpEntity<>(null, httpHeaders);
        try {
            ResponseEntity<UserResponse> responseEntity = restTemplate
                    .exchange(url, HttpMethod.GET, requestEntity, UserResponse.class);
            return responseEntity.getBody();
        } catch (HttpStatusCodeException hse) {
            log.error("zoom get user failed: " + hse.getResponseBodyAsString());
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_GET_USER_FAILED, null);
        } catch (RestClientException rce) {
            log.error("zoom get user api error", rce);
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_GET_USER_FAILED, null);
        }
    }

    /**
     * Returns account token if it isn't expired else refreshes token and returns it.
     *
     * @param zoomAccount zoom account
     * @return access token
     */
    public String getToken(ZoomAccount zoomAccount) {
        if (ValidationUtils.isEmptyString(zoomProperties.getClientId())
                || ValidationUtils.isEmptyString(zoomProperties.getClientSecret())) {
            log.error("zoom application credentials is invalid");
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_AUTH_FAILED, null);
        }
        LocalDateTime currentTime = LocalDateTime.now();
        Date now = Date.from(currentTime.atZone(ZoneId.systemDefault()).toInstant());
        if (zoomAccount.getTokenExpirationTime().after(now)) {
            return zoomAccount.getAccessToken();
        }
        log.info("zoom access token expired for user: " + zoomAccount.getUser().getUserId());
        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(BASE_AUTH_TOKEN_URL);
        urlBuilder.queryParam("grant_type", "refresh_token");
        urlBuilder.queryParam("refresh_token", zoomAccount.getRefreshToken());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(zoomProperties.getClientId(), zoomProperties.getClientSecret());
        HttpEntity<Object> requestEntity = new HttpEntity<>(null, httpHeaders);
        TokenResponse tokenResponse;
        try {
            ResponseEntity<TokenResponse> responseEntity = restTemplate
                    .exchange(urlBuilder.toUriString(), HttpMethod.POST, requestEntity, TokenResponse.class);
            tokenResponse = responseEntity.getBody();
        } catch (HttpStatusCodeException hse) {
            log.error("zoom refreshing access token failed: " + hse.getResponseBodyAsString());
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_REFRESH_TOKEN_FAILED, null);
        } catch (RestClientException rce) {
            log.error("zoom refreshing access token api error", rce);
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_REFRESH_TOKEN_FAILED, null);
        }

        if (tokenResponse == null) {
            log.error("zoom refreshing token failed, api response is empty");
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_REFRESH_TOKEN_FAILED, null);
        }

        log.info("zoom access token refreshed for user: " + zoomAccount.getUser().getUserId());

        zoomAccount.setAccessToken(tokenResponse.getAccessToken());
        zoomAccount.setRefreshToken(tokenResponse.getRefreshToken());

        // access token expires in 1 hour, but decreasing expiration time to refresh token proactively
        LocalDateTime expirationTime = LocalDateTime.now()
                .plusSeconds(tokenResponse.getExpiresIn() - TOKEN_EXPIRATION_OFFSET);
        zoomAccount.setTokenExpirationTime(Date.from(expirationTime.atZone(ZoneId.systemDefault()).toInstant()));

        zoomAccountRepository.save(zoomAccount);
        return zoomAccount.getAccessToken();
    }

    /**
     * Creates zoom meeting for user based on passed meeting.
     *
     * @param user           fitwise user(instructor)
     * @param meetingRequest meeting information
     * @return
     */
    public List<ZoomMeeting> createMeetingForUser(User user, ZoomMeetingRequest meetingRequest) {
        if (user == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_USER_NOT_FOUND, null);
        }
        if (meetingRequest == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_INVALID, null);
        }
        if (ValidationUtils.isEmptyString(meetingRequest.getTitle())) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_TITLE_INVALID, null);
        }
        if (ValidationUtils.isEmptyString(meetingRequest.getStartTime())) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_START_INVALID, null);
        }
        if (meetingRequest.getDuration() <= 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_DURATION_INVALID, null);
        }
        if (ValidationUtils.isEmptyString(meetingRequest.getTimeZone()) || !fitwiseUtils.isValidTimeZone(meetingRequest.getTimeZone())) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_TIMEZONE_INVALID, null);
        }

        ZoomAccount zoomAccount = getZoomAccount(user);
        if (zoomAccount == null) {
            throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_ZOOM_ACCOUNT_NOT_FOUND, null);
        }

        String accessToken = getToken(zoomAccount);
        if (ValidationUtils.isEmptyString(accessToken)) {
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_ACCESS_TOKEN_INVALID, null);
        }

        Meeting meeting = new Meeting();
        meeting.setType(ZoomConstants.MEETING_SCHEDULED);
        meeting.setTopic(meetingRequest.getTitle());

        // expected date time format is 2011-12-03T10:15:30
        LocalDateTime start;
        try {
            start = LocalDateTime.parse(meetingRequest.getStartTime());
        } catch (DateTimeParseException dte) {
            log.error("invalid meeting start time", dte);
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_START_INVALID, null);
        }
        meeting.setStartTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(start));
        meeting.setTimezone(meetingRequest.getTimeZone());
        meeting.setDuration(meetingRequest.getDuration());
        Recurrence recurrence = null;
        List<ZonedDateTime> exceptionDateTimes = null;
        if (meetingRequest.getRecurrenceRules() != null && !meetingRequest.getRecurrenceRules().isEmpty()) {
            for (String rule : meetingRequest.getRecurrenceRules()) {
                if (rule.contains("RRULE")) {
                    try {
                        recurrence = buildRecurrence(rule);
                    } catch (ApplicationException ae) {
                        log.error("zoom meeting creation: building recurrence failed", ae);
                        throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_RECURRENCE_INVALID, null);
                    }
                }

                if (rule.contains(StringConstants.RECURRENCE_EXDATE)) {
                    exceptionDateTimes = getExceptionDateTimes(rule);
                }
            }
        }

        if (recurrence != null) {
            meeting.setType(ZoomConstants.MEETING_RECURRING_FIXED);
            meeting.setRecurrence(recurrence);
        }

        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        String url = String.format("%s/v2/users/me/meetings", BASE_URL);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(accessToken);
        HttpEntity<Meeting> requestEntity = new HttpEntity<>(meeting, httpHeaders);
        Meeting createdMeeting;
        try {
            ResponseEntity<Meeting> responseEntity = restTemplate
                    .exchange(url, HttpMethod.POST, requestEntity, Meeting.class);
            log.info("zoom meeting created for user: " + user.getUserId());
            createdMeeting = responseEntity.getBody();
        } catch (HttpStatusCodeException hse) {
            log.error("zoom create meeting failed: " + hse.getResponseBodyAsString());
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_MEETING_CREATE_FAILED, null);
        } catch (RestClientException rce) {
            log.error("zoom create meeting error", rce);
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_MEETING_CREATE_FAILED, null);
        }
        List<ZoomMeeting> meetings = new ArrayList<>();
        if (createdMeeting.getOccurrences() != null && !createdMeeting.getOccurrences().isEmpty()) {
            List<String> deletedOccurrences = new ArrayList<>();
            if (exceptionDateTimes != null && !exceptionDateTimes.isEmpty()) {
                ZoneId timeZone = ZoneId.of(meeting.getTimezone());
                for (Occurrence occurrence : createdMeeting.getOccurrences()) {
                    Instant instant = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(occurrence.getStartTime()));
                    ZonedDateTime instanceAtTimezone = instant.atZone(timeZone);
                    if (exceptionDateTimes.contains(instanceAtTimezone)) {
                        deleteMeeting(zoomAccount, createdMeeting.getId(), occurrence.getOccurrenceId(), false);
                        deletedOccurrences.add(occurrence.getOccurrenceId());
                    }
                }
            }
            for (Occurrence occurrence : createdMeeting.getOccurrences()) {
                if (!deletedOccurrences.isEmpty() && deletedOccurrences.contains(occurrence.getOccurrenceId())) {
                    continue;
                }

                ZoomMeeting zoomMeeting = new ZoomMeeting();
                zoomMeeting.setMeetingId(Long.toString(createdMeeting.getId()));
                zoomMeeting.setMeetingType(ZoomConstants.MEETING_RECURRING);
                zoomMeeting.setOccurrenceId(occurrence.getOccurrenceId());
                zoomMeeting.setTopic(createdMeeting.getTopic());
                zoomMeeting.setStartTime(Date.from(Instant.from(DateTimeFormatter.ISO_INSTANT.parse(occurrence.getStartTime()))));
                zoomMeeting.setTimezone(createdMeeting.getTimezone());
                zoomMeeting.setDurationInMinutes(occurrence.getDuration().longValue());
                zoomMeeting.setJoinUrl(createdMeeting.getJoinUrl());
                zoomMeeting.setZoomAccount(zoomAccount);
                meetings.add(zoomMeeting);
            }
            zoomMeetingRepository.saveAll(meetings);
            return meetings;
        }

        ZoomMeeting zoomMeeting = new ZoomMeeting();
        zoomMeeting.setMeetingId(Long.toString(createdMeeting.getId()));
        zoomMeeting.setMeetingType(ZoomConstants.MEETING_SOLO);
        zoomMeeting.setTopic(createdMeeting.getTopic());
        zoomMeeting.setStartTime(Date.from(Instant.from(DateTimeFormatter.ISO_INSTANT.parse(createdMeeting.getStartTime()))));
        zoomMeeting.setTimezone(createdMeeting.getTimezone());
        zoomMeeting.setDurationInMinutes(createdMeeting.getDuration().longValue());
        zoomMeeting.setJoinUrl(createdMeeting.getJoinUrl());
        zoomMeeting.setZoomAccount(zoomAccount);
        meetings.add(zoomMeeting);

        zoomMeetingRepository.save(zoomMeeting);
        return meetings;
    }

    public ZoomMeeting getMeetingForUser(User user, String meetingId, String occurrenceId) {
        if (user == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_USER_NOT_FOUND, null);
        }
        if (ValidationUtils.isEmptyString(meetingId)) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_INVALID, null);
        }
        ZoomMeeting zoomMeeting;
        if (!ValidationUtils.isEmptyString(occurrenceId)) {
            zoomMeeting = getZoomMeeting(meetingId, occurrenceId);
        } else {
            zoomMeeting = getZoomMeeting(meetingId);
        }
        if (zoomMeeting == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_NOT_FOUND, null);
        }
        if (zoomMeeting.getZoomAccount() == null) {
            log.info("get zoom meeting failed: associated zoom account info is null");
            return null;
        }
        if (!user.getUserId().equals(zoomMeeting.getZoomAccount().getUser().getUserId())) {
            throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_ZOOM_ACCOUNT_NOT_FOUND, null);
        }
        ZoomAccount zoomAccount = zoomMeeting.getZoomAccount();
        String accessToken = getToken(zoomAccount);
        if (ValidationUtils.isEmptyString(accessToken)) {
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_ACCESS_TOKEN_INVALID, null);
        }
        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        String url;
        if (!ValidationUtils.isEmptyString(zoomMeeting.getOccurrenceId())) {
            url = String.format("%s/v2/meetings/%s?occurrence_id=%s", BASE_URL, meetingId, occurrenceId);
        } else {
            url = String.format("%s/v2/meetings/%s", BASE_URL, meetingId);
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(accessToken);
        HttpEntity<Object> requestEntity = new HttpEntity<>(null, httpHeaders);
        Meeting meeting;
        try {
            ResponseEntity<Meeting> responseEntity = restTemplate
                    .exchange(url, HttpMethod.GET, requestEntity, Meeting.class);
            meeting = responseEntity.getBody();
        } catch (HttpStatusCodeException hse) {
            log.error("zoom get meeting failed: " + hse.getResponseBodyAsString());
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_MEETING_GET_FAILED, null);
        } catch (RestClientException rce) {
            log.error("zoom get meeting error", rce);
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_MEETING_GET_FAILED, null);
        }
        zoomMeeting.setTopic(meeting.getTopic());
        zoomMeeting.setStartTime(Date.from(Instant.from(DateTimeFormatter.ISO_INSTANT.parse(meeting.getStartTime()))));
        zoomMeeting.setTimezone(meeting.getTimezone());
        zoomMeeting.setDurationInMinutes(meeting.getDuration().longValue());
        zoomMeeting.setJoinUrl(meeting.getJoinUrl());
        return zoomMeeting;
    }

    public ZoomMeeting updateMeetingForUser(User user, String meetingId, String occurrenceId, ZoomMeetingRequest meetingRequest) {
        if (user == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_USER_NOT_FOUND, null);
        }
        if (ValidationUtils.isEmptyString(meetingId)) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_INVALID, null);
        }
        if (meetingRequest == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_INVALID, null);
        }
        ZoomMeeting zoomMeeting;
        if (!ValidationUtils.isEmptyString(occurrenceId)) {
            zoomMeeting = getZoomMeeting(meetingId, occurrenceId);
        } else {
            zoomMeeting = getZoomMeeting(meetingId);
        }
        if (zoomMeeting == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_NOT_FOUND, null);
        }
        if (zoomMeeting.getZoomAccount() == null) {
            log.info("update zoom meeting failed: associated zoom account info is null");
            return null;
        }
        if (!user.getUserId().equals(zoomMeeting.getZoomAccount().getUser().getUserId())) {
            throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_ZOOM_ACCOUNT_NOT_FOUND, null);
        }
        ZoomAccount zoomAccount = zoomMeeting.getZoomAccount();
        String accessToken = getToken(zoomAccount);
        if (ValidationUtils.isEmptyString(accessToken)) {
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_ACCESS_TOKEN_INVALID, null);
        }
        Meeting meeting = new Meeting();
        boolean isOccurrence = !ValidationUtils.isEmptyString(zoomMeeting.getOccurrenceId());
        if (!ValidationUtils.isEmptyString(meetingRequest.getTitle()) && !isOccurrence) {
            meeting.setTopic(meetingRequest.getTitle());
        }
        if (!ValidationUtils.isEmptyString(meetingRequest.getStartTime())) {
            // expected date time format is 2011-12-03T10:15:30
            LocalDateTime start;
            try {
                start = LocalDateTime.parse(meetingRequest.getStartTime());
            } catch (DateTimeParseException dte) {
                log.error("invalid meeting start time", dte);
                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_START_INVALID, null);
            }
            meeting.setStartTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(start));
        }
        if (!ValidationUtils.isEmptyString(meetingRequest.getTimeZone()) && !isOccurrence) {
            if (!fitwiseUtils.isValidTimeZone(meetingRequest.getTimeZone())) {
                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_TIMEZONE_INVALID, null);
            }
            meeting.setTimezone(meetingRequest.getTimeZone());
        }
        if (meetingRequest.getDuration() != null && meetingRequest.getDuration() > 0) {
            meeting.setDuration(meetingRequest.getDuration());
        }
        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        String url;
        if (!ValidationUtils.isEmptyString(occurrenceId)) {
            url = String.format("%s/v2/meetings/%s?occurrence_id=%s", BASE_URL, meetingId, occurrenceId);
        } else {
            url = String.format("%s/v2/meetings/%s", BASE_URL, meetingId);
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(accessToken);
        HttpEntity<Meeting> requestEntity = new HttpEntity<>(meeting, httpHeaders);
        try {
            restTemplate
                    .exchange(url, HttpMethod.PATCH, requestEntity, String.class);
            log.info("zoom meeting updated for user: " + user.getUserId());
        } catch (HttpStatusCodeException hse) {
            log.error("zoom update meeting failed: " + hse.getResponseBodyAsString());
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_MEETING_UPDATE_FAILED, null);
        } catch (RestClientException rce) {
            log.error("zoom update meeting error", rce);
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_MEETING_UPDATE_FAILED, null);
        }
        ZoomMeeting updatedMeeting = getMeetingForUser(user, zoomMeeting.getMeetingId(), zoomMeeting.getOccurrenceId());
        zoomMeeting.setTopic(updatedMeeting.getTopic());
        zoomMeeting.setStartTime(updatedMeeting.getStartTime());
        zoomMeeting.setTimezone(updatedMeeting.getTimezone());
        zoomMeeting.setDurationInMinutes(updatedMeeting.getDurationInMinutes());
        zoomMeeting.setJoinUrl(updatedMeeting.getJoinUrl());
        zoomMeetingRepository.save(zoomMeeting);
        return zoomMeeting;
    }

    public void deleteMeetingForUser(User user, String meetingId, String occurrenceId) {
        if (user == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_USER_NOT_FOUND, null);
        }
        if (ValidationUtils.isEmptyString(meetingId)) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_INVALID, null);
        }
        ZoomMeeting zoomMeeting;
        if (!ValidationUtils.isEmptyString(occurrenceId)) {
            zoomMeeting = getZoomMeeting(meetingId, occurrenceId);
        } else {
            zoomMeeting = getZoomMeeting(meetingId);
        }
        if (zoomMeeting == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_NOT_FOUND, null);
        }
        if (zoomMeeting.getZoomAccount() == null) {
            log.info("delete zoom meeting failed: associated zoom account is null");
            return;
        }
        if (!user.getUserId().equals(zoomMeeting.getZoomAccount().getUser().getUserId())) {
            throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_ZOOM_ACCOUNT_NOT_FOUND, null);
        }
        deleteMeeting(zoomMeeting.getZoomAccount(), Long.parseLong(zoomMeeting.getMeetingId()), occurrenceId, true);
        zoomMeetingRepository.delete(zoomMeeting);
    }

    public void deleteMeeting(ZoomAccount zoomAccount, Long meetingId, String occurrenceId, boolean sendReminder) {
        if (zoomAccount == null) {
            throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_ZOOM_ACCOUNT_NOT_FOUND, null);
        }

        if (meetingId <= 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_INVALID, null);
        }

        String accessToken = getToken(zoomAccount);
        if (ValidationUtils.isEmptyString(accessToken)) {
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_ACCESS_TOKEN_INVALID, null);
        }
        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(String.format("%s/v2/meetings/%s", BASE_URL, meetingId));

        if (!ValidationUtils.isEmptyString(occurrenceId)) {
            uriBuilder.queryParam("occurrence_id", occurrenceId);
        }
        uriBuilder.queryParam("schedule_for_reminder", sendReminder);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(accessToken);
        HttpEntity<Meeting> requestEntity = new HttpEntity<>(null, httpHeaders);
        try {
            restTemplate
                    .exchange(uriBuilder.toUriString(), HttpMethod.DELETE, requestEntity, String.class);
            log.info("zoom meeting deleted for user: " + zoomAccount.getUser().getUserId());
        } catch (HttpStatusCodeException hse) {
            log.error("zoom delete meeting failed: " + hse.getResponseBodyAsString());
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_MEETING_DELETE_FAILED, null);
        } catch (RestClientException rce) {
            log.error("zoom delete meeting error", rce);
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_MEETING_DELETE_FAILED, null);
        }
    }

    public String getMeetingInvitation(ZoomAccount zoomAccount, Long meetingId) {
        if (zoomAccount == null) {
            log.info("zoom meeting get invitation failed: zoom account is null");
            return null;
        }
        String accessToken = getToken(zoomAccount);
        if (ValidationUtils.isEmptyString(accessToken)) {
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_ACCESS_TOKEN_INVALID, null);
        }
        return getMeetingInvitation(accessToken, meetingId);
    }

    public String getMeetingInvitation(String accessToken, Long meetingId) {
        if (ValidationUtils.isEmptyString(accessToken)) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_ACCESS_TOKEN_INVALID, null);
        }
        if (meetingId <= 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ZOOM_MEETING_INVALID, null);
        }

        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        String url = String.format("%s/v2/meetings/%s/invitation", BASE_URL, meetingId);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(accessToken);
        HttpEntity<Object> requestEntity = new HttpEntity<>(null, httpHeaders);

        try {
            ResponseEntity<InvitationResponse> responseEntity = restTemplate
                    .exchange(url, HttpMethod.GET, requestEntity, InvitationResponse.class);
            return responseEntity.getBody().getInvitation();
        } catch (HttpStatusCodeException hse) {
            log.error("zoom get meeting invitation failed: " + hse.getResponseBodyAsString());
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_GET_MEETING_INVITATION_FAILED, null);
        } catch (RestClientException rce) {
            log.error("zoom get meeting invitation api error", rce);
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_GET_MEETING_INVITATION_FAILED, null);
        }
    }

    private Recurrence buildRecurrence(String rrule) {
        if (ValidationUtils.isEmptyString(rrule)) {
            return null;
        }

        if (!rrule.contains("RRULE")) {
            return null;
        }

        if (!rrule.contains(";")) {
            return null;
        }

        Recurrence recurrence = new Recurrence();
        String[] ruleParts = rrule.split(";");

        for (String rulePart : ruleParts) {
            if (rulePart.contains("FREQ")) {
                if (recurrence.getType() != null && recurrence.getType() > 0) {
                    throw new ApplicationException(Constants.BAD_REQUEST,
                            "Zoom meeting recurrence frequency must not occur more than once", null);
                }

                if (rulePart.split("=").length < 2) {
                    throw new ApplicationException(Constants.BAD_REQUEST,
                            "Zoom meeting recurrence frequency is invalid", null);
                }

                String frequency = rulePart.split("=")[1];
                switch (frequency) {
                    case "DAILY":
                        recurrence.setType(1);
                        break;
                    case "WEEKLY":
                        recurrence.setType(2);
                        break;
                    default:
                        throw new ApplicationException(Constants.BAD_REQUEST,
                                "Zoom meeting recurrence, unsupported frequency " + frequency, null);
                }
            }
        }

        if (recurrence.getType() == null || recurrence.getType() == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST,
                    "Zoom meeting recurrence frequency is required", null);
        }

        for (String rulePart : ruleParts) {
            if (rulePart.contains("COUNT")) {
                if (recurrence.getEndTimes() != null && recurrence.getEndTimes() > 0) {
                    throw new ApplicationException(Constants.BAD_REQUEST,
                            "Zoom meeting recurrence count must not occur more than once", null);
                }

                if (rulePart.split("=").length < 2) {
                    throw new ApplicationException(Constants.BAD_REQUEST,
                            "Zoom meeting recurrence count is invalid", null);
                }

                String count = rulePart.split("=")[1];
                try {
                    recurrence.setEndTimes(Integer.parseInt(count));
                } catch (NumberFormatException nfe) {
                    throw new ApplicationException(Constants.BAD_REQUEST,
                            "Zoom meeting recurrence count is invalid", null);
                }
            }
        }

        for (String rulePart : ruleParts) {
            if (rulePart.contains("INTERVAL")) {
                if (recurrence.getRepeatInterval() != null && recurrence.getRepeatInterval() > 0) {
                    throw new ApplicationException(Constants.BAD_REQUEST,
                            "Zoom meeting recurrence interval must not occur more than once", null);
                }

                if (rulePart.split("=").length < 2) {
                    throw new ApplicationException(Constants.BAD_REQUEST,
                            "Zoom meeting recurrence interval is invalid", null);
                }

                String interval = rulePart.split("=")[1];
                Integer repeatInterval = null;
                try {
                    repeatInterval = Integer.parseInt(interval);
                } catch (NumberFormatException nfe) {
                    if (rulePart.split("=").length < 2) {
                        throw new ApplicationException(Constants.BAD_REQUEST,
                                "Zoom meeting recurrence interval is invalid", null);
                    }
                }

                switch (recurrence.getType()) {
                    case 1:
                        if (repeatInterval > 90) {
                            throw new ApplicationException(Constants.BAD_REQUEST,
                                    "zoom daily meeting interval must not be greater than is 90", null);
                        }
                        break;
                    case 2:
                        if (repeatInterval > 12) {
                            throw new ApplicationException(Constants.BAD_REQUEST,
                                    "zoom weekly meeting interval must not be greater than is 12", null);
                        }
                        break;
                }
                recurrence.setRepeatInterval(repeatInterval);
            }
        }

        if (recurrence.getRepeatInterval() == null || recurrence.getRepeatInterval() == 0) {
            recurrence.setRepeatInterval(1);
        }

        for (String rulePart : ruleParts) {
            if (rulePart.contains("BYDAY")) {
                if (!ValidationUtils.isEmptyString(recurrence.getWeeklyDays())) {
                    throw new ApplicationException(Constants.BAD_REQUEST,
                            "Zoom meeting recurrence weekly day list must not occur more than once", null);
                }

                if (rulePart.split("=").length < 2) {
                    throw new ApplicationException(Constants.BAD_REQUEST,
                            "Zoom meeting recurrence weekly day list is invalid", null);
                }

                String dayList = rulePart.split("=")[1];
                String[] days = dayList.split(",");

                Set<String> weeklyDays = new HashSet<>();
                for (String day : days) {
                    if (day.contains("SU")) {
                        weeklyDays.add("1");
                        continue;
                    }
                    if (day.contains("MO")) {
                        weeklyDays.add("2");
                        continue;
                    }
                    if (day.contains("TU")) {
                        weeklyDays.add("3");
                        continue;
                    }
                    if (day.contains("WE")) {
                        weeklyDays.add("4");
                        continue;
                    }
                    if (day.contains("TH")) {
                        weeklyDays.add("5");
                        continue;
                    }
                    if (day.contains("FR")) {
                        weeklyDays.add("6");
                        continue;
                    }
                    if (day.contains("SA")) {
                        weeklyDays.add("7");
                    }
                }

                recurrence.setWeeklyDays(String.join(",", weeklyDays));
            }
        }

        return recurrence;
    }

    public List<ZonedDateTime> getExceptionDateTimes(String exDateProperty) {
        List<ZonedDateTime> exceptionDateTimes = new ArrayList<>();
        if (ValidationUtils.isEmptyString(exDateProperty)) {
            return exceptionDateTimes;
        }
        if (!exDateProperty.contains(StringConstants.RECURRENCE_EXDATE)) {
            return exceptionDateTimes;
        }
        if (!exDateProperty.contains(":")) {
            return exceptionDateTimes;
        }
        if (exDateProperty.split(":").length != 2) {
            throw new ApplicationException(Constants.BAD_REQUEST, "zoom meeting exception date rule is invalid", null);
        }
        String[] property = exDateProperty.split(":");
        String propertyParam = property[0];
        String propertyValue = property[1];
        DateTimeFormatter dateTimeFormatter = null;
        ZoneId timeZone = null;
        String[] params = propertyParam.split(";");
        if (params.length > 1) {
            for (String param : params) {
                if (param.contains("VALUE")) {
                    if (param.split("=").length != 2) {
                        throw new ApplicationException(Constants.BAD_REQUEST, "Zoom meeting exdate value param is invalid", null);
                    }
                    String value = param.split("=")[1];
                    if (value.equals("DATE-TIME")) {
                        dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
                    }
                    if (value.equals("DATE")) {
                        dateTimeFormatter = DateTimeFormatter.BASIC_ISO_DATE;
                    }
                }
                if (param.contains("TZID")) {
                    if (param.split("=").length != 2) {
                        throw new ApplicationException(Constants.BAD_REQUEST, "Zoom meeting exdate tzid param is invalid", null);
                    }
                    String value = param.split("=")[1];
                    try {
                        timeZone = ZoneId.of(value);
                    } catch (DateTimeException dte) {
                        log.error("zoom meeting exdate tzid is invalid", dte);
                        throw new ApplicationException(Constants.BAD_REQUEST, "Zoom meeting exdate tzid param is invalid", null);
                    }
                }
            }
        }
        if (timeZone == null) {
            timeZone = ZoneOffset.UTC;
        }
        String[] values = propertyValue.split(",");
        for (String value : values) {
            if (dateTimeFormatter != null) {
                try {
                    LocalDateTime dateTime = LocalDateTime.from(dateTimeFormatter.parse(value));
                    exceptionDateTimes.add(ZonedDateTime.of(dateTime, timeZone));
                } catch (DateTimeException dte) {
                    log.error("zoom meeting: parsing exception date failed", dte);
                    throw new ApplicationException(Constants.BAD_REQUEST,
                            "exception date time format invalid", null);
                }
            }
        }
        return exceptionDateTimes;
    }

    /**
     * Removes the Zoom account from the fitwise user and notifies zoom for data compliance if desired.
     *
     * @param verificationToken                verification token sent with event
     * @param deauthorizationEventNotification event information
     */
    public void removeAccount(String verificationToken, DeauthorizationEventNotification deauthorizationEventNotification) {
        if (ValidationUtils.isEmptyString(verificationToken)
                || !verificationToken.equals(zoomProperties.getVerificationToken())) {
            log.error("zoom remove account failed: invalid verification token");
            return;
        }

        if (!"app_deauthorized".equals(deauthorizationEventNotification.getEvent())) {
            log.error("zoom remove account failed: unknown event " + deauthorizationEventNotification.getEvent());
            return;
        }

        if (ValidationUtils.isEmptyString(zoomProperties.getClientId())) {
            log.error("zoom remove account failed: application credentials is invalid");
            return;
        }

        if (!zoomProperties.getClientId().equals(deauthorizationEventNotification.getPayload().getClientId())) {
            log.error("zoom remove account failed: unknown client id");
            return;
        }

        List<ZoomAccount> accounts = zoomAccountRepository
                .findByAccountId(deauthorizationEventNotification.getPayload().getAccountId());
        if (accounts == null || accounts.isEmpty()) {
            log.error("zoom remove account failed: account not found");
            return;
        }

        for (ZoomAccount account : accounts) {
            anonymizeAccount(account);
        }

        if ("false".equalsIgnoreCase(deauthorizationEventNotification.getPayload().getUserDataRetention())) {
            try {
                notifyDataCompliance(deauthorizationEventNotification.getPayload());
            } catch (ApplicationException ae) {
                log.error("zoom notify data compliance failed");
            }
            if (deauthorizationEventNotification.getPayload() != null
                    && deauthorizationEventNotification.getPayload().getAccountId() != null) {
                log.info("zoom data compliance notified for account " + deauthorizationEventNotification.getPayload().getAccountId());
            }
        }
    }

    /**
     * Anonymize the passed Zoom account and if it has any scheduled meetings unlinks it.
     * @param zoomAccount
     */
    private void anonymizeAccount(ZoomAccount zoomAccount) {
        if (zoomAccount == null || zoomAccount.getUser() == null) {
            log.info("zoom account anonymize failed: invalid account");
            return;
        }
        List<ZoomMeeting> zoomMeetings = zoomMeetingRepository.findByZoomAccount(zoomAccount);
        if (zoomMeetings != null && zoomMeetings.isEmpty()) {
            log.info("zoom account anonymize: account's scheduled meetings is empty");
        }
        if (zoomMeetings != null && !zoomMeetings.isEmpty()) {
            for (ZoomMeeting meeting : zoomMeetings) {
                meeting.setZoomAccount(null);
            }
            zoomMeetingRepository.saveAll(zoomMeetings);
            log.info("zoom account anonymize: unlinked " + zoomMeetings.size() + " scheduled meetings");
        }
        Long userId = zoomAccount.getUser().getUserId();
        zoomAccount.setUserId(KeyConstants.KEY_ANONYMOUS);
        zoomAccount.setAccountId(KeyConstants.KEY_ANONYMOUS);
        zoomAccount.setAccountEmail(KeyConstants.KEY_ANONYMOUS);
        zoomAccount.setAccessToken(KeyConstants.KEY_ANONYMOUS);
        zoomAccount.setRefreshToken(KeyConstants.KEY_ANONYMOUS);
        zoomAccount.setTokenExpirationTime(null);
        zoomAccount.setState(null);
        zoomAccount.setActive(false);
        zoomAccount.setUser(null);
        zoomAccountRepository.save(zoomAccount);
        log.info("zoom account anonymize: anonymize account for user id " + userId);
    }

    /**
     * Notifies zoom about the user data removal from fitwise servers.
     *
     * @param deauthorization deauthorization event payload
     */
    private void notifyDataCompliance(Deauthorization deauthorization) {
        if (ValidationUtils.isEmptyString(zoomProperties.getClientId())
                || ValidationUtils.isEmptyString(zoomProperties.getClientSecret())) {
            log.error("zoom application credentials is invalid");
            throw new ApplicationException(Constants.ERROR_STATUS, "zoom application credential invalid", null);
        }
        if (deauthorization == null) {
            throw new ApplicationException(Constants.ERROR_STATUS, "deauthorization event is null", null);
        }
        if (ValidationUtils.isEmptyString(deauthorization.getAccountId())
                || ValidationUtils.isEmptyString(deauthorization.getUserId())) {
            throw new ApplicationException(Constants.ERROR_STATUS, "deauthorization event is invalid", null);
        }
        DataCompliance dataCompliance = new DataCompliance();
        dataCompliance.setClientId(zoomProperties.getClientId());
        dataCompliance.setUserId(deauthorization.getUserId());
        dataCompliance.setAccountId(deauthorization.getAccountId());
        dataCompliance.setDeauthorizationEventReceived(deauthorization);
        dataCompliance.setComplianceCompleted(true);
        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(zoomProperties.getClientId(), zoomProperties.getClientSecret());
        HttpEntity<DataCompliance> requestEntity = new HttpEntity<>(dataCompliance, httpHeaders);
        try {
            restTemplate.exchange(DATA_COMPLIANCE_URL, HttpMethod.POST, requestEntity, String.class);
        } catch (HttpStatusCodeException hse) {
            log.error("zoom notify data compliance failed: " + hse.getResponseBodyAsString());
            throw new ApplicationException(Constants.ERROR_STATUS, "Zoom data compliance notify failed", null);
        } catch (RestClientException rce) {
            log.error("zoom notify data compliance api error", rce);
            throw new ApplicationException(Constants.ERROR_STATUS, "Zoom data compliance notify failed", null);
        }
    }

    /**
     * Returns active Zoom account associated with user else null;
     *
     * @param user fitwise user
     * @return linked zoom account
     */
    public ZoomAccount getZoomAccount(User user) {
        if (user == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_USER_NOT_FOUND, null);
        }

        List<ZoomAccount> zoomUsers = zoomAccountRepository.findByUser(user);
        if (zoomUsers.isEmpty()) {
            return null;
        }

        ZoomAccount zoomAccountRes = null;
        for (ZoomAccount zoomAccount : zoomUsers){
            if (zoomAccount.isActive() && zoomAccount.getAccountId() != null){
                zoomAccountRes = zoomAccount;
                break;
            }
        }
        if (zoomAccountRes == null && !zoomUsers.isEmpty() && zoomUsers.get(0).getAccountId() != null){
            zoomAccountRes = zoomUsers.get(0);
            zoomAccountRes.setActive(true);
            zoomAccountRepository.save(zoomAccountRes);
        }
        return zoomAccountRes;
    }

    public ZoomMeeting getZoomMeeting(String meetingId) {
        if (ValidationUtils.isEmptyString(meetingId)) {
            return null;
        }

        Optional<ZoomMeeting> optionalMeeting = zoomMeetingRepository.findByMeetingId(meetingId);
        return optionalMeeting.orElse(null);
    }

    public ZoomMeeting getZoomMeeting(String meetingId, String occurrenceId) {
        if (ValidationUtils.isEmptyString(meetingId)) {
            return null;
        }

        if (ValidationUtils.isEmptyString(occurrenceId)) {
            return null;
        }

        Optional<ZoomMeeting> optionalMeeting = zoomMeetingRepository.findByMeetingIdAndOccurrenceId(meetingId, occurrenceId);
        return optionalMeeting.orElse(null);
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        int timeout = 8000;
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory
                = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(timeout);
        return clientHttpRequestFactory;
    }


    /**
     * Returns Zoom authorization url.
     *
     * @return authorization url
     */
    public ZoomAuthorization authorizeZoom(){
        User user = userComponents.getUser();

        if (ValidationUtils.isEmptyString(zoomProperties.getClientId())
                || ValidationUtils.isEmptyString(zoomProperties.getClientSecret())) {
            log.error("zoom application credentials is invalid");
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_AUTH_FAILED, null);
        }
        if (ValidationUtils.isEmptyString(zoomProperties.getRedirectUrl())) {
            log.error("zoom application redirection url is invalid");
            throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ZOOM_AUTH_FAILED, null);
        }

        ZoomAccount zoomAccount = null;
        List<ZoomAccount> zoomAccountList = zoomAccountRepository.findByUser(user);
        int noOfAuthenticatedAccounts = 0;
        if(zoomAccountList.isEmpty()){
            zoomAccount = new ZoomAccount();
            zoomAccount.setUser(user);
            zoomAccount.setState(UUID.randomUUID().toString());
            zoomAccountRepository.save(zoomAccount);
        } else {
            for (ZoomAccount zoomAccount1 : zoomAccountList){
                if(zoomAccount1.getAccountId() == null){
                    zoomAccount = zoomAccount1;
                } else {
                    noOfAuthenticatedAccounts++;
                }
            }
        }
        if (noOfAuthenticatedAccounts == zoomAccountList.size() && !zoomAccountList.isEmpty()){
            zoomAccount = new ZoomAccount();
            zoomAccount.setUser(user);
            zoomAccount.setState(UUID.randomUUID().toString());
            zoomAccountRepository.save(zoomAccount);
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(BASE_AUTH_URL);
        uriBuilder.queryParam("response_type", "code");
        uriBuilder.queryParam("client_id", zoomProperties.getClientId());
        uriBuilder.queryParam("redirect_uri", zoomProperties.getRedirectUrl());
        uriBuilder.queryParam("state", zoomAccount.getState());

        ZoomAuthorization zoomAuthorization = new ZoomAuthorization();
        zoomAuthorization.setAuthorizationUrl(uriBuilder.toUriString());
        zoomAuthorization.setAuthenticated(false);
        return zoomAuthorization;
    }

    /**
     * Returns list of zoom accounts
     *
     * @return zoom accounts list
     * @Param
     */
    public List<ZoomAccountsResponseView> getMyAllZoomAccounts(){
        User user = userComponents.getUser();
        List<ZoomAccount> zoomAccountList = zoomAccountRepository.findByUser(user);
        if (zoomAccountList.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, CalendarConstants.CAL_SCS_ZOOM_ACCOUNTS_EMPTY, null);
        }
        List<ZoomAccountsResponseView> zoomAccountsResponseViews  = new ArrayList<>();
        for (ZoomAccount zoomAccount : zoomAccountList){
            if(zoomAccount.getAccountId() != null){
                ZoomAccountsResponseView zoomAccountsResponseView = new ZoomAccountsResponseView();
                zoomAccountsResponseView.setAccountEmail(zoomAccount.getAccountEmail());
                zoomAccountsResponseView.setActive(zoomAccount.isActive());
                zoomAccountsResponseView.setAccountId(zoomAccount.getAccountId());
                zoomAccountsResponseViews.add(zoomAccountsResponseView);
            }
        }
        return zoomAccountsResponseViews;
    }

    /**
     * API to change the zoom account status
     *
     * @return userZoomAccountId, setActive
     */
    public void changeZoomAccountStatus(String userZoomAccountId, boolean setActive){
        User user = userComponents.getUser();
        //Validating the userZoomAccountId
        if (userZoomAccountId == null || userZoomAccountId.isEmpty()){
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_ID_NULL, MessageConstants.ERROR);
        }
        List<ZoomAccount> zoomAccountList = zoomAccountRepository.findByUserAndAccountId(user, userZoomAccountId);
        if (zoomAccountList.isEmpty()){
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_ID_INVALID, MessageConstants.ERROR);
        }
        ZoomAccount zoomAccount = zoomAccountList.get(0);
        List<ZoomAccount> existingAccounts = zoomAccountRepository.findByUser(user);
        //Set inactive for all other zoom accounts
        if (setActive){
            zoomAccount.setActive(true);
            for (ZoomAccount existingAccount : existingAccounts) {
                if (existingAccount.getZoomAccountId().equals(zoomAccount.getZoomAccountId())) {
                    continue;
                }
                existingAccount.setActive(false);
                zoomAccountRepository.save(existingAccount);
            }
        } else {
            zoomAccount.setActive(false);
        }
        zoomAccountRepository.save(zoomAccount);
    }

    @Transactional
    public void deleteZoomAccount(String accountId){
        User user = userComponents.getUser();

        if (!fitwiseUtils.isInstructor(user)){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_INSTRUCTOR, MessageConstants.ERROR);
        }
        //Validating the user input
        if (accountId == null || ValidationUtils.isEmptyString(accountId)){
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_ID_NULL, MessageConstants.ERROR);
        }
        List<ZoomAccount> zoomAccountList = zoomAccountRepository.findByUserAndAccountId(user, accountId);

        if (zoomAccountList.isEmpty()){
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_ID_INVALID, MessageConstants.ERROR);
        }
        ZoomAccount zoomAccount = zoomAccountList.get(0);

        if (zoomAccount.isActive()){
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.MSG_ERR_DELETE_ACTIVE_ZOOM_ACCOUNT, null);
        }

        List<ZoomMeeting> zoomMeetingList = zoomMeetingRepository.findByZoomAccount(zoomAccount);
        //Delete the zoom account
        if (zoomMeetingList.isEmpty()){

            zoomAccountRepository.deleteById(zoomAccount.getZoomAccountId());

        } else {
            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.MSG_ERR_ZOOM_DELETE_PACKAGE_USER_ACCOUNT, MessageConstants.ERROR);
        }
    }
}
