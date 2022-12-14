package com.fitwise.service.packaging;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.AppConfigConstants;
import com.fitwise.constants.CalendarConstants;
import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.InstructorConstant;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.StripeConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.constants.VideoUploadStatus;
import com.fitwise.constants.discounts.DiscountsConstants;
import com.fitwise.entity.AppConfigKeyValue;
import com.fitwise.entity.CircuitSchedule;
import com.fitwise.entity.Equipments;
import com.fitwise.entity.ExerciseSchedulers;
import com.fitwise.entity.Images;
import com.fitwise.entity.PackageDuration;
import com.fitwise.entity.PlatformType;
import com.fitwise.entity.PlatformWiseTaxDetail;
import com.fitwise.entity.Programs;
import com.fitwise.entity.Promotions;
import com.fitwise.entity.TimeSpan;
import com.fitwise.entity.User;
import com.fitwise.entity.UserCommunicationDetail;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.WorkoutMapping;
import com.fitwise.entity.calendar.CalendarMeetingType;
import com.fitwise.entity.calendar.UserKloudlessAccount;
import com.fitwise.entity.calendar.UserKloudlessCalendar;
import com.fitwise.entity.calendar.UserKloudlessMeeting;
import com.fitwise.entity.discounts.DiscountLevel;
import com.fitwise.entity.discounts.DiscountOfferMapping;
import com.fitwise.entity.discounts.OfferCodeDetail;
import com.fitwise.entity.instructor.InstructorTierDetails;
import com.fitwise.entity.instructor.Location;
import com.fitwise.entity.packaging.CancellationDuration;
import com.fitwise.entity.packaging.PackageExternalClientMapping;
import com.fitwise.entity.packaging.PackageKloudlessMapping;
import com.fitwise.entity.packaging.PackageMemberMapping;
import com.fitwise.entity.packaging.PackageOfferMapping;
import com.fitwise.entity.packaging.PackageProgramMapping;
import com.fitwise.entity.packaging.SessionCountPerWeek;
import com.fitwise.entity.packaging.SessionType;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.packaging.SubscriptionPackagePriceByPlatform;
import com.fitwise.entity.payments.stripe.StripeProductAndPackageMapping;
import com.fitwise.entity.subscription.PackageProgramSubscription;
import com.fitwise.entity.subscription.PackageSubscription;
import com.fitwise.entity.subscription.SubscriptionStatus;
import com.fitwise.exception.ApplicationException;
import com.fitwise.model.packaging.AccessModel;
import com.fitwise.model.packaging.CancellationDurationModel;
import com.fitwise.model.packaging.MeetingModel;
import com.fitwise.model.packaging.OfferSubscriptionPackageModel;
import com.fitwise.model.packaging.SubscriptionPackageModel;
import com.fitwise.program.model.PlatformWiseTaxDetailsModel;
import com.fitwise.program.model.ProgramPriceResponseModel;
import com.fitwise.program.model.ProgramTileModel;
import com.fitwise.program.service.ProgramService;
import com.fitwise.repository.AppConfigKeyValueRepository;
import com.fitwise.repository.ImageRepository;
import com.fitwise.repository.PackageDurationRepository;
import com.fitwise.repository.PlatformTypeRepository;
import com.fitwise.repository.PlatformWiseTaxDetailRepository;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.PromotionRepository;
import com.fitwise.repository.UserCommunicationDetailRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.repository.calendar.CalendarMeetingTypeRepository;
import com.fitwise.repository.calendar.UserKloudlessMeetingRepository;
import com.fitwise.repository.calendar.UserKloudlessScheduleRepository;
import com.fitwise.repository.discountsRepository.DiscountLevelRepository;
import com.fitwise.repository.discountsRepository.DiscountOfferMappingRepository;
import com.fitwise.repository.discountsRepository.OfferCodeDetailRepository;
import com.fitwise.repository.instructor.InstructorTierDetailsRepository;
import com.fitwise.repository.instructor.LocationRepository;
import com.fitwise.repository.packaging.CancellationDurationRepository;
import com.fitwise.repository.packaging.PackageExternalClientMappingRepository;
import com.fitwise.repository.packaging.PackageKloudlessMappingRepository;
import com.fitwise.repository.packaging.PackageMemberMappingRepository;
import com.fitwise.repository.packaging.PackageOfferMappingRepository;
import com.fitwise.repository.packaging.PackageProgramMappingRepository;
import com.fitwise.repository.packaging.SessionCountPerWeekRepository;
import com.fitwise.repository.packaging.SessionTypeRepository;
import com.fitwise.repository.packaging.SubscriptionPackagePriceByPlatformRepository;
import com.fitwise.repository.packaging.SubscriptionPackageRepository;
import com.fitwise.repository.payments.stripe.StripeProductAndPackageMappingRepository;
import com.fitwise.repository.subscription.PackageProgramSubscriptionRepository;
import com.fitwise.repository.subscription.PackageSubscriptionRepository;
import com.fitwise.repository.subscription.SubscriptionStatusRepo;
import com.fitwise.response.LocationResponse;
import com.fitwise.response.packaging.AccessControlExternalClientView;
import com.fitwise.response.packaging.AccessControlMemberView;
import com.fitwise.response.packaging.MeetingView;
import com.fitwise.response.packaging.MyMeetingsListView;
import com.fitwise.response.packaging.MyMeetingsView;
import com.fitwise.response.packaging.SubscriptionPackageTileView;
import com.fitwise.response.packaging.SubscriptionPackageView;
import com.fitwise.service.SubscriptionService;
import com.fitwise.service.calendar.CalendarService;
import com.fitwise.service.discountservice.DiscountsService;
import com.fitwise.service.instructor.InstructorService;
import com.fitwise.service.instructor.InstructorSubscriptionService;
import com.fitwise.service.payment.stripe.StripeService;
import com.fitwise.service.qbo.FitwiseQboEntityService;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.RequestParamValidator;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.utils.mail.AsyncMailerUtil;
import com.fitwise.utils.parsing.TaxDetailParsing;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.discounts.ProgramDiscountMappingListResponseView;
import com.fitwise.view.discounts.ProgramDiscountMappingResponseView;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

/*
 * Created by Vignesh G on 22/09/20
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionPackageService {

    @Autowired
    private UserComponents userComponents;
    @Autowired
    private FitwiseUtils fitwiseUtils;
    @Autowired
    private SubscriptionPackageRepository subscriptionPackageRepository;
    @Autowired
    private PackageDurationRepository packageDurationRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private ProgramRepository programRepository;
    @Autowired
    SessionTypeRepository sessionTypeRepository;

    @Autowired
    PackageProgramMappingRepository packageProgramMappingRepository;
    @Autowired
    PackageKloudlessMappingRepository packageKloudlessMappingRepository;
    @Autowired
    private PlatformWiseTaxDetailRepository platformWiseTaxDetailRepository;
    @Autowired
    private PlatformTypeRepository platformTypeRepository;
    @Autowired
    SubscriptionPackagePriceByPlatformRepository subscriptionPackagePriceByPlatformRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    InstructorSubscriptionService instructorSubscriptionService;
    @Autowired
    PackageMemberMappingRepository packageMemberMappingRepository;
    @Autowired
    PackageExternalClientMappingRepository packageExternalClientMappingRepository;
    @Autowired
    private ProgramService programService;
    @Autowired
    UserCommunicationDetailRepository userCommunicationDetailRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private OfferCodeDetailRepository offerCodeDetailRepository;

    @Autowired
    private PackageOfferMappingRepository packageOfferMappingRepository;

    @Autowired
    private DiscountLevelRepository discountLevelRepository;

    @Autowired
    private DiscountOfferMappingRepository discountOfferMappingRepository;

    @Autowired
    private DiscountsService discountsService;
    @Autowired
    private StripeService stripeService;
    @Autowired
    private StripeProductAndPackageMappingRepository stripeProductAndPackageMappingRepository;
    @Autowired
    SubscriptionService subscriptionService;

    @Autowired
    private UserKloudlessMeetingRepository userKloudlessMeetingRepository;

    @Autowired
    private SessionCountPerWeekRepository sessionCountPerWeekRepository;

    @Autowired
    private CancellationDurationRepository cancellationDurationRepository;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private CalendarMeetingTypeRepository calendarMeetingTypeRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private InstructorService instructorService;

    @Autowired
    private FitwiseQboEntityService fitwiseQboEntityService;
    
    @Autowired
    private PackageProgramSubscriptionRepository packageProgramSubscriptionRepository;
    
    @Autowired
    private PackageSubscriptionRepository packageSubscriptionRepository;
    
    @Autowired
    private SubscriptionStatusRepo subscriptionStatusRepo;
    
    @Autowired
    private UserKloudlessScheduleRepository userKloudlessScheduleRepository;
    
    @Autowired
    private AppConfigKeyValueRepository appConfigKeyValueRepository;

    private final AsyncMailerUtil asyncMailerUtil;
    private final PromotionRepository promotionRepository;
    private final InstructorTierDetailsRepository instructorTierDetailsRepository;

    /**
     * Method for Package name duplicate validation
     * @param packageName
     */
    public void validatePackageName(String packageName) {
        User currentUser = userComponents.getUser();
        SubscriptionPackage packageWithSameTitle = subscriptionPackageRepository.findByOwnerUserIdAndTitleIgnoreCase(currentUser.getUserId(), packageName);
        if (packageWithSameTitle != null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_SUBSCRIPTION_PACKAGE_DUPLICATE_TITLE, MessageConstants.ERROR);
        }
    }

    /**
     * Method to create subscription package
     * @param model
     * @return
     */
    @Transactional
    public SubscriptionPackageView createSubscriptionPackage(SubscriptionPackageModel model) {
        log.info("Create subscription package starts");
        long start = new Date().getTime();
        User currentUser = userComponents.getUser();
        if (!fitwiseUtils.isInstructor(currentUser)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_INSTRUCTOR, MessageConstants.ERROR);
        }
        SubscriptionPackage subscriptionPackage;
        if (model.getSubscriptionPackageId() == null || model.getSubscriptionPackageId() == 0) {
            subscriptionPackage = new SubscriptionPackage();
            subscriptionPackage.setOwner(currentUser);
        } else {
            subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageIdAndOwnerUserId(model.getSubscriptionPackageId(), currentUser.getUserId());
            ValidationUtils.throwException(subscriptionPackage == null, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_NOT_FOUND, Constants.BAD_REQUEST);
//            if (InstructorConstant.PUBLISH.equals(subscriptionPackage.getStatus())) {
//               throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_PUBLISH_SUBSCRIPTION_PACKAGE_RESTRICT_EDIT, MessageConstants.ERROR);
//            } else 
            	if (InstructorConstant.UNPUBLISH.equals(subscriptionPackage.getStatus()) || DBConstants.UNPUBLISH_EDIT.equals(subscriptionPackage.getStatus())) {
                throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_UN_PUBLISH_SUBSCRIPTION_PACKAGE_RESTRICT_EDIT, MessageConstants.ERROR);
            } else if (InstructorConstant.BLOCK.equals(subscriptionPackage.getStatus()) || DBConstants.BLOCK_EDIT.equals(subscriptionPackage.getStatus())) {
                throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_BLOCK_SUBSCRIPTION_PACKAGE_RESTRICT_EDIT, MessageConstants.ERROR);
            }
        }
        log.info("Validations in create package : time taken in millis : "+(new Date().getTime()-start));
        constructSubscriptionPackage(model, subscriptionPackage, false);
        subscriptionPackage = subscriptionPackageRepository.save(subscriptionPackage);
        SubscriptionPackageView subscriptionPackageView = constructSubscriptionPackageResponse(subscriptionPackage);
        log.info("Create package : total time taken in millis : "+(new Date().getTime()-start));
        log.info("Create subscription package ends");
        return subscriptionPackageView;
    }

    /**
     * @param model
     * @param subscriptionPackage
     * @param isRestrictedPackage
     */
    private void constructSubscriptionPackage(SubscriptionPackageModel model, SubscriptionPackage subscriptionPackage, boolean isRestrictedPackage) {
        //Plan phase changes
        boolean isStepperCompleted = constructPlan(subscriptionPackage, model, isRestrictedPackage);
        //Access Control phase changes
        isStepperCompleted = constructAccessControl(subscriptionPackage, model, isRestrictedPackage, isStepperCompleted);
        //Configure phase changes
        isStepperCompleted = constructConfiguration(subscriptionPackage, model, isRestrictedPackage, isStepperCompleted);
        //Price phase changes
        constructPrice(subscriptionPackage, model, isRestrictedPackage, isStepperCompleted);
    }

    /**
     * @param subscriptionPackage
     * @param model
     * @param isRestrictedPackage
     * @return
     */
    private boolean constructPlan(SubscriptionPackage subscriptionPackage, SubscriptionPackageModel model, boolean isRestrictedPackage) {
        log.info("Construct plan phase starts");
        long start = new Date().getTime();
        boolean isStepperCompleted = false;
        boolean isNewPackage = false;
        String title = model.getTitle();
        if (!isRestrictedPackage) {
            if (subscriptionPackage.getSubscriptionPackageId() == null || subscriptionPackage.getSubscriptionPackageId() == 0) {
                isNewPackage = true;
            }
            subscriptionPackage.setStatus(InstructorConstant.PLAN);
            //Plan - validation
            RequestParamValidator.emptyString(title, ValidationMessageConstants.MSG_TITLE_NULL);
            RequestParamValidator.stringLengthValidation(title, 2L, 45L, MessageConstants.MSG_ERR_SUBSCRIPTION_PACKAGE_TITLE_LENGTH);
            //Duplicate Package title validation
            if (isNewPackage || !subscriptionPackage.getTitle().equalsIgnoreCase(title)) {
                SubscriptionPackage packageWithSameTitle = subscriptionPackageRepository.findByOwnerUserIdAndTitleIgnoreCase(subscriptionPackage.getOwner().getUserId(), title);
                if (packageWithSameTitle != null) {
                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_SUBSCRIPTION_PACKAGE_DUPLICATE_TITLE, MessageConstants.ERROR);
                }
            }
            subscriptionPackage.setTitle(title);
        } else if (subscriptionPackage.getPostCompletionStatus() == null) {
            subscriptionPackage.setPostCompletionStatus(InstructorConstant.PLAN);
        }
        if (isRestrictedPackage && !subscriptionPackage.getTitle().equalsIgnoreCase(title)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_TITLE_CHANGE, MessageConstants.ERROR);
        }
//        if (!StringUtils.isEmpty(model.getShortDescription())) {
//            RequestParamValidator.stringLengthValidation(model.getShortDescription(), 10L, 45L, MessageConstants.MSG_SUBSCRIPTION_PACKAGE_SHORT_DESC_LENGTH);
//        }
//        if (!model.isSaveAsDraft()) {
//            RequestParamValidator.emptyString(model.getShortDescription(), MessageConstants.MSG_SUBSCRIPTION_PACKAGE_SHORT_DESC_REQ);
//        }
//        subscriptionPackage.setShortDescription("");
        if (!ValidationUtils.isEmptyString(model.getDescription())) {
            subscriptionPackage.setDescription(model.getDescription());
        }
        boolean isThumbnailSet = false;
        if (model.getImageId() != null && model.getImageId() != 0) {
            Images thumbnail = imageRepository.getOne(model.getImageId());
            ValidationUtils.throwException(thumbnail == null, ValidationMessageConstants.MSG_IMAGE_NOT_FOUND, Constants.BAD_REQUEST);
            subscriptionPackage.setImage(thumbnail);
            isThumbnailSet = true;
        }
       
        PackageDuration durationObj = packageDurationRepository.findByDuration(28L);
        ValidationUtils.throwException(durationObj == null, "Duration is unavailable", Constants.BAD_REQUEST);
        subscriptionPackage.setPackageDuration(durationObj);

        //Subscription Package promo update (Optional)
        if(model.getPromotionId() != null && model.getPromotionId() != 0){
            Promotions promotion = promotionRepository.findByPromotionId(model.getPromotionId());
            if(promotion == null){
                throw new ApplicationException (Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROMOTION_NOT_FOUND, null);
            }
            subscriptionPackage.setPromotion(promotion);
        }
        //Plan - Status change to Configure
//        if (!ValidationUtils.isEmptyString(model.getShortDescription()) && !ValidationUtils.isEmptyString(model.getTitle()) && isThumbnailSet) {
        if (!ValidationUtils.isEmptyString(model.getTitle()) && isThumbnailSet) {
            if (!isRestrictedPackage) {
                subscriptionPackage.setStatus(DBConstants.ACCESS_CONTROL);
            } else {
                subscriptionPackage.setPostCompletionStatus(DBConstants.ACCESS_CONTROL);
            }
            isStepperCompleted = true;
        }
        log.info("Plan phase : time taken in millis : "+(new Date().getTime()-start));
        log.info("Construct plan phase ends");
        return isStepperCompleted;
    }

    /**
     * @param subscriptionPackage
     * @param model
     * @param isRestrictedPackage
     * @param isPreviousStepperCompleted
     * @return
     */
    private boolean constructAccessControl(SubscriptionPackage subscriptionPackage, SubscriptionPackageModel model, boolean isRestrictedPackage, boolean isPreviousStepperCompleted) {
        log.info("Access control phase starts");
        long start = new Date().getTime();
        long profilingStart;
        long profilingEnd;

        boolean isStepperCompleted = false;
        if (DBConstants.ACCESS_CONTROL.equals(subscriptionPackage.getStatus()) || (isRestrictedPackage && DBConstants.ACCESS_CONTROL.equals(subscriptionPackage.getPostCompletionStatus())) || model.isSaveAsDraft()) {
            boolean isNewPackage = subscriptionPackage.getSubscriptionPackageId() == null || subscriptionPackage.getSubscriptionPackageId() == 0;
            AccessModel accessModel = model.getAccess();
            boolean isRestrictedAccess = accessModel.isRestrictedAccess();
            subscriptionPackage.setRestrictedAccess(isRestrictedAccess);
            boolean isAccessCompleted = !isRestrictedAccess;
            if (isRestrictedAccess) {
                List<Long> memberAccessIdList = accessModel.getClientIdList();
                List<String> externalClientEmailList = accessModel.getExternalClientEmailList();
                profilingStart = new Date().getTime();
                if ((memberAccessIdList == null || memberAccessIdList.isEmpty()) && (externalClientEmailList == null || externalClientEmailList.isEmpty())) {
                    if (!model.isSaveAsDraft()) {
                        throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_ACCESS_MEMBERS_NOT_SET, MessageConstants.ERROR);
                    }
                } else {
                    List<User> clientList = instructorSubscriptionService.getClientsOfInstructor();
                    if ( memberAccessIdList != null && !memberAccessIdList.isEmpty() && clientList == null) {
                        throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_NO_CLIENTS, MessageConstants.ERROR);
                    }
                    //Saving clients
                    if (!isNewPackage) {
                        List<PackageMemberMapping> packageMemberList = packageMemberMappingRepository.findBySubscriptionPackage(subscriptionPackage);
                        if (!packageMemberList.isEmpty()) {
                            packageMemberMappingRepository.deleteInBatch(packageMemberList);
                        }
                    }
                    List<Long> clientIdList = new ArrayList<>();
                    if(clientList != null && !clientList.isEmpty()){
                        clientIdList = clientList.stream().map(User::getUserId).collect(Collectors.toList());
                    }
                    List<PackageMemberMapping> packageMemberMappingList = new ArrayList<>();
                    for (Long memberId : memberAccessIdList) {
                        if (memberId == null || memberId == 0) {
                            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_ID_NULL, MessageConstants.ERROR);
                        }
                        User member = userRepository.findByUserId(memberId);
                        if (member == null) {
                            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_FOUND, MessageConstants.ERROR);
                        }
                        if (!fitwiseUtils.isMember(member)) {
                            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_MEMBER, MessageConstants.ERROR);
                        }

                        if (!clientIdList.contains(memberId)) {
                            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_MEMBER_IS_NOT_CLIENT, MessageConstants.ERROR);
                        }

                        PackageMemberMapping packageMemberMapping = new PackageMemberMapping();
                        packageMemberMapping.setSubscriptionPackage(subscriptionPackage);
                        packageMemberMapping.setUser(member);

                        packageMemberMappingList.add(packageMemberMapping);
                    }
                    if (!packageMemberMappingList.isEmpty()) {
                        subscriptionPackage.setPackageMemberMapping(packageMemberMappingList);
                    }
                    profilingEnd = new Date().getTime();
                    log.info("DB update for package clients : Time taken in millis : "+(profilingEnd-profilingStart));

                    //Saving external clients
                    profilingStart = new Date().getTime();
                    if (!isNewPackage) {
                        List<PackageExternalClientMapping> packageExternalClientMappings = packageExternalClientMappingRepository.findBySubscriptionPackage(subscriptionPackage);
                        if (!packageExternalClientMappings.isEmpty()) {
                            packageExternalClientMappingRepository.deleteInBatch(packageExternalClientMappings);
                        }
                    }

                    List<PackageExternalClientMapping> packageExternalClientMappings = new ArrayList<>();
                    for (String externalClientEmail : externalClientEmailList) {
                        ValidationUtils.validateEmail(externalClientEmail);

                        PackageExternalClientMapping externalClientMapping = new PackageExternalClientMapping();
                        externalClientMapping.setSubscriptionPackage(subscriptionPackage);

                        User member = userRepository.findByEmail(externalClientEmail);
                        if (member != null && fitwiseUtils.isMember(member)) {
                            externalClientMapping.setUser(member);
                        }
                        externalClientMapping.setExternalClientEmail(externalClientEmail);

                        packageExternalClientMappings.add(externalClientMapping);
                    }
                    if (!packageExternalClientMappings.isEmpty()) {
                        subscriptionPackage.setExternalMemberMapping(packageExternalClientMappings);
                    }
                    profilingEnd = new Date().getTime();
                    log.info("DB update for package external clients : Time taken in millis : "+(profilingEnd-profilingStart));
                    isAccessCompleted = true;
                }
            }

            subscriptionPackage.setClientMessage(model.getClientMessage());

            //Ignoring Status changes for RestrictedProgram
            if (isPreviousStepperCompleted && isAccessCompleted) {
                if (!isRestrictedPackage) {
                    subscriptionPackage.setStatus(DBConstants.CONFIGURE);
                } else {
                    subscriptionPackage.setPostCompletionStatus(DBConstants.CONFIGURE);
                }
                isStepperCompleted = true;

            }
        }
        profilingEnd = new Date().getTime();
        log.info("Access control phase  : Total Time taken in millis : "+(profilingEnd-start));
        log.info("Access control phase ends");

        return isStepperCompleted;
    }

    /**
     * @param subscriptionPackage
     * @param model
     * @param isRestrictedPackage
     * @param isPreviousStepperCompleted
     * @return
     */
    private boolean constructConfiguration(SubscriptionPackage subscriptionPackage, SubscriptionPackageModel model, boolean isRestrictedPackage, boolean isPreviousStepperCompleted) {
        log.info("Configuration phase starts");
        long start = new Date().getTime();
        long profilingStart;
        boolean isStepperCompleted = false;
        profilingStart = new Date().getTime();
        User currentUser = userComponents.getUser();
        if (DBConstants.CONFIGURE.equals(subscriptionPackage.getStatus()) || (isRestrictedPackage && DBConstants.CONFIGURE.equals(subscriptionPackage.getPostCompletionStatus())) || model.isSaveAsDraft()) {
            boolean isProgramConfigurationDone = false;
            boolean isNewPackage = subscriptionPackage.getSubscriptionPackageId() == null || subscriptionPackage.getSubscriptionPackageId() == 0;
            log.info("Basic validations : time taken in millis : "+(new Date().getTime()-profilingStart));
            //Configuring programs
            profilingStart = new Date().getTime();
            List<PackageProgramMapping> packageProgramMappings = new ArrayList<>();
            if (model.getProgramIdList() != null && !model.getProgramIdList().isEmpty()) {
            	if (!isNewPackage) {
                    List<PackageProgramMapping> packageProgramsListMapping = packageProgramMappingRepository.findBySubscriptionPackage(subscriptionPackage);
                    if (packageProgramsListMapping != null && !packageProgramsListMapping.isEmpty()) {
                        packageProgramMappingRepository.deleteInBatch(packageProgramsListMapping);
                    }
                }
                for (Long programId : model.getProgramIdList().stream().distinct().collect(Collectors.toList())) {
                    Programs program = programRepository.findByProgramIdAndOwnerUserId(programId, currentUser.getUserId());
                    ValidationUtils.throwException(program == null, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, Constants.BAD_REQUEST);
                    PackageProgramMapping packageProgramMapping = new PackageProgramMapping();
                    packageProgramMapping.setProgram(program);
                    packageProgramMapping.setSubscriptionPackage(subscriptionPackage);
                    packageProgramMappings.add(packageProgramMapping);
                }
                subscriptionPackage.setPackageProgramMapping(packageProgramMappings);
                  if (!isNewPackage) {
                    long subscriptionCount = subscriptionService.getActiveSubscriptionCountForPackage(subscriptionPackage.getSubscriptionPackageId());
                    if (subscriptionCount > 0) {
                    	  List<PackageSubscription> totalPackageSubscriptions = packageSubscriptionRepository.findBySubscriptionPackageSubscriptionPackageId(subscriptionPackage.getSubscriptionPackageId());
                          List<PackageSubscription> onlySubscribedPackages = new ArrayList<>();
                          for (PackageSubscription packageSubscription : totalPackageSubscriptions) {
                              if (packageSubscription.getUser() != null) {
                                  SubscriptionStatus subscriptionStatus = subscriptionService.getMemberPackageSubscriptionStatus(packageSubscription);
                                  // Getting only the active subscribed programs
                                  if (subscriptionStatus != null && (KeyConstants.KEY_PAID.equals(subscriptionStatus.getSubscriptionStatusName()) || KeyConstants.KEY_PAYMENT_PENDING.equals(subscriptionStatus.getSubscriptionStatusName()))) {
                                      onlySubscribedPackages.add(packageSubscription);
                                   
                                  }
                              }
                          }
                          for(PackageSubscription packageSubscriptionUser : onlySubscribedPackages){
                        	  Date now = new Date();
                        	  SubscriptionStatus newSubscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_PAID);
                        	  SubscriptionPackage subscriptionPackageUser = packageSubscriptionUser.getSubscriptionPackage();
                              List<PackageProgramSubscription> packageProgramSubscriptions = new ArrayList<>();
                              if (subscriptionPackageUser.getPackageProgramMapping() != null && !subscriptionPackageUser.getPackageProgramMapping().isEmpty()) {
                                  for (PackageProgramMapping packageProgramMapping : subscriptionPackageUser.getPackageProgramMapping()) {
                                      Programs program = packageProgramMapping.getProgram();

                                      PackageProgramSubscription packageProgramSubscription = packageProgramSubscriptionRepository.findByPackageSubscriptionAndUserAndProgram(packageSubscriptionUser, packageSubscriptionUser.getUser(), program);
                                      if (packageProgramSubscription == null) {
                                          packageProgramSubscription = new PackageProgramSubscription();
                                      }

                                      packageProgramSubscription.setPackageSubscription(packageSubscriptionUser);
                                      packageProgramSubscription.setUser(packageSubscriptionUser.getUser());
                                      packageProgramSubscription.setProgram(program);
                                      packageProgramSubscription.setSubscribedDate(now);
                                      packageProgramSubscription.setSubscriptionPlan(packageSubscriptionUser.getSubscriptionPlan());
                                      packageProgramSubscription.setSubscribedViaPlatform(packageSubscriptionUser.getSubscribedViaPlatform());
                                      packageProgramSubscription.setSubscriptionStatus(newSubscriptionStatus);

                                      packageProgramSubscriptions.add(packageProgramSubscription);
                                  }
                              }
                              packageSubscriptionUser.setPackageProgramSubscription(packageProgramSubscriptions);
                        	  
                          }
                    }
                }
                  isProgramConfigurationDone = true;
                  log.info("DB update for package programs : time taken in millis : "+(new Date().getTime()-profilingStart));
               
            }
            //Configuring session
            List<PackageKloudlessMapping> packageKloudlessMappings = new ArrayList<>();
            boolean isSessionConfigurationDone = false;
            int schedule_Count = 0;
            if (!isNewPackage) {
            	schedule_Count = userKloudlessScheduleRepository.countBySubscriptionPackageSubscriptionPackageId(subscriptionPackage.getSubscriptionPackageId());
         
            }
            if (schedule_Count > 0) {
            	if (InstructorConstant.UNPUBLISH.equals(subscriptionPackage.getStatus())) {
                  throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_PACKAGE_CANT_EDITED_IN_SCHEDULE, MessageConstants.ERROR);
               }
        	  isSessionConfigurationDone = true;
              }
          else {
        	  
          

            if (model.getMeetings() != null && !model.getMeetings().isEmpty()) {
                if (!isNewPackage) {
                    profilingStart = new Date().getTime();
                    for(MeetingModel meetingModel : model.getMeetings()){
                        PackageKloudlessMapping packageKloudlessMapping = packageKloudlessMappingRepository.findTop1BySubscriptionPackageAndUserKloudlessMeetingUserKloudlessMeetingId(subscriptionPackage,meetingModel.getMeetingId());
                        if(packageKloudlessMapping == null){
                            validateSessionTitleInPackage(meetingModel.getTitle(),subscriptionPackage.getSubscriptionPackageId());
                        }
                    }
                    
                    log.info("Validate session title inside package : time taken in millis : "+(new Date().getTime()-profilingStart));
                   profilingStart = new Date().getTime();
                   List<PackageKloudlessMapping> packageKloudlessMappingList = packageKloudlessMappingRepository.findBySubscriptionPackage(subscriptionPackage);
                   
                    if (packageKloudlessMappingList != null && !packageKloudlessMappingList.isEmpty()) {
                        packageKloudlessMappingRepository.deleteInBatch(packageKloudlessMappingList);
                    }
                }
                Set<Long> newScheduleOrderSet = new HashSet<>();
               for (MeetingModel meetingModel : model.getMeetings()) {
                    //Session Order validation
                    if (meetingModel.getOrder() == null) {
                        throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_MEETING_ORDER_MISSING, MessageConstants.ERROR);
                    }
                    if (meetingModel.getOrder().intValue() <= 0) {
                        throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_MEETING_ORDER_CANT_BE_ZERO, MessageConstants.ERROR);
                    }
                    if (meetingModel.getCountPerPackage() <= 0) {
                        throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_MEETING_COUNT_CANT_BE_ZERO, MessageConstants.ERROR);
                    }
                    int oldUniqueScheduleOrderSize = newScheduleOrderSet.size();
                    Long newScheduleOrder = meetingModel.getOrder();
                    newScheduleOrderSet.add(newScheduleOrder);
                    int newUniqueScheduleOrderSize = newScheduleOrderSet.size();
                    if (oldUniqueScheduleOrderSize == newUniqueScheduleOrderSize || newScheduleOrder > model.getMeetings().size()) {
                        throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_MEETING_ORDER_INCORRECT, MessageConstants.ERROR);
                    }
                    UserKloudlessMeeting userKloudlessMeeting = userKloudlessMeetingRepository.findByUserKloudlessMeetingId(meetingModel.getMeetingId());
                    ValidationUtils.throwException(userKloudlessMeeting == null, ValidationMessageConstants.MSG_MEETING_NOT_FOUND, Constants.BAD_REQUEST);
                    PackageKloudlessMapping packageKloudlessMapping = new PackageKloudlessMapping();
                    packageKloudlessMapping.setOrder(meetingModel.getOrder());
                    packageKloudlessMapping.setUserKloudlessMeeting(userKloudlessMeeting);
                    packageKloudlessMapping.setSubscriptionPackage(subscriptionPackage);
                    packageKloudlessMapping.setTotalSessionCount(meetingModel.getCountPerPackage());
                    packageKloudlessMapping.setTitle(meetingModel.getTitle());
                    /*
                     * Meeting Url validation for Virtual/Both
                     * */
                    String meetingType = userKloudlessMeeting.getCalendarMeetingType().getMeetingType();
                    if (DBConstants.INPERSON_OR_VIRTUAL_SESSION.equalsIgnoreCase(meetingType) || DBConstants.VIRTUAL_SESSION.equalsIgnoreCase(meetingType)) {
                        if(StringUtils.isEmpty(meetingModel.getMeetingUrl())){
                            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_INSTRUCTOR_MEETING_REQUIRED, null);
                        }else if(!ValidationUtils.isUrlValid(meetingModel.getMeetingUrl())){
                            throw new ApplicationException(Constants.BAD_REQUEST, CalendarConstants.CAL_ERR_INSTRUCTOR_INVALID_MEETING_URL, null);
                        }
                        packageKloudlessMapping.setMeetingUrl(meetingModel.getMeetingUrl());
                    }
                    if(userKloudlessMeeting.getCalendarMeetingType().getMeetingTypeId() == CalendarConstants.SESSION_IN_PERSON ||
                            (userKloudlessMeeting.getCalendarMeetingType().getMeetingTypeId() == CalendarConstants.SESSION_IN_PERSON_OR_VIRTUAL)){
                        if(meetingModel.getLocationId() != null){
                            Location location = locationRepository.findByUserUserIdAndLocationId(subscriptionPackage.getOwner().getUserId(),meetingModel.getLocationId());
                            if(location == null){
                                throw new ApplicationException(Constants.BAD_REQUEST,ValidationMessageConstants.MSG_LOCATION_NOT_FOUND,MessageConstants.ERROR);
                            }
                            packageKloudlessMapping.setLocation(location);
                        }else{
                            throw new ApplicationException(Constants.BAD_REQUEST,MessageConstants.MSG_LOCATION_NOT_ADDED,MessageConstants.ERROR);
                        }
                    }
                    packageKloudlessMappings.add(packageKloudlessMapping);
                }
                subscriptionPackage.setPackageKloudlessMapping(packageKloudlessMappings);
                isSessionConfigurationDone = true;
                log.info("DB update for package sessions : time taken in millis : "+(new Date().getTime()-profilingStart));
            }
          }
            //Configuring Cancellation
            profilingStart = new Date().getTime();
            boolean isCancellationConfigured = false;
            CancellationDurationModel cancellationDurationModel = model.getCancellationDuration();
            if (cancellationDurationModel != null && (cancellationDurationModel.getIsDays() != null && (cancellationDurationModel.getDays() != null || cancellationDurationModel.getHours() != null))) {
                TimeSpan cancellationDuration;
                if (!isNewPackage && subscriptionPackage.getCancellationDuration() != null) {
                    cancellationDuration = subscriptionPackage.getCancellationDuration();
                } else {
                    cancellationDuration = new TimeSpan();
                }
                if (cancellationDurationModel.getIsDays() != null && cancellationDurationModel.getIsDays()) {
                    cancellationDuration.setDays(cancellationDurationModel.getDays());
                    cancellationDuration.setHours(null);
                } else {
                    cancellationDuration.setHours(cancellationDurationModel.getHours());
                    cancellationDuration.setDays(null);
                }
                subscriptionPackage.setCancellationDuration(cancellationDuration);
                isCancellationConfigured = true;
            }
            log.info("Validations and DB update for cancellation duration : time taken in millis : "+(new Date().getTime()-profilingStart));
            //Ignoring Status changes for RestrictedProgram

            if (isPreviousStepperCompleted && (isProgramConfigurationDone || (isSessionConfigurationDone && isCancellationConfigured))) {
                if (!isRestrictedPackage) {
                    subscriptionPackage.setStatus(InstructorConstant.PRICE);
                } else {
                    subscriptionPackage.setPostCompletionStatus(InstructorConstant.PRICE);
                }
                isStepperCompleted = true;
            }
        }
        log.info("Configuration phase : total time taken in millis : "+(new Date().getTime()-start));
        log.info("Configuration phase ends");
        return isStepperCompleted;
    }

    /**
     * @param subscriptionPackage
     * @param model
     * @param isRestrictedPackage
     * @param isPreviousStepperCompleted
     * @return
     */
    private boolean constructPrice(SubscriptionPackage subscriptionPackage, SubscriptionPackageModel model, boolean isRestrictedPackage, boolean isPreviousStepperCompleted) {
        log.info("Price phase starts");
        long start = new Date().getTime();
        long profilingStart;

        boolean isStepperCompleted = false;
        if (InstructorConstant.PRICE.equals(subscriptionPackage.getStatus()) || (isRestrictedPackage && InstructorConstant.PRICE.equals(subscriptionPackage.getPostCompletionStatus())) || model.isSaveAsDraft()) {
           profilingStart = new Date().getTime();
            if (model.getPrice() != null && model.getPrice() != 0) {
                if (model.getPrice() < StripeConstants.STRIPE_MINIMUM_PRICE) {
                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERR_PRODUCT_MIN_PRICE, null);
                }

                if (model.getPrice() > StripeConstants.STRIPE_MAXIMUM_PRICE) {
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PACKAGE_PRICE_MAX_LIMIT_REACHED, null);
                }

                subscriptionPackage.setPrice(model.getPrice());

                boolean isNewPackage = subscriptionPackage.getSubscriptionPackageId() == null || subscriptionPackage.getSubscriptionPackageId() == 0;
                log.info("Price validations and DB update : Time taken in millis : "+(new Date().getTime()-profilingStart));
                profilingStart = new Date().getTime();
                if (!isNewPackage) {
                    List<SubscriptionPackagePriceByPlatform> subscriptionPackagePriceByPlatformList = subscriptionPackagePriceByPlatformRepository.findBySubscriptionPackage(subscriptionPackage);
                    if (!subscriptionPackagePriceByPlatformList.isEmpty()) {
                        subscriptionPackagePriceByPlatformRepository.deleteInBatch(subscriptionPackagePriceByPlatformList);
                    }
                }

                PlatformType platformType = platformTypeRepository.findByPlatform(DBConstants.WEB);
                PlatformWiseTaxDetail platformWiseTaxDetail = platformWiseTaxDetailRepository.findByActiveAndPlatformType(true, platformType);

                List<SubscriptionPackagePriceByPlatform> subscriptionPackagePriceByPlatforms = new ArrayList<>();
                SubscriptionPackagePriceByPlatform subscriptionPackagePriceByPlatform = new SubscriptionPackagePriceByPlatform();
                subscriptionPackagePriceByPlatform.setSubscriptionPackage(subscriptionPackage);
                subscriptionPackagePriceByPlatform.setPlatformWiseTaxDetail(platformWiseTaxDetail);
                subscriptionPackagePriceByPlatform.setPrice(model.getPrice());

                subscriptionPackagePriceByPlatforms.add(subscriptionPackagePriceByPlatform);

                subscriptionPackage.setPackagePriceByPlatforms(subscriptionPackagePriceByPlatforms);
                log.info("Price by platforms DB update : Time taken in millis : "+(new Date().getTime()-profilingStart));

                //Ignoring Status changes for RestrictedProgram
                if (isPreviousStepperCompleted) {
                    if (!isRestrictedPackage) {
                    	if(InstructorConstant.PUBLISH.equals(subscriptionPackage.getStatus())) {
                    		subscriptionPackage.setStatus(InstructorConstant.PUBLISH);
                    	}else {
                    		subscriptionPackage.setStatus(InstructorConstant.PRE_PUBLISH);
                    	}
                        
                    } else {
                        subscriptionPackage.setPostCompletionStatus(InstructorConstant.PRE_PUBLISH);
                    }
                    isStepperCompleted = true;
                }
            }
            profilingStart = new Date().getTime();
            if(model.getDiscountOffersIds()!=null && !model.getDiscountOffersIds().isEmpty()) {
                constructDiscountsOffer(subscriptionPackage, model);
            }
            log.info("Construct discount offers : Time taken in millis : "+(new Date().getTime()-profilingStart));

            profilingStart = new Date().getTime();
            checkPackagePriceWithOffers(subscriptionPackage);
            log.info("Validating package price with its offers price : Time taken in millis : "+(new Date().getTime()-profilingStart));

        }
        log.info("Price phase : Total Time taken in millis : "+(new Date().getTime()-start));
        log.info("Price phase ends");
        return isStepperCompleted;
    }

    public void constructDiscountsOffer(SubscriptionPackage subscriptionPackage, SubscriptionPackageModel model){
        List<Long> discountOfferIds = model.getDiscountOffersIds();
        constructDiscountsOffer(subscriptionPackage,discountOfferIds);
    }

    /**
     * Construct subscription package offers
     * @param subscriptionPackage
     * @param discountOfferIds
     */
    public void constructDiscountsOffer(SubscriptionPackage subscriptionPackage, List<Long> discountOfferIds){
        long profilingStart;
        List<PackageOfferMapping> packageOfferMappings = new ArrayList<>();
        int newPackageOfferMappingsForExistingUser = 0;
        int existingPackageOfferMappingsForExistingUsers = 0;
        int newPackageOfferMappingsForNewUser = 0;
        int existingPackageOfferMappingsForNewUser = 0;
        boolean isNewPackage = subscriptionPackage.getSubscriptionPackageId() == null || subscriptionPackage.getSubscriptionPackageId() == 0;
        Date now = new Date();
        profilingStart = new Date().getTime();
        for (Long offerId : discountOfferIds) {
            Optional<OfferCodeDetail> offerCodeDetail = offerCodeDetailRepository.findById(offerId);
            if(!offerCodeDetail.isPresent()){
                throw new ApplicationException(Constants.BAD_REQUEST, "Offer Code not found", null);
            }
            OfferCodeDetail ofCodeDetail = offerCodeDetail.get();
            PackageOfferMapping packageOfferMapping = packageOfferMappingRepository
                    .findBySubscriptionPackageSubscriptionPackageIdAndOfferCodeDetailOfferCodeId(subscriptionPackage.getSubscriptionPackageId(),
                            ofCodeDetail.getOfferCodeId());
            if (packageOfferMapping == null) {
                //Checking if offer code is used in some other subscription package or program
                PackageOfferMapping offerMapping = packageOfferMappingRepository.findByOfferCodeDetailOfferCodeId(ofCodeDetail.getOfferCodeId());
                if (offerMapping != null) {
                    throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_OFFER_CODE_ALREADY_MAPPED_TO_PACKAGE, null);
                }
                DiscountOfferMapping discountOfferMapping = discountOfferMappingRepository.findByOfferCodeDetailOfferCodeId(ofCodeDetail.getOfferCodeId());
                if (discountOfferMapping != null) {
                    throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_OFFER_CODE_ALREADY_MAPPED_TO_PROGRAM, null);
                }

                if (ofCodeDetail.getOfferStartingDate().before(now) && ofCodeDetail.getOfferEndingDate().before(now)) {
                    throw new ApplicationException(Constants.BAD_REQUEST, "Expired Offers can not be added", null);
                }
                if (ofCodeDetail.getIsNewUser()) {
                    List<PackageOfferMapping> packageOfferMappingsList = packageOfferMappingRepository.findBySubscriptionPackageSubscriptionPackageIdAndOfferCodeDetailIsNewUserAndOfferCodeDetailIsInUseAndOfferCodeDetailOfferStatus
                            (subscriptionPackage.getSubscriptionPackageId(), true, true, DiscountsConstants.OFFER_ACTIVE);
                    for (PackageOfferMapping subscriptionPackageOfferMapping : packageOfferMappingsList) {
                        //Cannot add two current offers for new user
                        if (subscriptionPackageOfferMapping.getOfferCodeDetail().getOfferStartingDate().after(now) && ofCodeDetail.getOfferStartingDate().after(now)) {
                            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_OFFER_UPCOMING_OFFER_NEW_USERS, null);
                        }
                        //Cannot add two current offers for new user
                        if (subscriptionPackageOfferMapping.getOfferCodeDetail().getOfferStartingDate().before(now) && ofCodeDetail.getOfferStartingDate().before(now)
                                && subscriptionPackageOfferMapping.getOfferCodeDetail().getOfferEndingDate().after(now) && ofCodeDetail.getOfferEndingDate().after(now)) {
                            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_OFFER_CURRENT_OFFER_NEW_USERS, null);
                        }
                        //Adding upcoming offer first and then adding current
                        if ((ofCodeDetail.getOfferStartingDate().before(subscriptionPackageOfferMapping.getOfferCodeDetail().getOfferStartingDate()) && !fitwiseUtils.isSameDay(ofCodeDetail.getOfferEndingDate(), subscriptionPackageOfferMapping.getOfferCodeDetail().getOfferStartingDate()) && ofCodeDetail.getOfferEndingDate().after(subscriptionPackageOfferMapping.getOfferCodeDetail().getOfferStartingDate()))) {
                            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_OFFER_END_DATE_NEW_USERS, null);
                        }
                        //Adding current offer first and then adding upcoming
                        if (ofCodeDetail.getOfferStartingDate().after(subscriptionPackageOfferMapping.getOfferCodeDetail().getOfferStartingDate()) && !fitwiseUtils.isSameDay(ofCodeDetail.getOfferStartingDate(), subscriptionPackageOfferMapping.getOfferCodeDetail().getOfferEndingDate()) && ofCodeDetail.getOfferStartingDate().before(subscriptionPackageOfferMapping.getOfferCodeDetail().getOfferEndingDate())) {
                            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_OFFER_START_DATE_NEW_USERS, null);
                        }
                    }
                }
                packageOfferMapping = new PackageOfferMapping();
                //New Discount-iTMS upload needed
                packageOfferMapping.setNeedDiscountUpdate(true);
                packageOfferMapping.setDiscountStatus(DiscountsConstants.NEW_DISCOUNT);
                //Mail
                packageOfferMapping.setNeedMailUpdate(true);
                if (ofCodeDetail.getOfferPrice() != null && ofCodeDetail.getOfferPrice().getPrice() >= subscriptionPackage.getPrice()) {
                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_OFFER_PRICE_GREATER_THAN_PACKAGE_PRICE, null);

                }
                if (ofCodeDetail.getIsNewUser()) {
                    newPackageOfferMappingsForNewUser++;
                } else {
                    newPackageOfferMappingsForExistingUser++;
                }
                //Creating coupon on Stripe for the offer code
                long temp = new Date().getTime();
                stripeService.createCoupon(ofCodeDetail, subscriptionPackage.getPrice());
                log.info("Creating stripe coupon for package offer : Time taken in millis : " + (new Date().getTime() - temp));
            }
            packageOfferMapping.setSubscriptionPackage(subscriptionPackage);
            DiscountLevel discountLevel = discountLevelRepository.findByDiscountLevelName(DiscountsConstants.PROGRAM_LEVEL);
            packageOfferMapping.setDiscountLevel(discountLevel);
            packageOfferMapping.setOfferCodeDetail(ofCodeDetail);
            packageOfferMapping.setInstructor(userComponents.getUser());
            packageOfferMappings.add(packageOfferMapping);

        }
        log.info("Price offers DB update : Time taken in millis : "+(new Date().getTime()-profilingStart));
        profilingStart = new Date().getTime();
        if(!isNewPackage){
            List<PackageOfferMapping> packageOfferMappingList = packageOfferMappingRepository.findBySubscriptionPackageSubscriptionPackageIdAndOfferCodeDetailIsInUseAndOfferCodeDetailOfferStatus
                    (subscriptionPackage.getSubscriptionPackageId(),true,DiscountsConstants.OFFER_ACTIVE);
            for(PackageOfferMapping packageOfferMapping : packageOfferMappingList){
                if (!packageOfferMapping.getOfferCodeDetail().getOfferEndingDate().before(now)) {
                    if(packageOfferMapping.getOfferCodeDetail().getIsNewUser()){
                        existingPackageOfferMappingsForNewUser++;
                    }else{
                        existingPackageOfferMappingsForExistingUsers++;
                    }
                }
            }
        }
        //Apple Validation : You can have up to 10 active promotional offers within the <promotional_offers> block.
        //Apple Validation : You can have up to 10 active promotional offers within the <promotional_offers> block.
        if(newPackageOfferMappingsForExistingUser + existingPackageOfferMappingsForExistingUsers > KeyConstants.KEY_MAX_OFFER_COUNT_EXISTING_USERS) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PACKAGE_OFFER_REACHED_MAX_COUNT_EXISTING_USERS, null);
        }
        if(newPackageOfferMappingsForNewUser + existingPackageOfferMappingsForNewUser > KeyConstants.KEY_MAX_OFFER_COUNT_NEW_USERS) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PACKAGE_OFFER_REACHED_MAX_COUNT_NEW_USERS, null);
        }
        log.info("Package offer count validations : Time taken in millis : "+(new Date().getTime()-profilingStart));
        subscriptionPackage.setPackageOfferMappings(packageOfferMappings);
    }

    /**
     * Check for whether subscription package price is lower than any of the offer price
     * @param subscriptionPackage
     */
    public void checkPackagePriceWithOffers(SubscriptionPackage subscriptionPackage){
        if(subscriptionPackage.getPrice() != null){
            Double subscriptionPackagePrice = subscriptionPackage.getPrice();
            Double offerPrice;
            List<PackageOfferMapping> packageOfferMappings = packageOfferMappingRepository.findBySubscriptionPackageSubscriptionPackageIdAndOfferCodeDetailIsInUseAndOfferCodeDetailOfferStatus(subscriptionPackage.getSubscriptionPackageId(), true,DiscountsConstants.OFFER_ACTIVE);
            if(!packageOfferMappings.isEmpty()){
                for(PackageOfferMapping packageOfferMapping : packageOfferMappings){
                    OfferCodeDetail offerCodeDetail = offerCodeDetailRepository.findByOfferCodeId(packageOfferMapping.getOfferCodeDetail().getOfferCodeId());
                    if(offerCodeDetail.getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_PAY_AS_YOU_GO)){
                        offerPrice = offerCodeDetail.getOfferPrice().getPrice();
                        if(offerPrice >= subscriptionPackagePrice){
                            throw new ApplicationException(Constants.CONTENT_NEEDS_TO_BE_VALIDATE, MessageConstants.MSG_ERR_SUBSCRIPTION_PACKAGE_OFFER_UPDATE, null);
                        }
                    }
                }
            }
        }
    }

    /**
     * Method to construct SubscriptionPackage response view
     * @param subscriptionPackage
     * @return
     */
    public SubscriptionPackageView constructSubscriptionPackageResponse(SubscriptionPackage subscriptionPackage) {
        log.info("Subscription Package response construction starts");
        long profilingStart = new Date().getTime();
        User instructor = subscriptionPackage.getOwner();
        UserProfile instructorProfile = userProfileRepository.findByUser(instructor);
        SubscriptionPackageView subscriptionPackageView = new SubscriptionPackageView();
        subscriptionPackageView.setSubscriptionPackageId(subscriptionPackage.getSubscriptionPackageId());
        subscriptionPackageView.setTitle(subscriptionPackage.getTitle());
//        subscriptionPackageView.setShortDescription(subscriptionPackage.getShortDescription());
        subscriptionPackageView.setDescription(subscriptionPackage.getDescription());
        subscriptionPackageView.setInstructorFirstName(instructorProfile.getFirstName());
        subscriptionPackageView.setInstructorLastName(instructorProfile.getLastName());
        if(instructorProfile.getProfileImage() != null){
            subscriptionPackageView.setInstructorProfileUrl(instructorProfile.getProfileImage().getImagePath());
        }
        if (subscriptionPackage.getImage() != null) {
            subscriptionPackageView.setImageId(subscriptionPackage.getImage().getImageId());
            subscriptionPackageView.setImageUrl(subscriptionPackage.getImage().getImagePath());
        }
        if (subscriptionPackage.getPackageDuration() != null) {
            subscriptionPackageView.setDuration(subscriptionPackage.getPackageDuration());
        }
        if((subscriptionPackage.getPromotion() != null && subscriptionPackage.getPromotion().getVideoManagement() != null) && (userComponents.getRole().equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR) ||
                (userComponents.getRole().equalsIgnoreCase(KeyConstants.KEY_ADMIN) && subscriptionPackage.getPromotion().getVideoManagement().getUploadStatus().equalsIgnoreCase(KeyConstants.KEY_COMPLETED)))){
            subscriptionPackageView.setPromoUrl(subscriptionPackage.getPromotion().getVideoManagement().getUrl());
            subscriptionPackageView.setPromoThumbnailUrl(subscriptionPackage.getPromotion().getVideoManagement().getThumbnail().getImagePath());
            subscriptionPackageView.setPromoUploadStatus(subscriptionPackage.getPromotion().getVideoManagement().getUploadStatus());
            subscriptionPackageView.setPromotionId(subscriptionPackage.getPromotion().getPromotionId());
        }
        long profilingEnd = new Date().getTime();
        log.info("Basic details : Time taken in millis : "+(profilingEnd-profilingStart));
        profilingStart = new Date().getTime();
        List<ProgramTileModel> programTileModels = new ArrayList<>();
        if (subscriptionPackage.getPackageProgramMapping() != null && !subscriptionPackage.getPackageProgramMapping().isEmpty()) {
            List<Equipments> equipments = new ArrayList<>();
            long temp = System.currentTimeMillis();
            List<Long> programIdList = subscriptionPackage.getPackageProgramMapping().stream().map(packageProgramMapping -> packageProgramMapping.getProgram().getProgramId()).collect(Collectors.toList());
            Map<Long, Long> offerCountMap = discountsService.getNoOfCurrentAvailableOffersOfProgramsForInstructor(programIdList);
            log.info("Offer count query : time taken in millis : "+(System.currentTimeMillis() - temp));
            for (PackageProgramMapping packageProgramMapping : subscriptionPackage.getPackageProgramMapping()) {
                Programs program = packageProgramMapping.getProgram();
                //Constructing Program tile model
                ProgramTileModel programTileModel = programService.constructProgramTileModel(program, offerCountMap);
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
            //Removing duplicate Equipment list and setting in response
            List<Equipments> equipmentsList = equipments.stream()
                    .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparingLong(Equipments::getEquipmentId))),
                            ArrayList::new));
            subscriptionPackageView.setEquipments(equipmentsList);
        }
        subscriptionPackageView.setPackagePrograms(programTileModels);
        profilingEnd = new Date().getTime();
        log.info("Package program and equipment details : Time taken in millis : "+(profilingEnd-profilingStart));
        profilingStart = new Date().getTime();
        if (subscriptionPackage.getPrice() != null) {
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            String roundedPrice = decimalFormat.format(subscriptionPackage.getPrice());
            subscriptionPackageView.setPrice(subscriptionPackage.getPrice());
            subscriptionPackageView.setFormattedPrice("$" + roundedPrice);
        }
        profilingEnd = new Date().getTime();
        log.info("Price details : Time taken in millis : "+(profilingEnd-profilingStart));
        profilingStart = new Date().getTime();
        List<MeetingView> meetingViewList = new ArrayList<>();
        if (subscriptionPackage.getPackageKloudlessMapping() != null && !subscriptionPackage.getPackageKloudlessMapping().isEmpty()) {
            for (PackageKloudlessMapping packageKloudlessMapping : subscriptionPackage.getPackageKloudlessMapping()) {
                MeetingView meetingView = new MeetingView();
                meetingView.setMeetingId(packageKloudlessMapping.getUserKloudlessMeeting().getUserKloudlessMeetingId());
                meetingView.setPackageSessionMappingId(packageKloudlessMapping.getSessionMappingId());
                meetingView.setOrder(packageKloudlessMapping.getOrder());
                if(packageKloudlessMapping.getTitle() != null){
                    meetingView.setTitle(packageKloudlessMapping.getTitle());
                }
                meetingView.setCalendarMeetingType(packageKloudlessMapping.getUserKloudlessMeeting().getCalendarMeetingType());
                meetingView.setTotalSessions(packageKloudlessMapping.getTotalSessionCount());
                meetingView.setDurationMinutes(packageKloudlessMapping.getUserKloudlessMeeting().getDuration().getMinutes());
                if(packageKloudlessMapping.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingTypeId() == CalendarConstants.SESSION_IN_PERSON ||
                       packageKloudlessMapping.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingTypeId() == CalendarConstants.SESSION_IN_PERSON_OR_VIRTUAL){
                    Location location = packageKloudlessMapping.getLocation();
                    if(location != null){
                        LocationResponse locationResponse = instructorService.constructLocationResponse(location);
                        meetingView.setLocation(locationResponse);
                    }
                }
                if(!StringUtils.isEmpty(packageKloudlessMapping.getMeetingUrl())){
                    meetingView.setMeetingUrl(packageKloudlessMapping.getMeetingUrl());
                }
                int meeting_count = 0;
                meeting_count = userKloudlessScheduleRepository.countBySubscriptionPackageSubscriptionPackageIdAndUserKloudlessMeetingUserKloudlessMeetingIdAndPackageKloudlessMappingSessionMappingId(subscriptionPackage.getSubscriptionPackageId(),packageKloudlessMapping.getUserKloudlessMeeting().getUserKloudlessMeetingId(),packageKloudlessMapping.getSessionMappingId());
            	if (meeting_count > 0) {
            		if (InstructorConstant.PUBLISH.equals(subscriptionPackage.getStatus()) || InstructorConstant.UNPUBLISH.equals(subscriptionPackage.getStatus())) {
            			meetingView.setIsSchedule(InstructorConstant.ISMEETINGSCHEDULE);
            		}
            		}
                meetingViewList.add(meetingView);
            }
        }
        subscriptionPackageView.setMeetings(meetingViewList);
        profilingEnd = new Date().getTime();
        log.info("Package Kloudless meeting details : Time taken in millis : "+(profilingEnd-profilingStart));
        profilingStart = new Date().getTime();
        TimeSpan cancellationDuration = subscriptionPackage.getCancellationDuration();
        if (cancellationDuration != null) {
            CancellationDurationModel cancellationDurationModel = new CancellationDurationModel();
            boolean isDays = true;
            if (cancellationDuration.getHours() != null) {
                isDays = false;
                cancellationDurationModel.setHours(cancellationDuration.getHours());
            } else {
                cancellationDurationModel.setDays(cancellationDuration.getDays());
            }
            cancellationDurationModel.setIsDays(isDays);
            subscriptionPackageView.setCancellationDuration(cancellationDurationModel);
        }
        profilingEnd = new Date().getTime();
        log.info("Cancellation duration details : Time taken in millis : "+(profilingEnd-profilingStart));
        profilingStart = new Date().getTime();
        subscriptionPackageView.setIsRestrictedAccess(subscriptionPackage.isRestrictedAccess());
        if (subscriptionPackage.isRestrictedAccess()) {
            //Getting clients view
            List<AccessControlMemberView> accessControlMemberViewList = new ArrayList<>();
            if (subscriptionPackage.getPackageMemberMapping() != null && !subscriptionPackage.getPackageMemberMapping().isEmpty()) {
                for (PackageMemberMapping packageMemberMapping : subscriptionPackage.getPackageMemberMapping()) {
                    AccessControlMemberView accessControlMemberView = constructAccessControlMemberView(packageMemberMapping.getUser());
                    accessControlMemberViewList.add(accessControlMemberView);
                }
            }
            //Getting external clients list. If one of them is now a client, we are adding to the client list
            List<AccessControlExternalClientView> accessControlExternalClientViews = new ArrayList<>();
            if (subscriptionPackage.getExternalMemberMapping() != null && !subscriptionPackage.getExternalMemberMapping().isEmpty()) {
                List<User> clientList = instructorSubscriptionService.getClientsOfInstructor();
                List<Long> clientIdList = new ArrayList<>();
                if (clientList != null) {
                    clientIdList = clientList.stream().map(User::getUserId).collect(Collectors.toList());
                }
                for (PackageExternalClientMapping packageExternalClientMapping : subscriptionPackage.getExternalMemberMapping()) {
                    User member;
                    if (packageExternalClientMapping.getUser() != null) {
                        member = packageExternalClientMapping.getUser();
                    } else {
                        member = userRepository.findByEmail(packageExternalClientMapping.getExternalClientEmail());
                    }
                    if (member != null && clientIdList.contains(member.getUserId())) {
                        AccessControlMemberView accessControlMemberView = constructAccessControlMemberView(member);
                        accessControlMemberViewList.add(accessControlMemberView);
                    } else {
                        AccessControlExternalClientView accessControlExternalClientView = new AccessControlExternalClientView();
                        accessControlExternalClientView.setEmail(packageExternalClientMapping.getExternalClientEmail());
                        accessControlExternalClientViews.add(accessControlExternalClientView);
                    }
                }
            }
            if (!accessControlMemberViewList.isEmpty()) {
                subscriptionPackageView.setAccessControlMembers(accessControlMemberViewList);
            }
            if(!accessControlExternalClientViews.isEmpty()){
                subscriptionPackageView.setAccessControlExternalClientMembers(accessControlExternalClientViews);
            }
        }
        subscriptionPackageView.setClientMessage(subscriptionPackage.getClientMessage());
        profilingEnd = new Date().getTime();
        log.info("Package Access details : Time taken in millis : "+(profilingEnd-profilingStart));
        profilingStart = new Date().getTime();
        boolean isZoomUpdated = false;
        UserCommunicationDetail userCommunicationDetail = userCommunicationDetailRepository.findByUser(userComponents.getUser());
        if (userCommunicationDetail != null && userCommunicationDetail.getZoomId() != null) {
            isZoomUpdated = true;
        }
        subscriptionPackageView.setIsZoomLinkUpdated(isZoomUpdated);
        subscriptionPackageView.setStatus(subscriptionPackage.getStatus());
        subscriptionPackageView.setPostCompletionStatus(subscriptionPackage.getPostCompletionStatus());
        profilingEnd = new Date().getTime();
        log.info("Zoom link and package status : Time taken in millis : "+(profilingEnd-profilingStart));
        profilingStart = new Date().getTime();
        boolean isMaximumCountReachedForNewUsers = false;
        boolean isMaximumCountReachedForExistingUsers = false;
        int newUserOffers = 0;
        int existingUserOffers = 0;
        if(subscriptionPackage.getPackageOfferMappings()!=null && !subscriptionPackage.getPackageOfferMappings().isEmpty()) {
            List<ProgramDiscountMappingResponseView> currentDiscounts = new ArrayList<>();
            List<ProgramDiscountMappingResponseView> freeOffers = new ArrayList<>();
            List<ProgramDiscountMappingResponseView> paidOffers = new ArrayList<>();
            List<ProgramDiscountMappingResponseView> upcomingDiscounts = new ArrayList<>();
            List<ProgramDiscountMappingResponseView> expiredDiscounts = new ArrayList<>();
            for (PackageOfferMapping packageOfferMapping : subscriptionPackage.getPackageOfferMappings()) {
                ProgramDiscountMappingResponseView sResponseView = new ProgramDiscountMappingResponseView();
                sResponseView.setOfferMappingId(packageOfferMapping.getPackageOfferMappingId());
                sResponseView.setOfferCodeId(packageOfferMapping.getOfferCodeDetail().getOfferCodeId());
                sResponseView.setOfferName(packageOfferMapping.getOfferCodeDetail().getOfferName().trim());
                sResponseView.setOfferCode(packageOfferMapping.getOfferCodeDetail().getOfferCode().toUpperCase());
                sResponseView.setOfferMode(packageOfferMapping.getOfferCodeDetail().getOfferMode());
                sResponseView.setOfferDuration(packageOfferMapping.getOfferCodeDetail().getOfferDuration());
                sResponseView.setOfferStartDate(fitwiseUtils.formatDate(packageOfferMapping.getOfferCodeDetail().getOfferStartingDate()));
                sResponseView.setOfferEndDate(fitwiseUtils.formatDate(packageOfferMapping.getOfferCodeDetail().getOfferEndingDate()));
                sResponseView.setOfferPrice(packageOfferMapping.getOfferCodeDetail().getOfferPrice());
                String formattedPrice;
                if (packageOfferMapping.getOfferCodeDetail().getOfferPrice() != null) {
                    formattedPrice = fitwiseUtils.formatPrice(packageOfferMapping.getOfferCodeDetail().getOfferPrice().getPrice());
                } else {
                    formattedPrice = KeyConstants.KEY_CURRENCY_US_DOLLAR+KeyConstants.KEY_DEFAULT_PRICE_FORMAT;
                }
                sResponseView.setFormattedOfferPrice(formattedPrice);
                if (subscriptionPackage.getPrice() != null) {
                    double savingsAmount = subscriptionPackage.getPrice();
                    if (packageOfferMapping.getOfferCodeDetail().getOfferPrice() != null) {
                        savingsAmount = subscriptionPackage.getPrice() - packageOfferMapping.getOfferCodeDetail().getOfferPrice().getPrice();
                    }
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                    String roundedSavingsPrice = decimalFormat.format(savingsAmount);
                    sResponseView.setFormattedSavingsAmount(KeyConstants.KEY_CURRENCY_US_DOLLAR + roundedSavingsPrice);
                }
                sResponseView.setOfferStatus(packageOfferMapping.getOfferCodeDetail().getOfferStatus());
                sResponseView.setIsNewUser(packageOfferMapping.getOfferCodeDetail().getIsNewUser());
                //Offer Validity check
                Date now = new Date();
                Date offerStart = packageOfferMapping.getOfferCodeDetail().getOfferStartingDate();
                Date offerEnd = packageOfferMapping.getOfferCodeDetail().getOfferEndingDate();
                if (packageOfferMapping.getOfferCodeDetail().getOfferStatus().equalsIgnoreCase(DiscountsConstants.OFFER_ACTIVE) && packageOfferMapping.getOfferCodeDetail().isInUse()) {
                    // Current Offers
                    if ((offerStart.equals(now) || offerStart.before(now)) && (offerEnd.equals(now) || offerEnd.after(now))) {
                        if(packageOfferMapping.getOfferCodeDetail().getIsNewUser()){
                             newUserOffers++;
                        }else {
                            existingUserOffers++;
                        }
                        if (packageOfferMapping.getOfferCodeDetail().getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_FREE)) {
                            freeOffers.add(sResponseView);
                        } else if (packageOfferMapping.getOfferCodeDetail().getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_PAY_AS_YOU_GO)) {
                            paidOffers.add(sResponseView);
                        }
                    }
                    // UpComing Offers
                    else if (offerStart.after(now) && offerEnd.after(now)) {
                        if(packageOfferMapping.getOfferCodeDetail().getIsNewUser()){
                            newUserOffers++;
                        }else {
                            existingUserOffers++;
                        }
                        upcomingDiscounts.add(sResponseView);
                    }
                }else {
                    // Expired Offers
                    if (offerStart.before(now) && offerEnd.before(now)) {
                        expiredDiscounts.add(sResponseView);
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
            if(newUserOffers >= KeyConstants.KEY_MAX_OFFER_COUNT_NEW_USERS){
                 isMaximumCountReachedForNewUsers = true;
            }
            if(existingUserOffers >= KeyConstants.KEY_MAX_OFFER_COUNT_EXISTING_USERS){
                isMaximumCountReachedForExistingUsers = true;
            }
            subscriptionPackageView.setDiscountOffers(discountOffers);
            subscriptionPackageView.setMaximumCountReachedForExistingUsers(isMaximumCountReachedForExistingUsers);
            subscriptionPackageView.setMaximumCountReachedForNewUsers(isMaximumCountReachedForNewUsers);
        }
        profilingEnd = new Date().getTime();
        log.info("Offer details : Time taken in millis : "+(profilingEnd-profilingStart));
        int paidSubscriptions = (int) subscriptionService.getActiveSubscriptionCountForPackage(subscriptionPackage.getSubscriptionPackageId());
        subscriptionPackageView.setActiveSubscriptions(paidSubscriptions);
        log.info("Paid subscriptions of a package : Time taken in millis : "+(new Date().getTime()-profilingEnd));
        if (KeyConstants.KEY_PUBLISH.equalsIgnoreCase(subscriptionPackage.getStatus())) {
            Date publishedDate = subscriptionPackage.getPublishedDate();
            subscriptionPackageView.setPublishedDate(fitwiseUtils.formatDate(publishedDate));
        }
        AppConfigKeyValue appConfig = appConfigKeyValueRepository.findByKeyString(AppConfigConstants.ADMIN_CONFIG_FLAT_TAX);
        if (appConfig != null) {
        	double flatTax = Double.parseDouble(appConfig.getValueString());
        	subscriptionPackageView.setFlatTax(flatTax);
        }
        log.info("Subscription Package response construction ends");
        return subscriptionPackageView;
    }

    private AccessControlMemberView constructAccessControlMemberView(User user) {
        AccessControlMemberView accessControlMemberView = new AccessControlMemberView();
        accessControlMemberView.setUserId(user.getUserId());
        accessControlMemberView.setEmail(user.getEmail());
        UserProfile userProfile = userProfileRepository.findByUser(user);
        accessControlMemberView.setName(userProfile.getFirstName());
        if (userProfile.getProfileImage() != null) {
            accessControlMemberView.setImageUrl(userProfile.getProfileImage().getImagePath());
        }
        return accessControlMemberView;
    }

    /**
     * Method to get instructor's SubscriptionPackage
     * @param subscriptionPackageId
     * @return
     */
    public SubscriptionPackageView getSubscriptionPackage(Long subscriptionPackageId) {
        User currentUser = userComponents.getUser();
        SubscriptionPackage subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageIdAndOwnerUserId(subscriptionPackageId, currentUser.getUserId());
        ValidationUtils.throwException(subscriptionPackage == null, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_NOT_FOUND, Constants.BAD_REQUEST);
        return constructSubscriptionPackageResponse(subscriptionPackage);
    }

    /**
     * Method to get Session types
     * @return
     */
    public Map<String, Object> getSessionTypes() {
        List<SessionType> sessionTypeList = sessionTypeRepository.findAll();
        if (sessionTypeList.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, MessageConstants.ERROR);
        }
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put(KeyConstants.KEY_SESSION_TYPES, sessionTypeList);
        return responseMap;
    }

    /**
     * Method to publish SubscriptionPackage
     * @param subscriptionPackageId
     */
    public void publishSubscriptionPackage(Long subscriptionPackageId) throws StripeException {
        log.info("Publish subscription package starts.");
        long apiStartTimeMillis = new Date().getTime();
        User currentUser = userComponents.getUser();
        SubscriptionPackage subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageIdAndOwnerUserId(subscriptionPackageId, currentUser.getUserId());
        ValidationUtils.throwException(subscriptionPackage == null, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_NOT_FOUND, Constants.BAD_REQUEST);
        log.info("Basic validation: Query to get subscription package : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        validatePublish(subscriptionPackage);
        subscriptionPackage.setStatus(InstructorConstant.PUBLISH);
        Date now = new Date();
        subscriptionPackage.setPublishedDate(now);
        subscriptionPackageRepository.save(subscriptionPackage);
        log.info("Query to save subscription package : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        //uploading package to stripe
        stripeService.createPackageInStripe(subscriptionPackageId);
        log.info("Create package in stripe : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        //Creating product for subscription package
        fitwiseQboEntityService.createOrUpdateQboProduct(subscriptionPackage);
        log.info("Create or update QBO product : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        try{
            List<String> emails = new ArrayList<>();
            if(subscriptionPackage.isRestrictedAccess()){
                List<PackageMemberMapping> packageMemberMappingList = packageMemberMappingRepository.findBySubscriptionPackage(subscriptionPackage);
                List<String> clientEmails = packageMemberMappingList.stream().map(packageMemberMapping -> packageMemberMapping.getUser().getEmail()).collect(Collectors.toList());
                List<PackageExternalClientMapping> packageExternalClientMappings = packageExternalClientMappingRepository.findBySubscriptionPackage(subscriptionPackage);
                List<String> externalClientEmails = packageExternalClientMappings.stream().map(PackageExternalClientMapping::getExternalClientEmail).collect(Collectors.toList());
                emails.addAll(clientEmails);
                emails.addAll(externalClientEmails);
                log.info("Query to get member and external client emails : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                profilingEndTimeMillis = new Date().getTime();
                asyncMailerUtil.triggerPackageInvite(emails, subscriptionPackage);
                log.info("Async mailer activated : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            }
        }catch (Exception e){
            log.error(MessageConstants.MSG_ERR_EXCEPTION + e.getMessage());
        }
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Publish subscription package ends.");
    }

    /**
     * @param subscriptionPackage
     */
    private void validatePublish(SubscriptionPackage subscriptionPackage) {
//        if (subscriptionPackage.getStatus().equalsIgnoreCase(InstructorConstant.PUBLISH)) {
//            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_ALREADY_PUBLISHED, null);
//        }
        if (subscriptionPackage.getStatus().equalsIgnoreCase(InstructorConstant.BLOCK) || subscriptionPackage.getStatus().equalsIgnoreCase(DBConstants.BLOCK_EDIT)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_BLOCKED, null);
        }
        List<String> inProgressStatusList = Arrays.asList(InstructorConstant.PLAN, DBConstants.CONFIGURE, InstructorConstant.PRICE, DBConstants.ACCESS_CONTROL);
        boolean isPackageInProgress = inProgressStatusList.stream().anyMatch(subscriptionPackage.getStatus()::equalsIgnoreCase);
        if (isPackageInProgress) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_SUBSCRIPTION_PACKAGE_NOT_COMPLETED_YET, null);
        }
        ValidationUtils.throwException(subscriptionPackage.getTitle().isEmpty(), ValidationMessageConstants.MSG_TITLE_NULL, Constants.BAD_REQUEST);
        ValidationUtils.throwException(subscriptionPackage.getOwner() == null, MessageConstants.MSG_NO_OWNER_FOR_PROGRAM, Constants.BAD_REQUEST);
        ValidationUtils.paramNullCheck(subscriptionPackage.getImage(), MessageConstants.MSG_SUBSCRIPTION_PACKAGE_THUMBNAIL_REQUIRED);
        if((subscriptionPackage.getPromotion() != null && subscriptionPackage.getPromotion().getVideoManagement() != null) && (subscriptionPackage.getPromotion().getVideoManagement().getUploadStatus() == null || !subscriptionPackage.getPromotion().getVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.COMPLETED))){
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERR_PGM_PROMO_UPLOAD_NOT_COMPLETED, null);
        }
        if(subscriptionPackage.getPackageProgramMapping().isEmpty() && subscriptionPackage.getPackageKloudlessMapping().isEmpty()){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ERR_PACKAGE_OR_MEETING_REQUIRED, null);
        }
        for(PackageKloudlessMapping packageKloudlessMapping : subscriptionPackage.getPackageKloudlessMapping()){
            if ((packageKloudlessMapping.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingTypeId() == CalendarConstants.SESSION_IN_PERSON
                    || packageKloudlessMapping.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingTypeId() == CalendarConstants.SESSION_IN_PERSON_OR_VIRTUAL)
                    && packageKloudlessMapping.getLocation() == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_LOCATION_NOT_ADDED, null);
            }
            String meetingType = packageKloudlessMapping.getUserKloudlessMeeting().getCalendarMeetingType().getMeetingType();
            if ((DBConstants.INPERSON_OR_VIRTUAL_SESSION.equalsIgnoreCase(meetingType) || DBConstants.VIRTUAL_SESSION.equalsIgnoreCase(meetingType)) && StringUtils.isEmpty(packageKloudlessMapping.getMeetingUrl())) {
                throw new ApplicationException(Constants.FORBIDDEN, CalendarConstants.CAL_ERR_INSTRUCTOR_MEETING_REQUIRED, null);
            }
        }
        if (!subscriptionPackage.getPackageKloudlessMapping().isEmpty() && (subscriptionPackage.getCancellationDuration() == null || (subscriptionPackage.getCancellationDuration() != null && subscriptionPackage.getCancellationDuration().getDays() == null && subscriptionPackage.getCancellationDuration().getHours() == null))) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_SUBSCRIPTION_PACKAGE_CANCELLATION_DURATION_EMPTY, null);
        }
        ValidationUtils.paramNullCheck(subscriptionPackage.getPrice(), MessageConstants.MSG_SUBSCRIPTION_PACKAGE_PRICE_REQUIRED);
        if (subscriptionPackage.getPrice() < StripeConstants.STRIPE_MINIMUM_PRICE) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERR_PRODUCT_MIN_PRICE, null);
        }
        if (subscriptionPackage.getPrice() > StripeConstants.STRIPE_MAXIMUM_PRICE) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PACKAGE_PRICE_MAX_LIMIT_REACHED, null);
        }
        if (subscriptionPackage.isRestrictedAccess() && ((subscriptionPackage.getPackageMemberMapping() == null || subscriptionPackage.getPackageMemberMapping().isEmpty()) && (subscriptionPackage.getExternalMemberMapping() == null || subscriptionPackage.getExternalMemberMapping().isEmpty()))) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_SUBSCRIPTION_PACKAGE_ACCESS_CONTROL_REQUIRED, null);
        }
        if (subscriptionPackage.isRestrictedAccess() && ValidationUtils.isEmptyString(subscriptionPackage.getClientMessage())) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_SUBSCRIPTION_PACKAGE_CLIENT_MESSAGE_REQUIRED, null);
        }
        //validate for active paid subscriptions
        long subscriptionCount = subscriptionService.getActiveSubscriptionCountForPackage(subscriptionPackage.getSubscriptionPackageId());
//        if (subscriptionCount > 0) {
//            throw new ApplicationException(Constants.CONFLICT, ValidationMessageConstants.MSG_CANT_PUBLISH_SUBSCRIBED_PACKAGE.replace(StringConstants.LITERAL_COUNT, String.valueOf(subscriptionCount)), MessageConstants.ERROR);
//        }
    }

    /**
     * Method to unpublish SubscriptionPackage
     * @param subscriptionPackageId
     */
    public void unpublishSubscriptionPackage(Long subscriptionPackageId) {
        User currentUser = userComponents.getUser();
        SubscriptionPackage subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageIdAndOwnerUserId(subscriptionPackageId, currentUser.getUserId());
        ValidationUtils.throwException(subscriptionPackage == null, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_NOT_FOUND, Constants.BAD_REQUEST);
        if (!subscriptionPackage.getStatus().equalsIgnoreCase(InstructorConstant.PUBLISH)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_CANT_UNPUBLISH_NOT_PUBLISHED, null);
        }
         //If the SubscriptionPackage is subscribed by other users, cancelling the subscriptions before un-publishing it
        List<PackageSubscription> packageSubscriptions = subscriptionService.getPaidSubscriptionsOfPackage(subscriptionPackageId);
        for (PackageSubscription packageSubscription : packageSubscriptions) {
            if (packageSubscription.isAutoRenewal()) {
                stripeService.cancelStripePackageSubscription(subscriptionPackageId, packageSubscription.getSubscribedViaPlatform().getPlatformTypeId(), packageSubscription.getUser(), false);
            }
        }
        /*Sending mail to subscribers*/
        asyncMailerUtil.triggerUnpublishPackageMail(packageSubscriptions);
        subscriptionPackage.setStatus(InstructorConstant.UNPUBLISH);
        subscriptionPackageRepository.save(subscriptionPackage);
        //Marking Stripe product mapping as inactive
        StripeProductAndPackageMapping productAndPackageMapping = stripeProductAndPackageMappingRepository.findBySubscriptionPackageSubscriptionPackageIdAndIsActive(subscriptionPackageId, true);
        if(productAndPackageMapping != null){
            productAndPackageMapping.setActive(false);
            stripeProductAndPackageMappingRepository.save(productAndPackageMapping);
        }
    }

    /**
     * Method to delete in progress Subscription package
     * @param subscriptionPackageId
     */
    @Transactional
    public void deleteInProgressSubscriptionPackage(Long subscriptionPackageId) {
        User currentUser = userComponents.getUser();
        SubscriptionPackage subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageIdAndOwnerUserId(subscriptionPackageId, currentUser.getUserId());
        ValidationUtils.throwException(subscriptionPackage == null, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_NOT_FOUND, Constants.BAD_REQUEST);
        List<String> restrictedStatusList = Arrays.asList(InstructorConstant.PUBLISH, InstructorConstant.UNPUBLISH, InstructorConstant.BLOCK, DBConstants.UNPUBLISH_EDIT, DBConstants.BLOCK_EDIT);
        boolean isPackageRestricted = restrictedStatusList.stream().anyMatch(subscriptionPackage.getStatus()::equalsIgnoreCase);
        if (isPackageRestricted) {
            throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_UN_PUBLISH_BLOCK_SUBSCRIPTION_PACKAGE_DELETE_INVALID, null);
        }
        List<PackageProgramMapping> packageProgramsListMapping = packageProgramMappingRepository.findBySubscriptionPackage(subscriptionPackage);
        if (packageProgramsListMapping != null && !packageProgramsListMapping.isEmpty()) {
            packageProgramMappingRepository.deleteInBatch(packageProgramsListMapping);
        }
        List<PackageKloudlessMapping> packageKloudlessMappingList = packageKloudlessMappingRepository.findBySubscriptionPackage(subscriptionPackage);
        if (!packageKloudlessMappingList.isEmpty()) {
            packageKloudlessMappingRepository.deleteInBatch(packageKloudlessMappingList);
        }
        List<SubscriptionPackagePriceByPlatform> subscriptionPackagePriceByPlatformList = subscriptionPackagePriceByPlatformRepository.findBySubscriptionPackage(subscriptionPackage);
        if (!subscriptionPackagePriceByPlatformList.isEmpty()) {
            subscriptionPackagePriceByPlatformRepository.deleteInBatch(subscriptionPackagePriceByPlatformList);
        }
        List<PackageMemberMapping> packageMemberList = packageMemberMappingRepository.findBySubscriptionPackage(subscriptionPackage);
        if (!packageMemberList.isEmpty()) {
            packageMemberMappingRepository.deleteInBatch(packageMemberList);
        }
        subscriptionPackageRepository.delete(subscriptionPackage);
    }

    /**
     * Method to remove program from SubscriptionPackage
     * @param subscriptionPackageId
     * @param programId
     */
    @Transactional
    public void removeProgram(Long subscriptionPackageId, Long programId) {
        User currentUser = userComponents.getUser();
        SubscriptionPackage subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageIdAndOwnerUserId(subscriptionPackageId, currentUser.getUserId());
        ValidationUtils.throwException(subscriptionPackage == null, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_NOT_FOUND, Constants.BAD_REQUEST);
        Programs program = programRepository.findByProgramIdAndOwnerUserId(programId, currentUser.getUserId());
        PackageProgramMapping packageProgramsListMapping = packageProgramMappingRepository.findTop1BySubscriptionPackageAndProgram(subscriptionPackage, program);
//        if (subscriptionPackage.getStatus().equalsIgnoreCase(InstructorConstant.PUBLISH)) {
//            throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_PUBLISHED_CANT_REMOVE_PROGRAM, null);
//        }
        if(packageProgramsListMapping != null){
            packageProgramMappingRepository.deleteByPackageProgramId(packageProgramsListMapping.getPackageProgramId());
        }
        List<PackageSubscription> packageSubscriptionUserList = packageSubscriptionRepository.findBySubscriptionPackageSubscriptionPackageId(subscriptionPackageId);
        for(PackageSubscription packageSubscriptionUser : packageSubscriptionUserList){
        	PackageProgramSubscription packageProgramSubscription = packageProgramSubscriptionRepository.findByPackageSubscriptionAndProgramProgramId( packageSubscriptionUser, programId);
        	if(packageProgramSubscription != null){
        		packageProgramSubscriptionRepository.deleteById(packageProgramSubscription.getId());
            }
        }
    }

    /**
     * Method to get Tax model for packages
     * @return
     */
    public List<PlatformWiseTaxDetailsModel> getPackageTaxDetails(Long packageId) {
        User user = userComponents.getUser();
        log.info("Get package tax details starts.");
        long apiStartTimeMillis = new Date().getTime();
        PlatformWiseTaxDetail platformWiseTaxDetail = platformWiseTaxDetailRepository.findByActiveAndPlatformTypePlatform(true, DBConstants.WEB);
        InstructorTierDetails instructorTierDetails = instructorTierDetailsRepository.findByUserAndActive(user, true);
        Double trainnrTax = 15.0;
        if (instructorTierDetails != null) {
            SubscriptionPackage subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageId(packageId);
            if(subscriptionPackage == null){
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_NOT_FOUND, null);
            }
            int programCount = (subscriptionPackage.getPackageProgramMapping() != null) ? subscriptionPackage.getPackageProgramMapping().size() : 0;
            int sessionCount = (subscriptionPackage.getPackageKloudlessMapping() != null && !subscriptionPackage.getPackageKloudlessMapping().isEmpty()) ? subscriptionPackage.getPackageKloudlessMapping().get(0).getTotalSessionCount() : 0;
            if(programCount == 0 && sessionCount > 0) {
                trainnrTax = instructorTierDetails.getTier().getTierTypeDetails().getServicesPackagesFees();
            } else {
                trainnrTax = instructorTierDetails.getTier().getTierTypeDetails().getPackagesFees();
            }
        }
        platformWiseTaxDetail.setTrainnrTaxPercentage(trainnrTax);
        log.info("Query to get platform wise tax details : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        List<PlatformWiseTaxDetailsModel> platformWiseTaxDetailsModels = new ArrayList<>();
        AppConfigKeyValue appConfig = appConfigKeyValueRepository.findByKeyString(AppConfigConstants.ADMIN_CONFIG_FLAT_TAX);

        platformWiseTaxDetailsModels.add(TaxDetailParsing.constructTaxDetails(platformWiseTaxDetail,appConfig));
        log.info("Construct tax details : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get package tax details ends.");
        return platformWiseTaxDetailsModels;
    }

    /**
     * Method to get Price break down model for packages
     * @param price
     * @return
     */
    public List<ProgramPriceResponseModel> getPackagePriceBreakdown(Double price, Long packageId) {
        if (price < StripeConstants.STRIPE_MINIMUM_PRICE) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERR_PRODUCT_MIN_PRICE, null);
        }
        log.info("Get package price breakdown starts.");
        long apiStartTimeMillis = new Date().getTime();
        List<ProgramPriceResponseModel> platformWiseTaxDetailsModelList = new ArrayList<>();
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        String priceStr;
        List<PlatformWiseTaxDetailsModel> taxDetailsByPlatformList = getPackageTaxDetails(packageId);
        log.info("Get tax details by platform list : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
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
            double trainnrTax = taxDetailsByPlatform.getTrainnrTax();
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
            double revenue = price - (appStoreTaxAmount + trainnrTaxAmount + creditCardTaxAmount);
            priceStr = decimalFormat.format(revenue);
            revenue = Double.parseDouble(priceStr);
            programPriceResponseModel.setPrice(revenue);
            programPriceResponseModel.setPriceFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + revenue);
            AppConfigKeyValue appConfig = appConfigKeyValueRepository.findByKeyString(AppConfigConstants.ADMIN_CONFIG_FLAT_TAX);
            if (appConfig != null) {
            	double flatTax = Double.parseDouble(appConfig.getValueString()); 
            	programPriceResponseModel.setFlatTax(flatTax);
            }
            platformWiseTaxDetailsModelList.add(programPriceResponseModel);
        }
        log.info("Construct program price response model list : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get package price breakdown ends.");
        return platformWiseTaxDetailsModelList;
    }

    //Method duplicated for optimisation
    /**
     * Method to construct SubscriptionPackageTileView
     * @param subscriptionPackage
     * @return
     */
    public SubscriptionPackageTileView constructPackageTileModel(SubscriptionPackage subscriptionPackage, Map<Long, Long> offerCount) {
        log.info("Package tile construction starts");
        long start = new Date().getTime();
        SubscriptionPackageTileView subscriptionPackageTileView = new SubscriptionPackageTileView();

        subscriptionPackageTileView.setSubscriptionPackageId(subscriptionPackage.getSubscriptionPackageId());
        subscriptionPackageTileView.setTitle(subscriptionPackage.getTitle());
        if (subscriptionPackage.getImage() != null) {
            subscriptionPackageTileView.setImageUrl(subscriptionPackage.getImage().getImagePath());
        }
        subscriptionPackageTileView.setDuration(subscriptionPackage.getPackageDuration().getDuration());

        subscriptionPackageTileView.setCreatedOn(subscriptionPackage.getCreatedDate());
        subscriptionPackageTileView.setCreatedOnFormatted(fitwiseUtils.formatDate(subscriptionPackage.getCreatedDate()));

        subscriptionPackageTileView.setLastUpdatedOn(subscriptionPackage.getModifiedDate());
        subscriptionPackageTileView.setLastUpdatedOnFormatted(fitwiseUtils.formatDate(subscriptionPackage.getModifiedDate()));
        subscriptionPackageTileView.setStatus(subscriptionPackage.getStatus());
        if(subscriptionPackage.getPublishedDate() != null){
            subscriptionPackageTileView.setPublishedDate(subscriptionPackage.getPublishedDate());
            subscriptionPackageTileView.setPublishedDateFormatted(fitwiseUtils.formatDate(subscriptionPackage.getPublishedDate()));
        }
        if(subscriptionPackage.getPrice() != null){
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            subscriptionPackageTileView.setPrice(subscriptionPackage.getPrice());
            subscriptionPackageTileView.setPriceFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + " " + decimalFormat.format(subscriptionPackage.getPrice()));
        }
        long temp = new Date().getTime();
        if (offerCount != null && offerCount.containsKey(subscriptionPackage.getSubscriptionPackageId())) {
            subscriptionPackageTileView.setNumberOfCurrentAvailableOffers(Math.toIntExact(offerCount.get(subscriptionPackage.getSubscriptionPackageId())));
        }
        log.info("Get no of current available offers : Time taken in millis : "+(new Date().getTime() - temp));

        temp = new Date().getTime();
        int paidSubscriptions = (int) subscriptionService.getActiveSubscriptionCountForPackage(subscriptionPackage.getSubscriptionPackageId());
        subscriptionPackageTileView.setActiveSubscriptions(paidSubscriptions);
        AppConfigKeyValue appConfig = appConfigKeyValueRepository.findByKeyString(AppConfigConstants.ADMIN_CONFIG_FLAT_TAX);
        if (appConfig != null) {
        	double flatTax = Double.parseDouble(appConfig.getValueString());
    	   subscriptionPackageTileView.setFlatTax(flatTax);
        }

        log.info("Get no of active subscriptions : Time taken in millis : "+(new Date().getTime() - temp));
        log.info("Response construction for a package tile : Time taken in millis : "+(new Date().getTime() - start));

        log.info("Package tile construction ends");
        return subscriptionPackageTileView;
    }


    /**
     * Method to get all Subscription Packages by status
     * @param statusList
     * @param pageNo
     * @param pageSize
     * @param sortOrder
     * @param sortBy
     * @param search
     * @return
     */
    private Map<String, Object> getPackages(List<String> statusList, int pageNo, int pageSize, String sortOrder, String sortBy, Optional<String> search) {
        log.info("Get packages starts");
        long start = new Date().getTime();
        User currentUser = userComponents.getUser();

        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ERROR, null);
        }

        if (!(sortBy.equalsIgnoreCase(SearchConstants.TITLE) || sortBy.equalsIgnoreCase(SearchConstants.CREATED_DATE))) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_SORT_BY, null);
        }

        if (!(sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC) || sortOrder.equalsIgnoreCase(SearchConstants.ORDER_ASC))) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ORDER_ERROR, null);
        }

        Sort sort = sortBy.equalsIgnoreCase(SearchConstants.CREATED_DATE) ? Sort.by("modifiedDate") : Sort.by("title");
        sort = sortOrder.equalsIgnoreCase(SearchConstants.ORDER_ASC) ? sort.ascending() : sort.descending();
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, sort);
        log.info("Basic validations : Time taken in millis : "+(new Date().getTime() - start));

        long profilingStart = new Date().getTime();
        Page<SubscriptionPackage> subscriptionPackagePage;
        if (search.isPresent() && !search.get().isEmpty()) {
            subscriptionPackagePage = subscriptionPackageRepository.findByOwnerUserIdAndStatusInAndTitleIgnoreCaseContaining(currentUser.getUserId(), statusList, search.get(), pageRequest);
        } else {
            subscriptionPackagePage = subscriptionPackageRepository.findByOwnerUserIdAndStatusIn(currentUser.getUserId(), statusList, pageRequest);
        }
        log.info("Query : Time taken in millis : "+(new Date().getTime() - profilingStart));


        if (subscriptionPackagePage.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }

        //Getting active offer counts for list of subscription packages
        List<Long> subscriptionPackageIdList = subscriptionPackagePage.stream().map(SubscriptionPackage::getSubscriptionPackageId).collect(Collectors.toList());
        Map<Long, Long> offerCountMap = discountsService.getNoOfCurrentAvailableOffersForInstructorPackages(subscriptionPackageIdList);

        profilingStart = new Date().getTime();
        List<SubscriptionPackageTileView> subscriptionPackageTileViews = new ArrayList<>();
        for (SubscriptionPackage subscriptionPackage : subscriptionPackagePage) {
            subscriptionPackageTileViews.add(constructPackageTileModel(subscriptionPackage, offerCountMap));
        }
        log.info("Response construction : Time taken in millis : "+(new Date().getTime() - profilingStart));


        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put(KeyConstants.KEY_SUBSCRIPTION_PACKAGES, subscriptionPackageTileViews);
        responseMap.put(KeyConstants.KEY_TOTAL_COUNT, subscriptionPackagePage.getTotalElements());
        log.info("Get packages : Time taken in millis : " +(new Date().getTime() - start));
        log.info("Get packages ends");

        return responseMap;
    }

    /**
     * Method to get all published Subscription Packages
     * @param pageNo
     * @param pageSize
     * @param sortOrder
     * @param sortBy
     * @param searchname
     * @return
     */
    public Map<String, Object> getPublishedPackages(int pageNo, int pageSize, String sortOrder, String sortBy, Optional<String> searchname) {
        log.info("Get published packages starts");
        long start = new Date().getTime();
        List<String> statusList = Arrays.asList(InstructorConstant.PUBLISH, InstructorConstant.PUBLISH_EDIT);
        Map<String, Object> packages = getPackages(statusList, pageNo, pageSize, sortOrder, sortBy, searchname);
        log.info("Get published packages : Total Time taken in millis : "+(new Date().getTime() - start));
        log.info("Get published packages ends");
        
        return packages;
    }

    /**
     * Method to get all inprogress Subscription Packages
     * @param pageNo
     * @param pageSize
     * @param sortOrder
     * @param sortBy
     * @param searchname
     * @return
     */
    public Map<String, Object> getInProgressPackages(int pageNo, int pageSize, String sortOrder, String sortBy, Optional<String> searchname) {
        log.info("Get inprogress packages starts");
        long start = new Date().getTime();

        List<String> statusList = Arrays.asList(InstructorConstant.PLAN, DBConstants.CONFIGURE, DBConstants.ACCESS_CONTROL, InstructorConstant.PRICE, InstructorConstant.PRE_PUBLISH);
        Map<String, Object> packages = getPackages(statusList, pageNo, pageSize, sortOrder, sortBy, searchname);
        log.info("Get inprogress packages : Total Time taken in millis : "+(new Date().getTime() - start));
        log.info("Get inprogress packages ends");

        return packages;

    }

    /**
     * Method to get all unpublished Subscription Packages
     * @param pageNo
     * @param pageSize
     * @param sortOrder
     * @param sortBy
     * @param searchname
     * @return
     */
    public Map<String, Object> getUnPublishedPackages(int pageNo, int pageSize, String sortOrder, String sortBy, Optional<String> searchname) {
        log.info("Get unpublished packages starts");
        long start = new Date().getTime();
        List<String> statusList = Arrays.asList(InstructorConstant.UNPUBLISH, DBConstants.UNPUBLISH_EDIT);
        Map<String, Object> packages = getPackages(statusList, pageNo, pageSize, sortOrder, sortBy, searchname);
        log.info("Get unpublished packages : Total Time taken in millis : "+(new Date().getTime() - start));
        log.info("Get unpublished packages ends");

        return packages ;

    }


    /**
     * Method to get all blocked Subscription Packages
     * @param pageNo
     * @param pageSize
     * @param sortOrder
     * @param sortBy
     * @param searchname
     * @return
     */
    public Map<String, Object> getBlockedPackages(int pageNo, int pageSize, String sortOrder, String sortBy, Optional<String> searchname) {
        log.info("Get blocked packages starts");
        long start = new Date().getTime();
        List<String> statusList = Arrays.asList(InstructorConstant.BLOCK, DBConstants.BLOCK_EDIT);
        Map<String, Object> packages = getPackages(statusList, pageNo, pageSize, sortOrder, sortBy, searchname);
        log.info("Get blocked packages : Total Time taken in millis : "+(new Date().getTime() - start));
        log.info("Get blocked packages ends");

        return packages;

    }

    /**
     * method to edit Unpublish or block SubscriptionPackage
     *
     * @param model
     * @return
     */
    @Transactional
    public SubscriptionPackageView restrictedPackageEdit(SubscriptionPackageModel model) {
        User currentUser = userComponents.getUser();
        if (!fitwiseUtils.isInstructor(currentUser)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_INSTRUCTOR, MessageConstants.ERROR);
        }
        if (model.getSubscriptionPackageId() == null || model.getSubscriptionPackageId() == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_ID_NULL, MessageConstants.ERROR);
        }
        SubscriptionPackage subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageIdAndOwnerUserId(model.getSubscriptionPackageId(), currentUser.getUserId());
        ValidationUtils.throwException(subscriptionPackage == null, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_NOT_FOUND, Constants.BAD_REQUEST);

        if (subscriptionPackage.getStatus() == null || subscriptionPackage.getStatus().isEmpty()) {
            throw new ApplicationException(Constants.CONFLICT, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_STATUS_NULL, MessageConstants.ERROR);
        }
        //Validation to allow only SubscriptionPackage in unpublish, unpublish_edit or block, block_edit state
        List<String> restrictedStatusList = Arrays.asList(InstructorConstant.UNPUBLISH, DBConstants.UNPUBLISH_EDIT, InstructorConstant.BLOCK, DBConstants.BLOCK_EDIT, InstructorConstant.PUBLISH,  DBConstants.PUBLISH_EDIT);
        boolean isPackageRestricted = restrictedStatusList.stream().anyMatch(subscriptionPackage.getStatus()::equalsIgnoreCase);
        if (!isPackageRestricted) {
            throw new ApplicationException(Constants.CONFLICT, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_NOT_RESTRICTED, MessageConstants.ERROR);
        }

        long subscriptionCount = subscriptionService.getActiveSubscriptionCountForPackage(model.getSubscriptionPackageId());
        if(!InstructorConstant.PUBLISH.equals(subscriptionPackage.getStatus())) {
      
        if (subscriptionCount > 0) {
            throw new ApplicationException(Constants.CONFLICT, ValidationMessageConstants.MSG_RESTRICTED_PACKAGE_SUBSCRIBED.replace(StringConstants.LITERAL_COUNT, String.valueOf(subscriptionCount)), MessageConstants.ERROR);
        }
        }

        constructSubscriptionPackage(model, subscriptionPackage, true);

        //Changing status of SubscriptionPackage
        if (InstructorConstant.UNPUBLISH.equals(subscriptionPackage.getStatus())) {
            subscriptionPackage.setStatus(DBConstants.UNPUBLISH_EDIT);
        }
        
      //Changing status of SubscriptionPackage
        if (InstructorConstant.PUBLISH.equals(subscriptionPackage.getStatus())) {
            subscriptionPackage.setStatus(DBConstants.PUBLISH);
        }
        if (InstructorConstant.BLOCK.equals(subscriptionPackage.getStatus())) {
            subscriptionPackage.setStatus(DBConstants.BLOCK_EDIT);
        }

        subscriptionPackage = subscriptionPackageRepository.save(subscriptionPackage);

        return constructSubscriptionPackageResponse(subscriptionPackage);
    }

    /**
     * Adding offers to published subscription package
     * @param model
     * @return
     */
    public ResponseModel addOffersInSubscriptionPackage(OfferSubscriptionPackageModel model) {
        User currentUser = userComponents.getUser();
        if(model.getSubscriptionPackageId() == null){
            throw new ApplicationException(Constants.BAD_REQUEST,ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_ID_NULL,null);
        }
        SubscriptionPackage subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageIdAndOwnerUserId(model.getSubscriptionPackageId(), currentUser.getUserId());
        if(subscriptionPackage == null){
            throw new ApplicationException(Constants.BAD_REQUEST,ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_NOT_FOUND,null);
        }
        if(!model.getDiscountOffersIds().isEmpty() || model.getDiscountOffersIds() != null){
            constructDiscountsOffer(subscriptionPackage,model.getDiscountOffersIds());
        }
        subscriptionPackage = subscriptionPackageRepository.save(subscriptionPackage);
        return new ResponseModel(Constants.SUCCESS_STATUS,MessageConstants.MSG_OFFERS_ADDED,constructSubscriptionPackageResponse(subscriptionPackage));
    }

    public ResponseModel removeOffersWithHigherPrice(Long subscriptionPackageId,Double newPackagePrice){
        List<PackageOfferMapping> packageOfferMappings = packageOfferMappingRepository.findBySubscriptionPackageSubscriptionPackageIdAndOfferCodeDetailIsInUseAndOfferCodeDetailOfferStatus(subscriptionPackageId, true,DiscountsConstants.OFFER_ACTIVE);
        if(!packageOfferMappings.isEmpty()){
            for(PackageOfferMapping packageOfferMapping : packageOfferMappings){
                OfferCodeDetail offerCodeDetail = offerCodeDetailRepository.findByOfferCodeId(packageOfferMapping.getOfferCodeDetail().getOfferCodeId());
                if (offerCodeDetail.getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_PAY_AS_YOU_GO) && offerCodeDetail.getOfferPrice().getPrice() >= newPackagePrice) {
                    packageOfferMappingRepository.delete(packageOfferMapping);

                }
            }
        }
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_OFFERS_REMOVED, null);
    }

    public ResponseModel removeSessionFromPackage(Long subscriptionPackageId,Long meetingId){
        SubscriptionPackage subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageId(subscriptionPackageId);
        if(subscriptionPackage == null){
            throw new ApplicationException(Constants.BAD_REQUEST,ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_NOT_FOUND,MessageConstants.ERROR);
        }
        if (subscriptionPackage.getStatus().equalsIgnoreCase(InstructorConstant.PUBLISH)) {
            throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_SUBSCRIPTION_PACKAGE_PUBLISHED_CANT_REMOVE_PROGRAM, null);
        }
        UserKloudlessMeeting userKloudlessMeeting = userKloudlessMeetingRepository.findByUserKloudlessMeetingId(meetingId);
        if(userKloudlessMeeting == null){
            throw new ApplicationException(Constants.BAD_REQUEST,ValidationMessageConstants.MSG_MEETING_NOT_FOUND,MessageConstants.ERROR);
        }
        PackageKloudlessMapping packageKloudlessMapping = packageKloudlessMappingRepository.findTop1BySubscriptionPackageAndUserKloudlessMeetingUserKloudlessMeetingId(subscriptionPackage,meetingId);
        if(packageKloudlessMapping == null){
            throw new ApplicationException(Constants.BAD_REQUEST,ValidationMessageConstants.MSG_SESSION_PACKAGE_MAPPING_NOT_FOUND,MessageConstants.ERROR);
        }
        packageKloudlessMappingRepository.delete(packageKloudlessMapping);
        return new ResponseModel(Constants.SUCCESS_STATUS,MessageConstants.MSG_SESSION_REMOVED_FROM_PACKAGE,null);
    }

    public void validateSessionTitleInPackage(String title,Long subscriptionPackageId){

        SubscriptionPackage subscriptionPackage = subscriptionPackageRepository.findBySubscriptionPackageId(subscriptionPackageId);
        if(subscriptionPackage != null){
            List<PackageKloudlessMapping> packageKloudlessMappingList = packageKloudlessMappingRepository.findBySubscriptionPackage(subscriptionPackage);
            for(PackageKloudlessMapping packageKloudlessMapping : packageKloudlessMappingList){
                if(title.equalsIgnoreCase(packageKloudlessMapping.getTitle())){
                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_SESSION_DUPLICATE_TITLE_IN_PACKAGE, MessageConstants.ERROR);
                }
            }
        }
    }

    public ResponseModel getCountPerWeek(){
        List<SessionCountPerWeek> sessionCountPerWeekList = sessionCountPerWeekRepository.findAll();
        Map<String,Object> responseMap = new HashMap<>();
        responseMap.put(KeyConstants.KEY_SESSION_COUNT_PER_WEEK,sessionCountPerWeekList);
        return  new ResponseModel(Constants.SUCCESS_STATUS,MessageConstants.MSG_DATA_RETRIEVED,responseMap);
    }

    public ResponseModel getCancellationDuration(boolean isDays){
        List<CancellationDuration> cancellationDurations;
        if(isDays){
             cancellationDurations = cancellationDurationRepository.findByIsDays(true);
        }else{
             cancellationDurations = cancellationDurationRepository.findByIsDays(false);
        }
        Map<String,Object> responseMap = new HashMap<>();
        responseMap.put(KeyConstants.KEY_CANCELLATION_DURATION,cancellationDurations);

        return  new ResponseModel(Constants.SUCCESS_STATUS,MessageConstants.MSG_DATA_RETRIEVED,responseMap);

    }

    public ResponseModel getMeetings(Long  meetingTypeId) {
        User user = userComponents.getUser();
        UserKloudlessAccount userKloudlessAccount = calendarService.getActiveAccount(user);
        UserKloudlessCalendar userKloudlessCalendar = calendarService.getActiveCalendarFromKloudlessAccount(userKloudlessAccount);
        if(userKloudlessCalendar == null){
            throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_CALENDAR_NOT_FOUND, null);
        }
        List<UserKloudlessMeeting> userKloudlessMeetings;
            CalendarMeetingType calendarMeetingType = calendarMeetingTypeRepository.findByMeetingTypeId(meetingTypeId);
            if(calendarMeetingType == null){
                throw new ApplicationException(Constants.NOT_FOUND, CalendarConstants.CAL_ERR_MEETING_TYPE_NOT_FOUND, null);
            }
            userKloudlessMeetings = userKloudlessMeetingRepository
                    .findByMeetingIdNotNullAndUserKloudlessCalendarAndCalendarMeetingType(userKloudlessCalendar, calendarMeetingType);

        if(userKloudlessMeetings.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, "", null);
        }
        List<MyMeetingsView> myMeetingsViews = new ArrayList<>();
        for(UserKloudlessMeeting userKloudlessMeeting : userKloudlessMeetings){
            MyMeetingsView myMeetingsView = new MyMeetingsView();
            List<PackageKloudlessMapping> packageKloudlessMappings = packageKloudlessMappingRepository.findByUserKloudlessMeeting(userKloudlessMeeting);
            if(!packageKloudlessMappings.isEmpty()){
                myMeetingsView.setUsedInPackage(true);
            }
            myMeetingsView.setFitwiseMeetingId(userKloudlessMeeting.getUserKloudlessMeetingId());
            myMeetingsView.setMeetingId(userKloudlessMeeting.getMeetingId());
            myMeetingsView.setMeetingTypeId(userKloudlessMeeting.getCalendarMeetingType().getMeetingTypeId());
            myMeetingsView.setMeetingType(userKloudlessMeeting.getCalendarMeetingType().getMeetingType());
            myMeetingsView.setName(userKloudlessMeeting.getName());
            myMeetingsView.setSessionDuration(userKloudlessMeeting.getMeetingDurationInDays());
            if(userKloudlessMeeting.getDuration() != null){
                myMeetingsView.setDurationInMinutes(userKloudlessMeeting.getDuration().getMinutes());
            }

            myMeetingsViews.add(myMeetingsView);
        }
        MyMeetingsListView myMeetingsListView = new MyMeetingsListView();
        myMeetingsListView.setMeetings(myMeetingsViews);
        myMeetingsListView.setTotalCount(myMeetingsViews.size());
        return new ResponseModel(Constants.SUCCESS_STATUS,MessageConstants.MSG_DATA_RETRIEVED, myMeetingsListView);
    }

    public ResponseModel getAllInstructorPackages(int pageNo, int pageSize, String sortBy, String sortOrder, Optional<String> searchName) {
        RequestParamValidator.pageSetup(pageNo, pageSize);
        if (!(sortBy.equalsIgnoreCase(SearchConstants.TITLE) || sortBy.equalsIgnoreCase(SearchConstants.CREATED_DATE))) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_SORT_BY, null);
        }
        if (!(sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC) || sortOrder.equalsIgnoreCase(SearchConstants.ORDER_ASC))) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ORDER_ERROR, null);
        }

        User user = userComponents.getUser();

        Sort sort;
        if (sortBy.equalsIgnoreCase(SearchConstants.TITLE)) {
            sort = Sort.by(SearchConstants.TITLE);
        } else {
            sort = Sort.by(SearchConstants.CREATED_DATE);
        }
        if (sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC)) {
            sort = sort.descending();
        }
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, sort);

        Page<SubscriptionPackage> subscriptionPackagePage;
        if (searchName.isPresent() && !searchName.get().isEmpty()) {
            subscriptionPackagePage = subscriptionPackageRepository.findByOwnerUserIdAndStatusAndTitleIgnoreCaseContaining(user.getUserId(), KeyConstants.KEY_PUBLISH, searchName.get(), pageRequest);
        } else {
            subscriptionPackagePage = subscriptionPackageRepository.findByOwnerUserIdAndStatus(user.getUserId(), KeyConstants.KEY_PUBLISH, pageRequest);
        }

        if (subscriptionPackagePage.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }

        //Getting active offer counts for list of subscription packages
        List<Long> subscriptionPackageIdList = subscriptionPackagePage.stream().map(SubscriptionPackage::getSubscriptionPackageId).collect(Collectors.toList());
        Map<Long, Long> offerCountMap = discountsService.getNoOfCurrentAvailableOffersForInstructorPackages(subscriptionPackageIdList);

        List<SubscriptionPackageTileView> subscriptionPackageTileViews = new ArrayList<>();
        for (SubscriptionPackage subscriptionPackage : subscriptionPackagePage) {
            subscriptionPackageTileViews.add(constructPackageTileModel(subscriptionPackage, offerCountMap));
        }

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put(KeyConstants.KEY_SUBSCRIPTION_PACKAGES, subscriptionPackageTileViews);
        responseMap.put(KeyConstants.KEY_TOTAL_COUNT, subscriptionPackagePage.getTotalElements());

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, responseMap);
    }
}
