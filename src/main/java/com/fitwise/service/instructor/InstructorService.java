package com.fitwise.service.instructor;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.User;
import com.fitwise.entity.UserCommunicationDetail;
import com.fitwise.entity.UserRole;
import com.fitwise.entity.instructor.InstructorTierDetails;
import com.fitwise.entity.instructor.Location;
import com.fitwise.entity.instructor.LocationType;
import com.fitwise.entity.instructor.Tier;
import com.fitwise.entity.instructor.TierTypeDetails;
import com.fitwise.entity.packaging.PackageKloudlessMapping;
import com.fitwise.entity.payments.authNet.Countries;
import com.fitwise.entity.subscription.PackageSubscription;
import com.fitwise.entity.subscription.ProgramSubscription;
import com.fitwise.entity.subscription.TierSubscription;
import com.fitwise.exception.ApplicationException;
import com.fitwise.model.LocationModel;
import com.fitwise.model.TierModel;
import com.fitwise.model.instructor.ZoomCredentialModel;
import com.fitwise.repository.UserCommunicationDetailRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.repository.UserRoleRepository;
import com.fitwise.repository.instructor.InstructorTierDetailsRepository;
import com.fitwise.repository.instructor.LocationRepository;
import com.fitwise.repository.instructor.LocationTypeRepository;
import com.fitwise.repository.instructor.TierRepository;
import com.fitwise.repository.packaging.PackageKloudlessMappingRepository;
import com.fitwise.repository.payments.authnet.CountriesRepository;
import com.fitwise.repository.subscription.PackageSubscriptionRepository;
import com.fitwise.repository.subscription.ProgramSubscriptionRepo;
import com.fitwise.repository.subscription.TierSubscriptionRepository;
import com.fitwise.response.ExternalClientResponseView;
import com.fitwise.response.LocationResponse;
import com.fitwise.response.LocationTypeView;
import com.fitwise.service.payment.stripe.StripeTierService;
import com.fitwise.specifications.UserSpecifications;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.instructor.ExistingAndRequestedTierResponseView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/*
 * Created by Vignesh G on 28/09/20
 */
@Service
@Slf4j
public class InstructorService {

    @Autowired
    private UserComponents userComponents;
    @Autowired
    private FitwiseUtils fitwiseUtils;
    @Autowired
    private UserCommunicationDetailRepository userCommunicationDetailRepository;
    @Autowired
    private BCryptPasswordEncoder bcryptPasswdEncoder;

    @Autowired
    private ProgramSubscriptionRepo programSubscriptionRepo;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PackageSubscriptionRepository packageSubscriptionRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private LocationTypeRepository locationTypeRepository;

    @Autowired
    private CountriesRepository countriesRepository;

    @Autowired
    private PackageKloudlessMappingRepository packageKloudlessMappingRepository;
    
    @Autowired
    private TierRepository tierRepository;

    @Autowired
    private StripeTierService stripeTierService;
    
    /**
     * The instructor tier details repository.
     */
    @Autowired
    InstructorTierDetailsRepository instructorTierDetailsRepository;
    
    /**
     * The Tier Subscription Repository
     */
    @Autowired
    TierSubscriptionRepository tierSubscriptionRepository;


    /**
     * Method to update zoom link
     * @param zoomCredentialModel zoom credential
     */
    public void updateZoomCredentials(ZoomCredentialModel zoomCredentialModel) {
        User currentUser = userComponents.getUser();
        if (!fitwiseUtils.isInstructor(currentUser)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_INSTRUCTOR, MessageConstants.ERROR);
        }

        if (zoomCredentialModel == null || ValidationUtils.isEmptyString(zoomCredentialModel.getZoomId()) || ValidationUtils.isEmptyString(zoomCredentialModel.getZoomPassword())) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ZOOM_ID_SECRET_EMPTY, MessageConstants.ERROR);
        }

        UserCommunicationDetail userCommunicationDetail = userCommunicationDetailRepository.findByUser(userComponents.getUser());
        if (userCommunicationDetail == null) {
            userCommunicationDetail = new UserCommunicationDetail();
            userCommunicationDetail.setUser(currentUser);
        }

        userCommunicationDetail.setZoomId(zoomCredentialModel.getZoomId());
        userCommunicationDetail.setZoomPassword(bcryptPasswdEncoder.encode(zoomCredentialModel.getZoomPassword()));
        userCommunicationDetailRepository.save(userCommunicationDetail);
    }

    public ResponseModel getExternalClients(Optional<String> search){
        User user = userComponents.getUser();
        List<String> statusList = Arrays.asList(KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING,KeyConstants.KEY_TRIAL);
        List<String> statusListForPackage = Arrays.asList(KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING);
        List<ProgramSubscription> programSubscriptionList = programSubscriptionRepo.findByProgramOwnerUserIdAndSubscriptionStatusSubscriptionStatusNameIn(user.getUserId(),statusList);
        List<PackageSubscription> packageSubscriptionList = packageSubscriptionRepository.findBySubscriptionPackageOwnerUserIdAndSubscriptionStatusSubscriptionStatusNameIn(user.getUserId(),statusListForPackage);
        List<Long> clientIds = programSubscriptionList.stream().map(programSubscription -> programSubscription.getUser().getUserId()).collect(Collectors.toList());
        clientIds.addAll(packageSubscriptionList.stream().map(packageSubscription -> packageSubscription.getUser().getUserId()).collect(Collectors.toList()));
        UserRole userRole = userRoleRepository.findByName(KeyConstants.KEY_MEMBER);
        List<User> userList;
        Specification<User> finalSpec;
        Specification<User> roleSpec = UserSpecifications.getUserByRoleId(userRole.getRoleId());
        Specification<User> externalClientSpec = UserSpecifications.getUsersNotInIdList(clientIds);
        if(search.isPresent() && !search.get().isEmpty()){
            Specification<User> emailSearchSpec = UserSpecifications.getUserByEmailContains(search.get());
            if(clientIds.isEmpty()){
                finalSpec = emailSearchSpec.and(roleSpec);
            }else{
                finalSpec = emailSearchSpec.and(externalClientSpec).and(roleSpec);
            }
        }else{
            if(clientIds.isEmpty()){
                finalSpec = roleSpec;
            }else{
                finalSpec = externalClientSpec.and(roleSpec);
            }
        }
        userList = userRepository.findAll(finalSpec);
        List<ExternalClientResponseView> externalClientResponseViews  = new ArrayList<>();

        for(User externalClient : userList){
            ExternalClientResponseView externalClientResponseView = new ExternalClientResponseView();
            externalClientResponseView.setUserId(externalClient.getUserId());
            externalClientResponseView.setEmail(externalClient.getEmail());
            externalClientResponseViews.add(externalClientResponseView);
        }

        if(externalClientResponseViews.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS,MessageConstants.MSG_DATA_NOT_AVAILABLE,null);
        }

        Map<String,Object> responseMap = new HashMap<>();
        responseMap.put(KeyConstants.KEY_USERS_LIST,externalClientResponseViews);
        responseMap.put(KeyConstants.KEY_MEMBER_COUNT,externalClientResponseViews.size());
        return  new ResponseModel(Constants.SUCCESS_STATUS,MessageConstants.MSG_DATA_RETRIEVED,responseMap);

    }

    /**
     * Save Location
     * @param locationModel
     * @return
     */
    public ResponseModel saveLocation(LocationModel locationModel){
        log.info("Save location starts.");
        long apiStartTimeMillis = new Date().getTime();

        User user = userComponents.getUser();

        LocationType locationType = locationTypeRepository.findByLocationTypeId(locationModel.getLocationTypeId());
        if(locationType == null){
            throw new ApplicationException(Constants.BAD_REQUEST,ValidationMessageConstants.MSG_LOCATION_TYPE_NOT_FOUND,MessageConstants.ERROR);
        }
        ValidationUtils.throwException(locationModel.getAddress().length() > ValidationConstants.ADDRESS_MAX_LENGTH,"Address should not be more than 60 characters",Constants.BAD_REQUEST);
        if(locationModel.getLandmark() != null){
            ValidationUtils.throwException(locationModel.getLandmark().length() > ValidationConstants.LANDMARK_MAX_LENGTH,"Landmark should not be more than 30 characters",Constants.BAD_REQUEST);
        }
        ValidationUtils.throwException(locationModel.getCity().length() > ValidationConstants.CITY_MAX_LENGTH,"City should not be more than 60 characters",Constants.BAD_REQUEST);
        ValidationUtils.throwException(locationModel.getState().length() > ValidationConstants.STATE_MAX_LENGTH,"State should not be more than 60 characters",Constants.BAD_REQUEST);
        log.info("Basic validations : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        Location location;
        if(locationModel.getLocationId() == 0 || locationModel.getLocationId() == null){
            location = new Location();
        }else{
            location = locationRepository.findByLocationId(locationModel.getLocationId());
            if(location == null){
                throw new ApplicationException(Constants.BAD_REQUEST,ValidationMessageConstants.MSG_LOCATION_NOT_FOUND,MessageConstants.ERROR);
            }
            boolean isFieldChange= (locationModel.getLocationTypeId() != null && location.getLocationType().getLocationTypeId() != null
                    && !locationModel.getLocationTypeId().equals(location.getLocationType().getLocationTypeId()))
                    || (locationModel.getAddress() != null && location.getAddress() != null
                    && !locationModel.getAddress().equalsIgnoreCase(location.getAddress()))
                    || (locationModel.getLandmark() != null && location.getLandMark() != null
                    && !locationModel.getLandmark().equalsIgnoreCase(location.getLandMark()))
                    || (locationModel.getCity() != null && location.getCity() != null
                    && !locationModel.getCity().equalsIgnoreCase(location.getCity()))
                    || (locationModel.getState() != null && location.getState() != null
                    && !locationModel.getState().equalsIgnoreCase(location.getState()))
                    || (locationModel.getZipcode() != null && location.getZipcode() != null
                    && !locationModel.getZipcode().equalsIgnoreCase(location.getZipcode()))
                    || (locationModel.getCountryId() != null && location.getCountry().getId() != null
                    && !locationModel.getCountryId().equals(location.getCountry().getId()));
            //If it's not used in any Package we can allow any fields to edit
			List<PackageKloudlessMapping> packageKloudlessMappings = packageKloudlessMappingRepository.findByLocation(location);
            log.info("Query to get location and package kloudless mapping : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
			if (isFieldChange) {
				if (!packageKloudlessMappings.isEmpty()) {
					throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_LOCATION_USED_IN_PACKAGE,MessageConstants.ERROR);
				}
			}
            if(location.isDefault()){
                throw new ApplicationException(Constants.BAD_REQUEST,MessageConstants.MSG_DEFAULT_LOCATION_CANNOT_BE_DELETED,MessageConstants.ERROR);
            }
        }
        profilingEndTimeMillis = new Date().getTime();
        location.setLocationType(locationType);
        location.setUser(user);
        location.setAddress(locationModel.getAddress());
        location.setLandMark(locationModel.getLandmark());
        location.setState(locationModel.getState());
        location.setCity(locationModel.getCity());
        location.setZipcode(locationModel.getZipcode());
        Optional<Countries> countries = countriesRepository.findById(locationModel.getCountryId());
        log.info("Query to get countries and construct location : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if(!countries.isPresent()){
            throw new ApplicationException(Constants.BAD_REQUEST,ValidationMessageConstants.MSG_COUNTRY_NOT_FOUND,MessageConstants.ERROR);
        } else {
            countries.get();
        }
        location.setCountry(countries.get());
        if(locationModel.isDefault()){
            Location currentDefaultLocation = locationRepository.findByUserUserIdAndIsDefault(user.getUserId(),true);
            if(currentDefaultLocation != null){
                currentDefaultLocation.setDefault(false);
                locationRepository.save(currentDefaultLocation);
            }

        }
        log.info("Query to set false to active location : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        location.setDefault(locationModel.isDefault());
        locationRepository.save(location);
        log.info("Query to save new location as default : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        LocationResponse locationResponse = constructLocationResponse(location);
        log.info("Response construction : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Save location ends.");
        return new ResponseModel(Constants.SUCCESS_STATUS,MessageConstants.MSG_LOCATION_SAVED,locationResponse);
    }

    /**
     * Delete Location
     * @param locationId
     * @return
     */
    public ResponseModel deleteLocation(Long locationId){
        User user = userComponents.getUser();
        if(locationId == null || locationId == 0){
            throw new ApplicationException(Constants.BAD_REQUEST,ValidationMessageConstants.MSG_LOCATION_ID_INCORRECT,MessageConstants.ERROR);
        }
        Location location = locationRepository.findByUserUserIdAndLocationId(user.getUserId(),locationId);
        if(location == null){
            throw new ApplicationException(Constants.BAD_REQUEST,ValidationMessageConstants.MSG_LOCATION_NOT_FOUND,MessageConstants.ERROR);
        }
        List<PackageKloudlessMapping> packageKloudlessMappings = packageKloudlessMappingRepository.findByLocation(location);
        if(!packageKloudlessMappings.isEmpty()){
            throw new ApplicationException(Constants.BAD_REQUEST,MessageConstants.MSG_LOCATION_USED_IN_PACKAGE,MessageConstants.ERROR);
        }
        if(location.isDefault()){
            throw new ApplicationException(Constants.BAD_REQUEST,MessageConstants.MSG_DEFAULT_LOCATION_CANNOT_BE_DELETED,MessageConstants.ERROR);
        }
        locationRepository.delete(location);
        return new ResponseModel(Constants.SUCCESS_STATUS,MessageConstants.MSG_LOCATION_DELETED,null);
    }

    /**
     * Get All saved locations of an instructor
     * @return
     */
    public ResponseModel getLocationsOfAnUser(){
        log.info("Get Locations starts");
        long start = new Date().getTime();
        long profilingStart;
        User user = userComponents.getUser();
        log.info("Validations : Time taken in millis : "+(new Date().getTime() - start));
        profilingStart = new Date().getTime();
        List<Location> locations = locationRepository.findByUserUserIdOrderByCreatedDateDesc(user.getUserId());
        log.info("Query : Time taken in millis : "+(new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();
        if(locations.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS,MessageConstants.MSG_DATA_NOT_AVAILABLE,null);
        }
        locations.sort(Comparator.comparing(Location::isDefault).reversed());
        log.info("Sorting : Time taken in millis : "+(new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();
        List<LocationResponse> locationResponses = new ArrayList<>();
        for(Location location : locations){
            LocationResponse locationResponse = constructLocationResponse(location);
            locationResponses.add(locationResponse);
        }
        log.info("Response construction : Time taken in millis : "+(new Date().getTime() - profilingStart));


        Map<String,Object> responseMap = new HashMap<>();
        responseMap.put(KeyConstants.KEY_TOTAL_COUNT,locationResponses.size());
        responseMap.put(KeyConstants.KEY_LOCATIONS,locationResponses);
        log.info("Get locations : Total Time taken in millis : "+(new Date().getTime() - start));
        log.info("Get Locations ends");
        return new ResponseModel(Constants.SUCCESS_STATUS,MessageConstants.MSG_DATA_RETRIEVED,responseMap);
    }

    public LocationResponse constructLocationResponse(Location location){
        LocationResponse locationResponse = new LocationResponse();
        locationResponse.setLocationId(location.getLocationId());
        locationResponse.setLocationTypeId(location.getLocationType().getLocationTypeId());
        locationResponse.setLocationType(location.getLocationType().getLocationType());
        locationResponse.setAddress(location.getAddress());
        if(location.getLandMark() != null){
            locationResponse.setLandmark(location.getLandMark());
        }else{
            locationResponse.setLandmark("");
        }
        locationResponse.setCity(location.getCity());
        locationResponse.setState(location.getState());
        locationResponse.setZipcode(location.getZipcode());
        locationResponse.setCountryId(location.getCountry().getId());
        locationResponse.setCountry(location.getCountry().getCountryName());
        locationResponse.setDefault(location.isDefault());
        List<PackageKloudlessMapping> packageKloudlessMappings = packageKloudlessMappingRepository.findByLocation(location);
        if(!packageKloudlessMappings.isEmpty()){
            locationResponse.setUsedInPackage(true);
        }
        return locationResponse;
    }

    public ResponseModel getLocationTypes(){
        List<LocationType> locationTypes = locationTypeRepository.findAll();
        if(locationTypes.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS,MessageConstants.MSG_DATA_NOT_AVAILABLE,null);
        }
        List<LocationTypeView> locationTypeViews = new ArrayList<>();
        for(LocationType locationType : locationTypes){
            LocationTypeView locationTypeView = new LocationTypeView();
            locationTypeView.setLocationTypeId(locationType.getLocationTypeId());
            locationTypeView.setLocationType(locationType.getLocationType());
            locationTypeViews.add(locationTypeView);
        }
        Map<String,Object>  response = new HashMap<>();
        response.put(KeyConstants.KEY_LOCATION_TYPES,locationTypeViews);
        return new ResponseModel(Constants.SUCCESS_STATUS,MessageConstants.MSG_DATA_RETRIEVED,response);
    }
    
    public ResponseModel getTierTypes(){
        List<Tier> tierTypes = tierRepository.findByIsActiveTrue();
        if(tierTypes.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS,MessageConstants.MSG_DATA_NOT_AVAILABLE,null);
        }
        List<TierModel> tierModels = new ArrayList<>();
        for (Tier tier : tierTypes) {
			TierModel tierModel = new TierModel();
			tierModel.setTierId(tier.getTierId());
			tierModel.setTierType(tier.getTierType());
			tierModel.setIsActive(tier.getIsActive());
			tierModel.setTierTypeId(tier.getTierTypeDetails().getTierTypeId());
			tierModel.setMonthlyCost(tier.getTierTypeDetails().getMonthlyCost());
			tierModel.setChargeFrequency(tier.getTierTypeDetails().getChargeFrequency());
			tierModel.setMinimumCommitment(tier.getTierTypeDetails().getMinimumCommitment());
			tierModel.setCancellationPolicy(tier.getTierTypeDetails().getCancellationPolicy());
			tierModel.setProgramsFees(tier.getTierTypeDetails().getProgramsFees());
			tierModel.setProgramsPackagesFees(tier.getTierTypeDetails().getProgramsPackagesFees());
			tierModel.setServicesPackagesFees(tier.getTierTypeDetails().getServicesPackagesFees());
			tierModel.setPackagesFees(tier.getTierTypeDetails().getPackagesFees());
			tierModel.setDirectoryLeads(tier.getTierTypeDetails().getDirectoryLeads());
			tierModel.setCommunityNewsletter(tier.getTierTypeDetails().getCommunityNewsletter());
			tierModel.setLogoCreation(tier.getTierTypeDetails().getLogoCreation());
			tierModel.setAccountCreationHelp(tier.getTierTypeDetails().getAccountCreationHelp());
			tierModel.setDedicatedAccountManager(tier.getTierTypeDetails().getDedicatedAccountManager());
			tierModel.setAdCreationHelp(tier.getTierTypeDetails().getAdCreationHelp());
			tierModels.add(tierModel);
		}
        Map<String,Object>  response = new HashMap<>();
        response.put(KeyConstants.KEY_TIER_TYPES,tierModels);
        return new ResponseModel(Constants.SUCCESS_STATUS,MessageConstants.MSG_DATA_RETRIEVED,response);
    }
	
	public ResponseModel getUserTierDetails() {
		User user = userComponents.getUser();
		Map<String, Object> response = new HashMap<>();
		boolean isUpgrade = false;
		boolean isDowngrade = false;
		TierSubscription tierSubscription = tierSubscriptionRepository.findByUser(user);
		if (tierSubscription != null) {
			TierTypeDetails tierTypeDetails = tierSubscription.getTier().getTierTypeDetails();
            response.put("tierType", tierSubscription.getTier().getTierType());
            response.put("subscribedOn", tierSubscription.getSubscribedDate());
            response.put("expiryOn", null);
			if(!tierSubscription.getTier().getTierType().equalsIgnoreCase(DBConstants.TIER_FREE)){
                Calendar cal = Calendar.getInstance();
                cal.setTime(tierSubscription.getSubscribedDate());
                cal.add(Calendar.DAY_OF_YEAR, Math.toIntExact(tierTypeDetails.getDurationInDays()));
                Date subscriptionExpiryDate = cal.getTime();
                response.put("expiryOn", subscriptionExpiryDate.getTime());
			}
			response.put(KeyConstants.KEY_TIER_TYPE_DETAILS, tierTypeDetails);
			if (tierTypeDetails.getTierTypeId() == 1) {
				isUpgrade = true;
            } else if (tierTypeDetails.getTierTypeId() == 2) {
				isUpgrade = true;
				isDowngrade = true;
			} else if (tierTypeDetails.getTierTypeId() == 3) {
                isDowngrade = true;
			}
			response.put(KeyConstants.KEY_UPGRADE_TIER, isUpgrade);
			response.put(KeyConstants.KEY_DOWNGRADE_TIER, isDowngrade);
		} else {
			response.put(KeyConstants.KEY_TIER_TYPE_DETAILS, null);
		}
		return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, response);
	}

    /**
     * Get Existing and Requested Tier details
     * @param requestedTierId
     * @return
     */
    public ResponseModel getExistingAndRequestedTierDetails(Long requestedTierId){
        // Get active tiers list
        List<Tier> tiers = tierRepository.findByIsActiveTrue();
        if(tiers.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS,MessageConstants.MSG_DATA_NOT_AVAILABLE,null);
        }
        // Find the requested tier from the active tier list
        Tier tier = tiers.stream().filter( tier1 -> requestedTierId.equals(tier1.getTierId()) ).findAny().orElse(null);
        ExistingAndRequestedTierResponseView existingAndRequestedTierResponseView = new ExistingAndRequestedTierResponseView();

        // Get details if requested tier is present
        if(tier != null){
            // Find and set existing tier details
            ExistingAndRequestedTierResponseView.TierDetailsView existingTierDetails = new ExistingAndRequestedTierResponseView.TierDetailsView();
            User user = userComponents.getUser();
            InstructorTierDetails instructorTierDetails = instructorTierDetailsRepository.findByUser(user);
            long numberOfDaysUtilized = 0L;
            double balance = 0.0d;
            double subscriptionAmount = tier.getTierTypeDetails().getMinimumCommitment();
            if (instructorTierDetails != null && instructorTierDetails.getTier().getTierType().equalsIgnoreCase(DBConstants.TIER_FREE)) {
                existingTierDetails.setTierType(instructorTierDetails.getTier().getTierType());
                existingTierDetails.setPrice(instructorTierDetails.getTier().getTierTypeDetails().getMinimumCommitment());
            } else if (instructorTierDetails != null && !instructorTierDetails.getTier().getTierType().equalsIgnoreCase(DBConstants.TIER_FREE)) {
                TierSubscription tierSubscription = tierSubscriptionRepository.findTop1ByUserUserIdOrderBySubscribedDateDesc(user.getUserId());
                balance = stripeTierService.getBalanceAmountOnTierChange(tierSubscription, tier);
                // Set subscription amount if it positive
                if((tier.getTierTypeDetails().getMinimumCommitment() - balance) >= 0 ){
                    subscriptionAmount = tier.getTierTypeDetails().getMinimumCommitment() - balance;
                }
                Date startDate = tierSubscription.getSubscribedDate();
                Date endDate = new Date();
                numberOfDaysUtilized = fitwiseUtils.getNumberOfDaysBetweenTwoDates(startDate, endDate);
                existingTierDetails.setTierType(instructorTierDetails.getTier().getTierType());
                existingTierDetails.setPrice(instructorTierDetails.getTier().getTierTypeDetails().getMinimumCommitment());
            } else {
                existingTierDetails.setTierType(null);
                existingTierDetails.setPrice(null);
            }
            existingAndRequestedTierResponseView.setNumberOfDaysUtilized(numberOfDaysUtilized);
            existingAndRequestedTierResponseView.setSubscriptionAmount(subscriptionAmount);
            existingAndRequestedTierResponseView.setBalanceAmount(balance);
            existingAndRequestedTierResponseView.setExistingTierDetails(existingTierDetails);
            // Set the requested tier details
            ExistingAndRequestedTierResponseView.TierDetailsView requestedTierDetails = new ExistingAndRequestedTierResponseView.TierDetailsView();
            requestedTierDetails.setTierType(tier.getTierType());
            requestedTierDetails.setPrice(tier.getTierTypeDetails().getMinimumCommitment());
            existingAndRequestedTierResponseView.setRequestedTierDetails(requestedTierDetails);
        } else {
            throw new ApplicationException(Constants.NOT_FOUND,MessageConstants.MSG_DATA_NOT_AVAILABLE,null);
        }

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, existingAndRequestedTierResponseView);
    }

}
