package com.fitwise.configuration;

import com.fitwise.authentication.AuthenticationProvider;
import com.fitwise.authentication.AuthenticationProviderForToken;
import com.fitwise.authentication.SecurityFilter;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SecurityFilterConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The Class WebSecurityConfig.
 */
@Configuration
@EnableWebSecurity
@Slf4j
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {


    /**
     * The auth provider for username password.
     */
    @Autowired
    private AuthenticationProvider authProviderForUsernamePassword;

    /**
     * The authentication provider for token.
     */
    @Autowired
    private AuthenticationProviderForToken authenticationProviderForToken;

    /**
     * B crypt password encoder.
     *
     * @return the b crypt password encoder
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configure.
     *
     * @param auth the auth
     * @throws Exception the exception
     */
    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProviderForUsernamePassword)
                .authenticationProvider(authenticationProviderForToken);
    }

    /**
     * Configure.
     *
     * @param http the http
     * @throws Exception the exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.addFilterBefore(new SecurityFilter(authenticationManager()), BasicAuthenticationFilter.class);
        setAuthorities(http);
        http.authorizeRequests().antMatchers(HttpMethod.OPTIONS, "/**").permitAll();
        http.authorizeRequests().antMatchers("/resources/**", "/swagger-ui.html", "/favicon**",
                        "/v1/social/validateFbUserProfileId", "/v1/social/validateAndLoginUsingUserEnteredEmail",
                        "/v1/social/validateAppleLogin", "/v1/social/**", "/v1/user/register", "/v1/user/login",
                        "/v1/user/forgotPassword", "/v1/user/validateEmail"
                , "/v1/user/authenticateAndAddNewRole", "/v1/user/generateOtp", "/v1/user/savePasswordAndNewRole",
                "/v1/user/validateOtp", "/v1/notification/**", "/v1/user/common/notifyMe", "/v1/zenDesk/zenDeskTicketWebHook",
                "/v1/payment/initiateOneTimePaymentTransactionByCard", "/v1/payment/initiateRecurringProgramSubscription",
                "/v1/payment/cancelRecurringSubscription", "/v1/payment/getWebHookDataFromAuthNet",
                "/v1/payment/getANetCustomerProfile", "/v1/payment/initiateOneTimePaymentTransactionByPaymentProfile",
                "/v1/payment/initiateRecurringSubscriptionByCard", "/v1/payment/initiateRecurringSubscriptionByPaymentProfile",
                "/v1/payment/iap/uploadmetadata", "/v1/payment/iap/notification/validateReceipt", "/v1/payment/iap/notification/initialPurchaseNotification",
                "/v1/payment/iap/notification/getNotificationfromAppstore", "/v1/member/program/all", "/v1/member/program/all/filter", "/v1/member/program/all/byType", "/v1/member/program/trending/filter", "/v1/member/program/trending", "v1/member/getInstructors", "/v1/member/instructorFilter", "/v1/member/getInstructorProfileForMember",
                "/v1/member/program/programsbyTypeFilter", "/v1/member/program/getProgramsByType", "/v1/member/program/getProgramDetails", "/v1/payment/iap/unPublishMetadata",
                "/qbo/**", "/v1/user/validateEmailForSocialLogin", "/v1/onboard/getAppConfigData", "/v1/zenDesk/createZenDeskTicketFromEmail", "/v1/order/getOrderReceipt","/v1/program/programPromoCompletionStatus",
                "/v1/payment/iap/notification/redirectNotificationfromAppstore","/v1/discounts/saveOfferCode","/v1/discounts/getAllOfferCodes","/v1/discounts/getOfferCodeDetail",
                "/v1/discounts/RemoveOfferCode","/v1/discounts/getAllOfferDuration","/v1/discounts/getPriceList","/v1/discounts/generateOfferCode","/v1/discounts/validateOfferName","/v1/discounts/updateOfferCode","/v1/discounts/validateOfferCode","/v1/discounts/getProgramOffers", "/v1/payment/stripe/notification", "/v1/member/package/getSubscriptionPackages", "/v1/member/package/getPackageFilters","/v1/member/package/getSubscriptionPackage",
                "/v1/cal/instructor/zoom/deauth","/fw/v2/user/login", "/v1/cal/webhook", "/v1/member/package/getInstructorPackages", "/fw/v2/member/instructors", "/v1/onboard/getProgramTypes", "/fw/v2/member/programs/trending", "/fw/v2/member/program", "/actuator/**").permitAll().
                and().exceptionHandling().accessDeniedHandler(new AccessDeniedHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
                log.info(MessageConstants.MSG_ERR_EXCEPTION + accessDeniedException.getMessage());
                response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.getLocalizedMessage());
            }
        });
    }

    /**
     * Sets the authorities.
     *
     * @param http the new authorities
     * @throws Exception the exception
     */
    private void setAuthorities(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/v1/member**")
                .hasAnyAuthority(SecurityFilterConstants.ROLE_MEMBER);
        http.authorizeRequests().antMatchers("/v1/program**", "/v1/instructor**")
                .hasAnyAuthority(SecurityFilterConstants.ROLE_INSTRUCTOR);
        http.authorizeRequests().antMatchers("/v1/admin**")
                .hasAnyAuthority(SecurityFilterConstants.ROLE_ADMIN);
        http.authorizeRequests().antMatchers("/v1/user/common**")
                .hasAnyAuthority(SecurityFilterConstants.ROLE_MEMBER, SecurityFilterConstants.ROLE_INSTRUCTOR);
    }

    /**
     * Custom authentication manager.
     *
     * @return the authentication manager
     * @throws Exception the exception
     */
    @Bean
    public AuthenticationManager customAuthenticationManager() throws Exception {
        return authenticationManager();
    }

}
