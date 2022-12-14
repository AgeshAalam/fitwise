package com.fitwise.view.cronofy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalendarModelResponse {
	   
	   
	    @JsonProperty("provider_name")
	    private String providerName;

	    @JsonProperty("profile_id")
	    private String profileId;

	    @JsonProperty("profile_name")
	    private String profileName;

	    @JsonProperty("calendar_id")
	    private String calendarId;

	    @JsonProperty("calendar_name")
	    private String calendarName;

	    @JsonProperty("calendar_readonly")
	    private boolean calendarReadOnly;

	    @JsonProperty("calendar_deleted")
	    private boolean calendarDeleted;
	    
	    @JsonProperty("calendar_integrated_conferencing_available")
	    private boolean calendarIntegratedConferencingAvailable;

	    @JsonIgnoreProperties(ignoreUnknown = true)
	    @JsonProperty("calendar_primary")
	    private boolean calendarPrimary;

	    @JsonIgnoreProperties(ignoreUnknown = true)
	    @JsonProperty("permission_level")
	    private String permissionLevel;
}
