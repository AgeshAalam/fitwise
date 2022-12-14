package com.fitwise.service.social;

import com.fasterxml.jackson.databind.JsonNode;
import com.fitwise.authentication.UserNamePasswordAuthenticationToken;
import com.fitwise.constants.*;
import com.fitwise.entity.*;
import com.fitwise.entity.social.FacebookAuthentication;
import com.fitwise.exception.ApplicationException;
import com.fitwise.properties.GeneralProperties;
import com.fitwise.repository.*;
import com.fitwise.repository.socialLogin.FacebookAuthRepository;
import com.fitwise.service.UserService;
import com.fitwise.service.messaging.MessagingService;
import com.fitwise.service.qbo.FitwiseQboEntityService;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.utils.*;
import com.fitwise.utils.mail.AsyncMailer;
import com.fitwise.view.LoginResponseView;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.auth.FacebookAuthenticationView;
import com.fitwise.view.socialLogin.FBPhoneNumberOnlyRequestView;
import com.fitwise.view.socialLogin.FbUserProfileIDWithEmailRequestView;
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
import org.springframework.social.support.URIBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class FbAuthService implements UserDetailsService {

    private String userEmail = "";

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
    private UserRoleMappingRepository userRoleMappingRepository;

    @Autowired
    FacebookAuthRepository facebookAuthRepository;

    @Autowired
    ValidationService validationService;

    @Autowired
    UserOtpRepository userOtpRepository;

    @Autowired
    private GeneralProperties generalProperties;

    @Autowired
    private AsyncMailer asyncMailer;

    @Autowired
    FitwiseUtils fitwiseUtils;

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
    public Map<String, Object> connectionFacebook(FacebookAuthenticationView facebookAuth)
            throws MalformedURLException, ProtocolException, IOException, ApplicationException {
        log.info("Facebook Authentication starts");
        log.info("Facebook authentication triggered.");

        long profilingStart = new Date().getTime();
        ValidationUtils.validateEmail(facebookAuth.getEmail());

        if (facebookAuth.getFirstName() == null || facebookAuth.getFirstName().isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FIRST_NAME_ERROR, Constants.RESPONSE_INVALID_DATA);
        }

        if (facebookAuth.getFacebookAppAccessToken() == null || facebookAuth.getFacebookAppAccessToken().isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_FACEBOOK_APP_ACCESS_TOKEN_EMPTY, Constants.RESPONSE_INVALID_DATA);
        }

        if (facebookAuth.getFacebookUserAccessToken() == null || facebookAuth.getFacebookUserAccessToken().isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_FACEBOOK_USER_ACCESS_TOKEN_EMPTY, Constants.RESPONSE_INVALID_DATA);
        }

        if (facebookAuth.getUserRole() == null || facebookAuth.getUserRole().isEmpty()) {
            throw new ApplicationException(Constants.ERROR_STATUS, ValidationMessageConstants.MSG_ROLE_NULL, Constants.RESPONSE_INVALID_DATA);
        }

        if (facebookAuth.getFacebookUserProfileId() == null || facebookAuth.getFacebookUserProfileId().isEmpty()) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_FB_PROFILE_ID_NULL, Constants.RESPONSE_INVALID_DATA);
        }
        log.info("Basic validations  : Time taken in millis : "+(new Date().getTime()-profilingStart));


        // URL connection
        profilingStart = new Date().getTime();
        URIBuilder builder = URIBuilder.fromUri(String.format("%s/debug_token", "https://graph.facebook.com"));
        builder.queryParam("access_token", facebookAuth.getFacebookAppAccessToken());
        builder.queryParam("input_token", facebookAuth.getFacebookUserAccessToken());
        URI uri = builder.build();
        RestTemplate restTemplate = new RestTemplate();

        try {
            JsonNode resp = null;

            Boolean isValid = resp.path("data").findValue("is_valid").asBoolean();
            if (!isValid) {
                throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY,
                        ValidationMessageConstants.MSG_FACEBOOK_ACCESS_TOKEN_INVALID, Constants.RESPONSE_INVALID_DATA);
            } else {
                log.info(ValidationMessageConstants.MSG_FACEBOOK_ACCESS_TOKEN_VALID);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        log.info("Facebook url connection : Time taken in millis : "+(new Date().getTime()-profilingStart));

        profilingStart = new Date().getTime();
        userEmail = facebookAuth.getEmail();
        userFieldValidator.isTestBotEmail(userEmail);

        /**
         * To check whether a user has already has the role else
         * throwing error to get user's permission to sync account of another role
         *
         * getIsRoleAddPermissionEnabled() - If value is true then user has authorized to sync his old role account.
         * So, the below validation is not needed
         */

        UserRole userRole = validationService.validateUserRole(facebookAuth.getUserRole());
        User user = userRepository.findByEmail(facebookAuth.getEmail());
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

        if (isUserRegisteredAlreadyForAnotherRole
                && !isUserRegisteredAlreadyForThisRole
                && !facebookAuth.getIsRoleAddPermissionEnabled()) {
            throw new ApplicationException(Constants.CAN_EDIT, MessageConstants.PROMPT_TO_ADD_NEW_ROLE, MessageConstants.ERROR);
        }

        log.info("Basic validations for mail and role : Time taken in millis : "+(new Date().getTime()-profilingStart));

        Map<String, Object> resMap = new HashMap<String, Object>();
        profilingStart = new Date().getTime();
        generateFitwiseAccessToken(facebookAuth, resMap);
        log.info("Generating fitwise access token : Time taken in millis : "+(new Date().getTime()-profilingStart));
        log.info("Facebook Authentication ends");


        return resMap;
    }


    /**
     * Used to generate access token for fitwise
     *
     * @param facebookAuth
     * @param resMap
     * @throws ApplicationException
     */
    @Transactional
    private void generateFitwiseAccessToken(FacebookAuthenticationView facebookAuth, Map<String, Object> resMap) throws ApplicationException {
        log.info("Generate fitwise access token starts");

        long profilingStart;
        boolean isNewRegistration = false;
        User user = null;
        if (!userEmail.isEmpty()) {
            profilingStart = new Date().getTime();
            boolean sendMailNotification = false;
            user = userRepository.findByEmail(userEmail);

            if (user != null) { // User already exists
                boolean hasRole = false;
                /*
                 * Checking whether the registered user has already the role
                 */
                UserRole role = roleRepository.findByName(facebookAuth.getUserRole());
                if (role == null) {
                    throw new ApplicationException(Constants.ERROR_STATUS, ValidationMessageConstants.MSG_ROLE_NOT_FOUND, null);
                } else {
                    for (UserRole userRole : AppUtils.getUserRoles(user)) { //AKHIL //user.getRole()) {
                        /*
                         * Checking whether the user with the same role already registered
                         */
                        if (userRole.getName().equalsIgnoreCase(role.getName())) {
                            hasRole = true;
                            log.info("FacebookSignIn - User already exists");
                            break;
                        }
                    }
                }

                /*FacebookAuthentication fbAuth = new FacebookAuthentication();
                fbAuth.setFacebookUserAccessToken(facebookAuth.getFacebookUserAccessToken());
                fbAuth.setFacebookUserProfileId(facebookAuth.getFacebookUserProfileId());
                fbAuth.setEmail(facebookAuth.getEmail());
                fbAuth.setUser(user);*/

                boolean isDataAlreadyExists = true;
                log.info("Basic role validations in case of existing user : Time taken in millis : "+(new Date().getTime()-profilingStart));


                profilingStart = new Date().getTime();
                FacebookAuthentication fbAuth = facebookAuthRepository.findByEmailAndUserRole(facebookAuth.getEmail(), facebookAuth.getUserRole());
                if (fbAuth == null) {
                    isDataAlreadyExists = false;
                    fbAuth = new FacebookAuthentication();
                    fbAuth.setFacebookUserAccessToken(facebookAuth.getFacebookUserAccessToken());
                    fbAuth.setFacebookUserProfileId(facebookAuth.getFacebookUserProfileId());
                    fbAuth.setEmail(facebookAuth.getEmail());
                    fbAuth.setUserRole(facebookAuth.getUserRole());
                    fbAuth.setUser(user);
                }

                Set<FacebookAuthentication> fbAuthSet = new HashSet<FacebookAuthentication>();
                fbAuthSet.add(fbAuth);
                if (!hasRole) {
                    isNewRegistration = true;
                    UserRoleMapping userRoleMapping = new UserRoleMapping();
                    userRoleMapping.setUser(user);
                    userRoleMapping.setUserRole(role);
                    userRoleMappingRepository.save(userRoleMapping); //AKHIL
                    user.setUserRoleMappings(Stream.of(userRoleMapping).collect(Collectors.toList())); //AKhil

                    sendMailNotification = true;
                }
                if (!isDataAlreadyExists) {
                    user.setFacebookAuth(fbAuthSet);
                }
                userRepository.save(user);
                log.info("DB update in case of existing user : Time taken in millis : "+(new Date().getTime()-profilingStart));

                profilingStart = new Date().getTime();
                fitwiseQboEntityService.createOrUpdateQboUser(user, role.getName());
                log.info("Create or update QBO user : Time taken in millis : "+(new Date().getTime()-profilingStart));

                profilingStart = new Date().getTime();
                LoginResponseView loginResponseView = login(userEmail, user.getPassword(), role.getName());
                log.info("Login : Time taken in millis : "+(new Date().getTime()-profilingStart));

                resMap.put(KeyConstants.KEY_EMAIL, userEmail);
                resMap.put(KeyConstants.KEY_AUTH_TOKEN, loginResponseView.getAuthToken());
                resMap.put(KeyConstants.KEY_INITIAL_REGISTRATION, "false");
            } else {
                profilingStart = new Date().getTime();
                isNewRegistration = true;
                /**
                 * NEW USER REGISTRATION THROUGH FACEBOOK
                 */
                UserRole role = null;
                try {
                    role = roleRepository.findByName(facebookAuth.getUserRole());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (role == null) {
                    throw new ApplicationException(Constants.ERROR_STATUS, ValidationMessageConstants.MSG_ROLE_NOT_FOUND, null);
                }
                log.info("Basic role validations in case of new user : Time taken in millis : "+(new Date().getTime()-profilingStart));

                /*
                 * To overcome spring security check, creating random password
                 */
                profilingStart = new Date().getTime();
                String randomPassword = AppUtils.generateRandomSpecialCharacters(15);
                user = new User();
                user.setEmail(userEmail);
                user.setPassword(bcryptPasswdEncoder.encode(randomPassword));

                FacebookAuthentication facebookAuthentication = new FacebookAuthentication();
                facebookAuthentication.setFacebookUserProfileId(facebookAuth.getFacebookUserProfileId());
                facebookAuthentication.setFacebookUserAccessToken(facebookAuth.getFacebookUserAccessToken());
                facebookAuthentication.setEmail(facebookAuth.getEmail());
                facebookAuthentication.setUserRole(facebookAuth.getUserRole());
                facebookAuthentication.setUser(user);

                Set<FacebookAuthentication> facebookAuthSet = new HashSet<FacebookAuthentication>();
                facebookAuthSet.add(facebookAuthentication);

                user.setFacebookAuth(facebookAuthSet);
                user.setNewSocialRegistration(true);
                // user.setRole(Stream.of(role).collect(Collectors.toSet()));

                UserRoleMapping userRoleMapping = new UserRoleMapping();
                userRoleMapping.setUser(user);
                userRoleMapping.setUserRole(role);
                userRoleMappingRepository.save(userRoleMapping); //AKHIL
                user.setUserRoleMappings(Stream.of(userRoleMapping).collect(Collectors.toList())); //AKhil

                userRepository.save(user);
                log.info("DB update in case of new user : Time taken in millis : "+(new Date().getTime()-profilingStart));

                profilingStart = new Date().getTime();
                fitwiseQboEntityService.createOrUpdateQboUser(user, role.getName());
                log.info("Create or update QBO user : Time taken in millis : "+(new Date().getTime()-profilingStart));

                profilingStart = new Date().getTime();
                UserProfile userProfile = new UserProfile();
                userProfile.setFirstName(facebookAuth.getFirstName());
                String lastName = facebookAuth.getLastName();
                if (lastName == null) {
                    lastName = "";
                }
                userProfile.setLastName(lastName);
                userProfile.setUser(user);
                userProfileRepository.save(userProfile);
                log.info("user profile DB update : Time taken in millis : "+(new Date().getTime()-profilingStart));

                profilingStart = new Date().getTime();
                LoginResponseView loginResponseView = login(userEmail, randomPassword, role.getName());
                log.info("Login : Time taken in millis : "+(new Date().getTime()-profilingStart));

                resMap.put(KeyConstants.KEY_EMAIL, userEmail);
                resMap.put(KeyConstants.KEY_AUTH_TOKEN, loginResponseView.getAuthToken());
                resMap.put(KeyConstants.KEY_INITIAL_REGISTRATION, "true");

                sendMailNotification = true;
            }
            resMap.put(Constants.USER_SIGN_UP_DATE, user.getCreatedDate());

            //Send mail for sign up
            if (sendMailNotification) {
                profilingStart = new Date().getTime();
                if (facebookAuth.getUserRole().equalsIgnoreCase(SecurityFilterConstants.ROLE_INSTRUCTOR)) {
                    //Send mail to instructor for sign up
                    String subject = EmailConstants.SIGNUP_INSTRUCTOR_SUBJECT;
                    String trainnrStudio = EmailConstants.TRAINNR_STUDIO_LINK.replace(EmailConstants.LITERAL_APP_URL, generalProperties.getInstructorBaseUrl());
                    String mailBody = EmailConstants.SIGNUP_INSTRUCTOR_CONTENT;
                    String userName = fitwiseUtils.getUserFullName(user);
                    mailBody = EmailConstants.BODY_HTML_TEMPLATE_3_PARA_WITH_BUTTON.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
                            .replace(EmailConstants.EMAIL_BODY, mailBody)
                            //.replace("#MAIL_BODY_2#", EmailConstants.SIGNUP_INSTRUCTOR_CONTENT_2)
                            .replace(EmailConstants.LITERAL_MAIL_BODY_3, EmailConstants.SIGNUP_INSTRUCTOR_CONTENT_3)
                            .replace(EmailConstants.EMAIL_SUPPORT_URL, trainnrStudio);
                    mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
                    asyncMailer.sendHtmlMail(user.getEmail(), subject, mailBody);
                } else if (facebookAuth.getUserRole().equalsIgnoreCase(SecurityFilterConstants.ROLE_MEMBER)) {
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
                log.info("Sending sign up mail : Time taken in millis : "+(new Date().getTime()-profilingStart));

                profilingStart = new Date().getTime();
                try{
                    if(sendMailNotification){
                        if(facebookAuth.getUserRole().equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR)){
                            messagingService.startConversationByAdmin(user.getUserId(), fitwiseUtils.getWelcomeMessage(SecurityFilterConstants.ROLE_INSTRUCTOR),facebookAuth.getUserRole());
                        }else if(facebookAuth.getUserRole().equalsIgnoreCase(KeyConstants.KEY_MEMBER)){
                            messagingService.startConversationByAdmin(user.getUserId(), fitwiseUtils.getWelcomeMessage(SecurityFilterConstants.ROLE_MEMBER),facebookAuth.getUserRole());
                        }
                    }
                }catch (Exception e){
                    log.info("start conversation failed");
                }
                log.info("Sending message from admin for new user : Time taken in millis : "+(new Date().getTime()-profilingStart));


            }
        }
        resMap.put(KeyConstants.KEY_IS_NEW_REGISTRATION, isNewRegistration);
        if(user != null){
            resMap.put(KeyConstants.KEY_USER_ID, user.getUserId());
        }
        log.info("Generate fitwise access token ends");

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
        Set<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();
        for (UserRole role : AppUtils.getUserRoles(user)) { //AKHIL //user.getRole()) {
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
                grantedAuthorities);
    }


    /**
     * Used to validate facebook profile id and if it is already exists in db and mapped with an email, returns the login response
     *
     * @param fbPhoneNumberOnlyRequestView - Request view that contains fb user id with access token and app token
     * @return
     * @throws IOException
     */
    @Transactional
    public ResponseModel validateFbUserProfileId(FBPhoneNumberOnlyRequestView fbPhoneNumberOnlyRequestView) throws IOException {

        // Validating user inputs
        validationService.isValidFbUserProfileId(fbPhoneNumberOnlyRequestView.getFacebookUserProfileId());
        validationService.validateUserRole(fbPhoneNumberOnlyRequestView.getUserRole());
        if (fbPhoneNumberOnlyRequestView.getFacebookAppAccessToken().isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_FACEBOOK_APP_ACCESS_TOKEN_EMPTY, MessageConstants.ERROR);
        } else if (fbPhoneNumberOnlyRequestView.getFacebookUserAccessToken().isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_FACEBOOK_USER_ACCESS_TOKEN_EMPTY, MessageConstants.ERROR);
        }

        FacebookAuthentication facebookAuthentication = facebookAuthRepository.findByFacebookUserProfileIdAndUserRole(fbPhoneNumberOnlyRequestView.getFacebookUserProfileId(), fbPhoneNumberOnlyRequestView.getUserRole());
        if (facebookAuthentication == null || facebookAuthentication.getUser().getEmail().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)) {
            throw new ApplicationException(Constants.NOT_EXIST_STATUS, MessageConstants.MSG_FB_PROFILE_NOT_FOUND, MessageConstants.ERROR);
        }

        // Checking whether the email already present for the profile id else returning error. Email will be got from user
        // and validateAndLoginUsingUserEnteredEmail api will be used

        if (facebookAuthentication.getUser() == null ||
                (facebookAuthentication.getUser().getEmail() == null &&
                        facebookAuthentication.getUser().getEmail().isEmpty())) {
            throw new ApplicationException(Constants.NOT_EXIST_STATUS, MessageConstants.MSG_FB_EMAIL_MAPPING_NOT_FOUND, MessageConstants.ERROR);
        }

        // Checking whether the user has registered for this role. If not throwing message to Client team to get approval for adding new role
        User user = facebookAuthentication.getUser();

        // Setting the new user related data to the old facebookAuthentication object and verifying it
        FacebookAuthenticationView facebookAuthenticationView = new FacebookAuthenticationView();
        facebookAuthenticationView.setUserRole(fbPhoneNumberOnlyRequestView.getUserRole());
        facebookAuthenticationView.setFacebookAppAccessToken(fbPhoneNumberOnlyRequestView.getFacebookAppAccessToken());
        facebookAuthenticationView.setFacebookUserAccessToken(fbPhoneNumberOnlyRequestView.getFacebookUserAccessToken());
        facebookAuthenticationView.setEmail(facebookAuthentication.getUser().getEmail());
        facebookAuthenticationView.setIsRoleAddPermissionEnabled(false);

        UserProfile userProfile = userProfileRepository.findByUserUserId(user.getUserId());
        if (userProfile != null) {
            facebookAuthenticationView.setFirstName(userProfile.getFirstName());
            facebookAuthenticationView.setLastName(userProfile.getLastName());
        }

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_USER_ADDED, connectionFacebook(facebookAuthenticationView));
    }


    /**
     * If a facebook account doesn't have any email under its account and only phone number, email will be got from user
     * Checking whether the email is validated using OTP
     * Checking whether any local fitwise accounts are available already for entered email. If yes, throw conflict and asking using whether we can merge account
     *
     * @param fbUserProfileIDWithEmailRequestView
     * @return
     * @throws IOException
     */
    @Transactional
    public ResponseModel validateAndLoginUsingUserEnteredEmail(FbUserProfileIDWithEmailRequestView fbUserProfileIDWithEmailRequestView) throws IOException {
        log.info("Validate and login using mail starts");
        long start = new Date().getTime();
        long profiingStart;

        // Validating user inputs
        ValidationUtils.emailRegexValidate(fbUserProfileIDWithEmailRequestView.getEmail());
        validationService.isValidFbUserProfileId(fbUserProfileIDWithEmailRequestView.getFacebookUserProfileId());
        UserRole role = validationService.validateUserRole(fbUserProfileIDWithEmailRequestView.getUserRole());
        if (fbUserProfileIDWithEmailRequestView.getFacebookAppAccessToken().isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_FACEBOOK_APP_ACCESS_TOKEN_EMPTY, MessageConstants.ERROR);
        } else if (fbUserProfileIDWithEmailRequestView.getFacebookUserAccessToken().isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_FACEBOOK_USER_ACCESS_TOKEN_EMPTY, MessageConstants.ERROR);
        }

        // Checking if the user entered email is verified using OTP
        UserOtp userOtp = userOtpRepository.findFirstByEmailAndOtpOrderByUpdatedOnDesc(fbUserProfileIDWithEmailRequestView.getEmail(), fbUserProfileIDWithEmailRequestView.getOtp());
        if (userOtp == null || !userOtp.isVerified()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_EMAIL_NOT_VERIFIED, MessageConstants.ERROR);
        }

        // Getting the user using email and validating whether the same email already exists in Fitwise
        // If the user has already registered for other roles and newly coming in for this role, throwing him merge message
        User user = userRepository.findByEmail(fbUserProfileIDWithEmailRequestView.getEmail());
        if (user != null) {
            boolean isAlreadyRegisteredForCurrentRole = false;
            boolean isAlreadyRegisteredForOtherRoles = false;
            List<UserRoleMapping> userRoleMappings = userRoleMappingRepository.findByUserUserId(user.getUserId());

            for (UserRoleMapping userRoleMapping : userRoleMappings) {
                if (userRoleMapping.getUserRole() != null) {
                    if (userRoleMapping.getUserRole().getName().equalsIgnoreCase(fbUserProfileIDWithEmailRequestView.getUserRole())) {
                        isAlreadyRegisteredForCurrentRole = true;
                    } else {
                        isAlreadyRegisteredForOtherRoles = true;
                    }
                }
            }

            if (!isAlreadyRegisteredForCurrentRole && isAlreadyRegisteredForOtherRoles) {
                throw new ApplicationException(Constants.CAN_EDIT, MessageConstants.MSG_USER_PROFILE_MERGE, MessageConstants.ERROR);
            }
        }
        log.info("Basic validations for mail,otp and role : Time taken in millis : "+(new Date().getTime()-start));

        profiingStart = new Date().getTime();
        FacebookAuthenticationView facebookAuthentication = new FacebookAuthenticationView();
        facebookAuthentication.setEmail(fbUserProfileIDWithEmailRequestView.getEmail());
        facebookAuthentication.setFacebookUserProfileId(fbUserProfileIDWithEmailRequestView.getFacebookUserProfileId());
        facebookAuthentication.setFacebookUserAccessToken(fbUserProfileIDWithEmailRequestView.getFacebookUserAccessToken());
        facebookAuthentication.setFacebookAppAccessToken(fbUserProfileIDWithEmailRequestView.getFacebookAppAccessToken());
        facebookAuthentication.setUserRole(fbUserProfileIDWithEmailRequestView.getUserRole());
        facebookAuthentication.setFirstName(fbUserProfileIDWithEmailRequestView.getFirstName());
        facebookAuthentication.setLastName(fbUserProfileIDWithEmailRequestView.getLastName());
        log.info("DB update for FB authentication : Time taken in millis : "+(new Date().getTime()-profiingStart));


        profiingStart = new Date().getTime();
        Map<String, Object> responseMap = connectionFacebook(facebookAuthentication);
        log.info("Authentication Facebook : Time taken in millis : "+(new Date().getTime()-profiingStart));

        log.info("Validate and login using mail : Total Time taken in millis : "+(new Date().getTime()-start));
        log.info("Validate and login using mail ends");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_USER_ADDED,responseMap );
    }


}

