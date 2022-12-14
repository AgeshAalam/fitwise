package com.fitwise.service.itms;

import com.fitwise.constants.DBConstants;
import com.fitwise.constants.EmailConstants;
import com.fitwise.constants.InstructorConstant;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.discounts.DiscountsConstants;
import com.fitwise.constants.payments.appleiap.NotificationConstants;
import com.fitwise.entity.AppConfigKeyValue;
import com.fitwise.entity.Programs;
import com.fitwise.entity.discounts.DiscountOfferMapping;
import com.fitwise.entity.discounts.OfferCode;
import com.fitwise.entity.discounts.OfferCodeDetail;
import com.fitwise.entity.itms.ItmsPublish;
import com.fitwise.entity.itms.ItmsUnpublish;
import com.fitwise.entity.payments.appleiap.IapJobInputs;
import com.fitwise.properties.AppleProperties;
import com.fitwise.repository.AppConfigKeyValueRepository;
import com.fitwise.repository.discountsRepository.DiscountOfferMappingRepository;
import com.fitwise.repository.discountsRepository.OfferCodeDetailRepository;
import com.fitwise.repository.discountsRepository.OfferCodeRepository;
import com.fitwise.repository.itms.ITMSPublishRepository;
import com.fitwise.repository.itms.ITMSUnPublishRepository;
import com.fitwise.repository.payments.appleiap.IAPJobInputsRepository;
import com.fitwise.service.payments.appleiap.InAppPurchaseService;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.mail.MailSender;
import com.fitwise.view.ResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ITMSService {

    @Autowired
    private ITMSPublishRepository itmsPublishRepository;

    @Autowired
    private ITMSUnPublishRepository itmsUnPublishRepository;
    
    @Autowired
    private InAppPurchaseService inAppPurchaseService;

    @Autowired
    private MailSender mailSender;

    @Autowired
    private AppleProperties appleProperties;

    @Autowired
    private AppConfigKeyValueRepository appConfigKeyValueRepository;
    
    @Autowired
    private OfferCodeDetailRepository offDetailRepository;
    
    @Autowired
    FitwiseUtils fitwiseUtils;
    
    @Autowired
    OfferCodeRepository offerCodeRepository;
    
    @Autowired
    DiscountOfferMappingRepository discountOfferMappingRepository;
    
    @Autowired
    FitwiseITMSUploadEntityService fitwiseITMSUploadEntityService;

    @Autowired
    private IAPJobInputsRepository iapJobInputsRepository;

    public void syncPublish() {
        /*
        * upload published programs.
        * */
        /*
        * TODO : cleared for sale delay handling is temporarily removed.
        * Update 'clearForSaleAllowed' in uploadPublishProgram() as false for reverting the removal.
        * */
        log.info("Publish Sync");
        List<ItmsPublish> itmsPublishes = itmsPublishRepository.findByNeedUpdate(true);
        for (ItmsPublish itmsPublish : itmsPublishes) {
            uploadPublishProgram(itmsPublish, true);
        }

        Date now = new Date();

        /*
        * upload already published program with changes in price.
        * One day delay is introduced in uploading to appstore, so that price in app store and fitwise in the same.
        * */
        List<ItmsPublish> itmsAwaitingPublishes = itmsPublishRepository.findByNeedUpdateAndStatus(false, DBConstants.ITMS_AWAITING_CLEAR_FOR_SALE);
        for (ItmsPublish itmsAwaitingPublish : itmsAwaitingPublishes) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(itmsAwaitingPublish.getModifiedDate());
            calendar.add(Calendar.DATE, 1);
            Date awaitingStartDate = calendar.getTime();
            if (fitwiseUtils.isSameDayInTimeZone(awaitingStartDate, now, "PST")) {
                uploadPublishProgram(itmsAwaitingPublish, true);
            }
        }

    }

    /**
     * upload published program in ITMS
     * @param itmsPublish
     * @param clearForSaleAllowed
     */
    private void uploadPublishProgram(ItmsPublish itmsPublish, boolean clearForSaleAllowed) {

        boolean isNewPublish = true;
        if (itmsPublish.getIsNew() != null && !itmsPublish.getIsNew().booleanValue()) {
            isNewPublish = false;
        }
        try {
            String status = DBConstants.ITMS_UPDATED;
            ResponseModel responseModel;
            if (isNewPublish) {
                responseModel = inAppPurchaseService.uploadXMLForNewPriceProgram(InstructorConstant.PUBLISH, itmsPublish.getProgram());
            } else {
                responseModel = inAppPurchaseService.uploadXMLForUpdatePriceProgram(InstructorConstant.PUBLISH, itmsPublish.getProgram(), clearForSaleAllowed);
            }
            if (responseModel.getPayload() != null) {
                try {
                    Map<String, String> uploadDataMap = (Map<String, String>) responseModel.getPayload();
                    if (uploadDataMap.get(KeyConstants.KEY_STATUS) != null) {
                        status = uploadDataMap.get(KeyConstants.KEY_STATUS);
                    }
                } catch (Exception e) {
                    log.warn("Exception while processing ITMS upload response map : " + e.getMessage());
                }
            }
            itmsPublish.setNeedUpdate(false);
            itmsPublish.setStatus(status);
        } catch (Exception exception) {
            itmsPublish.setStatus(exception.getMessage());
            log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
        }
        itmsPublishRepository.save(itmsPublish);
    }

    public void syncUnPublish(){
        log.info("UnPublish Sync");
        List<ItmsUnpublish> itmsUnpublishes = itmsUnPublishRepository.findByNeedUpdate(true);
        for(ItmsUnpublish itmsUnPublish : itmsUnpublishes){
            try {
                inAppPurchaseService.uploadXMLForUpdatePriceProgram(InstructorConstant.UNPUBLISH, itmsUnPublish.getProgram(), true);
                itmsUnPublish.setNeedUpdate(false);
                itmsUnPublish.setStatus(DBConstants.ITMS_UPDATED);
            }catch (Exception exception){
                itmsUnPublish.setStatus(exception.getMessage());
                log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
            }
            itmsUnPublishRepository.save(itmsUnPublish);
        }
    }

    public void syncAll(){
        syncPublish();
        syncUnPublish();
    }

    @Transactional
    public void sendDailyFeedback() {
        Date cDate = new Date();
        Calendar startcal = Calendar.getInstance();
        Calendar endcal = Calendar.getInstance();
        startcal.setTime(cDate);
        startcal.add(Calendar.DAY_OF_YEAR, -1);
        startcal.set(Calendar.HOUR_OF_DAY, 0);
        startcal.set(Calendar.MINUTE, 0);
        startcal.set(Calendar.SECOND, 0);
        endcal.setTime(cDate);
        endcal.add(Calendar.DAY_OF_YEAR, -1);
        endcal.set(Calendar.HOUR_OF_DAY, 23);
        endcal.set(Calendar.MINUTE, 59);
        endcal.set(Calendar.SECOND, 59);
        List<ItmsPublish> itmsPublishes = itmsPublishRepository.findAllByModifiedDateGreaterThanEqualAndModifiedDateLessThanEqual(startcal.getTime(), endcal.getTime());
        String subject = "";
        String header="\n The following programs have been uploaded into the App Store. Please find the details below.\n"
                + "<br> <table width='100%' border='1' align='center'>"
                + "<tr align='left'>"
                + "<td><b>Program Id & Name <b></td>"
                + "<td><b>Program Price <b></td>"
                + "<td><b>Status <b></td>"
                + "<td><b>Discount Details <b></td>"
                + "</tr>";

        String text = "";
        String mailBody = "";
        List<DiscountOfferMapping> finalList=new ArrayList<>();
        AppConfigKeyValue idConfigKeyValue = appConfigKeyValueRepository.findByKeyString(NotificationConstants.PDT_IDENTIFIER);
        if(!itmsPublishes.isEmpty()){
            subject = "AppStore - In-App Purchases Upload for Publish";
            text = header;
            for(ItmsPublish itmsPublish : itmsPublishes){
                String status = "";
                if (StringUtils.isEmpty(itmsPublish.getStatus())){
                    status = "Going to upload soon.";
                }else{
                    status = itmsPublish.getStatus();
                }
                //Fetch Discount Details
                List<DiscountOfferMapping> discounts = discountOfferMappingRepository.findByProgramsProgramIdAndNeedMailUpdate(itmsPublish.getProgram().getProgramId(), true);
                List<String> summaryDiscount=new ArrayList<>();
                int count=0;
                for(DiscountOfferMapping discount:discounts) {
                	finalList.add(discount);
                	count ++;
                	String mode=discount.getOfferCodeDetail().getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_FREE)? "free":discount.getOfferCodeDetail().getOfferPrice().getPrice()+" USD";
                	summaryDiscount.add(count +" ."+discount.getOfferCodeDetail().getOfferName()+" - "+mode+" - "+discount.getDiscountStatus()+"\n");
                }
                
                text = text + "<tr align='left'>" + "<td>" + idConfigKeyValue.getValueString() + "." + itmsPublish.getProgram().getProgramId() + " - " + itmsPublish.getProgram().getTitle() + "</td>" + "<td>" + itmsPublish.getProgram().getProgramPrices().getPrice()
                        + "</td>" + "<td>" + status + "</td>" +"<td>" + summaryDiscount + "</td>" +  "</tr>";
            }
            mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi Team,").replace(EmailConstants.EMAIL_BODY, text);
            mailSender.sendHtmlReminderMail(appleProperties.itmsNotificationToEMailAddress, subject, mailBody);
        }
        List<ItmsUnpublish> itmsUnPublishes = itmsUnPublishRepository.findAllByModifiedDateGreaterThanEqualAndModifiedDateLessThanEqual(startcal.getTime(), endcal.getTime());
        if(!itmsUnPublishes.isEmpty()){
            subject = "AppStore - In-App Purchases Upload for UnPublish";
            text = header;
            for(ItmsUnpublish itmsUnPublish : itmsUnPublishes){
                String status = "";
                if (StringUtils.isEmpty(itmsUnPublish.getStatus())){
                    status = "Going to upload soon.";
                }else{
                    status = itmsUnPublish.getStatus();
                }
              //Fetch Discount Details
                List<DiscountOfferMapping> discounts = discountOfferMappingRepository.findByProgramsProgramIdAndNeedMailUpdate(itmsUnPublish.getProgram().getProgramId(), true);
                List<String> summaryDiscount=new ArrayList<>();
                int count=0;
                for(DiscountOfferMapping discount:discounts) {
                	finalList.add(discount);
                	count ++;
                	String mode=discount.getOfferCodeDetail().getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_FREE)? "free":discount.getOfferCodeDetail().getOfferPrice().getPrice()+" USD";
                	summaryDiscount.add(count +" ."+discount.getOfferCodeDetail().getOfferName()+" - "+mode+" - "+discount.getDiscountStatus()+ "\n");
                 }
                text = text + "<tr align='left'>" + "<td>" + idConfigKeyValue.getValueString() + "." + itmsUnPublish.getProgram().getProgramId() + " - " + itmsUnPublish.getProgram().getTitle() + "</td>" + "<td>" + itmsUnPublish.getProgram().getProgramPrices().getPrice()
                        + "</td>" + "<td>" + status + "</td>" +"<td>" + summaryDiscount + "</td>"+ "</tr>";
            }
            mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi Team,").replace(EmailConstants.EMAIL_BODY, text);
            mailSender.sendHtmlReminderMail(appleProperties.itmsNotificationToEMailAddress, subject, mailBody);
            //
            for(DiscountOfferMapping discount:finalList) {
            	discount.setNeedMailUpdate(false);
            	discountOfferMappingRepository.save(discount);
            }
        }
    }

    public void cleanupIapFolders(){
        log.info("Cleanup the FitwiseJob folder");
        Date date = DateUtils.addMonths(new Date(), -1);
        List<IapJobInputs> iapJobInputsList = iapJobInputsRepository.findByModifiedDateLessThanOrModifiedDateIsNull(date);
        for(IapJobInputs iapJobInputs : iapJobInputsList){
            if(iapJobInputs != null && !StringUtils.isEmpty(iapJobInputs.getTempDir())){
                try {
                    File file = new File(iapJobInputs.getTempDir());
                    FileUtils.deleteDirectory(file);
                } catch (IOException exception) {
                    log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
                }
                iapJobInputsRepository.delete(iapJobInputs);
            }
        }
    }

	public void removeExpiredDiscounts() {
		Date date = new Date();
		List<Programs> programsList = new ArrayList<>();
		List<OfferCodeDetail> offerCodeDetails = offDetailRepository.findAll();
		if (!offerCodeDetails.isEmpty()) {
			for (OfferCodeDetail offerCodeDetail : offerCodeDetails) {
				if (offerCodeDetail.getOfferEndingDate().before(date)) {
					//Check Discount status.No need to check expiry in InActive discounts.Since it's already removed from app store
					if(offerCodeDetail.getOfferStatus().equalsIgnoreCase(DiscountsConstants.OFFER_ACTIVE)) {
						offerCodeDetail.setOfferStatus(KeyConstants.KEY_EXPIRED);
						offDetailRepository.save(offerCodeDetail);
						//
						OfferCode offerCodes = offerCodeRepository.findByOfferCodeName(offerCodeDetail.getOfferCode());
						if (offerCodes != null) {
							offerCodes.setStatus(KeyConstants.KEY_EXPIRED);
							offerCodeRepository.save(offerCodes);
						}
						//
						DiscountOfferMapping discount = discountOfferMappingRepository
								.findByOfferCodeDetailOfferCodeId(offerCodeDetail.getOfferCodeId());
						if (discount != null && discount.getPrograms().getStatus().equalsIgnoreCase(InstructorConstant.PUBLISH)) {
							discount.setNeedDiscountUpdate(true);
							discount.setDiscountStatus(DiscountsConstants.EXPIRED_DISCOUNT);
							//Mail
							discount.setNeedMailUpdate(true);							
							programsList.add(discount.getPrograms());
							discountOfferMappingRepository.save(discount);
							
						}
					}					
				}
			}
			//
			if (!programsList.isEmpty()) {
				for (Programs program : programsList.stream().distinct().collect(Collectors.toList())) {
					fitwiseITMSUploadEntityService.publishOrRepublish(program);
				}
			}
		}
		}
	

}
