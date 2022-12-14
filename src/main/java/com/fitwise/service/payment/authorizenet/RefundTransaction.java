package com.fitwise.service.payment.authorizenet;

import com.fitwise.configuration.StaticContextAccessor;
import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.properties.AuthorizeNetProperties;
import com.fitwise.utils.ValidationUtils;
import net.authorize.api.contract.v1.*;
import net.authorize.api.controller.CreateTransactionController;
import net.authorize.api.controller.base.ApiOperationBase;

import java.math.BigDecimal;

public class RefundTransaction {

    //
    // Run this sample from command line with:
    //                 java -jar target/ChargeCreditCard-jar-with-dependencies.jar
    //
    public static CreateTransactionResponse run(Double transactionAmount, String transactionID) {

        GetTransactionDetailsResponse transactionDetailsResponse = GetTransactionDetails.run(transactionID);
        if (transactionDetailsResponse == null || transactionDetailsResponse.getTransaction() == null
                || transactionDetailsResponse.getTransaction().getPayment() == null
                || transactionDetailsResponse.getTransaction().getPayment().getCreditCard() == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_INVALID_TRANSACTION_ID, null);
        }
        String maskedCardNumber = transactionDetailsResponse.getTransaction().getPayment().getCreditCard().getCardNumber();

        //Common code to set for all requests
        String env = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getEnvironment();
        String apiLoginId = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getLoginId();
        String transactionKey = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getTransactionKey();

        //Common code to set for all requests
        ApiOperationBase.setEnvironment(ValidationUtils.authorizeNetEnvironment(env));

        MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType();
        merchantAuthenticationType.setName(apiLoginId);
        merchantAuthenticationType.setTransactionKey(transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        // Create a payment object, last 4 of the credit card and expiration date are required
        PaymentType paymentType = new PaymentType();
        CreditCardType creditCard = new CreditCardType();
        creditCard.setCardNumber(maskedCardNumber);
        creditCard.setExpirationDate("XXXX"); // Can be masked for refund
        paymentType.setCreditCard(creditCard);

        // Create the payment transaction request
        TransactionRequestType txnRequest = new TransactionRequestType();
        txnRequest.setTransactionType(TransactionTypeEnum.REFUND_TRANSACTION.value());
        txnRequest.setRefTransId(transactionID);
        txnRequest.setAmount(new BigDecimal(transactionAmount.toString()));
        txnRequest.setPayment(paymentType);

        // Make the API Request
        CreateTransactionRequest apiRequest = new CreateTransactionRequest();
        apiRequest.setTransactionRequest(txnRequest);
        CreateTransactionController controller = new CreateTransactionController(apiRequest);
        controller.execute();

        CreateTransactionResponse response = controller.getApiResponse();

        if (response != null) {
            // If API Response is ok, go ahead and check the transaction response
            if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {
                TransactionResponse result = response.getTransactionResponse();
                if (result.getMessages() != null) {
                    System.out.println("Successfully created transaction with Transaction ID: " + result.getTransId());
                    System.out.println("Response Code: " + result.getResponseCode());
                    System.out.println("Message Code: " + result.getMessages().getMessage().get(0).getCode());
                    System.out.println("Description: " + result.getMessages().getMessage().get(0).getDescription());
                    System.out.println("Auth Code: " + result.getAuthCode());
                } else {
                    System.out.println("Failed Transaction.");
                    if (response.getTransactionResponse().getErrors() != null) {
                        System.out.println("Error Code: " + response.getTransactionResponse().getErrors().getError().get(0).getErrorCode());
                        System.out.println("Error message: " + response.getTransactionResponse().getErrors().getError().get(0).getErrorText());
                    }
                }
            } else {
                System.out.println("Failed Transaction.");
                if (response.getTransactionResponse() != null && response.getTransactionResponse().getErrors() != null) {
                    System.out.println("Error Code: " + response.getTransactionResponse().getErrors().getError().get(0).getErrorCode());
                    System.out.println("Error message: " + response.getTransactionResponse().getErrors().getError().get(0).getErrorText());
                } else {
                    System.out.println("Error Code: " + response.getMessages().getMessage().get(0).getCode());
                    System.out.println("Error message: " + response.getMessages().getMessage().get(0).getText());
                }
            }
        } else {
            System.out.println("Null Response.");
        }
        return response;
    }

}

