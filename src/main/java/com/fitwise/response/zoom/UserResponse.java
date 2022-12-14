package com.fitwise.response.zoom;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserResponse {

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("custom_attributes")
    private List<CustomAttribute> customAttributes = null;

    @JsonProperty("id")
    private String id;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("type")
    private Integer type;

    @JsonProperty("role_name")
    private String roleName;

    @JsonProperty("pmi")
    private Long pmi;

    @JsonProperty("use_pmi")
    private Boolean usePmi;

    @JsonProperty("personal_meeting_url")
    private String personalMeetingUrl;

    @JsonProperty("timezone")
    private String timezone;

    @JsonProperty("verified")
    private Integer verified;

    @JsonProperty("dept")
    private String dept;

    @JsonProperty("last_login_time")
    private String lastLoginTime;

    @JsonProperty("last_client_version")
    private String lastClientVersion;

    @JsonProperty("pic_url")
    private String picUrl;

    @JsonProperty("host_key")
    private String hostKey;

    @JsonProperty("jid")
    private String jid;

    @JsonProperty("group_ids")
    private List<Object> groupIds = null;

    @JsonProperty("im_group_ids")
    private List<String> imGroupIds = null;

    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("language")
    private String language;

    @JsonProperty("phone_country")
    private String phoneCountry;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("status")
    private String status;

    @JsonProperty("role_id")
    private String roleId;

}
