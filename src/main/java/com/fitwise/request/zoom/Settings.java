package com.fitwise.request.zoom;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Settings {

    @JsonProperty("host_video")
    private Boolean hostVideo;

    @JsonProperty("participant_video")
    private Boolean participantVideo;

    @JsonProperty("cn_meeting")
    private Boolean cnMeeting;

    @JsonProperty("in_meeting")
    private Boolean inMeeting;

    @JsonProperty("join_before_host")
    private Boolean joinBeforeHost;

    @JsonProperty("jbh_time")
    private Integer jbhTime;

    @JsonProperty("mute_upon_entry")
    private Boolean muteUponEntry;

    @JsonProperty("watermark")
    private Boolean watermark;

    @JsonProperty("use_pmi")
    private Boolean usePmi;

    @JsonProperty("approval_type")
    private Integer approvalType;

    @JsonProperty("registration_type")
    private Integer registrationType;

    @JsonProperty("audio")
    private String audio;

    @JsonProperty("auto_recording")
    private String autoRecording;

    @JsonProperty("enforce_login")
    private Boolean enforceLogin;

    @JsonProperty("enforce_login_domains")
    private String enforceLoginDomains;

    @JsonProperty("alternative_hosts")
    private String alternativeHosts;

    @JsonProperty("close_registration")
    private Boolean closeRegistration;

    @JsonProperty("show_share_button")
    private Boolean showShareButton;

    @JsonProperty("allow_multiple_devices")
    private Boolean allowMultipleDevices;

    @JsonProperty("registrants_confirmation_email")
    private Boolean registrantsConfirmationEmail;

    @JsonProperty("waiting_room")
    private Boolean waitingRoom;

    @JsonProperty("contact_name")
    private String contactName;

    @JsonProperty("contact_email")
    private String contactEmail;

    @JsonProperty("request_permission_to_unmute_participants")
    private Boolean requestPermissionToUnmuteParticipants;

    @JsonProperty("registrants_email_notification")
    private Boolean registrantsEmailNotification;

    @JsonProperty("meeting_authentication")
    private Boolean meetingAuthentication;

    @JsonProperty("authentication_option")
    private String authenticationOption;

    @JsonProperty("authentication_domains")
    private String authenticationDomains;

    @JsonProperty("encryption_type")
    private String encryptionType;

}
