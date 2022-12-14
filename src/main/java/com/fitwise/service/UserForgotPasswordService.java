package com.fitwise.service;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;
import java.util.Date;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.EmailConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.utils.EmailContentUtil;
import com.fitwise.utils.mail.AsyncMailer;
import com.fitwise.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.entity.User;
import com.fitwise.entity.UserForgotPasswordToken;
import com.fitwise.exception.ApplicationException;
import com.fitwise.properties.GeneralProperties;
import com.fitwise.repository.UserForgotPasswordTokenRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.view.ResetPasswordview;

/**
 * The Class UserForgotPasswordService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserForgotPasswordService {

    /** The user repository. */
    @Autowired
    private UserRepository userRepository;

    /** The user forgot password token repository. */
    @Autowired
    private UserForgotPasswordTokenRepository userForgotPasswordTokenRepository;

    /** The general properties. */
    @Autowired
    private GeneralProperties generalProperties;

    /** The bcrypt passwd encoder. */
    @Autowired
    private BCryptPasswordEncoder bcryptPasswdEncoder;

    @Autowired
    private ValidationService validationService;
    @Autowired
    private UserComponents userComponents;
    @Autowired
    private EmailContentUtil emailContentUtil;
    private final AsyncMailer asyncMailer;

    /**
     * Generate forgot password.
     *
     * @param email the email
     * @return true, if successful
     * @throws ApplicationException the application exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    public boolean generateForgotPassword(final String email, Optional<String> role) throws ApplicationException, UnsupportedEncodingException {
        log.info("Generate forgot password starts.");
        long apiStartTimeMillis = new Date().getTime();
        User user = userRepository.findByEmail(email);
        if(user == null || user.getUserId() == null){
            throw new ApplicationException(Constants.NOT_FOUND, MessageConstants.MSG_EMAIL_NOT_FOUND, MessageConstants.ERROR);
        }

        String currentRole = KeyConstants.KEY_INSTRUCTOR;
        if (role.isPresent()) {
            currentRole = role.get();
            if (!(KeyConstants.KEY_MEMBER.equalsIgnoreCase(currentRole) || KeyConstants.KEY_INSTRUCTOR.equalsIgnoreCase(currentRole))) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ROLE_INCORRECT, null);
            }
        }
        log.info("Basic validations : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        boolean isInstructor = false;
        if (KeyConstants.KEY_INSTRUCTOR.equalsIgnoreCase(currentRole)) {
            isInstructor = true;
        }

        String token = UUID.randomUUID().toString();
        UserForgotPasswordToken userForgotPasswordToken = new UserForgotPasswordToken();
        userForgotPasswordToken.setUser(user);
        userForgotPasswordToken.setResetToken(token);
        userForgotPasswordTokenRepository.save(userForgotPasswordToken);
        log.info("Query to save user forgot password token in DB : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        String tokenString = Base64.getEncoder().encodeToString((user.getUserId() + "##@##" + token).getBytes("utf-8"));
        String baseUrl = isInstructor ? generalProperties.getInstructorBaseUrl() : generalProperties.getMemberBaseUrl();
        String url = baseUrl + "/changePassword?token=" + tokenString;

        String subject = "Reset Password";
        String mailBody = "Click here to reset your password : " + url;
        mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi ,").replace(EmailConstants.EMAIL_BODY, mailBody);
        mailBody = isInstructor ? emailContentUtil.replaceInstructorAppUrl(mailBody) : emailContentUtil.replaceMemberAppUrl(mailBody);
        asyncMailer.sendHtmlMail(user.getEmail(), subject, mailBody);
        log.info("QAsync mailer activated : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Generate forgot password ends.");

        return true;
    }

    /**
     * Validate reset password token.
     *
     * @param userId the user id
     * @param token the token
     * @return the user forgot password token
     * @throws ApplicationException the application exception
     */
    public UserForgotPasswordToken validateResetPasswordToken(Long userId, String token) {
        UserForgotPasswordToken passwordToken = userForgotPasswordTokenRepository.findByResetToken(token);
        if ((passwordToken == null) || (passwordToken.getUser().getUserId().longValue() != userId.longValue())) {
            throw new ApplicationException(Constants.NOT_FOUND, MessageConstants.MSG_TOKEN_EXPIRED, MessageConstants.ERROR);
        }
        Calendar cal = Calendar.getInstance();
        if (passwordToken.getExpiryDate() == null || (cal.getTime().getTime() - passwordToken.getExpiryDate().getTime()) <= 3600) {
            throw new ApplicationException(Constants.GONE, MessageConstants.MSG_TOKEN_EXPIRED, MessageConstants.ERROR);
        }
        return passwordToken;
    }

    /**
     * Reset user password.
     *
     * @param resetPasswordview the reset passwordview
     * @return true, if successful
     * @throws ApplicationException the application exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    public boolean resetUserPassword(ResetPasswordview resetPasswordview) throws UnsupportedEncodingException {
        if (ValidationUtils.isEmptyString(resetPasswordview.getPassword())){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SECRET_NULL,
                    Constants.RESPONSE_INVALID_DATA);
        }
        String tokenString = new String(Base64.getDecoder().decode(resetPasswordview.getToken()),"utf-8");
        String[] tokenSplit = tokenString.split("##@##");
        UserForgotPasswordToken passwordToken = validateResetPasswordToken(Long.parseLong(tokenSplit[0]), tokenSplit[1]);
        validationService.validatePassword(resetPasswordview.getPassword());
        User user = userRepository.findByUserId(Long.parseLong(tokenSplit[0]));
        user.setPassword(bcryptPasswdEncoder.encode(resetPasswordview.getPassword()));
        user.setEnteredFitwisePassword(true);
        userRepository.save(user);
        passwordToken.setResetToken(null);
        userForgotPasswordTokenRepository.save(passwordToken);
        return true;
    }
}
