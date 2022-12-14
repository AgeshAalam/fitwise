package com.fitwise.service.receiptInvoice;

import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.discounts.DiscountsConstants;
import com.fitwise.constants.payments.appleiap.NotificationConstants;
import com.fitwise.entity.InstructorPayment;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.discounts.OfferCodeDetailAndOrderMapping;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.payments.appleiap.ApplePayment;
import com.fitwise.entity.payments.appleiap.AppleProductSubscription;
import com.fitwise.entity.payments.authNet.AuthNetPayment;
import com.fitwise.entity.payments.authNet.AuthNetSubscriptionChangesTracker;
import com.fitwise.entity.payments.common.InvoiceManagement;
import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.payments.stripe.billing.StripePayment;
import com.fitwise.entity.payments.stripe.billing.StripeSubscriptionChangesTracker;
import com.fitwise.entity.subscription.ProgramSubscription;
import com.fitwise.model.receiptPdf.ReportModel;
import com.fitwise.properties.StripeProperties;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.discountsRepository.OfferCodeDetailAndOrderMappingRepository;
import com.fitwise.repository.order.InstructorPaymentRepository;
import com.fitwise.repository.order.InvoiceManagementRepository;
import com.fitwise.repository.payments.appleiap.ApplePaymentRepository;
import com.fitwise.repository.payments.appleiap.AppleProductSubscriptionRepository;
import com.fitwise.repository.payments.authnet.AuthNetPaymentRepository;
import com.fitwise.repository.payments.authnet.AuthNetSubscriptionChangesTrackerRepository;
import com.fitwise.repository.payments.stripe.billing.StripePaymentRepository;
import com.fitwise.repository.payments.stripe.billing.StripeSubscriptionChangesTrackerRepository;
import com.fitwise.repository.subscription.ProgramSubscriptionRepo;
import com.fitwise.repository.subscription.SubscriptionAuditRepo;
import com.fitwise.service.payment.authorizenet.GetTransactionDetails;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.utils.FitwiseUtils;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.PaymentMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.authorize.api.contract.v1.CustomerAddressType;
import net.authorize.api.contract.v1.GetTransactionDetailsResponse;
import net.authorize.api.contract.v1.PaymentMaskedType;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.jasperreports.JasperReportsUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeSet;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

@Service
@Slf4j
@RequiredArgsConstructor
public class InvoicePDFGenerationService {

    private final ValidationService validationService;

    private final InvoiceManagementRepository invoiceManagementRepository;

    private final SubscriptionAuditRepo subscriptionAuditRepo;

    private final AuthNetPaymentRepository authNetPaymentRepository;

    private final UserProfileRepository userProfileRepository;

    private final ProgramSubscriptionRepo programSubscriptionRepo;

    private static Logger logger = LogManager.getLogger(InvoicePDFGenerationService.class);

    @Value("${invoice.logo.path}")
    private String logo_path;

    @Value("${invoice.template.path}")
    private String invoice_template;

    @Value("${invoice.package.template.path}")
    private String invoicePackageTemplate;

    private final ApplePaymentRepository applePaymentRepository;

    private final AppleProductSubscriptionRepository appleProductSubscriptionRepository;

    private final FitwiseUtils fitwiseUtils;

    private final StripePaymentRepository stripePaymentRepository;

    private final StripeSubscriptionChangesTrackerRepository stripeSubscriptionChangesTrackerRepository;

    private final AuthNetSubscriptionChangesTrackerRepository authNetSubscriptionChangesTrackerRepository;

    private final StripeProperties stripeProperties;

    private final OfferCodeDetailAndOrderMappingRepository offerCodeDetailAndOrderMappingRepository;

    private final InstructorPaymentRepository instructorPaymentRepository;

    /**
     * Used to get the order receipt - Will be downloaded as a pdf file
     *
     * @param orderId
     * @return
     */
    @Transactional
    public File generateInvoiceReceipt(String orderId, HttpServletRequest request,
                                       HttpServletResponse response) throws IOException {
        return generateInvoicePdf(orderId, "Trainnr-Invoice");
    }

    /**
     * Method to generate Pdf
     * @param orderId
     * @param fileName
     * @return
     * @throws IOException
     */
    public File generateInvoicePdf(String orderId, String fileName) throws IOException {
        log.info("Generate invoice PDF starts.");
        long apiStartTimeMillis = new Date().getTime();
        File pdfFile = File.createTempFile(fileName, ".pdf");
        log.info("Create template : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        logger.info(String.format("Invoice pdf path : %s", pdfFile.getAbsolutePath()));
        try (FileOutputStream pos = new FileOutputStream(pdfFile)) {
            // Load invoice JRXML template.
            final JasperReport report = loadTemplate(orderId);
            log.info("Load template : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            ReportModel reportModel = constructPdfReportData(orderId);
            log.info("Construct response model : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            // Fill parameters map.
            final Map<String, Object> parameters = parameters1(reportModel);
            // Create an empty datasource.
            final JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(StringConstants.INVOICE));
            // Render the invoice as a PDF file.
            JasperReportsUtils.renderAsPdf(report, parameters, dataSource, pos);
            log.info("Render the invoice as a PDF file : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
            log.info("Generate invoice PDF ends.");
            return pdfFile;
        } catch (final Exception e) {
            logger.error(String.format("An error occured during PDF creation: %s", e));
            log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
            log.info("Generate invoice PDF ends.");
            throw new RuntimeException(e);
        }
    }

    public ReportModel constructPdfReportData(String orderId) {
        log.info("Construct PDF report data starts.");
        long apiStartTimeMillis = new Date().getTime();
        OrderManagement orderManagement = validationService.isValidOrder(orderId);
        log.info("Get order management : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        User user = orderManagement.getUser();
        Long id;
        String title;
        User instructor;
        Long duration;
        double price;
        String shortDescription;
        if (KeyConstants.KEY_PROGRAM.equalsIgnoreCase(orderManagement.getSubscriptionType().getName())) {
            Programs program = orderManagement.getProgram();
            id = program.getProgramId();
            title = program.getTitle();
            instructor = program.getOwner();
            duration = program.getDuration().getDuration();
            price = program.getProgramPrices().getPrice();
            shortDescription = program.getShortDescription();
        } else {
            SubscriptionPackage subscriptionPackage = orderManagement.getSubscriptionPackage();
            id = subscriptionPackage.getSubscriptionPackageId();
            title = subscriptionPackage.getTitle();
            instructor = subscriptionPackage.getOwner();
            duration = subscriptionPackage.getPackageDuration().getDuration();
            price = subscriptionPackage.getPrice();
            shortDescription = subscriptionPackage.getShortDescription();
        }
        log.info("Get program details : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        ReportModel reportModel = new ReportModel();
        reportModel.setAddressLine1("651 N Broad St.");
        reportModel.setAddressLine2("Suite 205, #600,");
        reportModel.setAddressLine3("Middletown, DE 19709");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM d,yyyy");
        String userTimeZone = fitwiseUtils.getUserTimeZone();
        if (userTimeZone != null) {
            TimeZone timeZone = TimeZone.getTimeZone(userTimeZone);
            simpleDateFormat.setTimeZone(timeZone);
        }
        reportModel.setInvoiceDate(simpleDateFormat.format(orderManagement.getCreatedDate()));
        InvoiceManagement invoiceManagement = invoiceManagementRepository.findByOrderManagement(orderManagement);
        reportModel.setInvoiceNumber(invoiceManagement.getInvoiceNumber());
        UserProfile userProfile = userProfileRepository.findByUserUserId(user.getUserId());
        reportModel.setBilledToName(userProfile.getFirstName() + " " + userProfile.getLastName());
        reportModel.setUserEmail(user.getEmail());
        reportModel.setOrderId(orderId);
        log.info("Construct report model : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        InstructorPayment instructorPayment = instructorPaymentRepository.findByOrderManagement(orderManagement);
        if (orderManagement.getSubscribedViaPlatform().getPlatformTypeId() == 2) {
            profilingEndTimeMillis = new Date().getTime();
            Programs program = orderManagement.getProgram();
            //Subscription via Apple
            reportModel.setOrderStatus(orderManagement.getOrderStatus());
            reportModel.setPaymentNoteAmount(KeyConstants.KEY_CURRENCY_US_DOLLAR + instructorPayment.getTotalAmt() + KeyConstants.KEY_SPACE);
            reportModel.setPaymentNote(KeyConstants.KEY_PAID_VIA_APPLE);
            //Auto Renew On /Off
            // ProgramSubscription subscribedProgram = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), program.getProgramId());
            ApplePayment applepayment = applePaymentRepository.findTop1ByOrderManagementOrderIdOrderByCreatedDateDesc(orderManagement.getOrderId());
            if (applepayment != null && applepayment.getTransactionStatus().equalsIgnoreCase(KeyConstants.KEY_PAYMENT_SUCCESS)) {
                reportModel.setTransactionId(applepayment.getTransactionId());
                AppleProductSubscription appleProductSubscription = appleProductSubscriptionRepository.findTop1ByTransactionIdOrderByModifiedDateDesc(applepayment.getTransactionId());
				/*if (appleSubscription != null) {
					ProgramSubscription subscribedProgram = programSubscriptionRepo
							.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(
									appleSubscription.getUser().getUserId(),
									appleSubscription.getProgram().getProgramId());
					if (subscribedProgram != null && subscribedProgram.isAutoRenewal()) {
						reportModel.setAutoRenewal(KeyConstants.KEY_YES);
					}
				}*/
                if (appleProductSubscription.getAppleSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_ACTIVE)) {
                    reportModel.setAutoRenewal(KeyConstants.KEY_YES);
                }
                //setNextRenewalOn                              
                if (reportModel.getAutoRenewal().equalsIgnoreCase(KeyConstants.KEY_YES)) {
                    SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-YYYY");
                    userTimeZone = fitwiseUtils.getUserTimeZone();
                    if (userTimeZone != null) {
                        TimeZone timeZone = TimeZone.getTimeZone(userTimeZone);
                        formatter.setTimeZone(timeZone);
                    }
                    String nextRenewalDateInString = formatter.format(applepayment.getExpiryDate());
                    reportModel.setNextRenewalOn(nextRenewalDateInString);
                }
            }
            int totalNumberOfRenewalsCount = 0;
            int currentSubscriptionRenewalsCount = 0;
            log.info("Order Created Date :{} " + orderManagement.getCreatedDate());
            List<AppleProductSubscription> initialSubscription = appleProductSubscriptionRepository.findByProgramAndUserAndEvent(program, user, NotificationConstants.INITIAL_BUY);
            List<String> statusList = Arrays.asList(new String[]{NotificationConstants.RENEWAL, NotificationConstants.INTERACTIVE_RENEWAL});
            List<AppleProductSubscription> iOSSubscriptions = appleProductSubscriptionRepository
                    .findByProgramAndUserAndEventInAndCreatedDateLessThanEqual(program, user, statusList, orderManagement.getCreatedDate());
            log.info("list Count :{}" + iOSSubscriptions.size());
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
            reportModel.setTotalRenewalCount(totalNumberOfRenewalsCount);
            reportModel.setCurrentSubscriptionRenewalCount(currentSubscriptionRenewalsCount);
            log.info("totalNumberOfRenewalsCount {}", totalNumberOfRenewalsCount);
            log.info("currentSubscriptionRenewalsCount {}", currentSubscriptionRenewalsCount);
            // Setting the program term - Duration of the program
            reportModel.setProgramTerm("30" + KeyConstants.DAYS);
            reportModel.setProgramDuration("1");
            log.info("Auto renew on / off for IOS : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
        } else {
            profilingEndTimeMillis = new Date().getTime();
            List<String> statusList = Arrays.asList(new String[]{KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING});
            //Adding a buffer time of 3 minutes to consider delay in populating SubscriptionAudit table
            LocalDateTime localDateTime = orderManagement.getCreatedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            localDateTime = localDateTime.plusMinutes(3);
            Date orderCreationDateWithBuffer = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            int totalNumberOfRenewalsCount = 0;
            int currentSubscriptionRenewalsCount = 0;
            if (KeyConstants.KEY_STRIPE.equals(orderManagement.getModeOfPayment())) {
                StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderId);
                if (stripePayment != null) {
                    reportModel.setProgramPrice(stripePayment.getAmountPaid().toString() + KeyConstants.KEY_SPACE + KeyConstants.KEY_CURRENCY_US_DOLLAR);
                    if (stripePayment.getTransactionStatus().equalsIgnoreCase(KeyConstants.KEY_PAID)) {
                        reportModel.setOrderStatus(KeyConstants.KEY_SUCCESS);
                    }
                    // Marking the transaction status as refund
                    if (stripePayment.getTransactionStatus().equalsIgnoreCase(KeyConstants.KEY_REFUND)) {
                        reportModel.setOrderStatus(KeyConstants.KEY_REFUNDED);
                    }
                    reportModel.setTransactionId(stripePayment.getChargeId());
                }
                // Setting the program term - Duration of the program
                reportModel.setProgramTerm(duration + KeyConstants.DAYS);
                /**
                 * Getting the recent ARB data from the subscription changes tracker table
                 * Setting whether auto-subscription is ON/OFF
                 */
                StripeSubscriptionChangesTracker stripeSubscriptionChangesTracker = stripeSubscriptionChangesTrackerRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
                if (stripeSubscriptionChangesTracker != null && stripeSubscriptionChangesTracker.getIsSubscriptionActive()) {
                    reportModel.setAutoRenewal(KeyConstants.KEY_YES);
                }
                //Setting the next renewal date
                if (reportModel.getAutoRenewal().equalsIgnoreCase(KeyConstants.KEY_YES)) {
                    Calendar cal = Calendar.getInstance();
                    Date subscribedDate = orderManagement.getCreatedDate();
                    // Adding program duration to the subscribed date to get the Next renewal date
                    cal.setTime(subscribedDate);
                    cal.add(Calendar.DATE, Math.toIntExact(duration));
                    Date nextRenewalDate = cal.getTime();
                    String nextRenewalDateInString = fitwiseUtils.formatDate(nextRenewalDate);
                    reportModel.setNextRenewalOn(nextRenewalDateInString); // Setting Next renewal date only if auto-subscription is ON
                }
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d,yyyy HH:mm:ss");
                userTimeZone = fitwiseUtils.getUserTimeZone();
                if (userTimeZone != null) {
                    TimeZone timeZone = TimeZone.getTimeZone(userTimeZone);
                    dateFormat.setTimeZone(timeZone);
                }
                if (stripePayment != null) {
                    String chargeId = stripePayment.getChargeId();
                    Stripe.apiKey = stripeProperties.getApiKey();
                    try {
                        Charge charge = Charge.retrieve(chargeId);
                        PaymentMethod.BillingDetails billingDetails = charge.getBillingDetails();
                        if (billingDetails != null && billingDetails.getAddress() != null && billingDetails.getAddress().getPostalCode() != null) {
                            reportModel.setUserPostalCode(billingDetails.getAddress().getPostalCode());
                            Charge.PaymentMethodDetails paymentMethodDetails = charge.getPaymentMethodDetails();
                            if (instructorPayment != null) {
                                reportModel.setPaymentNoteAmount(KeyConstants.KEY_CURRENCY_US_DOLLAR + instructorPayment.getTotalAmt() + KeyConstants.KEY_SPACE);
                            }
                            reportModel.setPaymentNote(KeyConstants.KEY_WAS_PAID_ON + dateFormat.format(orderManagement.getCreatedDate()) + KeyConstants.KEY_BY + paymentMethodDetails.getCard().getBrand() + KeyConstants.KEY_CARD_ENDING + paymentMethodDetails.getCard().getLast4());
                        }
                    } catch (StripeException e) {
                        e.printStackTrace();
                    }
                }

                /**
                 * Setting the total renewals count
                 * Calculating the total number of subscriptions for the program done by the user
                 * and the renewals for the current subscription
                 */
                if (stripeSubscriptionChangesTracker != null) {
                    List<StripePayment> stripePaymentList = stripePaymentRepository.findBySubscriptionIdAndTransactionStatusAndCreatedDateLessThanEqual(stripeSubscriptionChangesTracker.getSubscriptionId(), KeyConstants.KEY_PAID, stripeSubscriptionChangesTracker.getModifiedDate());
                    stripePaymentList = stripePaymentList.stream()
                            .collect(collectingAndThen(toCollection(() -> new TreeSet<StripePayment>(comparing(StripePayment::getInvoiceId))), ArrayList::new));
                    currentSubscriptionRenewalsCount = stripePaymentList.size();
                }

                //Calculating total renewals based on subscription type
                if (KeyConstants.KEY_PROGRAM.equalsIgnoreCase(orderManagement.getSubscriptionType().getName())) {
                    totalNumberOfRenewalsCount = subscriptionAuditRepo.countByUserUserIdAndSubscriptionTypeNameAndProgramSubscriptionProgramProgramIdAndSubscriptionStatusSubscriptionStatusNameInAndCreatedDateLessThanEqual(orderManagement.getUser().getUserId(), KeyConstants.KEY_PROGRAM, id, statusList, orderCreationDateWithBuffer);
                } else {
                    totalNumberOfRenewalsCount = subscriptionAuditRepo.countBySubscriptionTypeNameAndUserUserIdAndPackageSubscriptionSubscriptionPackageSubscriptionPackageIdAndSubscriptionStatusSubscriptionStatusNameInAndCreatedDateLessThanEqual(KeyConstants.KEY_SUBSCRIPTION_PACKAGE, orderManagement.getUser().getUserId(), id, statusList, orderCreationDateWithBuffer);
                }


            } else {
                // Authorize.net Payment gateway

                AuthNetPayment authNetPayment = authNetPaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderId);
                if (authNetPayment != null) {
                    if (authNetPayment.getAmountPaid() != null) {
                        reportModel.setProgramPrice(KeyConstants.KEY_CURRENCY_US_DOLLAR + "" + authNetPayment.getAmountPaid().toString());
                        reportModel.setInvoiceAmount(KeyConstants.KEY_CURRENCY_US_DOLLAR + authNetPayment.getAmountPaid().toString() + KeyConstants.KEY_SPACE
                                + KeyConstants.KEY_USD);
                        reportModel.setPaymentNoteAmount(KeyConstants.KEY_CURRENCY_US_DOLLAR + authNetPayment.getAmountPaid().toString() + KeyConstants.KEY_SPACE);
                    } else {
                        reportModel.setProgramPrice(price + KeyConstants.KEY_SPACE + KeyConstants.KEY_CURRENCY_US_DOLLAR);
                        reportModel.setProgramPrice(KeyConstants.KEY_CURRENCY_US_DOLLAR + "" + price);
                        reportModel.setInvoiceAmount(KeyConstants.KEY_CURRENCY_US_DOLLAR + price + KeyConstants.KEY_SPACE
                                + KeyConstants.KEY_USD);
                        reportModel.setPaymentNoteAmount(KeyConstants.KEY_CURRENCY_US_DOLLAR + price + KeyConstants.KEY_SPACE);
                    }
                    if (authNetPayment.getResponseCode().equalsIgnoreCase(KeyConstants.ANET_TRANSACTION_SUCCESS_RESPONSE_CODE)) {
                        reportModel.setOrderStatus(KeyConstants.KEY_SUCCESS);
                    }
                    // Marking the transaction status as refund
                    if (authNetPayment.getTransactionStatus().equalsIgnoreCase(KeyConstants.KEY_REFUND_INITIATED)) {
                        reportModel.setOrderStatus(KeyConstants.KEY_REFUNDED);
                    }
                    // Setting Transaction id
                    reportModel.setTransactionId(authNetPayment.getTransactionId());
                }
                // Setting the program term - Duration of the program
                reportModel.setProgramTerm(duration + KeyConstants.DAYS);

                /**
                 * Getting the recent ARB data from the subscription changes tracker table
                 * Setting whether auto-subscription is ON/OFF
                 */
                AuthNetSubscriptionChangesTracker tracker = authNetSubscriptionChangesTrackerRepository.findTop1ByOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
                if (tracker != null && tracker.getIsSubscriptionActive()) {
                    reportModel.setAutoRenewal(KeyConstants.KEY_YES);
                }
                ProgramSubscription programSubscription =
                        programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(orderManagement.getUser().getUserId(),
                                orderManagement.getProgram().getProgramId());
                if (programSubscription != null) {
                    //Setting the next renewal date
                    if (reportModel.getAutoRenewal().equalsIgnoreCase(KeyConstants.KEY_YES)) {
                        Calendar cal = Calendar.getInstance();
                        Date subscribedDate = programSubscription.getSubscribedDate();
                        // Adding program duration to the subscribed date to get the Next renewal date
                        cal.setTime(subscribedDate);
                        cal.add(Calendar.DATE, Math.toIntExact(duration));
                        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-YYYY");
                        userTimeZone = fitwiseUtils.getUserTimeZone();
                        if (userTimeZone != null) {
                            TimeZone timeZone = TimeZone.getTimeZone(userTimeZone);
                            formatter.setTimeZone(timeZone);
                        }
                        Date nextRenewalDate = cal.getTime();
                        String nextRenewalDateInString = formatter.format(nextRenewalDate);
                        reportModel.setNextRenewalOn(nextRenewalDateInString); // Setting Next renewal date only if auto-subscription is ON
                    }
                    // Getting customer billing information and last 4 card data using transaction id
                    GetTransactionDetailsResponse apiResponse = GetTransactionDetails.run(authNetPayment.getTransactionId());
                    CustomerAddressType billToAddress = null;
                    PaymentMaskedType paymentMaskedType = null;
                    if (apiResponse != null && apiResponse.getTransaction() != null) {
                        billToAddress = apiResponse.getTransaction().getBillTo();
                        paymentMaskedType = apiResponse.getTransaction().getPayment();
                    }
                    if (billToAddress != null) {
                        if (billToAddress.getAddress() != null && billToAddress.getZip() != null) {
                            reportModel.setUserPostalCode(billToAddress.getZip());
                        } else {
                            reportModel.setUserPostalCode(KeyConstants.KEY_HYPHEN);
                        }
                    } else {
                        reportModel.setUserPostalCode(KeyConstants.KEY_HYPHEN);
                    }
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d,yyyy HH:mm:ss");
                    userTimeZone = fitwiseUtils.getUserTimeZone();
                    if (userTimeZone != null) {
                        TimeZone timeZone = TimeZone.getTimeZone(userTimeZone);
                        dateFormat.setTimeZone(timeZone);
                    }
                    if (paymentMaskedType != null && paymentMaskedType.getCreditCard() != null) {
                        String maskedCardNumber = paymentMaskedType.getCreditCard().getCardNumber();
                        String lastFourDigitsOfCard = maskedCardNumber.substring(maskedCardNumber.length() - 4);
                        reportModel.setPaymentNote(KeyConstants.KEY_WAS_PAID_ON + dateFormat.format(orderManagement.getCreatedDate()) + KeyConstants.KEY_BY + paymentMaskedType.getCreditCard().getCardType() + KeyConstants.KEY_CARD_ENDING + lastFourDigitsOfCard);
                    } else {
                        reportModel.setPaymentNote(KeyConstants.KEY_WAS_PAID_ON + dateFormat.format(orderManagement.getCreatedDate()));
                    }
                }
                /**
                 * Setting the total renewals count
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

                totalNumberOfRenewalsCount = subscriptionAuditRepo.countByUserUserIdAndSubscriptionTypeNameAndProgramSubscriptionProgramProgramIdAndSubscriptionStatusSubscriptionStatusNameInAndCreatedDateLessThanEqual(orderManagement.getUser().getUserId(),KeyConstants.KEY_PROGRAM, orderManagement.getProgram().getProgramId(), statusList, orderCreationDateWithBuffer);

            }

            //Since the second time purchase will only be considered as a renewal, decreasing the total counts by 1
            if (totalNumberOfRenewalsCount != 0) {
                totalNumberOfRenewalsCount = totalNumberOfRenewalsCount - 1;
            }
            if (currentSubscriptionRenewalsCount != 0) {
                currentSubscriptionRenewalsCount = currentSubscriptionRenewalsCount - 1;
            }
            reportModel.setTotalRenewalCount(totalNumberOfRenewalsCount);
            reportModel.setCurrentSubscriptionRenewalCount(currentSubscriptionRenewalsCount);
            reportModel.setProgramDuration(duration.toString());
            log.info("Process refund : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
        }
        //Make sure payment term is set after renewal count calculation
        //Payment term is obtained from the number renewals occurred until the order creation time.
        String paymentTerm = KeyConstants.KEY_NEW;
        if (reportModel.getTotalRenewalCount() > 0) {
            paymentTerm = KeyConstants.KEY_RENEWAL;
        }
        reportModel.setInvoicePaymentTerms(paymentTerm);
        reportModel.setPurchasedDate(simpleDateFormat.format(orderManagement.getCreatedDate()));
        UserProfile instructorProfile = userProfileRepository.findByUserUserId(instructor.getUserId());
        reportModel.setInstructorName(instructorProfile.getFirstName() + KeyConstants.KEY_SPACE + instructorProfile.getLastName());
        reportModel.setProgramName(title);
        //commented this line & added in individual blocks. Since apple has default 1 month duration
        //reportModel.setProgramDuration(program.getDuration().getDuration().toString());
        if (shortDescription != null && !shortDescription.isEmpty()) {
            reportModel.setProgramDescription(shortDescription);
        } else {
            reportModel.setProgramDescription(KeyConstants.KEY_HYPHEN);
        }
        //Construct Discounts
        OfferCodeDetailAndOrderMapping offerCodeDetailAndOrderMapping = offerCodeDetailAndOrderMappingRepository.findTop1ByOrderManagementOrderByCreatedDateDesc(orderManagement);
        if (instructorPayment != null) {
            reportModel.setProgramPrice(KeyConstants.KEY_CURRENCY_US_DOLLAR + "" + instructorPayment.getPriceOnPurchase());
            reportModel.setInvoiceAmount(KeyConstants.KEY_CURRENCY_US_DOLLAR + instructorPayment.getPriceOnPurchase() + KeyConstants.KEY_SPACE
                    + KeyConstants.KEY_USD);
        }
        if (offerCodeDetailAndOrderMapping != null) {
            reportModel.setOfferName(offerCodeDetailAndOrderMapping.getOfferCodeDetail().getOfferName().trim());
            if (offerCodeDetailAndOrderMapping.getOfferCodeDetail().getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_FREE)) {
                reportModel.setOfferPrice(KeyConstants.KEY_CURRENCY_US_DOLLAR + "" + instructorPayment.getPriceOnPurchase());
                reportModel.setTotalAmount(KeyConstants.KEY_CURRENCY_US_DOLLAR + "0.00");
                reportModel.setPaymentNote("");
                reportModel.setPaymentNoteAmount("");
            } else {
                double offerPrice = instructorPayment.getPriceOnPurchase() - offerCodeDetailAndOrderMapping.getOfferCodeDetail().getOfferPrice().getPrice();
                DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
                reportModel.setOfferPrice(KeyConstants.KEY_CURRENCY_US_DOLLAR + "" + decimalFormat.format(offerPrice));
                reportModel.setTotalAmount(KeyConstants.KEY_CURRENCY_US_DOLLAR + "" + offerCodeDetailAndOrderMapping.getOfferCodeDetail().getOfferPrice().getPrice());
            }
        }
        log.info("Construct discount : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Construct PDF report data ends.");
        return reportModel;
    }


    // Fill template order params
    private Map<String, Object> parameters1(ReportModel report) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("logo", getClass().getResourceAsStream(logo_path));
        parameters.put("report", report);
        return parameters;
    }

    // Load invoice JRXML template
    private JasperReport loadTemplate(String orderId) throws JRException {

        OrderManagement orderManagement = validationService.isValidOrder(orderId);

        String invoiceTemplate;
        if (KeyConstants.KEY_PROGRAM.equalsIgnoreCase(orderManagement.getSubscriptionType().getName())) {
            invoiceTemplate = invoice_template;
        } else {
            invoiceTemplate = invoicePackageTemplate;
        }

        logger.info(String.format("Invoice template path : %s", invoiceTemplate));

        final InputStream reportInputStream = getClass().getResourceAsStream(invoiceTemplate);
        final JasperDesign jasperDesign = JRXmlLoader.load(reportInputStream);

        return JasperCompileManager.compileReport(jasperDesign);
    }


}
