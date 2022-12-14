package com.fitwise.view.cronofy;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinkingProfile {
	
	@JsonProperty("provider_name")
    private String providerName;

    @JsonProperty("profile_id")
    private String profileId;
  
    @JsonProperty("profile_name")
    private String profileName;
    
    @JsonProperty("provider_service")
    private String providerService;

}
