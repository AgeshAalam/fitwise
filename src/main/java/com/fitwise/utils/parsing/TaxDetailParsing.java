package com.fitwise.utils.parsing;

import com.fitwise.entity.AppConfigKeyValue;
import com.fitwise.entity.PlatformWiseTaxDetail;
import com.fitwise.program.model.PlatformWiseTaxDetailsModel;

/**
 * The Class TaxDetailParsing.
 */
public class TaxDetailParsing {

    /**
     * Construct tax details.
     *
     * @param platformWiseTaxDetail the platform wise tax detail
     * @return the platform wise tax details model
     */
    public static PlatformWiseTaxDetailsModel constructTaxDetails(PlatformWiseTaxDetail platformWiseTaxDetail,AppConfigKeyValue appConfig){
        PlatformWiseTaxDetailsModel platformWiseTaxDetailsModel = new PlatformWiseTaxDetailsModel();
        platformWiseTaxDetailsModel.setPlatformWiseTaxDetailId(platformWiseTaxDetail.getPlatformWiseTaxDetailId());
        platformWiseTaxDetailsModel.setPlatform(platformWiseTaxDetail.getPlatformType().getPlatform());
        platformWiseTaxDetailsModel.setAppStoreTax(platformWiseTaxDetail.getAppStoreTaxPercentage());
        platformWiseTaxDetailsModel.setTrainnrTax(platformWiseTaxDetail.getTrainnrTaxPercentage());
        platformWiseTaxDetailsModel.setGeneralTax(platformWiseTaxDetail.getGeneralTaxPercentage());
        platformWiseTaxDetailsModel.setCreditCardTax(platformWiseTaxDetail.getCreditCardTaxPercentage());
        platformWiseTaxDetailsModel.setPlatformId(platformWiseTaxDetail.getPlatformType().getPlatformTypeId());
        platformWiseTaxDetailsModel.setCreditCardFixedCharges(platformWiseTaxDetail.getCreditCardFixedCharges());
	  if (appConfig != null) {
		double flatTax = Double.parseDouble(appConfig.getValueString());
		platformWiseTaxDetailsModel.setFlatTax(flatTax);
	}
        return platformWiseTaxDetailsModel;
    }
}
