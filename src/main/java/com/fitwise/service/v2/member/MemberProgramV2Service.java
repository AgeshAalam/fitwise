package com.fitwise.service.v2.member;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.fitwise.constants.*;
import com.fitwise.entity.*;
import com.fitwise.entity.instructor.InstructorTierDetails;
import com.fitwise.repository.*;
import com.fitwise.repository.instructor.InstructorTierDetailsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.discounts.DiscountsConstants;
import com.fitwise.entity.discounts.DiscountOfferMapping;
import com.fitwise.entity.packaging.PackageExternalClientMapping;
import com.fitwise.entity.packaging.PackageMemberMapping;
import com.fitwise.entity.packaging.PackageProgramMapping;
import com.fitwise.entity.payments.appleiap.AppleProductSubscription;
import com.fitwise.entity.payments.authNet.AuthNetArbSubscription;
import com.fitwise.entity.payments.authNet.AuthNetPayment;
import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.payments.stripe.StripeSubscriptionAndUserPackageMapping;
import com.fitwise.entity.payments.stripe.billing.StripePayment;
import com.fitwise.entity.payments.stripe.billing.StripeSubscriptionAndUserProgramMapping;
import com.fitwise.entity.subscription.PackageProgramSubscription;
import com.fitwise.entity.subscription.ProgramSubscription;
import com.fitwise.entity.subscription.SubscriptionStatus;
import com.fitwise.exception.ApplicationException;
import com.fitwise.program.model.ProgramPlatformPriceResponseModel;
import com.fitwise.program.model.ProgramTypeWithProgramTileModel;
import com.fitwise.repository.discountsRepository.DiscountOfferMappingRepository;
import com.fitwise.repository.member.WorkoutCompletionRepository;
import com.fitwise.repository.order.OrderManagementRepository;
import com.fitwise.repository.packaging.PackageExternalClientMappingRepository;
import com.fitwise.repository.packaging.PackageMemberMappingRepository;
import com.fitwise.repository.packaging.PackageProgramMappingRepository;
import com.fitwise.repository.payments.appleiap.AppleProductSubscriptionRepository;
import com.fitwise.repository.payments.authnet.AuthNetArbSubscriptionRepository;
import com.fitwise.repository.payments.authnet.AuthNetPaymentRepository;
import com.fitwise.repository.payments.stripe.StripeSubscriptionAndUserPackageMappingRepository;
import com.fitwise.repository.payments.stripe.billing.StripePaymentRepository;
import com.fitwise.repository.payments.stripe.billing.StripeSubscriptionAndUserProgramMappingRepository;
import com.fitwise.repository.subscription.PackageProgramSubscriptionRepository;
import com.fitwise.repository.subscription.ProgramSubscriptionRepo;
import com.fitwise.service.SubscriptionService;
import com.fitwise.service.admin.FitwiseShareService;
import com.fitwise.service.discountservice.DiscountsService;
import com.fitwise.service.freeproduct.FreeAccessService;
import com.fitwise.service.member.MemberProgramService;
import com.fitwise.specifications.ProgramSpecifications;
import com.fitwise.utils.Convertions;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.utils.parsing.ProgramDataParsing;
import com.fitwise.view.SubscriptionPackagePackageIdAndTitleView;
import com.fitwise.view.discounts.ProgramDiscountMappingListResponseView;
import com.fitwise.view.discounts.ProgramDiscountMappingResponseView;
import com.fitwise.view.member.MemberProgramDetailResponseView;
import com.fitwise.view.member.MemberWorkoutScheduleResponseView;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * Created by Vignesh.G on 19/05/21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MemberProgramV2Service {

    private final ProgramRepository programRepository;
    private final FeaturedProgramsRepository featuredProgramsRepository;
    private final ProgramDataParsing programDataParsing;
    private final UserComponents userComponents;
    private final FitwiseUtils fitwiseUtils;
    private final UserProfileRepository userProfileRepository;
    private final PackageProgramMappingRepository packageProgramMappingRepository;
    private final PackageMemberMappingRepository packageMemberMappingRepository;
    private final PackageExternalClientMappingRepository packageExternalClientMappingRepository;
    private final MemberProgramService memberProgramService;
    private final ProgramSubscriptionRepo programSubscriptionRepo;
    private final PackageProgramSubscriptionRepository packageProgramSubscriptionRepository;
    private final SubscriptionService subscriptionService;
    private final OrderManagementRepository orderManagementRepository;
    private final WorkoutCompletionRepository workoutCompletionRepository;
    private final AppleProductSubscriptionRepository appleProductSubscriptionRepository;
    private final AuthNetArbSubscriptionRepository authNetArbSubscriptionRepository;
    private final AuthNetPaymentRepository authNetPaymentRepository;
    private final StripeSubscriptionAndUserProgramMappingRepository stripeSubscriptionAndUserProgramMappingRepository;
    private final StripePaymentRepository stripePaymentRepository;
    private final StripeSubscriptionAndUserPackageMappingRepository stripeSubscriptionAndUserPackageMappingRepository;
    private final WorkoutScheduleRepository workoutScheduleRepository;
    private final RestActivityRepository restActivityRepository;
    private final ProgramViewsAuditRepository programViewsAuditRepository;
    private final DiscountOfferMappingRepository discountOfferMappingRepository;
    private final DiscountsService discountsService;
    private final FeaturedProgramTypeAndProgramsRepository featuredProgramTypeAndProgramsRepository;
    private final FitwiseShareService fitwiseShareService;
    private final FreeAccessService freeAccessService;
    private final InstructorTierDetailsRepository instructorTierDetailsRepository;
    private final AppConfigKeyValueRepository appConfigKeyValueRepository;

    public Map<String, Object> getTrendingPrograms(int pageNo, int pageSize, boolean isFeatured, List<Long> durationIdList, Optional<String> search) {
        log.info("Member Trending programs starts.");
        long apiStartTimeMillis = new Date().getTime();
        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ERROR, null);
        }
        long profilingStartTimeMillis = new Date().getTime();
        /**
         * Query Criteria for featured programs
         */
        List<Programs> programs;
        long totalCount;
        if (isFeatured) {
            List<FeaturedPrograms> featuredProgramsList = featuredProgramsRepository.findByProgramStatusOrderById(KeyConstants.KEY_PUBLISH);
            if (featuredProgramsList.isEmpty()) {
                throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, ValidationMessageConstants.MSG_FEATURED_PROGRAM_NOT_FOUND, null);
            }
            programs = featuredProgramsList.stream().map(FeaturedPrograms::getProgram).collect(Collectors.toList());
            totalCount = featuredProgramsList.size();
        }else{
            PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize);
            Specification<Programs> programStatusSpec = ProgramSpecifications.getProgramByStatus(KeyConstants.KEY_PUBLISH);
            Specification<Programs> stripeActiveSpec = ProgramSpecifications.getProgramByStripeMapping();
            Specification<Programs> finalSpec = programStatusSpec.and(stripeActiveSpec);
            if (search.isPresent() && !search.get().isEmpty()) {
                Specification<Programs> titleSearchSpec = ProgramSpecifications.getProgramByTitleContains(search.get());
                finalSpec = finalSpec.and(titleSearchSpec);
            }
            /**
             * Query Criteria for filter by duration
             */
            if (durationIdList != null && !durationIdList.isEmpty()) {
                Specification<Programs> durationSpec = ProgramSpecifications.getProgramByDurationIn(durationIdList);
                finalSpec = finalSpec.and(durationSpec);
            }
            Page<Programs> programPage = programRepository.findAll(finalSpec, pageRequest);
            programs = programPage.getContent();
            totalCount = programPage.getTotalElements();
        }
        /**
         * Query Criteria for search by program title
         */
        long profilingEndTimeMillis = new Date().getTime();
        log.info("Query : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        if (programs.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, null);
        }
        User user = userComponents.getAndValidateUser();
        profilingStartTimeMillis = new Date().getTime();
        List<Long> programIdList = programs.stream().map(Programs::getProgramId).collect(Collectors.toList());
        Map<Long, Long> offerCountMap = discountsService.getNoOfCurrentAvailableOffersOfProgramsForMember(programIdList, user);
        log.info("Offer count query : time taken in millis : "+(new Date().getTime() - profilingStartTimeMillis));
        Map<String, Object> trendingPrograms = new HashMap<>();
        trendingPrograms.put(KeyConstants.KEY_PROGRAMS, programDataParsing.constructProgramTileModelWithFreeAccess(programs, offerCountMap));
        log.info("Response construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        trendingPrograms.put(KeyConstants.KEY_TOTAL_COUNT, totalCount);
        long apiEndTimeMillis = new Date().getTime();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("Member Trending programs ends.");
        return trendingPrograms;
    }

    @Transactional
    public MemberProgramDetailResponseView getProgramDetails(Long programId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ParseException {
        log.info("Get program details starts");
        long apiStartTimeMillis = new Date().getTime();
        if (programId == null || programId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_NULL, MessageConstants.ERROR);
        }
        Programs program = programRepository.findByProgramId(programId);
        if (program == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, MessageConstants.ERROR);
        }
        List<String> programStatusList = Arrays.asList(InstructorConstant.PLAN, InstructorConstant.UPLOAD, InstructorConstant.SCHEDULE, InstructorConstant.PRICE, InstructorConstant.PRE_PUBLISH, DBConstants.BLOCK_EDIT, DBConstants.UNPUBLISH_EDIT);
        boolean isAccessRestricted = programStatusList.stream().anyMatch(program.getStatus()::equalsIgnoreCase);
        if (isAccessRestricted) {
            throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_MEMBER_PROGRAM_NOT_AVAILABLE, MessageConstants.ERROR);
        }
        log.info("Basic Validations with get program query : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        User user = userComponents.getAndValidateUser();
        log.info("Get user component : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        MemberProgramDetailResponseView memberProgramDetailResponseView = new MemberProgramDetailResponseView();
        memberProgramDetailResponseView.setProgramId(programId);
		Set<Long> freeAccessProgramIds = freeAccessService.getFreeProductProgramsIds();
		if (!freeAccessProgramIds.isEmpty() && freeAccessProgramIds.contains(programId)) {
			memberProgramDetailResponseView.setFreeToAccess(true);
		}
        boolean isMemberBlocked = false;
        if (user != null) {
            memberProgramDetailResponseView.setIsUserBlocked(fitwiseUtils.isUserBlocked(user));
        }
        if (program.getPromotion() != null && program.getPromotion().getVideoManagement() != null && !ValidationUtils.isEmptyString(program.getPromotion().getVideoManagement().getUrl())) {
            memberProgramDetailResponseView.setProgramPromoVideoId(program.getPromotion().getVideoManagement().getUrl());
            memberProgramDetailResponseView.setPromotionUploadStatus(program.getPromotion().getVideoManagement().getUploadStatus());
        }
        if (program.getImage() != null && !ValidationUtils.isEmptyString(program.getImage().getImagePath()))
            memberProgramDetailResponseView.setProgramThumbnail(program.getImage().getImagePath());
        memberProgramDetailResponseView.setProgramTitle(program.getTitle());
        memberProgramDetailResponseView.setShortDescription(program.getShortDescription());
        memberProgramDetailResponseView.setProgramDescription(program.getDescription());
        //Field to allow subscription
        List<String> subscriptionRestrictedStatusList = Arrays.asList(InstructorConstant.BLOCK, InstructorConstant.UNPUBLISH);
        boolean isProgramBlockOrUnpublished = subscriptionRestrictedStatusList.stream().anyMatch(program.getStatus()::equalsIgnoreCase);
        //Program in unpublish/block status not available for guest users. Not sending error msg since mobile team handled only for empty msg.
        if (user == null && isProgramBlockOrUnpublished) {
            throw new ApplicationException(Constants.NOT_FOUND, null, MessageConstants.ERROR);
        }
        memberProgramDetailResponseView.setIsSubscriptionRestricted(isProgramBlockOrUnpublished);
        //Setting Instructor Name to the response
        memberProgramDetailResponseView.setInstructorId(program.getOwner().getUserId());
        UserProfile instructor = userProfileRepository.findByUser(program.getOwner());
        if (instructor != null) {
            memberProgramDetailResponseView.setInstructorFirstName(instructor.getFirstName());
            memberProgramDetailResponseView.setInstructorLastName(instructor.getLastName());
            if (instructor.getProfileImage() != null && !ValidationUtils.isEmptyString(instructor.getProfileImage().getImagePath()))
                memberProgramDetailResponseView.setInstructorProfileImageUrl(instructor.getProfileImage().getImagePath());
        }
        log.info("Query to user profile and setting member program details response view : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        // Get & set Flat tax amount
        long profilingStartTimeMillis = new Date().getTime();
        AppConfigKeyValue appConfigKeyValue = appConfigKeyValueRepository.findByKeyString(AppConfigConstants.ADMIN_CONFIG_FLAT_TAX);
        if(appConfigKeyValue != null && appConfigKeyValue.getValueString() != null){
            double flatTaxAmount = Double.parseDouble(appConfigKeyValue.getValueString());
            memberProgramDetailResponseView.setFlatTax(flatTaxAmount);
        }
        profilingEndTimeMillis = new Date().getTime();
        log.info("Flat tax details : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        if (program.getProgramPrice() != null && !memberProgramDetailResponseView.isFreeToAccess()) {
            DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
            String price = KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(program.getProgramPrice());
            memberProgramDetailResponseView.setProgramPrice(decimalFormat.format(program.getProgramPrice()));
            memberProgramDetailResponseView.setFormattedProgramPrice(price);
            List<ProgramPlatformPriceResponseModel> programPlatformPriceModels = new ArrayList<>();
            InstructorTierDetails instructorTierDetails = instructorTierDetailsRepository.findByUserAndActive(program.getOwner(), true);
            Double trainnrTax = 15.0;
            if (instructorTierDetails != null) {
                trainnrTax = instructorTierDetails.getTier().getTierTypeDetails().getProgramsFees();
            }
            for (ProgramPriceByPlatform programPriceByPlatform : program.getProgramPriceByPlatforms()) {
                ProgramPlatformPriceResponseModel programPlatformPriceResponseModel = new ProgramPlatformPriceResponseModel();
                programPlatformPriceResponseModel.setProgramPriceByPlatformId(programPriceByPlatform.getProgramPriceByPlatformId());
                programPlatformPriceResponseModel.setPlatformWiseTaxDetailId(programPriceByPlatform.getPlatformWiseTaxDetail().getPlatformWiseTaxDetailId());
                programPlatformPriceResponseModel.setPlatform(programPriceByPlatform.getPlatformWiseTaxDetail().getPlatformType().getPlatform());
                programPlatformPriceResponseModel.setPlatformId(programPriceByPlatform.getPlatformWiseTaxDetail().getPlatformType().getPlatformTypeId());
                programPlatformPriceResponseModel.setPrice(programPriceByPlatform.getPrice());
                programPlatformPriceResponseModel.setAppStoreTax(programPriceByPlatform.getPlatformWiseTaxDetail().getAppStoreTaxPercentage());
                programPlatformPriceResponseModel.setTrainnrTax(trainnrTax);
                programPlatformPriceResponseModel.setGeneralTax(programPriceByPlatform.getPlatformWiseTaxDetail().getGeneralTaxPercentage());
                if(appConfigKeyValue != null && appConfigKeyValue.getValueString() != null){
                    double flatTaxAmount = Double.parseDouble(appConfigKeyValue.getValueString());
                    programPlatformPriceResponseModel.setFlatTax(flatTaxAmount);
                }
                programPlatformPriceModels.add(programPlatformPriceResponseModel);
            }
            memberProgramDetailResponseView.setProgramPlatformPriceResponseModels(programPlatformPriceModels);
        }
        log.info("Setting platform related details : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        boolean isTrialOrSubscribed = false;
        //To check whether the program is auto-subscribed.
        boolean programAutoSubscribed = false;
        //To check whether the program is subscribed.
        boolean isProgramSubscribed = false;
        boolean isTrialSubscription = false;
        boolean isNewSubscription = true;
        boolean isViaProgramSubscription = false;
        boolean isViaPackageSubscription = false;
        Date expiryDate = null;
        Date subscribedDate = null;
        String subscriptionValidity = null;
        List<SubscriptionPackagePackageIdAndTitleView> associatedPackages = new ArrayList<>();
        List<PackageProgramMapping> packageProgramMappingList = packageProgramMappingRepository.findByProgramAndSubscriptionPackageStatus(program, KeyConstants.KEY_PUBLISH);
        log.info("Query: package program subscription : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if (!packageProgramMappingList.isEmpty()) {
            for (PackageProgramMapping packageProgramMapping : packageProgramMappingList) {
                if (!packageProgramMapping.getSubscriptionPackage().isRestrictedAccess()) {
                    SubscriptionPackagePackageIdAndTitleView associatedPackageView = new SubscriptionPackagePackageIdAndTitleView();
                    associatedPackageView.setSubscriptionPackageId(packageProgramMapping.getSubscriptionPackage().getSubscriptionPackageId());
                    associatedPackageView.setTitle(packageProgramMapping.getSubscriptionPackage().getTitle());
                    associatedPackages.add(associatedPackageView);
                } else {
                    if (user != null) {
                        PackageMemberMapping packageMemberMapping = packageMemberMappingRepository.findTop1BySubscriptionPackageSubscriptionPackageIdAndUserUserId(packageProgramMapping.getSubscriptionPackage().getSubscriptionPackageId(), user.getUserId());
                        PackageExternalClientMapping packageExternalClientMapping = packageExternalClientMappingRepository.findTop1BySubscriptionPackageSubscriptionPackageIdAndExternalClientEmail(packageProgramMapping.getSubscriptionPackage().getSubscriptionPackageId(), user.getEmail());
                        if (packageMemberMapping != null || packageExternalClientMapping != null) {
                            SubscriptionPackagePackageIdAndTitleView associatedPackageView = new SubscriptionPackagePackageIdAndTitleView();
                            associatedPackageView.setSubscriptionPackageId(packageProgramMapping.getSubscriptionPackage().getSubscriptionPackageId());
                            associatedPackageView.setTitle(packageProgramMapping.getSubscriptionPackage().getTitle());
                            associatedPackages.add(associatedPackageView);
                        }
                    }
                }
            }
        }
        log.info("Set associated packages view : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        memberProgramDetailResponseView.setAssociatedPackages(associatedPackages);
        boolean isProgramSubscribedThroughPackages = false;
        if (user != null && !memberProgramDetailResponseView.isFreeToAccess()) {
            /*
             * Restarting program for user.
             * */
            try {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
                String token = request.getHeader(Constants.X_AUTHORIZATION);
                String timeZoneName = userComponents.getTimeZone(token);
                memberProgramService.checkAndResetProgramCompletion(timeZoneName, user, program);
            } catch (Exception e) {
                log.error("Exception while restarting program from Member program L2 : " + e.getMessage());
            }
            log.info("Restart program for member : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            // Checking whether the program is subscribed by the user
            ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), program.getProgramId());
            PackageProgramSubscription packageProgramSubscription;
            packageProgramSubscription = packageProgramSubscriptionRepository.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), program.getProgramId());
            log.info("Query: get program and package program subscriptions : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            boolean isPaidProgramSubscription = false;
            boolean isPaidPackageSubscription = false;
            String programSubscriptionStatus = null;
            String packageSubscriptionStatus = null;
            SubscriptionStatus subscriptionStatus = null;
            if (programSubscription != null) {
                subscriptionStatus = subscriptionService.getMemberProgramSubscriptionStatus(programSubscription);
                programSubscriptionStatus = subscriptionStatus.getSubscriptionStatusName();
                if (subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID) || subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAYMENT_PENDING)) {
                    isPaidProgramSubscription = true;
                }
            }
            log.info("Get subscription status for program : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            if (packageProgramSubscription != null) {
                subscriptionStatus = subscriptionService.getMemberPackageProgramSubscriptionStatus(packageProgramSubscription);
                packageSubscriptionStatus = subscriptionStatus.getSubscriptionStatusName();
                if (subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID) || subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAYMENT_PENDING)) {
                    isPaidPackageSubscription = true;
                }
            }
            log.info("Get subscription status for package program : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            if (programSubscription != null && packageProgramSubscription != null) {
                if ((isPaidProgramSubscription && isPaidPackageSubscription) || isPaidProgramSubscription) {
                    expiryDate = subscriptionService.getProgramSubscriptionExpiry(programSubscription);
                    //For bug id - 136962 , as per Arvind input ,given precedence to program followed by package if both are paid
                    isViaProgramSubscription = true;
                }else if (isPaidPackageSubscription) {
                    isViaPackageSubscription = true;
                    expiryDate = subscriptionService.getPackageSubscriptionExpiry(packageProgramSubscription.getPackageSubscription());
                } else if (programSubscriptionStatus.equalsIgnoreCase(KeyConstants.KEY_TRIAL) && packageSubscriptionStatus.equalsIgnoreCase(KeyConstants.KEY_EXPIRED)) {
                    isViaProgramSubscription = true;
                } else if (programSubscriptionStatus.equalsIgnoreCase(KeyConstants.KEY_EXPIRED) && packageSubscriptionStatus.equalsIgnoreCase(KeyConstants.KEY_EXPIRED)) {
                    Date programExpiryDate = subscriptionService.getProgramSubscriptionExpiry(programSubscription);
                    Date packageExpiryDate = subscriptionService.getPackageSubscriptionExpiry(packageProgramSubscription.getPackageSubscription());
                    if (packageExpiryDate.after(programExpiryDate)) {
                        isViaPackageSubscription = true;
                        expiryDate = packageExpiryDate;
                    } else {
                        isViaProgramSubscription = true;
                        expiryDate = programExpiryDate;
                    }
                }
            } else if (programSubscription != null && packageProgramSubscription == null) {
                expiryDate = subscriptionService.getProgramSubscriptionExpiry(programSubscription);
                isViaProgramSubscription = true;
            } else if (packageProgramSubscription != null && programSubscription == null) {
                isViaPackageSubscription = true;
                expiryDate = subscriptionService.getPackageSubscriptionExpiry(packageProgramSubscription.getPackageSubscription());
            }
            log.info("Get subscription expiry : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            //Program not available for blocked member without subscription
            if (isMemberBlocked && programSubscription == null && packageProgramSubscription == null) {
                throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_BLOCKED_MEMBER_PROGRAM_NOT_AVAILABLE, MessageConstants.ERROR);
            }
            //Program in unpublish/block status not available for members without subscription. Not sending error msg since mobile team handled only for empty msg.
            if (!memberProgramDetailResponseView.isFreeToAccess() && isProgramBlockOrUnpublished && programSubscription == null && packageProgramSubscription == null) {
                throw new ApplicationException(Constants.NOT_FOUND, null, MessageConstants.ERROR);
            }
            boolean isSubscriptionExpired = false;
            String orderStatus = null;
            boolean isOrderUnderProcessing = false;
            boolean setExpiryDate = false;
            PlatformType subscribedViaPlatform = null;
            if (isViaProgramSubscription) {
                subscriptionStatus = programSubscription.getSubscriptionStatus();
                //program processing state is not updated in L2 page.So condition moved here.
                subscribedViaPlatform = programSubscription.getSubscribedViaPlatform();
                subscribedDate = programSubscription.getSubscribedDate();
                OrderManagement orderManagement = orderManagementRepository.findTop1ByUserAndProgramOrderByCreatedDateDesc(user, program);
                if (orderManagement != null && orderManagement.getOrderStatus() != null) {
                    orderStatus = orderManagement.getOrderStatus();
                    if (orderManagement.getOrderStatus().equalsIgnoreCase(KeyConstants.KEY_PROCESSING)) {
                        isOrderUnderProcessing = true;
                    }
                }
                log.info("Get order management : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                profilingEndTimeMillis = new Date().getTime();
                if (subscriptionStatus != null) {
                    String statusName = subscriptionStatus.getSubscriptionStatusName();
                    if (statusName.equals(KeyConstants.KEY_PAID) || statusName.equals(KeyConstants.KEY_PAYMENT_PENDING) || statusName.equals(KeyConstants.KEY_EXPIRED)) {
                        isNewSubscription = false;
                    }
                    if (KeyConstants.KEY_EXPIRED.equalsIgnoreCase(subscriptionStatus.getSubscriptionStatusName())) {
                        isSubscriptionExpired = true;
                    } else if (KeyConstants.KEY_TRIAL.equals(subscriptionStatus.getSubscriptionStatusName())) {
                        int completedWorkouts = workoutCompletionRepository.countByMemberUserIdAndProgramProgramId(user.getUserId(), programId);
                        int trialWorkouts = fitwiseUtils.getTrialWorkoutsCountForProgram(programSubscription.getUser(), programSubscription.getProgram());
                        if (completedWorkouts >= trialWorkouts) {
                            isSubscriptionExpired = true;
                        }
                    }
                    log.info("Query: get workout completion : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                    profilingEndTimeMillis = new Date().getTime();
                    /**
                     * Setting whether the program subscription is Active along with Auto-Subscription Active status
                     * Also setting the order status  - Success/Failure/Processing
                     */
                    if (!subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_TRIAL)) {
                        if (programSubscription.getSubscribedViaPlatform().getPlatformTypeId() == 2) {
                            /**
                             * Program is subscribed via ios App
                             */
                            if (orderManagement != null) {
                                //Setting the order status based on the latest entry from the order management table
                                setExpiryDate = true;
                                if ((subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID)) || (subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAYMENT_PENDING))) {
                                    isProgramSubscribed = true;
                                }
                                // Setting whether the program's auto-susbcription is ON / OFF
                                AppleProductSubscription subscription = appleProductSubscriptionRepository.findTop1ByProgramAndUserOrderByModifiedDateDesc(program, user);
                                if (subscription != null && subscription.getAppleSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_ACTIVE)) {
                                    programAutoSubscribed = true;
                                }
                            }
                        } else {
                            /**
                             * Program is subscribed via Android / Web apps
                             */
                            // Setting whether the subscription is active for the program
                            if ((subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID)) || (subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAYMENT_PENDING))) {
                                isProgramSubscribed = true;
                                setExpiryDate = true;
                            }
                            // Setting the order status in the response
                            if (orderManagement != null) {
                                if (KeyConstants.KEY_AUTH_NET_PAYMENT_MODE.equals(orderManagement.getModeOfPayment())) {
                                    // Checking whether the program is auto-subscribed by the user
                                    AuthNetArbSubscription arbSubscription = authNetArbSubscriptionRepository.
                                            findTop1ByUserUserIdAndProgramProgramIdOrderByModifiedDateDesc(user.getUserId(), program.getProgramId());
                                    // Checking whether the auto-subscription is active or not
                                    if (arbSubscription != null && arbSubscription.getAuthNetSubscriptionStatus() != null &&
                                            arbSubscription.getAuthNetSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_ACTIVE)) {
                                        programAutoSubscribed = true;
                                    }
                                    AuthNetPayment authNetPayment = authNetPaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
                                    if (authNetPayment == null) {
                                        throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERROR_FETCHING_ORDER_TRANSACTION_DETAILS, null);
                                    }
                                    if (authNetPayment != null && authNetPayment.getResponseCode() != null && authNetPayment.getResponseCode().equalsIgnoreCase(KeyConstants.ANET_TRANSACTION_SUCCESS_RESPONSE_CODE)) {
                                        orderStatus = KeyConstants.KEY_SUCCESS;
                                    } else {
                                        orderStatus = KeyConstants.KEY_FAILURE;
                                    }
                                    memberProgramDetailResponseView.setIsARBUnderProcessing(authNetPayment.getIsARBUnderProcessing());
                                } else if (KeyConstants.KEY_STRIPE.equals(orderManagement.getModeOfPayment())) {
                                    StripeSubscriptionAndUserProgramMapping stripeSubscriptionAndUserProgramMapping = stripeSubscriptionAndUserProgramMappingRepository.findTop1ByUserUserIdAndProgramProgramIdOrderByModifiedDateDesc(user.getUserId(), program.getProgramId());
                                    if (stripeSubscriptionAndUserProgramMapping != null && stripeSubscriptionAndUserProgramMapping.getSubscriptionStatus() != null &&
                                            stripeSubscriptionAndUserProgramMapping.getSubscriptionStatus().getSubscriptionStatus().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_ACTIVE)) {
                                        programAutoSubscribed = true;
                                    }
                                    StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
                                    if (stripePayment == null) {
                                        throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERROR_FETCHING_ORDER_TRANSACTION_DETAILS, null);
                                    }
                                    // Getting the payment status
                                    if (stripePayment != null && KeyConstants.KEY_PAID.equals(stripePayment.getTransactionStatus())) {
                                        orderStatus = KeyConstants.KEY_SUCCESS;
                                    } else {
                                        orderStatus = KeyConstants.KEY_FAILURE;
                                    }
                                }
                            }
                        }
                    }
                    log.info("Setting whether the program subscription is Active along with Auto-Subscription Active status : " + (new Date().getTime() - profilingEndTimeMillis));
                    profilingEndTimeMillis = new Date().getTime();
                }
            } else if (isViaPackageSubscription) {
                subscribedDate = packageProgramSubscription.getSubscribedDate();
                subscribedViaPlatform = packageProgramSubscription.getPackageSubscription().getSubscribedViaPlatform();
                if (subscriptionStatus != null) {
                    if ((subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID)) || (subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAYMENT_PENDING))) {
                        isProgramSubscribed = true;
                        isProgramSubscribedThroughPackages = true;
                        setExpiryDate = true;
                    }
                    if (subscriptionStatus.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_EXPIRED)) {
                        isSubscriptionExpired = true;
                    }
                }
                OrderManagement orderManagement = orderManagementRepository.findTop1ByUserAndSubscriptionPackageOrderByCreatedDateDesc(user, packageProgramSubscription.getPackageSubscription().getSubscriptionPackage());
                log.info("Query: get order management : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                profilingEndTimeMillis = new Date().getTime();
                if (orderManagement != null) {
                    orderStatus = orderManagement.getOrderStatus();
                    if (orderManagement.getOrderStatus() != null && orderManagement.getOrderStatus().equalsIgnoreCase(KeyConstants.KEY_PROCESSING)) {
                        isOrderUnderProcessing = true;
                    }
                    subscribedDate = packageProgramSubscription.getSubscribedDate();
                    if (KeyConstants.KEY_STRIPE.equals(orderManagement.getModeOfPayment())) {
                        StripeSubscriptionAndUserPackageMapping stripeSubscriptionAndUserPackageMapping = stripeSubscriptionAndUserPackageMappingRepository.findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageIdOrderByModifiedDateDesc(user.getUserId(), packageProgramSubscription.getPackageSubscription().getSubscriptionPackage().getSubscriptionPackageId());
                        if (stripeSubscriptionAndUserPackageMapping != null && stripeSubscriptionAndUserPackageMapping.getSubscriptionStatus() != null &&
                                stripeSubscriptionAndUserPackageMapping.getSubscriptionStatus().getSubscriptionStatus().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_ACTIVE)) {
                            programAutoSubscribed = true;
                        }
                        StripePayment stripePayment = stripePaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
                        if (stripePayment == null) {
                            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERROR_FETCHING_ORDER_TRANSACTION_DETAILS, null);
                        }
                        // Getting the payment status
                        if (stripePayment != null && KeyConstants.KEY_PAID.equals(stripePayment.getTransactionStatus())) {
                            orderStatus = KeyConstants.KEY_SUCCESS;
                        } else {
                            orderStatus = KeyConstants.KEY_FAILURE;
                        }
                    }
                }
                log.info("Query: stripe subscription and stripe payment : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                profilingEndTimeMillis = new Date().getTime();
            }
            if (subscriptionStatus != null) {
                isTrialOrSubscribed = subscriptionService.isTrialOrSubscribedByUser(subscriptionStatus);
                log.info("Get subscription status(Trial or subscribed) : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                profilingEndTimeMillis = new Date().getTime();
            }
            memberProgramDetailResponseView.setOrderStatus(orderStatus);
            memberProgramDetailResponseView.setIsOrderUnderProcessing(isOrderUnderProcessing);
            if (setExpiryDate && expiryDate != null) {
                memberProgramDetailResponseView.setSubscriptionExpiry(expiryDate.getTime());
                memberProgramDetailResponseView.setSubscriptionExpiryDate(fitwiseUtils.formatDate(expiryDate));
            }
            memberProgramDetailResponseView.setSubscribedViaPlatform(subscribedViaPlatform);
            if (subscribedDate != null) {
                memberProgramDetailResponseView.setSubscribedDate(subscribedDate);
                memberProgramDetailResponseView.setSubscribedDateFormatted(fitwiseUtils.formatDate(subscribedDate));
            }
            //Setting whether the program is subscribed or not to the response
            memberProgramDetailResponseView.setProgramSubscribed(isProgramSubscribed);
            memberProgramDetailResponseView.setProgramAutoSubscribed(programAutoSubscribed);
            memberProgramDetailResponseView.setProgramSubscribedThroughPackage(isProgramSubscribedThroughPackages);
            if (isSubscriptionExpired) {
                //Program not available for blocked member with expired subscription
                if (isMemberBlocked) {
                    throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_BLOCKED_MEMBER_PROGRAM_NOT_AVAILABLE, MessageConstants.ERROR);
                }
                //For unpublished and blocked programs, if the subscription is expired or trial completed, L2 page is not rendered
                if (isProgramBlockOrUnpublished) {
                    throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_MEMBER_PROGRAM_NOT_AVAILABLE, MessageConstants.ERROR);
                }
            }
            String status = null;
            if (subscriptionStatus != null) {
                if (subscriptionStatus.getSubscriptionStatusName().equals(KeyConstants.KEY_PAYMENT_PENDING) || subscriptionStatus.getSubscriptionStatusName().equals(KeyConstants.KEY_PAID)) {
                    status = KeyConstants.KEY_SUBSCRIBED;
                } else {
                    status = subscriptionStatus.getSubscriptionStatusName();
                }
                if (status.equals(KeyConstants.KEY_TRIAL)) {
                    isTrialSubscription = true;
                }
            }
            memberProgramDetailResponseView.setSubscriptionStatus(status);
        }
        if (isProgramSubscribed) {
            if (expiryDate != null) {
                LocalDate startLocalDate = LocalDate.now();
                LocalDate endLocalDate = expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                Period diff = Period.between(startLocalDate, endLocalDate);
                int validity = diff.getDays();
                if (validity == 0) {
                    subscriptionValidity = "Expires today";
                } else {
                    subscriptionValidity = "Expires in " + validity + " days";
                }
            }
        } else {
            subscriptionValidity = "SUBSCRIBE";
        }
        memberProgramDetailResponseView.setSubscriptionValidity(subscriptionValidity);
        List<Equipments> programEquipments = new ArrayList<>();
        List<MemberWorkoutScheduleResponseView> workoutScheduleResponseViews = new ArrayList<>();
        int trialWorkoutScheduleCount = fitwiseUtils.getTrialWorkoutsCountForProgram(user, program);
        List<WorkoutSchedule> workoutSchedules = program.getWorkoutSchedules();
        long totalUniqueWorkouts = workoutSchedules.stream()
                .filter(workoutSchedule -> workoutSchedule.getWorkout() != null)
                .map(workoutSchedule -> workoutSchedule.getWorkout().getWorkoutId())
                .distinct().count();
        memberProgramDetailResponseView.setNoOfWorkouts(Math.toIntExact(totalUniqueWorkouts));
        log.info("Member program detail response view construction : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        /*
         * Iterating through the workoutScheduleResponseViews to get the duration, total Exercise count,
         * workout completion status and Equipments list.
         */
        RestActivity restInstructorActivity = restActivityRepository.findByRestActivity(DBConstants.REST);
        log.info("Query: get rest activity : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        OptionalLong startOrder = workoutSchedules.stream().mapToLong(WorkoutSchedule::getOrder).min();
        for (WorkoutSchedule workoutSchedule : workoutSchedules) {
            MemberWorkoutScheduleResponseView workoutResponseView = new MemberWorkoutScheduleResponseView();
            workoutResponseView.setWorkoutScheduleId(workoutSchedule.getWorkoutScheduleId());
            Long order = workoutSchedule.getOrder();
            workoutResponseView.setOrder(order);
            // Setting trial as true until first two non-rest workouts as trial
            boolean isTrialWorkoutSchedule = false;
            if (!memberProgramDetailResponseView.isFreeToAccess() && order.intValue() <= trialWorkoutScheduleCount) {
                isTrialWorkoutSchedule = true;
            }
            workoutResponseView.setTrial(isTrialWorkoutSchedule);
            //previous workout completed date
            if (user != null) {
                WorkoutSchedule previousWorkoutSchedule = null;
                WorkoutCompletion previousWorkoutCompletion = null;
                if ((memberProgramDetailResponseView.isFreeToAccess() || isTrialOrSubscribed) && order.longValue() != startOrder.getAsLong()) {
                    previousWorkoutSchedule = workoutScheduleRepository.findByOrderAndProgramsProgramId(order - 1, programId);
                    if (previousWorkoutSchedule != null) {
                        previousWorkoutCompletion = workoutCompletionRepository.findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdOrderByCompletedDateDesc(user.getUserId(), programId, previousWorkoutSchedule.getWorkoutScheduleId());
                        if (previousWorkoutCompletion != null) {
                            workoutResponseView.setPreviousWorkoutCompletionDate(previousWorkoutCompletion.getCompletedDate());
                        }
                    }
                }
                /*
                 * Workout schedule completion data
                 * Workout schedule - isToday data
                 * */
                //check whether workout is today
                boolean isTodayWorkout = false;
                if ((memberProgramDetailResponseView.isFreeToAccess() || isTrialOrSubscribed) && (!(isTrialSubscription && !isTrialWorkoutSchedule))) {
                    //check whether workout to be played today
                    isTodayWorkout = memberProgramService.isWorkoutToday(user.getUserId(), programId, order, startOrder.getAsLong(), previousWorkoutCompletion);
                }
                workoutResponseView.setTodayWorkout(isTodayWorkout);
                //if Workout is completed
                boolean isWorkoutCompleted = false;
                if (memberProgramDetailResponseView.isFreeToAccess() || isTrialOrSubscribed) {
                    WorkoutCompletion workoutCompletion = workoutCompletionRepository.findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdOrderByCompletedDateDesc(user.getUserId(), programId, workoutSchedule.getWorkoutScheduleId());
                    if (workoutCompletion != null) {
                        isWorkoutCompleted = true;
                        workoutResponseView.setWorkoutCompletedOn(fitwiseUtils.formatDate(workoutCompletion.getCompletedDate()));
                        workoutResponseView.setWorkoutCompletionDate(workoutCompletion.getCompletedDate());
                        workoutResponseView.setWorkoutCompletionDateFormatted(fitwiseUtils.formatDate(workoutCompletion.getCompletedDate()));
                    }
                }
                workoutResponseView.setWorkoutCompleted(isWorkoutCompleted);
            }
            if (workoutSchedule.isRestDay()) {
                //Constructing Rest workout related data
                workoutResponseView.setRestDay(true);
                boolean isRestActivity = false;
                //Rest Activity data in workout schedule response
                InstructorRestActivity instructorRestActivity = workoutSchedule.getInstructorRestActivity();
                String title = DBConstants.REST;
                String workoutThumbnail = null;
                if (instructorRestActivity != null) {
                    String restActivityName = instructorRestActivity.getActivityName();
                    if (!DBConstants.REST.equalsIgnoreCase(restActivityName)) {
                        String metric = instructorRestActivity.getRestActivityToMetricMapping().getRestMetric().getRestMetric();
                        title = restActivityName + " - " + instructorRestActivity.getValue() + " " + metric;
                        isRestActivity = true;
                    }
                    RestActivity restActivity = instructorRestActivity.getRestActivityToMetricMapping().getRestActivity();
                    if (restActivity.getImage() != null) {
                        workoutThumbnail = restActivity.getImage().getImagePath();
                    }
                } else {
                    RestActivity restActivity = restInstructorActivity;
                    if (restActivity.getImage() != null) {
                        workoutThumbnail = restActivity.getImage().getImagePath();
                    }
                }
                workoutResponseView.setRestActivity(isRestActivity);
                workoutResponseView.setWorkoutTitle(title);
                workoutResponseView.setDisplayTitle(Convertions.getDayText(order) + ": " + title);
                workoutResponseView.setWorkoutThumbnail(workoutThumbnail);
            } else {
                //Constructing workout related data
                Workouts workout = workoutSchedule.getWorkout();
                workoutResponseView.setWorkoutId(workout.getWorkoutId());
                workoutResponseView.setWorkoutTitle(workout.getTitle());
                workoutResponseView.setDisplayTitle(Convertions.getDayText(order) + ": " + workout.getTitle());
                if (workout.getImage() != null) {
                    workoutResponseView.setWorkoutThumbnail(workout.getImage().getImagePath());
                }
                //Calculating circuit count and workout duration
                int circuitCount = 0;
                int workoutDuration = 0;
                boolean isVideoProcessingPending = false;
                for (CircuitSchedule circuitSchedule : workout.getCircuitSchedules()) {
                    long circuitDuration = 0;
                    boolean isRestCircuit = circuitSchedule.isRestCircuit();
                    if (isRestCircuit) {
                        circuitDuration = circuitSchedule.getRestDuration();
                    } else if (circuitSchedule.getCircuit() != null && (circuitSchedule.getIsAudio() == null || !circuitSchedule.getIsAudio())) {
                        circuitCount++;
                        Set<ExerciseSchedulers> exerciseSchedules = circuitSchedule.getCircuit().getExerciseSchedules();
                        for (ExerciseSchedulers exerciseScheduler : exerciseSchedules) {
                            long exerciseDuration = 0;
                            if (exerciseScheduler.getExercise() != null) {
                                if (exerciseScheduler.getExercise().getVideoManagement() != null) {
                                    exerciseDuration = exerciseScheduler.getExercise().getVideoManagement().getDuration();

                                    if (exerciseScheduler.getLoopCount() != null && exerciseScheduler.getLoopCount() > 0) {
                                        //Repeat Count Change : Since repeat count is changes as no of times video should play
                                        exerciseDuration = exerciseDuration * exerciseScheduler.getLoopCount();
                                    }
									// Calculated Workout time for Set & Rep Based Workouts
									if (exerciseScheduler.getSetsCount() > 0 && exerciseScheduler.getRepsCount() > 0) {
										int setsDuration = exerciseScheduler.getSetsCount()
												* Constants.SET_REST_DURATION_IN_SEC; // # of Sets * 60 sec
										int repsDuration = exerciseScheduler.getSetsCount()
												* exerciseScheduler.getRepsCount()
												* Constants.REPS_REST_DURATION_IN_SEC; // # of Sets * # of Reps * 2 sec
										exerciseDuration = setsDuration + repsDuration;
									}
                                    if (fitwiseUtils.isVideoProcessingPending(exerciseScheduler.getExercise().getVideoManagement())) {
                                        isVideoProcessingPending = true;
                                    }
                                }
                                if (exerciseScheduler.getExercise().getEquipments() != null)
                                    programEquipments.addAll(exerciseScheduler.getExercise().getEquipments());
                            } else if (exerciseScheduler.getWorkoutRestVideo() != null) {
                                exerciseDuration = exerciseScheduler.getWorkoutRestVideo().getRestTime();
                            } else if (exerciseScheduler.getVoiceOver() != null) {
                                exerciseDuration = exerciseScheduler.getVoiceOver().getAudios().getDuration();
                            }
                            circuitDuration += exerciseDuration;
                        }
                        Long repeat = circuitSchedule.getRepeat();
                        Long restBetweenRepeat = circuitSchedule.getRestBetweenRepeat();
                        if (repeat != null && repeat > 0) {
                            //Repeat Count Change : Since repeat count is changes as no of times video should play
                            circuitDuration = circuitDuration * repeat;
                            long repeatRestDuration = 0;
                            if (restBetweenRepeat != null && restBetweenRepeat > 0) {
                                //Repeat Count Change : Since repeat count is changes as no of times video should play
                                repeatRestDuration = restBetweenRepeat * (repeat-1);
                            }
                            circuitDuration = circuitDuration  + repeatRestDuration;
                        }
                    } else if (circuitSchedule.getIsAudio() != null && circuitSchedule.getIsAudio()) {
                        for (CircuitAndVoiceOverMapping circuitAndVoiceOverMapping : circuitSchedule.getCircuit().getCircuitAndVoiceOverMappings()) {
                            circuitDuration += circuitAndVoiceOverMapping.getVoiceOver().getAudios().getDuration();
                        }
                    }
                    workoutDuration += circuitDuration;
                }
                workoutResponseView.setTotalCircuits(circuitCount);
                workoutResponseView.setDuration(workoutDuration);
                workoutResponseView.setVideoProcessingPending(isVideoProcessingPending);
            }
            workoutScheduleResponseViews.add(workoutResponseView);
        }
        log.info("Set program workout details : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        List<Equipments> programEquipmentsList = programEquipments.stream()
                .collect(collectingAndThen(toCollection(() -> new TreeSet<Equipments>(comparingLong(Equipments::getEquipmentId))), ArrayList::new));
        memberProgramDetailResponseView.setEquipments(programEquipmentsList);
        log.info("Get unique program equipment list : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        int totalDays = program.getDuration().getDuration().intValue();
        int completedWorkouts = 0;
        String progress = null;
        int progressPercent = 0;
        Date completionDate = null;
        if (user != null) {
            List<WorkoutCompletion> workoutCompletionList = workoutCompletionRepository.findByMemberUserIdAndProgramProgramId(user.getUserId(), program.getProgramId());
            log.info("Query: get workout completion : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            completedWorkouts = workoutCompletionList.size();
            if (isTrialSubscription && completedWorkouts >= trialWorkoutScheduleCount) {
                memberProgramDetailResponseView.setTrialCompleted(true);
            }
            if (completedWorkouts == totalDays) {
                progress = KeyConstants.KEY_COMPLETED;
                progressPercent = 100;
                completionDate = workoutCompletionList.get(workoutCompletionList.size() - 1).getCompletedDate();
            } else {
                progress = Convertions.getNumberWithZero(completedWorkouts) + "/" + totalDays;
                progressPercent = (completedWorkouts * 100) / totalDays;
            }
            log.info("Set workout progress : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
        }
        memberProgramDetailResponseView.setProgress(progress);
        memberProgramDetailResponseView.setProgressPercent(progressPercent);
        memberProgramDetailResponseView.setCompletedWorkouts(completedWorkouts);
        memberProgramDetailResponseView.setProgramCompletionDate(completionDate);
        memberProgramDetailResponseView.setProgramCompletionDateFormatted(fitwiseUtils.formatDate(completionDate));
        if (program.getProgramExpertiseLevel() != null) {
            memberProgramDetailResponseView.setProgramLevel(program.getProgramExpertiseLevel().getExpertiseLevel());
        }
		if (program.getProgramType() != null) {
			String programTypeName = program.getProgramType().getProgramTypeName();
			if (program.getProgramSubType() != null && !program.getProgramSubType().getName().isEmpty()) {
				programTypeName = programTypeName + " - " + program.getProgramSubType().getName();
			}
			memberProgramDetailResponseView.setProgramType(programTypeName);
		}
        if (program.getDuration() != null) {
            memberProgramDetailResponseView.setProgramDuration(program.getDuration().getDuration());
        }
        // Sorting exercises based on order
        workoutScheduleResponseViews.sort(Comparator.comparing(MemberWorkoutScheduleResponseView::getOrder));
        memberProgramDetailResponseView.setWorkouts(workoutScheduleResponseViews);
        ProgramViewsAudit programViewsAudit = new ProgramViewsAudit();
        programViewsAudit.setProgram(program);
        if (user != null) {
            programViewsAudit.setUser(user);
        }
        programViewsAudit.setDate(new Date());
        log.info("Sorting exercise based on order : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        programViewsAuditRepository.save(programViewsAudit);
        log.info("Query: save program view audit : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        /** discount Offers **/
        if (!isViaPackageSubscription) {
            List<DiscountOfferMapping> disList = discountOfferMappingRepository.findByProgramsProgramIdAndOfferCodeDetailIsInUseAndOfferCodeDetailOfferStatus(programId, true, DiscountsConstants.OFFER_ACTIVE);
            if (disList != null && !disList.isEmpty()) {
                List<ProgramDiscountMappingResponseView> currentDiscounts = new ArrayList<>();
                List<ProgramDiscountMappingResponseView> freeOffers = new ArrayList<>();
                List<ProgramDiscountMappingResponseView> paidOffers = new ArrayList<>();
                List<ProgramDiscountMappingResponseView> upcomingDiscounts = new ArrayList<>();
                List<ProgramDiscountMappingResponseView> expiredDiscounts = new ArrayList<>();
                for (DiscountOfferMapping disOffer : disList) {
                    if (isNewSubscription == disOffer.getOfferCodeDetail().getIsNewUser().booleanValue()) {
                        ProgramDiscountMappingResponseView sResponseView = new ProgramDiscountMappingResponseView();
                        sResponseView.setOfferMappingId(disOffer.getOfferMappingId());
                        sResponseView.setOfferCodeId(disOffer.getOfferCodeDetail().getOfferCodeId());
                        sResponseView.setOfferName(disOffer.getOfferCodeDetail().getOfferName().trim());
                        sResponseView.setOfferCode(disOffer.getOfferCodeDetail().getOfferCode().toUpperCase());
                        sResponseView.setOfferMode(disOffer.getOfferCodeDetail().getOfferMode());
                        sResponseView.setOfferDuration(disOffer.getOfferCodeDetail().getOfferDuration());
                        sResponseView.setOfferStartDate(fitwiseUtils.formatDate(disOffer.getOfferCodeDetail().getOfferStartingDate()));
                        sResponseView.setOfferEndDate(fitwiseUtils.formatDate(disOffer.getOfferCodeDetail().getOfferEndingDate()));
                        sResponseView.setOfferPrice(disOffer.getOfferCodeDetail().getOfferPrice());
                        String formattedPrice = null;
                        if (disOffer.getOfferCodeDetail().getOfferPrice() != null) {
                            formattedPrice = fitwiseUtils.formatPrice(disOffer.getOfferCodeDetail().getOfferPrice().getPrice());
                            sResponseView.setFormattedOfferPrice(formattedPrice);
                        } else {
                            formattedPrice = KeyConstants.KEY_CURRENCY_US_DOLLAR + KeyConstants.KEY_DEFAULT_PRICE_FORMAT;
                            sResponseView.setFormattedOfferPrice(formattedPrice);
                        }
                        if (program.getProgramPrices() != null) {
                            double savingsAmount = program.getProgramPrices().getPrice();
                            if (disOffer.getOfferCodeDetail().getOfferPrice() != null) {
                                savingsAmount = program.getProgramPrices().getPrice() - disOffer.getOfferCodeDetail().getOfferPrice().getPrice();
                            }
                            DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
                            sResponseView.setFormattedSavingsAmount(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(savingsAmount));
                        }
                        sResponseView.setOfferStatus(disOffer.getOfferCodeDetail().getOfferStatus());
                        sResponseView.setIsNewUser(disOffer.getOfferCodeDetail().getIsNewUser());
                        //Offer Validity check
                        Date now = new Date();
                        Date offerStart = disOffer.getOfferCodeDetail().getOfferStartingDate();
                        Date offerEnd = disOffer.getOfferCodeDetail().getOfferEndingDate();
                        if (disOffer.getOfferCodeDetail().getOfferStatus().equalsIgnoreCase(DiscountsConstants.OFFER_ACTIVE) && disOffer.getOfferCodeDetail().isInUse()) {
                            // Current Offers
                            if ((offerStart.equals(now) || offerStart.before(now)) && (offerEnd.equals(now) || offerEnd.after(now))) {
                                if (disOffer.getOfferCodeDetail().getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_FREE)) {
                                    freeOffers.add(sResponseView);
                                } else if (disOffer.getOfferCodeDetail().getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_PAY_AS_YOU_GO)) {
                                    paidOffers.add(sResponseView);
                                }
                            }
                            // UpComing Offers
                            else if (offerStart.after(now) && offerEnd.after(now)) {
                                upcomingDiscounts.add(sResponseView);
                            }
                        } else {
                            // Expired Offers
                            if (offerStart.before(now) && offerEnd.before(now)) {
                                expiredDiscounts.add(sResponseView);
                            }
                        }
                    }
                }
                ProgramDiscountMappingListResponseView discountOffers = new ProgramDiscountMappingListResponseView();
                paidOffers.sort((ProgramDiscountMappingResponseView f1, ProgramDiscountMappingResponseView f2) -> f1.compareTo(f2));
                freeOffers.sort((ProgramDiscountMappingResponseView f1, ProgramDiscountMappingResponseView f2) -> f1.compareTo(f2));
                Collections.sort(freeOffers, Collections.reverseOrder());
                freeOffers.stream().collect(toCollection(() -> currentDiscounts));
                paidOffers.stream().collect(toCollection(() -> currentDiscounts));
                discountOffers.setCurrentDiscounts(currentDiscounts);
                discountOffers.setUpcomingDiscounts(upcomingDiscounts);
                discountOffers.setExpiredDiscounts(expiredDiscounts);
                memberProgramDetailResponseView.setDiscountOffers(discountOffers);
            }
        }
        if(memberProgramDetailResponseView.isFreeToAccess()){
            memberProgramDetailResponseView.setOrderStatus(KeyConstants.KEY_SUCCESS);
            memberProgramDetailResponseView.setSubscriptionStatus(KeyConstants.KEY_SUBSCRIBED);
        }
        log.info("Set offer details : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get program details ends.");
        return memberProgramDetailResponseView;
    }

    /**
     * Get all featured programs by program type
     * @return
     */
    public List<ProgramTypeWithProgramTileModel> getFeaturedPrograms() {
        List<FeaturedProgramTypeAndPrograms> featuredProgramTypeAndPrograms = featuredProgramTypeAndProgramsRepository.findByProgramStatusOrderByFeaturedProgramId(KeyConstants.KEY_PUBLISH);
        Map<Long, List<FeaturedProgramTypeAndPrograms>> featuredProgramTypeAndProgramsMap = new LinkedHashMap<>();
        TreeSet<Long> availableProgramsIds = new TreeSet<>();
        for(FeaturedProgramTypeAndPrograms featuredProgramTypeAndProgram : featuredProgramTypeAndPrograms){
            availableProgramsIds.add(featuredProgramTypeAndProgram.getProgram().getProgramId());
            if(featuredProgramTypeAndProgramsMap.get(featuredProgramTypeAndProgram.getProgramType().getProgramTypeId()) == null){
                featuredProgramTypeAndProgramsMap.put(featuredProgramTypeAndProgram.getProgramType().getProgramTypeId(), new LinkedList<>());
            }
            featuredProgramTypeAndProgramsMap.get(featuredProgramTypeAndProgram.getProgramType().getProgramTypeId()).add(featuredProgramTypeAndProgram);
        }
        List<ProgramTypeWithProgramTileModel> programTypeWithProgramTileModels = new ArrayList<>();
        User user = userComponents.getAndValidateUser();
        Map<Long, Long> offerCountMap = discountsService.getNoOfCurrentAvailableOffersOfProgramsForMember(new ArrayList<>(availableProgramsIds), user);
        for(Long featuresProgramId : featuredProgramTypeAndProgramsMap.keySet()){
            ProgramTypeWithProgramTileModel programTypeWithProgramTileModel = new ProgramTypeWithProgramTileModel();
            ProgramTypes programTypes = null;
            List<Programs> programsList = new ArrayList<>();
            for(FeaturedProgramTypeAndPrograms featuredPrograms : featuredProgramTypeAndProgramsMap.get(featuresProgramId)){
                if(programTypes == null){
                    programTypes = featuredPrograms.getProgramType();
                }
                programsList.add(featuredPrograms.getProgram());
            }
            programTypeWithProgramTileModel.setProgramTypeId(programTypes.getProgramTypeId());
            programTypeWithProgramTileModel.setProgramTypeName(programTypes.getProgramTypeName());
            programTypeWithProgramTileModel.setPrograms(programDataParsing.constructProgramTileModelWithFreeAccess(programsList, offerCountMap));
            programTypeWithProgramTileModels.add(programTypeWithProgramTileModel);
        }
        return programTypeWithProgramTileModels;
    }
}

