package com.fitwise.service.admin;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.fitwise.admin.service.AdminService;
import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.InstructorConstant;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.constants.SecurityFilterConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.subscription.PackageSubscription;
import com.fitwise.entity.subscription.SubscriptionStatus;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.packaging.PackageProgramMappingRepository;
import com.fitwise.repository.packaging.SubscriptionPackageRepository;
import com.fitwise.repository.subscription.PackageSubscriptionRepository;
import com.fitwise.response.AdminListResponse;
import com.fitwise.response.AdminPackageView;
import com.fitwise.response.freeaccess.PackageMinimumDetails;
import com.fitwise.response.packaging.SubscriptionPackageTileView;
import com.fitwise.response.packaging.SubscriptionPackageView;
import com.fitwise.service.SubscriptionService;
import com.fitwise.service.discountservice.DiscountsService;
import com.fitwise.service.instructor.InstructorAnalyticsService;
import com.fitwise.service.packaging.SubscriptionPackageService;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.specifications.PackageSpecification;
import com.fitwise.specifications.jpa.SubscriptionPackageJpa;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.RequestParamValidator;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.admin.MemberPackageHistoryView;
import com.fitwise.view.admin.PackageResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AdminPackageService {

    @Autowired
    private SubscriptionPackageService subscriptionPackageService;

    @Autowired
    private SubscriptionPackageRepository subscriptionPackageRepository;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private FitwiseUtils fitwiseUtils;

    @Autowired
    private InstructorAnalyticsService instructorAnalyticsService;
    @Autowired
    private AdminService adminService;
    @Autowired
    private PackageSubscriptionRepository packageSubscriptionRepository;
    @Autowired
    private PackageProgramMappingRepository packageProgramMappingRepository;

    @Autowired
    private DiscountsService discountsService;

    @Autowired
    private SubscriptionPackageJpa subscriptionPackageJpa;

    /**
     * Gey subscription package details
     * @param subscriptionPackageId
     * @return
     */
    public ResponseModel getSubscriptionPackage(Long subscriptionPackageId) {

        SubscriptionPackage subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageId(subscriptionPackageId);
        ValidationUtils.throwException(subscriptionPackage == null, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_NOT_FOUND, Constants.BAD_REQUEST);

        SubscriptionPackageView subscriptionPackageView = subscriptionPackageService.constructSubscriptionPackageResponse(subscriptionPackage);

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, subscriptionPackageView);
    }

    /**
     * get All packages
     * @param pageNo
     * @param pageSize
     * @param sortBy
     * @param sortOrder
     * @param blockStatus
     * @param search
     * @return
     */
    public ResponseModel getPackages(int pageNo, int pageSize, String sortBy, String sortOrder, String blockStatus, Optional<String> search) {
        log.info("Get packages starts.");
        long apiStartTimeMillis = new Date().getTime();
        RequestParamValidator.pageSetup(pageNo, pageSize);
        List<String> allowedSortByList = Arrays.asList(SearchConstants.TITLE, SearchConstants.INSTRUCTOR_NAME, SearchConstants.PROGRAM_COUNT, SearchConstants.SESSION_COUNT, SearchConstants.ACTIVE_SUBSCRIPTIONS, SearchConstants.PUBLISHED_DATE);
        boolean isSortByAllowed = allowedSortByList.stream().anyMatch(sortBy::equalsIgnoreCase);
        if (!isSortByAllowed) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERROR_SORT_BY, null);
        }
        if (!(sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC) || sortOrder.equalsIgnoreCase(SearchConstants.ORDER_ASC))) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ORDER_ERROR, null);
        }
        if (blockStatus == null || blockStatus.isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_BLOCK_STATUS_PARAM_NULL, null);
        }
        if (!(blockStatus.equalsIgnoreCase(KeyConstants.KEY_ALL) || blockStatus.equalsIgnoreCase(KeyConstants.KEY_OPEN) || blockStatus.equalsIgnoreCase(KeyConstants.KEY_BLOCKED))) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_BLOCK_STATUS_PARAM_INCORRECT, null);
        }
        log.info("Basic validations : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        List<PackageResponse> packageResponses;
        List<SubscriptionPackage> subscriptionPackages;
        List<String> statusList = null;
        if (blockStatus.equalsIgnoreCase(KeyConstants.KEY_ALL)) {
            statusList = Arrays.asList(KeyConstants.KEY_PUBLISH, KeyConstants.KEY_BLOCK, DBConstants.BLOCK_EDIT);
        } else if (blockStatus.equalsIgnoreCase(KeyConstants.KEY_OPEN)) {
            statusList = Arrays.asList(KeyConstants.KEY_PUBLISH);
        } else if (blockStatus.equalsIgnoreCase(KeyConstants.KEY_BLOCKED)) {
            statusList = Arrays.asList(KeyConstants.KEY_BLOCK, DBConstants.BLOCK_EDIT);
        }
        if (search.isPresent() && !search.get().isEmpty()) {
            subscriptionPackages = subscriptionPackageRepository.findByStatusInAndTitleIgnoreCaseContaining(statusList, search.get());
        } else {
            subscriptionPackages = subscriptionPackageRepository.findByStatusIn(statusList);
        }
        log.info("Query to get subscription packages : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        packageResponses = getPackageResponses(subscriptionPackages);
        log.info("Construct package responses : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if (blockStatus.equalsIgnoreCase(KeyConstants.KEY_BLOCKED)) {
            packageResponses = packageResponses.stream().filter(PackageResponse::isBlocked).collect(Collectors.toList());
        } else if (blockStatus.equalsIgnoreCase(KeyConstants.KEY_OPEN)) {
            packageResponses = packageResponses.stream().filter(response -> !response.isBlocked()).collect(Collectors.toList());
        }
        log.info("Filter based on block status : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if (packageResponses == null || packageResponses.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        AdminListResponse<PackageResponse> adminListResponse = new AdminListResponse<>();
        adminListResponse.setTotalSizeOfList(packageResponses.size());
        adminListResponse.setPayloadOfAdmin(getPackageResponsePagination(packageResponses, pageNo, pageSize, sortBy, sortOrder));
        log.info("Get package response pagination : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get packages ends.");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, adminListResponse);
    }

    private List<PackageResponse> getPackageResponsePagination(List<PackageResponse> packageResponses, int pageNo, int pageSize, String sortBy, String sortOrder) {
        int fromIndex = (pageNo - 1) * pageSize;
        if (packageResponses == null || packageResponses.size() < fromIndex) {
            return Collections.emptyList();
        }
        List<PackageResponse> sortedPackageResponses = comparePackage(packageResponses, sortBy);
        if (sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC)) {
            Collections.reverse(sortedPackageResponses);
        }
        return sortedPackageResponses.subList(fromIndex, Math.min(fromIndex + pageSize, packageResponses.size()));
    }

    private List<PackageResponse> comparePackage(List<PackageResponse> packageResponses, String sortBy) {
        if (sortBy.equalsIgnoreCase(SearchConstants.TITLE)) {
            return packageResponses.stream().sorted(Comparator.comparing(PackageResponse::getTitle, String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList());
        } else if (sortBy.equalsIgnoreCase(SearchConstants.INSTRUCTOR_NAME) || sortBy.equalsIgnoreCase(SecurityFilterConstants.ROLE_INSTRUCTOR)) {
            return packageResponses.stream().sorted(Comparator.comparing(PackageResponse::getInstructorName, String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList());
        } else if (sortBy.equalsIgnoreCase(SearchConstants.ACTIVE_SUBSCRIPTIONS)) {
            return packageResponses.stream().sorted(Comparator.comparing(PackageResponse::getActiveSubscriptions).thenComparing(PackageResponse::getTitle, String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList());
        } else if (sortBy.equalsIgnoreCase(SearchConstants.PROGRAM_COUNT)) {
            return packageResponses.stream().sorted(Comparator.comparing(PackageResponse::getProgramCount).thenComparing(PackageResponse::getTitle, String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList());
        } else if (sortBy.equalsIgnoreCase(SearchConstants.SESSION_COUNT)) {
            return packageResponses.stream().sorted(Comparator.comparing(PackageResponse::getSessionCount)).collect(Collectors.toList());
        } else if (sortBy.equalsIgnoreCase(SearchConstants.PUBLISHED_DATE)) {
            return packageResponses.stream().sorted(Comparator.comparing(PackageResponse::getPublishedDate)).collect(Collectors.toList());
        }
        return packageResponses;
    }

    private List<PackageResponse> getPackageResponses(List<SubscriptionPackage> subscriptionPackages) {
        List<PackageResponse> packageResponses = new ArrayList<>();
        for (SubscriptionPackage subscriptionPackage : subscriptionPackages) {
            PackageResponse packageResponse = new PackageResponse();
            packageResponse.setSubscriptionPackageId(subscriptionPackage.getSubscriptionPackageId());
            packageResponse.setTitle(subscriptionPackage.getTitle());
            UserProfile userProfile = userProfileRepository.findByUserUserId(subscriptionPackage.getOwner().getUserId());
            packageResponse.setInstructorName(userProfile.getFirstName() + KeyConstants.KEY_SPACE + userProfile.getLastName());
            int programCount = 0;
            if(subscriptionPackage.getPackageProgramMapping() != null && !subscriptionPackage.getPackageProgramMapping().isEmpty()){
                programCount = subscriptionPackage.getPackageProgramMapping().size();
            }
            packageResponse.setProgramCount(programCount);
            packageResponse.setSessionCount(subscriptionPackageJpa.getBookableSessionCountForPackage(subscriptionPackage.getSubscriptionPackageId()));
            int activeSubscriptions = (int) subscriptionService.getActiveSubscriptionCountForPackage(subscriptionPackage.getSubscriptionPackageId());
            packageResponse.setActiveSubscriptions(activeSubscriptions);
            if (subscriptionPackage.getPublishedDate() != null) {
                packageResponse.setPublishedDate(subscriptionPackage.getPublishedDate());
                packageResponse.setPublishedDateFormatted(fitwiseUtils.formatDate(subscriptionPackage.getPublishedDate()));
            }
            boolean isBlocked = false;
            if (InstructorConstant.BLOCK.equalsIgnoreCase(subscriptionPackage.getStatus()) || DBConstants.BLOCK_EDIT.equalsIgnoreCase(subscriptionPackage.getStatus())) {
                isBlocked = true;
            }
            packageResponse.setBlocked(isBlocked);
            packageResponses.add(packageResponse);
        }
        return packageResponses;
    }

    /**
     * Get instructor packages list
     * @param pageNo
     * @param pageSize
     * @param instructorId
     * @return
     */
    public ResponseModel getInstructorPackages(int pageNo, int pageSize, Long instructorId) {
        log.info("Get instructor packages starts");
        long start = new Date().getTime();
        long profilingStart;
        long profilingEnd;
        User user = validationService.validateInstructorId(instructorId);

        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ERROR, null);
        }

        profilingStart = new Date().getTime();
        Sort sort = Sort.by("modifiedDate");
        sort = sort.descending();
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, sort);

        Specification<SubscriptionPackage> ownerSpec = PackageSpecification.getPackageByOwnerUserId(instructorId);
        Specification<SubscriptionPackage> stripeActiveSpec = PackageSpecification.getPackageByStripeMapping();
        Specification<SubscriptionPackage> statusSpec = PackageSpecification.getSubscriptionPackageByStatus(InstructorConstant.PUBLISH);

        Specification<SubscriptionPackage> finalSpec = statusSpec.and(ownerSpec).and(stripeActiveSpec);

        Page<SubscriptionPackage> subscriptionPackages = subscriptionPackageRepository.findAll(finalSpec, pageRequest);
        profilingEnd = new Date().getTime();
        log.info("Query : Time taken in millis : "+(profilingEnd-profilingStart));

        if (subscriptionPackages.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }

        //Getting active offer counts for list of subscription packages
        List<Long> subscriptionPackageIdList = subscriptionPackages.stream().map(SubscriptionPackage::getSubscriptionPackageId).collect(Collectors.toList());
        Map<Long, Long> offerCountMap = discountsService.getNoOfCurrentAvailableOffersForInstructorPackages(subscriptionPackageIdList);

        List<SubscriptionPackageTileView> subscriptionPackageTileViews = new ArrayList<>();
        profilingStart = new Date().getTime();
        for (SubscriptionPackage subscriptionPackage : subscriptionPackages) {
            subscriptionPackageTileViews.add(subscriptionPackageService.constructPackageTileModel(subscriptionPackage, offerCountMap));
        }
        profilingEnd = new Date().getTime();
        log.info("Response construction : Time taken in millis : "+(profilingEnd-profilingStart));

        AdminPackageView adminPackageView = new AdminPackageView();
        adminPackageView.setSubscriptionPackages(subscriptionPackageTileViews);
        adminPackageView.setTotalPackageCount(subscriptionPackages.getTotalElements());
        profilingStart = new Date().getTime();
        adminPackageView.setTotalSubscriptions((int) subscriptionService.getPaidSubscribedPackagesOfAnInstructor(user.getUserId()));
        profilingEnd = new Date().getTime();
        log.info("Get active subscriptions of an instructor : Time taken in millis : "+(profilingEnd-profilingStart));

        List<String> subscriptionTypeList = Arrays.asList(KeyConstants.KEY_SUBSCRIPTION_PACKAGE);
        profilingStart = new Date().getTime();
        double netRevenue = instructorAnalyticsService.calculateNetRevenueOfAnInstructor(user.getUserId(), subscriptionTypeList);
        profilingEnd = new Date().getTime();
        log.info("Get revenue of an instructor : Time taken in millis : "+(profilingEnd-profilingStart));

        adminPackageView.setNetRevenue(netRevenue);
        DecimalFormat df = new DecimalFormat("0.00");
        adminPackageView.setNetRevenueFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + df.format(netRevenue));
        profilingEnd = new Date().getTime();
        log.info("Get instructor packages : Total Time taken in millis : "+(profilingEnd-start));
        log.info("Get Instructor packages ends");

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, adminPackageView);

    }

    /**
     * Method to get the package subscription history of a member
     * @param memberId
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Object getMemberPackageHistory(Long memberId, int pageNo, int pageSize) {
        log.info("Get member package history starts.");
        long apiStartTimeMillis = new Date().getTime();
        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ERROR, null);
        }
        User member = validationService.validateMemberId(memberId);

        List<String> statusList = Arrays.asList(KeyConstants.KEY_PAID, KeyConstants.KEY_EXPIRED);
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize);
        Page<PackageSubscription> packageSubscriptionPage = packageSubscriptionRepository.findByUserUserIdAndSubscriptionStatusSubscriptionStatusNameIn(memberId, statusList, pageRequest);
        log.info("Query to get user and package subscription : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();

        List<MemberPackageHistoryView> packageList = new ArrayList<>();
        for (PackageSubscription packageSubscription : packageSubscriptionPage) {
            MemberPackageHistoryView memberProgramTileView = new MemberPackageHistoryView();

            SubscriptionPackage subscriptionPackage = packageSubscription.getSubscriptionPackage();

            memberProgramTileView.setSubscriptionPackageId(subscriptionPackage.getSubscriptionPackageId());
            memberProgramTileView.setTitle(subscriptionPackage.getTitle());
            memberProgramTileView.setDuration(subscriptionPackage.getPackageDuration().getDuration() + KeyConstants.KEY_DAYS);
            if (subscriptionPackage.getImage() != null) {
                memberProgramTileView.setImageUrl(subscriptionPackage.getImage().getImagePath());
            }

            Date subscribedDate = packageSubscription.getSubscribedDate();
            memberProgramTileView.setSubscribedDate(subscribedDate);
            memberProgramTileView.setSubscribedDateFormatted(fitwiseUtils.formatDate(subscribedDate));

            Date initialStartDate = packageSubscription.getCreatedDate();
            memberProgramTileView.setInitialStartDate(initialStartDate);
            memberProgramTileView.setInitialStartDateFormatted(fitwiseUtils.formatDate(initialStartDate));

            int programCount = packageProgramMappingRepository.countBySubscriptionPackage(packageSubscription.getSubscriptionPackage());
            memberProgramTileView.setNoOfPrograms(programCount);

            memberProgramTileView.setSessionCount(subscriptionPackageJpa.getBookableSessionCountForPackage(packageSubscription.getSubscriptionPackage().getSubscriptionPackageId()));

            //Setting subscription status of the package
            SubscriptionStatus subscriptionStatus = subscriptionService.getMemberPackageSubscriptionStatus(packageSubscription);
            if (subscriptionStatus != null) {
                if (KeyConstants.KEY_PAID.equals(subscriptionStatus.getSubscriptionStatusName())) {
                    // Program is subscribed
                    memberProgramTileView.setSubscriptionStatus(KeyConstants.KEY_SUBSCRIBED);
                } else {
                    memberProgramTileView.setSubscriptionStatus(subscriptionStatus.getSubscriptionStatusName());
                }
            }
            packageList.add(memberProgramTileView);
        }
        log.info("Construct member program tile view : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        if (packageSubscriptionPage.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }

        Map<String, Object> memberDetails = new HashMap<>();

        memberDetails.put(KeyConstants.KEY_SUBSCRIPTION_PACKAGES, packageList);
        memberDetails.put(KeyConstants.KEY_TOTAL_COUNT, packageSubscriptionPage.getTotalElements());


        List<PackageSubscription> paidPackageSubscriptions = subscriptionService.getPaidPackageSubscriptionsByAnUser(memberId);
        memberDetails.put(KeyConstants.KEY_TOTAL_SUBSCRIPTIONS, paidPackageSubscriptions.size());

        List<String> subscriptionTypeList = Arrays.asList(KeyConstants.KEY_SUBSCRIPTION_PACKAGE);
        double amountSpentByMember = adminService.getAmountSpentByMember(member, subscriptionTypeList);

        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        memberDetails.put(KeyConstants.KEY_AMOUNT_SPENT_BY_MEMBER, KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(amountSpentByMember));
        log.info("Query to get paid package subscription and amount spent by member : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get member package history ends.");

        return memberDetails;

    }
    
	/**
	 * Minimum package details.
	 *
	 * @param pageNo the page no
	 * @param pageSize the page size
	 * @param search the search
	 * @return the response model
	 */
	public ResponseModel minimumPackageDetails(final int pageNo, final int pageSize, String search) {
		ResponseModel response = new ResponseModel();
		RequestParamValidator.pageSetup(pageNo, pageSize);
		try {
			List<PackageMinimumDetails> responseList = new ArrayList<>();
			DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
			PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize);
			Specification<SubscriptionPackage> packageSpec = PackageSpecification.getSubscriptionPackageByTitle(search);
			Specification<SubscriptionPackage> packageStatusSpec = PackageSpecification
					.getSubscriptionPackageByStatus(KeyConstants.KEY_PUBLISH);
			Specification<SubscriptionPackage> nonRestrictedPackage = PackageSpecification.getNonRestrictedPackages();
			Specification<SubscriptionPackage> finalSpec = packageSpec.and(packageStatusSpec).and(nonRestrictedPackage);
			Page<SubscriptionPackage> packagePageRequest = subscriptionPackageRepository.findAll(finalSpec,
					pageRequest);
			for (SubscriptionPackage programPackage : packagePageRequest.getContent()) {
				UserProfile userProfile = userProfileRepository.findByUser(programPackage.getOwner());
				PackageMinimumDetails searchProgramView = new PackageMinimumDetails();
				searchProgramView.setSubscriptionPackageId(programPackage.getSubscriptionPackageId());
				searchProgramView.setTitle(programPackage.getTitle());
				searchProgramView.setPrice(String.valueOf(programPackage.getPrice()));
				searchProgramView.setFormattedPrice(
						KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(programPackage.getPrice()));
				if (userProfile != null) {
					searchProgramView.setInstructorName(userProfile.getFirstName() + " " + userProfile.getLastName());
				}
				responseList.add(searchProgramView);
			}
			AdminListResponse<PackageMinimumDetails> res = new AdminListResponse<>();
			res.setTotalSizeOfList(packagePageRequest.getTotalElements());
			res.setPayloadOfAdmin(responseList);
			response.setStatus(Constants.SUCCESS_STATUS);
			response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
			response.setPayload(res);
		} catch (Exception exception) {
			log.error(exception.getMessage());
			response.setStatus(Constants.ERROR_STATUS);
			response.setMessage(MessageConstants.ERROR_INTERNAL_SERVER_FAILURE);
		}
		return response;
	}

}
