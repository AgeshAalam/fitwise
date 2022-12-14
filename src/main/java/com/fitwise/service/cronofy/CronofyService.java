package com.fitwise.service.cronofy;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.client.ClientBuilder;

import org.biacode.jcronofy.api.client.CronofyClient;
import org.biacode.jcronofy.api.client.impl.CronofyClientImpl;
import org.biacode.jcronofy.api.model.GrantTypeModel;
import org.biacode.jcronofy.api.model.common.CronofyResponse;
import org.biacode.jcronofy.api.model.request.RevokeAccessTokenRequest;
import org.biacode.jcronofy.api.model.request.UpdateAccessTokenRequest;
import org.biacode.jcronofy.api.model.response.RevokeAccessTokenResponse;
import org.biacode.jcronofy.api.model.response.UpdateAccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;



import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fitwise.constants.CalendarConstants;
import com.fitwise.constants.Constants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.properties.CronofyProperties;
import com.fitwise.view.cronofy.AccessTokenResponse;
import com.fitwise.view.cronofy.CreateCalendarResponse;
import com.fitwise.view.cronofy.CreateRealtimeScheduleResponse;
import com.fitwise.view.cronofy.CreateavailabilityrulesResponse;
import com.fitwise.view.cronofy.GetScheduleStatusResponse;
import com.fitwise.view.cronofy.RealtimeScheduleResponse;
import com.fitwise.view.cronofy.RefreshAccessTokenResponse;
import com.fitwise.view.cronofy.RequestavailabilityrulesResponse;
import com.fitwise.view.cronofy.FreeBusyResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CronofyService {

	public static final String API_BASE_URL = "https://api.cronofy.com";
	public static final String API_OAUTH_URL = "https://api.cronofy.com/oauth/token";
	public static final String API_CREATE_CALENDER_URL = "https://api.cronofy.com/v1/calendars";
	
    public static final String API_CREATE_CRONOFY_AVAILABILITY_RULES = "https://api.cronofy.com/v1/availability_rules";
	
	public static final String API_DELETE_CRONOFY_AVAILABILITY_RULES = "https://api.cronofy.com/v1/availability_rules";
	
	public static final String API_REAL_TIME_SCHEDULING = "https://api.cronofy.com/v1/real_time_scheduling";
	
	public static final String CLIENT_ID = "FX2KDYdJEDTyZ9G8XDdm2DmhNaKjcP3k";
	public static final String CLIENT_SECRET = "CRN_qeFhOgyol3g1vrLB5zr4bnPrppTNSvdkmMsFLG";
	
	 @Autowired
	 private CronofyProperties cronofyProperties;
	 
   
	  
	   
	    
	 
	  public AccessTokenResponse getaccesstoken(final String code,final String redirectUri) {
			
			RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
	        
			 UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(API_OAUTH_URL);
			  Map<String, String> body = new LinkedHashMap<>(); 
                body.put("client_id",  CLIENT_ID);
                body.put("client_secret", CLIENT_SECRET);
                body.put("grant_type", "authorization_code");
                body.put("code", code);
                body.put("redirect_uri", redirectUri);


	          HttpHeaders httpHeaders = new HttpHeaders();
	          httpHeaders.add("Content-Type", "application/json; charset=utf-8");
	          HttpEntity<Object> requestEntity = new HttpEntity<Object>(body, httpHeaders);

	         AccessTokenResponse accessTokenResponse = null;
	         
	         try {
	            ResponseEntity<AccessTokenResponse> responseEntity = restTemplate
	                    .exchange(uriBuilder.toUriString(), HttpMethod.POST, requestEntity, AccessTokenResponse.class);
	            accessTokenResponse = responseEntity.getBody();
	       
	            } catch (HttpStatusCodeException hse) {
	            log.error("Cronofy get access token failed: " + hse.getResponseBodyAsString());
	               throw new ApplicationException(Constants.ERROR_STATUS, uriBuilder.toUriString(), null);
	         } catch (RestClientException rce) {
	            log.error("Cronofy get access token api failed: ", rce);
	               throw new ApplicationException(Constants.ERROR_STATUS, uriBuilder.toUriString(), null);
	         }
			  
	         return accessTokenResponse;
		  }
	
	   public CronofyResponse<UpdateAccessTokenResponse> updateAccessToken(final String token) {
		
		
		 final CronofyClient cronofyClient = new CronofyClientImpl(ClientBuilder.newBuilder().register(JacksonJsonProvider.class).build());

		
		 CronofyResponse<UpdateAccessTokenResponse> updateAccessTokenResponse = null;
		   
		 try {
				UpdateAccessTokenRequest updateAccessTokenRequest = new UpdateAccessTokenRequest();
				updateAccessTokenRequest.setClientId(cronofyProperties.getClientId());
				updateAccessTokenRequest.setClientSecret(cronofyProperties.getClientSecret());
				updateAccessTokenRequest.setGrantTypeModel(GrantTypeModel.REFRESH_TOKEN);
				updateAccessTokenRequest.setRefreshToken(token);
				
				updateAccessTokenResponse = cronofyClient.updateAccessToken(updateAccessTokenRequest);	
				
		  } catch (Exception exception) {
				log.error("cronofy update access token failed: ", exception);
				throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED, null);
		  }
		 
		  return updateAccessTokenResponse;
	   }
	 
	 
	 
	   public CronofyResponse<RevokeAccessTokenResponse> revokeAccessToken(final String token) {
		 
		  
			final CronofyClient cronofyClient = new CronofyClientImpl(ClientBuilder.newBuilder().register(JacksonJsonProvider.class).build());

		    CronofyResponse<RevokeAccessTokenResponse> revokeAccessTokenResponse = null;
		 
			try {
			
				  RevokeAccessTokenRequest revokeAccessTokenRequest = new RevokeAccessTokenRequest();
				  revokeAccessTokenRequest.setClientId(cronofyProperties.getClientId());
				  revokeAccessTokenRequest.setClientSecret(cronofyProperties.getClientSecret());
				  revokeAccessTokenRequest.setToken(token);
				
				  revokeAccessTokenResponse = cronofyClient.revokeAccessToken(revokeAccessTokenRequest);
				
			} catch (Exception exception) {
				log.error("cronofy revoke access token failed: ", exception);
				throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED, null);
		   }
		  
		    return revokeAccessTokenResponse;
		 
	    }
	  
	   public RefreshAccessTokenResponse refreshAccessToken(final String refreshToken) {
	    	 
   	    RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        
		    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(API_OAUTH_URL);
		    
		          Map<String, String> body = new LinkedHashMap<>(); 
		            body.put("client_id",  CLIENT_ID);
	                body.put("client_secret", CLIENT_SECRET);
	                body.put("grant_type", "refresh_token");
	                body.put("refresh_token", refreshToken);
              

                HttpHeaders httpHeaders = new HttpHeaders();
                HttpEntity<Object> requestEntity = new HttpEntity<Object>(body, httpHeaders);
           
                RefreshAccessTokenResponse refreshAccessTokenResponse = null;
               
          
          try {
              
       	        ResponseEntity<RefreshAccessTokenResponse> responseEntity = restTemplate
                          .exchange(uriBuilder.toUriString(), HttpMethod.POST, requestEntity, RefreshAccessTokenResponse.class);
       	         refreshAccessTokenResponse = responseEntity.getBody();
             
             } catch (HttpStatusCodeException hse) {
                     log.error("Cronofy get refresh token  failed: " + hse.getResponseBodyAsString());
                     throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED, null);
             } catch (RestClientException rce) {
                     log.error("Cronofy get refresh token  failed: ", rce);
                     throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED, null);
             }
   	 
          return refreshAccessTokenResponse;
	 
     }
	   
	   public Boolean deleteprofile(final String accessToken,final String profile_id) {
    	   boolean isDeleted = false;
    	   RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
    	 
    	   String url = String.format(API_BASE_URL + "/v1/profiles/%s/revoke", profile_id);
 		
    	   UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);
           
 		        HttpHeaders httpHeaders = new HttpHeaders();
                
 		        httpHeaders.setBearerAuth(accessToken);
               
                HttpEntity<Object> requestEntity = new HttpEntity<Object>(null, httpHeaders);
            
              try {
               
        	   ResponseEntity<String> responseEntity = restTemplate
                           .exchange(uriBuilder.toUriString(), HttpMethod.POST, requestEntity, String.class);
        	  
        	   if (!responseEntity.getStatusCode().equals(HttpStatus.ACCEPTED)) {
                   throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ACCOUNT_DELETION_FAILED, null);
               }
        	   else {
        		   isDeleted = true;
                }
              
        	   
             } catch (HttpStatusCodeException hse) {
                   log.error("Cronofy account delete failed: " + profile_id, hse);
                      throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ACCOUNT_DELETION_FAILED, null);
             } catch (RestClientException rce) {
                   log.error("Cronofy account delete failed: ", profile_id, rce);
                      throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_ACCOUNT_DELETION_FAILED, null);
             }
              
              return isDeleted;
     }
	   
	   public Boolean deleteevent(final String accessToken,final String calendar_id,final String event_id) {
	    	  
	    	 boolean isDeleted = false;
	    	
	    	 RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
	    	 
	    	   String url = String.format(API_BASE_URL + "/v1/calendars/%s/events", calendar_id);
	 		
	    	   UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);
	    	   
	    	        Map<String, String> body = new LinkedHashMap<>(); 
	                
	    	         body.put("event_id",  event_id);
           
	 		         HttpHeaders httpHeaders = new HttpHeaders();
	                
	 		         httpHeaders.setBearerAuth(accessToken);
	               
	                 HttpEntity<Object> requestEntity = new HttpEntity<Object>(body, httpHeaders);
	            
	              try {
	               
	        	   ResponseEntity<String> responseEntity = restTemplate
	                           .exchange(uriBuilder.toUriString(), HttpMethod.DELETE, requestEntity, String.class);
	        	  
	        	   if (!responseEntity.getStatusCode().equals(HttpStatus.ACCEPTED)) {
	                   throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_SCHEDULE_DELETION_FAILED, null);
	               }
	        	   else {
	        		   isDeleted = true;
	                }
	              
	        	   
	             } catch (HttpStatusCodeException hse) {
	                   log.error("Cronofy delete Schedule failed: " + event_id, hse);
	                      throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_SCHEDULE_DELETION_FAILED, null);
	             } catch (RestClientException rce) {
	                   log.error("Cronofy delete schedule failed: ", event_id, rce);
	                      throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_SCHEDULE_DELETION_FAILED, null);
	             }
	              
	              return isDeleted;
	     }
	   
	   public Boolean createUnavailableEvent(final String accessToken,final String calendar_id,final String event_id,final String event_start,final String event_end) {
	    	  
	    	 boolean isCreated = false;
	    	
	    	 RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
	    	 
	    	   String url = String.format(API_BASE_URL + "/v1/calendars/%s/events", calendar_id);
	 		
	    	   UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);
	    	   
	    	        Map<String, String> body = new LinkedHashMap<>(); 
	                
	    	         body.put("event_id",  "Unavailable_"+event_id);
	    	         
	    	         body.put("summary",  "UNAVAILABLE");
	    	         
	    	         body.put("description",  "Unavailable on Trainnr App.");
	    	         
	    	         body.put("start",  event_start);
	    	         
	    	         body.put("end",  event_end);
         
	 		         HttpHeaders httpHeaders = new HttpHeaders();
	                
	 		         httpHeaders.setBearerAuth(accessToken);
	               
	                 HttpEntity<Object> requestEntity = new HttpEntity<Object>(body, httpHeaders);
	            
	              try {
	               
	        	   ResponseEntity<String> responseEntity = restTemplate
	                           .exchange(uriBuilder.toUriString(), HttpMethod.POST, requestEntity, String.class);
	        	  
	        	   if (!responseEntity.getStatusCode().equals(HttpStatus.ACCEPTED)) {
	                   throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_UNAVAILABLE_EVENT_FAILED, null);
	               }
	        	   else {
	        		   isCreated = true;
	                }
	              
	        	   
	             } catch (HttpStatusCodeException hse) {
	                   log.error("Cronofy create unavailable event failed: " + event_id, hse);
	                      throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_UNAVAILABLE_EVENT_FAILED, null);
	             } catch (RestClientException rce) {
	                   log.error("Cronofy create unavailable event failed: ", event_id, rce);
	                      throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_UNAVAILABLE_EVENT_FAILED, null);
	             }
	              
	              return isCreated;
	     }
	   
	   public CreateCalendarResponse createCalendar(final String profileId, final String profilename,final String accessToken) {
    	 
    	    RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
    	    
    	    String calendar_Name = profilename+"-"+UUID.randomUUID().toString();
         
 		    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(API_CREATE_CALENDER_URL);
 		    
 		          Map<String, String> body = new LinkedHashMap<>(); 
                    body.put("profile_id",  profileId);
                    body.put("name", calendar_Name);
               

            HttpHeaders httpHeaders = new HttpHeaders();
                 httpHeaders.setBearerAuth(accessToken);
                 HttpEntity<Object> requestEntity = new HttpEntity<Object>(body, httpHeaders);
            
            CreateCalendarResponse createCalendarResponse = null;
                
           
           try {
               
        	     ResponseEntity<CreateCalendarResponse> responseEntity = restTemplate
                           .exchange(uriBuilder.toUriString(), HttpMethod.POST, requestEntity, CreateCalendarResponse.class);
                 createCalendarResponse = responseEntity.getBody();
              
              } catch (HttpStatusCodeException hse) {
                      log.error("Cronofy calender create failed: " + hse.getResponseBodyAsString());
                      throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_CALENDER_CREATE_FAILED, null);
              } catch (RestClientException rce) {
                      log.error("Cronofy calender create failed: ", rce);
                      throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_CALENDER_CREATE_FAILED, null);
              }
    	 
           return createCalendarResponse;
	 
      }
	   
	   public CreateavailabilityrulesResponse createavailabilityrules(RequestavailabilityrulesResponse requestavailabilityrulesResponse,final String accessToken) {
	    	 
	    	 RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());

                UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(API_CREATE_CRONOFY_AVAILABILITY_RULES);
	 	                	 Map<String,Object> body = new LinkedHashMap<>(); 
	                         body.put("availability_rule_id",  requestavailabilityrulesResponse.getAvailabilityRuleId());
	                         body.put("tzid", requestavailabilityrulesResponse.getTzid());
	                         body.put("calendar_ids", requestavailabilityrulesResponse.getCalendarIds());
	                         body.put("weekly_periods", requestavailabilityrulesResponse.getWeeklyPeriods());
	                         HttpHeaders httpHeaders = new HttpHeaders();
	                        
	                        httpHeaders.setBearerAuth(accessToken);
	                        
	                        HttpEntity<Object> requestEntity = new HttpEntity<Object>(body, httpHeaders);
	            
	           CreateavailabilityrulesResponse createavailabilityrulesResponse = null;
	                
	             try {
	               
	        	      ResponseEntity<CreateavailabilityrulesResponse> responseEntity = restTemplate
	                           .exchange(uriBuilder.toUriString(), HttpMethod.POST, requestEntity, CreateavailabilityrulesResponse.class);
	        	       createavailabilityrulesResponse = responseEntity.getBody();
	              
	              } catch (HttpStatusCodeException hse) {
	                   log.error("Cronofy avaibility Rules create failed: " + hse.getResponseBodyAsString());
	                   throw new ApplicationException(Constants.ERROR_STATUS,CalendarConstants.CAL_ERR_MEETING_LINKING_FAILED, null);
	              } catch (RestClientException rce) {
	                   log.error("Cronofy avaibility Rule create failed: ", rce);
	                   throw new ApplicationException(Constants.ERROR_STATUS,CalendarConstants.CAL_ERR_MEETING_LINKING_FAILED, null);
	             }
	    	 
	          return createavailabilityrulesResponse;
		  }
       
       
       public void deleteavailabilityrules(final String accessToken,final String availabilityRuleId) {
	    	 
	    	   RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
	    	 
	    	   String url = String.format(API_BASE_URL + "/v1/availability_rules/%s", availabilityRuleId);
	 		
	    	   UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);
               
	 		        HttpHeaders httpHeaders = new HttpHeaders();
	                
	 		        httpHeaders.setBearerAuth(accessToken);
	               
	                HttpEntity<Object> requestEntity = new HttpEntity<Object>(null, httpHeaders);
	            
	              try {
	               
	        	   ResponseEntity<String> responseEntity = restTemplate
	                           .exchange(uriBuilder.toUriString(), HttpMethod.DELETE, requestEntity, String.class);
	        	   if (!responseEntity.getStatusCode().equals(HttpStatus.ACCEPTED)) {
	                   throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_MEETING_DELETION_FAILED, null);
	               }
	              
	              
	             } catch (HttpStatusCodeException hse) {
	                   log.error("Cronofy meeting deletion failed: " + availabilityRuleId, hse);
	                      throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_MEETING_DELETION_FAILED, null);
	             } catch (RestClientException rce) {
	                   log.error("Cronofy meeting deletion failed: ", availabilityRuleId, rce);
	                      throw new ApplicationException(Constants.ERROR_STATUS, CalendarConstants.CAL_ERR_MEETING_DELETION_FAILED, null);
	             }
	     }
       
       public RealtimeScheduleResponse createscheduleInstance(CreateRealtimeScheduleResponse createRealtimeScheduleResponse) {
	    	 
	    	 RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
	 		   
	    	 UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(API_REAL_TIME_SCHEDULING);
	 	             
	    	       Map<String,Object> body = new LinkedHashMap<>(); 
	                     
	 	               body.put("oauth",  createRealtimeScheduleResponse.getRealtimeScheduleoauth());
                    
	 	               body.put("event", createRealtimeScheduleResponse.getRealtimeScheduleevent());
                     
                       body.put("availability", createRealtimeScheduleResponse.getRealtimeScheduleavailability());
                    
                       body.put("event_creation", createRealtimeScheduleResponse.getEventcreation());
                    
                       body.put("target_calendars", createRealtimeScheduleResponse.getTargetcalendars());
                    
                       body.put("callback_url", createRealtimeScheduleResponse.getCallbackUrl());
                     
                       body.put("redirect_urls", createRealtimeScheduleResponse.getRealtimeScheduleredirecturls());

	                   HttpHeaders httpHeaders = new HttpHeaders();
	                        
	                   httpHeaders.setBearerAuth(CLIENT_SECRET);
	                        
	                   HttpEntity<Object> requestEntity = new HttpEntity<Object>(body, httpHeaders);
	            
	                   RealtimeScheduleResponse realtimeScheduleResponse = null;
	                
	         try {
	               
	        	   ResponseEntity<RealtimeScheduleResponse> responseEntity = restTemplate
	                           .exchange(uriBuilder.toUriString(), HttpMethod.POST, requestEntity, RealtimeScheduleResponse.class);
	        	   realtimeScheduleResponse = responseEntity.getBody();
	              
	           } catch (HttpStatusCodeException hse) {
	                   log.error("Cronofy realtime schedule create failed: " + hse.getResponseBodyAsString());
	                      throw new ApplicationException(Constants.ERROR_STATUS,CalendarConstants.CAL_ERR_REAlTIME_SCHEDULE_LINKING_FAILED, null);
	           } catch (RestClientException rce) {
	                   log.error("Cronofy realtime schedule create failed: ", rce);
	                      throw new ApplicationException(Constants.ERROR_STATUS,CalendarConstants.CAL_ERR_REAlTIME_SCHEDULE_LINKING_FAILED, null);
	           }
	    	 
	             return realtimeScheduleResponse;
		   }
       
       
       public GetScheduleStatusResponse getscheduleStatus(final String Token) {
	    	 
	    	  RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
	         
	          String url = String.format(API_BASE_URL + "/v1/real_time_scheduling?token=%s", Token);
		 		
	    	  UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);
	    	          
	    	        HttpHeaders httpHeaders = new HttpHeaders();
	                        
	                httpHeaders.setBearerAuth(CLIENT_SECRET);
	                        
	                HttpEntity<Object> requestEntity = new HttpEntity<Object>(null, httpHeaders);
	            
	                GetScheduleStatusResponse getScheduleStatusResponse = null;
	                
	         try {
	               
	        	   ResponseEntity<GetScheduleStatusResponse> responseEntity = restTemplate
	                           .exchange(uriBuilder.toUriString(), HttpMethod.GET, requestEntity, GetScheduleStatusResponse.class);
	        	   
	        	   getScheduleStatusResponse = responseEntity.getBody();
	              
	             } catch (HttpStatusCodeException hse) {
	                   log.error("Cronofy realtime get schedule status failed: " + hse.getResponseBodyAsString());
	                      throw new ApplicationException(Constants.ERROR_STATUS,CalendarConstants.CAL_ERR_REAlTIME_SCHEDULE_LINKING_FAILED, null);
	             } catch (RestClientException rce) {
	                   log.error("Cronofy realtime get schedule status failed:", rce);
	                      throw new ApplicationException(Constants.ERROR_STATUS,CalendarConstants.CAL_ERR_REAlTIME_SCHEDULE_LINKING_FAILED, null);
	            }
	    	 
	           return getScheduleStatusResponse;
		 }
	 
       public FreeBusyResponse getFreeBusyEvents(final String accessToken, final String CalendarId, final String StartDate, final String EndDate) {
	    	 
	    	  RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
	         
	          //String url = String.format(API_BASE_URL + "/v1/free_busy?from=%s&to=%s&tzid=Etc/UTC&calendar_ids[]=%s", StartDate, EndDate, CalendarId);
		 		
	          String url = String.format(API_BASE_URL + "/v1/free_busy?from=%s&to=%s&tzid=Etc/UTC", StartDate, EndDate);
	          
	    	  UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);
	    	          
	    	        HttpHeaders httpHeaders = new HttpHeaders();

	                httpHeaders.setBearerAuth(accessToken);
	                        
	                HttpEntity<Object> requestEntity = new HttpEntity<Object>(null, httpHeaders);
	            
	                FreeBusyResponse getFreeBusyResponse = null;
	                
	         try {
	               
	        	   ResponseEntity<FreeBusyResponse> responseEntity = restTemplate
	                           .exchange(uriBuilder.toUriString(), HttpMethod.GET, requestEntity, FreeBusyResponse.class);
	        	   
	        	   getFreeBusyResponse = responseEntity.getBody();
	              
	             } catch (HttpStatusCodeException hse) {
	                   log.error("Cronofy get free busy event failed: " + hse.getResponseBodyAsString());
	                      throw new ApplicationException(Constants.ERROR_STATUS,CalendarConstants.CAL_ERR_GET_FREE_BUSY_EVENT_FAILED, null);
	             } catch (RestClientException rce) {
	                   log.error("Cronofy get free busy event failed: ", rce);
	                      throw new ApplicationException(Constants.ERROR_STATUS,CalendarConstants.CAL_ERR_GET_FREE_BUSY_EVENT_FAILED, null);
	            }
	    	 
	           return getFreeBusyResponse;
		 }
	  
         private ClientHttpRequestFactory getClientHttpRequestFactory() {
	         int timeout = 8000;
	         HttpComponentsClientHttpRequestFactory clientHttpRequestFactory
	                = new HttpComponentsClientHttpRequestFactory();
	         clientHttpRequestFactory.setConnectTimeout(timeout);
	         return clientHttpRequestFactory;
	        }
	 
	 
}
