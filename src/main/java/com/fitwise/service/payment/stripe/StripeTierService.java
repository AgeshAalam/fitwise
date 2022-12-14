package com.fitwise.service.payment.stripe;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.EmailConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.PaymentConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.StripeConstants;
import com.fitwise.entity.PlatformType;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.instructor.InstructorTierDetails;
import com.fitwise.entity.instructor.Tier;
import com.fitwise.entity.instructor.TierTypeDetails;
import com.fitwise.entity.payments.common.InvoiceManagement;
import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.payments.stripe.StripeProductAndTierMapping;
import com.fitwise.entity.payments.stripe.billing.StripeCustomerAndUserMapping;
import com.fitwise.entity.payments.stripe.billing.StripePayment;
import com.fitwise.entity.payments.stripe.billing.StripeProductAndPriceMapping;
import com.fitwise.entity.payments.stripe.billing.StripeSubscriptionAndUserTierMapping;
import com.fitwise.entity.payments.stripe.billing.StripeSubscriptionChangesTracker;
import com.fitwise.entity.payments.stripe.billing.StripeSubscriptionStatus;
import com.fitwise.entity.subscription.SubscriptionAudit;
import com.fitwise.entity.subscription.SubscriptionStatus;
import com.fitwise.entity.subscription.SubscriptionType;
import com.fitwise.entity.subscription.TierSubscription;
import com.fitwise.exception.ApplicationException;
import com.fitwise.properties.StripeProperties;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.instructor.InstructorTierDetailsRepository;
import com.fitwise.repository.order.InvoiceManagementRepository;
import com.fitwise.repository.order.OrderManagementRepository;
import com.fitwise.repository.payments.stripe.billing.StripeCustomerAndUserMappingRepository;
import com.fitwise.repository.payments.stripe.billing.StripePaymentRepository;
import com.fitwise.repository.payments.stripe.billing.StripeProductAndPriceMappingRepository;
import com.fitwise.repository.payments.stripe.billing.StripeProductAndTierMappingRepository;
import com.fitwise.repository.payments.stripe.billing.StripeSubscriptionAndUserTierMappingRepository;
import com.fitwise.repository.payments.stripe.billing.StripeSubscriptionChangesTrackerRepository;
import com.fitwise.repository.payments.stripe.billing.StripeSubscriptionStatusRepository;
import com.fitwise.repository.subscription.SubscriptionAuditRepo;
import com.fitwise.repository.subscription.SubscriptionStatusRepo;
import com.fitwise.repository.subscription.SubscriptionTypesRepo;
import com.fitwise.repository.subscription.TierSubscriptionRepository;
import com.fitwise.service.qbo.FitwiseQboEntityService;
import com.fitwise.service.receiptInvoice.InvoicePDFGenerationService;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.mail.AsyncMailer;
import com.fitwise.utils.payments.OrderNumberGenerator;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.order.OrderResponseView;
import com.fitwise.view.payment.stripe.CreateStripeCustomerRequestView;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Coupon;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.Subscription;
import com.stripe.param.CouponCreateParams;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.PriceUpdateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class StripeTierService {

    private final StripeProperties stripeProperties;
    private final StripeService stripeService;
    private final ValidationService validationService;
    private final UserComponents userComponents;
    private final TierSubscriptionRepository tierSubscriptionRepository;
    private final OrderManagementRepository orderManagementRepository;
    private final SubscriptionStatusRepo subscriptionStatusRepo;
    private final SubscriptionTypesRepo subscriptionTypesRepo;
    private final StripeCustomerAndUserMappingRepository stripeCustomerAndUserMappingRepository;
    private final UserProfileRepository userProfileRepository;
    private final StripeProductAndTierMappingRepository stripeProductAndTierMappingRepository;
    private final StripeProductAndPriceMappingRepository stripeProductAndPriceMappingRepository;
    private final InstructorTierDetailsRepository instructorTierDetailsRepository;
    private final InvoiceManagementRepository invoiceManagementRepository;
    private  final StripePaymentRepository stripePaymentRepository;
    private final StripeSubscriptionStatusRepository stripeSubscriptionStatusRepository;
    private final StripeSubscriptionAndUserTierMappingRepository stripeSubscriptionAndUserTierMappingRepository;
    private final StripeSubscriptionChangesTrackerRepository stripeSubscriptionChangesTrackerRepository;
    private final FitwiseUtils fitwiseUtils;
    private final InvoicePDFGenerationService invoicePDFGenerationService;
    private final AsyncMailer asyncMailer;
    private final SubscriptionAuditRepo subscriptionAuditRepo;
    private final FitwiseQboEntityService fitwiseQboEntityService;

    @Transactional
    public StripeProductAndTierMapping createTierProductInStripe(Tier tier) throws StripeException {
        Product product;
        StripeProductAndTierMapping productMapping;
        Stripe.apiKey = stripeProperties.getApiKey();
        Map<String, Object> params = new HashMap<>();
        params.put(KeyConstants.KEY_STRIPE_PRODUCT_NAME, tier.getTierType());
        productMapping = stripeProductAndTierMappingRepository.findByTierTierId(tier.getTierId());
        if (productMapping != null) {
            if (productMapping.isActive()) {
                log.warn("Stripe product already in active state. Please check and fix. tier Id : " + tier.getTierType());
            }
            Product existingProduct = Product.retrieve(productMapping.getStripeProductId());
            product = existingProduct.update(params);
        } else {
            product = Product.create(params);
            productMapping = new StripeProductAndTierMapping();
        }
        productMapping.setTier(tier);
        productMapping.setStripeProductId(product.getId());
        productMapping.setActive(true);
        stripeProductAndTierMappingRepository.save(productMapping);
        createPriceInStripeForTier(product.getId(), tier.getTierTypeDetails());
        return productMapping;
    }

    @Transactional
    public String createPriceInStripeForTier(String productId, TierTypeDetails tierDetails) throws StripeException {
        log.info("Step 6--->");
        String priceId;
        Stripe.apiKey = stripeProperties.getApiKey();
        // A positive integer in paise (or 0 for a free price) representing how much to charge.
        double unitPrice = tierDetails.getMinimumCommitment() * 100;
        long unitPriceInLong = (long) unitPrice;
        // Mapping product and pricing in Trainnr table
        Price price;
        List<StripeProductAndPriceMapping> productAndPriceMappings = stripeProductAndPriceMappingRepository.findByProductIdAndPrice(productId, tierDetails.getMinimumCommitment());
        StripeProductAndPriceMapping priceMapping = null;
        if (!productAndPriceMappings.isEmpty()) {
            //Updating the product price on stripe as inactive
            for (StripeProductAndPriceMapping productAndPriceMapping : productAndPriceMappings){
                boolean status = false;
                if (productAndPriceMapping.getPrice() != null && Objects.equals(productAndPriceMapping.getPrice(), tierDetails.getMinimumCommitment())) {
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
        if (priceMapping == null){
            priceMapping = new StripeProductAndPriceMapping();
            //Creating the product price on stripe
            PriceCreateParams params;
            // Checking the interval of the program and setting the stripe pricing interval as month or day
            params = PriceCreateParams.builder()
                    .setProduct(productId)
                    .setUnitAmount(unitPriceInLong) // This should be in paise/cents. 100L denotes 1$.
                    .setCurrency("usd")
                    .setRecurring(PriceCreateParams.Recurring.builder()
                            .setInterval(PriceCreateParams.Recurring.Interval.MONTH)
                            .setIntervalCount((long) tierDetails.getDurationInMonths())
                            .build())
                    .build();
            log.info("createPriceInStripeForTier : params " + params);
            price = Price.create(params);
            priceMapping.setProductId(productId);
            priceMapping.setPriceId(price.getId());
            priceMapping.setPrice(tierDetails.getMinimumCommitment());
            stripeProductAndPriceMappingRepository.save(priceMapping);
            priceId = price.getId();
        } else {
            priceId = priceMapping.getPriceId();
        }
        return priceId;
    }

    @Transactional
    public ResponseModel subscribeTier(CreateStripeCustomerRequestView stripeCustomerRequestView) throws StripeException {
        log.info("Step 1 --> Tier Subscribe");
        Tier tier = validationService.validateAndGetTier(stripeCustomerRequestView.getTierId());
        PlatformType platformType = validationService.validateAndGetPlatform(stripeCustomerRequestView.getDevicePlatformTypeId());
        if (platformType.getPlatform().equalsIgnoreCase(DBConstants.IOS)) {
            throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_STRIPE_IOS_NOT_ALLOWED, null);
        }
        log.info("Step 2 -->Validated");
        Stripe.apiKey = stripeProperties.getApiKey();
        User user = userComponents.getUser();
        log.info("Step 3 -->User fetched");
        boolean isNewSubscription = false;
        TierSubscription tierSubscription = tierSubscriptionRepository.findTop1ByUserUserIdOrderBySubscribedDateDesc(user.getUserId());
        Tier existingTier = null;
        String tierStatus = "newly added";
        long availableDays = 0;
        if (tierSubscription == null) {
            isNewSubscription = true;
        } else {
            existingTier = tierSubscription.getTier();
            if(tier.getTierId() > tierSubscription.getTier().getTierId()) {
                tierStatus = "upgraded";
            } else {
                if(tierSubscription.getSubscribedDate() != null){
                    long numberOfDaysUtilized = fitwiseUtils.getNumberOfDaysBetweenTwoDates(tierSubscription.getSubscribedDate(), new Date());
                    availableDays = tierSubscription.getTier().getTierTypeDetails().getDurationInDays() - numberOfDaysUtilized;
                }
                tierStatus = "downgraded";
            }
        }
        OrderResponseView orderResponseView = new OrderResponseView();
        boolean isCancelStripeSubscriptionRequired = false;
        String subscriptionStatusString = KeyConstants.KEY_PAID;
        if (tier.getTierType().equalsIgnoreCase(DBConstants.TIER_FREE)) {
            subscriptionStatusString = KeyConstants.KEY_FREE;
            if (tierSubscription != null && !tierSubscription.getTier().getTierType().equalsIgnoreCase(DBConstants.TIER_FREE)) {
                isCancelStripeSubscriptionRequired = true;
            }
        } else {
            OrderManagement orderManagement;
            String customerId;
            StripeCustomerAndUserMapping stripeCustomerAndUserMapping = stripeCustomerAndUserMappingRepository.findTop1ByUserUserId(user.getUserId());
            StripeProductAndTierMapping productAndTierMapping = stripeProductAndTierMappingRepository.findByTierTierIdAndIsActive(tier.getTierId(), true);
            if (stripeCustomerRequestView.getExistingOrderId() == null || stripeCustomerRequestView.getExistingOrderId().isEmpty()) {
                orderManagement = createTierOrder(user, tier, true, platformType, stripeCustomerRequestView.getExistingOrderId());
            } else {
                orderManagement = orderManagementRepository.findTop1ByOrderId(stripeCustomerRequestView.getExistingOrderId());
            }
            orderResponseView.setOrderId(orderManagement.getOrderId());
            if (stripeCustomerAndUserMapping == null) {
                stripeCustomerAndUserMapping = createStripeCustomer(user, stripeCustomerRequestView.getPaymentMethodId());
                customerId = stripeCustomerAndUserMapping.getStripeCustomerId();
            } else {
                customerId = stripeCustomerAndUserMapping.getStripeCustomerId();
                stripeService.attachPaymentMethodToCustomer(customerId, stripeCustomerRequestView.getPaymentMethodId(), orderManagement);
            }
            log.info("Step 4 -->Stripe customer fetched");
            if (productAndTierMapping == null) {
                productAndTierMapping = createTierProductInStripe(tier);
            }
            log.info("Step 5 -->Stripe price fetched");
            String productId = productAndTierMapping.getStripeProductId();
            List<StripeProductAndPriceMapping> productAndPriceMappings = stripeProductAndPriceMappingRepository.findByProductIdAndPrice(productId, tier.getTierTypeDetails().getMinimumCommitment());
            String priceId;
            if(productAndPriceMappings.isEmpty()){
                priceId = createPriceInStripeForTier(productId, tier.getTierTypeDetails());
            } else {
                priceId = productAndPriceMappings.get(0).getPriceId();
            }
            log.info("Step 6 -->Create subscription start");
            Stripe.apiKey = stripeProperties.getApiKey();
            boolean isAlreadySubscribedSubscriptionEnded = false;
            log.info("Step 7 -->Ins tier update");
            log.info("Step 8 -->Get existing");
            String subscriptionOrderStatus = null;
            if (!isNewSubscription) {
                Date subscribedDate = tierSubscription.getSubscribedDate();
                Calendar cal = Calendar.getInstance();
                cal.setTime(subscribedDate);
                cal.add(Calendar.MONTH, Math.toIntExact(tier.getTierTypeDetails().getDurationInMonths()));
                Date subscriptionEndDate = cal.getTime();
                Date currentDate = new Date();
                if (tierSubscription.getSubscriptionStatus() != null) {
                    if ((tierSubscription.getSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID))
                            && subscriptionEndDate.after(currentDate)) {
                        if (Objects.equals(tier.getTierId(), tierSubscription.getTier().getTierId())) {
                            throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_WAR_TIER_ALREADY_SUBSCRIBED, MessageConstants.ERROR);
                        } else if (tier.getTierId() > tierSubscription.getTier().getTierId()) {
                            subscriptionOrderStatus = PaymentConstants.TIER_SUBSCRIPTION_UPGRADE;
                        } else {
                            subscriptionOrderStatus = PaymentConstants.TIER_SUBSCRIPTION_DOWNGRADE;
                        }
                    } else {
                        isAlreadySubscribedSubscriptionEnded = true;
                    }
                }
            } else {
                subscriptionOrderStatus = PaymentConstants.TIER_SUBSCRIPTION_NEW;
            }
            log.info("Step 9 -->Ins tier update");
            String couponId = null;
            double amountPaid = tier.getTierTypeDetails().getMinimumCommitment();
            double balance = 0;
            if(!isAlreadySubscribedSubscriptionEnded && tierSubscription != null) {
                balance = getBalanceAmountOnTierChange(tierSubscription, tier);
                if (balance > 0) {
                    couponId = createTierAdjustmentCoupon(balance);
                    amountPaid -= balance;
                }
            }
            log.info("Step 10 -->Discount validated");
            List<Object> items = new ArrayList<>();
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("price", priceId);
            items.add(itemMap);
            Map<String, Object> params = new HashMap<>();
            params.put(StripeConstants.STRIPE_PROP_CUSTOMER, customerId);
            params.put("items", items);
            params.put("default_payment_method", stripeCustomerRequestView.getPaymentMethodId());
            if(availableDays > 0){
                params.put("trial_period_days", availableDays);
            } else if(StringUtils.hasText(couponId)) {
                params.put("coupon", couponId);
            }
            Subscription subscription = null;
            StripePayment stripePayment = new StripePayment();
            orderManagement.setDescription(subscriptionOrderStatus);
            InvoiceManagement invoiceManagement = invoiceManagementRepository.findByOrderManagement(orderManagement);
            stripePayment.setOrderManagement(orderManagement);
            stripePayment.setInvoiceManagement(invoiceManagement);
            orderResponseView.setOrderId(orderManagement.getOrderId());
            log.info("Step 11 -->Discount validated");
            try {
                subscription = Subscription.create(params);
                stripePayment.setAmountPaid(amountPaid);
                stripePayment.setAmountRefunded(null);
                stripePayment.setRefundTransactionId(null);
                stripePayment.setIsRefundUnderProcessing(false);
                stripePayment.setSubscriptionId(subscription.getId());
                stripePayment.setInvoiceId(subscription.getLatestInvoice());
                Invoice invoice =
                        Invoice.retrieve(subscription.getLatestInvoice());
                if (invoice != null && amountPaid > 0) {
                    stripePayment.setChargeId(invoice.getCharge());
                    Thread.sleep(2000);
                    Charge charge;
                    try {
                        charge = Charge.retrieve(invoice.getCharge());
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
                        log.error(MessageConstants.MSG_ERR_EXCEPTION + e.getMessage());
                    }
                }
                stripePayment.setTransactionStatus(KeyConstants.KEY_PAID);
                stripePayment = stripePaymentRepository.save(stripePayment);
                if (tierSubscription != null) {
                    isCancelStripeSubscriptionRequired = true;
                }
                orderManagement.setTierPaidAmt(amountPaid);
                orderManagement.setTierAdjustedAmt(balance);
                orderManagementRepository.save(orderManagement);
                fitwiseQboEntityService.createAndSyncQboInvoice(invoiceManagement);
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
            log.info("Step 12 -->Subscription created");
            StripeSubscriptionStatus subscriptionStatus = stripeSubscriptionStatusRepository.findBySubscriptionStatusIgnoreCaseContaining(KeyConstants.KEY_ACTIVE);
            StripeSubscriptionAndUserTierMapping stripeSubscriptionAndUserTierMapping = new StripeSubscriptionAndUserTierMapping();
            stripeSubscriptionAndUserTierMapping.setUser(userComponents.getUser());
            stripeSubscriptionAndUserTierMapping.setTier(tier);
            stripeSubscriptionAndUserTierMapping.setStripeSubscriptionId(subscription.getId());
            stripeSubscriptionAndUserTierMapping.setSubscriptionStatus(subscriptionStatus);
            stripeSubscriptionAndUserTierMappingRepository.save(stripeSubscriptionAndUserTierMapping);
            StripeSubscriptionChangesTracker subscriptionChangesTracker = new StripeSubscriptionChangesTracker();
            subscriptionChangesTracker.setIsSubscriptionActive(true);
            subscriptionChangesTracker.setOrderManagement(orderManagement);
            subscriptionChangesTracker.setSubscriptionId(subscription.getId());
            stripeSubscriptionChangesTrackerRepository.save(subscriptionChangesTracker);
            log.info("Step 13 -->Tracker updated");
            try{
                String subject = EmailConstants.TIER_SUBSCRIPTION_SUBJECT.replace("#TIER_NAME#",  tier.getTierType() );
                String mailBody = "Your subscription " + tierStatus + " successfully.";
                String userName = fitwiseUtils.getUserFullName(user);
                mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, mailBody);
                try {
                    File file = invoicePDFGenerationService.generateInvoicePdf(orderManagement.getOrderId(), StringConstants.INVOICE + invoiceManagement.getInvoiceNumber());
                    List<String> fileList = Collections.singletonList(file.getAbsolutePath());
                    asyncMailer.sendHtmlMailWithAttachment(user.getEmail(),null, subject, mailBody, fileList);
                } catch (Exception e) {
                    log.error("Invoice PDF generation failed for subscription mail. Order id : " + orderManagement.getOrderId());
                    log.error(MessageConstants.MSG_ERR_EXCEPTION + e.getMessage());
                    asyncMailer.sendHtmlMail(user.getEmail(), subject, mailBody);
                }
            }catch(Exception e){
                log.error(MessageConstants.MSG_ERR_EXCEPTION + e.getMessage());
            }
        }
        if (isNewSubscription) {
            addNewlySubscribedTierData(user, tier, platformType, subscriptionStatusString);
            log.info("Step 14 -->New tier added");
        } else {
            overrideAlreadySubscribedTierData(user, tierSubscription, platformType, true, tier, subscriptionStatusString);
            log.info("Step 15 -->Tier updated");
        }
        InstructorTierDetails tierDetails = instructorTierDetailsRepository.findByUserAndActive(user, true);
        if(tierDetails == null) {
            tierDetails = new InstructorTierDetails();
            tierDetails.setUser(user);
            tierDetails.setActive(true);
        }
        tierDetails.setTier(tier);
        instructorTierDetailsRepository.save(tierDetails);
        if(isCancelStripeSubscriptionRequired && !existingTier.getTierType().equalsIgnoreCase(DBConstants.TIER_FREE)){
            cancelStripePackageSubscription(tierSubscription.getUser(), existingTier);
        }
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SUBSCRIPTION_ADDED, orderResponseView);
    }

    private StripeCustomerAndUserMapping createStripeCustomer(User user, String paymentMethodId) {
        StripeCustomerAndUserMapping stripeCustomerAndUserMapping;
        UserProfile userProfile = userProfileRepository.findByUserUserId(user.getUserId());
        Map<String, Object> params = new HashMap<>();
        params.put(StringConstants.JSON_PROPERTY_KEY_DESCRIPTION, userProfile.getFirstName() + " " + userProfile.getLastName());
        params.put(StripeConstants.STRIPE_PROP_PAYMENT_METHOD, paymentMethodId);
        params.put(StripeConstants.STRIPE_PROP_EMAIL, user.getEmail());
        try {
            Customer customer = Customer.create(params);
            stripeCustomerAndUserMapping = new StripeCustomerAndUserMapping();
            stripeCustomerAndUserMapping.setUser(user);
            stripeCustomerAndUserMapping.setStripeCustomerId(customer.getId());
            stripeCustomerAndUserMappingRepository.save(stripeCustomerAndUserMapping);
        } catch (Exception exception) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_ATTACHING_PAYMENT_METHOD_TO_CUSTOMER, null);
        }
        return stripeCustomerAndUserMapping;
    }

    public void overrideAlreadySubscribedTierData(User user, TierSubscription subscribedTier, PlatformType
            platformType, boolean isAutoRenewal, Tier tier, String subscriptionStatusString) {
        SubscriptionStatus newSubscriptionStatus = subscriptionStatusRepo.
                findBySubscriptionStatusNameIgnoreCaseContaining(subscriptionStatusString);
        SubscriptionType subscriptionType = subscriptionTypesRepo.findByNameIgnoreCase(KeyConstants.KEY_TIER);
        subscribedTier.setSubscribedDate(new Date());
        subscribedTier.setSubscriptionStatus(newSubscriptionStatus);
        subscribedTier.setSubscribedViaPlatform(platformType);
        subscribedTier.setTier(tier);
        subscribedTier = tierSubscriptionRepository.save(subscribedTier);
        SubscriptionAudit subscriptionAudit = new SubscriptionAudit();
        subscriptionAudit.setUser(user);
        subscriptionAudit.setSubscriptionType(subscriptionType);
        subscriptionAudit.setSubscriptionStatus(newSubscriptionStatus);
        subscriptionAudit.setSubscribedViaPlatform(platformType);
        subscriptionAudit.setSubscriptionDate(new Date());
        subscriptionAudit.setAutoRenewal(isAutoRenewal);
        subscriptionAudit.setTierSubscription(subscribedTier);
        subscriptionAudit.setProgramSubscriptionPaymentHistory(null);
        if (tier.getTierType().equalsIgnoreCase(DBConstants.TIER_FREE)) {
            subscriptionAudit.setRenewalStatus(KeyConstants.KEY_NEW);
        } else {
            SubscriptionAudit subscriptionAuditOfTier = subscriptionAuditRepo.findBySubscriptionTypeNameAndTierSubscriptionTierSubscriptionIdOrderBySubscriptionDateDesc(KeyConstants.KEY_TIER, subscribedTier.getTierSubscriptionId()).get(0);
            if (subscriptionAuditOfTier.getRenewalStatus().equalsIgnoreCase(KeyConstants.KEY_NEW) && subscriptionAuditOfTier.getSubscriptionStatus() != null &&
                    subscriptionAuditOfTier.getSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_TRIAL)) {
                // If renewal status is new and subscription status is trial, then next paid subscription will be set to new
                subscriptionAudit.setRenewalStatus(KeyConstants.KEY_NEW);
            } else if (subscriptionAuditOfTier.getRenewalStatus().equalsIgnoreCase(KeyConstants.KEY_NEW)) {
                // Already the renewal status is new! So setting it has renewed on the second time
                subscriptionAudit.setRenewalStatus(KeyConstants.KEY_RENEWAL);
            } else if (subscriptionAuditOfTier.getRenewalStatus().equalsIgnoreCase(KeyConstants.KEY_RENEWAL)) {
                // Already the renewal status is renewed! So will be set as renew in next coming times
                subscriptionAudit.setRenewalStatus(KeyConstants.KEY_RENEWAL);
            }
        }
        subscriptionAuditRepo.save(subscriptionAudit);
    }

    @Transactional
    public void addNewlySubscribedTierData(User user, Tier tier, PlatformType platformType,
                                           String subscriptionStatus) {
        SubscriptionStatus newSubscriptionStatus = subscriptionStatusRepo
                .findBySubscriptionStatusNameIgnoreCaseContaining(subscriptionStatus);
        SubscriptionType subscriptionType = subscriptionTypesRepo.findByNameIgnoreCase(KeyConstants.KEY_TIER);
        TierSubscription tierSubscription = new TierSubscription();
        tierSubscription.setTier(tier);
        tierSubscription.setSubscribedViaPlatform(platformType);
        tierSubscription.setAutoRenewal(false);
        tierSubscription.setSubscribedDate(new Date());
        tierSubscription.setUser(user);
        tierSubscription.setSubscriptionStatus(newSubscriptionStatus);
        tierSubscriptionRepository.save(tierSubscription);
        SubscriptionAudit subscriptionAudit = new SubscriptionAudit();
        subscriptionAudit.setUser(user);
        subscriptionAudit.setSubscriptionType(subscriptionType);
        subscriptionAudit.setTierSubscription(tierSubscription);
        subscriptionAudit.setSubscriptionStatus(newSubscriptionStatus);
        subscriptionAudit.setSubscribedViaPlatform(platformType);
        subscriptionAudit.setSubscriptionDate(new Date());
        subscriptionAudit.setAutoRenewal(false);
        subscriptionAudit.setRenewalStatus(KeyConstants.KEY_NEW);
        subscriptionAudit.setProgramSubscriptionPaymentHistory(null);
        subscriptionAuditRepo.save(subscriptionAudit);
    }

    public OrderManagement createTierOrder(User user, Tier tier, boolean isARB, PlatformType platformType,
                                           String existingOrderId) {
        String orderNumber;
        if (existingOrderId != null && !existingOrderId.isEmpty()) {
            orderNumber = existingOrderId;
        } else {
            orderNumber = OrderNumberGenerator.generateOrderNumber();
        }
        SubscriptionType subscriptionType = subscriptionTypesRepo.findByNameIgnoreCase(KeyConstants.KEY_TIER);
        OrderManagement orderManagement = new OrderManagement();
        orderManagement.setOrderId(orderNumber);
        orderManagement.setModeOfPayment(KeyConstants.KEY_STRIPE);
        orderManagement.setIsAutoRenewable(isARB);
        orderManagement.setTier(tier);
        orderManagement.setSubscriptionType(subscriptionType);
        orderManagement.setUser(user);
        orderManagement.setSubscribedViaPlatform(platformType);
        OrderManagement savedOrderManagement = orderManagementRepository.save(orderManagement);
        if (existingOrderId == null || existingOrderId.isEmpty()) {
            stripeService.createInvoice(savedOrderManagement, existingOrderId);
        }
        return savedOrderManagement;
    }

    public Double getBalanceAmountOnTierChange(TierSubscription tierSubscription, Tier newTier) {
        Date startDate = tierSubscription.getSubscribedDate();
        Date endDate = new Date();
        long numberOfDaysUtilized = fitwiseUtils.getNumberOfDaysBetweenTwoDates(startDate, endDate);
        double costForADay = tierSubscription.getTier().getTierTypeDetails().getMinimumCommitment() / tierSubscription.getTier().getTierTypeDetails().getDurationInDays();
        double costUtilized = numberOfDaysUtilized * costForADay;
        double balance = tierSubscription.getTier().getTierTypeDetails().getMinimumCommitment() - costUtilized;
        if (newTier.getTierTypeDetails().getMinimumCommitment() <= balance) {
            balance = newTier.getTierTypeDetails().getMinimumCommitment();
        }
        return balance;
    }

    private String createTierAdjustmentCoupon(Double price) {
        String couponId;
        Stripe.apiKey = stripeProperties.getApiKey();
        CouponCreateParams couponParams;
        double priceDouble = price * 100;
        long priceInLong = (long) priceDouble;
        couponParams = CouponCreateParams.builder()
                .setCurrency("usd")
                .setDuration(CouponCreateParams.Duration.ONCE)
                .setAmountOff(priceInLong)
                .build();
        try {
            Coupon coupon = Coupon.create(couponParams);
            couponId = coupon.getId();
        } catch (StripeException e) {
            log.error(MessageConstants.MSG_ERR_EXCEPTION + e.getMessage());
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_STRIPE_COUPON_CREATION_FAILED, null);
        }
        return couponId;
    }

    @Transactional
    public void cancelStripePackageSubscription(User user, Tier existingSubscriptionTier) {
        Stripe.apiKey = stripeProperties.getApiKey();
        OrderManagement orderManagement = orderManagementRepository.findTop1ByUserAndTierOrderByCreatedDateDesc(user, existingSubscriptionTier);
        StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
        try {
            Subscription subscription = Subscription.retrieve(stripePayment.getSubscriptionId());
            subscription.cancel();
        } catch (StripeException e) {
            if (StripeConstants.STRIPE_RESOURCE_MISSING.equalsIgnoreCase(e.getCode())) {
                log.warn("Stripe Payment : " + stripePayment.getId());
            } else {
                log.error("Stripe Subscription failure : " + e.getMessage());
                throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_CANCELLING_SUBSCRIPTION_FAILED, null);
            }
        }
        StripeSubscriptionStatus stripeCancelledSubscriptionStatus = stripeSubscriptionStatusRepository.findBySubscriptionStatusIgnoreCaseContaining(KeyConstants.KEY_SUBSCRIPTION_CANCELLED);
        // Logging a new entry in the table that the subscription is cancelled
        StripeSubscriptionAndUserTierMapping stripeSubscriptionAndUserTierMapping = new StripeSubscriptionAndUserTierMapping();
        stripeSubscriptionAndUserTierMapping.setUser(user);
        stripeSubscriptionAndUserTierMapping.setTier(existingSubscriptionTier);
        stripeSubscriptionAndUserTierMapping.setStripeSubscriptionId(stripePayment.getSubscriptionId());
        stripeSubscriptionAndUserTierMapping.setSubscriptionStatus(stripeCancelledSubscriptionStatus);
        stripeSubscriptionAndUserTierMappingRepository.save(stripeSubscriptionAndUserTierMapping);
        // Logging the cancel event in subscription tracker table
        StripeSubscriptionChangesTracker tracker = stripeSubscriptionChangesTrackerRepository.findTop1BySubscriptionIdOrderByModifiedDateDesc(stripePayment.getSubscriptionId());
        StripeSubscriptionChangesTracker subscriptionChangesTracker = new StripeSubscriptionChangesTracker();
        subscriptionChangesTracker.setOrderManagement(tracker.getOrderManagement());
        subscriptionChangesTracker.setSubscriptionId(tracker.getSubscriptionId());
        subscriptionChangesTracker.setIsSubscriptionActive(false);
        stripeSubscriptionChangesTrackerRepository.save(subscriptionChangesTracker);
    }

    @Transactional
    public void renewTierSubscription(String subscriptionId, String invoiceId, String chargeId) throws StripeException {
        StripeSubscriptionAndUserTierMapping stripeSubscriptionAndUserTierMapping = stripeSubscriptionAndUserTierMappingRepository.findTop1ByStripeSubscriptionId(subscriptionId);
        Tier tier = stripeSubscriptionAndUserTierMapping.getTier();
        User user = stripeSubscriptionAndUserTierMapping.getUser();
        TierSubscription tierSubscription = tierSubscriptionRepository.findTop1ByUserUserIdAndTierTierIdOrderBySubscribedDateDesc(user.getUserId(), tier.getTierId());
        OrderManagement newOrderManagement = createTierOrder(user, tier, true, tierSubscription.getSubscribedViaPlatform(), null);
        overrideAlreadySubscribedTierData(user, tierSubscription, tierSubscription.getSubscribedViaPlatform(), true, tier, KeyConstants.KEY_PAID);
        SubscriptionType subscriptionType = newOrderManagement.getSubscriptionType();
        InvoiceManagement invoiceManagement = invoiceManagementRepository.findByOrderManagement(newOrderManagement);
        StripePayment stripePayment = new StripePayment();
        stripePayment.setOrderManagement(newOrderManagement);
        stripePayment.setInvoiceManagement(invoiceManagement);
        stripePayment.setAmountPaid(tier.getTierTypeDetails().getMinimumCommitment());
        stripePayment.setAmountRefunded(null);
        stripePayment.setRefundTransactionId(null);
        stripePayment.setIsRefundUnderProcessing(false);
        stripePayment.setSubscriptionId(subscriptionId);
        stripePayment.setInvoiceId(invoiceId);
        stripePayment.setChargeId(chargeId);
        stripePayment.setTransactionStatus(KeyConstants.KEY_PAID);
        stripePaymentRepository.save(stripePayment);
        fitwiseQboEntityService.createAndSyncQboInvoice(invoiceManagement);
        StripeSubscriptionChangesTracker tracker = stripeSubscriptionChangesTrackerRepository.findTop1BySubscriptionIdOrderByModifiedDateDesc(subscriptionId);
        if (tracker != null) {
            StripeSubscriptionChangesTracker stripeSubscriptionChangesTracker = new StripeSubscriptionChangesTracker();
            stripeSubscriptionChangesTracker.setIsSubscriptionActive(true);
            stripeSubscriptionChangesTracker.setOrderManagement(newOrderManagement);
            stripeSubscriptionChangesTracker.setSubscriptionId(tracker.getSubscriptionId());
            stripeSubscriptionChangesTrackerRepository.save(stripeSubscriptionChangesTracker);
        }
        if(subscriptionType != null && invoiceManagement != null){
            try{
                String subject = EmailConstants.TIER_SUBSCRIPTION_SUBJECT.replace("#TIER_NAME#",  tier.getTierType() );
                String mailBody = "Your subscription renewed successfully.";
                String userName = fitwiseUtils.getUserFullName(user);
                mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, mailBody);
                try {
                    File file = invoicePDFGenerationService.generateInvoicePdf(newOrderManagement.getOrderId(), StringConstants.INVOICE + invoiceManagement.getInvoiceNumber());
                    List<String> fileList = Collections.singletonList(file.getAbsolutePath());
                    asyncMailer.sendHtmlMailWithAttachment(user.getEmail(),null, subject, mailBody, fileList);
                } catch (Exception e) {
                    log.error("Invoice PDF generation failed for subscription mail. Order id : " + newOrderManagement.getOrderId());
                    log.error(MessageConstants.MSG_ERR_EXCEPTION + e.getMessage());
                    asyncMailer.sendHtmlMail(user.getEmail(), subject, mailBody);
                }
            }catch(Exception e){
                log.error(MessageConstants.MSG_ERR_EXCEPTION + e.getMessage());
            }
        }
    }
}