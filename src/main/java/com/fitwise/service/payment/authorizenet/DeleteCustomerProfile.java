package com.fitwise.service.payment.authorizenet;

import com.fitwise.configuration.StaticContextAccessor;
import com.fitwise.properties.AuthorizeNetProperties;
import com.fitwise.utils.ValidationUtils;
import net.authorize.api.contract.v1.*;
import net.authorize.api.controller.DeleteCustomerProfileController;
import net.authorize.api.controller.base.ApiOperationBase;

public class DeleteCustomerProfile {

    public static ANetApiResponse run( String customerProfileId) {

        String env = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getEnvironment();
        String loginId = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getLoginId();
        String transactionKey = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getTransactionKey();

        ApiOperationBase.setEnvironment(ValidationUtils.authorizeNetEnvironment(env));
        MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType();
        merchantAuthenticationType.setName(loginId);
        merchantAuthenticationType.setTransactionKey(transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        DeleteCustomerProfileRequest apiRequest = new DeleteCustomerProfileRequest();
        apiRequest.setCustomerProfileId(customerProfileId);

        DeleteCustomerProfileController controller = new DeleteCustomerProfileController(apiRequest);
        controller.execute();

        DeleteCustomerProfileResponse response = new DeleteCustomerProfileResponse();
        response = controller.getApiResponse();

        if (response != null) {

            if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {

                System.out.println(response.getMessages().getMessage().get(0).getCode());
                System.out.println(response.getMessages().getMessage().get(0).getText());
            } else {
                System.out.println("Failed to delete customer profile:  " + response.getMessages().getResultCode());
            }
        }
        return response;
    }
}
