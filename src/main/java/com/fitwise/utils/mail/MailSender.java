package com.fitwise.utils.mail;

import com.fitwise.properties.GeneralProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 * Created by Vignesh G on 18/02/21
 */
@Component
public class MailSender extends EmailUtils {

    @Autowired
    private GeneralProperties generalProperties;

    /**
     * Util to send mail as plain text
     * @param to
     * @param subject
     * @param text
     */
    public void sendTextMail(String to, String subject, String text) {
        if (generalProperties.getAllowFunctionalMail() != null && generalProperties.getAllowFunctionalMail().booleanValue()) {
            sendSimpleEmail(to, subject, text);
        }
    }

    /**
     * Util to send mail as plain text with attachments
     * @param to
     * @param cc
     * @param subject
     * @param text
     * @param attachmentFileNames
     */
    public void sendAttachmentMail(String to, String cc, String subject, String text, List<String> attachmentFileNames) {
        if (generalProperties.getAllowFunctionalMail() != null && generalProperties.getAllowFunctionalMail().booleanValue()) {
            sendEmailWithAttachment(to, cc, subject, text, attachmentFileNames);
        }
    }

    /**
     * Util to send remainder mail as plain text with attachments
     * @param to
     * @param cc
     * @param subject
     * @param text
     * @param attachmentFileNames
     */
    public void sendReminderAttachmentMail(String to, String cc, String subject, String text, List<String> attachmentFileNames) {
        if (generalProperties.getAllowReminderMail() != null && generalProperties.getAllowReminderMail().booleanValue()) {
            sendEmailWithAttachment(to, cc, subject, text, attachmentFileNames);
        }
    }

    /**
     * Util to send mail as HTML
     * @param to
     * @param subject
     * @param text
     */
    public void sendHtmlMail(String to, String subject, String text) {
        if (generalProperties.getAllowFunctionalMail() != null && generalProperties.getAllowFunctionalMail().booleanValue()) {
            sendEmailWithHTMLBody(to, subject, text);
        }
    }

    /**
     * Util to remainder send mail as HTML with attachments
     * @param to
     * @param subject
     * @param text
     */
    public void sendHtmlReminderMail(String to, String subject, String text) {
        if (generalProperties.getAllowReminderMail() != null && generalProperties.getAllowReminderMail().booleanValue()) {
            sendEmailWithHTMLBody(to, subject, text);
        }
    }

    /**
     * Util to send mail as HTML with attachments
     * @param to
     * @param cc
     * @param subject
     * @param text
     * @param attachmentFileNames
     */
    public void sendHtmlMailWithAttachment(String to, String cc, String subject, String text, List<String> attachmentFileNames) {
        if (generalProperties.getAllowFunctionalMail() != null && generalProperties.getAllowFunctionalMail().booleanValue()) {
            sendHtmlEmailWithAttachment(to, cc, subject, text, attachmentFileNames);
        }
    }


}
