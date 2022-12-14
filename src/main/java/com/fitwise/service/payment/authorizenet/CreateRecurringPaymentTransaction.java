package com.fitwise.service.payment.authorizenet;

import com.fitwise.configuration.StaticContextAccessor;
import com.fitwise.entity.Programs;
import com.fitwise.properties.AuthorizeNetProperties;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.payment.authorizenet.ANetRecurringSubscriptionRequestViewWithPaymentProfile;
import net.authorize.api.contract.v1.*;
import net.authorize.api.controller.ARBCreateSubscriptionController;
import net.authorize.api.controller.base.ApiOperationBase;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CreateRecurringPaymentTransaction {

    public static ANetApiResponse run(ANetRecurringSubscriptionRequestViewWithPaymentProfile subscriptionRequestView, Programs program) {
        //Common code to set for all requests
        String env = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getEnvironment();
        String loginId = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getLoginId();
        String transactionKey = StaticContextAccessor.getBean(AuthorizeNetProperties.class).getTransactionKey();

        ApiOperationBase.setEnvironment(ValidationUtils.authorizeNetEnvironment(env));
        MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType();
        merchantAuthenticationType.setName(loginId);
        merchantAuthenticationType.setTransactionKey(transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        // Set up payment schedule
        PaymentScheduleType schedule = new PaymentScheduleType();
        PaymentScheduleType.Interval interval = new PaymentScheduleType.Interval();
        interval.setLength(program.getDuration().getDuration().shortValue());
        interval.setUnit(ARBSubscriptionUnitEnum.DAYS);
        schedule.setInterval(interval);

        try {
            XMLGregorianCalendar startDate = DatatypeFactory.newInstance().newXMLGregorianCalendar();
            startDate.setDay(30);
            startDate.setMonth(8);
            startDate.setYear(2020);
            schedule.setStartDate(startDate); //2020-08-30
        } catch (Exception e) {
        }

        schedule.setTotalOccurrences((short) 12);
        schedule.setTrialOccurrences((short) 1);

        CustomerProfileIdType profile = new CustomerProfileIdType();
        profile.setCustomerProfileId(subscriptionRequestView.getCustomerProfileId());
        profile.setCustomerPaymentProfileId(subscriptionRequestView.getCustomerPaymentProfileId());
        profile.setCustomerAddressId(subscriptionRequestView.getCustomerAddressId());

        ARBSubscriptionType arbSubscriptionType = new ARBSubscriptionType();
        arbSubscriptionType.setPaymentSchedule(schedule);
        arbSubscriptionType.setAmount(new BigDecimal(program.getProgramPrice()).setScale(2, RoundingMode.CEILING));
        arbSubscriptionType.setTrialAmount(new BigDecimal(0.0).setScale(2, RoundingMode.CEILING));
        arbSubscriptionType.setProfile(profile);

        // Make the API Request
        ARBCreateSubscriptionRequest apiRequest = new ARBCreateSubscriptionRequest();
        apiRequest.setSubscription(arbSubscriptionType);
        ARBCreateSubscriptionController controller = new ARBCreateSubscriptionController(apiRequest);
        controller.execute();
        ARBCreateSubscriptionResponse response = controller.getApiResponse();
        if (response != null) {

            if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {

                System.out.println(response.getSubscriptionId());
                System.out.println(response.getMessages().getMessage().get(0).getCode());
                System.out.println(response.getMessages().getMessage().get(0).getText());
            } else {
                System.out.println("Failed to create Subscription:  " + response.getMessages().getResultCode());
                System.out.println(response.getMessages().getMessage().get(0).getText());
            }
        }

        return response;
    }
}