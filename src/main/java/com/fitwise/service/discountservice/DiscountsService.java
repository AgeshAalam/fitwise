package com.fitwise.service.discountservice;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.InstructorConstant;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.constants.discounts.DiscountsConstants;
import com.fitwise.entity.ProgramPrices;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.discounts.DiscountOfferMapping;
import com.fitwise.entity.discounts.ModeDurationMapping;
import com.fitwise.entity.discounts.OfferCode;
import com.fitwise.entity.discounts.OfferCodeDetail;
import com.fitwise.entity.discounts.OfferDuration;
import com.fitwise.entity.packaging.PackageOfferMapping;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.subscription.ProgramSubscription;
import com.fitwise.entity.subscription.SubscriptionStatus;
import com.fitwise.exception.ApplicationException;
import com.fitwise.program.model.ProgramPriceResponseModel;
import com.fitwise.program.model.ProgramResponseModel;
import com.fitwise.program.service.PriceService;
import com.fitwise.program.service.ProgramService;
import com.fitwise.repository.ProgramPricesRepository;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.discountsRepository.DiscountOfferMappingRepository;
import com.fitwise.repository.discountsRepository.ModeDurationMappingRepository;
import com.fitwise.repository.discountsRepository.OfferCodeDetailRepository;
import com.fitwise.repository.discountsRepository.OfferCodeRepository;
import com.fitwise.repository.discountsRepository.OfferDurationRepository;
import com.fitwise.repository.packaging.PackageOfferMappingRepository;
import com.fitwise.repository.packaging.SubscriptionPackageRepository;
import com.fitwise.repository.subscription.PackageSubscriptionRepository;
import com.fitwise.repository.subscription.ProgramSubscriptionRepo;
import com.fitwise.service.SubscriptionService;
import com.fitwise.service.itms.FitwiseITMSUploadEntityService;
import com.fitwise.service.payment.stripe.StripeService;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.specifications.jpa.ProgramJpa;
import com.fitwise.specifications.jpa.SubscriptionPackageJpa;
import com.fitwise.specifications.jpa.dao.OfferCountDao;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.OfferProgramRequestView;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.discounts.OfferCodeResponseView;
import com.fitwise.view.discounts.OfferCodesListView;
import com.fitwise.view.discounts.OfferDurationResponseView;
import com.fitwise.view.discounts.OfferPricesResponseView;
import com.fitwise.view.discounts.SaveOfferCodeRequestView;
import com.fitwise.view.discounts.SaveOfferCodeResponseView;
import com.fitwise.view.discounts.UpdateOfferCodeRequestView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class DiscountsService {

    @Autowired
    OfferCodeRepository offerCodeRepository;

    @Autowired
    OfferCodeDetailRepository offerCodeDetailRepository;

    @Autowired
    OfferDurationRepository offerDurationRepository;

    @Autowired
    ProgramPricesRepository pricesRepository;

    @Autowired
    ModeDurationMappingRepository modeDurationMappingRepository;

    @Autowired
    FitwiseUtils fitwiseUtils;

    @Autowired
    DiscountOfferMappingRepository discountOfferMappingRepository;

    @Autowired
    private PriceService priceService;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private ProgramService programService;
    @Autowired
    private StripeService stripeService;

    @Autowired
    private UserComponents userComponents;
    
    @Autowired
    private FitwiseITMSUploadEntityService fiEntityService;
    @Autowired
    ValidationService validationService;
    @Autowired
    ProgramSubscriptionRepo programSubscriptionRepo;
    @Autowired
    SubscriptionService subscriptionService;

    @Autowired
    SubscriptionPackageRepository subscriptionPackageRepository;

    @Autowired
    PackageOfferMappingRepository packageOfferMappingRepository;

    @Autowired
    private PackageSubscriptionRepository packageSubscriptionRepository;

    @Autowired
    UserProfileRepository userProfile;

    @Autowired
    ProgramRepository pgmRepo;

    private final SubscriptionPackageJpa subscriptionPackageJpa;
    private final ProgramJpa programJpa;


    public ResponseModel generateOfferCode(String offerName,Long programId) {
        log.info("Generate offer code starts");
        long start = new Date().getTime();
        long profilingStart;
    	 OfferCodeResponseView offerCodeResponseView = new OfferCodeResponseView();
        if (offerName == null || offerName.isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_OFFER_NAME_NULL, MessageConstants.ERROR);
        }

        //Validating the user input

        if (programId != null && programId == 0){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_NULL, MessageConstants.ERROR);
        }
        log.info("Basic validations : Time taken in millis : "+(new Date().getTime()-start));


        profilingStart = new Date().getTime();
        validateOfferName(offerName,programId);
        log.info("Offer name validation : Time taken in millis : "+(new Date().getTime()-profilingStart));

        profilingStart = new Date().getTime();
        Boolean isValid = false;
            Date date = new Date();
            DecimalFormat decimalFormat= new DecimalFormat("00");
            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            String offerCode = null;
            String newOfferName = offerName.replaceAll(" ","");
            String refName="";
            if (newOfferName.length() >= 3) {
            	refName=eliminateSpecialchar(offerName.replaceAll(" ", "").substring(0, 3).toUpperCase());
                offerCode = refName + decimalFormat.format(Double.valueOf(localDate.getDayOfMonth())) + decimalFormat.format(Double.valueOf(localDate.getMonthValue()));
            } else if (newOfferName.length() == 2) {
            	refName=eliminateSpecialchar(offerName.replaceAll(" ", "").toUpperCase());
                offerCode = refName + "0" + decimalFormat.format(Double.valueOf(localDate.getDayOfMonth())) + decimalFormat.format(Double.valueOf(localDate.getMonthValue()));
            }
        log.info("Offer code generation from offer name and date : Time taken in millis : "+(new Date().getTime()-profilingStart));

        profilingStart = new Date().getTime();
        OfferCode offerCode1 = offerCodeRepository.findByOfferCodeName(offerCode);
        log.info("Query : Time taken in millis : "+(new Date().getTime()-profilingStart));

        if(offerCode1 == null || (offerCode1 != null && offerCode1.getStatus().equalsIgnoreCase(DiscountsConstants.CODE_UNUSED))){
                isValid = true;
            }
            if(!isValid){
                profilingStart = new Date().getTime();
                offerCode = validateOfferCodeWithValidity(offerCode, newOfferName);
                log.info("Generating new offer code if generated one already present in DB : Time taken in millis : "+(new Date().getTime()-profilingStart));
            }

            profilingStart = new Date().getTime();
            OfferCode ofCode = offerCodeRepository.findByOfferCodeName(offerCode);
            if(ofCode == null){
                ofCode = new OfferCode();
                ofCode.setOfferCodeName(offerCode);
                ofCode.setStatus(DiscountsConstants.CODE_UNUSED);
                ofCode = offerCodeRepository.save(ofCode);
            }
        log.info("DB update : Time taken in millis : "+(new Date().getTime()-profilingStart));


        profilingStart = new Date().getTime();
        offerCodeResponseView.setId(ofCode.getId());
        offerCodeResponseView.setOfferCodeName(ofCode.getOfferCodeName());
        offerCodeResponseView.setStatus(ofCode.getStatus());
        log.info("Response construction : Time taken in millis : "+(new Date().getTime()-profilingStart));
        log.info("Generate offer code : Total time taken in millis : "+(new Date().getTime() - start));
        log.info("Generate offer code ends");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PROMO_CODE_GENERATED, offerCodeResponseView);
    }

    private String eliminateSpecialchar(String upperCase) {
    	String refname=null;
    	refname=upperCase.replaceAll("[^a-zA-Z0-9._]", "");
    	if(refname.isEmpty()) {
    		String random=UUID.randomUUID().toString();
    		if(random.length()>=3) {
    			random=random.replaceAll("-", "").substring(0, 3).toUpperCase();
    		}
    		refname=random;
    	}
		return refname;		
	}

	@Transactional
    public ResponseModel saveOfferCode(SaveOfferCodeRequestView saveOfferCodeRequestView) throws ParseException {
        log.info("Save offer code starts.");
        long apiStartTimeMillis = new Date().getTime();
        User user = userComponents.getUser();
        Long programId = null;
        Long packageId = null;
        //Validating the use input
        if (saveOfferCodeRequestView.getProgramId() != null && saveOfferCodeRequestView.getProgramId() != 0){
            programId = saveOfferCodeRequestView.getProgramId();
        }
        if (saveOfferCodeRequestView.getPackageId() != null && saveOfferCodeRequestView.getPackageId() != 0){
            packageId = saveOfferCodeRequestView.getPackageId();
        }
        //Model Validation
        ValidationUtils.throwException(saveOfferCodeRequestView.getMode() == null, ValidationMessageConstants.MSG_OFFER_MODE_NULL, Constants.BAD_REQUEST);
        ValidationUtils.throwException(saveOfferCodeRequestView.getIsNewUser() == null, ValidationMessageConstants.MSG_OFFER_APPLICABLE_USERS_NULL, Constants.BAD_REQUEST);
        log.info("Basic validations : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();

        OfferCodeDetail offerCodeDetail = new OfferCodeDetail();
        SaveOfferCodeResponseView sResponseView = new SaveOfferCodeResponseView();

        /** Offer Code validation **/
        String offerCode = saveOfferCodeRequestView.getOfferCode();
        if (Boolean.TRUE.equals(validateOfferCode(offerCode))) {
            offerCodeDetail.setOfferCode(offerCode);
        }
        /** Offer Name Validation **/
        String offerReferenceName = saveOfferCodeRequestView.getOfferName();
        if (programId != null && programId != 0){
            if (Boolean.TRUE.equals(validateOfferName(offerReferenceName,programId))) {
                String firstName="";
                String progName="";
                String newRefname="";
                offerCodeDetail.setOfferName(offerReferenceName.trim());
                //Construct Offer Name for iTMS upload
                Programs program= pgmRepo.findByProgramId(programId);
                UserProfile userName=userProfile.findByUser(user);
                if(userName!=null) {
                    firstName=userName.getFirstName();
                    if(firstName.length()>5) {
                        firstName=firstName.substring(0, 4);
                    }
                }
                if(program!=null) {
                    progName=program.getTitle();
                    if(progName.length()>5) {
                        progName=progName.substring(0, 4);
                    }
                }
                if(offerReferenceName!=null) {
                    if(offerReferenceName.length()>5) {
                        newRefname=offerReferenceName.substring(0, 4);
                    }
                }
                String random=UUID.randomUUID().toString().toUpperCase();
                String append=random.substring(0, random.indexOf('-'));
                String appleOfferName=firstName.concat(firstName).concat(progName).concat(newRefname).concat(append);
                offerCodeDetail.setAppleOfferName(appleOfferName);
            }
        } else {
            if (Boolean.TRUE.equals(validateOfferNameForPackakge(offerReferenceName, packageId))){
                String firstName="";
                String packName="";
                String newRefname="";
                offerCodeDetail.setOfferName(offerReferenceName.trim());
                //Construct Offer Name for iTMS upload
                SubscriptionPackage subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageId(packageId);
                UserProfile userName=userProfile.findByUser(user);
                if(userName!=null) {
                    firstName=userName.getFirstName();
                    if(firstName.length()>5) {
                        firstName=firstName.substring(0, 4);
                    }
                }
                if(subscriptionPackage!=null) {
                    packName=subscriptionPackage.getTitle();
                    if(packName.length()>5) {
                        packName=packName.substring(0, 4);
                    }
                }
                if(offerReferenceName!=null) {
                    if(offerReferenceName.length()>5) {
                        newRefname=offerReferenceName.substring(0, 4);
                    }
                }
                String random=UUID.randomUUID().toString().toUpperCase();
                String append=random.substring(0, random.indexOf('-'));
                String appleOfferName=firstName.concat(firstName).concat(packName).concat(newRefname).concat(append);
                offerCodeDetail.setAppleOfferName(appleOfferName);
            }
        }
        log.info("Validate offer name : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        
        /** Offer availability date validations **/
        Date now = new Date();
       //TODO INTRO_OFFER : add validation for new User
        if (saveOfferCodeRequestView.getOfferStartDate() == null || saveOfferCodeRequestView.getOfferEndDate() == null) {
            throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, ValidationMessageConstants.MSG_PROVIDE_START_DATE_AND_END_DATE, null);
        }
        if(saveOfferCodeRequestView.getIsNewUser().booleanValue() && fitwiseUtils.isSameDay(saveOfferCodeRequestView.getOfferStartDate(),saveOfferCodeRequestView.getOfferEndDate())){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_OFFER_SAME_DATES_NEW_USER, null);
        }

        Date startDate = saveOfferCodeRequestView.getOfferStartDate();
        Date endDate = saveOfferCodeRequestView.getOfferEndDate();
        if(!(fitwiseUtils.isSameDay(saveOfferCodeRequestView.getOfferStartDate(),now) || saveOfferCodeRequestView.getOfferStartDate().after(now))) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_START_DATE_GREATER_THAN_TODAY, null);
        } else if (endDate.before(startDate)) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_START_DATE_GREATER_THAN_END_DATE, null);
        }
        log.info("Offer availability date validation : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        LocalDate startLocalDate = saveOfferCodeRequestView.getOfferStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = saveOfferCodeRequestView.getOfferEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate todayDate = LocalDate.now();
        if((startLocalDate.getYear() - todayDate.getYear()) > KeyConstants.KEY_OFFER_AVAILABILTY_YEAR_LIMIT || (endLocalDate.getYear() - todayDate.getYear()) > KeyConstants.KEY_OFFER_AVAILABILTY_YEAR_LIMIT){
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_OFFER_AVAILABILITY_DATE_INCORRECT, null);
        }
        int yearDifference = endLocalDate.getYear() - startLocalDate.getYear();
        if(yearDifference > KeyConstants.KEY_OFFER_AVAILABILTY_YEAR_LIMIT){
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_OFFER_AVAILABILITY_PERIOD_INCORRECT, null);
        }
        offerCodeDetail.setOfferStartingDate(startDate);
        offerCodeDetail.setOfferEndingDate(endDate);

        /** New or Existing User **/
        offerCodeDetail.setIsNewUser(saveOfferCodeRequestView.getIsNewUser());

        /** Offer Duration **/
        OfferDuration offerDuration = offerDurationRepository.findByDurationId(saveOfferCodeRequestView.getDurationId());
        offerCodeDetail.setOfferDuration(offerDuration);

        /** No of Periods (Required for metadata XML generation) 
        offerCodeDetail.setOfferDurationCount(DiscountsConstants.DEFAULT_PERIOD);**/

        /** Offer Price **/
        if (saveOfferCodeRequestView.getMode().equalsIgnoreCase(DiscountsConstants.MODE_FREE)) {
            offerCodeDetail.setOfferMode(DiscountsConstants.MODE_FREE);
        } else {
            ValidationUtils.throwException(saveOfferCodeRequestView.getOfferPriceId() == null, ValidationMessageConstants.MSG_OFFER_PRICE_ID_NULL, Constants.BAD_REQUEST);
            Optional<ProgramPrices> programPrices = pricesRepository.findById(saveOfferCodeRequestView.getOfferPriceId());
            ValidationUtils.throwException(!programPrices.isPresent(), MessageConstants.MSG_PRICE_LIST_NOT_FOUND, Constants.BAD_REQUEST);
            ProgramPrices prices = programPrices.get();
            if (prices != null) {
                offerCodeDetail.setOfferMode(DiscountsConstants.MODE_PAY_AS_YOU_GO);
                offerCodeDetail.setOfferPrice(prices);
            }
        }
        log.info("Query to get offer prices : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        /** Offer Status **/
        offerCodeDetail.setOfferStatus(DiscountsConstants.OFFER_ACTIVE);
        //TODO: get Clarify on this.once mapped with programs change InUse flag as true
        offerCodeDetail.setInUse(true);
        offerCodeDetail.setOwner(user);
        OfferCodeDetail offCodeDetail = offerCodeDetailRepository.save(offerCodeDetail);
        // Set OfferCode Status as 'Used' in 'Offer_Code' table
        if (offCodeDetail != null) {
            OfferCode offerCode1 = offerCodeRepository.findByOfferCodeName(offCodeDetail.getOfferCode());
            offerCode1.setStatus(DiscountsConstants.CODE_USED);
            offerCodeRepository.save(offerCode1);
        }
        log.info("Query to save offer code : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        String formattedPrice = null;
        if(saveOfferCodeRequestView.getMode().equals(DiscountsConstants.MODE_PAY_AS_YOU_GO))
        {
            formattedPrice = fitwiseUtils.formatPrice(offCodeDetail.getOfferPrice().getPrice());
        }
        else {
            formattedPrice = KeyConstants.KEY_CURRENCY_US_DOLLAR+KeyConstants.KEY_DEFAULT_PRICE_FORMAT;
        }
        /** Response View **/
        sResponseView.setOfferCodeId(offCodeDetail.getOfferCodeId());
        sResponseView.setIsNewUser(offCodeDetail.getIsNewUser());
        sResponseView.setOfferCode(offCodeDetail.getOfferCode().toUpperCase());
        sResponseView.setOfferName(offCodeDetail.getOfferName().trim());
        sResponseView.setOfferMode(offCodeDetail.getOfferMode());
        sResponseView.setOfferPrice(offCodeDetail.getOfferPrice());
        sResponseView.setFormattedOfferPrice(formattedPrice);
        sResponseView.setOfferStartDate(fitwiseUtils.formatDate(offCodeDetail.getOfferStartingDate()));
        sResponseView.setOfferEndDate(fitwiseUtils.formatDate(offCodeDetail.getOfferEndingDate()));
        sResponseView.setOfferDuration(offCodeDetail.getOfferDuration());
        //sResponseView.setOfferDurationCount(offCodeDetail.getOfferDurationCount());
        sResponseView.setOfferStatus(offCodeDetail.getOfferStatus());
        log.info("Construct response model : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Save offer code ends.");
       
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_OFFER_CODE_SAVED, sResponseView);
    }


    public Boolean validateOfferCode(String offerCode) {
        Boolean isValidCode = true;
        OfferCode ofCode = offerCodeRepository.findByOfferCodeName(offerCode);
        if (ofCode == null) {
            isValidCode = false;
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_OFFER_CODE_INVALID, null);
        }

        List<String> statusList = Arrays.asList(new String[]{KeyConstants.KEY_EXPIRED, KeyConstants.KEY_INACTIVE_CAMEL_CASE});
        List<OfferCodeDetail> offerCodeDetails = offerCodeDetailRepository.findByOfferCodeAndOfferStatusNotInAndIsInUse(offerCode, statusList, true);
        if (!offerCodeDetails.isEmpty()) {
            isValidCode = false;
            throw new ApplicationException(Constants.BAD_REQUEST,
                    MessageConstants.MSG_OFFER_CODE_ALREADY_USED, null);
        }

        return isValidCode;
    }

    public ResponseModel removeOfferCode(Long offerId) {
        log.info("Remove offer code starts.");
        long apiStartTimeMillis = new Date().getTime();
        User user = userComponents.getUser();
        log.info("Get user : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        OfferCodeDetail offCode = offerCodeDetailRepository.findByOfferCodeIdAndOwner(offerId, user);
        log.info("Query to get offer code : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if (offCode == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_OFFER_CODE_DOESNT_EXIST, null);
        }
        if (offCode.getOfferStatus() != null && offCode.getOfferStatus().equalsIgnoreCase(DiscountsConstants.OFFER_INACTIVE)) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_OFFER_CODE_ALREADY_INACTIVE, MessageConstants.ERROR);
        }
        log.info("Basic validations : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        offCode.setOfferStatus(DiscountsConstants.OFFER_INACTIVE);
        offerCodeDetailRepository.save(offCode);
        log.info("Query to save offer code details : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        //
        OfferCode offer = offerCodeRepository.findByOfferCodeName(offCode.getOfferCode());
        log.info("Query to get offer code : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        offer.setStatus(DiscountsConstants.OFFER_INACTIVE);
        offerCodeRepository.save(offer);
        log.info("Query to save offer code : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        DiscountOfferMapping discountOfferMapping = discountOfferMappingRepository.findByOfferCodeDetailOfferCodeId(offerId);
        log.info("Query to get discounts offer mapping : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        
        if (discountOfferMapping != null) {   	
        	//iTMS tool upload required to remove Offer.
            if (discountOfferMapping.getPrograms().getStatus() != null && discountOfferMapping.getPrograms().getStatus().equalsIgnoreCase(InstructorConstant.PUBLISH)) {
            	if(Boolean.TRUE.equals(discountOfferMapping.getNeedDiscountUpdate())) {
            		//Not yet updated in App store
            		discountOfferMapping.setNeedDiscountUpdate(false);
            		//Mail
                	discountOfferMapping.setNeedMailUpdate(false);
            	}else {
            		// New User
            		discountOfferMapping.setNeedDiscountUpdate(true);
            		// Also set true for active status record if any.
            		if(Boolean.TRUE.equals(discountOfferMapping.getOfferCodeDetail().getIsNewUser())) {
            			DiscountOfferMapping discount=discountOfferMappingRepository.findTop1ByProgramsProgramIdAndOfferCodeDetailIsNewUserAndOfferCodeDetailOfferStatus(discountOfferMapping.getPrograms().getProgramId(), true,DiscountsConstants.OFFER_ACTIVE);
                    	if(discount!=null) {
                    		discount.setNeedDiscountUpdate(true);
                    		discountOfferMappingRepository.save(discount);
                    	}
                    }else {
                    	// Existing User
                    	discountOfferMapping.setNeedDiscountUpdate(true);
                		//Mail
                    	discountOfferMapping.setNeedMailUpdate(true);
                    }
                    log.info("Query to get and save discount offer mapping : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                    profilingEndTimeMillis = new Date().getTime();
                    // Stopping ITMS upload due to IAP remove in iOS app.
                	/*fiEntityService.publishOrRepublish(discountOfferMapping.getPrograms());
                    log.info("ITMS publish or republish completed : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                    profilingEndTimeMillis = new Date().getTime();*/
            	} 
            	
    		}else {
    			// Program is not yet Published into App store. iTMS tool run is not required.
    			discountOfferMapping.setNeedDiscountUpdate(false);
    			//Mail not required
    			discountOfferMapping.setNeedMailUpdate(false);
    		}            
            discountOfferMapping.setDiscountStatus(DiscountsConstants.REMOVE_DISCOUNT_INACTIVE);
        	discountOfferMappingRepository.save(discountOfferMapping);
            log.info("Query to save discount offer mapping : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
        }
        PackageOfferMapping packageOfferMapping = packageOfferMappingRepository.findByOfferCodeDetailOfferCodeId(offerId);
        log.info("Query to package offer mapping : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if(packageOfferMapping != null){
            if (packageOfferMapping.getSubscriptionPackage().getStatus() != null && packageOfferMapping.getSubscriptionPackage().getStatus().equalsIgnoreCase(InstructorConstant.PUBLISH)) {
                packageOfferMapping.setNeedDiscountUpdate(true);
                packageOfferMapping.setDiscountStatus(DiscountsConstants.REMOVE_DISCOUNT_INACTIVE);
                //Mail
                packageOfferMapping.setNeedMailUpdate(true);
                packageOfferMappingRepository.save(packageOfferMapping);
            }else {
                // Program is not yet Published into App store. iTMS tool run is not required.
                packageOfferMapping.setNeedDiscountUpdate(false);
                packageOfferMapping.setDiscountStatus(DiscountsConstants.REMOVE_DISCOUNT_INACTIVE);

                //Mail not required
                packageOfferMapping.setNeedMailUpdate(false);
                packageOfferMappingRepository.save(packageOfferMapping);
            }
        }
        log.info("Query to save package offer mapping : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Remove offer code ends.");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_OFFER_CODE_REMOVED, null);
    }

    public void unpublishDiscounts(Programs program) {
		List<DiscountOfferMapping> discountsList = program.getProgramDiscountMapping();
		if (!discountsList.isEmpty()) {
			for (DiscountOfferMapping discount : discountsList) {
				OfferCodeDetail offerdetail = offerCodeDetailRepository
						.findByOfferCodeId(discount.getOfferCodeDetail().getOfferCodeId());
				if(offerdetail.getOfferStatus().equalsIgnoreCase(DiscountsConstants.OFFER_ACTIVE)) {
					offerdetail.setOfferStatus(DiscountsConstants.OFFER_INACTIVE);
					offerCodeDetailRepository.save(offerdetail);
					//
					OfferCode offerCode = offerCodeRepository.findByOfferCodeName(offerdetail.getOfferCode());
					offerCode.setStatus(DiscountsConstants.OFFER_INACTIVE);
			        offerCodeRepository.save(offerCode);
			        //
			        discount.setNeedDiscountUpdate(true);
					discount.setDiscountStatus(DiscountsConstants.REMOVE_DISCOUNT);
					discount.setNeedMailUpdate(true);
					discountOfferMappingRepository.save(discount);
				}
			}
		}
    }

    public OfferCodesListView getAllOfferCodes() {
        User user = userComponents.getUser();
        OfferCodesListView allOffersList = new OfferCodesListView();

        List<SaveOfferCodeResponseView> expiredOffers = new ArrayList<>();
        List<SaveOfferCodeResponseView> upComingOffers = new ArrayList<>();
        List<SaveOfferCodeResponseView> currentOffers = new ArrayList<>();
            List<OfferCodeDetail> offerCodeDetails = offerCodeDetailRepository.findByOwnerAndOfferStatus(user, DiscountsConstants.OFFER_ACTIVE);
            if (offerCodeDetails.isEmpty()) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_OFFER_CODE_DETAILS_NOT_FOUND, null);
            }
            //removing discarded offer codes
            offerCodeDetails = offerCodeDetails.stream().filter(offerCodeDetail -> offerCodeDetail.isInUse()).collect(Collectors.toList());
            if (!offerCodeDetails.isEmpty()) {
                for (OfferCodeDetail offerCodeDetail : offerCodeDetails) {
                    String formattedPrice = null;
                    if(offerCodeDetail.getOfferMode().equals(DiscountsConstants.MODE_PAY_AS_YOU_GO))
                    {
                        formattedPrice = fitwiseUtils.formatPrice(offerCodeDetail.getOfferPrice().getPrice());
                    }
                    else {
                        formattedPrice = KeyConstants.KEY_CURRENCY_US_DOLLAR+KeyConstants.KEY_DEFAULT_PRICE_FORMAT;
                    }
                    SaveOfferCodeResponseView sResponseView = new SaveOfferCodeResponseView();
                    sResponseView.setOfferCodeId(offerCodeDetail.getOfferCodeId());
                    sResponseView.setOfferName(offerCodeDetail.getOfferName().trim());
                    sResponseView.setOfferCode(offerCodeDetail.getOfferCode().toUpperCase());
                    sResponseView.setOfferMode(offerCodeDetail.getOfferMode());
                    sResponseView.setOfferDuration(offerCodeDetail.getOfferDuration());
                    sResponseView.setOfferStartDate(fitwiseUtils.formatDate(offerCodeDetail.getOfferStartingDate()));
                    sResponseView.setOfferEndDate(fitwiseUtils.formatDate(offerCodeDetail.getOfferEndingDate()));
                    sResponseView.setOfferPrice(offerCodeDetail.getOfferPrice());
                    sResponseView.setFormattedOfferPrice(formattedPrice);
                    sResponseView.setOfferStatus(offerCodeDetail.getOfferStatus());
                    //sResponseView.setOfferDurationCount(offerCodeDetail.getOfferDurationCount());
                    sResponseView.setIsNewUser(offerCodeDetail.getIsNewUser());
                    // Offer Validity calculation
                    Date now = new Date();
                    Date offerStart = offerCodeDetail.getOfferStartingDate();
                    Date offerEnd = offerCodeDetail.getOfferEndingDate();
                    // Current Offers
                   // if ((offerStart.equals(now) || offerStart.before(now)) && (offerEnd.equals(now) || offerEnd.after(now))) {
                    if ((fitwiseUtils.isSameDay(offerStart,now) || offerStart.before(now)) && (fitwiseUtils.isSameDay(offerEnd,now) || offerEnd.after(now))) {
                        currentOffers.add(sResponseView);
                    }
                    // UpComing Offers
                    else if (offerStart.after(now) && offerEnd.after(now)) {
                        upComingOffers.add(sResponseView);
                    }
                    // Expired Offers
                    else if (offerStart.before(now) && offerEnd.before(now)) {
                        expiredOffers.add(sResponseView);
                    }
                }
                allOffersList.setCurrentOffers(currentOffers);
                allOffersList.setUpComingOffers(upComingOffers);
                allOffersList.setExpiredOffers(expiredOffers);
            }else{
                throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, "", "");
            }
       return allOffersList;
    }

    public SaveOfferCodeResponseView getOfferCodeDetail(Long offerId) throws ParseException {
        User user = userComponents.getUser();
        SaveOfferCodeResponseView sResponseView = new SaveOfferCodeResponseView();

        OfferCodeDetail offerCodeDetail = offerCodeDetailRepository.findByOfferCodeIdAndOwner(offerId, user);
        if (offerCodeDetail == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_OFFER_CODE_ID_INVALID, null);
        } else {
            String formattedPrice = null;
            if(offerCodeDetail.getOfferMode().equals(DiscountsConstants.MODE_PAY_AS_YOU_GO))
            {
                formattedPrice = fitwiseUtils.formatPrice(offerCodeDetail.getOfferPrice().getPrice());
            }
            else {
                formattedPrice = KeyConstants.KEY_CURRENCY_US_DOLLAR+KeyConstants.KEY_DEFAULT_PRICE_FORMAT;
            }
            sResponseView.setOfferCodeId(offerCodeDetail.getOfferCodeId());
            sResponseView.setOfferName(offerCodeDetail.getOfferName().trim());
            sResponseView.setOfferCode(offerCodeDetail.getOfferCode().toUpperCase());
            sResponseView.setOfferMode(offerCodeDetail.getOfferMode());
            sResponseView.setOfferDuration(offerCodeDetail.getOfferDuration());
            sResponseView.setOfferStartDate(fitwiseUtils.formatDate(offerCodeDetail.getOfferStartingDate()));
            sResponseView.setOfferEndDate(fitwiseUtils.formatDate(offerCodeDetail.getOfferEndingDate()));
            sResponseView.setOfferPrice(offerCodeDetail.getOfferPrice());
            sResponseView.setFormattedOfferPrice(formattedPrice);
            sResponseView.setOfferStatus(offerCodeDetail.getOfferStatus());
            //sResponseView.setOfferDurationCount(offerCodeDetail.getOfferDurationCount());
            sResponseView.setIsNewUser(offerCodeDetail.getIsNewUser());
        }
        return sResponseView;
     }

    @Transactional
    public ResponseModel updateOfferCode(UpdateOfferCodeRequestView updateOfferCodeRequestView) throws ParseException {
        log.info("Update Offer code starts");
        long start = new Date().getTime();
        long profilingStart = new Date().getTime();
        long profilingEnd;
        User user = userComponents.getUser();
       //TODO INTRO_OFFER : we can edit all details for New User (new Offer)
        ValidationUtils.throwException(updateOfferCodeRequestView.getOfferId()==null, ValidationMessageConstants.MSG_OFFER_CODE_ID_NULL, Constants.BAD_REQUEST);
        OfferCodeDetail offerCodeDetail = offerCodeDetailRepository.findByOfferCodeIdAndOwner(updateOfferCodeRequestView.getOfferId(), user);
        if(offerCodeDetail == null){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_OFFER_CODE_ID_INVALID, null);
        }
        SaveOfferCodeResponseView sResponseView = new SaveOfferCodeResponseView();
        if (offerCodeDetail.getOfferStatus() != null && offerCodeDetail.getOfferStatus().equalsIgnoreCase(DiscountsConstants.OFFER_INACTIVE)) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_OFFER_CODE_INACTIVE, null);
        }
        if (offerCodeDetail.getOfferMode() != null && offerCodeDetail.getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_FREE)) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_FREE_OFFER_CANNOT_UPDATED, null);
        }

        ValidationUtils.throwException(updateOfferCodeRequestView.getPriceId() == null, ValidationMessageConstants.MSG_OFFER_PRICE_ID_NULL, Constants.BAD_REQUEST);
        ProgramPrices offerPrice = pricesRepository.findByProgramPricesId(updateOfferCodeRequestView.getPriceId());
        if(offerPrice == null){
            throw new ApplicationException(Constants.BAD_REQUEST,ValidationMessageConstants.MSG_PRICE_ID_INVALID,MessageConstants.ERROR);
        }

        DiscountOfferMapping discountOfferMapping = discountOfferMappingRepository.findByOfferCodeDetailOfferCodeId(offerCodeDetail.getOfferCodeId());

        if (discountOfferMapping != null)
        {
            if (discountOfferMapping.getPrograms().getProgramPrice() == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_PRICE_NOT_FOUND, null);
            }

            if (offerPrice.getPrice() >= discountOfferMapping.getPrograms().getProgramPrices().getPrice()) {
                throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_OFFER_PRICE_GREATER_THAN_PROGRAM_PRICE, null);
            }
        }

        PackageOfferMapping packageOfferMapping = packageOfferMappingRepository.findByOfferCodeDetailOfferCodeId(offerCodeDetail.getOfferCodeId());
        if(packageOfferMapping != null){
            if (packageOfferMapping.getSubscriptionPackage().getPrice() == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_PRICE_NOT_FOUND, null);
            }

            if (offerPrice.getPrice() >= packageOfferMapping.getSubscriptionPackage().getPrice()) {
                throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_OFFER_PRICE_GREATER_THAN_PACKAGE_PRICE, null);
            }
        }
        profilingEnd = new Date().getTime();
        log.info("Validations in update offer code : Time taken in millis : "+(profilingEnd-profilingStart));

        //Marking Existing offer code as inactive
        profilingStart = new Date().getTime();
        offerCodeDetail.setInUse(false);
        offerCodeDetail.setOfferStatus(DiscountsConstants.OFFER_INACTIVE);
        offerCodeDetail.setDiscardDate(new Date());
        offerCodeDetailRepository.save(offerCodeDetail);
        //Creating a duplicate offer code and marking it as active
        OfferCodeDetail newOfferCodeDetail = new OfferCodeDetail();
        newOfferCodeDetail.setOfferName(offerCodeDetail.getOfferName().trim());
        newOfferCodeDetail.setOfferCode(offerCodeDetail.getOfferCode());
        newOfferCodeDetail.setOfferDuration(offerCodeDetail.getOfferDuration());
        newOfferCodeDetail.setOfferDurationCount(offerCodeDetail.getOfferDurationCount());
        newOfferCodeDetail.setOfferStartingDate(offerCodeDetail.getOfferStartingDate());
        newOfferCodeDetail.setOfferEndingDate(offerCodeDetail.getOfferEndingDate());
        newOfferCodeDetail.setOfferMode(offerCodeDetail.getOfferMode());
        newOfferCodeDetail.setOfferPrice(offerPrice);
        newOfferCodeDetail.setIsNewUser(offerCodeDetail.getIsNewUser());
        newOfferCodeDetail.setOfferStatus(offerCodeDetail.getOfferStatus());
        newOfferCodeDetail.setInUse(true);
        newOfferCodeDetail.setOfferStatus(DiscountsConstants.OFFER_ACTIVE);
        newOfferCodeDetail.setOwner(offerCodeDetail.getOwner());
        newOfferCodeDetail = offerCodeDetailRepository.save(newOfferCodeDetail);
        profilingEnd = new Date().getTime();
        log.info("Updating Offer code : Time taken in millis : "+(profilingEnd-profilingStart));

        if(discountOfferMapping != null)
        {
            //Creating Offer and program mapping for the duplicated offer code
            DiscountOfferMapping newDiscountOfferMapping = new DiscountOfferMapping();
            newDiscountOfferMapping.setInstructorId(discountOfferMapping.getInstructorId());
            newDiscountOfferMapping.setLevelMapping(discountOfferMapping.getLevelMapping());
            newDiscountOfferMapping.setPrograms(discountOfferMapping.getPrograms());
            newDiscountOfferMapping.setOfferCodeDetail(newOfferCodeDetail);
            newDiscountOfferMapping.setNeedDiscountUpdate(true);
            newDiscountOfferMapping.setDiscountStatus(DiscountsConstants.UPDATE_DISCOUNT);
            //MAil
            discountOfferMapping.setNeedMailUpdate(true);
            discountOfferMappingRepository.save(newDiscountOfferMapping);
            //iTMS tool upload required with updated Offer. Stopping ITMS upload due to IAP remove in iOS app
            /*if (discountOfferMapping.getPrograms().getStatus() != null && discountOfferMapping.getPrograms().getStatus().equalsIgnoreCase(InstructorConstant.PUBLISH)) {
                fiEntityService.publishOrRepublish(discountOfferMapping.getPrograms());
            }*/
            //Creating coupon on Stripe for the duplicated offer code
            profilingStart = new Date().getTime();
            stripeService.createCoupon(newOfferCodeDetail, discountOfferMapping.getPrograms().getProgramPrices().getPrice());
            profilingEnd = new Date().getTime();
            log.info("Creating stripe coupon for a program : Time taken in millis : "+(profilingEnd-profilingStart));
        }
        if(packageOfferMapping != null)
        {
            //Creating Offer and program mapping for the duplicated offer code
            PackageOfferMapping newPackageOfferMapping = new PackageOfferMapping();
            newPackageOfferMapping.setInstructor(packageOfferMapping.getInstructor());
            newPackageOfferMapping.setDiscountLevel(packageOfferMapping.getDiscountLevel());
            newPackageOfferMapping.setSubscriptionPackage(packageOfferMapping.getSubscriptionPackage());
            newPackageOfferMapping.setOfferCodeDetail(newOfferCodeDetail);
            newPackageOfferMapping.setNeedDiscountUpdate(true);
            newPackageOfferMapping.setDiscountStatus(DiscountsConstants.UPDATE_DISCOUNT);
            //Mail
            newPackageOfferMapping.setNeedMailUpdate(true);
            packageOfferMappingRepository.save(newPackageOfferMapping);

            //Creating coupon on Stripe for the duplicated offer code
            profilingStart = new Date().getTime();
            stripeService.createCoupon(newOfferCodeDetail, packageOfferMapping.getSubscriptionPackage().getPrice());
            profilingEnd = new Date().getTime();
            log.info("Creating stripe coupon for a package : Time taken in millis : "+(profilingEnd-profilingStart));

        }
        String formattedPrice = null;
        if(newOfferCodeDetail.getOfferMode().equals(DiscountsConstants.MODE_PAY_AS_YOU_GO))
        {
            formattedPrice = fitwiseUtils.formatPrice(newOfferCodeDetail.getOfferPrice().getPrice());
        }
        else {
            formattedPrice = KeyConstants.KEY_CURRENCY_US_DOLLAR+KeyConstants.KEY_DEFAULT_PRICE_FORMAT;
        }
        profilingStart = new Date().getTime();
        sResponseView.setOfferCodeId(newOfferCodeDetail.getOfferCodeId());
        sResponseView.setOfferName(newOfferCodeDetail.getOfferName().trim());
        sResponseView.setOfferCode(newOfferCodeDetail.getOfferCode().toUpperCase());
        sResponseView.setOfferMode(newOfferCodeDetail.getOfferMode());
        sResponseView.setOfferDuration(newOfferCodeDetail.getOfferDuration());
        sResponseView.setOfferStartDate(fitwiseUtils.formatDate(newOfferCodeDetail.getOfferStartingDate()));
        sResponseView.setOfferEndDate(fitwiseUtils.formatDate(newOfferCodeDetail.getOfferEndingDate()));
        sResponseView.setOfferPrice(newOfferCodeDetail.getOfferPrice());
        sResponseView.setFormattedOfferPrice(formattedPrice);
        sResponseView.setOfferStatus(newOfferCodeDetail.getOfferStatus());
        //sResponseView.setOfferDurationCount(newOfferCodeDetail.getOfferDurationCount());
        sResponseView.setIsNewUser(newOfferCodeDetail.getIsNewUser());
        profilingEnd = new Date().getTime();
        log.info("Offer Response construction : Time taken in millis : "+(profilingEnd-profilingStart));
        log.info("Update Offer code : Total Time taken in millis : "+(profilingEnd-start));
        log.info("Update offer code ends");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_OFFER_CODE_UPDATED, sResponseView);
    }

    public ResponseModel getAllOfferDuration(String mode) {
        List<OfferDurationResponseView> listDuration = new ArrayList<>();
        List<ModeDurationMapping> mappings = null;
        if (mode != null && mode.equalsIgnoreCase(DiscountsConstants.MODE_FREE)) {
            mappings = modeDurationMappingRepository.findByMode(DiscountsConstants.MODE_FREE);
        } else {
            mappings = modeDurationMappingRepository.findByMode(DiscountsConstants.MODE_PAY_AS_YOU_GO);
        }
        if (!mappings.isEmpty()) {
            for (ModeDurationMapping mapping : mappings) {
                OfferDurationResponseView offerDurationResponseView = new OfferDurationResponseView();
                offerDurationResponseView.setDurationId(mapping.getDuration().getDurationId());
                offerDurationResponseView.setDurationPeriod(mapping.getDuration().getDurationPeriod());
                listDuration.add(offerDurationResponseView);
            }
        } else {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_OFFER_DURATIONS_EMPTY, null);
        }

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_OFFER_DURATION_LIST_FETCHED, listDuration);
    }

    public ResponseModel getPriceList() {
        List<OfferPricesResponseView> offerPricesList = new ArrayList<>();
        List<ProgramPrices> programPrices = pricesRepository.findAll(Sort.by(Sort.Direction.ASC, "tier"));
        if (programPrices.isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_OFFER_PRICE_LIST_EMPTY, null);
        } else {
            for (ProgramPrices programPrice : programPrices) {
                OfferPricesResponseView offerPricesResponseView = new OfferPricesResponseView();
                offerPricesResponseView.setPricesId(programPrice.getProgramPricesId());
                offerPricesResponseView.setPrice(programPrice.getPrice());
                offerPricesList.add(offerPricesResponseView);
            }
        }

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_OFFER_PRICES_LIST_FETCHED, offerPricesList);
    }

    /**
     * Validating the offer name duplication on user and program level
     * @param offerReferenceName
     * @param programId
     * @return
     */
    public Boolean validateOfferName(String offerReferenceName, Long programId) {
		User user = userComponents.getUser();
        //Validating the user input
        if (programId == null || programId == 0){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_NULL, MessageConstants.ERROR);
        }
		//Length Validation
		if (offerReferenceName != null
				&& (offerReferenceName.trim().length() < 2 || offerReferenceName.trim().length() > 64)) {
			throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_OFFER_NAME_LENGTH_INCORRECT,
					null);
		}
		// Note :The reference name (Offer Name) must be unique for Instructor/Program (Fitwise DB);
		// can’t re-use a reference name(Offer Name), even if it's been deleted.so unique name append wit offer name (Apple Store)
		if(offerReferenceName!=null) {
            DiscountOfferMapping discountOffer = null;
		    if ( programId != 0){
                discountOffer=discountOfferMappingRepository.findByProgramsProgramIdAndOfferCodeDetailIsInUseAndOfferCodeDetailOfferNameAndOfferCodeDetailOwner(programId, true, offerReferenceName,user);
            }
			if(discountOffer != null ) {
				throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_OFFER_NAME_ALREADY_USED, null);
			}
		}
		return true;
	}

    public String validateOfferCodeWithValidity(String offerCode, String offerName) {
	    log.info("Validate offer code to generate new one starts");
        Boolean isValid = false;
        DecimalFormat decimalFormat= new DecimalFormat("00");
        Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int monthValue = localDate.getMonthValue();
        long profilingStart = new Date().getTime();
        OfferCode offerCode1 = offerCodeRepository.findByOfferCodeName(offerCode);
        if(offerCode1 == null || (offerCode1 != null && offerCode1.getStatus().equalsIgnoreCase(DiscountsConstants.CODE_UNUSED))){
            isValid = true;
        }
        log.info("Query and validity check : Time taken in millis : "+(new Date().getTime()-profilingStart));

        profilingStart = new Date().getTime();
        while (!isValid) {
        	//
        	monthValue++;
        	String refName="";
            if (offerName.length() >= 3) {
            	refName=eliminateSpecialchar(offerName.replaceAll(" ", "").substring(0, 3).toUpperCase());
                offerCode = refName + decimalFormat.format(Double.valueOf(localDate.getDayOfMonth())) + decimalFormat.format(monthValue);
            } else if (offerName.length() == 2) {
            	refName=eliminateSpecialchar(offerName.replaceAll(" ", "").toUpperCase());
                offerCode = refName + "0" + decimalFormat.format(Double.valueOf(localDate.getDayOfMonth())) + decimalFormat.format(monthValue);
            }

            OfferCode code = offerCodeRepository.findByOfferCodeName(offerCode);
            if(code == null || (code != null && code.getStatus().equalsIgnoreCase(DiscountsConstants.CODE_UNUSED))){
                isValid = true;
            }
        }
        log.info("Iterations for generating valid offer code : Time taken in millis : "+(new Date().getTime()-profilingStart));
        log.info("Validate offer code to generate new one ends");
        return offerCode;
    }


    /**
     * Getting price breakdown by platform for offer price
      * @param offerId
     * @return
     */
    public  ResponseModel getOfferPriceBreakDown(Long offerId){
        List<ProgramPriceResponseModel> programOfferPriceResponseModels = new ArrayList<>();
        if(offerId == null){
            throw new ApplicationException(Constants.BAD_REQUEST,ValidationMessageConstants.MSG_OFFER_CODE_ID_INVALID,MessageConstants.ERROR);
        }
        OfferCodeDetail offerCodeDetail = offerCodeDetailRepository.findByOfferCodeId(offerId);
        if(offerCodeDetail == null){
            throw new ApplicationException(Constants.BAD_REQUEST,ValidationMessageConstants.MSG_OFFER_NOT_FOUND,MessageConstants.ERROR);
        }
        if(offerCodeDetail.getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_FREE)){
            throw new ApplicationException(Constants.BAD_REQUEST,MessageConstants.MSG_PRICE_BREAKDOWN_FOR_FREE_MODE,MessageConstants.ERROR);
        }else{
            Double price = offerCodeDetail.getOfferPrice().getPrice();
            programOfferPriceResponseModels = priceService.getProgramRevenueByPlatform(price);
        }
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, programOfferPriceResponseModels);
    }

    public ResponseModel removeOffersWithHigherPrice(Long programId,Double newProgramPrice){
        List<DiscountOfferMapping> discountOfferMappings = discountOfferMappingRepository.findByProgramsProgramIdAndOfferCodeDetailIsInUseAndOfferCodeDetailOfferStatus(programId, true,DiscountsConstants.OFFER_ACTIVE);
        if(!discountOfferMappings.isEmpty()){
            for(DiscountOfferMapping discountOfferMapping : discountOfferMappings){
                OfferCodeDetail offerCodeDetail = offerCodeDetailRepository.findByOfferCodeId(discountOfferMapping.getOfferCodeDetail().getOfferCodeId());
                if(offerCodeDetail.getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_PAY_AS_YOU_GO)){
                    if(offerCodeDetail.getOfferPrice().getPrice() >= newProgramPrice){
                        discountOfferMappingRepository.delete(discountOfferMapping);
                    }
                }
            }
        }
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_OFFERS_REMOVED, null);
    }

    public ResponseModel addOffersInProgram(OfferProgramRequestView offerProgramRequestView) throws NoSuchAlgorithmException, KeyStoreException, ParseException, KeyManagementException {
        log.info("Add offers in program starts");
        long start = new Date().getTime();
        long profilingStart;
        long profilingEnd;
        if(offerProgramRequestView.getProgramId() == null){
            throw new ApplicationException(Constants.BAD_REQUEST,ValidationMessageConstants.MSG_PROGRAM_ID_NULL,null);
        }
        Programs program = programRepository.findByProgramId(offerProgramRequestView.getProgramId());
        if(program == null){
            throw new ApplicationException(Constants.BAD_REQUEST,ValidationMessageConstants.MSG_PROGRAM_ID_INCORRECT,null);
        }
        profilingStart = new Date().getTime();
        if(!offerProgramRequestView.getDiscountOffersIds().isEmpty() || offerProgramRequestView.getDiscountOffersIds() != null){
            programService.doConstructDiscountsOffer(program,offerProgramRequestView.getDiscountOffersIds());
        }
        profilingEnd = new Date().getTime();
        log.info("Construct discount offers : Time taken in millis : "+(profilingEnd-profilingStart));

        program = programRepository.save(program);
        // Add offer's in Existing Program; iTMS upload is required. Stopping ITMS upload due to IAP remove in iOS app.
		/*if (program.getStatus() != null && program.getStatus().equalsIgnoreCase(InstructorConstant.PUBLISH)) {
			fiEntityService.publishOrRepublish(program);
		}*/
        ProgramResponseModel programResponseModel = programService.constructProgramModel(program);
		profilingEnd = new Date().getTime();
		log.info("Add offers in program : Total time taken in millis : "+(profilingEnd-start));
		log.info("Add offers in program ends");
        return new ResponseModel(Constants.SUCCESS_STATUS,MessageConstants.MSG_OFFERS_ADDED,programResponseModel);
    }

	public OfferCodesListView getProgramOffers(Long programId) {
        User currentUser = userComponents.getUser();
		
        Programs program = validationService.validateProgramId(programId);
        OfferCodesListView allOffersList = new OfferCodesListView();

        boolean isMember = KeyConstants.KEY_MEMBER.equalsIgnoreCase(userComponents.getRole()) ? true : false;
        boolean isNewSubscription = true;
        if (isMember) {
            ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(currentUser.getUserId(), program.getProgramId());
            if (programSubscription != null) {
                SubscriptionStatus subscriptionStatus = subscriptionService.getMemberProgramSubscriptionStatus(programSubscription);
                if (subscriptionStatus != null) {
                    String StatusName = subscriptionStatus.getSubscriptionStatusName();
                    if (StatusName.equals(KeyConstants.KEY_PAID) || StatusName.equals(KeyConstants.KEY_PAYMENT_PENDING) || StatusName.equals(KeyConstants.KEY_EXPIRED)) {
                        isNewSubscription = false;
                    }
                }
            }
        }
        
        List<SaveOfferCodeResponseView> currentOffers = new ArrayList<>();
        List<SaveOfferCodeResponseView> freeOffers = new ArrayList<>();
        List<SaveOfferCodeResponseView> paidOffers = new ArrayList<>();
        	List<DiscountOfferMapping> currentOffersList=discountOfferMappingRepository.findByProgramsProgramIdAndOfferCodeDetailIsInUseAndOfferCodeDetailOfferStatus(programId, true, DiscountsConstants.OFFER_ACTIVE);
          	if (currentOffersList.isEmpty()) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_OFFER_CODE_DETAILS_NOT_FOUND, null);
            }

            if (!currentOffersList.isEmpty()) {
                for (DiscountOfferMapping discounts : currentOffersList) {
                    if (!isMember || (isMember && isNewSubscription == discounts.getOfferCodeDetail().getIsNewUser().booleanValue())) {
                        String formattedPrice = null;
                        if(discounts.getOfferCodeDetail().getOfferMode().equals(DiscountsConstants.MODE_PAY_AS_YOU_GO))
                        {
                            formattedPrice = fitwiseUtils.formatPrice(discounts.getOfferCodeDetail().getOfferPrice().getPrice());
                        }
                        else {
                            formattedPrice = KeyConstants.KEY_CURRENCY_US_DOLLAR+KeyConstants.KEY_DEFAULT_PRICE_FORMAT;
                        }
                        SaveOfferCodeResponseView sResponseView = new SaveOfferCodeResponseView();
                        sResponseView.setOfferCodeId(discounts.getOfferCodeDetail().getOfferCodeId());
                        sResponseView.setOfferName(discounts.getOfferCodeDetail().getOfferName().trim());
                        sResponseView.setOfferCode(discounts.getOfferCodeDetail().getOfferCode().toUpperCase());
                        sResponseView.setOfferMode(discounts.getOfferCodeDetail().getOfferMode());
                        sResponseView.setOfferDuration(discounts.getOfferCodeDetail().getOfferDuration());
                        sResponseView.setOfferStartDate(fitwiseUtils.formatDate(discounts.getOfferCodeDetail().getOfferStartingDate()));
                        sResponseView.setOfferEndDate(fitwiseUtils.formatDate(discounts.getOfferCodeDetail().getOfferEndingDate()));
                        sResponseView.setOfferPrice(discounts.getOfferCodeDetail().getOfferPrice());
                        sResponseView.setFormattedOfferPrice(formattedPrice);
                        sResponseView.setOfferStatus(discounts.getOfferCodeDetail().getOfferStatus());
                        //sResponseView.setOfferDurationCount(discounts.getOfferCodeDetail().getOfferDurationCount());
                        sResponseView.setIsNewUser(discounts.getOfferCodeDetail().getIsNewUser());

                        //Savings amount added
                        if (program.getProgramPrices() != null) {
                            double savingsAmount = program.getProgramPrices().getPrice();
                            if (discounts.getOfferCodeDetail().getOfferPrice() != null) {
                                savingsAmount = program.getProgramPrices().getPrice() - discounts.getOfferCodeDetail().getOfferPrice().getPrice();
                            }
                            DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
                            sResponseView.setFormattedSavingsAmount(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(savingsAmount));
                        }

                        // Offer Validity calculation
                        Date now = new Date();
                        Date offerStart = discounts.getOfferCodeDetail().getOfferStartingDate();
                        Date offerEnd = discounts.getOfferCodeDetail().getOfferEndingDate();
                        // Current Offers
					
					if ((offerStart.equals(now) || offerStart.before(now))
							&& (offerEnd.equals(now) || offerEnd.after(now))) {
						if (discounts.getOfferCodeDetail().getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_FREE)) {
							freeOffers.add(sResponseView);
						} else if (discounts.getOfferCodeDetail().getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_PAY_AS_YOU_GO)) {
							paidOffers.add(sResponseView);
						}
						// currentOffers.add(sResponseView);
					}
                    /*
                    // UpComing Offers
                    else if (offerStart.after(date) && offerEnd.after(date)) {
                        upComingOffers.add(sResponseView);
                    }
                    // Expired Offers
                    else if (offerStart.before(date) && offerEnd.before(date)) {
                        expiredOffers.add(sResponseView);
                    }
                    */
                    }
                }
                paidOffers.sort((SaveOfferCodeResponseView f1,SaveOfferCodeResponseView f2) -> f1.compareTo(f2)); 
                //Collections.sort(paidOffers, Collections.reverseOrder());
                freeOffers.sort((SaveOfferCodeResponseView f1,SaveOfferCodeResponseView f2) -> f1.compareTo(f2));  
                Collections.sort(freeOffers, Collections.reverseOrder());
                freeOffers.stream().collect(Collectors.toCollection(()->currentOffers));
                paidOffers.stream().collect(Collectors.toCollection(()->currentOffers));
                allOffersList.setCurrentOffers(currentOffers);
            }

        return allOffersList;
   
	}


    /**
     * Get No of current available offers for list of instructor package ids
     * @param subscriptionPackageIds
     * @return
     */
    public Map<Long, Long> getNoOfCurrentAvailableOffersForInstructorPackages(List<Long> subscriptionPackageIds){
        List<OfferCountDao> offerCountDaos = subscriptionPackageJpa.getNumberOfCurrentOffersForInstructorPackages(subscriptionPackageIds);
        Map<Long, Long> offerCountDaoMap = new HashMap<>();

        if (offerCountDaos != null && !offerCountDaos.isEmpty()){
            offerCountDaoMap = offerCountDaos.stream()
                    .collect(Collectors.toMap(OfferCountDao::getId, OfferCountDao::getOfferCount));
        }
        return  offerCountDaoMap;
    }

    /**
     * Get No of current available offers for list of package ids
     * @param subscriptionPackageIds
     * @param member
     * @return
     */
    public Map<Long, Long> getNoOfCurrentAvailableOffersOfPackagesForMember(List<Long> subscriptionPackageIds, User member){
        Map<Long, Long> offerCountMap = new HashMap<>();
        if(!subscriptionPackageIds.isEmpty()){
            offerCountMap =  subscriptionPackageJpa.getNumberOfCurrentOffersForMemberPackages(subscriptionPackageIds, member);
        }
        return  offerCountMap;
    }

    /**
     * Get No of current available offers for list of program ids for member
     * @param programIds
     * @param user
     * @return
     */
    public Map<Long, Long> getNoOfCurrentAvailableOffersOfProgramsForMember(List<Long> programIds, User user) {
        Map<Long, Long> offerCountMap = new HashMap<>();
        if (!programIds.isEmpty()) {
            offerCountMap = programJpa.getNumberOfCurrentOffersForMemberPrograms(programIds, user);
        }
        return offerCountMap;
    }

    /**
     * Get No of current available offers for list of program ids for instructor
     * @param programIds
     * @return
     */
    public Map<Long, Long> getNoOfCurrentAvailableOffersOfProgramsForInstructor(List<Long> programIds) {
        Map<Long, Long> offerCountMap = new HashMap<>();
        if (!programIds.isEmpty()) {
            offerCountMap = programJpa.getNumberOfCurrentAvailableOffersOfProgramsForInstructor(programIds);
        }
        return offerCountMap;
    }


    /**
     * generate offer code for package API
     * @param offerName, packageId
     * @return
     */
    public ResponseModel generateOfferCodeForPackage(String offerName, Long packageId) {
        log.info("Generate offer code for package starts.");
        long apiStartTimeMillis = new Date().getTime();
        OfferCodeResponseView offerCodeResponseView = new OfferCodeResponseView();
        if (offerName == null || offerName.isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_OFFER_NAME_NULL, MessageConstants.ERROR);
        }

        //Validating the user input
        if (packageId == null || packageId == 0){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_ID_NULL, MessageConstants.ERROR);
        }
        log.info("Basic validations : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();

        validateOfferNameForPackakge(offerName, packageId);
        Boolean isValid = false;
        Date date = new Date();
        DecimalFormat decimalFormat= new DecimalFormat("00");
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String offerCode = null;
        String newOfferName = offerName.replaceAll(" ","");
        String refName="";
        if (newOfferName.length() >= 3) {
            refName=eliminateSpecialchar(offerName.replaceAll(" ", "").substring(0, 3).toUpperCase());
            offerCode = refName + decimalFormat.format(Double.valueOf(localDate.getDayOfMonth())) + decimalFormat.format(Double.valueOf(localDate.getMonthValue()));
        } else if (newOfferName.length() == 2) {
            refName=eliminateSpecialchar(offerName.replaceAll(" ", "").toUpperCase());
            offerCode = refName + "0" + decimalFormat.format(Double.valueOf(localDate.getDayOfMonth())) + decimalFormat.format(Double.valueOf(localDate.getMonthValue()));
        }
        OfferCode offerCode1 = offerCodeRepository.findByOfferCodeName(offerCode);
        log.info("Query to get offer code : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if(offerCode1 == null || (offerCode1 != null && offerCode1.getStatus().equalsIgnoreCase(DiscountsConstants.CODE_UNUSED))){
            isValid = true;
        }
        if(!isValid){
            offerCode = validateOfferCodeWithValidity(offerCode, newOfferName);
        }

        OfferCode ofCode = offerCodeRepository.findByOfferCodeName(offerCode);
        if(ofCode == null){
            ofCode = new OfferCode();
            ofCode.setOfferCodeName(offerCode);
            ofCode.setStatus(DiscountsConstants.CODE_UNUSED);
            ofCode = offerCodeRepository.save(ofCode);
        }
        log.info("Query to get and save offer code : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));

        offerCodeResponseView.setId(ofCode.getId());
        offerCodeResponseView.setOfferCodeName(ofCode.getOfferCodeName());
        offerCodeResponseView.setStatus(ofCode.getStatus());
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Generate offer code for package ends.");

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PROMO_CODE_GENERATED, offerCodeResponseView);
    }

    /**
     * validate offer name for package API
     * @param offerReferenceName, packageId
     * @return
     */
    public Boolean validateOfferNameForPackakge(String offerReferenceName, Long packageId) {
        User user = userComponents.getUser();
        //Validating the user input
        if ((packageId == null || packageId ==0)){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_ID_NULL, MessageConstants.ERROR);
        }
        if (offerReferenceName != null
                && (offerReferenceName.trim().length() < 2 || offerReferenceName.trim().length() > 64)) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_OFFER_NAME_LENGTH_INCORRECT,
                    null);
        }
        // Note :The reference name (Offer Name) must be unique for Instructor/Program (Fitwise DB);
        // can’t re-use a reference name(Offer Name), even if it's been deleted.so unique name append wit offer name (Apple Store)
        if(offerReferenceName!=null) {
            PackageOfferMapping packageOfferMapping = null;
            if (packageId != 0){
                packageOfferMapping = packageOfferMappingRepository.findBySubscriptionPackageSubscriptionPackageIdAndOfferCodeDetailIsInUseAndOfferCodeDetailOfferNameAndOfferCodeDetailOwner(packageId, true, offerReferenceName, user);
            }
            if(packageOfferMapping != null) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_OFFER_NAME_ALREADY_USED, null);
            }
        }
        return true;
    }

}