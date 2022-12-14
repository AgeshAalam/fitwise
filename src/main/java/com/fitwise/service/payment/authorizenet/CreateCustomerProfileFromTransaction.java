package com.fitwise.service.payment.authorizenet;

import com.fitwise.configuration.StaticContextAccessor;
import com.fitwise.properties.AuthorizeNetProperties;
import com.fitwise.utils.ValidationUtils;
import net.authorize.api.contract.v1.CreateCustomerProfileFromTransactionRequest;
import net.authorize.api.contract.v1.CreateCustomerProfileResponse;
import net.authorize.api.contract.v1.MerchantAuthenticationType;
import net.authorize.api.controller.CreateCustomerProfileFromTransactionController;
import net.authorize.api.controller.base.ApiOperationBase;

public class CreateCustomerProfileFromTransaction {

    public static CreateCustomerProfileResponse run(String transactionId) {

        String env = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getEnvironment();
        String loginId = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getLoginId();
        String transactionKey = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getTransactionKey();

        ApiOperationBase.setEnvironment(ValidationUtils.authorizeNetEnvironment(env));
        MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType();
        merchantAuthenticationType.setName(loginId);
        merchantAuthenticationType.setTransactionKey(transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        CreateCustomerProfileFromTransactionRequest transaction_request = new CreateCustomerProfileFromTransactionRequest();
        transaction_request.setTransId(transactionId);

        CreateCustomerProfileFromTransactionController createProfileController = new CreateCustomerProfileFromTransactionController(transaction_request);
        createProfileController.execute();
        CreateCustomerProfileResponse customer_response = createProfileController.getApiResponse();

        if (customer_response != null) {
            System.out.println(transaction_request.getTransId());
        }
        return customer_response;
    }
}
