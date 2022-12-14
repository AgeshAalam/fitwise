package com.fitwise.service.payment.authorizenet;

import com.fitwise.configuration.StaticContextAccessor;
import com.fitwise.entity.Programs;
import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.properties.AuthorizeNetProperties;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.payment.authorizenet.ANetRecurringSubscriptionRequestViewWithPaymentProfile;
import net.authorize.api.contract.v1.*;
import net.authorize.api.controller.ARBCreateSubscriptionController;
import net.authorize.api.controller.base.ApiOperationBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;

public class CreateARBForFutureUsingCustomerProfile {

    public static ARBCreateSubscriptionResponse run(ANetRecurringSubscriptionRequestViewWithPaymentProfile recurringSubscriptionRequestView,
                                                    Programs program, OrderManagement orderManagement, long futureDate) {
        Logger logger = LoggerFactory.getLogger(CreateARBForFutureUsingCustomerProfile.class);

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

        // convert date to calendar
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());

        // manipulate date
        c.add(Calendar.DATE, Math.toIntExact(futureDate));


        // convert calendar to date
        Date subscriptionStartDate = c.getTime();
        logger.info("Subscription start Date of ARB :::" + subscriptionStartDate.getTime());

        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(subscriptionStartDate);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            XMLGregorianCalendar startDate = DatatypeFactory.newInstance().newXMLGregorianCalendar();
            startDate.setDay(day);
            startDate.setMonth(month + 1); // For May, Month count will be denoted as 4, so adding 1
            startDate.setYear(year);
            schedule.setStartDate(startDate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        schedule.setTotalOccurrences((short) 9999);
        schedule.setTrialOccurrences((short) 0);

        CustomerProfileIdType profile = new CustomerProfileIdType();
        profile.setCustomerProfileId(recurringSubscriptionRequestView.getCustomerProfileId());
        profile.setCustomerPaymentProfileId(recurringSubscriptionRequestView.getCustomerPaymentProfileId());
        profile.setCustomerAddressId(recurringSubscriptionRequestView.getCustomerAddressId());

        ARBSubscriptionType arbSubscriptionType = new ARBSubscriptionType();
        arbSubscriptionType.setPaymentSchedule(schedule);
        arbSubscriptionType.setAmount(new BigDecimal(program.getProgramPrice()).setScale(2, RoundingMode.CEILING));
        arbSubscriptionType.setTrialAmount(new BigDecimal(program.getProgramPrice()).setScale(2, RoundingMode.CEILING));
        arbSubscriptionType.setProfile(profile);

        OrderType orderType = new OrderType();
        orderType.setInvoiceNumber(orderManagement.getOrderId());
        arbSubscriptionType.setOrder(orderType);

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
