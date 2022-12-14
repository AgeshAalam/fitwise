package com.fitwise.service.member;


import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.fitwise.constants.*;
import com.fitwise.entity.*;
import com.fitwise.repository.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.discounts.DiscountsConstants;
import com.fitwise.entity.calendar.CalendarMeetingType;
import com.fitwise.entity.calendar.CronofyAvailabilityRules;
import com.fitwise.entity.calendar.UserKloudlessAccount;
import com.fitwise.entity.calendar.UserKloudlessCalendar;
import com.fitwise.entity.calendar.UserKloudlessSchedule;
import com.fitwise.entity.instructor.Location;
import com.fitwise.entity.packaging.PackageExternalClientMapping;
import com.fitwise.entity.packaging.PackageKloudlessMapping;
import com.fitwise.entity.packaging.PackageMemberMapping;
import com.fitwise.entity.packaging.PackageOfferMapping;
import com.fitwise.entity.packaging.PackageProgramMapping;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.payments.stripe.StripeSubscriptionAndUserPackageMapping;
import com.fitwise.entity.payments.stripe.billing.StripePayment;
import com.fitwise.entity.product.FreeAccessPackages;
import com.fitwise.entity.subscription.PackageSubscription;
import com.fitwise.entity.subscription.SubscriptionStatus;
import com.fitwise.exception.ApplicationException;
import com.fitwise.model.member.PackageFilterModel;
import com.fitwise.model.packaging.CancellationDurationModel;
import com.fitwise.repository.calendar.CalendarMeetingTypeRepository;
import com.fitwise.repository.calendar.CronofyAvailabilityRulesRepository;
import com.fitwise.repository.calendar.UserKloudlessScheduleRepository;
import com.fitwise.repository.order.OrderManagementRepository;
import com.fitwise.repository.packaging.PackageExternalClientMappingRepository;
import com.fitwise.repository.packaging.PackageKloudlessMappingRepository;
import com.fitwise.repository.packaging.PackageMemberMappingRepository;
import com.fitwise.repository.packaging.PackageOfferMappingRepository;
import com.fitwise.repository.packaging.PackageProgramMappingRepository;
import com.fitwise.repository.packaging.SubscriptionPackageRepository;
import com.fitwise.repository.payments.stripe.StripeSubscriptionAndUserPackageMappingRepository;
import com.fitwise.repository.payments.stripe.billing.StripePaymentRepository;
import com.fitwise.repository.subscription.PackageSubscriptionRepository;
import com.fitwise.response.MemberLocationResponse;
import com.fitwise.response.packaging.MemberPackageScheduleDayView;
import com.fitwise.response.packaging.MemberPackageSessionView;
import com.fitwise.response.packaging.MemberPackageTileListView;
import com.fitwise.response.packaging.MemberPackageTileView;
import com.fitwise.response.packaging.PackageFilterView;
import com.fitwise.response.packaging.ProgramTileForPackageView;
import com.fitwise.response.packaging.SessionMemberView;
import com.fitwise.response.packaging.SessionScheduleMemberView;
import com.fitwise.response.packaging.SubscriptionPackageFilterModel;
import com.fitwise.response.packaging.SubscriptionPackageMemberView;
import com.fitwise.response.packaging.SubscriptionPackageResponseViewForDiscover;
import com.fitwise.response.packaging.SubscriptionPackageTileViewForDiscover;
import com.fitwise.service.InstructorUnavailabilityService;
import com.fitwise.service.SubscriptionService;
import com.fitwise.service.calendar.CalendarService;
import com.fitwise.service.calendar.KloudLessService;
import com.fitwise.service.cronofy.CronofyService;
import com.fitwise.service.discountservice.DiscountsService;
import com.fitwise.service.freeproduct.FreeAccessService;
import com.fitwise.service.v2.member.MemberPackageV2Service;
import com.fitwise.specifications.PackageSpecification;
import com.fitwise.specifications.PackageSubscriptionSpcifications;
import com.fitwise.specifications.jpa.SubscriptionPackageJpa;
import com.fitwise.specifications.jpa.dao.PackageDao;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.RequestParamValidator;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.utils.parsing.ProgramDataParsing;
import com.fitwise.view.InstructorUnavailabilityMemberView;
import com.fitwise.view.MemberCalendarFilterView;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.cronofy.CronofyMeetingModel;
import com.fitwise.view.cronofy.CronofyschedulePayload;
import com.fitwise.view.cronofy.RealtimeSchedulequeryperiods;
import com.fitwise.view.cronofy.RefreshAccessTokenResponse;
import com.fitwise.view.cronofy.SchedulePayloadcreator;
import com.fitwise.view.cronofy.SchedulePayloadcustomproperties;
import com.fitwise.view.cronofy.SchedulePayloadorganizer;
import com.fitwise.view.discounts.ProgramDiscountMappingListResponseView;
import com.fitwise.view.discounts.ProgramDiscountMappingResponseView;
import com.fitwise.view.member.Filter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kloudless.models.Resource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberPackageService {

    @Autowired
    private SubscriptionPackageRepository subscriptionPackageRepository;

    @Autowired
    private PackageKloudlessMappingRepository packageKloudlessMappingRepository;

    @Autowired
    private PackageProgramMappingRepository packageProgramMappingRepository;

    @Autowired
    private UserComponents userComponents;

    @Autowired
    private PackageMemberMappingRepository packageMemberMappingRepository;

    @Autowired
    private PackageExternalClientMappingRepository packageExternalClientMappingRepository;

    @Autowired
    private ProgramTypeRepository programTypeRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private OrderManagementRepository orderManagementRepository;

    @Autowired
    private PackageSubscriptionRepository packageSubscriptionRepository;

    @Autowired
    private FitwiseUtils fitwiseUtils;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private StripePaymentRepository stripePaymentRepository;

    @Autowired
    private StripeSubscriptionAndUserPackageMappingRepository stripeSubscriptionAndUserPackageMappingRepository;

    @Autowired
    private PackageOfferMappingRepository packageOfferMappingRepository;

    @Autowired
    private DiscountsService discountsService;

    @Autowired
    private UserKloudlessScheduleRepository userKloudlessScheduleRepository;

    @Autowired
    private KloudLessService kloudLessService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CalendarMeetingTypeRepository calendarMeetingTypeRepository;

    @Autowired
    private ProgramDataParsing programDataParsing;

    @Autowired
    private PackageAccessTokenRepository packageAccessTokenRepository;

    @Autowired
    private InstructorUnavailabilityService instructorUnavailabilityService;

    private final SubscriptionPackageJpa subscriptionPackageJpa;

    @Autowired
	private CronofyAvailabilityRulesRepository cronofyAvailabilityRulesRepository;
    
    @Autowired
    private CalendarService calendarService;
    
    @Autowired
	private CronofyService cronofyService;

    private final FreeAccessService freeAccessService;
    private final MemberPackageV2Service memberPackageV2Service;
    private final AppConfigKeyValueRepository appConfigKeyValueRepository;




    public SubscriptionPackageFilterModel getSubscriptionPackageFilter()
    {
        SubscriptionPackageFilterModel subscriptionPackageFilterModel = new SubscriptionPackageFilterModel();

        List<ProgramTypes> programTypesList = programTypeRepository.findAll();
        List<CalendarMeetingType> calendarMeetingTypeList = calendarMeetingTypeRepository.findAll();


        List<Filter> programFilterList = new ArrayList<>();
        List<Filter> meetingFilterList = new ArrayList<>();
        for(ProgramTypes programTypes : programTypesList)
        {
            Filter filter = new Filter();
            filter.setFilterId(programTypes.getProgramTypeId());
            filter.setFilterName(programTypes.getProgramTypeName());
            programFilterList.add(filter);
        }
        for(CalendarMeetingType calendarMeetingType : calendarMeetingTypeList)
        {
            Filter filter = new Filter();
            filter.setFilterId(calendarMeetingType.getMeetingTypeId());
            if(calendarMeetingType.getMeetingType().equalsIgnoreCase(DBConstants.INPERSON_SESSION)){
                filter.setFilterName(KeyConstants.INPERSON_SESSION_FILTER);
            }else if(calendarMeetingType.getMeetingType().equalsIgnoreCase(DBConstants.INPERSON_OR_VIRTUAL_SESSION)){
                filter.setFilterName(KeyConstants.INPERSON_OR_VIRTUAL_SESSION_FILTER);
            }else{
                filter.setFilterName(calendarMeetingType.getMeetingType());
            }
            meetingFilterList.add(filter);
        }

        PackageFilterView packageFilterViewForProgramType = new PackageFilterView();
        PackageFilterView packageFilterViewForMeetingType = new PackageFilterView();
        packageFilterViewForProgramType.setFilterName(KeyConstants.KEY_PROGRAM_TYPE_FILTER_NAME);
        packageFilterViewForProgramType.setFilters(programFilterList);
        packageFilterViewForMeetingType.setFilterName(KeyConstants.KEY_SESSION_TYPE_FILTER_NAME);
        packageFilterViewForMeetingType.setFilters(meetingFilterList);

        subscriptionPackageFilterModel.setFilterData(Arrays.asList(packageFilterViewForProgramType, packageFilterViewForMeetingType));

        return subscriptionPackageFilterModel;
    }

    /**
     * Get Subscription Packages
     * @param pageNo
     * @param pageSize
     * @param filterModel
     * @param search
     * @return
     */
    public SubscriptionPackageResponseViewForDiscover getSubscriptionPackages(final int pageNo, final int pageSize, PackageFilterModel filterModel, Optional<String> search){
        long startGetSubscriptionTIme = new Date().getTime();
        log.info("Get subscription package start : " + startGetSubscriptionTIme);
        long profilingStartTime;
        long profilingEndTime;
        RequestParamValidator.pageSetup(pageNo, pageSize);
        List<Long> sessionTypeIdList;
        List<Long> programTypeIdList;
        profilingStartTime = new Date().getTime();
        Specification<SubscriptionPackage> statusSpec = PackageSpecification.getSubscriptionPackageByStatus(InstructorConstant.PUBLISH);
        Specification<SubscriptionPackage> finalSpec;
        if (search.isPresent() && !search.get().isEmpty()) {
            Specification<SubscriptionPackage> searchSpec = PackageSpecification.getSubscriptionPackageByTitle(search.get());
            if (!(filterModel.getSession() == null || filterModel.getSession().isEmpty()) && !(filterModel.getProgram() == null || filterModel.getProgram().isEmpty())) {
                sessionTypeIdList = filterModel.getSession().stream().map(Filter::getFilterId).collect(Collectors.toList());
                Specification<SubscriptionPackage> sessionSpec = PackageSpecification.getSubscriptionPackageBySessionType(sessionTypeIdList);
                programTypeIdList = filterModel.getProgram().stream().map(Filter::getFilterId).collect(Collectors.toList());
                Specification<SubscriptionPackage> programSpec = PackageSpecification.getSubscriptionPackageByProgramType(programTypeIdList);
                finalSpec = statusSpec.and(searchSpec).and(sessionSpec).and(programSpec);
            } else {
                if (!(filterModel.getSession() == null || filterModel.getSession().isEmpty())) {
                    sessionTypeIdList = filterModel.getSession().stream().map(Filter::getFilterId).collect(Collectors.toList());
                    Specification<SubscriptionPackage> sessionSpec = PackageSpecification.getSubscriptionPackageBySessionType(sessionTypeIdList);
                    finalSpec = statusSpec.and(searchSpec).and(sessionSpec);
                } else if (!(filterModel.getProgram() == null || filterModel.getProgram().isEmpty())) {
                    programTypeIdList = filterModel.getProgram().stream().map(Filter::getFilterId).collect(Collectors.toList());
                    Specification<SubscriptionPackage> programSpec = PackageSpecification.getSubscriptionPackageByProgramType(programTypeIdList);
                    finalSpec = statusSpec.and(searchSpec).and(programSpec);
                } else {
                    finalSpec = statusSpec.and(searchSpec);
                }
            }
        } else {
            if (!(filterModel.getSession() == null || filterModel.getSession().isEmpty()) && !(filterModel.getProgram() == null || filterModel.getProgram().isEmpty())) {
                sessionTypeIdList = filterModel.getSession().stream().map(Filter::getFilterId).collect(Collectors.toList());
                Specification<SubscriptionPackage> sessionSpec = PackageSpecification.getSubscriptionPackageBySessionType(sessionTypeIdList);
                programTypeIdList = filterModel.getProgram().stream().map(Filter::getFilterId).collect(Collectors.toList());
                Specification<SubscriptionPackage> programSpec = PackageSpecification.getSubscriptionPackageByProgramType(programTypeIdList);
                finalSpec = statusSpec.and(sessionSpec).and(programSpec);
            } else {
                if (!(filterModel.getSession() == null || filterModel.getSession().isEmpty())) {
                    sessionTypeIdList = filterModel.getSession().stream().map(Filter::getFilterId).collect(Collectors.toList());
                    Specification<SubscriptionPackage> sessionSpec = PackageSpecification.getSubscriptionPackageBySessionType(sessionTypeIdList);
                    finalSpec = statusSpec.and(sessionSpec);
                } else if (!(filterModel.getProgram() == null || filterModel.getProgram().isEmpty())) {
                    programTypeIdList = filterModel.getProgram().stream().map(Filter::getFilterId).collect(Collectors.toList());
                    Specification<SubscriptionPackage> programSpec = PackageSpecification.getSubscriptionPackageByProgramType(programTypeIdList);
                    finalSpec = statusSpec.and(programSpec);
                } else {
                    finalSpec = statusSpec;
                }
            }
        }
        //Stripe active condition
        Specification<SubscriptionPackage> stripeActiveSpec = PackageSpecification.getPackageByStripeMapping();
        finalSpec = finalSpec.and(stripeActiveSpec);
        Specification<SubscriptionPackage> nonRestrictedSpec = PackageSpecification.getNonRestrictedPackages();


        Specification<SubscriptionPackage> packageIdsSpec ;
        List<Long> packageIds = new ArrayList<>();
        User user = userComponents.getAndValidateUser();
        if(user != null){
            List<PackageMemberMapping> packageMemberMappings = packageMemberMappingRepository.findByUser(user);
            packageIds.addAll(packageMemberMappings.stream()
                    .map(packageMemberMapping -> packageMemberMapping.getSubscriptionPackage().getSubscriptionPackageId())
                    .distinct()
                    .collect(Collectors.toList()));
            List<PackageExternalClientMapping> packageExternalClientMappings = packageExternalClientMappingRepository.findByExternalClientEmail(user.getEmail());
            packageIds.addAll(packageExternalClientMappings.stream()
                    .map(packageExternalClientMapping -> packageExternalClientMapping.getSubscriptionPackage().getSubscriptionPackageId())
                    .distinct()
                    .collect(Collectors.toList()));
            if(!packageIds.isEmpty()){
                packageIdsSpec = PackageSpecification.getPackageIdsIn(packageIds);
                Specification<SubscriptionPackage> packageSpecification = nonRestrictedSpec.or(packageIdsSpec);
                finalSpec = finalSpec.and(packageSpecification);
            }else{
                finalSpec = finalSpec.and(nonRestrictedSpec);
            }


        }else{
            finalSpec = finalSpec.and(nonRestrictedSpec);
        }


        Page<SubscriptionPackage> subscriptionPackageList = subscriptionPackageRepository.findAll(finalSpec, PageRequest.of((pageNo -1), pageSize));
        profilingEndTime = new Date().getTime();
        log.info("Querying paginated data : Time taken in milliseconds : "+(profilingEndTime-profilingStartTime));
        List<SubscriptionPackageTileViewForDiscover> subscriptionPackageTileViewList = new ArrayList<>();
        List<Long> packageIdList = subscriptionPackageList.stream().map(SubscriptionPackage::getSubscriptionPackageId).collect(Collectors.toList());
        Map<Long, Long> offerCountDaoMap = discountsService.getNoOfCurrentAvailableOffersOfPackagesForMember(packageIdList, user);
        Long sessionCount;
        Long programCount;
        profilingStartTime = new Date().getTime();
        // Get Free access program Ids
        List<Long> freeAccessSubscriptionPackageIds = freeAccessService.getUserSpecificFreeAccessSubscriptionPackageIds();
        for (SubscriptionPackage subscriptionPackage : subscriptionPackageList) {
            sessionCount = Long.valueOf(subscriptionPackageJpa.getBookableSessionCountForPackage(subscriptionPackage.getSubscriptionPackageId()));
            programCount = packageProgramMappingRepository.countBySubscriptionPackageSubscriptionPackageId(subscriptionPackage.getSubscriptionPackageId());
            SubscriptionPackageTileViewForDiscover subscriptionPackageTileViewForDiscover = constructSubscriptionPackageTileViewForDiscover(subscriptionPackage, offerCountDaoMap);
            subscriptionPackageTileViewForDiscover.setProgramCount(programCount);
            subscriptionPackageTileViewForDiscover.setSessionCount(sessionCount);
            if(freeAccessSubscriptionPackageIds.contains(subscriptionPackage.getSubscriptionPackageId())) {
            	subscriptionPackageTileViewForDiscover.setFreeToAccess(true);
            }
            subscriptionPackageTileViewList.add(subscriptionPackageTileViewForDiscover);
        }
        profilingEndTime = new Date().getTime();
        log.info("Response data construction : Time taken in milliseconds : "+(profilingEndTime-profilingStartTime));

        if(subscriptionPackageTileViewList.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        SubscriptionPackageResponseViewForDiscover subscriptionPackageResponseViewForDiscover = new SubscriptionPackageResponseViewForDiscover();
        subscriptionPackageResponseViewForDiscover.setSubscriptionPackages(subscriptionPackageTileViewList);
        subscriptionPackageResponseViewForDiscover.setTotalCount((int)subscriptionPackageList.getTotalElements());
        log.info("Get subscription package end : " + (new Date().getTime() - startGetSubscriptionTIme));
        return subscriptionPackageResponseViewForDiscover;
    }

    public SubscriptionPackageTileViewForDiscover constructSubscriptionPackageTileViewForDiscover(SubscriptionPackage subscriptionPackage,
                                                                                                  Map<Long, Long> offerCountDaoMap)
    {
        SubscriptionPackageTileViewForDiscover subscriptionPackageTileViewForDiscover = new SubscriptionPackageTileViewForDiscover();

        subscriptionPackageTileViewForDiscover.setSubscriptionPackageId(subscriptionPackage.getSubscriptionPackageId());
        if (subscriptionPackage.getImage() != null) {
            subscriptionPackageTileViewForDiscover.setImageUrl(subscriptionPackage.getImage().getImagePath());
        }
        subscriptionPackageTileViewForDiscover.setDuration(subscriptionPackage.getPackageDuration().getDuration());
        subscriptionPackageTileViewForDiscover.setTitle(subscriptionPackage.getTitle());
        subscriptionPackageTileViewForDiscover.setStatus(subscriptionPackage.getStatus());
        long numberOfCurrentAvailableOffers = offerCountDaoMap.get(subscriptionPackage.getSubscriptionPackageId());
        subscriptionPackageTileViewForDiscover.setNumberOfCurrentAvailableOffers((int) numberOfCurrentAvailableOffers);
        String instructorName = "";
        if (subscriptionPackage.getOwner() != null) {
            UserProfile userProfile = userProfileRepository.findByUser(subscriptionPackage.getOwner());
            if (userProfile != null) {
                if (userProfile.getProfileImage() != null && userProfile.getProfileImage().getImagePath() != null) {
                    subscriptionPackageTileViewForDiscover.setInstructorProfileUrl(userProfile.getProfileImage().getImagePath());
                }
                if (userProfile.getFirstName() != null && userProfile.getLastName() != null) {
                    instructorName = userProfile.getFirstName() + KeyConstants.KEY_SPACE + userProfile.getLastName();
                } else if (userProfile.getFirstName() != null) {
                    instructorName = userProfile.getFirstName();
                } else if (userProfile.getLastName() != null) {
                    instructorName = userProfile.getLastName();
                }

            }
        }
        subscriptionPackageTileViewForDiscover.setInstructorName(instructorName);
        return subscriptionPackageTileViewForDiscover;
    }

    /**
     * Get subscription package details for member
     * @param subscriptionPackageId
     * @return
     */
    public SubscriptionPackageMemberView getSubscriptionPackageDetails(Long subscriptionPackageId,Optional<String> token) throws ParseException, UnsupportedEncodingException {
        log.info("getSubscriptionPackageDetails starts.");
        long apiStartTimeMillis = new Date().getTime();

        long profilingStartTimeMillis = new Date().getTime();
        User user = userComponents.getAndValidateUser();
        if (subscriptionPackageId == null || subscriptionPackageId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_ID_NULL, MessageConstants.ERROR);
        }
        SubscriptionPackage subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageId(subscriptionPackageId);
        ValidationUtils.throwException(subscriptionPackage == null, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_NOT_FOUND, Constants.BAD_REQUEST);
        if(user == null && subscriptionPackage.isRestrictedAccess() && (!token.isPresent() || token.get().isEmpty()) ){
            throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_GUEST_USER_NO_ACCESS_FOR_PACKAGE, MessageConstants.ERROR);
        }
        if(user == null && subscriptionPackage.isRestrictedAccess() && token.isPresent() && !token.get().isEmpty()){
            String tokenString = new String(Base64.getDecoder().decode(token.get()),"utf-8");
            String[] tokenSplit = tokenString.split("##@##");
            PackageAccessToken packageAccessToken = packageAccessTokenRepository.findBySubscriptionPackageSubscriptionPackageIdAndAccessToken(Long.parseLong(tokenSplit[0]),tokenSplit[1]);
            if(packageAccessToken == null){
                throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_GUEST_USER_NO_ACCESS_FOR_PACKAGE, MessageConstants.ERROR);
            }
        }
        //Checking package status
        List<String> packageStatusList = Arrays.asList(InstructorConstant.PLAN, DBConstants.ACCESS_CONTROL, DBConstants.CONFIGURE, InstructorConstant.PRICE, InstructorConstant.PRE_PUBLISH, DBConstants.BLOCK_EDIT, DBConstants.UNPUBLISH_EDIT);
        boolean isRestricted = packageStatusList.stream().anyMatch(subscriptionPackage.getStatus()::equalsIgnoreCase);
        if (isRestricted) {
            throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_MEMBER_PACKAGE_NOT_AVAILABLE, MessageConstants.ERROR);
        }
        //Checking whether this member has access to a package
        if (user != null && subscriptionPackage.isRestrictedAccess()) {
            PackageMemberMapping packageMemberMapping = packageMemberMappingRepository.findTop1BySubscriptionPackageSubscriptionPackageIdAndUserUserId(subscriptionPackageId, user.getUserId());
            PackageExternalClientMapping packageExternalClientMapping = packageExternalClientMappingRepository.findTop1BySubscriptionPackageSubscriptionPackageIdAndExternalClientEmail(subscriptionPackageId, user.getEmail());
            if (packageMemberMapping == null && packageExternalClientMapping == null) {
                throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_MEMBER_NO_ACCESS_FOR_PACKAGE, MessageConstants.ERROR);
            }

        }
        long profilingEndTimeMillis = new Date().getTime();
        log.info("Validation : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        
        // Get free access package list
       	boolean isFreeAccessExits = freeAccessService.isExitsFreeAccessPackageForSpecificUser(subscriptionPackage);
        profilingStartTimeMillis = new Date().getTime();
        SubscriptionPackageMemberView subscriptionPackageMemberView = new SubscriptionPackageMemberView();
        if(isFreeAccessExits) {
        	subscriptionPackageMemberView.setFreeToAccess(true);
        }
        subscriptionPackageMemberView.setSubscriptionPackageId(subscriptionPackage.getSubscriptionPackageId());
        subscriptionPackageMemberView.setTitle(subscriptionPackage.getTitle());
        subscriptionPackageMemberView.setShortDescription(subscriptionPackage.getShortDescription());
        subscriptionPackageMemberView.setDescription(subscriptionPackage.getDescription());
        subscriptionPackageMemberView.setIsRestrictedAccess(subscriptionPackage.isRestrictedAccess());
        if (subscriptionPackage.getClientMessage() != null && !subscriptionPackage.getClientMessage().isEmpty()) {
            subscriptionPackageMemberView.setClientMessage(subscriptionPackage.getClientMessage());
        }
       
        if (subscriptionPackage.getImage() != null) {
            subscriptionPackageMemberView.setImageId(subscriptionPackage.getImage().getImageId());
            subscriptionPackageMemberView.setImageUrl(subscriptionPackage.getImage().getImagePath());
        }
        UserProfile instructorProfile = userProfileRepository.findByUser(subscriptionPackage.getOwner());
        if (instructorProfile != null) {
            subscriptionPackageMemberView.setInstructorId(instructorProfile.getUser().getUserId());
            subscriptionPackageMemberView.setInstructorFirstName(instructorProfile.getFirstName());
            subscriptionPackageMemberView.setInstructorLastName(instructorProfile.getLastName());
            if (instructorProfile.getProfileImage() != null) {
                subscriptionPackageMemberView.setInstructorProfileImageUrl(instructorProfile.getProfileImage().getImagePath());
            }
        }
        if (subscriptionPackage.getPackageDuration() != null) {
            subscriptionPackageMemberView.setDuration(subscriptionPackage.getPackageDuration().getDuration());
        }

        if(subscriptionPackage.getPromotion() != null && subscriptionPackage.getPromotion().getVideoManagement() != null && subscriptionPackage.getPromotion().getVideoManagement().getUploadStatus().equalsIgnoreCase(KeyConstants.KEY_COMPLETED)){
            subscriptionPackageMemberView.setPromoUrl(subscriptionPackage.getPromotion().getVideoManagement().getUrl());
            subscriptionPackageMemberView.setPromoThumbnailUrl(subscriptionPackage.getPromotion().getVideoManagement().getThumbnail().getImagePath());
            subscriptionPackageMemberView.setPromoUploadStatus(subscriptionPackage.getPromotion().getVideoManagement().getUploadStatus());
        }
        profilingEndTimeMillis = new Date().getTime();
        log.info("Basic details : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = new Date().getTime();
        List<ProgramTileForPackageView> programTileModels = new ArrayList<>();
        int programCount = 0;
        if (subscriptionPackage.getPackageProgramMapping() != null && !subscriptionPackage.getPackageProgramMapping().isEmpty()) {
            List<Equipments> equipments = new ArrayList<>();
            long temp = System.currentTimeMillis();
            List<Long> programIdList = subscriptionPackage.getPackageProgramMapping().stream().map(packageProgramMapping -> packageProgramMapping.getProgram().getProgramId()).collect(Collectors.toList());
            Map<Long, Long> offerCountMap = discountsService.getNoOfCurrentAvailableOffersOfProgramsForMember(programIdList, user);
            log.info("Offer count query : time taken in millis : "+(System.currentTimeMillis() - temp));

            for (PackageProgramMapping packageProgramMapping : subscriptionPackage.getPackageProgramMapping()) {
                Programs program = packageProgramMapping.getProgram();
                //Constructing Program tile model
                ProgramTileForPackageView programTileModel = programDataParsing.constructProgramTileModelForPackage(user,program, offerCountMap);
                programTileModels.add(programTileModel);
                //Getting equipment list for all the programs
                if (program.getProgramMapping() != null && !program.getProgramMapping().isEmpty()) {
                    for (WorkoutMapping workoutMapping : program.getProgramMapping()) {
                        Set<CircuitSchedule> circuitSchedules = workoutMapping.getWorkout().getCircuitSchedules();
                        for (CircuitSchedule circuitSchedule : circuitSchedules) {
                            if (!circuitSchedule.isRestCircuit()) {
                                for (ExerciseSchedulers exerciseScheduler : circuitSchedule.getCircuit().getExerciseSchedules()) {
                                    if (exerciseScheduler.getExercise() != null && exerciseScheduler.getExercise().getEquipments() != null) {
                                        equipments.addAll(exerciseScheduler.getExercise().getEquipments());

                                    }
                                }
                            }
                        }
                    }
                }
            }
            programCount = programTileModels.size();
            //Removing duplicate Equipment list and setting in response
            List<Equipments> equipmentsList = equipments.stream()
                    .collect(collectingAndThen(toCollection(() -> new TreeSet<Equipments>(comparingLong(Equipments::getEquipmentId))),
                            ArrayList::new));
            subscriptionPackageMemberView.setEquipments(equipmentsList);
        }
        subscriptionPackageMemberView.setPackagePrograms(programTileModels);
        subscriptionPackageMemberView.setNumberOfPrograms(programCount);
        profilingEndTimeMillis = new Date().getTime();
        log.info("Package Programs and equipments : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        profilingStartTimeMillis = new Date().getTime();
        if (subscriptionPackage.getPrice() != null && !subscriptionPackageMemberView.isFreeToAccess()) {
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            subscriptionPackageMemberView.setPrice(subscriptionPackage.getPrice());
            subscriptionPackageMemberView.setFormattedPrice(KeyConstants.KEY_CURRENCY_US_DOLLAR +decimalFormat.format(subscriptionPackage.getPrice()) );
        }
        CancellationDurationModel cancellationDurationModel = fitwiseUtils.constructCancellationDurationModel(subscriptionPackage.getCancellationDuration());
        if(cancellationDurationModel != null){
            subscriptionPackageMemberView.setCancellationDuration(cancellationDurationModel);
        }
        profilingEndTimeMillis = new Date().getTime();
        log.info("Price and cancellation duration : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        profilingStartTimeMillis = new Date().getTime();
        List<SessionMemberView> sessions = constructSessionMemberView(user,subscriptionPackage);
        subscriptionPackageMemberView.setSessions(sessions);
        if(user != null){
            List<SessionScheduleMemberView> sessionSchedules = getMySchedulesForPackage(subscriptionPackageId);
            subscriptionPackageMemberView.setSessionSchedules(sessionSchedules);
        }
        profilingEndTimeMillis = new Date().getTime();
        log.info("Session details : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        boolean isMemberBlocked = false;
        if (user != null) {
            isMemberBlocked = fitwiseUtils.isCurrentMemberBlocked();
            subscriptionPackageMemberView.setMemberBlocked(isMemberBlocked);
        }
        profilingStartTimeMillis = new Date().getTime();
        boolean isNewSubscription = true;
        boolean isSubscriptionExpired = false;
        boolean isPackageSubscribed = false;
        boolean isPackageAutoSubscribed = false;
        //Field to allow subscription
        List<String> subscriptionRestrictedStatusList = Arrays.asList(InstructorConstant.BLOCK, InstructorConstant.UNPUBLISH);
        boolean isPackageBlockedOrUnpublished = subscriptionRestrictedStatusList.stream().anyMatch(subscriptionPackage.getStatus()::equalsIgnoreCase);
        subscriptionPackageMemberView.setIsSubscriptionRestricted(isPackageBlockedOrUnpublished);

        //Program in unpublish/block status status not available for guest users. Not sending error msg since front team handled only for empty msg.
        if (user == null && isPackageBlockedOrUnpublished) {
            throw new ApplicationException(Constants.NOT_FOUND, null, MessageConstants.ERROR);
        }

        if (user != null && !subscriptionPackageMemberView.isFreeToAccess()) {
            //program processing state is not updated in L2 page.So condition moved here.
            OrderManagement orderManagement = orderManagementRepository.findTop1ByUserAndSubscriptionPackageOrderByCreatedDateDesc(user, subscriptionPackage);
            if (orderManagement != null && orderManagement.getOrderStatus() != null) {
                subscriptionPackageMemberView.setOrderStatus(orderManagement.getOrderStatus());
                if (orderManagement.getOrderStatus().equalsIgnoreCase(KeyConstants.KEY_PROCESSING)) {
                    subscriptionPackageMemberView.setOrderUnderProcessing(true);
                }
            }

            PackageSubscription packageSubscription = packageSubscriptionRepository.findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageIdOrderBySubscribedDateDesc(user.getUserId(), subscriptionPackageId);

            if (packageSubscription == null && isMemberBlocked) {
                throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_BLOCKED_MEMBER_PACKAGE_NOT_AVAILABLE, MessageConstants.ERROR);
            }
            if (packageSubscription == null && isPackageBlockedOrUnpublished) {
                throw new ApplicationException(Constants.NOT_FOUND, null, MessageConstants.ERROR);
            }
            SubscriptionStatus subscriptionStatus = subscriptionService.getMemberPackageSubscriptionStatus(packageSubscription);
            List<String> packageSubscribedStatusList = Arrays.asList(KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING, KeyConstants.KEY_EXPIRED);

            if (subscriptionStatus != null) {
                if (packageSubscribedStatusList.stream().anyMatch(subscriptionStatus.getSubscriptionStatusName()::equalsIgnoreCase)) {
                    isNewSubscription = false;
                }
                if (subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_EXPIRED)) {
                    isSubscriptionExpired = true;
                }
                if (subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID) || subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAYMENT_PENDING)) {
                    isPackageSubscribed = true;
                    if (orderManagement != null && orderManagement.getSubscribedViaPlatform().getPlatform().equalsIgnoreCase(DBConstants.WEB)) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(packageSubscription.getSubscribedDate());
                        cal.add(Calendar.DAY_OF_YEAR, Math.toIntExact(packageSubscription.getSubscriptionPlan().getDuration()));
                        subscriptionPackageMemberView.setSubscriptionExpiry(cal.getTimeInMillis());
                        subscriptionPackageMemberView.setSubscriptionExpiryDate(fitwiseUtils.formatDateWithTime(cal.getTime()));

                        StripeSubscriptionAndUserPackageMapping stripeSubscriptionAndUserPackageMapping = stripeSubscriptionAndUserPackageMappingRepository.findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageIdOrderByModifiedDateDesc(user.getUserId(), subscriptionPackage.getSubscriptionPackageId());
                        if (stripeSubscriptionAndUserPackageMapping != null && stripeSubscriptionAndUserPackageMapping.getSubscriptionStatus() != null &&
                                stripeSubscriptionAndUserPackageMapping.getSubscriptionStatus().getSubscriptionStatus().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_ACTIVE)) {
                            isPackageAutoSubscribed = true;
                        }

                        StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
                        if (stripePayment == null) {
                            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERROR_FETCHING_ORDER_TRANSACTION_DETAILS, null);
                        }
                        // Getting the payment status
                        if (stripePayment != null && KeyConstants.KEY_PAID.equals(stripePayment.getTransactionStatus())) {
                            subscriptionPackageMemberView.setOrderStatus(KeyConstants.KEY_SUCCESS);
                        } else {
                            subscriptionPackageMemberView.setOrderStatus(KeyConstants.KEY_FAILURE);
                        }

                    }
                    String status;
                    if (subscriptionStatus.getSubscriptionStatusName().equals(KeyConstants.KEY_PAYMENT_PENDING) || subscriptionStatus.getSubscriptionStatusName().equals(KeyConstants.KEY_PAID)) {
                        status = KeyConstants.KEY_SUBSCRIBED;
                    } else {
                        status = subscriptionStatus.getSubscriptionStatusName();
                    }
                    subscriptionPackageMemberView.setSubscriptionStatus(status);
                }
                subscriptionPackageMemberView.setSubscribedViaPlatform(packageSubscription.getSubscribedViaPlatform());
                subscriptionPackageMemberView.setPackageSubscribed(isPackageSubscribed);
                subscriptionPackageMemberView.setPackageAutoSubscribed(isPackageAutoSubscribed);

            }
            if (isSubscriptionExpired) {
                if (isMemberBlocked) {
                    throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_BLOCKED_MEMBER_PACKAGE_NOT_AVAILABLE, MessageConstants.ERROR);
                }
                //For unpublished and blocked package, if the subscription is expired or trial completed, L2 page is not rendered
                if (isPackageBlockedOrUnpublished) {
                    throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_MEMBER_PACKAGE_NOT_AVAILABLE, MessageConstants.ERROR);
                }
            }
            if (packageSubscription != null) {
                subscriptionPackageMemberView.setSubscribedDate(packageSubscription.getSubscribedDate());
                subscriptionPackageMemberView.setSubscribedDateFormatted(fitwiseUtils.formatDateWithTime(packageSubscription.getSubscribedDate()));
                String subscriptionValidity;
                if (isPackageSubscribed) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(packageSubscription.getSubscribedDate());
                    cal.add(Calendar.DAY_OF_YEAR, Math.toIntExact(packageSubscription.getSubscriptionPlan().getDuration()));
                    Date subscriptionExpiryDate = cal.getTime();

                    LocalDate startLocalDate = LocalDate.now();
                    LocalDate endLocalDate = subscriptionExpiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                    Period diff = Period.between(startLocalDate, endLocalDate);
                    int validity = diff.getDays();
                    if (validity == 0) {
                        subscriptionValidity = "Expires today";
                    } else {
                        subscriptionValidity = "Expires in " + validity + " days";
                    }
                } else {
                    subscriptionValidity = "SUBSCRIBE";
                }
                subscriptionPackageMemberView.setSubscriptionValidity(subscriptionValidity);
            }
        }
        profilingEndTimeMillis = new Date().getTime();
        log.info("Subscription details : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        // Get & set Flat tax amount
        profilingStartTimeMillis = new Date().getTime();
        AppConfigKeyValue appConfigKeyValue = appConfigKeyValueRepository.findByKeyString(AppConfigConstants.ADMIN_CONFIG_FLAT_TAX);
        if(appConfigKeyValue != null && appConfigKeyValue.getValueString() != null){
            double flatTaxAmount = Double.parseDouble(appConfigKeyValue.getValueString());
            subscriptionPackageMemberView.setFlatTax(flatTaxAmount);
        }
        profilingEndTimeMillis = new Date().getTime();
        log.info("Flat tax details : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = new Date().getTime();
        /** discount Offers **/
        List<PackageOfferMapping> packageOfferMappings = packageOfferMappingRepository.findBySubscriptionPackageSubscriptionPackageIdAndOfferCodeDetailIsInUseAndOfferCodeDetailOfferStatus(subscriptionPackageId, true, DiscountsConstants.OFFER_ACTIVE);
        if (packageOfferMappings != null && !packageOfferMappings.isEmpty()) {

            List<ProgramDiscountMappingResponseView> currentDiscounts = new ArrayList<>();
            List<ProgramDiscountMappingResponseView> freeOffers = new ArrayList<>();
            List<ProgramDiscountMappingResponseView> paidOffers = new ArrayList<>();
            List<ProgramDiscountMappingResponseView> upcomingDiscounts = new ArrayList<>();
            List<ProgramDiscountMappingResponseView> expiredDiscounts = new ArrayList<>();
            for (PackageOfferMapping packageOfferMapping : packageOfferMappings) {
                if (isNewSubscription == packageOfferMapping.getOfferCodeDetail().getIsNewUser().booleanValue()) {
                    ProgramDiscountMappingResponseView discountMappingResponseView = new ProgramDiscountMappingResponseView();

                    discountMappingResponseView.setOfferMappingId(packageOfferMapping.getPackageOfferMappingId());
                    discountMappingResponseView.setOfferCodeId(packageOfferMapping.getOfferCodeDetail().getOfferCodeId());
                    discountMappingResponseView.setOfferName(packageOfferMapping.getOfferCodeDetail().getOfferName().trim());
                    discountMappingResponseView.setOfferCode(packageOfferMapping.getOfferCodeDetail().getOfferCode().toUpperCase());
                    discountMappingResponseView.setOfferMode(packageOfferMapping.getOfferCodeDetail().getOfferMode());
                    discountMappingResponseView.setOfferDuration(packageOfferMapping.getOfferCodeDetail().getOfferDuration());
                    discountMappingResponseView.setOfferStartDate(fitwiseUtils.formatDate(packageOfferMapping.getOfferCodeDetail().getOfferStartingDate()));
                    discountMappingResponseView.setOfferEndDate(fitwiseUtils.formatDate(packageOfferMapping.getOfferCodeDetail().getOfferEndingDate()));

                    discountMappingResponseView.setOfferPrice(packageOfferMapping.getOfferCodeDetail().getOfferPrice());
                    String formattedPrice;
                    if (packageOfferMapping.getOfferCodeDetail().getOfferPrice() != null) {
                        formattedPrice = fitwiseUtils.formatPrice(packageOfferMapping.getOfferCodeDetail().getOfferPrice().getPrice());
                        discountMappingResponseView.setFormattedOfferPrice(formattedPrice);
                    }
                    else {
                        formattedPrice = KeyConstants.KEY_CURRENCY_US_DOLLAR+KeyConstants.KEY_DEFAULT_PRICE_FORMAT;
                        discountMappingResponseView.setFormattedOfferPrice(formattedPrice);
                    }

                    if (subscriptionPackage.getPrice() != null) {
                        double savingsAmount = subscriptionPackage.getPrice();
                        if (packageOfferMapping.getOfferCodeDetail().getOfferPrice() != null) {
                            savingsAmount = subscriptionPackage.getPrice() - packageOfferMapping.getOfferCodeDetail().getOfferPrice().getPrice();
                        }
                        DecimalFormat decimalFormat = new DecimalFormat("0.00");
                        String roundedSavingsPrice = decimalFormat.format(savingsAmount);
                        discountMappingResponseView.setFormattedSavingsAmount(KeyConstants.KEY_CURRENCY_US_DOLLAR + roundedSavingsPrice);
                    }

                    discountMappingResponseView.setOfferStatus(packageOfferMapping.getOfferCodeDetail().getOfferStatus());
                    discountMappingResponseView.setIsNewUser(packageOfferMapping.getOfferCodeDetail().getIsNewUser());
                    //Offer Validity check
                    Date now = new Date();
                    Date offerStart = packageOfferMapping.getOfferCodeDetail().getOfferStartingDate();
                    Date offerEnd = packageOfferMapping.getOfferCodeDetail().getOfferEndingDate();
                    if (packageOfferMapping.getOfferCodeDetail().getOfferStatus().equalsIgnoreCase(DiscountsConstants.OFFER_ACTIVE) && packageOfferMapping.getOfferCodeDetail().isInUse()) {
                        // Current Offers
                        if ((offerStart.equals(now) || offerStart.before(now)) && (offerEnd.equals(now) || offerEnd.after(now))) {
                            if (packageOfferMapping.getOfferCodeDetail().getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_FREE)) {
                                freeOffers.add(discountMappingResponseView);
                            } else if (packageOfferMapping.getOfferCodeDetail().getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_PAY_AS_YOU_GO)) {
                                paidOffers.add(discountMappingResponseView);
                            }
                        }
                        // UpComing Offers
                        else if (offerStart.after(now) && offerEnd.after(now)) {
                            upcomingDiscounts.add(discountMappingResponseView);
                        }
                    }else {
                        // Expired Offers
                        if (offerStart.before(now) && offerEnd.before(now)) {
                            expiredDiscounts.add(discountMappingResponseView);
                        }
                    }
                }
            }
            ProgramDiscountMappingListResponseView discountOffers = new ProgramDiscountMappingListResponseView();
            paidOffers.sort((ProgramDiscountMappingResponseView f1,ProgramDiscountMappingResponseView f2) -> f1.compareTo(f2));
            freeOffers.sort((ProgramDiscountMappingResponseView f1,ProgramDiscountMappingResponseView f2) -> f1.compareTo(f2));
            Collections.sort(freeOffers, Collections.reverseOrder());
            freeOffers.stream().collect(Collectors.toCollection(()->currentDiscounts));
            paidOffers.stream().collect(Collectors.toCollection(()->currentDiscounts));
            discountOffers.setCurrentDiscounts(currentDiscounts);
            discountOffers.setUpcomingDiscounts(upcomingDiscounts);
            discountOffers.setExpiredDiscounts(expiredDiscounts);
            subscriptionPackageMemberView.setDiscountOffers(discountOffers);

            profilingEndTimeMillis = new Date().getTime();
            log.info("Discount details : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        }
        long apiEndTimeMillis = new Date().getTime();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("getSubscriptionPackageDetails ends.");

        return subscriptionPackageMemberView;
    }

    /**
     * Get member subscribed packages based subscription status
     * @param status
     * @param pageNo
     * @param pageSize
     * @param searchName
     * @return
     */
    public ResponseModel getMyPackages(String status, int pageNo, int pageSize, Optional<String> searchName){
        log.info("Get my packages starts.");
        long apiStartTimeMillis = new Date().getTime();
        User user = userComponents.getUser();
        RequestParamValidator.pageSetup(pageNo, pageSize);
        if(StringUtils.isEmpty(status)){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_STATUS_PARAM_NULL, null);
        }
        log.info(StringConstants.LOG_FIELD_VALIDATION + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        List<String> statusList = new ArrayList<>();
        //possible subscription status based on request param subscriptionStatusParam
        Specification<PackageSubscription> packageSubscriptionSpecification;
        String searchString = "";
        if(searchName.isPresent() && !StringUtils.isEmpty(searchName.get())){
            searchString = searchName.get();
        }
        // User specific subscription package Ids
        List<FreeAccessPackages> freeAccessSupscriptionPackageList = freeAccessService.getUserSpecificFreeAccessPackage(null);
        List<Long> freeAccessSupscriptionPackageIdList = freeAccessSupscriptionPackageList.stream().map(packageSubscription -> packageSubscription.getSubscriptionPackage().getSubscriptionPackageId()).collect(Collectors.toList());

        if (status.equalsIgnoreCase(KeyConstants.KEY_PAID)) {
            statusList.add(KeyConstants.KEY_PAID);
            statusList.add(KeyConstants.KEY_PAYMENT_PENDING);
            packageSubscriptionSpecification = PackageSubscriptionSpcifications.getPackageSubscriptionByUserAndStatusAndMemberSearchWithActive(user.getUserId(), statusList,searchString);
        } else {
            statusList.add(KeyConstants.KEY_PAID);
            statusList.add(KeyConstants.KEY_PAYMENT_PENDING);
            statusList.add(KeyConstants.KEY_EXPIRED);
            if(status.equalsIgnoreCase(KeyConstants.KEY_EXPIRED)){
                packageSubscriptionSpecification = PackageSubscriptionSpcifications.getPackageSubscriptionByUserAndStatusAndMemberSearchWithExpired(user.getUserId(), statusList,searchString);
            }else{
                packageSubscriptionSpecification = PackageSubscriptionSpcifications.getPackageSubscriptionByUserAndStatusAndTitleSearch(user.getUserId(), statusList,searchString);
            }
        }
        Page<PackageSubscription> packageSubscriptionPage = packageSubscriptionRepository.findAll(packageSubscriptionSpecification, PageRequest.of(pageNo -1, pageSize));
        log.info("Query to get package subscriptions : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
		if (packageSubscriptionPage.isEmpty() && freeAccessSupscriptionPackageList.isEmpty()
				|| (packageSubscriptionPage.isEmpty() && status.equalsIgnoreCase(KeyConstants.KEY_EXPIRED))) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        List<Long> packageIdList = packageSubscriptionPage.stream().map(packageSubscription -> packageSubscription.getSubscriptionPackage().getSubscriptionPackageId()).collect(Collectors.toList());
        Map<Long, Long> offerCountDaoMap = discountsService.getNoOfCurrentAvailableOffersOfPackagesForMember(packageIdList, user);
        log.info("Get offer count dao map : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        List<MemberPackageTileView> memberPackageTileViewList = new ArrayList<>();
        MemberPackageTileListView memberPackageTileListView = new MemberPackageTileListView();
        for(PackageSubscription packageSubscription : packageSubscriptionPage.getContent()){
			if (!freeAccessSupscriptionPackageIdList
					.contains(packageSubscription.getSubscriptionPackage().getSubscriptionPackageId())) {  // Skip subscription package if id present in free package list
                MemberPackageTileView memberPackageTileView = new MemberPackageTileView();
                OrderManagement orderManagement = orderManagementRepository.findTop1ByUserAndSubscriptionPackageOrderByCreatedDateDesc(user,packageSubscription.getSubscriptionPackage());
                if (KeyConstants.KEY_STRIPE.equals(orderManagement.getModeOfPayment())) {
                    StripeSubscriptionAndUserPackageMapping stripeSubscriptionAndUserPackageMapping = stripeSubscriptionAndUserPackageMappingRepository.findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageIdOrderByModifiedDateDesc(user.getUserId(), orderManagement.getSubscriptionPackage().getSubscriptionPackageId());
                    if (stripeSubscriptionAndUserPackageMapping != null && stripeSubscriptionAndUserPackageMapping.getSubscriptionStatus() != null &&
                            stripeSubscriptionAndUserPackageMapping.getSubscriptionStatus().getSubscriptionStatus().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_ACTIVE)) {
                        memberPackageTileView.setAutoSubscriptionOn(true);
                    }
                }
                memberPackageTileView.setOrderStatus(orderManagement.getOrderStatus());
                Calendar cal = Calendar.getInstance();
                cal.setTime(packageSubscription.getSubscribedDate());
                cal.add(Calendar.DAY_OF_YEAR, Math.toIntExact(packageSubscription.getSubscriptionPlan().getDuration()));
                String statusName = KeyConstants.KEY_PAID;
                if(new Date().after(cal.getTime())){
                    statusName = KeyConstants.KEY_EXPIRED;
                }
                memberPackageTileView.setSubscriptionExpiry(cal.getTime());
                memberPackageTileView.setSubscriptionExpiryFormatted(fitwiseUtils.formatDateWithTime(cal.getTime()));

                memberPackageTileView.setSubscriptionPackageId(packageSubscription.getSubscriptionPackage().getSubscriptionPackageId());
                memberPackageTileView.setTitle(packageSubscription.getSubscriptionPackage().getTitle());
                memberPackageTileView.setDuration(packageSubscription.getSubscriptionPackage().getPackageDuration().getDuration().intValue() + " days");
                memberPackageTileView.setImageId(packageSubscription.getSubscriptionPackage().getImage().getImageId());
                memberPackageTileView.setImageUrl(packageSubscription.getSubscriptionPackage().getImage().getImagePath());
                int programCount = packageProgramMappingRepository.countBySubscriptionPackage(packageSubscription.getSubscriptionPackage());
                memberPackageTileView.setNoOfPrograms(programCount);
                int sessionCount = subscriptionPackageJpa.getBookableSessionCountForPackage(packageSubscription.getSubscriptionPackage().getSubscriptionPackageId());
                memberPackageTileView.setSessionCount(sessionCount);
                memberPackageTileView.setSubscribedDate(packageSubscription.getSubscribedDate());
                memberPackageTileView.setSubscribedDateFormatted(fitwiseUtils.formatDate(packageSubscription.getSubscribedDate()));
                long numberOfCurrentAvailableOffers = offerCountDaoMap.get(packageSubscription.getSubscriptionPackage().getSubscriptionPackageId());
                memberPackageTileView.setNumberOfCurrentAvailableOffers((int) numberOfCurrentAvailableOffers);
                memberPackageTileView.setSubscriptionStatus(statusName);
                memberPackageTileViewList.add(memberPackageTileView);
        	}
        }
        log.info("Construct member package tile view : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get my packages ends.");
        memberPackageTileListView.setTotalCount((int)packageSubscriptionPage.getTotalElements());
        memberPackageTileListView.setPackages(memberPackageTileViewList);
		// Add free access packages list in response
		List<MemberPackageTileView> packageList = memberPackageV2Service.constructFreeAccessPackageList();
		if (!packageList.isEmpty() && !status.equalsIgnoreCase(KeyConstants.KEY_EXPIRED)) {
			memberPackageTileListView.setTotalCount(memberPackageTileListView.getTotalCount() + packageList.size());
			memberPackageTileViewList.addAll(packageList);
			memberPackageTileListView.setPackages(memberPackageTileViewList);
		}
        return new ResponseModel(Constants.SUCCESS_STATUS,MessageConstants.MSG_DATA_RETRIEVED, memberPackageTileListView);
    }
    
    /**
     * Get subscribed packages List
     * @return
     */
    public ResponseModel getSubscribedPackagesList(int pageNo, int pageSize){
        log.info("Get subscribed package list starts.");
        long apiStartTimeMillis = new Date().getTime();

        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ERROR, null);
        }

        User user = userComponents.getUser();

        PageRequest pageRequest = PageRequest.of(pageNo-1,pageSize);
        List<String> statusList = new ArrayList<>();
        statusList.add(KeyConstants.KEY_PAID);
        statusList.add(KeyConstants.KEY_PAYMENT_PENDING);

        Specification<PackageSubscription> finalSpec = PackageSubscriptionSpcifications.getPackageSubscriptionByUserAndStatusAndWithActiveOrderByDate(user.getUserId(),statusList, SearchConstants.ORDER_DSC);
        Page<PackageSubscription> packageSubscriptionPage = packageSubscriptionRepository.findAll(finalSpec,pageRequest);
        log.info("Query to get package subscriptions : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        if(packageSubscriptionPage.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        List<MemberPackageTileView> memberPackageTileViews = new ArrayList<>();

        for(PackageSubscription packageSubscription : packageSubscriptionPage.getContent()){
            MemberPackageTileView memberPackageTileView = new MemberPackageTileView();
            SubscriptionPackage subscriptionPackage = packageSubscription.getSubscriptionPackage();
            memberPackageTileView.setSubscriptionPackageId(subscriptionPackage.getSubscriptionPackageId());
            memberPackageTileView.setTitle(subscriptionPackage.getTitle());
            memberPackageTileView.setDuration(subscriptionPackage.getPackageDuration().getDuration().intValue() + " Days");
            memberPackageTileView.setImageId(subscriptionPackage.getImage().getImageId());
            memberPackageTileView.setImageUrl(subscriptionPackage.getImage().getImagePath());
            int programCount = packageProgramMappingRepository.countBySubscriptionPackage(subscriptionPackage);
            memberPackageTileView.setNoOfPrograms(programCount);
            int sessionCount = subscriptionPackageJpa.getBookableSessionCountForPackage(subscriptionPackage.getSubscriptionPackageId());
            memberPackageTileView.setSessionCount(sessionCount);
            memberPackageTileView.setSubscribedDate(packageSubscription.getSubscribedDate());
            memberPackageTileView.setSubscribedDateFormatted(fitwiseUtils.formatDate(packageSubscription.getSubscribedDate()));

            OrderManagement orderManagement = orderManagementRepository.findTop1ByUserAndSubscriptionPackageOrderByCreatedDateDesc(user,subscriptionPackage);
            if (KeyConstants.KEY_STRIPE.equals(orderManagement.getModeOfPayment())) {
                StripeSubscriptionAndUserPackageMapping stripeSubscriptionAndUserPackageMapping = stripeSubscriptionAndUserPackageMappingRepository.findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageIdOrderByModifiedDateDesc(user.getUserId(), orderManagement.getSubscriptionPackage().getSubscriptionPackageId());
                if (stripeSubscriptionAndUserPackageMapping != null && stripeSubscriptionAndUserPackageMapping.getSubscriptionStatus() != null &&
                        stripeSubscriptionAndUserPackageMapping.getSubscriptionStatus().getSubscriptionStatus().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_ACTIVE)) {
                    memberPackageTileView.setAutoSubscriptionOn(true);
                }
            }
            memberPackageTileViews.add(memberPackageTileView);
        }
        log.info("Construct member package tile view : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        long totalCount = packageSubscriptionPage.getTotalElements();

        MemberPackageTileListView memberPackageTileListView = new MemberPackageTileListView();
        memberPackageTileListView.setTotalCount((int) totalCount);
        memberPackageTileListView.setPackages(memberPackageTileViews);
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get subscribed package list ends.");

        return  new ResponseModel(Constants.SUCCESS_STATUS,MessageConstants.MSG_DATA_RETRIEVED,memberPackageTileListView);

    }

    public List<SessionScheduleMemberView> getMySchedulesForPackage(long subscriptionPackageId) throws ParseException {
        User user = userComponents.getUser();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(StringConstants.PATTERN_DATE);
        List<SessionScheduleMemberView> sessionSchedules = new ArrayList<>();
        List<UserKloudlessSchedule> userKloudlessSchedules = userKloudlessScheduleRepository.findByUserAndSubscriptionPackageSubscriptionPackageId(user,subscriptionPackageId);
        if(!userKloudlessSchedules.isEmpty()){
            for(UserKloudlessSchedule userKloudlessSchedule : userKloudlessSchedules){
                SessionScheduleMemberView sessionScheduleMemberView = new SessionScheduleMemberView();
                sessionScheduleMemberView.setMeetingId(userKloudlessSchedule.getUserKloudlessMeeting().getMeetingId());
                sessionScheduleMemberView.setFitwiseScheduleId(userKloudlessSchedule.getUserKloudlessScheduleId());
                Resource resource = kloudLessService.getKloudlessSchedule(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar(), userKloudlessSchedule.getScheduleId());
                if(resource == null){
                    continue;
                }
                Gson gson = new Gson();
                sessionScheduleMemberView.setSchedulePayload(gson.toJson(resource.getData()));
                sessionScheduleMemberView.setSubscriptionPackageId(subscriptionPackageId);
                sessionScheduleMemberView.setBookingDate(simpleDateFormat.parse(fitwiseUtils.formatDate(userKloudlessSchedule.getBookingDate())));
                sessionSchedules.add(sessionScheduleMemberView);
            }
        }
        return sessionSchedules;
    }


    /**
     * Get subscribed packages List
     * @param userId
     * @param pageNo
     * @param pageSize
     * @return
     */
    public SubscriptionPackageResponseViewForDiscover getInstructorPackages(final Long userId, final int pageNo, final int pageSize, Optional<String> search) {
        log.info("Get Instructor packages starts");
        long start = new Date().getTime();
        long profilingStart;
        long profilingEnd;
        if (userId == null) {
            throw new ApplicationException(Constants.ERROR_STATUS, ValidationMessageConstants.MSG_INSTRUCTOR_ID_NULL, null);
        }
        User instructor = userRepository.findByUserId(userId);
        if (!fitwiseUtils.isInstructor(instructor)) {
            throw new ApplicationException(Constants.ERROR_STATUS, ValidationMessageConstants.MSG_USER_NOT_INSTRUCTOR, null);
        }
        RequestParamValidator.pageSetup(pageNo, pageSize);
        profilingEnd = new Date().getTime();
        log.info(StringConstants.LOG_FIELD_VALIDATION + (profilingEnd - start));
        User user = userComponents.getAndValidateUser();
        profilingStart = new Date().getTime();
        List<PackageDao> packageDaos = subscriptionPackageJpa.getInstructorPackages(instructor.getUserId(), user, pageNo - 1, pageSize, search);
        log.info("Query Time  : " + (new Date().getTime() - profilingStart));
        if (packageDaos.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        profilingStart = new Date().getTime();
        List<Long> packageIdList = packageDaos.stream().map(PackageDao::getSubscriptionPackageId).collect(Collectors.toList());
        Map<Long, Long> offerCountDaoMap = discountsService.getNoOfCurrentAvailableOffersOfPackagesForMember(packageIdList, user);
        log.info("Query Time for offer count : " + (new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();
        List<SubscriptionPackageTileViewForDiscover> subscriptionPackageTileViewForDiscoverList = constructInstructorPackages(packageDaos, offerCountDaoMap, instructor);
        profilingEnd = new Date().getTime();
        log.info("response construction : " + (profilingEnd - profilingStart));
        SubscriptionPackageResponseViewForDiscover subscriptionPackageResponseViewForDiscover = new SubscriptionPackageResponseViewForDiscover();
        subscriptionPackageResponseViewForDiscover.setSubscriptionPackages(subscriptionPackageTileViewForDiscoverList);
        profilingStart = new Date().getTime();
        subscriptionPackageResponseViewForDiscover.setTotalCount(Math.toIntExact(subscriptionPackageJpa.getInstructorPackagesCount(instructor.getUserId(), user, search)));
        log.info("Count query : " + (new Date().getTime() - profilingStart));
        profilingEnd = new Date().getTime();
        log.info("API total time : " + (profilingEnd - start));
        log.info("Get Instructor packages ends");
        return subscriptionPackageResponseViewForDiscover;
    }
   
    public ResponseModel getPackageSchedules(Long subscriptionPackageId) throws ParseException {
        log.info("Get package schedules starts.");
        long apiStartTimeMillis = new Date().getTime();
        User user = userComponents.getUser();
        String member_Account_Token = "";
        String member_Calendar_ID = "";
		 UserKloudlessAccount account_member = calendarService.getActiveAccount(user);
		 if(account_member != null){
			 UserKloudlessCalendar activeCalendar_member = calendarService.getActiveCalendarFromKloudlessAccount(account_member);
			 if(activeCalendar_member != null){
				 member_Account_Token = account_member.getRefreshToken();
		         member_Calendar_ID = activeCalendar_member.getCalendarId();
		     }
	     }

        SubscriptionPackage subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageId(subscriptionPackageId);
        ValidationUtils.throwException(subscriptionPackage == null, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_NOT_FOUND, Constants.BAD_REQUEST);
        PackageSubscription packageSubscription = packageSubscriptionRepository.findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageIdOrderBySubscribedDateDesc(user.getUserId(),subscriptionPackageId);
        if(packageSubscription == null){
            throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_MEMBER_NOT_SUBSCRIBED_FOR_PACKAGE, MessageConstants.ERROR);
        }
        SubscriptionStatus subscriptionStatus = subscriptionService.getMemberPackageSubscriptionStatus(packageSubscription);
        if (subscriptionStatus != null && !subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID) && !subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAYMENT_PENDING)) {
            throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_MEMBER_NOT_SUBSCRIBED_FOR_PACKAGE, MessageConstants.ERROR);
        }
        log.info(StringConstants.LOG_FIELD_VALIDATION + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(StringConstants.PATTERN_DATE);
        Date subscribedDate = fitwiseUtils.convertToUserTimeZone(packageSubscription.getSubscribedDate());
        Date scheduleDate = subscribedDate;
        TimeZone UNAV_userTimeZone = TimeZone.getTimeZone(fitwiseUtils.getUserTimeZone());
        TimeZone userTimeZone = TimeZone.getTimeZone("Etc/UTC");
        int order = 1;
        log.info("Time zone change : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        List<MemberPackageScheduleDayView> memberPackageScheduleDayViews = new ArrayList<>();
        while(order <= KeyConstants.KEY_SESSION_BOOKING_DURATION){
            MemberPackageScheduleDayView memberPackageScheduleDayView = new MemberPackageScheduleDayView();
            memberPackageScheduleDayView.setDate(simpleDateFormat.format(scheduleDate));
            memberPackageScheduleDayView.setOrder(order);
            ZonedDateTime start = scheduleDate.toInstant().atZone(userTimeZone.toZoneId())
                    .with(ChronoField.HOUR_OF_DAY, 0)
                    .with(ChronoField.MINUTE_OF_DAY, 0)
                    .with(ChronoField.SECOND_OF_DAY, 0);
            ZonedDateTime UNAV_start = scheduleDate.toInstant().atZone(UNAV_userTimeZone.toZoneId())
                    .with(ChronoField.HOUR_OF_DAY, 0)
                    .with(ChronoField.MINUTE_OF_DAY, 0)
                    .with(ChronoField.SECOND_OF_DAY, 0);
            ZonedDateTime UNAV_end = scheduleDate.toInstant().atZone(UNAV_userTimeZone.toZoneId())
                    .with(ChronoField.HOUR_OF_DAY, 23)
                    .with(ChronoField.MINUTE_OF_HOUR, 30)
                    .with(ChronoField.SECOND_OF_MINUTE, 0);

            Date startTimeInUtc = Date.from(start.withZoneSameInstant(ZoneId.systemDefault()).toInstant());
            log.info("start time in utc : "+startTimeInUtc);
            Date UNAV_startTimeInUtc = Date.from(UNAV_start.withZoneSameInstant(ZoneId.systemDefault()).toInstant());
            Date UNAV_endTimeInUtc = Date.from(UNAV_end.withZoneSameInstant(ZoneId.systemDefault()).toInstant());
            Date endTimeInUtc = Date.from(start.with(LocalTime.MAX).withZoneSameInstant(ZoneId.systemDefault()).toInstant());

            log.info("end time in utc : "+endTimeInUtc);
            List<InstructorUnavailabilityMemberView> instructorUnavailabilityMemberViews = new ArrayList<>();
            boolean isInstructorUnavailableForAWholeDay = false;
            int unavialabilityCount = 0;
            for(PackageKloudlessMapping packageKloudlessMapping : subscriptionPackage.getPackageKloudlessMapping()){
                UserKloudlessCalendar userKloudlessCalendar = packageKloudlessMapping.getUserKloudlessMeeting().getUserKloudlessCalendar();
                if(userKloudlessCalendar != null){
                    isInstructorUnavailableForAWholeDay = instructorUnavailabilityService.checkInstructorUnavailabilityForADay(UNAV_startTimeInUtc,UNAV_endTimeInUtc,userKloudlessCalendar);
                    if (isInstructorUnavailableForAWholeDay) {
                        unavialabilityCount++;
                    }
                    List<InstructorUnavailabilityMemberView> instructorUnavailabilityMemberViewsTemp = instructorUnavailabilityService.constructInstructorUnavailability(UNAV_startTimeInUtc,UNAV_endTimeInUtc,userKloudlessCalendar);
                    instructorUnavailabilityMemberViews.addAll(instructorUnavailabilityMemberViewsTemp);
                }
            }

            if (unavialabilityCount > 0) {
                 isInstructorUnavailableForAWholeDay = true;
            }

            memberPackageScheduleDayView.setInstructorUnavailableForAWholeDay(isInstructorUnavailableForAWholeDay);
            List<Long> memberViewIdList = new ArrayList<>();
            List<InstructorUnavailabilityMemberView> memberViewList = new ArrayList<>();
            for (InstructorUnavailabilityMemberView memberView : instructorUnavailabilityMemberViews) {
                if (!memberViewIdList.contains(memberView.getInstructorUnavailabilityId())) {
                    memberViewIdList.add(memberView.getInstructorUnavailabilityId());
                    memberViewList.add(memberView);
                }
            }

            memberPackageScheduleDayView.setInstructorUnavailabilities(memberViewList);


            boolean isBookingRestrictedForADay = false;
            if(isInstructorUnavailableForAWholeDay){
                isBookingRestrictedForADay = true;
            }
            int bookedSessionsForADay = 0;
            List<MemberPackageSessionView> memberPackageSessionViews = new ArrayList<>();
            int noOfSessionsBookedInADay = 0;

            if(!isBookingRestrictedForADay){
                List<PackageKloudlessMapping> packageKloudlessMappings = packageKloudlessMappingRepository.findBySubscriptionPackage(subscriptionPackage);
                for(PackageKloudlessMapping packageKloudlessMapping : packageKloudlessMappings){

                	
                	  log.info("Query to get cronofy availabilityrules : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
      		          profilingEndTimeMillis = new Date().getTime();
      			  
      		          List<CronofyAvailabilityRules> cronofyAvailabilityRules = cronofyAvailabilityRulesRepository
      	        		   .findByUserKloudlessMeeting(packageKloudlessMapping.getUserKloudlessMeeting());
      			  
      			     log.info("Query to get cronofy availabilityrules : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
      		          profilingEndTimeMillis = new Date().getTime();
      			  
      		       
      		          CronofyMeetingModel cronofyMeetingModel = new CronofyMeetingModel();
                   
      			   
      			     for(CronofyAvailabilityRules cronofyAvailability : cronofyAvailabilityRules){
                	
                	      cronofyMeetingModel.setCronofyavailabilityrulesid(cronofyAvailability.getUserCronofyAvailabilityRulesId());
      		          }
      			   
      			      CronofyAvailabilityRules cronofyAvailabilityRule = cronofyAvailabilityRulesRepository
		                    .findByUserCronofyAvailabilityRulesId(cronofyMeetingModel.getCronofyavailabilityrulesid());
      			      if(cronofyAvailabilityRule.getWeeklyPeriods() == null){
                              continue;
                        }
                	


//                    boolean isMaximumBookingsReachedForWeek = false;
//
//                    if(packageKloudlessMapping.getUserKloudlessMeeting().getMeetingWindow() == null){
//                        continue;
//                    }


                    boolean isBookingRestricted = false;
                    MemberPackageSessionView  memberPackageSessionView = new MemberPackageSessionView();
                    memberPackageSessionView.setUserKloudlessMeetingId(packageKloudlessMapping.getUserKloudlessMeeting().getUserKloudlessMeetingId());
                    memberPackageSessionView.setPackageSessionMappingId(packageKloudlessMapping.getSessionMappingId());
                    memberPackageSessionView.setMeetingTitle(packageKloudlessMapping.getTitle());
                    memberPackageSessionView.setMeetingTypeId(packageKloudlessMapping.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingTypeId());
                    memberPackageSessionView.setMeetingType(packageKloudlessMapping.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingType());
                    memberPackageSessionView.setMeetingId(packageKloudlessMapping.getUserKloudlessMeeting().getMeetingId());
                    
                    Set<String> availableDaysList = new HashSet<>();
                   
                     boolean isAvailableDay = false;
                    
                  //  boolean isAvailableDay = true;
                      JSONArray jsonArray = new JSONArray(cronofyAvailabilityRule.getWeeklyPeriods());
     		        
      		           for (int i = 0; i < jsonArray.length(); i++) {
      		    	   
      		    	    JSONObject jsonObject = jsonArray.getJSONObject(i);
      		    	   
      		    	      String  weeklyDay = jsonObject.getString("day");
      		    	      availableDaysList.add(weeklyDay.trim().substring(0,3).toUpperCase());
              			  
      		    	  }

                      log.info("schedule date in member time zone" + scheduleDate);
                      log.info("schedule date in instructor timezone" + scheduleDate.toInstant().atZone(ZoneId.of(packageKloudlessMapping.getUserKloudlessMeeting().getTimeZone())));
                      DayOfWeek dayOfTheWeek = scheduleDate.toInstant().atZone(userTimeZone.toZoneId()).getDayOfWeek();
                      
                      String scheduleday = dayOfTheWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).substring(0,3).toUpperCase();
                      log.info("day of the week in instructor timezone for a date : "+scheduleDate + " is day :" + scheduleday);
                  
                      if(availableDaysList.contains(scheduleday)){
                        isAvailableDay = true;
                      }
                    
                      List<UserKloudlessSchedule> userKloudlessSchedules = userKloudlessScheduleRepository.findByUserAndPackageKloudlessMappingSessionMappingIdAndBookingDateGreaterThanEqualAndBookingDateLessThanEqual(
                            user,packageKloudlessMapping.getSessionMappingId(),startTimeInUtc,endTimeInUtc);
                      for(UserKloudlessSchedule userKloudlessSchedule : userKloudlessSchedules) {
                   	   
                  	    if(userKloudlessSchedule.getScheduleStartTime() == null) {
                  	    	RefreshAccessTokenResponse refreshAccessTokenResponse = null;
              		        
               		        refreshAccessTokenResponse = cronofyService.refreshAccessToken(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar().getUserKloudlessAccount().getRefreshToken());
               		        
               		        if (refreshAccessTokenResponse == null) {
               			 		throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED,null);
               			    }
               	        
               	       
               	           if(!cronofyService.deleteevent(refreshAccessTokenResponse.getAccessToken(),userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar().getCalendarId(),userKloudlessSchedule.getUserKloudlessScheduleId().toString())){
               	                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_KLOUDLESS_SCHEDULE_DELETE_FAILED, MessageConstants.ERROR);
               	            }
               	        
	               	        if((member_Account_Token.length() > 0) && (member_Calendar_ID.length() > 0 ))
	                        {
	               	        	RefreshAccessTokenResponse member_refreshAccessTokenResponse = null;
	              		        
	               	        	member_refreshAccessTokenResponse = cronofyService.refreshAccessToken(member_Account_Token);
	               		        
	               		        if (member_refreshAccessTokenResponse == null) {
	               			 		throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_ACCOUNT_LINKING_FAILED,null);
	               			    }
	               	        
	               	       
	               	           if(!cronofyService.deleteevent(member_refreshAccessTokenResponse.getAccessToken(),member_Calendar_ID,userKloudlessSchedule.getUserKloudlessScheduleId().toString())){
	               	                throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_KLOUDLESS_SCHEDULE_DELETE_FAILED, MessageConstants.ERROR);
	               	            }
	                        }
               	        
               	           userKloudlessScheduleRepository.delete(userKloudlessSchedule);
                  	    }
                     } 
                      
                      List<UserKloudlessSchedule> userKloudlessSchedulesafterdelete = userKloudlessScheduleRepository.findByUserAndPackageKloudlessMappingSessionMappingIdAndBookingDateGreaterThanEqualAndBookingDateLessThanEqual(
                              user,packageKloudlessMapping.getSessionMappingId(),startTimeInUtc,endTimeInUtc);
                     
                       if(!userKloudlessSchedulesafterdelete.isEmpty()){
                          isBookingRestricted = true;
                       }
                    
                      for(UserKloudlessSchedule userKloudlessSchedule : userKloudlessSchedulesafterdelete) {
                        if (packageKloudlessMapping.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingTypeId() == 3 && userKloudlessSchedule.getMeetingTypeId() != null) {
                            memberPackageSessionView.setSelectedMeetingTypeIdInSchedule(userKloudlessSchedule.getMeetingTypeId().getMeetingTypeId());
                        }
                        boolean isCompleted = false;
                        memberPackageSessionView.setUserKloudlessScheduleId(userKloudlessSchedule.getUserKloudlessScheduleId());
                        
                         
                        
                        CronofyschedulePayload cronofyschedulePayload = new CronofyschedulePayload();
                        
                        cronofyschedulePayload.setAccountId(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar().getUserKloudlessAccount().getAccountId());
                        cronofyschedulePayload.setCalendarId(userKloudlessSchedule.getUserKloudlessMeeting().getUserKloudlessCalendar().getCalendarId());
                                      
                                SchedulePayloadcreator schedulePayloadcreator = new SchedulePayloadcreator();
                                 schedulePayloadcreator.setEmail(user.getEmail());
                                 cronofyschedulePayload.setCreator(schedulePayloadcreator);
                                      
                                SchedulePayloadorganizer schedulePayloadorganizer =new SchedulePayloadorganizer();
                                 schedulePayloadorganizer.setEmail(user.getEmail());
                                 cronofyschedulePayload.setOrganizer(schedulePayloadorganizer); 
                                        
                                 cronofyschedulePayload.setCreated(userKloudlessSchedule.getUserKloudlessMeeting().getStartDateInUtc().toString());
                                 cronofyschedulePayload.setModified(userKloudlessSchedule.getUserKloudlessMeeting().getEndDateInUtc().toString());
                                        
                               
                                 cronofyschedulePayload.setStart(userKloudlessSchedule.getScheduleStartTime());
                                 cronofyschedulePayload.setStartimezone(userKloudlessSchedule.getUserKloudlessMeeting().getTimeZone());

                                 cronofyschedulePayload.setEnd(userKloudlessSchedule.getScheduleEndTime());
                                 cronofyschedulePayload.setEndtimezone(userKloudlessSchedule.getUserKloudlessMeeting().getTimeZone());
                                 
                                 cronofyschedulePayload.setName(packageKloudlessMapping.getTitle());
                                 if(packageKloudlessMapping.getLocation() != null){
                                     cronofyschedulePayload.setLocation(packageKloudlessMapping.getLocation().getCity()+","+packageKloudlessMapping.getLocation().getState()+","+packageKloudlessMapping.getLocation().getCountry().getCountryName());  
                              	}else{
                              		cronofyschedulePayload.setLocation("");                                    	 
                               	}

                               
                                List<SchedulePayloadcustomproperties> payloadcustomproperties = new ArrayList<>();
                                       
                                     SchedulePayloadcustomproperties SPCP_country = new SchedulePayloadcustomproperties();
                                         
                                           SPCP_country.setKey("country");
                                          
                                           if(packageKloudlessMapping.getLocation() != null){
                                        	   SPCP_country.setValue(packageKloudlessMapping.getLocation().getCountry().getCountryName());
                                        	}else{
                                        		SPCP_country.setValue("");                           	 
                                         	}
                                           SPCP_country.setPrivate(true);
                                                
                                          payloadcustomproperties.add(SPCP_country);
                                   
                                     SchedulePayloadcustomproperties SPCP_address = new SchedulePayloadcustomproperties();        
                                   
                                            SPCP_address.setKey("address");
                                            if(packageKloudlessMapping.getLocation() != null){
                                            	SPCP_address.setValue(packageKloudlessMapping.getLocation().getAddress());
                                         	}else{
                                         		SPCP_address.setValue("");                                      	 
                                          	}
                                            SPCP_address.setPrivate(true);
                                        
                                           payloadcustomproperties.add(SPCP_address);
                                  
                                     SchedulePayloadcustomproperties SPCP_fitwiseMeetingId = new SchedulePayloadcustomproperties();        
                                  
                                                 SPCP_fitwiseMeetingId.setKey("fitwiseMeetingId");
                                                 SPCP_fitwiseMeetingId.setValue(packageKloudlessMapping.getUserKloudlessMeeting().getUserKloudlessMeetingId().toString());
                                                 SPCP_fitwiseMeetingId.setPrivate(true);
                                     
                                            payloadcustomproperties.add(SPCP_fitwiseMeetingId);
                               
                                      SchedulePayloadcustomproperties SPCP_city = new SchedulePayloadcustomproperties();        
                               
                                             SPCP_city.setKey("city");
                                             if(packageKloudlessMapping.getLocation() != null){
                                            	 SPCP_city.setValue(packageKloudlessMapping.getLocation().getCity());
                                          	}else{
                                          		SPCP_city.setValue("");                                        	 
                                           	}
                                             SPCP_city.setPrivate(true);
                                  
                                           payloadcustomproperties.add(SPCP_city);
                              
                                           SchedulePayloadcustomproperties SPCP_meetingWindowId = new SchedulePayloadcustomproperties();        
                            
                                             SPCP_meetingWindowId.setKey("meetingWindowId");
                                             SPCP_meetingWindowId.setValue("");
                                             SPCP_meetingWindowId.setPrivate(true);
                                             payloadcustomproperties.add(SPCP_meetingWindowId);
                                       
                                           SchedulePayloadcustomproperties SPCP_sessionName = new SchedulePayloadcustomproperties();        
                                       
                                             SPCP_sessionName.setKey("sessionName");
                                             SPCP_sessionName.setValue(packageKloudlessMapping.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingType());
                                             SPCP_sessionName.setPrivate(true);
                                             payloadcustomproperties.add(SPCP_sessionName );
                                           
                                           SchedulePayloadcustomproperties SPCP_packageId = new SchedulePayloadcustomproperties();        
                                           
                                             SPCP_packageId.setKey("packageId");
                                             SPCP_packageId.setValue(packageKloudlessMapping.getSubscriptionPackage().getSubscriptionPackageId().toString());
                                             SPCP_packageId.setPrivate(true);
                                             payloadcustomproperties.add(SPCP_packageId);
                                           
                                             SchedulePayloadcustomproperties SPCP_fitwiseScheduleId = new SchedulePayloadcustomproperties();        
                                           
                                               SPCP_fitwiseScheduleId.setKey("fitwiseScheduleId");
                                               SPCP_fitwiseScheduleId.setValue(userKloudlessSchedule.getUserKloudlessScheduleId().toString());
                                               SPCP_fitwiseScheduleId.setPrivate(true);
                                               payloadcustomproperties.add(SPCP_fitwiseScheduleId);
                                           
                                             SchedulePayloadcustomproperties SPCP_packageTitle = new SchedulePayloadcustomproperties();        
                                           
                                               SPCP_packageTitle.setKey("packageTitle");
                                               SPCP_packageTitle.setValue(packageKloudlessMapping.getTitle());
                                               SPCP_packageTitle.setPrivate(true);
                                          
                                               payloadcustomproperties.add(SPCP_packageTitle);
                                            
                                             SchedulePayloadcustomproperties SPCP_zipcode = new SchedulePayloadcustomproperties();        
                                           
                                                   SPCP_zipcode.setKey("zipcode");
                                               if(packageKloudlessMapping.getLocation() != null){
                                            	   SPCP_zipcode.setValue(packageKloudlessMapping.getLocation().getZipcode());
                                            	}else{
                                            		SPCP_zipcode.setValue("");                               	 
                                             	}
                                                   SPCP_zipcode.setPrivate(true);
                                          
                                               payloadcustomproperties.add(SPCP_zipcode);
                                           
                                             SchedulePayloadcustomproperties SPCP_sessionType = new SchedulePayloadcustomproperties();        
                                             
                                               SPCP_sessionType.setKey("sessionType");
                                               SPCP_sessionType.setValue(packageKloudlessMapping.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingTypeId().toString());
                                               SPCP_sessionType.setPrivate(true);
                                         
                                               payloadcustomproperties.add(SPCP_sessionType);
                                              
                                              SchedulePayloadcustomproperties SPCP_state = new SchedulePayloadcustomproperties();        
                                              
                                                SPCP_state.setKey("state");
                                               if(packageKloudlessMapping.getLocation() != null){
                                            	   SPCP_state.setValue(packageKloudlessMapping.getLocation().getState());
                                            	}else{
                                            		SPCP_state.setValue("");                              	 
                                             	}
                                                   SPCP_state.setPrivate(true);
                                         
                                              payloadcustomproperties.add(SPCP_state);
                                            
                                              SchedulePayloadcustomproperties SPCP_landmark = new SchedulePayloadcustomproperties();        
                                              
                                              SPCP_landmark.setKey("landmark");
                                                
                                                 if(packageKloudlessMapping.getLocation() != null){
                                                	 if(packageKloudlessMapping.getLocation().getLandMark() != null){
                                                		 SPCP_landmark.setValue(packageKloudlessMapping.getLocation().getLandMark());
                                                  	}else{
                                                  		SPCP_landmark.setValue("");                                       	 
                                                   	}
                                                 }else{
                                                	 SPCP_landmark.setValue("");
                                                 }
                                                 SPCP_landmark.setPrivate(true);
                                         
                                                 payloadcustomproperties.add(SPCP_landmark);
                                            
                                               SchedulePayloadcustomproperties SPCP_sessionNameInPackage = new SchedulePayloadcustomproperties();        
                                               
                                                    SPCP_sessionNameInPackage.setKey("sessionNameInPackage");
                                                    SPCP_sessionNameInPackage.setValue(packageKloudlessMapping.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingType());
                                                    SPCP_sessionNameInPackage.setPrivate(true);
                                         
                                                  payloadcustomproperties.add(SPCP_sessionNameInPackage);
                                              
                                               SchedulePayloadcustomproperties SPCP_isFitwiseEvent = new SchedulePayloadcustomproperties();        
                                               
                                                 SPCP_isFitwiseEvent.setKey("isFitwiseEvent");
                                                 SPCP_isFitwiseEvent.setValue("");
                                                 SPCP_isFitwiseEvent.setPrivate(true);
                                         
                                                  payloadcustomproperties.add(SPCP_isFitwiseEvent);
                                             
                                             
                                               SchedulePayloadcustomproperties SPCP_zoomMeetingLink = new SchedulePayloadcustomproperties();        
                                                  
                                    				SPCP_zoomMeetingLink.setKey("zoomMeetingLink");
                                                  if(packageKloudlessMapping.getMeetingUrl() != null){
                                                  	SPCP_zoomMeetingLink.setValue(packageKloudlessMapping.getMeetingUrl());
                                                  }else{
                                                  	SPCP_zoomMeetingLink.setValue("");
                                                  }
                                                  SPCP_zoomMeetingLink.setPrivate(true);
                                      
                                                  payloadcustomproperties.add(SPCP_zoomMeetingLink);


                                          
                                  cronofyschedulePayload.setCustom_properties(payloadcustomproperties);
                                  Gson gson = new Gson();
                                  memberPackageSessionView.setSchedulePayload(gson.toJson(cronofyschedulePayload));
                                  
                                  //  memberPackageSessionView.setSchedulePayload(userKloudlessSchedule.getSchedulePayload());
                                  memberPackageSessionView.setIsRescheduled(userKloudlessSchedule.getIsRescheduled());
                                  
                                 String now = fitwiseUtils.formatDate(new Date());
                                 Date currentDateInUserTimeZone = fitwiseUtils.constructDate(now);
                                 if (scheduleDate.before(currentDateInUserTimeZone)) {
                                    isCompleted = true;
                                  }
                                 memberPackageSessionView.setCompleted(isCompleted);
                                 noOfSessionsBookedInADay++;
                    }
                    int availableSessions = 0;
                    Date meetingStartDateInUtc = packageKloudlessMapping.getUserKloudlessMeeting().getStartDateInUtc();
                    Date meetingEndDateInUtc = packageKloudlessMapping.getUserKloudlessMeeting().getEndDateInUtc();
                    Date meetingStartDate = fitwiseUtils.convertToUserTimeZone(meetingStartDateInUtc);
                    Date meetingEndDate = fitwiseUtils.convertToUserTimeZone(meetingEndDateInUtc);

                    if(isAvailableDay && (fitwiseUtils.isSameDay(scheduleDate,meetingStartDate) || fitwiseUtils.isSameDay(scheduleDate,meetingEndDate) || (scheduleDate.before(meetingEndDate) && scheduleDate.after(meetingStartDate)))){
                        int bookedSessions = userKloudlessScheduleRepository.countByUserAndPackageKloudlessMappingSessionMappingId(
                                user,packageKloudlessMapping.getSessionMappingId());
                        int totalSessions = packageKloudlessMapping.getTotalSessionCount();
                        availableSessions = totalSessions-bookedSessions;
                        if(availableSessions <= 0){
                            isBookingRestricted = true;
                        }
                    }else{
                        isBookingRestricted = true;
                    }

                    if(isBookingRestricted){
                        bookedSessionsForADay++;
                    }
                    memberPackageSessionView.setBookingRestricted(isBookingRestricted);
                    memberPackageSessionView.setNoOfAvailableSessions(availableSessions);
                    memberPackageSessionViews.add(memberPackageSessionView);
                }
                if(bookedSessionsForADay == packageKloudlessMappings.size()){
                    isBookingRestrictedForADay = true;
                }
            }
            memberPackageScheduleDayView.setSessions(memberPackageSessionViews);
            memberPackageScheduleDayView.setBookingRestrictedForADay(isBookingRestrictedForADay);
            memberPackageScheduleDayView.setNoOfSessionsBookedInADay(noOfSessionsBookedInADay);
            memberPackageScheduleDayViews.add(memberPackageScheduleDayView);

            Calendar cal = Calendar.getInstance();
            cal.setTime(scheduleDate);
            cal.add(Calendar.DATE, 1);
            scheduleDate = cal.getTime();
            order++;

        }
        log.info("Construct member package schedule views : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get package schedules ends.");
        return  new ResponseModel(Constants.SUCCESS_STATUS,MessageConstants.MSG_DATA_RETRIEVED,memberPackageScheduleDayViews);
     }

    public ResponseModel getSubscribedPackagesAndSessions(){
        User user = userComponents.getUser();
        List<PackageSubscription> packageSubscriptions = packageSubscriptionRepository.findByUserUserIdOrderBySubscribedDateDesc(user.getUserId());
        List<PackageSubscription> paidPackageSubscriptions = new ArrayList<>();
        for(PackageSubscription packageSubscription : packageSubscriptions) {
            SubscriptionStatus subscriptionStatus = subscriptionService.getMemberPackageSubscriptionStatus(packageSubscription);
            if (subscriptionStatus != null && (subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID) || subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAYMENT_PENDING))) {
                paidPackageSubscriptions.add(packageSubscription);
            }
        }
        List<MemberCalendarFilterView> memberCalendarFilterViews = new ArrayList<>();

        for(PackageSubscription packageSubscription : paidPackageSubscriptions){
            MemberCalendarFilterView memberCalendarFilterView = new MemberCalendarFilterView();
            List<SessionMemberView> sessions = constructSessionMemberView(user,packageSubscription.getSubscriptionPackage());
            memberCalendarFilterView.setSubscriptionPackageId(packageSubscription.getSubscriptionPackage().getSubscriptionPackageId());
            memberCalendarFilterView.setTitle(packageSubscription.getSubscriptionPackage().getTitle());
            TimeSpan cancellationDuration = packageSubscription.getSubscriptionPackage().getCancellationDuration();
            if (cancellationDuration != null) {
                CancellationDurationModel cancellationDurationModel = new CancellationDurationModel();
                boolean isDays = false;
                if (cancellationDuration.getDays() != null) {
                    isDays = true;
                    cancellationDurationModel.setDays(cancellationDuration.getDays());
                } else {
                    cancellationDurationModel.setHours(cancellationDuration.getHours());
                }
                cancellationDurationModel.setIsDays(isDays);
                memberCalendarFilterView.setCancellationDuration(cancellationDurationModel);
            }
            memberCalendarFilterView.setSessions(sessions);
            memberCalendarFilterViews.add(memberCalendarFilterView);
        }
        Map<String,Object> responseMap = new HashMap<>();
        responseMap.put(KeyConstants.KEY_SUBSCRIPTION_PACKAGES,memberCalendarFilterViews);

        return new ResponseModel(Constants.SUCCESS_STATUS,MessageConstants.MSG_DATA_RETRIEVED,responseMap);
    }

    private List<SessionMemberView> constructSessionMemberView(User user,SubscriptionPackage subscriptionPackage)  {
        List<SessionMemberView> sessions = new ArrayList<>();
        List<PackageKloudlessMapping> packageKloudlessMappings = subscriptionPackage.getPackageKloudlessMapping();
        for(PackageKloudlessMapping packageKloudlessMapping : packageKloudlessMappings){
            SessionMemberView sessionMemberView = new SessionMemberView();
            sessionMemberView.setFitwiseMeetingId(packageKloudlessMapping.getUserKloudlessMeeting().getUserKloudlessMeetingId());
            sessionMemberView.setPackageSessionMappingId(packageKloudlessMapping.getSessionMappingId());
            if(packageKloudlessMapping.getTitle() != null){
                sessionMemberView.setTitle(packageKloudlessMapping.getTitle());
            }
            sessionMemberView.setMeetingTypeId(packageKloudlessMapping.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingTypeId());
            sessionMemberView.setMeetingType(packageKloudlessMapping.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingType());
            sessionMemberView.setDurationMinutes(packageKloudlessMapping.getUserKloudlessMeeting().getDuration().getMinutes());
            sessionMemberView.setOrder(packageKloudlessMapping.getOrder());
            int totalNoOfSessions = packageKloudlessMapping.getTotalSessionCount();
            int bookedSessionCount = userKloudlessScheduleRepository.countByUserAndPackageKloudlessMappingSessionMappingId(
                    user, packageKloudlessMapping.getSessionMappingId());
            int availableSessionCount = totalNoOfSessions - bookedSessionCount;
            sessionMemberView.setTotalNoOfSessions(totalNoOfSessions);
            sessionMemberView.setNoOfBookedSessions(bookedSessionCount);
            sessionMemberView.setNoOfAvailableSessions(availableSessionCount);
           
            Date date = new Date();
          
            int completedSessionCount = 0;
           
            List<UserKloudlessSchedule> userKloudlessSchedules = userKloudlessScheduleRepository.findByUserAndPackageKloudlessMappingSessionMappingId(user
                  ,packageKloudlessMapping.getSessionMappingId());
          //  completedSessionCount = userKloudlessSchedules.size();
            for(UserKloudlessSchedule userKloudlessSchedule : userKloudlessSchedules){
                if(!fitwiseUtils.isSameDay(date,userKloudlessSchedule.getBookingDate()) && userKloudlessSchedule.getBookingDate().before(date)){
                    completedSessionCount++;
                }else if(fitwiseUtils.isSameDay(date,userKloudlessSchedule.getBookingDate())){
                	
                   //  JSONObject jsonObject = new JSONObject(userKloudlessSchedule.getSchedulePayload());
                   
                  // String endTimeString = jsonObject.getString("end");
                	
                if(userKloudlessSchedule.getScheduleEndTime() != null) {
                	
                		
                		String endTimeString = userKloudlessSchedule.getScheduleEndTime();
                		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        Date endTime;
                        try {
                            endTime = dateFormat.parse(endTimeString);
                            if(endTime.before(date)){
                                completedSessionCount++;
                            }
                        } catch (ParseException e) {
                            log.info(MessageConstants.MSG_ERR_EXCEPTION + e.getMessage());
                        }
                }
                	
                	
                    
                }
            }
           
            sessionMemberView.setNoOfCompletedSessions(completedSessionCount);
           
            if(packageKloudlessMapping.getLocation() != null){
                sessionMemberView.setLocation(constructLocationResponse(packageKloudlessMapping.getLocation()));
            }
            sessions.add(sessionMemberView);
        }

        return sessions;
    }

    public MemberLocationResponse constructLocationResponse(Location location){
        MemberLocationResponse locationResponse = new MemberLocationResponse();
        locationResponse.setLocationId(location.getLocationId());
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
        locationResponse.setCountry(location.getCountry().getCountryName());

        return locationResponse;
    }

    /**
     * Construct package view for member
     * @param packageDaos Packages data
     * @param offerCountDaoMap Number of  offers for all the packages
     * @param instructor Instructor to whose package needs to fetch
     * @return Package details view for member discover
     */
    public List<SubscriptionPackageTileViewForDiscover> constructInstructorPackages(List<PackageDao> packageDaos, Map<Long, Long> offerCountDaoMap, User instructor) {
        String instructorName = "";
        String instructorProfileUrl = null;
        if (instructor != null) {
            UserProfile userProfile = userProfileRepository.findByUser(instructor);
            if (userProfile != null) {
                if (userProfile.getProfileImage() != null && userProfile.getProfileImage().getImagePath() != null) {
                    instructorProfileUrl = userProfile.getProfileImage().getImagePath();
                }
                instructorName = fitwiseUtils.constructUserName(userProfile.getFirstName(), userProfile.getLastName());
            }
        }
        List<SubscriptionPackageTileViewForDiscover> subscriptionPackageTileViews = new ArrayList<>();
        for (PackageDao packageDao : packageDaos) {
            SubscriptionPackageTileViewForDiscover subscriptionPackageTileView = new SubscriptionPackageTileViewForDiscover();
            subscriptionPackageTileView.setSubscriptionPackageId(packageDao.getSubscriptionPackageId());
            subscriptionPackageTileView.setImageUrl(packageDao.getImageUrl());
            subscriptionPackageTileView.setDuration(packageDao.getDuration());
            subscriptionPackageTileView.setTitle(packageDao.getTitle());
            subscriptionPackageTileView.setStatus(packageDao.getStatus());
            long numberOfCurrentAvailableOffers = offerCountDaoMap.get(packageDao.getSubscriptionPackageId());
            subscriptionPackageTileView.setNumberOfCurrentAvailableOffers((int) numberOfCurrentAvailableOffers);
            subscriptionPackageTileView.setInstructorName(instructorName);
            subscriptionPackageTileView.setInstructorProfileUrl(instructorProfileUrl);
            subscriptionPackageTileView.setProgramCount(packageDao.getProgramCount());
            subscriptionPackageTileView.setSessionCount(packageDao.getSessionCount());
            if(packageDao.getPrice() != null){
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                subscriptionPackageTileView.setPrice(packageDao.getPrice());
                subscriptionPackageTileView.setPriceFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + " " + decimalFormat.format(packageDao.getPrice()));
            }
            subscriptionPackageTileViews.add(subscriptionPackageTileView);
        }
        return subscriptionPackageTileViews;
    }
}
