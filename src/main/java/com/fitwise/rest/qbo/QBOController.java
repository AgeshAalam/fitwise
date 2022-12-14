package com.fitwise.rest.qbo;

import com.fitwise.constants.Constants;
import com.fitwise.constants.QboConstants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.service.RedisService;
import com.fitwise.service.qbo.ClientFactoryService;
import com.fitwise.service.qbo.QBOService;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.qbo.Entity;
import com.intuit.ipp.data.CompanyInfo;
import com.intuit.ipp.data.Error;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.exception.InvalidTokenException;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.services.QueryResult;
import com.intuit.oauth2.client.OAuth2PlatformClient;
import com.intuit.oauth2.config.OAuth2Config;
import com.intuit.oauth2.config.Scope;
import com.intuit.oauth2.data.BearerTokenResponse;
import com.intuit.oauth2.exception.InvalidRequestException;
import com.intuit.oauth2.exception.OAuthException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * Controller will communicate with qbo and manage the connection
 */
@RestController
@RequestMapping(value = "/qbo")
@CrossOrigin("*")
@Slf4j
public class QBOController {

    @Autowired
    ClientFactoryService factory;

    @Autowired
    public QBOService helper;

    @Autowired
    private QBOService qboService;

    @Autowired
    private RedisService redisService;

    private static final String failureMsg="Failed";

    /**
     * Controller mapping for qbo connection
     * @return
     */
    @GetMapping(value = "/connectToQuickbooks")
    public @ResponseBody View connectToQuickbooks() {
        log.info("inside connectToQuickbooks ");
        OAuth2Config oauth2Config = factory.getOAuth2Config();
        String redirectUri = factory.getPropertyValue("OAuth2AppRedirectUri");
        String csrf = oauth2Config.generateCSRFToken();
        redisService.set(QboConstants.KEY_CSRF_TOKEN,csrf);
        try {
            List<Scope> scopes = new ArrayList<>();
            scopes.add(Scope.Accounting);
            return new RedirectView(oauth2Config.prepareUrl(scopes, redirectUri, csrf), true, true, false);
        } catch (InvalidRequestException exception) {
            log.error("Exception calling connectToQuickbooks ", exception);
        }
        return null;
    }

    /**
     *  This is the redirect handler you configure in your app on developer.intuit.com
     *  The Authorization code has a short lifetime.
     *  Hence Unless a user action is quick and mandatory, proceed to exchange the Authorization Code for
     *  BearerToken
     *
     * @param authCode
     * @param state
     * @param realmId
     * @param session
     * @return
     */
    @GetMapping(value = "/oauth2redirect")
    public @ResponseBody String callBackFromOAuth(@RequestParam("code") String authCode, @RequestParam("state") String state, @RequestParam(value = "realmId", required = false) String realmId, HttpSession session) {
        log.info("inside oauth2redirect of sample"  );
        try {
            String csrfToken = redisService.get(QboConstants.KEY_CSRF_TOKEN);
            if (csrfToken.equals(state)) {
                redisService.set(QboConstants.KEY_REALM_ID, realmId);
                redisService.set(QboConstants.KEY_AUTH_CODE, authCode);
                OAuth2PlatformClient client  = factory.getOAuth2PlatformClient();
                String redirectUri = factory.getPropertyValue("OAuth2AppRedirectUri");
                log.info("inside oauth2redirect of sample -- redirectUri " + redirectUri  );
                BearerTokenResponse bearerTokenResponse = client.retrieveBearerTokens(authCode, redirectUri);
                redisService.set(QboConstants.KEY_ACCESS_TOKEN, bearerTokenResponse.getAccessToken());
                redisService.set(QboConstants.KEY_REFRESH_TOKEN, bearerTokenResponse.getRefreshToken());
                log.info("Qbo Access token expires in " + bearerTokenResponse.getExpiresIn());
                log.info("Qbo Refresh token expires in " + bearerTokenResponse.getXRefreshTokenExpiresIn());
                CompanyInfo companyInfo = getCompany();
                if(companyInfo == null || !companyInfo.getId().equalsIgnoreCase(factory.getPropertyValue("qbo.company.id")) || !companyInfo.getCompanyName().equalsIgnoreCase(factory.getPropertyValue("qbo.company.name"))){
                    redisService.set(QboConstants.KEY_REALM_ID, "");
                    redisService.set(QboConstants.KEY_AUTH_CODE, "");
                    redisService.set(QboConstants.KEY_ACCESS_TOKEN, "");
                    redisService.set(QboConstants.KEY_REFRESH_TOKEN, "");
                    throw new ApplicationException(Constants.BAD_REQUEST, "Qbo Connection failure", null);
                }
                return "Connected";
            }
            log.info("csrf token mismatch " );
        } catch (OAuthException exception) {
            log.error("Exception in callback handler ", exception);
        }
        return null;
    }

    /**
     * Getting the campany information for the current qbo connection
     * @return
     */
    public CompanyInfo getCompany() {
        log.info("Get company details");
        String realmId = redisService.get(QboConstants.KEY_REALM_ID);
        if (StringUtils.isEmpty(realmId)) {
            throw new ApplicationException(Constants.BAD_REQUEST,"No realm ID.  QBO calls only work if the accounting scope was passed!", null);
        }
        String accessToken = redisService.get(QboConstants.KEY_ACCESS_TOKEN);
        try {
            log.info("Query for company");
            DataService service = helper.getDataService(realmId, accessToken);
            String sql = "select * from companyinfo";
            QueryResult queryResult = service.executeQuery(sql);
            return processResponse(queryResult);
        } catch (InvalidTokenException e) {
            log.error("Error while calling executeQuery :: " + e.getMessage());
            log.info("received 401 during company info call, refreshing tokens now");
            OAuth2PlatformClient client  = factory.getOAuth2PlatformClient();
            String refreshToken = redisService.get(QboConstants.KEY_REFRESH_TOKEN);
            try {
                BearerTokenResponse bearerTokenResponse = client.refreshToken(refreshToken);
                redisService.set(QboConstants.KEY_ACCESS_TOKEN, bearerTokenResponse.getAccessToken());
                redisService.set(QboConstants.KEY_REFRESH_TOKEN, bearerTokenResponse.getRefreshToken());
                log.info("calling companyinfo using new tokens");
                DataService service = helper.getDataService(realmId, accessToken);
                String sql = "select * from company info";
                QueryResult queryResult = service.executeQuery(sql);
                return processResponse(queryResult);
            } catch (OAuthException e1) {
                log.error("Error while calling bearer token :: " + e.getMessage());
                throw new ApplicationException(Constants.BAD_REQUEST, failureMsg, null);
            } catch (FMSException e1) {
                log.error("Error while calling company currency :: " + e.getMessage());
                throw new ApplicationException(Constants.BAD_REQUEST, failureMsg, null);
            }
        } catch (FMSException exception) {
            List<Error> list = exception.getErrorList();
            list.forEach(error -> log.error("Error while calling executeQuery :: " + error.getMessage()));
            throw new ApplicationException(Constants.BAD_REQUEST, failureMsg, null);
        }
    }

    /**
     * Parsing the queryresult
     * @param queryResult
     * @return
     */
    private CompanyInfo processResponse(QueryResult queryResult) {
        log.info("Process response");
        CompanyInfo companyInfo = null;
        if (!queryResult.getEntities().isEmpty()) {
            companyInfo = (CompanyInfo) queryResult.getEntities().get(0);
        }
        return companyInfo;
    }

    private static final String SIGNATURE = "intuit-signature";

    @PostMapping(value = "/webhookevents")
    public ResponseEntity<ResponseModel> qboEventCapture(@RequestHeader(SIGNATURE) String signature, @RequestBody String payload){
        log.info("QBO webhook events starts");
        long profilingStart = new Date().getTime();
        long profilingStartTimeInMillis;
        long profilingEndTimeInMillis;
        log.info("Webhook triggered");
        if (!org.springframework.util.StringUtils.hasText(signature)) {
            return new ResponseEntity<>(new ResponseModel(Constants.SUCCESS_STATUS, Constants.RESPONSE_SUCCESS, null), HttpStatus.FORBIDDEN);
        }
        if (!org.springframework.util.StringUtils.hasText(payload)) {
            new ResponseEntity<>(new ResponseModel(Constants.SUCCESS_STATUS, Constants.RESPONSE_SUCCESS, null), HttpStatus.OK);
        }
        log.info("Payload : " + payload);
        profilingStartTimeInMillis = new Date().getTime();
        if (isRequestValid(signature, payload, factory.getPropertyValue("qbo.verifier.token"))) {
            profilingEndTimeInMillis = new Date().getTime();
            log.info("Request validation : time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));
            profilingStartTimeInMillis = new Date().getTime();
            JSONObject jsonObject  = new JSONObject(payload);
            JSONArray eventNotifications =  (JSONArray) jsonObject.get("eventNotifications");
            profilingEndTimeInMillis = new Date().getTime();
            log.info("Getting event notifications from payload : time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));

            log.info(String.valueOf(eventNotifications));
            long start = new Date().getTime();
            for(int i = 0; i < eventNotifications.length(); i++){
                profilingStartTimeInMillis = new Date().getTime();
                JSONObject eventNotification = (JSONObject) eventNotifications.get(i);
                String realmId  = (String) eventNotification.get("realmId");
                profilingEndTimeInMillis = new Date().getTime();
                log.info("Getting realm id : time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));
                if(redisService.get(QboConstants.KEY_REALM_ID).equalsIgnoreCase(realmId)){
                    JSONObject dataChangeObject = eventNotification.getJSONObject("dataChangeEvent");
                    JSONArray entities = dataChangeObject.getJSONArray("entities");
                    long startTime = new Date().getTime();
                    for(int j = 0; j < entities.length(); j++){
                        JSONObject entityObject = (JSONObject) entities.get(i);
                        Entity entity = new Entity();
                        entity.setId(entityObject.getString("id"));
                        entity.setName(entityObject.getString("name"));
                        entity.setOperation(entityObject.getString("operation"));
                        entity.setLastUpdated((String)entityObject.get("lastUpdated"));
                        if(entity.getName().equalsIgnoreCase("BillPayment")){
                            profilingStartTimeInMillis = new Date().getTime();
                            qboService.updateVendorBillPayment(entity);
                            profilingEndTimeInMillis = new Date().getTime();
                            log.info("Update vendor bill payment : time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));

                        }
                    }
                    profilingEndTimeInMillis = new Date().getTime();
                    log.info("Saving Entitiesvent in QBO : " +(entities.length()) + "Entities : total time taken in milliseconds :" +(profilingEndTimeInMillis-startTime));
                }
            }
            profilingEndTimeInMillis = new Date().getTime();
            log.info("Saving Events in QBO : " +(eventNotifications.length()) + "event notifications : total time taken in milliseconds :" +(profilingEndTimeInMillis-start));
            log.info("Total time for Qbo webhook event api :" +(profilingEndTimeInMillis-profilingStart));
            return new ResponseEntity<>(new ResponseModel(Constants.SUCCESS_STATUS, Constants.RESPONSE_SUCCESS, null), HttpStatus.FORBIDDEN);
        } else {
            return new ResponseEntity<>(new ResponseModel(Constants.SUCCESS_STATUS, Constants.RESPONSE_SUCCESS, null), HttpStatus.FORBIDDEN);
        }
    }

    private static final String ALGORITHM = "HmacSHA256";
    public boolean isRequestValid(String signature, String payload, String verifier) {
        log.info("Validation");
        if (signature == null) {
            return false;
        }
        try {
            SecretKeySpec secretKey = new SecretKeySpec(verifier.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(secretKey);
            String hash = Base64.getEncoder().encodeToString(mac.doFinal(payload.getBytes()));
            return hash.equals(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException exception) {
            return false;
        }
    }

}