package com.fitwise.service.qbo;

import javax.annotation.PostConstruct;

import com.intuit.oauth2.client.OAuth2PlatformClient;
import com.intuit.oauth2.config.Environment;
import com.intuit.oauth2.config.OAuth2Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

/**
 * Managing the qbo client activity
 */
@Service
@PropertySource(value="classpath:/application.properties", ignoreResourceNotFound=true)
public class ClientFactoryService {

    @Autowired
    org.springframework.core.env.Environment env;

    OAuth2PlatformClient client;
    OAuth2Config oauth2Config;

    /**
     * Initiating the authentication with qbo
     */
    @PostConstruct
    public void init() {
        oauth2Config = new OAuth2Config.OAuth2ConfigBuilder(env.getProperty("OAuth2AppClientId"), env.getProperty("OAuth2AppClientSecret")) //set client id, secret
                .callDiscoveryAPI(Environment.SANDBOX) // call discovery API to populate urls
                .buildConfig();
        client  = new OAuth2PlatformClient(oauth2Config);
    }

    /**
     * Get the auth client
     * @return
     */
    public OAuth2PlatformClient getOAuth2PlatformClient()  {
        return client;
    }

    /**
     * Get auth config
     * @return
     */
    public OAuth2Config getOAuth2Config()  {
        return oauth2Config;
    }

    /**
     * Getting the environment value for the given property name
     * @param proppertyName
     * @return
     */
    public String getPropertyValue(String proppertyName) {
        return env.getProperty(proppertyName);
    }

}