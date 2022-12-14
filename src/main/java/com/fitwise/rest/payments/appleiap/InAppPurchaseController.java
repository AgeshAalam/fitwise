package com.fitwise.rest.payments.appleiap;

import com.fitwise.service.payments.appleiap.InAppPurchaseService;
import com.fitwise.view.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/v1/payment/iap")
public class InAppPurchaseController {

	@Autowired
	InAppPurchaseService inAppPurchaseService;

	@GetMapping("/uploadmetadata")
	@ResponseBody
	public ResponseModel uploadMetadata() throws IOException {
		return inAppPurchaseService.publishPrograms();
	}

	@PostMapping("/unPublishMetadata")
	@ResponseBody
	public ResponseModel unPublishMetadata(List<Long> programId) throws IOException {
		return inAppPurchaseService.unPublishPrograms(programId);
	}
}
