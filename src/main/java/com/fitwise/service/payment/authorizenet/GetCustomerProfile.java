package com.fitwise.service.payment.authorizenet;

import com.fitwise.configuration.StaticContextAccessor;
import com.fitwise.properties.AuthorizeNetProperties;
import com.fitwise.utils.ValidationUtils;
import net.authorize.api.contract.v1.*;

import net.authorize.api.contract.v1.MerchantAuthenticationType;
import net.authorize.api.controller.base.ApiOperationBase;
import net.authorize.api.controller.GetCustomerProfileController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetCustomerProfile {

    public static GetCustomerProfileResponse run(String customerProfileId) {

        Logger logger = LoggerFactory.getLogger(GetCustomerProfile.class);

        String env = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getEnvironment();
        String loginId = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getLoginId();
        String transactionKey = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getTransactionKey();

        ApiOperationBase.setEnvironment(ValidationUtils.authorizeNetEnvironment(env));
        MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType();
        merchantAuthenticationType.setName(loginId);
        merchantAuthenticationType.setTransactionKey(transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        GetCustomerProfileRequest apiRequest = new GetCustomerProfileRequest();
        apiRequest.setCustomerProfileId(customerProfileId);

        GetCustomerProfileController controller = new GetCustomerProfileController(apiRequest);
        controller.execute();

        GetCustomerProfileResponse response = new GetCustomerProfileResponse();
        response = controller.getApiResponse();

        if (response != null) {

            if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {

                logger.info(response.getMessages().getMessage().get(0).getCode());
                logger.info(response.getMessages().getMessage().get(0).getText());

                logger.info(response.getProfile().getMerchantCustomerId());
                logger.info(response.getProfile().getDescription());
                logger.info(response.getProfile().getEmail());
                logger.info(response.getProfile().getCustomerProfileId());

                if ((!response.getProfile().getPaymentProfiles().isEmpty()) &&
                        (response.getProfile().getPaymentProfiles().get(0).getBillTo() != null)) {
                    logger.info(response.getProfile().getPaymentProfiles().get(0).getBillTo().getFirstName());
                    logger.info(response.getProfile().getPaymentProfiles().get(0).getBillTo().getLastName());
                    logger.info(response.getProfile().getPaymentProfiles().get(0).getBillTo().getCompany());
                    logger.info(response.getProfile().getPaymentProfiles().get(0).getBillTo().getAddress());
                    logger.info(response.getProfile().getPaymentProfiles().get(0).getBillTo().getCity());
                    logger.info(response.getProfile().getPaymentProfiles().get(0).getBillTo().getState());
                    logger.info(response.getProfile().getPaymentProfiles().get(0).getBillTo().getZip());
                    logger.info(response.getProfile().getPaymentProfiles().get(0).getBillTo().getCountry());
                    logger.info(response.getProfile().getPaymentProfiles().get(0).getBillTo().getPhoneNumber());
                    logger.info(response.getProfile().getPaymentProfiles().get(0).getBillTo().getFaxNumber());

                    logger.info(response.getProfile().getPaymentProfiles().get(0).getCustomerPaymentProfileId());

                    logger.info(response.getProfile().getPaymentProfiles().get(0).getPayment().getCreditCard().getCardNumber());
                    logger.info(response.getProfile().getPaymentProfiles().get(0).getPayment().getCreditCard().getExpirationDate());
                }

                if (!response.getProfile().getShipToList().isEmpty()) {
                    logger.info(response.getProfile().getShipToList().get(0).getFirstName());
                    logger.info(response.getProfile().getShipToList().get(0).getLastName());
                    logger.info(response.getProfile().getShipToList().get(0).getCompany());
                    logger.info(response.getProfile().getShipToList().get(0).getAddress());
                    logger.info(response.getProfile().getShipToList().get(0).getCity());
                    logger.info(response.getProfile().getShipToList().get(0).getState());
                    logger.info(response.getProfile().getShipToList().get(0).getZip());
                    logger.info(response.getProfile().getShipToList().get(0).getCountry());
                    logger.info(response.getProfile().getShipToList().get(0).getPhoneNumber());
                    logger.info(response.getProfile().getShipToList().get(0).getFaxNumber());
                }

                if ((response.getSubscriptionIds() != null) && (response.getSubscriptionIds().getSubscriptionId() != null) &&
                        (!response.getSubscriptionIds().getSubscriptionId().isEmpty())) {
                    logger.info("List of subscriptions:");
                    for (String subscriptionid : response.getSubscriptionIds().getSubscriptionId())
                        logger.info(subscriptionid);
                }

            } else {
                logger.info("Failed to get customer profile:  " + response.getMessages().getResultCode());
            }
        }
        return response;
    }
}
