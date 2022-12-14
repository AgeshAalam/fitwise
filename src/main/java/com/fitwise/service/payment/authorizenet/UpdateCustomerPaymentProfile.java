package com.fitwise.service.payment.authorizenet;

import com.fitwise.configuration.StaticContextAccessor;
import com.fitwise.properties.AuthorizeNetProperties;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.payment.authorizenet.ANetBillingAddressView;
import net.authorize.api.contract.v1.*;
import net.authorize.api.controller.base.ApiOperationBase;
import net.authorize.api.controller.UpdateCustomerPaymentProfileController;

public class UpdateCustomerPaymentProfile {

    public static UpdateCustomerPaymentProfileResponse run(GetCustomerPaymentProfileResponse customerPaymentProfile, ANetBillingAddressView addressView) {

        String env = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getEnvironment();
        String loginId = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getLoginId();
        String transactionKey = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getTransactionKey();

        ApiOperationBase.setEnvironment(ValidationUtils.authorizeNetEnvironment(env));
        MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType();
        merchantAuthenticationType.setName(loginId);
        merchantAuthenticationType.setTransactionKey(transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        //customer address
        CustomerAddressType customerAddress = new CustomerAddressType();
        customerAddress.setFirstName(addressView.getFirstName());
        customerAddress.setLastName(addressView.getLastName());
        customerAddress.setAddress(addressView.getAddress());
        customerAddress.setCity(addressView.getCity());
        customerAddress.setState(addressView.getState());
        customerAddress.setZip(addressView.getZip());
        //customerAddress.setCountry(addressView.getCountry());
        //customerAddress.setPhoneNumber("");

        //credit card details
        CreditCardType creditCard = new CreditCardType();
        creditCard.setCardNumber(customerPaymentProfile.getPaymentProfile().getPayment().getCreditCard().getCardNumber());
        creditCard.setExpirationDate(customerPaymentProfile.getPaymentProfile().getPayment().getCreditCard().getExpirationDate());

        PaymentType paymentType = new PaymentType();
        paymentType.setCreditCard(creditCard);

        CustomerPaymentProfileExType customer = new CustomerPaymentProfileExType();
        customer.setPayment(paymentType);
        customer.setCustomerPaymentProfileId(customerPaymentProfile.getPaymentProfile().getCustomerPaymentProfileId());
        customer.setBillTo(customerAddress);

        UpdateCustomerPaymentProfileRequest apiRequest = new UpdateCustomerPaymentProfileRequest();
        apiRequest.setCustomerProfileId(customerPaymentProfile.getPaymentProfile().getCustomerProfileId());
        apiRequest.setPaymentProfile(customer);
        apiRequest.setValidationMode(ValidationModeEnum.LIVE_MODE);

        UpdateCustomerPaymentProfileController controller = new UpdateCustomerPaymentProfileController(apiRequest);
        controller.execute();

        UpdateCustomerPaymentProfileResponse response = new UpdateCustomerPaymentProfileResponse();
        response = controller.getApiResponse();

        if (response != null) {

            if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {

                System.out.println(response.getMessages().getMessage().get(0).getCode());
                System.out.println(response.getMessages().getMessage().get(0).getText());
            } else {
                System.out.println("Failed to update customer payment profile:  " + response.getMessages().getResultCode());
            }
        }
        return response;
    }
}
