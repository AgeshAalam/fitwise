package com.fitwise.rest.payment;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.properties.StripeProperties;
import com.fitwise.service.payment.stripe.StripeService;
import com.fitwise.service.payment.stripe.StripeTierService;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.payment.stripe.CreateStripeCustomerRequestView;
import com.fitwise.view.payment.stripe.PackageSubscriptionModel;
import com.fitwise.view.payment.stripe.StripeRefundRequestView;
import com.fitwise.view.payment.stripe.StripeSavePaymentResponseView;
import com.fitwise.view.payment.stripe.admin.PayoutSettingsResponseView;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/payment/stripe")
@RequiredArgsConstructor
public class StripeController {

    @Autowired
    private StripeService stripeService;

    @Autowired
    private StripeProperties stripeProperties;

    private final StripeTierService stripeTierService;

    @PostMapping("/createCustomerAndDoPaymentInStripe")
    public ResponseModel createCustomerAndDoPaymentInStripe(@RequestBody CreateStripeCustomerRequestView stripeCustomerRequestView, HttpServletResponse response) throws StripeException {
        ResponseModel res = stripeService.createCustomerAndDoPaymentInStripe(stripeCustomerRequestView);
        if (res.getStatus() == Constants.ERROR_STATUS) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return res;
    }

    @GetMapping("/getUserSavedCards")
    public ResponseModel getUserSavedCardsInStripe() throws StripeException {
        return stripeService.getStripeSavedCards();
    }

    @GetMapping("/cancelStripeSubscription")
    public ResponseModel cancelStripeProgramSubscription(@RequestParam Long programId, @RequestParam Long platformId) {
        return stripeService.cancelStripeProgramSubscription(programId, platformId);
    }

    @PostMapping("/notification")
    public ResponseModel stripeWebhookEndpoint(@RequestBody String json, HttpServletRequest request) {
        String header = request.getHeader("Stripe-Signature");
        String endpointSecret = stripeProperties.getEndPointSecret();
        Event event;
        try {
            event = Webhook.constructEvent(json, header, endpointSecret);
            stripeService.logWebHookEvent(event);
            stripeService.handleStripeNotification(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_STRIPE_NOTIFICATION_RECEIVED, null);
    }


    /**
     * Used to process stripe refund transaction
     *
     * @param stripeRefundRequestView
     * @return
     */
    @PostMapping("/refund")
    public ResponseModel refund(@RequestBody StripeRefundRequestView stripeRefundRequestView) {
        return stripeService.processProgramRefund(stripeRefundRequestView);
    }

    /**
     * API to Upload all UploadPendingPrograms to stripe
     *
     * @return
     */
    @GetMapping("/upload/pendingPrograms")
    public ResponseModel getUploadPendingPrograms() {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_STRIPE_PRODUCTS_UPLOADED, stripeService.getUploadPendingPrograms());
    }

    /**
     * API to Upload all pending programs to stripe
     *
     * @return
     */
    @PutMapping("/upload/uploadPublishedPrograms")
    public ResponseModel uploadPublishedPrograms() {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_STRIPE_PRODUCTS_UPLOADED, stripeService.uploadPublishedPrograms());
    }


    /**
     * API to get stripe payout settings
     *
     * @return
     */
    @GetMapping("/admin/getStripePayoutSettings")
    public ResponseModel getPayoutSettings() {
        return stripeService.getPayoutSettings();
    }

    /**
     * API to save stripe payout settings
     *
     * @param payoutSettingsResponseView
     * @return
     */
    @PostMapping("/admin/saveStripePayoutSettings")
    public ResponseModel savePayoutSettings(@RequestBody PayoutSettingsResponseView payoutSettingsResponseView) {
        return stripeService.savePayoutSettings(payoutSettingsResponseView);
    }


    /**
     * API to subscribe to subscriptionPackage
     * @param packageSubscriptionModel
     * @param response
     * @return
     */
    @PostMapping("/subscriptionPackage/createCustomerAndSubscribe")
    public ResponseModel createCustomerAndSubscribePackage(@RequestBody PackageSubscriptionModel packageSubscriptionModel, HttpServletResponse response) throws StripeException {
        ResponseModel res = stripeService.createCustomerAndSubscribePackage(packageSubscriptionModel);
        if (res.getStatus() == Constants.ERROR_STATUS) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return res;
    }

    /**
     * API to cancel subscription of subscriptionPackage
     * @param subscriptionPackageId
     * @param platformId
     * @return
     */
    @GetMapping("/subscriptionPackage/cancelStripeSubscription")
    public ResponseModel cancelStripePackageSubscription(@RequestParam Long subscriptionPackageId, @RequestParam Long platformId) {
        return stripeService.cancelStripePackageSubscription(subscriptionPackageId, platformId);
    }

    /**
     * Used to process stripe refund transaction of subscription package
     *
     * @param stripeRefundRequestView
     * @return
     */
    @PostMapping("/subscriptionPackage/refund")
    public ResponseModel refundPackageSubscription(@RequestBody StripeRefundRequestView stripeRefundRequestView) {
        return stripeService.processPackageRefund(stripeRefundRequestView);
    }

    /**
     * API to create customer and attach payment method to customer
     * @param paymentMethodId
     * @return
     * @throws StripeException
     */
    @PostMapping("/saveCustomerAndPaymentMethod")
    public ResponseModel saveCustomerAndPaymentMethod(@RequestParam String paymentMethodId) {
        StripeSavePaymentResponseView stripeSavePaymentResponseView = stripeService.saveCustomerAndPaymentMethod(paymentMethodId);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PAYMENT_METHOD_ATTACHED, stripeSavePaymentResponseView);
    }

    @PostMapping("/subscribeTier")
    public ResponseModel subscribeTier(@RequestBody CreateStripeCustomerRequestView stripeCustomerRequestView, HttpServletResponse response) throws StripeException {
        ResponseModel res = stripeTierService.subscribeTier(stripeCustomerRequestView);
        if (res.getStatus() == Constants.ERROR_STATUS) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return res;
    }

}