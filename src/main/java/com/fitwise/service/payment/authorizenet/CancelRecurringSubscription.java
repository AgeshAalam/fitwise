package com.fitwise.service.payment.authorizenet;


import com.fitwise.configuration.StaticContextAccessor;
import com.fitwise.properties.AuthorizeNetProperties;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.payment.authorizenet.CancelRecurringSubscriptionRequestView;
import net.authorize.api.contract.v1.*;
import net.authorize.api.controller.base.ApiOperationBase;
import net.authorize.api.controller.ARBCancelSubscriptionController;

/**
 * This class is used to cancel a recurring subscription
 */
public class CancelRecurringSubscription {

    public static ANetApiResponse run(CancelRecurringSubscriptionRequestView cancelRecurringSubscriptionRequestView) {
        //Common code to set for all requests
        String env = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getEnvironment();
        String loginId = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getLoginId();
        String transactionKey = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getTransactionKey();

        ApiOperationBase.setEnvironment(ValidationUtils.authorizeNetEnvironment(env));
        MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType();
        merchantAuthenticationType.setName(loginId);
        merchantAuthenticationType.setTransactionKey(transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        // Make the API Request
        ARBCancelSubscriptionRequest apiRequest = new ARBCancelSubscriptionRequest();
        apiRequest.setSubscriptionId(cancelRecurringSubscriptionRequestView.getSubscriptionId());
        ARBCancelSubscriptionController controller = new ARBCancelSubscriptionController(apiRequest);
        controller.execute();
        ARBCancelSubscriptionResponse response = controller.getApiResponse();
        if (response != null) {

            if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {

                System.out.println(response.getMessages().getMessage().get(0).getCode());
                System.out.println(response.getMessages().getMessage().get(0).getText());
            } else {
                System.out.println("Failed to cancel Subscription:  " + response.getMessages().getResultCode());
            }
        }
        return response;
    }
}