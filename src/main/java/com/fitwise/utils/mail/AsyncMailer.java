package com.fitwise.utils.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 * Created by Vignesh.G on 15/07/21
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AsyncMailer {

    private final MailSender mailSender;

    @Async("threadPoolTaskExecutor")
    public void sendAsyncTextMail(String to, String subject, String text) {
        mailSender.sendTextMail(to, subject, text);
    }

    @Async("threadPoolTaskExecutor")
    public void sendAttachmentMail(String to, String cc, String subject, String text, List<String> attachmentFileNames) {
        mailSender.sendAttachmentMail(to, cc, subject, text, attachmentFileNames);
    }

    @Async("threadPoolTaskExecutor")
    public void sendReminderAttachmentMail(String to, String cc, String subject, String text, List<String> attachmentFileNames) {
        mailSender.sendReminderAttachmentMail(to, cc, subject, text, attachmentFileNames);
    }

    @Async("threadPoolTaskExecutor")
    public void sendHtmlMail(String to, String subject, String text) {
        mailSender.sendHtmlMail(to, subject, text);
    }

    @Async("threadPoolTaskExecutor")
    public void sendHtmlReminderMail(String to, String subject, String text) {
        mailSender.sendHtmlReminderMail(to, subject, text);
    }

    @Async("threadPoolTaskExecutor")
    public void sendHtmlMailWithAttachment(String to, String cc, String subject, String text, List<String> attachmentFileNames) {
        mailSender.sendHtmlMailWithAttachment(to, cc, subject, text, attachmentFileNames);
    }

}
