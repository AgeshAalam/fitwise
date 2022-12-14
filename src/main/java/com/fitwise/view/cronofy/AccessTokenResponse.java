package com.fitwise.view.cronofy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessTokenResponse {
	
	@JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("access_token")
    private String accessToken;
  
    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("scope")
    private String scope;
    
    @JsonProperty("account_id")
    private String accountId;
    
    @JsonProperty("sub")
    private String sub;
   
    @JsonProperty("linking_profile")
    private LinkingProfile linkingProfile;

   
}
