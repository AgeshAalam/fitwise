package com.fitwise.service;

import com.ecwid.maleorang.MailchimpException;
import com.fitwise.constants.Constants;
import com.fitwise.constants.EmailConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SecurityFilterConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.NotifyEmail;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.NotifyEmailRepository;
import com.fitwise.service.mailchimp.MailchimpService;
import com.fitwise.utils.RecaptchaUtil;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.utils.mail.AsyncMailer;
import com.fitwise.view.NotifyMeView;
import com.fitwise.view.ResponseModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class NotifyEmailService {

    @Autowired
    private NotifyEmailRepository notifyEmailRepository;

    @Autowired
    private RecaptchaUtil recaptchaUtil;
    
    @Autowired
    private MailchimpService mailchimpService;

    private final AsyncMailer asyncMailer;

    public ResponseModel saveNotifyEmail(final NotifyMeView notifyMeView, final HttpServletRequest request) throws IOException, MailchimpException{
        String remoteIp = request.getRemoteAddr();
        String capchaReponse = recaptchaUtil.validateCapcha(notifyMeView.getCapchaResponse(), remoteIp);
        if(!capchaReponse.isEmpty()){
            throw new ApplicationException(Constants.CAPCHA_FAILURE, "Capcha failure. " + capchaReponse, null);
        }
        ResponseModel responseModel;
        if(ValidationUtils.isEmptyString(notifyMeView.getEmail())){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EMAIL_EMPTY, null);
        }else if(!ValidationUtils.emailRegexValidate(notifyMeView.getEmail())){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EMAIL_ERROR, null);
        } else if(ValidationUtils.isEmptyString(notifyMeView.getRole())){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ROLE_NULL, null);
        }else if((notifyMeView.getRole().equalsIgnoreCase(SecurityFilterConstants.ROLE_INSTRUCTOR) || notifyMeView.getRole().equalsIgnoreCase(SecurityFilterConstants.ROLE_MEMBER))){
            NotifyEmail notifyEmail = notifyEmailRepository.findByEmailAndRole(notifyMeView.getEmail(), notifyMeView.getRole());
            if(notifyEmail != null){
                if(notifyMeView.getRole().equalsIgnoreCase(SecurityFilterConstants.ROLE_INSTRUCTOR)){
                    responseModel = new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_NOTIFY_INSTRUCTOR_EXIST, null);
                }else{
                    responseModel = new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_NOTIFY_MEMBER_EXIST, null);
                }
            } else {
                notifyEmail = new NotifyEmail();
                notifyEmail.setEmail(notifyMeView.getEmail());
                notifyEmail.setRole(notifyMeView.getRole());
                notifyEmailRepository.save(notifyEmail);
                mailchimpService.createEmailList(notifyMeView.getEmail(),notifyMeView.getRole());
                String emailBody = "";
                if(notifyMeView.getRole().equalsIgnoreCase(SecurityFilterConstants.ROLE_INSTRUCTOR)){
                    emailBody = EmailConstants.BODY_HTML_NOTIFY_INSTRUCTOR;
                    responseModel = new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_NOTIFY_INSTRUCTOR, null);
                }else{
                    emailBody = EmailConstants.BODY_HTML_NOTIFY_MEMBER;
                    responseModel = new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_NOTIFY_MEMBER, null);
                }
                emailBody = emailBody.replace("#FIRSTNAME#", "");
                asyncMailer.sendHtmlMail(notifyEmail.getEmail(), EmailConstants.SUBJECT_NOTIFY, emailBody);
            }
        }else {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ROLE_INCORRECT, null);
        }
        return responseModel;
    }
}
