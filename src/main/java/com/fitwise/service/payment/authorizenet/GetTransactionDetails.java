package com.fitwise.service.payment.authorizenet;

import com.fitwise.configuration.StaticContextAccessor;
import com.fitwise.properties.AuthorizeNetProperties;
import com.fitwise.utils.ValidationUtils;
import com.google.gson.Gson;
import net.authorize.api.contract.v1.*;
import net.authorize.api.controller.GetTransactionDetailsController;
import net.authorize.api.controller.base.ApiOperationBase;

public class GetTransactionDetails {

    public static GetTransactionDetailsResponse run(String transactionId) {

        String env = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getEnvironment();
        String loginId = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getLoginId();
        String transactionKey = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getTransactionKey();

        ApiOperationBase.setEnvironment(ValidationUtils.authorizeNetEnvironment(env));
        MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType();
        merchantAuthenticationType.setName(loginId);
        merchantAuthenticationType.setTransactionKey(transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        GetTransactionDetailsRequest getRequest = new GetTransactionDetailsRequest();
        getRequest.setMerchantAuthentication(merchantAuthenticationType);
        getRequest.setTransId(transactionId);

        GetTransactionDetailsController controller = new GetTransactionDetailsController(getRequest);
        controller.execute();
        GetTransactionDetailsResponse getResponse = controller.getApiResponse();

        if (getResponse != null) {

            if (getResponse.getMessages().getResultCode() == MessageTypeEnum.OK) {
                String result = new Gson().toJson(getResponse);
                System.out.println("Get Transaction detail response ---------------------->" + result);

                System.out.println(getResponse.getMessages().getMessage().get(0).getCode());
                System.out.println(getResponse.getMessages().getMessage().get(0).getText());
            } else {
                System.out.println("Failed to get transaction details:  " + getResponse.getMessages().getResultCode());
            }
        }
        return getResponse;
    }
}