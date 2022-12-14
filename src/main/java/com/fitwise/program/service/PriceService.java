package com.fitwise.program.service;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.AppConfigConstants;
import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.entity.AppConfigKeyValue;
import com.fitwise.entity.PlatformType;
import com.fitwise.entity.PlatformWiseTaxDetail;
import com.fitwise.entity.User;
import com.fitwise.entity.instructor.InstructorTierDetails;
import com.fitwise.exception.ApplicationException;
import com.fitwise.program.model.PlatformWiseTaxDetailsModel;
import com.fitwise.program.model.ProgramPriceResponseModel;
import com.fitwise.repository.AppConfigKeyValueRepository;
import com.fitwise.repository.PlatformTypeRepository;
import com.fitwise.repository.PlatformWiseTaxDetailRepository;
import com.fitwise.repository.instructor.InstructorTierDetailsRepository;
import com.fitwise.utils.parsing.TaxDetailParsing;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class PriceService.
 */
@Service
@RequiredArgsConstructor
public class PriceService {

	private final PlatformTypeRepository platformTypeRepository;
	private final PlatformWiseTaxDetailRepository platformWiseTaxDetailRepository;
	private final UserComponents userComponents;
	@Autowired
	private AppConfigKeyValueRepository appConfigKeyValueRepository;

	private final InstructorTierDetailsRepository instructorTierDetailsRepository;

	/**
	 * Gets the tax details by platform.
	 *
	 * @return the tax details by platform
	 */
	public List<PlatformWiseTaxDetailsModel> getTaxDetailsByPlatform(){
		User user = userComponents.getUser();
		List<PlatformType> platformTypes = platformTypeRepository.findAll();
		if(platformTypes.isEmpty()){
			throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PLATFORM_NOT_AVAILABLE, null);
		}
		List<PlatformWiseTaxDetail> platformWiseTaxDetails = platformWiseTaxDetailRepository.findByActiveAndPlatformTypeIn(true, platformTypes);
		if(platformWiseTaxDetails.isEmpty()){
			throw new ApplicationException(Constants.NOT_FOUND, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
		}
		InstructorTierDetails instructorTierDetails = instructorTierDetailsRepository.findByUserAndActive(user, true);
		Double trainnrTax = 15.0;
		if (instructorTierDetails != null) {
			trainnrTax = instructorTierDetails.getTier().getTierTypeDetails().getProgramsFees();
		}
		List<PlatformWiseTaxDetailsModel> platformWiseTaxDetailsModels = new ArrayList<>();
		for(PlatformWiseTaxDetail platformWiseTaxDetail: platformWiseTaxDetails){
			if(null != trainnrTax){
				platformWiseTaxDetail.setTrainnrTaxPercentage(trainnrTax);
			}
			AppConfigKeyValue appConfig = appConfigKeyValueRepository.findByKeyString(AppConfigConstants.ADMIN_CONFIG_FLAT_TAX);
			platformWiseTaxDetailsModels.add(TaxDetailParsing.constructTaxDetails(platformWiseTaxDetail,appConfig));
		}
		return  platformWiseTaxDetailsModels;
	}

	public List<ProgramPriceResponseModel> getProgramRevenueByPlatform(Double price) {
		List<ProgramPriceResponseModel> platformWiseTaxDetailsModelList = new ArrayList<>();
		DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
		String priceStr;
		List<PlatformWiseTaxDetailsModel> taxDetailsByPlatformList = getTaxDetailsByPlatform();
		User user = userComponents.getUser();
		InstructorTierDetails instructorTierDetails = instructorTierDetailsRepository.findByUserAndActive(user, true);
		Double trainnrTax = 15.0;
		if (instructorTierDetails != null) {
			trainnrTax = instructorTierDetails.getTier().getTierTypeDetails().getProgramsFees();
		}
		AppConfigKeyValue appConfig = appConfigKeyValueRepository.findByKeyString(AppConfigConstants.ADMIN_CONFIG_FLAT_TAX);
		for (PlatformWiseTaxDetailsModel taxDetailsByPlatform : taxDetailsByPlatformList) {
			ProgramPriceResponseModel programPriceResponseModel = new ProgramPriceResponseModel();
			programPriceResponseModel.setPlatformWiseTaxDetailId(taxDetailsByPlatform.getPlatformWiseTaxDetailId());
			programPriceResponseModel.setPlatformId(taxDetailsByPlatform.getPlatformId());
			programPriceResponseModel.setPlatform(taxDetailsByPlatform.getPlatform());
			double appStoreTax = taxDetailsByPlatform.getAppStoreTax();
			programPriceResponseModel.setAppStoreTax(appStoreTax);
			double appStoreTaxAmount = (appStoreTax / 100) * price;
			priceStr = decimalFormat.format(appStoreTaxAmount);
			appStoreTaxAmount = Double.parseDouble(priceStr);
			programPriceResponseModel.setAppStoreTaxAmount(appStoreTaxAmount);
			programPriceResponseModel.setTrainnrTax(trainnrTax);
			double trainnrTaxAmount = (trainnrTax / 100) * price;
			priceStr = decimalFormat.format(trainnrTaxAmount);
			trainnrTaxAmount = Double.parseDouble(priceStr);
			programPriceResponseModel.setTrainnrTaxAmount(trainnrTaxAmount);
			programPriceResponseModel.setGeneralTax(taxDetailsByPlatform.getGeneralTax());
			double creditCardTax = taxDetailsByPlatform.getCreditCardTax();
			programPriceResponseModel.setCreditCardTax(creditCardTax);
			double creditCardFixedCharge = taxDetailsByPlatform.getCreditCardFixedCharges();
			programPriceResponseModel.setCreditCardFixedCharges(creditCardFixedCharge);
			double creditCardTaxAmount = ((creditCardTax / 100) * price) + creditCardFixedCharge;
			priceStr = decimalFormat.format(creditCardTaxAmount);
			creditCardTaxAmount = Double.parseDouble(priceStr);
			programPriceResponseModel.setCreditCardTaxAmount(creditCardTaxAmount);
			//Revenue calculated based on rounded off tax amount.
			double revenue = price - (appStoreTaxAmount + trainnrTaxAmount + creditCardTaxAmount);
			priceStr = decimalFormat.format(revenue);
			revenue = Double.parseDouble(priceStr);
			programPriceResponseModel.setPrice(revenue);
			programPriceResponseModel.setPriceFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(revenue));
			if (appConfig != null) {
	        	double flatTax = Double.parseDouble(appConfig.getValueString());
	        	programPriceResponseModel.setFlatTax(flatTax);
	        }
			platformWiseTaxDetailsModelList.add(programPriceResponseModel);
		}
		return platformWiseTaxDetailsModelList;
	}
}
