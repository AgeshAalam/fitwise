package com.fitwise.rest.discountsController;



import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Optional;

import com.fitwise.exception.ApplicationException;
import com.fitwise.view.OfferProgramRequestView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.service.discountservice.DiscountsService;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.discounts.SaveOfferCodeRequestView;
import com.fitwise.view.discounts.UpdateOfferCodeRequestView;


@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/discounts")
public class DiscountsController {
	
	@Autowired
	DiscountsService discountsService;
	
	@PostMapping("/generateOfferCode")
	public ResponseModel generateOfferCode(@RequestParam String offerName,@RequestParam Long programId) {
		return discountsService.generateOfferCode(offerName,programId);
	}
	
	@PostMapping("/saveOfferCode")
	public ResponseModel saveOfferCode(@RequestBody SaveOfferCodeRequestView saveOfferCodeRequestView) throws ParseException {
		return discountsService.saveOfferCode(saveOfferCodeRequestView);
	}
	
	@PutMapping("/updateOfferCode")
	public ResponseModel updateOfferCode(@RequestBody UpdateOfferCodeRequestView updateOfferCodeRequestView) throws ParseException {
		return discountsService.updateOfferCode(updateOfferCodeRequestView);
	}
	
	@PostMapping("/RemoveOfferCode")
	public ResponseModel removeOfferCode(@RequestParam Long offerId) {
		return discountsService.removeOfferCode(offerId);
	}

	@GetMapping("/getAllOfferCodes")
	public ResponseModel getAllOfferCodes() {
		return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_OFFER_LIST_FETCHED, discountsService.getAllOfferCodes());
	}
	
	@GetMapping("/getOfferCodeDetail")
	public ResponseModel getOfferCodeDetail(@RequestParam Long offerId) throws ParseException {
		return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_OFFER_CODE_DETAIL_FETCHED, discountsService.getOfferCodeDetail(offerId));
	}
	
	@GetMapping("/getAllOfferDuration")
	public ResponseModel getAllOfferDuration(@RequestParam String mode) {
		return discountsService.getAllOfferDuration(mode);
	}
	
	@GetMapping("/getPriceList")
	public ResponseModel getPriceList() {
		return discountsService.getPriceList();
	}
	
	@PostMapping("/validateOfferName")
	public Boolean validateOfferName(@RequestParam String offerName,@RequestParam Long programId) {
		return discountsService.validateOfferName(offerName,programId);
	}
	
	@PostMapping("/validateOfferCode")
	public Boolean validateOfferCode(@RequestParam String offerCode) {
		return discountsService.validateOfferCode(offerCode);
	}

	@GetMapping("/getOfferPriceBreakDown")
	public ResponseModel getOfferPriceBreakdown(@RequestParam Long offerId){
		return discountsService.getOfferPriceBreakDown(offerId);
	}

	@GetMapping("/removeOffersWithHigherPrice")
	public ResponseModel removeOffersWithHigherPrice(@RequestParam Long programId, @RequestParam Double newProgramPrice){
		return discountsService.removeOffersWithHigherPrice(programId,newProgramPrice);
	}

	@PostMapping("/addOffersInProgram")
	public ResponseModel addOffersInProgram(@RequestBody OfferProgramRequestView offerProgramRequestView) throws NoSuchAlgorithmException, KeyStoreException, ParseException, KeyManagementException {
		return discountsService.addOffersInProgram(offerProgramRequestView);
	}
	
	@GetMapping("/getProgramOffers")
	public ResponseModel getProgramOffers(@RequestParam Long programId) {
		return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_OFFER_LIST_FETCHED, discountsService.getProgramOffers(programId));
	}

	/**
	 * generate offer code for package API
	 * @return
	 */
	@PostMapping("/generateOfferCodeForPackage")
	public ResponseModel generateOfferCodeForPackage(@RequestParam String offerName, @RequestParam Long packageId) {
		return discountsService.generateOfferCodeForPackage(offerName, packageId);
	}

	/**
	 * validate offer name for package API
	 * @return
	 */
	@PostMapping("/validateOfferNameForPackage")
	public Boolean validateOfferNameForPackage(@RequestParam String offerName,@RequestParam Long packageId) {
		return discountsService.validateOfferNameForPackakge(offerName,packageId);
	}
}
