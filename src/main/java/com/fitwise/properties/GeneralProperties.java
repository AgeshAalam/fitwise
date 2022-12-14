package com.fitwise.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The Class GeneralProperties.
 */
@Component
@Getter
public class GeneralProperties {

    /**
     * The otp expiry in seconds.
     */
    @Value("${otp.expiry-seconds}")
    private int otpExpiryInSeconds;

    /**
     * The application url.
     */
    @Value("${application.url}")
    private String applicationUrl;

    @Value("${instructor.base.url}")
    private String instructorBaseUrl;

    @Value("${member.base.url}")
    private String memberBaseUrl;

    @Value("${signup.tracking.mail.to}")
    private String signUpTrackingMailToAddr;

    @Value("${signup.tracking.mail.cc}")
    private String signUpTrackingMailCcAddr;

    @Value("${capcha.site.key}")
    private String capchaSiteKey;

    @Value("${capcha.secret.key}")
    private String capchaSecretKey;

    @Value("${capcha.verify.url}")
    private String capchaVerifyUrl;

    /**
     * Property for QBO
     */
    @Value("${qbo.auth.expiry.notification.to}")
    private String qboAuthExpiryNotificationToEmailAddresses;

    @Value("${report.sample.pdf}")
    private String samplePdfPath;

    @Value("${qbo.account.bank.name}")
    private String bankAccountName;

    @Value("${app.test.bot.emails}")
    private String testBotEmailDomains;

    @Value("${bulk.upload.sample.csv.url}")
    private String bulkUploadSampleCsvUrl;

    @Value("${bulk.invite.sample.csv.url}")
    private String bulkInviteSampleCsvUrl;

    @Value("${qbo.nofdays.sync.payment}")
    private String nofDaysToSyncAnetSettlement;

    /**
     * Property for Super admin emails
     */
    @Value("${super.admin.email.addresses}")
    private String superAdminEmailAddresses;

    /**
     * Email properties
     */
    @Value("${functional.mail.allow}")
    private Boolean allowFunctionalMail;

    @Value("${reminder.mail.allow}")
    private Boolean allowReminderMail;

    @Value("${spring.mail.username}")
    private String fromEmailAddress;

    @Value("${email.sender.title}")
    private String senderTitle;

}
