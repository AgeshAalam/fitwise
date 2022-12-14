package com.fitwise.rest.subscription;

import com.fitwise.constants.Constants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.service.SubscriptionService;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.payment.authorizenet.ANetCustomerProfileRequestView;
import com.fitwise.view.payment.authorizenet.ANetOneTimeProgramSubscriptionUsingCardRequestView;
import com.fitwise.view.payment.authorizenet.ANetRecurringSubscriptionRequestViewWithCardData;
import com.fitwise.view.payment.authorizenet.ANetRecurringSubscriptionRequestViewWithPaymentProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/member")
public class ProgramSubscriptionController {

    @Autowired
    SubscriptionService subscriptionService;

    @GetMapping("/getSubscriptionPlans")
    public ResponseModel getSubscriptionPlans() {
        return subscriptionService.getAllSubscriptionPlans();
    }

    @GetMapping("/getSubscriptionTypes")
    public ResponseModel getSubscriptionTypes() {
        return subscriptionService.getAllSubscriptionTypes();
    }

    /**
     * IN USE
     * <p>
     * Used to initiate recurring program subscription by card
     *
     * @param oneTimeSubscriptionRequestView
     * @param response
     * @return
     */
    @PostMapping("/initiateRecurringSubscriptionByCard")
    public ResponseModel initiateRecurringSubscriptionWithCard(@RequestBody ANetRecurringSubscriptionRequestViewWithCardData oneTimeSubscriptionRequestView, HttpServletResponse response) {

        ResponseModel res = subscriptionService.initiateRecurringProgramSubscriptionUsingFormToken(oneTimeSubscriptionRequestView);
        if (res.getStatus() == Constants.ERROR_STATUS) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        return res;
    }

    /**
     * IN USE
     * <p>
     * Used to initiate recurring program subscription using payment profile
     *
     * @param subscriptionView
     * @param response
     * @return
     * @throws ApplicationException
     */
    @PostMapping("/initiateRecurringProgramSubscriptionUsingPaymentProfile")
    public ResponseModel initiateRecurringProgramSubscriptionUsingPaymentProfile(@RequestBody ANetRecurringSubscriptionRequestViewWithPaymentProfile subscriptionView, HttpServletResponse response) throws ApplicationException {

        ResponseModel res = subscriptionService.initiateRecurringProgramSubscriptionUsingPaymentProfile(subscriptionView);
        if (res.getStatus() == Constants.ERROR_STATUS) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return res;
    }

    /**
     * NOT IN USE FOR NOW
     *
     * @param subscriptionView
     * @param response
     * @return
     * @throws ApplicationException
     */
    @PostMapping("/initiateOneTimeProgramSubscriptionUsingCard")
    public ResponseModel subscribeToProgram(@RequestBody ANetOneTimeProgramSubscriptionUsingCardRequestView subscriptionView, HttpServletResponse response) throws ApplicationException {

        ResponseModel res = subscriptionService.initiateOneTimeProgramSubscriptionUsingCard(subscriptionView);
        if (res.getStatus() == Constants.ERROR_STATUS) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return res;
    }


    /**
     * NOT IN USE FOR NOW
     *
     * @param aNetCustomerProfileRequestView
     * @param response
     * @return
     */
    @PostMapping("/initiateOneTimeProgramSubscriptionUsingPaymentProfile")
    public ResponseModel initiateOneTimeProgramSubscriptionUsingPaymentProfile(@RequestBody ANetCustomerProfileRequestView aNetCustomerProfileRequestView, HttpServletResponse response) {
        ResponseModel res = subscriptionService.initiateOneTimeProgramSubscriptionUsingPaymentProfile(aNetCustomerProfileRequestView);
        if (res.getStatus() == Constants.ERROR_STATUS) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return res;
    }

    @PostMapping("/subscribeProgramInTrialMode")
    public ResponseModel subscribeProgramInTrialMode(@RequestParam Long programId, @RequestParam Long platformId) {
        return subscriptionService.subscribeProgramInTrialMode(programId, platformId);
    }

    /**
     * Used to cancel ARB in Auth.net
     *
     * @param programId
     * @param platformId
     * @return
     */
    @PostMapping("/cancelRecurringProgramSubscription")
    public ResponseModel cancelRecurringProgramSubscription(@RequestParam Long programId, @RequestParam Long platformId) {
        return subscriptionService.cancelRecurringProgramSubscription(programId, platformId);
    }

    /**
     * NOT IN USE FOR NOW
     * <p>
     * This api will be called when a program is already subscribed for one time and at the same time user wants to
     * auto-subscribe the program for future using card data
     *
     * @param oneTimeSubscriptionRequestView
     * @return
     */
    @PostMapping("/initiateARBForFutureByCard")
    public ResponseModel initiateRecurringSubscriptionForFutureWithCard(@RequestBody ANetRecurringSubscriptionRequestViewWithCardData oneTimeSubscriptionRequestView) {
        return subscriptionService.initiateRecurringSubscriptionForFutureWithCard(oneTimeSubscriptionRequestView);
    }

    /**
     * NOT IN USE FOR NOW
     * <p>
     * This api will be called when a program is already subscribed for one time and at the same time user wants to
     * auto-subscribe the program for future using payment profile
     *
     * @param recurringSubscriptionRequestView
     * @return
     */
    @PostMapping("/initiateARBForFutureByPaymentProfile")
    public ResponseModel initiateRecurringSubscriptionForFutureWithPaymentProfile(@RequestBody ANetRecurringSubscriptionRequestViewWithPaymentProfile recurringSubscriptionRequestView) {
        return subscriptionService.initiateRecurringSubscriptionForFutureWithPaymentProfile(recurringSubscriptionRequestView);
    }

    /**
     * Returns Manage subscriptions list
     *
     * @return
     */
    @GetMapping("/getSubscribedProgramsList")
    public ResponseModel getSubscribedProgramsList(@RequestParam Optional<Boolean> isAllSubscriptionType) {
        return subscriptionService.getSubscribedProgramsList(isAllSubscriptionType);
    }


    @GetMapping("/getCountriesList")
    public ResponseModel getCountriesList() {
        return subscriptionService.getCountriesList();
    }

}
