package com.fitwise.service.payment.authorizenet;

import com.fitwise.configuration.StaticContextAccessor;
import com.fitwise.properties.AuthorizeNetProperties;
import com.fitwise.utils.ValidationUtils;
import net.authorize.api.contract.v1.DeleteCustomerPaymentProfileRequest;
import net.authorize.api.contract.v1.DeleteCustomerPaymentProfileResponse;
import net.authorize.api.contract.v1.MerchantAuthenticationType;
import net.authorize.api.contract.v1.MessageTypeEnum;
import net.authorize.api.controller.DeleteCustomerPaymentProfileController;
import net.authorize.api.controller.base.ApiOperationBase;

public class DeleteCustomerPaymentProfile {

    public static DeleteCustomerPaymentProfileResponse run(String customerProfileId, String customerPaymentProfileId) {

        String env = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getEnvironment();
        String loginId = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getLoginId();
        String transactionKey = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getTransactionKey();

        ApiOperationBase.setEnvironment(ValidationUtils.authorizeNetEnvironment(env));
        MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType();
        merchantAuthenticationType.setName(loginId);
        merchantAuthenticationType.setTransactionKey(transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        DeleteCustomerPaymentProfileRequest apiRequest = new DeleteCustomerPaymentProfileRequest();
        apiRequest.setCustomerProfileId(customerProfileId);
        apiRequest.setCustomerPaymentProfileId(customerPaymentProfileId);

        DeleteCustomerPaymentProfileController controller = new DeleteCustomerPaymentProfileController(apiRequest);
        controller.execute();

        DeleteCustomerPaymentProfileResponse response = new DeleteCustomerPaymentProfileResponse();
        response = controller.getApiResponse();

        if (response != null) {

            if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {

                System.out.println(response.getMessages().getMessage().get(0).getCode());
                System.out.println(response.getMessages().getMessage().get(0).getText());
            } else {
                System.out.println("Failed to delete customer payment profile:  " + response.getMessages().getResultCode());
            }
        }
        return response;
    }
}
