package com.fitwise.service.social;

import com.fitwise.authentication.UserNamePasswordAuthenticationToken;
import com.fitwise.constants.*;
import com.fitwise.entity.*;
import com.fitwise.entity.social.AppleAuthentication;
import com.fitwise.exception.ApplicationException;
import com.fitwise.properties.GeneralProperties;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.repository.UserRoleMappingRepository;
import com.fitwise.repository.UserRoleRepository;
import com.fitwise.repository.socialLogin.AppleAuthRepository;
import com.fitwise.service.UserService;
import com.fitwise.service.messaging.MessagingService;
import com.fitwise.service.qbo.FitwiseQboEntityService;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.utils.AppUtils;
import com.fitwise.utils.EmailContentUtil;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.mail.AsyncMailer;
import com.fitwise.utils.UserFieldValidator;
import com.fitwise.utils.appleLogin.AppleLoginUtil;
import com.fitwise.view.LoginResponseView;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.socialLogin.AppleLoginRequestView;
import lombok.RequiredArgsConstructor;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppleAuthService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AppleAuthRepository appleAuthRepository;

    @Autowired
    ValidationService validationService;

    @Autowired
    UserRoleMappingRepository userRoleMappingRepository;

    @Autowired
    UserRoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder bcryptPasswdEncoder;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private GeneralProperties generalProperties;

    private final AsyncMailer asyncMailer;

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

    /**
     * Used to validate apple sign in and grant Fitwise access token
     *
     * @param appleLoginRequestView
     * @return
     * @throws Exception
     */
    @Transactional
    public ResponseModel validateAppleSignIn(AppleLoginRequestView appleLoginRequestView) throws Exception {

        UserRole userRole = validationService.validateUserRole(appleLoginRequestView.getUserRole());
        PlatformType platformType = validationService.validateAndGetPlatform(appleLoginRequestView.getPlatformTypeId());

        /**
         * Manipulating the user input with regarding to apple login and verifying whether the given inputs are valid
         */
        if (!appleLoginRequestView.getIsAuthenticatedToAddNewRole()) {
            String auth = AppleLoginUtil.appleAuth(userRole, appleLoginRequestView.getAppleAuthorizationToken(), platformType);
            if (auth == null || auth.isEmpty()) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_INVALID_APPLE_AUTH_DATA, MessageConstants.ERROR);
            }
        }

        String userEmail = "";
        AppleAuthentication appleAuthentication = appleAuthRepository.findTop1ByAppleUserId(appleLoginRequestView.getAppleUserId());
        if (appleAuthentication != null) {
            // User already registered using apple authentication. So, getting email from apple authentication table
            if (appleAuthentication.getUser() != null) {
                userEmail = appleAuthentication.getUser().getEmail();
            } else {
                userEmail = appleLoginRequestView.getEmail();
            }
        } else {
            // No user belongs to given apple authentication. So, getting email from request data
            userEmail = appleLoginRequestView.getEmail();
        }

        if (userEmail.isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_LOGIN_NOT_PROCESSABLE, MessageConstants.ERROR);
        }
        userFieldValidator.isTestBotEmail(userEmail);
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

        if (isUserRegisteredAlreadyForAnotherRole
                && !isUserRegisteredAlreadyForThisRole
                && !appleLoginRequestView.getIsAuthenticatedToAddNewRole()) {
            throw new ApplicationException(Constants.CAN_EDIT, MessageConstants.PROMPT_TO_ADD_NEW_ROLE, MessageConstants.ERROR);
        }

        Map<String, Object> resMap = new HashMap<String, Object>();
        generateFitwiseAccessToken(userEmail, appleLoginRequestView, resMap);

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_USER_ADDED, resMap);
    }


    @Transactional
    private void generateFitwiseAccessToken(String userEmail, AppleLoginRequestView appleLoginRequestView, Map<String, Object> resMap) {
        boolean isNewRegistration = false;
        User user = null;
        if (!userEmail.isEmpty()) {
            boolean sendMailNotification = false;

            user = userRepository.findByEmail(userEmail);

            if (user != null) {// User already exists
                boolean hasRole = false;

                //Checking whether the registered user has already the role
                UserRole role = roleRepository.findByName(appleLoginRequestView.getUserRole());
                if (role == null) {
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ROLE_NOT_FOUND, null);
                } else {
                    for (UserRole userRole : AppUtils.getUserRoles(user)) {
                        // Checking whether the user with the same role already registered
                        if (userRole.getName().equalsIgnoreCase(role.getName())) {
                            hasRole = true;
                            break;
                        }
                    }
                }
                boolean isDataAlreadyExists = true;

                AppleAuthentication appleAuthentication = appleAuthRepository.findByUserUserIdAndUserRole(user.getUserId(), appleLoginRequestView.getUserRole());
                if (appleAuthentication == null) {
                    isDataAlreadyExists = false;
                    appleAuthentication = new AppleAuthentication();
                    appleAuthentication.setAppleUserId(appleLoginRequestView.getAppleUserId());
                    appleAuthentication.setUser(user);
                    appleAuthentication.setUserRole(appleLoginRequestView.getUserRole());
                }

                Set<AppleAuthentication> appleAuthSet = new HashSet<>();
                appleAuthSet.add(appleAuthentication);
                if (!hasRole) {
                    isNewRegistration= true;
                    UserRoleMapping userRoleMapping = new UserRoleMapping();
                    userRoleMapping.setUser(user);
                    userRoleMapping.setUserRole(role);
                    userRoleMappingRepository.save(userRoleMapping);
                    user.setUserRoleMappings(Stream.of(userRoleMapping).collect(Collectors.toList()));

                    sendMailNotification = true;
                }
                if (!isDataAlreadyExists) {
                    user.setAppleAuth(appleAuthSet);
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
                 * NEW USER REGISTRATION THROUGH APPLE
                 */
                UserRole role = roleRepository.findByName(appleLoginRequestView.getUserRole());
                if (role == null) {
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ROLE_NOT_FOUND, null);
                }

                /*
                 * To overcome spring security check, creating random password
                 *
                 * If the user has registered first time through apple login, password will be
                 * empty. So, set random password and clear the password field while logging out
                 * from the app. If Random password is not set, we are getting authentication error
                 * since no password is available
                 */
                String randomPassword = AppUtils.generateRandomSpecialCharacters(15);
                user = new User();
                user.setEmail(userEmail);
                user.setPassword(bcryptPasswdEncoder.encode(randomPassword));

                AppleAuthentication appleAuthentication = new AppleAuthentication();
                appleAuthentication.setUser(user);
                appleAuthentication.setAppleUserId(appleLoginRequestView.getAppleUserId());
                appleAuthentication.setUserRole(appleLoginRequestView.getUserRole());

                Set<AppleAuthentication> appleAuthSet = new HashSet<AppleAuthentication>();
                appleAuthSet.add(appleAuthentication);

                user.setAppleAuth(appleAuthSet);
                user.setNewSocialRegistration(true);

                UserRoleMapping userRoleMapping = new UserRoleMapping();
                userRoleMapping.setUser(user);
                userRoleMapping.setUserRole(role);
                userRoleMappingRepository.save(userRoleMapping);
                user.setUserRoleMappings(Stream.of(userRoleMapping).collect(Collectors.toList()));

                userRepository.save(user);

                UserProfile userProfile = new UserProfile();
                String firstName = "";
                if (appleLoginRequestView.getFirstName() == null){
                    userProfile.setFirstName(firstName);
                } else {
                    userProfile.setFirstName(appleLoginRequestView.getFirstName());
                }
                String lastName = appleLoginRequestView.getLastName();
                if (lastName == null) {
                    lastName = "";
                }
                userProfile.setLastName(lastName);
                userProfile.setUser(user);
                userProfileRepository.save(userProfile);
                fitwiseQboEntityService.createOrUpdateQboUser(user, role.getName());
                LoginResponseView loginResponseView = login(userEmail, randomPassword, role.getName());

                resMap.put(KeyConstants.KEY_EMAIL, userEmail);
                resMap.put(KeyConstants.KEY_AUTH_TOKEN, loginResponseView.getAuthToken());
                resMap.put(KeyConstants.KEY_INITIAL_REGISTRATION, "true");

                sendMailNotification = true;

            }
            resMap.put(Constants.USER_SIGN_UP_DATE, user.getCreatedDate());

            //Send mail for sign up
            if (sendMailNotification) {
                if (appleLoginRequestView.getUserRole().equalsIgnoreCase(SecurityFilterConstants.ROLE_INSTRUCTOR)) {
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
                } else if (appleLoginRequestView.getUserRole().equalsIgnoreCase(SecurityFilterConstants.ROLE_MEMBER)) {
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
                    if(appleLoginRequestView.getUserRole().equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR)){
                        messagingService.startConversationByAdmin(user.getUserId(), fitwiseUtils.getWelcomeMessage(SecurityFilterConstants.ROLE_INSTRUCTOR),appleLoginRequestView.getUserRole());
                    }else if(appleLoginRequestView.getUserRole().equalsIgnoreCase(KeyConstants.KEY_MEMBER)){
                        messagingService.startConversationByAdmin(user.getUserId(), fitwiseUtils.getWelcomeMessage(SecurityFilterConstants.ROLE_MEMBER),appleLoginRequestView.getUserRole());
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


    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException(email);
        }
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        for (UserRole role : AppUtils.getUserRoles(user)) {
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
                grantedAuthorities);
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

}
