package com.fitwise.utils;

import com.fitwise.entity.User;
import com.fitwise.entity.UserRole;
import com.fitwise.entity.UserRoleMapping;
import net.authorize.Environment;
import net.authorize.api.controller.base.ApiOperationBase;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class AppUtils {

    public static String generateRandomSpecialCharacters(int length) {
        String password = new Random().ints(10, 33, 122).collect(StringBuilder::new,
                StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return password;
    }

    public static int generateRandonNumber() {
        return 100000 + new Random().nextInt(900000);
    }


    public static HttpComponentsClientHttpRequestFactory sslExceptionHandler() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        return requestFactory;
    }

    public static Set<UserRole> getUserRoles(User user){
        Set<UserRole> userRoles = new HashSet<>();
        for (UserRoleMapping userRoleMapping :  user.getUserRoleMappings()) {
            userRoles.add(userRoleMapping.getUserRole());
        }
        return userRoles;
    }
}
