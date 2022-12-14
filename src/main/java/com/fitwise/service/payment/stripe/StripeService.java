package com.fitwise.service.payment.stripe;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.*;
import com.fitwise.constants.discounts.DiscountsConstants;
import com.fitwise.entity.*;
import com.fitwise.entity.discounts.DiscountOfferMapping;
import com.fitwise.entity.discounts.OfferCodeDetail;
import com.fitwise.entity.discounts.OfferCodeDetailAndOrderMapping;
import com.fitwise.entity.packaging.PackageOfferMapping;
import com.fitwise.entity.packaging.PackageProgramMapping;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.payments.common.InvoiceManagement;
import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.payments.credits.InstructorPaymentCredits;
import com.fitwise.entity.payments.paypal.UserAccountAndPayPalIdMapping;
import com.fitwise.entity.payments.stripe.*;
import com.fitwise.entity.payments.stripe.billing.*;
import com.fitwise.entity.payments.stripe.connect.AppleSettlementByStripe;
import com.fitwise.entity.payments.stripe.connect.StripeAccountAndUserMapping;
import com.fitwise.entity.payments.stripe.connect.StripeTransferErrorLog;
import com.fitwise.entity.payments.stripe.settings.PayoutModeSettings;
import com.fitwise.entity.subscription.*;
import com.fitwise.exception.ApplicationException;
import com.fitwise.properties.GeneralProperties;
import com.fitwise.properties.StripeProperties;
import com.fitwise.repository.AppConfigKeyValueRepository;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.SubscriptionPaymentHistoryRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.discountsRepository.DiscountOfferMappingRepository;
import com.fitwise.repository.discountsRepository.OfferCodeDetailAndOrderMappingRepository;
import com.fitwise.repository.discountsRepository.OfferCodeDetailRepository;
import com.fitwise.repository.order.InstructorPaymentRepository;
import com.fitwise.repository.order.InvoiceManagementRepository;
import com.fitwise.repository.order.OrderManagementRepository;
import com.fitwise.repository.packaging.PackageOfferMappingRepository;
import com.fitwise.repository.payments.credits.InstructorPaymentsCreditsRepository;
import com.fitwise.repository.payments.stripe.StripeCouponAndOfferCodeMappingRepository;
import com.fitwise.repository.payments.stripe.StripeProductAndPackageMappingRepository;
import com.fitwise.repository.payments.stripe.StripeSubscriptionAndCouponMappingRepository;
import com.fitwise.repository.payments.stripe.StripeSubscriptionAndUserPackageMappingRepository;
import com.fitwise.repository.payments.stripe.billing.*;
import com.fitwise.repository.payments.stripe.connect.AppleSettlementByStripeRepository;
import com.fitwise.repository.payments.stripe.connect.StripeAccountAndUserMappingRepository;
import com.fitwise.repository.payments.stripe.connect.StripeTransferErrorLogRepository;
import com.fitwise.repository.payments.stripe.paypal.UserAccountAndPayPalIdMappingRepository;
import com.fitwise.repository.payments.stripe.settings.PayoutModeSettingsRepository;
import com.fitwise.repository.subscription.*;
import com.fitwise.service.NotificationTriggerService;
import com.fitwise.service.SubscriptionService;
import com.fitwise.service.dynamiclink.DynamicLinkService;
import com.fitwise.service.qbo.FitwiseQboEntityService;
import com.fitwise.service.qbo.QBOService;
import com.fitwise.service.receiptInvoice.InvoicePDFGenerationService;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.specifications.ProgramSpecifications;
import com.fitwise.utils.EmailContentUtil;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.utils.mail.AsyncMailer;
import com.fitwise.utils.payments.OrderNumberGenerator;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.order.OrderResponseView;
import com.fitwise.view.payment.stripe.*;
import com.fitwise.view.payment.stripe.admin.PayoutSettingsResponseView;
import com.google.gson.Gson;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.RequestOptions;
import com.stripe.param.CouponCreateParams;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.PriceUpdateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StripeService {

    @Autowired
    ProgramSubscriptionRepo programSubscriptionRepo;

    @Autowired
    SubscriptionAuditRepo subscriptionAuditRepo;

    @Autowired
    private UserComponents userComponents;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private StripeSubscriptionStatusRepository stripeSubscriptionStatusRepository;

    @Autowired
    private SubscriptionStatusRepo subscriptionStatusRepo;

    @Autowired
    private SubscriptionTypesRepo subscriptionTypesRepo;

    @Autowired
    private SubscriptionPlansRepo subscriptionPlansRepo;

    @Autowired
    private OrderManagementRepository orderManagementRepo;

    @Autowired
    private InvoiceManagementRepository invoiceManagementRepository;

    @Autowired
    private FitwiseQboEntityService fitwiseQboEntityService;

    private final AsyncMailer asyncMailer;

    @Autowired
    FitwiseUtils fitwiseUtils;

    @Autowired
    private InstructorPaymentRepository instructorPaymentRepository;

    @Autowired
    NotificationTriggerService notificationTriggerService;

    @Autowired
    private GeneralProperties generalProperties;

    @Autowired
    private SubscriptionPaymentHistoryRepository subscriptionPaymentHistoryRepository;

    @Autowired
    private EmailContentUtil emailContentUtil;

    @Autowired
    private StripeProductAndProgramMappingRepository stripeProductAndProgramMappingRepository;

    @Autowired
    private StripeProperties stripeProperties;

    @Autowired
    private StripeProductAndPriceMappingRepository stripeProductAndPriceMappingRepository;

    @Autowired
    private StripeCustomerAndUserMappingRepository stripeCustomerAndUserMappingRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private StripeSubscriptionAndUserProgramMappingRepository stripeSubscriptionAndUserProgramMappingRepository;

    Logger logger = LoggerFactory.getLogger(StripeService.class);

    @Autowired
    private StripePaymentRepository stripePaymentRepository;

    @Autowired
    private StripePaymentFeeDetailsRepository stripePaymentFeeDetailsRepository;

    @Autowired
    private StripeSubscriptionChangesTrackerRepository stripeSubscriptionChangesTrackerRepository;

    @Autowired
    private StripeInvoiceAndSubscriptionMappingRepository stripeInvoiceAndSubscriptionMappingRepository;
    @Autowired
    private StripeWebHookLoggerRepository stripeWebHookLoggerRepository;

    @Autowired
    private StripePayoutRepository stripePayoutRepository;

    @Autowired
    private StripeBalanceTransactionRepository stripeBalanceTransactionRepository;

    @Autowired
    private StripeBalanceTransactionFeeDetailsMappingRepository stripeBalanceTransactionFeeDetailsMappingRepository;

    @Autowired
    ProgramRepository programRepository;
    @Autowired
    StripeCouponAndOfferCodeMappingRepository stripeCouponAndOfferCodeMappingRepository;
    @Autowired
    OfferCodeDetailRepository offerCodeDetailRepository;
    @Autowired
    private DiscountOfferMappingRepository discountOfferMappingRepository;
    @Autowired
    OfferCodeDetailAndOrderMappingRepository offerCodeDetailAndOrderMappingRepository;
    @Autowired
    StripeSubscriptionAndCouponMappingRepository stripeSubscriptionAndCouponMappingRepository;
    @Autowired
    SubscriptionService subscriptionService;
    @Autowired
    PackageSubscriptionRepository packageSubscriptionRepository;
    @Autowired
    private StripeProductAndPackageMappingRepository stripeProductAndPackageMappingRepository;
    @Autowired
    private StripeSubscriptionAndUserPackageMappingRepository stripeSubscriptionAndUserPackageMappingRepository;
    @Autowired
    private PackageProgramSubscriptionRepository packageProgramSubscriptionRepository;
    @Autowired
    private PackageOfferMappingRepository packageOfferMappingRepository;

    @Autowired
    private StripeAccountAndUserMappingRepository stripeAccountAndUserMappingRepository;

    @Autowired
    private StripeTransferErrorLogRepository stripeTransferErrorLogRepository;

    @Autowired
    private AppleSettlementByStripeRepository appleSettlementByStripeRepository;

    @Autowired
    private StripeConnectService stripeConnectService;
    @Autowired
    private QBOService qboService;

    @Autowired
    private InstructorPaymentsCreditsRepository instructorPaymentsCreditsRepository;

    @Autowired
    private PayoutModeSettingsRepository payoutModeSettingsRepository;

    @Autowired
    private DynamicLinkService dynamicLinkService;
    @Autowired
    InvoicePDFGenerationService invoicePDFGenerationService;

    private final UserAccountAndPayPalIdMappingRepository userAccountAndPayPalIdMappingRepository;
    private final StripeSubscriptionAndUserTierMappingRepository stripeSubscriptionAndUserTierMappingRepository;
    private final TierSubscriptionRepository tierSubscriptionRepository;
    @Autowired
    private StripeTierService stripeTierService;
    private final AppConfigKeyValueRepository appConfigKeyValueRepository;

    /**
     * Used to create a product in Stripe under Billing after creating program
     *
     * @param programId - Id of the program uploaded by an instructor
     * @return response model
     * @throws StripeException stripe exception
     */
    @Transactional
    public void createProgramInStripe(Long programId) throws StripeException {
        Product product = null;
        Programs program = null;
        try {
            program = validationService.validateProgramId(programId);
            Stripe.apiKey = stripeProperties.getApiKey();
            Map<String, Object> params = new HashMap<>();
            params.put(KeyConstants.KEY_STRIPE_PRODUCT_NAME, program.getTitle());
            StripeProductAndProgramMapping productMapping = stripeProductAndProgramMappingRepository.findByProgramProgramId(program.getProgramId());
            if (productMapping != null) {
                if (productMapping.isActive()) {
                    log.warn("Stripe product already in active state before publishing program. Please check and fix. Program Id : " + programId);
                }
                //Updating the product on stripe
                Product existingProduct = Product.retrieve(productMapping.getStripeProductId());
                product = existingProduct.update(params);
            } else {
                //Creating the product on stripe
                product = Product.create(params);
                productMapping = new StripeProductAndProgramMapping();
            }
            productMapping.setProgram(program);
            productMapping.setStripeProductId(product.getId());
            productMapping.setActive(true);
            stripeProductAndProgramMappingRepository.save(productMapping);
        } finally {
            if (product != null)
                createProductPriceInStripe(product.getId(), program.getDuration().getDuration(), program.getProgramPrices().getPrice());
        }
    }

    /**
     * Used to create price in Stripe for a product
     *
     * @param productId - Stripe product object
     * @param productPrice - Trainnr program object
     * @return
     */
    @Transactional
    public String createProductPriceInStripe(String productId, Long duration, double productPrice) throws StripeException {
        Stripe.apiKey = stripeProperties.getApiKey();
        // A positive integer in paise (or 0 for a free price) representing how much to charge.
        double flatTax = Double.parseDouble(appConfigKeyValueRepository.findByKeyString(AppConfigConstants.ADMIN_CONFIG_FLAT_TAX).getValueString());
        double unitPrice = (productPrice + flatTax) * 100;
        long unitPriceInLong = (long) unitPrice;
        // Mapping product and pricing in Trainnr table
        Price price;
        List<StripeProductAndPriceMapping> productAndPriceMappings = stripeProductAndPriceMappingRepository.findByProductId(productId);
        StripeProductAndPriceMapping priceMapping = null;
        if (!productAndPriceMappings.isEmpty()) {
            //Updating the product price on stripe as inactive
            for (StripeProductAndPriceMapping productAndPriceMapping : productAndPriceMappings){
                boolean status = false;
                if (productAndPriceMapping.getPrice() != null && productAndPriceMapping.getPrice() == (productPrice + flatTax)) {
                    priceMapping = productAndPriceMapping;
                    status = true;
                }
                PriceUpdateParams priceUpdateParams = PriceUpdateParams.builder()
                        .setActive(status)
                        .build();
                Price existingPrice = Price.retrieve(productAndPriceMapping.getPriceId());
                existingPrice.update(priceUpdateParams);
            }
        }
        if (priceMapping == null) {
            priceMapping = new StripeProductAndPriceMapping();
            //Creating the product price on stripe
            PriceCreateParams params;
            // Checking the interval of the program and setting the stripe pricing interval as month or day
            params = PriceCreateParams.builder()
                    .setProduct(productId)
                    .setUnitAmount(unitPriceInLong) // This should be in paise/cents. 100L denotes 1$.
                    .setCurrency("usd")
                    .setRecurring(PriceCreateParams.Recurring.builder()
                            .setInterval(PriceCreateParams.Recurring.Interval.DAY)
                            .setIntervalCount(duration)
                            .build())
                    .build();
            price = Price.create(params);
            priceMapping.setProductId(productId);
            priceMapping.setPriceId(price.getId());
            priceMapping.setPrice(productPrice + flatTax);
            stripeProductAndPriceMappingRepository.save(priceMapping);
        }
        return priceMapping.getPriceId();
    }

    /**
     * Used to create customer object in Stripe and create subscription
     *
     * @param stripeCustomerRequestView
     * @return
     * @throws StripeException
     */
    @Transactional
    public ResponseModel createCustomerAndDoPaymentInStripe(CreateStripeCustomerRequestView stripeCustomerRequestView) throws StripeException {
        log.info("Create customer and do payment in stripe starts.");
        long apiStartTimeMillis = new Date().getTime();
        Stripe.apiKey = stripeProperties.getApiKey();
        Programs program = validationService.validateProgramId(stripeCustomerRequestView.getProgramId());
        PlatformType platformType = validationService.validateAndGetPlatform(stripeCustomerRequestView.getDevicePlatformTypeId());
        log.info("Validate program and platform id's : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        if (stripeCustomerRequestView.getDevicePlatformTypeId() == 2) {
            throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_STRIPE_IOS_NOT_ALLOWED, null);
        }
        User user = userComponents.getUser();
        Date now = new Date();
        log.info("Get user : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        boolean isNewSubscription = true;
        ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), program.getProgramId());
        if (programSubscription != null) {
            SubscriptionStatus subscriptionStatus = subscriptionService.getMemberProgramSubscriptionStatus(programSubscription);
            if (subscriptionStatus != null) {
                String statusName = subscriptionStatus.getSubscriptionStatusName();
                if (statusName.equals(KeyConstants.KEY_PAID) || statusName.equals(KeyConstants.KEY_PAYMENT_PENDING) || statusName.equals(KeyConstants.KEY_EXPIRED)) {
                    isNewSubscription = false;
                }
            }
        }
        log.info("Verify whether the subscription is new or existing one : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        //Offer code validation
        OfferCodeDetail offerCodeDetail = null;
        if (stripeCustomerRequestView.getOfferCode() != null && !stripeCustomerRequestView.getOfferCode().isEmpty()) {
            OfferCodeDetail currentOfferCodeDetail = offerCodeDetailRepository.findByOfferCodeAndIsInUse(stripeCustomerRequestView.getOfferCode(), true);
            if (currentOfferCodeDetail == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_OFFER_CODE_DOESNT_EXIST, null);
            }
            if (now.before(currentOfferCodeDetail.getOfferStartingDate()) || now.after(currentOfferCodeDetail.getOfferEndingDate())) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_OFFER_CODE_NOT_VALID, null);
            }
            DiscountOfferMapping discountOfferMapping = discountOfferMappingRepository.findByProgramsProgramIdAndOfferCodeDetailOfferCodeId(program.getProgramId(), currentOfferCodeDetail.getOfferCodeId());
            if (discountOfferMapping == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_OFFER_CODE_NOT_ACCEPTED_FOR_PROGRAM, null);
            }
            if (DiscountsConstants.OFFER_INACTIVE.equals(currentOfferCodeDetail.getOfferStatus())) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_OFFER_CODE_NOT_AVAILABLE, null);
            }
            if (isNewSubscription && currentOfferCodeDetail.getIsNewUser().booleanValue() || !isNewSubscription && !currentOfferCodeDetail.getIsNewUser().booleanValue()) {
                offerCodeDetail = currentOfferCodeDetail;
            } else {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_OFFER_CODE_NOT_APPLICABLE_FOR_USER, null);
            }
        }
        log.info("Offer code validation : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        OrderManagement orderManagement = null;
        if (stripeCustomerRequestView.getExistingOrderId() == null || stripeCustomerRequestView.getExistingOrderId().isEmpty()) {
            orderManagement = createProgramOrder(user, program, true, KeyConstants.KEY_STRIPE, platformType, stripeCustomerRequestView.getExistingOrderId(), offerCodeDetail);
        } else {
            orderManagement = orderManagementRepo.findTop1ByOrderId(stripeCustomerRequestView.getExistingOrderId());
        }
        log.info("Getting order management : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        String stripeCustomerId = "";
        StripeCustomerAndUserMapping stripeCustomerAndUserMapping = stripeCustomerAndUserMappingRepository.findTop1ByUserUserId(user.getUserId());
        log.info("Query: getting stripe customer and user mapping : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if (stripeCustomerAndUserMapping == null) {
            UserProfile userProfile = userProfileRepository.findByUserUserId(user.getUserId());
            Map<String, Object> params = new HashMap<>();
            params.put(StringConstants.JSON_PROPERTY_KEY_DESCRIPTION, userProfile.getFirstName() + " " + userProfile.getLastName());
            params.put(StripeConstants.STRIPE_PROP_PAYMENT_METHOD, stripeCustomerRequestView.getPaymentMethodId());
            params.put(StripeConstants.STRIPE_PROP_EMAIL, user.getEmail());
            OrderResponseView orderResponseView = new OrderResponseView();
            orderResponseView.setOrderId(orderManagement.getOrderId());
            /**
             * Creating customer in stripe and mapping it with Trainnr user object
             */
            try {
                Customer customer = Customer.create(params);
                stripeCustomerId = customer.getId();
                User fitwiseUser = userComponents.getUser();
                stripeCustomerAndUserMapping = new StripeCustomerAndUserMapping();
                stripeCustomerAndUserMapping.setUser(fitwiseUser);
                stripeCustomerAndUserMapping.setStripeCustomerId(customer.getId());
                stripeCustomerAndUserMappingRepository.save(stripeCustomerAndUserMapping);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseModel(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_ATTACHING_PAYMENT_METHOD_TO_CUSTOMER, orderResponseView);
            }
        } else {
            stripeCustomerId = stripeCustomerAndUserMapping.getStripeCustomerId();
            attachPaymentMethodToCustomer(stripeCustomerId, stripeCustomerRequestView.getPaymentMethodId(), orderManagement);
        }
        log.info("Creating customer in stripe and mapping it with Trainnr user object : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        StripeProductAndProgramMapping productAndProgramMapping = stripeProductAndProgramMappingRepository.findByProgramProgramIdAndIsActive(program.getProgramId(), true);
        if (productAndProgramMapping == null) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_PRODUCT_PROGRAM_MAPPING_NOT_FOUND, null);
        }
        String productId = productAndProgramMapping.getStripeProductId();
        String priceId = "";
        List<StripeProductAndPriceMapping> productAndPriceMappings = stripeProductAndPriceMappingRepository.findByProductIdAndPrice(productId, program.getProgramPrices().getPrice());
        if (productAndPriceMappings.isEmpty()) {
            priceId = createProductPriceInStripe(productId, program.getDuration().getDuration(), program.getProgramPrices().getPrice());
        } else {
            priceId = productAndPriceMappings.get(0).getPriceId();
        }
        log.info("Query stripe mapping : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (profilingEndTimeMillis - apiStartTimeMillis));
        log.info("Create customer and do payment in stripe ends.");
        return createProgramSubscriptionInStripe(stripeCustomerId, priceId, stripeCustomerRequestView.getPaymentMethodId(), program, platformType, orderManagement, offerCodeDetail);
    }

    @Transactional
    public ResponseModel attachPaymentMethodToCustomer(String customerId, String paymentMethodId, OrderManagement orderManagement) {
        Stripe.apiKey = stripeProperties.getApiKey();
        PaymentMethod paymentMethod;
        try {
            paymentMethod = PaymentMethod.retrieve(paymentMethodId);
            Map<String, Object> params = new HashMap<>();
            params.put(StripeConstants.STRIPE_PROP_CUSTOMER, customerId);
            paymentMethod.attach(params);
        } catch (StripeException exception) {
            log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
            OrderResponseView orderResponseView = new OrderResponseView();
            orderResponseView.setOrderId(orderManagement.getOrderId());
            return new ResponseModel(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_ATTACHING_PAYMENT_METHOD_TO_CUSTOMER, orderResponseView);
        }
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PAYMENT_METHOD_ATTACHED, paymentMethod);
    }


    /**
     * Creating monthly subscription in Stripe
     *
     * @throws StripeException
     */
    @Transactional
    public ResponseModel createProgramSubscriptionInStripe(String customerId, String priceId, String paymentMethodId, Programs program, PlatformType platformType, OrderManagement orderManagement, OfferCodeDetail offerCodeDetail) {
        Stripe.apiKey = stripeProperties.getApiKey();
        User user = userComponents.getUser();
        boolean isAlreadySubscribedSubscriptionEnded = false;
        boolean isNewSubscription = false;

        ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), program.getProgramId());
        if (programSubscription == null) {
            isNewSubscription = true;
        }

        if (!isNewSubscription) {

            // Getting the subscriptionDate
            Date subscribedDate = programSubscription.getSubscribedDate();

            // Adding program duration to the subscribed date to get the subscription End date
            Calendar cal = Calendar.getInstance();
            cal.setTime(subscribedDate);
            cal.add(Calendar.DATE, Math.toIntExact(program.getDuration().getDuration()));
            Date subscriptionEndDate = cal.getTime();
            Date currentDate = new Date();

            // User already subscribed the program
            if (programSubscription.getSubscriptionStatus() != null) {
                if ((programSubscription.getSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID))
                        && subscriptionEndDate.after(currentDate)) {
                    // Program already subscribed
                    throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_PROGRAM_ALREADY_SUBSCRIBED, MessageConstants.ERROR);
                } else {
                    isAlreadySubscribedSubscriptionEnded = true;
                }
            }
        }
        boolean isCouponApplied = false;
        String stripeCouponId = null;
        StripeCouponAndOfferCodeMapping stripeCouponAndOfferCodeMapping = null;
        if (offerCodeDetail != null) {
            stripeCouponAndOfferCodeMapping = stripeCouponAndOfferCodeMappingRepository.findByOfferCodeDetail(offerCodeDetail);
            if (stripeCouponAndOfferCodeMapping == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_STRIPE_COUPON_NOT_FOUND_FOR_OFFER, null);
            }
            isCouponApplied = true;
            stripeCouponId = stripeCouponAndOfferCodeMapping.getStripeCouponId();
        }
        List<Object> items = new ArrayList<>();
        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("price", priceId);
        items.add(itemMap);
        Map<String, Object> params = new HashMap<>();
        params.put(StripeConstants.STRIPE_PROP_CUSTOMER, customerId);
        params.put("items", items);
        params.put("default_payment_method", paymentMethodId);
        if (isCouponApplied) {
            params.put("coupon", stripeCouponId);
        }
        Subscription subscription = null;
        StripePayment stripePayment = new StripePayment();
        InvoiceManagement invoiceManagement = invoiceManagementRepository.findByOrderManagement(orderManagement);
        stripePayment.setOrderManagement(orderManagement);
        stripePayment.setInvoiceManagement(invoiceManagement);
        OrderResponseView orderResponseView = new OrderResponseView();
        orderResponseView.setOrderId(orderManagement.getOrderId());

        try {
            subscription = Subscription.create(params);
            if (!isCouponApplied) {
                stripePayment.setAmountPaid(program.getProgramPrices().getPrice());
            } else {
                if (DiscountsConstants.MODE_FREE.equalsIgnoreCase(offerCodeDetail.getOfferMode())) {
                    stripePayment.setAmountPaid(0.00);
                } else {
                    stripePayment.setAmountPaid(offerCodeDetail.getOfferPrice().getPrice());
                }
            }

            if (isCouponApplied) {
                //Mapping created between stripe subscription and stripe coupons.
                StripeSubscriptionAndCouponMapping stripeSubscriptionAndCouponMapping = new StripeSubscriptionAndCouponMapping();
                stripeSubscriptionAndCouponMapping.setStripeCouponAndOfferCodeMapping(stripeCouponAndOfferCodeMapping);
                stripeSubscriptionAndCouponMapping.setStripeSubscriptionId(subscription.getId());

                Date now = new Date();
                stripeSubscriptionAndCouponMapping.setSubscriptionStartDate(now);

                LocalDateTime localDateTime = now.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                localDateTime = localDateTime.plusMonths(offerCodeDetail.getOfferDuration().getDurationInMonths());
                LocalDate localDate = localDateTime.toLocalDate();
                LocalTime endLocalTime = LocalTime.of(23, 59, 59);
                localDateTime = localDate.atTime(endLocalTime);
                Date couponEndDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                stripeSubscriptionAndCouponMapping.setCouponEndDate(couponEndDate);

                stripeSubscriptionAndCouponMappingRepository.save(stripeSubscriptionAndCouponMapping);
            }

            stripePayment.setAmountRefunded(null);
            stripePayment.setRefundTransactionId(null);
            stripePayment.setIsRefundUnderProcessing(false);
            stripePayment.setSubscriptionId(subscription.getId());
            stripePayment.setInvoiceId(subscription.getLatestInvoice());

            // Retrieving invoice object in-order to fetch charge id which will be used for Refund related operations.
            Invoice invoice =
                    Invoice.retrieve(subscription.getLatestInvoice());
            if (invoice != null) {
                stripePayment.setChargeId(invoice.getCharge());

                // Introducing 2 secs delay to check Charge status
                Thread.sleep(2000);

                Charge charge;
                try {
                    charge = Charge.retrieve(invoice.getCharge());
                    /**
                     * If charge is getting failed, cancelling the subscription and throwing error
                     */
                    if (charge != null) {
                        if (charge.getStatus().equalsIgnoreCase(StripeConstants.STRIPE_PROP_FAILED) || charge.getStatus().equalsIgnoreCase("pending")) {
                            stripePayment.setTransactionStatus(KeyConstants.KEY_FAILURE);
                            stripePayment.setErrorMessage("Charge failed after subscription");
                            stripePaymentRepository.save(stripePayment);
                            subscription.cancel(); // Cancelling the stripe subscription
                            return new ResponseModel(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_ADDING_SUBSCRIPTION, orderResponseView);
                        }

                        String paymentIntentId = charge.getPaymentIntent();
                        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
                        if (paymentIntent != null && (paymentIntent.getStatus().equalsIgnoreCase(StripeConstants.STRIPE_PROP_FAILED) || paymentIntent.getStatus().equalsIgnoreCase("canceled"))) {
                            stripePayment.setTransactionStatus(KeyConstants.KEY_FAILURE);
                            stripePayment.setErrorMessage("Payment intent failed after subscription");
                            stripePaymentRepository.save(stripePayment);
                            subscription.cancel(); // Cancelling the stripe subscription
                            return new ResponseModel(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_ADDING_SUBSCRIPTION, orderResponseView);
                        }
                    }
                } catch (StripeException e) {
                    e.printStackTrace();
                }
            }
            stripePayment.setTransactionStatus(KeyConstants.KEY_PAID);
            stripePayment = stripePaymentRepository.save(stripePayment);
            fitwiseQboEntityService.createAndSyncQboInvoice(invoiceManagement);
            /**
             * Paying the instructor his share
             */
            if (offerCodeDetail == null || !DiscountsConstants.MODE_FREE.equalsIgnoreCase(offerCodeDetail.getOfferMode())) {
                payToInstructor(stripePayment);
            }
        } catch (StripeException e) {
            stripePayment.setErrorCode(e.getCode());
            stripePayment.setErrorStatusCode(e.getStatusCode().toString());
            stripePayment.setErrorMessage(e.getStripeError().getMessage());
            stripePayment.setDeclinedCode(e.getStripeError().getDeclineCode());
            stripePaymentRepository.save(stripePayment);
            log.error(MessageConstants.MSG_ERR_EXCEPTION + e.getMessage());
            return new ResponseModel(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_ADDING_SUBSCRIPTION, orderResponseView);
        } catch (InterruptedException e) {
            log.error(MessageConstants.MSG_ERR_EXCEPTION + e.getMessage());
            Thread.currentThread().interrupt();
        }
        log.info("Subscription created --------------------> {}", subscription.getId());
        // Getting the active subscription status
        StripeSubscriptionStatus subscriptionStatus = stripeSubscriptionStatusRepository.findBySubscriptionStatusIgnoreCaseContaining(KeyConstants.KEY_ACTIVE);
        StripeSubscriptionAndUserProgramMapping stripeSubscriptionAndUserProgramMapping = new StripeSubscriptionAndUserProgramMapping();
        stripeSubscriptionAndUserProgramMapping.setUser(userComponents.getUser());
        stripeSubscriptionAndUserProgramMapping.setProgram(program);
        stripeSubscriptionAndUserProgramMapping.setStripeSubscriptionId(subscription.getId());
        stripeSubscriptionAndUserProgramMapping.setSubscriptionStatus(subscriptionStatus);
        stripeSubscriptionAndUserProgramMappingRepository.save(stripeSubscriptionAndUserProgramMapping);
        StripeSubscriptionChangesTracker subscriptionChangesTracker = new StripeSubscriptionChangesTracker();
        subscriptionChangesTracker.setIsSubscriptionActive(true);
        subscriptionChangesTracker.setOrderManagement(orderManagement);
        subscriptionChangesTracker.setSubscriptionId(subscription.getId());
        stripeSubscriptionChangesTrackerRepository.save(subscriptionChangesTracker);
        if (isNewSubscription) {
            addNewlySubscribedProgramData(user, program, platformType, true, true, orderManagement);
        }
        if (isAlreadySubscribedSubscriptionEnded) {
            overrideAlreadySubscribedProgramData(user, programSubscription, platformType, true, orderManagement);
        }
        try{
            //Sending mail to member
            String subject = EmailConstants.PROGRAM_SUBSCRIPTION_SUBJECT.replace(EmailConstants.PROGRAM_TITLE,  program.getTitle() );
            String mailBody = EmailConstants.PROGRAM_SUBSCRIPTION_CONTENT.replace(EmailConstants.PROGRAM_TITLE, "<b>" + program.getTitle() + "</b>");
            String userName = fitwiseUtils.getUserFullName(user);
            User instructor = program.getOwner();
            String memberProgram = EmailConstants.MEMBER_PROGRAM_LINK.replace(EmailConstants.LITERAL_APP_URL, dynamicLinkService.constructProgramLinkForMember(program.getProgramId(),instructor));
            mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, mailBody)
                    .replace(EmailConstants.EMAIL_SUPPORT_URL, memberProgram);
            mailBody = emailContentUtil.replaceMemberAppUrl(mailBody);
            try {
                File file = invoicePDFGenerationService.generateInvoicePdf(orderManagement.getOrderId(), StringConstants.INVOICE + invoiceManagement.getInvoiceNumber());
                List<String> fileList = Collections.singletonList(file.getAbsolutePath());
                asyncMailer.sendHtmlMailWithAttachment(user.getEmail(),null, subject, mailBody, fileList);
            } catch (Exception e) {
                log.error("Invoice PDF generation failed for subscription mail. Order id : " + orderManagement.getOrderId());
                log.error(MessageConstants.MSG_ERR_EXCEPTION + e.getMessage());
                asyncMailer.sendHtmlMail(user.getEmail(), subject, mailBody);
            }
            /*
             * Stripe connect onboarding reminder mail
             * */
            boolean isOnboardingDetailsSubmitted = false;
            StripeAccountAndUserMapping stripeAccountAndUserMapping = stripeAccountAndUserMappingRepository.findByUserUserId(instructor.getUserId());
            if(stripeAccountAndUserMapping != null && stripeAccountAndUserMapping.getIsDetailsSubmitted() != null && stripeAccountAndUserMapping.getIsDetailsSubmitted()){
                isOnboardingDetailsSubmitted = true;
            }
            boolean isOnBoardedViaPayPal = false;
            UserAccountAndPayPalIdMapping userAccountAndPayPalIdMapping = userAccountAndPayPalIdMappingRepository.findByUserUserId(instructor.getUserId());
            if (userAccountAndPayPalIdMapping != null && !userAccountAndPayPalIdMapping.getPayPalId().isEmpty()) {
                isOnBoardedViaPayPal = true;
            }
            if (!isOnboardingDetailsSubmitted && !isOnBoardedViaPayPal) {
                userName = fitwiseUtils.getUserFullName(instructor);
                subject = EmailConstants.STRIPE_CONNECT_ONBOARD_REMINDER_SUBJECT;
                String trainnrDashboard = EmailConstants.STRIPE_CONNECT_ONBOARD_DASHBOARD_LINK.replace(EmailConstants.LITERAL_APP_URL, generalProperties.getInstructorBaseUrl() + RestUrlConstants.APP_INSTRUCTOR_DASHBOARD);
                mailBody = EmailConstants.STRIPE_CONNECT_ONBOARD_REMINDER_CONTENT;
                mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
                        .replace(EmailConstants.EMAIL_BODY, mailBody)
                        .replace(EmailConstants.EMAIL_SUPPORT_URL, trainnrDashboard);
                mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
                asyncMailer.sendHtmlMail(instructor.getEmail(), subject, mailBody);
            }
        }catch(Exception e){
            log.error(MessageConstants.MSG_ERR_EXCEPTION + e.getMessage());
        }
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SUBSCRIPTION_ADDED, orderResponseView);
    }

    @Transactional
    public ResponseModel getStripeSavedCards() throws StripeException {
        log.info("Get stripe saved cards starts.");
        long apiStartTimeMillis = new Date().getTime();
        Stripe.apiKey = stripeProperties.getApiKey();
        User user = userComponents.getUser();
        StripeCustomerAndUserMapping customerAndUserMapping = stripeCustomerAndUserMappingRepository.findTop1ByUserUserId(user.getUserId());
        StripeSavedCardsResponseView cardsResponseView = new StripeSavedCardsResponseView();
        log.info("Get user and customerAndUserMapping : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();

        if (customerAndUserMapping != null) {
            Map<String, Object> params = new HashMap<>();
            params.put(StripeConstants.STRIPE_PROP_CUSTOMER, customerAndUserMapping.getStripeCustomerId());
            params.put("type", "card");
            PaymentMethodCollection paymentMethodCollection = PaymentMethod.list(params);
            log.info("Get payment method collection : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            List<PaymentMethod> savedCardData = paymentMethodCollection.getData();
            cardsResponseView.setCustomerId(customerAndUserMapping.getStripeCustomerId());
            List<StripePaymentMethodResponseView> stripePaymentMethodResponseViews = new ArrayList<>();

            for (PaymentMethod cardData : savedCardData) {
                StripePaymentMethodResponseView stripePaymentMethodResponseView = new StripePaymentMethodResponseView();
                stripePaymentMethodResponseView.setCardType(cardData.getCard().getBrand());
                stripePaymentMethodResponseView.setPaymentMethodId(cardData.getId());
                stripePaymentMethodResponseView.setMaskedCardNumber(cardData.getCard().getLast4());
                stripePaymentMethodResponseViews.add(stripePaymentMethodResponseView);
            }
            cardsResponseView.setSavedCards(stripePaymentMethodResponseViews);
            log.info("Construct response view : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        }
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get stripe saved cards ends.");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SAVED_CARDS_FETCHED, cardsResponseView);
    }

    /**
     * Cancelling program subscription - Stripe payment gateway
     *
     * @param programId
     * @param platformId
     * @return
     */
    public ResponseModel cancelStripeProgramSubscription(Long programId, Long platformId) {
        log.info("Cancel stripe program subscription starts.");
        long apiStartTimeMillis = new Date().getTime();

        // If the subscription was done in Apple pay and user is trying to cancel that in android / web platforms
        if (platformId == 2) {
            throw new ApplicationException(Constants.CAN_EDIT, MessageConstants.MSG_APPLE_PAY_CANCELLATION_IN_AUTH_NET, null);
        }

        User user = userComponents.getUser();
        ResponseModel responseModel = cancelStripeProgramSubscription(programId, platformId, user, true);
        log.info("Cancel stripe program subscription : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Cancel stripe program subscription ends.");
        return responseModel;
    }


    /**
     * Cancelling program subscription - Stripe payment gateway
     *
     * @param programId
     * @param platformId
     * @return
     */
    @Transactional
    public ResponseModel cancelStripeProgramSubscription(Long programId, Long platformId, User user, boolean sendMailNotification) {
        log.info("Cancel stripe program subscriptions - method in stripe service starts.");
        long apiStartTimeMillis = new Date().getTime();

        // If the subscription was done in Apple pay and user is trying to cancel that in android / web platforms
        if (platformId == 2) {
            throw new ApplicationException(Constants.CAN_EDIT, MessageConstants.MSG_APPLE_PAY_CANCELLATION_IN_AUTH_NET, null);
        }
        Stripe.apiKey = stripeProperties.getApiKey();
        Programs program = validationService.validateProgramId(programId);
        StripeSubscriptionAndUserProgramMapping mapping = stripeSubscriptionAndUserProgramMappingRepository
                .findTop1ByUserUserIdAndProgramProgramId(user.getUserId(),
                        program.getProgramId());
        log.info("Queries ti get program and StripeSubscriptionAndUserProgramMapping : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();

        if (mapping == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PROGRAM_NOT_SUBSCRIBED, null);
        }
        try {
            Subscription subscription = Subscription.retrieve(mapping.getStripeSubscriptionId());
            subscription.cancel();
        } catch (StripeException e) {
            if (StripeConstants.STRIPE_RESOURCE_MISSING.equalsIgnoreCase(e.getCode())) {
                log.warn("Stripe Subscription not found for : " + mapping.getStripeSubscriptionId());
            } else {
                log.error("Stripe Subscription failure : " + e.getMessage());
                throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_CANCELLING_SUBSCRIPTION_FAILED, null);
            }
        }
        log.info("Get stripe subscription and cancel it : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), programId);
        log.info("Query to get program subscription : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if (programSubscription != null) {
            StripeSubscriptionStatus stripeCancelledSubscriptionStatus = stripeSubscriptionStatusRepository.findBySubscriptionStatusIgnoreCaseContaining(KeyConstants.KEY_SUBSCRIPTION_CANCELLED);

            // Logging a new entry in the table that the subscription is cancelled
            StripeSubscriptionAndUserProgramMapping stripeSubscriptionAndUserProgramMapping = new StripeSubscriptionAndUserProgramMapping();
            stripeSubscriptionAndUserProgramMapping.setUser(user);
            stripeSubscriptionAndUserProgramMapping.setProgram(program);
            stripeSubscriptionAndUserProgramMapping.setSubscriptionStatus(stripeCancelledSubscriptionStatus);
            stripeSubscriptionAndUserProgramMapping.setStripeSubscriptionId(mapping.getStripeSubscriptionId());
            stripeSubscriptionAndUserProgramMappingRepository.save(stripeSubscriptionAndUserProgramMapping);
            log.info("Queries to get StripeSubscriptionStatus and save StripeSubscriptionAndUserProgramMapping : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();

            // Logging the cancel event in subscription tracker table
            try {
                StripeSubscriptionChangesTracker tracker = stripeSubscriptionChangesTrackerRepository.findTop1BySubscriptionIdOrderByModifiedDateDesc(mapping.getStripeSubscriptionId());
				StripeSubscriptionChangesTracker subscriptionChangesTracker = new StripeSubscriptionChangesTracker();
				subscriptionChangesTracker.setOrderManagement(tracker.getOrderManagement());
				subscriptionChangesTracker.setSubscriptionId(tracker.getSubscriptionId());
				subscriptionChangesTracker.setIsSubscriptionActive(false);
				stripeSubscriptionChangesTrackerRepository.save(subscriptionChangesTracker);
			} catch (Exception exception) {
				log.error(exception.getMessage());
			}
            log.info("Query save StripeSubscriptionChangesTracker : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            // Setting the auto-renewal flag as false
            programSubscription.setAutoRenewal(false);
            programSubscriptionRepo.save(programSubscription);
            log.info("Query to update the program's auto renewal off : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            // Sending mail to user for subscription cancel event
            if (sendMailNotification) {
                String subject = EmailConstants.AUTORENEWAL_SUBJECT;
                String mailBody = EmailConstants.AUTORENEWAL_PROGRAM_CONTENT.replace(EmailConstants.LITERAL_PROGRAM_NAME, "<b>" + program.getTitle() + "</b>");
                String userName = fitwiseUtils.getUserFullName(user);
                mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, mailBody);
                mailBody = emailContentUtil.replaceMemberAppUrl(mailBody);
                asyncMailer.sendHtmlMail(user.getEmail(), subject, mailBody);
            }
            log.info("Async mailer activated : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        }
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Cancel stripe program subscription - method in stripe service ends.");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SUBSCRIPTION_CANCELLED_SUCCESSFULLY, null);
    }


    /**
     * Service method to handle web hooks from stripe
     *
     * @param stripeEvent
     * @return
     */
    @Transactional
    public ResponseModel handleStripeNotification(Event stripeEvent) {
        log.info("Handle stripe notification starts.");
        long apiStartTimeMillis = new Date().getTime();
        Optional<StripeObject> strObj = stripeEvent.getDataObjectDeserializer().getObject();
        if (stripeEvent.getType().equalsIgnoreCase("account.updated")) {
            ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
            Runnable task2 = () -> {
                Account account = null;
				if (strObj.isPresent()) {
					account = (Account) strObj.get();
				}
                StripeAccountAndUserMapping stripeAccountAndUserMapping = stripeAccountAndUserMappingRepository.findByStripeAccountId(account.getId());
                if (stripeAccountAndUserMapping != null) {
                    if (account.getDetailsSubmitted()) {
                        stripeAccountAndUserMapping.setIsDetailsSubmitted(true);
                    }
                    if (account.getPayoutsEnabled() && account.getChargesEnabled()) {
                        stripeAccountAndUserMapping.setIsOnBoardingCompleted(true);
                    }
                    stripeAccountAndUserMappingRepository.save(stripeAccountAndUserMapping);
                }
            };
            ses.schedule(task2, 2, TimeUnit.MINUTES);
            ses.shutdown();
        }
        log.info("Query: update stripe account and user mapping in DB : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        /**
         * Charge succeeded notification from stripe
         * Occurs whenever a new charge is created and is successful.
         */
        if (stripeEvent.getType().equalsIgnoreCase("invoice.created")) {
            // Invoice created
            Invoice invoice = (Invoice) stripeEvent.getDataObjectDeserializer().getObject().get();
            String subscriptionId = invoice.getSubscription();
            StripeInvoiceAndSubscriptionMapping stripeInvoiceAndSubscriptionMapping = new StripeInvoiceAndSubscriptionMapping();
            stripeInvoiceAndSubscriptionMapping.setStripeInvoiceId(invoice.getId());
            stripeInvoiceAndSubscriptionMapping.setStripeSubscriptionId(invoice.getSubscription());
            stripeInvoiceAndSubscriptionMapping.setChargeId(invoice.getCharge());
            stripeInvoiceAndSubscriptionMappingRepository.save(stripeInvoiceAndSubscriptionMapping);
            if((invoice.getDiscount() != null && invoice.getDiscounts() != null && !invoice.getDiscounts().isEmpty()) && (stripeSubscriptionAndUserProgramMappingRepository.existsByStripeSubscriptionId(subscriptionId) || stripeSubscriptionAndUserPackageMappingRepository.existsByStripeSubscriptionId(subscriptionId))) {
                // Saving Stripe invoice and subscription id
                // This is used to get the subscription id while payment_intent is getting succeeded
                // Payment_intent object doesn't have subscription id
                /*
                 * Updating subscription for free offers. Since it is free, payment_intent.succeeded is not received.
                 */
                StripeSubscriptionAndCouponMapping stripeSubscriptionAndCouponMapping = stripeSubscriptionAndCouponMappingRepository.findByStripeSubscriptionId(invoice.getSubscription());
                if (stripeSubscriptionAndCouponMapping != null) {
                    Date now = new Date();
                    if (now.before(stripeSubscriptionAndCouponMapping.getCouponEndDate())) {
                        OfferCodeDetail offerCodeDetail = stripeSubscriptionAndCouponMapping.getStripeCouponAndOfferCodeMapping().getOfferCodeDetail();
                        if (DiscountsConstants.MODE_FREE.equalsIgnoreCase(offerCodeDetail.getOfferMode())) {
                            // Introducing 3min delay since first subscription might be getting logged in DB.
                            ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
                            Runnable task2 = () -> {
                                StripePayment stripePayment = stripePaymentRepository.findTop1ByInvoiceIdAndChargeId(invoice.getId(), invoice.getCharge());
                                if (stripePayment == null) {
                                    try {
                                        renewSubscriptionFromWebhook(subscriptionId, invoice.getId(), invoice.getCharge(), offerCodeDetail);
                                    } catch (StripeException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            ses.schedule(task2, 3, TimeUnit.MINUTES);
                            ses.shutdown();
                        }
                    }
                }
            }
        }
        log.info("Creating invoice : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        /**
         * Occurs when a PaymentIntent has successfully completed payment.
         */
        if (stripeEvent.getType().equalsIgnoreCase("payment_intent.succeeded")) {
            PaymentIntent paymentIntent = null;
			if (strObj.isPresent()) {
				paymentIntent = (PaymentIntent) strObj.get();
			}
            String invoiceId = paymentIntent.getInvoice();
            StripeInvoiceAndSubscriptionMapping invoiceAndSubscriptionMapping = stripeInvoiceAndSubscriptionMappingRepository.findByStripeInvoiceId(invoiceId);
            if (invoiceAndSubscriptionMapping != null) {
                String subscriptionId = invoiceAndSubscriptionMapping.getStripeSubscriptionId();
                String chargeId = invoiceAndSubscriptionMapping.getChargeId();
                boolean isDiscountApplied = false;
                if (chargeId == null) {
                    Stripe.apiKey = stripeProperties.getApiKey();
                    try {
                        Invoice invoice = Invoice.retrieve(invoiceId);
                        chargeId = invoice.getCharge();
                        if(invoice.getDiscount() != null && invoice.getDiscounts() != null && !invoice.getDiscounts().isEmpty()){
                            isDiscountApplied = true;
                        }
                    } catch (StripeException e) {
                        e.printStackTrace();
                    }
                }
                // Introducing 3min delay since first subscription might be getting logged in DB.
                ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
                String finalChargeId = chargeId;
                boolean finalIsDiscountApplied = isDiscountApplied;
                Runnable task2 = () -> {
                    StripePayment stripePayment = stripePaymentRepository.findTop1ByInvoiceIdAndChargeId(invoiceId, finalChargeId);
                    // For first time subscription, data is populated into DB directly from API response. In-order to overcome webhook data also getting added
                    // in that scenario, using this below check!
                    if (stripePayment == null) {
                        //Updating subscription data once payment is successful
                        //Obtaining offer code applied for the subscription
                        OfferCodeDetail renewalOfferCodeDetail = null;
                        StripeSubscriptionAndCouponMapping stripeSubscriptionAndCouponMapping = stripeSubscriptionAndCouponMappingRepository.findByStripeSubscriptionId(subscriptionId);
                        if (finalIsDiscountApplied && stripeSubscriptionAndCouponMapping != null) {
                            Date now = new Date();
                            if (now.before(stripeSubscriptionAndCouponMapping.getCouponEndDate())) {
                                OfferCodeDetail offerCodeDetail = stripeSubscriptionAndCouponMapping.getStripeCouponAndOfferCodeMapping().getOfferCodeDetail();
                                if (DiscountsConstants.MODE_PAY_AS_YOU_GO.equalsIgnoreCase(offerCodeDetail.getOfferMode())) {
                                    renewalOfferCodeDetail = offerCodeDetail;
                                }
                            }
                        }
                        try {
                            renewSubscriptionFromWebhook(subscriptionId, invoiceId, finalChargeId, renewalOfferCodeDetail);
                        } catch (StripeException exception) {
                            log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
                        }
                    }
                };
                ses.schedule(task2, 3, TimeUnit.MINUTES);
                ses.shutdown();
            }
        }
        log.info("Renew subscription : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        /**
         * Occurs when a PaymentIntent has started processing
         */
        /**
         * Occurs when a PaymentIntent has failed the attempt to create a payment method or a payment.
         */
        if (stripeEvent.getType().equalsIgnoreCase("payment_intent.payment_failed")) {
            PaymentIntent paymentIntent = null;
			if (strObj.isPresent()) {
				paymentIntent = (PaymentIntent) strObj.get();
			}
            StripeInvoiceAndSubscriptionMapping stripeInvoiceAndSubscriptionMapping = stripeInvoiceAndSubscriptionMappingRepository.findByStripeInvoiceId(paymentIntent.getInvoice());
            if (stripeInvoiceAndSubscriptionMapping != null) {
                String subscriptionId = stripeInvoiceAndSubscriptionMapping.getStripeSubscriptionId();
                //Cancel program subscription
                StripeSubscriptionAndUserProgramMapping stripeSubscriptionAndUserProgramMapping = stripeSubscriptionAndUserProgramMappingRepository.findTop1ByStripeSubscriptionId(subscriptionId);
                if (stripeSubscriptionAndUserProgramMapping != null) {
                    User user = stripeSubscriptionAndUserProgramMapping.getUser();
                    Programs program = stripeSubscriptionAndUserProgramMapping.getProgram();
                    ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), program.getProgramId());
                    cancelStripeProgramSubscription(program.getProgramId(), programSubscription.getSubscribedViaPlatform().getPlatformTypeId(), user, true);
                }
                //Cancel package subscription
                StripeSubscriptionAndUserPackageMapping stripeSubscriptionAndUserPackageMapping = stripeSubscriptionAndUserPackageMappingRepository.findTop1ByStripeSubscriptionId(subscriptionId);
                if (stripeSubscriptionAndUserPackageMapping != null) {
                    User user = stripeSubscriptionAndUserPackageMapping.getUser();
                    SubscriptionPackage subscriptionPackage = stripeSubscriptionAndUserPackageMapping.getSubscriptionPackage();
                    PackageSubscription packageSubscription = packageSubscriptionRepository.findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageIdOrderBySubscribedDateDesc(user.getUserId(), subscriptionPackage.getSubscriptionPackageId());
                    cancelStripeProgramSubscription(subscriptionPackage.getSubscriptionPackageId(), packageSubscription.getSubscribedViaPlatform().getPlatformTypeId(), user, true);
                }
            }
        }
        log.info("Cancel package subscription when payment failed : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        /**
         * Occurs when a PaymentIntent transitions to requires_action state
         */
        if (stripeEvent.getType().equalsIgnoreCase("payment_intent.requires_action")) {
            PaymentIntent paymentIntent = null;
			if (strObj.isPresent()) {
				paymentIntent = (PaymentIntent) strObj.get();
			}
            StripeInvoiceAndSubscriptionMapping stripeInvoiceAndSubscriptionMapping = stripeInvoiceAndSubscriptionMappingRepository.findByStripeInvoiceId(paymentIntent.getInvoice());
            if (stripeInvoiceAndSubscriptionMapping != null) {
                String subscriptionId = stripeInvoiceAndSubscriptionMapping.getStripeSubscriptionId();
                StripeSubscriptionAndUserProgramMapping stripeSubscriptionAndUserProgramMapping = stripeSubscriptionAndUserProgramMappingRepository.findTop1ByStripeSubscriptionId(subscriptionId);
                if (stripeSubscriptionAndUserProgramMapping != null) {
                    User user = stripeSubscriptionAndUserProgramMapping.getUser();
                    Programs program = stripeSubscriptionAndUserProgramMapping.getProgram();
                    ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), program.getProgramId());
                    cancelStripeProgramSubscription(program.getProgramId(), programSubscription.getSubscribedViaPlatform().getPlatformTypeId(), user, true);
                }
                //Cancel package subscription
                StripeSubscriptionAndUserPackageMapping stripeSubscriptionAndUserPackageMapping = stripeSubscriptionAndUserPackageMappingRepository.findTop1ByStripeSubscriptionId(subscriptionId);
                if (stripeSubscriptionAndUserPackageMapping != null) {
                    User user = stripeSubscriptionAndUserPackageMapping.getUser();
                    SubscriptionPackage subscriptionPackage = stripeSubscriptionAndUserPackageMapping.getSubscriptionPackage();
                    PackageSubscription packageSubscription = packageSubscriptionRepository.findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageIdOrderBySubscribedDateDesc(user.getUserId(), subscriptionPackage.getSubscriptionPackageId());
                    cancelStripeProgramSubscription(subscriptionPackage.getSubscriptionPackageId(), packageSubscription.getSubscribedViaPlatform().getPlatformTypeId(), user, true);
                }
            }
        }
        log.info("Cancel package subscription when payment intent requires action : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        /**
         * Occurs whenever a payout is created.
         */
        if (stripeEvent.getType().equalsIgnoreCase(KeyConstants.KEY_PAYOUT_CREATED)) {
            Payout payout = null;
			if (strObj.isPresent()) {
				payout = (Payout) strObj.get();
			}
            logPayoutEvent(payout, KeyConstants.KEY_PAYOUT_CREATED);
        }
        /**
         * payout.paid
         *
         * Occurs whenever a payout is expected to be available in the destination account.
         * If the payout fails, a payout.failed notification is also sent, at a later time.
         */
        if (stripeEvent.getType().equalsIgnoreCase(KeyConstants.KEY_PAYOUT_PAID)) {
            logger.info("STRIPE WEBHOOK NOTIFICATION PAYOUT PAID" + stripeEvent.getId());
            Payout payout = null;
			if (strObj.isPresent()) {
				payout = (Payout) strObj.get();
			}
            logPayoutEvent(payout, KeyConstants.KEY_PAYOUT_PAID);
        }
        /**
         * Occurs whenever a payout attempt fails.
         */
        if (stripeEvent.getType().equalsIgnoreCase(KeyConstants.KEY_PAYOUT_FAILED)) {
            Payout payout = null;
			if (strObj.isPresent()) {
				payout = (Payout) strObj.get();
			}
            logPayoutEvent(payout, KeyConstants.KEY_PAYOUT_FAILED);
        }
        /**
         * Occurs whenever a payout is canceled.
         */
        if (stripeEvent.getType().equalsIgnoreCase(KeyConstants.KEY_PAYOUT_CANCELED)) {
            Payout payout = null;
			if (strObj.isPresent()) {
				payout = (Payout) strObj.get();
			}
            logPayoutEvent(payout, KeyConstants.KEY_PAYOUT_CANCELED);
        }
        /**
         * Will be received once a transfer is paid
         */
        if (stripeEvent.getType().equalsIgnoreCase(KeyConstants.KEY_TRANSFER_PAID)) {
            //Introducing delay for instructor transfer after subscription
            ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
            Runnable task2 = () -> {
                Transfer transfer = (Transfer) stripeEvent
                        .getDataObjectDeserializer()
                        .getObject().get();
                InstructorPayment instructorPayment = instructorPaymentRepository.findTop1ByStripeTransferId(transfer.getId());
                if (instructorPayment != null) {
                    instructorPayment.setStripeTransferStatus(stripeEvent.getType());
                    instructorPaymentRepository.save(instructorPayment);
                }
            };
            ses.schedule(task2, 1, TimeUnit.MINUTES);
            ses.shutdown();
        }
        /**
         * Will be received once a transfer is failed
         */
        if (stripeEvent.getType().equalsIgnoreCase(KeyConstants.KEY_TRANSFER_FAILED)) {
            //Introducing delay for instructor transfer after subscription
            ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
            Runnable task2 = () -> {
                Transfer transfer = (Transfer) stripeEvent
                        .getDataObjectDeserializer()
                        .getObject().get();
                InstructorPayment instructorPayment = instructorPaymentRepository.findTop1ByStripeTransferId(transfer.getId());
                if (instructorPayment != null) {
                    instructorPayment.setIsTransferDone(false);
                    instructorPayment.setIsTransferFailed(true);
                    instructorPayment.setStripeTransferStatus(stripeEvent.getType());
                    instructorPaymentRepository.save(instructorPayment);
                }
            };
            ses.schedule(task2, 1, TimeUnit.MINUTES);
            ses.shutdown();
        }
        if (stripeEvent.getType().equalsIgnoreCase(KeyConstants.KEY_TRANSFER_REVERSED)) {
            Transfer transfer = null;
			if (strObj.isPresent()) {
				transfer = (Transfer) strObj.get();
			}
            InstructorPayment instructorPayment = instructorPaymentRepository.findTop1ByStripeTransferId(transfer.getId());
            if (instructorPayment != null) {
                instructorPayment.setStripeTransferStatus(stripeEvent.getType());
                instructorPaymentRepository.save(instructorPayment);
            }
        }
        log.info("Query: updating instructor payment repo based on transfer paid and failed : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if (stripeEvent.getType().equalsIgnoreCase(KeyConstants.KEY_TOPUP_SUCCEEDED)) {
            Topup topup = null;
            if (strObj.isPresent()) {
            	topup = (Topup) strObj.get();
			}
            AppleSettlementByStripe appleSettlementByStripe = appleSettlementByStripeRepository.findTop1ByStripeTopUpId(topup.getId());
            if (appleSettlementByStripe != null) {
                ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
                Runnable task2 = () -> {
                    appleSettlementByStripe.setStripeTopUpStatus(stripeEvent.getType());
                    appleSettlementByStripeRepository.save(appleSettlementByStripe);
                    InstructorPayment instructorPayment = appleSettlementByStripe.getInstructorPayment();
                    StripeAccountAndUserMapping stripeAccountAndUserMapping =
                            stripeAccountAndUserMappingRepository.findByUserUserId(instructorPayment.getOrderManagement().getProgram().getOwner().getUserId());
                    Stripe.apiKey = stripeProperties.getApiKey();
                    Map<String, Object> params = new HashMap<>();
                    params.put(KeyConstants.KEY_AMOUNT, (int) (instructorPayment.getInstructorShare() * 100));
                    params.put(StripeConstants.STRIPE_PROP_CURRENCY, "usd");
                    params.put(StripeConstants.STRIPE_PROP_DESTINATION, stripeAccountAndUserMapping.getStripeAccountId());
                    params.put(StripeConstants.STRIPE_PROP_TRANSFER_GROUP, instructorPayment.getOrderManagement().getOrderId());
                    try {
                        /**
                         * Creating a transfer to payout to instructor
                         */
                        Transfer transfer = Transfer.create(params);
                        instructorPayment.setIsTransferDone(true);
                        instructorPayment.setTransferMode(KeyConstants.KEY_STRIPE);
                        instructorPayment.setStripeTransferId(transfer.getId());
                        instructorPayment.setIsTransferAttempted(true);
                        instructorPayment.setIsTransferFailed(false);
                        instructorPayment.setTransferDate(new Date());
                        instructorPayment.setStripeTransferStatus(KeyConstants.KEY_TRANSFER_CREATED);
                        instructorPaymentRepository.save(instructorPayment);
                        /**
                         * Checking whether the Instructors has any credits (Balances to be paid to Trainnr in case of Refunds)
                         * and initiating reverse transfer wrt and updating the credits table
                         */
                        User instructor = instructorPayment.getOrderManagement().getProgram().getOwner();
                        InstructorPaymentCredits instructorPaymentCredit = instructorPaymentsCreditsRepository.findByInstructorUserIdAndCurrencyType(instructor.getUserId(), KeyConstants.KEY_CURRENCY_US_DOLLAR);
                        if (instructorPaymentCredit != null && instructorPaymentCredit.getTotalCredits() > 0) {
                            double instructorShareAmount = instructorPayment.getInstructorShare();
                            double instructorCreditAmount = instructorPaymentCredit.getTotalCredits();
                            if (instructorCreditAmount >= instructorShareAmount) {
                                // If both transfer amount and credit amount is same, Reverse transfer the full amount from transfer
                                // If Credit amount is greater than share amount, reversing the transfer from the share amount
                                TransferReversal transferReversal = stripeConnectService.reverseTransfer(transfer.getId(), instructorShareAmount);
                                if (transferReversal != null) {
                                    stripeConnectService.reduceCreditAmount(instructorPayment, instructorPaymentCredit, instructorShareAmount);
                                }
                            } else {
                                // If Credit amount is lesser than share amount, reversing the transfer from the credit amount
                                TransferReversal transferReversal = stripeConnectService.reverseTransfer(transfer.getId(), instructorCreditAmount);
                                if (transferReversal != null) {
                                    stripeConnectService.reduceCreditAmount(instructorPayment, instructorPaymentCredit, instructorCreditAmount);
                                }
                            }
                        }
                    } catch (StripeException exception) {
                        instructorPayment.setIsTransferFailed(true);
                        instructorPayment.setIsTransferAttempted(true);
                        instructorPayment.setIsTransferDone(false);
                        instructorPaymentRepository.save(instructorPayment);
                        StripeTransferErrorLog stripeTransferErrorLog = new StripeTransferErrorLog();
                        stripeTransferErrorLog.setInstructorPayment(instructorPayment);
                        stripeTransferErrorLog.setErrorCode(exception.getCode());
                        stripeTransferErrorLog.setErrorMessage(exception.getMessage());
                        stripeTransferErrorLog.setStripeErrorCode(exception.getStripeError().getCode());
                        stripeTransferErrorLog.setStripeErrorMessage(exception.getStripeError().getMessage());
                        stripeTransferErrorLogRepository.save(stripeTransferErrorLog);
                        logger.error(MessageConstants.MSG_ERR_EXCEPTION, exception.getMessage());
                    }
                };
                ses.schedule(task2, 5, TimeUnit.MINUTES);
                ses.shutdown();
            }
        }
        log.info("Creating transfer : Time taken in millis : {}", (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if (stripeEvent.getType().equalsIgnoreCase(KeyConstants.KEY_TOPUP_FAILED)
                || stripeEvent.getType().equalsIgnoreCase(KeyConstants.KEY_TOPUP_CANCELLED) ||
                stripeEvent.getType().equalsIgnoreCase(KeyConstants.KEY_TOPUP_REVERSED)) {
            Topup topup = null;
            if (strObj.isPresent()) {
            	topup = (Topup) strObj.get();
			}
            AppleSettlementByStripe appleSettlementByStripe = appleSettlementByStripeRepository.findTop1ByStripeTopUpId(topup.getId());
            if (appleSettlementByStripe != null) {
                ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
                Runnable task2 = () -> {
                    appleSettlementByStripe.setStripeTopUpStatus(stripeEvent.getType());
                    appleSettlementByStripe.setStripeErrorMessage(stripeEvent.getType());
                    appleSettlementByStripeRepository.save(appleSettlementByStripe);
                    InstructorPayment instructorPayment = appleSettlementByStripe.getInstructorPayment();
                    instructorPayment.setIsTransferDone(false);
                    instructorPayment.setIsTransferFailed(true);
                    instructorPayment.setStripeTransferStatus(stripeEvent.getType());
                    instructorPaymentRepository.save(instructorPayment);
                };
                ses.schedule(task2, 5, TimeUnit.MINUTES);
                ses.shutdown();
            }
        }
        log.info("Apple settlement by stripe : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Handle stripe notification ends.");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_STRIPE_NOTIFICATION_RECEIVED, null);
    }

    /**
     * Logging web hook in a table
     *
     * @param stripeEvent
     */
    public void logWebHookEvent(Event stripeEvent) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(stripeEvent);
        StripeWebHookLogger stripeLogger = new StripeWebHookLogger();
        stripeLogger.setWebHookNotification(jsonString);
        stripeLogger.setEventType(stripeEvent.getType());
        stripeWebHookLoggerRepository.save(stripeLogger);
    }

    /**
     * Updating subscription data in tables once payment is successful
     *
     * @param subscriptionId
     * @param invoiceId
     * @param chargeId
     */
    private void renewSubscriptionFromWebhook(String subscriptionId, String invoiceId, String chargeId, OfferCodeDetail offerCodeDetail) throws StripeException {
        if (stripeSubscriptionAndUserProgramMappingRepository.existsByStripeSubscriptionId(subscriptionId) || stripeSubscriptionAndUserPackageMappingRepository.existsByStripeSubscriptionId(subscriptionId)) {
            boolean isOrderCreated = false;
            OrderManagement newOrderManagement = null;
            double price = 0;
            SubscriptionType subscriptionType = null;
            StripeSubscriptionAndUserProgramMapping stripeSubscriptionAndUserProgramMapping = null;
            if (stripeSubscriptionAndUserProgramMappingRepository.existsByStripeSubscriptionId(subscriptionId)) {
                stripeSubscriptionAndUserProgramMapping = stripeSubscriptionAndUserProgramMappingRepository.findTop1ByStripeSubscriptionId(subscriptionId);
                Programs program = stripeSubscriptionAndUserProgramMapping.getProgram();
                User user = stripeSubscriptionAndUserProgramMapping.getUser();
                price = program.getProgramPrices().getPrice();
                ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), program.getProgramId());
                /*
                 * Creating Order for the Transaction
                 */
                newOrderManagement = createProgramOrder(user, program, true, KeyConstants.KEY_STRIPE, programSubscription.getSubscribedViaPlatform(), "", offerCodeDetail);
                // Adding the subscription auditing data to table via the below method
                overrideAlreadySubscribedProgramData(user, programSubscription, programSubscription.getSubscribedViaPlatform(), true, newOrderManagement);
                isOrderCreated = true;
                subscriptionType = newOrderManagement.getSubscriptionType();
            } else if (stripeSubscriptionAndUserPackageMappingRepository.existsByStripeSubscriptionId(subscriptionId)) {
                StripeSubscriptionAndUserPackageMapping stripeSubscriptionAndUserPackageMapping = stripeSubscriptionAndUserPackageMappingRepository.findTop1ByStripeSubscriptionId(subscriptionId);
                SubscriptionPackage subscriptionPackage = stripeSubscriptionAndUserPackageMapping.getSubscriptionPackage();
                User user = stripeSubscriptionAndUserProgramMapping.getUser();
                price = subscriptionPackage.getPrice();
                PackageSubscription packageSubscription = packageSubscriptionRepository.findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageIdOrderBySubscribedDateDesc(user.getUserId(), subscriptionPackage.getSubscriptionPackageId());
                /*
                 * Creating Order for the Transaction
                 */
                newOrderManagement = createPackageOrder(user, subscriptionPackage, true, KeyConstants.KEY_STRIPE, packageSubscription.getSubscribedViaPlatform(), "", offerCodeDetail);
                // Adding the subscription auditing data to table via the below method
                overrideAlreadySubscribedPackageData(user, packageSubscription, packageSubscription.getSubscribedViaPlatform(), true, newOrderManagement);
                isOrderCreated = true;
                subscriptionType = newOrderManagement.getSubscriptionType();
            }
            InvoiceManagement invoiceManagement = null;
            if (isOrderCreated) {
                invoiceManagement = invoiceManagementRepository.findByOrderManagement(newOrderManagement);
                StripePayment stripePayment = new StripePayment();
                stripePayment.setOrderManagement(newOrderManagement);
                stripePayment.setInvoiceManagement(invoiceManagement);
                if (offerCodeDetail == null) {
                    stripePayment.setAmountPaid(price);
                } else {
                    if (DiscountsConstants.MODE_FREE.equalsIgnoreCase(offerCodeDetail.getOfferMode())) {
                        stripePayment.setAmountPaid(0.00);
                    } else {
                        stripePayment.setAmountPaid(offerCodeDetail.getOfferPrice().getPrice());
                    }
                }
                stripePayment.setAmountRefunded(null);
                stripePayment.setRefundTransactionId(null);
                stripePayment.setIsRefundUnderProcessing(false);
                stripePayment.setSubscriptionId(subscriptionId);
                stripePayment.setInvoiceId(invoiceId);
                stripePayment.setChargeId(chargeId);
                stripePayment.setTransactionStatus(KeyConstants.KEY_PAID);
                stripePayment = stripePaymentRepository.save(stripePayment);
                fitwiseQboEntityService.createAndSyncQboInvoice(invoiceManagement);
                /**
                 * Paying the instructor his share
                 * Not invoked for Free Offers.-> Charge is not created for free offers
                 */
                if (chargeId != null) {
                    payToInstructor(stripePayment);
                }
                // Logging in subscription tracker table
                StripeSubscriptionChangesTracker tracker = stripeSubscriptionChangesTrackerRepository.findTop1BySubscriptionIdOrderByModifiedDateDesc(subscriptionId);
                if (tracker != null) {
                    StripeSubscriptionChangesTracker stripeSubscriptionChangesTracker = new StripeSubscriptionChangesTracker();
                    stripeSubscriptionChangesTracker.setIsSubscriptionActive(true);
                    stripeSubscriptionChangesTracker.setOrderManagement(newOrderManagement);
                    stripeSubscriptionChangesTracker.setSubscriptionId(tracker.getSubscriptionId());
                    stripeSubscriptionChangesTrackerRepository.save(stripeSubscriptionChangesTracker);
                }
            }
            if(subscriptionType != null && newOrderManagement != null && invoiceManagement != null){
                if(subscriptionType.getName().equalsIgnoreCase(KeyConstants.KEY_PROGRAM)){
                    try{
                        //Sending mail to member
                        Programs programs = newOrderManagement.getProgram();
                        String subject = EmailConstants.PROGRAM_SUBSCRIPTION_RENEWAL_SUBJECT.replace(EmailConstants.PROGRAM_TITLE,  programs.getTitle() );
                        String mailBody = EmailConstants.PROGRAM_SUBSCRIPTION_RENEWAL_CONTENT.replace(EmailConstants.PROGRAM_TITLE, "<b>" + programs.getTitle() + "</b>");
                        String userName = fitwiseUtils.getUserFullName(newOrderManagement.getUser());
                        User instructor = programs.getOwner();
                        String memberProgram = EmailConstants.MEMBER_PROGRAM_LINK.replace(EmailConstants.LITERAL_APP_URL, dynamicLinkService.constructProgramLinkForMember(newOrderManagement.getProgram().getProgramId(),instructor));
                        mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, mailBody)
                                .replace(EmailConstants.EMAIL_SUPPORT_URL, memberProgram);
                        mailBody = emailContentUtil.replaceMemberAppUrl(mailBody);
                        try {
                            File file = invoicePDFGenerationService.generateInvoicePdf(newOrderManagement.getOrderId(), StringConstants.INVOICE + invoiceManagement.getInvoiceNumber());
                            List<String> fileList = Collections.singletonList(file.getAbsolutePath());
                            asyncMailer.sendHtmlMailWithAttachment(newOrderManagement.getUser().getEmail(),null, subject, mailBody, fileList);
                        } catch (Exception e) {
                            log.error("Invoice PDF generation failed for subscription renewal mail. Order id : " + newOrderManagement.getOrderId());
                            log.error(MessageConstants.MSG_ERR_EXCEPTION + e.getMessage());
                            asyncMailer.sendHtmlMail(newOrderManagement.getUser().getEmail(), subject, mailBody);
                        }
                        /*
                         * Stripe connect onboarding reminder mail
                         * */
                        boolean isOnboardingDetailsSubmitted = false;
                        StripeAccountAndUserMapping stripeAccountAndUserMapping = stripeAccountAndUserMappingRepository.findByUserUserId(instructor.getUserId());
                        if(stripeAccountAndUserMapping != null && stripeAccountAndUserMapping.getIsDetailsSubmitted() != null && stripeAccountAndUserMapping.getIsDetailsSubmitted().booleanValue()){
                            isOnboardingDetailsSubmitted = true;
                        }
                        boolean isOnBoardedViaPayPal = false;
                        UserAccountAndPayPalIdMapping userAccountAndPayPalIdMapping = userAccountAndPayPalIdMappingRepository.findByUserUserId(instructor.getUserId());
                        if (userAccountAndPayPalIdMapping != null && !userAccountAndPayPalIdMapping.getPayPalId().isEmpty()) {
                            isOnBoardedViaPayPal = true;
                        }
                        if (!isOnboardingDetailsSubmitted && !isOnBoardedViaPayPal) {
                            userName = fitwiseUtils.getUserFullName(instructor);
                            subject = EmailConstants.STRIPE_CONNECT_ONBOARD_REMINDER_SUBJECT;
                            String trainnrDashboard = EmailConstants.STRIPE_CONNECT_ONBOARD_DASHBOARD_LINK.replace(EmailConstants.LITERAL_APP_URL, generalProperties.getInstructorBaseUrl() + RestUrlConstants.APP_INSTRUCTOR_DASHBOARD);
                            mailBody = EmailConstants.STRIPE_CONNECT_ONBOARD_REMINDER_CONTENT;
                            mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
                                    .replace(EmailConstants.EMAIL_BODY, mailBody)
                                    .replace(EmailConstants.EMAIL_SUPPORT_URL, trainnrDashboard);
                            mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
                            asyncMailer.sendHtmlMail(instructor.getEmail(), subject, mailBody);
                        }
                    }catch(Exception e){
                        log.error(MessageConstants.MSG_ERR_EXCEPTION + e.getMessage());
                    }
                }else if(subscriptionType.getName().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_PACKAGE)){
                    try{
                        //Sending mail to member
                        SubscriptionPackage subscriptionPackage  = newOrderManagement.getSubscriptionPackage();
                        String subject = EmailConstants.PACKAGE_SUBSCRIPTION_RENEWAL_SUBJECT.replace(EmailConstants.PACKAGE_TITLE,  subscriptionPackage.getTitle() );
                        String mailBody = EmailConstants.PACKAGE_SUBSCRIPTION_RENEWAL_CONTENT.replace(EmailConstants.PACKAGE_TITLE, "<b>" + subscriptionPackage.getTitle() + "</b>");
                        String userName = fitwiseUtils.getUserFullName(newOrderManagement.getUser());
                        User instructor = subscriptionPackage.getOwner();
                        String memberPackage = EmailConstants.MEMBER_PACKAGE_LINK.replace(EmailConstants.LITERAL_APP_URL, dynamicLinkService.constructPackageLinkForMember(subscriptionPackage.getSubscriptionPackageId(),null,instructor));
                        mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, mailBody)
                                .replace(EmailConstants.EMAIL_SUPPORT_URL, memberPackage);
                        mailBody = emailContentUtil.replaceMemberAppUrl(mailBody);

                        try {
                            File file = invoicePDFGenerationService.generateInvoicePdf(newOrderManagement.getOrderId(), StringConstants.INVOICE + invoiceManagement.getInvoiceNumber());
                            List<String> fileList = Collections.singletonList(file.getAbsolutePath());

                            asyncMailer.sendHtmlMailWithAttachment(newOrderManagement.getUser().getEmail(),null, subject, mailBody, fileList);
                        } catch (Exception e) {
                            log.error("Invoice PDF generation failed for subscription renewal mail. Order id : " + newOrderManagement.getOrderId());
                            log.error(MessageConstants.MSG_ERR_EXCEPTION + e.getMessage());
                            asyncMailer.sendHtmlMail(newOrderManagement.getUser().getEmail(), subject, mailBody);
                        }

                        /*
                         * Stripe connect onboarding reminder mail
                         * */
                        boolean isOnboardingDetailsSubmitted = false;
                        StripeAccountAndUserMapping stripeAccountAndUserMapping = stripeAccountAndUserMappingRepository.findByUserUserId(instructor.getUserId());
                        if(stripeAccountAndUserMapping != null && stripeAccountAndUserMapping.getIsDetailsSubmitted() != null && stripeAccountAndUserMapping.getIsDetailsSubmitted()){
                            isOnboardingDetailsSubmitted = true;
                        }

                        boolean isOnBoardedViaPayPal = false;
                        UserAccountAndPayPalIdMapping userAccountAndPayPalIdMapping = userAccountAndPayPalIdMappingRepository.findByUserUserId(instructor.getUserId());
                        if (userAccountAndPayPalIdMapping != null && !userAccountAndPayPalIdMapping.getPayPalId().isEmpty()) {
                            isOnBoardedViaPayPal = true;
                        }

                        if (!isOnboardingDetailsSubmitted && !isOnBoardedViaPayPal) {
                            userName = fitwiseUtils.getUserFullName(instructor);
                            subject = EmailConstants.STRIPE_CONNECT_ONBOARD_REMINDER_SUBJECT;
                            String trainnrDashboard = EmailConstants.STRIPE_CONNECT_ONBOARD_DASHBOARD_LINK.replace(EmailConstants.LITERAL_APP_URL, generalProperties.getInstructorBaseUrl() + RestUrlConstants.APP_INSTRUCTOR_DASHBOARD);
                            mailBody = EmailConstants.STRIPE_CONNECT_ONBOARD_REMINDER_CONTENT;
                            mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
                                    .replace(EmailConstants.EMAIL_BODY, mailBody)
                                    .replace(EmailConstants.EMAIL_SUPPORT_URL, trainnrDashboard);
                            mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
                            asyncMailer.sendHtmlMail(instructor.getEmail(), subject, mailBody);
                        }
                    }catch(Exception exception){
                        log.error(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
                    }
                }
            }
        } else if (stripeSubscriptionAndUserTierMappingRepository.existsByStripeSubscriptionId(subscriptionId)) {
            stripeTierService.renewTierSubscription(subscriptionId, invoiceId, chargeId);
        }
    }

    /**
     * Method used to create Order
     *
     * @param program       - Program Object
     * @param paymentVendor - String that implies whether the payment is from Authorize.net or Apple
     * @param isARB         - Boolean that intimates whether auto-subscription or not
     * @return
     */
    @Transactional
    public OrderManagement createProgramOrder(User user, Programs program, boolean isARB, String
            paymentVendor, PlatformType platformType, String existingOrderId, OfferCodeDetail offerCodeDetail) {

        OrderManagement orderManagement;
        String orderNumber;

        if (existingOrderId != null && !existingOrderId.isEmpty()) {
            orderNumber = existingOrderId;
        } else {
            /*
             * The below piece of code generates a unique order number
             */
            orderNumber = OrderNumberGenerator.generateOrderNumber();
            logger.info("Order id =================> {}", orderNumber);
        }
        // Constructing the order management object
        SubscriptionType subscriptionType = subscriptionTypesRepo.findByNameIgnoreCase(KeyConstants.KEY_PROGRAM);

        orderManagement = new OrderManagement();
        orderManagement.setOrderId(orderNumber);
        orderManagement.setModeOfPayment(KeyConstants.KEY_STRIPE);
        orderManagement.setIsAutoRenewable(isARB);
        orderManagement.setProgram(program);
        orderManagement.setSubscriptionType(subscriptionType);
        orderManagement.setUser(user);
        orderManagement.setSubscribedViaPlatform(platformType);

        // Saving the order management object in repository
        OrderManagement savedOrderManagement = orderManagementRepo.save(orderManagement);

        if (offerCodeDetail != null) {
            OfferCodeDetailAndOrderMapping offerCodeDetailAndOrderMapping = new OfferCodeDetailAndOrderMapping();
            offerCodeDetailAndOrderMapping.setOfferCodeDetail(offerCodeDetail);
            offerCodeDetailAndOrderMapping.setOrderManagement(savedOrderManagement);
            offerCodeDetailAndOrderMappingRepository.save(offerCodeDetailAndOrderMapping);
        }

        // Creating invoice for the order
        if (existingOrderId == null || existingOrderId.isEmpty()) {
            // Create invoice if only the order is new
            createInvoice(savedOrderManagement, existingOrderId);
        }

        return savedOrderManagement;
    }


    /**
     * ,Method used to create invoice number
     *
     * @param orderManagement
     * @return
     */
    @Transactional
    public InvoiceManagement createInvoice(OrderManagement orderManagement, String exstOrdId) {
        InvoiceManagement invoiceManagement;
        try {
            if (exstOrdId != null && !exstOrdId.isEmpty()) {
                invoiceManagement = invoiceManagementRepository.findByOrderManagementOrderId(exstOrdId);
            } else {
                invoiceManagement = new InvoiceManagement();
                invoiceManagement.setInvoiceNumber(OrderNumberGenerator.generateInvoiceNumber());
            }
            invoiceManagement.setOrderManagement(orderManagement);
            invoiceManagementRepository.save(invoiceManagement);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ApplicationException(Constants.ERROR_STATUS, "Error in createInvoice method", e.getMessage());
        }
        return invoiceManagement;
    }


    @Transactional
    public void overrideAlreadySubscribedProgramData(User user, ProgramSubscription subscribedProgram, PlatformType
            platformType, boolean isAutoRenewal, OrderManagement orderManagement) {

        // If only payment status is success, entry will be added in the Subscription table
        SubscriptionStatus newSubscriptionStatus = subscriptionStatusRepo.
                findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_PAID);
        SubscriptionPlan subscriptionPlan = subscriptionPlansRepo.findByDuration(subscribedProgram.getProgram().getDuration().getDuration());

        SubscriptionType subscriptionType = subscriptionTypesRepo.findByNameIgnoreCase(KeyConstants.KEY_PROGRAM);

        subscribedProgram.setSubscriptionPlan(subscriptionPlan);
        subscribedProgram.setSubscribedDate(new Date());
        subscribedProgram.setSubscriptionStatus(newSubscriptionStatus);
        subscribedProgram.setSubscribedViaPlatform(platformType);

        ProgramSubscription saveProgramSubscription = programSubscriptionRepo.save(subscribedProgram);

        //Subscription related notifications
        notificationTriggerService.invokeSubscriptionPushNotification(saveProgramSubscription);

        //Saving revenueAudit table to store all tax details
        ProgramSubscriptionPaymentHistory programSubscriptionPaymentHistory = new ProgramSubscriptionPaymentHistory();
        programSubscriptionPaymentHistory.setOrderManagement(orderManagement);
        subscriptionPaymentHistoryRepository.save(programSubscriptionPaymentHistory);

        SubscriptionAudit subscriptionAudit = new SubscriptionAudit();
        subscriptionAudit.setUser(user);
        subscriptionAudit.setSubscriptionType(subscriptionType);
        subscriptionAudit.setProgramSubscription(saveProgramSubscription);
        subscriptionAudit.setSubscriptionPlan(subscriptionPlan);
        subscriptionAudit.setSubscriptionStatus(newSubscriptionStatus);
        subscriptionAudit.setSubscribedViaPlatform(platformType);
        subscriptionAudit.setSubscriptionDate(new Date());
        subscriptionAudit.setAutoRenewal(isAutoRenewal);
        subscriptionAudit.setProgramSubscriptionPaymentHistory(programSubscriptionPaymentHistory);

        SubscriptionAudit subscriptionAuditOfProgram = subscriptionAuditRepo.findBySubscriptionTypeNameAndProgramSubscriptionProgramSubscriptionIdOrderBySubscriptionDateDesc(KeyConstants.KEY_PROGRAM, subscribedProgram.getProgramSubscriptionId()).get(0);


        if (subscriptionAuditOfProgram.getRenewalStatus().equalsIgnoreCase(KeyConstants.KEY_NEW) && subscriptionAuditOfProgram.getSubscriptionStatus() != null &&
                subscriptionAuditOfProgram.getSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_TRIAL)) {
            // If renewal status is new and subscription status is trial, then next paid subscription will be set to new
            subscriptionAudit.setRenewalStatus(KeyConstants.KEY_NEW);

        } else if (subscriptionAuditOfProgram.getRenewalStatus().equalsIgnoreCase(KeyConstants.KEY_NEW)) {
            // Already the renewal status is new! So setting it has renew on the second time
            subscriptionAudit.setRenewalStatus(KeyConstants.KEY_RENEWAL);

        } else if (subscriptionAuditOfProgram.getRenewalStatus().equalsIgnoreCase(KeyConstants.KEY_RENEWAL)) {
            // Already the renewal status is renew! So will be set as renew in next coming times
            subscriptionAudit.setRenewalStatus(KeyConstants.KEY_RENEWAL);
        }
        subscriptionAuditRepo.save(subscriptionAudit);
    }


    @Transactional
    public void addNewlySubscribedProgramData(User user, Programs program, PlatformType platformType,
                                              boolean isPaymentSuccess, boolean isAutoRenewal, OrderManagement orderManagement) {

        // If only payment status is success, entry will be added in the Subscription table
        if (isPaymentSuccess) {
            SubscriptionStatus newSubscriptionStatus = subscriptionStatusRepo.
                    findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_PAID);

            SubscriptionPlan subscriptionPlan = subscriptionPlansRepo.findByDuration(program.getDuration().getDuration());
            SubscriptionType subscriptionType = subscriptionTypesRepo.findByNameIgnoreCase(KeyConstants.KEY_PROGRAM);

            ProgramSubscription programSubscription = new ProgramSubscription();
            programSubscription.setProgram(program);
            programSubscription.setSubscriptionPlan(subscriptionPlan);
            programSubscription.setSubscribedViaPlatform(platformType);
            programSubscription.setAutoRenewal(isAutoRenewal);
            programSubscription.setSubscribedDate(new Date());
            programSubscription.setUser(user);
            programSubscription.setSubscriptionStatus(newSubscriptionStatus);
            programSubscriptionRepo.save(programSubscription);

            //Subscription related notifications
            notificationTriggerService.invokeSubscriptionPushNotification(programSubscription);

            //Saving revenueAudit table to store all tax details
            ProgramSubscriptionPaymentHistory programSubscriptionPaymentHistory = new ProgramSubscriptionPaymentHistory();
            programSubscriptionPaymentHistory.setOrderManagement(orderManagement);
            subscriptionPaymentHistoryRepository.save(programSubscriptionPaymentHistory);

            /*
             * Auditing the subscription
             */
            SubscriptionAudit subscriptionAudit = new SubscriptionAudit();
            subscriptionAudit.setUser(user);
            subscriptionAudit.setSubscriptionType(subscriptionType);
            subscriptionAudit.setProgramSubscription(programSubscription);
            subscriptionAudit.setSubscriptionPlan(subscriptionPlan);
            subscriptionAudit.setSubscriptionStatus(newSubscriptionStatus);
            subscriptionAudit.setSubscribedViaPlatform(platformType);
            subscriptionAudit.setSubscriptionDate(new Date());
            subscriptionAudit.setAutoRenewal(isAutoRenewal);
            subscriptionAudit.setRenewalStatus(KeyConstants.KEY_NEW);
            subscriptionAudit.setProgramSubscriptionPaymentHistory(programSubscriptionPaymentHistory);
            subscriptionAuditRepo.save(subscriptionAudit);
        }
    }

    /**
     * Used to process refund for a transaction
     *
     * @param stripeRefundRequestView
     * @return
     */
    @Transactional
    public ResponseModel processProgramRefund(StripeRefundRequestView stripeRefundRequestView) {
        log.info("Process program refund starts.");
        long apiStartTimeMillis = new Date().getTime();
        Stripe.apiKey = stripeProperties.getApiKey();

        Map<String, Object> params = new HashMap<>();
        params.put(StripeConstants.STRIPE_PROP_CHARGE, stripeRefundRequestView.getChargeId());
        params.put(KeyConstants.KEY_AMOUNT, (int) (stripeRefundRequestView.getRefundableAmount() * 100)); //100 denotes paise/cents.

        long profilingEndTimeMillis = 0;
        try {
            Refund refund = Refund.create(params);
            if (refund != null) {

                StripePayment stripePayment = stripePaymentRepository.findTop1ByChargeId(stripeRefundRequestView.getChargeId());
                StripePayment refundedStripePayment = new StripePayment();
                refundedStripePayment.setOrderManagement(stripePayment.getOrderManagement());
                refundedStripePayment.setInvoiceId(stripePayment.getInvoiceId());
                refundedStripePayment.setChargeId(stripePayment.getChargeId());
                refundedStripePayment.setInvoiceManagement(stripePayment.getInvoiceManagement());
                refundedStripePayment.setTransactionStatus(KeyConstants.KEY_REFUND);
                refundedStripePayment.setSubscriptionId(stripePayment.getSubscriptionId());
                refundedStripePayment.setAmountPaid(stripePayment.getAmountPaid());
                refundedStripePayment.setAmountRefunded(stripeRefundRequestView.getRefundableAmount());
                refundedStripePayment.setRefundTransactionId(refund.getId());
                refundedStripePayment.setIsRefundUnderProcessing(false);
                stripePaymentRepository.save(refundedStripePayment);
                log.info("Query: get and save stripe payment for refund : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
                profilingEndTimeMillis = new Date().getTime();

                User user = stripePayment.getOrderManagement().getUser();
                User instructor = stripePayment.getOrderManagement().getProgram().getOwner();
                Programs program = stripePayment.getOrderManagement().getProgram();

                // Changing the status of the subscription to REFUND
                ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), program.getProgramId());
                SubscriptionStatus subscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_REFUND);
                programSubscription.setSubscriptionStatus(subscriptionStatus);
                programSubscriptionRepo.save(programSubscription);
                log.info("Query: get program, subscription status and save program subscription : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                profilingEndTimeMillis = new Date().getTime();

                SubscriptionAudit oldSubscriptionAudit = subscriptionAuditRepo.
                        findTop1ByUserUserIdAndSubscriptionTypeNameAndProgramSubscriptionProgramProgramIdOrderByCreatedDateDesc(user.getUserId(), KeyConstants.KEY_PROGRAM, program.getProgramId());
                log.info("Query to get subscription audit : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                profilingEndTimeMillis = new Date().getTime();

                //Saving revenueAudit table to store all tax details
                ProgramSubscriptionPaymentHistory programSubscriptionPaymentHistory = new ProgramSubscriptionPaymentHistory();
                programSubscriptionPaymentHistory.setOrderManagement(stripePayment.getOrderManagement());
                subscriptionPaymentHistoryRepository.save(programSubscriptionPaymentHistory);
                log.info("Query to save subscription payment history : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                profilingEndTimeMillis = new Date().getTime();

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
                log.info("Query to save subscription audit : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                profilingEndTimeMillis = new Date().getTime();
                //QBO REFUND
                fitwiseQboEntityService.createAndSyncStripeRefund(stripePayment);
                log.info("QBO: create and sync stripe refund : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                profilingEndTimeMillis = new Date().getTime();

                //Reverse transfer from Instructor connected account to Fitwise Stripe account
                try {
                    InstructorPayment instructorPayment = instructorPaymentRepository.findByOrderManagement(stripePayment.getOrderManagement());
                    if (instructorPayment != null) {
                        double instructorShareForRefund = qboService.getRefundAmount(instructorPayment);
                        StripeAccountAndUserMapping stripeAccountAndUserMapping = stripeAccountAndUserMappingRepository.findByUserUserId(instructor.getUserId());


                        /**
                         * Checking and Getting the available balance from the user connected account
                         */
                        Stripe.apiKey = stripeProperties.getApiKey();
                        RequestOptions requestOptions = RequestOptions.builder().setStripeAccount(stripeAccountAndUserMapping.getStripeAccountId()).build();
                        Long connectedAccountBalanceAmount = 0L;
                        // Stripe Get Balance API
                        Balance balance = Balance.retrieve(requestOptions);
                        for (Balance.Money balances : balance.getAvailable()) {
                            if (balances.getCurrency().equalsIgnoreCase(KeyConstants.KEY_CURRENCY_USD)) {
                                connectedAccountBalanceAmount += balances.getAmount();
                            }
                        }

                        //pendingReversalAmount represents the amount available to be reversed in this transfer
                        long pendingReversalAmount = 0;
                        try {
                            Transfer transfer = Transfer.retrieve(instructorPayment.getStripeTransferId());
                            pendingReversalAmount = transfer.getAmount() - transfer.getAmountReversed();
                        } catch (StripeException e) {
                            e.printStackTrace();
                        }

                        // Since the available balance amount is given by 1000's, dividing it by 100.
                        double convertedConnectedAccountBalanceAmount = (double) connectedAccountBalanceAmount / 100;

                        //Reverse transfer is possible only if pendingReversalAmount is available
                        if (pendingReversalAmount > 0) {
                            if (convertedConnectedAccountBalanceAmount > 0) {
                                //pendingReversalAmount can be equal to or less than instructorShareForRefund, which the amount transferred to instructor
                                //if pendingReversalAmount is equal to instructorShareForRefund,  we attempt to reverse whole instructor share
                                if (pendingReversalAmount == instructorShareForRefund) {
                                    if (convertedConnectedAccountBalanceAmount < instructorShareForRefund) {
                                        // If connected account has some available balance left over, Reverse transferring that available balance amount
                                        TransferReversal transferReversal = stripeConnectService.reverseTransfer(instructorPayment.getStripeTransferId(), convertedConnectedAccountBalanceAmount);
                                        if (transferReversal != null) {
                                            //QBO: DEBIT CAPTURE
                                            fitwiseQboEntityService.createAndSyncPaymentInsufficientBalance(instructorPayment.getOrderManagement(), convertedConnectedAccountBalanceAmount);
                                            // If Transfer succeeded, adding the balance amount to credit
                                            double balanceAmount = instructorShareForRefund - convertedConnectedAccountBalanceAmount;
                                            stripeConnectService.addInstructorCredit(instructorPayment, instructor, balanceAmount, KeyConstants.KEY_CURRENCY_US_DOLLAR);
                                        } else {
                                            // If Transfer fails, adding the total amount to Credit
                                            stripeConnectService.addInstructorCredit(instructorPayment, instructor, instructorShareForRefund, KeyConstants.KEY_CURRENCY_US_DOLLAR);
                                        }
                                    } else if (convertedConnectedAccountBalanceAmount >= instructorShareForRefund) {
                                        // If connected account has more available balance than refund amount, Reverse transferring the refund amount
                                        TransferReversal transferReversal = stripeConnectService.reverseTransfer(instructorPayment.getStripeTransferId(), instructorShareForRefund);
                                        if (transferReversal == null) {
                                            // If the reverse transfer fails, adding the total amount to Instructor Credit
                                            stripeConnectService.addInstructorCredit(instructorPayment, instructor, instructorShareForRefund, KeyConstants.KEY_CURRENCY_US_DOLLAR);
                                        } else {
                                            //QBO: DEBIT CAPTURE
                                            fitwiseQboEntityService.createAndSyncPaymentInsufficientBalance(instructorPayment.getOrderManagement(), instructorShareForRefund);
                                        }
                                    }
                                } else if (pendingReversalAmount < instructorShareForRefund) {
                                    //if pendingReversalAmount is less than instructorShareForRefund,  we attempt to reverse the pendingReversalAmount
                                    if (convertedConnectedAccountBalanceAmount < pendingReversalAmount) {
                                        // If connected account has some available balance left over, Reverse transferring that available balance amount
                                        TransferReversal transferReversal = stripeConnectService.reverseTransfer(instructorPayment.getStripeTransferId(), convertedConnectedAccountBalanceAmount);
                                        if (transferReversal != null) {
                                            //QBO DEBIT CAPTURE
                                            fitwiseQboEntityService.createAndSyncPaymentInsufficientBalance(instructorPayment.getOrderManagement(), convertedConnectedAccountBalanceAmount);
                                            // If Transfer succeeded, adding the balance amount to credit
                                            double balanceAmount = instructorShareForRefund - convertedConnectedAccountBalanceAmount;
                                            stripeConnectService.addInstructorCredit(instructorPayment, instructor, balanceAmount, KeyConstants.KEY_CURRENCY_US_DOLLAR);
                                        } else {
                                            // If Transfer fails, adding the total amount to Credit
                                            stripeConnectService.addInstructorCredit(instructorPayment, instructor, instructorShareForRefund, KeyConstants.KEY_CURRENCY_US_DOLLAR);
                                        }
                                    } else if (convertedConnectedAccountBalanceAmount >= pendingReversalAmount) {
                                        // If connected account has more available balance than refund amount, Reverse transferring the refund amount
                                        TransferReversal transferReversal = stripeConnectService.reverseTransfer(instructorPayment.getStripeTransferId(), pendingReversalAmount);
                                        if (transferReversal != null) {
                                            //QBO: DEBIT CAPTURE
                                            fitwiseQboEntityService.createAndSyncPaymentInsufficientBalance(instructorPayment.getOrderManagement(), Double.parseDouble(String.valueOf(pendingReversalAmount)));
                                            // If Transfer succeeded, adding the balance amount to credit
                                            double balanceAmount = instructorShareForRefund - pendingReversalAmount;
                                            stripeConnectService.addInstructorCredit(instructorPayment, instructor, balanceAmount, KeyConstants.KEY_CURRENCY_US_DOLLAR);
                                        } else {
                                            // If the reverse transfer fails, adding the total amount to Instructor Credit
                                            stripeConnectService.addInstructorCredit(instructorPayment, instructor, instructorShareForRefund, KeyConstants.KEY_CURRENCY_US_DOLLAR);
                                        }
                                    }
                                }
                            } else {
                                // Connected balance has zero available balance. Adding the total amount to Instructor Credit
                                stripeConnectService.addInstructorCredit(instructorPayment, instructor, instructorShareForRefund, KeyConstants.KEY_CURRENCY_US_DOLLAR);
                            }
                        } else {
                            // Total transfer amount has been reversed already. Adding the total amount to Instructor Credit
                            stripeConnectService.addInstructorCredit(instructorPayment, instructor, instructorShareForRefund, KeyConstants.KEY_CURRENCY_US_DOLLAR);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Exception while Reversing transfer made to instructor {}", e.getMessage());
                    e.printStackTrace();
                }
                log.info("Reverse transfer from Instructor connected account to Fitwise Stripe account : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                profilingEndTimeMillis = new Date().getTime();

                DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
                String roundedPrice = decimalFormat.format(stripeRefundRequestView.getRefundableAmount());

                //Sending mail to member for refund
                String subject = EmailConstants.REFUND_INITIATED_PROGRAM_SUBJECT.replace(EmailConstants.LITERAL_PROGRAM_NAME, "'" + program.getTitle() + "'");
                String supportLink = EmailConstants.TRAINNR_SUPPORT_LINK.replace(EmailConstants.EMAIL_EMAIL_ADDRESS, user.getEmail());
                String mailBody = EmailConstants.REFUND_INITIATED_PROGRAM_CONTENT
                        .replace(EmailConstants.LITERAL_PROGRAM_NAME, "<b>" + program.getTitle() + "</b>")
                        .replace("#REFUND_AMOUNT#", roundedPrice);
                String userName = fitwiseUtils.getUserFullName(user);
                mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON
                        .replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
                        .replace(EmailConstants.EMAIL_BODY, mailBody)
                        .replace(EmailConstants.EMAIL_SUPPORT_URL, supportLink);
                mailBody = emailContentUtil.replaceMemberAppUrl(mailBody);
                asyncMailer.sendHtmlMail(user.getEmail(), subject, mailBody);
                log.info("Mail sent : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            }
        } catch (StripeException e) {
            e.printStackTrace();
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_FAILED_TO_REFUND, null);
        }
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Process program refund ends.");

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_TRANSACTION_REFUND_SUCCESS, null);
    }

    /**
     * Method used to log payout events into Trainnr DB
     *
     * @param payout
     * @param eventType
     */
    @Transactional
    public void logPayoutEvent(Payout payout, String eventType) {
        StripePayout stripePayout = new StripePayout();
        stripePayout.setPayoutId(payout.getId());
        stripePayout.setArrivalDateTimeStamp(payout.getArrivalDate());
        stripePayout.setBalanceTransactionId(payout.getBalanceTransaction());
        stripePayout.setAmount(Double.valueOf(payout.getAmount()));
        stripePayout.setCurrency(payout.getCurrency());
        stripePayout.setDestination(payout.getDestination());
        stripePayout.setFailureCode(payout.getFailureCode());
        stripePayout.setFailureMessage(payout.getFailureMessage());
        stripePayout.setStatus(payout.getStatus());
        stripePayout.setType(payout.getType());
        stripePayout.setEventType(eventType);
        stripePayoutRepository.save(stripePayout);
        try {
            if (eventType.equalsIgnoreCase(KeyConstants.KEY_PAYOUT_PAID)) {
                Stripe.apiKey = stripeProperties.getApiKey();
                Map<String, Object> params = new HashMap<>();
                params.put("payout", payout.getId());
                params.put("limit", 100);
                BalanceTransactionCollection balanceTransactionCollection = BalanceTransaction.list(params);
                for (BalanceTransaction balanceTransaction : balanceTransactionCollection.getData()) {
                    if (balanceTransaction.getType().equalsIgnoreCase(StripeConstants.STRIPE_PROP_CHARGE)) {
                        StripePayment stripePayment = stripePaymentRepository.findTop1ByChargeId(balanceTransaction.getSource());
                        if (stripePayment == null) {
                            continue;
                        }
                        fitwiseQboEntityService.createAndSyncStripePayment(stripePayment);
                        for (BalanceTransaction.Fee fee : balanceTransaction.getFeeDetails()) {
                            StripePaymentFeeDetails stripePaymentFeeDetails = stripePaymentFeeDetailsRepository.findByStripePaymentAndType(stripePayment, fee.getType());
                            if (stripePaymentFeeDetails == null) {
                                stripePaymentFeeDetails = new StripePaymentFeeDetails();
                                stripePaymentFeeDetails.setStripePayment(stripePayment);
                            }
                            stripePaymentFeeDetails.setAmount(fee.getAmount());
                            stripePaymentFeeDetails.setCurrency(fee.getCurrency());
                            stripePaymentFeeDetails.setDescription(fee.getDescription());
                            stripePaymentFeeDetails.setStripePayout(stripePayout);
                            stripePaymentFeeDetailsRepository.save(stripePaymentFeeDetails);
                        }
                    }
                }
            }

            BalanceTransaction balanceTransaction = BalanceTransaction.retrieve(payout.getBalanceTransaction());
            if (balanceTransaction != null) {
                StripeBalanceTransaction stripeBalanceTransaction = new StripeBalanceTransaction();
                stripeBalanceTransaction.setBalanceTransactionId(balanceTransaction.getId());
                stripeBalanceTransaction.setAmount(Double.valueOf(balanceTransaction.getAmount()));
                stripeBalanceTransaction.setAvailableOn(balanceTransaction.getAvailableOn());
                stripeBalanceTransaction.setCreatedOn(balanceTransaction.getCreated());
                stripeBalanceTransaction.setCurrency(balanceTransaction.getCurrency());
                stripeBalanceTransaction.setFee(Double.valueOf(balanceTransaction.getFee())); // Total amount
                stripeBalanceTransaction.setNetAmount(Double.valueOf(balanceTransaction.getNet())); // Amount after deduction
                stripeBalanceTransaction.setStatus(balanceTransaction.getStatus());
                stripeBalanceTransaction.setSource(balanceTransaction.getSource());
                stripeBalanceTransaction.setType(balanceTransaction.getType());
                stripeBalanceTransaction.setStripePayout(stripePayout);
                stripeBalanceTransactionRepository.save(stripeBalanceTransaction);
                for (BalanceTransaction.Fee fee : balanceTransaction.getFeeDetails()) {
                    StripeBalanceTransactionFeeDetailsMapping stripeBalanceTransactionFeeDetailsMapping = new StripeBalanceTransactionFeeDetailsMapping();
                    stripeBalanceTransactionFeeDetailsMapping.setAmount(Double.valueOf(fee.getAmount()));
                    stripeBalanceTransactionFeeDetailsMapping.setCurrency(fee.getCurrency());
                    stripeBalanceTransactionFeeDetailsMapping.setDescription(fee.getDescription());
                    stripeBalanceTransactionFeeDetailsMapping.setType(fee.getType());
                    stripeBalanceTransactionFeeDetailsMapping.setStripeBalanceTransaction(stripeBalanceTransaction);
                    stripeBalanceTransactionFeeDetailsMappingRepository.save(stripeBalanceTransactionFeeDetailsMapping);
                }
            }


        } catch (StripeException e) {
            e.printStackTrace();
        }
    }


    /**
     * Method used to process payment to instructor
     */
    @Transactional
    public void payToInstructor(StripePayment stripePayment) {
        OrderManagement orderManagement = stripePayment.getOrderManagement();
        User instructor;
        if (KeyConstants.KEY_PROGRAM.equalsIgnoreCase(orderManagement.getSubscriptionType().getName())) {
            instructor = orderManagement.getProgram().getOwner();
        } else {
            instructor = orderManagement.getSubscriptionPackage().getOwner();
        }
        InstructorPayment instructorPayment = instructorPaymentRepository.findByOrderManagement(stripePayment.getOrderManagement());

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
                    instructorPayment.setIsTransferFailed(false);
                    instructorPayment.setTransferDate(new Date());
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

                            TransferReversal transferReversal = stripeConnectService.reverseTransfer(transfer.getId(), instructorShareAmount);
                            if (transferReversal != null) {
                                stripeConnectService.reduceCreditAmount(instructorPayment, instructorPaymentCredit, instructorShareAmount);
                            }
                        } else {
                            // If Credit amount is lesser than share amount, reversing the transfer from the credit amount
                            TransferReversal transferReversal = stripeConnectService.reverseTransfer(transfer.getId(), instructorCreditAmount);
                            if (transferReversal != null) {
                                stripeConnectService.reduceCreditAmount(instructorPayment, instructorPaymentCredit, instructorCreditAmount);
                            }
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
                logger.error(MessageConstants.MSG_ERR_EXCEPTION, e.getMessage());
            }

        }
    }

    /**
     * Method used to process payment to fitwise
     */
    @Transactional
    public void payToFitwise(StripePayment stripePayment) {
        OrderManagement orderManagement = stripePayment.getOrderManagement();
        User instructor;
        if (KeyConstants.KEY_TIER.equalsIgnoreCase(orderManagement.getSubscriptionType().getName())) {
            instructor = orderManagement.getProgram().getOwner();
        } else {
            instructor = orderManagement.getSubscriptionPackage().getOwner();
        }
        InstructorPayment instructorPayment = instructorPaymentRepository.findByOrderManagement(stripePayment.getOrderManagement());

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
                    instructorPayment.setIsTransferFailed(false);
                    instructorPayment.setTransferDate(new Date());
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

                            TransferReversal transferReversal = stripeConnectService.reverseTransfer(transfer.getId(), instructorShareAmount);
                            if (transferReversal != null) {
                                stripeConnectService.reduceCreditAmount(instructorPayment, instructorPaymentCredit, instructorShareAmount);
                            }
                        } else {
                            // If Credit amount is lesser than share amount, reversing the transfer from the credit amount
                            TransferReversal transferReversal = stripeConnectService.reverseTransfer(transfer.getId(), instructorCreditAmount);
                            if (transferReversal != null) {
                                stripeConnectService.reduceCreditAmount(instructorPayment, instructorPaymentCredit, instructorCreditAmount);
                            }
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
                logger.error(MessageConstants.MSG_ERR_EXCEPTION, e.getMessage());
            }

        }
    }


    /**
     * @return
     */
    public List<Programs> getPendingPublishedPrograms() {
        Specification<Programs> programStatusSpec = ProgramSpecifications.getProgramByStatus(KeyConstants.KEY_PUBLISH);
        Specification<Programs> finalSpec = programStatusSpec;
        List<StripeProductAndProgramMapping> productAndProgramMappingList = stripeProductAndProgramMappingRepository.findAll();
        if (!productAndProgramMappingList.isEmpty()) {
            List<Long> uploadedProgramId = productAndProgramMappingList.stream().map(productAndProgramMapping -> productAndProgramMapping.getProgram().getProgramId()).collect(Collectors.toList());
            Specification<Programs> notInIdListSpec = ProgramSpecifications.getProgramsNotInIdList(uploadedProgramId);
            finalSpec = programStatusSpec.and(notInIdListSpec);
        }
        return programRepository.findAll(finalSpec);
    }

    /**
     * Method to Upload all UploadPendingPrograms to stripe
     */
    public Map<String, Object> getUploadPendingPrograms() {
        List<Programs> programs = getPendingPublishedPrograms();
        if (programs.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        List<Long> remainingProgramId = programs.stream().map(Programs::getProgramId).collect(Collectors.toList());
        Map<String, Object> respMap = new HashMap<>();
        respMap.put(KeyConstants.KEY_PROGRAM_COUNT, programs.size());
        respMap.put(KeyConstants.KEY_PROGRAMS, remainingProgramId);
        return respMap;
    }

    /**
     * Method to Upload all programs to stripe
     */
    public Map<String, Integer> uploadPublishedPrograms() {
        List<Programs> programs = getPendingPublishedPrograms();
        if (programs.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        int success = 0;
        for (Programs program : programs) {
            try {
                createProgramInStripe(program.getProgramId());
                success++;
            } catch (StripeException e) {
                log.error("Exception occurred while uploading to stripe. Program ID : " + program.getProgramId());
                e.printStackTrace();
            }
        }
        Map<String, Integer> respMap = new HashMap<>();
        respMap.put(KeyConstants.KEY_TOTAL_COUNT, programs.size());
        respMap.put(KeyConstants.KEY_SUCCESS, success);
        return respMap;
    }

    /**
     * Create Coupon for program on stripe
     *
     * @param offerCodeDetail
     * @param price
     */
    public void createCoupon(OfferCodeDetail offerCodeDetail, Double price) {

        if (offerCodeDetail == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_OFFER_NOT_FOUND_STRIPE_COUPON_FAILED, null);
        }

        StripeCouponAndOfferCodeMapping stripeCouponAndOfferCodeMapping = stripeCouponAndOfferCodeMappingRepository.findByOfferCodeDetail(offerCodeDetail);
        if (stripeCouponAndOfferCodeMapping != null) {
            throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_STRIPE_COUPON_EXISTS_FOR_OFFER, null);
        }

        Stripe.apiKey = stripeProperties.getApiKey();

        if (offerCodeDetail.getOfferDuration() == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_OFFER_DURATION_MISSING, null);
        }
        long offerValidityMonths = offerCodeDetail.getOfferDuration().getDurationInMonths();

        if (offerCodeDetail.getOfferEndingDate() == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_OFFER_END_DATE_MISSING, null);
        }
        long endTimeUnixTimeStamp = offerCodeDetail.getOfferEndingDate().getTime() / 1000;

        CouponCreateParams couponParams;
        if (DiscountsConstants.MODE_FREE.equalsIgnoreCase(offerCodeDetail.getOfferMode())) {
            couponParams = CouponCreateParams.builder()
                    .setCurrency("usd")
                    .setDuration(CouponCreateParams.Duration.REPEATING)
                    .setDurationInMonths(offerValidityMonths)
                    .setPercentOff(new BigDecimal("100"))
                    .setRedeemBy(endTimeUnixTimeStamp)
                    .build();
        } else {
            if (price == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_STRIPE_COUPON_PRICE_NOT_FOUND, null);
            }
            double priceDouble = price * 100;
            long priceInLong = (long) priceDouble;

            if (offerCodeDetail.getOfferPrice() == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_OFFER_PRICE_NOT_FOUND, null);
            }
            double offerPrice = offerCodeDetail.getOfferPrice().getPrice() * 100;
            long offerPriceInLong = (long) offerPrice;

            long discountedPrice = priceInLong - offerPriceInLong;

            couponParams = CouponCreateParams.builder()
                    .setCurrency("usd")
                    .setDuration(CouponCreateParams.Duration.REPEATING)
                    .setDurationInMonths(offerValidityMonths)
                    .setAmountOff(discountedPrice)
                    .setRedeemBy(endTimeUnixTimeStamp)
                    .build();
        }

        try {
            Coupon coupon = Coupon.create(couponParams);

            stripeCouponAndOfferCodeMapping = new StripeCouponAndOfferCodeMapping();
            stripeCouponAndOfferCodeMapping.setOfferCodeDetail(offerCodeDetail);
            stripeCouponAndOfferCodeMapping.setStripeCouponId(coupon.getId());
            stripeCouponAndOfferCodeMappingRepository.save(stripeCouponAndOfferCodeMapping);

        } catch (StripeException e) {
            e.printStackTrace();
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_STRIPE_COUPON_CREATION_FAILED, null);
        }
    }


    /**
     * Used to check whether the payout schedule settings is in Manual/Automatic
     *
     * @return
     */
    public ResponseModel getPayoutSettings() {
        boolean isPayoutSettingsAutomatic = false;
        PayoutModeSettings payoutModeSettings = payoutModeSettingsRepository.findByIsManualFalse();
        if (null != payoutModeSettings) {
            isPayoutSettingsAutomatic = true;
        }
        Map<String, Object> settingsMap = new HashMap<>();
        settingsMap.put(KeyConstants.KEY_PAYOUT_SETTINGS_MODE, isPayoutSettingsAutomatic);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PAYOUT_ALREADY_DONE, settingsMap);
    }

    public ResponseModel savePayoutSettings(PayoutSettingsResponseView payoutSettingsResponseView) {
        PayoutModeSettings payoutModeSettings = payoutModeSettingsRepository.findTop1ByPayoutSettingsId(1L);

        if (payoutSettingsResponseView.getIsAutomatic()) {
            // If top-up is created, then payout settings mode is not Automatic. For top-ups to be created, Payout mode schedule should be Manual
            if (isTopUpCreated()) {
                /*
                 * Sending mail notification to super admins
                 */
                asyncMailer.sendAttachmentMail(generalProperties.getSuperAdminEmailAddresses(), null, "Change Stripe Payout Schedule to Automatic", MessageConstants.MSG_WARNING_PAYOUT_MODE_MANUAL, null);
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_WARNING_PAYOUT_MODE_MANUAL, null);
            }
            payoutModeSettings.setManual(false);
        } else {
            // Notify that the payout settings has been changed to Manual
            payoutModeSettings.setManual(true);
        }
        payoutModeSettingsRepository.save(payoutModeSettings);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PAYOUT_MODE_SETTINGS_SAVED, null);
    }

    public boolean isTopUpCreated() {
        Stripe.apiKey = stripeProperties.getApiKey();
        Map<String, Object> params = new HashMap<>();
        params.put(KeyConstants.KEY_AMOUNT, 100);
        params.put(StripeConstants.STRIPE_PROP_CURRENCY, "usd");
        params.put(StringConstants.JSON_PROPERTY_KEY_DESCRIPTION, "Payout Mode check");
        params.put("statement_descriptor", "PayoutModecheck");
        try {
            /**
             * Topping up Fitwise stripe account from Fitwise bank account
             */
            Topup.create(params);
        } catch (StripeException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            if (e.getMessage().contains(StripeConstants.STRIPE_TOP_UP_MANUAL_MODE_PROMPT)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Used to create a product in Stripe under Billing after creating SubscriptionPackage
     *
     * @param subscriptionPackageId
     * @return
     * @throws StripeException
     */
    @Transactional
    public ResponseModel createPackageInStripe(Long subscriptionPackageId) throws StripeException {

        Product product = null;
        SubscriptionPackage subscriptionPackage = null;
        try {
            subscriptionPackage = validationService.validateSubscriptionPackageId(subscriptionPackageId);

            Stripe.apiKey = stripeProperties.getApiKey();
            Map<String, Object> params = new HashMap<>();
            params.put(KeyConstants.KEY_STRIPE_PRODUCT_NAME, subscriptionPackage.getTitle());

            StripeProductAndPackageMapping productAndPackageMapping = stripeProductAndPackageMappingRepository.findBySubscriptionPackageSubscriptionPackageId(subscriptionPackageId);
            if (productAndPackageMapping != null) {

                if (productAndPackageMapping.isActive()) {
                    log.warn("Stripe product already in active state before publishing SubscriptionPackage. Please check and fix. Program Id : " + subscriptionPackageId);
                }
                //Updating the product on stripe
                Product existingProduct = Product.retrieve(productAndPackageMapping.getStripeProductId());
                product = existingProduct.update(params);
            } else {
                //Creating the product on stripe
                product = Product.create(params);
                productAndPackageMapping = new StripeProductAndPackageMapping();
            }
            productAndPackageMapping.setSubscriptionPackage(subscriptionPackage);
            productAndPackageMapping.setStripeProductId(product.getId());
            productAndPackageMapping.setActive(true);
            stripeProductAndPackageMappingRepository.save(productAndPackageMapping);
            return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_STRIPE_PRODUCT_CREATED, null);
        } finally {
            if (product != null)
                createProductPriceInStripe(product.getId(), subscriptionPackage.getPackageDuration().getDuration(), subscriptionPackage.getPrice());
        }
    }

    /**
     * @param packageSubscriptionModel
     * @return
     */
    @Transactional
    public ResponseModel createCustomerAndSubscribePackage(PackageSubscriptionModel packageSubscriptionModel) throws StripeException {
        log.info("Create customer and subscribe package starts.");
        long apiStartTimeMillis = new Date().getTime();
        Stripe.apiKey = stripeProperties.getApiKey();
        SubscriptionPackage subscriptionPackage = validationService.validateSubscriptionPackageId(packageSubscriptionModel.getSubscriptionPackageId());
        PlatformType platformType = validationService.validateAndGetPlatform(packageSubscriptionModel.getDevicePlatformTypeId());
        log.info("Validate subscription package and platform type id's : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        if (packageSubscriptionModel.getDevicePlatformTypeId() == 2) {
            throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_STRIPE_IOS_NOT_ALLOWED, null);
        }
        User user = userComponents.getUser();
        Date now = new Date();
        log.info("Get user : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        boolean isNewSubscription = true;
        PackageSubscription packageSubscription = packageSubscriptionRepository.findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageIdOrderBySubscribedDateDesc(user.getUserId(), subscriptionPackage.getSubscriptionPackageId());
        if (packageSubscription != null) {
            SubscriptionStatus subscriptionStatus = subscriptionService.getMemberPackageSubscriptionStatus(packageSubscription);
            if (subscriptionStatus != null) {
                String statusName = subscriptionStatus.getSubscriptionStatusName();
                if (statusName.equals(KeyConstants.KEY_PAID) || statusName.equals(KeyConstants.KEY_PAYMENT_PENDING) || statusName.equals(KeyConstants.KEY_EXPIRED)) {
                    isNewSubscription = false;
                }
            }
        }
        log.info("Check whether the subscription is new or existing one : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        //Offer code validation
        OfferCodeDetail offerCodeDetail = null;
        if (packageSubscriptionModel.getOfferCode() != null && !packageSubscriptionModel.getOfferCode().isEmpty()) {
            OfferCodeDetail currentOfferCodeDetail = offerCodeDetailRepository.findByOfferCodeAndIsInUse(packageSubscriptionModel.getOfferCode(), true);
            if (currentOfferCodeDetail == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_OFFER_CODE_DOESNT_EXIST, null);
            }
            if (now.before(currentOfferCodeDetail.getOfferStartingDate()) || now.after(currentOfferCodeDetail.getOfferEndingDate())) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_OFFER_CODE_NOT_VALID, null);
            }
            PackageOfferMapping packageOfferMapping = packageOfferMappingRepository.findBySubscriptionPackageSubscriptionPackageIdAndOfferCodeDetailOfferCodeId(subscriptionPackage.getSubscriptionPackageId(), currentOfferCodeDetail.getOfferCodeId());
            if (packageOfferMapping == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_OFFER_CODE_NOT_ACCEPTED_FOR_PACKAGE, null);
            }
            if (DiscountsConstants.OFFER_INACTIVE.equals(currentOfferCodeDetail.getOfferStatus())) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_OFFER_CODE_NOT_AVAILABLE, null);
            }
            if (isNewSubscription && currentOfferCodeDetail.getIsNewUser() || !isNewSubscription && !currentOfferCodeDetail.getIsNewUser()) {
                offerCodeDetail = currentOfferCodeDetail;
            } else {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_OFFER_CODE_NOT_APPLICABLE_FOR_USER, null);
            }
        }
        log.info("Offer code validation : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        OrderManagement orderManagement;
        if (packageSubscriptionModel.getExistingOrderId() == null || packageSubscriptionModel.getExistingOrderId().isEmpty()) {
            orderManagement = createPackageOrder(user, subscriptionPackage, true, KeyConstants.KEY_STRIPE, platformType, packageSubscriptionModel.getExistingOrderId(), offerCodeDetail);
        } else {
            orderManagement = orderManagementRepo.findTop1ByOrderId(packageSubscriptionModel.getExistingOrderId());
        }
        log.info("Getting order management : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        String stripeCustomerId = "";
        StripeCustomerAndUserMapping stripeCustomerAndUserMapping = stripeCustomerAndUserMappingRepository.findTop1ByUserUserId(user.getUserId());
        log.info("Query to get stripe customer and user mapping from DB : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if (stripeCustomerAndUserMapping == null) {
            UserProfile userProfile = userProfileRepository.findByUserUserId(user.getUserId());
            Map<String, Object> params = new HashMap<>();
            params.put(StringConstants.JSON_PROPERTY_KEY_DESCRIPTION, userProfile.getFirstName() + " " + userProfile.getLastName());
            params.put(StripeConstants.STRIPE_PROP_PAYMENT_METHOD, packageSubscriptionModel.getPaymentMethodId());
            params.put(StripeConstants.STRIPE_PROP_EMAIL, user.getEmail());
            OrderResponseView orderResponseView = new OrderResponseView();
            orderResponseView.setOrderId(orderManagement.getOrderId());
            /**
             * Creating customer in stripe and mapping it with Trainnr user object
             */
            try {
                Customer customer = Customer.create(params);
                stripeCustomerId = customer.getId();
                User fitwiseUser = userComponents.getUser();
                stripeCustomerAndUserMapping = new StripeCustomerAndUserMapping();
                stripeCustomerAndUserMapping.setUser(fitwiseUser);
                stripeCustomerAndUserMapping.setStripeCustomerId(customer.getId());
                stripeCustomerAndUserMappingRepository.save(stripeCustomerAndUserMapping);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseModel(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_ATTACHING_PAYMENT_METHOD_TO_CUSTOMER, orderResponseView);
            }
        } else {
            stripeCustomerId = stripeCustomerAndUserMapping.getStripeCustomerId();
            attachPaymentMethodToCustomer(stripeCustomerId, packageSubscriptionModel.getPaymentMethodId(), orderManagement);
        }
        log.info("Creating customer in stripe and mapping it with Trainnr user object : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        StripeProductAndPackageMapping productAndPackageMapping = stripeProductAndPackageMappingRepository.findBySubscriptionPackageSubscriptionPackageIdAndIsActive(subscriptionPackage.getSubscriptionPackageId(), true);
        if (productAndPackageMapping == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PRODUCT_PACKAGE_MAPPING_NOT_FOUND, null);
        }
        String productId = productAndPackageMapping.getStripeProductId();
        List<StripeProductAndPriceMapping> productAndPriceMappings = stripeProductAndPriceMappingRepository.findByProductIdAndPrice(productId, subscriptionPackage.getPrice());
        String priceId;
        if (productAndPriceMappings.isEmpty()) {
            priceId = createProductPriceInStripe(productId, subscriptionPackage.getPackageDuration().getDuration(), subscriptionPackage.getPrice());
        } else {
            priceId = productAndPriceMappings.get(0).getPriceId();
        }
        log.info("Query to get stripe product from DB : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        ResponseModel responseModel = createPackageSubscriptionInStripe(stripeCustomerId, priceId, packageSubscriptionModel.getPaymentMethodId(), subscriptionPackage, platformType, orderManagement, offerCodeDetail);
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Create customer and subscribe package ends.");
        return responseModel;
    }

    /**
     * Method used to create Order
     *
     * @param user
     * @param subscriptionPackage
     * @param isARB               - Boolean that intimates whether auto-subscription or not
     * @param paymentVendor       - String that implies whether the payment is from Authorize.net or Apple
     * @param platformType
     * @param existingOrderId
     * @param offerCodeDetail
     * @return
     */
    @Transactional
    public OrderManagement createPackageOrder(User user, SubscriptionPackage subscriptionPackage, boolean isARB, String
            paymentVendor, PlatformType platformType, String existingOrderId, OfferCodeDetail offerCodeDetail) {

        OrderManagement orderManagement;
        String orderNumber;

        if (existingOrderId != null && !existingOrderId.isEmpty()) {
            orderNumber = existingOrderId;
        } else {
            /*
             * The below piece of code generates a unique order number
             */
            orderNumber = OrderNumberGenerator.generateOrderNumber();
            logger.info("Order id =================> {}", orderNumber);
        }
        // Constructing the order management object
        SubscriptionType subscriptionType = subscriptionTypesRepo.findByNameIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_PACKAGE);

        orderManagement = new OrderManagement();
        orderManagement.setOrderId(orderNumber);
        orderManagement.setModeOfPayment(KeyConstants.KEY_STRIPE);
        orderManagement.setIsAutoRenewable(isARB);
        orderManagement.setSubscriptionPackage(subscriptionPackage);
        orderManagement.setSubscriptionType(subscriptionType);
        orderManagement.setUser(user);
        orderManagement.setSubscribedViaPlatform(platformType);

        // Saving the order management object in repository
        OrderManagement savedOrderManagement = orderManagementRepo.save(orderManagement);

        if (offerCodeDetail != null) {
            OfferCodeDetailAndOrderMapping offerCodeDetailAndOrderMapping = new OfferCodeDetailAndOrderMapping();
            offerCodeDetailAndOrderMapping.setOfferCodeDetail(offerCodeDetail);
            offerCodeDetailAndOrderMapping.setOrderManagement(savedOrderManagement);
            offerCodeDetailAndOrderMappingRepository.save(offerCodeDetailAndOrderMapping);
        }

        // Creating invoice for the order
        if (existingOrderId == null || existingOrderId.isEmpty()) {
            // Create invoice if only the order is new
            createInvoice(savedOrderManagement, existingOrderId);
        }

        return savedOrderManagement;
    }


    /**
     * Creating monthly subscription in Stripe
     *
     * @throws StripeException
     */
    @Transactional
    public ResponseModel createPackageSubscriptionInStripe(String customerId, String priceId, String paymentMethodId, SubscriptionPackage subscriptionPackage, PlatformType platformType, OrderManagement orderManagement, OfferCodeDetail offerCodeDetail) {
        log.info("Create package subscription in stripe starts.");
        long apiStartTimeMillis = new Date().getTime();
        Stripe.apiKey = stripeProperties.getApiKey();
        User user = userComponents.getUser();
        boolean isAlreadySubscribedSubscriptionEnded = false;
        boolean isNewSubscription = false;
        log.info("Get user and stripe api key : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        PackageSubscription packageSubscription = packageSubscriptionRepository.findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageIdOrderBySubscribedDateDesc(user.getUserId(), subscriptionPackage.getSubscriptionPackageId());
        log.info("Query: getting package subscription from DB : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if (packageSubscription == null) {
            isNewSubscription = true;
        }
        if (!isNewSubscription) {
            // Getting the subscriptionDate
            Date subscribedDate = packageSubscription.getSubscribedDate();
            // Adding program duration to the subscribed date to get the subscription End date
            Calendar cal = Calendar.getInstance();
            cal.setTime(subscribedDate);
            cal.add(Calendar.DATE, Math.toIntExact(subscriptionPackage.getPackageDuration().getDuration()));
            Date subscriptionEndDate = cal.getTime();
            Date currentDate = new Date();
            // User already subscribed the program
            if (packageSubscription.getSubscriptionStatus() != null) {
                if ((packageSubscription.getSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID))
                        && subscriptionEndDate.after(currentDate)) {
                    // Program already subscribed
                    throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_PACKAGE_ALREADY_SUBSCRIBED, MessageConstants.ERROR);
                } else {
                    isAlreadySubscribedSubscriptionEnded = true;
                }
            }
        }
        log.info("Check whether already subscribed subscription ended or not : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        boolean isCouponApplied = false;
        String stripeCouponId = null;
        StripeCouponAndOfferCodeMapping stripeCouponAndOfferCodeMapping = null;
        if (offerCodeDetail != null) {
            stripeCouponAndOfferCodeMapping = stripeCouponAndOfferCodeMappingRepository.findByOfferCodeDetail(offerCodeDetail);
            if (stripeCouponAndOfferCodeMapping == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_STRIPE_COUPON_NOT_FOUND_FOR_OFFER, null);
            }
            isCouponApplied = true;
            stripeCouponId = stripeCouponAndOfferCodeMapping.getStripeCouponId();
        }
        log.info("Get stripe coupon id : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        List<Object> items = new ArrayList<>();
        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("price", priceId);
        items.add(itemMap);
        Map<String, Object> params = new HashMap<>();
        params.put(StripeConstants.STRIPE_PROP_CUSTOMER, customerId);
        params.put("items", items);
        params.put("default_payment_method", paymentMethodId);
        if (isCouponApplied) {
            params.put("coupon", stripeCouponId);
        }
        log.info("Create request to stripe  : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        Subscription subscription = null;
        StripePayment stripePayment = new StripePayment();
        InvoiceManagement invoiceManagement = invoiceManagementRepository.findByOrderManagement(orderManagement);
        log.info("Query: get invoice management : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        stripePayment.setOrderManagement(orderManagement);
        stripePayment.setInvoiceManagement(invoiceManagement);
        OrderResponseView orderResponseView = new OrderResponseView();
        orderResponseView.setOrderId(orderManagement.getOrderId());
        try {
            subscription = Subscription.create(params);
            log.info("Create stripe subscription : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            if (!isCouponApplied) {
                stripePayment.setAmountPaid(subscriptionPackage.getPrice());
            } else {
                if (DiscountsConstants.MODE_FREE.equalsIgnoreCase(offerCodeDetail.getOfferMode())) {
                    stripePayment.setAmountPaid(0.00);
                } else {
                    stripePayment.setAmountPaid(offerCodeDetail.getOfferPrice().getPrice());
                }
            }
            if (isCouponApplied) {
                //Mapping created between stripe subscription and stripe coupons.
                StripeSubscriptionAndCouponMapping stripeSubscriptionAndCouponMapping = new StripeSubscriptionAndCouponMapping();
                stripeSubscriptionAndCouponMapping.setStripeCouponAndOfferCodeMapping(stripeCouponAndOfferCodeMapping);
                stripeSubscriptionAndCouponMapping.setStripeSubscriptionId(subscription.getId());
                Date now = new Date();
                stripeSubscriptionAndCouponMapping.setSubscriptionStartDate(now);
                LocalDateTime localDateTime = now.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                localDateTime = localDateTime.plusMonths(offerCodeDetail.getOfferDuration().getDurationInMonths());
                LocalDate localDate = localDateTime.toLocalDate();
                LocalTime endLocalTime = LocalTime.of(23, 59, 59);
                localDateTime = localDate.atTime(endLocalTime);
                Date couponEndDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                stripeSubscriptionAndCouponMapping.setCouponEndDate(couponEndDate);
                stripeSubscriptionAndCouponMappingRepository.save(stripeSubscriptionAndCouponMapping);
                log.info("Save stripe subscription and coupon mapping : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                profilingEndTimeMillis = new Date().getTime();
            }
            stripePayment.setAmountRefunded(null);
            stripePayment.setRefundTransactionId(null);
            stripePayment.setIsRefundUnderProcessing(false);
            stripePayment.setSubscriptionId(subscription.getId());
            stripePayment.setInvoiceId(subscription.getLatestInvoice());
            // Retrieving invoice object in-order to fetch charge id which will be used for Refund related operations.
            Invoice invoice =
                    Invoice.retrieve(subscription.getLatestInvoice());
            if (invoice != null) {
                stripePayment.setChargeId(invoice.getCharge());
                // Introducing 2 secs delay to check Charge status
                Thread.sleep(2000);
                Charge charge;
                try {
                    charge = Charge.retrieve(invoice.getCharge());
                    log.info("Charging for subscription : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                    profilingEndTimeMillis = new Date().getTime();
                    /**
                     * If charge is getting failed, cancelling the subscription and throwing error
                     */
                    if (charge != null) {
                        if (charge.getStatus().equalsIgnoreCase(StripeConstants.STRIPE_PROP_FAILED) || charge.getStatus().equalsIgnoreCase("pending")) {
                            stripePayment.setTransactionStatus(KeyConstants.KEY_FAILURE);
                            stripePayment.setErrorMessage("Charge failed after subscription");
                            stripePaymentRepository.save(stripePayment);
                            subscription.cancel(); // Cancelling the stripe subscription
                            return new ResponseModel(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_ADDING_SUBSCRIPTION, orderResponseView);
                        }
                        String paymentIntentId = charge.getPaymentIntent();
                        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
						if (paymentIntent != null && (paymentIntent.getStatus().equalsIgnoreCase(StripeConstants.STRIPE_PROP_FAILED)
								|| paymentIntent.getStatus().equalsIgnoreCase("canceled"))) {
							stripePayment.setTransactionStatus(KeyConstants.KEY_FAILURE);
							stripePayment.setErrorMessage("Payment intent failed after subscription");
							stripePaymentRepository.save(stripePayment);
							subscription.cancel(); // Cancelling the stripe subscription
							return new ResponseModel(Constants.ERROR_STATUS,
									MessageConstants.MSG_ERROR_ADDING_SUBSCRIPTION, orderResponseView);
						}
                    }
                    log.info("Query: Saving failed subscription in DB : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                    profilingEndTimeMillis = new Date().getTime();
                } catch (StripeException e) {
                    e.printStackTrace();
                }
            }
            stripePayment.setTransactionStatus(KeyConstants.KEY_PAID);
            stripePaymentRepository.save(stripePayment);
            log.info("Query: stripe payment repository : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            //Creating invoice in QBO
            fitwiseQboEntityService.createAndSyncQboInvoice(invoiceManagement);
            log.info("Create and sync QBO : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            /**
             * Paying the instructor his share
             */
            if (offerCodeDetail == null || !DiscountsConstants.MODE_FREE.equalsIgnoreCase(offerCodeDetail.getOfferMode())) {
                payToInstructor(stripePayment);
            }
        } catch (StripeException e) {
            stripePayment.setErrorCode(e.getCode());
            stripePayment.setErrorStatusCode(e.getStatusCode().toString());
            stripePayment.setErrorMessage(e.getStripeError().getMessage());
            stripePayment.setDeclinedCode(e.getStripeError().getDeclineCode());
            stripePaymentRepository.save(stripePayment);
            e.printStackTrace();
            return new ResponseModel(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_ADDING_SUBSCRIPTION, orderResponseView);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        logger.info("Subscription created --------------------> {}", subscription.getId());

        // Getting the active subscription status
        StripeSubscriptionStatus subscriptionStatus = stripeSubscriptionStatusRepository.findBySubscriptionStatusIgnoreCaseContaining(KeyConstants.KEY_ACTIVE);

        StripeSubscriptionAndUserPackageMapping stripeSubscriptionAndUserPackageMapping = new StripeSubscriptionAndUserPackageMapping();
        stripeSubscriptionAndUserPackageMapping.setUser(user);
        stripeSubscriptionAndUserPackageMapping.setSubscriptionPackage(subscriptionPackage);
        stripeSubscriptionAndUserPackageMapping.setStripeSubscriptionId(subscription.getId());
        stripeSubscriptionAndUserPackageMapping.setSubscriptionStatus(subscriptionStatus);
        stripeSubscriptionAndUserPackageMappingRepository.save(stripeSubscriptionAndUserPackageMapping);

        StripeSubscriptionChangesTracker subscriptionChangesTracker = new StripeSubscriptionChangesTracker();
        subscriptionChangesTracker.setIsSubscriptionActive(true);
        subscriptionChangesTracker.setOrderManagement(orderManagement);
        subscriptionChangesTracker.setSubscriptionId(subscription.getId());
        stripeSubscriptionChangesTrackerRepository.save(subscriptionChangesTracker);
        log.info("Updating stripe tables : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        if (isNewSubscription) {
            addNewlySubscribedPackageData(user, subscriptionPackage, platformType, true, true, orderManagement);
        }

        if (isAlreadySubscribedSubscriptionEnded) {
            overrideAlreadySubscribedPackageData(user, packageSubscription, platformType, true, orderManagement);
        }

        try{
            //Sending mail to member
            String subject = EmailConstants.PACKAGE_SUBSCRIPTION_SUBJECT.replace(EmailConstants.PACKAGE_TITLE,  subscriptionPackage.getTitle() );
            String mailBody = EmailConstants.PACKAGE_SUBSCRIPTION_CONTENT.replace(EmailConstants.PACKAGE_TITLE, "<b>" + subscriptionPackage.getTitle() + "</b>");
            String userName = fitwiseUtils.getUserFullName(user);
            User instructor = subscriptionPackage.getOwner();
            String memberPackage = EmailConstants.MEMBER_PACKAGE_LINK.replace(EmailConstants.LITERAL_APP_URL, dynamicLinkService.constructPackageLinkForMember(subscriptionPackage.getSubscriptionPackageId(),null,instructor));
            mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, mailBody)
                    .replace(EmailConstants.EMAIL_SUPPORT_URL, memberPackage);
            mailBody = emailContentUtil.replaceMemberAppUrl(mailBody);
            log.info("Sending mail to member : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            try {
                File file = invoicePDFGenerationService.generateInvoicePdf(orderManagement.getOrderId(), StringConstants.INVOICE + invoiceManagement.getInvoiceNumber());
                List<String> fileList = Collections.singletonList(file.getAbsolutePath());
                asyncMailer.sendHtmlMailWithAttachment(orderManagement.getUser().getEmail(),null, subject, mailBody, fileList);
            } catch (Exception e) {
                log.error("Invoice PDF generation failed for subscription mail. Order id : " + orderManagement.getOrderId());
                log.error(MessageConstants.MSG_ERR_EXCEPTION + e.getMessage());
                asyncMailer.sendHtmlMail(user.getEmail(), subject, mailBody);
            }
            log.info("Generate invoice PDF : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            /*
             * Stripe connect onboarding reminder mail
             * */
            boolean isOnboardingDetailsSubmitted = false;
            StripeAccountAndUserMapping stripeAccountAndUserMapping = stripeAccountAndUserMappingRepository.findByUserUserId(instructor.getUserId());
            if(stripeAccountAndUserMapping != null && stripeAccountAndUserMapping.getIsDetailsSubmitted() != null && stripeAccountAndUserMapping.getIsDetailsSubmitted()){
                isOnboardingDetailsSubmitted = true;
            }
            log.info("Stripe account and user mapping : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();

            boolean isOnBoardedViaPayPal = false;
            UserAccountAndPayPalIdMapping userAccountAndPayPalIdMapping = userAccountAndPayPalIdMappingRepository.findByUserUserId(instructor.getUserId());
            if (userAccountAndPayPalIdMapping != null && !userAccountAndPayPalIdMapping.getPayPalId().isEmpty()) {
                isOnBoardedViaPayPal = true;
            }
            log.info("User account and paypal id mapping : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            if (!isOnboardingDetailsSubmitted && !isOnBoardedViaPayPal) {
                userName = fitwiseUtils.getUserFullName(instructor);
                subject = EmailConstants.STRIPE_CONNECT_ONBOARD_REMINDER_SUBJECT;
                String trainnrDashboard = EmailConstants.STRIPE_CONNECT_ONBOARD_DASHBOARD_LINK.replace(EmailConstants.LITERAL_APP_URL, generalProperties.getInstructorBaseUrl() + RestUrlConstants.APP_INSTRUCTOR_DASHBOARD);
                mailBody = EmailConstants.STRIPE_CONNECT_ONBOARD_REMINDER_CONTENT;
                mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
                        .replace(EmailConstants.EMAIL_BODY, mailBody)
                        .replace(EmailConstants.EMAIL_SUPPORT_URL, trainnrDashboard);
                mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
                asyncMailer.sendHtmlMail(instructor.getEmail(), subject, mailBody);
            }
            log.info("Sending mail: stripe connect onboard reminder : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        }catch(Exception e){
            log.error(MessageConstants.MSG_ERR_EXCEPTION + e.getMessage());
        }
        log.info("Create package subscription in stripe ends: Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SUBSCRIPTION_ADDED, orderResponseView);
    }

    /**
     * @param user
     * @param subscriptionPackage
     * @param platformType
     * @param isPaymentSuccess
     * @param isAutoRenewal
     * @param orderManagement
     */
    @Transactional
    public void addNewlySubscribedPackageData(User user, SubscriptionPackage subscriptionPackage, PlatformType platformType, boolean isPaymentSuccess, boolean isAutoRenewal, OrderManagement orderManagement) {

        Date now = new Date();

        // If only payment status is success, entry will be added in the Subscription table
        if (isPaymentSuccess) {
            SubscriptionStatus newSubscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_PAID);

            SubscriptionPlan subscriptionPlan = subscriptionPlansRepo.findByDuration(subscriptionPackage.getPackageDuration().getDuration());
            SubscriptionType subscriptionType = subscriptionTypesRepo.findByNameIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_PACKAGE);

            PackageSubscription packageSubscription = new PackageSubscription();
            packageSubscription.setUser(user);
            packageSubscription.setSubscriptionPackage(subscriptionPackage);
            packageSubscription.setSubscribedDate(now);
            packageSubscription.setSubscriptionPlan(subscriptionPlan);
            packageSubscription.setSubscriptionStatus(newSubscriptionStatus);
            packageSubscription.setAutoRenewal(isAutoRenewal);
            packageSubscription.setSubscribedViaPlatform(platformType);

            List<PackageProgramSubscription> packageProgramSubscriptions = new ArrayList<>();
            if (subscriptionPackage.getPackageProgramMapping() != null && !subscriptionPackage.getPackageProgramMapping().isEmpty()) {
                for (PackageProgramMapping packageProgramMapping : subscriptionPackage.getPackageProgramMapping()) {
                    Programs program = packageProgramMapping.getProgram();

                    PackageProgramSubscription packageProgramSubscription = new PackageProgramSubscription();
                    packageProgramSubscription.setPackageSubscription(packageSubscription);
                    packageProgramSubscription.setUser(user);
                    packageProgramSubscription.setProgram(program);
                    packageProgramSubscription.setSubscribedDate(now);
                    packageProgramSubscription.setSubscriptionPlan(subscriptionPlan);
                    packageProgramSubscription.setSubscribedViaPlatform(platformType);
                    packageProgramSubscription.setSubscriptionStatus(newSubscriptionStatus);

                    packageProgramSubscriptions.add(packageProgramSubscription);
                }
            }
            packageSubscription.setPackageProgramSubscription(packageProgramSubscriptions);

            packageSubscriptionRepository.save(packageSubscription);

            //Saving revenueAudit table to store all tax details
            ProgramSubscriptionPaymentHistory programSubscriptionPaymentHistory = new ProgramSubscriptionPaymentHistory();
            programSubscriptionPaymentHistory.setOrderManagement(orderManagement);
            subscriptionPaymentHistoryRepository.save(programSubscriptionPaymentHistory);

            /*
             * Auditing the subscription
             */
            SubscriptionAudit subscriptionAudit = new SubscriptionAudit();
            subscriptionAudit.setUser(user);
            subscriptionAudit.setSubscriptionType(subscriptionType);
            subscriptionAudit.setPackageSubscription(packageSubscription);
            subscriptionAudit.setSubscriptionPlan(subscriptionPlan);
            subscriptionAudit.setSubscriptionStatus(newSubscriptionStatus);
            subscriptionAudit.setSubscribedViaPlatform(platformType);
            subscriptionAudit.setSubscriptionDate(now);
            subscriptionAudit.setAutoRenewal(isAutoRenewal);
            subscriptionAudit.setRenewalStatus(KeyConstants.KEY_NEW);
            subscriptionAudit.setProgramSubscriptionPaymentHistory(programSubscriptionPaymentHistory);

            subscriptionAuditRepo.save(subscriptionAudit);

        }
    }

    /**
     * @param user
     * @param packageSubscription
     * @param platformType
     * @param isAutoRenewal
     * @param orderManagement
     */
    @Transactional
    public void overrideAlreadySubscribedPackageData(User user, PackageSubscription packageSubscription, PlatformType platformType, boolean isAutoRenewal, OrderManagement orderManagement) {

        Date now = new Date();

        // If only payment status is success, entry will be added in the Subscription table
        SubscriptionStatus newSubscriptionStatus = subscriptionStatusRepo.
                findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_PAID);
        SubscriptionPlan subscriptionPlan = subscriptionPlansRepo.findByDuration(packageSubscription.getSubscriptionPackage().getPackageDuration().getDuration());

        SubscriptionType subscriptionType = subscriptionTypesRepo.findByNameIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_PACKAGE);

        packageSubscription.setSubscriptionPlan(subscriptionPlan);
        packageSubscription.setSubscribedDate(now);
        packageSubscription.setSubscriptionStatus(newSubscriptionStatus);
        packageSubscription.setSubscribedViaPlatform(platformType);

        SubscriptionPackage subscriptionPackage = packageSubscription.getSubscriptionPackage();
        List<PackageProgramSubscription> packageProgramSubscriptions = new ArrayList<>();
        if (subscriptionPackage.getPackageProgramMapping() != null && !subscriptionPackage.getPackageProgramMapping().isEmpty()) {
            for (PackageProgramMapping packageProgramMapping : subscriptionPackage.getPackageProgramMapping()) {
                Programs program = packageProgramMapping.getProgram();

                PackageProgramSubscription packageProgramSubscription = packageProgramSubscriptionRepository.findByPackageSubscriptionAndUserAndProgram(packageSubscription, user, program);
                if (packageProgramSubscription == null) {
                    packageProgramSubscription = new PackageProgramSubscription();
                }

                packageProgramSubscription.setPackageSubscription(packageSubscription);
                packageProgramSubscription.setUser(user);
                packageProgramSubscription.setProgram(program);
                packageProgramSubscription.setSubscribedDate(now);
                packageProgramSubscription.setSubscriptionPlan(subscriptionPlan);
                packageProgramSubscription.setSubscribedViaPlatform(platformType);
                packageProgramSubscription.setSubscriptionStatus(newSubscriptionStatus);

                packageProgramSubscriptions.add(packageProgramSubscription);
            }
        }
        packageSubscription.setPackageProgramSubscription(packageProgramSubscriptions);

        PackageSubscription savePackageSubscription = packageSubscriptionRepository.save(packageSubscription);

        //Saving revenueAudit table to store all tax details
        ProgramSubscriptionPaymentHistory programSubscriptionPaymentHistory = new ProgramSubscriptionPaymentHistory();
        programSubscriptionPaymentHistory.setOrderManagement(orderManagement);
        subscriptionPaymentHistoryRepository.save(programSubscriptionPaymentHistory);

        SubscriptionAudit subscriptionAudit = new SubscriptionAudit();
        subscriptionAudit.setUser(user);
        subscriptionAudit.setSubscriptionType(subscriptionType);
        subscriptionAudit.setPackageSubscription(savePackageSubscription);
        subscriptionAudit.setSubscriptionPlan(subscriptionPlan);
        subscriptionAudit.setSubscriptionStatus(newSubscriptionStatus);
        subscriptionAudit.setSubscribedViaPlatform(platformType);
        subscriptionAudit.setSubscriptionDate(now);
        subscriptionAudit.setAutoRenewal(isAutoRenewal);
        subscriptionAudit.setProgramSubscriptionPaymentHistory(programSubscriptionPaymentHistory);

        SubscriptionAudit subscriptionAuditOfPackage = subscriptionAuditRepo.findBySubscriptionTypeNameAndPackageSubscriptionIdOrderBySubscriptionDateDesc(KeyConstants.KEY_SUBSCRIPTION_PACKAGE, packageSubscription.getId()).get(0);


        if (subscriptionAuditOfPackage.getRenewalStatus().equalsIgnoreCase(KeyConstants.KEY_NEW) && subscriptionAuditOfPackage.getSubscriptionStatus() != null &&
                subscriptionAuditOfPackage.getSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_TRIAL)) {
            // If renewal status is new and subscription status is trial, then next paid subscription will be set to new
            subscriptionAudit.setRenewalStatus(KeyConstants.KEY_NEW);

        } else if (subscriptionAuditOfPackage.getRenewalStatus().equalsIgnoreCase(KeyConstants.KEY_NEW)) {
            // Already the renewal status is new! So setting it has renew on the second time
            subscriptionAudit.setRenewalStatus(KeyConstants.KEY_RENEWAL);

        } else if (subscriptionAuditOfPackage.getRenewalStatus().equalsIgnoreCase(KeyConstants.KEY_RENEWAL)) {
            // Already the renewal status is renew! So will be set as renew in next coming times
            subscriptionAudit.setRenewalStatus(KeyConstants.KEY_RENEWAL);
        }

        subscriptionAuditRepo.save(subscriptionAudit);
    }


    /**
     * Cancelling program subscription - Stripe payment gateway
     *
     * @param subscriptionPackageId
     * @param platformId
     * @return
     */
    public ResponseModel cancelStripePackageSubscription(Long subscriptionPackageId, Long platformId) {

        // If the subscription was done in Apple pay and user is trying to cancel that in android / web platforms
        if (platformId == 2) {
            throw new ApplicationException(Constants.CAN_EDIT, MessageConstants.MSG_APPLE_PAY_CANCELLATION_IN_AUTH_NET, null);
        }

        User user = userComponents.getUser();
        return cancelStripePackageSubscription(subscriptionPackageId, platformId, user, true);
    }

    /**
     * Cancelling package subscription - Stripe payment gateway
     *
     * @param subscriptionPackageId
     * @param platformId
     * @param user
     * @param sendMailNotification
     * @return
     */
    @Transactional
    public ResponseModel cancelStripePackageSubscription(Long subscriptionPackageId, Long platformId, User user, boolean sendMailNotification) {

        // If the subscription was done in Apple pay and user is trying to cancel that in android / web platforms
        if (platformId == 2) {
            throw new ApplicationException(Constants.CAN_EDIT, MessageConstants.MSG_APPLE_PAY_CANCELLATION_IN_AUTH_NET, null);
        }

        Stripe.apiKey = stripeProperties.getApiKey();

        SubscriptionPackage subscriptionPackage = validationService.validateSubscriptionPackageId(subscriptionPackageId);
        StripeSubscriptionAndUserPackageMapping mapping = stripeSubscriptionAndUserPackageMappingRepository.findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageId(user.getUserId(), subscriptionPackageId);

        if (mapping == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PACKAGE_NOT_SUBSCRIBED, null);
        }

        try {
            Subscription subscription = Subscription.retrieve(mapping.getStripeSubscriptionId());
            subscription.cancel();
        } catch (StripeException e) {
            if (StripeConstants.STRIPE_RESOURCE_MISSING.equalsIgnoreCase(e.getCode())) {
                log.warn("Stripe Subscription not found for : " + mapping.getStripeSubscriptionId());
            } else {
                log.error("Stripe Subscription failure : " + e.getMessage());
                throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_CANCELLING_SUBSCRIPTION_FAILED, null);
            }
        }

        PackageSubscription packageSubscription = packageSubscriptionRepository.findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageIdOrderBySubscribedDateDesc(user.getUserId(), subscriptionPackageId);
        if (packageSubscription != null) {
            StripeSubscriptionStatus stripeCancelledSubscriptionStatus = stripeSubscriptionStatusRepository.findBySubscriptionStatusIgnoreCaseContaining(KeyConstants.KEY_SUBSCRIPTION_CANCELLED);

            // Logging a new entry in the table that the subscription is cancelled
            StripeSubscriptionAndUserPackageMapping stripeSubscriptionAndUserPackageMapping = new StripeSubscriptionAndUserPackageMapping();
            stripeSubscriptionAndUserPackageMapping.setUser(user);
            stripeSubscriptionAndUserPackageMapping.setSubscriptionPackage(subscriptionPackage);
            stripeSubscriptionAndUserPackageMapping.setStripeSubscriptionId(mapping.getStripeSubscriptionId());
            stripeSubscriptionAndUserPackageMapping.setSubscriptionStatus(stripeCancelledSubscriptionStatus);
            stripeSubscriptionAndUserPackageMappingRepository.save(stripeSubscriptionAndUserPackageMapping);

            // Logging the cancel event in subscription tracker table
            StripeSubscriptionChangesTracker tracker = stripeSubscriptionChangesTrackerRepository.findTop1BySubscriptionIdOrderByModifiedDateDesc(mapping.getStripeSubscriptionId());
            StripeSubscriptionChangesTracker subscriptionChangesTracker = new StripeSubscriptionChangesTracker();
            subscriptionChangesTracker.setOrderManagement(tracker.getOrderManagement());
            subscriptionChangesTracker.setSubscriptionId(tracker.getSubscriptionId());
            subscriptionChangesTracker.setIsSubscriptionActive(false);
            stripeSubscriptionChangesTrackerRepository.save(subscriptionChangesTracker);

            // Setting the auto-renewal flag as false
            packageSubscription.setAutoRenewal(false);
            packageSubscriptionRepository.save(packageSubscription);

            // Sending mail to user for subscription cancel event
            if (sendMailNotification) {
                String subject = EmailConstants.AUTORENEWAL_SUBJECT;
                String mailBody = EmailConstants.AUTORENEWAL_PACKAGE_CONTENT.replace(EmailConstants.LITERAL_PACKAGE_NAME, "<b>" + subscriptionPackage.getTitle() + "</b>");
                String userName = fitwiseUtils.getUserFullName(user);
                mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, mailBody);
                mailBody = emailContentUtil.replaceMemberAppUrl(mailBody);
                asyncMailer.sendHtmlMail(user.getEmail(), subject, mailBody);
            }
        }
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SUBSCRIPTION_CANCELLED_SUCCESSFULLY, null);
    }

    /**
     * Used to process refund for a transaction
     *
     * @param stripeRefundRequestView
     * @return
     */
    @Transactional
    public ResponseModel processPackageRefund(StripeRefundRequestView stripeRefundRequestView) {
        Stripe.apiKey = stripeProperties.getApiKey();

        Map<String, Object> params = new HashMap<>();
        params.put(StripeConstants.STRIPE_PROP_CHARGE, stripeRefundRequestView.getChargeId());
        params.put(KeyConstants.KEY_AMOUNT, (int) (stripeRefundRequestView.getRefundableAmount() * 100)); //100 denotes paise/cents.

        try {
            Refund refund = Refund.create(params);
            if (refund != null) {

                StripePayment stripePayment = stripePaymentRepository.findTop1ByChargeId(stripeRefundRequestView.getChargeId());
                StripePayment refundedStripePayment = new StripePayment();
                refundedStripePayment.setOrderManagement(stripePayment.getOrderManagement());
                refundedStripePayment.setInvoiceId(stripePayment.getInvoiceId());
                refundedStripePayment.setChargeId(stripePayment.getChargeId());
                refundedStripePayment.setInvoiceManagement(stripePayment.getInvoiceManagement());
                refundedStripePayment.setTransactionStatus(KeyConstants.KEY_REFUND);
                refundedStripePayment.setSubscriptionId(stripePayment.getSubscriptionId());
                refundedStripePayment.setAmountPaid(stripePayment.getAmountPaid());
                refundedStripePayment.setAmountRefunded(stripeRefundRequestView.getRefundableAmount());
                refundedStripePayment.setRefundTransactionId(refund.getId());
                refundedStripePayment.setIsRefundUnderProcessing(false);
                stripePaymentRepository.save(refundedStripePayment);

                User user = stripePayment.getOrderManagement().getUser();
                SubscriptionPackage subscriptionPackage = stripePayment.getOrderManagement().getSubscriptionPackage();
                User instructor = subscriptionPackage.getOwner();

                // Changing the status of the subscription to REFUND
                PackageSubscription packageSubscription = packageSubscriptionRepository.findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageIdOrderBySubscribedDateDesc(user.getUserId(), subscriptionPackage.getSubscriptionPackageId());

                SubscriptionStatus subscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_REFUND);
                packageSubscription.setSubscriptionStatus(subscriptionStatus);
                packageSubscriptionRepository.save(packageSubscription);

                List<PackageProgramSubscription> packageProgramSubscriptionList = packageSubscription.getPackageProgramSubscription();
                for (PackageProgramSubscription packageProgramSubscription : packageProgramSubscriptionList) {
                    packageProgramSubscription.setSubscriptionStatus(subscriptionStatus);

                    packageProgramSubscriptionRepository.save(packageProgramSubscription);
                }

                SubscriptionAudit oldSubscriptionAudit = subscriptionAuditRepo.
                        findTop1BySubscriptionTypeNameAndUserUserIdAndPackageSubscriptionSubscriptionPackageSubscriptionPackageIdOrderByCreatedDateDesc(KeyConstants.KEY_SUBSCRIPTION_PACKAGE, user.getUserId(), subscriptionPackage.getSubscriptionPackageId());

                //Saving revenueAudit table to store all tax details
                ProgramSubscriptionPaymentHistory programSubscriptionPaymentHistory = new ProgramSubscriptionPaymentHistory();
                programSubscriptionPaymentHistory.setOrderManagement(stripePayment.getOrderManagement());
                subscriptionPaymentHistoryRepository.save(programSubscriptionPaymentHistory);

                // Adding new entry to the Subscription Audit table for Refund
                SubscriptionAudit newSubscriptionAudit = new SubscriptionAudit();
                newSubscriptionAudit.setPackageSubscription(oldSubscriptionAudit.getPackageSubscription());
                newSubscriptionAudit.setSubscriptionType(oldSubscriptionAudit.getSubscriptionType());
                newSubscriptionAudit.setSubscriptionPlan(oldSubscriptionAudit.getSubscriptionPlan());
                newSubscriptionAudit.setSubscriptionDate(oldSubscriptionAudit.getSubscriptionDate());
                newSubscriptionAudit.setSubscribedViaPlatform(oldSubscriptionAudit.getSubscribedViaPlatform());
                newSubscriptionAudit.setRenewalStatus(oldSubscriptionAudit.getRenewalStatus());
                newSubscriptionAudit.setUser(user);
                newSubscriptionAudit.setSubscriptionStatus(subscriptionStatus);
                newSubscriptionAudit.setProgramSubscriptionPaymentHistory(programSubscriptionPaymentHistory);
                subscriptionAuditRepo.save(newSubscriptionAudit);
                fitwiseQboEntityService.createAndSyncStripeRefund(stripePayment);

                //Reverse transfer from Instructor connected account to Fitwise Stripe account
                try {
                    InstructorPayment instructorPayment = instructorPaymentRepository.findByOrderManagement(stripePayment.getOrderManagement());
                    if (instructorPayment != null) {
                        double instructorShareForRefund = qboService.getRefundAmount(instructorPayment);
                        StripeAccountAndUserMapping stripeAccountAndUserMapping = stripeAccountAndUserMappingRepository.findByUserUserId(instructor.getUserId());


                        if (stripeAccountAndUserMapping != null && stripeAccountAndUserMapping.getIsOnBoardingCompleted().booleanValue()) {
                            /**
                             * Checking and Getting the available balance from the user connected account
                             */
                            Stripe.apiKey = stripeProperties.getApiKey();
                            RequestOptions requestOptions = RequestOptions.builder().setStripeAccount(stripeAccountAndUserMapping.getStripeAccountId()).build();
                            Long connectedAccountBalanceAmount = 0L;
                            // Stripe Get Balance API
                            Balance balance = Balance.retrieve(requestOptions);
                            for (Balance.Money balances : balance.getAvailable()) {
                                if (balances.getCurrency().equalsIgnoreCase(KeyConstants.KEY_CURRENCY_USD)) {
                                    connectedAccountBalanceAmount += balances.getAmount();
                                }
                            }

                            //pendingReversalAmount represents the amount available to be reversed in this transfer
                            long pendingReversalAmount = 0;
                            try {
                                Transfer transfer = Transfer.retrieve(instructorPayment.getStripeTransferId());
                                pendingReversalAmount = transfer.getAmount() - transfer.getAmountReversed();
                            } catch (StripeException e) {
                                e.printStackTrace();
                            }

                            // Since the available balance amount is given by 1000's, dividing it by 100.
                            double convertedConnectedAccountBalanceAmount = (double) connectedAccountBalanceAmount / 100;

                            //Reverse transfer is possible only if pendingReversalAmount is available
                            if (pendingReversalAmount > 0) {
                                if (convertedConnectedAccountBalanceAmount > 0) {
                                    //pendingReversalAmount can be equal to or less than instructorShareForRefund, which the amount transferred to instructor
                                    //if pendingReversalAmount is equal to instructorShareForRefund,  we attempt to reverse whole instructor share
                                    if (pendingReversalAmount == instructorShareForRefund) {
                                        if (convertedConnectedAccountBalanceAmount < instructorShareForRefund) {
                                            // If connected account has some available balance left over, Reverse transferring that available balance amount
                                            TransferReversal transferReversal = stripeConnectService.reverseTransfer(instructorPayment.getStripeTransferId(), convertedConnectedAccountBalanceAmount);
                                            if (transferReversal != null) {
                                                //QBO: DEBIT CAPTURE
                                                fitwiseQboEntityService.createAndSyncPaymentInsufficientBalance(instructorPayment.getOrderManagement(), convertedConnectedAccountBalanceAmount);
                                                // If Transfer succeeded, adding the balance amount to credit
                                                double balanceAmount = instructorShareForRefund - convertedConnectedAccountBalanceAmount;
                                                stripeConnectService.addInstructorCredit(instructorPayment, instructor, balanceAmount, KeyConstants.KEY_CURRENCY_US_DOLLAR);
                                            } else {
                                                // If Transfer fails, adding the total amount to Credit
                                                stripeConnectService.addInstructorCredit(instructorPayment, instructor, instructorShareForRefund, KeyConstants.KEY_CURRENCY_US_DOLLAR);
                                            }
                                        } else if (convertedConnectedAccountBalanceAmount >= instructorShareForRefund) {
                                            // If connected account has more available balance than refund amount, Reverse transferring the refund amount
                                            TransferReversal transferReversal = stripeConnectService.reverseTransfer(instructorPayment.getStripeTransferId(), instructorShareForRefund);
                                            if (transferReversal == null) {
                                                // If the reverse transfer fails, adding the total amount to Instructor Credit
                                                stripeConnectService.addInstructorCredit(instructorPayment, instructor, instructorShareForRefund, KeyConstants.KEY_CURRENCY_US_DOLLAR);
                                            } else {
                                                //QBO: DEBIT CAPTURE
                                                fitwiseQboEntityService.createAndSyncPaymentInsufficientBalance(instructorPayment.getOrderManagement(), instructorShareForRefund);
                                            }
                                        }
                                    } else if (pendingReversalAmount < instructorShareForRefund) {
                                        //if pendingReversalAmount is less than instructorShareForRefund,  we attempt to reverse the pendingReversalAmount
                                        if (convertedConnectedAccountBalanceAmount < pendingReversalAmount) {
                                            // If connected account has some available balance left over, Reverse transferring that available balance amount
                                            TransferReversal transferReversal = stripeConnectService.reverseTransfer(instructorPayment.getStripeTransferId(), convertedConnectedAccountBalanceAmount);
                                            if (transferReversal != null) {
                                                //QBO DEBIT CAPTURE
                                                fitwiseQboEntityService.createAndSyncPaymentInsufficientBalance(instructorPayment.getOrderManagement(), convertedConnectedAccountBalanceAmount);
                                                // If Transfer succeeded, adding the balance amount to credit
                                                double balanceAmount = instructorShareForRefund - convertedConnectedAccountBalanceAmount;
                                                stripeConnectService.addInstructorCredit(instructorPayment, instructor, balanceAmount, KeyConstants.KEY_CURRENCY_US_DOLLAR);
                                            } else {
                                                // If Transfer fails, adding the total amount to Credit
                                                stripeConnectService.addInstructorCredit(instructorPayment, instructor, instructorShareForRefund, KeyConstants.KEY_CURRENCY_US_DOLLAR);
                                            }
                                        } else if (convertedConnectedAccountBalanceAmount >= pendingReversalAmount) {
                                            // If connected account has more available balance than refund amount, Reverse transferring the refund amount
                                            TransferReversal transferReversal = stripeConnectService.reverseTransfer(instructorPayment.getStripeTransferId(), pendingReversalAmount);
                                            if (transferReversal != null) {
                                                //QBO: DEBIT CAPTURE
                                                fitwiseQboEntityService.createAndSyncPaymentInsufficientBalance(instructorPayment.getOrderManagement(), Double.parseDouble(String.valueOf(pendingReversalAmount)));
                                                // If Transfer succeeded, adding the balance amount to credit
                                                double balanceAmount = instructorShareForRefund - pendingReversalAmount;
                                                stripeConnectService.addInstructorCredit(instructorPayment, instructor, balanceAmount, KeyConstants.KEY_CURRENCY_US_DOLLAR);
                                            } else {
                                                // If the reverse transfer fails, adding the total amount to Instructor Credit
                                                stripeConnectService.addInstructorCredit(instructorPayment, instructor, instructorShareForRefund, KeyConstants.KEY_CURRENCY_US_DOLLAR);
                                            }
                                        }
                                    }
                                } else {
                                    // Connected balance has zero available balance. Adding the total amount to Instructor Credit
                                    stripeConnectService.addInstructorCredit(instructorPayment, instructor, instructorShareForRefund, KeyConstants.KEY_CURRENCY_US_DOLLAR);
                                }
                            } else {
                                // Total transfer amount has been reversed already. Adding the total amount to Instructor Credit
                                stripeConnectService.addInstructorCredit(instructorPayment, instructor, instructorShareForRefund, KeyConstants.KEY_CURRENCY_US_DOLLAR);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Exception while Reversing transfer made to instructor {}", e.getMessage());
                    e.printStackTrace();
                }

                DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
                String roundedPrice = decimalFormat.format(stripeRefundRequestView.getRefundableAmount());

                //Sending mail to member for refund
                String subject = EmailConstants.REFUND_INITIATED_PACKAGE_SUBJECT.replace(EmailConstants.LITERAL_PACKAGE_NAME, "'" + subscriptionPackage.getTitle() + "'");
                String supportLink = EmailConstants.TRAINNR_SUPPORT_LINK.replace(EmailConstants.EMAIL_EMAIL_ADDRESS, user.getEmail());
                String mailBody = EmailConstants.REFUND_INITIATED_PACKAGE_CONTENT
                        .replace(EmailConstants.LITERAL_PACKAGE_NAME, "<b>" + subscriptionPackage.getTitle() + "</b>")
                        .replace("#REFUND_AMOUNT#", roundedPrice);
                String userName = fitwiseUtils.getUserFullName(user);
                mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON
                        .replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
                        .replace(EmailConstants.EMAIL_BODY, mailBody)
                        .replace(EmailConstants.EMAIL_SUPPORT_URL, supportLink);
                mailBody = emailContentUtil.replaceMemberAppUrl(mailBody);
                asyncMailer.sendHtmlMail(user.getEmail(), subject, mailBody);
            }
        } catch (StripeException e) {
            e.printStackTrace();
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_FAILED_TO_REFUND, null);
        }

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_TRANSACTION_REFUND_SUCCESS, null);
    }


    /**
     * Method to create customer and attach payment method to customer
     * @param paymentMethodId
     * @return
     */
    public StripeSavePaymentResponseView saveCustomerAndPaymentMethod(String paymentMethodId) {

        log.info("Save customer and payment method starts.");
        long apiStartTimeMillis = new Date().getTime();
        if (ValidationUtils.isEmptyString(paymentMethodId)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_STRIPE_PAYMENT_ID_MISSING, null);
        }

        Stripe.apiKey = stripeProperties.getApiKey();
        log.info("Basic validation and getting stripe ap key : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        PaymentMethod paymentMethod = null;
        try {
            paymentMethod = PaymentMethod.retrieve(paymentMethodId);
            log.info("Retrieve payment method from stripe : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception occurred while retrieving payment method : " + e.getMessage());
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_STRIPE_PAYMENT_ID_INVALID, null);
        }

        User currentUser = userComponents.getUser();
        log.info("Getting current user : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        String stripeCustomerId = "";
        StripeCustomerAndUserMapping stripeCustomerAndUserMapping = stripeCustomerAndUserMappingRepository.findTop1ByUserUserId(currentUser.getUserId());
        log.info("Query: get stripe customer and user mapping from DB : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        if (stripeCustomerAndUserMapping == null) {
            UserProfile userProfile = userProfileRepository.findByUserUserId(currentUser.getUserId());
            log.info("Query: Getting user profile from DB : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            Map<String, Object> params = new HashMap<>();
            params.put(StringConstants.JSON_PROPERTY_KEY_DESCRIPTION, userProfile.getFirstName() + " " + userProfile.getLastName());
            params.put(StripeConstants.STRIPE_PROP_PAYMENT_METHOD, paymentMethodId);
            params.put(StripeConstants.STRIPE_PROP_EMAIL, currentUser.getEmail());

            /**
             * Creating customer in stripe and mapping it with Trainnr user object
             */
            try {
                Customer customer = Customer.create(params);
                log.info("Create customer in stripe : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                profilingEndTimeMillis = new Date().getTime();
                stripeCustomerId = customer.getId();
                stripeCustomerAndUserMapping = new StripeCustomerAndUserMapping();
                stripeCustomerAndUserMapping.setUser(currentUser);
                stripeCustomerAndUserMapping.setStripeCustomerId(stripeCustomerId);
                stripeCustomerAndUserMappingRepository.save(stripeCustomerAndUserMapping);
                log.info("Query: save stripe customer and user mapping in DB : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                profilingEndTimeMillis = new Date().getTime();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Exception occurred in saveCustomerAndPaymentMethod() : " + e.getMessage());
                throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_ATTACHING_PAYMENT_METHOD_TO_NEW_CUSTOMER, null);
            }
        } else {
            stripeCustomerId = stripeCustomerAndUserMapping.getStripeCustomerId();
            attachPaymentMethodToCustomer(stripeCustomerId, paymentMethodId);
            log.info("Attach payment method to customer : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
        }

        /*
        * Constructing response
        * */
        StripeSavePaymentResponseView stripeSavePaymentResponseView = new StripeSavePaymentResponseView();
        stripeSavePaymentResponseView.setUserId(currentUser.getUserId());
        stripeSavePaymentResponseView.setCustomerId(stripeCustomerId);

        StripePaymentMethodResponseView stripePaymentMethodResponseView = new StripePaymentMethodResponseView();
        stripePaymentMethodResponseView.setCardType(paymentMethod.getCard().getBrand());
        stripePaymentMethodResponseView.setPaymentMethodId(paymentMethod.getId());
        stripePaymentMethodResponseView.setMaskedCardNumber(paymentMethod.getCard().getLast4());
        stripeSavePaymentResponseView.setSavedCard(stripePaymentMethodResponseView);
        log.info("Construct response model : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Save customer and payment method ends.");

        return stripeSavePaymentResponseView;
    }

    @Transactional
    public void attachPaymentMethodToCustomer(String customerId, String paymentMethodId) {
        Stripe.apiKey = stripeProperties.getApiKey();

        PaymentMethod paymentMethod = null;
        try {
            paymentMethod = PaymentMethod.retrieve(paymentMethodId);
            Map<String, Object> params = new HashMap<>();
            params.put(StripeConstants.STRIPE_PROP_CUSTOMER, customerId);
            paymentMethod.attach(params);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception occurred while attaching payment to customer : " + e.getMessage());
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_ATTACHING_PAYMENT_METHOD_TO_CUSTOMER, null);
        }
    }

}

