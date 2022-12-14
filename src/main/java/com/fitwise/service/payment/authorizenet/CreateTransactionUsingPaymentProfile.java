package com.fitwise.service.payment.authorizenet;

import com.fitwise.configuration.StaticContextAccessor;
import com.fitwise.constants.KeyConstants;
import com.fitwise.properties.AuthorizeNetProperties;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.payment.authorizenet.ANetCustomerProfileRequestView;
import net.authorize.api.contract.v1.*;
import net.authorize.api.controller.CreateTransactionController;
import net.authorize.api.controller.base.ApiOperationBase;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fitwise.response.payment.authorize.net.ANetTransactionResponse;

public class CreateTransactionUsingPaymentProfile {

    public static ANetTransactionResponse run(ANetCustomerProfileRequestView customerProfile, Double amount, String refTransId) {

        String env = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getEnvironment();
        String loginId = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getLoginId();
        String transactionKey = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getTransactionKey();

        ApiOperationBase.setEnvironment(ValidationUtils.authorizeNetEnvironment(env));
        MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType();
        merchantAuthenticationType.setName(loginId);
        merchantAuthenticationType.setTransactionKey(transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);


        // Set the profile ID to charge
        CustomerProfilePaymentType profileToCharge = new CustomerProfilePaymentType();
        profileToCharge.setCustomerProfileId(customerProfile.getCustomerProfileId());
        PaymentProfile paymentProfile = new PaymentProfile();
        paymentProfile.setPaymentProfileId(customerProfile.getCustomerPaymentProfileId());
        profileToCharge.setPaymentProfile(paymentProfile);

        // Create the payment transaction request
        TransactionRequestType txnRequest = new TransactionRequestType();
        txnRequest.setTransactionType(TransactionTypeEnum.AUTH_CAPTURE_TRANSACTION.value());
        txnRequest.setProfile(profileToCharge);
        txnRequest.setAmount(new BigDecimal(amount).setScale(2, RoundingMode.CEILING));
        txnRequest.setRefTransId(refTransId); //Invoice id of fitwise

        CreateTransactionRequest apiRequest = new CreateTransactionRequest();
        apiRequest.setTransactionRequest(txnRequest);
        CreateTransactionController controller = new CreateTransactionController(apiRequest);
        controller.execute();

        CreateTransactionResponse response = controller.getApiResponse();
        ANetTransactionResponse aNetTransactionResponse = new ANetTransactionResponse();

        if (response != null) {
            aNetTransactionResponse.setResponseCode(response.getTransactionResponse().getResponseCode());

            // If API Response is ok, go ahead and check the transaction response
            if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {
                TransactionResponse result = response.getTransactionResponse();
                if (result.getMessages() != null) {
                    aNetTransactionResponse.setTransactionId(result.getTransId());
                    aNetTransactionResponse.setTransactionStatus(KeyConstants.KEY_PAYMENT_PENDING);
                    System.out.println("Successfully created transaction with Transaction ID: " + result.getTransId());
                    System.out.println("Response Code: " + result.getResponseCode());
                    System.out.println("Message Code: " + result.getMessages().getMessage().get(0).getCode());
                    System.out.println("Description: " + result.getMessages().getMessage().get(0).getDescription());
                    System.out.println("Auth Code: " + result.getAuthCode());
                } else {
                    System.out.println("Failed Transaction.");
                    aNetTransactionResponse.setTransactionStatus(KeyConstants.KEY_PAYMENT_FAILURE);
                    if (response.getTransactionResponse().getErrors() != null) {
                        aNetTransactionResponse.setErrorCode(response.getTransactionResponse().getErrors().getError().get(0).getErrorCode());
                        aNetTransactionResponse.setErrorMessage(response.getTransactionResponse().getErrors().getError().get(0).getErrorText());
                        System.out.println("Error Code: " + response.getTransactionResponse().getErrors().getError().get(0).getErrorCode());
                        System.out.println("Error message: " + response.getTransactionResponse().getErrors().getError().get(0).getErrorText());
                    }
                }
            } else {
                System.out.println("Failed Transaction.");
                aNetTransactionResponse.setTransactionStatus(KeyConstants.KEY_PAYMENT_FAILURE);
                if (response.getTransactionResponse() != null && response.getTransactionResponse().getErrors() != null) {
                    System.out.println("Error Code: " + response.getTransactionResponse().getErrors().getError().get(0).getErrorCode());
                    System.out.println("Error message: " + response.getTransactionResponse().getErrors().getError().get(0).getErrorText());
                    aNetTransactionResponse.setErrorCode(response.getTransactionResponse().getErrors().getError().get(0).getErrorCode());
                    aNetTransactionResponse.setErrorMessage(response.getTransactionResponse().getErrors().getError().get(0).getErrorText());
                } else {
                    System.out.println("Error Code: " + response.getMessages().getMessage().get(0).getCode());
                    System.out.println("Error message: " + response.getMessages().getMessage().get(0).getText());
                    aNetTransactionResponse.setErrorCode(response.getMessages().getMessage().get(0).getCode());
                    aNetTransactionResponse.setErrorMessage(response.getMessages().getMessage().get(0).getText());
                }
            }
        } else {
            System.out.println("Null Response.");
            aNetTransactionResponse.setTransactionStatus(KeyConstants.KEY_PAYMENT_FAILURE);
            aNetTransactionResponse.setErrorMessage("Failure. Null Response");
        }

        return aNetTransactionResponse;
    }
}
