package com.fitwise.service.payment.authorizenet;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.EmailConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.PaymentConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.discounts.DiscountsConstants;
import com.fitwise.constants.payments.appleiap.NotificationConstants;
import com.fitwise.entity.PlatformType;
import com.fitwise.entity.ProgramSubscriptionPaymentHistory;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.discounts.OfferCodeDetail;
import com.fitwise.entity.discounts.OfferCodeDetailAndOrderMapping;
import com.fitwise.entity.payments.appleiap.ApplePayment;
import com.fitwise.entity.payments.appleiap.AppleProductSubscription;
import com.fitwise.entity.payments.authNet.AuthNetArbSubscription;
import com.fitwise.entity.payments.authNet.AuthNetCustomerProfile;
import com.fitwise.entity.payments.authNet.AuthNetPayment;
import com.fitwise.entity.payments.authNet.AuthNetSubscriptionChangesTracker;
import com.fitwise.entity.payments.authNet.AuthNetSubscriptionStatus;
import com.fitwise.entity.payments.authNet.AuthNetWebHookLogger;
import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.payments.common.RefundReasonForAMember;
import com.fitwise.entity.payments.common.RefundReasons;
import com.fitwise.entity.payments.stripe.StripeSubscriptionAndUserPackageMapping;
import com.fitwise.entity.payments.stripe.billing.StripeCustomerAndUserMapping;
import com.fitwise.entity.payments.stripe.billing.StripePayment;
import com.fitwise.entity.payments.stripe.billing.StripeSubscriptionAndUserProgramMapping;
import com.fitwise.entity.payments.stripe.billing.StripeSubscriptionChangesTracker;
import com.fitwise.entity.subscription.PackageSubscription;
import com.fitwise.entity.subscription.ProgramSubscription;
import com.fitwise.entity.subscription.SubscriptionAudit;
import com.fitwise.entity.subscription.SubscriptionStatus;
import com.fitwise.exception.ApplicationException;
import com.fitwise.properties.QboProperties;
import com.fitwise.properties.StripeProperties;
import com.fitwise.repository.AuthNetSubscriptionStatusRepository;
import com.fitwise.repository.SubscriptionPaymentHistoryRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.discountsRepository.OfferCodeDetailAndOrderMappingRepository;
import com.fitwise.repository.order.OrderManagementRepository;
import com.fitwise.repository.payments.appleiap.ApplePaymentRepository;
import com.fitwise.repository.payments.appleiap.AppleProductSubscriptionRepository;
import com.fitwise.repository.payments.authnet.AuthNetArbSubscriptionRepository;
import com.fitwise.repository.payments.authnet.AuthNetCustomerProfileRepository;
import com.fitwise.repository.payments.authnet.AuthNetPaymentRepository;
import com.fitwise.repository.payments.authnet.AuthNetSubscriptionChangesTrackerRepository;
import com.fitwise.repository.payments.authnet.AuthNetWebHookLoggerRepository;
import com.fitwise.repository.payments.authnet.RefundReasonForAMemberRepository;
import com.fitwise.repository.payments.authnet.RefundReasonsRepository;
import com.fitwise.repository.payments.stripe.StripeSubscriptionAndUserPackageMappingRepository;
import com.fitwise.repository.payments.stripe.billing.StripeCustomerAndUserMappingRepository;
import com.fitwise.repository.payments.stripe.billing.StripePaymentRepository;
import com.fitwise.repository.payments.stripe.billing.StripeSubscriptionAndUserProgramMappingRepository;
import com.fitwise.repository.payments.stripe.billing.StripeSubscriptionChangesTrackerRepository;
import com.fitwise.repository.subscription.PackageSubscriptionRepository;
import com.fitwise.repository.subscription.ProgramSubscriptionRepo;
import com.fitwise.repository.subscription.SubscriptionAuditRepo;
import com.fitwise.repository.subscription.SubscriptionStatusRepo;
import com.fitwise.response.payment.authorize.net.ANetTransactionResponse;
import com.fitwise.response.payment.authorize.net.PaymentProfile;
import com.fitwise.service.SubscriptionService;
import com.fitwise.service.qbo.FitwiseQboEntityService;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.specifications.OrderManagementSpecifications;
import com.fitwise.utils.EmailContentUtil;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.mail.AsyncMailer;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.order.OrderDetailResponseView;
import com.fitwise.view.order.OrderDiscountDetails;
import com.fitwise.view.order.OrderHistoryResponseView;
import com.fitwise.view.order.OrderHistoryResponseViewOfAMember;
import com.fitwise.view.order.OrderHistoryTileResponseView;
import com.fitwise.view.order.OrderHistoryTileResponseViewOfAMember;
import com.fitwise.view.payment.authorizenet.ANetBillingAddressView;
import com.fitwise.view.payment.authorizenet.ANetCustomerProfileRequestView;
import com.fitwise.view.payment.authorizenet.ANetOneTimeProgramSubscriptionUsingCardRequestView;
import com.fitwise.view.payment.authorizenet.ANetRecurringSubscriptionRequestViewWithPaymentProfile;
import com.fitwise.view.payment.authorizenet.ANetSubscriptionId;
import com.fitwise.view.payment.authorizenet.ANetSubscriptionUpdateResponseView;
import com.fitwise.view.payment.authorizenet.ANetVoidCreatedResponseView;
import com.fitwise.view.payment.authorizenet.AuthorizationCaptureWebHookResponseView;
import com.fitwise.view.payment.authorizenet.CancelRecurringSubscriptionRequestView;
import com.fitwise.view.payment.authorizenet.GetCustomerProfileResponseView;
import com.fitwise.view.payment.authorizenet.PostRefundReasonRequestView;
import com.fitwise.view.payment.authorizenet.RefundCreatedPayload;
import com.fitwise.view.payment.authorizenet.RefundCreatedResponseView;
import com.fitwise.view.payment.authorizenet.SubscriptionCancellationResponseView;
import com.fitwise.view.payment.authorizenet.SubscriptionCreatedResponseView;
import com.google.gson.Gson;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import lombok.extern.slf4j.Slf4j;
import net.authorize.api.contract.v1.ANetApiResponse;
import net.authorize.api.contract.v1.ARBCreateSubscriptionResponse;
import net.authorize.api.contract.v1.CreateTransactionResponse;
import net.authorize.api.contract.v1.CustomerPaymentProfileMaskedType;
import net.authorize.api.contract.v1.GetCustomerProfileResponse;
import net.authorize.api.contract.v1.GetTransactionDetailsResponse;
import net.authorize.api.contract.v1.MessageTypeEnum;
import net.authorize.api.contract.v1.TransactionResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

@Service
@Slf4j
public class PaymentService {

    @Autowired
    UserComponents userComponents;

    @Autowired
    ValidationService validationService;

    @Autowired
    AuthNetCustomerProfileRepository authNetCustomerProfileRepository;

    @Autowired
    SubscriptionService subscriptionService;

    @Autowired
    AuthNetPaymentRepository authNetPaymentRepository;

    @Autowired
    ProgramSubscriptionRepo programSubscriptionRepo;

    @Autowired
    SubscriptionAuditRepo subscriptionAuditRepo;

    @Autowired
    SubscriptionStatusRepo subscriptionStatusRepo;

    @Autowired
    AuthNetArbSubscriptionRepository authNetArbSubscriptionRepository;

    @Autowired
    AuthNetSubscriptionStatusRepository authNetSubscriptionStatusRepository;

    @Autowired
    OrderManagementRepository orderManagementRepository;

    @Autowired
    ApplePaymentRepository applePaymentRepository;

    @Autowired
    UserProfileRepository userProfileRepository;

    @Autowired
    private AppleProductSubscriptionRepository appleProductSubscriptionRepository;

    @Autowired
    private SubscriptionPaymentHistoryRepository subscriptionPaymentHistoryRepository;

    @Autowired
    private FitwiseQboEntityService fitwiseQboEntityService;

    @Autowired
    FitwiseUtils fitwiseUtils;

    @Autowired
    AuthNetWebHookLoggerRepository authNetWebHookLoggerRepository;

    @Autowired
    AuthNetSubscriptionChangesTrackerRepository authNetSubscriptionChangesTrackerRepository;

    @Autowired
    private RefundReasonsRepository refundReasonsRepository;

    @Autowired
    private RefundReasonForAMemberRepository refundReasonForAMemberRepository;

    @Autowired
    private QboProperties qboProperties;
    @Autowired
    private EmailContentUtil emailContentUtil;
    @Autowired
    private AsyncMailer asyncMailer;
    @Autowired
    StripePaymentRepository stripePaymentRepository;
    @Autowired
    StripeSubscriptionChangesTrackerRepository stripeSubscriptionChangesTrackerRepository;
    @Autowired
    StripeSubscriptionAndUserProgramMappingRepository stripeSubscriptionAndUserProgramMappingRepository;
    @Autowired
    StripeSubscriptionAndUserPackageMappingRepository stripeSubscriptionAndUserPackageMappingRepository;
    @Autowired
    private StripeProperties stripeProperties;
    @Autowired
    OfferCodeDetailAndOrderMappingRepository offerCodeDetailAndOrderMappingRepository;
    @Autowired
    PackageSubscriptionRepository packageSubscriptionRepository;

    @Autowired
    private StripeCustomerAndUserMappingRepository stripeCustomerAndUserMappingRepository;

    /**
     * Used to enter logs
     */
    Logger logger = LoggerFactory.getLogger(PaymentService.class);


    /**
     * Method that will initiate one time transaction in Authorize.net
     *
     * @param subscriptionRequestView - Request view class
     * @param orderId                 - It will be used as a Reference Transaction id
     * @return CreateTransactionResponse - An Auth.net class that contains the transaction response
     */
    @Transactional
    public ANetTransactionResponse initiateOneTimePaymentTransactionByCard(ANetOneTimeProgramSubscriptionUsingCardRequestView subscriptionRequestView, String orderId) {
        Programs program = validationService.validateProgramIdBlocked(subscriptionRequestView.getProgramId());
        ANetTransactionResponse apiResponse = CreateTransactionUsingFormToken.run(subscriptionRequestView, userComponents.getUser().getUserId(), program.getProgramPrice(), orderId);
        logger.info("OneTime Payment response : {} ", apiResponse);
        // Saving the customer profile id from Authorize.net towards the Fitwise user id
        if (subscriptionRequestView.isDoSaveCardData() && apiResponse.getCustomerProfile() != null && apiResponse.getCustomerProfile().getCustomerProfileId() != null) {
            AuthNetCustomerProfile savedANetCustomerProfile = authNetCustomerProfileRepository.findByUserUserId(userComponents.getUser().getUserId());
            if (savedANetCustomerProfile != null) {
                savedANetCustomerProfile.setAuthNetCustomerProfileId(apiResponse.getCustomerProfile().getCustomerProfileId());
                authNetCustomerProfileRepository.save(savedANetCustomerProfile);
            } else {
                AuthNetCustomerProfile authNetCustomerProfile = new AuthNetCustomerProfile();
                authNetCustomerProfile.setUser(userComponents.getUser());
                authNetCustomerProfile.setAuthNetCustomerProfileId(apiResponse.getCustomerProfile().getCustomerProfileId());
                authNetCustomerProfileRepository.save(authNetCustomerProfile);
            }
        }
        return apiResponse;
    }

    /**
     * Method that returns the customer profiles along with the payment profiles matched with it
     *
     * @return
     */
    @Transactional
    public ResponseModel getANetCustomerProfile() {

        User user = userComponents.getUser();
        AuthNetCustomerProfile authNetCustomerProfile = authNetCustomerProfileRepository.findByUserUserId(user.getUserId());
        if (authNetCustomerProfile == null || authNetCustomerProfile.getAuthNetCustomerProfileId() == null) {
            return new ResponseModel(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_ANET_NO_PAYMENT_PROFILES, null);
        }

        GetCustomerProfileResponse profileResponse = GetCustomerProfile.run(authNetCustomerProfile.getAuthNetCustomerProfileId());

        if (profileResponse == null || profileResponse.getProfile() == null || profileResponse.getProfile().getCustomerProfileId() == null
                || profileResponse.getProfile().getPaymentProfiles().isEmpty()) {
            return new ResponseModel(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_ANET_NO_PAYMENT_PROFILES, null);
        }
        logger.info("Customer profile retrieved : {}", profileResponse);
        GetCustomerProfileResponseView customerProfileResponseView = new GetCustomerProfileResponseView();
        customerProfileResponseView.setCustomerProfileId(profileResponse.getProfile().getCustomerProfileId());
        customerProfileResponseView.setMerchantCustomerId(profileResponse.getProfile().getMerchantCustomerId());
        List<PaymentProfile> paymentProfiles = new ArrayList<>();
        for (CustomerPaymentProfileMaskedType customerPaymentProfileMaskedType : profileResponse.getProfile().getPaymentProfiles()) {
            PaymentProfile paymentProfile = new PaymentProfile();
            paymentProfile.setMaskedCardNumber(customerPaymentProfileMaskedType.getPayment().getCreditCard().getCardNumber());
            paymentProfile.setCardType(customerPaymentProfileMaskedType.getPayment().getCreditCard().getCardType());
            paymentProfile.setMaskedExpirationDate(customerPaymentProfileMaskedType.getPayment().getCreditCard().getExpirationDate());
            paymentProfile.setPaymentProfileId(customerPaymentProfileMaskedType.getCustomerPaymentProfileId());
            ANetBillingAddressView aNetBillingAddressView = new ANetBillingAddressView();
            aNetBillingAddressView.setFirstName(customerPaymentProfileMaskedType.getBillTo().getFirstName());
            aNetBillingAddressView.setLastName(customerPaymentProfileMaskedType.getBillTo().getLastName());
            aNetBillingAddressView.setAddress(customerPaymentProfileMaskedType.getBillTo().getAddress());
            aNetBillingAddressView.setCity(customerPaymentProfileMaskedType.getBillTo().getCity());
            aNetBillingAddressView.setState(customerPaymentProfileMaskedType.getBillTo().getState());
            aNetBillingAddressView.setZip(customerPaymentProfileMaskedType.getBillTo().getZip());
            paymentProfile.setBillTo(aNetBillingAddressView);
            List<ANetSubscriptionId> subscriptionIdLists = new ArrayList<>();
            if (customerPaymentProfileMaskedType.getSubscriptionIds() != null && customerPaymentProfileMaskedType.getSubscriptionIds().getSubscriptionId() != null) {
                for (int i = 0; i < customerPaymentProfileMaskedType.getSubscriptionIds().getSubscriptionId().size(); i++) {
                    ANetSubscriptionId subscriptionId = new ANetSubscriptionId();
                    subscriptionId.setSubscriptionId(customerPaymentProfileMaskedType.getSubscriptionIds().getSubscriptionId().get(i));
                    subscriptionIdLists.add(subscriptionId);
                }
            }
            paymentProfile.setSubscriptionIds(subscriptionIdLists);
            paymentProfiles.add(paymentProfile);
        }
        customerProfileResponseView.setPaymentProfileList(paymentProfiles);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PAYMENT_PROFILES_RETRIEVED, customerProfileResponseView);
    }

    public ANetTransactionResponse initiateOneTimeProgramSubscriptionByPaymentProfile(ANetCustomerProfileRequestView customerProfileRequestView, Double amount, String orderId) {
        ANetTransactionResponse transactionResponse = CreateTransactionUsingPaymentProfile.run(customerProfileRequestView, amount, orderId);
        logger.info("Onetime payment created by payment profile : {}", transactionResponse);
        return transactionResponse;
    }

    /**
     * Method that will initiate recurring payment transaction
     *
     * @param recurringSubscriptionRequestView - Request view class
     * @return CreateTransactionResponse - An Auth.net class that contains the transaction response
     */
    @Transactional
    public ARBCreateSubscriptionResponse initiateRecurringProgramSubscriptionUsingPaymentProfile(ANetRecurringSubscriptionRequestViewWithPaymentProfile recurringSubscriptionRequestView, OrderManagement orderManagement) {
        Programs program = validationService.validateProgramIdBlocked(recurringSubscriptionRequestView.getProgramId());
        ANetRecurringSubscriptionRequestViewWithPaymentProfile subscriptionRequestView = new ANetRecurringSubscriptionRequestViewWithPaymentProfile();
        subscriptionRequestView.setProgramId(program.getProgramId());
        subscriptionRequestView.setCustomerProfileId(recurringSubscriptionRequestView.getCustomerProfileId());
        subscriptionRequestView.setCustomerPaymentProfileId(recurringSubscriptionRequestView.getCustomerPaymentProfileId());
        subscriptionRequestView.setDevicePlatformId(recurringSubscriptionRequestView.getDevicePlatformId());
        ARBCreateSubscriptionResponse apiResponse = CreateSubscriptionUsingCustomerProfile.run(subscriptionRequestView, program, orderManagement);
        logger.info("Recurring Payment response : {}", apiResponse);
        return apiResponse;
    }

    /**
     * Method that will initiate recurring payment transaction
     *
     * @param recurringSubscriptionRequestView - Request view class
     * @return CreateTransactionResponse - An Auth.net class that contains the transaction response
     */
    @Transactional
    public ARBCreateSubscriptionResponse initiateARBForFutureUsingPaymentProfile(ANetRecurringSubscriptionRequestViewWithPaymentProfile recurringSubscriptionRequestView, OrderManagement orderManagement) {
        Programs program = validationService.validateProgramIdBlocked(recurringSubscriptionRequestView.getProgramId());
        ANetRecurringSubscriptionRequestViewWithPaymentProfile subscriptionRequestView = new ANetRecurringSubscriptionRequestViewWithPaymentProfile();
        subscriptionRequestView.setProgramId(program.getProgramId());
        subscriptionRequestView.setCustomerProfileId(recurringSubscriptionRequestView.getCustomerProfileId());
        subscriptionRequestView.setCustomerPaymentProfileId(recurringSubscriptionRequestView.getCustomerPaymentProfileId());
        subscriptionRequestView.setDevicePlatformId(recurringSubscriptionRequestView.getDevicePlatformId());
        ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(userComponents.getUser().getUserId(), program.getProgramId());
        long ongoingSubscriptionValidForDays = 0;
        /*
         * The below block of code executes only if a program is already subscribed using One time payment and after that
         *  user wants to auto-subscribe the program while the ongoing subscription is ON!
         */
        // Checking if the program subscription status is in Paid/Payment Pending
        if (programSubscription != null && programSubscription.getSubscriptionStatus() != null) {
            if ((programSubscription.getSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID) ||
                    (programSubscription.getSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAYMENT_PENDING)))) {
                if (programSubscription.getSubscribedDate() != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(programSubscription.getSubscribedDate());
                    cal.add(Calendar.DAY_OF_YEAR, Math.toIntExact(programSubscription.getSubscriptionPlan().getDuration()));
                    Date subscriptionExpiryDate = cal.getTime();
                    // If today the subscription is getting over
                    if (fitwiseUtils.isSameDay(subscriptionExpiryDate, new Date())) {
                        ongoingSubscriptionValidForDays = 0;
                    }
                    // Subscription is active and it will get over in future
                    if (subscriptionExpiryDate.after(new Date())) {
                        long duration = subscriptionExpiryDate.getTime() - new Date().getTime();
                        ongoingSubscriptionValidForDays = TimeUnit.MILLISECONDS.toDays(duration);
                    }
                }
            }
        }
        ARBCreateSubscriptionResponse apiResponse = CreateARBForFutureUsingCustomerProfile.run(subscriptionRequestView, program, orderManagement, ongoingSubscriptionValidForDays);
        logger.info("Recurring Payment response : {}", apiResponse);
        return apiResponse;
    }

    public ANetApiResponse cancelRecurringProgramSubscription(CancelRecurringSubscriptionRequestView cancelRecurringSubscriptionRequestView) {
        ANetApiResponse apiResponse = CancelRecurringSubscription.run(cancelRecurringSubscriptionRequestView);
        logger.info("Cancel Recurring Payment response : {}", apiResponse);
        return apiResponse;
    }

    @Transactional
    public ResponseModel getAuthNetWebHookData(Object webHookView) {
        logger.info("Webhook received data object: {}", webHookView.toString());
        Gson gson = new Gson();
        String json = gson.toJson(webHookView);
        logger.info("Webhook received data converted to Json: {}", json);
        logAuthorizeNetWebHookNotification(json);
        /*
         * Parsing and get the Event type from json string
         */
        JSONObject jsonObject = new JSONObject(json);
        String eventType = jsonObject.getString("eventType");
        separateWebHookNotificationsBasedOnEventType(eventType, json, gson);
        return new ResponseModel();
    }


    /**
     * Method that will compare the eventType occurred in Authorize.net and save the required data in Fit-wise Database
     *
     * @param eventType
     */
    @Transactional
    public void separateWebHookNotificationsBasedOnEventType(String eventType, String json, Gson gson) {
        /*
         * Will be triggered when a transaction was authorized and captured and sent
         * for bank to deduct. This state can be considered as Payment success state.
         */
        if (eventType.equalsIgnoreCase(KeyConstants.EVENT_TYPE_AUTH_CAPTURE)) {
            AuthorizationCaptureWebHookResponseView authorizationCaptureWebHookResponseView = gson.fromJson(json, AuthorizationCaptureWebHookResponseView.class);
            logger.info("Authorization and Capture response : {}", authorizationCaptureWebHookResponseView.getPayload().toString());
            captureWebhookAndSaveDataForTransactionThroughARB(authorizationCaptureWebHookResponseView.getPayload().getId());
        }
        /*
         * Will be triggered when new automatic billing subscription is created
         */
        if (eventType.equalsIgnoreCase(KeyConstants.EVENT_TYPE_SUBSCRIPTION_CREATED)) {
            SubscriptionCreatedResponseView subscriptionCreatedResponseView = gson.fromJson(json, SubscriptionCreatedResponseView.class);
            logger.info("Subscription created response : {}", subscriptionCreatedResponseView.getPayload().toString());
        }
        /*
         * Will be triggered when a refund is created
         */
        if (eventType.equalsIgnoreCase(KeyConstants.EVENT_TYPE_REFUND_CREATED)) {
            RefundCreatedResponseView refundCreatedResponseView = gson.fromJson(json, RefundCreatedResponseView.class);
            logger.info("Refund created response : {}", refundCreatedResponseView.getPayload().toString());
            /**
             * Since Refund is already logged in directly using API, introducing a 5 secs delay so that the refund entry
             * through the direct api will first put in place. If the refund is triggered directly from Authorize.net portal
             * and not through Trainnr Admin website, then the below piece of code puts an refund entry in the table
             */
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            logRefundOfATransaction(refundCreatedResponseView.getPayload());
                        }
                    },
                    40000
            );
        }
        /*
         * Will be triggered when a subscription gets cancelled
         */
        if (eventType.equalsIgnoreCase(KeyConstants.EVENT_TYPE_SUBSCRIPTION_CANCELLED)) {
            SubscriptionCancellationResponseView subscriptionCancellationResponseView = gson.fromJson(json, SubscriptionCancellationResponseView.class);
            logger.info("Subscription cancelled response : {}", subscriptionCancellationResponseView.getPayload().toString());
        }
        /*
         * Will be triggered when a subscription gets suspended. This state will be called when a transaction gets failed
         * due to insufficient funds or incorrect bank details or first transaction failure in ARB.
         *
         * If a subscription is in suspended state and the user has not updated his data within the next due date,
         * then the subscription will be terminated
         *
         * If Automatic retry option is turned ON, even if the user has not updated his data within the next due date in spite of suspension,
         * the subscription will not be terminated.
         */
        if (eventType.equalsIgnoreCase(KeyConstants.EVENT_TYPE_SUBSCRIPTION_SUSPENDED)) {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject payload = jsonObject.getJSONObject("payload");
            String subscriptionId = payload.getString("id");
            logARBSuspendStatus(subscriptionId);
        }
        /*
         * Will be triggered when a subscription gets terminated.
         */
        if (eventType.equalsIgnoreCase(KeyConstants.EVENT_TYPE_SUBSCRIPTION_TERMINATED)) {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject payload = jsonObject.getJSONObject("payload");
            String subscriptionId = payload.getString("id");
            logARBTerminatedStatus(subscriptionId);
        }
        /*
         * Will be triggered when a subscription gets updated.
         */
        if (eventType.equalsIgnoreCase(KeyConstants.EVENT_TYPE_SUBSCRIPTION_UPDATED)) {
            ANetSubscriptionUpdateResponseView subscriptionUpdateResponseView = gson.fromJson(json, ANetSubscriptionUpdateResponseView.class);
            logger.info("Updated Subscription response : {}", subscriptionUpdateResponseView.getPayload().toString());
        }

        /*
         * Will be triggered when a subscription is going to expire soon.
         * In Fit-wise case, this won't be getting called since the subscription length is infinite!
         */
        if (eventType.equalsIgnoreCase(KeyConstants.EVENT_TYPE_SUBSCRIPTION_EXPIRING)) {

        }

        /*
         * Will be triggered when a subscription is voided.
         *
         * Void is a state where an unsettled transaction is cancelled/ payment reverted.
         * Refund is a state where a settled transaction is cancelled / payment reverted.
         */
        if (eventType.equalsIgnoreCase(KeyConstants.EVENT_TYPE_PAYMENT_VOID_CREATED)) {
            ANetVoidCreatedResponseView voidCreatedResponseView = gson.fromJson(json, ANetVoidCreatedResponseView.class);
            logger.info("Updated Subscription response : {}", voidCreatedResponseView.getPayload().toString());
            logVoidOfATransaction(voidCreatedResponseView.getPayload().getId());
        }
    }

    public void captureWebhookAndSaveDataForTransactionThroughARB(String transactionId) {
        getTransactionDetails(transactionId);
    }

    /**
     * Method used to log refund information in table
     *
     * @param refundCreatedPayload - refundCreatedPayload.getId()-->Refund Transaction id. This differs from payment transaction id
     */
    @Transactional
    public void logRefundOfATransaction(RefundCreatedPayload refundCreatedPayload) {
        String transactionId = refundCreatedPayload.getId();
        GetTransactionDetailsResponse response = GetTransactionDetails.run(transactionId);
        if (response != null && response.getTransaction() != null && !response.getTransaction().getRefTransId().isEmpty()) {
            AuthNetPayment authNetPayment = authNetPaymentRepository.findTop1ByTransactionId(response.getTransaction().getRefTransId());
            // Adding a new entry for the void transaction type
            if (authNetPayment != null && authNetPayment.getTransactionStatus().equalsIgnoreCase(KeyConstants.KEY_TRANSACTION_SETTLED_SUCCESSFULLY)) {
                AuthNetPayment refundedAuthNetPayment = new AuthNetPayment();
                refundedAuthNetPayment.setTransactionId(authNetPayment.getTransactionId());
                if (authNetPayment.getAmountPaid() != null) {
                    refundedAuthNetPayment.setAmountPaid(authNetPayment.getAmountPaid());
                } else {
                    refundedAuthNetPayment.setAmountPaid(authNetPayment.getOrderManagement().getProgram().getProgramPrices().getPrice());
                }
                refundedAuthNetPayment.setTransactionStatus(KeyConstants.KEY_REFUND_INITIATED);
                refundedAuthNetPayment.setArbSubscriptionId(authNetPayment.getArbSubscriptionId());
                refundedAuthNetPayment.setReceiptNumber(authNetPayment.getReceiptNumber());
                refundedAuthNetPayment.setOrderManagement(authNetPayment.getOrderManagement());
                refundedAuthNetPayment.setIsARB(authNetPayment.getIsARB());
                refundedAuthNetPayment.setResponseCode(authNetPayment.getResponseCode());
                refundedAuthNetPayment.setRefundTransactionId(transactionId);
                refundedAuthNetPayment.setAmountRefunded(refundCreatedPayload.getAuthAmount());
                refundedAuthNetPayment.setIsRefundUnderProcessing(false);
                authNetPaymentRepository.save(refundedAuthNetPayment);
                User user = authNetPayment.getOrderManagement().getUser();
                Programs program = authNetPayment.getOrderManagement().getProgram();
                // Changing the status of the subscription to REFUND
                ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), program.getProgramId());
                SubscriptionStatus subscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_REFUND);
                programSubscription.setSubscriptionStatus(subscriptionStatus);
                programSubscriptionRepo.save(programSubscription);
                SubscriptionAudit oldSubscriptionAudit = subscriptionAuditRepo.
                        findTop1ByUserUserIdAndSubscriptionTypeNameAndProgramSubscriptionProgramProgramIdOrderByCreatedDateDesc(user.getUserId(), KeyConstants.KEY_PROGRAM, program.getProgramId());
                //Saving revenueAudit table to store all tax details
                ProgramSubscriptionPaymentHistory programSubscriptionPaymentHistory = new ProgramSubscriptionPaymentHistory();
                programSubscriptionPaymentHistory.setOrderManagement(authNetPayment.getOrderManagement());
                subscriptionPaymentHistoryRepository.save(programSubscriptionPaymentHistory);
                // Adding new entry to the Subscription Audit table for Refund
                SubscriptionAudit newSubscriptionAudit = new SubscriptionAudit();
                newSubscriptionAudit.setProgramSubscription(oldSubscriptionAudit.getProgramSubscription());
                newSubscriptionAudit.setSubscriptionType(oldSubscriptionAudit.getSubscriptionType());
                newSubscriptionAudit.setSubscriptionPlan(oldSubscriptionAudit.getSubscriptionPlan());
                newSubscriptionAudit.setSubscriptionDate(oldSubscriptionAudit.getSubscriptionDate());
                newSubscriptionAudit.setSubscribedViaPlatform(oldSubscriptionAudit.getSubscribedViaPlatform());
                newSubscriptionAudit.setRenewalStatus(oldSubscriptionAudit.getRenewalStatus());
                newSubscriptionAudit.setUser(user);
                newSubscriptionAudit.setSubscriptionStatus(subscriptionStatus);
                newSubscriptionAudit.setProgramSubscriptionPaymentHistory(programSubscriptionPaymentHistory);
                subscriptionAuditRepo.save(newSubscriptionAudit);
                fitwiseQboEntityService.createAndSyncAnetRefund(authNetPayment);
            }
        }
    }

    @Transactional
    public void logVoidOfATransaction(String transactionId) {
        AuthNetPayment authNetPayment = authNetPaymentRepository.findByTransactionIdOrderByModifiedDateDesc(transactionId);
        // Adding a new entry for the void transaction type
        if (authNetPayment != null) {
            AuthNetPayment voidedAuthNetPayment = new AuthNetPayment();
            voidedAuthNetPayment.setTransactionId(authNetPayment.getTransactionId());
            if (authNetPayment.getAmountPaid() != null) {
                voidedAuthNetPayment.setAmountPaid(authNetPayment.getAmountPaid());
            } else {
                voidedAuthNetPayment.setAmountPaid(voidedAuthNetPayment.getOrderManagement().getProgram().getProgramPrices().getPrice());
            }
            voidedAuthNetPayment.setTransactionStatus(KeyConstants.KEY_VOID);
            voidedAuthNetPayment.setArbSubscriptionId(authNetPayment.getArbSubscriptionId());
            voidedAuthNetPayment.setReceiptNumber(authNetPayment.getReceiptNumber());
            voidedAuthNetPayment.setOrderManagement(authNetPayment.getOrderManagement());
            voidedAuthNetPayment.setIsARB(authNetPayment.getIsARB());
            voidedAuthNetPayment.setResponseCode(authNetPayment.getResponseCode());
            authNetPaymentRepository.save(voidedAuthNetPayment);

            User user = authNetPayment.getOrderManagement().getUser();
            Programs program = authNetPayment.getOrderManagement().getProgram();

            // Changing the status of the subscription to VOID
            ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), program.getProgramId());
            SubscriptionStatus subscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_VOID);
            programSubscription.setSubscriptionStatus(subscriptionStatus);
            programSubscriptionRepo.save(programSubscription);

            SubscriptionAudit oldSubscriptionAudit = subscriptionAuditRepo.
                    findTop1ByUserUserIdAndSubscriptionTypeNameAndProgramSubscriptionProgramProgramIdOrderByCreatedDateDesc(user.getUserId(), KeyConstants.KEY_PROGRAM, program.getProgramId());
            // Adding new entry to the Subscription Audit table for Void
            SubscriptionAudit newSubscriptionAudit = new SubscriptionAudit();
            newSubscriptionAudit.setProgramSubscription(oldSubscriptionAudit.getProgramSubscription());
            newSubscriptionAudit.setSubscriptionType(oldSubscriptionAudit.getSubscriptionType());
            newSubscriptionAudit.setSubscriptionPlan(oldSubscriptionAudit.getSubscriptionPlan());
            newSubscriptionAudit.setSubscriptionDate(oldSubscriptionAudit.getSubscriptionDate());
            newSubscriptionAudit.setSubscribedViaPlatform(oldSubscriptionAudit.getSubscribedViaPlatform());
            newSubscriptionAudit.setRenewalStatus(oldSubscriptionAudit.getRenewalStatus());
            newSubscriptionAudit.setUser(user);
            newSubscriptionAudit.setSubscriptionStatus(subscriptionStatus);
            subscriptionAuditRepo.save(newSubscriptionAudit);
        }
    }

    @Transactional
    public void logARBSuspendStatus(String subscriptionId) {
        AuthNetArbSubscription arbSubscription = authNetArbSubscriptionRepository.findTop1ByANetSubscriptionId(subscriptionId);
        if (arbSubscription != null) {
            User user = arbSubscription.getUser();
            Programs program = arbSubscription.getProgram();
            PlatformType platformType = arbSubscription.getSubscribedViaPlatform();
            // Saving the suspended subscription status to table
            AuthNetArbSubscription suspendedSubscription = new AuthNetArbSubscription();
            suspendedSubscription.setSubscribedViaPlatform(platformType);
            suspendedSubscription.setUser(user);
            suspendedSubscription.setProgram(program);
            suspendedSubscription.setANetSubscriptionId(arbSubscription.getANetSubscriptionId());
            AuthNetSubscriptionStatus authNetSubscriptionStatus = authNetSubscriptionStatusRepository.findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_SUSPENDED);
            suspendedSubscription.setAuthNetSubscriptionStatus(authNetSubscriptionStatus);
            authNetArbSubscriptionRepository.save(suspendedSubscription);
            //Cancelling the subscription
            subscriptionService.cancelRecurringProgramSubscriptionForARBSuspendedTerminatedStatus(user, program.getProgramId(), platformType.getPlatformTypeId());
        }
    }

    @Transactional
    public void logARBTerminatedStatus(String subscriptionId) {
        AuthNetArbSubscription arbSubscription = authNetArbSubscriptionRepository.findTop1ByANetSubscriptionId(subscriptionId);

        if (arbSubscription != null) {
            User user = arbSubscription.getUser();
            Programs program = arbSubscription.getProgram();
            PlatformType platformType = arbSubscription.getSubscribedViaPlatform();

            // Saving the terminated subscription status to table
            AuthNetArbSubscription suspendedSubscription = new AuthNetArbSubscription();
            suspendedSubscription.setSubscribedViaPlatform(platformType);
            suspendedSubscription.setUser(user);
            suspendedSubscription.setProgram(program);
            suspendedSubscription.setANetSubscriptionId(arbSubscription.getANetSubscriptionId());
            AuthNetSubscriptionStatus authNetSubscriptionStatus = authNetSubscriptionStatusRepository.findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_TERMINATED);
            suspendedSubscription.setAuthNetSubscriptionStatus(authNetSubscriptionStatus);
            authNetArbSubscriptionRepository.save(suspendedSubscription);

            //Cancelling the subscription
            subscriptionService.cancelRecurringProgramSubscriptionForARBSuspendedTerminatedStatus(user, program.getProgramId(), platformType.getPlatformTypeId());
        }
    }


    /**
     * Don't use this for normal API calls. Method dedicated only for webhooks
     *
     * @param transactionId
     */
    public void getTransactionDetails(String transactionId) {
        GetTransactionDetailsResponse apiResponse = GetTransactionDetails.run(transactionId);
        if (apiResponse.getTransaction().getSubscription() != null) {
            subscriptionService.saveTransactionFromARB(apiResponse, String.valueOf(apiResponse.getTransaction().getSubscription().getId()));
        }
    }

    /**
     * Returns the order history of an user
     *
     * @return
     */
    @Transactional
    public ResponseModel getOrderHistory(final int pageNo, final int pageSize, String subscriptionTypeParam, Optional<Boolean> isAllSubscriptionType) {
        log.info("Get order history starts.");
        long apiStartTimeMillis = new Date().getTime();
        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ERROR, null);
        }
        User user = userComponents.getUser();
        OrderHistoryResponseView orderHistoryResponseView = new OrderHistoryResponseView();
        Specification<OrderManagement> userSpecification = OrderManagementSpecifications.getOrderByUserGroupByOrderId(user.getUserId());
        List<String> subscriptionTypeList;
        if (isAllSubscriptionType.isPresent() && isAllSubscriptionType.get()) {
            subscriptionTypeList = Arrays.asList(KeyConstants.KEY_PROGRAM, KeyConstants.KEY_SUBSCRIPTION_PACKAGE);
        } else {
            subscriptionTypeList = Arrays.asList(subscriptionTypeParam);
        }
        Specification<OrderManagement> subscriptionTypeSpecification = OrderManagementSpecifications.getOrderBySubscriptionTypeIn(subscriptionTypeList);
        Specification<OrderManagement> finalSpec = userSpecification.and(subscriptionTypeSpecification);
        Sort sort = Sort.by("createdDate").descending();
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, sort);
        Page<OrderManagement> orderManagementPage = orderManagementRepository.findAll(finalSpec, pageRequest);
        log.info("Query to get order management : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        List<OrderHistoryTileResponseView> orderHistoryTileResponseViews = new ArrayList<>();
        for (OrderManagement orderManagement : orderManagementPage) {
            OrderHistoryTileResponseView orderHistoryTileResponseView = new OrderHistoryTileResponseView();
            orderHistoryTileResponseView.setOrderId(orderManagement.getOrderId());
            String subscriptionType = orderManagement.getSubscriptionType().getName();
            orderHistoryTileResponseView.setSubscriptionType(subscriptionType);
            String title;
            if (KeyConstants.KEY_PROGRAM.equalsIgnoreCase(subscriptionType)) {
                //TODO Its temp fix, have to implement properly for deleted programs
                if (orderManagement.getProgram().getStatus().equalsIgnoreCase(KeyConstants.KEY_DELETED)) {
                    continue;
                }
                title = orderManagement.getProgram().getTitle();
            } else {
                title = orderManagement.getSubscriptionPackage().getTitle();
                //TODO Its temp fix, have to implement properly for deleted programs
                if (orderManagement.getSubscriptionPackage().getStatus().equalsIgnoreCase(KeyConstants.KEY_DELETED)) {
                    continue;
                }
            }
            orderHistoryTileResponseView.setProgramName(title);
            // Getting the platform through which the order is created
            if (orderManagement.getSubscribedViaPlatform().getPlatformTypeId() == 1 ||
                    orderManagement.getSubscribedViaPlatform().getPlatformTypeId() == 3) {
                orderHistoryTileResponseView.setOrderDate(fitwiseUtils.formatDate(orderManagement.getCreatedDate()));
                orderHistoryTileResponseView.setOrderDateTimeStamp(orderManagement.getCreatedDate());
                //Re-ordering is not valid after two days of order creation
                Calendar cal = Calendar.getInstance();
                cal.setTime(orderManagement.getCreatedDate());
                cal.add(Calendar.DAY_OF_YEAR, Math.toIntExact(2));
                Date reOrderingValidPeriod = cal.getTime();
                if (reOrderingValidPeriod.after(new Date())) {
                    orderHistoryTileResponseView.setCanReOrder(true);
                } else {
                    orderHistoryTileResponseView.setCanReOrder(false);
                }
                if (KeyConstants.KEY_AUTH_NET_PAYMENT_MODE.equals(orderManagement.getModeOfPayment())) {
                    // Via Authorize.net
                    AuthNetPayment authNetPayment = authNetPaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
                    // Getting the payment status
                    if (authNetPayment != null && authNetPayment.getResponseCode() != null && authNetPayment.getResponseCode().equalsIgnoreCase(KeyConstants.ANET_TRANSACTION_SUCCESS_RESPONSE_CODE)) {
                        orderHistoryTileResponseView.setOrderStatus(KeyConstants.KEY_SUCCESS);
                        orderHistoryTileResponseView.setCanReOrder(false); // Since the payment is success, setting the re-ordering as false
                        if (authNetPayment.getTransactionStatus().equalsIgnoreCase(KeyConstants.KEY_REFUND_INITIATED)) {
                            orderHistoryTileResponseView.setOrderStatus(KeyConstants.KEY_REFUNDED);
                        }
                    } else {
                        orderHistoryTileResponseView.setOrderStatus(KeyConstants.KEY_FAILURE);
                    }
                } else if (KeyConstants.KEY_STRIPE.equals(orderManagement.getModeOfPayment())) {
                    StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
                    // Getting the payment status
                    if (stripePayment != null && KeyConstants.KEY_PAID.equals(stripePayment.getTransactionStatus())) {
                        orderHistoryTileResponseView.setOrderStatus(KeyConstants.KEY_SUCCESS);
                        orderHistoryTileResponseView.setCanReOrder(false); // Since the payment is success, setting the re-ordering as false
                    } else if (stripePayment != null && KeyConstants.KEY_REFUND.equals(stripePayment.getTransactionStatus())) {
                        orderHistoryTileResponseView.setOrderStatus(KeyConstants.KEY_REFUNDED);
                    } else {
                        orderHistoryTileResponseView.setOrderStatus(KeyConstants.KEY_FAILURE);
                    }
                }
            } else if (orderManagement.getSubscribedViaPlatform().getPlatformTypeId() == 2) {
                String orderDate = fitwiseUtils.formatDate(orderManagement.getCreatedDate());
                orderHistoryTileResponseView.setOrderDate(orderDate);
                orderHistoryTileResponseView.setOrderDateTimeStamp(orderManagement.getCreatedDate());
                ApplePayment applePayment = applePaymentRepository.findTop1ByOrderManagementOrderIdOrderByCreatedDateDesc(orderManagement.getOrderId());
                if (applePayment != null) {
                    if (applePayment.getPurchaseDate() != null) {
                        String orderDt = fitwiseUtils.formatDate(applePayment.getPurchaseDate());
                        orderHistoryTileResponseView.setOrderDate(orderDt);
                        orderHistoryTileResponseView.setOrderDateTimeStamp(applePayment.getPurchaseDate());
                    }
                }
                Calendar cal = Calendar.getInstance();
                cal.setTime(orderManagement.getCreatedDate());
                cal.add(Calendar.DAY_OF_YEAR, Math.toIntExact(2));
                Date reOrderingValidPeriod = cal.getTime();
                orderHistoryTileResponseView.setCanReOrder(reOrderingValidPeriod.after(new Date()));
                orderHistoryTileResponseView.setOrderStatus(orderManagement.getOrderStatus());
                if (orderManagement.getOrderStatus().equalsIgnoreCase(KeyConstants.KEY_SUCCESS) || orderManagement.getOrderStatus().equalsIgnoreCase(KeyConstants.KEY_PROCESSING)) {
                    orderHistoryTileResponseView.setCanReOrder(false);
                }
            }
            orderHistoryTileResponseViews.add(orderHistoryTileResponseView);
        }
        log.info("Construct order history : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        orderHistoryResponseView.setOrders(orderHistoryTileResponseViews);
        orderHistoryResponseView.setTotalOrderCount(orderManagementPage.getTotalElements());
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setPayload(orderHistoryResponseView);
        responseModel.setMessage(MessageConstants.MSG_ORDER_HISTORY_FETCHED_SUCCESSFULLY);
        log.info("Construct response model : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get order history ends.");
        return responseModel;
    }

    /**
     * Returns the details of an order
     *
     * @param orderId
     * @return
     */
    @Transactional
    public ResponseModel getOrderDetail(String orderId) {
        log.info("Get order details starts.");
        long apiStartTimeMillis = new Date().getTime();
        User user = userComponents.getUser();
        OrderManagement orderManagement = orderManagementRepository.findTop1ByOrderIdOrderByCreatedDateDesc(orderId);
        if (orderManagement == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_NO_ORDER_FOUND, null);
        }
        log.info("Query to get order management and user : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();

        String subscriptionType = orderManagement.getSubscriptionType().getName();

        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);

        OrderDetailResponseView orderDetailResponseView = new OrderDetailResponseView();

        Long id;
        String title;
        User instructor;
        Long duration;
        String thumbnailUrl = null;
        double price;
        if (KeyConstants.KEY_PROGRAM.equalsIgnoreCase(subscriptionType)) {
            id = orderManagement.getProgram().getProgramId();
            title = orderManagement.getProgram().getTitle();
            instructor = orderManagement.getProgram().getOwner();
            duration = orderManagement.getProgram().getDuration().getDuration();
            price = orderManagement.getProgram().getProgramPrices().getPrice();
            if(orderManagement.getProgram().getImage() != null){
                thumbnailUrl = orderManagement.getProgram().getImage().getImagePath();
            }
        } else {
            id = orderManagement.getSubscriptionPackage().getSubscriptionPackageId();
            title = orderManagement.getSubscriptionPackage().getTitle();
            instructor = orderManagement.getSubscriptionPackage().getOwner();
            duration = orderManagement.getSubscriptionPackage().getPackageDuration().getDuration();
            price = orderManagement.getSubscriptionPackage().getPrice();
            if(orderManagement.getSubscriptionPackage().getImage() != null){
                thumbnailUrl = orderManagement.getSubscriptionPackage().getImage().getImagePath();
            }
        }
        log.info("Get program details : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        orderDetailResponseView.setProgramId(id);
        orderDetailResponseView.setProgramName(title);
        orderDetailResponseView.setThumbnailUrl(thumbnailUrl);

        orderDetailResponseView.setOrderId(orderManagement.getOrderId());
        if (orderManagement.getSubscribedViaPlatform() != null)
            orderDetailResponseView.setSubscribedViaPlatformId(orderManagement.getSubscribedViaPlatform().getPlatformTypeId());

        assert orderManagement.getSubscribedViaPlatform() != null;
        if (orderManagement.getSubscribedViaPlatform().getPlatformTypeId() == 1 || orderManagement.getSubscribedViaPlatform().getPlatformTypeId() == 3) {

            UserProfile userProfile = userProfileRepository.findByUser(instructor);
            orderDetailResponseView.setInstructorName(userProfile.getFirstName() + KeyConstants.KEY_SPACE + userProfile.getLastName());
            orderDetailResponseView.setDuration(duration + KeyConstants.KEY_SPACE + KeyConstants.DAYS);

            String date = fitwiseUtils.formatDate(orderManagement.getCreatedDate());
            orderDetailResponseView.setPurchasedDate(date);
            orderDetailResponseView.setPurchasedDateTimeStamp(orderManagement.getCreatedDate());

            int currentSubscriptionRenewalsCount = 0;
            if (KeyConstants.KEY_AUTH_NET_PAYMENT_MODE.equals(orderManagement.getModeOfPayment())) {
                AuthNetPayment authNetPayment = authNetPaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderId);
                if (authNetPayment == null) {
                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERROR_FETCHING_ORDER_TRANSACTION_DETAILS, null);
                }
                orderDetailResponseView.setTransactionId(authNetPayment.getTransactionId());

                //Setting the order status based on the response from authorize.net
                if (authNetPayment.getResponseCode() != null && authNetPayment.getResponseCode().equalsIgnoreCase(KeyConstants.ANET_TRANSACTION_SUCCESS_RESPONSE_CODE)) {
                    orderDetailResponseView.setOrderStatus(KeyConstants.KEY_SUCCESS);
                    orderDetailResponseView.setOrderSuccess(true);
                    if (authNetPayment.getTransactionStatus().equalsIgnoreCase(KeyConstants.KEY_REFUND_INITIATED)) {
                        orderDetailResponseView.setOrderStatus(KeyConstants.KEY_REFUNDED);
                    }
                } else {
                    orderDetailResponseView.setOrderStatus(KeyConstants.KEY_FAILURE);
                    orderDetailResponseView.setOrderSuccess(false);
                }
                if (authNetPayment.getAmountPaid() != null) {
                    orderDetailResponseView.setPrice(authNetPayment.getAmountPaid().toString());
                    orderDetailResponseView.setFormattedPrice(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(authNetPayment.getAmountPaid().doubleValue()));
                    orderDetailResponseView.setOrderTotal(authNetPayment.getAmountPaid().toString());
                } else {
                    orderDetailResponseView.setPrice(String.valueOf(orderManagement.getProgram().getProgramPrices().getPrice()));
                    orderDetailResponseView.setFormattedPrice(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(orderManagement.getProgram().getProgramPrices().getPrice()));
                    orderDetailResponseView.setOrderTotal(String.valueOf(orderManagement.getProgram().getProgramPrices().getPrice()));
                }
                /**
                 * Getting the recent ARB data from the subscription changes tracker table
                 */
                AuthNetSubscriptionChangesTracker tracker = authNetSubscriptionChangesTrackerRepository.findTop1ByOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
                if (tracker != null && tracker.getIsSubscriptionActive()) {
                    orderDetailResponseView.setAutoRenewal(true);
                }
                /**
                 * Calculating the total number of subscriptions for the program done by the user
                 * and the renewals for the current subscription
                 */
                if (tracker != null) {
                    List<AuthNetSubscriptionChangesTracker> authNetArbSubscriptionTrackerList = authNetSubscriptionChangesTrackerRepository.findBySubscriptionIdAndIsSubscriptionActiveTrueAndCreatedDateLessThanEqualOrderByModifiedDateDesc(tracker.getSubscriptionId(), tracker.getModifiedDate());
                    if (authNetArbSubscriptionTrackerList != null) {
                        for (AuthNetSubscriptionChangesTracker authNetSubscriptionChangesTracker : authNetArbSubscriptionTrackerList) {
                            List<AuthNetPayment> authNetPayments = authNetPaymentRepository.findByArbSubscriptionIdAndResponseCode(authNetSubscriptionChangesTracker.getSubscriptionId(),
                                    KeyConstants.ANET_TRANSACTION_SUCCESS_RESPONSE_CODE);
                            for (AuthNetPayment aNetPayment : authNetPayments) {
                                if (tracker != null &&
                                        aNetPayment.getArbSubscriptionId().equals(tracker.getSubscriptionId())) {
                                    currentSubscriptionRenewalsCount++;
                                }
                            }
                        }
                    }
                }
                // Marking the transaction status as REFUND
                if (authNetPayment.getTransactionStatus().equalsIgnoreCase(KeyConstants.KEY_REFUND_INITIATED)) {
                    orderDetailResponseView.setOrderStatus(KeyConstants.KEY_REFUNDED);
                    orderDetailResponseView.setIsTransactionRefunded(true);
                    orderDetailResponseView.setRefundTransactionId(authNetPayment.getRefundTransactionId());
                    orderDetailResponseView.setRefundedAmount(authNetPayment.getAmountRefunded());
                    if (authNetPayment.getAmountRefunded() != null) {
                        orderDetailResponseView.setFormattedRefundAmount(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(authNetPayment.getAmountRefunded()));
                    }
                    String nextRenewalDateInString = fitwiseUtils.formatDateWithTime(authNetPayment.getModifiedDate());
                    orderDetailResponseView.setRefundedDate(nextRenewalDateInString);
                    orderDetailResponseView.setRefundedDateTimeStamp(authNetPayment.getModifiedDate());
                    RefundReasonForAMember refundReasonForAMember = refundReasonForAMemberRepository.findByTransactionId(authNetPayment.getTransactionId());
                    orderDetailResponseView.setRefundReason(refundReasonForAMember.getRefundReasonInWord());
                }
                orderDetailResponseView.setPaymentGateWay(KeyConstants.KEY_AUTH_NET);
            } else if (KeyConstants.KEY_STRIPE.equals(orderManagement.getModeOfPayment())) {
                orderDetailResponseView.setPaymentGateWay(KeyConstants.KEY_STRIPE);
                StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
                if (stripePayment == null) {
                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERROR_FETCHING_ORDER_TRANSACTION_DETAILS, null);
                }
                orderDetailResponseView.setTransactionId(stripePayment.getChargeId());
                //Setting the order status based on the response from authorize.net
                if (KeyConstants.KEY_PAID.equals(stripePayment.getTransactionStatus())) {
                    orderDetailResponseView.setOrderStatus(KeyConstants.KEY_SUCCESS);
                    orderDetailResponseView.setOrderSuccess(true);
                } else if (KeyConstants.KEY_REFUND.equals(stripePayment.getTransactionStatus())) {
                    orderDetailResponseView.setOrderStatus(KeyConstants.KEY_REFUNDED);
                } else {
                    orderDetailResponseView.setOrderStatus(KeyConstants.KEY_FAILURE);
                    orderDetailResponseView.setOrderSuccess(false);
                }
                orderDetailResponseView.setPrice(String.valueOf(price));
                orderDetailResponseView.setFormattedPrice(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(price));
                double orderTotal;
                if (stripePayment.getAmountPaid() != null) {
                    orderTotal = stripePayment.getAmountPaid();
                } else {
                    orderTotal = price;
                }
                orderDetailResponseView.setOrderTotal(String.valueOf(orderTotal));
                orderDetailResponseView.setFormattedOrderTotal(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(orderTotal));
                /**
                 * Getting the recent ARB data from the subscription changes tracker table
                 */
                StripeSubscriptionChangesTracker stripeSubscriptionChangesTracker = stripeSubscriptionChangesTrackerRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
                if (stripeSubscriptionChangesTracker != null && stripeSubscriptionChangesTracker.getIsSubscriptionActive()) {
                    orderDetailResponseView.setAutoRenewal(true);
                }
                /**
                 * Calculating the total number of subscriptions for the program done by the user
                 * and the renewals for the current subscription
                 */
                if (stripeSubscriptionChangesTracker != null) {
                    List<StripePayment> stripePaymentList = stripePaymentRepository.findBySubscriptionIdAndTransactionStatusAndCreatedDateLessThanEqual(stripeSubscriptionChangesTracker.getSubscriptionId(), KeyConstants.KEY_PAID, stripeSubscriptionChangesTracker.getModifiedDate());
                    stripePaymentList = stripePaymentList.stream()
                            .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparing(StripePayment::getInvoiceId))), ArrayList::new));
                    currentSubscriptionRenewalsCount = stripePaymentList.size();
                }
                // Marking the transaction status as REFUND
                if (KeyConstants.KEY_REFUND.equalsIgnoreCase(stripePayment.getTransactionStatus())) {
                    orderDetailResponseView.setOrderStatus(KeyConstants.KEY_REFUNDED);
                    orderDetailResponseView.setIsTransactionRefunded(true);
                    orderDetailResponseView.setRefundTransactionId(stripePayment.getRefundTransactionId());
                    orderDetailResponseView.setRefundedAmount(stripePayment.getAmountRefunded());
                    orderDetailResponseView.setFormattedRefundAmount(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(stripePayment.getAmountRefunded()));
                    String nextRenewalDateInString = fitwiseUtils.formatDateWithTime(stripePayment.getModifiedDate());
                    orderDetailResponseView.setRefundedDate(nextRenewalDateInString);
                    orderDetailResponseView.setRefundedDateTimeStamp(stripePayment.getModifiedDate());
                    RefundReasonForAMember refundReasonForAMember = refundReasonForAMemberRepository.findByTransactionId(stripePayment.getChargeId());
                    if (refundReasonForAMember != null) {
                        orderDetailResponseView.setRefundReason(refundReasonForAMember.getRefundReasonInWord());
                    }
                }

            }
            List<String> statusList = Arrays.asList(KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING);
            //Adding a buffer time of 3 minutes to consider delay in populating SubscriptionAudit table
            LocalDateTime localDateTime = orderManagement.getCreatedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            localDateTime = localDateTime.plusMinutes(3);
            Date orderCreationDateWithBuffer = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            int totalNumberOfRenewalsCount;
            if (KeyConstants.KEY_PROGRAM.equalsIgnoreCase(subscriptionType)) {
                totalNumberOfRenewalsCount = subscriptionAuditRepo.countByUserUserIdAndSubscriptionTypeNameAndProgramSubscriptionProgramProgramIdAndSubscriptionStatusSubscriptionStatusNameInAndCreatedDateLessThanEqual(orderManagement.getUser().getUserId(), KeyConstants.KEY_PROGRAM, orderManagement.getProgram().getProgramId(), statusList, orderCreationDateWithBuffer);
            } else {
                totalNumberOfRenewalsCount = subscriptionAuditRepo.countBySubscriptionTypeNameAndUserUserIdAndPackageSubscriptionSubscriptionPackageSubscriptionPackageIdAndSubscriptionStatusSubscriptionStatusNameInAndCreatedDateLessThanEqual(KeyConstants.KEY_SUBSCRIPTION_PACKAGE, orderManagement.getUser().getUserId(), orderManagement.getSubscriptionPackage().getSubscriptionPackageId(), statusList, orderCreationDateWithBuffer);
            }

            //Since the second time purchase will only be considered as a renewal, decreasing the total counts by 1
            if (totalNumberOfRenewalsCount != 0) {
                totalNumberOfRenewalsCount = totalNumberOfRenewalsCount - 1;
            }
            if (currentSubscriptionRenewalsCount != 0) {
                currentSubscriptionRenewalsCount = currentSubscriptionRenewalsCount - 1;
            }

            orderDetailResponseView.setTotalRenewalCount(totalNumberOfRenewalsCount);
            orderDetailResponseView.setCurrentSubscriptionRenewalCount(currentSubscriptionRenewalsCount);

            if (orderDetailResponseView.isAutoRenewal()) {
                boolean isSubscribed = false;
                if (KeyConstants.KEY_PROGRAM.equalsIgnoreCase(subscriptionType)) {
                    ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(orderManagement.getUser().getUserId(), orderManagement.getProgram().getProgramId());
                    if (programSubscription != null) {
                        isSubscribed = true;
                    }
                } else {
                    PackageSubscription packageSubscription = packageSubscriptionRepository.findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageIdOrderBySubscribedDateDesc(orderManagement.getUser().getUserId(), orderManagement.getSubscriptionPackage().getSubscriptionPackageId());
                    if (packageSubscription != null) {
                        isSubscribed = true;
                    }
                }

                if (isSubscribed) {
                    Calendar cal = Calendar.getInstance();
                    Date subscribedDate = orderManagement.getCreatedDate();

                    if (orderDetailResponseView.isAutoRenewal()) {
                        // Adding program duration to the subscribed date to get the Next renewal date
                        cal.setTime(subscribedDate);
                        cal.add(Calendar.DATE, Math.toIntExact(duration));

                        Date nextRenewalDate = cal.getTime();
                        orderDetailResponseView.setNextRenewalDateTimeStamp(nextRenewalDate);

                        String nextRenewalDateInString = null;
                        if (KeyConstants.KEY_AUTH_NET_PAYMENT_MODE.equals(orderManagement.getModeOfPayment())) {
                            nextRenewalDateInString = fitwiseUtils.formatDate(nextRenewalDate);
                            nextRenewalDateInString = nextRenewalDateInString + KeyConstants.KEY_SPACE + "05:00:00";
                        } else if (KeyConstants.KEY_STRIPE.equals(orderManagement.getModeOfPayment())) {
                            nextRenewalDateInString = fitwiseUtils.formatDateWithTime(nextRenewalDate);
                        }
                        orderDetailResponseView.setNextRenewalDate(nextRenewalDateInString); // Setting Next renewal date only if auto-subscription is ON
                    }

                    // Adding program duration to the subscribed date to get the subscription End date
                    cal = Calendar.getInstance();
                    cal.setTime(subscribedDate);
                    cal.add(Calendar.DATE, Math.toIntExact(duration));

                    Date subscriptionEndDate = cal.getTime();
                    String subscriptionEndDateInString = fitwiseUtils.formatDateWithTime(subscriptionEndDate);

                    if (subscriptionEndDate.after(new Date())) {
                        orderDetailResponseView.setCurrentStatus(KeyConstants.KEY_EXPIRES_ON + subscriptionEndDateInString);
                    } else {
                        orderDetailResponseView.setCurrentStatus(KeyConstants.KEY_EXPIRED_ON + subscriptionEndDateInString);
                    }
                }
            }
            log.info("Set order details based on web and android : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));

        } else if (orderManagement.getSubscribedViaPlatform().getPlatformTypeId() == 2) {

            if (orderManagement.getOrderStatus().equalsIgnoreCase(KeyConstants.KEY_SUCCESS)) {
                orderDetailResponseView.setOrderSuccess(true);
            } else {
                orderDetailResponseView.setOrderSuccess(false);
            }
            //Program Price
            ApplePayment applePayment = applePaymentRepository.findTop1ByOrderManagementOrderIdOrderByCreatedDateDesc(orderId);
            if (applePayment != null) {
                orderDetailResponseView.setPrice(applePayment.getProgramPrice().toString());
                orderDetailResponseView
                        .setFormattedPrice(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(applePayment.getProgramPrice().doubleValue()));
                orderDetailResponseView.setOrderTotal(applePayment.getProgramPrice().toString());
                orderDetailResponseView.setFormattedOrderTotal(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(applePayment.getProgramPrice().doubleValue()));
            }

            UserProfile userProfile = userProfileRepository.findByUser(instructor);
            orderDetailResponseView
                    .setInstructorName(userProfile.getFirstName() + KeyConstants.KEY_SPACE + userProfile.getLastName());
            orderDetailResponseView.setDuration("1" + KeyConstants.KEY_SPACE + KeyConstants.KEY_MONTH);

            // Manipulating whether the order is success or failure
            orderDetailResponseView.setOrderStatus(orderManagement.getOrderStatus());
            if (orderManagement.getOrderStatus().equalsIgnoreCase(KeyConstants.KEY_PROCESSING)) {
                orderDetailResponseView.setIsOrderUnderProcessing(true);
            } else {
                orderDetailResponseView.setIsOrderUnderProcessing(false);
            }

            String orderDate = fitwiseUtils.formatDate(orderManagement.getCreatedDate());
            orderDetailResponseView.setPurchasedDate(orderDate);
            orderDetailResponseView.setPurchasedDateTimeStamp(orderManagement.getCreatedDate());

            if (orderManagement.getOrderStatus().equalsIgnoreCase(KeyConstants.KEY_SUCCESS)) {
                applePayment = applePaymentRepository.findTop1ByOrderManagementOrderIdOrderByCreatedDateDesc(orderId);
                if (applePayment == null) {
                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERROR_FETCHING_ORDER_TRANSACTION_DETAILS, null);
                }

                if (applePayment != null && applePayment.getTransactionStatus().equalsIgnoreCase(KeyConstants.KEY_PAYMENT_SUCCESS)) {
                    orderDetailResponseView.setTransactionId(applePayment.getTransactionId());
                    AppleProductSubscription appleSubscription = appleProductSubscriptionRepository.findTop1ByTransactionIdOrderByModifiedDateDesc(applePayment.getTransactionId());
                    if (appleSubscription != null && appleSubscription.getAppleSubscriptionStatus() != null) {
                        // Set Auto renewal flag
                        if (appleSubscription.getAppleSubscriptionStatus().getSubscriptionStatusName()
                                .equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_ACTIVE)) {
                            orderDetailResponseView.setAutoRenewal(true);
                        } else {
                            orderDetailResponseView.setAutoRenewal(false);
                        }
                        //
                    }

                    if (applePayment.getPurchaseDate() != null) {
                        String purchaseDate = fitwiseUtils.formatDate(applePayment.getPurchaseDate());
                        orderDetailResponseView.setPurchasedDate(purchaseDate);
                        orderDetailResponseView.setPurchasedDateTimeStamp(applePayment.getPurchaseDate());
                    }
                    if (applePayment.getExpiryDate() != null) {

                        String expiryDate = fitwiseUtils.formatDateWithTime(applePayment.getExpiryDate());

                        if (applePayment.getExpiryDate().after(new Date())) {
                            orderDetailResponseView.setCurrentStatus(KeyConstants.KEY_EXPIRES_ON + expiryDate);
                        } else {
                            orderDetailResponseView.setCurrentStatus(KeyConstants.KEY_EXPIRED_ON + expiryDate);
                        }
                        // Setting auto-renewal date value only if auto subscription is ON
                        if (orderDetailResponseView.isAutoRenewal()) {
                            orderDetailResponseView.setNextRenewalDate(expiryDate);
                            orderDetailResponseView.setNextRenewalDateTimeStamp(applePayment.getExpiryDate());
                        }
                    }
                }

                /**
                 * Calculating the total number of subscriptions for the program done by the
                 * user and the renewals for the current subscription
                 */
                int totalNumberOfRenewalsCount = 0;
                int currentSubscriptionRenewalsCount = 0;
                log.info("Order Created Date :{} ", orderManagement.getCreatedDate());
                List<AppleProductSubscription> initialSubscription = appleProductSubscriptionRepository.findByProgramAndUserAndEvent(orderManagement.getProgram(), user, NotificationConstants.INITIAL_BUY);
                List<String> statusList = Arrays.asList(NotificationConstants.RENEWAL, NotificationConstants.INTERACTIVE_RENEWAL);
                List<AppleProductSubscription> iOSSubscriptions = appleProductSubscriptionRepository
                        .findByProgramAndUserAndEventInAndCreatedDateLessThanEqual(orderManagement.getProgram(), user, statusList, orderManagement.getCreatedDate());

                log.info("list Count :{}", iOSSubscriptions.size());
                if (!iOSSubscriptions.isEmpty()) {
                    if (!initialSubscription.isEmpty()) {
                        totalNumberOfRenewalsCount = iOSSubscriptions.size();
                        currentSubscriptionRenewalsCount = iOSSubscriptions.size();
                    } else {
                        totalNumberOfRenewalsCount = iOSSubscriptions.size() - 1;
                        currentSubscriptionRenewalsCount = iOSSubscriptions.size() - 1;
                    }

                }
                // In Apple Same Original Transaction Id will be maintained next set of renewals as well.
                // Unique transaction and weborderLineItem id will be generated for each subscription

                orderDetailResponseView.setTotalRenewalCount(totalNumberOfRenewalsCount);
                orderDetailResponseView.setCurrentSubscriptionRenewalCount(currentSubscriptionRenewalsCount);
                log.info("totalNumberOfRenewalsCount {}", totalNumberOfRenewalsCount);
                log.info("currentSubscriptionRenewalsCount {}", currentSubscriptionRenewalsCount);
            }
            log.info("Set order details based on apple : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));

        }

        profilingEndTimeMillis = new Date().getTime();
        //Adding offer code details
        OfferCodeDetailAndOrderMapping offerCodeDetailAndOrderMapping = offerCodeDetailAndOrderMappingRepository.findTop1ByOrderManagementOrderByCreatedDateDesc(orderManagement);
        if (offerCodeDetailAndOrderMapping != null) {
            OfferCodeDetail offerCodeDetail = offerCodeDetailAndOrderMapping.getOfferCodeDetail();
            OrderDiscountDetails orderDiscountDetails = new OrderDiscountDetails();
            orderDiscountDetails.setOfferName(offerCodeDetail.getOfferName().trim());
            orderDiscountDetails.setOfferCode(offerCodeDetail.getOfferCode().toUpperCase());

            double offerPrice = DiscountsConstants.MODE_FREE.equalsIgnoreCase(offerCodeDetail.getOfferMode()) ? 0.00 : offerCodeDetail.getOfferPrice().getPrice();
            double discountAmount = price - offerPrice;

            orderDiscountDetails.setDiscountAmount(0 - discountAmount);
            orderDiscountDetails.setFormattedDiscountAmount("-" + KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(discountAmount));
            //iOS payment->offer price is set as order total
            if (orderManagement.getSubscribedViaPlatform().getPlatformTypeId() == 2) {
                orderDetailResponseView.setOrderTotal(String.valueOf(offerPrice));
                orderDetailResponseView.setFormattedOrderTotal(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(offerPrice));
            }
            orderDetailResponseView.setOrderDiscountDetails(orderDiscountDetails);
        }
        log.info("Adding offer code details : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get order details ends.");

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_ORDER_DETAILS_FETCHED_SUCCESSFULLY, orderDetailResponseView);
    }


    /**
     * API used in Admin portal to check order history of a member
     *
     * @param memberId
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Transactional
    public ResponseModel getOrderHistoryOfAMember(Long memberId, final int pageNo, final int pageSize, String subscriptionType) {

        log.info("Get order history of a member starts.");
        long apiStartTimeMillis = new Date().getTime();
        User member = validationService.validateMemberId(memberId);
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);

        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ERROR, null);
        }

        List<String> subscriptionTypeList;
        subscriptionTypeList = Arrays.asList(subscriptionType);

        Specification<OrderManagement> userSpecification = OrderManagementSpecifications.getOrderByUserGroupByOrderId(member.getUserId());
        Specification<OrderManagement> subscriptionTypeSpecification = OrderManagementSpecifications.getOrderBySubscriptionTypeIn(subscriptionTypeList);
        Specification<OrderManagement> finalSpec = userSpecification.and(subscriptionTypeSpecification);
        Sort sort = Sort.by("createdDate").descending();
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, sort);
        Page<OrderManagement> orderManagements = orderManagementRepository.findAll(finalSpec, pageRequest);
        log.info("Query: Get member and order management : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();

        List<OrderHistoryTileResponseViewOfAMember> orderHistoryTileResponseViews = new ArrayList<>();

        for (OrderManagement orderManagement : orderManagements) {
            OrderHistoryTileResponseViewOfAMember orderHistoryTileResponseView = new OrderHistoryTileResponseViewOfAMember();
            orderHistoryTileResponseView.setOrderId(orderManagement.getOrderId()); // Setting order id

            String title;
            User instructor;
            double price;
            Long duration;
            if (KeyConstants.KEY_PROGRAM.equalsIgnoreCase(subscriptionType)) {
                //TODO Its temp fix, have to implement properly for deleted programs
                if (orderManagement.getProgram().getStatus().equalsIgnoreCase(KeyConstants.KEY_DELETED)) {
                    continue;
                }
                title = orderManagement.getProgram().getTitle();
                instructor = orderManagement.getProgram().getOwner();
                price = orderManagement.getProgram().getProgramPrices().getPrice();
                duration = orderManagement.getProgram().getDuration().getDuration();
            } else {
                //TODO Its temp fix, have to implement properly for deleted programs
                if (orderManagement.getSubscriptionPackage().getStatus().equalsIgnoreCase(KeyConstants.KEY_DELETED)) {
                    continue;
                }
                title = orderManagement.getSubscriptionPackage().getTitle();
                instructor = orderManagement.getSubscriptionPackage().getOwner();
                price = orderManagement.getSubscriptionPackage().getPrice();
                duration = orderManagement.getSubscriptionPackage().getPackageDuration().getDuration();
            }
            orderHistoryTileResponseView.setProgramName(title); // Setting program name
            boolean isOfferApplied = false;
            OfferCodeDetailAndOrderMapping offerCodeDetailAndOrderMapping = offerCodeDetailAndOrderMappingRepository.findTop1ByOrderManagementOrderByCreatedDateDesc(orderManagement);
            if (offerCodeDetailAndOrderMapping != null) {
                isOfferApplied = true;
            }
            UserProfile userProfile = userProfileRepository.findByUser(instructor);
            if (userProfile != null) {
                orderHistoryTileResponseView.setInstructorName(userProfile.getFirstName() + " " + userProfile.getLastName()); //Setting instructor name
            }
            orderHistoryTileResponseView.setPurchasedDate(fitwiseUtils.formatDate(orderManagement.getCreatedDate())); // Setting purchased date
            orderHistoryTileResponseView.setPurchasedDateTimeStamp(orderManagement.getCreatedDate()); // Setting purchased date
            // Getting the platform through which the order is created
            if (orderManagement.getSubscribedViaPlatform().getPlatformTypeId() == 1 ||
                    orderManagement.getSubscribedViaPlatform().getPlatformTypeId() == 3) {
                int currentSubscriptionRenewalsCount = 0;
                if (KeyConstants.KEY_AUTH_NET_PAYMENT_MODE.equals(orderManagement.getModeOfPayment())) {
                    // Via Authorize.net
                    AuthNetPayment authNetPayment = authNetPaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
                    orderHistoryTileResponseView.setProgramPrice(authNetPayment.getAmountPaid()); //Setting program price
                    orderHistoryTileResponseView.setFormattedProgramPrice(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(authNetPayment.getAmountPaid().doubleValue()));
                    orderHistoryTileResponseView.setOrderTotalPrice(authNetPayment.getAmountPaid()); // Setting order price
                    orderHistoryTileResponseView.setFormattedOrderTotalPrice(fitwiseUtils.formatPrice(authNetPayment.getAmountPaid()));
                    // Getting the payment status
                    if (authNetPayment != null && authNetPayment.getResponseCode() != null
                            && authNetPayment.getResponseCode().equalsIgnoreCase(KeyConstants.ANET_TRANSACTION_SUCCESS_RESPONSE_CODE)) {
                        orderHistoryTileResponseView.setOrderStatus(KeyConstants.KEY_SUCCESS); // Setting order success status
                        orderHistoryTileResponseView.setTransactionId(authNetPayment.getTransactionId()); // Setting transaction id
                        orderHistoryTileResponseView.setPlatformName(KeyConstants.KEY_AUTH_NET);
                        if (authNetPayment.getTransactionStatus().equalsIgnoreCase(KeyConstants.KEY_TRANSACTION_SETTLED_SUCCESSFULLY)) {
                            orderHistoryTileResponseView.setCanRefund(true);
                            orderHistoryTileResponseView.setSettlementDate(authNetPayment.getModifiedDate());
                        }
                        // Marking the transaction status as REFUND
                        if (authNetPayment.getTransactionStatus().equalsIgnoreCase(KeyConstants.KEY_REFUND_INITIATED)) {
                            orderHistoryTileResponseView.setOrderStatus(KeyConstants.KEY_REFUNDED);
                            orderHistoryTileResponseView.setIsTransactionRefunded(true);
                            orderHistoryTileResponseView.setTransactionId(authNetPayment.getTransactionId());
                            orderHistoryTileResponseView.setRefundTransactionId(authNetPayment.getRefundTransactionId());
                            orderHistoryTileResponseView.setRefundedAmount(authNetPayment.getAmountRefunded());
                            if (authNetPayment.getAmountRefunded() != null) {
                                orderHistoryTileResponseView.setFormattedRefundAmount(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(authNetPayment.getAmountRefunded()));
                            }
                            String nextRenewalDateInString = fitwiseUtils.formatDate(authNetPayment.getModifiedDate());
                            orderHistoryTileResponseView.setRefundedDate(nextRenewalDateInString);
                            orderHistoryTileResponseView.setRefundedDateTimeStamp(authNetPayment.getModifiedDate());
                            RefundReasonForAMember refundReasonForAMember = refundReasonForAMemberRepository.findByTransactionId(authNetPayment.getTransactionId());
                            orderHistoryTileResponseView.setRefundReason(refundReasonForAMember.getRefundReasonInWord());
                        }
                        orderHistoryTileResponseView.setProgramPrice(authNetPayment.getAmountPaid()); //Setting program price
                        orderHistoryTileResponseView.setFormattedProgramPrice(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(authNetPayment.getAmountPaid().doubleValue()));
                        orderHistoryTileResponseView.setOrderTotalPrice(authNetPayment.getAmountPaid()); // Setting order price
                    } else {
                        orderHistoryTileResponseView.setOrderStatus(KeyConstants.KEY_FAILURE); // Setting order failure status
                    }
                    // Checking if auto renewal state if active
                    AuthNetArbSubscription currentAuthNetArbSubscription = authNetArbSubscriptionRepository.
                            findTop1ByUserUserIdAndProgramProgramIdOrderByModifiedDateDesc(member.getUserId(), orderManagement.getProgram().getProgramId());
                    if (currentAuthNetArbSubscription != null && currentAuthNetArbSubscription.getAuthNetSubscriptionStatus() != null &&
                            currentAuthNetArbSubscription.getAuthNetSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_ACTIVE)) {
                        orderHistoryTileResponseView.setAutoRenewal(true); // Setting auto-renewal status ON or OFF
                    }

                    /**
                     * Calculating the total number of subscriptions for the program done by the user
                     * and the number of renewals from the current subscription
                     */
                    List<AuthNetArbSubscription> authNetArbSubscriptionList = authNetArbSubscriptionRepository
                            .findByUserUserIdAndProgramProgramIdAndAuthNetSubscriptionStatusSubscriptionStatusName(member.getUserId(),
                                    orderManagement.getProgram().getProgramId(), KeyConstants.KEY_SUBSCRIPTION_ACTIVE);

                    for (AuthNetArbSubscription aNetArbSubscription : authNetArbSubscriptionList) {
                        List<AuthNetPayment> authNetPayments = authNetPaymentRepository
                                .findByArbSubscriptionIdAndResponseCode(aNetArbSubscription.getANetSubscriptionId(),
                                        KeyConstants.ANET_TRANSACTION_SUCCESS_RESPONSE_CODE);
                        for (AuthNetPayment aNetPayment : authNetPayments) {
                            if (currentAuthNetArbSubscription != null &&
                                    aNetPayment.getArbSubscriptionId().equals(currentAuthNetArbSubscription.getANetSubscriptionId())) {
                                currentSubscriptionRenewalsCount++;
                            }
                        }
                    }
                    orderHistoryTileResponseView.setPaymentGateway(KeyConstants.KEY_AUTH_NET);
                } else if (KeyConstants.KEY_STRIPE.equals(orderManagement.getModeOfPayment())) {

                    orderHistoryTileResponseView.setPaymentGateway(KeyConstants.KEY_STRIPE);

                    // Getting the payment status
                    StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
                    if (stripePayment != null) {
                        orderHistoryTileResponseView.setPlatformName(KeyConstants.KEY_STRIPE);
                        if (KeyConstants.KEY_PAID.equalsIgnoreCase(stripePayment.getTransactionStatus())) {
                            orderHistoryTileResponseView.setOrderStatus(KeyConstants.KEY_SUCCESS); // Setting order success status
                            orderHistoryTileResponseView.setTransactionId(stripePayment.getChargeId()); // Setting transaction id

                            //Refund is applicable only for fully paid or orders with discount offers. Not for free orders.
                            if (!isOfferApplied || DiscountsConstants.MODE_PAY_AS_YOU_GO.equalsIgnoreCase(offerCodeDetailAndOrderMapping.getOfferCodeDetail().getOfferMode())) {
                                //Can refund before 90 days since payment
                                LocalDateTime localDateTime = stripePayment.getModifiedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                                LocalDateTime localStartDateTime = localDateTime.plusDays(stripeProperties.getRefundPeriodStart());
                                Date refundStartDate = Date.from(localStartDateTime.atZone(ZoneId.systemDefault()).toInstant());
                                LocalDateTime localEndDateTime = localDateTime.plusDays(stripeProperties.getRefundPeriodEnd());
                                Date refundEndDate = Date.from(localEndDateTime.atZone(ZoneId.systemDefault()).toInstant());
                                Date now = new Date();

                                if (now.after(refundStartDate) && now.before(refundEndDate)) {
                                    orderHistoryTileResponseView.setCanRefund(true);
                                }
                            }

                            orderHistoryTileResponseView.setSettlementDate(stripePayment.getModifiedDate());
                        }

                        // Marking the transaction status as REFUND
                        if (KeyConstants.KEY_REFUND.equalsIgnoreCase(stripePayment.getTransactionStatus())) {
                            orderHistoryTileResponseView.setOrderStatus(KeyConstants.KEY_REFUNDED);
                            orderHistoryTileResponseView.setIsTransactionRefunded(true);
                            orderHistoryTileResponseView.setTransactionId(stripePayment.getChargeId());
                            orderHistoryTileResponseView.setRefundTransactionId(stripePayment.getRefundTransactionId());
                            orderHistoryTileResponseView.setRefundedAmount(stripePayment.getAmountRefunded());
                            orderHistoryTileResponseView.setFormattedRefundAmount(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(stripePayment.getAmountRefunded()));
                            String nextRenewalDateInString = fitwiseUtils.formatDate(stripePayment.getModifiedDate());
                            orderHistoryTileResponseView.setRefundedDate(nextRenewalDateInString);
                            orderHistoryTileResponseView.setRefundedDateTimeStamp(stripePayment.getModifiedDate());
                            RefundReasonForAMember refundReasonForAMember = refundReasonForAMemberRepository.findByTransactionId(stripePayment.getChargeId());
                            if (refundReasonForAMember != null) {
                                orderHistoryTileResponseView.setRefundReason(refundReasonForAMember.getRefundReasonInWord());
                            }
                        }

                        orderHistoryTileResponseView.setProgramPrice(price); //Setting program price
                        orderHistoryTileResponseView.setFormattedProgramPrice(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(price));
                        orderHistoryTileResponseView.setOrderTotalPrice(stripePayment.getAmountPaid()); // Setting order price
                        orderHistoryTileResponseView.setFormattedOrderTotalPrice(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(stripePayment.getAmountPaid()));
                        orderHistoryTileResponseView.setTransactionId(stripePayment.getChargeId()); // Setting transaction id

                    } else {
                        orderHistoryTileResponseView.setOrderStatus(KeyConstants.KEY_FAILURE); // Setting order failure status
                    }

                    // Checking if auto renewal state if active
                    boolean isAutoRenewal = false;
                    if (KeyConstants.KEY_PROGRAM.equalsIgnoreCase(subscriptionType)) {
                        StripeSubscriptionAndUserProgramMapping stripeSubscriptionAndUserProgramMapping = stripeSubscriptionAndUserProgramMappingRepository.findTop1ByUserUserIdAndProgramProgramIdOrderByModifiedDateDesc(member.getUserId(), orderManagement.getProgram().getProgramId());
                        if (stripeSubscriptionAndUserProgramMapping != null && stripeSubscriptionAndUserProgramMapping.getSubscriptionStatus() != null &&
                                stripeSubscriptionAndUserProgramMapping.getSubscriptionStatus().getSubscriptionStatus().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_ACTIVE)) {
                            isAutoRenewal = true;
                        }
                    } else {
                        StripeSubscriptionAndUserPackageMapping stripeSubscriptionAndUserPackageMapping = stripeSubscriptionAndUserPackageMappingRepository.findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageIdOrderByModifiedDateDesc(member.getUserId(), orderManagement.getSubscriptionPackage().getSubscriptionPackageId());
                        if (stripeSubscriptionAndUserPackageMapping != null && stripeSubscriptionAndUserPackageMapping.getSubscriptionStatus() != null &&
                                stripeSubscriptionAndUserPackageMapping.getSubscriptionStatus().getSubscriptionStatus().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_ACTIVE)) {
                            isAutoRenewal = true;
                        }
                    }
                    orderHistoryTileResponseView.setAutoRenewal(isAutoRenewal); // Setting auto-renewal status ON or OFF

                    /**
                     * Calculating the total number of subscriptions for the program done by the user
                     * and the number of renewals from the current subscription
                     */

                    StripeSubscriptionChangesTracker stripeSubscriptionChangesTracker = stripeSubscriptionChangesTrackerRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());

                    if (stripeSubscriptionChangesTracker != null) {
                        List<StripePayment> stripePaymentList = stripePaymentRepository.findBySubscriptionIdAndTransactionStatusAndCreatedDateLessThanEqual(stripeSubscriptionChangesTracker.getSubscriptionId(), KeyConstants.KEY_PAID, stripeSubscriptionChangesTracker.getModifiedDate());
                        stripePaymentList = stripePaymentList.stream()
                                .collect(collectingAndThen(toCollection(() -> new TreeSet<StripePayment>(comparing(StripePayment::getInvoiceId))), ArrayList::new));
                        currentSubscriptionRenewalsCount = stripePaymentList.size();
                    }

                }

                List<String> statusList = Arrays.asList(KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING);
                //Adding a buffer time of 3 minutes to consider delay in populating SubscriptionAudit table
                LocalDateTime localDateTime = orderManagement.getCreatedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                localDateTime = localDateTime.plusMinutes(3);
                Date orderCreationDateWithBuffer = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                int totalNumberOfRenewalsCount;
                if (KeyConstants.KEY_PROGRAM.equalsIgnoreCase(subscriptionType)) {
                    totalNumberOfRenewalsCount = subscriptionAuditRepo.countByUserUserIdAndSubscriptionTypeNameAndProgramSubscriptionProgramProgramIdAndSubscriptionStatusSubscriptionStatusNameInAndCreatedDateLessThanEqual(orderManagement.getUser().getUserId(), KeyConstants.KEY_PROGRAM, orderManagement.getProgram().getProgramId(), statusList, orderCreationDateWithBuffer);
                } else {
                    totalNumberOfRenewalsCount = subscriptionAuditRepo.countBySubscriptionTypeNameAndUserUserIdAndPackageSubscriptionSubscriptionPackageSubscriptionPackageIdAndSubscriptionStatusSubscriptionStatusNameInAndCreatedDateLessThanEqual(KeyConstants.KEY_SUBSCRIPTION_PACKAGE, orderManagement.getUser().getUserId(), orderManagement.getSubscriptionPackage().getSubscriptionPackageId(), statusList, orderCreationDateWithBuffer);
                }

                //Since the second time purchase will only be considered as a renewal, decreasing the total counts by 1
                if (totalNumberOfRenewalsCount != 0) {
                    totalNumberOfRenewalsCount = totalNumberOfRenewalsCount - 1;
                }
                if (currentSubscriptionRenewalsCount != 0) {
                    currentSubscriptionRenewalsCount = currentSubscriptionRenewalsCount - 1;
                }

                orderHistoryTileResponseView.setTotalRenewalCount(totalNumberOfRenewalsCount);
                orderHistoryTileResponseView.setCurrentSubscriptionRenewalCount(currentSubscriptionRenewalsCount);

                // Setting next renewal date if Auto renewal status of the program is ON
                if (orderHistoryTileResponseView.isAutoRenewal()) {
                    boolean isSubscribed = false;
                    if (KeyConstants.KEY_PROGRAM.equalsIgnoreCase(subscriptionType)) {
                        ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(member.getUserId(), orderManagement.getProgram().getProgramId());
                        if (programSubscription != null) {
                            isSubscribed = true;
                        }
                    } else {
                        PackageSubscription packageSubscription = packageSubscriptionRepository.findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageIdOrderBySubscribedDateDesc(member.getUserId(), orderManagement.getSubscriptionPackage().getSubscriptionPackageId());
                        if (packageSubscription != null) {
                            isSubscribed = true;
                        }
                    }

                    if (isSubscribed) {
                        Calendar cal = Calendar.getInstance();
                        Date subscribedDate = orderManagement.getCreatedDate();

                        cal.setTime(subscribedDate);
                        cal.add(Calendar.DATE, Math.toIntExact(duration));

                        Date nextRenewalDate = cal.getTime();

                        orderHistoryTileResponseView.setNextRenewalDateTimeStamp(nextRenewalDate);
                        String nextRenewalDateInString = fitwiseUtils.formatDate(nextRenewalDate);
                        orderHistoryTileResponseView.setNextRenewalDate(nextRenewalDateInString);
                    }
                }

                orderHistoryTileResponseView.setDuration(duration + KeyConstants.KEY_SPACE + KeyConstants.DAYS); // Setting program duration

            } else if (orderManagement.getSubscribedViaPlatform().getPlatformTypeId() == 2) {

                String orderDate = fitwiseUtils.formatDate(orderManagement.getCreatedDate());
                orderHistoryTileResponseView.setPurchasedDate(orderDate);
                orderHistoryTileResponseView.setPurchasedDateTimeStamp(orderManagement.getCreatedDate());
                // Manipulating whether the order is success or failure
                orderHistoryTileResponseView.setOrderStatus(orderManagement.getOrderStatus());
                // Setting program duration
                orderHistoryTileResponseView.setDuration(1 + KeyConstants.KEY_SPACE + KeyConstants.KEY_MONTH);
                // Setting TotalRenewalCount & CurrentSubscriptionRenewalCount
                /**
                 * Calculating the total number of subscriptions for the program done by the
                 * user and the renewals for the current subscription
                 */
                int totalNumberOfRenewalsCount = 0;
                int currentSubscriptionRenewalsCount = 0;
                log.info("Order Created Date :{} ", orderManagement.getCreatedDate());
                List<AppleProductSubscription> initialSubscription = appleProductSubscriptionRepository.findByProgramAndUserAndEvent(orderManagement.getProgram(), orderManagement.getUser(), NotificationConstants.INITIAL_BUY);
                List<String> statusList = Arrays.asList(NotificationConstants.RENEWAL, NotificationConstants.INTERACTIVE_RENEWAL);
                List<AppleProductSubscription> iOSSubscriptions = appleProductSubscriptionRepository
                        .findByProgramAndUserAndEventInAndCreatedDateLessThanEqual(orderManagement.getProgram(), orderManagement.getUser(), statusList, orderManagement.getCreatedDate());
                log.info("list Count :{}", iOSSubscriptions.size());
                if (!iOSSubscriptions.isEmpty()) {
                    if (!initialSubscription.isEmpty()) {
                        totalNumberOfRenewalsCount = iOSSubscriptions.size();
                        currentSubscriptionRenewalsCount = iOSSubscriptions.size();
                    } else {
                        totalNumberOfRenewalsCount = iOSSubscriptions.size() - 1;
                        currentSubscriptionRenewalsCount = iOSSubscriptions.size() - 1;
                    }
                }
                // In Apple Same Original Transaction Id will be maintained next set of renewals as well.
                // Unique transaction and weborderLineItem id will be generated for each subscription

                orderHistoryTileResponseView.setTotalRenewalCount(totalNumberOfRenewalsCount);
                orderHistoryTileResponseView.setCurrentSubscriptionRenewalCount(currentSubscriptionRenewalsCount);
                log.info("totalNumberOfRenewalsCount {}", totalNumberOfRenewalsCount);
                log.info("currentSubscriptionRenewalsCount {}", currentSubscriptionRenewalsCount);

                //
                ApplePayment applePayment = applePaymentRepository
                        .findTop1ByOrderManagementOrderIdOrderByCreatedDateDesc(orderManagement.getOrderId());
                if (applePayment != null) {
                    orderHistoryTileResponseView.setProgramPrice(applePayment.getProgramPrice()); //Setting program price
                    orderHistoryTileResponseView.setFormattedProgramPrice(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(applePayment.getProgramPrice().doubleValue()));
                    orderHistoryTileResponseView.setOrderTotalPrice(applePayment.getProgramPrice()); // Setting order price
                    orderHistoryTileResponseView.setFormattedOrderTotalPrice(fitwiseUtils.formatPrice(applePayment.getProgramPrice()));

                    if (applePayment.getPurchaseDate() != null) {
                        String purchaseDate = fitwiseUtils.formatDate(applePayment.getPurchaseDate());
                        orderHistoryTileResponseView.setPurchasedDate(purchaseDate);
                        orderHistoryTileResponseView.setPurchasedDateTimeStamp(applePayment.getPurchaseDate());
                    }
                    if (applePayment.getTransactionId() != null) {
                        orderHistoryTileResponseView.setTransactionId(applePayment.getTransactionId());
                    }
                    //
                    AppleProductSubscription appleSubscription = appleProductSubscriptionRepository.findTop1ByTransactionIdOrderByModifiedDateDesc(applePayment.getTransactionId());
                    if (appleSubscription != null && appleSubscription.getAppleSubscriptionStatus() != null) {
                        // Set Auto renewal flag
                        if (appleSubscription.getAppleSubscriptionStatus().getSubscriptionStatusName()
                                .equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_ACTIVE)) {
                            orderHistoryTileResponseView.setAutoRenewal(true);
                        } else {
                            orderHistoryTileResponseView.setAutoRenewal(false);
                        }
                        //
                    }
                    if (orderHistoryTileResponseView.isAutoRenewal() && applePayment.getExpiryDate() != null) {
                        String expiryDate = fitwiseUtils.formatDate(applePayment.getExpiryDate());
                        orderHistoryTileResponseView.setNextRenewalDate(expiryDate);
                        orderHistoryTileResponseView.setNextRenewalDateTimeStamp(applePayment.getExpiryDate());
                    }
                }
            }

            //Adding offer code details
            if (isOfferApplied) {
                OfferCodeDetail offerCodeDetail = offerCodeDetailAndOrderMapping.getOfferCodeDetail();
                OrderDiscountDetails orderDiscountDetails = new OrderDiscountDetails();
                orderDiscountDetails.setOfferName(offerCodeDetail.getOfferName().trim());
                orderDiscountDetails.setOfferCode(offerCodeDetail.getOfferCode().toUpperCase());

                double offerPrice = DiscountsConstants.MODE_FREE.equalsIgnoreCase(offerCodeDetail.getOfferMode()) ? 0.00 : offerCodeDetail.getOfferPrice().getPrice();
                double discountAmount = price - offerPrice;

                orderDiscountDetails.setDiscountAmount(0 - discountAmount);
                orderDiscountDetails.setFormattedDiscountAmount("-" + KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(discountAmount));

                if (orderManagement.getSubscribedViaPlatform().getPlatformTypeId() == 2) {
                    orderHistoryTileResponseView.setOrderTotalPrice(offerPrice);
                    orderHistoryTileResponseView.setFormattedOrderTotalPrice(fitwiseUtils.formatPrice(offerPrice));
                }
                orderHistoryTileResponseView.setOrderDiscountDetails(orderDiscountDetails);
            }

            orderHistoryTileResponseViews.add(orderHistoryTileResponseView);
        }
        log.info("Construct order history : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        OrderHistoryResponseViewOfAMember orderHistoryResponseView = new OrderHistoryResponseViewOfAMember();
        orderHistoryResponseView.setOrders(orderHistoryTileResponseViews);
        orderHistoryResponseView.setTotalOrderCount(orderManagements.getTotalElements());

        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setPayload(orderHistoryResponseView);
        responseModel.setMessage(MessageConstants.MSG_ORDER_HISTORY_FETCHED_SUCCESSFULLY);
        log.info("Construct response model : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get order history of a member ends.");

        return responseModel;
    }


    //TODO Need to implement logic
    public boolean isInstructorPayOffPending(Long instructorId) {
        List<String> statusList = Arrays.asList(KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING);
        int subscriptionHistoryCount = subscriptionAuditRepo.countBySubscriptionTypeNameAndSubscriptionStatusSubscriptionStatusNameInAndProgramSubscriptionProgramOwnerUserId(KeyConstants.KEY_PROGRAM, statusList, instructorId);
        if (subscriptionHistoryCount > 0) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * Method used to check and update Authorize.net Transaction status
     */
    @Transactional
    public void checkAndUpdateAuthNetTransactionStatus() {

        /**
         * Getting the current transaction status of Payment pending transactions
         */
        List<AuthNetPayment> pendingAuthNetPayments = authNetPaymentRepository.findByTransactionStatusIgnoreCaseContaining(KeyConstants.KEY_PAYMENT_PENDING);
        for (AuthNetPayment authNetPayment : pendingAuthNetPayments) {
            OrderManagement orderManagement = authNetPayment.getOrderManagement();
            User user = orderManagement.getUser();
            Programs program = orderManagement.getProgram();
            ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), program.getProgramId());

            GetTransactionDetailsResponse transactionDetailsResponse = GetTransactionDetails.run(authNetPayment.getTransactionId());
            if (programSubscription != null && transactionDetailsResponse != null && transactionDetailsResponse.getTransaction() != null) {
                if (transactionDetailsResponse.getTransaction().getTransactionStatus().equalsIgnoreCase(KeyConstants.KEY_TRANSACTION_SETTLED_SUCCESSFULLY)) {
                    // PAYMENT SETTLED SUCCESSFULLY!
                    // Transactions with this status have been approved and successfully settled
                    authNetPayment.setTransactionStatus(KeyConstants.KEY_TRANSACTION_SETTLED_SUCCESSFULLY);
                    SubscriptionStatus subscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_PAID);
                    programSubscription.setSubscriptionStatus(subscriptionStatus);
                    //Sync payment to QBO
                    fitwiseQboEntityService.createAndSyncAnetPayment(authNetPayment);
                } else if (transactionDetailsResponse.getTransaction().getTransactionStatus().equalsIgnoreCase(KeyConstants.KEY_TRANSACTION_DECLINED)) {
                    // PAYMENT DECLINED!
                    // Transactions with this status were not approved at the processor. These transactions cannot be captured and submitted for settlement
                    authNetPayment.setTransactionStatus(KeyConstants.KEY_TRANSACTION_DECLINED);
                    SubscriptionStatus subscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_PAYMENT_FAILED);
                    programSubscription.setSubscriptionStatus(subscriptionStatus);
                } else if (transactionDetailsResponse.getTransaction().getTransactionStatus().equalsIgnoreCase(KeyConstants.KEY_TRANSACTION_EXPIRED)) {
                    // PAYMENT EXPIRED!
                    //Transactions that are expired were authorized but never submitted for capture.
                    // Transactions typically expire approximately 30 days after the initial authorization.
                    authNetPayment.setTransactionStatus(KeyConstants.KEY_TRANSACTION_EXPIRED);
                    SubscriptionStatus subscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_PAYMENT_FAILED);
                    programSubscription.setSubscriptionStatus(subscriptionStatus);
                } else if (transactionDetailsResponse.getTransaction().getTransactionStatus().equalsIgnoreCase(KeyConstants.KEY_TRANSACTION_SETTLEMENT_ERROR)) {
                    // PAYMENT SETTLEMENT ERROR!
                    authNetPayment.setTransactionStatus(KeyConstants.KEY_TRANSACTION_SETTLEMENT_ERROR);
                    SubscriptionStatus subscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_PAYMENT_FAILED);
                    programSubscription.setSubscriptionStatus(subscriptionStatus);
                } else if (transactionDetailsResponse.getTransaction().getTransactionStatus().equalsIgnoreCase(KeyConstants.KEY_TRANSACTION_GENERAL_ERROR)) {
                    authNetPayment.setTransactionStatus(KeyConstants.KEY_TRANSACTION_GENERAL_ERROR);
                    SubscriptionStatus subscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_PAYMENT_FAILED);
                    programSubscription.setSubscriptionStatus(subscriptionStatus);
                }
                programSubscriptionRepo.save(programSubscription);
                authNetPaymentRepository.save(authNetPayment);
            }
        }


        /**
         * Checking whether the refund got settled to User and updating the status in Auth.net table
         */
        List<AuthNetPayment> refundedAuthNetPayments = authNetPaymentRepository.findByTransactionStatusIgnoreCaseContaining(KeyConstants.KEY_REFUND_INITIATED);
        for (AuthNetPayment refundedPayment : refundedAuthNetPayments) {

            GetTransactionDetailsResponse transactionDetails = GetTransactionDetails.run(refundedPayment.getTransactionId());
            if (transactionDetails.getTransaction().getTransactionStatus().equalsIgnoreCase(KeyConstants.KEY_TRANSACTION_REFUND_SETTLED)) {
                refundedPayment.setTransactionStatus(KeyConstants.KEY_TRANSACTION_REFUND_SETTLED);
                authNetPaymentRepository.save(refundedPayment);
            }
        }
    }

    /**
     * Used to delete user data from authorize.net related stuffs
     *
     * @param userId
     */
    @Transactional
    public void deleteUserFromAuthorizeNet(Long userId) {

        AuthNetCustomerProfile customerProfile = authNetCustomerProfileRepository.findByUserUserId(userId);
        if (customerProfile != null) {

            // Deleting customer profile from Authorize.net
            DeleteCustomerProfile.run(customerProfile.getAuthNetCustomerProfileId());

            // Deleting customer profile from auth_net_customer_profile table
            authNetCustomerProfileRepository.delete(customerProfile);
        }

        // Deleting Authorize.net based user subscription data from Fitwise table
        List<AuthNetArbSubscription> arbSubscriptions = authNetArbSubscriptionRepository.findByUserUserId(userId);
        if (!arbSubscriptions.isEmpty()) {
            authNetArbSubscriptionRepository.deleteInBatch(arbSubscriptions);
        }
    }


    /**
     * Used to delete user data from authorize.net related stuffs
     *
     * @param userId
     */
    @Transactional
    public void deleteUserFromStripe(Long userId) {

        StripeCustomerAndUserMapping stripeCustomerAndUserMapping = stripeCustomerAndUserMappingRepository.findTop1ByUserUserId(userId);
        if (stripeCustomerAndUserMapping != null) {
            Stripe.apiKey = stripeProperties.getApiKey();
            Customer customer;
            try {
                customer = Customer.retrieve(stripeCustomerAndUserMapping.getStripeCustomerId());
                customer.delete();
            } catch (StripeException e) {
                logger.error("Error deleting Stripe user --->" + e.getMessage());
                e.printStackTrace();
            }
            stripeCustomerAndUserMappingRepository.delete(stripeCustomerAndUserMapping);

            List<StripeSubscriptionAndUserProgramMapping> subscriptionAndUserProgramMappings =
                    stripeSubscriptionAndUserProgramMappingRepository.findByUserUserId(userId);
            if (!subscriptionAndUserProgramMappings.isEmpty()) {
                stripeSubscriptionAndUserProgramMappingRepository.deleteInBatch(subscriptionAndUserProgramMappings);
            }
        }
    }


    /**
     * Method used to calculate the due date of payments that needs to be settled to the Instructor
     * Once transaction is success, this month will be called to update the settlement date.
     * <p>
     * For apple payments: #Get the current date, #Navigate to the last date of the month, #Add 45 days to it, #Get the last date of the month
     *
     * @param orderId - Order id
     */
    @Transactional
    public Date dueDateLogicCalculator(String orderId) {
        OrderManagement orderManagement = orderManagementRepository.findTop1ByOrderId(orderId);
        Date instructorPaymentSettlementDate = null;
        if (orderManagement != null) {
            if (orderManagement.getSubscribedViaPlatform() != null) {
                LocalDate settlementDate;
                if (orderManagement.getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_STRIPE)) {
                    LocalDate today = LocalDate.now();
                    settlementDate = today.plusDays(qboProperties.getDueDateBufferStripe());
                } else if (orderManagement.getSubscribedViaPlatform().getPlatformTypeId() == 2) {
                    // Subscribed via Apple payments

                    // Getting the current subscription date
                    LocalDate today = LocalDate.now();
                    // Getting the last date of the month
                    LocalDate lastDateOfMonth = today.withDayOfMonth(today.lengthOfMonth());
                    // Adding 45 days to the last date of the month
                    LocalDate appleSettlementDate = lastDateOfMonth.plusDays(qboProperties.getAppleDateDateLogicBufferInDays());
                    // Getting the last date of the settlement date falling month
                    settlementDate = appleSettlementDate.withDayOfMonth(appleSettlementDate.lengthOfMonth());

                } else {
                    // Subscribed via Authorize.net

                    // Getting the current subscription date
                    LocalDate today = LocalDate.now();
                    // Adding 3 days to the current subscription date
                    LocalDate authNetSettlementDate = today.plusDays(qboProperties.getAuthnetDateDateLogicBufferInDays());
                    // Getting the last date of the authorize.net settlement date falling month
                    settlementDate = authNetSettlementDate.withDayOfMonth(authNetSettlementDate.lengthOfMonth());
                }
                instructorPaymentSettlementDate = Date.from(settlementDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
        }
        return instructorPaymentSettlementDate;
    }


    /**
     * Used to capture all webhooks from Authorize.net and save in our system
     *
     * @param jsonString
     */
    public void logAuthorizeNetWebHookNotification(String jsonString) {
        AuthNetWebHookLogger logger = new AuthNetWebHookLogger();
        logger.setWebHookNotification(jsonString);
        authNetWebHookLoggerRepository.save(logger);
    }

    /**
     * Method used to refund a transaction to the user
     *
     * @param transactionId
     * @param refundAmount
     * @return
     */
    @Transactional
    public ResponseModel refundAuthorizeNetPayment(String transactionId, Double refundAmount) {
        CreateTransactionResponse refundTransactionResponse = RefundTransaction.run(refundAmount, transactionId);
        boolean isRefundSuccess = false;

        if (refundTransactionResponse != null) {
            if (refundTransactionResponse.getMessages().getResultCode() == MessageTypeEnum.OK) {
                TransactionResponse result = refundTransactionResponse.getTransactionResponse();
                if (result.getMessages() != null) {
                    isRefundSuccess = true;

                    AuthNetPayment authNetPayment = authNetPaymentRepository.findTop1ByTransactionId(transactionId);
                    // Adding a new entry for the void transaction type
                    if (authNetPayment != null) {
                        AuthNetPayment refundedAuthNetPayment = new AuthNetPayment();
                        refundedAuthNetPayment.setTransactionId(authNetPayment.getTransactionId());
                        refundedAuthNetPayment.setAmountPaid(authNetPayment.getAmountPaid());
                        refundedAuthNetPayment.setTransactionStatus(KeyConstants.KEY_REFUND_INITIATED);
                        refundedAuthNetPayment.setArbSubscriptionId(authNetPayment.getArbSubscriptionId());
                        refundedAuthNetPayment.setReceiptNumber(authNetPayment.getReceiptNumber());
                        refundedAuthNetPayment.setOrderManagement(authNetPayment.getOrderManagement());
                        refundedAuthNetPayment.setIsARB(authNetPayment.getIsARB());
                        refundedAuthNetPayment.setResponseCode(authNetPayment.getResponseCode());
                        refundedAuthNetPayment.setRefundTransactionId(refundTransactionResponse.getTransactionResponse().getTransId());
                        refundedAuthNetPayment.setAmountRefunded(refundAmount);
                        authNetPaymentRepository.save(refundedAuthNetPayment);

                        User user = authNetPayment.getOrderManagement().getUser();
                        Programs program = authNetPayment.getOrderManagement().getProgram();

                        // Changing the status of the subscription to REFUND
                        ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), program.getProgramId());
                        SubscriptionStatus subscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_REFUND);
                        programSubscription.setSubscriptionStatus(subscriptionStatus);
                        programSubscriptionRepo.save(programSubscription);

                        SubscriptionAudit oldSubscriptionAudit = subscriptionAuditRepo.
                                findTop1ByUserUserIdAndSubscriptionTypeNameAndProgramSubscriptionProgramProgramIdOrderByCreatedDateDesc(user.getUserId(), KeyConstants.KEY_PROGRAM, program.getProgramId());

                        //Saving revenueAudit table to store all tax details
                        ProgramSubscriptionPaymentHistory programSubscriptionPaymentHistory = new ProgramSubscriptionPaymentHistory();
                        programSubscriptionPaymentHistory.setOrderManagement(authNetPayment.getOrderManagement());
                        subscriptionPaymentHistoryRepository.save(programSubscriptionPaymentHistory);

                        // Adding new entry to the Subscription Audit table for Refund
                        SubscriptionAudit newSubscriptionAudit = new SubscriptionAudit();
                        newSubscriptionAudit.setProgramSubscription(oldSubscriptionAudit.getProgramSubscription());
                        newSubscriptionAudit.setSubscriptionType(oldSubscriptionAudit.getSubscriptionType());
                        newSubscriptionAudit.setSubscriptionPlan(oldSubscriptionAudit.getSubscriptionPlan());
                        newSubscriptionAudit.setSubscriptionDate(oldSubscriptionAudit.getSubscriptionDate());
                        newSubscriptionAudit.setSubscribedViaPlatform(oldSubscriptionAudit.getSubscribedViaPlatform());
                        newSubscriptionAudit.setRenewalStatus(oldSubscriptionAudit.getRenewalStatus());
                        newSubscriptionAudit.setUser(user);
                        newSubscriptionAudit.setSubscriptionStatus(subscriptionStatus);
                        newSubscriptionAudit.setProgramSubscriptionPaymentHistory(programSubscriptionPaymentHistory);
                        subscriptionAuditRepo.save(newSubscriptionAudit);
                        fitwiseQboEntityService.createAndSyncAnetRefund(authNetPayment);
                        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
                        String roundedPrice = decimalFormat.format(refundAmount);
                        //Sending mail to member for refund
                        String subject = EmailConstants.REFUND_INITIATED_PROGRAM_SUBJECT.replace(EmailConstants.LITERAL_PROGRAM_NAME, "'" + program.getTitle() + "'");
                        String supportLink = EmailConstants.TRAINNR_SUPPORT_LINK.replace(EmailConstants.EMAIL_EMAIL_ADDRESS, user.getEmail());
                        String mailBody = EmailConstants.REFUND_INITIATED_PROGRAM_CONTENT
                                .replace(EmailConstants.LITERAL_PROGRAM_NAME, "<b>" + program.getTitle() + "</b>")
                                .replace("#REFUND_AMOUNT#", roundedPrice);
                        String userName = fitwiseUtils.getUserFullName(user);
                        mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON
                                .replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
                                .replace("#MAIL_BODY#", mailBody)
                                .replace(EmailConstants.EMAIL_SUPPORT_URL, supportLink);
                        mailBody = emailContentUtil.replaceMemberAppUrl(mailBody);
                        asyncMailer.sendHtmlMail(user.getEmail(), subject, mailBody);
                    }
                }
            }
        }
        if (!isRefundSuccess) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_FAILED_TO_REFUND, null);
        }
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_TRANSACTION_REFUND_SUCCESS, null);
    }

    /**
     * Used to refund reasons
     *
     * @return
     */
    @Transactional
    public ResponseModel getRefundReasons() {
        List<RefundReasons> refundReasonList = refundReasonsRepository.findAll();
        if (refundReasonList.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_REFUND_REASONS_EMPTY, null);
        }
        Map<String, Object> refundReasons = new HashMap<>();
        refundReasons.put(KeyConstants.KEY_REFUND_REASONS, refundReasonList);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_REFUND_REASONS_FETCHED, refundReasons);
    }

    /**
     * Used to post refund reason for a member
     *
     * @param postRefundReasonRequestView
     * @return
     */
    @Transactional
    public ResponseModel postRefundReason(PostRefundReasonRequestView postRefundReasonRequestView) {
        User user = validationService.validateMemberId(postRefundReasonRequestView.getMemberId());
        RefundReasons refundReason = validationService.validateRefundReason(postRefundReasonRequestView.getRefundReasonId());
        OrderManagement orderManagement = validationService.validateOrderId(postRefundReasonRequestView.getOrderId());
        if (orderManagement.getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_ANET)) {
            AuthNetPayment authNetPayment = authNetPaymentRepository.
                    findTop1ByTransactionId(postRefundReasonRequestView.getTransactionId());
            if (authNetPayment == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_INVALID_TRANSACTION_ID, null);
            }
        } else if (orderManagement.getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_STRIPE)) {
            StripePayment payment = stripePaymentRepository.findTop1ByChargeId(postRefundReasonRequestView.getTransactionId());
            if (payment == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_INVALID_TRANSACTION_ID, null);
            }
        }
        RefundReasonForAMember refundReasonForAMember = new RefundReasonForAMember();
        refundReasonForAMember.setMember(user);
        refundReasonForAMember.setRefundReasonInWord(postRefundReasonRequestView.getRefundReason());
        refundReasonForAMember.setRefundReasons(refundReason);
        refundReasonForAMember.setTransactionId(postRefundReasonRequestView.getTransactionId());
        refundReasonForAMember.setOrderManagement(orderManagement);
        refundReasonForAMemberRepository.save(refundReasonForAMember);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_REFUND_REASON_POSTED, null);
    }


    /**
     * Used to get the card type used for a transaction
     *
     * @param transactionId
     * @return
     */
    @Transactional
    public String getCardType(String transactionId) {
        GetTransactionDetailsResponse getTransactionDetailsResponse = GetTransactionDetails.run(transactionId);
        if (getTransactionDetailsResponse != null && getTransactionDetailsResponse.getTransaction() != null
                && getTransactionDetailsResponse.getTransaction().getPayment() != null
                && getTransactionDetailsResponse.getTransaction().getPayment().getCreditCard() != null) {
            return getTransactionDetailsResponse.getTransaction().getPayment().getCreditCard().getCardType();
        }
        return "";
    }

}