package com.fitwise.rest.payment;

import com.fitwise.service.payment.paypal.PayPalService;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.payment.paypal.PayPalRequestView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/payment/payPal")
public class PayPalController {

    @Autowired
    private PayPalService payPalService;

    @PostMapping("/savePayPalId")
    public ResponseModel savePayPalId(@RequestBody PayPalRequestView paypalRequestView) {
        return payPalService.savePayPalId(paypalRequestView.getPayPalId());
    }
}


