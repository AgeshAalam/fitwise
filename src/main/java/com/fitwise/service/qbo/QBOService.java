package com.fitwise.service.qbo;

import com.fitwise.constants.AppConfigConstants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.PaymentConstants;
import com.fitwise.constants.QboConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.TaxConstants;
import com.fitwise.constants.discounts.DiscountsConstants;
import com.fitwise.encryption.AESEncryption;
import com.fitwise.entity.InstructorPayment;
import com.fitwise.entity.PlatformType;
import com.fitwise.entity.Programs;
import com.fitwise.entity.TaxId;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.discounts.OfferCodeDetailAndOrderMapping;
import com.fitwise.entity.instructor.Tier;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.payments.appleiap.ApplePayment;
import com.fitwise.entity.payments.authNet.AuthNetPayment;
import com.fitwise.entity.payments.common.InvoiceManagement;
import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.payments.stripe.billing.StripePayment;
import com.fitwise.entity.qbo.QboBill;
import com.fitwise.entity.qbo.QboBillPayment;
import com.fitwise.entity.qbo.QboBillPaymentForBillPaid;
import com.fitwise.entity.qbo.QboBillPaymentInsufficientBalance;
import com.fitwise.entity.qbo.QboCustomer;
import com.fitwise.entity.qbo.QboDeposit;
import com.fitwise.entity.qbo.QboDepositInsufficientBalance;
import com.fitwise.entity.qbo.QboInvoice;
import com.fitwise.entity.qbo.QboPayment;
import com.fitwise.entity.qbo.QboProduct;
import com.fitwise.entity.qbo.QboProductCategory;
import com.fitwise.entity.qbo.QboRefund;
import com.fitwise.entity.qbo.QboRefundExpense;
import com.fitwise.entity.qbo.QboVendor;
import com.fitwise.entity.qbo.QboVendorBillPayment;
import com.fitwise.entity.qbo.QboVendorCredit;
import com.fitwise.properties.GeneralProperties;
import com.fitwise.properties.QboProperties;
import com.fitwise.repository.AppConfigKeyValueRepository;
import com.fitwise.repository.PlatformTypeRepository;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.TaxRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.discountsRepository.OfferCodeDetailAndOrderMappingRepository;
import com.fitwise.repository.instructor.TierRepository;
import com.fitwise.repository.order.InstructorPaymentRepository;
import com.fitwise.repository.order.InvoiceManagementRepository;
import com.fitwise.repository.packaging.SubscriptionPackageRepository;
import com.fitwise.repository.payments.authnet.AuthNetPaymentRepository;
import com.fitwise.repository.payments.stripe.billing.StripePaymentRepository;
import com.fitwise.repository.qbo.QboBillPaymentBillPaidRepository;
import com.fitwise.repository.qbo.QboBillPaymentInsufficientBalanceRepository;
import com.fitwise.repository.qbo.QboBillPaymentRepository;
import com.fitwise.repository.qbo.QboBillRepository;
import com.fitwise.repository.qbo.QboCustomerRepository;
import com.fitwise.repository.qbo.QboDepositInsufficientBalanceRepository;
import com.fitwise.repository.qbo.QboDepositRepository;
import com.fitwise.repository.qbo.QboInvoiceRepository;
import com.fitwise.repository.qbo.QboPaymentRepository;
import com.fitwise.repository.qbo.QboProductCategoryRepository;
import com.fitwise.repository.qbo.QboProductRepository;
import com.fitwise.repository.qbo.QboRefundExpenseRepository;
import com.fitwise.repository.qbo.QboRefundRepository;
import com.fitwise.repository.qbo.QboVendorBillPaymentRepository;
import com.fitwise.repository.qbo.QboVendorCreditRepository;
import com.fitwise.repository.qbo.QboVendorRepository;
import com.fitwise.service.RedisService;
import com.fitwise.service.payment.authorizenet.PaymentService;
import com.fitwise.utils.mail.AsyncMailer;
import com.fitwise.utils.payments.OrderNumberGenerator;
import com.fitwise.view.qbo.Entity;
import com.intuit.ipp.core.Context;
import com.intuit.ipp.core.IEntity;
import com.intuit.ipp.core.ServiceType;
import com.intuit.ipp.data.Account;
import com.intuit.ipp.data.AccountBasedExpenseLineDetail;
import com.intuit.ipp.data.Bill;
import com.intuit.ipp.data.BillPayment;
import com.intuit.ipp.data.BillPaymentCheck;
import com.intuit.ipp.data.BillPaymentTypeEnum;
import com.intuit.ipp.data.BillableStatusEnum;
import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.Deposit;
import com.intuit.ipp.data.DepositLineDetail;
import com.intuit.ipp.data.DiscountLineDetail;
import com.intuit.ipp.data.EmailAddress;
import com.intuit.ipp.data.Error;
import com.intuit.ipp.data.Invoice;
import com.intuit.ipp.data.Item;
import com.intuit.ipp.data.ItemBasedExpenseLineDetail;
import com.intuit.ipp.data.ItemTypeEnum;
import com.intuit.ipp.data.Line;
import com.intuit.ipp.data.LineDetailTypeEnum;
import com.intuit.ipp.data.LinkedTxn;
import com.intuit.ipp.data.Payment;
import com.intuit.ipp.data.PaymentTypeEnum;
import com.intuit.ipp.data.Purchase;
import com.intuit.ipp.data.ReferenceType;
import com.intuit.ipp.data.RefundReceipt;
import com.intuit.ipp.data.SalesItemLineDetail;
import com.intuit.ipp.data.TelephoneNumber;
import com.intuit.ipp.data.Vendor;
import com.intuit.ipp.data.VendorCredit;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.exception.InvalidTokenException;
import com.intuit.ipp.security.OAuth2Authorizer;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.services.QueryResult;
import com.intuit.ipp.util.Config;
import com.intuit.oauth2.client.OAuth2PlatformClient;
import com.intuit.oauth2.data.BearerTokenResponse;
import com.intuit.oauth2.exception.OAuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service with process all the qbo queries for fitwise
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class QBOService {

    @Autowired
    ClientFactoryService factory;

    @Autowired
    UserProfileRepository userProfileRepository;

    @Autowired
    QboVendorRepository qboVendorRepository;

    @Autowired
    QboCustomerRepository qboCustomerRepository;

    @Autowired
    QboProductRepository qboProductRepository;

    @Autowired
    QboProductCategoryRepository qboProductCategoryRepository;

    @Autowired
    ProgramRepository programRepository;

    @Autowired
    TaxRepository taxRepository;

    @Autowired
    QboInvoiceRepository qboInvoiceRepository;

    @Autowired
    InvoiceManagementRepository invoiceManagementRepository;

    @Autowired
    QboPaymentRepository qboPaymentRepository;

    @Autowired
    RedisService redisService;

    @Autowired
    AsyncMailer asyncMailer;

    @Autowired
    GeneralProperties generalProperties;

    @Autowired
    PlatformTypeRepository platformTypeRepository;

    @Autowired
    QboBillRepository qboBillRepository;

    @Autowired
    InstructorPaymentRepository instructorPaymentRepository;

    @Autowired
    QboDepositRepository qboDepositRepository;

    @Autowired
    QboRefundRepository qboRefundRepository;

    @Autowired
    QboVendorCreditRepository qboVendorCreditRepository;

    @Autowired
    QboBillPaymentRepository qboBillPaymentRepository;

    @Autowired
    QboRefundExpenseRepository qboRefundExpenseRepository;

    @Autowired
    FitwiseQboEntityService fitwiseQboEntityService;

    @Autowired
    QboVendorBillPaymentRepository qboVendorBillPaymentRepository;

    @Autowired
    QboProperties qboProperties;

    @Autowired
    AuthNetPaymentRepository authNetPaymentRepository;

    @Autowired
    PaymentService paymentService;

    @Autowired
    StripePaymentRepository stripePaymentRepository;

    @Autowired
    OfferCodeDetailAndOrderMappingRepository offerCodeDetailAndOrderMappingRepository;

    @Autowired
    QboBillPaymentBillPaidRepository qboBillPaymentBillPaidRepository;

    @Autowired
    QboBillPaymentInsufficientBalanceRepository qboBillPaymentInsufficientBalanceRepository;

    @Autowired
    QboDepositInsufficientBalanceRepository qboDepositInsufficientBalanceRepository;

    @Autowired
    SubscriptionPackageRepository subscriptionPackageRepository;

    @Autowired
    AESEncryption aesEncryption;
    private final TierRepository tierRepository;
    private final AppConfigKeyValueRepository appConfigKeyValueRepository;

    /**
     * Getting the data service for the qbo interaction
     * @param realmId QBO realm Id
     * @param accessToken QBO access token
     * @return DataService
     * @throws FMSException FMS Exception
     */
    public DataService getDataService(String realmId, String accessToken) throws FMSException {
        String url = factory.getPropertyValue("IntuitAccountingAPIHost") + "/v3/company";
        Config.setProperty(Config.BASE_URL_QBO, url);
        OAuth2Authorizer oauth = new OAuth2Authorizer(accessToken);
        Context context = new Context(oauth, ServiceType.QBO, realmId);
        return new DataService(context);
    }

    /**
     * Queries data from QuickBooks
     * @param sql Query
     * @return IEntity
     */
    public List<? extends IEntity> queryData(String sql) throws OAuthException, FMSException {
        String realmId = redisService.get(QboConstants.KEY_REALM_ID);
        if (StringUtils.isEmpty(realmId)) {
            log.error("Realm id is null ");
        }
        String accessToken = redisService.get(QboConstants.KEY_ACCESS_TOKEN);
        try {
            DataService service = getDataService(realmId, accessToken);
            QueryResult queryResult = service.executeQuery(sql);
            return queryResult.getEntities();
        } catch (InvalidTokenException e) {
            log.error("Error while calling executeQuery :: " + e.getMessage());
            log.info("received 401 during company info call, refreshing tokens now");
            OAuth2PlatformClient client  = factory.getOAuth2PlatformClient();
            String refreshToken = redisService.get(QboConstants.KEY_REFRESH_TOKEN);
            try {
                BearerTokenResponse bearerTokenResponse = client.refreshToken(refreshToken);
                redisService.set(QboConstants.KEY_ACCESS_TOKEN,bearerTokenResponse.getAccessToken());
                redisService.set(QboConstants.KEY_REFRESH_TOKEN, bearerTokenResponse.getRefreshToken());
                log.info("calling company info using new tokens");
                DataService service = getDataService(realmId, accessToken);
                QueryResult queryResult = service.executeQuery(sql);
                return queryResult.getEntities();
            } catch (OAuthException e1) {
                log.error("Error while calling bearer token :: " + e.getMessage());
                sendEmailAuthFailure();
                throw e1;
            } catch (FMSException e1) {
                log.error("Error while calling company currency :: " + e.getMessage());
                throw e1;
            }
        } catch (FMSException e) {
            List<Error> list = e.getErrorList();
            list.forEach(error -> log.error("Error while calling executeQuery :: " + error.getMessage()));
            refreshToken();
            throw e;
        }
    }

    /**
     * Creating the QBO entity
     * @param entity Generic Entity
     * @param <T> Generic Type
     * @return Generic Entity
     */
    public <T extends IEntity> T createEntity(T entity) throws OAuthException, FMSException {
        try{
            DataService dataService = getDataService(redisService.get(QboConstants.KEY_REALM_ID), redisService.get(QboConstants.KEY_ACCESS_TOKEN));
            entity = dataService.add(entity);
        }catch (InvalidTokenException e) {
            log.error("Error while calling executeQuery :: " + e.getMessage());
            log.info("received 401 during company info call, refreshing tokens now");
            OAuth2PlatformClient client  = factory.getOAuth2PlatformClient();
            String refreshToken = redisService.get(QboConstants.KEY_REFRESH_TOKEN);
            try {
                BearerTokenResponse bearerTokenResponse = client.refreshToken(refreshToken);
                redisService.set(QboConstants.KEY_ACCESS_TOKEN,bearerTokenResponse.getAccessToken());
                redisService.set(QboConstants.KEY_REFRESH_TOKEN, bearerTokenResponse.getRefreshToken());
                log.info("calling company info using new tokens");
                DataService service = getDataService(redisService.get(QboConstants.KEY_REALM_ID), redisService.get(QboConstants.KEY_ACCESS_TOKEN));
                entity = service.add(entity);
                return entity;
            } catch (OAuthException e1) {
                log.error("Error while calling bearer token :: " + e.getMessage());
                sendEmailAuthFailure();
                throw e1;
            } catch (FMSException e1) {
                log.error("Error while calling company currency :: " + e.getMessage());
                throw e1;
            }
        } catch (FMSException e) {
            List<Error> list = e.getErrorList();
            list.forEach(error -> log.error("Error while calling executeQuery :: " + error.getMessage()));
            refreshToken();
            throw e;
        }
        return entity;
    }

    /**
     * Updating the QBO entity
     * @param entity Generic entity
     * @param <T> Generic type
     * @return entity Generic entity
     * @throws FMSException Represents all intuit sdk exceptions
     */
    public <T extends IEntity> T updateEntity(T entity) throws FMSException, OAuthException {
        try{
            DataService dataService = getDataService(redisService.get(QboConstants.KEY_REALM_ID), redisService.get(QboConstants.KEY_ACCESS_TOKEN));
            entity = dataService.update(entity);
        }catch (InvalidTokenException e) {
            log.error("Error while calling executeQuery :: " + e.getMessage());
            log.info("received 401 during company info call, refreshing tokens now");
            OAuth2PlatformClient client  = factory.getOAuth2PlatformClient();
            String refreshToken = redisService.get(QboConstants.KEY_REFRESH_TOKEN);
            try {
                BearerTokenResponse bearerTokenResponse = client.refreshToken(refreshToken);
                redisService.set(QboConstants.KEY_ACCESS_TOKEN,bearerTokenResponse.getAccessToken());
                redisService.set(QboConstants.KEY_REFRESH_TOKEN, bearerTokenResponse.getRefreshToken());
                log.info("calling company info using new tokens");
                DataService service = getDataService(redisService.get(QboConstants.KEY_REALM_ID), redisService.get(QboConstants.KEY_ACCESS_TOKEN));
                entity = service.update(entity);
                return entity;
            } catch (OAuthException e1) {
                log.error("Error while calling bearer token :: " + e.getMessage());
                sendEmailAuthFailure();
                throw e1;
            } catch (FMSException e1) {
                log.error("Error while calling company currency :: " + e.getMessage());
                throw e1;
            }
        } catch (FMSException e) {
            List<Error> list = e.getErrorList();
            list.forEach(error -> log.error("Error while calling executeQuery :: " + error.getMessage()));
            refreshToken();
            throw e;
        }
        return entity;
    }

    /**
     * Find the entity for the given entity with the id
     * @param entity Generic entity
     * @param <T> Generic type
     * @return Generic entity
     */
    public <T extends IEntity> T findEntity(T entity) throws OAuthException, FMSException {
        try{
            DataService dataService = getDataService(redisService.get(QboConstants.KEY_REALM_ID), redisService.get(QboConstants.KEY_ACCESS_TOKEN));
            entity = dataService.findById(entity);
        }catch (InvalidTokenException e) {
            log.error("Error while calling executeQuery :: " + e.getMessage());
            log.info("received 401 during company info call, refreshing tokens now");
            OAuth2PlatformClient client  = factory.getOAuth2PlatformClient();
            String refreshToken = redisService.get(QboConstants.KEY_REFRESH_TOKEN);
            try {
                BearerTokenResponse bearerTokenResponse = client.refreshToken(refreshToken);
                redisService.set(QboConstants.KEY_ACCESS_TOKEN,bearerTokenResponse.getAccessToken());
                redisService.set(QboConstants.KEY_REFRESH_TOKEN, bearerTokenResponse.getRefreshToken());
                log.info("calling company info using new tokens");
                DataService service = getDataService(redisService.get(QboConstants.KEY_REALM_ID), redisService.get(QboConstants.KEY_ACCESS_TOKEN));
                entity = service.findById(entity);
                return entity;
            } catch (OAuthException e1) {
                log.error("Error while calling bearer token :: " + e.getMessage());
                sendEmailAuthFailure();
                throw e1;
            } catch (FMSException e1) {
                log.error("Error while calling company currency :: " + e.getMessage());
                throw e1;
            }
        } catch (FMSException e) {
            refreshToken();
            throw e;
        }
        return entity;
    }

    /**
     * Sync the users who all are created in fitwise as member and instructor
     */
    @Async("threadPoolTaskExecutor")
    public void syncUsers(){
        log.info("User sync to QBO start");
        List<QboVendor> qboVendors = qboVendorRepository.findByNeedUpdate(true);
        for(QboVendor qboVendor : qboVendors){
            try{
                UserProfile userProfile = userProfileRepository.findByUser(qboVendor.getUser());
                if(userProfile == null){
                    qboVendor.setNeedUpdate(false);
                    qboVendor.setUpdateStatus(MessageConstants.MSG_USR_PROFILE_NOT_FOUND);
                    qboVendorRepository.save(qboVendor);
                    continue;
                }
                if(qboVendor.getUser().getEmail().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)){
                    qboVendor.setNeedUpdate(false);
                    qboVendor.setUpdateStatus(MessageConstants.MSG_USR_ACCOUNT_DELETED);
                    qboVendorRepository.save(qboVendor);
                    continue;
                }
                Vendor vendor = new Vendor();
                if(qboVendor.getVendorId() != null){
                    vendor.setId(qboVendor.getVendorId());
                    vendor = findEntity(vendor);
                    if(vendor == null || vendor.getId() == null){
                        vendor = new Vendor();
                    }
                }
                Account expensePaymentAccount = findAccountByName(QboConstants.ACCOUNT_EXPENSE_INSTRUCTOR_PAYMENTS);
                if(expensePaymentAccount == null){
                    qboVendor.setUpdateStatus(QboConstants.MSG_EXPENSE_ACCOUNT_MISSING);
                    qboVendorRepository.save(qboVendor);
                    continue;
                }
                ReferenceType defaultExpenseAccountReferenceType = new ReferenceType();
                defaultExpenseAccountReferenceType.setValue(expensePaymentAccount.getId());
                vendor.setAPAccountRef(defaultExpenseAccountReferenceType);
                log.info("Expense account Id " + vendor.getAPAccountRef().getValue());
                TaxId taxId = taxRepository.findByUserUserId(qboVendor.getUser().getUserId());
                if (taxId != null) {
                    String encryptedTaxNo = taxId.getTaxNumber();
                    String originalTaxNo = "";
                    try{
                        originalTaxNo = aesEncryption.decrypt(encryptedTaxNo);
                    }catch (Exception e){
                        log.info("Decryption failed" + e.getMessage());
                    }
                    if (!StringUtils.isEmpty(originalTaxNo)) {
                        String taxNumber;
                        if (taxId.getTaxTypes().getTaxType().equalsIgnoreCase(TaxConstants.TAX_SSN)) {
                            taxNumber = originalTaxNo.substring(0, 2) + "-" + originalTaxNo.substring(2);
                        } else {
                            taxNumber = originalTaxNo.substring(0, 3) + "-" + originalTaxNo.substring(3, 5) + "-" + originalTaxNo.substring(5);
                        }
                        vendor.setTaxIdentifier(taxNumber);
                    } else {
                        vendor.setTaxIdentifier("");
                    }
                }else {
                    vendor.setTaxIdentifier("");
                }

                vendor.setVendor1099(true);
                TelephoneNumber telephoneNumber = new TelephoneNumber();
                if(!StringUtils.isEmpty(userProfile.getContactNumber())){
                    telephoneNumber.setFreeFormNumber(userProfile.getContactNumber());
                }else{
                    telephoneNumber.setFreeFormNumber("");
                }
                vendor.setMobile(telephoneNumber);
                if(vendor.getId() == null){
                    EmailAddress emailAddress = new EmailAddress();
                    emailAddress.setAddress(qboVendor.getUser().getEmail());
                    vendor.setPrimaryEmailAddr(emailAddress);
                    vendor.setGivenName(userProfile.getFirstName());
                    vendor.setFamilyName(userProfile.getLastName());
                    String displayName = getDisplayName(userProfile, "V");
                    vendor.setDisplayName(displayName);
                    qboVendor.setDisplayName(displayName);
                    vendor = createEntity(vendor);
                    if(vendor.getId() == null){
                        qboVendor.setUpdateStatus(QboConstants.MSG_ENTITY_CREATE_FAILURE);
                        qboVendorRepository.save(qboVendor);
                        continue;
                    }
                    qboVendor.setVendorId(vendor.getId());
                } else {
                    if(vendor.getGivenName() == null || (vendor.getGivenName() != null && !vendor.getGivenName().equalsIgnoreCase(userProfile.getFirstName())) || (vendor.getFamilyName() != null && !vendor.getFamilyName().equalsIgnoreCase(userProfile.getLastName()))){
                        vendor.setGivenName(userProfile.getFirstName());
                        vendor.setFamilyName(userProfile.getLastName());
                        String displayName = getDisplayName(userProfile, "V");
                        vendor.setDisplayName(displayName);
                        qboVendor.setDisplayName(displayName);
                    }
                    updateEntity(vendor);
                    if(qboVendor.getIsBillCreated() == null || !qboVendor.getIsBillCreated()){
                        qboVendor.setIsBillCreated(true);
                    }
                }
                qboVendor.setNeedUpdate(false);
                qboVendor.setUpdateStatus(QboConstants.MSG_UPDATED);
                qboVendorRepository.save(qboVendor);
            }catch (Exception exception){
                log.info(exception.getMessage());
            }
        }
        List<QboCustomer> qboCustomers = qboCustomerRepository.findByNeedUpdate(true);
        for(QboCustomer qboCustomer : qboCustomers){
            try{
                if(qboCustomer.getUser().getEmail().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)){
                    qboCustomer.setNeedUpdate(false);
                    qboCustomer.setUpdateStatus(MessageConstants.MSG_USR_ACCOUNT_DELETED);
                    qboCustomerRepository.save(qboCustomer);
                    continue;
                }
                UserProfile userProfile = userProfileRepository.findByUser(qboCustomer.getUser());
                if(userProfile == null){
                    qboCustomer.setNeedUpdate(false);
                    qboCustomer.setUpdateStatus(MessageConstants.MSG_USR_PROFILE_NOT_FOUND);
                    qboCustomerRepository.save(qboCustomer);
                    continue;
                }
                Customer customer = new Customer();
                if(qboCustomer.getCustomerId() != null){
                    customer.setId(qboCustomer.getCustomerId());
                    customer = findEntity(customer);
                    if(customer == null || customer.getId() == null){
                        customer = new Customer();
                    }
                }
                TelephoneNumber telephoneNumber = new TelephoneNumber();
                if(!StringUtils.isEmpty(userProfile.getContactNumber())){
                    telephoneNumber.setFreeFormNumber(userProfile.getContactNumber());
                }else{
                    telephoneNumber.setFreeFormNumber("");
                }
                customer.setMobile(telephoneNumber);
                if(customer.getId() == null){
                    customer.setGivenName(userProfile.getFirstName());
                    customer.setFamilyName(userProfile.getLastName());
                    String displayName = getDisplayName(userProfile, "C");
                    customer.setDisplayName(displayName);
                    qboCustomer.setDisplayName(displayName);
                    EmailAddress emailAddress = new EmailAddress();
                    emailAddress.setAddress(qboCustomer.getUser().getEmail());
                    customer.setPrimaryEmailAddr(emailAddress);
                    customer = createEntity(customer);
                    if(customer.getId() == null){
                        qboCustomer.setUpdateStatus(QboConstants.MSG_ENTITY_CREATE_FAILURE);
                        qboCustomerRepository.save(qboCustomer);
                        continue;
                    }
                    qboCustomer.setCustomerId(customer.getId());
                } else {
                    if(customer.getGivenName() == null || !customer.getGivenName().equalsIgnoreCase(userProfile.getFirstName()) || !customer.getFamilyName().equalsIgnoreCase(userProfile.getLastName())){
                        customer.setGivenName(userProfile.getFirstName());
                        customer.setFamilyName(userProfile.getLastName());
                        String displayName = getDisplayName(userProfile, "C");
                        customer.setDisplayName(displayName);
                        qboCustomer.setDisplayName(displayName);
                    }
                    updateEntity(customer);
                }
                qboCustomer.setNeedUpdate(false);
                qboCustomer.setUpdateStatus(QboConstants.MSG_UPDATED);
                qboCustomerRepository.save(qboCustomer);
            }catch (Exception exception){
                log.error(exception.getMessage());
            }
        }
        log.info("User sync to QBO end");
    }

    private String getDisplayName(UserProfile userProfile, String userType) {
        int count = findNameOccurrenceCountInQboUser(userProfile);
        String displayName = userProfile.getFirstName() + " " + userProfile.getLastName() + " " + (count++) + " " + userType;
        while(isExistDisplayName(displayName, userType)){
            displayName = userProfile.getFirstName() + " " + userProfile.getLastName() + " " + (count++) + " " + userType;
        }
        return displayName;
    }

    private boolean isExistDisplayName(String displayName, String type) {
        boolean result = false;
        if(type.equalsIgnoreCase("V")){
            List<QboVendor> qboVendors = qboVendorRepository.findByDisplayName(displayName);
            if(!qboVendors.isEmpty()){
                result = true;
            }
        }else{
            List<QboCustomer> qboCustomers = qboCustomerRepository.findByDisplayName(displayName);
            if(!qboCustomers.isEmpty()){
                result = true;
            }
        }
        return result;
    }

    /**
     * Find the number of occurrence by the name in qbo
     * @param profile Profile info
     * @return Number of existing user has same name
     */
    public int findNameOccurrenceCountInQboUser(UserProfile profile) {
        return userProfileRepository.countByFirstNameAndLastName(profile.getFirstName(), profile.getLastName());
    }

    private Vendor findVendorByCompanyName(String companyName) throws FMSException, OAuthException {
        Vendor vendor = null;
        List<? extends  IEntity> entityList = queryData("select * from vendor where CompanyName = '" + companyName + "'");
        if(entityList != null && entityList.size() > 0){
            vendor = (Vendor) entityList.get(0);
        }
        return vendor;
    }

    /**
     * Sync the user products
     */
    @Async("threadPoolTaskExecutor")
    public void syncProducts() {
        log.info("Product sync to QBO start");
        List<QboProduct> qboProducts = qboProductRepository.findByNeedUpdate(true);
        for(QboProduct qboProduct : qboProducts){
            try{
                Item parentCategory;
                List<PlatformType> platformTypes = platformTypeRepository.findAll();
                Vendor vendor = null;
                if (qboProduct.getTier() == null) {
                    boolean isProgram;
                    User productOwner;
                    if(qboProduct.getProgram() != null){
                        isProgram = true;
                        productOwner = qboProduct.getProgram().getOwner();
                    }else{
                        isProgram = false;
                        productOwner = qboProduct.getSubscriptionPackage().getOwner();
                    }
                    boolean isProductDeleted = false;
                    String errMsg = "";
                    if(isProgram && qboProduct.getProgram().getTitle().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)){
                        isProductDeleted = true;
                        errMsg = MessageConstants.MSG_PGM_DELETED;
                    }
                    if(!isProgram && qboProduct.getSubscriptionPackage().getTitle().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)){
                        isProductDeleted = true;
                        errMsg = MessageConstants.MSG_PKG_DELETED;
                    }
                    if(isProductDeleted){
                        qboProduct.setNeedUpdate(false);
                        qboProduct.setUpdateStatus(errMsg);
                        qboProductRepository.save(qboProduct);
                        continue;
                    }
                    List<QboVendor> qboVendors = qboVendorRepository.findByUser(productOwner);
                    if(qboVendors.isEmpty()){
                        QboVendor qboVendor = new QboVendor();
                        qboVendor.setUser(qboProduct.getProgram().getOwner());
                        qboVendor.setNeedUpdate(true);
                        qboVendorRepository.save(qboVendor);
                        errMsg = QboConstants.MSG_VENDOR_MISSING;
                    }else{
                        if(qboVendors.get(0).getVendorId() != null){
                            vendor = new Vendor();
                            vendor.setId(qboVendors.get(0).getVendorId());
                            vendor = findEntity(vendor);
                            if(vendor == null || vendor.getId() == null){
                                vendor = null;
                                qboVendors.get(0).setVendorId(null);
                                qboVendors.get(0).setNeedUpdate(true);
                                qboVendorRepository.save(qboVendors.get(0));
                            }
                        }
                        errMsg = QboConstants.MSG_VENDOR_NOT_AVAILABLE;
                    }
                    if(vendor == null){
                        qboProduct.setUpdateStatus(errMsg);
                        qboProductRepository.save(qboProduct);
                        continue;
                    }
                    if(qboVendors.get(0).getUser().getEmail().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)){
                        qboProduct.setNeedUpdate(false);
                        qboProduct.setUpdateStatus(MessageConstants.MSG_USR_ACCOUNT_DELETED);
                        qboProductRepository.save(qboProduct);
                        continue;
                    }
                    if(isProgram){
                        parentCategory = getProductMainCategory(qboProduct.getProgram().getProgramType().getProgramTypeName());
                    }else{
                        parentCategory = getProductMainCategory(QboConstants.QBO_CAT_SUBSCRIPTION_PACKAGE);
                    }
                } else {
                    parentCategory = getProductMainCategory(QboConstants.QBO_CAT_TIER);
                }
                Item category = getProductCategory(qboProduct, vendor, parentCategory);
                if(category == null){
                    qboProduct.setUpdateStatus(QboConstants.MSG_PRODUCT_CATEGORY_NOT_AVAILABLE);
                    qboProductRepository.save(qboProduct);
                    continue;
                }
                Double flatTax = Double.parseDouble(appConfigKeyValueRepository.findByKeyString(AppConfigConstants.ADMIN_CONFIG_FLAT_TAX).getValueString());
                for(PlatformType platformType : platformTypes){
                    String description;
                    String name;
                    Double unitPrice;
                    if(qboProduct.getTier() != null){
                        description = "";
                        name = qboProduct.getTier().getTierType().replaceAll("[^a-zA-Z0-9]", " ") + " " + platformType.getPlatform();
                        unitPrice = qboProduct.getTier().getTierTypeDetails().getMinimumCommitment();
                    } else if(qboProduct.getProgram() != null){
                        description = qboProduct.getProgram().getDescription();
                        name = qboProduct.getProgram().getTitle().replaceAll("[^a-zA-Z0-9]", " ") + " By " + vendor.getDisplayName() + " " + platformType.getPlatform();
                        unitPrice = qboProduct.getProgram().getProgramPrice();
                    }else{
                        description = qboProduct.getSubscriptionPackage().getDescription();
                        name = qboProduct.getSubscriptionPackage().getTitle().replaceAll("[^a-zA-Z0-9]", " ") + " By " + vendor.getDisplayName() + " " + platformType.getPlatform();
                        unitPrice = qboProduct.getSubscriptionPackage().getPrice();
                    }
                    unitPrice = unitPrice + flatTax;
                    String productId = null;
                    ReferenceType incomeReferenceType = new ReferenceType();
                    ReferenceType expenseAccountReferenceType = new ReferenceType();
                    Account incomeAccount = null;
                    if(platformType.getPlatform().equalsIgnoreCase(DBConstants.ANDROID)){
                        productId = qboProduct.getProductIdAndroid();
                        incomeAccount = findAccountByName(QboConstants.ACCOUNT_INCOME_TRAINNR_ANDROID);
                    }else if(platformType.getPlatform().equalsIgnoreCase(DBConstants.IOS)){
                        productId = qboProduct.getProductIdIos();
                        incomeAccount = findAccountByName(QboConstants.ACCOUNT_INCOME_TRAINNR_IOS);
                    }else if(platformType.getPlatform().equalsIgnoreCase(DBConstants.WEB)){
                        productId = qboProduct.getProductIdWeb();
                        incomeAccount = findAccountByName(QboConstants.ACCOUNT_INCOME_TRAINNR_WEB);
                    }
                    if(incomeAccount == null){
                        qboProduct.setUpdateStatus(QboConstants.MSG_INCOME_ACCOUNT_MISSING);
                        qboProductRepository.save(qboProduct);
                        continue;
                    }
                    incomeReferenceType.setValue(incomeAccount.getId());
                    Account expenseAccount = findAccountByName(QboConstants.ACCOUNT_EXPENSE_INSTRUCTOR_PAYMENTS);
                    if(expenseAccount == null){
                        qboProduct.setUpdateStatus(QboConstants.MSG_EXPENSE_ACCOUNT_MISSING);
                        qboProductRepository.save(qboProduct);
                        continue;
                    }
                    expenseAccountReferenceType.setValue(expenseAccount.getId());
                    Item item = new Item();
                    if(productId != null){
                        item.setId(productId);
                        item = findEntity(item);
                        if(item == null || item.getId() == null){
                            productId = null;
                            item = new Item();
                        }
                    }
                    item.setDescription(description);
                    item.setUnitPrice(new BigDecimal(unitPrice));
                    item.setName(name);
                    item.setIncomeAccountRef(incomeReferenceType);
                    item.setExpenseAccountRef(expenseAccountReferenceType);
                    if(productId == null){
                        item.setSku(String.valueOf(qboProduct.getSkuNumber()));
                        item.setType(ItemTypeEnum.NON_INVENTORY);
                        ReferenceType categoryReferenceType = new ReferenceType();
                        categoryReferenceType.setValue(category.getId());
                        categoryReferenceType.setName(category.getName());
                        item.setParentRef(categoryReferenceType);
                        item.setSubItem(true);
                        if(qboProduct.getTier() == null) {
                            ReferenceType referenceType = new ReferenceType();
                            referenceType.setValue(vendor.getId());
                            referenceType.setName(vendor.getDisplayName());
                            item.setPrefVendorRef(referenceType);
                        }
                        item = createEntity(item);
                        if(item.getId() == null){
                            qboProduct.setUpdateStatus(QboConstants.MSG_ENTITY_CREATE_FAILURE);
                            qboProductRepository.save(qboProduct);
                            continue;
                        }
                        if(platformType.getPlatform().equalsIgnoreCase(DBConstants.ANDROID)){
                            qboProduct.setProductIdAndroid(item.getId());
                        }else if(platformType.getPlatform().equalsIgnoreCase(DBConstants.IOS)){
                            qboProduct.setProductIdIos(item.getId());
                        }else if(platformType.getPlatform().equalsIgnoreCase(DBConstants.WEB)){
                            qboProduct.setProductIdWeb(item.getId());
                        }
                    }else{
                        updateEntity(item);
                    }
                }
                qboProduct.setNeedUpdate(false);
                qboProduct.setUpdateStatus(QboConstants.MSG_UPDATED);
                qboProductRepository.save(qboProduct);
            }catch (Exception exception){
                log.error(exception.getMessage());
            }
        }
        log.info("Product sync to QBO end");
    }

    private Account findAccountByName(String incomeAccountName) throws FMSException, OAuthException{
        Account account = null;
        List<? extends  IEntity> entityList = queryData("select * from Account where Name = '" + incomeAccountName + "'");
        if(entityList != null && entityList.size() > 0){
            account = (Account) entityList.get(0);
        }
        return account;
    }

    /**
     * Sync fitwise invoice with qbo invoice
     */
    @Async("threadPoolTaskExecutor")
    public void syncInvoice() {
        log.info("Invoice sync to QBO start");
        List<QboInvoice> invoiceList = qboInvoiceRepository.findByNeedUpdate(true);
        for(QboInvoice qboInvoice : invoiceList){
            try{
                Invoice invoice = (Invoice) findInvoiceByDocNumber(qboInvoice.getInvoice().getInvoiceNumber());
                if(invoice == null){
                    invoice = new Invoice();
                }
                Double price;
                List<QboProduct> qboProducts;
                Programs program = null;
                Tier tier = null;
                SubscriptionPackage subscriptionPackage = null;
                if(KeyConstants.KEY_TIER.equalsIgnoreCase(qboInvoice.getInvoice().getOrderManagement().getSubscriptionType().getName())){
                    tier = tierRepository.findByTierId(qboInvoice.getInvoice().getOrderManagement().getTier().getTierId());
                    price = tier.getTierTypeDetails().getMinimumCommitment();
                    qboProducts = qboProductRepository.findByTier(tier);
                } else if(KeyConstants.KEY_PROGRAM.equalsIgnoreCase(qboInvoice.getInvoice().getOrderManagement().getSubscriptionType().getName())){
                    program = programRepository.findByProgramId(qboInvoice.getInvoice().getOrderManagement().getProgram().getProgramId());
                    price = program.getProgramPrices().getPrice();
                    qboProducts = qboProductRepository.findByProgram(program);
                }else{
                    subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageId(qboInvoice.getInvoice().getOrderManagement().getSubscriptionPackage().getSubscriptionPackageId());
                    price = subscriptionPackage.getPrice();
                    qboProducts = qboProductRepository.findBySubscriptionPackage(subscriptionPackage);
                }
                if(isProductDeleted(qboInvoice.getInvoice().getOrderManagement())){
                    qboInvoice.setNeedUpdate(false);
                    qboInvoice.setUpdateStatus(MessageConstants.MSG_QBO_PRODUCT_DELETED);
                    qboInvoiceRepository.save(qboInvoice);
                    continue;
                }
                Line line = new Line();
                line.setDetailType(LineDetailTypeEnum.SALES_ITEM_LINE_DETAIL);
                SalesItemLineDetail salesItemLineDetail = new SalesItemLineDetail();
                salesItemLineDetail.setUnitPrice(new BigDecimal(price));
                Item item = null;
                String errMsg;
                if(qboProducts.isEmpty()){
                    QboProduct product = new QboProduct();
                    String productType = "PKG";
                    if(tier != null){
                        product.setTier(tier);
                        productType = "TIER";
                    } else if(program != null){
                        product.setProgram(program);
                        productType = "PGM";
                    } else {
                        product.setSubscriptionPackage(subscriptionPackage);
                    }
                    product.setSkuNumber(OrderNumberGenerator.generateProductSKU(productType));
                    product.setNeedUpdate(true);
                    qboProductRepository.save(product);
                    errMsg = QboConstants.MSG_PRODUCT_MISSING;
                }else{
                    String productId = null;
                    if(qboInvoice.getInvoice().getOrderManagement().getSubscribedViaPlatform().getPlatform().equalsIgnoreCase(DBConstants.ANDROID)){
                        productId = qboProducts.get(0).getProductIdAndroid();
                    }else if(qboInvoice.getInvoice().getOrderManagement().getSubscribedViaPlatform().getPlatform().equalsIgnoreCase(DBConstants.IOS)){
                        productId = qboProducts.get(0).getProductIdIos();
                    }else if(qboInvoice.getInvoice().getOrderManagement().getSubscribedViaPlatform().getPlatform().equalsIgnoreCase(DBConstants.WEB)){
                        productId = qboProducts.get(0).getProductIdWeb();
                    }
                    if(productId != null){
                        item = new Item();
                        item.setId(productId);
                        item = findEntity(item);
                        if(item == null || item.getId() == null){
                            item = null;
                            qboProducts.get(0).setProductIdWeb(null);
                            qboProducts.get(0).setProductIdIos(null);
                            qboProducts.get(0).setProductIdAndroid(null);
                            qboProducts.get(0).setNeedUpdate(true);
                            qboProductRepository.save(qboProducts.get(0));
                        }
                    }
                    errMsg = QboConstants.MSG_PRODUCT_NOT_AVAILABLE;
                }
                if(item == null){
                    qboInvoice.setUpdateStatus(errMsg);
                    qboInvoiceRepository.save(qboInvoice);
                    continue;
                }
                ReferenceType itemReference = new ReferenceType();
                itemReference.setValue(item.getId());
                itemReference.setName(item.getName());
                salesItemLineDetail.setItemRef(itemReference);
                line.setSalesItemLineDetail(salesItemLineDetail);
                line.setAmount(new BigDecimal(price));
                List<QboCustomer> qboCustomers = qboCustomerRepository.findByUser(qboInvoice.getInvoice().getOrderManagement().getUser());
                Customer customer = null;
                if(qboCustomers.isEmpty()){
                    QboCustomer qboCustomer = new QboCustomer();
                    qboCustomer.setUser(qboInvoice.getInvoice().getOrderManagement().getUser());
                    qboCustomer.setNeedUpdate(true);
                    qboCustomerRepository.save(qboCustomer);
                    errMsg = QboConstants.MSG_CUSTOMER_MISSING;
                }else{
                    if(qboCustomers.get(0).getCustomerId() != null){
                        customer = new Customer();
                        customer.setId(qboCustomers.get(0).getCustomerId());
                        customer = findEntity(customer);
                        if(customer == null || customer.getId() == null){
                            customer = null;
                            qboCustomers.get(0).setCustomerId(null);
                            qboCustomers.get(0).setNeedUpdate(true);
                            qboCustomerRepository.save(qboCustomers.get(0));
                        }
                    }
                    errMsg = QboConstants.MSG_CUSTOMER_NOT_AVAILABLE;
                }
                if(customer == null){
                    qboInvoice.setUpdateStatus(errMsg);
                    qboInvoiceRepository.save(qboInvoice);
                    continue;
                }
                if(qboCustomers.get(0).getUser().getEmail().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)){
                    qboInvoice.setNeedUpdate(false);
                    qboInvoice.setUpdateStatus(MessageConstants.MSG_USR_ACCOUNT_DELETED);
                    qboInvoiceRepository.save(qboInvoice);
                    continue;
                }
                ReferenceType customerReferenceType = new ReferenceType();
                customerReferenceType.setValue(customer.getId());
                customerReferenceType.setName(customer.getDisplayName());
                List<Line> lines = new ArrayList<>();
                invoice.setPrivateNote(null);
                if(qboInvoice.getInvoice().getOrderManagement().getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_STRIPE)){
                    StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementAndTransactionStatusOrderByModifiedDateDesc(qboInvoice.getInvoice().getOrderManagement(), KeyConstants.KEY_PAID);
                    if(stripePayment != null && stripePayment.getChargeId() != null){
                        line.setDescription("Stripe charge ref " + stripePayment.getChargeId());
                        invoice.setPrivateNote("Stripe charge ref " + stripePayment.getChargeId());
                    }
                }
                lines.add(line);
                invoice.setLine(lines);
                invoice.setCustomerRef(customerReferenceType);
                invoice.setDocNumber(qboInvoice.getInvoice().getInvoiceNumber());
                EmailAddress emailAddress = new EmailAddress();
                emailAddress.setAddress(qboInvoice.getInvoice().getOrderManagement().getUser().getEmail());
                invoice.setBillEmail(emailAddress);
                invoice.setAllowOnlineACHPayment(true);
                OfferCodeDetailAndOrderMapping offerCodeDetailAndOrderMapping = offerCodeDetailAndOrderMappingRepository.findTop1ByOrderManagementOrderByCreatedDateDesc(qboInvoice.getInvoice().getOrderManagement());
                if(KeyConstants.KEY_TIER.equalsIgnoreCase(qboInvoice.getInvoice().getOrderManagement().getSubscriptionType().getName()) && qboInvoice.getInvoice().getOrderManagement().getTierAdjustedAmt() > 0){
                    Line discountLine = new Line();
                    discountLine.setDetailType(LineDetailTypeEnum.DISCOUNT_LINE_DETAIL);
                    DiscountLineDetail discountLineDetail = new DiscountLineDetail();
                    discountLineDetail.setPercentBased(false);
                    double discount = qboInvoice.getInvoice().getOrderManagement().getTierAdjustedAmt();
                    discountLine.setAmount(BigDecimal.valueOf(discount));
                    if(invoice.getPrivateNote() != null){
                        invoice.setPrivateNote(invoice.getPrivateNote() + ", Discount applied for adjustment of previous subscription");
                    }else{
                        invoice.setPrivateNote("Discount applied for adjustment of previous subscription");
                    }
                    discountLine.setDiscountLineDetail(discountLineDetail);
                    lines.add(discountLine);
                } else if(offerCodeDetailAndOrderMapping != null){
                    Line discountLine = new Line();
                    discountLine.setDetailType(LineDetailTypeEnum.DISCOUNT_LINE_DETAIL);
                    DiscountLineDetail discountLineDetail = new DiscountLineDetail();
                    if(offerCodeDetailAndOrderMapping.getOfferCodeDetail().getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_FREE)){
                        discountLineDetail.setPercentBased(true);
                        discountLineDetail.setDiscountPercent(new BigDecimal(100));
                        discountLine.setAmount(new BigDecimal(price));
                    }else{
                        discountLineDetail.setPercentBased(false);
                        double discount = price - offerCodeDetailAndOrderMapping.getOfferCodeDetail().getOfferPrice().getPrice();
                        discountLine.setAmount(BigDecimal.valueOf(discount));
                    }
                    if(invoice.getPrivateNote() != null){
                        invoice.setPrivateNote(invoice.getPrivateNote() + ", Discount applied : " + offerCodeDetailAndOrderMapping.getOfferCodeDetail().getOfferName());
                    }else{
                        invoice.setPrivateNote("Discount applied : " + offerCodeDetailAndOrderMapping.getOfferCodeDetail().getOfferName());
                    }
                    discountLine.setDiscountLineDetail(discountLineDetail);
                    lines.add(discountLine);
                }
                invoice.setLine(lines);
                invoice = createEntity(invoice);
                if(invoice.getId() == null){
                    qboInvoice.setUpdateStatus(QboConstants.MSG_ENTITY_CREATE_FAILURE);
                    qboInvoiceRepository.save(qboInvoice);
                    continue;
                }
                qboInvoice.setNeedUpdate(false);
                qboInvoice.setUpdateStatus(QboConstants.MSG_UPDATED);
                qboInvoiceRepository.save(qboInvoice);
            }catch (Exception exception){
                log.error(exception.getMessage());
            }
        }
        log.info("Invoice sync to QBO end");
    }

    private boolean isProductDeleted(OrderManagement orderManagement) {
        boolean productDeleted = false;
        if(KeyConstants.KEY_PROGRAM.equalsIgnoreCase(orderManagement.getSubscriptionType().getName())){
            if(KeyConstants.KEY_ANONYMOUS.equalsIgnoreCase(orderManagement.getProgram().getTitle())){
                productDeleted = true;
            }
        }else if (KeyConstants.KEY_SUBSCRIPTION_PACKAGE.equalsIgnoreCase(orderManagement.getSubscriptionType().getName())){
            if(KeyConstants.KEY_ANONYMOUS.equalsIgnoreCase(orderManagement.getSubscriptionPackage().getTitle())){
                productDeleted = true;
            }
        }
        return productDeleted;
    }

    /**
     * Find the invoice in qbo by the fitwise invoice number
     * @param invoiceNumber Invoice number
     * @return Generic entity
     * @throws FMSException Generic Intuit SDK exception
     */
    public IEntity findInvoiceByDocNumber(String invoiceNumber) throws FMSException, OAuthException {
        List<? extends IEntity> entityList = queryData("select * from invoice where DocNumber = '" + invoiceNumber + "'");
        IEntity invoice = null;
        if(entityList != null && entityList.size() > 0){
            invoice = entityList.get(0);
        }
        return invoice;
    }

    /**
     * Sync the fitwise payments with qbo payments
     */
    @Async("threadPoolTaskExecutor")
    public void syncPayment() {
        log.info("Payment sync to QBO start");
        List<QboPayment> qboPayments = qboPaymentRepository.findByNeedUpdate(true);
        for(QboPayment qboPayment : qboPayments){
            try{
                Payment payment = new Payment();
                if(qboPayment.getQboPaymentId() != null){
                    payment.setId(qboPayment.getQboPaymentId());
                    payment = findEntity(payment);
                }
                OrderManagement orderManagement = null;
                if(qboPayment.getAuthNetPayment() != null){
                    AuthNetPayment authNetPayment = qboPayment.getAuthNetPayment();
                    orderManagement = authNetPayment.getOrderManagement();
                    //Check buffer number of days
                    Date today = new Date();
                    if(qboPayment.getModifiedDate() != null){
                        Date lastUpdated = qboPayment.getModifiedDate();
                        long difMilliSeconds = today.getTime() - lastUpdated.getTime();
                        long diffDays = TimeUnit.DAYS.convert(difMilliSeconds, TimeUnit.MILLISECONDS);
                        if(diffDays < Integer.parseInt(generalProperties.getNofDaysToSyncAnetSettlement())){
                            continue;
                        }
                    }
                }else if(qboPayment.getApplePayment() != null){
                    ApplePayment applePayment = qboPayment.getApplePayment();
                    orderManagement = applePayment.getOrderManagement();
                } else if(qboPayment.getStripePayment() != null){
                    StripePayment stripePayment = qboPayment.getStripePayment();
                    orderManagement = stripePayment.getOrderManagement();
                }
                double price;
                if(KeyConstants.KEY_TIER.equalsIgnoreCase(orderManagement.getSubscriptionType().getName())){
                    price = orderManagement.getTier().getTierTypeDetails().getMinimumCommitment();
                } else if(KeyConstants.KEY_PROGRAM.equalsIgnoreCase(orderManagement.getSubscriptionType().getName())){
                    price = orderManagement.getProgram().getProgramPrices().getPrice();
                }else{
                    price = orderManagement.getSubscriptionPackage().getPrice();
                }
                OfferCodeDetailAndOrderMapping offerCodeDetailAndOrderMapping = offerCodeDetailAndOrderMappingRepository.findTop1ByOrderManagementOrderByCreatedDateDesc(orderManagement);
                if (orderManagement.getTier() != null && orderManagement.getTierAdjustedAmt() > 0){
                    payment.setTotalAmt(BigDecimal.valueOf(orderManagement.getTierPaidAmt()));
                } else if(offerCodeDetailAndOrderMapping != null){
                    if(offerCodeDetailAndOrderMapping.getOfferCodeDetail().getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_FREE)){
                        payment.setTotalAmt(BigDecimal.valueOf(0));
                    }else{
                        payment.setTotalAmt(BigDecimal.valueOf(offerCodeDetailAndOrderMapping.getOfferCodeDetail().getOfferPrice().getPrice()));
                    }
                }else{
                    payment.setTotalAmt(BigDecimal.valueOf(price));
                }
                List<QboCustomer> qboCustomers = qboCustomerRepository.findByUser(orderManagement.getUser());
                Customer customer = null;
                String errMsg;
                if(qboCustomers.isEmpty()){
                    QboCustomer qboCustomer = new QboCustomer();
                    qboCustomer.setUser(orderManagement.getUser());
                    qboCustomer.setNeedUpdate(true);
                    qboCustomerRepository.save(qboCustomer);
                    errMsg = QboConstants.MSG_CUSTOMER_MISSING;
                }else{
                    if(qboCustomers.get(0).getCustomerId() != null){
                        customer = new Customer();
                        customer.setId(qboCustomers.get(0).getCustomerId());
                        customer = findEntity(customer);
                        if(customer == null || customer.getId() == null){
                            customer = null;
                            qboCustomers.get(0).setCustomerId(null);
                            qboCustomers.get(0).setNeedUpdate(true);
                            qboCustomerRepository.save(qboCustomers.get(0));
                        }
                    }
                    errMsg = QboConstants.MSG_CUSTOMER_NOT_AVAILABLE;
                }
                if(customer == null){
                    qboPayment.setUpdateStatus(errMsg);
                    qboPaymentRepository.save(qboPayment);
                    continue;
                }
                if(qboCustomers.get(0).getUser().getEmail().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)){
                    qboPayment.setNeedUpdate(false);
                    qboPayment.setUpdateStatus(MessageConstants.MSG_USR_ACCOUNT_DELETED);
                    qboPaymentRepository.save(qboPayment);
                    continue;
                }
                ReferenceType customerRef = new ReferenceType();
                customerRef.setValue(customer.getId());
                customerRef.setName(customer.getDisplayName());
                payment.setCustomerRef(customerRef);
                List<Line> lines = new ArrayList<>();
                Line line = new Line();
                line.setAmount(BigDecimal.valueOf(price));
                List<LinkedTxn> linkedTxns = new ArrayList<>();
                LinkedTxn linkedTxn = new LinkedTxn();
                linkedTxn.setTxnType(StringConstants.INVOICE);
                InvoiceManagement invoiceManagement = invoiceManagementRepository.findByOrderManagement(orderManagement);
                Invoice invoice = (Invoice)findInvoiceByDocNumber(invoiceManagement.getInvoiceNumber());
                if(invoice == null){
                    qboPayment.setUpdateStatus(QboConstants.MSG_INVOICE_NOT_AVAILABLE);
                    qboPaymentRepository.save(qboPayment);
                    continue;
                }
                linkedTxn.setTxnId(invoice.getId());
                linkedTxns.add(linkedTxn);
                line.setLinkedTxn(linkedTxns);
                lines.add(line);
                payment.setLine(lines);
                payment.setTxnDate(orderManagement.getCreatedDate());
                if(qboPayment.getQboPaymentId() == null){
                    payment = createEntity(payment);
                    qboPayment.setQboPaymentId(payment.getId());
                }else{
                    updateEntity(payment);
                }
                if(orderManagement.getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_ANET)){
                    qboPayment.setIsFixedCCBillPaymentCreated(false);
                }else{
                    qboPayment.setIsFixedCCBillPaymentCreated(true);
                }
                qboPayment.setIsCCBillPaymentCreated(false);
                qboPayment.setNeedUpdate(false);
                qboPayment.setUpdateStatus(QboConstants.MSG_UPDATED);
                qboPaymentRepository.save(qboPayment);
            }catch (Exception exception){
                exception.printStackTrace();
                log.error(exception.getMessage());
            }
        }
        log.info("Payment sync to QBO end");
    }

    @Async("threadPoolTaskExecutor")
    public void syncBill(){
        log.info("Bill sync to QBO start");
        List<QboBill> qboBills = qboBillRepository.findByNeedUpdate(true);
        for(QboBill qboBill : qboBills){
            try{
                Bill bill = new Bill();
                InstructorPayment instructorPayment = qboBill.getInstructorPayment();
                User owner;
                if(KeyConstants.KEY_PROGRAM.equalsIgnoreCase(instructorPayment.getOrderManagement().getSubscriptionType().getName())){
                    owner = instructorPayment.getOrderManagement().getProgram().getOwner();
                }else{
                    owner = instructorPayment.getOrderManagement().getSubscriptionPackage().getOwner();
                }
                List<QboVendor> qboVendors = qboVendorRepository.findByUser(owner);
                Vendor vendor = null;
                String errMsg;
                if(StringUtils.isEmpty(instructorPayment.getCardType()) && instructorPayment.getOrderManagement().getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_ANET)){
                    AuthNetPayment authNetPayment = authNetPaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(instructorPayment.getOrderManagement().getOrderId());
                    if(!StringUtils.isEmpty(authNetPayment.getTransactionId())){
                        String cardType = paymentService.getCardType(authNetPayment.getTransactionId());
                        if(cardType.equalsIgnoreCase(KeyConstants.KEY_CARD_TYPE_AMERICAN_EXPRESS)){
                            instructorPayment = fitwiseQboEntityService.updateInstructorPayment(instructorPayment, cardType);
                        }
                    }else{
                        qboBill.setUpdateStatus(QboConstants.MSG_ANET_TRANSACTION_NOT_FOUND);
                        qboBillRepository.save(qboBill);
                        continue;
                    }
                }
                if(qboVendors.isEmpty()){
                    QboVendor qboVendor = new QboVendor();
                    qboVendor.setUser(owner);
                    qboVendor.setNeedUpdate(true);
                    qboVendorRepository.save(qboVendor);
                    errMsg = QboConstants.MSG_VENDOR_MISSING;
                }else{
                    if(qboVendors.get(0).getVendorId() != null){
                        vendor = new Vendor();
                        vendor.setId(qboVendors.get(0).getVendorId());
                        vendor = findEntity(vendor);
                        if(vendor == null || vendor.getId() == null){
                            vendor = null;
                            qboVendors.get(0).setVendorId(null);
                            qboVendors.get(0).setNeedUpdate(true);
                            qboVendorRepository.save(qboVendors.get(0));
                        }
                    }
                    errMsg = QboConstants.MSG_VENDOR_NOT_AVAILABLE;
                }
                if(vendor == null){
                    qboBill.setUpdateStatus(errMsg);
                    qboBillRepository.save(qboBill);
                    continue;
                }
                if(qboVendors.get(0).getUser().getEmail().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)){
                    qboBill.setNeedUpdate(false);
                    qboBill.setUpdateStatus(MessageConstants.MSG_USR_ACCOUNT_DELETED);
                    qboBillRepository.save(qboBill);
                    continue;
                }
                ReferenceType vendorRef = new ReferenceType();
                vendorRef.setValue(vendor.getId());
                bill.setVendorRef(vendorRef);
                //Line Array
                Line line = new Line();
                ItemBasedExpenseLineDetail itemBasedExpenseLineDetail = new ItemBasedExpenseLineDetail();
                String productId = null;
                List<QboProduct> qboProducts;
                if(KeyConstants.KEY_PROGRAM.equalsIgnoreCase(instructorPayment.getOrderManagement().getSubscriptionType().getName())){
                    qboProducts = qboProductRepository.findByProgram(instructorPayment.getOrderManagement().getProgram());
                    if(qboProducts.get(0).getProgram().getTitle().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)){
                        qboBill.setNeedUpdate(false);
                        qboBill.setUpdateStatus(MessageConstants.MSG_PGM_DELETED);
                        qboBillRepository.save(qboBill);
                        continue;
                    }
                }else{
                    qboProducts = qboProductRepository.findBySubscriptionPackage(instructorPayment.getOrderManagement().getSubscriptionPackage());
                    if(qboProducts.get(0).getSubscriptionPackage().getTitle().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)){
                        qboBill.setNeedUpdate(false);
                        qboBill.setUpdateStatus(MessageConstants.MSG_PKG_DELETED);
                        qboBillRepository.save(qboBill);
                        continue;
                    }
                }
                Item item = null;
                if(qboProducts.isEmpty()){
                    QboProduct product = new QboProduct();
                    product.setProgram(instructorPayment.getOrderManagement().getProgram());
                    product.setSubscriptionPackage(instructorPayment.getOrderManagement().getSubscriptionPackage());
                    product.setNeedUpdate(true);
                    qboProductRepository.save(product);
                    errMsg = QboConstants.MSG_PRODUCT_MISSING;
                }else{
                    if(instructorPayment.getOrderManagement().getSubscribedViaPlatform().getPlatform().equalsIgnoreCase(DBConstants.ANDROID)){
                        productId = qboProducts.get(0).getProductIdAndroid();
                    }else if(instructorPayment.getOrderManagement().getSubscribedViaPlatform().getPlatform().equalsIgnoreCase(DBConstants.IOS)){
                        productId = qboProducts.get(0).getProductIdIos();
                    }else if(instructorPayment.getOrderManagement().getSubscribedViaPlatform().getPlatform().equalsIgnoreCase(DBConstants.WEB)){
                        productId = qboProducts.get(0).getProductIdWeb();
                    }
                    if(productId != null){
                        item = new Item();
                        item.setId(productId);
                        item = findEntity(item);
                        if(item == null || item.getId() == null){
                            item = null;
                            qboProducts.get(0).setProductIdWeb(null);
                            qboProducts.get(0).setProductIdIos(null);
                            qboProducts.get(0).setProductIdAndroid(null);
                            qboProducts.get(0).setNeedUpdate(true);
                            qboProductRepository.save(qboProducts.get(0));
                        }
                    }
                    errMsg = QboConstants.MSG_PRODUCT_NOT_AVAILABLE;
                }
                if(item == null){
                    qboBill.setUpdateStatus(errMsg);
                    qboBillRepository.save(qboBill);
                    continue;
                }
                ReferenceType itemReference = new ReferenceType();
                itemReference.setValue(item.getId());
                itemBasedExpenseLineDetail.setItemRef(itemReference);
                List<QboCustomer> qboCustomers = qboCustomerRepository.findByUser(instructorPayment.getOrderManagement().getUser());
                Customer customer = null;
                if(qboCustomers.isEmpty()){
                    QboCustomer qboCustomer = new QboCustomer();
                    qboCustomer.setUser(instructorPayment.getOrderManagement().getUser());
                    qboCustomer.setNeedUpdate(true);
                    qboCustomerRepository.save(qboCustomer);
                    errMsg = QboConstants.MSG_CUSTOMER_MISSING;
                }else{
                    if(qboCustomers.get(0).getCustomerId() != null){
                        customer = new Customer();
                        customer.setId(qboCustomers.get(0).getCustomerId());
                        customer = findEntity(customer);
                        if(customer == null || customer.getId() == null){
                            customer = null;
                            qboCustomers.get(0).setCustomerId(null);
                            qboCustomers.get(0).setNeedUpdate(true);
                            qboCustomerRepository.save(qboCustomers.get(0));
                        }
                    }
                    errMsg = QboConstants.MSG_CUSTOMER_NOT_AVAILABLE;
                }
                if(customer == null){
                    qboBill.setUpdateStatus(errMsg);
                    qboBillRepository.save(qboBill);
                    continue;
                }
                if(qboCustomers.get(0).getUser().getEmail().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)){
                    qboBill.setNeedUpdate(false);
                    qboBill.setUpdateStatus(MessageConstants.MSG_USR_ACCOUNT_DELETED);
                    qboBillRepository.save(qboBill);
                    continue;
                }
                ReferenceType customerRef = new ReferenceType();
                customerRef.setValue(customer.getId());
                itemBasedExpenseLineDetail.setCustomerRef(customerRef);
                itemBasedExpenseLineDetail.setBillableStatus(BillableStatusEnum.BILLABLE);
                line.setItemBasedExpenseLineDetail(itemBasedExpenseLineDetail);
                line.setAmount(BigDecimal.valueOf(instructorPayment.getInstructorShare()));
                line.setDetailType(LineDetailTypeEnum.ITEM_BASED_EXPENSE_LINE_DETAIL);
                InvoiceManagement invoiceManagement = invoiceManagementRepository.findByOrderManagement(instructorPayment.getOrderManagement());
                String description = "Invoice Ref" + invoiceManagement.getInvoiceNumber();
                OfferCodeDetailAndOrderMapping offerCodeDetailAndOrderMapping = offerCodeDetailAndOrderMappingRepository.findTop1ByOrderManagementOrderByCreatedDateDesc(instructorPayment.getOrderManagement());
                if(offerCodeDetailAndOrderMapping != null){
                    description += ", Discount applied : " + offerCodeDetailAndOrderMapping.getOfferCodeDetail().getOfferName();
                }
                if(instructorPayment.getOrderManagement().getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_STRIPE)){
                    StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementAndTransactionStatusOrderByModifiedDateDesc(instructorPayment.getOrderManagement(), KeyConstants.KEY_PAID);
                    if(stripePayment != null && stripePayment.getChargeId() != null) {
                        description += " , Stripe charge ref " + stripePayment.getChargeId();
                    }
                }
                line.setDescription(description);
                bill.setPrivateNote(description);
                List<Line> lines = new ArrayList<>();
                lines.add(line);
                bill.setLine(lines);
                bill.setTxnDate(instructorPayment.getOrderManagement().getCreatedDate());
                bill.setDueDate(instructorPayment.getDueDate());
                bill.setDocNumber(instructorPayment.getBillNumber());
                if(qboBill.getBillId() != null){
                    bill.setId(qboBill.getBillId());
                    updateEntity(bill);
                }else{
                    bill = createEntity(bill);
                    qboBill.setBillId(bill.getId());
                }
                if(instructorPayment.getOrderManagement().getSubscribedViaPlatform().getPlatform().equalsIgnoreCase(DBConstants.ANDROID) || qboBill.getInstructorPayment().getOrderManagement().getSubscribedViaPlatform().getPlatform().equalsIgnoreCase(DBConstants.WEB)){
                    qboBill.setIsFixedCCBillCreated(false);
                }else{
                    qboBill.setIsFixedCCBillCreated(true);
                }
                qboBill.setIsCCBillCreated(false);
                qboBill.setNeedUpdate(false);
                qboBill.setUpdateStatus(QboConstants.MSG_UPDATED);
                qboBillRepository.save(qboBill);
            }catch (Exception exception){
                log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
            }
        }
        log.info("Bill sync to QBO end");
    }

    @Async("threadPoolTaskExecutor")
    public void syncDeposit() {
        log.info("Deposit sync to QBO start");
        List<QboDeposit> qboDeposits = qboDepositRepository.findByNeedUpdate(true);
        for(QboDeposit qboDeposit : qboDeposits){
            try{
                Deposit deposit = new Deposit();
                if(qboDeposit.getDepositId() != null){
                    deposit.setId(qboDeposit.getDepositId());
                    deposit = findEntity(deposit);
                }
                InstructorPayment instructorPayment = instructorPaymentRepository.findByOrderManagement(qboDeposit.getInstructorPayment().getOrderManagement());
                if(StringUtils.isEmpty(instructorPayment.getCardType()) && instructorPayment.getOrderManagement().getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_ANET)){
                    AuthNetPayment authNetPayment = authNetPaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(instructorPayment.getOrderManagement().getOrderId());
                    if(!StringUtils.isEmpty(authNetPayment.getTransactionId())){
                        String cardType = paymentService.getCardType(authNetPayment.getTransactionId());
                        if(cardType.equalsIgnoreCase(KeyConstants.KEY_CARD_TYPE_AMERICAN_EXPRESS)){
                            instructorPayment = fitwiseQboEntityService.updateInstructorPayment(instructorPayment, cardType);
                        }
                    }else{
                        qboDeposit.setUpdateStatus(QboConstants.MSG_ANET_TRANSACTION_NOT_FOUND);
                        qboDepositRepository.save(qboDeposit);
                        continue;
                    }
                }
                Account depositToAccount = findAccountByName(generalProperties.getBankAccountName());
                if(depositToAccount == null){
                    qboDeposit.setUpdateStatus(QboConstants.MSG_PROCESSING_ACCOUNT_MISSING);
                    qboDepositRepository.save(qboDeposit);
                    continue;
                }
                ReferenceType depositToAccountReferenceType = new ReferenceType();
                depositToAccountReferenceType.setValue(depositToAccount.getId());
                deposit.setDepositToAccountRef(depositToAccountReferenceType);
                Line linkedTxnLine = new Line();
                linkedTxnLine.setAmount(BigDecimal.valueOf(instructorPayment.getTotalAmt()));
                Payment payment = new Payment();
                payment.setId(qboDeposit.getQboPayment().getQboPaymentId());
                payment = findEntity(payment);
                if(payment == null){
                    qboDeposit.setUpdateStatus(QboConstants.MSG_PAYMENT_MISSING);
                    qboDepositRepository.save(qboDeposit);
                    continue;
                }
                LinkedTxn linkedTxn = new LinkedTxn();
                linkedTxn.setTxnId(payment.getId());
                linkedTxn.setTxnType("Payment");
                linkedTxn.setTxnLineId("0");
                List<LinkedTxn> linkedTxns = new ArrayList<>();
                linkedTxns.add(linkedTxn);
                linkedTxnLine.setLinkedTxn(linkedTxns);
                OrderManagement orderManagement = null;
                if(qboDeposit.getQboPayment().getAuthNetPayment() != null){
                    orderManagement = qboDeposit.getQboPayment().getAuthNetPayment().getOrderManagement();
                }else if(qboDeposit.getQboPayment().getApplePayment() != null){
                    orderManagement = qboDeposit.getQboPayment().getApplePayment().getOrderManagement();
                }else if(qboDeposit.getQboPayment().getStripePayment() != null){
                    orderManagement = qboDeposit.getQboPayment().getStripePayment().getOrderManagement();
                }
                Line depositLine = new Line();
                depositLine.setDetailType(LineDetailTypeEnum.DEPOSIT_LINE_DETAIL);
                depositLine.setAmount(BigDecimal.valueOf(qboDeposit.getInstructorPayment().getProviderCharge() * (-1)));
                log.info("P Chg : " + qboDeposit.getInstructorPayment().getProviderCharge());
                DepositLineDetail depositLineDetail = new DepositLineDetail();
                ReferenceType accountReferenceType = new ReferenceType();
                ReferenceType vendorReferenceType = new ReferenceType();
                String description =  "Variable Credit Card Processing Fees.";
                Account processingFeesAccount = null;
                Vendor entity = null;
                if(orderManagement.getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_ANET)){
                    processingFeesAccount = findAccountByName(QboConstants.ACCOUNT_EXPENSE_CREDIT_CARD_VARIABLE_PROCESSING);
                    entity = findVendorByCompanyName(QboConstants.VENDOR_VISA);
                }else if(orderManagement.getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_APPLE)){
                    processingFeesAccount = findAccountByName(QboConstants.ACCOUNT_EXPENSE_APP_STORE_FEE);
                    entity = findVendorByCompanyName(QboConstants.VENDOR_APPLE);
                    description = "App store commission.";
                }else if(orderManagement.getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_STRIPE)){
                    processingFeesAccount = findAccountByName(QboConstants.ACCOUNT_EXPENSE_STRIPE);
                    entity = findVendorByCompanyName(QboConstants.VENDOR_STRIPE);
                }
                if(processingFeesAccount == null){
                    qboDeposit.setUpdateStatus(QboConstants.MSG_PROCESSING_ACCOUNT_MISSING);
                    qboDepositRepository.save(qboDeposit);
                    continue;
                }
                if(entity == null){
                    qboDeposit.setUpdateStatus(QboConstants.MSG_VENDOR_NOT_AVAILABLE);
                    qboDepositRepository.save(qboDeposit);
                    continue;
                }
                vendorReferenceType.setValue(entity.getId());
                accountReferenceType.setValue(processingFeesAccount.getId());
                depositLineDetail.setAccountRef(accountReferenceType);
                depositLineDetail.setEntity(vendorReferenceType);
                depositLine.setDepositLineDetail(depositLineDetail);
                InvoiceManagement invoiceManagement = invoiceManagementRepository.findByOrderManagement(orderManagement);
                description = description + " Invoice Ref" + invoiceManagement.getInvoiceNumber();
                if(instructorPayment.getOrderManagement().getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_STRIPE)){
                    StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementAndTransactionStatusOrderByModifiedDateDesc(instructorPayment.getOrderManagement(), KeyConstants.KEY_PAID);
                    if(stripePayment != null && stripePayment.getChargeId() != null){
                        description = description + " , Stripe charge ref " + stripePayment.getChargeId();
                    }
                }
                depositLine.setDescription(description);
                deposit.setPrivateNote(description);
                List<Line> lines = new ArrayList<>();
                lines.add(depositLine);
                lines.add(linkedTxnLine);
                deposit.setLine(lines);
                List<QboCustomer> qboCustomers = qboCustomerRepository.findByUser(orderManagement.getUser());
                Customer customer = null;
                String errMsg;
                if(qboCustomers.isEmpty()){
                    QboCustomer qboCustomer = new QboCustomer();
                    qboCustomer.setUser(orderManagement.getUser());
                    qboCustomer.setNeedUpdate(true);
                    qboCustomerRepository.save(qboCustomer);
                    errMsg = QboConstants.MSG_CUSTOMER_MISSING;
                }else{
                    if(qboCustomers.get(0).getCustomerId() != null){
                        customer = new Customer();
                        customer.setId(qboCustomers.get(0).getCustomerId());
                        customer = findEntity(customer);
                        if(customer == null || customer.getId() == null){
                            customer = null;
                            qboCustomers.get(0).setCustomerId(null);
                            qboCustomers.get(0).setNeedUpdate(true);
                            qboCustomerRepository.save(qboCustomers.get(0));
                        }
                    }
                    errMsg = QboConstants.MSG_CUSTOMER_NOT_AVAILABLE;
                }
                if(customer == null){
                    qboDeposit.setUpdateStatus(errMsg);
                    qboDepositRepository.save(qboDeposit);
                    continue;
                }
                if(qboCustomers.get(0).getUser().getEmail().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)){
                    qboDeposit.setNeedUpdate(false);
                    qboDeposit.setUpdateStatus(MessageConstants.MSG_USR_ACCOUNT_DELETED);
                    qboDepositRepository.save(qboDeposit);
                    continue;
                }
                deposit.setDocNumber(qboCustomers.get(0).getDisplayName());
                if(qboDeposit.getDepositId() == null){
                    deposit = createEntity(deposit);
                    qboDeposit.setDepositId(deposit.getId());
                }else{
                    updateEntity(deposit);
                }
                qboDeposit.setNeedUpdate(false);
                qboDepositRepository.save(qboDeposit);
            }catch (Exception exception){
                log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
            }
        }
        log.info("Deposit sync to QBO end");
    }

    @Async("threadPoolTaskExecutor")
    public void syncBillPaymentForBillPaid() {
        log.info("Bill payment for bill paid user");
        List<QboBillPaymentForBillPaid> qboBillPayments = qboBillPaymentBillPaidRepository.findByNeedUpdate(true);
        for(QboBillPaymentForBillPaid qboBillPayment : qboBillPayments){
            try{
                BillPayment billPayment = new BillPayment();
                if(qboBillPayment.getQboBillPaymentId() != null){
                    billPayment.setId(qboBillPayment.getQboBillPaymentId());
                    billPayment = findEntity(billPayment);
                }
                User owner;
                if(KeyConstants.KEY_PROGRAM.equalsIgnoreCase(qboBillPayment.getOrderManagement().getSubscriptionType().getName())){
                    owner = qboBillPayment.getOrderManagement().getProgram().getOwner();
                }else{
                    owner = qboBillPayment.getOrderManagement().getSubscriptionPackage().getOwner();
                }
                List<QboVendor> qboVendors = qboVendorRepository.findByUser(owner);
                Vendor vendor = null;
                String errMsg;
                if(qboVendors.isEmpty()){
                    QboVendor qboVendor = new QboVendor();
                    qboVendor.setUser(owner);
                    qboVendor.setNeedUpdate(true);
                    qboVendorRepository.save(qboVendor);
                    errMsg = QboConstants.MSG_VENDOR_MISSING;
                }else{
                    if(qboVendors.get(0).getVendorId() != null){
                        vendor = new Vendor();
                        vendor.setId(qboVendors.get(0).getVendorId());
                        vendor = findEntity(vendor);
                        if(vendor == null || vendor.getId() == null){
                            vendor = null;
                            qboVendors.get(0).setVendorId(null);
                            qboVendors.get(0).setNeedUpdate(true);
                            qboVendorRepository.save(qboVendors.get(0));
                        }
                    }
                    errMsg = QboConstants.MSG_VENDOR_NOT_AVAILABLE;
                }
                if(vendor == null){
                    qboBillPayment.setUpdateStatus(errMsg);
                    qboBillPaymentBillPaidRepository.save(qboBillPayment);
                    continue;
                }
                if(qboVendors.get(0).getUser().getEmail().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)){
                    qboBillPayment.setNeedUpdate(false);
                    qboBillPayment.setUpdateStatus(MessageConstants.MSG_USR_ACCOUNT_DELETED);
                    qboBillPaymentBillPaidRepository.save(qboBillPayment);
                    continue;
                }
                ReferenceType vendorRef = new ReferenceType();
                vendorRef.setValue(vendor.getId());
                billPayment.setVendorRef(vendorRef);
                List<Line> lines= new ArrayList<>();
                Line billLine = new Line();
                InstructorPayment instructorPayment = instructorPaymentRepository.findByOrderManagement(qboBillPayment.getOrderManagement());
                List<QboBill> qboBills = qboBillRepository.findByInstructorPayment(instructorPayment);
                if(qboBills.isEmpty()){
                    qboBillPayment.setUpdateStatus(MessageConstants.MSG_BILL_NOT_FOUND);
                    qboBillPaymentBillPaidRepository.save(qboBillPayment);
                    continue;
                }
                Bill bill = new Bill();
                bill.setId(qboBills.get(0).getBillId());
                bill = findEntity(bill);
                List<LinkedTxn> linkedTxns = new ArrayList<>();
                LinkedTxn linkedTxn = new LinkedTxn();
                linkedTxn.setTxnId(bill.getId());
                linkedTxn.setTxnType("Bill");
                linkedTxns.add(linkedTxn);
                billLine.setLinkedTxn(linkedTxns);
                billLine.setAmount(BigDecimal.valueOf(instructorPayment.getInstructorShare()));
                InvoiceManagement invoiceManagement = invoiceManagementRepository.findByOrderManagement(qboBillPayment.getOrderManagement());
                String billDescription = "Invoice Ref" + invoiceManagement.getInvoiceNumber();
                if(instructorPayment.getOrderManagement().getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_STRIPE)){
                    StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementAndTransactionStatusOrderByModifiedDateDesc(instructorPayment.getOrderManagement(), KeyConstants.KEY_PAID);
                    billDescription = billDescription + " , Stripe charge ref " + stripePayment.getChargeId();
                }
                billLine.setDescription(billDescription);
                lines.add(billLine);
                billPayment.setLine(lines);
                billPayment.setTotalAmt(BigDecimal.valueOf(instructorPayment.getInstructorShare()));
                billPayment.setPayType(BillPaymentTypeEnum.CHECK);
                Account account = findAccountByName(generalProperties.getBankAccountName());
                if(account == null){
                    qboBillPayment.setUpdateStatus(QboConstants.MSG_PROCESSING_ACCOUNT_MISSING);
                    qboBillPaymentBillPaidRepository.save(qboBillPayment);
                    continue;
                }
                ReferenceType bankAccountReferenceType = new ReferenceType();
                bankAccountReferenceType.setValue(account.getId());
                BillPaymentCheck billPaymentCheck = new BillPaymentCheck();
                billPaymentCheck.setBankAccountRef(bankAccountReferenceType);
                billPayment.setCheckPayment(billPaymentCheck);
                billPayment.setPrivateNote(billDescription);
                if(qboBillPayment.getQboBillPaymentId() == null){
                    billPayment = createEntity(billPayment);
                    qboBillPayment.setQboBillPaymentId(billPayment.getId());
                }else{
                    updateEntity(billPayment);
                }
                qboBillPayment.setNeedUpdate(false);
                qboBillPayment.setUpdateStatus(QboConstants.MSG_UPDATED);
                qboBillPaymentBillPaidRepository.save(qboBillPayment);
            }catch (Exception exception){
                log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
            }
        }
        log.info("Vendor credit sync to QBO end");
    }

    @Async("threadPoolTaskExecutor")
    public void syncRefund() {
        log.info("Refund Receipt sync to QBO start");
        List<QboRefund> qboRefunds = qboRefundRepository.findByNeedUpdate(true);
        for(QboRefund qboRefund : qboRefunds){
            try{
                RefundReceipt refundReceipt = new RefundReceipt();
                if(qboRefund.getQboRefundReceiptId() != null){
                    refundReceipt.setId(qboRefund.getQboRefundReceiptId());
                    refundReceipt = findEntity(refundReceipt);
                }
                Account depositToAccount = findAccountByName(generalProperties.getBankAccountName());
                if(depositToAccount == null){
                    qboRefund.setUpdateStatus(QboConstants.MSG_PROCESSING_ACCOUNT_MISSING);
                    qboRefundRepository.save(qboRefund);
                    continue;
                }
                ReferenceType depositToAccountReferenceType = new ReferenceType();
                depositToAccountReferenceType.setValue(depositToAccount.getId());
                refundReceipt.setDepositToAccountRef(depositToAccountReferenceType);
                SalesItemLineDetail salesItemLineDetail = new SalesItemLineDetail();
                OrderManagement orderManagement = null;
                if(qboRefund.getAuthNetPayment() != null){
                    orderManagement = qboRefund.getAuthNetPayment().getOrderManagement();
                }else if(qboRefund.getApplePayment() != null){
                    orderManagement = qboRefund.getApplePayment().getOrderManagement();
                }else if(qboRefund.getStripePayment() != null){
                    orderManagement = qboRefund.getStripePayment().getOrderManagement();
                }
                String errMsg;
                String productId = "";
                List<QboProduct> qboProducts;
                if(KeyConstants.KEY_PROGRAM.equalsIgnoreCase(orderManagement.getSubscriptionType().getName())){
                    qboProducts = qboProductRepository.findByProgram(orderManagement.getProgram());
                    if(orderManagement.getProgram().getTitle().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)){
                        qboRefund.setNeedUpdate(false);
                        qboRefund.setUpdateStatus(MessageConstants.MSG_PGM_DELETED);
                        qboRefundRepository.save(qboRefund);
                        continue;
                    }
                }else{
                    qboProducts = qboProductRepository.findBySubscriptionPackage(orderManagement.getSubscriptionPackage());
                    if(orderManagement.getSubscriptionPackage().getTitle().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)){
                        qboRefund.setNeedUpdate(false);
                        qboRefund.setUpdateStatus(MessageConstants.MSG_PKG_DELETED);
                        qboRefundRepository.save(qboRefund);
                        continue;
                    }
                }
                Item item = null;
                if(qboProducts.isEmpty()){
                    QboProduct product = new QboProduct();
                    product.setProgram(orderManagement.getProgram());
                    product.setSubscriptionPackage(orderManagement.getSubscriptionPackage());
                    product.setNeedUpdate(true);
                    qboProductRepository.save(product);
                    errMsg = QboConstants.MSG_PRODUCT_MISSING;
                }else{
                    if(orderManagement.getSubscribedViaPlatform().getPlatform().equalsIgnoreCase(DBConstants.ANDROID)){
                        productId = qboProducts.get(0).getProductIdAndroid();
                    }else if(orderManagement.getSubscribedViaPlatform().getPlatform().equalsIgnoreCase(DBConstants.IOS)){
                        productId = qboProducts.get(0).getProductIdIos();
                    }else if(orderManagement.getSubscribedViaPlatform().getPlatform().equalsIgnoreCase(DBConstants.WEB)){
                        productId = qboProducts.get(0).getProductIdWeb();
                    }
                    if(productId != null){
                        item = new Item();
                        item.setId(productId);
                        item = findEntity(item);
                        if(item == null || item.getId() == null){
                            item = null;
                            qboProducts.get(0).setProductIdWeb(null);
                            qboProducts.get(0).setProductIdIos(null);
                            qboProducts.get(0).setProductIdAndroid(null);
                            qboProducts.get(0).setNeedUpdate(true);
                            qboProductRepository.save(qboProducts.get(0));
                        }
                    }
                    errMsg = QboConstants.MSG_PRODUCT_NOT_AVAILABLE;
                }
                if(item == null){
                    qboRefund.setUpdateStatus(errMsg);
                    qboRefundRepository.save(qboRefund);
                    continue;
                }
                ReferenceType itemReference = new ReferenceType();
                itemReference.setValue(item.getId());
                salesItemLineDetail.setItemRef(itemReference);
                List<QboCustomer> qboCustomers = qboCustomerRepository.findByUser(orderManagement.getUser());
                Customer customer = null;
                if(qboCustomers.isEmpty()){
                    QboCustomer qboCustomer = new QboCustomer();
                    qboCustomer.setUser(orderManagement.getUser());
                    qboCustomer.setNeedUpdate(true);
                    qboCustomerRepository.save(qboCustomer);
                    errMsg = QboConstants.MSG_CUSTOMER_MISSING;
                }else{
                    if(qboCustomers.get(0).getCustomerId() != null){
                        customer = new Customer();
                        customer.setId(qboCustomers.get(0).getCustomerId());
                        customer = findEntity(customer);
                        if(customer == null || customer.getId() == null){
                            customer = null;
                            qboCustomers.get(0).setCustomerId(null);
                            qboCustomers.get(0).setNeedUpdate(true);
                            qboCustomerRepository.save(qboCustomers.get(0));
                        }
                    }
                    errMsg = QboConstants.MSG_CUSTOMER_NOT_AVAILABLE;
                }
                if(customer == null){
                    qboRefund.setUpdateStatus(errMsg);
                    qboRefundRepository.save(qboRefund);
                    continue;
                }
                if(qboCustomers.get(0).getUser().getEmail().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)){
                    qboRefund.setNeedUpdate(false);
                    qboRefund.setUpdateStatus(MessageConstants.MSG_USR_ACCOUNT_DELETED);
                    qboRefundRepository.save(qboRefund);
                    continue;
                }
                ReferenceType customerRef = new ReferenceType();
                customerRef.setValue(customer.getId());
                Line line = new Line();
                line.setDetailType(LineDetailTypeEnum.SALES_ITEM_LINE_DETAIL);
                line.setSalesItemLineDetail(salesItemLineDetail);
                if (orderManagement.getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_STRIPE)) {
                    StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementAndTransactionStatusOrderByModifiedDateDesc(orderManagement, KeyConstants.KEY_REFUND);
                    line.setAmount(BigDecimal.valueOf(stripePayment.getAmountRefunded()));
                }else{
                    InstructorPayment instructorPayment = instructorPaymentRepository.findByOrderManagement(orderManagement);
                    line.setAmount(BigDecimal.valueOf(instructorPayment.getTotalAmt()));
                }
                InvoiceManagement invoiceManagement = invoiceManagementRepository.findByOrderManagement(orderManagement);
                Invoice invoice = (Invoice)findInvoiceByDocNumber(invoiceManagement.getInvoiceNumber());
                if(invoice == null){
                    qboRefund.setUpdateStatus(QboConstants.MSG_INVOICE_NOT_AVAILABLE);
                    qboRefundRepository.save(qboRefund);
                    continue;
                }
                String description = "Invoice Ref" + invoiceManagement.getInvoiceNumber();
                if(orderManagement.getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_STRIPE)){
                    StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementAndTransactionStatusOrderByModifiedDateDesc(orderManagement, KeyConstants.KEY_REFUND);
                    description = description + " , Stripe charge ref " + stripePayment.getChargeId() + " , Stripe refund ref " + stripePayment.getRefundTransactionId();
                }
                line.setDescription(description);
                refundReceipt.setPrivateNote(description);
                List<Line> lines = new ArrayList<>();
                lines.add(line);
                refundReceipt.setLine(lines);
                refundReceipt.setCustomerRef(customerRef);
                if(qboRefund.getQboRefundReceiptId() == null){
                    refundReceipt = createEntity(refundReceipt);
                    qboRefund.setQboRefundReceiptId(refundReceipt.getId());
                }else{
                    updateEntity(refundReceipt);
                }
                qboRefund.setNeedUpdate(false);
                qboRefund.setUpdateStatus(QboConstants.MSG_UPDATED);
                qboRefundRepository.save(qboRefund);
            }catch (Exception exception){
                log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
            }
        }
        log.info("Refund receipt sync to QBO end");
    }

    @Async("threadPoolTaskExecutor")
    public void syncVendorCredit() {
        log.info("Vendor credit sync to QBO start");
        List<QboVendorCredit> qboVendorCredits = qboVendorCreditRepository.findByNeedUpdate(true);
        for(QboVendorCredit qboVendorCredit : qboVendorCredits){
            try{
                VendorCredit vendorCredit = new VendorCredit();
                if(qboVendorCredit.getQboVendorCreditId() != null){
                    vendorCredit.setId(qboVendorCredit.getQboVendorCreditId());
                    vendorCredit = findEntity(vendorCredit);
                }
                User owner;
                if(KeyConstants.KEY_PROGRAM.equalsIgnoreCase(qboVendorCredit.getOrderManagement().getSubscriptionType().getName())){
                    owner = qboVendorCredit.getOrderManagement().getProgram().getOwner();
                }else{
                    owner = qboVendorCredit.getOrderManagement().getSubscriptionPackage().getOwner();
                }
                List<QboVendor> qboVendors = qboVendorRepository.findByUser(owner);
                Vendor vendor = null;
                String errMsg;
                if(qboVendors.isEmpty()){
                    QboVendor qboVendor = new QboVendor();
                    qboVendor.setUser(owner);
                    qboVendor.setNeedUpdate(true);
                    qboVendorRepository.save(qboVendor);
                    errMsg = QboConstants.MSG_VENDOR_MISSING;
                }else{
                    if(qboVendors.get(0).getVendorId() != null){
                        vendor = new Vendor();
                        vendor.setId(qboVendors.get(0).getVendorId());
                        vendor = findEntity(vendor);
                        if(vendor == null || vendor.getId() == null){
                            vendor = null;
                            qboVendors.get(0).setVendorId(null);
                            qboVendors.get(0).setNeedUpdate(true);
                            qboVendorRepository.save(qboVendors.get(0));
                        }
                    }
                    errMsg = QboConstants.MSG_VENDOR_NOT_AVAILABLE;
                }
                if(vendor == null){
                    qboVendorCredit.setUpdateStatus(errMsg);
                    qboVendorCreditRepository.save(qboVendorCredit);
                    continue;
                }
                if(qboVendors.get(0).getUser().getEmail().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)){
                    qboVendorCredit.setNeedUpdate(false);
                    qboVendorCredit.setUpdateStatus(MessageConstants.MSG_USR_ACCOUNT_DELETED);
                    qboVendorCreditRepository.save(qboVendorCredit);
                    continue;
                }
                ReferenceType vendorRef = new ReferenceType();
                vendorRef.setValue(vendor.getId());
                vendorCredit.setVendorRef(vendorRef);
                List<Line> lines = new ArrayList<>();
                Line line = new Line();
                ItemBasedExpenseLineDetail itemBasedExpenseLineDetail = new ItemBasedExpenseLineDetail();
                String productId = "";
                List<QboProduct> qboProducts;
                if(KeyConstants.KEY_PROGRAM.equalsIgnoreCase(qboVendorCredit.getOrderManagement().getSubscriptionType().getName())){
                    qboProducts = qboProductRepository.findByProgram(qboVendorCredit.getOrderManagement().getProgram());
                    if(qboVendorCredit.getOrderManagement().getProgram().getTitle().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)){
                        qboVendorCredit.setNeedUpdate(false);
                        qboVendorCredit.setUpdateStatus(MessageConstants.MSG_PGM_DELETED);
                        qboVendorCreditRepository.save(qboVendorCredit);
                        continue;
                    }
                }else {
                    qboProducts = qboProductRepository.findBySubscriptionPackage(qboVendorCredit.getOrderManagement().getSubscriptionPackage());
                    if(qboVendorCredit.getOrderManagement().getSubscriptionPackage().getTitle().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)){
                        qboVendorCredit.setNeedUpdate(false);
                        qboVendorCredit.setUpdateStatus(MessageConstants.MSG_PKG_DELETED);
                        qboVendorCreditRepository.save(qboVendorCredit);
                        continue;
                    }
                }
                Item item = null;
                if(qboProducts.isEmpty()){
                    QboProduct product = new QboProduct();
                    product.setProgram(qboVendorCredit.getOrderManagement().getProgram());
                    product.setNeedUpdate(true);
                    qboProductRepository.save(product);
                    errMsg = QboConstants.MSG_PRODUCT_MISSING;
                }else{
                    if(qboVendorCredit.getOrderManagement().getSubscribedViaPlatform().getPlatform().equalsIgnoreCase(DBConstants.ANDROID)){
                        productId = qboProducts.get(0).getProductIdAndroid();
                    }else if(qboVendorCredit.getOrderManagement().getSubscribedViaPlatform().getPlatform().equalsIgnoreCase(DBConstants.IOS)){
                        productId = qboProducts.get(0).getProductIdIos();
                    }else if(qboVendorCredit.getOrderManagement().getSubscribedViaPlatform().getPlatform().equalsIgnoreCase(DBConstants.WEB)){
                        productId = qboProducts.get(0).getProductIdWeb();
                    }
                    if(productId != null){
                        item = new Item();
                        item.setId(productId);
                        item = findEntity(item);
                        if(item == null || item.getId() == null){
                            item = null;
                            qboProducts.get(0).setProductIdWeb(null);
                            qboProducts.get(0).setProductIdIos(null);
                            qboProducts.get(0).setProductIdAndroid(null);
                            qboProducts.get(0).setNeedUpdate(true);
                            qboProductRepository.save(qboProducts.get(0));
                        }
                    }
                    errMsg = QboConstants.MSG_PRODUCT_NOT_AVAILABLE;
                }
                if(item == null){
                    qboVendorCredit.setUpdateStatus(errMsg);
                    qboVendorCreditRepository.save(qboVendorCredit);
                    continue;
                }
                ReferenceType itemReference = new ReferenceType();
                itemReference.setValue(item.getId());
                itemBasedExpenseLineDetail.setItemRef(itemReference);
                List<QboCustomer> qboCustomers = qboCustomerRepository.findByUser(qboVendorCredit.getOrderManagement().getUser());
                Customer customer = null;
                if(qboCustomers.isEmpty()){
                    QboCustomer qboCustomer = new QboCustomer();
                    qboCustomer.setUser(qboVendorCredit.getOrderManagement().getUser());
                    qboCustomer.setNeedUpdate(true);
                    qboCustomerRepository.save(qboCustomer);
                    errMsg = QboConstants.MSG_CUSTOMER_MISSING;
                }else{
                    if(qboCustomers.get(0).getCustomerId() != null){
                        customer = new Customer();
                        customer.setId(qboCustomers.get(0).getCustomerId());
                        customer = findEntity(customer);
                        if(customer == null || customer.getId() == null){
                            customer = null;
                            qboCustomers.get(0).setCustomerId(null);
                            qboCustomers.get(0).setNeedUpdate(true);
                            qboCustomerRepository.save(qboCustomers.get(0));
                        }
                    }
                    errMsg = QboConstants.MSG_CUSTOMER_NOT_AVAILABLE;
                }
                if(customer == null){
                    qboVendorCredit.setUpdateStatus(errMsg);
                    qboVendorCreditRepository.save(qboVendorCredit);
                    continue;
                }
                if(qboCustomers.get(0).getUser().getEmail().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)){
                    qboVendorCredit.setUpdateStatus(MessageConstants.MSG_USR_ACCOUNT_DELETED);
                    qboVendorCredit.setNeedUpdate(false);
                    qboVendorCreditRepository.save(qboVendorCredit);
                    continue;
                }
                ReferenceType customerRef = new ReferenceType();
                customerRef.setValue(customer.getId());
                itemBasedExpenseLineDetail.setCustomerRef(customerRef);
                line.setItemBasedExpenseLineDetail(itemBasedExpenseLineDetail);
                InstructorPayment instructorPayment = instructorPaymentRepository.findByOrderManagement(qboVendorCredit.getOrderManagement());
                if(StringUtils.isEmpty(instructorPayment.getCardType()) && instructorPayment.getOrderManagement().getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_ANET)){
                    AuthNetPayment authNetPayment = authNetPaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(instructorPayment.getOrderManagement().getOrderId());
                    if(!StringUtils.isEmpty(authNetPayment.getTransactionId())){
                        String cardType = paymentService.getCardType(authNetPayment.getTransactionId());
                        if(cardType.equalsIgnoreCase(KeyConstants.KEY_CARD_TYPE_AMERICAN_EXPRESS)){
                            instructorPayment = fitwiseQboEntityService.updateInstructorPayment(instructorPayment, cardType);
                        }
                    }else{
                        qboVendorCredit.setUpdateStatus(QboConstants.MSG_ANET_TRANSACTION_NOT_FOUND);
                        qboVendorCredit.setNeedUpdate(true);
                        qboVendorCreditRepository.save(qboVendorCredit);
                        continue;
                    }
                }
                if(instructorPayment.getOrderManagement().getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_STRIPE)){
                    line.setAmount(BigDecimal.valueOf(getRefundAmount(instructorPayment)));
                }else{
                    line.setAmount(BigDecimal.valueOf(instructorPayment.getInstructorShare()));
                }
                line.setDetailType(LineDetailTypeEnum.ITEM_BASED_EXPENSE_LINE_DETAIL);
                InvoiceManagement invoiceManagement = invoiceManagementRepository.findByOrderManagement(qboVendorCredit.getOrderManagement());
                String description = "Invoice Ref" + invoiceManagement.getInvoiceNumber();
                if(instructorPayment.getOrderManagement().getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_STRIPE)){
                    StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementAndTransactionStatusOrderByModifiedDateDesc(instructorPayment.getOrderManagement(), KeyConstants.KEY_REFUND);
                    description = description + " , Stripe charge ref " + stripePayment.getChargeId() + " , Stripe refund ref " + stripePayment.getRefundTransactionId();
                }
                line.setDescription(description);
                vendorCredit.setPrivateNote(description);
                lines.add(line);
                vendorCredit.setLine(lines);
                if(qboVendorCredit.getQboVendorCreditId() == null){
                    vendorCredit = createEntity(vendorCredit);
                    qboVendorCredit.setQboVendorCreditId(vendorCredit.getId());
                }else{
                    updateEntity(vendorCredit);
                }
                qboVendorCredit.setNeedUpdate(false);
                qboVendorCredit.setUpdateStatus(QboConstants.MSG_UPDATED);
                qboVendorCreditRepository.save(qboVendorCredit);
            }catch (Exception exception){
                log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
            }
        }
        log.info("Vendor credit sync to QBO end");
    }

    @Async("threadPoolTaskExecutor")
    public void syncBillPayment() {
        log.info("Bill payment sync to QBO start");
        List<QboBillPayment> qboBillPayments = qboBillPaymentRepository.findByNeedUpdate(true);
        for(QboBillPayment qboBillPayment : qboBillPayments){
            try{
                BillPayment billPayment = new BillPayment();
                if(qboBillPayment.getQboBillPaymentId() != null){
                    billPayment.setId(qboBillPayment.getQboBillPaymentId());
                    billPayment = findEntity(billPayment);
                }
                User owner;
                if(KeyConstants.KEY_PROGRAM.equalsIgnoreCase(qboBillPayment.getOrderManagement().getSubscriptionType().getName())){
                    owner = qboBillPayment.getOrderManagement().getProgram().getOwner();
                }else{
                    owner = qboBillPayment.getOrderManagement().getSubscriptionPackage().getOwner();
                }
                List<QboVendor> qboVendors = qboVendorRepository.findByUser(owner);
                Vendor vendor = null;
                String errMsg;
                if(qboVendors.isEmpty()){
                    QboVendor qboVendor = new QboVendor();
                    qboVendor.setUser(owner);
                    qboVendor.setNeedUpdate(true);
                    qboVendorRepository.save(qboVendor);
                    errMsg = QboConstants.MSG_VENDOR_MISSING;
                }else{
                    if(qboVendors.get(0).getVendorId() != null){
                        vendor = new Vendor();
                        vendor.setId(qboVendors.get(0).getVendorId());
                        vendor = findEntity(vendor);
                        if(vendor == null || vendor.getId() == null){
                            vendor = null;
                            qboVendors.get(0).setVendorId(null);
                            qboVendors.get(0).setNeedUpdate(true);
                            qboVendorRepository.save(qboVendors.get(0));
                        }
                    }
                    errMsg = QboConstants.MSG_VENDOR_NOT_AVAILABLE;
                }
                if(vendor == null){
                    qboBillPayment.setUpdateStatus(errMsg);
                    qboBillPaymentRepository.save(qboBillPayment);
                    continue;
                }
                if(qboVendors.get(0).getUser().getEmail().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)){
                    qboBillPayment.setNeedUpdate(false);
                    qboBillPayment.setUpdateStatus(MessageConstants.MSG_USR_ACCOUNT_DELETED);
                    qboBillPaymentRepository.save(qboBillPayment);
                    continue;
                }
                ReferenceType vendorRef = new ReferenceType();
                vendorRef.setValue(vendor.getId());
                billPayment.setVendorRef(vendorRef);
                List<Line> lines= new ArrayList<>();
                Line billLine = new Line();
                InstructorPayment instructorPayment = instructorPaymentRepository.findByOrderManagement(qboBillPayment.getOrderManagement());
                if(StringUtils.isEmpty(instructorPayment.getCardType()) && instructorPayment.getOrderManagement().getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_ANET)){
                    AuthNetPayment authNetPayment = authNetPaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(instructorPayment.getOrderManagement().getOrderId());
                    if(!StringUtils.isEmpty(authNetPayment.getTransactionId())){
                        String cardType = paymentService.getCardType(authNetPayment.getTransactionId());
                        if(cardType.equalsIgnoreCase(KeyConstants.KEY_CARD_TYPE_AMERICAN_EXPRESS)){
                            instructorPayment = fitwiseQboEntityService.updateInstructorPayment(instructorPayment, cardType);
                        }
                    }else{
                        qboBillPayment.setNeedUpdate(true);
                        qboBillPayment.setUpdateStatus(QboConstants.MSG_ANET_TRANSACTION_NOT_FOUND);
                        qboBillPaymentRepository.save(qboBillPayment);
                        continue;
                    }
                }
                List<QboBill> qboBills = qboBillRepository.findByInstructorPayment(instructorPayment);
                Bill bill = new Bill();
                bill.setId(qboBills.get(0).getBillId());
                bill = findEntity(bill);
                List<LinkedTxn> linkedTxns = new ArrayList<>();
                LinkedTxn linkedTxn = new LinkedTxn();
                linkedTxn.setTxnId(bill.getId());
                linkedTxn.setTxnType("Bill");
                linkedTxns.add(linkedTxn);
                billLine.setLinkedTxn(linkedTxns);
                if(instructorPayment.getOrderManagement().getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_STRIPE)){
                    billLine.setAmount(BigDecimal.valueOf(getRefundAmount(instructorPayment)));
                }else{
                    billLine.setAmount(BigDecimal.valueOf(instructorPayment.getInstructorShare()));
                }
                Line vendorCreditLine = new Line();
                InvoiceManagement invoiceManagement = invoiceManagementRepository.findByOrderManagement(qboBillPayment.getOrderManagement());
                String billDescription = "Invoice Ref" + invoiceManagement.getInvoiceNumber();
                String vendorCreditDescription = "The product purchase is refunded to customer. Invoice Ref" + invoiceManagement.getInvoiceNumber();
                if(instructorPayment.getOrderManagement().getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_STRIPE)){
                    StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementAndTransactionStatusOrderByModifiedDateDesc(instructorPayment.getOrderManagement(), KeyConstants.KEY_REFUND);
                    billDescription = billDescription + " , Stripe charge ref " + stripePayment.getChargeId() + " , Stripe refund ref " + stripePayment.getRefundTransactionId();
                    vendorCreditDescription = vendorCreditDescription + " , Stripe charge ref " + stripePayment.getChargeId() + " , Stripe refund ref " + stripePayment.getRefundTransactionId();
                }
                billLine.setDescription(billDescription);
                vendorCreditLine.setDescription(vendorCreditDescription);
                lines.add(billLine);
                List<QboVendorCredit> vendorCredits = qboVendorCreditRepository.findByOrderManagement(qboBillPayment.getOrderManagement());
                VendorCredit vendorCredit = new VendorCredit();
                vendorCredit.setId(vendorCredits.get(0).getQboVendorCreditId());
                vendorCredit = findEntity(vendorCredit);
                List<LinkedTxn> vClinkedTxns = new ArrayList<>();
                LinkedTxn vclinkedTxn = new LinkedTxn();
                vclinkedTxn.setTxnId(vendorCredit.getId());
                vclinkedTxn.setTxnType("VendorCredit");
                vClinkedTxns.add(vclinkedTxn);
                vendorCreditLine.setLinkedTxn(vClinkedTxns);
                vendorCreditLine.setAmount(vendorCredit.getTotalAmt());
                lines.add(vendorCreditLine);
                billPayment.setLine(lines);
                billPayment.setTotalAmt(new BigDecimal(0));
                billPayment.setPayType(BillPaymentTypeEnum.CHECK);
                Account account = findAccountByName(generalProperties.getBankAccountName());
                if(account == null){
                    qboBillPayment.setUpdateStatus(QboConstants.MSG_PROCESSING_ACCOUNT_MISSING);
                    qboBillPaymentRepository.save(qboBillPayment);
                    continue;
                }
                ReferenceType bankAccountReferenceType = new ReferenceType();
                bankAccountReferenceType.setValue(account.getId());
                BillPaymentCheck billPaymentCheck = new BillPaymentCheck();
                billPaymentCheck.setBankAccountRef(bankAccountReferenceType);
                billPayment.setCheckPayment(billPaymentCheck);
                billPayment.setPrivateNote(billDescription);
                if(qboBillPayment.getQboBillPaymentId() == null){
                    billPayment = createEntity(billPayment);
                    qboBillPayment.setQboBillPaymentId(billPayment.getId());
                }else{
                    updateEntity(billPayment);
                }
                qboBillPayment.setNeedUpdate(false);
                qboBillPayment.setUpdateStatus(QboConstants.MSG_UPDATED);
                qboBillPaymentRepository.save(qboBillPayment);
            }catch (Exception exception){
                log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
            }
        }
        log.info("Vendor credit sync to QBO end");
    }

    @Async("threadPoolTaskExecutor")
    public void syncRefundExpense() {
        log.info("Bill payment sync to QBO start");
        List<QboRefundExpense> qboRefundExpenses = qboRefundExpenseRepository.findByNeedUpdate(true);
        for(QboRefundExpense qboRefundExpense : qboRefundExpenses){
            try{
                Purchase purchase = new Purchase();
                if(qboRefundExpense.getQboPurchaseId() != null){
                    purchase.setId(qboRefundExpense.getQboPurchaseId());
                    purchase = findEntity(purchase);
                }
                ReferenceType accountReferenceType = new ReferenceType();
                ReferenceType vendorReferenceType = new ReferenceType();
                Account processingFeesAccount = null;
                Vendor entity = null;
                if(qboRefundExpense.getOrderManagement().getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_APPLE)){
                    processingFeesAccount = findAccountByName(QboConstants.ACCOUNT_EXPENSE_APP_STORE_FEE);
                    entity = findVendorByCompanyName(QboConstants.VENDOR_APPLE);
                }else if(qboRefundExpense.getOrderManagement().getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_ANET)){
                    processingFeesAccount = findAccountByName(QboConstants.ACCOUNT_EXPENSE_CREDIT_CARD_FIXED_PROCESSING);
                    entity = findVendorByCompanyName(QboConstants.VENDOR_VISA);
                }else if(qboRefundExpense.getOrderManagement().getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_STRIPE)){
                    processingFeesAccount = findAccountByName(QboConstants.ACCOUNT_EXPENSE_STRIPE);
                    entity = findVendorByCompanyName(QboConstants.VENDOR_STRIPE);
                }
                if(processingFeesAccount == null){
                    qboRefundExpense.setUpdateStatus(QboConstants.MSG_PROCESSING_ACCOUNT_MISSING);
                    qboRefundExpenseRepository.save(qboRefundExpense);
                    continue;
                }
                if(entity == null){
                    qboRefundExpense.setUpdateStatus(QboConstants.MSG_VENDOR_NOT_AVAILABLE);
                    qboRefundExpenseRepository.save(qboRefundExpense);
                    continue;
                }
                vendorReferenceType.setValue(entity.getId());
                accountReferenceType.setValue(processingFeesAccount.getId());
                purchase.setEntityRef(vendorReferenceType);
                purchase.setPaymentType(PaymentTypeEnum.CHECK);
                Account account = findAccountByName(generalProperties.getBankAccountName());
                if(account == null){
                    qboRefundExpense.setUpdateStatus(QboConstants.MSG_PROCESSING_ACCOUNT_MISSING);
                    qboRefundExpenseRepository.save(qboRefundExpense);
                    continue;
                }
                ReferenceType bankAccountReferenceType = new ReferenceType();
                bankAccountReferenceType.setValue(account.getId());
                purchase.setAccountRef(bankAccountReferenceType);
                List<Line> lines = new ArrayList<>();
                Line line = new Line();
                line.setDetailType(LineDetailTypeEnum.ACCOUNT_BASED_EXPENSE_LINE_DETAIL);
                InstructorPayment instructorPayment = instructorPaymentRepository.findByOrderManagement(qboRefundExpense.getOrderManagement());
                if(instructorPayment.getOrderManagement().getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_STRIPE)){
                    line.setAmount(BigDecimal.valueOf(getRefundAmount(instructorPayment)));
                }else{
                    line.setAmount(BigDecimal.valueOf(qboProperties.getRefundFixedPrice()));
                }
                AccountBasedExpenseLineDetail accountBasedExpenseLineDetail = new AccountBasedExpenseLineDetail();
                accountBasedExpenseLineDetail.setAccountRef(accountReferenceType);
                List<QboCustomer> qboCustomers = qboCustomerRepository.findByUser(qboRefundExpense.getOrderManagement().getUser());
                String errMsg;
                Customer customer = null;
                if(qboCustomers.isEmpty()){
                    QboCustomer qboCustomer = new QboCustomer();
                    qboCustomer.setUser(qboRefundExpense.getOrderManagement().getUser());
                    qboCustomer.setNeedUpdate(true);
                    qboCustomerRepository.save(qboCustomer);
                    errMsg = QboConstants.MSG_CUSTOMER_MISSING;
                }else{
                    if(qboCustomers.get(0).getCustomerId() != null){
                        customer = new Customer();
                        customer.setId(qboCustomers.get(0).getCustomerId());
                        customer = findEntity(customer);
                        if(customer == null || customer.getId() == null){
                            customer = null;
                            qboCustomers.get(0).setCustomerId(null);
                            qboCustomers.get(0).setNeedUpdate(true);
                            qboCustomerRepository.save(qboCustomers.get(0));
                        }
                    }
                    errMsg = QboConstants.MSG_CUSTOMER_NOT_AVAILABLE;
                }
                if(customer == null){
                    qboRefundExpense.setUpdateStatus(errMsg);
                    qboRefundExpenseRepository.save(qboRefundExpense);
                    continue;
                }
                if(qboCustomers.get(0).getUser().getEmail().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)){
                    qboRefundExpense.setNeedUpdate(false);
                    qboRefundExpense.setUpdateStatus(MessageConstants.MSG_USR_ACCOUNT_DELETED);
                    qboRefundExpenseRepository.save(qboRefundExpense);
                    continue;
                }
                ReferenceType customerRef = new ReferenceType();
                customerRef.setValue(customer.getId());
                accountBasedExpenseLineDetail.setCustomerRef(customerRef);
                line.setAccountBasedExpenseLineDetail(accountBasedExpenseLineDetail);
                InvoiceManagement invoiceManagement = invoiceManagementRepository.findByOrderManagement(qboRefundExpense.getOrderManagement());
                if(instructorPayment.getOrderManagement().getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_STRIPE)){
                    StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementAndTransactionStatusOrderByModifiedDateDesc(instructorPayment.getOrderManagement(), KeyConstants.KEY_REFUND);
                    line.setDescription("Refund processing fee by Stripe. Invoice Ref " + invoiceManagement.getInvoiceNumber() + " , Stripe charge ref " + stripePayment.getChargeId()  + " , Stripe refund ref " + stripePayment.getRefundTransactionId());
                    purchase.setPrivateNote("Invoice Ref " + invoiceManagement.getInvoiceNumber() + " , Stripe charge ref " + stripePayment.getChargeId()  + " , Stripe refund ref " + stripePayment.getRefundTransactionId());
                }else if(instructorPayment.getOrderManagement().getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_ANET)){
                    line.setDescription("Refund processing fee by Auth.net. Invoice Ref" + invoiceManagement.getInvoiceNumber());
                    purchase.setPrivateNote("Invoice Ref" + invoiceManagement.getInvoiceNumber());
                }else if(instructorPayment.getOrderManagement().getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_APPLE)){
                    line.setDescription("Refund processing fee by Apple. Invoice Ref" + invoiceManagement.getInvoiceNumber());
                    purchase.setPrivateNote("Invoice Ref" + invoiceManagement.getInvoiceNumber());
                }
                lines.add(line);
                purchase.setLine(lines);
                if(qboRefundExpense.getQboPurchaseId() == null){
                    purchase = createEntity(purchase);
                    qboRefundExpense.setQboPurchaseId(purchase.getId());
                }else{
                    updateEntity(purchase);
                }
                qboRefundExpense.setNeedUpdate(false);
                qboRefundExpense.setUpdateStatus(QboConstants.MSG_UPDATED);
                qboRefundExpenseRepository.save(qboRefundExpense);
            }catch (Exception exception){
                log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
            }
        }
        log.info("Vendor credit sync to QBO end");
    }

    public double getRefundAmount(InstructorPayment instructorPayment) {
        StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementAndTransactionStatusOrderByModifiedDateDesc(instructorPayment.getOrderManagement(), KeyConstants.KEY_REFUND);
        return getRefundAmount(instructorPayment, stripePayment);
    }

    public double getRefundAmount(InstructorPayment instructorPayment, StripePayment stripePayment) {
        double tempRefundAmt;
        if( stripePayment.getAmountRefunded() > (instructorPayment.getTotalAmt() - instructorPayment.getProviderCharge())){
            tempRefundAmt = instructorPayment.getTotalAmt() - instructorPayment.getProviderCharge();
        }else{
            tempRefundAmt = stripePayment.getAmountRefunded();
        }
        double refundAmt;
        if(stripePayment.getAmountRefunded() == instructorPayment.getTotalAmt()){
            refundAmt = instructorPayment.getInstructorShare();
        }else{
            double instructorShare = 100d - qboProperties.getFitwiseShare();
            refundAmt = (instructorShare * tempRefundAmt) / 100;
        }
        return refundAmt;
    }

    /**
     * Sending email for the QBO token exception
     */
    private void sendEmailAuthFailure(){
        asyncMailer.sendAsyncTextMail(generalProperties.getQboAuthExpiryNotificationToEmailAddresses(), "QBO Auth failed", "QBO Authentication failed. Please check the token expiry.");
    }

    public void syncAllEntities(){
        syncUsers();
        syncProducts();
        syncInvoice();
        syncBill();
        syncPayment();
        syncDeposit();
        syncBillPaymentForBillPaid();
        syncRefund();
        syncVendorCredit();
        syncBillPayment();
        syncRefundExpense();
        syncDepositInsufficientBalance();
        syncBillPaymentInsufficientBalance();
    }

    private void refreshToken() throws OAuthException {
        try {
            OAuth2PlatformClient client  = factory.getOAuth2PlatformClient();
            String refreshToken = redisService.get(QboConstants.KEY_REFRESH_TOKEN);
            BearerTokenResponse bearerTokenResponse = client.refreshToken(refreshToken);
            redisService.set(QboConstants.KEY_ACCESS_TOKEN,bearerTokenResponse.getAccessToken());
            redisService.set(QboConstants.KEY_REFRESH_TOKEN, bearerTokenResponse.getRefreshToken());
        } catch (OAuthException e1) {
            log.error("Error while calling bearer token :: " + e1.getMessage());
            sendEmailAuthFailure();
            throw e1;
        }
    }

    private Item getProductMainCategory(String categoryName) throws OAuthException, FMSException, IOException {
        Item item = new Item();
        List<QboProductCategory> qboProductCategories = qboProductCategoryRepository.findTop1ByCategoryName(categoryName);
        if(qboProductCategories.isEmpty()){
            item.setType(ItemTypeEnum.CATEGORY);
            item.setName(categoryName);
            try{
                item = createEntity(item);
            }catch (FMSException exception){
                if(!categoryName.contains("'")){
                    item = findCategoryByName(categoryName);
                    if(item == null){
                        throw new IOException();
                    }
                }
            }
            QboProductCategory qboProductCategory = new QboProductCategory();
            qboProductCategory.setCategoryName(categoryName);
            qboProductCategory.setProductCategoryId(item.getId());
            qboProductCategoryRepository.save(qboProductCategory);
        }else{
            item.setId(qboProductCategories.get(0).getProductCategoryId());
            item = findEntity(item);
            if(item == null){
                throw new IOException();
            }
        }
        return item;
    }

    private Item getProductCategory(QboProduct qboProduct, Vendor vendor, Item parentCategory) throws OAuthException, FMSException {
        Item item = null;
        String productTitle;
        List<QboProductCategory> qboProductCategories;
        if(qboProduct.getTier() != null){
            qboProductCategories = qboProductCategoryRepository.findTop1ByTier(qboProduct.getTier());
            productTitle = qboProduct.getTier().getTierType().replaceAll("[^a-zA-Z0-9]", " ");
        } else if(qboProduct.getProgram() != null){
            qboProductCategories = qboProductCategoryRepository.findTop1ByProgram(qboProduct.getProgram());
            productTitle = qboProduct.getProgram().getTitle().replaceAll("[^a-zA-Z0-9]", " ");
        }else{
            qboProductCategories = qboProductCategoryRepository.findTop1BySubscriptionPackage(qboProduct.getSubscriptionPackage());
            productTitle = qboProduct.getSubscriptionPackage().getTitle().replaceAll("[^a-zA-Z0-9]", " ");
        }
        QboProductCategory qboProductCategory;
        if (vendor != null) {
            productTitle = productTitle + " By " + vendor.getDisplayName();
        }
        if(!qboProductCategories.isEmpty()){
            qboProductCategory = qboProductCategories.get(0);
            item = new Item();
            item.setId(qboProductCategory.getProductCategoryId());
            item = findEntity(item);
            //Validation on Category exist on V1.6.6
            if(item == null || item.getName() == null){
                item = null;
            }else if(item.getName() != null && !item.getName().equalsIgnoreCase(productTitle)) {
                item.setName(productTitle);
                qboProductCategory.setCategoryName(productTitle);
                item = updateEntity(item);
                qboProductCategory.setProductCategoryId(item.getId());
                qboProductCategoryRepository.save(qboProductCategory);
            }
        }else{
            qboProductCategory = new QboProductCategory();
            if(qboProduct.getTier() != null) {
                qboProductCategory.setTier(qboProduct.getTier());
            }else if(qboProduct.getProgram() != null){
                qboProductCategory.setProgram(qboProduct.getProgram());
            }else{
                qboProductCategory.setSubscriptionPackage(qboProduct.getSubscriptionPackage());
            }
        }
        if(item == null){
            item = new Item();
            item.setType(ItemTypeEnum.CATEGORY);
            item.setName(productTitle);
            ReferenceType categoryReferenceType = new ReferenceType();
            categoryReferenceType.setValue(parentCategory.getId());
            categoryReferenceType.setName(parentCategory.getName());
            item.setParentRef(categoryReferenceType);
            item.setSubItem(true);
            try{
                item = createEntity(item);
            }catch (FMSException exception){
                if(!(productTitle).contains("'")){
                    item = findCategoryByName(productTitle);
                }
            }
            qboProductCategory.setCategoryName(productTitle);
            if(item != null){
                qboProductCategory.setProductCategoryId(item.getId());
            }
            qboProductCategoryRepository.save(qboProductCategory);
        }
        return item;
    }

    private Item findCategoryByName(String incomeAccountName) throws FMSException, OAuthException{
        Item item = null;
        List<? extends  IEntity> entityList = queryData("select * from Item where Type='Category' AND Name = '" + incomeAccountName + "'");
        if(entityList != null && entityList.size() > 0){
            item = (Item) entityList.get(0);
        }
        return item;
    }

    public QboVendorBillPayment updateVendorBillPayment(Entity entity) {
        long profilingStartTimeInMillis;
        long profilingEndTimeInMillis;
        log.info("Vendor Bill Payment update");
        QboVendorBillPayment vendorBillPayment = null;
        try{
            BillPayment billPayment = new BillPayment();
            billPayment.setId(entity.getId());
            billPayment = findEntity(billPayment);
            log.info("Bill Payment Id : " + billPayment.getId());
            QboBill qboBill = null;
            boolean isVendorCredit = false;
            profilingStartTimeInMillis = new Date().getTime();
            for(LinkedTxn linkedTxn : billPayment.getLine().get(0).getLinkedTxn()){
                if(linkedTxn.getTxnType().equalsIgnoreCase("VendorCredit")){
                    isVendorCredit = true;
                }
                if(linkedTxn.getTxnType().equalsIgnoreCase("Bill")){
                    log.info("Bill Txn " + linkedTxn.getTxnId());
                    Bill bill = new Bill();
                    bill.setId(linkedTxn.getTxnId());
                    bill = findEntity(bill);
                    List<QboBill> qboBills = qboBillRepository.findByBillId(bill.getId());
                    if(!qboBills.isEmpty()){
                        qboBill = qboBills.get(0);
                    }
                }
            }
            profilingEndTimeInMillis = new Date().getTime();
            log.info("Iterating bill payments and getting qbo bill : Time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));
            if(qboBill != null && !isVendorCredit){
                profilingStartTimeInMillis = new Date().getTime();
                List<QboVendorBillPayment> vendorBillPayments = qboVendorBillPaymentRepository.findByQboBillPaymentId(billPayment.getId());
                vendorBillPayment = new QboVendorBillPayment();
                if(!vendorBillPayments.isEmpty()){
                    vendorBillPayment = vendorBillPayments.get(0);
                }
                vendorBillPayment.setQboBill(qboBill);
                vendorBillPayment.setQboBillPaymentId(billPayment.getId());
                vendorBillPayment.setSettlementAmt(billPayment.getTotalAmt());
                qboVendorBillPaymentRepository.save(vendorBillPayment);
                profilingEndTimeInMillis = new Date().getTime();
                log.info("Vendor bill payment update : time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));

                /*
                 * Updating payout method
                 */
                profilingStartTimeInMillis = new Date().getTime();
                InstructorPayment instructorPayment = qboBill.getInstructorPayment();
                if (instructorPayment != null) {
                    Date txnDate = billPayment.getTxnDate();
                    if (txnDate != null) {
                        instructorPayment.setTransferDate(txnDate);
                    }

                    if (instructorPayment.getTransferMode() == null) {
                        instructorPayment.setTransferMode(QboConstants.QBO_PAYPAL_PAYOUT_MODE);
                    }
                    instructorPayment.setIsTransferDone(true);
                    instructorPayment.setIsTransferAttempted(true);
                    instructorPaymentRepository.save(instructorPayment);
                }
                profilingEndTimeInMillis = new Date().getTime();
                log.info("Instructor payment update : time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));
            }
        }catch (Exception exception){
            log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
        }
        return vendorBillPayment;
    }

    public void syncDepositInsufficientBalance() {
        log.info("Deposit insufficient balance sync to QBO start");
        List<QboDepositInsufficientBalance> qboDepositInsufficientBalances = qboDepositInsufficientBalanceRepository.findByNeedUpdate(true);
        for(QboDepositInsufficientBalance qboDeposit : qboDepositInsufficientBalances){
            try{
                Deposit deposit = new Deposit();
                if(qboDeposit.getDepositId() != null){
                    deposit.setId(qboDeposit.getDepositId());
                    deposit = findEntity(deposit);
                }
                OrderManagement orderManagement = qboDeposit.getOrderManagement();
                InstructorPayment instructorPayment = instructorPaymentRepository.findByOrderManagement(orderManagement);
                Account depositToAccount = findAccountByName(generalProperties.getBankAccountName());
                if(depositToAccount == null){
                    qboDeposit.setUpdateStatus(QboConstants.MSG_PROCESSING_ACCOUNT_MISSING);
                    qboDepositInsufficientBalanceRepository.save(qboDeposit);
                    continue;
                }
                ReferenceType depositToAccountReferenceType = new ReferenceType();
                depositToAccountReferenceType.setValue(depositToAccount.getId());
                deposit.setDepositToAccountRef(depositToAccountReferenceType);
                Line linkedTxnLine = new Line();
                linkedTxnLine.setAmount(BigDecimal.valueOf(qboDeposit.getDebitAmount()));
                VendorCredit vendorCredit = new VendorCredit();
                List<QboVendorCredit> qboVendorCredits = qboVendorCreditRepository.findByOrderManagement(qboDeposit.getOrderManagement());
                if(qboVendorCredits.isEmpty() || qboVendorCredits.get(0) == null){
                    continue;
                }
                vendorCredit.setId(qboVendorCredits.get(0).getQboVendorCreditId());
                vendorCredit = findEntity(vendorCredit);
                if(vendorCredit == null){
                    qboDeposit.setUpdateStatus(QboConstants.MSG_PAYMENT_MISSING);
                    qboDepositInsufficientBalanceRepository.save(qboDeposit);
                    continue;
                }
                LinkedTxn linkedTxn = new LinkedTxn();
                linkedTxn.setTxnId(vendorCredit.getId());
                linkedTxn.setTxnType("VendorCredit");
                linkedTxn.setTxnLineId("0");
                List<LinkedTxn> linkedTxns = new ArrayList<>();
                linkedTxns.add(linkedTxn);
                linkedTxnLine.setLinkedTxn(linkedTxns);
                Line depositLine = new Line();
                depositLine.setDetailType(LineDetailTypeEnum.DEPOSIT_LINE_DETAIL);
                depositLine.setAmount(BigDecimal.valueOf(qboDeposit.getDebitAmount()));
                DepositLineDetail depositLineDetail = new DepositLineDetail();
                ReferenceType accountReferenceType = new ReferenceType();
                ReferenceType vendorReferenceType = new ReferenceType();
                String description =  "Deposit against credit.";
                User productOwner;
                if(KeyConstants.KEY_PROGRAM.equalsIgnoreCase(orderManagement.getSubscriptionType().getName())){
                    productOwner = orderManagement.getProgram().getOwner();
                }else{
                    productOwner = orderManagement.getSubscriptionPackage().getOwner();
                }
                List<QboVendor> qboVendors = qboVendorRepository.findByUser(productOwner);
                Vendor vendor = null;
                String errMsg;
                if(qboVendors.isEmpty()){
                    QboVendor qboVendor = new QboVendor();
                    qboVendor.setUser(productOwner);
                    qboVendor.setNeedUpdate(true);
                    qboVendorRepository.save(qboVendor);
                    errMsg = QboConstants.MSG_VENDOR_MISSING;
                }else{
                    if(qboVendors.get(0).getVendorId() != null){
                        vendor = new Vendor();
                        vendor.setId(qboVendors.get(0).getVendorId());
                        vendor = findEntity(vendor);
                        if(vendor == null || vendor.getId() == null){
                            vendor = null;
                            qboVendors.get(0).setVendorId(null);
                            qboVendors.get(0).setNeedUpdate(true);
                            qboVendorRepository.save(qboVendors.get(0));
                        }
                    }
                    errMsg = QboConstants.MSG_VENDOR_NOT_AVAILABLE;
                }
                if(vendor == null){
                    qboDeposit.setUpdateStatus(errMsg);
                    qboDepositInsufficientBalanceRepository.save(qboDeposit);
                    continue;
                }
                if(qboVendors.get(0).getUser().getEmail().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)){
                    qboDeposit.setNeedUpdate(false);
                    qboDeposit.setUpdateStatus(MessageConstants.MSG_USR_ACCOUNT_DELETED);
                    qboDepositInsufficientBalanceRepository.save(qboDeposit);
                    continue;
                }
                Account account = findAccountByName(QboConstants.ACCOUNT_DEFAULT_PAYABLE);
                if(account == null){
                    qboDeposit.setUpdateStatus(QboConstants.MSG_PROCESSING_ACCOUNT_MISSING);
                    qboDepositInsufficientBalanceRepository.save(qboDeposit);
                    continue;
                }
                vendorReferenceType.setValue(vendor.getId());
                accountReferenceType.setValue(account.getId());
                depositLineDetail.setAccountRef(accountReferenceType);
                depositLineDetail.setEntity(vendorReferenceType);
                depositLine.setDepositLineDetail(depositLineDetail);
                InvoiceManagement invoiceManagement = invoiceManagementRepository.findByOrderManagement(orderManagement);
                description = description + " Invoice Ref" + invoiceManagement.getInvoiceNumber();
                if(instructorPayment.getOrderManagement().getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_STRIPE)){
                    StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementAndTransactionStatusOrderByModifiedDateDesc(instructorPayment.getOrderManagement(), KeyConstants.KEY_PAID);
                    if(stripePayment != null && stripePayment.getChargeId() != null){
                        description = description + " , Stripe charge ref " + stripePayment.getChargeId();
                    }
                }
                depositLine.setDescription(description);
                deposit.setPrivateNote(description);
                List<Line> lines = new ArrayList<>();
                lines.add(depositLine);
                deposit.setLine(lines);
                List<QboCustomer> qboCustomers = qboCustomerRepository.findByUser(orderManagement.getUser());
                Customer customer = null;
                if(qboCustomers.isEmpty()){
                    QboCustomer qboCustomer = new QboCustomer();
                    qboCustomer.setUser(orderManagement.getUser());
                    qboCustomer.setNeedUpdate(true);
                    qboCustomerRepository.save(qboCustomer);
                    errMsg = QboConstants.MSG_CUSTOMER_MISSING;
                }else{
                    if(qboCustomers.get(0).getCustomerId() != null){
                        customer = new Customer();
                        customer.setId(qboCustomers.get(0).getCustomerId());
                        customer = findEntity(customer);
                        if(customer == null || customer.getId() == null){
                            customer = null;
                            qboCustomers.get(0).setCustomerId(null);
                            qboCustomers.get(0).setNeedUpdate(true);
                            qboCustomerRepository.save(qboCustomers.get(0));
                        }
                    }
                    errMsg = QboConstants.MSG_CUSTOMER_NOT_AVAILABLE;
                }
                if(customer == null){
                    qboDeposit.setUpdateStatus(errMsg);
                    qboDepositInsufficientBalanceRepository.save(qboDeposit);
                    continue;
                }
                if(qboCustomers.get(0).getUser().getEmail().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)){
                    qboDeposit.setNeedUpdate(false);
                    qboDeposit.setUpdateStatus(MessageConstants.MSG_USR_ACCOUNT_DELETED);
                    qboDepositInsufficientBalanceRepository.save(qboDeposit);
                    continue;
                }
                deposit.setDocNumber(qboCustomers.get(0).getDisplayName());
                deposit.setTotalAmt(BigDecimal.valueOf(qboDeposit.getDebitAmount()));
                if(qboDeposit.getDepositId() == null){
                    deposit = createEntity(deposit);
                    qboDeposit.setDepositId(deposit.getId());
                }else{
                    updateEntity(deposit);
                }
                qboDeposit.setNeedUpdate(false);
                qboDepositInsufficientBalanceRepository.save(qboDeposit);
            }catch (Exception exception){
                log.error(exception.getMessage());
            }
        }
        log.info("Deposit insufficient balance sync to QBO end");
    }

    public void syncBillPaymentInsufficientBalance() {
        log.info("Bill payment for insufficient balance sync to QBO start");
        List<QboBillPaymentInsufficientBalance> qboBillPayments = qboBillPaymentInsufficientBalanceRepository.findByNeedUpdate(true);
        for(QboBillPaymentInsufficientBalance qboBillPayment : qboBillPayments){
            try{
                BillPayment billPayment = new BillPayment();
                if(qboBillPayment.getQboBillPaymentId() != null){
                    billPayment.setId(qboBillPayment.getQboBillPaymentId());
                    billPayment = findEntity(billPayment);
                }
                OrderManagement orderManagement = qboBillPayment.getOrderManagement();
                User productOwner;
                if(KeyConstants.KEY_PROGRAM.equalsIgnoreCase(orderManagement.getSubscriptionType().getName())){
                    productOwner = orderManagement.getProgram().getOwner();
                }else{
                    productOwner = orderManagement.getSubscriptionPackage().getOwner();
                }
                List<QboVendor> qboVendors = qboVendorRepository.findByUser(productOwner);
                Vendor vendor = null;
                String errMsg;
                if(qboVendors.isEmpty()){
                    QboVendor qboVendor = new QboVendor();
                    qboVendor.setUser(productOwner);
                    qboVendor.setNeedUpdate(true);
                    qboVendorRepository.save(qboVendor);
                    errMsg = QboConstants.MSG_VENDOR_MISSING;
                }else{
                    if(qboVendors.get(0).getVendorId() != null){
                        vendor = new Vendor();
                        vendor.setId(qboVendors.get(0).getVendorId());
                        vendor = findEntity(vendor);
                        if(vendor == null || vendor.getId() == null){
                            vendor = null;
                            qboVendors.get(0).setVendorId(null);
                            qboVendors.get(0).setNeedUpdate(true);
                            qboVendorRepository.save(qboVendors.get(0));
                        }
                    }
                    errMsg = QboConstants.MSG_VENDOR_NOT_AVAILABLE;
                }
                if(vendor == null){
                    qboBillPayment.setUpdateStatus(errMsg);
                    qboBillPaymentInsufficientBalanceRepository.save(qboBillPayment);
                    continue;
                }
                if(qboVendors.get(0).getUser().getEmail().equalsIgnoreCase(KeyConstants.KEY_ANONYMOUS)){
                    qboBillPayment.setNeedUpdate(false);
                    qboBillPayment.setUpdateStatus(MessageConstants.MSG_USR_ACCOUNT_DELETED);
                    qboBillPaymentInsufficientBalanceRepository.save(qboBillPayment);
                    continue;
                }
                ReferenceType vendorRef = new ReferenceType();
                vendorRef.setValue(vendor.getId());
                billPayment.setVendorRef(vendorRef);
                billPayment.setPayType(BillPaymentTypeEnum.CHECK);
                billPayment.setTotalAmt(new BigDecimal(0));
                Account account = findAccountByName(generalProperties.getBankAccountName());
                if(account == null){
                    qboBillPayment.setUpdateStatus(QboConstants.MSG_PROCESSING_ACCOUNT_MISSING);
                    qboBillPaymentInsufficientBalanceRepository.save(qboBillPayment);
                    continue;
                }
                ReferenceType bankAccountReferenceType = new ReferenceType();
                bankAccountReferenceType.setValue(account.getId());
                BillPaymentCheck billPaymentCheck = new BillPaymentCheck();
                billPaymentCheck.setBankAccountRef(bankAccountReferenceType);
                billPayment.setCheckPayment(billPaymentCheck);
                InvoiceManagement invoiceManagement = invoiceManagementRepository.findByOrderManagement(orderManagement);
                String vendorCreditDescription = "The product purchase is refunded to customer. Invoice Ref" + invoiceManagement.getInvoiceNumber();
                if(orderManagement.getModeOfPayment().equalsIgnoreCase(PaymentConstants.MODE_OF_PAYMENT_STRIPE)){
                    StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementOrderByModifiedDateDesc(orderManagement);
                    vendorCreditDescription = vendorCreditDescription + " , Stripe charge ref " + stripePayment.getChargeId();
                }
                billPayment.setPrivateNote(vendorCreditDescription);
                List<Line> lines= new ArrayList<>();

                Deposit deposit = new Deposit();
                deposit.setId(qboBillPayment.getDepositInsufficientBalance().getDepositId());
                deposit = findEntity(deposit);
                List<LinkedTxn> linkedTxns = new ArrayList<>();
                LinkedTxn linkedTxn = new LinkedTxn();
                linkedTxn.setTxnId(deposit.getId());
                linkedTxn.setTxnType("Deposit");
                linkedTxns.add(linkedTxn);
                Line depositLine = new Line();
                depositLine.setLinkedTxn(linkedTxns);
                depositLine.setAmount(deposit.getTotalAmt());

                Line vendorCreditLine = new Line();
                vendorCreditLine.setDescription(vendorCreditDescription);
                List<QboVendorCredit> vendorCredits = qboVendorCreditRepository.findByOrderManagement(orderManagement);
                VendorCredit vendorCredit = new VendorCredit();
                vendorCredit.setId(vendorCredits.get(0).getQboVendorCreditId());
                vendorCredit = findEntity(vendorCredit);
                List<LinkedTxn> vClinkedTxns = new ArrayList<>();
                LinkedTxn vclinkedTxn = new LinkedTxn();
                vclinkedTxn.setTxnId(vendorCredit.getId());
                vclinkedTxn.setTxnType("VendorCredit");
                vClinkedTxns.add(vclinkedTxn);
                vendorCreditLine.setLinkedTxn(vClinkedTxns);
                vendorCreditLine.setAmount(deposit.getTotalAmt());
                lines.add(depositLine);
                lines.add(vendorCreditLine);
                billPayment.setLine(lines);

                if(qboBillPayment.getQboBillPaymentId() == null){
                    billPayment = createEntity(billPayment);
                    qboBillPayment.setQboBillPaymentId(billPayment.getId());
                }else{
                    updateEntity(billPayment);
                }
                qboBillPayment.setNeedUpdate(false);
                qboBillPayment.setUpdateStatus(QboConstants.MSG_UPDATED);
                qboBillPaymentInsufficientBalanceRepository.save(qboBillPayment);
            }catch (Exception exception){
                log.error(exception.getMessage());
            }
        }
        log.info("Bill payment for insufficient balance sync to QBO end");
    }

    public void retrieveVendorBillPaidTransactions(){
        log.info("Validating the transaction for paid bills");
        try{
            List<QboBill> qboBills = qboBillRepository.findByNeedUpdateAndBillPaidAndInstructorPaymentDueDateLessThan(false, false, new Date());
            log.info("Bills TO proceed : " + qboBills.size());
            for(QboBill qboBill : qboBills){
                try{
                    //Validate the existing paid vendor bills
                    List<QboVendorBillPayment> qboVendorBillPayments = qboVendorBillPaymentRepository.findByQboBill(qboBill);
                    log.info("Paid Details : " + qboVendorBillPayments.size());
                    BigDecimal paidAmount = BigDecimal.valueOf(0);
                    if(!qboVendorBillPayments.isEmpty()){
                        for(QboVendorBillPayment vendorBillPayment : qboVendorBillPayments){
                            paidAmount = paidAmount.add(vendorBillPayment.getSettlementAmt());
                        }
                    }
                    BigDecimal instShare = BigDecimal.valueOf(qboBill.getInstructorPayment().getInstructorShare()).setScale(2, RoundingMode.HALF_EVEN);
                    if(paidAmount.doubleValue() == instShare.doubleValue()){
                        qboBill.setBillPaid(true);
                    }else{
                        log.info("Getting bill details : " + qboBill.getInstructorPayment().getBillNumber());
                        paidAmount = new BigDecimal(0);
                        Bill bill = new Bill();
                        bill.setId(qboBill.getBillId());
                        bill = findEntity(bill);
                        log.info("Bill Id : " + bill.getId());
                        for(LinkedTxn linkedTxn : bill.getLinkedTxn()){
                            log.info("Bill Txn : " + linkedTxn.getTxnType());
                            if(linkedTxn.getTxnType().equalsIgnoreCase("BillPaymentCheck")){
                                log.info("Bill Txn : " + linkedTxn.getTxnType());
                                Entity entity = new Entity();
                                entity.setId(linkedTxn.getTxnId());
                                QboVendorBillPayment vendorBillPayment = updateVendorBillPayment(entity);
                                log.info("Settlement Amt : " + vendorBillPayment.getSettlementAmt());
                                paidAmount = paidAmount.add(vendorBillPayment.getSettlementAmt());
                            }
                        }
                        if(bill.getBalance().doubleValue() == 0){
                            qboBill.setBillPaid(true);
                        }
                        log.info("Bill balance : " + bill.getBalance());
                    }
                    qboBill.setPaidAmount(paidAmount);
                    qboBillRepository.save(qboBill);
                }catch (Exception exceptionInBillSync){
                    log.info(exceptionInBillSync.getMessage());
                }
            }
        }catch (Exception exception){
            log.error(exception.getMessage());
        }
        log.info("Validating the transaction Completed");
    }
}