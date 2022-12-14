package com.fitwise.service.payment.authorizenet;

import com.fitwise.configuration.StaticContextAccessor;
import com.fitwise.properties.AuthorizeNetProperties;
import com.fitwise.utils.ValidationUtils;
import net.authorize.api.contract.v1.*;

import net.authorize.api.contract.v1.MerchantAuthenticationType;
import net.authorize.api.controller.base.ApiOperationBase;
import net.authorize.api.controller.GetCustomerPaymentProfileController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetCustomerPaymentProfile {

    public static GetCustomerPaymentProfileResponse run(String customerProfileId,
                                      String customerPaymentProfileId) {

        Logger logger = LoggerFactory.getLogger(GetCustomerPaymentProfile.class);

        String env = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getEnvironment();
        String loginId = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getLoginId();
        String transactionKey = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getTransactionKey();

        ApiOperationBase.setEnvironment(ValidationUtils.authorizeNetEnvironment(env));
        MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType();
        merchantAuthenticationType.setName(loginId);
        merchantAuthenticationType.setTransactionKey(transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        GetCustomerPaymentProfileRequest apiRequest = new GetCustomerPaymentProfileRequest();
        apiRequest.setCustomerProfileId(customerProfileId);
        apiRequest.setCustomerPaymentProfileId(customerPaymentProfileId);

        GetCustomerPaymentProfileController controller = new GetCustomerPaymentProfileController(apiRequest);
        controller.execute();

        GetCustomerPaymentProfileResponse response = new GetCustomerPaymentProfileResponse();
        response = controller.getApiResponse();

        if (response!=null) {

            if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {

             /*   logger.info(response.getMessages().getMessage().get(0).getCode());
                logger.info(response.getMessages().getMessage().get(0).getText());

                logger.info(response.getPaymentProfile().getBillTo().getFirstName());
                logger.info(response.getPaymentProfile().getBillTo().getLastName());
                logger.info(response.getPaymentProfile().getBillTo().getCompany());
                logger.info(response.getPaymentProfile().getBillTo().getAddress());
                logger.info(response.getPaymentProfile().getBillTo().getCity());
                logger.info(response.getPaymentProfile().getBillTo().getState());
                logger.info(response.getPaymentProfile().getBillTo().getZip());
                logger.info(response.getPaymentProfile().getBillTo().getCountry());
                logger.info(response.getPaymentProfile().getBillTo().getPhoneNumber());
                logger.info(response.getPaymentProfile().getBillTo().getFaxNumber());

                logger.info(response.getPaymentProfile().getCustomerPaymentProfileId());

                logger.info(response.getPaymentProfile().getPayment().getCreditCard().getCardNumber());
                logger.info(response.getPaymentProfile().getPayment().getCreditCard().getExpirationDate());*/

                if ((response.getPaymentProfile().getSubscriptionIds() != null) && (response.getPaymentProfile().getSubscriptionIds().getSubscriptionId() != null) &&
                        (!response.getPaymentProfile().getSubscriptionIds().getSubscriptionId().isEmpty())) {
                    logger.info("List of subscriptions:");
                    for (String subscriptionid : response.getPaymentProfile().getSubscriptionIds().getSubscriptionId())
                        logger.info(subscriptionid);
                }
            } else {
                logger.info("Failed to get customer payment profile:  " + response.getMessages().getResultCode());
            }
        }
        return response;
    }
}
