package com.fitwise.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitwise.admin.service.AdminService;
import com.fitwise.authentication.UserNamePasswordAuthenticationToken;
import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.EmailConstants;
import com.fitwise.constants.InstructorConstant;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.constants.SecurityFilterConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.encryption.AESEncryption;
import com.fitwise.entity.BlockedUser;
import com.fitwise.entity.Gender;
import com.fitwise.entity.Images;
import com.fitwise.entity.InstructorAwards;
import com.fitwise.entity.InstructorCertification;
import com.fitwise.entity.InstructorProgramExperience;
import com.fitwise.entity.OtherExpertise;
import com.fitwise.entity.ProgramExpertiseGoalsMapping;
import com.fitwise.entity.ProgramExpertiseMapping;
import com.fitwise.entity.ProgramTypes;
import com.fitwise.entity.SamplePrograms;
import com.fitwise.entity.TaxId;
import com.fitwise.entity.TaxTypes;
import com.fitwise.entity.User;
import com.fitwise.entity.UserOtp;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.UserProgramGoalsMapping;
import com.fitwise.entity.UserRole;
import com.fitwise.entity.UserRoleMapping;
import com.fitwise.entity.UserWorkoutStatus;
import com.fitwise.entity.WorkoutMapping;
import com.fitwise.entity.YearsOfExpertise;
import com.fitwise.entity.instructor.InstructorTierDetails;
import com.fitwise.entity.payments.authNet.Countries;
import com.fitwise.entity.product.FreeAccessProgram;
import com.fitwise.entity.view.ViewAdminPrograms;
import com.fitwise.exception.ApplicationException;
import com.fitwise.exercise.service.VimeoService;
import com.fitwise.properties.GeneralProperties;
import com.fitwise.repository.AwardsRepository;
import com.fitwise.repository.BlockedUserRepository;
import com.fitwise.repository.CertificateRepository;
import com.fitwise.repository.ExerciseRepository;
import com.fitwise.repository.GenderRepository;
import com.fitwise.repository.ImageRepository;
import com.fitwise.repository.InstructorExperienceRepository;
import com.fitwise.repository.OtherExpertiseRepository;
import com.fitwise.repository.ProgramExpertiseGoalsMappingRepository;
import com.fitwise.repository.ProgramExpertiseMappingRepository;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.ProgramTypeRepository;
import com.fitwise.repository.SampleProgramsRepository;
import com.fitwise.repository.TaxRepository;
import com.fitwise.repository.TaxTypesRepository;
import com.fitwise.repository.UserOtpRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.UserProgramGoalsMappingRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.repository.UserRoleMappingRepository;
import com.fitwise.repository.UserRoleRepository;
import com.fitwise.repository.UserWorkoutStatusRepository;
import com.fitwise.repository.WorkoutMappingRepository;
import com.fitwise.repository.WorkoutRepository;
import com.fitwise.repository.YearsOfExpertiseRepository;
import com.fitwise.repository.instructor.InstructorTierDetailsRepository;
import com.fitwise.repository.packaging.SubscriptionPackageRepository;
import com.fitwise.repository.payments.authnet.CountriesRepository;
import com.fitwise.repository.product.FreeAccessProgramReposity;
import com.fitwise.repository.view.ViewAdminProgramsRepository;
import com.fitwise.response.AdminListResponse;
import com.fitwise.response.InstructorResponse;
import com.fitwise.response.MemberResponse;
import com.fitwise.response.ProgramResponse;
import com.fitwise.service.instructor.InstructorAnalyticsService;
import com.fitwise.service.instructor.InstructorProgramService;
import com.fitwise.service.messaging.MessagingService;
import com.fitwise.service.qbo.FitwiseQboEntityService;
import com.fitwise.service.v2.instructor.UserLinkService;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.specifications.UserProfileSpecifications;
import com.fitwise.specifications.view.ViewAdminProgramSpecification;
import com.fitwise.utils.AppUtils;
import com.fitwise.utils.Convertions;
import com.fitwise.utils.EmailContentUtil;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.RequestParamValidator;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.utils.mail.AsyncMailer;
import com.fitwise.view.AddNewRoleView;
import com.fitwise.view.AwardDeleteRequestView;
import com.fitwise.view.AwardsCreationView;
import com.fitwise.view.AwardsRequestView;
import com.fitwise.view.BasicDetailsRequestView;
import com.fitwise.view.CertificateDeleteRequestView;
import com.fitwise.view.CertificationCreationView;
import com.fitwise.view.CertificationRequestView;
import com.fitwise.view.ExperienceUserView;
import com.fitwise.view.ExperienceView;
import com.fitwise.view.ExpertiseLevelView;
import com.fitwise.view.GoalsRequestView;
import com.fitwise.view.GoalsView;
import com.fitwise.view.LoginResponseView;
import com.fitwise.view.MemberGoalsView;
import com.fitwise.view.OtpView;
import com.fitwise.view.PostPhoneNumberView;
import com.fitwise.view.ProgramExpertiseView;
import com.fitwise.view.RegistrationUserView;
import com.fitwise.view.ResetPasswordRequestView;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.TaxIdView;
import com.fitwise.view.TaxView;
import com.fitwise.view.UserRequestView;
import com.fitwise.view.V2LoginResponseView;
import com.fitwise.view.ValidateEmailView;
import com.fitwise.view.instructor.AddNewRoleViewWithOtp;
import com.fitwise.view.instructor.TaxResponseView;
import com.intuit.ipp.exception.FMSException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class UserService.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    /**
     * The user repository.
     */
    @Autowired
    UserRepository userRepository;

    @Autowired
    GenderRepository genderRepository;

    /**
     * The role repository.
     */
    @Autowired
    UserRoleRepository userRoleRepository;

    /**
     * The user profile repository.
     */
    @Autowired
    UserProfileRepository userProfileRepository;

    /**
     * The certificate repository.
     */
    @Autowired
    CertificateRepository certificateRepository;

    /**
     * The awards repository.
     */
    @Autowired
    AwardsRepository awardsRepository;

    /**
     * The image repository.
     */
    @Autowired
    ImageRepository imageRepository;

    @Autowired
    InstructorProgramService instructorProgramService;

    @Autowired
    ProgramRepository programRepository;
    @Autowired
    YearsOfExpertiseRepository yearsOfExpertiseRepository;

    /**
     * The program type repository.
     */
    @Autowired
    ProgramTypeRepository programTypeRepository;

    /**
     * The tax types repository.
     */
    @Autowired
    TaxTypesRepository taxTypesRepository;

    /**
     * The tax repository.
     */
    @Autowired
    TaxRepository taxRepository;

    /**
     * The instructor experience repository.
     */
    @Autowired
    InstructorExperienceRepository instructorExperienceRepository;

    /**
     * The bcrypt passwd encoder.
     */
    @Autowired
    BCryptPasswordEncoder bcryptPasswdEncoder;

    /**
     * The authentication manager.
     */
    @Autowired
    AuthenticationManager authenticationManager;

    /**
     * The mapper.
     */
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * The user otp repository.
     */
    @Autowired
    UserOtpRepository userOtpRepository;

    /**
     * The general properties.
     */
    @Autowired
    GeneralProperties generalProperties;

    private final AsyncMailer asyncMailer;

    /**
     * The user program goals mapping repository.
     */
    @Autowired
    UserProgramGoalsMappingRepository userProgramGoalsMappingRepository;

    @Autowired
    ProgramExpertiseMappingRepository programExpertiseMappingRepository;

    @Autowired
    ProgramExpertiseGoalsMappingRepository programExpertiseGoalsMappingRepository;

    @Autowired
    ValidationService validationService;

    /**
     * The exercise repository.
     */
    @Autowired
    ExerciseRepository exerciseRepository;

    @Autowired
    UserRoleMappingRepository userRoleMappingRepository;

    @Autowired
    SubscriptionService subscriptionService;

    @Autowired
    UserComponents userComponents;

    @Autowired
    OtherExpertiseRepository otherExpertiseRepository;

    @Autowired
    BlockedUserRepository blockedUserRepository;

    @Autowired
    FitwiseUtils fitwiseUtils;

    @Autowired
    WorkoutRepository workoutRepository;

    @Autowired
    WorkoutMappingRepository workoutMappingRepository;

    @Autowired
    UserWorkoutStatusRepository userWorkoutStatusRepository;

    @Autowired
    FitwiseQboEntityService fitwiseQboEntityService;

    @Autowired
    SampleProgramsRepository sampleProgramsRepository;

    @Autowired
    AdminService adminService;

    @Autowired
    EmailContentUtil emailContentUtil;

    @Autowired
    MessagingService messagingService;

    @Autowired
    InstructorAnalyticsService instructorAnalyticsService;

    @Autowired
    SubscriptionPackageRepository subscriptionPackageRepository;

    @Autowired
    CountriesRepository countriesRepository;

    @Autowired
    VimeoService vimeoService;

    @Autowired
    AESEncryption aesEncryption;

    @Autowired
    ViewAdminProgramsRepository viewAdminProgramsRepository;

    private final UserLinkService userLinkService;
    
    /** The free access program repository. */
    private final FreeAccessProgramReposity freeAccessProgramReposity;
    
    /**
     * The instructor tier details repository.
     */
    @Autowired
    InstructorTierDetailsRepository instructorTierDetailsRepository;

    /**
     * Gets the user.
     *
     * @param userId the user id
     * @return the user
     */
    public User getUser(long userId) {
        return userRepository.getOne(userId);
    }

    /**
     * User save.
     * Once OTP validation is completed, this api will be called from Client apps to register a user!
     *
     * @param userView the user view
     * @return the map
     */
    @Transactional
    public Map<String, Object> userSave(final RegistrationUserView userView) throws FMSException {
        log.info("User register starts.");
        long apiStartTimeMillis = new Date().getTime();
        if (!(userView.getUserRole().equalsIgnoreCase(SecurityFilterConstants.ROLE_MEMBER) || userView.getUserRole().equalsIgnoreCase(SecurityFilterConstants.ROLE_INSTRUCTOR))) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ROLE_INCORRECT, null);
        }
        /*
         * Validations for Email - Should not be empty, Maximum length 100 characters, Should be a valid email
         * Validations for FirstName - Should not be empty, Maximum length 50 characters, only alphabets
         * Validations for LastName - Should not be empty, Maximum length 50 characters,only alphabets
         * Validations for Password - Should not be empty, Min length 8, Max length 16
         */
        RequestParamValidator.allowOnlyAlphabets(userView.getFirstName(), ValidationMessageConstants.MSG_FIRST_NAME_ERROR);
        RequestParamValidator.stringLengthValidation(userView.getFirstName(), null, 50L, ValidationMessageConstants.MSG_FIRST_NAME_ERROR);
        RequestParamValidator.allowOnlyAlphabets(userView.getLastName(), ValidationMessageConstants.MSG_LAST_NAME_ERROR);
        RequestParamValidator.stringLengthValidation(userView.getLastName(), null, 50L, ValidationMessageConstants.MSG_LAST_NAME_ERROR);
        RequestParamValidator.emptyString(userView.getEmail(), ValidationMessageConstants.MSG_EMAIL_EMPTY);
        RequestParamValidator.stringLengthValidation(userView.getEmail(), null, 100L, ValidationMessageConstants.MSG_ERR_EMAIL_MAX_LENGTH);
        if (!ValidationService.isValidEmailAddress(userView.getEmail())) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EMAIL_ERROR,
                    Constants.RESPONSE_INVALID_DATA);
        }
        RequestParamValidator.emptyString(userView.getUserRole(), ValidationMessageConstants.MSG_ROLE_NULL);
        log.info(StringConstants.LOG_FIELD_VALIDATION + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        /*
         * Checking whether the password has the required constraints
         */
        validationService.validatePassword(userView.getPassword());
        UserRole role = userRoleRepository.findByName(userView.getUserRole());
        if (role == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ROLE_NOT_FOUND,
                    Constants.RESPONSE_INVALID_DATA);
        }
        log.info("Checking whether the password has the required constraints : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        Map<String, Object> respMap = new HashMap<>();
        try {
            User user = userRepository.findByEmail(userView.getEmail()); // To check whether the user is already
            log.info("Query to get user : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            // registered
            if (user != null) {
                for (UserRole userRole : AppUtils.getUserRoles(user)) {
                    if (userRole.getName().equalsIgnoreCase(userView.getUserRole())) {
                        throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_USER_ALREADY_EXIST,
                                null);
                    }
                }
                log.info("Existing user: validate role : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                profilingEndTimeMillis = new Date().getTime();
                /**
                 * If the user is registering for a new role, the below checks needs to be done
                 * 1. Whether he has
                 */
                UserOtp userOtp = userOtpRepository.findFirstByEmailAndOtpOrderByUpdatedOnDesc(userView.getEmail(), userView.getOtp());
                log.info("Existing user: Query to get user otp : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                profilingEndTimeMillis = new Date().getTime();
                if (userOtp == null) {
                    throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_VERIFYING_OTP, MessageConstants.ERROR);
                }
                if (userOtp.getUpdatedOn() == null) {
                    throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_VERIFYING_OTP, MessageConstants.ERROR);
                }
                Timestamp timestampFromDB = userOtp.getUpdatedOn();
                Timestamp currentTimeStamp = new Timestamp(System.currentTimeMillis());
                /*
                 * Checking whether the registration is being done after 10 mins
                 */
                if (currentTimeStamp.getTime() - timestampFromDB.getTime() >= 10 * 60 * 1000) {
                    throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_SESSION_EXPIRED, MessageConstants.ERROR);
                }
                /*
                 * Checking whether the email is verified
                 */
                if (userOtp.isVerified() && !userOtp.isActive()) {
                    /*
                     * Setting new role to existing user
                     */
                    List<UserRoleMapping> userRoleMappings = userRoleMappingRepository.findByUser(user);
                    log.info("Existing user: Query to get user role mapping : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                    profilingEndTimeMillis = new Date().getTime();
                    UserRoleMapping userRoleMapping = new UserRoleMapping();
                    userRoleMapping.setUser(user);
                    userRoleMapping.setUserRole(role);
                    userRoleMappings.add(userRoleMapping);
                    user.setUserRoleMappings(userRoleMappings);
                    userRoleMappingRepository.save(userRoleMapping);
                    log.info("Existing user: Query to save user role mapping : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                    profilingEndTimeMillis = new Date().getTime();
                    user = userRepository.save(user);
                    log.info("Existing user: Query to save user : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                    profilingEndTimeMillis = new Date().getTime();
                    fitwiseQboEntityService.createOrUpdateQboUser(user, role.getName());
                    log.info("Existing user: Create or update QBO service : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                    userView.setId(user.getUserId());
                    createAuthToken(userView, respMap);
                } else {
                    throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_EMAIL_NOT_VERIFIED, MessageConstants.ERROR);
                }
            } else {
                profilingEndTimeMillis = new Date().getTime();
                createNewUser(userView, respMap);
                log.info("New user: Create new user ends : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            }
        } catch (ApplicationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.ERROR_INTERNAL_SERVER_FAILURE, MessageConstants.ERROR);
        }
        profilingEndTimeMillis = new Date().getTime();
        if (userView.getUserRole().equalsIgnoreCase(SecurityFilterConstants.ROLE_INSTRUCTOR)) {
            User user = userRepository.findByEmail(userView.getEmail());
            String subject = EmailConstants.SIGNUP_INSTRUCTOR_SUBJECT;
            String trainnrStudio = EmailConstants.TRAINNR_STUDIO_LINK.replace(EmailConstants.LITERAL_APP_URL, generalProperties.getInstructorBaseUrl());
            String mailBody = EmailConstants.SIGNUP_INSTRUCTOR_CONTENT;
            String userName = fitwiseUtils.getUserFullName(user);
            mailBody = EmailConstants.BODY_HTML_TEMPLATE_3_PARA_WITH_BUTTON
                    .replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
                    .replace(EmailConstants.EMAIL_BODY, mailBody)
                    .replace(EmailConstants.LITERAL_MAIL_BODY_3, EmailConstants.SIGNUP_INSTRUCTOR_CONTENT_3)
                    .replace(EmailConstants.EMAIL_SUPPORT_URL, trainnrStudio);
            mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
            asyncMailer.sendHtmlMail(user.getEmail(), subject, mailBody);
        } else {
            //Send mail to member to update profile
            User user = userRepository.findByEmail(userView.getEmail());
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
        log.info("sending mail : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        try {
            User user = userRepository.findByEmail(userView.getEmail());
            log.info("Query to get user : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            if (userView.getUserRole().equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR)) {
                messagingService.startConversationByAdmin(user.getUserId(), fitwiseUtils.getWelcomeMessage(SecurityFilterConstants.ROLE_INSTRUCTOR), userView.getUserRole());
            } else if (userView.getUserRole().equalsIgnoreCase(KeyConstants.KEY_MEMBER)) {
                messagingService.startConversationByAdmin(user.getUserId(), fitwiseUtils.getWelcomeMessage(SecurityFilterConstants.ROLE_MEMBER), userView.getUserRole());
            }
            log.info("Starting conversation : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        } catch (Exception exception) {
            log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
        }
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("User register ends.");
        return respMap;
    }

    /**
     * User email validate.
     *
     * @param emailView the email view
     * @return the response model
     * @throws ApplicationException the application exception
     */
    public ResponseModel userEmailValidate(ValidateEmailView emailView) {
        log.info("userEmailValidate starts.");
        long apiStartTimeMillis = new Date().getTime();

        long profilingStartTimeMillis = new Date().getTime();
        ValidationUtils.validateEmail(emailView.getEmail());
        if (ValidationUtils.isEmptyString(emailView.getRole())) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ROLE_NULL,
                    Constants.RESPONSE_INVALID_DATA);
        }
        ResponseModel res = new ResponseModel();
        try {
            User user = userRepository.findByEmail(emailView.getEmail());
            if (user != null) {
                LoginResponseView loginResponseView = new LoginResponseView();
                boolean isNewRole = true;
                /* Check if user is registering for a new role*/
                for (UserRole userRole : AppUtils.getUserRoles(user)) {
                    if (userRole.getName().equalsIgnoreCase(emailView.getRole())) {
                        isNewRole = false;
                        break;
                    }
                }
                /*
                 * If user registers for a new role, show him "Add new role" prompt
                 */
                if (isNewRole) {
                    loginResponseView.setUserId(user.getUserId());
                    loginResponseView.setSignUpDate(user.getCreatedDate());
                    loginResponseView.setNewRolePrompt(true);
                    /*
                     * If user has entered password for Fitwise,
                     * return it in the response which will be used by the Client
                     * to show appropriate pop-up to validate the user!
                     */
                    if (user.isEnteredFitwisePassword()) {
                        loginResponseView.setHasFitwisePassword(true);
                    }

                    res.setStatus(Constants.SUCCESS_STATUS);
                    res.setMessage(MessageConstants.PROMPT_TO_ADD_NEW_ROLE);
                    res.setPayload(loginResponseView);

                    long profilingEndTimeMillis = new Date().getTime();
                    log.info("Adding new role : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
                } else {
                    throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_USER_ALREADY_EXIST, MessageConstants.ERROR);
                }
            } else {
                UserOtp userOtp = new UserOtp();
                int otp = AppUtils.generateRandonNumber();
                List<UserOtp> userOtps = userOtpRepository.findByEmailOrderByOtpId(emailView.getEmail());
                if (!userOtps.isEmpty()) {
                    userOtp = userOtps.get(0);
                }
                userOtp.setEmail(emailView.getEmail());
                userOtp.setOtp(otp);
                userOtp.setActive(true);
                userOtpRepository.save(userOtp);

                String subject = EmailConstants.OTP_SUBJECT;
                String mailBody = EmailConstants.OTP_CONTENT.replace(EmailConstants.LITERAL_OTP, String.valueOf(otp));
                mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi ,").replace(EmailConstants.EMAIL_BODY, mailBody);
                if (KeyConstants.KEY_INSTRUCTOR.equalsIgnoreCase(emailView.getRole())) {
                    mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
                } else {
                    mailBody = emailContentUtil.replaceMemberAppUrl(mailBody);
                }
                asyncMailer.sendHtmlMail(emailView.getEmail(), subject, mailBody);

                res.setStatus(Constants.CONTENT_NEEDS_TO_BE_VALIDATE);
                res.setMessage(ValidationMessageConstants.MSG_VALIDATE_OTP);
                long profilingEndTimeMillis = new Date().getTime();
                log.info("OTP mail : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
            }
        } catch (Exception exception) {
            throw exception;
        }

        long apiEndTimeMillis = new Date().getTime();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("userEmailValidate ends.");

        return res;
    }

    /**
     * Method to will create a new user in Fitwise DB.
     *
     * @param userView the user view
     * @param respMap  the resp map
     */
    private void createNewUser(RegistrationUserView userView, Map<String, Object> respMap) {
        log.info("Create new user starts.");
        long apiStartTimeMillis = new Date().getTime();
        UserRole role = userRoleRepository.findByName(userView.getUserRole());
        log.info("New user: Query to get user role : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        if (role == null) {
            throw new ApplicationException(Constants.ERROR_STATUS, ValidationMessageConstants.MSG_ROLE_NOT_FOUND, null);
        }
        UserOtp userOtp = userOtpRepository.findFirstByEmailAndOtpOrderByUpdatedOnDesc(userView.getEmail(), userView.getOtp());
        log.info("New user: Query to get user otp : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if (userOtp == null) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_VERIFYING_OTP, MessageConstants.ERROR);
        }
        if (userOtp.getUpdatedOn() == null) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_VERIFYING_OTP, MessageConstants.ERROR);
        }
        Timestamp timestampFromDB = userOtp.getUpdatedOn();
        Timestamp currentTimeStamp = new Timestamp(System.currentTimeMillis());
        /* Checking whether the registration is being done after 10 mins */
        if (currentTimeStamp.getTime() - timestampFromDB.getTime() >= 10 * 60 * 1000) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_SESSION_EXPIRED, MessageConstants.ERROR);
        }
        /*
         * Checking whether the email is verified
         */
        if (userOtp.isVerified() && !userOtp.isActive()) {
            UserProfile userProfile = new UserProfile();
            userProfile.setFirstName(userView.getFirstName());
            String lastName = userView.getLastName();
            if (lastName == null) {
                lastName = "";
            }
            userProfile.setLastName(lastName);
            User newUser = new User();
            newUser.setEmail(userView.getEmail());
            newUser.setPassword(bcryptPasswdEncoder.encode(userView.getPassword()));
            //start
            UserRoleMapping userRoleMapping = new UserRoleMapping();
            userRoleMapping.setUser(newUser);
            userRoleMapping.setUserRole(role);
            //end
            newUser.setUserRoleMappings(Stream.of(userRoleMapping).collect(Collectors.toList())); //AKhil
            newUser.setEnteredFitwisePassword(true);
            userRoleMappingRepository.save(userRoleMapping); //AKHIL
            log.info("New user: Query to save user role mapping : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            userRepository.save(newUser);
            log.info("New user: Query to save user : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            userProfile.setUser(newUser);
            userProfileRepository.save(userProfile);
            log.info("New user: Query to save user profile : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            createAuthToken(userView, respMap);
            fitwiseQboEntityService.createOrUpdateQboUser(newUser, role.getName());
            log.info("New user: Create or update QBO user : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        } else {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_EMAIL_NOT_VERIFIED, MessageConstants.ERROR);
        }
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Create new user ends.");
    }


    /**
     * Load user by username.
     *
     * @param email the email
     * @return the user details
     */
    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            log.info("UserName not found");
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_FOUND, null);
        }
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        for (UserRole role : AppUtils.getUserRoles(user)) { // AKHIL
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
                grantedAuthorities);
    }

    /**
     * Load user.
     *
     * @param email the email
     * @return the user
     */
    public User loadUser(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_USERNAME_SECRET_INCORRECT, null);
        }
        return user;
    }

    /**
     * The throwException method.
     *
     * @param message the message
     * @param status  the status
     * @throws ApplicationException the application exception
     */
    private void throwException(final String message, final long status) {
        throw new ApplicationException(status, message, MessageConstants.ERROR_INTERNAL_SERVER_FAILURE);
    }

    /**
     * Method to get user Profile.
     *
     * @return ResponseModel - To return Success/Error status
     */
    public ResponseModel getUserProfile() {
        log.info("Get user profile starts.");
        long apiStartTimeMillis = new Date().getTime();
        ResponseModel res = new ResponseModel();
        Map<String, Object> profileObj = new HashMap<>();
        User user = userComponents.getUser();

        validationService.validateMemberId(user.getUserId());
        log.info("Get user and validate member : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();

        UserProfile userProfile = userProfileRepository.findByUser(user);
        log.info("Query: get user profile : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        profileObj.put(KeyConstants.KEY_EMAIL, user.getEmail());
        profileObj.put(KeyConstants.KEY_USER_FIRST_NAME, userProfile.getFirstName());
        profileObj.put(KeyConstants.KEY_USER_LAST_NAME, userProfile.getLastName());
        profileObj.put(KeyConstants.KEY_PROFILE_IMAGE, userProfile.getProfileImage());
        profileObj.put(KeyConstants.KEY_COVER_IMAGE, userProfile.getCoverImage());
        profileObj.put(KeyConstants.KEY_USER_BIO, userProfile.getBiography());
        profileObj.put(KeyConstants.KEY_ISD_CODE, userProfile.getIsdCode());
        if (userProfile.getCountryCode() != null) {
            profileObj.put(KeyConstants.KEY_COUNTRY_CODE, userProfile.getCountryCode());

            Countries country = countriesRepository.findByCountryCode(userProfile.getCountryCode());
            if (country != null)
                profileObj.put(KeyConstants.KEY_COUNTRY_NAME, country.getCountryName());
        }
        profileObj.put(KeyConstants.KEY_CONTACT_NUMBER, userProfile.getContactNumber());
        profileObj.put(KeyConstants.KEY_GENDER, userProfile.getGender());
        log.info("Query: get country from DB : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        List<ProgramExpertiseView> programExpertiseViews = getUserProgramExpertiseLevels();
        profileObj.put(KeyConstants.KEY_SELECTED_PROGRAM_EXPERTISE_LEVELS, programExpertiseViews);
        log.info("Get program expertise views : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        List<Long> programExpertiseMappings = new ArrayList<>();
        for (ProgramExpertiseView programExpertiseView : programExpertiseViews) {
            List<ExpertiseLevelView> expertiseLevelViews = programExpertiseView.getExpertiseLevels();
            for (ExpertiseLevelView expertiseLevelView : expertiseLevelViews) {
                if (expertiseLevelView.isSelected()) {
                    programExpertiseMappings.add(expertiseLevelView.getProgramExpertiseMappingId());
                }
            }
        }
        log.info("Construct program expertise mapping : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        GoalsRequestView goalsRequestView = new GoalsRequestView();
        goalsRequestView.setProgramExpertiseMappingIds(programExpertiseMappings);

        // Getting goals based on selected programTypes and expertiseLevels
        profileObj.put(KeyConstants.KEY_PROGRAM_GOALS, getGoals(goalsRequestView));

        res.setPayload(profileObj);
        res.setStatus(Constants.SUCCESS_STATUS);
        res.setMessage(MessageConstants.MSG_USER_PROFILE_FETCHED);
        log.info("Cosntruct response model : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get user profile ends.");

        return res;
    }

    /**
     * Method to update user profile.
     *
     * @param userRequestView - Class used to process UserProfile data
     * @return ResponseModel - To return Success/Error status
     * @throws ApplicationException the application exception
     */
    public ResponseModel updateUserProfile(UserRequestView userRequestView) {
        ResponseModel res = new ResponseModel();
        User user = userComponents.getUser();
        if (!ValidationUtils.isEmptyString(userRequestView.getDob())) {
            ValidationUtils.validateDOB(userRequestView.getDob());
        }
        UserProfile userProfile = updateUserProfileData(user, userRequestView);
        userProfile.setUser(user);
        userProfileRepository.save(userProfile);
        fitwiseQboEntityService.createOrUpdateQboUser(user, SecurityFilterConstants.ROLE_MEMBER);
        res.setPayload(null);
        res.setStatus(Constants.SUCCESS_STATUS);
        res.setMessage(MessageConstants.MSG_USER_PROFILE_SAVED);
        return res;
    }

    /**
     * Validating and setting the data to the user Object obtained from Database.
     *
     * @param user            the user
     * @param userRequestView the user request view
     * @return the user profile
     */
    public UserProfile updateUserProfileData(User user, UserRequestView userRequestView) {
        UserProfile userProfile = userProfileRepository.findByUser(user);
        if (!ValidationUtils.isEmptyString(userRequestView.getFirstName())) {
            userProfile.setFirstName(userRequestView.getFirstName());
        }
        if (!ValidationUtils.isEmptyString(userRequestView.getLastName())) {
            userProfile.setLastName(userRequestView.getLastName());
        }
        if (!ValidationUtils.isEmptyString(userRequestView.getDob())) {
            userProfile.setDob(userRequestView.getDob());
        }
        if (!ValidationUtils.isEmptyString(userRequestView.getBiography())) {
            userProfile.setBiography(userRequestView.getBiography());
        }
        Gender gender = genderRepository.findByGenderId(userRequestView.getGenderId());
        if (gender == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_GENDER_ERROR,
                    Constants.RESPONSE_INVALID_DATA);
        }
        userProfile.setGender(gender);

        if (userRequestView.getContactNumber() != null && userRequestView.getCountryCode() != null && !ValidationUtils.isEmptyString(userRequestView.getContactNumber())) {

            if (!ValidationUtils.validatePhonenumber(userRequestView.getCountryCode(), userRequestView.getContactNumber())) {
                throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, ValidationMessageConstants.MSG_PHONENUMBER_INVALID, null);
            }
            userProfile.setContactNumber(userRequestView.getContactNumber());
            userProfile.setCountryCode(userRequestView.getCountryCode());

        } else {
            userProfile.setContactNumber(null);
            userProfile.setCountryCode(null);
        }


        if (userRequestView.getNotificationStatus() != null) {
            userProfile.setNotificationStatus(userRequestView.getNotificationStatus());
        }

        return userProfile;
    }

    /**
     * To check whether the entered password matches to the email saved.
     *
     * @param email    - User's email
     * @param password - User's password
     * @throws ApplicationException the application exception
     */
    public void validateEmailPassword(String email, String password) {
        UserDetails userDetails = loadUserByUsername(email);
        UserNamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UserNamePasswordAuthenticationToken(
                userDetails, password, null, false);
        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        if (!authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_USERNAME_SECRET_INCORRECT, null);
        }
    }

    /**
     * Validate otp.
     *
     * @param otpView the otp view
     * @return true, if successful
     */
    public boolean validateOtp(OtpView otpView) {
        List<UserOtp> userOtps = userOtpRepository.findByEmailAndOtpOrderByOtpId(otpView.getEmail(), otpView.getOtp());
        if (!userOtps.isEmpty()) {
            Long expiryTime = (new Date().getTime() - userOtps.get(0).getUpdatedOn().getTime()) / 1000;
            if (expiryTime < generalProperties.getOtpExpiryInSeconds() && userOtps.get(0).isActive()) {
                userOtps.get(0).setActive(false);
                userOtps.get(0).setVerified(true);
                userOtpRepository.save(userOtps.get(0));
            } else {
                throw new ApplicationException(Constants.GONE, MessageConstants.MSG_OTP_EXPIRED, MessageConstants.ERROR);
            }
        } else {
            throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, ValidationMessageConstants.MSG_OTP_INVALID, MessageConstants.ERROR);
        }
        return true;
    }

    /**
     * Gets the user.
     *
     * @param userId the user id
     * @return the user
     */
    public User getUser(Long userId) {
        return userRepository.findByUserId(userId);
    }

    public ResponseModel getInstructorProfile() {
        User user = userComponents.getUser();
        return getInstructorProfile(Integer.parseInt(String.valueOf(user.getUserId())));
    }

    /**
     * Gets the instructor profile.
     *
     * @return the instructor profile
     */
    public ResponseModel getInstructorProfile(final int userId) {
        long start = new Date().getTime();
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_FOUND, null);
        }
        ResponseModel res = new ResponseModel();
        Map<String, Object> profileObj = new HashMap<>();
        Set<UserRole> roles = AppUtils.getUserRoles(user); //AKHIL
        boolean isInstructor = false;
        for (UserRole role : roles) {
            if (role.getName().equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR)) {
                isInstructor = true;
            }
        }
        if (isInstructor) {
            List<ExperienceView> instructorExperienceList = new ArrayList<>();
            TaxId taxId = taxRepository.findByUserUserId(user.getUserId());
            UserProfile userProfile = userProfileRepository.findByUser(user);
            profileObj.put(KeyConstants.KEY_USER_FIRST_NAME, userProfile.getFirstName());
            profileObj.put(KeyConstants.KEY_USER_LAST_NAME, userProfile.getLastName());
            profileObj.put(KeyConstants.KEY_USER_BIO, userProfile.getBiography());
            profileObj.put(KeyConstants.KEY_SHORT_BIO, userProfile.getShortBio());
            InstructorTierDetails instructorTierDetails = instructorTierDetailsRepository.findByUser(user);
            if(instructorTierDetails != null) {
            	profileObj.put(KeyConstants.KEY_TIER_TYPE_ID, instructorTierDetails.getTier().getTierId());
            }else {
            	profileObj.put(KeyConstants.KEY_TIER_TYPE_ID, null);
            }
            Long promoId = null;
            String promoUrl = null;
            String promoThumbnailUrl = null;
            String promoUploadStatus = null;
            if(userProfile.getPromotion() != null){
                promoId = userProfile.getPromotion().getPromotionId();
                if(userProfile.getPromotion().getVideoManagement() != null){
                    promoUrl = userProfile.getPromotion().getVideoManagement().getUrl();
                    promoThumbnailUrl = userProfile.getPromotion().getVideoManagement().getThumbnail().getImagePath();
                    promoUploadStatus = userProfile.getPromotion().getVideoManagement().getUploadStatus();
                }
            }
            profileObj.put(KeyConstants.KEY_PROMO_ID, promoId);
            profileObj.put(KeyConstants.KEY_PROMO_URL, promoUrl);
            profileObj.put(KeyConstants.KEY_PROMO_THUMBNAIL_URL, promoThumbnailUrl);
            profileObj.put(KeyConstants.KEY_PROMO_UPLOAD_STATUS, promoUploadStatus);



            profileObj.put(KeyConstants.KEY_CONTACT_NUMBER, userProfile.getContactNumber());
            profileObj.put(KeyConstants.KEY_ISD_CODE, userProfile.getIsdCode());
            profileObj.put(KeyConstants.KEY_COUNTRY_CODE, userProfile.getCountryCode());
            Countries countries = countriesRepository.findByCountryCode(userProfile.getCountryCode());
            if (countries != null) {
                profileObj.put(KeyConstants.KEY_COUNTRY_NAME, countries.getCountryName());
            } else {
                profileObj.put(KeyConstants.KEY_COUNTRY_NAME, null);
            }
            profileObj.put(KeyConstants.KEY_PROFILE_LOCATION, userProfile.getLocation());
            profileObj.put(KeyConstants.KEY_PROFILE_IMAGE, userProfile.getProfileImage());
            profileObj.put(KeyConstants.KEY_COVER_IMAGE, userProfile.getCoverImage());
            profileObj.put(KeyConstants.KEY_GENDER, userProfile.getGender());
            profileObj.put(KeyConstants.KEY_EMAIL, user.getEmail());
            List<InstructorCertification> instructorCertificationList = certificateRepository.findByUser(user);
            profileObj.put(KeyConstants.KEY_CERTIFICATION, instructorCertificationList);
            List<InstructorAwards> instructorAwardsList = awardsRepository.findByUser(user);
            profileObj.put(KeyConstants.KEY_AWARDS, instructorAwardsList);
            List<InstructorProgramExperience> instructorProgramExperienceList = instructorExperienceRepository.findByUserUserId(user.getUserId());
            List<ProgramTypes> programsTypesList = programTypeRepository.findByOrderByProgramTypeNameAsc();
            for (ProgramTypes programTypes : programsTypesList) {
                ExperienceView experienceView = new ExperienceView();
                innerLoop:
                for (int i = 0; i < instructorProgramExperienceList.size(); i++) {
                    if (programTypes.getProgramTypeId().equals(instructorProgramExperienceList.get(i).getProgramType().getProgramTypeId())) {
                        experienceView.setSelected(true);
                        experienceView.setYearsOfExperienceId(instructorProgramExperienceList.get(i).getExperience().getExperienceId());
                        experienceView.setNumberOfYears(instructorProgramExperienceList.get(i).getExperience().getNumberOfYears());
                        break innerLoop;
                    } else {
                        experienceView.setSelected(false);
                    }
                }
                experienceView.setProgramTypeId(programTypes.getProgramTypeId());
                experienceView.setProgramType(programTypes.getProgramTypeName());
                instructorExperienceList.add(experienceView);
            }
            profileObj.put(KeyConstants.KEY_INSTRUCTOR_EXPERIENCE, instructorExperienceList);
            List<OtherExpertise> otherExpertiseList = otherExpertiseRepository.findByUserUserId(user.getUserId());
            profileObj.put(KeyConstants.KEY_OTHER_EXPERTISE, otherExpertiseList);
            profileObj.put(KeyConstants.SOCIAL_LINKS, userLinkService.getSocialLinks(user));
            profileObj.put(KeyConstants.EXTERNAL_LINKS, userLinkService.getExternalLinks(user));
            if (taxId != null) {
                TaxView taxView = new TaxView();
                String taxNo = null;
                if(taxId.getTaxNumber() != null){
                    try{
                        taxNo = aesEncryption.decrypt(taxId.getTaxNumber());
                    }catch (Exception e){
                        log.info("Decryption failed");
                    }
                }
                taxView.setTaxNumber(Convertions.getMaskedString(taxNo));
                taxView.setTaxNumberType(taxId.getTaxTypes().getTaxNumberType());
                taxView.setTaxTypeId(taxId.getTaxTypes().getTaxTypeId());
                profileObj.put(KeyConstants.KEY_TAX_ID, taxView);
            } else {
                profileObj.put(KeyConstants.KEY_TAX_ID, null);
            }
            res.setPayload(profileObj);
            res.setStatus(Constants.SUCCESS_STATUS);
            res.setMessage(MessageConstants.MSG_USER_PROFILE_FETCHED);
        } else {
            throw new ApplicationException(Constants.NOT_FOUND, MessageConstants.MSG_WRONG_INSTRUCTOR_ID,
                    MessageConstants.ERROR);
        }
        log.info("Get instructor profile : time taken in millis : "+(new Date().getTime()-start));
        return res;
    }

    /**
     * Method to get YearsOfExpertise-dropDown.
     *
     * @return the years of expertise
     */
    public List<YearsOfExpertise> getYearsOfExpertise() {
        return yearsOfExpertiseRepository.findAll();
    }

    /**
     * Method to get tax id types.
     *
     * @return the tax types
     */
    public List<TaxTypes> getTaxTypes() {
        return taxTypesRepository.findAll();
    }

    /**
     * Method to get gender types.
     *
     * @return the gender types
     */
    public List<Gender> getGender() {
        return genderRepository.findAll();
    }

    /**
     * Method to update basic details-email,firstName,lastName,contactNumber and biography of the user.
     *
     * @param basicDetailsRequestView the basic details request view
     * @return the response model
     * @throws ApplicationException the application exception
     */
    public ResponseModel updateBasicDetails(BasicDetailsRequestView basicDetailsRequestView)  {
        log.info("Update basic details starts.");
        long apiStartTimeMillis = new Date().getTime();
        ResponseModel res = new ResponseModel();
        User user = userComponents.getUser();
        UserProfile userProfile = userProfileRepository.findByUser(user);
        if (ValidationUtils.isEmptyString(basicDetailsRequestView.getFirstName()) || basicDetailsRequestView.getFirstName().length() > 50
                || !validationService.isStringContainsOnlyAlphabets(basicDetailsRequestView.getFirstName())) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FIRST_NAME_ERROR,
                    Constants.RESPONSE_INVALID_DATA);
        }
        if (ValidationUtils.isEmptyString(basicDetailsRequestView.getLastName()) || basicDetailsRequestView.getLastName().length() > 50
                || !validationService.isStringContainsOnlyAlphabets(basicDetailsRequestView.getLastName())) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_LAST_NAME_ERROR,
                    Constants.RESPONSE_INVALID_DATA);
        }
        log.info(StringConstants.LOG_FIELD_VALIDATION + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        userProfile.setFirstName(basicDetailsRequestView.getFirstName());
        userProfile.setLastName(basicDetailsRequestView.getLastName());
        // Updating country code
        if (basicDetailsRequestView.getCountryCode() != null && !basicDetailsRequestView.getCountryCode().isEmpty()) {
            userProfile.setCountryCode(basicDetailsRequestView.getCountryCode());
        }
        if (basicDetailsRequestView.getContactNumber() != null && !ValidationUtils.isEmptyString(basicDetailsRequestView.getContactNumber())) {
            if (basicDetailsRequestView.getIsdCountryCode() == null || basicDetailsRequestView.getIsdCountryCode().isEmpty()) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_COUNTRY_CODE_IS_NULL, null);
            }
            if (basicDetailsRequestView.getIsdCode() == null || basicDetailsRequestView.getIsdCode().isEmpty()) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ISD_CODE_IS_NULL, null);
            }
            // Updating contact number with isd code and country code
            if (ValidationUtils.validatePhonenumber(basicDetailsRequestView.getIsdCountryCode(), basicDetailsRequestView.getContactNumber())) {
                userProfile.setContactNumber(basicDetailsRequestView.getContactNumber());
                userProfile.setIsdCode(basicDetailsRequestView.getIsdCode());
            } else {
                throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, ValidationMessageConstants.MSG_PHONENUMBER_INVALID, null);
            }
        }else{
            userProfile.setContactNumber(null);
            userProfile.setIsdCode(null);
        }
        log.info("Update contact and country code : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        userProfile.setBiography(basicDetailsRequestView.getAbout());
        Gender gender = null;
        if (basicDetailsRequestView.getGenderId() == null) {
            gender = genderRepository.findByGenderType(DBConstants.PREFER_NOT_TO_SAY);
        } else {
            gender = genderRepository.findByGenderId(basicDetailsRequestView.getGenderId());
        }
        if (gender == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_GENDER_ERROR, null);
        }
        userProfile.setGender(gender);
        userProfile.setUser(user);
        userProfileRepository.save(userProfile);
        log.info("Query to get gender and save user profile : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        fitwiseQboEntityService.createOrUpdateQboUser(user, SecurityFilterConstants.ROLE_INSTRUCTOR);
        log.info("Create or update QBO user : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        res.setPayload(null);
        res.setStatus(Constants.SUCCESS_STATUS);
        res.setMessage(MessageConstants.MSG_USER_BASIC_DETAILS_SAVED);
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Update basic details ends.");
        return res;
    }

    /**
     * Method to generate access token for Fitwise user.
     *
     * @param userView the user view
     * @param respMap  the resp map
     * @throws ApplicationException the application exception
     */
    private void createAuthToken(Object userView, Map<String, Object> respMap) {
        if (userView instanceof RegistrationUserView) {
            LoginResponseView loginResponseView = login(((RegistrationUserView) userView).getEmail(),
                    ((RegistrationUserView) userView).getPassword(), ((RegistrationUserView) userView).getUserRole());
            respMap.put(KeyConstants.KEY_AUTH_TOKEN, loginResponseView.getAuthToken());
            respMap.put(KeyConstants.KEY_USER_ID, loginResponseView.getUserId());
            respMap.put(Constants.USER_SIGN_UP_DATE, loginResponseView.getSignUpDate());
        } else if (userView instanceof AddNewRoleView) {
            LoginResponseView loginResponseView = login(((AddNewRoleView) userView).getEmail(),
                    ((AddNewRoleView) userView).getPassword(), ((AddNewRoleView) userView).getRole());
            respMap.put(KeyConstants.KEY_AUTH_TOKEN, loginResponseView.getAuthToken());
            respMap.put(KeyConstants.KEY_USER_ID, loginResponseView.getUserId());
            respMap.put(Constants.USER_SIGN_UP_DATE, loginResponseView.getSignUpDate());
        } else if (userView instanceof AddNewRoleViewWithOtp) {
            LoginResponseView loginResponseView = login(((AddNewRoleViewWithOtp) userView).getEmail(),
                    ((AddNewRoleViewWithOtp) userView).getPassword(), ((AddNewRoleViewWithOtp) userView).getRole());
            respMap.put(KeyConstants.KEY_AUTH_TOKEN, loginResponseView.getAuthToken());
            respMap.put(KeyConstants.KEY_USER_ID, loginResponseView.getUserId());
            respMap.put(Constants.USER_SIGN_UP_DATE, loginResponseView.getSignUpDate());
        }
    }

    /**
     * Method used to add new role to user.
     *
     * @param newRoleView the new role view
     * @return the response model
     * @throws ApplicationException the application exception
     */
    @Transactional
    public ResponseModel addNewRoleToUser(AddNewRoleView newRoleView) {
        log.info("addNewRoleToUser starts.");
        long apiStartTimeMillis = new Date().getTime();

        long profilingStartTimeMillis = new Date().getTime();
        ResponseModel responseModel = new ResponseModel();
        Map<String, Object> respMap = new HashMap<>();
        User user = userRepository.findByEmail(newRoleView.getEmail()); // To check whether the user is already
        UserRole role;

        if (ValidationUtils.isEmptyString(newRoleView.getPassword())) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_SECRET_EMPTY, MessageConstants.ERROR);
        }

        if (ValidationUtils.isEmptyString(newRoleView.getEmail())) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EMAIL_EMPTY, MessageConstants.ERROR);
        }

        // registered
        if (user != null) {
            /*
             * Check whether the entered email & password matches
             */
            validateEmailPassword(newRoleView.getEmail(), newRoleView.getPassword());
            /*
             * Checking whether the user has entered a correct role
             */
            role = userRoleRepository.findByName(newRoleView.getRole());
            if (role == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ROLE_NOT_FOUND,
                        Constants.RESPONSE_INVALID_DATA);
            }
            /*
             * If the new role is already added for the user, throw error
             */
            for (UserRole userRole : AppUtils.getUserRoles(user)) { //AKHIL
                if (userRole.getName().equalsIgnoreCase(newRoleView.getRole())) {
                    throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_USER_ALREADY_EXIST, null);
                }
            }
        } else {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EMAIL_ERROR, MessageConstants.ERROR);
        }
        long profilingEndTimeMillis = new Date().getTime();
        log.info("Validation : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        /*
         * Adding new role to the existing user
         */

        profilingStartTimeMillis = new Date().getTime();
        //start
        List<UserRoleMapping> userRoleMappings = userRoleMappingRepository.findByUser(user);

        UserRoleMapping userRoleMapping = new UserRoleMapping();
        userRoleMapping.setUser(user);
        userRoleMapping.setUserRole(role);
        userRoleMappings.add(userRoleMapping);
        user.setUserRoleMappings(userRoleMappings);
        userRoleMappingRepository.save(userRoleMapping);
        //END

        userRepository.save(user);
        profilingEndTimeMillis = new Date().getTime();
        log.info("Role update : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        /*
         * Creating auth token for the user
         */
        profilingStartTimeMillis = new Date().getTime();
        createAuthToken(newRoleView, respMap);
        profilingEndTimeMillis = new Date().getTime();
        log.info("createAuthToken : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = new Date().getTime();
        fitwiseQboEntityService.createOrUpdateQboUser(user, role.getName());
        profilingEndTimeMillis = new Date().getTime();
        log.info("QBO update : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = new Date().getTime();
        if (newRoleView.getRole().equalsIgnoreCase(SecurityFilterConstants.ROLE_INSTRUCTOR)) {
            String subject = EmailConstants.SIGNUP_INSTRUCTOR_SUBJECT;
            String trainnrStudio = EmailConstants.TRAINNR_STUDIO_LINK.replace(EmailConstants.LITERAL_APP_URL, generalProperties.getInstructorBaseUrl());
            String mailBody = EmailConstants.SIGNUP_INSTRUCTOR_CONTENT;
            String userName = fitwiseUtils.getUserFullName(user);
            mailBody = EmailConstants.BODY_HTML_TEMPLATE_3_PARA_WITH_BUTTON
                    .replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
                    .replace(EmailConstants.EMAIL_BODY, mailBody)
                    //.replace("#MAIL_BODY_2#", EmailConstants.SIGNUP_INSTRUCTOR_CONTENT_2)
                    .replace(EmailConstants.LITERAL_MAIL_BODY_3, EmailConstants.SIGNUP_INSTRUCTOR_CONTENT_3)
                    .replace(EmailConstants.EMAIL_SUPPORT_URL, trainnrStudio);
            mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
            asyncMailer.sendHtmlMail(user.getEmail(), subject, mailBody);
        } else {
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
        profilingEndTimeMillis = new Date().getTime();
        log.info("Mail notification : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = new Date().getTime();
        try {
            if (role.getName().equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR)) {
                messagingService.startConversationByAdmin(user.getUserId(), fitwiseUtils.getWelcomeMessage(SecurityFilterConstants.ROLE_INSTRUCTOR), role.getName());
            } else if (role.getName().equalsIgnoreCase(KeyConstants.KEY_MEMBER)) {
                messagingService.startConversationByAdmin(user.getUserId(), fitwiseUtils.getWelcomeMessage(SecurityFilterConstants.ROLE_MEMBER), role.getName());
            }
        } catch (Exception exception) {
            log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
        }
        profilingEndTimeMillis = new Date().getTime();
        log.info("New message from admin : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        /*
         * Setting the data to response model
         */

        long apiEndTimeMillis = new Date().getTime();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("addNewRoleToUser ends.");

        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_NEW_ROLE_ADDED);
        responseModel.setPayload(respMap);
        return responseModel;
    }

    /**
     * Login.
     *
     * @param userName the user name
     * @param password the password
     * @param role     the role
     * @return the login response view
     * @throws ApplicationException the application exception
     */
    @Transactional
    public LoginResponseView login(final String userName, final String password, final String role) {
        ValidationUtils.validateEmail(userName);
        if (ValidationUtils.isEmptyString(password)) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_SECRET_EMPTY, MessageConstants.ERROR);
        }
        UserDetails userDetails = loadUserByEmail(userName, role);
        /*
         * Check whether the role input from user is valid
         */
        if (!validateRoleFromUser(role)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ROLE_NOT_FOUND, null);
        }
        User user = loadUser(userName);
        if (!BCrypt.checkpw(password, userDetails.getPassword())) {
            throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_USERNAME_SECRET_INCORRECT, MessageConstants.ERROR_INTERNAL_SERVER_FAILURE);
        }
        UserNamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UserNamePasswordAuthenticationToken(
                userDetails, password, null, false);
        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        LoginResponseView loginResponseView = new LoginResponseView();
        if (authentication.isAuthenticated() && authentication.getPrincipal() != null) {
            /*
             * Say, If a user has already been registered under "Member" role with password for Fit-wise,
             * he can use the same login credentials to login to Instructor app also.
             * But the user should confirm for adding new role as "Instructor" in the prompt
             * shown to him in Client apps.
             *
             * setNewRolePrompt - boolean to take care of the above scenario!
             */
            boolean isNewRole = true;
            for (UserRole userRole : AppUtils.getUserRoles(user)) {
                if (userRole.getName().equalsIgnoreCase(role)) {
                    isNewRole = false;
                    break;
                }
            }
            /*
             * If user has entered password for Fitwise,
             * return it in the response which will be used by the Client
             * to show appropriate pop-up to validate the user!
             */
            if (user.isEnteredFitwisePassword()) {
                loginResponseView.setHasFitwisePassword(true);
            }
            loginResponseView.setUserId(user.getUserId());
            loginResponseView.setSignUpDate(user.getCreatedDate());
            if (isNewRole) {
                /*
                 * This auth token will act temporarily for the user.
                 * Once the new role is added, new auth token should be updated!
                 */
                loginResponseView.setAuthToken(authentication.getName());
                loginResponseView.setNewRolePrompt(true);
            } else {
                loginResponseView.setAuthToken(authentication.getName());
                loginResponseView.setNewRolePrompt(false);
            }
        } else {
            throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_USERNAME_SECRET_INCORRECT, null);
        }
        return loginResponseView;
    }

    /**
     * Method to check whether the user input role exists in DB.
     *
     * @param userRole - Input string from user
     * @return - Returns a boolean value
     */
    private boolean validateRoleFromUser(String userRole) {
        boolean isValidRole = true;
        UserRole role = userRoleRepository.findByName(userRole);
        if (role == null) {
            isValidRole = false;
        }
        return isValidRole;
    }

    /**
     * Method to add instructor experience in programs.
     *
     * @param experienceUserView the experience user view
     * @return the response model
     * @throws ApplicationException the application exception
     */
    public ResponseModel updateInstructorExperience(ExperienceUserView experienceUserView) {
        log.info("Update instructor experience starts");
        long start = new Date().getTime();
        ResponseModel res = new ResponseModel();
        User user = userComponents.getUser();
        if (experienceUserView.getInstructorExperienceList() == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_NULL_INSTRUCTOR_EXPERIENCE_LIST,
                    MessageConstants.ERROR);
        }
        instructorProgramService.updateExperience(user, experienceUserView.getInstructorExperienceList());
        res.setPayload(null);
        res.setStatus(Constants.SUCCESS_STATUS);
        res.setMessage(MessageConstants.MSG_USER_EXPERIENCE_UPDATED);
        log.info("Update instructor experience : Total time take in millis : "+(new Date().getTime() - start));
        log.info("Update instructor experience ends");
        return res;
    }

    /**
     * Method to update user tax Id.
     *
     * @param taxIdView the tax id view
     * @return the response model
     * @throws ApplicationException the application exception
     */
    public ResponseModel addTaxId(TaxIdView taxIdView) {
        log.info("Update Tax Id starts");
        long start = new Date().getTime();
        long profilingStart;
        long profilingEnd;
        ResponseModel res = new ResponseModel();
        User user = userComponents.getUser();
        profilingStart = new Date().getTime();
        TaxId userTaxId = taxRepository.findByUserUserId(user.getUserId());
        if (userTaxId != null) {
            throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_ERR_TAX_ID_EXIST, MessageConstants.ERROR);
        }
        TaxTypes taxTypes = taxTypesRepository.findByTaxTypeId(taxIdView.getTaxTypeId());
        if (taxTypes == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_WRONG_TAX_TYPE_ID, MessageConstants.ERROR);
        }
        if (ValidationUtils.isEmptyString(taxIdView.getTaxNumber())) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_TAX_NUMBER_NULL, MessageConstants.ERROR);
        }
        String encryptedtaxNo = null;
        try{
            encryptedtaxNo = aesEncryption.encrypt(taxIdView.getTaxNumber());
        }catch (Exception e){
            log.info("Encryption failed");
        }
        TaxId duplicateTaxId = taxRepository.findByTaxNumberAndTaxTypesTaxTypeId(encryptedtaxNo, taxIdView.getTaxTypeId());
        if (duplicateTaxId != null) {
            throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_TAX_NUMBER_ALREADY_EXIST, MessageConstants.ERROR);
        }
        profilingEnd = new Date().getTime();
        log.info("Tax Id validations : Time taken in millis : " + (profilingEnd - profilingStart));
        userTaxId = new TaxId();
        userTaxId.setTaxTypes(taxTypes);
        userTaxId.setTaxNumber(encryptedtaxNo);
        userTaxId.setUser(user);
        taxRepository.save(userTaxId);
        profilingStart = new Date().getTime();
        fitwiseQboEntityService.createOrUpdateQboUser(user, SecurityFilterConstants.ROLE_INSTRUCTOR);
        profilingEnd = new Date().getTime();
        log.info("Create or update qbo user : Time taken in millis : " + (profilingEnd - profilingStart));
        res.setPayload(userTaxId);
        res.setStatus(Constants.SUCCESS_STATUS);
        res.setMessage(MessageConstants.MSG_TAX_ID_SAVED);
        //Response construction
        TaxResponseView taxResponseView = new TaxResponseView();
        taxResponseView.setId(userTaxId.getId());
        String taxNo = null;
        try{
            taxNo = aesEncryption.decrypt(userTaxId.getTaxNumber());
        }catch (Exception e){
            log.info("Encryption failed");
        }
        taxResponseView.setTaxNumber(taxNo);
        res.setPayload(taxResponseView);
        res.setStatus(Constants.SUCCESS_STATUS);
        res.setMessage(MessageConstants.MSG_TAX_ID_SAVED);
        profilingEnd = new Date().getTime();
        log.info("Update Tax Id : Total  Time taken in millis : " + (profilingEnd - start));
        log.info("Update tax Id ends");
        return res;
    }


    /**
     * Delete the tax number for the logged in user
     *
     * @return
     */
    @Transactional
    public ResponseModel deleteTaxId() {
        ResponseModel res = new ResponseModel();
        User user = userComponents.getUser();
        TaxId userTaxId = taxRepository.findByUserUserId(user.getUserId());
        if (userTaxId == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERR_TAX_ID_NOT_EXIST, MessageConstants.ERROR);
        }
        taxRepository.delete(userTaxId);
        fitwiseQboEntityService.createOrUpdateQboUser(user, SecurityFilterConstants.ROLE_INSTRUCTOR);
        res.setPayload(null);
        res.setStatus(Constants.SUCCESS_STATUS);
        res.setMessage(MessageConstants.MSG_SUCCESS_TAX_ID_DELETED);
        return res;
    }

    /**
     * Method to Add new certification.
     *
     * @param certificationCreationView the certification creation view
     * @return the response model
     * @throws ApplicationException the application exception
     */
    public ResponseModel createCertification(CertificationCreationView certificationCreationView) {
        log.info("Create certification starts");
        long start = new Date().getTime();
        ResponseModel res = new ResponseModel();
        ValidationUtils.validateDOB(certificationCreationView.getIssuedDate());

        if (certificationCreationView.getCertificateImageId() == null || certificationCreationView.getCertificateImageId() == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ERR_CERTIFICATE_IMAGE_MISSING,
                    MessageConstants.ERROR);
        }
        User user = userComponents.getUser();
        InstructorCertification instructorCertification = new InstructorCertification();
        validationService.validateInstructorId(user.getUserId());

        if (!ValidationUtils.isEmptyString(certificationCreationView.getAcademyName())) {
            instructorCertification.setAcademyName(certificationCreationView.getAcademyName());
        } else {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ACADEMY_NAME_NULL,
                    MessageConstants.ERROR);
        }
        if (!ValidationUtils.isEmptyString(certificationCreationView.getCertificateTitle())) {
            instructorCertification.setCertificateTitle(certificationCreationView.getCertificateTitle());
        } else {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CERTIFICATE_TITLE_NULL,
                    MessageConstants.ERROR);
        }

        if (certificationCreationView.getIssuedDate() != null) {

            ValidationUtils.validateDate(certificationCreationView.getIssuedDate());
            instructorCertification.setIssuedDate(certificationCreationView.getIssuedDate());

        } else {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ISSUED_DATE_NULL,
                    MessageConstants.ERROR);
        }
        if (certificationCreationView.getCertificateImageId() != null) {
            Images images = imageRepository.findByImageId(certificationCreationView.getCertificateImageId());
            if (images != null) {
                instructorCertification.setCertificateImage(images);
            } else {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_IMAGE_NOT_FOUND,
                        MessageConstants.ERROR);
            }

        } else {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ERR_CERTIFICATE_IMAGE_MISSING,
                    MessageConstants.ERROR);
        }

        instructorCertification.setUser(user);
        certificateRepository.save(instructorCertification);
        res.setPayload(instructorCertification);
        res.setStatus(Constants.SUCCESS_STATUS);
        res.setMessage(MessageConstants.MSG_USER_CERTIFICATION_SAVED);
        long end = new Date().getTime();
        log.info("Create certtification time taken in millis : "+(end-start));
        log.info("create certification ends");
        return res;
    }


    /**
     * Method to update instructor certification.
     *
     * @param certificationRequestView the certification request view
     * @return the response model
     * @throws ApplicationException the application exception
     */
    public ResponseModel updateCertification(CertificationRequestView certificationRequestView) {
        ResponseModel res = new ResponseModel();
        ValidationUtils.validateDOB(certificationRequestView.getIssuedDate());
        User user = userComponents.getUser();
        if (certificationRequestView.getInstructorCertificateId() == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CERTIFICATE_ID_NULL,
                    MessageConstants.ERROR);
        }
        InstructorCertification instructorCertification = certificateRepository.findByUserUserIdAndInstructorCertificateId(user.getUserId(), certificationRequestView.getInstructorCertificateId());

        if (instructorCertification == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CERTIFICATE_NOT_FOUND,
                    MessageConstants.ERROR);
        }
        if (!ValidationUtils.isEmptyString(certificationRequestView.getAcademyName())) {
            instructorCertification.setAcademyName(certificationRequestView.getAcademyName());
        } else {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ACADEMY_NAME_NULL,
                    MessageConstants.ERROR);
        }

        if (!ValidationUtils.isEmptyString(certificationRequestView.getCertificateTitle())) {
            instructorCertification.setCertificateTitle(certificationRequestView.getCertificateTitle());
        } else {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CERTIFICATE_TITLE_NULL,
                    MessageConstants.ERROR);
        }

        if (certificationRequestView.getIssuedDate() != null) {
            ValidationUtils.validateDate(certificationRequestView.getIssuedDate());
            instructorCertification.setIssuedDate(certificationRequestView.getIssuedDate());

        } else {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ISSUED_DATE_NULL,
                    MessageConstants.ERROR);
        }


        if (certificationRequestView.getCertificateImageId() != null) {

            Images images = imageRepository.findByImageId(certificationRequestView.getCertificateImageId());
            if (images != null) {
                instructorCertification.setCertificateImage(images);
            } else {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_IMAGE_NOT_FOUND,
                        MessageConstants.ERROR);
            }

        } else {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ERR_CERTIFICATE_IMAGE_MISSING,
                    MessageConstants.ERROR);
        }

        certificateRepository.save(instructorCertification);
        res.setPayload(null);
        res.setStatus(Constants.SUCCESS_STATUS);
        res.setMessage(MessageConstants.MSG_USER_CERTIFICATION_SAVED);
        return res;
    }

    /**
     * Method to delete Certificate.
     *
     * @param certificateDeleteRequestView the certificate delete request view
     * @return the response model
     * @throws ApplicationException the application exception
     */
    public ResponseModel deleteCertification(CertificateDeleteRequestView certificateDeleteRequestView) {
        ResponseModel res = new ResponseModel();
        User user = userComponents.getUser();
        if (certificateDeleteRequestView.getInstructorCertificateId() == null || certificateDeleteRequestView.getInstructorCertificateId() == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CERTIFICATE_ID_NULL,
                    MessageConstants.ERROR);
        }
        InstructorCertification instructorCertification = certificateRepository.findByUserUserIdAndInstructorCertificateId(user.getUserId(), certificateDeleteRequestView.getInstructorCertificateId());
        if (instructorCertification == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CERTIFICATE_NOT_FOUND,
                    MessageConstants.ERROR);
        }
        certificateRepository.delete(instructorCertification);

        res.setPayload(null);
        res.setStatus(Constants.SUCCESS_STATUS);
        res.setMessage(MessageConstants.MSG_USER_CERTIFICATION_DELETED);
        return res;
    }

    /**
     * Method to add new award.
     *
     * @param awardsCreationView the awards creation view
     * @return the response model
     */
    public ResponseModel createAwards(AwardsCreationView awardsCreationView) {
        log.info("Create awards starts.");
        long apiStartTimeMillis = new Date().getTime();
        ResponseModel res = new ResponseModel();
        ValidationUtils.validateDOB(awardsCreationView.getIssuedDate());
        User user = userComponents.getUser();
        if (awardsCreationView.getAwardImageId() == null || awardsCreationView.getAwardImageId() == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ERR_AWARD_IMAGE_MISSING,
                    MessageConstants.ERROR);
        }
        InstructorAwards instructorAwards = new InstructorAwards();
        if (!ValidationUtils.isEmptyString(awardsCreationView.getAwardsTitle())) {
            instructorAwards.setAwardsTitle(awardsCreationView.getAwardsTitle());
        } else {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_AWARD_TITLE_NULL,
                    MessageConstants.ERROR);
        }
        if (!ValidationUtils.isEmptyString(awardsCreationView.getExternalSiteLink())) {
            instructorAwards.setExternalSiteLink(awardsCreationView.getExternalSiteLink());

        }
        if (!ValidationUtils.isEmptyString(awardsCreationView.getOrganizationRecognized())) {
            instructorAwards.setOrganizationRecognized(awardsCreationView.getOrganizationRecognized());
        } else {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ORGANIZATION_NULL,
                    MessageConstants.ERROR);
        }
        if (awardsCreationView.getIssuedDate() != null) {
            ValidationUtils.validateDate(awardsCreationView.getIssuedDate());
            instructorAwards.setIssuedDate(awardsCreationView.getIssuedDate());

        } else {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ISSUED_DATE_NULL,
                    MessageConstants.ERROR);
        }
        log.info(StringConstants.LOG_FIELD_VALIDATION + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();

        if (awardsCreationView.getAwardImageId() != null) {
            Images images = imageRepository.findByImageId(awardsCreationView.getAwardImageId());
            log.info("Query to image : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            if (images != null) {
                instructorAwards.setAwardImage(images);
            } else {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_IMAGE_NOT_FOUND,
                        MessageConstants.ERROR);
            }

        } else {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ERR_AWARD_IMAGE_MISSING,
                    MessageConstants.ERROR);
        }
        instructorAwards.setUser(user);
        awardsRepository.save(instructorAwards);
        log.info("Query to save awards : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        res.setPayload(instructorAwards);
        res.setStatus(Constants.SUCCESS_STATUS);
        res.setMessage(MessageConstants.MSG_USER_AWARDS_SAVED);
        log.info("Response construction : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Create awards ends.");
        return res;
    }

    /**
     * Method to update awards.
     *
     * @param awardsRequestView the awards request view
     * @return the response model
     * @throws ApplicationException the application exception
     */
    public ResponseModel updateAwards(AwardsRequestView awardsRequestView) {
        ResponseModel res = new ResponseModel();
        ValidationUtils.validateDOB(awardsRequestView.getIssuedDate());
        User user = userComponents.getUser();
        if (awardsRequestView.getAwardsId() == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_AWARD_ID_NULL,
                    MessageConstants.ERROR);
        }
        InstructorAwards instructorAwards = awardsRepository.findByUserUserIdAndAwardsId(user.getUserId(), awardsRequestView.getAwardsId());
        if (!ValidationUtils.isEmptyString(awardsRequestView.getAwardsTitle())) {
            instructorAwards.setAwardsTitle(awardsRequestView.getAwardsTitle());
        } else {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_AWARD_TITLE_NULL,
                    MessageConstants.ERROR);
        }
        if (awardsRequestView.getAwardImageId() != null) {
            Images images = imageRepository.findByImageId(awardsRequestView.getAwardImageId());
            if (images != null) {
                instructorAwards.setAwardImage(images);
            } else {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_IMAGE_NOT_FOUND,
                        MessageConstants.ERROR);
            }
        } else {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ERR_AWARD_IMAGE_MISSING,
                    MessageConstants.ERROR);
        }
        if (!ValidationUtils.isEmptyString(awardsRequestView.getExternalSiteLink())) {
            instructorAwards.setExternalSiteLink(awardsRequestView.getExternalSiteLink());
        } else if (awardsRequestView.getExternalSiteLink() == null || awardsRequestView.getExternalSiteLink().trim().length() == 0) {
            instructorAwards.setExternalSiteLink("");
        }
        if (!ValidationUtils.isEmptyString(awardsRequestView.getOrganizationRecognized())) {
            instructorAwards.setOrganizationRecognized(awardsRequestView.getOrganizationRecognized());
        } else {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ORGANIZATION_NULL,
                    MessageConstants.ERROR);
        }
        if (awardsRequestView.getIssuedDate() != null) {
            ValidationUtils.validateDate(awardsRequestView.getIssuedDate());
            instructorAwards.setIssuedDate(awardsRequestView.getIssuedDate());
        } else {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ISSUED_DATE_NULL,
                    MessageConstants.ERROR);
        }
        awardsRepository.save(instructorAwards);
        res.setPayload(null);
        res.setStatus(Constants.SUCCESS_STATUS);
        res.setMessage(MessageConstants.MSG_USER_AWARDS_SAVED);
        return res;
    }

    /**
     * Method to delete award.
     *
     * @param awardDeleteRequestView the award delete request view
     * @return the response model
     * @throws ApplicationException the application exception
     */
    public ResponseModel deleteAwards(AwardDeleteRequestView awardDeleteRequestView) {
        ResponseModel res = new ResponseModel();
        User user = userComponents.getUser();
        if (awardDeleteRequestView.getAwardsId() == null || awardDeleteRequestView.getAwardsId() == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_AWARD_ID_NULL,
                    MessageConstants.ERROR);
        }
        InstructorAwards instructorAwards = awardsRepository.findByUserUserIdAndAwardsId(user.getUserId(), awardDeleteRequestView.getAwardsId());
        if (instructorAwards == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_AWARD_NOT_FOUND,
                    MessageConstants.ERROR);
        }
        awardsRepository.delete(instructorAwards);
        res.setPayload(null);
        res.setStatus(Constants.SUCCESS_STATUS);
        res.setMessage(MessageConstants.MSG_USER_AWARDS_DELETED);
        return res;
    }


    /**
     * Method to upload profile image.
     *
     * @param imageId the image id
     * @return the response model
     */
    public ResponseModel uploadProfileImage(Long imageId) {
        ResponseModel res = new ResponseModel();
        User user = userComponents.getUser();
        if (imageId == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_IMAGE_ID_NULL,
                    MessageConstants.ERROR);
        }
        Images images = imageRepository.findByImageId(imageId);
        if (images == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_IMAGE_NOT_FOUND,
                    MessageConstants.ERROR);
        }
        UserProfile userProfile = userProfileRepository.findByUser(user);
        userProfile.setProfileImage(images);
        userProfileRepository.save(userProfile);
        res.setPayload(null);
        res.setStatus(Constants.SUCCESS_STATUS);
        res.setMessage(MessageConstants.MSG_IMAGE_SAVED);
        return res;
    }

    /**
     * Method to upload cover image.
     *
     * @param imageId the image id
     * @return the response model
     */
    public ResponseModel uploadCoverImage(Long imageId) {
        ResponseModel res = new ResponseModel();
        User user = userComponents.getUser();
        if (imageId == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_IMAGE_ID_NULL,
                    MessageConstants.ERROR);
        }
        Images images = imageRepository.findByImageId(imageId);
        if (images == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_IMAGE_NOT_FOUND,
                    MessageConstants.ERROR);
        }
        UserProfile userProfile = userProfileRepository.findByUser(user);
        userProfile.setCoverImage(images);
        userProfileRepository.save(userProfile);
        res.setPayload(null);
        res.setStatus(Constants.SUCCESS_STATUS);
        res.setMessage(MessageConstants.MSG_IMAGE_SAVED);
        return res;
    }


    /**
     * Method used to trigger OTP to given email.
     *
     * @param email the email
     * @return the response model
     */
    public ResponseModel generateOtp(String email) {
        ResponseModel res = new ResponseModel();
        ValidationUtils.validateEmail(email);
        UserOtp userOtp = new UserOtp();
        int otp = AppUtils.generateRandonNumber();
        List<UserOtp> userOtps = userOtpRepository.findByEmailOrderByOtpId(email);
        if (!userOtps.isEmpty()) {
            userOtp = userOtps.get(0);
        }
        userOtp.setEmail(email);
        userOtp.setOtp(otp);
        userOtp.setActive(true);
        userOtpRepository.save(userOtp);

        String subject = EmailConstants.OTP_SUBJECT;
        String mailBody = EmailConstants.OTP_CONTENT.replace(EmailConstants.LITERAL_OTP, String.valueOf(otp));
        mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi ,").replace(EmailConstants.EMAIL_BODY, mailBody);
        mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
        asyncMailer.sendHtmlMail(email, subject, mailBody);

        res.setStatus(Constants.CONTENT_NEEDS_TO_BE_VALIDATE);
        res.setMessage(ValidationMessageConstants.MSG_VALIDATE_OTP);
        return res;
    }

    /**
     * Method used to get and save new password and role to the existing user who don't have his fit-wise
     * platform password. This scenario will occur when the user logged-in already using Social login.
     * <p>
     *
     * @param newRoleView the new role view
     * @param newRoleView
     * @return
     * @throws ApplicationException the application exception
     */
    public ResponseModel addNewRoleAndPassword(AddNewRoleViewWithOtp newRoleView) {
        ResponseModel responseModel = new ResponseModel();
        Map<String, Object> respMap = new HashMap<>();
        User user = userRepository.findByEmail(newRoleView.getEmail()); // To check whether the user is already
        if (user == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_FOUND, null);
        }

        // Checking if the user is authenticated using otp
        UserOtp userOtp = userOtpRepository.findFirstByEmailAndOtpOrderByUpdatedOnDesc(newRoleView.getEmail(), newRoleView.getOtp());
        if (userOtp == null || !userOtp.isVerified()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_EMAIL_NOT_VERIFIED, MessageConstants.ERROR);
        }

        UserRole role = userRoleRepository.findByName(newRoleView.getRole());
        if (role == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ROLE_NOT_FOUND,
                    Constants.RESPONSE_INVALID_DATA);
        }

        //Checking whether the password has the required constraints
        validationService.validatePassword(newRoleView.getPassword());

        user.setPassword(newRoleView.getPassword());

        //Getting the list of roles mapped to the user
        List<UserRoleMapping> userRoleMappings = userRoleMappingRepository.findByUser(user);
        boolean doesUserAlreadyRegisteredForTheRole = false;

        for (UserRoleMapping userRoleMapping : userRoleMappings) {
            if (userRoleMapping.getUserRole().getName().equalsIgnoreCase(newRoleView.getRole())) {
                doesUserAlreadyRegisteredForTheRole = true;
                break;
            }
        }

        if (!doesUserAlreadyRegisteredForTheRole) {
            UserRoleMapping userRoleMapping = new UserRoleMapping();
            userRoleMapping.setUser(user);
            userRoleMapping.setUserRole(role);
            userRoleMappings.add(userRoleMapping);
            user.setUserRoleMappings(userRoleMappings);
            userRoleMappingRepository.save(userRoleMapping);
        }

        userRepository.save(user);
        fitwiseQboEntityService.createOrUpdateQboUser(user, role.getName());
        createAuthToken(newRoleView, respMap);

        if (!doesUserAlreadyRegisteredForTheRole && newRoleView.getRole().equalsIgnoreCase(SecurityFilterConstants.ROLE_INSTRUCTOR)) {
            user = userRepository.findByEmail(newRoleView.getEmail());
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
        } else if (!doesUserAlreadyRegisteredForTheRole && newRoleView.getRole().equalsIgnoreCase(SecurityFilterConstants.ROLE_MEMBER)) {
            //Send mail to member to update profile
            user = userRepository.findByEmail(newRoleView.getEmail());
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
        try {
            if (!doesUserAlreadyRegisteredForTheRole) {
                if (role.getName().equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR)) {
                    messagingService.startConversationByAdmin(user.getUserId(), fitwiseUtils.getWelcomeMessage(SecurityFilterConstants.ROLE_INSTRUCTOR), role.getName());
                } else if (role.getName().equalsIgnoreCase(KeyConstants.KEY_MEMBER)) {
                    messagingService.startConversationByAdmin(user.getUserId(), fitwiseUtils.getWelcomeMessage(SecurityFilterConstants.ROLE_MEMBER), role.getName());
                }
            }
        } catch (Exception exception) {
            log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
        }
        /*
         * Setting the data to response model
         */
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_NEW_ROLE_ADDED);
        responseModel.setPayload(respMap);
        return responseModel;
    }

    /**
     * Gets the member.
     *
     * @param pageNo   the page size
     * @param pageSize the page count
     * @return the members
     * @throws ApplicationException the application exception
     * @Param sortOrder for asc or dsc
     * @Param sortBy the memers
     * @Param searchName for searching single member
     */

    public AdminListResponse getMember(final int pageNo, final int pageSize, String sortOrder, String sortBy, Optional<String> searchName, String blockStatus) {
        log.info("Admin member L1 starts.");
        long apiStartTimeMillis = new Date().getTime();

        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ERROR, null);
        }

        List<String> allowedSortByList = Arrays.asList(SearchConstants.MEMBER_NAME, SearchConstants.AMOUNT_SPENT, SearchConstants.TOTAL_SUBSCRIPTION, SearchConstants.COMPLETED_PROGRAM, SearchConstants.STATUS, SecurityFilterConstants.ROLE_INSTRUCTOR, SearchConstants.PACKAGE_SUBSCRIPTION_COUNT);
        boolean isSortByAllowed = allowedSortByList.stream().anyMatch(sortBy::equalsIgnoreCase);

        if (!isSortByAllowed) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_SORT_BY, null);
        }

        if (!(sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC) || sortOrder.equalsIgnoreCase(SearchConstants.ORDER_ASC))) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ORDER_ERROR, null);
        }

        if (blockStatus == null || blockStatus.isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_BLOCK_STATUS_PARAM_NULL, null);
        }
        if (!(blockStatus.equalsIgnoreCase(KeyConstants.KEY_ALL) || blockStatus.equalsIgnoreCase(KeyConstants.KEY_OPEN) || blockStatus.equalsIgnoreCase(KeyConstants.KEY_BLOCKED))) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_BLOCK_STATUS_PARAM_INCORRECT, null);
        }
        List<MemberResponse> memberResponses = null;
        AdminListResponse adminListResponse = new AdminListResponse();
        if (sortBy.equalsIgnoreCase(SearchConstants.MEMBER_NAME)) {
            long profilingStartTimeMillis = new Date().getTime();

            UserRole userRole = userRoleRepository.findByName(SecurityFilterConstants.ROLE_MEMBER);
            if (userRole == null) {
                throw new ApplicationException(Constants.NOT_FOUND, MessageConstants.ERROR, null);
            }

            Specification<UserProfile> roleSpec = UserProfileSpecifications.getUserProfilesByRoleId(userRole.getRoleId());
            Specification<UserProfile> finalSpec = roleSpec;

            //Search criteria
            if (searchName.isPresent() && !searchName.get().isEmpty()) {
                Specification<UserProfile> searchSpec = UserProfileSpecifications.getUserProfileByName(searchName.get());
                finalSpec = finalSpec.and(searchSpec);
            }

            //Block criteria
            List<BlockedUser> blockedMembers = blockedUserRepository.findByUserRoleName(SecurityFilterConstants.ROLE_MEMBER);
            List<Long> blockedUserIdList = blockedMembers.stream().map(user -> user.getUser().getUserId()).collect(Collectors.toList());
            Specification<UserProfile> blockSpec = null;
            if (!KeyConstants.KEY_ALL.equalsIgnoreCase(blockStatus)) {
                if (KeyConstants.KEY_BLOCKED.equalsIgnoreCase(blockStatus)) {
                    if (!blockedUserIdList.isEmpty()) {
                        blockSpec = UserProfileSpecifications.getUserProfilesInUserIdList(blockedUserIdList);
                    } else {
                        //when no instructor is blocked, return empty list when blocked users are requested
                        return new AdminListResponse();
                    }
                } else if (KeyConstants.KEY_OPEN.equalsIgnoreCase(blockStatus) && !blockedUserIdList.isEmpty()) {
                    blockSpec = UserProfileSpecifications.getUserProfilesNotInUserIdList(blockedUserIdList);
                }
                if (blockSpec != null) {
                    finalSpec = finalSpec.and(blockSpec);
                }
            }
            Specification<UserProfile> orderSpec = UserProfileSpecifications.getUserProfilesOrderByName(sortOrder);
            Specification<UserProfile> finalOrderSpec = finalSpec.and(orderSpec);

            PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize);
            Page<UserProfile> userProfilePage = userProfileRepository.findAll(finalOrderSpec, pageRequest);

            long profilingEndTimeMillis = new Date().getTime();
            log.info("Query : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

            if (userProfilePage.isEmpty()) {
                return new AdminListResponse();
            }

            profilingStartTimeMillis = new Date().getTime();
            memberResponses = getMemberResponse(userProfilePage.getContent());

            adminListResponse.setPayloadOfAdmin(memberResponses);
            adminListResponse.setTotalSizeOfList(userProfilePage.getTotalElements());
            profilingEndTimeMillis = new Date().getTime();
            log.info("Response construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        } else {

            int fromIndex = (pageNo - 1) * pageSize;
            List<UserProfile> userProfilesList = null;

            long profilingStartTimeMillis = new Date().getTime();

            if (searchName.isPresent() && !searchName.get().isEmpty()) {
                if (searchName.get().isEmpty()) {
                    return new AdminListResponse();
                }
                if (searchName.get().split(" ").length > 0) {
                    userProfilesList = (userProfileRepository.findByFirstNameAndLastName(searchName.get(), SecurityFilterConstants.ROLE_MEMBER));
                } else {
                    userProfilesList = (userProfileRepository.findByName(searchName.get(), SecurityFilterConstants.ROLE_MEMBER));
                }
            } else {
                userProfilesList = getUserProfiles(SecurityFilterConstants.ROLE_MEMBER);
            }
            long profilingEndTimeMillis = new Date().getTime();
            log.info("Query : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

            profilingStartTimeMillis = new Date().getTime();
            memberResponses = getMemberResponse(userProfilesList);
            profilingEndTimeMillis = new Date().getTime();
            log.info("Response construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

            profilingStartTimeMillis = new Date().getTime();
            if (blockStatus.equalsIgnoreCase(KeyConstants.KEY_BLOCKED)) {
                memberResponses = memberResponses.stream().filter(MemberResponse::isBlocked).collect(Collectors.toList());
            } else if (blockStatus.equalsIgnoreCase(KeyConstants.KEY_OPEN)) {
                memberResponses = memberResponses.stream().filter(response -> !response.isBlocked()).collect(Collectors.toList());
            }
            profilingEndTimeMillis = new Date().getTime();
            log.info("Block status filtering : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

            if (memberResponses == null || memberResponses.size() < fromIndex) {
                return new AdminListResponse();
            }

            profilingStartTimeMillis = new Date().getTime();
            List<MemberResponse> memberResponses1 = compareMember(memberResponses, sortBy);
            if (sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC)) {
                Collections.reverse(memberResponses1);
            }
            profilingEndTimeMillis = new Date().getTime();
            log.info("Sorting : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

            profilingStartTimeMillis = new Date().getTime();
            adminListResponse.setTotalSizeOfList(memberResponses1.size());
            adminListResponse.setPayloadOfAdmin(memberResponses1.subList(fromIndex, Math.min(fromIndex + pageSize, memberResponses.size())));
            profilingEndTimeMillis = new Date().getTime();
            log.info("Pagination sublist : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        }

        long apiEndTimeMillis = new Date().getTime();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("Admin member L1 ends.");

        return adminListResponse;
    }

    private List<MemberResponse> getMemberResponse(List<UserProfile> userProfilesList) {
        if (userProfilesList.isEmpty()) {
            return Collections.emptyList();
        }

        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);

        List<MemberResponse> memberResponses = new ArrayList<>();
        for (UserProfile userProfile : userProfilesList) {
            try {
                MemberResponse memberResponse = new MemberResponse();
                memberResponse.setUserId(userProfile.getUser().getUserId());
                memberResponse.setMemberName(userProfile.getFirstName() + " " + userProfile.getLastName());
                try {
                    memberResponse.setImageUrl(userProfile.getProfileImage().getImagePath());
                } catch (Exception exception) {
                    memberResponse.setImageUrl(null);
                }
                memberResponse.setTotalSubscription(subscriptionService.getPaidProgramSubscriptionsByAnUser(userProfile.getUser().getUserId()).size());

                int completedProgramCount = adminService.getCompletedProgramCountByMember(userProfile.getUser().getUserId());
                memberResponse.setCompletedProgram(completedProgramCount);

                List<String> subscriptionTypeList = Arrays.asList(KeyConstants.KEY_PROGRAM, KeyConstants.KEY_SUBSCRIPTION_PACKAGE);
                double amountSpentByMember = adminService.getAmountSpentByMember(userProfile.getUser(), subscriptionTypeList);
                memberResponse.setAmountSpent(amountSpentByMember);
                memberResponse.setAmountSpentFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(amountSpentByMember));

                UserRole userRole = userRoleRepository.findByName(KeyConstants.KEY_MEMBER);
                boolean isActive = fitwiseUtils.isUserActive(userProfile.getUser(), userRole);
                String status = Boolean.toString(isActive);
                memberResponse.setStatus(status);

                //user blocked status field in addition to user active status
                boolean isUserBlocked = blockedUserRepository.existsByUserUserIdAndUserRoleName(userProfile.getUser().getUserId(), KeyConstants.KEY_MEMBER);
                memberResponse.setBlocked(isUserBlocked);

                subscriptionTypeList = Arrays.asList(KeyConstants.KEY_PROGRAM);
                double amountSpentOnProgram = adminService.getAmountSpentByMember(userProfile.getUser(), subscriptionTypeList);
                memberResponse.setAmountSpentOnProgram(amountSpentOnProgram);
                memberResponse.setAmountSpentOnProgramFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(amountSpentOnProgram));

                subscriptionTypeList = Arrays.asList(KeyConstants.KEY_SUBSCRIPTION_PACKAGE);
                double amountSpentOnPackage = adminService.getAmountSpentByMember(userProfile.getUser(), subscriptionTypeList);
                memberResponse.setAmountSpentOnPackage(amountSpentOnPackage);
                memberResponse.setAmountSpentOnPackageFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(amountSpentOnPackage));

                memberResponse.setPackageSubscriptionCount(subscriptionService.getPaidPackageSubscriptionsByAnUser(userProfile.getUser().getUserId()).size());

                memberResponses.add(memberResponse);
            } catch (Exception e) {
                log.info(MessageConstants.MSG_ERR_EXCEPTION + e.getMessage());
            }
        }
        return memberResponses;
    }

    private List<MemberResponse> compareMember(List<MemberResponse> memberResponses, String sortBy){
        if (sortBy.equalsIgnoreCase(SearchConstants.MEMBER_NAME) || sortBy.equalsIgnoreCase(SecurityFilterConstants.ROLE_MEMBER)) {
            return memberResponses.stream().sorted(Comparator.comparing(MemberResponse::getMemberName, String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList());
        } else if (sortBy.equalsIgnoreCase(SearchConstants.AMOUNT_SPENT)) {
            return memberResponses.stream().sorted(Comparator.comparingDouble(MemberResponse::getAmountSpent).thenComparing(MemberResponse::getMemberName)).collect(Collectors.toList());
        } else if (sortBy.equalsIgnoreCase(SearchConstants.TOTAL_SUBSCRIPTION)) {
            return memberResponses.stream().sorted(Comparator.comparingLong(MemberResponse::getTotalSubscription).thenComparing(MemberResponse::getMemberName)).collect(Collectors.toList());
        } else if (sortBy.equalsIgnoreCase(SearchConstants.COMPLETED_PROGRAM)) {
            return memberResponses.stream().sorted(Comparator.comparingLong(MemberResponse::getCompletedProgram).thenComparing(MemberResponse::getMemberName)).collect(Collectors.toList());
        } else if (sortBy.equalsIgnoreCase(SearchConstants.STATUS)) {
            return memberResponses.stream().sorted(Comparator.comparing(MemberResponse::getStatus).thenComparing(MemberResponse::getMemberName)).collect(Collectors.toList());
        } else if (sortBy.equalsIgnoreCase(SearchConstants.PACKAGE_SUBSCRIPTION_COUNT)) {
            return memberResponses.stream().sorted(Comparator.comparing(MemberResponse::getPackageSubscriptionCount).thenComparing(MemberResponse::getMemberName)).collect(Collectors.toList());
        }
        return memberResponses;
    }

    private List<UserProfile> getUserProfiles(String userRoleName) {
        UserRole userRole = userRoleRepository.findByName(userRoleName);
        if (userRole == null) {
            throw new ApplicationException(Constants.NOT_FOUND, MessageConstants.ERROR, null);
        }
        List<User> users = userRepository.findByUserRoleMappingsUserRole(userRole);
        return userProfileRepository.findByUserIn(users);
    }

    /**
     * Gets the instructor.
     *
     * @param pageNo   the page size
     * @param pageSize the page count
     * @return the instructors
     * @throws ApplicationException the application exception
     * @Param sortOrder for asc or dsc
     * @Param sortBy the instructors
     * @Param searchName for searching single instructor
     */
    public AdminListResponse getInstructor(final int pageNo, final int pageSize, String sortOrder, String sortBy, Optional<String> searchName, String blockStatus){
        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ERROR, null);
        }

        log.info("Admin instructor L1 starts.");
        long apiStartTimeMillis = new Date().getTime();
        String[] allowedSorts = {SearchConstants.INSTRUCTOR_NAME, SearchConstants.UPCOMING_PAYMENT, SearchConstants.TOTAL_SUBSCRIPTION, SearchConstants.PUBLISHED_PROGRAM, SearchConstants.STATUS, SearchConstants.ONBOARDED_DATE, SearchConstants.TOTAL_EXERCISES, SearchConstants.PACKAGE_SUBSCRIPTION_COUNT, SearchConstants.PUBLISHED_PACKAGE_COUNT};
        List<String> allowedSortByList = Arrays.asList(allowedSorts);
        boolean isSortByAllowed = allowedSortByList.stream().anyMatch(sortBy::equalsIgnoreCase);
        if (!isSortByAllowed) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_SORT_BY, null);
        }
        if (!(sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC) || sortOrder.equalsIgnoreCase(SearchConstants.ORDER_ASC))) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ORDER_ERROR, null);
        }
        if (blockStatus == null || blockStatus.isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_BLOCK_STATUS_PARAM_NULL, null);
        }
        if (!(blockStatus.equalsIgnoreCase(KeyConstants.KEY_ALL) || blockStatus.equalsIgnoreCase(KeyConstants.KEY_OPEN) || blockStatus.equalsIgnoreCase(KeyConstants.KEY_BLOCKED))) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_BLOCK_STATUS_PARAM_INCORRECT, null);
        }
        List<InstructorResponse> instructorResponses;
        AdminListResponse adminListResponse = new AdminListResponse();
        if (sortBy.equalsIgnoreCase(SearchConstants.INSTRUCTOR_NAME)) {
            long profilingStartTimeMillis = new Date().getTime();

            UserRole userRole = userRoleRepository.findByName(SecurityFilterConstants.ROLE_INSTRUCTOR);
            if (userRole == null) {
                throw new ApplicationException(Constants.NOT_FOUND, MessageConstants.ERROR, null);
            }

            Specification<UserProfile> roleSpec = UserProfileSpecifications.getUserProfilesByRoleId(userRole.getRoleId());
            Specification<UserProfile> finalSpec = roleSpec;

            //Search criteria
            if (searchName.isPresent() && !searchName.get().isEmpty()) {
                Specification<UserProfile> searchSpec = UserProfileSpecifications.getUserProfileByName(searchName.get());
                finalSpec = finalSpec.and(searchSpec);
            }

            //Block criteria
            List<BlockedUser> blockedInstructors = blockedUserRepository.findByUserRoleName(SecurityFilterConstants.ROLE_INSTRUCTOR);
            List<Long> blockedUserIdList = blockedInstructors.stream().map(user -> user.getUser().getUserId()).collect(Collectors.toList());
            Specification<UserProfile> blockSpec = null;
            if (!KeyConstants.KEY_ALL.equalsIgnoreCase(blockStatus)) {
                if (KeyConstants.KEY_BLOCKED.equalsIgnoreCase(blockStatus)) {
                    if (!blockedUserIdList.isEmpty()) {
                        blockSpec = UserProfileSpecifications.getUserProfilesInUserIdList(blockedUserIdList);
                    } else {
                        //when no instructor is blocked, return empty list when blocked users are requested
                        return new AdminListResponse();
                    }
                } else if (KeyConstants.KEY_OPEN.equalsIgnoreCase(blockStatus) && !blockedUserIdList.isEmpty()) {
                    blockSpec = UserProfileSpecifications.getUserProfilesNotInUserIdList(blockedUserIdList);

                }
                if (blockSpec != null) {
                    finalSpec = finalSpec.and(blockSpec);
                }
            }

            Specification<UserProfile> orderSpec = UserProfileSpecifications.getUserProfilesOrderByName(sortOrder);
            Specification<UserProfile> finalOrderSpec = finalSpec.and(orderSpec);

            PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize);
            Page<UserProfile> userProfilePage = userProfileRepository.findAll(finalOrderSpec, pageRequest);

            long profilingEndTimeMillis = new Date().getTime();
            log.info("Query : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

            if (userProfilePage.isEmpty()) {
                return new AdminListResponse();
            }

            profilingStartTimeMillis = new Date().getTime();

            instructorResponses = getInstructorResponse(userProfilePage.getContent());

            adminListResponse.setPayloadOfAdmin(instructorResponses);
            adminListResponse.setTotalSizeOfList(userProfilePage.getTotalElements());
            profilingEndTimeMillis = new Date().getTime();
            log.info("Response construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        }else{
            int fromIndex = (pageNo - 1) * pageSize;
            List<UserProfile> userProfilesPage = null;
            long profilingStartTimeMillis = new Date().getTime();

            if (searchName.isPresent() && !searchName.get().isEmpty()) {
                if (searchName.get().split(" ").length > 0) {
                    userProfilesPage = (userProfileRepository.findByFirstNameAndLastName(searchName.get(), SecurityFilterConstants.ROLE_INSTRUCTOR));
                } else {
                    userProfilesPage = (userProfileRepository.findByName(searchName.get(), SecurityFilterConstants.ROLE_INSTRUCTOR));
                }
            } else {
                userProfilesPage = getUserProfiles(SecurityFilterConstants.ROLE_INSTRUCTOR);
            }
            long profilingEndTimeMillis = new Date().getTime();
            log.info("Query : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
            profilingStartTimeMillis = new Date().getTime();
            instructorResponses = getInstructorResponse(userProfilesPage);
            profilingEndTimeMillis = new Date().getTime();
            log.info("Response construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

            profilingStartTimeMillis = new Date().getTime();
            if (blockStatus.equalsIgnoreCase(KeyConstants.KEY_BLOCKED)) {
                instructorResponses = instructorResponses.stream().filter(InstructorResponse::isBlocked).collect(Collectors.toList());
            } else if (blockStatus.equalsIgnoreCase(KeyConstants.KEY_OPEN)) {
                instructorResponses = instructorResponses.stream().filter(response -> !response.isBlocked()).collect(Collectors.toList());
            }
            profilingEndTimeMillis = new Date().getTime();
            log.info("Block status filtering : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

            if (instructorResponses == null || instructorResponses.size() < fromIndex) {
                return new AdminListResponse();
            }
            profilingStartTimeMillis = new Date().getTime();
            List<InstructorResponse> instructorResponses1 = compareInstructor(instructorResponses, sortBy);
            if (sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC)) {
                Collections.reverse(instructorResponses1);
            }
            profilingEndTimeMillis = new Date().getTime();
            log.info("Sorting : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
            profilingStartTimeMillis = new Date().getTime();
            adminListResponse.setTotalSizeOfList(instructorResponses1.size());
            adminListResponse.setPayloadOfAdmin(instructorResponses1.subList(fromIndex, Math.min(fromIndex + pageSize, instructorResponses.size())));
            profilingEndTimeMillis = new Date().getTime();
            log.info("Pagination sublist : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        }
        long apiEndTimeMillis = new Date().getTime();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("Admin instructor L1 ends.");
        return adminListResponse;
    }

    private List<InstructorResponse> getInstructorResponse(List<UserProfile> userProfilesPage)  {
        if (userProfilesPage.isEmpty()) {
            return Collections.emptyList();
        }
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        List<InstructorResponse> instructorResponses = new ArrayList<>();
        for (UserProfile userProfile : userProfilesPage) {
            try {
                InstructorResponse instructorResponse = new InstructorResponse();
                if (userProfile.getUser() != null && userProfile.getUser().getUserId() != null)
                    instructorResponse.setUserId(userProfile.getUser().getUserId());
                instructorResponse.setInstructorName(userProfile.getFirstName() + " " + userProfile.getLastName());
                try {
                    instructorResponse.setImageUrl(userProfile.getProfileImage().getImagePath());
                } catch (Exception exception) {
                    instructorResponse.setImageUrl(null);
                }
                instructorResponse.setTotalSubscription(subscriptionService.getActiveSubscripionCountOfAnInstructor(userProfile.getUser().getUserId()));
                instructorResponse.setPublishedProgram(programRepository.countByOwnerUserIdAndStatus(userProfile.getUser().getUserId(), InstructorConstant.PUBLISH));

                UserRole userRole = userRoleRepository.findByName(KeyConstants.KEY_INSTRUCTOR);
                boolean isActive = fitwiseUtils.isUserActive(userProfile.getUser(), userRole);
                String status = Boolean.toString(isActive);
                instructorResponse.setStatus(status);

                UserRoleMapping userRoleMapping = userRoleMappingRepository.findTop1ByUserUserIdAndUserRoleName(userProfile.getUser().getUserId(), KeyConstants.KEY_INSTRUCTOR);
                instructorResponse.setOnboardedDate(userRoleMapping.getCreatedDate());
                instructorResponse.setOnboardedDateFormatted(fitwiseUtils.formatDate(userRoleMapping.getCreatedDate()));

                //Total Upcoming payment
                double upcomingPayment = instructorAnalyticsService.calculateOutstandingPaymentOfAnInstructor(userProfile.getUser().getUserId(), null);
                instructorResponse.setUpcomingPayment(upcomingPayment);
                instructorResponse.setUpcomingPaymentFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(upcomingPayment));

                //Upcoming payment for Program subscriptions
                instructorResponse.setProgramOutstandingPayment(upcomingPayment);
                instructorResponse.setProgramOutstandingPaymentFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(upcomingPayment));

                //user blocked status field in addition to user active status
                boolean isUserBlocked = blockedUserRepository.existsByUserUserIdAndUserRoleName(userProfile.getUser().getUserId(), KeyConstants.KEY_INSTRUCTOR);
                instructorResponse.setBlocked(isUserBlocked);
                instructorResponse.setTotalExercises(exerciseRepository.countByOwnerUserId(userProfile.getUser().getUserId()));

                instructorResponse.setPackageSubscriptionCount(subscriptionService.getPaidSubscribedPackagesOfAnInstructor(userProfile.getUser().getUserId()));
                instructorResponse.setPublishedPackageCount(subscriptionPackageRepository.countByOwnerUserIdAndStatus(userProfile.getUser().getUserId(), InstructorConstant.PUBLISH));

                //Upcoming payment for Package subscriptions
                instructorResponse.setPackageOutstandingPayment(upcomingPayment);
                instructorResponse.setPackageOutstandingPaymentFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(upcomingPayment));

                instructorResponses.add(instructorResponse);
            } catch (Exception exception) {
                //if user profile is not there, we are not adding that user profile.
            }
        }
        return instructorResponses;
    }

    private List<InstructorResponse> compareInstructor(List<InstructorResponse> instructorResponses, String sortBy)  {

        if (sortBy.equalsIgnoreCase(SearchConstants.INSTRUCTOR_NAME) || sortBy.equalsIgnoreCase(SecurityFilterConstants.ROLE_INSTRUCTOR)) {
            return instructorResponses.stream().sorted(Comparator.comparing(InstructorResponse::getInstructorName, String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList());
        } else if (sortBy.equalsIgnoreCase(SearchConstants.UPCOMING_PAYMENT)) {
            return instructorResponses.stream().sorted(Comparator.comparingDouble(InstructorResponse::getUpcomingPayment)).collect(Collectors.toList());
        } else if (sortBy.equalsIgnoreCase(SearchConstants.TOTAL_SUBSCRIPTION)) {
            return instructorResponses.stream().sorted(Comparator.comparingLong(InstructorResponse::getTotalSubscription)).collect(Collectors.toList());
        } else if (sortBy.equalsIgnoreCase(SearchConstants.PUBLISHED_PROGRAM)) {
            return instructorResponses.stream().sorted(Comparator.comparingLong(InstructorResponse::getPublishedProgram)).collect(Collectors.toList());
        } else if (sortBy.equalsIgnoreCase(SearchConstants.STATUS)) {
            return instructorResponses.stream().sorted(Comparator.comparing(InstructorResponse::getStatus)).collect(Collectors.toList());
        } else if (sortBy.equalsIgnoreCase(SearchConstants.ONBOARDED_DATE)) {
            return instructorResponses.stream().sorted(Comparator.comparing(InstructorResponse::getOnboardedDate)).collect(Collectors.toList());
        } else if (sortBy.equalsIgnoreCase(SearchConstants.TOTAL_EXERCISES)) {
            return instructorResponses.stream().sorted(Comparator.comparing(InstructorResponse::getTotalExercises)).collect(Collectors.toList());
        } else if (sortBy.equalsIgnoreCase(SearchConstants.PACKAGE_SUBSCRIPTION_COUNT)) {
            return instructorResponses.stream().sorted(Comparator.comparing(InstructorResponse::getPackageSubscriptionCount)).collect(Collectors.toList());
        } else if (sortBy.equalsIgnoreCase(SearchConstants.PUBLISHED_PACKAGE_COUNT)) {
            return instructorResponses.stream().sorted(Comparator.comparing(InstructorResponse::getPublishedPackageCount)).collect(Collectors.toList());
        }

        return instructorResponses;
    }

    /**
     * Gets the programs.
     *
     * @param pageNo   the page size
     * @param pageSize the page count
     * @return the programs
     * @throws ApplicationException the application exception
     * @Param programTypeId for programType
     * @Param sortOrder for asc or dsc
     * @Param sortBy the programs
     * @Param searchName is for searching for programs
     */

    public AdminListResponse getProgram(final int pageNo, final int pageSize, Optional<Long> programTypeIdOptional, String sortOrder, String sortBy, Optional<String> searchName, String blockStatus) {
        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ERROR, null);
        }
        if (!(sortBy.equalsIgnoreCase(SearchConstants.PROGRAM_NAME) || sortBy.equalsIgnoreCase(SearchConstants.CREATED_DATE) || sortBy.equalsIgnoreCase(SearchConstants.TOTAL_SUBSCRIPTION)
                || sortBy.equalsIgnoreCase(SearchConstants.INSTRUCTOR_NAME) || sortBy.equalsIgnoreCase(SecurityFilterConstants.ROLE_INSTRUCTOR) || sortBy.equalsIgnoreCase(SearchConstants.RATING))) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_SORT_BY, null);
        }
        if (!(sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC) || sortOrder.equalsIgnoreCase(SearchConstants.ORDER_ASC))) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ORDER_ERROR, null);
        }

        if (blockStatus == null || blockStatus.isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_BLOCK_STATUS_PARAM_NULL, null);
        }
        if (!(blockStatus.equalsIgnoreCase(KeyConstants.KEY_ALL) || blockStatus.equalsIgnoreCase(KeyConstants.KEY_OPEN) || blockStatus.equalsIgnoreCase(KeyConstants.KEY_BLOCKED))) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_BLOCK_STATUS_PARAM_INCORRECT, null);
        }

        log.info("Admin program L1 starts.");
        long apiStartTimeMillis = new Date().getTime();

        //Adding program status spec
        List<String> statusList = Arrays.asList(KeyConstants.KEY_PUBLISH, KeyConstants.KEY_BLOCK, DBConstants.BLOCK_EDIT);
        Specification<ViewAdminPrograms> finalSpec = ViewAdminProgramSpecification.getProgramStatusSpecification(statusList);
        if (programTypeIdOptional.isPresent() && programTypeIdOptional.get() != 0){
            Specification<ViewAdminPrograms> programTypeSpec = ViewAdminProgramSpecification.getProgramTypeSpecification(programTypeIdOptional.get());
            finalSpec = finalSpec.and(programTypeSpec);
        }

        Page<ViewAdminPrograms> viewAdminProgramsList;

        //Adding program title spec and instructor name spec
        if (searchName.isPresent() && !searchName.get().isEmpty()){
            Specification<ViewAdminPrograms> programTitleSearchSpec = ViewAdminProgramSpecification.getProgramTitleSpecification(searchName.get());
            Specification<ViewAdminPrograms> instructorNameSearchSpec = ViewAdminProgramSpecification.getInstructorNameSpecification(searchName.get().replaceAll(" ", ""));
            //searchName may be program title or instructor name. so adding both in 'or' criteria
            finalSpec = finalSpec.and(programTitleSearchSpec.or(instructorNameSearchSpec));
        }

        //Adding block spec
        if (blockStatus.equalsIgnoreCase(KeyConstants.KEY_BLOCKED)){
            finalSpec = finalSpec.and(ViewAdminProgramSpecification.getProgramBlockStatus(true));
        } else if (blockStatus.equalsIgnoreCase(KeyConstants.KEY_OPEN)){
            finalSpec = finalSpec.and(ViewAdminProgramSpecification.getProgramBlockStatus(false));
        }

        //Adding sort spec
        finalSpec = finalSpec.and(ViewAdminProgramSpecification.getProgramSortSpecification(sortBy, sortOrder));
        PageRequest pageRequest = PageRequest.of(pageNo-1, pageSize);
        long profilingStartTimeMillis = new Date().getTime();
        log.info("Creating spec and page request : Time taken in millis : " + (profilingStartTimeMillis - apiStartTimeMillis));
        profilingStartTimeMillis = new Date().getTime();
        viewAdminProgramsList = viewAdminProgramsRepository.findAll(finalSpec, pageRequest);

        long profilingEndTimeMillis = new Date().getTime();
        log.info("Query : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        
        // FreePrograms List
		List<FreeAccessProgram> freeAccessProgramList = freeAccessProgramReposity
				.findByFreeProductTypeAndIsActive(DBConstants.FREE_ACCESS_TYPE_ALL, true);
		List<Long> freeProgramIdsList = freeAccessProgramList.stream()
				.map(freeProduct -> freeProduct.getProgram().getProgramId()).collect(Collectors.toList());

        profilingStartTimeMillis = new Date().getTime();
        List<ProgramResponse> programResponses = new ArrayList<>();
        for (ViewAdminPrograms viewAdminPrograms : viewAdminProgramsList){

            ProgramResponse programResponse = new ProgramResponse();
            programResponse.setInstructorName(viewAdminPrograms.getInstructorFirstName() + " " + viewAdminPrograms.getInstructorLastName());
            programResponse.setProgramId(viewAdminPrograms.getProgramId());
            programResponse.setProgramName(viewAdminPrograms.getProgramName());
            programResponse.setSubscriptions(viewAdminPrograms.getActiveSubscriptionCount());
            programResponse.setCreatedDate(viewAdminPrograms.getCreatedDate());
            programResponse.setCreatedDateFormatted(fitwiseUtils.formatDate(viewAdminPrograms.getCreatedDate()));
            programResponse.setModifiedDate(viewAdminPrograms.getModifiedDate());
            programResponse.setModifiedDateFormatted(fitwiseUtils.formatDate(viewAdminPrograms.getModifiedDate()));
            programResponse.setBlocked(viewAdminPrograms.getIsBlocked());
            if (viewAdminPrograms.getRating() != null){
                programResponse.setRating(viewAdminPrograms.getRating().setScale(2, RoundingMode.HALF_UP));
            } else {
                programResponse.setRating(new BigDecimal(0).setScale(2, RoundingMode.HALF_UP));
            }
			if (freeProgramIdsList.contains(viewAdminPrograms.getProgramId())) {
				programResponse.setFreeToAccess(true);
			}
            programResponses.add(programResponse);
        }

        profilingEndTimeMillis = new Date().getTime();
        log.info("Response construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = new Date().getTime();
        AdminListResponse<ProgramResponse> adminListResponse = new AdminListResponse<>();
        adminListResponse.setTotalSizeOfList(viewAdminProgramsRepository.count(finalSpec));
        adminListResponse.setPayloadOfAdmin(programResponses);

        profilingEndTimeMillis = new Date().getTime();
        log.info("Pagination sublist : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        long apiEndTimeMillis = new Date().getTime();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("Admin program L1 ends.");

        return adminListResponse;
    }



    public ResponseModel getProgramTypeCount(String blockStatus) {
        log.info("Get program type count starts");
        long start = new Date().getTime();
        long profilingStart;

        if (blockStatus == null || blockStatus.isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_BLOCK_STATUS_PARAM_NULL, null);
        }
        if (!(blockStatus.equalsIgnoreCase(KeyConstants.KEY_ALL) || blockStatus.equalsIgnoreCase(KeyConstants.KEY_OPEN) || blockStatus.equalsIgnoreCase(KeyConstants.KEY_BLOCKED))) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_BLOCK_STATUS_PARAM_INCORRECT, null);
        }

        List<String> statusList = new ArrayList<>();
        if (blockStatus.equalsIgnoreCase(KeyConstants.KEY_BLOCKED)) {
            statusList.add(KeyConstants.KEY_BLOCK);
            statusList.add(DBConstants.BLOCK_EDIT);
        } else if (blockStatus.equalsIgnoreCase(KeyConstants.KEY_OPEN)) {
            statusList.add(KeyConstants.KEY_PUBLISH);
        } else {
            statusList.add(KeyConstants.KEY_PUBLISH);
            statusList.add(KeyConstants.KEY_BLOCK);
            statusList.add(DBConstants.BLOCK_EDIT);
        }
        log.info("Basic validations and status list : Time taken in millis : "+(new Date().getTime() - start));


        profilingStart = new Date().getTime();
        List<SamplePrograms> sampleProgramsList = sampleProgramsRepository.findByProgramsStatusIn(statusList);
        log.info("Sample program query : Time taken in millis : "+(new Date().getTime() - profilingStart));

        profilingStart = new Date().getTime();
        Map<Long, Integer> sampleProgramTypes = new HashMap<>();
        for (SamplePrograms sampleProgram : sampleProgramsList) {
            if (!sampleProgramTypes.containsKey(sampleProgram.getPrograms().getProgramType().getProgramTypeId())) {
                sampleProgramTypes.put(sampleProgram.getPrograms().getProgramType().getProgramTypeId(), 1);
            } else {
                sampleProgramTypes.put(sampleProgram.getPrograms().getProgramType().getProgramTypeId(), sampleProgramTypes.get(sampleProgram.getPrograms().getProgramType().getProgramTypeId()) + 1);
            }
        }
        log.info("Count of sample programs based on types : Time taken in millis : "+(new Date().getTime() - profilingStart));

        profilingStart = new Date().getTime();
        List<ProgramTypes> programTypes = programTypeRepository.findByOrderByProgramTypeNameAsc();
        log.info("Program Types query : Time taken in millis : "+(new Date().getTime() - profilingStart));

        Map<String, Long> programCountTypes = new LinkedHashMap<>();
        profilingStart = new Date().getTime();
        for (ProgramTypes programType : programTypes) {
            long count = programRepository.countByProgramTypeProgramTypeIdAndStatusIn(programType.getProgramTypeId(), statusList);

            if (sampleProgramTypes.containsKey(programType.getProgramTypeId())) {
                count = count - sampleProgramTypes.get(programType.getProgramTypeId());
            }
            programCountTypes.put(programType.getProgramTypeName(), count);
        }
        long allCount = programRepository.countByStatusIn(statusList) - sampleProgramsList.size();
        programCountTypes.put(KeyConstants.KEY_ALL, allCount);
        log.info("Taking program count for each type : Time taken in millis : "+(new Date().getTime() - profilingStart));
        log.info("Get program type count : total time taken in millis : "+(new Date().getTime() - start));
        log.info("Get program type count ends");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PROGRAM_TYPES_COUNT, programCountTypes);
    }

    /**
     * To remove profile image.
     *
     * @return
     */
    public ResponseModel removeProfileImage() {
        ResponseModel responseModel = new ResponseModel();
        User user = userComponents.getUser();
        UserProfile userProfile = userProfileRepository.findByUserUserId(user.getUserId());
        if (userProfile.getProfileImage() == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_WRONG_PROFILE_IMAGE, MessageConstants.ERROR);

        }
        Images images = imageRepository.findByImageId(userProfile.getProfileImage().getImageId());
        userProfile.setProfileImage(null);
        userProfileRepository.save(userProfile);
        imageRepository.delete(images);

        responseModel.setPayload(null);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_USER_PROFILE_IMAGE_REMOVED);
        return responseModel;

    }

    /**
     * To remove cover image.
     *
     * @return
     */
    public ResponseModel removeCoverImage() {
        ResponseModel responseModel = new ResponseModel();
        User user = userComponents.getUser();
        UserProfile userProfile = userProfileRepository.findByUserUserId(user.getUserId());
        if (userProfile.getCoverImage() == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_WRONG_COVER_IMAGE, MessageConstants.ERROR);

        }
        Images images = imageRepository.findByImageId(userProfile.getCoverImage().getImageId());
        userProfile.setCoverImage(null);
        userProfileRepository.save(userProfile);

        imageRepository.delete(images);

        responseModel.setPayload(null);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_USER_COVER_IMAGE_REMOVED);
        return responseModel;
    }

    /**
     * To reset Password
     *
     * @param resetPasswordRequestView
     * @return ResponseModel
     */
    public ResponseModel resetPassword(ResetPasswordRequestView resetPasswordRequestView) {
        log.info("resetPassword starts.");
        long apiStartTimeMillis = new Date().getTime();

        ResponseModel responseModel = new ResponseModel();

        long profilingStartTimeMillis = new Date().getTime();
        User user = userComponents.getUser();
        String currentRole = userComponents.getRole();

        if (ValidationUtils.isEmptyString(resetPasswordRequestView.getCurrentPassword())) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CURRENT_SECRET_INVALID, MessageConstants.ERROR);

        }

        if (ValidationUtils.isEmptyString(resetPasswordRequestView.getNewPassword())) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_NEW_SECRET_INVALID, MessageConstants.ERROR);

        }

        /*
        check whether current password is correct
         */
        UserDetails userDetails = loadUserByUsername(user.getEmail());
        UserNamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UserNamePasswordAuthenticationToken(
                userDetails, resetPasswordRequestView.getCurrentPassword(), null, false);
        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        if (!authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            throwException("Your Current Password is incorrect, please enter the correct password and try again.", Constants.BAD_REQUEST);
        }

        validationService.validatePassword(resetPasswordRequestView.getNewPassword());
        if (resetPasswordRequestView.getCurrentPassword().equals(resetPasswordRequestView.getNewPassword())) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_WRONG_SECRET, MessageConstants.ERROR);

        }
        long profilingEndTimeMillis = new Date().getTime();
        log.info("Validation : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = new Date().getTime();
        user.setPassword(bcryptPasswdEncoder.encode(resetPasswordRequestView.getNewPassword()));
        userRepository.save(user);
        profilingEndTimeMillis = new Date().getTime();
        log.info("Password encryption : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        responseModel.setPayload(null);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_SECRET_CHANGED);

        profilingStartTimeMillis = new Date().getTime();
        String subject = EmailConstants.SECRET_CHANGED_SUBJECT;
        String supportLink = EmailConstants.TRAINNR_SUPPORT_LINK.replace(EmailConstants.EMAIL_EMAIL_ADDRESS, user.getEmail());
        String mailBody = EmailConstants.SECRET_CHANGED_CONTENT;
        String userName = fitwiseUtils.getUserFullName(user);
        mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
                .replace(EmailConstants.EMAIL_BODY, mailBody)
                .replace(EmailConstants.EMAIL_SUPPORT_URL, supportLink);
        if (KeyConstants.KEY_INSTRUCTOR.equalsIgnoreCase(currentRole)) {
            mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
        } else {
            mailBody = emailContentUtil.replaceMemberAppUrl(mailBody);
        }
        asyncMailer.sendHtmlMail(user.getEmail(), subject, mailBody);
        profilingEndTimeMillis = new Date().getTime();
        log.info("Mail : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        long apiEndTimeMillis = new Date().getTime();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("resetPassword ends.");

        return responseModel;
    }

    public UserDetails loadUserByEmail(String email, String roleName) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            log.info("UserName not found");
            throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_USERNAME_SECRET_INCORRECT, null);
        }
        if (roleName.equalsIgnoreCase(SecurityFilterConstants.ROLE_ADMIN)) {
            boolean hasRole = false;
            List<UserRoleMapping> userRoleMappings = userRoleMappingRepository.findByUser(user);
            for (UserRoleMapping userRoleMapping : userRoleMappings) {
                if (userRoleMapping.getUserRole().getName().equalsIgnoreCase(roleName)) {
                    hasRole = true;
                    break;
                }
            }
            if (!hasRole) {
                throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_INVALID_ADMIN_ACCESS, null);
            }
        }
        UserRole role = userRoleRepository.findByName(roleName);

        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
                grantedAuthorities);
    }

    /**
     * method to get all programTypes and expertiseLevels including whether it is selected by user or not
     *
     * @return
     */
    public List<ProgramExpertiseView> getUserProgramExpertiseLevels() {
        User user = userComponents.getUser();

        List<ProgramExpertiseView> programExpertiseViews = new ArrayList<>();

        //user's selected programTypes and expertise levels
        List<UserProgramGoalsMapping> userProgramGoalsMappings = userProgramGoalsMappingRepository.findByUserUserId(user.getUserId());
        List<ProgramExpertiseMapping> programExpertiseMappingList = userProgramGoalsMappings.stream()
                .map(userProgramGoalsMapping -> userProgramGoalsMapping.getProgramExpertiseGoalsMapping().getProgramExpertiseMapping())
                .collect(Collectors.toList());

        //Removing duplicate Id
        Set<Long> programExpertiseMappingIdSet = programExpertiseMappingList.stream().map(ProgramExpertiseMapping::getProgramExpertiseMappingId).collect(Collectors.toSet());
        for (ProgramTypes programTypes : programTypeRepository.findByOrderByProgramTypeNameAsc()) {
            ProgramExpertiseView programExpertiseView = new ProgramExpertiseView();
            List<ExpertiseLevelView> expertiseLevels = new ArrayList<>();
            List<ProgramExpertiseMapping> allExpertiseMappingForProgramType = programExpertiseMappingRepository.findByProgramTypeProgramTypeId(programTypes.getProgramTypeId());
            boolean isProgramTypeSelected = false;
            for (ProgramExpertiseMapping programExpertiseMapping : allExpertiseMappingForProgramType) {
                ExpertiseLevelView expertiseLevel = new ExpertiseLevelView();
                expertiseLevel.setProgramExpertiseMappingId(programExpertiseMapping.getProgramExpertiseMappingId());
                expertiseLevel.setExpertiseLevel(programExpertiseMapping.getExpertiseLevel().getExpertiseLevel());
                //If user has selected the expertise level
                if (programExpertiseMappingIdSet.contains(programExpertiseMapping.getProgramExpertiseMappingId())) {
                    expertiseLevel.setSelected(true);
                    isProgramTypeSelected = true;
                }
                expertiseLevels.add(expertiseLevel);
            }
            programExpertiseView.setProgramType(programTypes.getProgramTypeName());
            programExpertiseView.setProgramTypeSelected(isProgramTypeSelected);
            programExpertiseView.setExpertiseLevels(expertiseLevels);
            programExpertiseViews.add(programExpertiseView);
        }
        return programExpertiseViews;
    }

    /**
     * To get goals based on user selected programType.
     *
     * @return
     */
    public List<MemberGoalsView> getGoals(GoalsRequestView goalsRequestView) {
        User user = userComponents.getUser();

        //User's goals
        List<UserProgramGoalsMapping> userProgramGoalsMappings = userProgramGoalsMappingRepository.findByUserUserId(user.getUserId());
        List<Long> userProgramExpertiseGoalsMappingIdList = userProgramGoalsMappings.stream().map(userProgramGoalsMapping -> userProgramGoalsMapping.getProgramExpertiseGoalsMapping().getProgramExpertiseGoalsMappingId()).collect(Collectors.toList());

        List<MemberGoalsView> memberGoalsViews = new ArrayList<>();
        for (Long programExpertiseMappingId : goalsRequestView.getProgramExpertiseMappingIds()) {
            List<GoalsView> goalsViewList = new ArrayList<>();
            MemberGoalsView memberGoalsView = new MemberGoalsView();
            List<ProgramExpertiseGoalsMapping> programExpertiseGoalsMappings = programExpertiseGoalsMappingRepository.findByProgramExpertiseMappingProgramExpertiseMappingId(programExpertiseMappingId);
            for (ProgramExpertiseGoalsMapping programExpertiseGoalsMapping : programExpertiseGoalsMappings) {
                GoalsView goalsView = new GoalsView();
                goalsView.setGoal(programExpertiseGoalsMapping.getProgramGoals().getProgramGoal());
                goalsView.setProgramExpertiseGoalMappingId(programExpertiseGoalsMapping.getProgramExpertiseGoalsMappingId());
                //checking whether the goal is already selected by user
                if (userProgramExpertiseGoalsMappingIdList.contains(programExpertiseGoalsMapping.getProgramExpertiseGoalsMappingId())) {
                    goalsView.setSelected(true);
                }

                goalsViewList.add(goalsView);
            }

            ProgramExpertiseMapping programExpertiseMapping = programExpertiseMappingRepository.findByProgramExpertiseMappingId(programExpertiseMappingId);
            memberGoalsView.setProgramType(programExpertiseMapping.getProgramType().getProgramTypeName());
            memberGoalsView.setProgramExpertiseMappingId(programExpertiseMapping.getProgramExpertiseMappingId());
            memberGoalsView.setGoals(goalsViewList);
            memberGoalsViews.add(memberGoalsView);
        }
        return memberGoalsViews;
    }


    /**
     * Method to add Contact number to the user's profile
     *
     * @param phoneNumberView
     * @return
     */
    public ResponseModel addPhoneNumberToUserProfile(PostPhoneNumberView phoneNumberView) {
        User user = userComponents.getUser();
        String phoneNumber = phoneNumberView.getPhoneNumber();
        if (ValidationUtils.isEmptyString(phoneNumber)) {
            throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, ValidationMessageConstants.MSG_PHONENUMBER_EMPTY, null);
        }
        if (!ValidationUtils.validatePhonenumber(phoneNumberView.getCountryCode(), phoneNumber)) {
            throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, ValidationMessageConstants.MSG_PHONENUMBER_INVALID, null);
        }
        UserProfile userProfile = userProfileRepository.findByUser(user);
        userProfile.setContactNumber(phoneNumber);
        userProfile.setIsdCode(phoneNumberView.getIsdCode());
        UserProfile updatedUserProfile = userProfileRepository.save(userProfile);
        Map<String, String> resMap = new HashMap<>();
        resMap.put(KeyConstants.KEY_USER_ID, String.valueOf(user.getUserId()));
        resMap.put(KeyConstants.KEY_CONTACT_NUMBER, updatedUserProfile.getContactNumber());
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_USER_PHONE_NUMBER_ADDED, resMap);
    }

    public ResponseModel updateUserWorkoutStatus(Long programId, Long workoutId) {

        validationService.validateProgramIdBlocked(programId);
        if (!workoutRepository.existsByWorkoutId(workoutId)) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_WORKOUT_NOT_EXISTS, null);
        }

        WorkoutMapping workoutMapping = workoutMappingRepository.findByProgramsProgramIdAndWorkoutWorkoutId(programId, workoutId);

        if (workoutMapping == null) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_WORKOUT_MAPPING_ERROR, null);
        }

        UserWorkoutStatus userWorkoutStatus = new UserWorkoutStatus();
        userWorkoutStatus.setProgram(workoutMapping.getPrograms());
        userWorkoutStatus.setCompletionDate(new Date());
        userWorkoutStatus.setWorkouts(workoutMapping.getWorkout());
        userWorkoutStatus.setStatus(KeyConstants.KEY_COMPLETED);
        userWorkoutStatus.setUser(userComponents.getUser());
        userWorkoutStatusRepository.save(userWorkoutStatus);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_USER_WORKOUT_STATUS, null);
    }
    
    /**
     * Used to validate user email in social login.
     * Since it should not throw conflict error that email already exists
     *
     * @param emailView the email view
     * @return the response model
     * @throws ApplicationException the application exception
     */
    public ResponseModel userEmailValidateForSocialLogin(ValidateEmailView emailView) {
        ValidationUtils.validateEmail(emailView.getEmail());
        if (ValidationUtils.isEmptyString(emailView.getRole())) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ROLE_NULL,
                    Constants.RESPONSE_INVALID_DATA);
        }
        ResponseModel res = new ResponseModel();
        try {
            UserOtp userOtp = new UserOtp();
            int otp = AppUtils.generateRandonNumber();
            List<UserOtp> userOtps = userOtpRepository.findByEmailOrderByOtpId(emailView.getEmail());
            if (!userOtps.isEmpty()) {
                userOtp = userOtps.get(0);
            }
            userOtp.setEmail(emailView.getEmail());
            userOtp.setOtp(otp);
            userOtp.setActive(true);
            userOtp.setVerified(false);
            userOtpRepository.save(userOtp);

            String subject = EmailConstants.OTP_SUBJECT;
            String mailBody = EmailConstants.OTP_CONTENT.replace(EmailConstants.LITERAL_OTP, String.valueOf(otp));
            mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi ,").replace(EmailConstants.EMAIL_BODY, mailBody);
            if (KeyConstants.KEY_INSTRUCTOR.equalsIgnoreCase(emailView.getRole())) {
                mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
            } else {
                mailBody = emailContentUtil.replaceMemberAppUrl(mailBody);
            }
            asyncMailer.sendHtmlMail(emailView.getEmail(), subject, mailBody);
            res.setStatus(Constants.CONTENT_NEEDS_TO_BE_VALIDATE);
            res.setMessage(ValidationMessageConstants.MSG_VALIDATE_OTP);
        } catch (Exception e) {
            throw e;
        }
        return res;
    }

    /**
     * Login.
     *
     * @param userName the user name
     * @param password the password
     * @param role     the role
     * @return the login response view
     * @throws ApplicationException the application exception
     */
    @Transactional
    public V2LoginResponseView v2Login(final String userName, final String password, final String role) {
        log.info("V2Login starts.");
        long apiStartTimeMillis = new Date().getTime();
        ValidationUtils.validateEmail(userName);
        if (ValidationUtils.isEmptyString(password)) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_SECRET_EMPTY, MessageConstants.ERROR);
        }
        UserDetails userDetails = loadUserByEmail(userName, role);
        /*
         * Check whether the role input from user is valid
         */
        if (!validateRoleFromUser(role)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ROLE_NOT_FOUND, null);
        }
        User user = loadUser(userName);
        if (!BCrypt.checkpw(password, userDetails.getPassword())) {
            throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_USERNAME_SECRET_INCORRECT, MessageConstants.ERROR_INTERNAL_SERVER_FAILURE);
        }
        log.info(StringConstants.LOG_FIELD_VALIDATION + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        UserNamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UserNamePasswordAuthenticationToken(
                userDetails, password, null, false);
        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        log.info("Authenticate the user : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        V2LoginResponseView loginResponseView = new V2LoginResponseView();
        if (authentication.isAuthenticated() && authentication.getPrincipal() != null) {
            /*
             * Say, If a user has already been registered under "Member" role with password for Fit-wise,
             * he can use the same login credentials to login to Instructor app also.
             * But the user should confirm for adding new role as "Instructor" in the prompt
             * shown to him in Client apps.
             *
             * setNewRolePrompt - boolean to take care of the above scenario!
             */
            boolean isNewRole = true;
            for (UserRole userRole : AppUtils.getUserRoles(user)) {
                if (userRole.getName().equalsIgnoreCase(role)) {
                    isNewRole = false;
                    break;
                }
            }
            /*
             * If user has entered password for Fitwise,
             * return it in the response which will be used by the Client
             * to show appropriate pop-up to validate the user!
             */
            if (user.isEnteredFitwisePassword()) {
                loginResponseView.setHasFitwisePassword(true);
            }
            loginResponseView.setUserId(user.getUserId());
            loginResponseView.setSignUpDate(user.getCreatedDate());
            loginResponseView.setAuthToken(authentication.getName());
            loginResponseView.setNewRolePrompt(isNewRole);
        } else {
            throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_USERNAME_SECRET_INCORRECT, null);
        }
        log.info("Verify the user is registered or not : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        /**
         * Check whether its super admin role
         */
        if (role.equalsIgnoreCase(SecurityFilterConstants.ROLE_ADMIN)) {
            String superAdminEmailAddresses = generalProperties.getSuperAdminEmailAddresses();
            String[] superAdminEmails = superAdminEmailAddresses.split(",");
            for (String email : superAdminEmails) { //Checking for super admin emails
                if (email.equalsIgnoreCase(userName)) {
                    loginResponseView.setSuperAdmin(true);
                    break;
                }
            }
        }
        log.info("Check whether it's super admin role : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("V2Login ends.");
        return loginResponseView;
    }

    /**
     *
     * Get single video details along with video qualities
     * @param vimeoId
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    public ResponseModel getVideoDetails(Long vimeoId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        if(vimeoId == null || vimeoId == 0){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VIMEO_ID_NULL,null);
        }
        Map<String,Object>  response = new HashMap<>();
        response.put(KeyConstants.KEY_VIDEO_STANDARDS,vimeoService.getVimeoVideos(vimeoId));
        return  new ResponseModel(Constants.SUCCESS_STATUS,MessageConstants.MSG_DATA_RETRIEVED,response);
    }

}