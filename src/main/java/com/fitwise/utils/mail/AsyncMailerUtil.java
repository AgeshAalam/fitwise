package com.fitwise.utils.mail;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.EmailConstants;
import com.fitwise.entity.PackageAccessToken;
import com.fitwise.entity.User;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.subscription.PackageSubscription;
import com.fitwise.entity.subscription.ProgramSubscription;
import com.fitwise.listeners.block.packaging.PackageBlockEvent;
import com.fitwise.listeners.block.program.ProgramBlockEvent;
import com.fitwise.repository.PackageAccessTokenRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.service.SubscriptionService;
import com.fitwise.service.dynamiclink.DynamicLinkService;
import com.fitwise.utils.EmailContentUtil;
import com.fitwise.utils.FitwiseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/*
 * Created by Vignesh.G on 15/07/21
 */
@Component
@RequiredArgsConstructor
public class AsyncMailerUtil {

    private final DynamicLinkService dynamicLinkService;
    private final UserRepository userRepository;
    private final UserComponents userComponents;
    private final FitwiseUtils fitwiseUtils;
    private final EmailContentUtil emailContentUtil;
    private final MailSender mailSender;
    private final PackageAccessTokenRepository packageAccessTokenRepository;
    private final SubscriptionService subscriptionService;

    /**
     * @param emails
     * @param subscriptionPackage
     * @throws UnsupportedEncodingException
     */
    @Async("threadPoolTaskExecutor")
    public void triggerPackageInvite(List<String> emails, SubscriptionPackage subscriptionPackage) throws UnsupportedEncodingException {
        User currentUser = userComponents.getUser();

        for (String email : emails) {
            User user = userRepository.findByEmail(email);
            String subject = EmailConstants.MEMBER_PACKAGE_NOTIFY_SUBJECT.replace(EmailConstants.LITERAL_PACKAGE_NAME, subscriptionPackage.getTitle());
            String mailBody = subscriptionPackage.getClientMessage();
            String token = generatePackageAccessToken(email, user, subscriptionPackage);
            String memberPackage = EmailConstants.MEMBER_PACKAGE_LINK.replace(EmailConstants.LITERAL_APP_URL, dynamicLinkService.constructPackageLinkForMember(subscriptionPackage.getSubscriptionPackageId(), token, currentUser));
            String userName = fitwiseUtils.getUserFullName(user);
            if (user != null) {
                mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, mailBody)
                        .replace(EmailConstants.EMAIL_SUPPORT_URL, memberPackage);
            } else {
                mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + ",").replace(EmailConstants.EMAIL_BODY, mailBody)
                        .replace(EmailConstants.EMAIL_SUPPORT_URL, memberPackage);
            }
            mailBody = emailContentUtil.replaceMemberAppUrl(mailBody);
            mailSender.sendHtmlMail(email, subject, mailBody);
        }
    }

    /**
     * @param email
     * @param user
     * @param subscriptionPackage
     * @return
     * @throws UnsupportedEncodingException
     */
    private String generatePackageAccessToken(String email, User user, SubscriptionPackage subscriptionPackage) throws UnsupportedEncodingException {
        String token = UUID.randomUUID().toString();
        PackageAccessToken packageAccessToken = new PackageAccessToken();
        if (user != null) {
            packageAccessToken.setUser(user);
        }
        packageAccessToken.setAccessToken(token);
        packageAccessToken.setSubscriptionPackage(subscriptionPackage);
        packageAccessToken.setEmail(email);
        packageAccessTokenRepository.save(packageAccessToken);
        return Base64.getEncoder().encodeToString((subscriptionPackage.getSubscriptionPackageId() + "##@##" + token).getBytes("utf-8"));
    }

    /**
     * @param programBlockEvent
     */
    @Async("threadPoolTaskExecutor")
    public void triggerProgramBlockMail(ProgramBlockEvent programBlockEvent) {
        //Sending mail to the program's owner : instructors
        String subject = EmailConstants.PROGRAM_BLOCK_INSTRUCTOR_SUBJECT.replace(EmailConstants.LITERAL_PROGRAM_NAME, "'" + programBlockEvent.getProgram().getTitle() + "'");
        String supportLink = EmailConstants.TRAINNR_SUPPORT_LINK.replace(EmailConstants.EMAIL_EMAIL_ADDRESS, programBlockEvent.getProgram().getOwner().getEmail());
        String mailBody = EmailConstants.PROGRAM_BLOCK_INSTRUCTOR_CONTENT.replace(EmailConstants.LITERAL_PROGRAM_NAME, "<b>" + programBlockEvent.getProgram().getTitle() + "</b>");
        String userName = fitwiseUtils.getUserFullName(programBlockEvent.getProgram().getOwner());
        mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON
                .replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
                .replace(EmailConstants.EMAIL_BODY, mailBody)
                .replace(EmailConstants.EMAIL_SUPPORT_URL, supportLink);
        mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
        mailSender.sendHtmlMail(programBlockEvent.getProgram().getOwner().getEmail(), subject, mailBody);
    }

    /**
     * @param programBlockEvent
     */
    @Async("threadPoolTaskExecutor")
    public void triggerProgramUnblockMail(ProgramBlockEvent programBlockEvent) {
        //Sending mail to the program's owner : instructors
        String subject = EmailConstants.PROGRAM_UNBLOCK_INSTRUCTOR_SUBJECT.replace(EmailConstants.LITERAL_PROGRAM_NAME, "'" + programBlockEvent.getProgram().getTitle() + "'");
        String mailBody = EmailConstants.PROGRAM_UNBLOCK_INSTRUCTOR_CONTENT.replace(EmailConstants.LITERAL_PROGRAM_NAME, "<b>" + programBlockEvent.getProgram().getTitle() + "</b>");
        String userName = fitwiseUtils.getUserFullName(programBlockEvent.getProgram().getOwner());
        mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, mailBody);
        mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
        mailSender.sendHtmlMail(programBlockEvent.getProgram().getOwner().getEmail(), subject, mailBody);
    }

    /**
     * @param packageBlockEvent
     */
    @Async("threadPoolTaskExecutor")
    public void triggerPackageBlockMail(PackageBlockEvent packageBlockEvent) {
        //Sending mail to the Package's owner : instructors
        String subject = EmailConstants.PACKAGE_BLOCK_INSTRUCTOR_SUBJECT.replace(EmailConstants.LITERAL_PACKAGE_NAME, "'" + packageBlockEvent.getSubscriptionPackage().getTitle() + "'");
        String supportLink = EmailConstants.TRAINNR_SUPPORT_LINK.replace(EmailConstants.EMAIL_EMAIL_ADDRESS, packageBlockEvent.getSubscriptionPackage().getOwner().getEmail());
        String mailBody = EmailConstants.PACKAGE_BLOCK_INSTRUCTOR_CONTENT.replace(EmailConstants.LITERAL_PACKAGE_NAME, "<b>" + packageBlockEvent.getSubscriptionPackage().getTitle() + "</b>");
        String userName = fitwiseUtils.getUserFullName(packageBlockEvent.getSubscriptionPackage().getOwner());
        mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON
                .replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
                .replace(EmailConstants.EMAIL_BODY, mailBody)
                .replace(EmailConstants.EMAIL_SUPPORT_URL, supportLink);
        mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
        mailSender.sendHtmlMail(packageBlockEvent.getSubscriptionPackage().getOwner().getEmail(), subject, mailBody);

        //Sending mail to subscribers
        subject = EmailConstants.PACKAGE_BLOCK_MEMBER_SUBJECT.replace(EmailConstants.LITERAL_PACKAGE_NAME, "'" + packageBlockEvent.getSubscriptionPackage().getTitle() + "'");
        mailBody = EmailConstants.PACKAGE_BLOCK_MEMBER_CONTENT.replace(EmailConstants.LITERAL_PACKAGE_NAME, "<b>" + packageBlockEvent.getSubscriptionPackage().getTitle() + "</b>");

        List<PackageSubscription> packageSubscriptions = subscriptionService.getPaidSubscriptionsOfPackage(packageBlockEvent.getSubscriptionPackageId());
        for (PackageSubscription packageSubscription : packageSubscriptions) {
            String newMailBody = mailBody;
            userName = fitwiseUtils.getUserFullName(packageSubscription.getUser());
            newMailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, newMailBody);
            newMailBody = emailContentUtil.replaceMemberAppUrl(newMailBody);
            mailSender.sendHtmlMail(packageSubscription.getUser().getEmail(), subject, newMailBody);
        }
    }

    /**
     * @param packageBlockEvent
     */
    @Async("threadPoolTaskExecutor")
    public void triggerPackageUnblockMail(PackageBlockEvent packageBlockEvent) {
        //Sending mail to the Package's owner : instructors
        String subject = EmailConstants.PACKAGE_UNBLOCK_INSTRUCTOR_SUBJECT.replace(EmailConstants.LITERAL_PACKAGE_NAME, "'" + packageBlockEvent.getSubscriptionPackage().getTitle() + "'");
        String mailBody = EmailConstants.PACKAGE_UNBLOCK_INSTRUCTOR_CONTENT.replace(EmailConstants.LITERAL_PACKAGE_NAME, "<b>" + packageBlockEvent.getSubscriptionPackage().getTitle() + "</b>");
        String userName = fitwiseUtils.getUserFullName(packageBlockEvent.getSubscriptionPackage().getOwner());
        mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, mailBody);
        mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
        mailSender.sendHtmlMail(packageBlockEvent.getSubscriptionPackage().getOwner().getEmail(), subject, mailBody);
    }

    /**
     * @param programSubscriptionList
     */
    @Async("threadPoolTaskExecutor")
    public void triggerUnpublishProgramMail(List<ProgramSubscription> programSubscriptionList) {
        for (ProgramSubscription programSubscription : programSubscriptionList) {
            if (programSubscription.isAutoRenewal()) {
                String subject = EmailConstants.AUTORENEWAL_SUBJECT;
                String mailBody = EmailConstants.AUTORENEWAL_PROGRAM_CONTENT.replace(EmailConstants.LITERAL_PROGRAM_NAME, "<b>" + programSubscription.getProgram().getTitle() + "</b>");
                String userName = fitwiseUtils.getUserFullName(programSubscription.getUser());
                mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, mailBody);
                mailBody = emailContentUtil.replaceMemberAppUrl(mailBody);
                mailSender.sendHtmlMail(programSubscription.getUser().getEmail(), subject, mailBody);
            }
        }
    }

    /**
     * @param packageSubscriptions
     */
    @Async("threadPoolTaskExecutor")
    public void triggerUnpublishPackageMail(List<PackageSubscription> packageSubscriptions) {
        for (PackageSubscription packageSubscription : packageSubscriptions) {
            if (packageSubscription.isAutoRenewal()) {
                String subject = EmailConstants.AUTORENEWAL_SUBJECT;
                String mailBody = EmailConstants.AUTORENEWAL_PACKAGE_CONTENT.replace(EmailConstants.LITERAL_PACKAGE_NAME, "<b>" + packageSubscription.getSubscriptionPackage().getTitle() + "</b>");
                String userName = fitwiseUtils.getUserFullName(packageSubscription.getUser());
                mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, mailBody);
                mailBody = emailContentUtil.replaceMemberAppUrl(mailBody);
                mailSender.sendHtmlMail(packageSubscription.getUser().getEmail(), subject, mailBody);
            }
        }
    }

}