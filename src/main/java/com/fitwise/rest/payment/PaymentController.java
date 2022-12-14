package com.fitwise.rest.payment;

import com.fitwise.service.payment.authorizenet.PaymentService;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.payment.authorizenet.ANetRefundRequestView;
import com.fitwise.view.payment.authorizenet.PostRefundReasonRequestView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Related to Authorize.net
 */
@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/payment")
public class PaymentController {

    @Autowired
    PaymentService paymentService;

    @GetMapping("/getCustomerSavedCardData")
    public ResponseModel getANetCustomerProfile() {
        return paymentService.getANetCustomerProfile();
    }

    /**
     * Method that will receive the notification from Authorization.net Web-hooks
     *
     * @param webHookView
     * @return
     */
    @PostMapping("/getWebHookDataFromAuthNet")
    public ResponseModel getAuthNetWebHookData(@RequestBody Object webHookView) {
        return paymentService.getAuthNetWebHookData(webHookView);
    }

    /**
     * Used to refund amount to user from Authorize.net
     *
     * @param aNetRefundRequestView
     * @return
     */
    @PostMapping("/authorizeNetRefund")
    public ResponseModel refundAmount(@RequestBody ANetRefundRequestView aNetRefundRequestView) {
        return paymentService.refundAuthorizeNetPayment(aNetRefundRequestView.getTransactionId(), aNetRefundRequestView.getRefundableAmount());
    }

    /**
     * Used to return the refund reasons
     *
     * @return
     */
    @GetMapping("/getRefundReasons")
    public ResponseModel refundReasons() {
        return paymentService.getRefundReasons();
    }

    /**
     * Used to post refund reason
     *
     * @param postRefundReasonRequestView
     * @return
     */
    @PostMapping("/postRefundReason")
    public ResponseModel postRefundReason(@RequestBody PostRefundReasonRequestView postRefundReasonRequestView) {
        return paymentService.postRefundReason(postRefundReasonRequestView);
    }
}
