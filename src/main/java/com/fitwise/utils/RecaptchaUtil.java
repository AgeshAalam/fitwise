package com.fitwise.utils;

import com.fitwise.properties.GeneralProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecaptchaUtil {

    @Autowired
    private GeneralProperties generalProperties;

    public static final Map<String, String>
            RECAPTCHA_ERROR_CODE = new HashMap<>();

    static {
        RECAPTCHA_ERROR_CODE.put("missing-input-secret","The secret parameter is missing");
        RECAPTCHA_ERROR_CODE.put("invalid-input-secret","The secret parameter is invalid or malformed");
        RECAPTCHA_ERROR_CODE.put("missing-input-response","The response parameter is missing");
        RECAPTCHA_ERROR_CODE.put("invalid-input-response","The response parameter is invalid or malformed");
        RECAPTCHA_ERROR_CODE.put("bad-request","The request is invalid or malformed");
        RECAPTCHA_ERROR_CODE.put("timeout-or-duplicate","The response is no longer valid: either is too old or has been used previously.");
    }

    public String validateCapcha(String response, String remoteIp){
        String capchaResponse = "";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> responseEntity= restTemplate.postForEntity(generalProperties.getCapchaVerifyUrl() + "?secret=" + generalProperties.getCapchaSecretKey() + "&response=" + response + "&remoteip=" + remoteIp, new HashMap<>(), Map.class, new HashMap<>());
        Map<String, Object> responsData = responseEntity.getBody();
        boolean capchaResponseFromAPI = (boolean) responsData.get("success");
        if(!capchaResponseFromAPI){
            List<String> errorCodes = (List)responsData.get("error-codes");
            capchaResponse = errorCodes.stream()
                    .map(s -> RecaptchaUtil.RECAPTCHA_ERROR_CODE.get(s))
                    .collect(Collectors.joining(", "));
        }
        return capchaResponse;
    }
}