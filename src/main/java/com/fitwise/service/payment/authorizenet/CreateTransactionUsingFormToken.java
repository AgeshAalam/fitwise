package com.fitwise.service.payment.authorizenet;

import com.fitwise.configuration.StaticContextAccessor;
import com.fitwise.constants.KeyConstants;
import com.fitwise.properties.AuthorizeNetProperties;
import com.fitwise.response.payment.authorize.net.CustomerProfile;
import com.fitwise.response.payment.authorize.net.ANetTransactionResponse;
import com.fitwise.response.payment.authorize.net.PaymentProfile;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.payment.authorizenet.ANetOneTimeProgramSubscriptionUsingCardRequestView;
import net.authorize.Environment;
import net.authorize.api.contract.v1.*;
import net.authorize.api.controller.CreateTransactionController;
import net.authorize.api.controller.base.ApiOperationBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that is used to initiate an one time payment transaction via Mobile apps.
 * The card data entered in Mobile apps will be converted to payment nonce(Encrypted form of Entered credit card info)
 * by passing it to Authorize.net SDK.
 * This will be happening in Client side itself. We won't get any Credit card information from Client apps to our BE
 * instead Payment nonce will be passed.
 */
public class CreateTransactionUsingFormToken {

    public static ANetTransactionResponse run(ANetOneTimeProgramSubscriptionUsingCardRequestView oneTimeSubscriptionRequestView, Long userId, Double subscriptionAmount, String refTransId) {

        Logger logger = LoggerFactory.getLogger(CreateTransactionUsingFormToken.class);

        //Common code to set for all requests

        String env = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getEnvironment();
        String loginId = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getLoginId();
        String transactionKey = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getTransactionKey();

        Environment environment = ValidationUtils.authorizeNetEnvironment(env);

        ApiOperationBase.setEnvironment(environment);
        MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType();
        merchantAuthenticationType.setName(loginId);
        merchantAuthenticationType.setTransactionKey(transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        // Populate the payment data
        PaymentType paymentType = new PaymentType();
        OpaqueDataType OpaqueData = new OpaqueDataType();
        OpaqueData.setDataDescriptor("COMMON.ACCEPT.INAPP.PAYMENT");
        OpaqueData.setDataValue(oneTimeSubscriptionRequestView.getFormToken());
        paymentType.setOpaqueData(OpaqueData);

        // Creating the customer profile for the transaction
        CustomerDataType customer = new CustomerDataType();
        customer.setId(String.valueOf(userId));

        CustomerAddressType billToInfo = new CustomerAddressType();
        billToInfo.setFirstName(oneTimeSubscriptionRequestView.getFirstName());
        billToInfo.setLastName(oneTimeSubscriptionRequestView.getLastName());
        billToInfo.setAddress(oneTimeSubscriptionRequestView.getAddress());
        billToInfo.setCity(oneTimeSubscriptionRequestView.getCity());
        billToInfo.setState(oneTimeSubscriptionRequestView.getState());
        billToInfo.setCountry(oneTimeSubscriptionRequestView.getCountry());
        billToInfo.setZip(oneTimeSubscriptionRequestView.getZip());

        // Create the payment transaction request
        TransactionRequestType txnRequest = new TransactionRequestType();
        txnRequest.setTransactionType(TransactionTypeEnum.AUTH_CAPTURE_TRANSACTION.value());
        txnRequest.setAmount(BigDecimal.valueOf(subscriptionAmount).setScale(2, RoundingMode.CEILING));
        txnRequest.setPayment(paymentType);
        txnRequest.setCustomer(customer);
        txnRequest.setRefTransId(refTransId); //Invoice id of fitwise
        txnRequest.setBillTo(billToInfo);

        // Make the API Request
        CreateTransactionRequest apiRequest = new CreateTransactionRequest();
        apiRequest.setTransactionRequest(txnRequest);
        CreateTransactionController controller = new CreateTransactionController(apiRequest);
        controller.execute();

        CreateTransactionResponse response = controller.getApiResponse();
        ANetTransactionResponse aNetTransactionResponse = new ANetTransactionResponse();

        if (response != null) {
            // If API Response is ok, go ahead and check the transaction response
            aNetTransactionResponse.setResponseCode(response.getTransactionResponse().getResponseCode());

            if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {
                net.authorize.api.contract.v1.TransactionResponse result = response.getTransactionResponse();
                if (result.getMessages() != null) {
                    aNetTransactionResponse.setTransactionId(result.getTransId());
                    aNetTransactionResponse.setTransactionStatus(KeyConstants.KEY_PAYMENT_PENDING);
                    logger.info("Successfully created transaction with Transaction ID: " + result.getTransId());
                    logger.info("Response Code: " + result.getResponseCode());
                    logger.info("Message Code: " + result.getMessages().getMessage().get(0).getCode());
                    logger.info("Description: " + result.getMessages().getMessage().get(0).getDescription());
                    logger.info("Auth Code: " + result.getAuthCode());

                    /*
                     * Creating Customer profile and payment profile only if the user gives us permission to save card data!
                     */
                    if (oneTimeSubscriptionRequestView.isDoSaveCardData()) {

                        /**
                         * Based on the transaction id, we got create a customer profile for the user
                         * Customer profile consists of Payment profile inside it
                         */
                        CreateCustomerProfileResponse customerProfileResponse = CreateCustomerProfileFromTransaction.run(result.getTransId());

                        if (customerProfileResponse != null && customerProfileResponse.getCustomerProfileId() != null &&
                                customerProfileResponse.getCustomerPaymentProfileIdList() != null
                                && !customerProfileResponse.getCustomerPaymentProfileIdList().getNumericString().isEmpty()) {

                            CustomerProfile customerProfile = new CustomerProfile();
                            customerProfile.setCustomerProfileId(customerProfileResponse.getCustomerProfileId());
                            List<PaymentProfile> paymentProfileList = new ArrayList<>();

                            PaymentProfile paymentProfile = new PaymentProfile();
                            for (String s : customerProfileResponse.getCustomerPaymentProfileIdList().getNumericString()) {
                                paymentProfile.setPaymentProfileId(s);
                                paymentProfileList.add(paymentProfile);
                            }

                            customerProfile.setPaymentProfileList(paymentProfileList);
                            aNetTransactionResponse.setCustomerProfile(customerProfile);
                        }
                    }
                } else {
                    logger.info("Failed Transaction.");
                    aNetTransactionResponse.setTransactionStatus(KeyConstants.KEY_PAYMENT_FAILURE);
                    if (response.getTransactionResponse().getErrors() != null) {
                        aNetTransactionResponse.setErrorCode(response.getTransactionResponse().getErrors().getError().get(0).getErrorCode());
                        aNetTransactionResponse.setErrorMessage(response.getTransactionResponse().getErrors().getError().get(0).getErrorText());
                        logger.info("Error Code: " + response.getTransactionResponse().getErrors().getError().get(0).getErrorCode());
                        logger.info("Error message: " + response.getTransactionResponse().getErrors().getError().get(0).getErrorText());
                    }
                }
            } else {
                logger.info("Failed Transaction.");
                aNetTransactionResponse.setTransactionStatus(KeyConstants.KEY_PAYMENT_FAILURE);
                if (response.getTransactionResponse() != null && response.getTransactionResponse().getErrors() != null) {
                    logger.info("Error Code: " + response.getTransactionResponse().getErrors().getError().get(0).getErrorCode());
                    logger.info("Error message: " + response.getTransactionResponse().getErrors().getError().get(0).getErrorText());
                    aNetTransactionResponse.setErrorCode(response.getTransactionResponse().getErrors().getError().get(0).getErrorCode());
                    aNetTransactionResponse.setErrorMessage(response.getTransactionResponse().getErrors().getError().get(0).getErrorText());
                } else {
                    logger.info("Error Code: " + response.getMessages().getMessage().get(0).getCode());
                    logger.info("Error message: " + response.getMessages().getMessage().get(0).getText());
                    aNetTransactionResponse.setErrorCode(response.getMessages().getMessage().get(0).getCode());
                    aNetTransactionResponse.setErrorMessage(response.getMessages().getMessage().get(0).getText());
                }
            }
        } else {
            logger.info("Null Response.");
            aNetTransactionResponse.setTransactionStatus(KeyConstants.KEY_PAYMENT_FAILURE);
            aNetTransactionResponse.setErrorMessage("Failure. Null Response");
        }

        return aNetTransactionResponse;
    }


}

