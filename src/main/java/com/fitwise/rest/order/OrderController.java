package com.fitwise.rest.order;

import com.fitwise.constants.KeyConstants;
import com.fitwise.service.payment.authorizenet.PaymentService;
import com.fitwise.service.receiptInvoice.InvoicePDFGenerationService;
import com.fitwise.view.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_PDF;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/order")
public class OrderController {

    @Autowired
    PaymentService paymentService;

    @Autowired
    InvoicePDFGenerationService invoicePDFGenerationService;

    @GetMapping("/getOrderHistory")
    public ResponseModel getOrderHistory(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam Optional<Boolean> isAllSubscriptionType) {
        return paymentService.getOrderHistory(pageNo, pageSize, KeyConstants.KEY_PROGRAM, isAllSubscriptionType);
    }

    /**
     * API to get Subscription package order details
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/subscriptionPackage/orderHistory")
    public ResponseModel getPackageOrderHistory(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam Optional<Boolean> isAllSubscriptionType) {
        return paymentService.getOrderHistory(pageNo, pageSize, KeyConstants.KEY_SUBSCRIPTION_PACKAGE, isAllSubscriptionType);
    }

    @PostMapping("/getOrderDetail")
    public ResponseModel getOrderDetail(@RequestParam String orderId) {
        return paymentService.getOrderDetail(orderId);
    }

    @GetMapping("/getOrderHistoryOfAMember")
    public ResponseModel getOrderHistoryOfAMember(@RequestParam Long memberId, @RequestParam final int pageNo, @RequestParam final int pageSize) {
        return paymentService.getOrderHistoryOfAMember(memberId, pageNo, pageSize, KeyConstants.KEY_PROGRAM);
    }

    /**
     * API to get Subscription package order details of a member from admin portal
     * @param memberId
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/subscriptionPackage/orderHistoryOfAMember")
    public ResponseModel getPackageOrderHistoryOfAMember(@RequestParam Long memberId, @RequestParam final int pageNo, @RequestParam final int pageSize) {
        return paymentService.getOrderHistoryOfAMember(memberId, pageNo, pageSize, KeyConstants.KEY_SUBSCRIPTION_PACKAGE);
    }

    @GetMapping("/getOrderReceipt")
    public ResponseEntity<InputStreamResource> getOrderReceipt(@RequestParam String orderId,
                                                               HttpServletRequest request,
                                                               HttpServletResponse response) throws IOException {
        File file = invoicePDFGenerationService.generateInvoiceReceipt(orderId, request, response);
        final HttpHeaders httpHeaders = getHttpHeaders(orderId, file);
        return new ResponseEntity<>(new InputStreamResource(new FileInputStream(file)), httpHeaders, OK);
    }

    private HttpHeaders getHttpHeaders(String code, File invoicePdf) {
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(APPLICATION_PDF);
        respHeaders.setContentLength(invoicePdf.length());
        respHeaders.setContentDispositionFormData("attachment", format("%s.pdf", code));
        return respHeaders;
    }
}
