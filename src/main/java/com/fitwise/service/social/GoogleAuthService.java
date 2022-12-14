package com.fitwise.service.social;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitwise.authentication.AuthenticationProviderForToken;
import com.fitwise.authentication.TokenAuthentication;
import com.fitwise.authentication.UserNamePasswordAuthenticationToken;
import com.fitwise.constants.*;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.UserRole;
import com.fitwise.entity.UserRoleMapping;
import com.fitwise.entity.social.GoogleAuthentication;
import com.fitwise.exception.ApplicationException;
import com.fitwise.properties.GeneralProperties;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.repository.UserRoleMappingRepository;
import com.fitwise.repository.UserRoleRepository;
import com.fitwise.repository.socialLogin.GoogleAuthenticationRepository;
import com.fitwise.service.UserService;
import com.fitwise.service.messaging.MessagingService;
import com.fitwise.service.qbo.FitwiseQboEntityService;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.utils.*;
import com.fitwise.utils.mail.AsyncMailer;
import com.fitwise.view.LoginResponseView;
import com.fitwise.view.UserInfo;
import com.fitwise.view.auth.GoogleAuthenticationView;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class GoogleAuthService implements UserDetailsService {

    private String userEmail = "";

    @Autowired
    private AuthenticationProviderForToken authenticationProviderForToken;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private UserRoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder bcryptPasswdEncoder;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRoleMappingRepository userRoleMappingRepository; //AKHIL

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ValidationService validationService;
    @Autowired
    private GeneralProperties generalProperties;
    @Autowired
    private AsyncMailer asyncMailer;
    @Autowired
    FitwiseUtils fitwiseUtils;

    @Autowired
    private GoogleAuthenticationRepository googleAuthenticationRepository;

    @Autowired
    private FitwiseQboEntityService fitwiseQboEntityService;

    @Autowired
    private UserFieldValidator userFieldValidator;
    @Autowired
    private UserService userService;
    @Autowired
    private EmailContentUtil emailContentUtil;

    @Autowired
    private MessagingService messagingService;

    @Transactional
    public Map<String, Object> connectionGoogle(GoogleAuthenticationView googleAuth)
            throws IOException, ApplicationException {
        long startLogin = new Date().getTime();
        log.info("Google login started");
        long tempStart = new Date().getTime();
        if (googleAuth.getFirstName() == null || googleAuth.getFirstName().isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FIRST_NAME_ERROR,
                    Constants.RESPONSE_INVALID_DATA);
        }
        if (googleAuth.getGoogleAuthenticationToken() == null || googleAuth.getGoogleAuthenticationToken().isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_GOOGLE_ACCESS_TOKEN_EMPTY,
                    Constants.RESPONSE_INVALID_DATA);
        }
        if (googleAuth.getUserRole() == null || googleAuth.getUserRole().isEmpty()) {
            throw new ApplicationException(Constants.ERROR_STATUS, ValidationMessageConstants.MSG_ROLE_NULL,
                    Constants.RESPONSE_INVALID_DATA);
        }
        if (googleAuth.getClientId() == null || googleAuth.getClientId().isEmpty()) {
            throw new ApplicationException(Constants.ERROR_STATUS, ValidationMessageConstants.MSG_CLIENT_ID_NULL,
                    Constants.RESPONSE_INVALID_DATA);
        }
        log.info("Field validation completed : " + (new Date().getTime() - tempStart));
        tempStart = new Date().getTime();
        GoogleAuthentication googleAuthentication = new GoogleAuthentication();
        googleAuthentication.setGoogleAuthenticationToken(googleAuth.getGoogleAuthenticationToken());
        googleAuthentication.setClientId(googleAuth.getClientId());
        googleAuthentication.setUserRole(googleAuth.getUserRole());
        NetHttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new GsonFactory();
		/*
		Verifying google id token
		 */
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(googleAuthentication.getClientId()))
                .build();
        GoogleIdToken idToken = GoogleIdToken.parse(verifier.getJsonFactory(), googleAuthentication.getGoogleAuthenticationToken());
        GoogleIdToken.Payload payload = idToken.getPayload();
        userEmail = payload.getEmail();
        log.info("Google validation and get email : " + (new Date().getTime() - tempStart));
        tempStart = new Date().getTime();
        userFieldValidator.isTestBotEmail(userEmail);
        log.info("Test bot validation : " + (new Date().getTime() - tempStart));
        tempStart = new Date().getTime();
        /**
         * To check whether a user has already has the role else
         * throwing error to get user's permission to sync account of another role
         *
         * getIsRoleAddPermissionEnabled() - If value is true then user has authorized to sync his old role account.
         * So, the below validation is not needed
         */
        UserRole userRole = validationService.validateUserRole(googleAuth.getUserRole());
        log.info("User role validation : " + (new Date().getTime() - tempStart));
        tempStart = new Date().getTime();
        User user = userRepository.findByEmail(userEmail);
        boolean isUserRegisteredAlreadyForThisRole = false;
        boolean isUserRegisteredAlreadyForAnotherRole = false;
        if (user != null) {
            // Getting the roles of user
            List<UserRoleMapping> roleMappings = userRoleMappingRepository.findByUser(user);
            if (roleMappings != null && !roleMappings.isEmpty()) {
                for (UserRoleMapping userRoleMapping : roleMappings) {
                    if (userRoleMapping.getUserRole() != null &&
                            userRoleMapping.getUserRole().getName() != null &&
                            userRoleMapping.getUserRole().getName().equalsIgnoreCase(userRole.getName())) {
                        isUserRegisteredAlreadyForThisRole = true;
                    } else if (userRoleMapping.getUserRole() != null &&
                            userRoleMapping.getUserRole().getName() != null &&
                            !userRoleMapping.getUserRole().getName().isEmpty()) {
                        isUserRegisteredAlreadyForAnotherRole = true;
                    }
                }
            }
        }
        if (isUserRegisteredAlreadyForAnotherRole && !isUserRegisteredAlreadyForThisRole && !googleAuth.getIsRoleAddPermissionEnabled()) {
            throw new ApplicationException(Constants.CAN_EDIT, MessageConstants.PROMPT_TO_ADD_NEW_ROLE, MessageConstants.ERROR);
        }
        Map<String, Object> resMap = new HashMap<>();
        log.info("Role check : " + (new Date().getTime() - tempStart));
        tempStart = new Date().getTime();
        generateFitwiseAccessToken(googleAuth, resMap);
        log.info("login for fw : " + (new Date().getTime() - tempStart));
        log.info("Google login completion : " + (new Date().getTime() - startLogin));
        return resMap;
    }
    
    /**
     * @param googleAuth
     * @param resMap
     * @throws ApplicationException
     */
    private void generateFitwiseAccessToken(GoogleAuthenticationView googleAuth, Map<String, Object> resMap)
            throws ApplicationException {

        boolean isNewRegistration = false;
        User user = null;
        if (!userEmail.isEmpty()) {
            boolean sendMailNotification = false;

            user = userRepository.findByEmail(userEmail);

            if (user != null) {// User already exists

                boolean hasRole = false;
                /*
                 * Checking whether the registered user has already the role
                 */
                UserRole role = roleRepository.findByName(googleAuth.getUserRole());
                if (role == null) {
                    throw new ApplicationException(Constants.ERROR_STATUS, ValidationMessageConstants.MSG_ROLE_NOT_FOUND, null);
                } else {
                    for (UserRole userRole : AppUtils.getUserRoles(user)) { //AKHIL  //user.getRole()) {
                        /*
                         * Checking whether the user with the same role already registered
                         */
                        if (userRole.getName().equalsIgnoreCase(role.getName())) {
                            hasRole = true;
                            log.info("GoogleSignin - User already exists");
                            break;
                        }
                    }
                }

                boolean isDataAlreadyExists = true;

                GoogleAuthentication googleAuthentication = googleAuthenticationRepository.findByUserUserIdAndUserRole(user.getUserId(), googleAuth.getUserRole());
                if (googleAuthentication == null) {
                    isDataAlreadyExists = false;
                    googleAuthentication = new GoogleAuthentication();
                    googleAuthentication.setGoogleAuthenticationToken(googleAuth.getGoogleAuthenticationToken());
                    googleAuthentication.setUser(user);
                    googleAuthentication.setClientId(googleAuth.getClientId());
                    googleAuthentication.setUserRole(googleAuth.getUserRole());
                }

                Set<GoogleAuthentication> googleAuthSet = new HashSet<>();
                googleAuthSet.add(googleAuthentication);
                if (!hasRole) {
                    isNewRegistration = true;
                    UserRoleMapping userRoleMapping = new UserRoleMapping();
                    userRoleMapping.setUser(user);
                    userRoleMapping.setUserRole(role);
                    userRoleMappingRepository.save(userRoleMapping);
                    user.setUserRoleMappings(Stream.of(userRoleMapping).collect(Collectors.toList()));

                    sendMailNotification = true;

                }
                if (!isDataAlreadyExists) {
                    user.setGoogleAuth(googleAuthSet);
                }
                userRepository.save(user);
                fitwiseQboEntityService.createOrUpdateQboUser(user, role.getName());
                LoginResponseView loginResponseView = login(userEmail, user.getPassword(), role.getName());
                resMap.put(KeyConstants.KEY_EMAIL, userEmail);
                resMap.put(KeyConstants.KEY_AUTH_TOKEN, loginResponseView.getAuthToken());
                resMap.put(KeyConstants.KEY_INITIAL_REGISTRATION, "false");
            } else {
                isNewRegistration = true;
                /**
                 * NEW USER REGISTRATION THROUGH GOOGLE
                 */
                UserRole role = roleRepository.findByName(googleAuth.getUserRole());
                if (role == null) {
                    throw new ApplicationException(Constants.ERROR_STATUS, ValidationMessageConstants.MSG_ROLE_NOT_FOUND, null);
                }

                /*
                 * To overcome spring security check, creating random password!
                 *
                 * If the user has registered first time through social login, password will be
                 * empty. So, set random password and clear the password field while logging out
                 * from the app. If Random password is not set, we are getting authentication error
                 * since no password is available
                 */
                String randomPassword = AppUtils.generateRandomSpecialCharacters(15);
                user = new User();
                user.setEmail(userEmail);
                user.setPassword(bcryptPasswdEncoder.encode(randomPassword));

                GoogleAuthentication googleAuthentication = new GoogleAuthentication();
                googleAuthentication.setGoogleAuthenticationToken(googleAuth.getGoogleAuthenticationToken());
                googleAuthentication.setUser(user);
                googleAuthentication.setUserRole(googleAuth.getUserRole());
                googleAuthentication.setClientId(googleAuth.getClientId());

                Set<GoogleAuthentication> googleAuthSet = new HashSet<GoogleAuthentication>();
                googleAuthSet.add(googleAuthentication);

                user.setGoogleAuth(googleAuthSet);
                user.setNewSocialRegistration(true);

                //user.setRole(Stream.of(role).collect(Collectors.toSet()));

                UserRoleMapping userRoleMapping = new UserRoleMapping();
                userRoleMapping.setUser(user);
                userRoleMapping.setUserRole(role);
                user.setUserRoleMappings(Stream.of(userRoleMapping).collect(Collectors.toList())); //AKhil
                userRoleMappingRepository.save(userRoleMapping); //AKHIL

                userRepository.save(user);
                fitwiseQboEntityService.createOrUpdateQboUser(user, role.getName());
                UserProfile userProfile = new UserProfile();
                userProfile.setFirstName(googleAuth.getFirstName());
                String lastName = googleAuth.getLastName();
                if (lastName == null) {
                    lastName = "";
                }
                userProfile.setLastName(lastName);
                userProfile.setUser(user);
                userProfileRepository.save(userProfile);

                LoginResponseView loginResponseView = login(userEmail, randomPassword, role.getName());

                resMap.put(KeyConstants.KEY_EMAIL, userEmail);
                resMap.put(KeyConstants.KEY_AUTH_TOKEN, loginResponseView.getAuthToken());
                resMap.put(KeyConstants.KEY_INITIAL_REGISTRATION, "true");

                sendMailNotification = true;
            }
            resMap.put(Constants.USER_SIGN_UP_DATE, user.getCreatedDate());

            //Send mail for sign up
            if (sendMailNotification) {
                if (googleAuth.getUserRole().equalsIgnoreCase(SecurityFilterConstants.ROLE_INSTRUCTOR)) {
                    //Send mail to instructor for sign up
                    String subject = EmailConstants.SIGNUP_INSTRUCTOR_SUBJECT;
                    String trainnrStudio = EmailConstants.TRAINNR_STUDIO_LINK.replace(EmailConstants.LITERAL_APP_URL, generalProperties.getInstructorBaseUrl());
                    String mailBody = EmailConstants.SIGNUP_INSTRUCTOR_CONTENT;
                    String userName = fitwiseUtils.getUserFullName(user);
                    mailBody = EmailConstants.BODY_HTML_TEMPLATE_3_PARA_WITH_BUTTON.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
                            .replace(EmailConstants.EMAIL_BODY, mailBody)
                            .replace(EmailConstants.LITERAL_MAIL_BODY_3, EmailConstants.SIGNUP_INSTRUCTOR_CONTENT_3)
                            .replace(EmailConstants.EMAIL_SUPPORT_URL, trainnrStudio);
                    mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
                    asyncMailer.sendHtmlMail(user.getEmail(), subject, mailBody);
                } else if (googleAuth.getUserRole().equalsIgnoreCase(SecurityFilterConstants.ROLE_MEMBER)) {
                    //Send mail to member to update profile
                    String subject = EmailConstants.SIGNUP_MEMBER_PROFILE_SUBJECT;
                    String trainnrSite = EmailConstants.TRAINNR_SITE_LINK;
                    String mailBody = EmailConstants.SIGNUP_MEMBER_PROFILE_CONTENT;
                    String userName = fitwiseUtils.getUserFullName(user);
                    mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
                            .replace(EmailConstants.EMAIL_BODY, mailBody)
                            .replace(EmailConstants.EMAIL_SUPPORT_URL, trainnrSite);
                    mailBody = emailContentUtil.replaceMemberAppUrl(mailBody);
                    asyncMailer.sendHtmlMail(user.getEmail(), subject, mailBody);

                    //Send mail to member to explore programs
                    subject = EmailConstants.SIGNUP_MEMBER_START_SUBSCRIBE_SUBJECT;
                    trainnrSite = EmailConstants.TRAINNR_SITE_LINK;
                    mailBody = EmailConstants.SIGNUP_MEMBER_START_SUBSCRIBE_CONTENT;
                    mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
                            .replace(EmailConstants.EMAIL_BODY, mailBody)
                            .replace(EmailConstants.EMAIL_SUPPORT_URL, trainnrSite);
                    mailBody = emailContentUtil.replaceMemberAppUrl(mailBody);
                    asyncMailer.sendHtmlReminderMail(user.getEmail(), subject, mailBody);
                }
                try{
                    if(sendMailNotification){
                        if(googleAuth.getUserRole().equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR)){
                            messagingService.startConversationByAdmin(user.getUserId(), fitwiseUtils.getWelcomeMessage(SecurityFilterConstants.ROLE_INSTRUCTOR),googleAuth.getUserRole());
                        }else if(googleAuth.getUserRole().equalsIgnoreCase(KeyConstants.KEY_MEMBER)){
                            messagingService.startConversationByAdmin(user.getUserId(), fitwiseUtils.getWelcomeMessage(SecurityFilterConstants.ROLE_MEMBER),googleAuth.getUserRole());
                        }
                    }
                }catch (Exception e){
                    log.info("start conversation failed");
                }

            }
        }
        resMap.put(KeyConstants.KEY_IS_NEW_REGISTRATION, isNewRegistration);
        if(user != null){
            resMap.put(KeyConstants.KEY_USER_ID, user.getUserId());
        }
    }

    /**
     * The throwException method
     *
     * @param message
     * @param status
     * @throws ApplicationException
     */
    private void throwException(final String message, final long status) throws ApplicationException {
        throw new ApplicationException(status, message, "Internal Error");
    }

    @Transactional
    public LoginResponseView login(final String userName, final String password, String roleName) throws ApplicationException {
        UserDetails userDetails = userService.loadUserByEmail(userName, roleName);
        UserNamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UserNamePasswordAuthenticationToken(
                userDetails, password, null, true);

        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        LoginResponseView loginResponseView = new LoginResponseView();

        if (authentication.isAuthenticated() && authentication.getPrincipal() != null) {
            loginResponseView.setAuthToken(authentication.getName());
        } else {
            throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_USER_ALREADY_EXIST, "Internal Error");
        }
        return loginResponseView;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            log.info("UserName not found");
            throw new UsernameNotFoundException(email);
        }
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        for (UserRole role : AppUtils.getUserRoles(user)) { //AKHIL
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
                grantedAuthorities);
    }

    public UserInfo tokenValidate(final String token) throws ApplicationException {
        if (ValidationUtils.isEmptyString(token)) {
            log.debug("x-Auth Token is Empty");
            throwException("Unauthorized user", Constants.UNAUTHORIZED);
        }
        UserInfo userInfo = null;
        TokenAuthentication tokenAuthentication = new TokenAuthentication(null, token);

        Authentication authentication = authenticationProviderForToken.authenticate(tokenAuthentication);
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("Invalid Token");
            return null;
        } else {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            try {
                JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(authentication));
                if (jsonNode.isNull()) {
                    log.error("Error When input has no content to bind the value in JsonNode",
                            new ApplicationException(Constants.ERROR_STATUS,
                                    "Error When input has no content to bind the value in JsonNode", null));
                }
                Set<String> roleList = new LinkedHashSet<String>();
                for (JsonNode jsonNode2 : jsonNode.get(Constants.AUTHORITIES)) {
                    roleList.add(jsonNode2.get(Constants.AUTHORITY).asText());
                }
                userInfo = mapper.readValue(jsonNode.get(Constants.PRINCIPAL).toString(), UserInfo.class);
                userInfo.setRoleList(roleList);
            } catch (JsonParseException exception) {
                log.warn(exception.getMessage(), exception);
            } catch (JsonMappingException exception) {
                log.warn(exception.getMessage(), exception);
                exception.printStackTrace();
            } catch (IOException exception) {
                log.error(exception.getMessage(), exception);
            }
        }
        return userInfo;
    }
}
