package com.fitwise.block.service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.EmailConstants;
import com.fitwise.constants.InstructorConstant;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.BlockedPrograms;
import com.fitwise.entity.BlockedProgramsAudit;
import com.fitwise.entity.BlockedUser;
import com.fitwise.entity.BlockedUserAudit;
import com.fitwise.entity.ChatConversation;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.UserRole;
import com.fitwise.entity.packaging.BlockedPackage;
import com.fitwise.entity.packaging.BlockedPackageAudit;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.payments.stripe.StripeProductAndPackageMapping;
import com.fitwise.entity.payments.stripe.billing.StripeProductAndProgramMapping;
import com.fitwise.entity.subscription.PackageSubscription;
import com.fitwise.entity.subscription.ProgramSubscription;
import com.fitwise.exception.ApplicationException;
import com.fitwise.listeners.block.packaging.PackageBlockEvent;
import com.fitwise.listeners.block.program.ProgramBlockEvent;
import com.fitwise.listeners.invokers.admin.BlockListeners;
import com.fitwise.repository.BlockedProgramsAuditRepository;
import com.fitwise.repository.BlockedProgramsRepository;
import com.fitwise.repository.BlockedUserAuditRepository;
import com.fitwise.repository.BlockedUserRepository;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.repository.packaging.BlockedPackageAuditRepository;
import com.fitwise.repository.packaging.BlockedPackageRepository;
import com.fitwise.repository.packaging.SubscriptionPackageRepository;
import com.fitwise.repository.payments.stripe.StripeProductAndPackageMappingRepository;
import com.fitwise.repository.payments.stripe.billing.StripeProductAndProgramMappingRepository;
import com.fitwise.repository.product.FreeAccessProgramReposity;
import com.fitwise.service.SubscriptionService;
import com.fitwise.service.admin.AdminFreeAccessService;
import com.fitwise.service.messaging.MessagingService;
import com.fitwise.service.payment.stripe.StripeService;
import com.fitwise.service.payments.appleiap.IAPServerNotificationService;
import com.fitwise.utils.AppUtils;
import com.fitwise.utils.EmailContentUtil;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.mail.AsyncMailer;
import com.fitwise.view.ResponseModel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AdminBlockedService {

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
     * The program repository.
     */
    @Autowired
    private ProgramRepository programRepository;

    /**
     * The blocked programs' repository.
     */
    @Autowired
    private BlockedProgramsRepository blockedProgramsRepository;

    /**
     * The blocked programs audit repository.
     */
    @Autowired
    private BlockedProgramsAuditRepository blockedProgramsAuditRepository;

    /**
     * The blocked user repository.
     */
    @Autowired
    private BlockedUserRepository blockedUserRepository;

    /**
     * The blocked user audit repository.
     */
    @Autowired
    private BlockedUserAuditRepository blockedUserAuditRepository;

    @Autowired
    MessagingService messagingService;

    private final AsyncMailer asyncMailer;

    @Autowired
    BlockListeners blockListeners;
    @Autowired
    FitwiseUtils fitwiseUtils;
    @Autowired
    SubscriptionService subscriptionService;

    @Autowired
    IAPServerNotificationService iapServerNotificationService;
    @Autowired
    private EmailContentUtil emailContentUtil;
    @Autowired
    private StripeProductAndProgramMappingRepository stripeProductAndProgramMappingRepository;
    @Autowired
    private SubscriptionPackageRepository subscriptionPackageRepository;
    @Autowired
    private BlockedPackageRepository blockedPackageRepository;
    @Autowired
    private StripeProductAndPackageMappingRepository stripeProductAndPackageMappingRepository;
    @Autowired
    private BlockedPackageAuditRepository blockedPackageAuditRepository;
    @Autowired
    private StripeService stripeService;

    private final AdminFreeAccessService adminFreeAccessService;
    
    private final FreeAccessProgramReposity freeAccessProgramReposity;

    /**
     * blocks the user.
     *
     * @param userId
     * @param role   of the user
     * @return the response model
     * @throws ApplicationException the application exception
     */
    public ResponseModel blockUser(Long userId, String role) {
        log.info("Block user starts.");
        long apiStartTimeMillis = new Date().getTime();
        User currentUser = userComponents.getUser();
        boolean isAdmin = fitwiseUtils.isAdmin(currentUser);
        if (!isAdmin) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_ADMIN, MessageConstants.ERROR);
        }
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
        if (blockedUserRepository.existsByUserUserIdAndUserRoleName(userId, role)) {
            return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_ALL_READY_BLOCKED, null);
        }
        Optional<UserRole> userRoleOptional = AppUtils.getUserRoles(user).stream().filter(userRole -> userRole.getName().equalsIgnoreCase(role)).findAny();
        if (!userRoleOptional.isPresent()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_USER_NOT_FOUND_WITH_ROLE, null);
        }
        log.info("Basic validation and get current user and validate the user role : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        UserRole userRole = userRoleOptional.get();

        //Block all programs of instructor
        if (KeyConstants.KEY_INSTRUCTOR.equalsIgnoreCase(role)) {
            List<Programs> programsList = programRepository.findByOwnerUserIdAndStatus(user.getUserId(), KeyConstants.KEY_PUBLISH);
            for (Programs program : programsList) {
                blockProgram(program.getProgramId(), KeyConstants.KEY_INSTRUCTOR_BLOCK);
            }
            log.info("Block all programs of an instructor : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();

            //Sending mail to instructor about block
            String subject = EmailConstants.INSTRUCTOR_BLOCK_SUBJECT;
            String supportLink = EmailConstants.TRAINNR_SUPPORT_LINK.replace(EmailConstants.EMAIL_EMAIL_ADDRESS, user.getEmail());
            String mailBody = EmailConstants.INSTRUCTOR_BLOCK_CONTENT;
            String userName = fitwiseUtils.getUserFullName(user);
            mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON
                    .replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
                    .replace(EmailConstants.EMAIL_BODY, mailBody)
                    .replace(EmailConstants.EMAIL_SUPPORT_URL, supportLink);
            mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
            asyncMailer.sendHtmlMail(user.getEmail(), subject, mailBody);
            log.info("Sent mail to an instructor : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();

        } else if (KeyConstants.KEY_MEMBER.equalsIgnoreCase(role)) {
            //Setting autorenewal false for all subscriptions of member

            List<ProgramSubscription> programSubscriptions = subscriptionService.getPaidProgramSubscriptionsByAnUser(user.getUserId());
            log.info("Query to get program subscriptions : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            for (ProgramSubscription programSubscription : programSubscriptions) {
                if (programSubscription.isAutoRenewal()) {
                    if (programSubscription.getSubscribedViaPlatform() != null) {
                        if (programSubscription.getSubscribedViaPlatform().getPlatformTypeId() == 2) {
                            // Subscribed via Apple IAP
                            iapServerNotificationService.cancelSubscription(programSubscription.getProgram().getProgramId(), programSubscription.getSubscribedViaPlatform().getPlatformTypeId(), programSubscription.getUser());
                        } else {
                            // Subscribed via Authorize.net
                            subscriptionService.cancelRecurringProgramSubscription(programSubscription.getProgram().getProgramId(), programSubscription.getSubscribedViaPlatform().getPlatformTypeId(), programSubscription.getUser(), false);
                        }
                    }
                }
            }
            log.info("Set auto renewal false to all subscriptions : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();

            List<PackageSubscription> packageSubscriptions = subscriptionService.getPaidPackageSubscriptionsByAnUser(user.getUserId());
            for (PackageSubscription packageSubscription : packageSubscriptions) {
                if (packageSubscription.isAutoRenewal()) {
                    if (packageSubscription.getSubscribedViaPlatform() != null) {
                        if (packageSubscription.getSubscribedViaPlatform() != null) {
                            if (packageSubscription.getSubscribedViaPlatform().getPlatformTypeId() != 2) {
                                stripeService.cancelStripePackageSubscription(packageSubscription.getSubscriptionPackage().getSubscriptionPackageId(), packageSubscription.getSubscribedViaPlatform().getPlatformTypeId(), packageSubscription.getUser(), false);
                            }
                        }
                    }
                }
            }
            log.info("Cancelling stripe subscriptions : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();

            //Sending mail to instructor about block
            String subject = EmailConstants.MEMBER_BLOCK_SUBJECT;
            String supportLink = EmailConstants.TRAINNR_SUPPORT_LINK.replace(EmailConstants.EMAIL_EMAIL_ADDRESS, user.getEmail());
            String mailBody = EmailConstants.MEMBER_BLOCK_CONTENT;
            String userName = fitwiseUtils.getUserFullName(user);
            mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON
                    .replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
                    .replace(EmailConstants.EMAIL_BODY, mailBody)
                    .replace(EmailConstants.EMAIL_SUPPORT_URL, supportLink);
            mailBody = emailContentUtil.replaceMemberAppUrl(mailBody);
            asyncMailer.sendHtmlMail(user.getEmail(), subject, mailBody);
            log.info("Sent mail to member : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
        }

        //Block user conversations
        List<ChatConversation> conversationList;
        if (KeyConstants.KEY_INSTRUCTOR.equalsIgnoreCase(role)) {
            conversationList = messagingService.getInstructorConversations(userId);
        } else {
            conversationList = messagingService.getMemberConversations(userId);
        }
        for (ChatConversation conversation : conversationList) {
            messagingService.blockConversationByAdmin(conversation.getConversationId());
        }
        log.info("Block user conversation : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        BlockedUser blockedUser = new BlockedUser();
        blockedUser.setUser(user);
        blockedUser.setUserRole(userRole);
        blockedUser.setBlockedUserDate(new Date());
        blockedUser.setWhoBlocked(currentUser);
        blockedUserRepository.save(blockedUser);
        log.info("Query to save blocked user : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        BlockedUserAudit blockedUserAudit = new BlockedUserAudit();
        blockedUserAudit.setUser(user);
        blockedUserAudit.setUserRole(userRole);
        blockedUserAudit.setStatus(KeyConstants.KEY_BLOCK);
        blockedUserAudit.setHappenedDate(new Date());
        blockedUserAudit.setDoneBy(currentUser);
        blockedUserAuditRepository.save(blockedUserAudit);
        log.info("Query to save blocked user audit : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Block user ends.");

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_USER_BLOCKED, null);
    }


    /**
     * un block the user.
     *
     * @param userId
     * @param role   of the user
     * @return the response model
     * @throws ApplicationException the application exception
     */
    public ResponseModel unBlockUser(Long userId, String role) {
        User currentUser = userComponents.getUser();
        boolean isAdmin = fitwiseUtils.isAdmin(currentUser);
        if (!isAdmin) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_ADMIN, MessageConstants.ERROR);
        }

        if (!(KeyConstants.KEY_MEMBER.equalsIgnoreCase(role) || KeyConstants.KEY_INSTRUCTOR.equalsIgnoreCase(role))) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ROLE_INCORRECT, null);
        }
        if (userId == null || userId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_ID_NULL, MessageConstants.ERROR);
        }
        if (!blockedUserRepository.existsByUserUserIdAndUserRoleName(userId, role)) {
            return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_USER_NOT_BLOCKED, null);
        }
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_FOUND, MessageConstants.ERROR);
        }
        Optional<UserRole> userRoleOptional = AppUtils.getUserRoles(user).stream().filter(userRole -> userRole.getName().equalsIgnoreCase(role)).findAny();
        if (!userRoleOptional.isPresent()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_USER_NOT_FOUND_WITH_ROLE, null);
        }
        UserRole userRole = userRoleOptional.get();

        //Unblock all programs of instructor
        if (KeyConstants.KEY_INSTRUCTOR.equalsIgnoreCase(role)) {
            List<BlockedPrograms> blockedProgramList = blockedProgramsRepository.findByProgramOwnerUserIdAndBlockType(userId, KeyConstants.KEY_INSTRUCTOR_BLOCK);
            List<Long> programIdList = blockedProgramList.stream().map(blockedProgram -> blockedProgram.getProgram().getProgramId()).collect(Collectors.toList());

            long subscriptionCount = subscriptionService.getOverallActiveSubscriptionCountForProgramsList(programIdList);
            if (subscriptionCount > 0) {
                throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_CANT_UNBLOCK_INSTRUCTOR_SUBSCRIBED_PROGRAMS, MessageConstants.ERROR);
            }

            for (Long programId : programIdList) {
                unBlockProgram(programId, KeyConstants.KEY_INSTRUCTOR_BLOCK);
            }

            //Sending mail to instructor about block
            String subject = EmailConstants.INSTRUCTOR_UNBLOCK_SUBJECT;
            String mailBody = EmailConstants.INSTRUCTOR_UNBLOCK_CONTENT;
            String userName = fitwiseUtils.getUserFullName(user);
            mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, mailBody);
            mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
            asyncMailer.sendHtmlMail(user.getEmail(), subject, mailBody);
        } else if (KeyConstants.KEY_MEMBER.equalsIgnoreCase(role)) {

            String subject = EmailConstants.MEMBER_UNBLOCK_SUBJECT;
            String mailBody = EmailConstants.MEMBER_UNBLOCK_CONTENT;
            String userName = fitwiseUtils.getUserFullName(user);
            mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, mailBody);
            mailBody = emailContentUtil.replaceMemberAppUrl(mailBody);
            asyncMailer.sendHtmlMail(user.getEmail(), subject, mailBody);
        }

        //Unblock user conversations
        List<ChatConversation> conversationList;
        if (KeyConstants.KEY_INSTRUCTOR.equalsIgnoreCase(role)) {
            conversationList = messagingService.getInstructorConversations(userId);
        } else {
            conversationList = messagingService.getMemberConversations(userId);
        }
        for (ChatConversation conversation : conversationList) {
            messagingService.unblockConversationByAdmin(conversation.getConversationId());
        }

        blockedUserRepository.deleteByUserUserIdAndUserRole(user.getUserId(), userRole);

        BlockedUserAudit blockedUserAudit = new BlockedUserAudit();
        blockedUserAudit.setUser(user);
        blockedUserAudit.setUserRole(userRole);
        blockedUserAudit.setStatus(KeyConstants.KEY_UN_BLOCK);
        blockedUserAudit.setHappenedDate(new Date());
        blockedUserAudit.setDoneBy(currentUser);
        blockedUserAuditRepository.save(blockedUserAudit);

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_USER_UN_BLOCKED, null);
    }

    /**
     * blocks the program.
     *
     * @param programId
     * @return the response model
     * @throws ApplicationException the application exception
     */
    public ResponseModel blockProgram(Long programId, String blockType) {
        User currentUser = userComponents.getUser();
        boolean isAdmin = fitwiseUtils.isAdmin(currentUser);
        if (!isAdmin) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_ADMIN, MessageConstants.ERROR);
        }
        if (programId == null || programId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_NULL, MessageConstants.ERROR);
        }
        Programs program = programRepository.findByProgramId(programId);
        if (program == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, MessageConstants.ERROR);
        }
        BlockedPrograms blockedProgram = blockedProgramsRepository.findByProgramProgramIdAndBlockType(programId, blockType);
        if (blockedProgram != null) {
            throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_ALREADY_PROGRAM_BLOCKED, MessageConstants.ERROR);
        }
        if (!KeyConstants.KEY_PUBLISH.equals(program.getStatus())) {
            throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_PROGRAM_BLOCK_ONLY_PUBLISHED, MessageConstants.ERROR);
        }
        //invoking listeners for program block
        ProgramBlockEvent programBlockEvent = new ProgramBlockEvent();
        programBlockEvent.setProgramId(programId);
        programBlockEvent.setProgram(program);
        programBlockEvent.setBlockType(blockType);
        blockListeners.invokeProgramBlockListeners(Constants.LISTENER_BLOCK_OPERATION, programBlockEvent);
        program.setStatus(KeyConstants.KEY_BLOCK);
        programRepository.save(program);
        //Marking Stripe product mapping as inactive
        StripeProductAndProgramMapping stripeProductMapping = stripeProductAndProgramMappingRepository.findByProgramProgramIdAndIsActive(program.getProgramId(), true);
        if(stripeProductMapping != null){
            stripeProductMapping.setActive(false);
            stripeProductAndProgramMappingRepository.save(stripeProductMapping);
        }
        blockedProgram = new BlockedPrograms();
        blockedProgram.setProgram(program);
        blockedProgram.setProgramBlockedDate(new Date());
        blockedProgram.setWhoBlocked(currentUser);
        blockedProgram.setBlockType(blockType);
        blockedProgramsRepository.save(blockedProgram);
        BlockedProgramsAudit blockedProgramsAudit = new BlockedProgramsAudit();
        blockedProgramsAudit.setProgram(program);
        blockedProgramsAudit.setStatus(program.getStatus());
        blockedProgramsAudit.setHappenedDate(new Date());
        blockedProgramsAudit.setDoneBy(currentUser);
        blockedProgramsAudit.setBlockType(blockType);
        blockedProgramsAuditRepository.save(blockedProgramsAudit);
		try {
			boolean isFreeProgramExits = freeAccessProgramReposity.existsByProgramAndFreeProductTypeAndIsActive(program,
					DBConstants.FREE_ACCESS_TYPE_ALL, true);
			if (isFreeProgramExits) {
				adminFreeAccessService.removeFreeProgramsForAllUsers(Collections.singletonList(programId));
			}
		}catch (ApplicationException applicationException){
            log.info("Program not have free access");
        }
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PROGRAM_BLOCKED, null);
    }

    /**
     * un block the program.
     *
     * @param programId
     * @return the response model
     * @throws ApplicationException the application exception
     */
    public ResponseModel unBlockProgram(Long programId, String blockType) {
        User currentUser = userComponents.getUser();
        boolean isAdmin = fitwiseUtils.isAdmin(currentUser);
        if (!isAdmin) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_ADMIN, MessageConstants.ERROR);
        }
        if (programId == null || programId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_NULL, MessageConstants.ERROR);
        }
        Programs program = programRepository.findByProgramId(programId);
        if (program == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, MessageConstants.ERROR);
        }
        BlockedPrograms blockedProgram = blockedProgramsRepository.findByProgramProgramIdAndBlockType(programId, blockType);
        if (blockedProgram == null) {
            if (KeyConstants.KEY_PROGRAM_BLOCK.equals(blockType) && blockedProgramsRepository.existsByProgramProgramId(programId)) {
                throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_PROGRAM_NOT_BLOCKED_IN_PROGRAM_BLOCK_TYPE, MessageConstants.ERROR);
            } else {
                throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_PROGRAM_NOT_BLOCKED, MessageConstants.ERROR);
            }
        }

        //Can not unblock a program with active subscriptions
        long paidSubscriptionsCount = subscriptionService.getActiveSubscriptionCountOfProgram(programId);
        if (paidSubscriptionsCount > 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CANT_UNBLOCK_SUBSCRIBED_PROGRAM.replace(StringConstants.LITERAL_COUNT, String.valueOf(paidSubscriptionsCount)), MessageConstants.ERROR);
        }

        blockedProgramsRepository.delete(blockedProgram);

        if (!blockedProgramsRepository.existsByProgramProgramId(programId)) {
            program.setStatus(InstructorConstant.PLAN);
            programRepository.save(program);
        }

        BlockedProgramsAudit blockedProgramsAudit = new BlockedProgramsAudit();
        blockedProgramsAudit.setProgram(program);
        blockedProgramsAudit.setStatus(KeyConstants.KEY_UN_BLOCK);
        blockedProgramsAudit.setHappenedDate(new Date());
        blockedProgramsAudit.setDoneBy(currentUser);
        blockedProgramsAudit.setBlockType(blockType);
        blockedProgramsAuditRepository.save(blockedProgramsAudit);

        //invoking listeners for program block
        ProgramBlockEvent programBlockEvent = new ProgramBlockEvent();
        programBlockEvent.setProgramId(programId);
        programBlockEvent.setProgram(program);
        programBlockEvent.setBlockType(blockType);
        blockListeners.invokeProgramBlockListeners(Constants.LISTENER_UNBLOCK_OPERATION, programBlockEvent);

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PROGRAM_UN_BLOCKED, null);
    }


    /**
     * Method to block a subscriptionPackage
     * @param subscriptionPackageId
     * @param blockType
     */
    public void blockPackage(Long subscriptionPackageId, String blockType) {
        User currentUser = userComponents.getUser();
        boolean isAdmin = fitwiseUtils.isAdmin(currentUser);
        if (!isAdmin) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_ADMIN, MessageConstants.ERROR);
        }
        if (subscriptionPackageId == null || subscriptionPackageId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_ID_NULL, MessageConstants.ERROR);
        }
        SubscriptionPackage subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageId(subscriptionPackageId);
        if (subscriptionPackage == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_NOT_FOUND, MessageConstants.ERROR);
        }

        BlockedPackage blockedPackage = blockedPackageRepository.findBySubscriptionPackageSubscriptionPackageIdAndBlockType(subscriptionPackageId, blockType);
        if (blockedPackage != null) {
            throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_ALREADY_PACKAGE_BLOCKED, MessageConstants.ERROR);
        }

        if (!KeyConstants.KEY_PUBLISH.equals(subscriptionPackage.getStatus())) {
            throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_PACKAGE_BLOCK_ONLY_PUBLISHED, MessageConstants.ERROR);
        }

        //invoking listeners for package block
        PackageBlockEvent packageBlockEvent = new PackageBlockEvent();
        packageBlockEvent.setSubscriptionPackageId(subscriptionPackageId);
        packageBlockEvent.setSubscriptionPackage(subscriptionPackage);
        packageBlockEvent.setBlockType(blockType);
        blockListeners.invokePackageBlockListeners(Constants.LISTENER_BLOCK_OPERATION, packageBlockEvent);

        subscriptionPackage.setStatus(KeyConstants.KEY_BLOCK);
        subscriptionPackageRepository.save(subscriptionPackage);

        //Marking Stripe product mapping as inactive
        StripeProductAndPackageMapping stripeProductMapping = stripeProductAndPackageMappingRepository.findBySubscriptionPackageSubscriptionPackageIdAndIsActive(subscriptionPackageId, true);
        if (stripeProductMapping != null) {
            stripeProductMapping.setActive(false);
            stripeProductAndPackageMappingRepository.save(stripeProductMapping);
        }

        blockedPackage = new BlockedPackage();
        blockedPackage.setSubscriptionPackage(subscriptionPackage);
        blockedPackage.setBlockedDate(new Date());
        blockedPackage.setBlockedBy(currentUser);
        blockedPackage.setBlockType(blockType);
        blockedPackageRepository.save(blockedPackage);

        BlockedPackageAudit blockedPackageAudit = new BlockedPackageAudit();
        blockedPackageAudit.setSubscriptionPackage(subscriptionPackage);
        blockedPackageAudit.setStatus(subscriptionPackage.getStatus());
        blockedPackageAudit.setHappenedDate(new Date());
        blockedPackageAudit.setDoneBy(currentUser);
        blockedPackageAudit.setBlockType(blockType);
        blockedPackageAuditRepository.save(blockedPackageAudit);

    }

    /**
     * Method to unblock a Subscription Package
     * @param subscriptionPackageId
     * @param blockType
     */
    public void unblockPackage(Long subscriptionPackageId, String blockType) {

        User currentUser = userComponents.getUser();
        boolean isAdmin = fitwiseUtils.isAdmin(currentUser);
        if (!isAdmin) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_ADMIN, MessageConstants.ERROR);
        }
        if (subscriptionPackageId == null || subscriptionPackageId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_ID_NULL, MessageConstants.ERROR);
        }
        SubscriptionPackage subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageId(subscriptionPackageId);
        if (subscriptionPackage == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_NOT_FOUND, MessageConstants.ERROR);
        }

        BlockedPackage blockedPackage = blockedPackageRepository.findBySubscriptionPackageSubscriptionPackageIdAndBlockType(subscriptionPackageId, blockType);
        if (blockedPackage == null) {
            if (KeyConstants.KEY_PACKAGE_BLOCK.equals(blockType) && blockedPackageRepository.existsBySubscriptionPackageSubscriptionPackageId(subscriptionPackageId)) {
                throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_PACKAGE_NOT_BLOCKED_IN_PACKAGE_BLOCK_TYPE, MessageConstants.ERROR);
            } else {
                throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_PACKAGE_NOT_BLOCKED, MessageConstants.ERROR);
            }
        }

        //Can not unblock a package with active subscriptions
        long subscriptionCount = subscriptionService.getActiveSubscriptionCountForPackage(subscriptionPackageId);
        if (subscriptionCount > 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CANT_UNBLOCK_SUBSCRIBED_PROGRAM.replace(StringConstants.LITERAL_COUNT, String.valueOf(subscriptionCount)), MessageConstants.ERROR);
        }

        blockedPackageRepository.delete(blockedPackage);

        if (!blockedPackageRepository.existsBySubscriptionPackageSubscriptionPackageId(subscriptionPackageId)) {
            subscriptionPackage.setStatus(InstructorConstant.PLAN);
            subscriptionPackageRepository.save(subscriptionPackage);
        }

        BlockedPackageAudit blockedPackageAudit = new BlockedPackageAudit();
        blockedPackageAudit.setSubscriptionPackage(subscriptionPackage);
        blockedPackageAudit.setStatus(KeyConstants.KEY_UN_BLOCK);
        blockedPackageAudit.setHappenedDate(new Date());
        blockedPackageAudit.setDoneBy(currentUser);
        blockedPackageAudit.setBlockType(blockType);
        blockedPackageAuditRepository.save(blockedPackageAudit);

        //invoking listeners for package block
        PackageBlockEvent packageBlockEvent = new PackageBlockEvent();
        packageBlockEvent.setSubscriptionPackageId(subscriptionPackageId);
        packageBlockEvent.setSubscriptionPackage(subscriptionPackage);
        packageBlockEvent.setBlockType(blockType);
        blockListeners.invokePackageBlockListeners(Constants.LISTENER_UNBLOCK_OPERATION, packageBlockEvent);

    }
}
