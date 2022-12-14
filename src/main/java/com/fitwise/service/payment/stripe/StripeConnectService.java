package com.fitwise.service.payment.stripe;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.constants.SecurityFilterConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.StripeConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.constants.payments.appleiap.NotificationConstants;
import com.fitwise.entity.InstructorPayment;
import com.fitwise.entity.PlatformType;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.UserRoleMapping;
import com.fitwise.entity.payments.appleiap.ApplePayment;
import com.fitwise.entity.payments.authNet.AuthNetPayment;
import com.fitwise.entity.payments.authNet.Countries;
import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.payments.credits.InstructorPaymentCreditAudit;
import com.fitwise.entity.payments.credits.InstructorPaymentCreditHistory;
import com.fitwise.entity.payments.credits.InstructorPaymentCredits;
import com.fitwise.entity.payments.paypal.UserAccountAndPayPalIdMapping;
import com.fitwise.entity.payments.stripe.billing.StripePayment;
import com.fitwise.entity.payments.stripe.connect.AppleSettlementByStripe;
import com.fitwise.entity.payments.stripe.connect.StripeAccountAndUserMapping;
import com.fitwise.entity.payments.stripe.connect.StripeReverseTransferErrorLog;
import com.fitwise.entity.payments.stripe.connect.StripeTransferAndReversalMapping;
import com.fitwise.entity.payments.stripe.connect.StripeTransferErrorLog;
import com.fitwise.entity.payments.stripe.settings.PayoutModeSettings;
import com.fitwise.exception.ApplicationException;
import com.fitwise.properties.GeneralProperties;
import com.fitwise.properties.StripeProperties;
import com.fitwise.repository.PlatformTypeRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.repository.order.InstructorPaymentRepository;
import com.fitwise.repository.payments.appleiap.ApplePaymentRepository;
import com.fitwise.repository.payments.authnet.AuthNetPaymentRepository;
import com.fitwise.repository.payments.authnet.CountriesRepository;
import com.fitwise.repository.payments.credits.InstructorPaymentCreditAuditRepository;
import com.fitwise.repository.payments.credits.InstructorPaymentCreditHistoryRepository;
import com.fitwise.repository.payments.credits.InstructorPaymentsCreditsRepository;
import com.fitwise.repository.payments.stripe.billing.StripePaymentRepository;
import com.fitwise.repository.payments.stripe.connect.AppleSettlementByStripeRepository;
import com.fitwise.repository.payments.stripe.connect.StripeAccountAndUserMappingRepository;
import com.fitwise.repository.payments.stripe.connect.StripeReverseTransferErrorLogRepository;
import com.fitwise.repository.payments.stripe.connect.StripeTransferAndReversalMappingRepository;
import com.fitwise.repository.payments.stripe.connect.StripeTransferErrorLogRepository;
import com.fitwise.repository.payments.stripe.paypal.UserAccountAndPayPalIdMappingRepository;
import com.fitwise.repository.payments.stripe.settings.PayoutModeSettingsRepository;
import com.fitwise.service.instructor.InstructorAnalyticsService;
import com.fitwise.service.qbo.FitwiseQboEntityService;
import com.fitwise.specifications.InstructorPaymentSpecifications;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.mail.AsyncMailer;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.payment.paypal.PayPalTransactionRequestView;
import com.fitwise.view.payment.stripe.admin.FailedTopUpPayout;
import com.fitwise.view.payment.stripe.admin.PayoutDetailsResponseView;
import com.fitwise.view.payment.stripe.admin.PayoutsTileResponseView;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.Balance;
import com.stripe.model.LoginLink;
import com.stripe.model.Topup;
import com.stripe.model.Transfer;
import com.stripe.model.TransferReversal;
import com.stripe.net.RequestOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StripeConnectService {

    @Autowired
    private StripeProperties stripeProperties;

    @Autowired
    private UserComponents userComponents;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StripeAccountAndUserMappingRepository stripeAccountAndUserMappingRepository;

    @Autowired
    private GeneralProperties generalProperties;

    @Autowired
    private UserAccountAndPayPalIdMappingRepository userAccountAndPayPalIdMappingRepository;

    @Autowired
    StripeTransferAndReversalMappingRepository stripeTransferAndReversalMappingRepository;

    @Autowired
    private StripeReverseTransferErrorLogRepository stripeReverseTransferErrorLogRepository;

    @Autowired
    private InstructorPaymentRepository instructorPaymentRepository;

    @Autowired
    private StripePaymentRepository stripePaymentRepository;

    @Autowired
    private CountriesRepository countriesRepository;

    @Autowired
    private StripeTransferErrorLogRepository stripeTransferErrorLogRepository;

    @Autowired
    private InstructorPaymentsCreditsRepository instructorPaymentsCreditsRepository;

    @Autowired
    private InstructorPaymentCreditAuditRepository instructorPaymentCreditAuditRepository;

    @Autowired
    private InstructorPaymentCreditHistoryRepository instructorPaymentCreditHistoryRepository;

    @Autowired
    private AppleSettlementByStripeRepository appleSettlementByStripeRepository;

    @Autowired
    private FitwiseUtils fitwiseUtils;

    @Autowired
    private PlatformTypeRepository platformTypeRepository;

    @Autowired
    AuthNetPaymentRepository authNetPaymentRepository;

    @Autowired
    ApplePaymentRepository applePaymentRepository;

    @Autowired
    private FitwiseQboEntityService fitwiseQboEntityService;

    @Autowired
    private InstructorAnalyticsService instructorAnalyticsService;

    @Autowired
    private PayoutModeSettingsRepository payoutModeSettingsRepository;

    @Autowired
    private AsyncMailer asyncMailer;

    /**
     * Used to create a stripe connect account for the instructor
     *
     * @return
     */
    @Transactional
    public ResponseModel createAccount(String countryCode) {
        log.info("Create account starts.");
        long apiStartTimeMillis = new Date().getTime();
        User instructor = userComponents.getUser();
        StripeAccountAndUserMapping accountAndUserMapping = stripeAccountAndUserMappingRepository.findByUserUserId(instructor.getUserId());
        Account account;
        log.info("Get user and AccountAndUserMapping : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();

        if (accountAndUserMapping == null) {
            Stripe.apiKey = stripeProperties.getApiKey();

            Map<String, Object> params = new HashMap<>();
            params.put("type", "express");
            params.put("country", countryCode);
            params.put(StripeConstants.STRIPE_PROP_EMAIL, instructor.getEmail());

            try {
                account = Account.create(params);
                log.info("Create account in stripe : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                profilingEndTimeMillis = new Date().getTime();
                StripeAccountAndUserMapping stripeAccountAndUserMapping = new StripeAccountAndUserMapping();
                stripeAccountAndUserMapping.setStripeAccountId(account.getId());
                stripeAccountAndUserMapping.setUser(userComponents.getUser());
                stripeAccountAndUserMapping.setIsOnBoardingCompleted(false);
                stripeAccountAndUserMappingRepository.save(stripeAccountAndUserMapping);
                log.info("Query: save stripe account and user mapping : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                profilingEndTimeMillis = new Date().getTime();
            } catch (StripeException e) {
                log.error("Error creating Stripe connect Account ------>" + e.getMessage());
                throw new ApplicationException(Constants.ERROR_STATUS, e.getMessage(), e.getMessage());
            }
        } else {
            String accountId = accountAndUserMapping.getStripeAccountId();
            if (accountAndUserMapping.getIsOnBoardingCompleted()) {
                throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_ONBOARDING_ALREADY_COMPLETED, null);
            }
            try {
                account = Account.retrieve(accountId);
                log.info("Retrieve account from stripe : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                profilingEndTimeMillis = new Date().getTime();
            } catch (StripeException e) {
                log.error(e.getMessage());
                throw new ApplicationException(Constants.ERROR_STATUS, e.getMessage(), e.getMessage());
            }
        }
        ResponseModel responseModel = createAccountLinks(account);
        log.info("Create account links : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Create account ends.");
        return responseModel;
    }


    /**
     * To create account links in Stripe connect
     *
     * @param account
     * @return
     */
    public ResponseModel createAccountLinks(Account account) {
        Stripe.apiKey = stripeProperties.getApiKey();
        String appUrl = generalProperties.getApplicationUrl();

        StripeAccountAndUserMapping stripeAccountAndUserMapping = stripeAccountAndUserMappingRepository.findByStripeAccountId(account.getId());
        if (stripeAccountAndUserMapping != null && stripeAccountAndUserMapping.getIsOnBoardingCompleted()) {
            throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_ONBOARDING_ALREADY_COMPLETED, null);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("account", account.getId());
        params.put("refresh_url", appUrl + "/app/settings/integration/refresh");
        params.put("return_url", appUrl + "/app/settings/integration/return");
        params.put("type", "account_onboarding");

        String onBoardingUrl;
        try {
            AccountLink accountLink = AccountLink.create(params);
            onBoardingUrl = accountLink.getUrl();
        } catch (StripeException e) {
            log.error("Error creating Stripe connect Account Link------>" + e.getMessage());
            throw new ApplicationException(Constants.ERROR_STATUS, e.getMessage(), e.getMessage());
        }
        Map<String, Object> map = new HashMap<>();
        map.put(KeyConstants.KEY_ONBOARDING_URL, onBoardingUrl);

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_ACCOUNT_LINK_CREATED, map);
    }

    /**
     * When the user is partially or fully on-boarded, the below method is called
     *
     * @return
     */
    @Transactional
    public ResponseModel returnCallFromStripe() {
        log.info("---------------STRIPE CONNECTED ACCOUNT - RETURN API CALL CAME FROM CLIENT--------------------");
        User user = userComponents.getUser();
        StripeAccountAndUserMapping stripeAccountAndUserMapping = stripeAccountAndUserMappingRepository.findByUserUserId(user.getUserId());
        if (stripeAccountAndUserMapping == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ACCOUNT_NOT_CREATED_YET, null);
        }

        String accountId = stripeAccountAndUserMapping.getStripeAccountId();

        Stripe.apiKey = stripeProperties.getApiKey();
        try {
            Account account = Account.retrieve(accountId);
            if (account != null) {
                if (account.getDetailsSubmitted()) {
                    log.info("---------------STRIPE CONNECTED ACCOUNT - DETAILS SUBMITTED--------------------");
                    stripeAccountAndUserMapping.setIsDetailsSubmitted(true);
                    stripeAccountAndUserMappingRepository.save(stripeAccountAndUserMapping);
                }

                if (account.getChargesEnabled() && account.getPayoutsEnabled()) {
                    log.info("---------------STRIPE CONNECTED ACCOUNT - CHARGES AND PAYOUTS ENABLED--------------------");
                    stripeAccountAndUserMapping.setIsOnBoardingCompleted(true);
                    stripeAccountAndUserMappingRepository.save(stripeAccountAndUserMapping);
                }
            }
        } catch (StripeException e) {
            log.error("Error fetching Stripe connect account information " + e.getCode() + KeyConstants.KEY_SPACE + e.getMessage());
            throw new ApplicationException(Constants.ERROR_STATUS, e.getMessage(), e.getMessage());
        }

        boolean isAccountCreated = false;
        boolean isOnBoarded = false;
        if (!stripeAccountAndUserMapping.getStripeAccountId().isEmpty()) {
            isAccountCreated = true;
        }
        if (stripeAccountAndUserMapping.getIsOnBoardingCompleted()) {
            isOnBoarded = true;
        }

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(KeyConstants.KEY_IS_ONBOARDED, isOnBoarded);
        dataMap.put(KeyConstants.KEY_IS_ACCOUNT_CREATED, isAccountCreated);

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_RETURN_URL_ACCESSED, dataMap);
    }


    /**
     * Gets called from FE when the Stripe account link is idle for some time and gets expired
     *
     * @return
     */
    @Transactional
    public ResponseModel completeOnboarding() {
        log.info("---------------REFRESH/COMPLETE ONBOARDING API CALL CAME FROM CLIENT--------------------");
        User user = userComponents.getUser();
        StripeAccountAndUserMapping accountAndUserMapping = stripeAccountAndUserMappingRepository.findByUserUserId(user.getUserId());
        if (accountAndUserMapping == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_CONNECT_ACC_NOT_FOUND, null);
        }

        // If the user has provided all the account details and its currently under progress
        if (accountAndUserMapping.getIsDetailsSubmitted() != null && accountAndUserMapping.getIsDetailsSubmitted()) {
            throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_ACCOUNT_UNDER_VERIFICATION, null);
        }

        String accountId = accountAndUserMapping.getStripeAccountId();
        Account account;
        try {
            account = Account.retrieve(accountId);
        } catch (StripeException e) {
            log.error(e.getMessage());
            throw new ApplicationException(Constants.ERROR_STATUS, e.getMessage(), e.getMessage());
        }
        return createAccountLinks(account);
    }

    /**
     * Method to retrieve url to access dashboard
     *
     * @return
     */
    @Transactional
    public ResponseModel accessMyDashboard() {
        Stripe.apiKey = stripeProperties.getApiKey();

        User user = userComponents.getUser();
        StripeAccountAndUserMapping accountAndUserMapping = stripeAccountAndUserMappingRepository.findByUserUserId(user.getUserId());
        if (accountAndUserMapping == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_CONNECT_ACC_NOT_FOUND, null);
        }
        if (!accountAndUserMapping.getIsOnBoardingCompleted()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_COMPLETE_ONBOARDING, null);
        }
        String accountId = accountAndUserMapping.getStripeAccountId();
        LoginLink loginLink;

        try {
            loginLink = LoginLink.createOnAccount(
                    accountId,
                    (Map<String, Object>) null,
                    (RequestOptions) null
            );
        } catch (StripeException e) {
            log.error(e.getMessage());
            throw new ApplicationException(Constants.ERROR_STATUS, e.getMessage(), e.getMessage());
        }
        Map<String, Object> map = new HashMap<>();
        map.put(KeyConstants.KEY_DASHBOARD_URL, loginLink.getUrl());
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DASHBOARD_URL_RETRIEVED, map);
    }


    /**
     * Used to check whether a user has onboarded in Stripe
     *
     * @return
     */
    @Transactional
    public ResponseModel checkInstructorOnBoardingStatus() {
        log.info("Check instructor on-boarding status starts.");
        long apiStartTimeMillis = new Date().getTime();
        User instructor = userComponents.getUser();

        UserProfile userProfile = userProfileRepository.findByUserUserId(instructor.getUserId());

        StripeAccountAndUserMapping stripeAccountAndUserMapping =
                stripeAccountAndUserMappingRepository.findByUserUserId(instructor.getUserId());
        log.info("Queries to get user, user profile and StripeAccountAndUserMapping : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        boolean isOnBoardedViaPayPal = false;

        boolean isAccountCreated = false;
        boolean isOnBoarded = false;
        String payPalId = "";

        if (stripeAccountAndUserMapping != null && !stripeAccountAndUserMapping.getStripeAccountId().isEmpty()) {
            isAccountCreated = true;
        }
        if (stripeAccountAndUserMapping != null && stripeAccountAndUserMapping.getIsOnBoardingCompleted()) {
            isOnBoarded = true;
        } else if (stripeAccountAndUserMapping != null && !stripeAccountAndUserMapping.getIsOnBoardingCompleted()) {
            Stripe.apiKey = stripeProperties.getApiKey();
            try {
                Account account = Account.retrieve(stripeAccountAndUserMapping.getStripeAccountId());
                log.info("Retrieve stripe account : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                profilingEndTimeMillis = new Date().getTime();
                if (account != null) {
                    if (account.getDetailsSubmitted()) {
                        stripeAccountAndUserMapping.setIsDetailsSubmitted(true);
                        stripeAccountAndUserMappingRepository.save(stripeAccountAndUserMapping);
                    }

                    if (account.getChargesEnabled() && account.getPayoutsEnabled()) {
                        stripeAccountAndUserMapping.setIsOnBoardingCompleted(true);
                        stripeAccountAndUserMappingRepository.save(stripeAccountAndUserMapping);
                        isOnBoarded = true;
                    }
                    log.info("Queries to update stripeAccountAndUserMappingRepository : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                }
            } catch (StripeException e) {
               log.error("Error fetching Stripe connect account information " + e.getCode() + KeyConstants.KEY_SPACE + e.getMessage());
            }
        }

        /**
         * Checking whether the account verification in stripe is in progress
         */
        boolean isVerificationUnderProgress = false;
        if (stripeAccountAndUserMapping != null
                && stripeAccountAndUserMapping.getIsDetailsSubmitted()
                && !stripeAccountAndUserMapping.getIsOnBoardingCompleted()) {
            isVerificationUnderProgress = true;
        }

        // Checking whether the user has provided the paypal id
        UserAccountAndPayPalIdMapping userAccountAndPayPalIdMapping = userAccountAndPayPalIdMappingRepository.findByUserUserId(instructor.getUserId());
        if (userAccountAndPayPalIdMapping != null && !userAccountAndPayPalIdMapping.getPayPalId().isEmpty()) {
            isAccountCreated = true;
            isOnBoarded = true;
            isOnBoardedViaPayPal = true;
            payPalId = userAccountAndPayPalIdMapping.getPayPalId();
        }
        log.info("Checking whether the user has provided the paypal id : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(KeyConstants.KEY_IS_ONBOARDED, isOnBoarded);
        dataMap.put(KeyConstants.KEY_IS_ACCOUNT_CREATED, isAccountCreated);
        dataMap.put(KeyConstants.KEY_IS_ONBOARDED_VIA_PAYPAL, isOnBoardedViaPayPal);
        dataMap.put(KeyConstants.KEY_PAYPAL_ID, payPalId);
        dataMap.put(KeyConstants.KEY_VERIFICATION_UNDER_PROGRESS, isVerificationUnderProgress);
        /**
         * Fetching stripe account balance
         */
        double stripeAmount = 0;
        if (stripeAccountAndUserMapping != null && !stripeAccountAndUserMapping.getStripeAccountId().isEmpty()) {
            Stripe.apiKey = stripeProperties.getApiKey();
            RequestOptions requestOptions = RequestOptions.builder().setStripeAccount(stripeAccountAndUserMapping.getStripeAccountId()).build();
            log.info("Building stripe request options : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            try {
                Balance balance = Balance.retrieve(requestOptions);
                log.info("Retrieve stripe balance : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));

                /**
                 *
                 * Getting the total available balance
                 */
                for (Balance.Money money : balance.getPending()) {
                    stripeAmount += money.getAmount();
                }
                stripeAmount = stripeAmount / 100;

            } catch (StripeException e) {
                log.error("Exception while retrieving balance: " + e.getMessage());
            }
        }
        profilingEndTimeMillis = new Date().getTime();
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);

        dataMap.put(KeyConstants.KEY_STRIPE_AMOUNT, KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(stripeAmount));

        List<String> subscriptionTypeList = Arrays.asList(new String[]{KeyConstants.KEY_PROGRAM, KeyConstants.KEY_SUBSCRIPTION_PACKAGE});
        double instructorOutstandingBalance = instructorAnalyticsService.calculateOutstandingPaymentOfAnInstructor(instructor.getUserId(), subscriptionTypeList);

        double appleUpcomingPayment = instructorOutstandingBalance - stripeAmount;

        // Add apple amount here
        dataMap.put(KeyConstants.KEY_APPLE_AMOUNT, KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(appleUpcomingPayment));

        if (userProfile != null)
            dataMap.put(KeyConstants.KEY_PROFILE_COUNTRY_CODE, userProfile.getCountryCode());
        log.info("Response construction : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Check instructor on-boarding status ends.");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_ONBOARDING_STATUS_FETCHED, dataMap);
    }


    /**
     * Used to check whether a country is supported by Stripe
     *
     * @param countryCode
     * @return
     */
    @Transactional
    public ResponseModel checkCountrySupport(String countryCode) {
        Stripe.apiKey = stripeProperties.getApiKey();

        boolean isCountrySupported = false;

        Countries country = countriesRepository.findByCountryCode(countryCode);
        if (country == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_COUNTRY_NOT_SUPPORTED, null);
        }

        if (country.getIsStripeSupported()) {
            isCountrySupported = true;
        }


        /**
         * Don't use the below CountrySpec api from Stripe. That is dedicated to Custom connect onboarding.
         * - 8Jan2021
         * CountrySpec countrySpec = CountrySpec.retrieve(countryCode);
         */
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put(KeyConstants.KEY_IS_COUNTRY_SUPPORTED, isCountrySupported);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_COUNTRY_STATUS_FETCHED, objectMap);
    }

    /**
     * Reversing transfer made to instructors if refund is raised
     *
     * @param stripeTransferId
     * @param reversalAmount
     */
    public TransferReversal reverseTransfer(String stripeTransferId, double reversalAmount) {

        TransferReversal transferReversal = null;

        if (stripeTransferId == null) {
            throw new ApplicationException(Constants.NOT_EXIST_STATUS, MessageConstants.MSG_STRIPE_TRANSFER_ID_MISSING, null);
        }
        if (reversalAmount <= 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_REVERSE_TRANSFER_ZERO, null);
        }

        StripeTransferAndReversalMapping stripeTransferAndReversalMapping = new StripeTransferAndReversalMapping();
        stripeTransferAndReversalMapping.setStripeTransferId(stripeTransferId);
        stripeTransferAndReversalMapping.setReversalAmount(reversalAmount);
        stripeTransferAndReversalMapping.setStatus(KeyConstants.KEY_INITIATED);
        stripeTransferAndReversalMappingRepository.save(stripeTransferAndReversalMapping);

        try {
            Transfer transfer = Transfer.retrieve(stripeTransferId);

            Map<String, Object> params = new HashMap<>();
            params.put(KeyConstants.KEY_AMOUNT, (int) (reversalAmount * 100));

            transferReversal = transfer.getReversals().create(params);

            stripeTransferAndReversalMapping.setStripeTransferReversalId(transferReversal.getId());
            stripeTransferAndReversalMapping.setStatus(KeyConstants.KEY_SUCCESS);
            stripeTransferAndReversalMappingRepository.save(stripeTransferAndReversalMapping);

        } catch (StripeException e) {
            log.error("Exception while reversing transfer id = " + stripeTransferId + " : " + e.getMessage());

            stripeTransferAndReversalMapping.setStatus(KeyConstants.KEY_FAILURE);
            stripeTransferAndReversalMappingRepository.save(stripeTransferAndReversalMapping);

            StripeReverseTransferErrorLog stripeReverseTransferErrorLog = new StripeReverseTransferErrorLog();
            stripeReverseTransferErrorLog.setStripeTransferId(stripeTransferId);
            stripeReverseTransferErrorLog.setReverseTransferMappingId(stripeTransferAndReversalMapping.getId());
            stripeReverseTransferErrorLog.setErrorCode(e.getCode());
            stripeReverseTransferErrorLog.setErrorMessage(e.getMessage());
            stripeReverseTransferErrorLog.setStripeErrorCode(e.getStripeError().getCode());
            stripeReverseTransferErrorLog.setStripeErrorMessage(e.getStripeError().getMessage());
            stripeReverseTransferErrorLogRepository.save(stripeReverseTransferErrorLog);
        }
        return transferReversal;
    }


    /**
     * Used to get the payouts list
     *
     * @param pageNo
     * @param pageSize
     * @param filterType
     * @param sortBy
     * @param sortOrder
     * @param platform
     * @param search
     * @return
     */
    @Transactional
    public ResponseModel getPayouts(int pageNo, int pageSize, String filterType, String sortBy, String sortOrder, String platform, Optional<String> search) {
        log.info("getPayouts starts.");
        long apiStartTimeMillis = System.currentTimeMillis();

        long profilingStartTimeMillis = System.currentTimeMillis();
        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ERROR, null);
        }

        List<String> sortByList = Arrays.asList(new String[]{SearchConstants.DUE_DATE, SearchConstants.PAID_DATE, SearchConstants.PLATFORM, SearchConstants.INSTRUCTOR_SHARE});
        boolean isSortValid = sortByList.stream().anyMatch(sortBy::equalsIgnoreCase);
        if (!isSortValid) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERROR_SORT_BY, null);
        }
        if (!(SearchConstants.ORDER_DSC.equalsIgnoreCase(sortOrder) || SearchConstants.ORDER_ASC.equalsIgnoreCase(sortOrder))) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ORDER_ERROR, null);
        }
        List<String> filterTypeList = Arrays.asList(new String[]{KeyConstants.KEY_ALL, KeyConstants.KEY_PAID, KeyConstants.KEY_FAILURE, SearchConstants.NOT_PAID, KeyConstants.KEY_PROCESSING});
        boolean isfilterTypeFound = filterTypeList.stream().anyMatch(filterType::equalsIgnoreCase);
        if (!isfilterTypeFound) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_INVALID_PAYOUT_FILTER_TYPE, null);
        }
        if (!(DBConstants.IOS.equalsIgnoreCase(platform) || SearchConstants.ANDROID_AND_WEB.equalsIgnoreCase(platform))) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_INVALID_PAYOUT_PLATFORM, null);
        }
        long profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Validation : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = System.currentTimeMillis();
        List<PlatformType> platformTypeList = new ArrayList<>();
        if (platform.equalsIgnoreCase(DBConstants.IOS)) {
            PlatformType iOSPlatform = platformTypeRepository.findByPlatformTypeId(NotificationConstants.APPLE_PLATFORM);
            platformTypeList.add(iOSPlatform);
        } else {
            PlatformType androidPlatform = platformTypeRepository.findByPlatformTypeId(1L);
            PlatformType webPlatform = platformTypeRepository.findByPlatformTypeId(3L);
            platformTypeList.add(androidPlatform);
            platformTypeList.add(webPlatform);
        }
        List<Long> platformTypeIdList = platformTypeList.stream().map(PlatformType::getPlatformTypeId).collect(Collectors.toList());
        Sort sort;
        if (sortBy.equalsIgnoreCase(SearchConstants.DUE_DATE)) {
            sort = Sort.by(SearchConstants.DUE_DATE).and(Sort.by(DBConstants.INSTRUCTOR_PAYMENT_ID));
        } else if (sortBy.equalsIgnoreCase(SearchConstants.PAID_DATE)) {
            sort = Sort.by("transferDate").and(Sort.by(DBConstants.INSTRUCTOR_PAYMENT_ID));
        } else if (sortBy.equalsIgnoreCase(SearchConstants.PLATFORM)) {
            sort = Sort.by("orderManagement.subscribedViaPlatform.platform").and(Sort.by(DBConstants.INSTRUCTOR_PAYMENT_ID));
        } else {
            sort = Sort.by("instructorShare").and(Sort.by(DBConstants.INSTRUCTOR_PAYMENT_ID));
        }
        if (sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC)) {
            sort = sort.descending();
        }
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, sort);
        Specification<InstructorPayment> finalSpecification;
        Specification<InstructorPayment> platformInSpec = InstructorPaymentSpecifications.findByPlatformIn(platformTypeIdList);
        if (filterType.equalsIgnoreCase(KeyConstants.KEY_PAID)) {
            Specification<InstructorPayment> isTransferDoneSpec = InstructorPaymentSpecifications.findByIsTransferDone(true);
            finalSpecification = platformInSpec.and(isTransferDoneSpec);
        } else if (filterType.equalsIgnoreCase(KeyConstants.KEY_FAILURE)) {
            Specification<InstructorPayment> isTransferFailedSpec = InstructorPaymentSpecifications.findByIsTransferFailed(true);
            finalSpecification = platformInSpec.and(isTransferFailedSpec);
        } else if (filterType.equalsIgnoreCase(SearchConstants.NOT_PAID)) {
            Specification<InstructorPayment> isTransferDoneSpec = InstructorPaymentSpecifications.findByIsTransferDone(false);
            Specification<InstructorPayment> isTopUpInitiatedSpec = InstructorPaymentSpecifications.findByIsTopUpInitiated(false);
            finalSpecification = platformInSpec.and(isTransferDoneSpec).and(isTopUpInitiatedSpec);
        } else if (filterType.equalsIgnoreCase(KeyConstants.KEY_PROCESSING)) {
            Specification<InstructorPayment> isTransferDoneSpec = InstructorPaymentSpecifications.findByIsTransferDone(false);
            Specification<InstructorPayment> isTopUpInitiatedSpec = InstructorPaymentSpecifications.findByIsTopUpInitiated(true);
            Specification<InstructorPayment> isTransferFailedSpec = InstructorPaymentSpecifications.findByIsTransferFailed(false);
            finalSpecification = platformInSpec.and(isTransferDoneSpec).and(isTopUpInitiatedSpec).and(isTransferFailedSpec);
        } else {
            finalSpecification = platformInSpec;
        }
        if (search.isPresent() && !search.get().isEmpty()) {
            Specification<InstructorPayment> nameSearchSpec = InstructorPaymentSpecifications.findByInstructorName(search.get());
            finalSpecification = finalSpecification.and(nameSearchSpec);
        }
        Page<InstructorPayment> instructorPaymentPage = instructorPaymentRepository.findAll(finalSpecification, pageRequest);
        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Query : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        if (instructorPaymentPage.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }

        profilingStartTimeMillis = System.currentTimeMillis();
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        List<PayoutsTileResponseView> payouts = new ArrayList<>();
        for (InstructorPayment instructorPayment : instructorPaymentPage) {
            PayoutsTileResponseView payoutsTileResponseView = new PayoutsTileResponseView();
            String instructorName = KeyConstants.KEY_ANONYMOUS;
            User instructor;
            if (KeyConstants.KEY_PROGRAM.equalsIgnoreCase(instructorPayment.getOrderManagement().getSubscriptionType().getName())) {
                instructor = instructorPayment.getOrderManagement().getProgram().getOwner();
            } else {
                instructor = instructorPayment.getOrderManagement().getSubscriptionPackage().getOwner();
            }
            if (instructor != null) {
                instructorName = fitwiseUtils.getUserFullName(instructor);
            }

            payoutsTileResponseView.setSubscriptionType(instructorPayment.getOrderManagement().getSubscriptionType().getName());

            payoutsTileResponseView.setInstructorPaymentId(instructorPayment.getInstructorPaymentId());
            payoutsTileResponseView.setInstructorName(instructorName);
            payoutsTileResponseView.setInstructorShare(decimalFormat.format(instructorPayment.getInstructorShare()));
            payoutsTileResponseView.setInstructorShareFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(instructorPayment.getInstructorShare()));
            payoutsTileResponseView.setDueDate(fitwiseUtils.formatDate(instructorPayment.getDueDate()));
            payoutsTileResponseView.setDueDateTimeStamp(instructorPayment.getDueDate());
            if (instructorPayment.getStripeTransferStatus() != null && KeyConstants.KEY_TRANSFER_FAILED.equalsIgnoreCase(instructorPayment.getStripeTransferStatus())) {
                payoutsTileResponseView.setStatus(KeyConstants.KEY_FAILURE);
            } else if (null != instructorPayment.getIsTransferDone() && instructorPayment.getIsTransferDone().booleanValue()) {
                payoutsTileResponseView.setStatus(KeyConstants.KEY_PAID);
                payoutsTileResponseView.setTransactionId(instructorPayment.getStripeTransferId());
            } else if ((instructorPayment.getIsTopUpInitiated() != null && instructorPayment.getIsTopUpInitiated().booleanValue()) && (instructorPayment.getIsTransferDone() != null && !instructorPayment.getIsTransferDone().booleanValue()) && (instructorPayment.getIsTransferFailed() != null && !instructorPayment.getIsTransferFailed().booleanValue())) {
                payoutsTileResponseView.setStatus(KeyConstants.KEY_PROCESSING);
            } else {
                payoutsTileResponseView.setStatus(KeyConstants.KEY_NOT_PAID);
            }

            /*
            * Marking 100% refund orders as Paid
            * */
            if (payoutsTileResponseView.getStatus().equals(KeyConstants.KEY_FAILURE) || payoutsTileResponseView.getStatus().equals(KeyConstants.KEY_NOT_PAID)) {
                if (platform.equalsIgnoreCase(DBConstants.IOS)) {
                    if (KeyConstants.KEY_REFUNDED.equals(instructorPayment.getOrderManagement().getOrderStatus())) {
                        payoutsTileResponseView.setStatus(KeyConstants.KEY_REFUNDED);
                    }
                } else {
                    StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(instructorPayment.getOrderManagement().getOrderId());
                    if (stripePayment != null && KeyConstants.KEY_REFUND.equals(stripePayment.getTransactionStatus())) {
                        double amountPaid = stripePayment.getAmountPaid() != null ? stripePayment.getAmountPaid().doubleValue() : 0;
                        double refundAmount = stripePayment.getAmountRefunded() != null ? stripePayment.getAmountRefunded().doubleValue() : 0;
                        if (amountPaid - refundAmount == 0) {
                            payoutsTileResponseView.setStatus(KeyConstants.KEY_REFUNDED);
                        }
                    }
                }
            }

            //Checking whether the instructor has on-boarded in stripe
            if (instructor != null) {
                StripeAccountAndUserMapping stripeAccountAndUserMapping
                        = stripeAccountAndUserMappingRepository.findByUserUserId(instructor.getUserId());
                if (stripeAccountAndUserMapping == null) {
                    payoutsTileResponseView.setInstructorPayoutMode(KeyConstants.KEY_PAYPAL);
                } else {
                    payoutsTileResponseView.setInstructorPayoutMode(KeyConstants.KEY_STRIPE);
                }
            }
            payoutsTileResponseView.setSubscribedViaPlatform(instructorPayment.getOrderManagement().getSubscribedViaPlatform().getPlatform());
            if (instructorPayment.getTransferDate() != null) {
                payoutsTileResponseView.setPaidDate(fitwiseUtils.formatDate(instructorPayment.getTransferDate()));
            }
            if (instructorPayment.getIsTransferFailed() != null && instructorPayment.getIsTransferFailed()) {
                StripeTransferErrorLog stripeTransferErrorLog = stripeTransferErrorLogRepository.
                        findTop1ByInstructorPaymentInstructorPaymentId(instructorPayment.getInstructorPaymentId());
                if (stripeTransferErrorLog != null) {
                    payoutsTileResponseView.setFailureMessage(stripeTransferErrorLog.getErrorMessage());
                }
            }

            payoutsTileResponseView.setPayoutPaidVia(instructorPayment.getTransferMode());
            payoutsTileResponseView.setTransferBillNumber(instructorPayment.getBillNumber());

            payouts.add(payoutsTileResponseView);
        }
        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Data construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = System.currentTimeMillis();
        Map<String, Object> payoutsMap = new HashMap<>();
        payoutsMap.put(KeyConstants.KEY_PAYOUTS, payouts);
        payoutsMap.put(KeyConstants.KEY_TOTAL_COUNT, instructorPaymentPage.getTotalElements());
        Date now = new Date();
        long pendingInstructorPayments = instructorPaymentRepository
                .countByIsTransferDoneAndIsTopUpInitiatedAndOrderManagementSubscribedViaPlatformInAndDueDateLessThanAndInstructorShareGreaterThan(false, false, platformTypeList, now, 0);
        payoutsMap.put(KeyConstants.KEY_TOP_UP_COUNT, pendingInstructorPayments);
        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Response construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        long apiEndTimeMillis = System.currentTimeMillis();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("getPayouts ends.");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PAYOUTS_LIST_FETCHED, payoutsMap);
    }

    @Transactional
    public ResponseModel reInitiateTransfer(Long instructorPaymentId) {
        InstructorPayment instructorPayment = instructorPaymentRepository.findByInstructorPaymentId(instructorPaymentId);
        if (instructorPayment == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_INVALID_INSTRUCTOR_PAYMENT_ID, null);
        }

        if (!instructorPayment.getIsTransferFailed() || instructorPayment.getIsTransferDone()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAYOUT_ALREADY_DONE, null);
        }

        User instructor;
        if (KeyConstants.KEY_PROGRAM.equalsIgnoreCase(instructorPayment.getOrderManagement().getSubscriptionType().getName())) {
            instructor = instructorPayment.getOrderManagement().getProgram().getOwner();
        } else {
            instructor = instructorPayment.getOrderManagement().getSubscriptionPackage().getOwner();
        }
        StripePayment stripePayment = stripePaymentRepository.
                findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(instructorPayment.getOrderManagement().getOrderId());

        StripeAccountAndUserMapping stripeAccountAndUserMapping = stripeAccountAndUserMappingRepository.findByUserUserId(instructor.getUserId());
        if (stripeAccountAndUserMapping != null && !stripeAccountAndUserMapping.getStripeAccountId().isEmpty()) {
            // Instructor has on-boarded in Stripe
            Stripe.apiKey = stripeProperties.getApiKey();

            Map<String, Object> params = new HashMap<>();
            params.put(KeyConstants.KEY_AMOUNT, (int) (instructorPayment.getInstructorShare() * 100));
            params.put(StripeConstants.STRIPE_PROP_CURRENCY, "usd");
            params.put(StripeConstants.STRIPE_PROP_DESTINATION, stripeAccountAndUserMapping.getStripeAccountId());
            params.put(StripeConstants.STRIPE_PROP_TRANSFER_GROUP, stripePayment.getOrderManagement().getOrderId());
            params.put("source_transaction", stripePayment.getChargeId());

            try {
                /**
                 * Creating a transfer to payout to instructor
                 */
                Transfer transfer = Transfer.create(params);
                if (transfer != null) {
                    instructorPayment.setIsTransferDone(true);
                    instructorPayment.setTransferMode(KeyConstants.KEY_STRIPE);
                    instructorPayment.setStripeTransferId(transfer.getId());
                    instructorPayment.setIsTransferAttempted(true);
                    instructorPayment.setTransferDate(new Date());
                    instructorPayment.setIsTransferFailed(false);
                    instructorPayment.setStripeTransferStatus(KeyConstants.KEY_TRANSFER_CREATED);
                    instructorPaymentRepository.save(instructorPayment);

                    /**
                     * Checking whether the Instructors has any credits (Balances to be paid to Trainnr in case of Refunds)
                     * and initiating reverse transfer wrt and updating the credits table
                     */
                    InstructorPaymentCredits instructorPaymentCredit = instructorPaymentsCreditsRepository.findByInstructorUserIdAndCurrencyType(instructor.getUserId(), KeyConstants.KEY_CURRENCY_US_DOLLAR);
                    if (instructorPaymentCredit != null && instructorPaymentCredit.getTotalCredits() > 0) {
                        double instructorShareAmount = instructorPayment.getInstructorShare();
                        double instructorCreditAmount = instructorPaymentCredit.getTotalCredits();
                        if (instructorCreditAmount >= instructorShareAmount) {
                            // If both transfer amount and credit amount is same, Reverse transfer the full amount from transfer
                            // If Credit amount is greater than share amount, reversing the transfer from the share amount
                            TransferReversal transferReversal = reverseTransfer(transfer.getId(), instructorShareAmount);
                            if (transferReversal != null)
                                reduceCreditAmount(instructorPayment, instructorPaymentCredit, instructorShareAmount);
                        } else {
                            // If Credit amount is lesser than share amount, reversing the transfer from the credit amount
                            TransferReversal transferReversal = reverseTransfer(transfer.getId(), instructorCreditAmount);
                            if (transferReversal != null)
                                reduceCreditAmount(instructorPayment, instructorPaymentCredit, instructorCreditAmount);
                        }
                    }
                }
            } catch (StripeException e) {
                instructorPayment.setIsTransferFailed(true);
                instructorPayment.setIsTransferAttempted(true);
                instructorPayment.setIsTransferDone(false);
                instructorPaymentRepository.save(instructorPayment);

                StripeTransferErrorLog stripeTransferErrorLog = new StripeTransferErrorLog();
                stripeTransferErrorLog.setInstructorPayment(instructorPayment);
                stripeTransferErrorLog.setErrorCode(e.getCode());
                stripeTransferErrorLog.setErrorMessage(e.getMessage());
                stripeTransferErrorLog.setStripeErrorCode(e.getStripeError().getCode());
                stripeTransferErrorLog.setStripeErrorMessage(e.getStripeError().getMessage());
                stripeTransferErrorLogRepository.save(stripeTransferErrorLog);
                log.error("Error in creating a transfer ---> " + e.getMessage());
            }
        }
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_TRANSFER_DONE, null);
    }


    @ResponseBody
    public ResponseModel savePayPalTransaction(PayPalTransactionRequestView payPalTransactionRequestView) {
        InstructorPayment instructorPayment = instructorPaymentRepository.
                findByInstructorPaymentId(payPalTransactionRequestView.getInstructorPaymentId());

        if (instructorPayment == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_INVALID_INSTRUCTOR_PAYMENT_ID, null);
        }

        if (null != instructorPayment.getIsTransferDone() && instructorPayment.getIsTransferDone()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAYOUT_ALREADY_DONE, null);
        }

        if (payPalTransactionRequestView.getTransferDate() == null || payPalTransactionRequestView.getTransferDate() == 0L) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_DATE_NULL, null);
        }
        Date transferDate = new Date(payPalTransactionRequestView.getTransferDate());

        instructorPayment.setTransferDate(transferDate);
        instructorPayment.setIsTransferDone(true);
        instructorPayment.setIsTransferAttempted(true);
        instructorPayment.setTransferMode(payPalTransactionRequestView.getPaymentMode());
        instructorPayment.setStripeTransferId(payPalTransactionRequestView.getTransactionId());
        instructorPaymentRepository.save(instructorPayment);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PAYPAL_TRX_DATA_SAVED, null);
    }


    /**
     * Method use to update the reduced credit amount that was reversed from the transfer.
     *
     * @param instructorPayment
     * @param instructorPaymentCredit
     * @param debitedCreditAmount
     */
    public void reduceCreditAmount(InstructorPayment instructorPayment, InstructorPaymentCredits instructorPaymentCredit, Double debitedCreditAmount) {
        double oldCreditAmount = instructorPaymentCredit.getTotalCredits();
        double updatedCreditAmount = oldCreditAmount - debitedCreditAmount;
        instructorPaymentCredit.setTotalCredits(updatedCreditAmount);
        instructorPaymentsCreditsRepository.save(instructorPaymentCredit);


        double oldCreditAmountForDebit = oldCreditAmount;
        double remainingDebitAmount = debitedCreditAmount;

        List<InstructorPaymentCreditAudit> creditAuditList = instructorPaymentCreditAuditRepository.findByInstructorPaymentCreditsInstructorAndIsCreditAndIsCreditSettled(instructorPaymentCredit.getInstructor(), true, false);
        for (InstructorPaymentCreditAudit creditAudit : creditAuditList) {
            InstructorPaymentCreditAudit instructorPaymentCreditAudit = new InstructorPaymentCreditAudit();
            instructorPaymentCreditAudit.setCredit(false);
            instructorPaymentCreditAudit.setInstructorPaymentCredits(instructorPaymentCredit);
            instructorPaymentCreditAudit.setInstructorPayment(instructorPayment);

            double creditAuditCreditSettledAmount = creditAudit.getCreditSettledAmount() != null ? creditAudit.getCreditSettledAmount() : 0.0;

            boolean breakLoop = false;
            boolean isCreditSettled = false;
            double amountToSettle = creditAudit.getAmount() - creditAuditCreditSettledAmount;
            double amount;

            /*
            amount > amountToSettle -> amountToSettle is  amount && creditAudit -> update settled amount and isSettled true
            amount == amountToSettle -> amountToSettle is  amount and break loop && creditAudit -> update settled amount and isSettled true
            amount < amountToSettle -> amount is debit  and break loop && creditAudit -> update settled amount

            instructorPaymentCreditAudit -> set instructor payment id
            instructorPaymentCreditAudit -> update currentTotalCredit
            */
            if (remainingDebitAmount > amountToSettle) {
                amount = amountToSettle;
                isCreditSettled = true;
            } else if (remainingDebitAmount == amountToSettle) {
                amount = amountToSettle;
                isCreditSettled = true;
                breakLoop = true;
            } else {
                amount = remainingDebitAmount;
                breakLoop = true;
            }
            remainingDebitAmount = remainingDebitAmount - amount;

            /*
             * Updating settled amount and isCreditSettled for the old credit that is being settled
             * */
            double creditSettledAmount = creditAuditCreditSettledAmount + amount;
            creditAudit.setCreditSettledAmount(creditSettledAmount);
            creditAudit.setCreditSettled(isCreditSettled);
            instructorPaymentCreditAuditRepository.save(creditAudit);

            instructorPaymentCreditAudit.setAmount(amount);

            /*
             * Setting current instructor credit after this debit entry
             * */
            oldCreditAmountForDebit = oldCreditAmountForDebit - amount;
            instructorPaymentCreditAudit.setCurrentTotalCredit(oldCreditAmountForDebit);

            //Setting the order of the old credit that is being settled
            instructorPaymentCreditAudit.setSettledInstructorPayment(creditAudit.getInstructorPayment());
            instructorPaymentCreditAuditRepository.save(instructorPaymentCreditAudit);
            //QBO: DEBIT CAPTURE
            fitwiseQboEntityService.createAndSyncPaymentInsufficientBalance(creditAudit.getInstructorPayment().getOrderManagement(), amount);
            if (breakLoop) {
                break;
            }

        }
        /*
         * Maintaining history of debit
         * */
        InstructorPaymentCreditHistory instructorPaymentCreditHistory = new InstructorPaymentCreditHistory();
        instructorPaymentCreditHistory.setCredit(false);
        instructorPaymentCreditHistory.setAmount(debitedCreditAmount);
        instructorPaymentCreditHistory.setCurrentTotalCredit(instructorPaymentCredit.getTotalCredits());
        instructorPaymentCreditHistory.setInstructorPaymentCredits(instructorPaymentCredit);
        instructorPaymentCreditHistory.setInstructorPayment(instructorPayment);
        instructorPaymentCreditHistoryRepository.save(instructorPaymentCreditHistory);

    }


    /**
     * Method use to update the added credit amount against the Instructor
     *
     * @param instructorPayment
     * @param instructor
     * @param newInstructorCreditAmount
     * @param currencyType
     */
    public void addInstructorCredit(InstructorPayment instructorPayment, User instructor, Double newInstructorCreditAmount, String currencyType) {
        InstructorPaymentCredits instructorPaymentCredits = instructorPaymentsCreditsRepository.findByInstructorUserIdAndCurrencyType(instructor.getUserId(), currencyType);
        StripeAccountAndUserMapping stripeAccountAndUserMapping = stripeAccountAndUserMappingRepository.findByUserUserId(instructor.getUserId());

        if (instructorPaymentCredits == null) {
            instructorPaymentCredits = new InstructorPaymentCredits();
            instructorPaymentCredits.setCurrencyType(currencyType);
            instructorPaymentCredits.setTotalCredits(newInstructorCreditAmount);
            instructorPaymentCredits.setInstructor(instructor);
            if (stripeAccountAndUserMapping != null) {
                instructorPaymentCredits.setStripeAccountId(stripeAccountAndUserMapping.getStripeAccountId());
            }
            instructorPaymentsCreditsRepository.save(instructorPaymentCredits);
        } else {
            double oldCreditAmount = instructorPaymentCredits.getTotalCredits();
            double updatedCreditAmount = oldCreditAmount + newInstructorCreditAmount;
            instructorPaymentCredits.setTotalCredits(updatedCreditAmount);
            instructorPaymentsCreditsRepository.save(instructorPaymentCredits);
        }

        InstructorPaymentCreditAudit instructorPaymentCreditAudit = new InstructorPaymentCreditAudit();
        instructorPaymentCreditAudit.setCredit(true);
        instructorPaymentCreditAudit.setAmount(newInstructorCreditAmount);
        instructorPaymentCreditAudit.setCurrentTotalCredit(instructorPaymentCredits.getTotalCredits());
        instructorPaymentCreditAudit.setInstructorPaymentCredits(instructorPaymentCredits);
        instructorPaymentCreditAudit.setInstructorPayment(instructorPayment);
        instructorPaymentCreditAudit.setCreditSettled(false);
        instructorPaymentCreditAudit.setCreditSettledAmount(0.0);
        instructorPaymentCreditAudit.setSettledInstructorPayment(null);
        instructorPaymentCreditAuditRepository.save(instructorPaymentCreditAudit);
        /*
         * * Maintaining history of Credit
         * */
        InstructorPaymentCreditHistory instructorPaymentCreditHistory = new InstructorPaymentCreditHistory();
        instructorPaymentCreditHistory.setCredit(true);
        instructorPaymentCreditHistory.setAmount(newInstructorCreditAmount);
        instructorPaymentCreditHistory.setCurrentTotalCredit(instructorPaymentCredits.getTotalCredits());
        instructorPaymentCreditHistory.setInstructorPaymentCredits(instructorPaymentCredits);
        instructorPaymentCreditHistory.setInstructorPayment(instructorPayment);
        instructorPaymentCreditHistoryRepository.save(instructorPaymentCreditHistory);
    }

    /**
     * Method used to pay instructors their share for the programs subscribed via Apple payments
     * Since we didn't have any webhook event for payment settlement state from Apple, we will assume 45 days from
     * subscribed date as settlement date to Fitwise bank account.
     */
    public ResponseModel processInstructorApplePayments() throws ParseException {

        /**
         * Checking whether the request is from a Admin
         */
        boolean isAdmin = false;
        boolean isSuperAdmin = false;
        User user = userComponents.getUser();
        String userEmail = user.getEmail();
        List<UserRoleMapping> userRoleMappings = user.getUserRoleMappings();
        for (UserRoleMapping userRoleMapping : userRoleMappings) {
            if (userRoleMapping.getUserRole().getName().equalsIgnoreCase(SecurityFilterConstants.ROLE_ADMIN)) {
                isAdmin = true;
            }
        }
        if (!isAdmin) {
            throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_ACCESS_RESTRICTED, null);
        }


        /**
         * Check whether its super admin role
         */
        String superAdminEmailAddresses = generalProperties.getSuperAdminEmailAddresses();
        if (!superAdminEmailAddresses.isEmpty()) {
            String[] superAdminEmails = superAdminEmailAddresses.split(",");
            for (String email : superAdminEmails) { //Checking for super admin emails
                if (email.equalsIgnoreCase(userEmail)) {
                    isSuperAdmin = true;
                    break;
                }
            }
            if (!isSuperAdmin) {
                throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_ACCESS_SA_RESTRICTED, null);
            }
        }


        /**
         * This api call is not allowed for a particular time period in a day since payout might be getting created at the
         * same time from Stripe
         */

        log.info("processInstructorApplePayments initiated.");

        String timeFrom = stripeProperties.getPayoutCreationStartTime();
        Date t1 = new SimpleDateFormat(Constants.TIME_FORMAT).parse(timeFrom);
        Calendar c1 = Calendar.getInstance();
        c1.setTime(t1);

        String timeTo = stripeProperties.getPayoutCreationEndTime();
        Date t2 = new SimpleDateFormat(Constants.TIME_FORMAT).parse(timeTo);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(t2);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String dateTime = dtf.format(now);
        // Splitting and getting only time
        String[] dateTimeSplit = dateTime.split(" ");

        String formattedTime = dateTimeSplit[1];
        Date t3 = new SimpleDateFormat(Constants.TIME_FORMAT).parse(formattedTime);
        Calendar c3 = Calendar.getInstance();
        c3.setTime(t3);

        Date x = c3.getTime();
        if (x.after(c1.getTime()) && x.before(c2.getTime())) {
            throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, MessageConstants.MSG_SAME_DAY_PAYOUT_TOPUP_ERROR, null);
        }

        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        Date today = new Date();

        PlatformType iOSPlatform = platformTypeRepository.findByPlatformTypeId(NotificationConstants.APPLE_PLATFORM);
        List<PlatformType> platformTypeList = new ArrayList<>();
        platformTypeList.add(iOSPlatform);

        // Fetching the transactions to be paid out to instructor
        List<InstructorPayment> pendingInstructorPayments = instructorPaymentRepository.findByIsTransferDoneAndIsTopUpInitiatedAndOrderManagementSubscribedViaPlatformInAndDueDateLessThanAndInstructorShareGreaterThan(false, false, platformTypeList, today, 0);
        if (pendingInstructorPayments.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }

        List<FailedTopUpPayout> failedTopUpPayoutList = new ArrayList<>();

        for (InstructorPayment instructorPayment : pendingInstructorPayments) {
            // Checking whether the order is subscribed via iOS platform
            //Checking if due date is before now

            User instructor;
            if (KeyConstants.KEY_PROGRAM.equalsIgnoreCase(instructorPayment.getOrderManagement().getSubscriptionType().getName())) {
                instructor = instructorPayment.getOrderManagement().getProgram().getOwner();
            } else {
                instructor = instructorPayment.getOrderManagement().getSubscriptionPackage().getOwner();
            }

            StripeAccountAndUserMapping stripeAccountAndUserMapping = stripeAccountAndUserMappingRepository.
                    findByUserUserId(instructor.getUserId());

            //Checking whether the instructor is on-boarded in stripe
            if (stripeAccountAndUserMapping != null) {
                Stripe.apiKey = stripeProperties.getApiKey();

                Map<String, Object> params = new HashMap<>();
                int instructorShare = (int) (instructorPayment.getInstructorShare() * 100);
                //This value must be greater than or equal to 1.; else will throw error
                // code: parameter_invalid_integer
                params.put(KeyConstants.KEY_AMOUNT, instructorShare);
                params.put(StripeConstants.STRIPE_PROP_CURRENCY, "usd");
                params.put(StringConstants.JSON_PROPERTY_KEY_DESCRIPTION, instructorPayment.getOrderManagement().getOrderId());
                String billNo = instructorPayment.getFixedBillNumber();
                billNo = billNo.replace("#FITBL", "BL");
                // com.stripe.exception.InvalidRequestException: The statement descriptor must be at most 15 characters
                if (billNo.length() <= 15) {
                    params.put("statement_descriptor", billNo); // This string should not exceed 15 characters
                }
                AppleSettlementByStripe appleSettlementByStripe = new AppleSettlementByStripe();
                appleSettlementByStripe.setInstructorPayment(instructorPayment);

                try {
                    /**
                     * Topping up Fitwise stripe account from Fitwise bank account
                     */
                    Topup topup = Topup.create(params);
                    if (topup != null) {

                        log.info("Top up created for instructor payment id : " + instructorPayment.getInstructorPaymentId() + ". Stripe top up id : " + topup.getId());

                        appleSettlementByStripe.setStripeTopUpId(topup.getId());
                        appleSettlementByStripe.setStripeTopUpStatus(topup.getStatus());
                        appleSettlementByStripeRepository.save(appleSettlementByStripe);

                        instructorPayment.setIsTopUpInitiated(true);
                        instructorPaymentRepository.save(instructorPayment);

                        log.info("Top up completed for top up id : " + topup.getId());
                    }
                } catch (StripeException e) {

                    log.error("Top up creation failed ------------>" + e.getMessage());

                    /**
                     * If the payout schedule is not automatic, saving it in our FW DB and throwing an error to the Admin
                     */
                    if (e.getMessage().contains(StripeConstants.STRIPE_TOP_UP_MANUAL_MODE_PROMPT)) {
                        PayoutModeSettings payoutModeSettings = payoutModeSettingsRepository.findTop1ByPayoutSettingsId(1L);
                        if (payoutModeSettings != null) {
                            payoutModeSettings.setManual(false);
                            payoutModeSettingsRepository.save(payoutModeSettings);
                        }
                        throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_STRIPE_TOP_UP_MANUAL_PROMPT, null);
                    }
                    instructorPayment.setIsTransferDone(false);
                    instructorPayment.setIsTransferFailed(true);
                    instructorPaymentRepository.save(instructorPayment);

                    appleSettlementByStripe.setErrorCode(e.getCode());
                    appleSettlementByStripe.setErrorMessage(e.getMessage());
                    appleSettlementByStripe.setStripeErrorCode(e.getStripeError().getCode());
                    appleSettlementByStripe.setStripeErrorMessage(e.getStripeError().getMessage());
                    appleSettlementByStripeRepository.save(appleSettlementByStripe);


                    String instructorName = KeyConstants.KEY_ANONYMOUS;
                    if (instructor != null) {
                        instructorName = fitwiseUtils.getUserFullName(instructor);
                    }
                    FailedTopUpPayout failedTopUpPayout = new FailedTopUpPayout();
                    failedTopUpPayout.setInstructorName(instructorName);
                    failedTopUpPayout.setInstructorShare(decimalFormat.format(instructorPayment.getInstructorShare()));
                    failedTopUpPayout.setDueDate(fitwiseUtils.formatDate(instructorPayment.getDueDate()));
                    failedTopUpPayout.setFailureMessage(e.getMessage());

                    failedTopUpPayoutList.add(failedTopUpPayout);
                }
            }
        }
        String responseMsg = MessageConstants.MSG_TOP_UP_COMPLETED;
        if (!failedTopUpPayoutList.isEmpty()) {
            responseMsg = MessageConstants.MSG_TOP_UP_FAILURE;
            constructAndSendTopUpFailureMail(failedTopUpPayoutList);
        }
        return new ResponseModel(Constants.SUCCESS_STATUS, responseMsg, null);
    }

    /*
     * Sending mail notification to super admins
     * @param failedTopUpPayoutList
     */
    private void constructAndSendTopUpFailureMail(List<FailedTopUpPayout> failedTopUpPayoutList) {
        StringBuilder mailBody = new StringBuilder();
        if (!failedTopUpPayoutList.isEmpty()) {
            mailBody.append("<html><head><style>table, th, td{ border: 1px solid black; border-collapse: collapse;}</style></head><body>");
            mailBody.append("<p>Please find the failed instructor payouts  : </p>");
            mailBody.append("<br>");
            mailBody.append("<table>");
            mailBody.append("<tr>");
            mailBody.append("<th>S.No</th>");
            mailBody.append("<th>Instructor Name</th>");
            mailBody.append("<th>Instructor Share</th>");
            mailBody.append("<th>Due Date</th>");
            mailBody.append("<th>Reason</th>");
            mailBody.append("</tr>");
            for (int i = 0; i < failedTopUpPayoutList.size(); i++) {
                FailedTopUpPayout payout = failedTopUpPayoutList.get(i);

                mailBody.append("<tr>");
                mailBody.append("<td>").append(i + 1).append("</td>");
                mailBody.append("<td>").append(payout.getInstructorName()).append("</td>");
                mailBody.append("<td>").append(payout.getInstructorShare()).append("</td>");
                mailBody.append("<td>").append(payout.getDueDate()).append("</td>");
                mailBody.append("<td>").append(payout.getFailureMessage()).append("</td>");
                mailBody.append("</tr>");
            }
            mailBody.append("</table></body></html>");
        }


        asyncMailer.sendHtmlMail(generalProperties.getSuperAdminEmailAddresses(), "Stripe Top up failed", mailBody.toString());
    }

    /**
     * Method used to return the Instructor payment details
     *
     * @param instructorPaymentId
     * @return
     */
    public ResponseModel getInstructorPayoutDetails(Long instructorPaymentId) {
        log.info("getInstructorPayoutDetails starts.");
        long apiStartTimeMillis = System.currentTimeMillis();

        long profilingStartTimeMillis = System.currentTimeMillis();
        InstructorPayment instructorPayment = instructorPaymentRepository.findByInstructorPaymentId(instructorPaymentId);
        OrderManagement orderManagement = instructorPayment.getOrderManagement();
        User member = orderManagement.getUser();

        String title;
        User instructor;
        if (KeyConstants.KEY_PROGRAM.equalsIgnoreCase(orderManagement.getSubscriptionType().getName())) {
            title = orderManagement.getProgram().getTitle();
            instructor = orderManagement.getProgram().getOwner();
        } else {
            title = orderManagement.getSubscriptionPackage().getTitle();
            instructor = orderManagement.getSubscriptionPackage().getOwner();
        }

        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);

        PayoutDetailsResponseView payoutDetailsResponseView = new PayoutDetailsResponseView();
        payoutDetailsResponseView.setSubscriptionType(orderManagement.getSubscriptionType().getName());
        payoutDetailsResponseView.setTransferAmount(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(instructorPayment.getInstructorShare()));
        payoutDetailsResponseView.setTransferDueDate(fitwiseUtils.formatDate(instructorPayment.getDueDate()));

        if (KeyConstants.KEY_TRANSFER_FAILED.equalsIgnoreCase(instructorPayment.getStripeTransferStatus())) {
            payoutDetailsResponseView.setTransferStatus(KeyConstants.KEY_FAILURE);
        } else if (instructorPayment.getIsTransferDone() != null && instructorPayment.getIsTransferDone()) {
            payoutDetailsResponseView.setTransferStatus(KeyConstants.KEY_PAID);
            payoutDetailsResponseView.setPayoutTransactionId(instructorPayment.getStripeTransferId());
            payoutDetailsResponseView.setTransferPaidDate(fitwiseUtils.formatDate(instructorPayment.getTransferDate()));
        } else if ((instructorPayment.getIsTopUpInitiated() != null && instructorPayment.getIsTopUpInitiated().booleanValue()) && (instructorPayment.getIsTransferDone() != null && !instructorPayment.getIsTransferDone().booleanValue()) && (instructorPayment.getIsTransferFailed() != null && !instructorPayment.getIsTransferFailed().booleanValue())) {
            payoutDetailsResponseView.setTransferStatus(KeyConstants.KEY_PROCESSING);
        } else {
            payoutDetailsResponseView.setTransferStatus(KeyConstants.KEY_NOT_PAID);
        }
        long profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Query and basic details : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = System.currentTimeMillis();
        /*
         * Marking 100% refund orders as Paid
         * */
        if (payoutDetailsResponseView.getTransferStatus().equals(KeyConstants.KEY_FAILURE) || payoutDetailsResponseView.getTransferStatus().equals(KeyConstants.KEY_NOT_PAID)) {
            if (orderManagement.getSubscribedViaPlatform().getPlatform().equalsIgnoreCase(DBConstants.IOS)) {
                if (KeyConstants.KEY_REFUNDED.equals(instructorPayment.getOrderManagement().getOrderStatus())) {
                    payoutDetailsResponseView.setTransferStatus(KeyConstants.KEY_REFUNDED);
                }
            } else {
                StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(instructorPayment.getOrderManagement().getOrderId());
                if (stripePayment != null && KeyConstants.KEY_REFUND.equals(stripePayment.getTransactionStatus())) {
                    double amountPaid = stripePayment.getAmountPaid() != null ? stripePayment.getAmountPaid().doubleValue() : 0;
                    double refundAmount = stripePayment.getAmountRefunded() != null ? stripePayment.getAmountRefunded().doubleValue() : 0;
                    if (amountPaid - refundAmount == 0) {
                        payoutDetailsResponseView.setTransferStatus(KeyConstants.KEY_REFUNDED);
                    }
                }
            }
        }
        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Refund details : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = System.currentTimeMillis();
        //Checking whether the instructor has on-boarded in stripe
        if (instructor != null) {
            StripeAccountAndUserMapping stripeAccountAndUserMapping
                    = stripeAccountAndUserMappingRepository.findByUserUserId(instructor.getUserId());
            if (stripeAccountAndUserMapping == null) {
                payoutDetailsResponseView.setTransferMode(KeyConstants.KEY_PAYPAL);
            } else {
                payoutDetailsResponseView.setTransferMode(KeyConstants.KEY_STRIPE);
            }
        }

        payoutDetailsResponseView.setTransferBillNumber(instructorPayment.getBillNumber());
        payoutDetailsResponseView.setTransferVariableBillNumber(instructorPayment.getVariableBillNumber());
        payoutDetailsResponseView.setTransferFixedBillNumber(instructorPayment.getFixedBillNumber());
        payoutDetailsResponseView.setTransferProviderCharge(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(instructorPayment.getProviderCharge()));
        payoutDetailsResponseView.setTransferFixedCharge(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(instructorPayment.getFixedCharge()));

        UserProfile userProfile = userProfileRepository.findByUserUserId(member.getUserId());
        payoutDetailsResponseView.setMemberName(userProfile.getFirstName() + " " + userProfile.getLastName());

        String instructorName = KeyConstants.KEY_ANONYMOUS;
        if (instructor != null) {
            instructorName = fitwiseUtils.getUserFullName(instructor);
        }
        payoutDetailsResponseView.setInstructorName(instructorName);
        payoutDetailsResponseView.setProgramName(title);
        payoutDetailsResponseView.setSubscribedViaPlatform(orderManagement.getSubscribedViaPlatform().getPlatform());

        payoutDetailsResponseView.setSubscribedDate(fitwiseUtils.formatDate(orderManagement.getCreatedDate()));

        //Obtaining the amount - for offer or regular case
        double purchasedAmount = instructorPayment.getTotalAmt();
        payoutDetailsResponseView.setPurchasedAmount(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(purchasedAmount));

        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Instructor and charge details : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = System.currentTimeMillis();
        String transactionId = null;
        String paymentMode = orderManagement.getModeOfPayment();
        if (KeyConstants.KEY_AUTH_NET_PAYMENT_MODE.equals(paymentMode)) {
            AuthNetPayment authNetPayment = authNetPaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
            if (authNetPayment != null) {
                transactionId = authNetPayment.getTransactionId();
            }
        } else if (KeyConstants.KEY_STRIPE.equals(orderManagement.getModeOfPayment())) {
            StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
            if (stripePayment != null) {
                transactionId = stripePayment.getChargeId();
            }
        } else if (NotificationConstants.MODE_OF_PAYMENT.equals(orderManagement.getModeOfPayment())) {
            ApplePayment applePayment = applePaymentRepository.findTop1ByOrderManagementOrderIdOrderByCreatedDateDesc(orderManagement.getOrderId());
            if (applePayment != null) {
                transactionId = applePayment.getTransactionId();
            }
        }
        payoutDetailsResponseView.setPurchaseTransactionId(transactionId);
        payoutDetailsResponseView.setPayoutPaidVia(instructorPayment.getTransferMode());
        if (instructorPayment.getInstructor() != null) {
            payoutDetailsResponseView.setEmail(instructorPayment.getInstructor().getEmail());
        }

        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Transaction details : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        long apiEndTimeMillis = System.currentTimeMillis();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("getInstructorPayoutDetails ends.");

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PAYOUT_DETAILS_FETCHED, payoutDetailsResponseView);
    }


    /**
     * Get Instructor share status
     *
     * @param instructorId
     * @return
     */
    public ResponseModel getInstructorShareStatus(Long instructorId) {
        log.info("Get instructor share status starts.");
        long apiStartTimeMillis = new Date().getTime();
        User instructor = userRepository.findByUserId(instructorId);
        UserProfile userProfile = userProfileRepository.findByUserUserId(instructor.getUserId());

        StripeAccountAndUserMapping stripeAccountAndUserMapping =
                stripeAccountAndUserMappingRepository.findByUserUserId(instructor.getUserId());
        log.info("query to get instructor, user profile and stripe account and user mapping : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        boolean isOnBoardedViaPayPal = false;

        boolean isAccountCreated = false;
        boolean isOnBoarded = false;
        String payPalId = "";

        if (stripeAccountAndUserMapping != null && !stripeAccountAndUserMapping.getStripeAccountId().isEmpty()) {
            isAccountCreated = true;
        }
        if (stripeAccountAndUserMapping != null && stripeAccountAndUserMapping.getIsOnBoardingCompleted()) {
            isOnBoarded = true;
        }

        /**
         * Checking whether the account verification in stripe is in progress
         */
        boolean isVerificationUnderProgress = false;
        if (stripeAccountAndUserMapping != null
                && stripeAccountAndUserMapping.getIsDetailsSubmitted()
                && !stripeAccountAndUserMapping.getIsOnBoardingCompleted()) {
            isVerificationUnderProgress = true;
        }

        // Checking whether the user has provided the paypal id
        UserAccountAndPayPalIdMapping userAccountAndPayPalIdMapping = userAccountAndPayPalIdMappingRepository.findByUserUserId(instructor.getUserId());
        if (userAccountAndPayPalIdMapping != null && !userAccountAndPayPalIdMapping.getPayPalId().isEmpty()) {
            isAccountCreated = true;
            isOnBoarded = true;
            isOnBoardedViaPayPal = true;
            payPalId = userAccountAndPayPalIdMapping.getPayPalId();
        }
        log.info("Checking whether the user has provided the paypal id : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(KeyConstants.KEY_IS_ONBOARDED, isOnBoarded);
        dataMap.put(KeyConstants.KEY_IS_ACCOUNT_CREATED, isAccountCreated);
        dataMap.put(KeyConstants.KEY_IS_ONBOARDED_VIA_PAYPAL, isOnBoardedViaPayPal);
        dataMap.put(KeyConstants.KEY_PAYPAL_ID, payPalId);
        dataMap.put(KeyConstants.KEY_VERIFICATION_UNDER_PROGRESS, isVerificationUnderProgress);


        /**
         * Fetching stripe account balance
         */
        double stripeAmount = 0;
        if (stripeAccountAndUserMapping != null && !stripeAccountAndUserMapping.getStripeAccountId().isEmpty()) {
            Stripe.apiKey = stripeProperties.getApiKey();
            RequestOptions requestOptions = RequestOptions.builder().setStripeAccount(stripeAccountAndUserMapping.getStripeAccountId()).build();
            try {
                Balance balance = Balance.retrieve(requestOptions);

                /**
                 *
                 * Getting the total available balance
                 */
                for (Balance.Money money : balance.getPending()) {
                    stripeAmount += money.getAmount();
                }
                stripeAmount = stripeAmount / 100;

            } catch (StripeException e) {
                log.error("Exception while retrieving balance: " + e.getMessage());
            }
        }
        log.info("Fetching stripe account balance : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        dataMap.put(KeyConstants.KEY_STRIPE_AMOUNT, KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(stripeAmount));

        /**
         * Since Stripe subscription share will be instantly shared to the Instructor under Pending balance,
         * the other available payment share will be of apple subscriptions. So, calculating the total pending balance
         * and setting it as Apple outstanding balance.
         */

        List<String> subscriptionTypeList = Arrays.asList(new String[]{KeyConstants.KEY_PROGRAM, KeyConstants.KEY_SUBSCRIPTION_PACKAGE});
        double instructorOutstandingBalance = instructorAnalyticsService.calculateOutstandingPaymentOfAnInstructor(instructorId, subscriptionTypeList);
        log.info("Get instructor outstanding balance : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));

        double appleUpcomingPayment = instructorOutstandingBalance - stripeAmount;


        // Add apple amount here
        dataMap.put(KeyConstants.KEY_APPLE_AMOUNT, KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(appleUpcomingPayment));

        if (userProfile != null)
            dataMap.put(KeyConstants.KEY_PROFILE_COUNTRY_CODE, userProfile.getCountryCode());
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get instructor share status ends.");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_ONBOARDING_STATUS_FETCHED, dataMap);
    }

    /**
     * @return
     */
    public ResponseModel updateInstructorInInstructorPayment() {
        List<InstructorPayment> instructorPaymentList = instructorPaymentRepository.findAll();

        for (InstructorPayment instructorPayment : instructorPaymentList) {
            if (KeyConstants.KEY_PROGRAM.equalsIgnoreCase(instructorPayment.getOrderManagement().getSubscriptionType().getName())) {
                instructorPayment.setInstructor(instructorPayment.getOrderManagement().getProgram().getOwner());
            } else {
                instructorPayment.setInstructor(instructorPayment.getOrderManagement().getSubscriptionPackage().getOwner());
            }
            instructorPaymentRepository.save(instructorPayment);

        }
        return new ResponseModel(Constants.SUCCESS_STATUS, "Instructor updated in InstructorPayment table.", null);
    }

    /**
     * @return
     */
    public ResponseModel getInstructorNullInInstructorPayment() {
        List<InstructorPayment> instructorPaymentList = instructorPaymentRepository.findAll();

        Map<String, Object> map = new HashMap<>();

        List<Long> idList = new ArrayList<>();

        for (InstructorPayment instructorPayment : instructorPaymentList) {
            if (KeyConstants.KEY_PROGRAM.equalsIgnoreCase(instructorPayment.getOrderManagement().getSubscriptionType().getName())) {
                if (instructorPayment.getOrderManagement().getProgram().getOwner() == null) {
                    continue;
                }
            } else {
                if (instructorPayment.getOrderManagement().getSubscriptionPackage().getOwner() == null) {
                    continue;
                }
            }

            if (instructorPayment.getInstructor() == null) {
                idList.add(instructorPayment.getInstructorPaymentId());
            }
        }

        map.put("count", idList.size());
        map.put("date", idList);

        return new ResponseModel(Constants.SUCCESS_STATUS, "Instructor null rows fetched", map);
    }

}

