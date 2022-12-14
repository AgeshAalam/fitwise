package com.fitwise.rest.payment;

import com.fitwise.service.payment.stripe.StripeConnectService;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.payment.paypal.PayPalTransactionRequestView;
import com.fitwise.view.payment.stripe.InstructorPaymentIdRequestView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/payment/connect")
public class StripeConnectController {

    @Autowired
    private StripeConnectService stripeConnectService;

    /**
     * Used to create account for an instructor in Stripe connect
     *
     * @return
     */
    @GetMapping("/createAccount")
    public ResponseModel createAccount(@RequestParam String countryCode) {
        return stripeConnectService.createAccount(countryCode);
    }

    /**
     * <----STRIPE RETURN---->
     * When account on-boarding is partially or fully completed, user will be redirected to a return url in browser.
     * From web, BE will get a follow up call in the below endpoint.
     *
     * @return
     */
    @GetMapping("/return")
    public ResponseModel returnUrl() {
        return stripeConnectService.returnCallFromStripe();
    }

    /**
     * <----STRIPE REFRESH---->
     * When the on-boarding web page is kept idle for some time, user will be redirected to a refresh url in browser.
     * From web, BE will get a follow up call in the below endpoint.
     *
     * @return
     */
    @GetMapping("/completeOnboarding")
    public ResponseModel reAuth() {
        return stripeConnectService.completeOnboarding();
    }

    /**
     * Creates a single-use login link for an Express account user to access their Stripe dashboard
     *
     * @return
     */
    @GetMapping("/accessDashboard")
    public ResponseModel accessMyDashboard() {
        return stripeConnectService.accessMyDashboard();
    }


    /**
     * Used to check on-boarding status in connect
     *
     * @return
     */
    @GetMapping("/checkInstructorOnBoardingStatus")
    public ResponseModel checkConnectOnBoardingStatus() {
        return stripeConnectService.checkInstructorOnBoardingStatus();
    }


    /**
     * Used to get Instructor share status
     *
     * @param instructorId
     * @return
     */
    @GetMapping("/getInstructorOnBoardingStatus")
    public ResponseModel getInstructorShareStatus(@RequestParam Long instructorId) {
        return stripeConnectService.getInstructorShareStatus(instructorId);
    }


    /**
     * To check whether a country is supported by stripe connect
     */
    @GetMapping("/checkCountrySupport")
    public ResponseModel checkCountrySupport(@RequestParam String countryCode) {
        return stripeConnectService.checkCountrySupport(countryCode);
    }

    /**
     * To get the payouts list
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/admin/getPayouts")
    public ResponseModel getPayouts(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam String filterType, @RequestParam String sortBy, @RequestParam String sortOrder, @RequestParam String platform, Optional<String> search) {
        return stripeConnectService.getPayouts(pageNo, pageSize, filterType, sortBy, sortOrder, platform, search);
    }

    /**
     * To re-initiate a transfer
     *
     * @param instructorPaymentIdRequestView
     * @return
     */
    @PostMapping("/admin/reInitiateTransfer")
    public ResponseModel reInitiateTransfer(@RequestBody InstructorPaymentIdRequestView instructorPaymentIdRequestView) {
        return stripeConnectService.reInitiateTransfer(instructorPaymentIdRequestView.getInstructorPaymentId());
    }


    /**
     * Used to save paypal transaction data
     *
     * @param payPalTransactionRequestView
     * @return
     */
    @PostMapping("/admin/savePayPalTransaction")
    public ResponseModel savePayPalTransaction(@RequestBody PayPalTransactionRequestView payPalTransactionRequestView) {
        return stripeConnectService.savePayPalTransaction(payPalTransactionRequestView);
    }

    /**
     * Method used to pay instructors their share for the programs subscribed via Apple payments
     * Since we didn't have any webhook event for payment settlement state from Apple, we will assume 45 days from
     * subscribed date as settlement date to Fitwise bank account.
     *
     * @return
     */
    @GetMapping("/admin/applePayments/topUp")
    public ResponseModel processInstructorApplePayments() throws ParseException {
        return stripeConnectService.processInstructorApplePayments();
    }

    /**
     * Used to get the Instructor payouts (Transfer) details
     *
     * @param instructorPaymentId
     * @return
     */
    @GetMapping("/admin/getInstructorPayoutDetails")
    public ResponseModel getInstructorPayoutDetails(@RequestParam Long instructorPaymentId) {
        return stripeConnectService.getInstructorPayoutDetails(instructorPaymentId);
    }

    /**
     * @return
     */
    @PutMapping("/updateInstructorInInstructorPayment")
    public ResponseModel updateInstructorInInstructorPayment() {
        return stripeConnectService.updateInstructorInInstructorPayment();
    }

    /**
     * @return
     */
    @GetMapping("/getInstructorNullInInstructorPayment")
    public ResponseModel getInstructorNullInInstructorPayment() {
        return stripeConnectService.getInstructorNullInInstructorPayment();
    }

}
