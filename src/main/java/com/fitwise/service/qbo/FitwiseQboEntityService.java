package com.fitwise.service.qbo;

import com.fitwise.constants.*;
import com.fitwise.constants.discounts.DiscountsConstants;
import com.fitwise.entity.InstructorPayment;
import com.fitwise.entity.PlatformWiseTaxDetail;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.discounts.OfferCodeDetail;
import com.fitwise.entity.discounts.OfferCodeDetailAndOrderMapping;
import com.fitwise.entity.instructor.Tier;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.payments.appleiap.ApplePayment;
import com.fitwise.entity.payments.authNet.AuthNetPayment;
import com.fitwise.entity.payments.authNet.cardTypes.CardTypeWithProcessingCharge;
import com.fitwise.entity.payments.common.InvoiceManagement;
import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.payments.stripe.billing.StripePayment;
import com.fitwise.entity.qbo.*;
import com.fitwise.properties.StripeProperties;
import com.fitwise.repository.AppConfigKeyValueRepository;
import com.fitwise.repository.PlatformWiseTaxDetailRepository;
import com.fitwise.repository.discountsRepository.OfferCodeDetailAndOrderMappingRepository;
import com.fitwise.repository.order.InstructorPaymentRepository;
import com.fitwise.repository.payments.authnet.AuthNetPaymentRepository;
import com.fitwise.repository.payments.authnet.cardTypes.CardTypeWithProcessingChargeRepo;
import com.fitwise.repository.payments.stripe.billing.StripePaymentRepository;
import com.fitwise.repository.qbo.*;
import com.fitwise.service.admin.FitwiseShareService;
import com.fitwise.service.payment.authorizenet.PaymentService;
import com.fitwise.utils.payments.OrderNumberGenerator;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.BalanceTransaction;
import com.stripe.model.Charge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FitwiseQboEntityService {

    @Autowired
    private QboVendorRepository qboVendorRepository;

    @Autowired
    private QboCustomerRepository qboCustomerRepository;

    @Autowired
    private QboProductRepository qboProductRepository;

    @Autowired
    private InstructorPaymentRepository instructorPaymentRepository;

    @Autowired
    private PlatformWiseTaxDetailRepository platformWiseTaxDetailRepository;

    @Autowired
    private QboBillRepository qboBillRepository;

    @Autowired
    private QboDepositRepository qboDepositRepository;

    @Autowired
    private QboInvoiceRepository qboInvoiceRepository;

    @Autowired
    private QboPaymentRepository qboPaymentRepository;

    @Autowired
    private QboRefundRepository qboRefundRepository;

    @Autowired
    private QboVendorCreditRepository qboVendorCreditRepository;

    @Autowired
    private QboBillPaymentRepository qboBillPaymentRepository;

    @Autowired
    private QboRefundExpenseRepository qboRefundExpenseRepository;

    @Autowired
    private QBOService qboService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AuthNetPaymentRepository authNetPaymentRepository;

    @Autowired
    private CardTypeWithProcessingChargeRepo cardTypeWithProcessingChargeRepo;

    @Autowired
    private StripeProperties stripeProperties;

    @Autowired
    private StripePaymentRepository stripePaymentRepository;

    @Autowired
    OfferCodeDetailAndOrderMappingRepository offerCodeDetailAndOrderMappingRepository;

    @Autowired
    private QboBillPaymentBillPaidRepository qboBillPaymentBillPaidRepository;

    @Autowired
    private QboBillPaymentInsufficientBalanceRepository qboBillPaymentInsufficientBalanceRepository;

    @Autowired
    private QboDepositInsufficientBalanceRepository qboDepositInsufficientBalanceRepository;

    @Autowired
    private QboVendorBillPaymentRepository qboVendorBillPaymentRepository;

    private final FitwiseShareService fitwiseShareService;
    private final AppConfigKeyValueRepository appConfigKeyValueRepository;

    public void createOrUpdateQboUser(final User user, final String role){
        if(role.equalsIgnoreCase(SecurityFilterConstants.ROLE_INSTRUCTOR)){
            List<QboVendor> qboVendors = qboVendorRepository.findByUser(user);
            QboVendor qboVendor;
            if(qboVendors.isEmpty()){
                qboVendor = new QboVendor();
                qboVendor.setUser(user);
            }else{
                qboVendor = qboVendors.get(0);
            }
            qboVendor.setNeedUpdate(true);
            qboVendorRepository.save(qboVendor);
        }else if(role.equalsIgnoreCase(SecurityFilterConstants.ROLE_MEMBER)){
            List<QboCustomer> qboCustomers = qboCustomerRepository.findByUser(user);
            QboCustomer qboCustomer;
            if(qboCustomers.isEmpty()){
                qboCustomer = new QboCustomer();
                qboCustomer.setUser(user);
            }else{
                qboCustomer = qboCustomers.get(0);
            }
            qboCustomer.setNeedUpdate(true);
            qboCustomerRepository.save(qboCustomer);
        }
    }

    public void createOrUpdateQboProduct(Programs program){
        List<QboProduct> qboProducts = qboProductRepository.findByProgram(program);
        QboProduct qboProduct;
        if(qboProducts.isEmpty()){
            qboProduct = new QboProduct();
            qboProduct.setProgram(program);
        }else{
            qboProduct = qboProducts.get(0);
        }
        if(qboProduct.getSkuNumber() == null){
            qboProduct.setSkuNumber(OrderNumberGenerator.generateProductSKU("PGM"));
        }
        qboProduct.setNeedUpdate(true);
        qboProductRepository.save(qboProduct);
    }

    public void createOrUpdateQboProduct(SubscriptionPackage subscriptionPackage){
        List<QboProduct> qboProducts = qboProductRepository.findBySubscriptionPackage(subscriptionPackage);
        QboProduct qboProduct;
        if(qboProducts.isEmpty()){
            qboProduct = new QboProduct();
            qboProduct.setSubscriptionPackage(subscriptionPackage);
            qboProduct.setSkuNumber(OrderNumberGenerator.generateProductSKU("PKG"));
        }else{
            qboProduct = qboProducts.get(0);
        }
        qboProduct.setNeedUpdate(true);
        qboProductRepository.save(qboProduct);
        qboService.syncProducts();
    }

    public void createAndSyncQboInvoice(InvoiceManagement invoiceManagement) throws StripeException {
    	log.info("Step 25 -->");
        List<QboInvoice> qboInvoices = qboInvoiceRepository.findByInvoice(invoiceManagement);
        QboInvoice qboInvoice;
        if(qboInvoices.isEmpty()){
            qboInvoice = new QboInvoice();
        }else{
            qboInvoice = qboInvoices.get(0);
        }
        qboInvoice.setInvoice(invoiceManagement);
        qboInvoice.setNeedUpdate(true);
        qboInvoiceRepository.save(qboInvoice);
        Tier tier = invoiceManagement.getOrderManagement().getTier();
        if(tier == null) {
            InstructorPayment instructorPayment = createInstructorPayment(invoiceManagement.getOrderManagement());
            createAndSyncBill(instructorPayment);
        }
        log.info("Step 34.5 -->");
    }

    public void createAndSyncAnetPayment(AuthNetPayment authNetPayment) {
        List<QboPayment> qboPayments = qboPaymentRepository.findByAuthNetPayment(authNetPayment);
        QboPayment qboPayment;
        if(qboPayments.isEmpty()){
            qboPayment = new QboPayment();
        }else{
            qboPayment = qboPayments.get(0);
        }
        qboPayment.setAuthNetPayment(authNetPayment);
        qboPayment.setNeedUpdate(true);
        qboPaymentRepository.save(qboPayment);
        InstructorPayment instructorPayment = instructorPaymentRepository.findByOrderManagement(authNetPayment.getOrderManagement());
        createAndSyncDeposit(qboPayment,instructorPayment);
    }

    public void createAndSyncApplePayment(ApplePayment applePayment) {
        List<QboPayment> qboPayments = qboPaymentRepository.findByApplePayment(applePayment);
        QboPayment qboPayment;
        if(qboPayments.isEmpty()){
            qboPayment = new QboPayment();
        }else{
            qboPayment = qboPayments.get(0);
        }
        qboPayment.setApplePayment(applePayment);
        qboPayment.setNeedUpdate(true);
        qboPaymentRepository.save(qboPayment);
        InstructorPayment instructorPayment = instructorPaymentRepository.findByOrderManagement(applePayment.getOrderManagement());
        createAndSyncDeposit(qboPayment,instructorPayment);
    }

    public void createAndSyncStripePayment(StripePayment stripePayment) {
        List<QboPayment> qboPayments = qboPaymentRepository.findByStripePayment(stripePayment);
        QboPayment qboPayment;
        if(qboPayments.isEmpty()){
            qboPayment = new QboPayment();
        }else{
            qboPayment = qboPayments.get(0);
        }
        qboPayment.setStripePayment(stripePayment);
        qboPayment.setNeedUpdate(true);
        qboPaymentRepository.save(qboPayment);
        if(stripePayment.getOrderManagement().getTier() == null) {
            InstructorPayment instructorPayment = instructorPaymentRepository.findByOrderManagement(stripePayment.getOrderManagement());
            createAndSyncDeposit(qboPayment,instructorPayment);
            crateOrUpdateFitwiseBillPaidEntity(stripePayment.getOrderManagement());
        }
    }

    private void crateOrUpdateFitwiseBillPaidEntity(OrderManagement orderManagement) {
        List<QboBillPaymentForBillPaid> qboBillPayments = qboBillPaymentBillPaidRepository.findByOrderManagement(orderManagement);
        QboBillPaymentForBillPaid qboBillPaymentForBillPaid = null;
        if(qboBillPayments.isEmpty()){
            qboBillPaymentForBillPaid = new QboBillPaymentForBillPaid();
        }else{
            qboBillPaymentForBillPaid = qboBillPayments.get(0);
        }
        qboBillPaymentForBillPaid.setOrderManagement(orderManagement);
        qboBillPaymentForBillPaid.setNeedUpdate(true);
        qboBillPaymentBillPaidRepository.save(qboBillPaymentForBillPaid);
    }

    public void createAndSyncBill(InstructorPayment instructorPayment) {
    	log.info("Step 34 -->");
        List<QboBill> qboBills = qboBillRepository.findByInstructorPayment(instructorPayment);
        log.info("Step 34.1 -->");
        QboBill qboBill;
        if(qboBills.isEmpty()){
        	log.info("Step 34.2 -->");
            qboBill = new QboBill();
        }else{
        	log.info("Step 34.3 -->");
            qboBill = qboBills.get(0);
        }
        qboBill.setInstructorPayment(instructorPayment);
        qboBill.setNeedUpdate(true);
        qboBill.setBillPaid(false);
        qboBillRepository.save(qboBill);
        log.info("Step 34.4 -->");
    }

    public void createAndSyncDeposit(QboPayment qboPayment, InstructorPayment instructorPayment) {
        List<QboDeposit> qboDeposits = qboDepositRepository.findByQboPayment(qboPayment);
        QboDeposit qboDeposit;
        if(qboDeposits.isEmpty()){
            qboDeposit = new QboDeposit();
        }else{
            qboDeposit = qboDeposits.get(0);
        }
        qboDeposit.setQboPayment(qboPayment);
        qboDeposit.setInstructorPayment(instructorPayment);
        qboDeposit.setNeedUpdate(true);
        qboDepositRepository.save(qboDeposit);
    }

    public InstructorPayment createInstructorPayment(OrderManagement orderManagement) throws StripeException {
    	log.info("Step 26 -->");
        InstructorPayment instructorPayment = instructorPaymentRepository.findByOrderManagement(orderManagement);
        if(instructorPayment == null){
            instructorPayment = new InstructorPayment();
        }
        double priceOnPurchase;
        User instructor;
        double trainnrTax;
        double flatTax = Double.parseDouble(appConfigKeyValueRepository.findByKeyString(AppConfigConstants.ADMIN_CONFIG_FLAT_TAX).getValueString());
        if (KeyConstants.KEY_PROGRAM.equalsIgnoreCase(orderManagement.getSubscriptionType().getName())) {
        	log.info("Step 27 -->");
            priceOnPurchase = orderManagement.getProgram().getProgramPrices().getPrice();
            instructor = orderManagement.getProgram().getOwner();
            trainnrTax = fitwiseShareService.getFitwiseShare(instructor, KeyConstants.KEY_PROGRAM, orderManagement);
        } else {
        	log.info("Step 28 -->");
            priceOnPurchase = orderManagement.getSubscriptionPackage().getPrice();
            instructor = orderManagement.getSubscriptionPackage().getOwner();
            trainnrTax = fitwiseShareService.getFitwiseShare(instructor, KeyConstants.KEY_SUBSCRIPTION_PACKAGE, orderManagement);
        }
        instructorPayment.setInstructor(instructor);
        double totalAmt;
        OfferCodeDetail offerCodeDetail = null;
        OfferCodeDetailAndOrderMapping offerCodeDetailAndOrderMapping = offerCodeDetailAndOrderMappingRepository.findTop1ByOrderManagementOrderByCreatedDateDesc(orderManagement);
        if(offerCodeDetailAndOrderMapping != null) {
            offerCodeDetail = offerCodeDetailAndOrderMapping.getOfferCodeDetail();
            totalAmt = DiscountsConstants.MODE_FREE.equalsIgnoreCase(offerCodeDetail.getOfferMode()) ? 0.00 : offerCodeDetail.getOfferPrice().getPrice();
        } else {
            totalAmt = priceOnPurchase;
        }
        totalAmt += flatTax;
        instructorPayment.setBillNumber(OrderNumberGenerator.generateBillNumber());
        PlatformWiseTaxDetail platformWiseTaxDetail = platformWiseTaxDetailRepository.findByActiveAndPlatformType(true, orderManagement.getSubscribedViaPlatform());
        instructorPayment.setTotalAmt(totalAmt);
        instructorPayment.setFlatTax(flatTax);
        double fixedCharge = 0;
        instructorPayment.setVariableBillNumber(OrderNumberGenerator.generateBillNumber());
        if(offerCodeDetail != null && offerCodeDetail.getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_FREE)){
            instructorPayment.setProviderCharge(0);
        }else{
            if(orderManagement.getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_ANET)){
            	log.info("Step 29 -->");
                AuthNetPayment authNetPayment = authNetPaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
                double taxCalcPercentage = platformWiseTaxDetail.getCreditCardTaxPercentage();
                if(authNetPayment.getIsDomesticCard() != null && !authNetPayment.getIsDomesticCard()){
                    taxCalcPercentage = taxCalcPercentage + platformWiseTaxDetail.getCreditCardTaxNonDomesticAdditionalPercentage();
                }
                double creditCardTaxAmount = (taxCalcPercentage / 100) * totalAmt;
                instructorPayment.setProviderCharge(creditCardTaxAmount);
                fixedCharge = platformWiseTaxDetail.getCreditCardFixedCharges();
                instructorPayment.setFixedCharge(fixedCharge);
            }else if(orderManagement.getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_APPLE)){
                double appStoreTaxAmount = (platformWiseTaxDetail.getAppStoreTaxPercentage() / 100) * totalAmt;
                instructorPayment.setProviderCharge(appStoreTaxAmount);
            }else if(orderManagement.getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_STRIPE)){
            	log.info("Step 30 -->");
                Stripe.apiKey = stripeProperties.getApiKey();
                StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
                Charge charge = Charge.retrieve(stripePayment.getChargeId());
                String balanceTransactionId = charge.getBalanceTransaction();
                Stripe.apiKey = stripeProperties.getApiKey();
                try {
                    BalanceTransaction balanceTransaction = BalanceTransaction.retrieve(balanceTransactionId);
                    log.info("Step 31 -->");
                    //Getting fee as paise
                    double processingFees = ((double) balanceTransaction.getFee()) / 100;
                    instructorPayment.setProviderCharge(processingFees);
                } catch (StripeException exception) {
                    log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
                }
            }
        }
        double trainnrTaxAmount = 0.00;
        double instructorShare = 0.00;
        if (totalAmt > 0.00 && orderManagement.getTier() == null) {
        	log.info("Step 32 -->");
            trainnrTaxAmount = ((trainnrTax / 100) * totalAmt) + flatTax;
            instructorShare = totalAmt - (instructorPayment.getProviderCharge() + fixedCharge + trainnrTaxAmount);
        }
        log.info("instructorShare -------------> {}", instructorShare);
        log.info("trainnrTaxAmount -------------> {}", trainnrTaxAmount);
        instructorPayment.setFitwiseShare(trainnrTaxAmount);
        instructorPayment.setInstructorShare(instructorShare);
        instructorPayment.setOrderManagement(orderManagement);
        instructorPayment.setFixedBillNumber(OrderNumberGenerator.generateBillNumber());
        instructorPayment.setDueDate(paymentService.dueDateLogicCalculator(orderManagement.getOrderId()));
        instructorPayment.setPriceOnPurchase(priceOnPurchase);
        instructorPaymentRepository.save(instructorPayment);
        log.info("Step 33 -->");
        return instructorPayment;
    }

    public void createAndSyncAnetRefund(AuthNetPayment authNetPayment) {
        createAndSyncVendorCredit(authNetPayment.getOrderManagement());
        List<QboRefund> qboRefunds = qboRefundRepository.findByAuthNetPayment(authNetPayment);
        QboRefund qboRefund;
        if(qboRefunds.isEmpty()){
            qboRefund = new QboRefund();
        }else{
            qboRefund = qboRefunds.get(0);
        }
        qboRefund.setAuthNetPayment(authNetPayment);
        qboRefund.setNeedUpdate(true);
        qboRefundRepository.save(qboRefund);
    }

    public void createAndSyncStripeRefund(StripePayment stripePayment) {
        InstructorPayment instructorPayment = instructorPaymentRepository.findByOrderManagement(stripePayment.getOrderManagement());
        List<QboVendorBillPayment> qboVendorBillPayments = qboVendorBillPaymentRepository.findByQboBillInstructorPaymentOrderManagement(instructorPayment.getOrderManagement());
        if(qboVendorBillPayments.isEmpty()){
            List<QboRefund> qboRefunds = qboRefundRepository.findByStripePayment(stripePayment);
            QboRefund qboRefund;
            if(qboRefunds.isEmpty()){
                qboRefund = new QboRefund();
            }else{
                qboRefund = qboRefunds.get(0);
            }
            qboRefund.setStripePayment(stripePayment);
            qboRefund.setNeedUpdate(true);
            qboRefundRepository.save(qboRefund);
            createAndSyncVendorCredit(stripePayment.getOrderManagement());
            createAndSyncBillPayment(stripePayment.getOrderManagement());
            createAndSyncExpenseOnRefund(stripePayment.getOrderManagement());
        }else{
            createAndSyncVendorCredit(stripePayment.getOrderManagement());
        }
    }

    public void createAndSyncAppleRefund(ApplePayment applePayment) {
        InstructorPayment instructorPayment = instructorPaymentRepository.findByOrderManagement(applePayment.getOrderManagement());
        List<QboVendorBillPayment> qboVendorBillPayments = qboVendorBillPaymentRepository.findByQboBillInstructorPaymentOrderManagement(instructorPayment.getOrderManagement());
        if(qboVendorBillPayments.isEmpty()) {
            List<QboRefund> qboRefunds = qboRefundRepository.findByApplePayment(applePayment);
            QboRefund qboRefund;
            if(qboRefunds.isEmpty()){
                qboRefund = new QboRefund();
            }else{
                qboRefund = qboRefunds.get(0);
            }
            qboRefund.setApplePayment(applePayment);
            qboRefund.setNeedUpdate(true);
            qboRefundRepository.save(qboRefund);
            createAndSyncVendorCredit(applePayment.getOrderManagement());
            createAndSyncBillPayment(applePayment.getOrderManagement());
            createAndSyncExpenseOnRefund(applePayment.getOrderManagement());
        } else {
            createAndSyncVendorCredit(applePayment.getOrderManagement());
        }
    }

    public void createAndSyncVendorCredit(OrderManagement orderManagement){
        List<QboVendorCredit> qboVendorCredits = qboVendorCreditRepository.findByOrderManagement(orderManagement);
        QboVendorCredit qboVendorCredit;
        if(qboVendorCredits.isEmpty()){
            qboVendorCredit = new QboVendorCredit();
        }else{
            qboVendorCredit = qboVendorCredits.get(0);
        }
        qboVendorCredit.setOrderManagement(orderManagement);
        qboVendorCredit.setNeedUpdate(true);
        qboVendorCreditRepository.save(qboVendorCredit);
    }

    public void createAndSyncBillPayment(OrderManagement orderManagement){
        List<QboBillPayment> qboBillPayments = qboBillPaymentRepository.findByOrderManagement(orderManagement);
        QboBillPayment qboBillPayment;
        if(qboBillPayments.isEmpty()){
            qboBillPayment = new QboBillPayment();
        }else{
            qboBillPayment = qboBillPayments.get(0);
        }
        qboBillPayment.setOrderManagement(orderManagement);
        qboBillPayment.setNeedUpdate(true);
        qboBillPaymentRepository.save(qboBillPayment);
    }

    private void createAndSyncExpenseOnRefund(OrderManagement orderManagement) {
        List<QboRefundExpense> qboRefundExpenses = qboRefundExpenseRepository.findByOrderManagement(orderManagement);
        QboRefundExpense qboRefundExpense;
        if(qboRefundExpenses.isEmpty()){
            qboRefundExpense = new QboRefundExpense();
        }else{
            qboRefundExpense = qboRefundExpenses.get(0);
        }
        qboRefundExpense.setOrderManagement(orderManagement);
        qboRefundExpense.setNeedUpdate(true);
        qboRefundExpenseRepository.save(qboRefundExpense);
    }

    public InstructorPayment updateInstructorPayment(InstructorPayment instructorPayment, String cardType) {
        CardTypeWithProcessingCharge cardTypeWithProcessingCharge = cardTypeWithProcessingChargeRepo.findByCardType(cardType);
        OrderManagement orderManagement = instructorPayment.getOrderManagement();
        if(orderManagement.getSubscribedViaPlatform().getPlatform().equalsIgnoreCase(DBConstants.ANDROID) || orderManagement.getSubscribedViaPlatform().getPlatform().equalsIgnoreCase(DBConstants.WEB)){
            AuthNetPayment authNetPayment = authNetPaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
            double taxCalcPercentage = cardTypeWithProcessingCharge.getDomesticProcessingPercentage();
            if(authNetPayment.getIsDomesticCard() != null && !authNetPayment.getIsDomesticCard()){
                taxCalcPercentage = taxCalcPercentage + cardTypeWithProcessingCharge.getInternationalProcessingPercentage();
            }
            double creditCardTaxAmount = (taxCalcPercentage / 100) * orderManagement.getProgram().getProgramPrices().getPrice();
            creditCardTaxAmount += cardTypeWithProcessingCharge.getAdditionalCharge();
            instructorPayment.setProviderCharge(creditCardTaxAmount);
        }
        double instructorShare = instructorPayment.getTotalAmt() - (instructorPayment.getProviderCharge() + instructorPayment.getFixedCharge() + instructorPayment.getFitwiseShare());
        instructorPayment.setInstructorShare(instructorShare);
        instructorPayment.setCardType(cardType);
        instructorPaymentRepository.save(instructorPayment);
        return instructorPayment;
    }

    public void createAndSyncPaymentInsufficientBalance(OrderManagement orderManagement, Double debitAmount){
        List<QboDepositInsufficientBalance> qboDeposits = qboDepositInsufficientBalanceRepository.findByOrderManagement(orderManagement);
        QboDepositInsufficientBalance qboDeposit;
        if(qboDeposits.isEmpty()){
            qboDeposit = new QboDepositInsufficientBalance();
        }else{
            qboDeposit = qboDeposits.get(0);
        }
        qboDeposit.setOrderManagement(orderManagement);
        qboDeposit.setDebitAmount(debitAmount);
        qboDeposit.setNeedUpdate(true);
        qboDepositInsufficientBalanceRepository.save(qboDeposit);
        List<QboBillPaymentInsufficientBalance> qboBillPayments = qboBillPaymentInsufficientBalanceRepository.findByOrderManagement(orderManagement);
        QboBillPaymentInsufficientBalance qboBillPayment;
        if(qboBillPayments.isEmpty()){
            qboBillPayment = new QboBillPaymentInsufficientBalance();
        }else{
            qboBillPayment = qboBillPayments.get(0);
        }
        qboBillPayment.setOrderManagement(orderManagement);
        qboBillPayment.setDebitAmount(debitAmount);
        qboBillPayment.setDepositInsufficientBalance(qboDeposit);
        qboBillPayment.setNeedUpdate(true);
        qboBillPaymentInsufficientBalanceRepository.save(qboBillPayment);
    }
}