package com.fitwise.delete.service;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.EmailConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.Challenge;
import com.fitwise.entity.ChatConversation;
import com.fitwise.entity.Circuit;
import com.fitwise.entity.CircuitCompletion;
import com.fitwise.entity.DeleteReasonAudit;
import com.fitwise.entity.DeleteReasons;
import com.fitwise.entity.DeletedProgramAudit;
import com.fitwise.entity.DeletedUserAudit;
import com.fitwise.entity.ExerciseCompletion;
import com.fitwise.entity.Exercises;
import com.fitwise.entity.InstructorAwards;
import com.fitwise.entity.InstructorCertification;
import com.fitwise.entity.InstructorProgramExperience;
import com.fitwise.entity.OtherExpertise;
import com.fitwise.entity.ProgramFeedback;
import com.fitwise.entity.ProgramRating;
import com.fitwise.entity.Programs;
import com.fitwise.entity.TaxId;
import com.fitwise.entity.User;
import com.fitwise.entity.UserActiveInactiveTracker;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.UserProgramGoalsMapping;
import com.fitwise.entity.UserRole;
import com.fitwise.entity.UserRoleMapping;
import com.fitwise.entity.WorkoutCompletion;
import com.fitwise.entity.WorkoutFeedback;
import com.fitwise.entity.WorkoutMapping;
import com.fitwise.entity.Workouts;
import com.fitwise.entity.packaging.DeletedPackageAudit;
import com.fitwise.entity.packaging.PackageKloudlessMapping;
import com.fitwise.entity.packaging.PackageMemberMapping;
import com.fitwise.entity.packaging.PackageProgramMapping;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.packaging.SubscriptionPackagePriceByPlatform;
import com.fitwise.entity.payments.paypal.UserAccountAndPayPalIdMapping;
import com.fitwise.entity.payments.stripe.connect.StripeAccountAndUserMapping;
import com.fitwise.entity.social.AppleAuthentication;
import com.fitwise.entity.social.FacebookAuthentication;
import com.fitwise.entity.social.GoogleAuthentication;
import com.fitwise.entity.subscription.ProgramSubscription;
import com.fitwise.entity.thumbnail.ThumbnailImages;
import com.fitwise.exception.ApplicationException;
import com.fitwise.program.service.ProgramService;
import com.fitwise.properties.StripeProperties;
import com.fitwise.repository.AwardsRepository;
import com.fitwise.repository.BlockedProgramsRepository;
import com.fitwise.repository.BlockedUserRepository;
import com.fitwise.repository.CertificateRepository;
import com.fitwise.repository.DeleteReasonAuditRepository;
import com.fitwise.repository.DeleteReasonsRepository;
import com.fitwise.repository.DeletedProgramAuditRepository;
import com.fitwise.repository.DeletedUserAuditRepository;
import com.fitwise.repository.ExerciseRepository;
import com.fitwise.repository.InstructorExperienceRepository;
import com.fitwise.repository.OtherExpertiseRepository;
import com.fitwise.repository.ProgramRatingRepository;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.SampleProgramsRepository;
import com.fitwise.repository.TaxRepository;
import com.fitwise.repository.UserActiveInactiveTrackerRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.UserProgramGoalsMappingRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.repository.UserRoleMappingRepository;
import com.fitwise.repository.UserRoleRepository;
import com.fitwise.repository.WorkoutMappingRepository;
import com.fitwise.repository.WorkoutRepository;
import com.fitwise.repository.challenge.ChallengeRepository;
import com.fitwise.repository.circuit.CircuitRepository;
import com.fitwise.repository.feedback.ProgramFeedbackRepository;
import com.fitwise.repository.feedback.WorkoutFeedbackRepository;
import com.fitwise.repository.member.CircuitCompletionRepository;
import com.fitwise.repository.member.ExerciseCompletionRepository;
import com.fitwise.repository.member.WorkoutCompletionRepository;
import com.fitwise.repository.messaging.ChatConversationRepository;
import com.fitwise.repository.packaging.BlockedPackageRepository;
import com.fitwise.repository.packaging.DeletedPackageAuditRepository;
import com.fitwise.repository.packaging.PackageKloudlessMappingRepository;
import com.fitwise.repository.packaging.PackageMemberMappingRepository;
import com.fitwise.repository.packaging.PackageProgramMappingRepository;
import com.fitwise.repository.packaging.SubscriptionPackagePriceByPlatformRepository;
import com.fitwise.repository.packaging.SubscriptionPackageRepository;
import com.fitwise.repository.payments.stripe.connect.StripeAccountAndUserMappingRepository;
import com.fitwise.repository.payments.stripe.paypal.UserAccountAndPayPalIdMappingRepository;
import com.fitwise.repository.socialLogin.AppleAuthRepository;
import com.fitwise.repository.socialLogin.FacebookAuthRepository;
import com.fitwise.repository.socialLogin.GoogleAuthenticationRepository;
import com.fitwise.repository.subscription.ProgramSubscriptionRepo;
import com.fitwise.repository.thumbnail.ThumbnailRepository;
import com.fitwise.request.DeleteReasonsRequest;
import com.fitwise.service.SubscriptionService;
import com.fitwise.service.ThumbnailService;
import com.fitwise.service.fcm.UserFcmTokenService;
import com.fitwise.service.payment.authorizenet.PaymentService;
import com.fitwise.service.v2.instructor.InstructorProfileService;
import com.fitwise.utils.AppUtils;
import com.fitwise.utils.EmailContentUtil;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.mail.AsyncMailer;
import com.fitwise.view.ResponseModel;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteService {

    /**
     * The user components.
     */
    @Autowired
    private UserComponents userComponents;

    /**
     * The user repository.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * The user role repository.
     */
    @Autowired
    private UserRoleRepository userRoleRepository;

    /**
     * The program repository.
     */
    @Autowired
    private ProgramRepository programRepository;

    /**
     * The program subscription repo repository.
     */
    @Autowired
    private ProgramSubscriptionRepo programSubscriptionRepo;

    /**
     * The user profile repository.
     */
    @Autowired
    private UserProfileRepository userProfileRepository;

    /**
     * The admin blocked service.
     */
    @Autowired
    private UserRoleMappingRepository userRoleMappingRepository;

    /**
     * To delete user audit repository.
     */
    @Autowired
    private DeletedUserAuditRepository deletedUserAuditRepository;

    /**
     * The deleted program audit repository.
     */
    @Autowired
    private DeletedProgramAuditRepository deletedProgramAuditRepository;

    /**
     * The workout mapping repository.
     */
    @Autowired
    private WorkoutMappingRepository workoutMappingRepository;

    /**
     * The workout repository.
     */
    @Autowired
    private WorkoutRepository workoutRepository;

    /**
     * To delete reason repository.
     */
    @Autowired
    private DeleteReasonsRepository deleteReasonsRepository;

    /**
     * To delete reason audit repository.
     */
    @Autowired
    private DeleteReasonAuditRepository deleteReasonAuditRepository;

    @Autowired
    private ProgramService programService;
    @Autowired
    FacebookAuthRepository facebookAuthRepository;
    @Autowired
    GoogleAuthenticationRepository googleAuthenticationRepository;
    @Autowired
    AppleAuthRepository appleAuthRepository;

    private final AsyncMailer asyncMailer;

    @Autowired
    FitwiseUtils fitwiseUtils;
    @Autowired
    private BlockedProgramsRepository blockedProgramsRepository;
    @Autowired
    SubscriptionService subscriptionService;
    @Autowired
    private BlockedUserRepository blockedUserRepository;
    @Autowired
    PaymentService paymentService;
    @Autowired
    private SampleProgramsRepository sampleProgramsRepository;
    @Autowired
    private UserFcmTokenService userFcmTokenService;
    @Autowired
    private InstructorExperienceRepository instructorExperienceRepository;
    @Autowired
    private CircuitRepository circuitRepository;
    @Autowired
    private ExerciseRepository exerciseRepository;
    @Autowired
    private AwardsRepository awardsRepository;
    @Autowired
    private CertificateRepository certificateRepository;
    @Autowired
    private OtherExpertiseRepository otherExpertiseRepository;
    @Autowired
    UserActiveInactiveTrackerRepository userActiveInactiveTrackerRepository;
    @Autowired
    ChatConversationRepository chatConversationRepository;
    @Autowired
    ChallengeRepository challengeRepository;
    @Autowired
    private UserProgramGoalsMappingRepository userProgramGoalsMappingRepository;
    @Autowired
    ProgramFeedbackRepository programFeedbackRepository;
    @Autowired
    WorkoutFeedbackRepository workoutFeedbackRepository;
    @Autowired
    ProgramRatingRepository programRatingRepository;
    @Autowired
    ThumbnailRepository thumbnailRepository;
    @Autowired
    ThumbnailService thumbnailService;
    @Autowired
    TaxRepository taxRepository;
    @Autowired
    ExerciseCompletionRepository exerciseCompletionRepository;
    @Autowired
    CircuitCompletionRepository circuitCompletionRepository;
    @Autowired
    WorkoutCompletionRepository workoutCompletionRepository;
    @Autowired
    private EmailContentUtil emailContentUtil;
    @Autowired
    private SubscriptionPackageRepository subscriptionPackageRepository;
    @Autowired
    PackageMemberMappingRepository packageMemberMappingRepository;
    @Autowired
    PackageProgramMappingRepository packageProgramMappingRepository;
    @Autowired
    PackageKloudlessMappingRepository packageKloudlessMappingRepository;
    @Autowired
    SubscriptionPackagePriceByPlatformRepository subscriptionPackagePriceByPlatformRepository;
    @Autowired
    private BlockedPackageRepository blockedPackageRepository;
    @Autowired
    private DeletedPackageAuditRepository deletedPackageAuditRepository;

    @Autowired
    private StripeAccountAndUserMappingRepository stripeAccountAndUserMappingRepository;

    @Autowired
    private StripeProperties stripeProperties;

    @Autowired
    private UserAccountAndPayPalIdMappingRepository userAccountAndPayPalIdMappingRepository;

    @Autowired
    private InstructorProfileService instructorProfileService;

    /**
     * Delete Instructor.
     *
     * @param deleteReasonsRequests of the instructor
     * @return ResponseModel
     */
    @org.springframework.transaction.annotation.Transactional
    public ResponseModel deleteInstructorAccount(DeleteReasonsRequest deleteReasonsRequests) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        User user = userComponents.getUser();
        if (!fitwiseUtils.isInstructor(user)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_INSTRUCTOR, MessageConstants.ERROR);
        }
        for (Long deleteReasonId : deleteReasonsRequests.getDeleteReasons()) {
            if (!deleteReasonsRepository.existsByDeleteReasonId(deleteReasonId)) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MAG_NOT_FOUND_DELETE_REASON, null);
            }
        }
        String email = user.getEmail();
        String userName = fitwiseUtils.getUserFullName(user);
        ResponseModel responseModel = deleteUser(user.getUserId(), KeyConstants.KEY_INSTRUCTOR, false);
        List<DeleteReasons> deleteReasons = deleteReasonsRepository.findAllById(deleteReasonsRequests.getDeleteReasons());
        UserRole userRole = userRoleRepository.findByName(KeyConstants.KEY_INSTRUCTOR);
        for (DeleteReasons deleteReason : deleteReasons) {
            DeleteReasonAudit deleteReasonAudit = new DeleteReasonAudit();
            deleteReasonAudit.setDeleteReasons(deleteReason);
            deleteReasonAudit.setUser(user);
            deleteReasonAudit.setUserRole(userRole);
            deleteReasonAuditRepository.save(deleteReasonAudit);
        }
        //Send mail to instructor
        String subject = EmailConstants.ACCOUNT_DELETE_SUBJECT;
        String mailBody = EmailConstants.ACCOUNT_DELETE_CONTENT;
        mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, mailBody);
        mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
        asyncMailer.sendHtmlMail(email, subject, mailBody);
        return responseModel;
    }

    /**
     * Delete Member.
     *
     * @return ResponseModel
     */
    @org.springframework.transaction.annotation.Transactional
    public ResponseModel deleteMemberAccount(DeleteReasonsRequest deleteReasonsRequest) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        User user = userComponents.getUser();
        if (!fitwiseUtils.isMember(user)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_MEMBER, MessageConstants.ERROR);
        }
        for (Long deleteReasonId : deleteReasonsRequest.getDeleteReasons()) {
            if (!deleteReasonsRepository.existsByDeleteReasonId(deleteReasonId)) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MAG_NOT_FOUND_DELETE_REASON, null);
            }
        }
        ResponseModel responseModel = deleteUser(user.getUserId(), KeyConstants.KEY_MEMBER, false);
        List<DeleteReasons> deleteReasons = deleteReasonsRepository.findAllById(deleteReasonsRequest.getDeleteReasons());
        UserRole userRole = userRoleRepository.findByName(KeyConstants.KEY_MEMBER);
        for (DeleteReasons deleteReason : deleteReasons) {
            DeleteReasonAudit deleteReasonAudit = new DeleteReasonAudit();
            deleteReasonAudit.setDeleteReasons(deleteReason);
            deleteReasonAudit.setUser(user);
            deleteReasonAudit.setUserRole(userRole);
            deleteReasonAuditRepository.save(deleteReasonAudit);
        }
        return responseModel;
    }

    /**
     * Delete user.
     *
     * @param userId of the user
     * @param role   of the user
     * @return ResponseModel
     */
    @Transactional(rollbackOn = {Exception.class})
    public ResponseModel deleteUserAccount(Long userId, String role) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        User admin = userComponents.getUser();
        boolean isAdmin = fitwiseUtils.isAdmin(admin);
        if (!isAdmin) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_ADMIN, MessageConstants.ERROR);
        }
        User user = userRepository.findByUserId(userId);
        String email = user.getEmail();
        String userName = fitwiseUtils.getUserFullName(user);
        ResponseModel responseModel = deleteUser(userId, role, true);
        //Send mail to notify account delete
        if(!role.equalsIgnoreCase(KeyConstants.KEY_MEMBER)){
            String subject = EmailConstants.ACCOUNT_DELETE_BY_ADMIN_SUBJECT.replace("#ROLE#", role);
            String supportLink = EmailConstants.TRAINNR_SUPPORT_LINK.replace(EmailConstants.EMAIL_EMAIL_ADDRESS, user.getEmail());
            String mailBody = EmailConstants.ACCOUNT_DELETE_BY_ADMIN_CONTENT.replace("#ROLE#", role);
            mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
                    .replace(EmailConstants.EMAIL_BODY, mailBody)
                    .replace(EmailConstants.EMAIL_SUPPORT_URL, supportLink);
            if (KeyConstants.KEY_INSTRUCTOR.equalsIgnoreCase(role)) {
                mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
            } else {
                mailBody = emailContentUtil.replaceMemberAppUrl(mailBody);
            }
            asyncMailer.sendHtmlMail(email, subject, mailBody);
        }
        return responseModel;
    }

    @org.springframework.transaction.annotation.Transactional
    public ResponseModel deleteUser(Long userId, String role, boolean isAdmin) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        User currentUser = userComponents.getUser();
        if (!(KeyConstants.KEY_MEMBER.equalsIgnoreCase(role) || KeyConstants.KEY_INSTRUCTOR.equalsIgnoreCase(role))) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ROLE_INCORRECT, null);
        }
        if (userId == null || userId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_ID_NULL, MessageConstants.ERROR);
        }
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_FOUND, MessageConstants.ERROR);
        }
        if (isAdmin && !blockedUserRepository.existsByUserUserIdAndUserRoleName(userId, role)) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_USER_CANT_DELETE_NOT_BLOCKED, null);
        }
        Optional<UserRole> userRoleOptional = AppUtils.getUserRoles(user).stream().filter(userRole -> userRole.getName().equalsIgnoreCase(role)).findAny();
        if (!userRoleOptional.isPresent()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_USER_NOT_FOUND_WITH_ROLE, null);
        }
        UserRole userRole = userRoleOptional.get();
        //delete user validation based on role
        if (userRole.getName().equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR)) {
            List<Programs> programsList = programRepository.findByOwnerUserId(user.getUserId());
            List<Long> programIdList = programsList.stream().map(Programs::getProgramId).collect(Collectors.toList());
            long subscriptionCount = subscriptionService.getOverallActiveSubscriptionCountForProgramsList(programIdList);
            if (subscriptionCount > 0) {
                throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_CANT_DELETE_INSTRUCTOR_SUBSCRIBED_PROGRAMS, MessageConstants.ERROR);
            }
            //Validation to check if payment is yet to be paid to instructor
            if (paymentService.isInstructorPayOffPending(userId)) {
                if (isAdmin) {
                    throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_ADMIN_CANT_DELETE_INSTRUCTOR_PAYMENT_PENDING, MessageConstants.ERROR);
                } else {
                    throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_CANT_DELETE_INSTRUCTOR_PAYMENT_PENDING, MessageConstants.ERROR);
                }
            }
            //Delete all data related to instructor
            deleteInstructorData(user, userRole);
        } else if (userRole.getName().equalsIgnoreCase(KeyConstants.KEY_MEMBER)) {
            List<ProgramSubscription> programSubscriptions = subscriptionService.getPaidProgramSubscriptionsByAnUser(user.getUserId());
            if (!programSubscriptions.isEmpty()) {
                int subscriptionCount = programSubscriptions.size();
                if (isAdmin) {
                    throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_ADMIN_CANT_DELETE_MEMBER_SUBSCRIBED_PROGRAMS.replace(StringConstants.LITERAL_COUNT, String.valueOf(subscriptionCount)), MessageConstants.ERROR);
                } else {
                    throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_CANT_DELETE_MEMBER_SUBSCRIBED_PROGRAMS.replace(StringConstants.LITERAL_COUNT, String.valueOf(subscriptionCount)), MessageConstants.ERROR);
                }
            }
            //Delete all data related to member
            deleteMemberData(user, userRole);
            //Deleting auth.net data for member
            paymentService.deleteUserFromAuthorizeNet(user.getUserId());
            paymentService.deleteUserFromStripe(user.getUserId());
        }
        //Multi role users
        if (AppUtils.getUserRoles(user).size() >= 2) {
            List<UserRoleMapping> userRoleMapping = userRoleMappingRepository.findByUserUserIdAndUserRoleName(user.getUserId(), userRole.getName());
            List<UserRoleMapping> userRoleMappings = new ArrayList<>();
            for (UserRoleMapping userRoleMapping1 : user.getUserRoleMappings()) {
                if (!userRole.getName().equalsIgnoreCase(userRoleMapping1.getUserRole().getName())) {
                    userRoleMappings.add(userRoleMapping1);
                }
            }
            user.setUserRoleMappings(userRoleMappings);
            userRepository.save(user);
            userRoleMappingRepository.deleteAll(userRoleMapping);
            //Removing social login account related data
            removeSocialLoginData(user, userRole);
        } else {
            //single role users
            //Remove fcm tokens of user
            userFcmTokenService.removeFcmTokenOfUser(user);
            makeUserAsAnonymous(user, userRole);
        }
        blockedUserRepository.deleteByUserUserIdAndUserRole(user.getUserId(), userRole);
        DeletedUserAudit deletedUserAudit = new DeletedUserAudit();
        deletedUserAudit.setUser(user);
        deletedUserAudit.setHappenedDate(new Date());
        deletedUserAudit.setDoneBy(currentUser);
        deletedUserAudit.setUserRole(userRole);
        deletedUserAuditRepository.save(deletedUserAudit);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_USER_DELETED, null);
    }

    /**
     * Delete all data related to instructor
     *
     * @param user
     */
    private void deleteInstructorData(User user, UserRole userRole) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        //Deleting instructor programs related data
        deleteProgramData(user);
        List<ThumbnailImages> thumbnailImages = thumbnailRepository.findByUserUserIdAndType(user.getUserId(), KeyConstants.KEY_CUSTOM);
        if (!thumbnailImages.isEmpty()) {
            for (ThumbnailImages thumbnailImage : thumbnailImages) {
                thumbnailService.deleteThumbnailFromLibrary(thumbnailImage.getImages().getImageId(), user);
            }
        }
        UserProfile userProfile = userProfileRepository.findByUser(user);
        if(userProfile.getPromotion() != null){
            instructorProfileService.deleteInstructorPromo(userProfile.getPromotion().getPromotionId(), user);
        }
        //Instructor experience details deleted
        List<InstructorProgramExperience> instructorProgramExperienceList = instructorExperienceRepository.findByUserUserId(user.getUserId());
        if (!instructorProgramExperienceList.isEmpty()) {
            instructorExperienceRepository.deleteInBatch(instructorProgramExperienceList);
        }
        List<InstructorAwards> instructorAwardsList = awardsRepository.findByUser(user);
        if (!instructorAwardsList.isEmpty()) {
            awardsRepository.deleteInBatch(instructorAwardsList);
        }
        List<InstructorCertification> instructorCertificationList = certificateRepository.findByUser(user);
        if (!instructorCertificationList.isEmpty()) {
            certificateRepository.deleteInBatch(instructorCertificationList);
        }
        List<OtherExpertise> otherExpertiseList = otherExpertiseRepository.findByUserUserId(user.getUserId());
        if (!otherExpertiseList.isEmpty()) {
            otherExpertiseRepository.deleteInBatch(otherExpertiseList);
        }
        List<UserActiveInactiveTracker> userActiveInactiveTrackers = userActiveInactiveTrackerRepository.findByUserUserIdAndUserRoleRoleId(user.getUserId(), userRole.getRoleId());
        if (!userActiveInactiveTrackers.isEmpty()) {
            userActiveInactiveTrackerRepository.deleteInBatch(userActiveInactiveTrackers);
        }
        //Removing relation with conversations
        List<ChatConversation> conversationList = chatConversationRepository.findByPrimaryUserUserUserId(user.getUserId());
        if (!conversationList.isEmpty()) {
            for (ChatConversation conversation : conversationList) {
                conversation.setPrimaryUser(null);
            }
            chatConversationRepository.saveAll(conversationList);
        }
        TaxId taxId = taxRepository.findByUserUserId(user.getUserId());
        if (taxId != null) {
            taxRepository.delete(taxId);
        }
        /**
         * Delete connected account from Stripe if on-boarded
         */
        StripeAccountAndUserMapping stripeAccountAndUserMapping = stripeAccountAndUserMappingRepository.findByUserUserId(user.getUserId());
        if (stripeAccountAndUserMapping != null) {
            Stripe.apiKey = stripeProperties.getApiKey();
            Account account;
            try {
                account = Account.retrieve(stripeAccountAndUserMapping.getStripeAccountId());
                account.delete();
            } catch (StripeException exception) {
                log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
            }
            stripeAccountAndUserMappingRepository.delete(stripeAccountAndUserMapping);
        }
        /**
         * Delete Pay-pal account if provided by user
         */
        UserAccountAndPayPalIdMapping userAccountAndPayPalIdMapping = userAccountAndPayPalIdMappingRepository.findByUserUserId(user.getUserId());
        if (userAccountAndPayPalIdMapping != null) {
            userAccountAndPayPalIdMappingRepository.delete(userAccountAndPayPalIdMapping);
        }
    }

    private void deleteProgramData(User user) {
        List<Programs> programsList = programRepository.findByOwnerUserId(user.getUserId());
        for (Programs program : programsList) {
            //removing program workout mappings.
            List<WorkoutMapping> workoutMappings = workoutMappingRepository.findByPrograms(program);
            workoutMappingRepository.deleteInBatch(workoutMappings);
            //removing program's promotion.
            if (program.getPromotion() != null) {
                programService.deletePromotion(program.getPromotion().getPromotionId(), program.getProgramId());
            }
            program = getProgramAsAnonymous(program);
            programRepository.save(program);
            blockedProgramsRepository.deleteByProgramProgramId(program.getProgramId());
        }
        //Delete all instructor workouts
        List<Workouts> workouts = workoutRepository.findByOwnerUserId(user.getUserId());
        for (Workouts workout : workouts) {
            workout = fitwiseUtils.makeWorkoutAnonymous(workout);
            workoutRepository.save(workout);
        }
        List<Circuit> circuits = circuitRepository.findByOwnerUserId(user.getUserId());
        for (Circuit circuit : circuits) {
            circuit = fitwiseUtils.makeCircuitAnonymous(circuit);
            circuitRepository.save(circuit);
        }
        List<Exercises> exerciseList = exerciseRepository.findByOwnerUserId(user.getUserId());
        for (Exercises exercise : exerciseList) {
            exercise = fitwiseUtils.makeExerciseAnonymous(exercise);
            exerciseRepository.save(exercise);
        }
    }

    private void deleteMemberData(User user, UserRole userRole) {
        //Removing relation with conversations
        List<ChatConversation> conversationList = chatConversationRepository.findBySecondaryUserUserUserId(user.getUserId());
        if (!conversationList.isEmpty()) {
            for (ChatConversation conversation : conversationList) {
                conversation.setSecondaryUser(null);
            }
            chatConversationRepository.saveAll(conversationList);
        }
        List<Challenge> challengeList = challengeRepository.findByUserUserId(user.getUserId());
        if (!challengeList.isEmpty()) {
            challengeRepository.deleteInBatch(challengeList);
        }
        List<ExerciseCompletion> exerciseCompletionList = exerciseCompletionRepository.findByMemberUserId(user.getUserId());
        if (!exerciseCompletionList.isEmpty()) {
            exerciseCompletionRepository.deleteInBatch(exerciseCompletionList);
        }
        List<CircuitCompletion> circuitCompletionList = circuitCompletionRepository.findByMemberUserId(user.getUserId());
        if (!circuitCompletionList.isEmpty()) {
            circuitCompletionRepository.deleteInBatch(circuitCompletionList);
        }
        List<WorkoutCompletion> workoutCompletionList = workoutCompletionRepository.findByMemberUserId(user.getUserId());
        if (!workoutCompletionList.isEmpty()) {
            workoutCompletionRepository.deleteInBatch(workoutCompletionList);
        }
        List<UserActiveInactiveTracker> userActiveInactiveTrackers = userActiveInactiveTrackerRepository.findByUserUserIdAndUserRoleRoleId(user.getUserId(), userRole.getRoleId());
        if (!userActiveInactiveTrackers.isEmpty()) {
            userActiveInactiveTrackerRepository.deleteInBatch(userActiveInactiveTrackers);
        }
        List<ProgramSubscription> programSubscriptionList = programSubscriptionRepo.findByUserUserId(user.getUserId());
        if (!programSubscriptionList.isEmpty()) {
            for (ProgramSubscription programSubscription : programSubscriptionList) {
                programSubscription.setUser(null);
            }
            programSubscriptionRepo.saveAll(programSubscriptionList);
        }
        List<UserProgramGoalsMapping> userProgramGoalsMapping = userProgramGoalsMappingRepository.findByUserUserId(user.getUserId());
        if (!userProgramGoalsMapping.isEmpty()) {
            userProgramGoalsMappingRepository.deleteInBatch(userProgramGoalsMapping);
        }
        List<ProgramFeedback> programFeedbackList = programFeedbackRepository.findByUser(user);
        if (!programFeedbackList.isEmpty()) {
            for (ProgramFeedback programFeedback : programFeedbackList) {
                programFeedback.setUser(null);
            }
            programFeedbackRepository.saveAll(programFeedbackList);
        }
        List<ProgramRating> programRatingList = programRatingRepository.findByUser(user);
        if (!programRatingList.isEmpty()) {
            for (ProgramRating programRating : programRatingList) {
                programRating.setUser(null);
            }
            programRatingRepository.saveAll(programRatingList);
        }
        List<WorkoutFeedback> workoutFeedbackList = workoutFeedbackRepository.findByUserUserId(user.getUserId());
        if (!workoutFeedbackList.isEmpty()) {
            for (WorkoutFeedback workoutFeedback : workoutFeedbackList) {
                workoutFeedback.setUser(null);
            }
            workoutFeedbackRepository.saveAll(workoutFeedbackList);
        }
    }

    /**
     * Removing social login account related data
     *
     * @param user
     * @param userRole
     */
    private void removeSocialLoginData(User user, UserRole userRole) {
        // Removing the facebook record based on user role if exists
        FacebookAuthentication fbAuth = facebookAuthRepository.findByEmailAndUserRole(user.getEmail(), userRole.getName());
        if (fbAuth != null) {
            fbAuth.setFacebookUserAccessToken(null);
            fbAuth.setFacebookAppAccessToken(null);
            fbAuth.setFacebookUserProfileId(KeyConstants.KEY_ANONYMOUS);
            fbAuth.setFirstName(KeyConstants.KEY_ANONYMOUS);
            fbAuth.setLastName(KeyConstants.KEY_ANONYMOUS);
            fbAuth.setUserRole(KeyConstants.KEY_ANONYMOUS);
            fbAuth.setEmail(KeyConstants.KEY_ANONYMOUS);
            fbAuth.setUser(null);
            fbAuth.setIsRoleAddPermissionEnabled(null);
            facebookAuthRepository.save(fbAuth);
        }
        // Removing the Apple record based on user role if exists
        AppleAuthentication appleAuth = appleAuthRepository.findByUserUserIdAndUserRole(user.getUserId(), userRole.getName());
        if (appleAuth != null) {
            appleAuth.setAppleUserId(KeyConstants.KEY_ANONYMOUS);
            appleAuth.setUser(null);
            appleAuth.setUserRole(KeyConstants.KEY_ANONYMOUS);
            appleAuthRepository.save(appleAuth);
        }
        // Removing the Google record based on user role if exists
        GoogleAuthentication googleAuth = googleAuthenticationRepository.findByUserUserIdAndUserRole(user.getUserId(), userRole.getName());
        if (googleAuth != null) {
            googleAuth.setGoogleAuthenticationToken(null);
            googleAuth.setClientId(KeyConstants.KEY_ANONYMOUS);
            googleAuth.setFirstName(KeyConstants.KEY_ANONYMOUS);
            googleAuth.setLastName(KeyConstants.KEY_ANONYMOUS);
            googleAuth.setUserRole(KeyConstants.KEY_ANONYMOUS);
            googleAuth.setUser(null);
            googleAuth.setIsRoleAddPermissionEnabled(null);
            googleAuthenticationRepository.save(googleAuth);
        }
    }

    /**
     * Delete program via instructor.
     *
     * @param programId of the program
     * @return ResponseModel
     */
    @Transactional(rollbackOn = {Exception.class})
    public ResponseModel deleteProgramViaInstructor(Long programId) {
        User user = userComponents.getUser();
        if (!fitwiseUtils.isInstructor(user)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_INSTRUCTOR, MessageConstants.ERROR);
        }
        return deleteProgram(programId, true);
    }

    /**
     * Delete program via admin.
     *
     * @param programId of the program
     * @return ResponseModel
     */
    @Transactional(rollbackOn = {Exception.class})
    public ResponseModel deleteProgramViaAdmin(Long programId) {
        User user = userComponents.getUser();
        boolean isAdmin = fitwiseUtils.isAdmin(user);
        if (!isAdmin) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_ADMIN, MessageConstants.ERROR);
        }
        return deleteProgram(programId, false);
    }

    /**
     * Delete program.
     *
     * @param programId of the program
     * @return ResponseModel
     */
    public ResponseModel deleteProgram(Long programId, boolean isInstructor) {
        User doneBy = userComponents.getUser();
        if (programId == null || programId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_NULL, MessageConstants.ERROR);
        }
        Programs program;
        if (isInstructor) {
            program = programRepository.findByProgramIdAndOwnerUserId(programId, doneBy.getUserId());
        } else {
            program = programRepository.findByProgramId(programId);
        }
        if (program == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, MessageConstants.ERROR);
        }

        List<Long> sampleProgramsIdList = sampleProgramsRepository.findAll().stream().map(sampleProgram -> sampleProgram.getPrograms().getProgramId()).collect(Collectors.toList());
        if (sampleProgramsIdList.contains(programId)) {
            return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CANT_DELETE_SAMPLE_PROGRAM, null);
        }

        if (deletedProgramAuditRepository.existsByProgramProgramId(programId)) {
            return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PROGRAM_DELETED_ALREADY, null);
        }
        if (!isInstructor && !blockedProgramsRepository.existsByProgramProgramId(programId)) {
            throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_PROGRAM_CANT_DELETE_NOT_BLOCKED, null);
        }
        //Can not delete a program with active subscriptions
        long paidSubscriptionsCount = subscriptionService.getActiveSubscriptionCountOfProgram(programId);
        if (paidSubscriptionsCount > 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CANT_DELETE_SUBSCRIBED_PROGRAM.replace(StringConstants.LITERAL_COUNT, String.valueOf(paidSubscriptionsCount)), MessageConstants.ERROR);
        }
        //removing program workout mappings.
        List<WorkoutMapping> workoutMappings = workoutMappingRepository.findByPrograms(program);
        workoutMappingRepository.deleteInBatch(workoutMappings);
        User instructor = program.getOwner();
        String title = program.getTitle();
        //removing program's promotion.
        programService.deletePromotion(program.getPromotion().getPromotionId(), programId);
        program = getProgramAsAnonymous(program);
        programRepository.save(program);
        blockedProgramsRepository.deleteByProgramProgramId(program.getProgramId());
        DeletedProgramAudit deletedProgramAudit = new DeletedProgramAudit();
        deletedProgramAudit.setDoneBy(doneBy);
        deletedProgramAudit.setHappenedDate(new Date());
        deletedProgramAudit.setProgram(program);
        deletedProgramAuditRepository.save(deletedProgramAudit);
        //Sending mail to the program's owner : instructors
        String subject;
        String mailBody;
        String userName = fitwiseUtils.getUserFullName(instructor);
        if (isInstructor) {
            subject = EmailConstants.PROGRAM_DELETE_SUBJECT.replace(EmailConstants.LITERAL_PROGRAM_NAME, "'" + title + "'");
            mailBody = EmailConstants.PROGRAM_DELETE_INSTRUCTOR_CONTENT.replace(EmailConstants.LITERAL_PROGRAM_NAME, "<b>" + title + "</b>");
            mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, mailBody);
        } else {
            subject = EmailConstants.PROGRAM_DELETE_BY_ADMIN_SUBJECT.replace(EmailConstants.LITERAL_PROGRAM_NAME, "'" + title + "'");
            String supportLink = EmailConstants.TRAINNR_SUPPORT_LINK.replace(EmailConstants.EMAIL_EMAIL_ADDRESS, instructor.getEmail());
            mailBody = EmailConstants.PROGRAM_DELETE_BY_ADMIN_CONTENT.replace(EmailConstants.LITERAL_PROGRAM_NAME, "<b>" + title + "</b>");
            mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON
                    .replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
                    .replace(EmailConstants.EMAIL_BODY, mailBody)
                    .replace(EmailConstants.EMAIL_SUPPORT_URL, supportLink);
        }
        mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
        asyncMailer.sendHtmlMail(instructor.getEmail(), subject, mailBody);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PROGRAM_DELETED, null);
    }

    /**
     * we are changing user account as anonymous
     */
    public void makeUserAsAnonymous(User user, UserRole userRole) {
        UserProfile userProfile = userProfileRepository.findByUser(user);
        user.setEmail(KeyConstants.KEY_ANONYMOUS);
        user.setCreatedDate(null);
        user.setPassword(KeyConstants.KEY_ANONYMOUS);
        user.setEnteredFitwisePassword(false);
        user.setNewSocialRegistration(false);
        user.setUserRoleMappings(null);

        if (user.getFacebookAuth() != null && !user.getFacebookAuth().isEmpty()) {
            makeFbAuthAsAnonymous(user.getFacebookAuth());
            user.setFacebookAuth(null);
        }
        if (user.getGoogleAuth() != null && !user.getGoogleAuth().isEmpty()) {
            makeGoogleAuthAsAnonymous(user.getGoogleAuth());
            user.setGoogleAuth(null);
        }
        if (user.getAppleAuth() != null && !user.getAppleAuth().isEmpty()) {
            makeAppleAuthAsAnonymous(user.getAppleAuth());
            user.setAppleAuth(null);
        }
        userRepository.save(user);
        userProfile.setFirstName(KeyConstants.KEY_ANONYMOUS);
        userProfile.setLastName(KeyConstants.KEY_ANONYMOUS);
        userProfile.setBiography(KeyConstants.KEY_ANONYMOUS);
        userProfile.setDob(KeyConstants.KEY_ANONYMOUS);
        userProfile.setContactNumber(KeyConstants.KEY_ANONYMOUS);
        userProfile.setCountryCode(KeyConstants.KEY_ANONYMOUS);
        userProfile.setNotificationStatus(false);
        userProfile.setCoverImage(null);
        userProfile.setGender(null);
        userProfile.setProfileImage(null);
        userProfileRepository.save(userProfile);
        List<UserRoleMapping> userRoleMapping = userRoleMappingRepository.findByUserUserIdAndUserRoleName(user.getUserId(), userRole.getName());
        userRoleMappingRepository.deleteAll(userRoleMapping);
    }

    private void makeFbAuthAsAnonymous(Set<FacebookAuthentication> facebookAuthSet) {
        List<FacebookAuthentication> facebookAuthList = new ArrayList<>();
        for (FacebookAuthentication fbAuth : facebookAuthSet) {
            fbAuth.setFacebookUserAccessToken(null);
            fbAuth.setFacebookAppAccessToken(null);
            fbAuth.setFacebookUserProfileId(KeyConstants.KEY_ANONYMOUS);
            fbAuth.setFirstName(KeyConstants.KEY_ANONYMOUS);
            fbAuth.setLastName(KeyConstants.KEY_ANONYMOUS);
            fbAuth.setUserRole(KeyConstants.KEY_ANONYMOUS);
            fbAuth.setEmail(KeyConstants.KEY_ANONYMOUS);
            fbAuth.setUser(null);
            fbAuth.setIsRoleAddPermissionEnabled(null);
            facebookAuthList.add(fbAuth);
        }
        facebookAuthRepository.saveAll(facebookAuthList);
    }

    private void makeGoogleAuthAsAnonymous(Set<GoogleAuthentication> googleAuthSet) {
        List<GoogleAuthentication> googleAuthList = new ArrayList<>();
        for (GoogleAuthentication googleAuth : googleAuthSet) {
            googleAuth.setGoogleAuthenticationToken(null);
            googleAuth.setClientId(KeyConstants.KEY_ANONYMOUS);
            googleAuth.setFirstName(KeyConstants.KEY_ANONYMOUS);
            googleAuth.setLastName(KeyConstants.KEY_ANONYMOUS);
            googleAuth.setUserRole(KeyConstants.KEY_ANONYMOUS);
            googleAuth.setUser(null);
            googleAuth.setIsRoleAddPermissionEnabled(null);
            googleAuthList.add(googleAuth);
        }
        googleAuthenticationRepository.saveAll(googleAuthList);
    }

    private void makeAppleAuthAsAnonymous(Set<AppleAuthentication> appleAuthSet) {
        List<AppleAuthentication> appleAuthList = new ArrayList<>();
        for (AppleAuthentication appleAuth : appleAuthSet) {
            appleAuth.setAppleUserId(KeyConstants.KEY_ANONYMOUS);
            appleAuth.setUser(null);
            appleAuth.setUserRole(KeyConstants.KEY_ANONYMOUS);
            appleAuthList.add(appleAuth);
        }
        appleAuthRepository.saveAll(appleAuthList);
    }

    /**
     * we are changing program as anonymous
     */
    public Programs getProgramAsAnonymous(Programs program) {
        program.setStatus(KeyConstants.KEY_DELETED);
        program.setProgramType(null);
        program.setDescription(KeyConstants.KEY_ANONYMOUS);
        program.setShortDescription(KeyConstants.KEY_ANONYMOUS);
        program.setDuration(null);
        program.setFlag(false);
        program.setImage(null);
        program.setInstructorYearOfExperience(null);
        program.setProgramExpertiseLevel(null);
        program.setProgramPrice(0.0);
        program.setProgramMapping(null);
        program.setProgramPriceByPlatforms(null);
        program.setProgramWiseGoals(null);
        program.setPromotion(null);
        program.setPublish(false);
        program.setPublishedDate(null);
        program.setTitle(KeyConstants.KEY_ANONYMOUS);
        program.setWorkoutSchedules(null);
        program.setOwner(null);
        return program;
    }

    public List<DeleteReasons> getDeleteReasons() {
        return deleteReasonsRepository.findAll();
    }

    /**
     * Method to Delete SubscriptionPackage Via Instructor
     * @param subscriptionPackageId
     */
    @org.springframework.transaction.annotation.Transactional
    public void deleteSubscriptionPackageViaInstructor(Long subscriptionPackageId) {
        User user = userComponents.getUser();
        if (!fitwiseUtils.isInstructor(user)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_INSTRUCTOR, MessageConstants.ERROR);
        }
        deleteSubscriptionPackage(subscriptionPackageId, true);
    }

    /**
     * @param subscriptionPackageId
     */
    @org.springframework.transaction.annotation.Transactional
    public void deleteSubscriptionPackageViaAdmin(Long subscriptionPackageId) {
        User user = userComponents.getUser();
        boolean isAdmin = fitwiseUtils.isAdmin(user);
        if (!isAdmin) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_ADMIN, MessageConstants.ERROR);
        }
        deleteSubscriptionPackage(subscriptionPackageId, false);
    }

    /**
     * Method to Delete SubscriptionPackage
     * @param subscriptionPackageId
     * @param isInstructor
     */
    private void deleteSubscriptionPackage(Long subscriptionPackageId, boolean isInstructor) {
        User doneBy = userComponents.getUser();
        if (subscriptionPackageId == null || subscriptionPackageId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_ID_NULL, MessageConstants.ERROR);
        }
        SubscriptionPackage subscriptionPackage;
        if (isInstructor) {
            subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageIdAndOwnerUserId(subscriptionPackageId, doneBy.getUserId());
        } else {
            subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageId(subscriptionPackageId);
        }
        if (subscriptionPackage == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_NOT_FOUND, MessageConstants.ERROR);
        }
        if (deletedPackageAuditRepository.existsBySubscriptionPackageSubscriptionPackageId(subscriptionPackageId)) {
            throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_PACKAGE_DELETED_ALREADY, null);
        }
        if (!isInstructor && !blockedPackageRepository.existsBySubscriptionPackageSubscriptionPackageId(subscriptionPackageId)) {
            throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_PACKAGE_CANT_DELETE_NOT_BLOCKED, null);
        }
        //Can not delete a subscriptionPackage with active subscriptions
        long subscriptionCount = subscriptionService.getActiveSubscriptionCountForPackage(subscriptionPackageId);
        if (subscriptionCount > 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CANT_DELETE_SUBSCRIBED_PACKAGE.replace(StringConstants.LITERAL_COUNT, String.valueOf(subscriptionCount)), MessageConstants.ERROR);
        }
        List<PackageProgramMapping> packageProgramsListMapping = packageProgramMappingRepository.findBySubscriptionPackage(subscriptionPackage);
        if ( packageProgramsListMapping != null && !packageProgramsListMapping.isEmpty()) {
            packageProgramMappingRepository.deleteInBatch(packageProgramsListMapping);
        }
        List<PackageKloudlessMapping> packageKloudlessMappingList = packageKloudlessMappingRepository.findBySubscriptionPackage(subscriptionPackage);
        if (!packageKloudlessMappingList.isEmpty()) {
            packageKloudlessMappingRepository.deleteInBatch(packageKloudlessMappingList);
        }
        List<SubscriptionPackagePriceByPlatform> subscriptionPackagePriceByPlatformList = subscriptionPackagePriceByPlatformRepository.findBySubscriptionPackage(subscriptionPackage);
        if (!subscriptionPackagePriceByPlatformList.isEmpty()) {
            subscriptionPackagePriceByPlatformRepository.deleteInBatch(subscriptionPackagePriceByPlatformList);
        }
        List<PackageMemberMapping> packageMemberList = packageMemberMappingRepository.findBySubscriptionPackage(subscriptionPackage);
        if (!packageMemberList.isEmpty()) {
            packageMemberMappingRepository.deleteInBatch(packageMemberList);
        }
        User instructor = subscriptionPackage.getOwner();
        String title = subscriptionPackage.getTitle();
        getPackageAsAnonymous(subscriptionPackage);
        subscriptionPackageRepository.save(subscriptionPackage);
        blockedPackageRepository.deleteBySubscriptionPackageSubscriptionPackageId(subscriptionPackageId);
        DeletedPackageAudit deletedPackageAudit = new DeletedPackageAudit();
        deletedPackageAudit.setDoneBy(doneBy);
        deletedPackageAudit.setHappenedDate(new Date());
        deletedPackageAudit.setSubscriptionPackage(subscriptionPackage);
        deletedPackageAuditRepository.save(deletedPackageAudit);
        //Sending mail to the program's owner : instructors
        String subject;
        String mailBody;
        String userName = fitwiseUtils.getUserFullName(instructor);
        if (isInstructor) {
            subject = EmailConstants.PACKAGE_DELETE_SUBJECT.replace(EmailConstants.LITERAL_PACKAGE_NAME, "'" + title + "'");
            mailBody = EmailConstants.PACKAGE_DELETE_INSTRUCTOR_CONTENT.replace(EmailConstants.LITERAL_PACKAGE_NAME, "<b>" + title + "</b>");
            mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, mailBody);
        } else {
            subject = EmailConstants.PACKAGE_DELETE_BY_ADMIN_SUBJECT.replace(EmailConstants.LITERAL_PACKAGE_NAME, "'" + title + "'");
            String supportLink = EmailConstants.TRAINNR_SUPPORT_LINK.replace(EmailConstants.EMAIL_EMAIL_ADDRESS, instructor.getEmail());
            mailBody = EmailConstants.PACKAGE_DELETE_BY_ADMIN_CONTENT.replace(EmailConstants.LITERAL_PACKAGE_NAME, "<b>" + title + "</b>");
            mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON
                    .replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
                    .replace(EmailConstants.EMAIL_BODY, mailBody)
                    .replace(EmailConstants.EMAIL_SUPPORT_URL, supportLink);
        }
        mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
        asyncMailer.sendHtmlMail(instructor.getEmail(), subject, mailBody);
    }

    /**
     * Mark SubscriptionPackage as anonymous
     * @param subscriptionPackage
     */
    private void getPackageAsAnonymous(SubscriptionPackage subscriptionPackage) {
        subscriptionPackage.setOwner(null);
        subscriptionPackage.setStatus(KeyConstants.KEY_DELETED);
        subscriptionPackage.setTitle(KeyConstants.KEY_ANONYMOUS);
        subscriptionPackage.setShortDescription(KeyConstants.KEY_ANONYMOUS);
        subscriptionPackage.setDescription(KeyConstants.KEY_ANONYMOUS);
        subscriptionPackage.setImage(null);
        subscriptionPackage.setPackageDuration(null);
        subscriptionPackage.setPackageProgramMapping(null);
        subscriptionPackage.setPackageKloudlessMapping(null);
        subscriptionPackage.setCancellationDuration(null);
        subscriptionPackage.setRestrictedAccess(false);
        subscriptionPackage.setPackageMemberMapping(null);
        subscriptionPackage.setExternalMemberMapping(null);
        subscriptionPackage.setClientMessage(null);
        subscriptionPackage.setPackagePrice(null);
        subscriptionPackage.setPrice(null);
        subscriptionPackage.setPackagePriceByPlatforms(null);
        subscriptionPackage.setPostCompletionStatus(null);
    }
}