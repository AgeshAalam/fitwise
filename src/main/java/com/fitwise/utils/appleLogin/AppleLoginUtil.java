package com.fitwise.utils.appleLogin;

import com.fitwise.constants.KeyConstants;
import com.fitwise.entity.PlatformType;
import com.fitwise.entity.UserRole;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.PrivateKey;
import java.util.Date;

public class AppleLoginUtil {
    private static String APPLE_AUTH_URL = "https://appleid.apple.com/auth/token";

    private static String KEY_ID = "M8T49V9A82";
    private static String TEAM_ID = "ARU3B8JAV2";
    private static String CLIENT_ID_MEMBER_MOBILE = "com.fitwise.trainnr.member";
    private static String CLIENT_ID_INSTRUCTOR_MOBILE = "com.fitwise.trainnr.instructor";
    private static String CLIENT_ID_MEMBER_WEB = "com.fitwise.trainnr.member-webapp";
    private static String CLIENT_ID_INSTRUCTOR_WEB = "com.fitwise.trainnr.instructor-webapp";

    private static PrivateKey pKey;

    private static PrivateKey getPrivateKey() throws Exception {
        //read your key
        InputStream inputStream = new ClassPathResource("apple/AuthKey_M8T49V9A82.p8").getInputStream();

        final PEMParser pemParser = new PEMParser(new InputStreamReader(inputStream));
        final JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        final PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject();
        final PrivateKey pKey = converter.getPrivateKey(object);

        return pKey;
    }

    private static String generateJWT(String clientId) throws Exception {
        if (pKey == null) {
            pKey = getPrivateKey();
        }

        String token = Jwts.builder()
                .setHeaderParam(JwsHeader.KEY_ID, KEY_ID)
                .setIssuer(TEAM_ID)
                .setAudience("https://appleid.apple.com")
                .setSubject(clientId)
                .setExpiration(new Date(System.currentTimeMillis() + (1000 * 60 * 5)))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .signWith(pKey, SignatureAlgorithm.ES256)
                .compact();

        return token;
    }

    /*
     * Returns unique user id from apple
     * */
    public static String appleAuth(UserRole userRole, String authorizationCode, PlatformType platformType) throws Exception {

        String clientId = "";
        if (platformType.getPlatformTypeId() == 1 || platformType.getPlatformTypeId() == 2) {
            // Mobile apps
            if (userRole.getName().equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR)) {
                clientId = CLIENT_ID_INSTRUCTOR_MOBILE;
            } else if (userRole.getName().equalsIgnoreCase(KeyConstants.KEY_MEMBER)) {
                clientId = CLIENT_ID_MEMBER_MOBILE;
            }
        } else {
            // Web apps
            if (userRole.getName().equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR)) {
                clientId = CLIENT_ID_INSTRUCTOR_WEB;
            } else if (userRole.getName().equalsIgnoreCase(KeyConstants.KEY_MEMBER)) {
                clientId = CLIENT_ID_MEMBER_WEB;
            }
        }


        String token = generateJWT(clientId);

        HttpResponse<String> response = Unirest.post(APPLE_AUTH_URL)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .field("client_id", clientId)
                .field("client_secret", token)
                .field("grant_type", "authorization_code")
                .field("code", authorizationCode)
                .asString();

        TokenResponse tokenResponse = new Gson().fromJson(response.getBody(), TokenResponse.class);
        IdTokenPayload idTokenPayload;
        if (tokenResponse.getId_token() != null && !tokenResponse.getId_token().isEmpty()) {
            String idToken = tokenResponse.getId_token();
            String payload = idToken.split("\\.")[1];//0 is header we ignore it for now
            String decoded = new String(Decoders.BASE64.decode(payload));
            idTokenPayload = new Gson().fromJson(decoded, IdTokenPayload.class);
        } else {
            return null;
        }
        return idTokenPayload.getSub();
    }


}