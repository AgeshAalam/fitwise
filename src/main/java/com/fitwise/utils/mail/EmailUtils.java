package com.fitwise.utils.mail;

import com.fitwise.properties.GeneralProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;

@Component
@Slf4j
public class EmailUtils {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private GeneralProperties generalProperties;

    protected void sendSimpleEmail(String to, String subject, String text) {
        try{
            log.info("Sending email");
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
        }catch (Exception exception){
            log.debug("Email send issue : " + exception.getMessage());
        }
    }

    /**
     * Sending email with attachment
     * @param to
     * @param cc
     * @param subject
     * @param text
     * @param attachmentFileNames
     */
    protected void sendEmailWithAttachment(String to,String cc, String subject, String text, List<String> attachmentFileNames) {
        MimeMessage message = emailSender.createMimeMessage();
        try {
            if (to == null || to.isEmpty()) {
                throw new Exception("Mail Recipient Address empty");
            }
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            if (to.contains(",")) {
                String toAddrs[] = to.trim().split(",");
                helper.setTo(toAddrs);
            } else {
                helper.setTo(to);
            }
            if (cc != null && !cc.isEmpty()) {
                if (cc.contains(",")) {
                    String ccAddrs[] = cc.trim().split(",");
                    helper.setCc(ccAddrs);
                } else {
                    helper.setCc(cc);
                }
            }
            helper.setSubject(subject);
            helper.setText(text);
            if (attachmentFileNames != null && !attachmentFileNames.isEmpty()) {
                FileSystemResource file = null;
                for (String fileName : attachmentFileNames) {
                    file = new FileSystemResource(fileName);
                    helper.addAttachment(file.getFilename(), file);
                }
            }
            message.setFrom(new InternetAddress(generalProperties.getFromEmailAddress(),generalProperties.getSenderTitle()));
            emailSender.send(message);
        } catch (Exception e) {
            log.debug("Email send issue : " + e.getMessage());
        }
    }

    /**
     * Send the email with HTML body
     * @param to
     * @param subject
     * @param text
     */
    protected void sendEmailWithHTMLBody(String to, String subject, String text) {
        MimeMessage message = emailSender.createMimeMessage();
        try {
            if (to == null || to.isEmpty()) {
                throw new Exception("Mail Recipient Address empty");
            }
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            if (to.contains(",")) {
                String toAddrs[] = to.trim().split(",");
                helper.setTo(toAddrs);
            } else {
                helper.setTo(to);
            }
            helper.setSubject(subject);
            helper.setText(text, true);
            message.setFrom(new InternetAddress(generalProperties.getFromEmailAddress(),generalProperties.getSenderTitle()));
            emailSender.send(message);
        } catch (Exception exception) {
            log.info(exception.getMessage());
        }
    }

    /**
     * Util to send mail with HTML body and attachments
     * @param to
     * @param cc
     * @param subject
     * @param text
     * @param attachmentFileNames
     */
    protected void sendHtmlEmailWithAttachment(String to, String cc, String subject, String text, List<String> attachmentFileNames) {
        MimeMessage message = emailSender.createMimeMessage();
        try {
            if (to == null || to.isEmpty()) {
                throw new Exception("Mail Recipient Address empty");
            }
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            if (to.contains(",")) {
                String toAddrs[] = to.trim().split(",");
                helper.setTo(toAddrs);
            } else {
                helper.setTo(to);
            }
            if (cc != null && !cc.isEmpty()) {
                if (cc.contains(",")) {
                    String ccAddrs[] = cc.trim().split(",");
                    helper.setCc(ccAddrs);
                } else {
                    helper.setCc(cc);
                }
            }
            helper.setSubject(subject);
            helper.setText(text, true);
            if (attachmentFileNames != null && !attachmentFileNames.isEmpty()) {
                FileSystemResource file = null;
                for (String fileName : attachmentFileNames) {
                    file = new FileSystemResource(fileName);
                    helper.addAttachment(file.getFilename(), file);
                }
            }
            message.setFrom(new InternetAddress(generalProperties.getFromEmailAddress(),generalProperties.getSenderTitle()));
            emailSender.send(message);
        } catch (Exception e) {
            log.debug("Email send issue : " + e.getMessage());
        }
    }

}
