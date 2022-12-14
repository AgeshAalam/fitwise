package com.fitwise.rest.payments.appleiap;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fitwise.constants.Constants;
import com.fitwise.service.payments.appleiap.IAPServerNotificationService;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.payments.appleiap.InitialPurchaseRequestView;
import com.fitwise.view.payments.appleiap.PayloadJsonRequestView;
import com.fitwise.view.payments.appleiap.VerifyReceiptRequestView;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/payment/iap/notification")
public class IAPServerNotificationController {

	@Autowired 
	IAPServerNotificationService iServerNotificationService;
	
	@PostMapping("/validateReceipt")
	public ResponseModel validateReceipt(@RequestBody VerifyReceiptRequestView requestView) {
		return iServerNotificationService.getExpiryProgramList();	
	}
	
	@PostMapping("/initialPurchaseNotification")
	public ResponseModel captureInitialPurchaseDetails(@RequestBody InitialPurchaseRequestView requestView) throws  IOException {
		return iServerNotificationService.captureNewBuyDetails(requestView);		
	}
	
	@PostMapping("/getNotificationfromAppstore")
	public ResponseModel serverNotification(@RequestBody Object webHookView) {
		return iServerNotificationService.serverNotification(webHookView);		
	}
	
	@PostMapping("/redirectNotificationfromAppstore")
	public void redirectNotificationfromAppstore(@RequestBody String notification) {
		JSONObject obj=new JSONObject(notification);
		iServerNotificationService.serverNotificationBasedOnNotificationType(obj.getJSONObject("notification"));		
	}
	
	@PostMapping("/paymentSettlement")
	public ResponseModel paymentSettlement() {
		return iServerNotificationService.paymentSettlementProcess();		
	}
	
	@GetMapping("/generateSignature")
	public ResponseModel generateSignature(@RequestParam String productIdentifier,@RequestParam String offerIdentifier,@RequestParam String applicationUsrname) throws Exception {
		 return new ResponseModel(Constants.SUCCESS_STATUS,"Signature Generated Succesfully",iServerNotificationService.generateSignature(productIdentifier,offerIdentifier,applicationUsrname));
	}
	
	@PostMapping("/getJson")
	public String getJson(@RequestBody PayloadJsonRequestView view) throws ClientProtocolException, IOException {
		return iServerNotificationService.getJson(view);
	}
}
