package com.fitwise.service;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.EmailConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.PlatformType;
import com.fitwise.entity.ProgramSubscriptionPaymentHistory;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.payments.appleiap.ApplePayment;
import com.fitwise.entity.payments.authNet.AuthNetArbSubscription;
import com.fitwise.entity.payments.authNet.AuthNetPayment;
import com.fitwise.entity.payments.authNet.AuthNetPaymentProfile;
import com.fitwise.entity.payments.authNet.AuthNetSubscriptionChangesTracker;
import com.fitwise.entity.payments.authNet.AuthNetSubscriptionStatus;
import com.fitwise.entity.payments.authNet.Countries;
import com.fitwise.entity.payments.common.InvoiceManagement;
import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.subscription.InstructorSubscription;
import com.fitwise.entity.subscription.PackageProgramSubscription;
import com.fitwise.entity.subscription.PackageSubscription;
import com.fitwise.entity.subscription.ProgramSubscription;
import com.fitwise.entity.subscription.SubscriptionAudit;
import com.fitwise.entity.subscription.SubscriptionPlan;
import com.fitwise.entity.subscription.SubscriptionStatus;
import com.fitwise.entity.subscription.SubscriptionType;
import com.fitwise.exception.ApplicationException;
import com.fitwise.properties.StripeProperties;
import com.fitwise.repository.AuthNetSubscriptionStatusRepository;
import com.fitwise.repository.BlockedUserRepository;
import com.fitwise.repository.PlatformTypeRepository;
import com.fitwise.repository.SubscriptionPaymentHistoryRepository;
import com.fitwise.repository.member.WorkoutCompletionRepository;
import com.fitwise.repository.order.InvoiceManagementRepository;
import com.fitwise.repository.order.OrderManagementRepository;
import com.fitwise.repository.payments.appleiap.ApplePaymentRepository;
import com.fitwise.repository.payments.authnet.AuthNetArbSubscriptionRepository;
import com.fitwise.repository.payments.authnet.AuthNetPaymentProfileRepository;
import com.fitwise.repository.payments.authnet.AuthNetPaymentRepository;
import com.fitwise.repository.payments.authnet.AuthNetSubscriptionChangesTrackerRepository;
import com.fitwise.repository.payments.authnet.CountriesRepository;
import com.fitwise.repository.subscription.InstructorSubscriptionRepo;
import com.fitwise.repository.subscription.PackageProgramSubscriptionRepository;
import com.fitwise.repository.subscription.PackageSubscriptionRepository;
import com.fitwise.repository.subscription.ProgramSubscriptionRepo;
import com.fitwise.repository.subscription.SubscriptionAuditRepo;
import com.fitwise.repository.subscription.SubscriptionPlansRepo;
import com.fitwise.repository.subscription.SubscriptionStatusRepo;
import com.fitwise.repository.subscription.SubscriptionTypesRepo;
import com.fitwise.response.payment.authorize.net.ANetOneTimeTransactionResponseView;
import com.fitwise.response.payment.authorize.net.ANetTransactionResponse;
import com.fitwise.response.payment.authorize.net.CustomerProfile;
import com.fitwise.response.payment.authorize.net.PaymentProfile;
import com.fitwise.service.payment.authorizenet.CreateCustomerProfileFromTransaction;
import com.fitwise.service.payment.authorizenet.PaymentService;
import com.fitwise.service.payment.stripe.StripeService;
import com.fitwise.service.qbo.FitwiseQboEntityService;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.specifications.jpa.ApplePaymentJPA;
import com.fitwise.specifications.jpa.OrderManagementJPA;
import com.fitwise.specifications.jpa.PackageSubscriptionJpa;
import com.fitwise.specifications.jpa.ProgramJpa;
import com.fitwise.specifications.jpa.ProgramSubscriptionJPA;
import com.fitwise.specifications.jpa.StripePaymentJPA;
import com.fitwise.specifications.jpa.dao.ApplePaymentDAO;
import com.fitwise.specifications.jpa.dao.AppleSubscriptionStatusDAO;
import com.fitwise.specifications.jpa.dao.ManageSubscriptionDAO;
import com.fitwise.specifications.jpa.dao.ManageSubscriptionDAOForPackage;
import com.fitwise.specifications.jpa.dao.ManageSubscriptionForProgramWithStripe;
import com.fitwise.specifications.jpa.dao.ManageSubscriptionWithStripeDAO;
import com.fitwise.specifications.jpa.dao.PackageSubscriptionDAO;
import com.fitwise.specifications.jpa.dao.PackageSubscriptionDAOWithStripe;
import com.fitwise.specifications.jpa.dao.ProgramSubscriptionDAO;
import com.fitwise.specifications.jpa.dao.ProgramSubscriptionDAOWithStripe;
import com.fitwise.specifications.jpa.dao.StripePaymentDAO;
import com.fitwise.specifications.jpa.dao.StripeSubscriptionStatusDAO;
import com.fitwise.specifications.jpa.dao.SubscriptionCountDao;
import com.fitwise.specifications.jpa.dao.WorkoutCompletionDAO;
import com.fitwise.utils.EmailContentUtil;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.mail.AsyncMailer;
import com.fitwise.utils.payments.OrderNumberGenerator;
import com.fitwise.view.CountryView;
import com.fitwise.view.InstructorSubscriptionView;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.SubscribedProgramTileResponseView;
import com.fitwise.view.SubscribedProgramsResponseView;
import com.fitwise.view.order.OrderResponseView;
import com.fitwise.view.payment.authorizenet.ANetCustomerProfileRequestView;
import com.fitwise.view.payment.authorizenet.ANetOneTimeProgramSubscriptionUsingCardRequestView;
import com.fitwise.view.payment.authorizenet.ANetRecurringSubscriptionRequestViewWithCardData;
import com.fitwise.view.payment.authorizenet.ANetRecurringSubscriptionRequestViewWithPaymentProfile;
import com.fitwise.view.payment.authorizenet.CancelRecurringSubscriptionRequestView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.authorize.api.contract.v1.ANetApiResponse;
import net.authorize.api.contract.v1.ARBCreateSubscriptionResponse;
import net.authorize.api.contract.v1.CreateCustomerProfileResponse;
import net.authorize.api.contract.v1.GetTransactionDetailsResponse;
import net.authorize.api.contract.v1.MessageTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.maxBy;
import static java.util.stream.Collectors.toCollection;
@Service
@Slf4j
@EnableAsync
@RequiredArgsConstructor
public class SubscriptionService {

    @Autowired
    SubscriptionTypesRepo typeRepo;

    @Autowired
    ProgramSubscriptionRepo programSubscriptionRepo;

    @Autowired
    InstructorSubscriptionRepo instructorSubscriptionRepo;

    @Autowired
    SubscriptionAuditRepo subscriptionAuditRepo;

    @Autowired
    private UserComponents userComponents;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private SubscriptionStatusRepo subscriptionStatusRepo;

    @Autowired
    private SubscriptionTypesRepo subscriptionTypesRepo;

    @Autowired
    private PlatformTypeRepository platformsRepo;

    @Autowired
    private BlockedUserRepository blockedUserRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private SubscriptionPlansRepo subscriptionPlansRepo;

    @Autowired
    private OrderManagementRepository orderManagementRepo;

    @Autowired
    private AuthNetPaymentRepository authNetPaymentRepository;

    @Autowired
    private AuthNetArbSubscriptionRepository authNetArbSubscriptionRepository;

    @Autowired
    private AuthNetSubscriptionStatusRepository authNetSubscriptionStatusRepository;

    @Autowired
    private InvoiceManagementRepository invoiceManagementRepository;

    @Autowired
    private CountriesRepository countriesRepository;

    @Autowired
    private FitwiseQboEntityService fitwiseQboEntityService;

    @Autowired
    private ApplePaymentRepository applePaymentRepository;

    private final AsyncMailer asyncMailer;

    @Autowired
    FitwiseUtils fitwiseUtils;
    @Autowired
    private WorkoutCompletionRepository workoutCompletionRepository;

    @Autowired
    NotificationTriggerService notificationTriggerService;

    @Autowired
    private SubscriptionPaymentHistoryRepository subscriptionPaymentHistoryRepository;

    @Autowired
    private AuthNetSubscriptionChangesTrackerRepository authNetSubscriptionChangesTrackerRepository;

    @Autowired
    private EmailContentUtil emailContentUtil;

    @Autowired
    private AuthNetPaymentProfileRepository authNetPaymentProfileRepository;
    @Autowired
    private OrderManagementRepository orderManagementRepository;
    @Autowired
    private StripeService stripeService;

    @Autowired
    private StripeProperties stripeProperties;
    @Autowired
    private PackageSubscriptionRepository packageSubscriptionRepository;

    @Autowired
    private PackageProgramSubscriptionRepository packageProgramSubscriptionRepository;

    private final ProgramSubscriptionJPA programSubscriptionJPA;

    @Autowired
    private PackageSubscriptionJpa packageSubscriptionJpa;

    @Autowired
    private OrderManagementJPA orderManagementJPA;

    @Autowired
    private StripePaymentJPA stripePaymentJPA;

    @Autowired
    private ApplePaymentJPA applePaymentJPA;

    @Autowired
    private ProgramJpa programJpa;

    /**
     * Method to retrieve all Subscription plans
     *
     * @return Subscription Plans list
     */
    public ResponseModel getAllSubscriptionPlans() {
        ResponseModel res = new ResponseModel();
        Map<String, Object> resMap = new HashMap<>();
        resMap.put(KeyConstants.MSG_SUBSCRIPTION_PLANS, subscriptionPlansRepo.findAll());
        res.setStatus(Constants.SUCCESS_STATUS);
        res.setMessage(MessageConstants.MSG_FETCHED_SUBSCRIPTION_PLANS);
        res.setPayload(resMap);
        return res;
    }

    /**
     * Method to retrieve all Subscription Types
     *
     * @return Subscription Types list
     */
    public ResponseModel getAllSubscriptionTypes() {
        ResponseModel res = new ResponseModel();
        Map<String, Object> resMap = new HashMap<>();
        resMap.put(KeyConstants.MSG_SUBSCRIPTION_PLANS, typeRepo.findAll());
        res.setStatus(Constants.SUCCESS_STATUS);
        res.setMessage(MessageConstants.MSG_FETCHED_SUBSCRIPTION_TYPES);
        res.setPayload(resMap);
        return res;
    }

    /**
     * Method used to subscribe Instructor
     *
     * @param instructorSubscriptionView
     * @return
     */
    public ResponseModel subscribeInstructor(InstructorSubscriptionView instructorSubscriptionView) {
        long userId = userComponents.getUser().getUserId();
        User instructor = validationService.validateInstructorId(instructorSubscriptionView.getInstructor_id());
        if (blockedUserRepository.existsByUserUserIdAndUserRoleName(instructor.getUserId(), KeyConstants.KEY_INSTRUCTOR)) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_INSTRUCTOR_BLOCKED, null);
        }
        SubscriptionPlan newSubscriptionPlan = validationService.validateSubscriptionPlanId
                (instructorSubscriptionView.getSubscription_plan_id());

        SubscriptionStatus newSubscriptionStatus = subscriptionStatusRepo.
                findBySubscriptionStatusId(instructorSubscriptionView.getSubscriptionStatusId());

        PlatformType newClientDevicePlatform = platformsRepo.findByPlatformTypeId(instructorSubscriptionView.getDevicePlatformId());

        SubscriptionType subscriptionType = subscriptionTypesRepo.findByNameIgnoreCase(KeyConstants.KEY_INSTRUCTOR);

        //TODO Check if the program is already subscribed before making subscription
        InstructorSubscription subscribedInstructor = instructorSubscriptionRepo.findByUserUserIdAndInstructorUserId(userId, instructorSubscriptionView.getInstructor_id());
        if (subscribedInstructor != null) {

            // Program has been already subscribed

            SubscriptionPlan subscribedPlan = subscribedInstructor.getSubscriptionPlan();
            Long duration = subscribedPlan.getDuration();

            Date subscribedDate = subscribedInstructor.getSubscribedDate();

            Calendar cal = Calendar.getInstance();
            cal.setTime(subscribedDate);
            cal.add(Calendar.DATE, Math.toIntExact(duration));

            Date subscriptionEndDate = cal.getTime();
            Date currentDate = new Date();


            if (subscribedInstructor.getSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(
                    KeyConstants.KEY_PAID) && subscriptionEndDate.after(currentDate)) {
                //TODO Instructor already subscribed
                throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_INSTRUCTOR_ALREADY_SUBSCRIBED, MessageConstants.ERROR);
            } else {

                // TODO Instructor subscription
                // TODO Instructor has been subscribed but subscription period was over
                // TODO Allow re-subscription

                // TODO Will be entered as new entry under subscription Audit

                subscribedInstructor.setSubscriptionPlan(newSubscriptionPlan);
                subscribedInstructor.setSubscribedDate(new Date());
                subscribedInstructor.setSubscriptionStatus(newSubscriptionStatus);

                InstructorSubscription savedInstructorSubscription = instructorSubscriptionRepo.save(subscribedInstructor);

                SubscriptionAudit subscriptionAudit = new SubscriptionAudit();

                subscriptionAudit.setUser(userComponents.getUser());
                subscriptionAudit.setSubscriptionType(subscriptionType);
                subscriptionAudit.setInstructorSubscription(savedInstructorSubscription);
                subscriptionAudit.setSubscriptionPlan(newSubscriptionPlan);
                subscriptionAudit.setSubscriptionStatus(newSubscriptionStatus);
                subscriptionAudit.setSubscribedViaPlatform(newClientDevicePlatform);
                subscriptionAudit.setSubscriptionDate(new Date());
                subscriptionAudit.setAutoRenewal(instructorSubscriptionView.isAutoRenewal());

                subscriptionAuditRepo.save(subscriptionAudit);

            }
        } else {

            //TODO User newly subscribing the program

            InstructorSubscription instructorSubscription = new InstructorSubscription();
            instructorSubscription.setInstructor(instructor);
            instructorSubscription.setSubscriptionPlan(newSubscriptionPlan);
            instructorSubscription.setAutoRenewal(instructorSubscriptionView.isAutoRenewal());
            instructorSubscription.setSubscribedDate(new Date());
            instructorSubscription.setUser(userComponents.getUser());
            instructorSubscription.setSubscriptionStatus(newSubscriptionStatus);
            instructorSubscriptionRepo.save(instructorSubscription);

            /*
             * Auditing the subscription
             */
            SubscriptionAudit subscriptionAudit = new SubscriptionAudit();
            subscriptionAudit.setUser(userComponents.getUser());
            subscriptionAudit.setSubscriptionType(subscriptionType);
            subscriptionAudit.setInstructorSubscription(instructorSubscription);
            subscriptionAudit.setSubscriptionPlan(newSubscriptionPlan);
            subscriptionAudit.setSubscriptionStatus(newSubscriptionStatus);
            subscriptionAudit.setSubscribedViaPlatform(newClientDevicePlatform);
            subscriptionAudit.setSubscriptionDate(new Date());
            subscriptionAudit.setAutoRenewal(instructorSubscriptionView.isAutoRenewal());

            subscriptionAuditRepo.save(subscriptionAudit);

        }
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SUBSCRIPTION_ADDED, null);
    }

    /**
     * Returns current total subscriptions count for a program uploaded by an Instructor
     *
     * @param instructorId
     * @return
     */
    public long getActiveSubscripionCountOfAnInstructor(Long instructorId) {
        long subscriptionCount = 0;
        subscriptionCount += programSubscriptionJPA.getActiveSubscriptionCountOfAnInstructorForIos(instructorId);
        subscriptionCount += programSubscriptionJPA.getActiveSubscriptionCountOfAnInstructorForStripe(instructorId);
        return subscriptionCount;
    }


    /**
     * Returns current total subscriptions for a packages uploaded by an Instructor
     * ###### packageSubscriptions - Contains the total subscriptions which includes TRIAL, Unsubscribed, Payment Pending mode also
     * ###### onlySubscribedPackages - Contains only the subscribed packages list
     * <p>
     * Method to Get paid Package subscriptions of an instructor
     *
     * @param instructorId
     * @return
     */
    public long getPaidSubscribedPackagesOfAnInstructor(Long instructorId) {
        long subscriptionCount = 0;
        subscriptionCount += packageSubscriptionJpa.getActivePackageSubscriptionCountOfAnInstructor(instructorId);
        return subscriptionCount;
    }

    /**
     * Returns current total subscriptions of a program
     * <p>
     * ######## totalProgramSubscriptions - list that includes programs with TRIAL/UNSUBSCRIBED/PAYMENT PENDING subscription status also
     * ######## onlySubscribedPrograms - List that contains only subscribed programs
     *
     * @param programId
     * @return
     */
    public List<ProgramSubscription> getPaidSubscriptionsOfProgram(Long programId) {
        List<ProgramSubscription> totalProgramSubscriptions = programSubscriptionRepo.findByProgramProgramId(programId);
        List<ProgramSubscription> onlySubscribedPrograms = new ArrayList<>();
        for (ProgramSubscription programSubscription : totalProgramSubscriptions) {
            if (programSubscription.getUser() != null) {
                SubscriptionStatus subscriptionStatus = getMemberProgramSubscriptionStatus(programSubscription);
                // Getting only the active subscribed programs
                if (subscriptionStatus != null && KeyConstants.KEY_PAID.equals(subscriptionStatus.getSubscriptionStatusName()) || KeyConstants.KEY_PAYMENT_PENDING.equals(subscriptionStatus.getSubscriptionStatusName())) {
                    onlySubscribedPrograms.add(programSubscription);
                }
            }
        }
        return onlySubscribedPrograms;
    }

    /**
     * Get Active subscription count of single program
     *
     * @param programId
     * @return
     */
    public long getActiveSubscriptionCountOfProgram(Long programId) {
        long count = 0;
        SubscriptionCountDao subscriptionCountDao = programSubscriptionJPA.getActiveSubscriptionCountForProgram(programId);
        if (subscriptionCountDao != null) {
            if (subscriptionCountDao.getStripeActiveSubscriptionCount() != null) {
                count += subscriptionCountDao.getStripeActiveSubscriptionCount();
            }
            if (subscriptionCountDao.getIosActiveSubscriptionCount() != null) {
                count += subscriptionCountDao.getIosActiveSubscriptionCount();
            }
        }
        return count;
    }

    /**
     * Get Overall active subscription count for list of programs
     *
     * @param programIdsList
     * @return
     */
    public long getOverallActiveSubscriptionCountForProgramsList(List<Long> programIdsList) {
        long subscriptionCount = 0;
        if (!programIdsList.isEmpty()) {
            subscriptionCount += programSubscriptionJPA.getStripeActiveSubscriptionCountForPrograms(programIdsList);
            subscriptionCount += programSubscriptionJPA.getIosActiveSubscriptionCountForPrograms(programIdsList);

        }
        return subscriptionCount;
    }

    /**
     * get Overall Active Subscription Count For Packages List
     *
     * @param packageIdsList
     * @return
     */
    public long getOverallActiveSubscriptionCountForPackagesList(List<Long> packageIdsList) {
        long subscriptionCount = 0;
        if (!packageIdsList.isEmpty()) {
            subscriptionCount += packageSubscriptionJpa.getOverallActiveSubscriptionCountForPackagesList(packageIdsList);
        }
        return subscriptionCount;
    }


    /**
     * Active Trial and paid subscriptions of programs list
     *
     * @param programIdList
     * @return
     */
    public List<ProgramSubscription> getActiveTrialAndPaidSubscriptionsOfProgramList(List<Long> programIdList) {
        List<ProgramSubscription> totalProgramSubscriptions = programSubscriptionRepo.findByProgramProgramIdIn(programIdList);
        List<ProgramSubscription> activeTrialAndPaidSubscriptions = new ArrayList<>();
        for (ProgramSubscription programSubscription : totalProgramSubscriptions) {
            if (programSubscription.getUser() != null) {
                SubscriptionStatus subscriptionStatus = getMemberProgramSubscriptionStatus(programSubscription);
                if (subscriptionStatus != null) {
                    // Getting only the active trial and paid subscription programs
                    if (KeyConstants.KEY_PAID.equals(subscriptionStatus.getSubscriptionStatusName()) || KeyConstants.KEY_PAYMENT_PENDING.equals(subscriptionStatus.getSubscriptionStatusName())) {
                        activeTrialAndPaidSubscriptions.add(programSubscription);
                    } else if (KeyConstants.KEY_TRIAL.equals(subscriptionStatus.getSubscriptionStatusName())) {
                        int completedWorkouts = workoutCompletionRepository.countByMemberUserIdAndProgramProgramId(programSubscription.getUser().getUserId(), programSubscription.getProgram().getProgramId());
                        int trialWorkouts = fitwiseUtils.getTrialWorkoutsCountForProgram(programSubscription.getUser(), programSubscription.getProgram());
                        if (completedWorkouts < trialWorkouts) {
                            activeTrialAndPaidSubscriptions.add(programSubscription);
                        }
                    }
                }

            }
        }
        return activeTrialAndPaidSubscriptions;
    }

    /**
     * Returns the list of current subscriptions done by an User
     * ######### totalProgramSubscriptions - liPst that includes programs with TRIAL/UNSUBSCRIBED/PAYMENT PENDING subscription status also
     * ######### onlySubscribedPrograms - List that contains only subscribed programs
     *
     * @param userId
     * @return
     */
    public List<ProgramSubscription> getPaidProgramSubscriptionsByAnUser(Long userId) {
        List<ProgramSubscription> totalProgramSubscriptions = programSubscriptionRepo.findByUserUserIdOrderBySubscribedDateDesc(userId);
        List<ProgramSubscription> onlySubscribedPrograms = new ArrayList<>();
        for (ProgramSubscription programSubscription : totalProgramSubscriptions) {
            if (programSubscription.getUser() != null) {
                SubscriptionStatus subscriptionStatus = getMemberProgramSubscriptionStatus(programSubscription);
                // Getting only the active subscribed programs
                if (subscriptionStatus != null && (KeyConstants.KEY_PAID.equals(subscriptionStatus.getSubscriptionStatusName()) || KeyConstants.KEY_PAYMENT_PENDING.equals(subscriptionStatus.getSubscriptionStatusName()))) {
                    onlySubscribedPrograms.add(programSubscription);
                }
            }
        }
        return onlySubscribedPrograms;
    }

    public List<PackageSubscription> getPaidPackageSubscriptionsByAnUser(Long userId) {
        List<PackageSubscription> totalPackageSubscriptions = packageSubscriptionRepository.findByUserUserIdOrderBySubscribedDateDesc(userId);
        List<PackageSubscription> onlySubscribedPrograms = new ArrayList<>();
        for (PackageSubscription packageSubscription : totalPackageSubscriptions) {
            if (packageSubscription.getUser() != null) {
                SubscriptionStatus subscriptionStatus = getMemberPackageSubscriptionStatus(packageSubscription);
                // Getting only the active subscribed programs
                if (subscriptionStatus != null && (KeyConstants.KEY_PAID.equals(subscriptionStatus.getSubscriptionStatusName()) || KeyConstants.KEY_PAYMENT_PENDING.equals(subscriptionStatus.getSubscriptionStatusName()))) {
                    onlySubscribedPrograms.add(packageSubscription);
                }
            }
        }
        return onlySubscribedPrograms;
    }

    /**
     * @param memberId
     * @param programId
     * @return
     */
    public boolean isProgramTrialOrSubscribedByUser(Long memberId, Long programId) {
        boolean isSubscribed = false;
        List<String> subscriptionStatusList = Arrays.asList(KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING, KeyConstants.KEY_TRIAL);
        SubscriptionStatus programSubscriptionStatus = null;
        SubscriptionStatus packageProgramSubscriptionStatus = null;
        ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(memberId, programId);
        if (programSubscription != null) {
            programSubscriptionStatus = getMemberProgramSubscriptionStatus(programSubscription);
        }
        PackageProgramSubscription packageProgramSubscription = packageProgramSubscriptionRepository.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(memberId, programId);
        if (packageProgramSubscription != null) {
            packageProgramSubscriptionStatus = getMemberPackageProgramSubscriptionStatus(packageProgramSubscription);
        }
        if ((programSubscriptionStatus != null && subscriptionStatusList.stream().anyMatch(programSubscriptionStatus.getSubscriptionStatusName()::equalsIgnoreCase)) ||
                (packageProgramSubscriptionStatus != null && subscriptionStatusList.stream().anyMatch(packageProgramSubscriptionStatus.getSubscriptionStatusName()::equalsIgnoreCase))) {
            isSubscribed = true;
        }
        return isSubscribed;
    }

    public boolean isTrialOrSubscribedByUser(SubscriptionStatus subscriptionStatus) {
        boolean isSubscribed = false;
        List<String> subscriptionStatusList = Arrays.asList(KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING, KeyConstants.KEY_TRIAL);
        if (subscriptionStatus != null && subscriptionStatusList.stream().anyMatch(subscriptionStatus.getSubscriptionStatusName()::equalsIgnoreCase)) {
            isSubscribed = true;
        }
        return isSubscribed;
    }

    /**
     * Method to get list of member active program subscription DAO with search
     *
     * @param memberId
     * @param subscriptionStatusList
     * @param searchName
     * @param subscriptionStatusParam
     * @return
     */
    public List<ProgramSubscriptionDAOWithStripe> getActiveMemberProgramSubscriptionsDAO(List<String> subscriptionStatusList, Long memberId, Optional<String> searchName, String subscriptionStatusParam) {

        List<ProgramSubscriptionDAO> programSubscriptionListOfAnIOS = new ArrayList<>();
        List<ProgramSubscriptionDAO> programSubscriptionListOfAnStripe = new ArrayList<>();
        if (subscriptionStatusParam.equalsIgnoreCase(KeyConstants.KEY_TRIAL)) {
            /**
             * Getting Trial program subscriptions
             */
            programSubscriptionListOfAnStripe = programSubscriptionJPA.getProgramSubscriptionListForTrialMember(subscriptionStatusList, memberId, searchName);
        } else if (subscriptionStatusParam.equalsIgnoreCase(KeyConstants.KEY_PAID)) {
            /**
             * Getting Paid program subscriptions
             */
            List<Long> appleSubscriptionProgramId = orderManagementJPA.getActiveProgramSubscriptionsProgramIdListByUserIdForIOS(memberId, true);
            if (!appleSubscriptionProgramId.isEmpty()) {
                programSubscriptionListOfAnIOS = programSubscriptionJPA.getProgramSubscriptionListByProgramListAndMemberId(appleSubscriptionProgramId, subscriptionStatusList, memberId, searchName);
            }

            programSubscriptionListOfAnStripe = programSubscriptionJPA.getActiveProgramSubscriptionsListByUserIdForStripe(subscriptionStatusList, memberId, true, searchName);
        } else if (subscriptionStatusParam.equalsIgnoreCase(KeyConstants.KEY_EXPIRED)) {
            /**
             * Getting Expired program subscriptions
             */
            List<Long> appleSubscriptionProgramId = orderManagementJPA.getActiveProgramSubscriptionsProgramIdListByUserIdForIOS(memberId, false);
            if (!appleSubscriptionProgramId.isEmpty()) {
                programSubscriptionListOfAnIOS = programSubscriptionJPA.getProgramSubscriptionListByProgramListAndMemberId(appleSubscriptionProgramId, subscriptionStatusList, memberId, searchName);
            }

            programSubscriptionListOfAnStripe = programSubscriptionJPA.getActiveProgramSubscriptionsListByUserIdForStripe(subscriptionStatusList, memberId, false, searchName);
        }

        List<ProgramSubscriptionDAO> programSubscriptionDAOList = new ArrayList<>();
        programSubscriptionDAOList.addAll(programSubscriptionListOfAnIOS);
        programSubscriptionDAOList.addAll(programSubscriptionListOfAnStripe);

        //Extracting order management id list separated by mode of payment(IOS or (ANDROID, WEB)
        List<Integer> iosOrderIdList = new ArrayList<>();

        if (!programSubscriptionDAOList.isEmpty() && !subscriptionStatusParam.equalsIgnoreCase(KeyConstants.KEY_TRIAL)){
            iosOrderIdList = programSubscriptionDAOList.stream().filter(manageSubscriptionDAO -> manageSubscriptionDAO.getSubscribedViaPlatform().getPlatformTypeId() == 2)
                    .map(manageSubscriptionDAO -> manageSubscriptionDAO.getOrderManagement().getId()).collect(Collectors.toList());
        }
        List<Integer> androidOrWebOrderIdList = new ArrayList<>();
        if (!programSubscriptionDAOList.isEmpty() && !subscriptionStatusParam.equalsIgnoreCase(KeyConstants.KEY_TRIAL)){
            androidOrWebOrderIdList = programSubscriptionDAOList.stream().filter(manageSubscriptionDAO -> manageSubscriptionDAO.getSubscribedViaPlatform().getPlatformTypeId() == 1 ||
                    manageSubscriptionDAO.getSubscribedViaPlatform().getPlatformTypeId() == 3).map(manageSubscriptionDAO -> manageSubscriptionDAO.getOrderManagement().getId())
                    .collect(Collectors.toList());
        }
        //Getting stripe payment transaction status
        List<StripePaymentDAO> stripePaymentDAOList = new ArrayList<>();
        if (!androidOrWebOrderIdList.isEmpty()) {
            stripePaymentDAOList = stripePaymentJPA.getStripeTransactionStatusByOrderManagementIdList(androidOrWebOrderIdList);
        }

        //Getting stripe payment transaction status based in modified date desc
        Map<Integer, Optional<StripePaymentDAO>> stripeSubscriptionStatusMap = new HashMap<>();
        if (!stripePaymentDAOList.isEmpty()) {
            stripeSubscriptionStatusMap = stripePaymentDAOList.stream().collect(
                    groupingBy(StripePaymentDAO::getOrderManagementId, maxBy(Comparator.comparing(StripePaymentDAO::getModifiedDate))));
        }

        //Getting apple expiry date
        List<ApplePaymentDAO> applePaymentDAOList = new ArrayList<>();
        if (!iosOrderIdList.isEmpty()) {
            applePaymentDAOList = applePaymentJPA.getAppleExpiryDateByOrderManagementIdList(iosOrderIdList);
        }

        //Getting apple expiry date based in created date desc
        Map<Integer, Optional<ApplePaymentDAO>> applePaymentDAOMap = new HashMap<>();
        if (!applePaymentDAOList.isEmpty()) {
            applePaymentDAOMap = applePaymentDAOList.stream().collect(
                    groupingBy(ApplePaymentDAO::getOrderManagementId, maxBy(Comparator.comparing(ApplePaymentDAO::getCreatedDate))));
        }

        //Extracting program id list from programSubscriptionDAOList
        List<Long> programIdList = new ArrayList<>();
        if (!programSubscriptionDAOList.isEmpty()) {
            programIdList = programSubscriptionDAOList.stream().map(programSubscriptionDAO -> programSubscriptionDAO.getProgram().getProgramId()).collect(Collectors.toList());
        }
        List<WorkoutCompletionDAO> workoutCompletionDAOList = new ArrayList<>();
        if (!programIdList.isEmpty()) {
            workoutCompletionDAOList = programJpa.getWorkoutCompletionListByProgramIdList(programIdList, memberId);
        }
        //Creating map of Map<ProgramId, WorkoutCompletionDAO>
        Map<Long, WorkoutCompletionDAO> workoutCompletionDAOMap = new HashMap<>();
        if (!workoutCompletionDAOList.isEmpty()) {
            workoutCompletionDAOMap = workoutCompletionDAOList.stream().collect(Collectors.toMap(
                    WorkoutCompletionDAO::getProgramId, workoutCompletionDAO -> workoutCompletionDAO
            ));
        }

        List<ProgramSubscriptionDAOWithStripe> programSubscriptionDAOWithStripeList = new ArrayList<>();
        for (ProgramSubscriptionDAO programSubscriptionDAO : programSubscriptionDAOList) {
            ProgramSubscriptionDAOWithStripe programSubscriptionDAOWithStripe = new ProgramSubscriptionDAOWithStripe();

            programSubscriptionDAOWithStripe.setProgram(programSubscriptionDAO.getProgram());
            programSubscriptionDAOWithStripe.setSubscriptionPlan(programSubscriptionDAO.getSubscriptionPlan());
            programSubscriptionDAOWithStripe.setSubscribedDate(programSubscriptionDAO.getSubscribedDate());
            programSubscriptionDAOWithStripe.setSubscriptionStatus(programSubscriptionDAO.getSubscriptionStatus());
            programSubscriptionDAOWithStripe.setOrderManagement(programSubscriptionDAO.getOrderManagement());
            programSubscriptionDAOWithStripe.setSubscribedViaPlatform(programSubscriptionDAO.getSubscribedViaPlatform());

            if (stripeSubscriptionStatusMap != null && !subscriptionStatusParam.equalsIgnoreCase(KeyConstants.KEY_TRIAL) &&
                    stripeSubscriptionStatusMap.containsKey(programSubscriptionDAO.getOrderManagement().getId())){
                if (stripeSubscriptionStatusMap.get(programSubscriptionDAO.getOrderManagement().getId()).isPresent()){
                    programSubscriptionDAOWithStripe.setStripeTransactionStatus(stripeSubscriptionStatusMap.get(programSubscriptionDAO.getOrderManagement().getId()).get().getStripeTransactionStatus());
                }
            }
            if (applePaymentDAOMap != null && !subscriptionStatusParam.equalsIgnoreCase(KeyConstants.KEY_TRIAL) &&
                    applePaymentDAOMap.containsKey(programSubscriptionDAO.getOrderManagement().getId())){
                if (applePaymentDAOMap.get(programSubscriptionDAO.getOrderManagement().getId()).isPresent()){
                    programSubscriptionDAOWithStripe.setAppleExpiryDate(applePaymentDAOMap.get(programSubscriptionDAO.getOrderManagement().getId()).get().getAppleExpiryDate());
                }
            }
            if (workoutCompletionDAOMap != null && workoutCompletionDAOMap.containsKey(programSubscriptionDAO.getProgram().getProgramId())) {
                programSubscriptionDAOWithStripe.setWorkoutCompletionCount(workoutCompletionDAOMap.get(programSubscriptionDAO.getProgram().getProgramId()).getWorkoutCompletionCount());
                programSubscriptionDAOWithStripe.setFirstWorkoutCompletionDate(workoutCompletionDAOMap.get(programSubscriptionDAO.getProgram().getProgramId()).getFirstWorkoutCompletionDate());
                programSubscriptionDAOWithStripe.setLastWorkoutCompletionDate(workoutCompletionDAOMap.get(programSubscriptionDAO.getProgram().getProgramId()).getLastWorkoutCompletionDate());
            }

            programSubscriptionDAOWithStripeList.add(programSubscriptionDAOWithStripe);

        }

        return programSubscriptionDAOWithStripeList;
    }


    /**
     * Method to get list of member active program subscription DAO with search
     *
     * @param memberId
     * @param subscriptionStatusList
     * @param searchName
     * @param subscriptionStatusParam
     * @return
     */
    public List<PackageSubscriptionDAOWithStripe> getActivePackageSubscriptionDAOForMember(List<String> subscriptionStatusList, Long memberId, Optional<String> searchName, String subscriptionStatusParam) {
        List<PackageSubscriptionDAO> packageSubscriptionDAOS = new ArrayList<>();
        if (subscriptionStatusParam.equalsIgnoreCase(KeyConstants.KEY_PAID)) {
            /**
             * Getting Paid program subscriptions
             */
            packageSubscriptionDAOS = packageSubscriptionJpa.getActivePackageSubscriptionDAOAnMember(subscriptionStatusList, memberId, searchName, true);

        } else if (subscriptionStatusParam.equalsIgnoreCase(KeyConstants.KEY_EXPIRED)) {
            /**
             * Getting Expired program subscriptions
             */
            packageSubscriptionDAOS = packageSubscriptionJpa.getActivePackageSubscriptionDAOAnMember(subscriptionStatusList, memberId, searchName, false);
        }

        //Extracting order management id list from packageSubscriptionDAOS
        List<Integer> orderManagementIdList = new ArrayList<>();
        if (!packageSubscriptionDAOS.isEmpty()) {
            orderManagementIdList = packageSubscriptionDAOS.stream().map(packageSubscriptionDAO -> packageSubscriptionDAO.getOrderManagement().getId()).collect(Collectors.toList());
        }

        //Getting stripe payment transaction status
        List<StripePaymentDAO> stripePaymentDAOList = new ArrayList<>();
        if (!orderManagementIdList.isEmpty()) {
            stripePaymentDAOList = stripePaymentJPA.getStripeTransactionStatusByOrderManagementIdList(orderManagementIdList);
        }
        //Getting stripe payment transaction status based in modified date desc
        Map<Integer, Optional<StripePaymentDAO>> stripeSubscriptionStatusMap = new HashMap<>();
        if (!stripePaymentDAOList.isEmpty()) {
            stripeSubscriptionStatusMap = stripePaymentDAOList.stream().collect(
                    groupingBy(StripePaymentDAO::getOrderManagementId, maxBy(Comparator.comparing(StripePaymentDAO::getModifiedDate))));
        }

        //Extracting program id list from programSubscriptionDAOList
        List<Long> programIdList = new ArrayList<>();
        if (!packageSubscriptionDAOS.isEmpty()) {
            programIdList = packageSubscriptionDAOS.stream().map(packageSubscriptionDAO -> packageSubscriptionDAO.getProgram().getProgramId()).collect(Collectors.toList());
        }
        List<WorkoutCompletionDAO> workoutCompletionDAOList = new ArrayList<>();
        if (!programIdList.isEmpty()) {
            workoutCompletionDAOList = programJpa.getWorkoutCompletionListByProgramIdList(programIdList, memberId);
        }
        //Getting apple expiry date based in created date desc
        Map<Long, WorkoutCompletionDAO> workoutCompletionDAOMap = new HashMap<>();
        if (workoutCompletionDAOList != null) {
            workoutCompletionDAOMap = workoutCompletionDAOList.stream().collect(Collectors.toMap(
                    WorkoutCompletionDAO::getProgramId, workoutCompletionDAO -> workoutCompletionDAO
            ));
        }

        List<PackageSubscriptionDAOWithStripe> packageSubscriptionDAOWithStripeList = new ArrayList<>();
        for (PackageSubscriptionDAO packageSubscriptionDAO : packageSubscriptionDAOS) {
            PackageSubscriptionDAOWithStripe packageSubscriptionDAOWithStripe = new PackageSubscriptionDAOWithStripe();

            packageSubscriptionDAOWithStripe.setSubscribedViaPlatform(packageSubscriptionDAO.getSubscribedViaPlatform());
            packageSubscriptionDAOWithStripe.setSubscribedDate(packageSubscriptionDAO.getSubscribedDate());
            packageSubscriptionDAOWithStripe.setSubscriptionPlan(packageSubscriptionDAO.getSubscriptionPlan());
            packageSubscriptionDAOWithStripe.setProgram(packageSubscriptionDAO.getProgram());
            packageSubscriptionDAOWithStripe.setSubscriptionPackage(packageSubscriptionDAO.getSubscriptionPackage());
            packageSubscriptionDAOWithStripe.setSubscriptionStatus(packageSubscriptionDAO.getSubscriptionStatus());
            packageSubscriptionDAOWithStripe.setOrderManagement(packageSubscriptionDAO.getOrderManagement());

            if (stripeSubscriptionStatusMap != null && stripeSubscriptionStatusMap.containsKey(packageSubscriptionDAO.getOrderManagement().getId())) {
                if (stripeSubscriptionStatusMap.get(packageSubscriptionDAO.getOrderManagement().getId()).isPresent()) {
                    packageSubscriptionDAOWithStripe.setStripeTransactionStatus(stripeSubscriptionStatusMap.get(packageSubscriptionDAO.getOrderManagement().getId()).get().getStripeTransactionStatus());
                }
            }
            if (workoutCompletionDAOMap != null && workoutCompletionDAOMap.containsKey(packageSubscriptionDAO.getProgram().getProgramId())) {
                packageSubscriptionDAOWithStripe.setWorkoutCompletionCount(workoutCompletionDAOMap.get(packageSubscriptionDAO.getProgram().getProgramId()).getWorkoutCompletionCount());
                packageSubscriptionDAOWithStripe.setFirstWorkoutCompletionDate(workoutCompletionDAOMap.get(packageSubscriptionDAO.getProgram().getProgramId()).getFirstWorkoutCompletionDate());
                packageSubscriptionDAOWithStripe.setLastWorkoutCompletionDate(workoutCompletionDAOMap.get(packageSubscriptionDAO.getProgram().getProgramId()).getLastWorkoutCompletionDate());
            }

            packageSubscriptionDAOWithStripeList.add(packageSubscriptionDAOWithStripe);

        }

        return packageSubscriptionDAOWithStripeList;
    }


    /**
     * Method to get list of member active program subscription for manage subscription
     *
     * @param memberId
     * @return
     */
    private List<ManageSubscriptionForProgramWithStripe> getActiveMemberProgramSubscriptions(Long memberId) {

        List<ManageSubscriptionDAO> programSubscriptionListOfAnIOS = new ArrayList<>();
        List<Long> appleSubscriptionProgramId = orderManagementJPA.getActiveProgramSubscriptionsProgramIdListByUserIdForIOS(memberId, true);
        if (!appleSubscriptionProgramId.isEmpty()) {
            programSubscriptionListOfAnIOS = programSubscriptionJPA.getProgramSubscriptionListByProgramListAndMemberId(appleSubscriptionProgramId, memberId);
        }

        List<ManageSubscriptionDAO> programSubscriptionListOfAnStripe = programSubscriptionJPA.getActiveProgramSubscriptionsListByUserIdForStripe(memberId);

        //concatenating the IOS and STRIPE subscriptions
        List<ManageSubscriptionDAO> manageSubscriptionDAOList = new ArrayList<>();
        manageSubscriptionDAOList.addAll(programSubscriptionListOfAnIOS);
        manageSubscriptionDAOList.addAll(programSubscriptionListOfAnStripe);

        //Some user's may subscribe same program from apple and ios.We have to give a single instance for that particular program. So restricting duplication by program id.
        manageSubscriptionDAOList = manageSubscriptionDAOList.stream()
                .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparingLong(value -> value.getProgram().getProgramId()))),
                        ArrayList::new));

        //Extracting program id list separated by mode of payment(IOS or (ANDROID, WEB)
        List<Long> iosProgramIdList = manageSubscriptionDAOList.stream().filter(manageSubscriptionDAO -> manageSubscriptionDAO.getSubscribedViaPlatform().getPlatformTypeId() == 2)
                .map(manageSubscriptionDAO -> manageSubscriptionDAO.getProgram().getProgramId()).collect(Collectors.toList());
        List<Long> androidOrWebProgramIdList = manageSubscriptionDAOList.stream().filter(manageSubscriptionDAO -> manageSubscriptionDAO.getSubscribedViaPlatform().getPlatformTypeId() == 1 ||
                manageSubscriptionDAO.getSubscribedViaPlatform().getPlatformTypeId() == 3).map(manageSubscriptionDAO -> manageSubscriptionDAO.getProgram().getProgramId())
                .collect(Collectors.toList());

        //getting stripe subscription status for extracted program id's
        List<StripeSubscriptionStatusDAO> stripeSubscriptionStatusDAOList = new ArrayList<>();
        if (!androidOrWebProgramIdList.isEmpty()) {
            stripeSubscriptionStatusDAOList = programSubscriptionJPA.getStripeSubscriptionStatusByProgramIdListAndMemberId(androidOrWebProgramIdList, memberId);
        }

        //getting apple subscription status for extracted program id's
        List<AppleSubscriptionStatusDAO> appleSubscriptionStatusDAOList = new ArrayList<>();
        if (!iosProgramIdList.isEmpty()) {
            appleSubscriptionStatusDAOList = programSubscriptionJPA.getIOSSubscriptionStatusByProgramIdListAndMemberId(iosProgramIdList, memberId);
        }

        //Getting stripe subscription status map<programId, stripeStatus> based on latest modified date
        Map<Long, Optional<StripeSubscriptionStatusDAO>> stripeSubscriptionStatusMap = stripeSubscriptionStatusDAOList.stream().collect(
                groupingBy(StripeSubscriptionStatusDAO::getProgramId, maxBy(Comparator.comparing(StripeSubscriptionStatusDAO::getModifiedDate))));

        //Getting apple subscription status map<programId, appleStatus> based on latest modified date
        Map<Long, Optional<AppleSubscriptionStatusDAO>> appleSubscriptionStatusMap = appleSubscriptionStatusDAOList.stream().collect(
                groupingBy(AppleSubscriptionStatusDAO::getProgramId, maxBy(Comparator.comparing(AppleSubscriptionStatusDAO::getModifiedDate))));

        //construct manage subscription with stripe response model
        List<ManageSubscriptionForProgramWithStripe> manageSubscriptionForProgramWithStripeList = new ArrayList<>();
        for (ManageSubscriptionDAO manageSubscriptionDAO : manageSubscriptionDAOList) {

            if (stripeSubscriptionStatusMap.containsKey(manageSubscriptionDAO.getProgram().getProgramId())) {
                ManageSubscriptionForProgramWithStripe manageSubscriptionForProgramWithStripe = new ManageSubscriptionForProgramWithStripe();

                manageSubscriptionForProgramWithStripe.setProgram(manageSubscriptionDAO.getProgram());
                manageSubscriptionForProgramWithStripe.setSubscribedDate(manageSubscriptionDAO.getSubscribedDate());
                manageSubscriptionForProgramWithStripe.setSubscribedViaPlatform(manageSubscriptionDAO.getSubscribedViaPlatform());
                manageSubscriptionForProgramWithStripe.setOrderManagement(manageSubscriptionDAO.getOrderManagement());
                if (stripeSubscriptionStatusMap.get(manageSubscriptionDAO.getProgram().getProgramId()).isPresent()) {
                    manageSubscriptionForProgramWithStripe.setStripeSubscriptionStatus(stripeSubscriptionStatusMap.get(manageSubscriptionDAO.getProgram().getProgramId()).get().getSubscriptionStatus());
                }

                manageSubscriptionForProgramWithStripeList.add(manageSubscriptionForProgramWithStripe);
            } else if (appleSubscriptionStatusMap.containsKey(manageSubscriptionDAO.getProgram().getProgramId())) {
                ManageSubscriptionForProgramWithStripe manageSubscriptionForProgramWithStripe = new ManageSubscriptionForProgramWithStripe();

                manageSubscriptionForProgramWithStripe.setProgram(manageSubscriptionDAO.getProgram());
                manageSubscriptionForProgramWithStripe.setSubscribedDate(manageSubscriptionDAO.getSubscribedDate());
                manageSubscriptionForProgramWithStripe.setSubscribedViaPlatform(manageSubscriptionDAO.getSubscribedViaPlatform());
                manageSubscriptionForProgramWithStripe.setOrderManagement(manageSubscriptionDAO.getOrderManagement());
                if (appleSubscriptionStatusMap.get(manageSubscriptionDAO.getProgram().getProgramId()).isPresent()) {
                    manageSubscriptionForProgramWithStripe.setAppleSubscriptionStatus(appleSubscriptionStatusMap.get(manageSubscriptionDAO.getProgram().getProgramId()).get().getAppleSubscriptionStatus());
                }

                manageSubscriptionForProgramWithStripeList.add(manageSubscriptionForProgramWithStripe);
            }
        }

        return manageSubscriptionForProgramWithStripeList;
    }

    /**
     * Method to get list of member active package subscription for manage subscription
     *
     * @param memberId
     * @return
     */
    private List<ManageSubscriptionWithStripeDAO> getActiveMemberPackageSubscriptions(Long memberId) {

        //manageSubscriptionDAOForPackageList order by subscribed date desc
        List<ManageSubscriptionDAOForPackage> manageSubscriptionDAOForPackageList = packageSubscriptionJpa.getActivePackageSubscriptionDAOAnMember(memberId);

        //Extracting subscription package id list from manageSubscriptionDAOForPackageList
        List<Long> packageSubscriptionIdList = manageSubscriptionDAOForPackageList.stream().map(manageSubscriptionDAOForPackage ->
                manageSubscriptionDAOForPackage.getSubscriptionPackage().getSubscriptionPackageId()).collect(Collectors.toList());
        //getting stripe subscription status for extracted subscription package id's in and member id
        List<StripeSubscriptionStatusDAO> stripeSubscriptionStatusList = new ArrayList<>();
        if (!packageSubscriptionIdList.isEmpty()) {
            stripeSubscriptionStatusList = packageSubscriptionJpa.getStripeSubscriptionStatusBySubscriptionIdListAndMemberId(packageSubscriptionIdList, memberId);
        }

        //Getting stripe subscription status map<packageId, stripeStatus> based on latest modified date
        Map<Long, Optional<StripeSubscriptionStatusDAO>> stripeSubscriptionStatusMap = stripeSubscriptionStatusList.stream().collect(
                groupingBy(StripeSubscriptionStatusDAO::getProgramId, maxBy(Comparator.comparing(StripeSubscriptionStatusDAO::getModifiedDate))));

        //construct manage subscription with stripe response model
        List<ManageSubscriptionWithStripeDAO> manageSubscriptionWithStripeDAOList = new ArrayList<>();
        for (ManageSubscriptionDAOForPackage manageSubscriptionDAOForPackage : manageSubscriptionDAOForPackageList) {

            if (stripeSubscriptionStatusMap.containsKey(manageSubscriptionDAOForPackage.getSubscriptionPackage().getSubscriptionPackageId())) {
                ManageSubscriptionWithStripeDAO manageSubscriptionWithStripeDAO = new ManageSubscriptionWithStripeDAO();

                manageSubscriptionWithStripeDAO.setSubscriptionPackage(manageSubscriptionDAOForPackage.getSubscriptionPackage());
                manageSubscriptionWithStripeDAO.setSubscribedDate(manageSubscriptionDAOForPackage.getSubscribedDate());
                manageSubscriptionWithStripeDAO.setSubscribedViaPlatform(manageSubscriptionDAOForPackage.getSubscribedViaPlatform());
                manageSubscriptionWithStripeDAO.setOrderManagement(manageSubscriptionDAOForPackage.getOrderManagement());
                manageSubscriptionWithStripeDAO.setProgramCount(manageSubscriptionDAOForPackage.getProgramCount());
                manageSubscriptionWithStripeDAO.setSessionCount(manageSubscriptionDAOForPackage.getSessionCount());
                if (stripeSubscriptionStatusMap.get(manageSubscriptionDAOForPackage.getSubscriptionPackage().getSubscriptionPackageId()).isPresent()) {
                    manageSubscriptionWithStripeDAO.setStripeSubscriptionStatus(
                            stripeSubscriptionStatusMap.get(manageSubscriptionDAOForPackage.getSubscriptionPackage().getSubscriptionPackageId()).get().getSubscriptionStatus());
                }

                manageSubscriptionWithStripeDAOList.add(manageSubscriptionWithStripeDAO);
            }
        }

        return manageSubscriptionWithStripeDAOList;
    }

    /**
     * Method to check if the program is subscribed
     *
     * @param memberId
     * @param programId
     * @return
     */
    @Transactional
    public SubscriptionStatus getMemberProgramSubscriptionStatus(Long memberId, Long programId) {
        ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(memberId, programId);
        return getMemberProgramSubscriptionStatus(programSubscription);
    }

    public SubscriptionStatus getMemberProgramSubscriptionStatus(ProgramSubscription programSubscription) {
        /**
         *   Subscription Status codes and their names
         *   1', 'Trial'
         *  '2', 'PaymentPending'
         *  '3', 'Paid'
         *  '4', 'Unsubscribed'
         *  '5', 'Expired'
         *  '6', 'PaymentFailed'
         *  '7', 'Void'
         *  '8' Refund
         */
        SubscriptionStatus subscriptionStatus = null;
        if (programSubscription != null) {
            subscriptionStatus = programSubscription.getSubscriptionStatus();
            if (subscriptionStatus != null && (programSubscription.getSubscriptionStatus().getSubscriptionStatusId() == 3 || programSubscription.getSubscriptionStatus().getSubscriptionStatusId() == 2)) {
                if (programSubscription.getSubscribedViaPlatform().getPlatformTypeId() == 2) {
                    Date now = new Date();
                    Date expiryDate = new Date();

                    OrderManagement orderManagement = orderManagementRepo.findTop1ByUserAndProgramOrderByCreatedDateDesc(programSubscription.getUser(), programSubscription.getProgram());
                    if (orderManagement != null) {
                        ApplePayment applePayment = applePaymentRepository.findTop1ByOrderManagementOrderIdOrderByCreatedDateDesc(orderManagement.getOrderId());
                        if (applePayment != null && applePayment.getExpiryDate() != null) {
                            expiryDate = applePayment.getExpiryDate();
                        }
                    }
                    if (expiryDate.before(now)) {
                        subscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusId((long) 5);
                    }
                } else {
                    Date now = new Date();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(programSubscription.getSubscribedDate());
                    cal.add(Calendar.DAY_OF_YEAR, Math.toIntExact(programSubscription.getSubscriptionPlan().getDuration()));
                    Date subscriptionExpiryDate = cal.getTime();
                    OrderManagement orderManagement = orderManagementRepository.findTop1ByUserAndProgramOrderByCreatedDateDesc(programSubscription.getUser(), programSubscription.getProgram());
                    //Buffer in Stripe subscription expiry time
                    if (orderManagement != null && KeyConstants.KEY_STRIPE.equals(orderManagement.getModeOfPayment())) {
                        LocalDateTime localDateTime = subscriptionExpiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                        int bufferMinutes = stripeProperties.getSubscriptionExpiryBufferMinutes();
                        if (bufferMinutes > 0) {
                            localDateTime = localDateTime.plusMinutes(bufferMinutes);
                            subscriptionExpiryDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                        }
                    }
                    if (subscriptionExpiryDate.before(now)) {
                        subscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusId((long) 5);
                    }
                }
            }
        }
        return subscriptionStatus;
    }

    /**
     * Subscription status of a PackageProgramSubscription
     *
     * @param packageProgramSubscription
     * @return
     */
    public SubscriptionStatus getMemberPackageProgramSubscriptionStatus(PackageProgramSubscription packageProgramSubscription) {
        SubscriptionStatus subscriptionStatus = null;

        if (packageProgramSubscription != null) {
            subscriptionStatus = packageProgramSubscription.getSubscriptionStatus();
            if (subscriptionStatus != null) {
                if (subscriptionStatus.getSubscriptionStatusId() == 3) { // Paid State
                    Date now = new Date();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(packageProgramSubscription.getSubscribedDate());
                    cal.add(Calendar.DAY_OF_YEAR, Math.toIntExact(packageProgramSubscription.getSubscriptionPlan().getDuration()));
                    Date subscriptionExpiryDate = cal.getTime();
                    OrderManagement orderManagement = orderManagementRepository.findTop1ByUserAndSubscriptionPackageOrderByCreatedDateDesc(packageProgramSubscription.getUser(), packageProgramSubscription.getPackageSubscription().getSubscriptionPackage());
                    //Buffer in Stripe subscription expiry time
                    if (orderManagement != null && KeyConstants.KEY_STRIPE.equals(orderManagement.getModeOfPayment())) {
                        LocalDateTime localDateTime = subscriptionExpiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                        int bufferMinutes = stripeProperties.getSubscriptionExpiryBufferMinutes();
                        if (bufferMinutes > 0) {
                            localDateTime = localDateTime.plusMinutes(bufferMinutes);
                            subscriptionExpiryDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                        }
                    }
                    if (subscriptionExpiryDate.before(now)) {
                        subscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_EXPIRED);
                    }
                } else if (subscriptionStatus.getSubscriptionStatusId() == 2) { // Payment pending state
                    Date now = new Date();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(packageProgramSubscription.getSubscribedDate());
                    cal.add(Calendar.DAY_OF_YEAR, Math.toIntExact(packageProgramSubscription.getSubscriptionPlan().getDuration()));
                    Date subscriptionExpiryDate = cal.getTime();
                    if (subscriptionExpiryDate.before(now)) {
                        subscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_EXPIRED);
                    }
                }
            }
        }
        return subscriptionStatus;

    }

    public SubscriptionStatus getMemberPackageSubscriptionStatus(PackageSubscription packageSubscription) {
        SubscriptionStatus subscriptionStatus = null;

        if (packageSubscription != null) {
            subscriptionStatus = packageSubscription.getSubscriptionStatus();
            if (subscriptionStatus != null) {
                if (subscriptionStatus.getSubscriptionStatusId() == 3) { // Paid State
                    Date now = new Date();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(packageSubscription.getSubscribedDate());
                    cal.add(Calendar.DAY_OF_YEAR, Math.toIntExact(packageSubscription.getSubscriptionPlan().getDuration()));
                    Date subscriptionExpiryDate = cal.getTime();
                    OrderManagement orderManagement = orderManagementRepository.findTop1ByUserAndSubscriptionPackageOrderByCreatedDateDesc(packageSubscription.getUser(), packageSubscription.getSubscriptionPackage());
                    //Buffer in Stripe subscription expiry time
                    if (orderManagement != null && KeyConstants.KEY_STRIPE.equals(orderManagement.getModeOfPayment())) {
                        LocalDateTime localDateTime = subscriptionExpiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                        int bufferMinutes = stripeProperties.getSubscriptionExpiryBufferMinutes();
                        if (bufferMinutes > 0) {
                            localDateTime = localDateTime.plusMinutes(bufferMinutes);
                            subscriptionExpiryDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                        }
                    }
                    if (subscriptionExpiryDate.before(now)) {
                        subscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_EXPIRED);
                    }
                } else if (subscriptionStatus.getSubscriptionStatusId() == 2) { // Payment pending state
                    Date now = new Date();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(packageSubscription.getSubscribedDate());
                    cal.add(Calendar.DAY_OF_YEAR, Math.toIntExact(packageSubscription.getSubscriptionPlan().getDuration()));
                    Date subscriptionExpiryDate = cal.getTime();
                    if (subscriptionExpiryDate.before(now)) {
                        subscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_EXPIRED);
                    }
                }
            }
        }
        return subscriptionStatus;

    }

    /**
     * Returns current total subscriptions of a Package
     * <p>
     * ######## totalProgramSubscriptions - list that includes programs with TRIAL/UNSUBSCRIBED/PAYMENT PENDING subscription status also
     * ######## onlySubscribedPrograms - List that contains only subscribed programs
     *
     * @param subscriptionPackageId
     * @return
     */
    public List<PackageSubscription> getPaidSubscriptionsOfPackage(Long subscriptionPackageId) {
        List<PackageSubscription> totalPackageSubscriptions = packageSubscriptionRepository.findBySubscriptionPackageSubscriptionPackageId(subscriptionPackageId);
        List<PackageSubscription> onlySubscribedPackages = new ArrayList<>();
        for (PackageSubscription packageSubscription : totalPackageSubscriptions) {
            if (packageSubscription.getUser() != null) {
                SubscriptionStatus subscriptionStatus = getMemberPackageSubscriptionStatus(packageSubscription);
                // Getting only the active subscribed programs
                if (subscriptionStatus != null && (KeyConstants.KEY_PAID.equals(subscriptionStatus.getSubscriptionStatusName()) || KeyConstants.KEY_PAYMENT_PENDING.equals(subscriptionStatus.getSubscriptionStatusName()))) {
                    onlySubscribedPackages.add(packageSubscription);
                }
            }
        }
        return onlySubscribedPackages;
    }

    /**
     * get Active Subscription Count For Package
     *
     * @param packageId
     * @return
     */
    public long getActiveSubscriptionCountForPackage(Long packageId) {
        return packageSubscriptionJpa.getActiveSubscriptionCountForPackage(packageId);
    }

    @Transactional
    public void overrideAlreadySubscribedProgramData(User user, ProgramSubscription subscribedProgram, PlatformType
            platformType, boolean isPaymentSuccess, boolean isAutoRenewal, OrderManagement orderManagement) {
        // If only payment status is success, entry will be added in the Subscription table
        if (isPaymentSuccess) {

            SubscriptionStatus newSubscriptionStatus = subscriptionStatusRepo.
                    findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_PAYMENT_PENDING);
            SubscriptionPlan subscriptionPlan = subscriptionPlansRepo.findByDuration(subscribedProgram.getProgram().getDuration().getDuration());

            SubscriptionType subscriptionType = subscriptionTypesRepo.findByNameIgnoreCase(KeyConstants.KEY_PROGRAM);

            subscribedProgram.setSubscriptionPlan(subscriptionPlan);
            subscribedProgram.setSubscribedDate(new Date());
            subscribedProgram.setSubscriptionStatus(newSubscriptionStatus);
            subscribedProgram.setSubscribedViaPlatform(platformType);

            ProgramSubscription saveProgramSubscription = programSubscriptionRepo.save(subscribedProgram);

            //Subscription related notifications
            notificationTriggerService.invokeSubscriptionPushNotification(saveProgramSubscription);

            //Saving revenueAudit table to store all tax details
            ProgramSubscriptionPaymentHistory programSubscriptionPaymentHistory = new ProgramSubscriptionPaymentHistory();
            programSubscriptionPaymentHistory.setOrderManagement(orderManagement);
            subscriptionPaymentHistoryRepository.save(programSubscriptionPaymentHistory);

            SubscriptionAudit subscriptionAudit = new SubscriptionAudit();
            subscriptionAudit.setUser(user);
            subscriptionAudit.setSubscriptionType(subscriptionType);
            subscriptionAudit.setProgramSubscription(saveProgramSubscription);
            subscriptionAudit.setSubscriptionPlan(subscriptionPlan);
            subscriptionAudit.setSubscriptionStatus(newSubscriptionStatus);
            subscriptionAudit.setSubscribedViaPlatform(platformType);
            subscriptionAudit.setSubscriptionDate(new Date());
            subscriptionAudit.setAutoRenewal(isAutoRenewal);
            subscriptionAudit.setProgramSubscriptionPaymentHistory(programSubscriptionPaymentHistory);

            SubscriptionAudit subscriptionAuditOfProgram = subscriptionAuditRepo.findBySubscriptionTypeNameAndProgramSubscriptionProgramSubscriptionIdOrderBySubscriptionDateDesc(KeyConstants.KEY_PROGRAM, subscribedProgram.getProgramSubscriptionId()).get(0);


            if (subscriptionAuditOfProgram.getRenewalStatus().equalsIgnoreCase(KeyConstants.KEY_NEW) && subscriptionAuditOfProgram.getSubscriptionStatus() != null &&
                    subscriptionAuditOfProgram.getSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_TRIAL)) {
                // If renewal status is new and subscription status is trial, then next paid subscription will be set to new
                subscriptionAudit.setRenewalStatus(KeyConstants.KEY_NEW);

            } else if (subscriptionAuditOfProgram.getRenewalStatus().equalsIgnoreCase(KeyConstants.KEY_NEW)) {
                // Already the renewal status is new! So setting it has renew on the second time
                subscriptionAudit.setRenewalStatus(KeyConstants.KEY_RENEWAL);

            } else if (subscriptionAuditOfProgram.getRenewalStatus().equalsIgnoreCase(KeyConstants.KEY_RENEWAL)) {
                // Already the renewal status is renew! So will be set as renew in next coming times
                subscriptionAudit.setRenewalStatus(KeyConstants.KEY_RENEWAL);
            }
            subscriptionAuditRepo.save(subscriptionAudit);
        }
    }

    /**
     * This method is used to log the subscription if a user newly subscribing a program
     *
     * @param program
     * @param platformType     - Indicates the device platform through which the payment is carried out. Android/iOS/Web
     * @param isPaymentSuccess - In case of Authorize.net, "PaymentPending" will be immediate state once the payment is done.
     *                         After a day, once the payment is moved to settled state, state in our Fitwise system will
     *                         be changed to "Paid" state.
     */
    @Transactional
    public void addNewlySubscribedProgramData(User user, Programs program, PlatformType platformType,
                                              boolean isPaymentSuccess, boolean isAutoRenewal, OrderManagement orderManagement) {

        // If only payment status is success, entry will be added in the Subscription table
        if (isPaymentSuccess) {
            SubscriptionStatus newSubscriptionStatus = subscriptionStatusRepo.
                    findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_PAYMENT_PENDING);

            SubscriptionPlan subscriptionPlan = subscriptionPlansRepo.findByDuration(program.getDuration().getDuration());
            SubscriptionType subscriptionType = subscriptionTypesRepo.findByNameIgnoreCase(KeyConstants.KEY_PROGRAM);

            ProgramSubscription programSubscription = new ProgramSubscription();
            programSubscription.setProgram(program);
            programSubscription.setSubscriptionPlan(subscriptionPlan);
            programSubscription.setSubscribedViaPlatform(platformType);
            programSubscription.setAutoRenewal(isAutoRenewal);
            programSubscription.setSubscribedDate(new Date());
            programSubscription.setUser(user);
            programSubscription.setSubscriptionStatus(newSubscriptionStatus);
            programSubscriptionRepo.save(programSubscription);

            //Subscription related notifications
            notificationTriggerService.invokeSubscriptionPushNotification(programSubscription);

            //Saving revenueAudit table to store all tax details
            ProgramSubscriptionPaymentHistory programSubscriptionPaymentHistory = new ProgramSubscriptionPaymentHistory();
            programSubscriptionPaymentHistory.setOrderManagement(orderManagement);
            subscriptionPaymentHistoryRepository.save(programSubscriptionPaymentHistory);

            /*
             * Auditing the subscription
             */
            SubscriptionAudit subscriptionAudit = new SubscriptionAudit();
            subscriptionAudit.setUser(user);
            subscriptionAudit.setSubscriptionType(subscriptionType);
            subscriptionAudit.setProgramSubscription(programSubscription);
            subscriptionAudit.setSubscriptionPlan(subscriptionPlan);
            subscriptionAudit.setSubscriptionStatus(newSubscriptionStatus);
            subscriptionAudit.setSubscribedViaPlatform(platformType);
            subscriptionAudit.setSubscriptionDate(new Date());
            subscriptionAudit.setAutoRenewal(isAutoRenewal);
            subscriptionAudit.setRenewalStatus(KeyConstants.KEY_NEW);
            subscriptionAudit.setProgramSubscriptionPaymentHistory(programSubscriptionPaymentHistory);
            subscriptionAuditRepo.save(subscriptionAudit);
        }
    }


    /**
     * Method used to create Order
     *
     * @param program       - Program Object
     * @param paymentVendor - String that implies whether the payment is from Authorize.net or Apple
     * @param isARB         - Boolean that intimates whether auto-subscription or not
     * @return
     */
    @Transactional
    private OrderManagement createOrder(User user, Programs program, boolean isARB, String paymentVendor, PlatformType platformType, String existingOrderId) {

        OrderManagement orderManagement;
        String orderNumber;

        if (!existingOrderId.isEmpty()) {
            orderNumber = existingOrderId;
        } else {
            /*
             * The below piece of code generates a unique order number
             */
            orderNumber = OrderNumberGenerator.generateOrderNumber();
            log.info("Order id =================> " + orderNumber);
        }
        // Constructing the order management object
        orderManagement = new OrderManagement();
        orderManagement.setOrderId(orderNumber);
        orderManagement.setModeOfPayment(KeyConstants.KEY_AUTH_NET_PAYMENT_MODE);
        orderManagement.setIsAutoRenewable(isARB);
        orderManagement.setProgram(program);
        orderManagement.setUser(user);
        orderManagement.setSubscribedViaPlatform(platformType);

        // Saving the order management object in repository
        OrderManagement savedOrderManagement = orderManagementRepo.save(orderManagement);

        // Creating invoice for the order
        if (existingOrderId.isEmpty()) {
            // Create invoice if only the order is new
            createInvoice(savedOrderManagement, existingOrderId);
        }

        return savedOrderManagement;
    }

    /**
     * ,Method used to create invoice number
     *
     * @param orderManagement
     * @return
     */
    @Transactional
    private InvoiceManagement createInvoice(OrderManagement orderManagement, String exstOrdId) {
        InvoiceManagement invoiceManagement;
        try {
            if (!exstOrdId.isEmpty()) {
                invoiceManagement = invoiceManagementRepository.findByOrderManagementOrderId(exstOrdId);
            } else {
                invoiceManagement = new InvoiceManagement();
                invoiceManagement.setInvoiceNumber(OrderNumberGenerator.generateInvoiceNumber());
            }
            invoiceManagement.setOrderManagement(orderManagement);
            invoiceManagementRepository.save(invoiceManagement);

        } catch (Exception exception) {
            throw new ApplicationException(Constants.ERROR_STATUS, "Error in createInvoice method", exception.getMessage());
        }
        return invoiceManagement;
    }

    /**
     * Method to add transaction data to Payment Table
     *
     * @param aNetTransactionResponse
     * @param orderManagement
     */
    @Transactional
    private void addTransactionDataToAnetPaymentTable(ANetTransactionResponse
                                                              aNetTransactionResponse, OrderManagement orderManagement) {
        AuthNetPayment authNetPayment = new AuthNetPayment();
        authNetPayment.setOrderManagement(orderManagement);
        if (aNetTransactionResponse.getTransactionId() != null)
            authNetPayment.setTransactionId(aNetTransactionResponse.getTransactionId());
        if (orderManagement != null && orderManagement.getProgram() != null && orderManagement.getProgram().getProgramPrices() != null)
            authNetPayment.setAmountPaid(orderManagement.getProgram().getProgramPrices().getPrice());
        if (aNetTransactionResponse.getTransactionStatus() != null)
            authNetPayment.setTransactionStatus(aNetTransactionResponse.getTransactionStatus());
        if (aNetTransactionResponse.getErrorCode() != null)
            authNetPayment.setErrorCode(aNetTransactionResponse.getErrorCode());
        if (aNetTransactionResponse.getErrorMessage() != null)
            authNetPayment.setErrorMessage(aNetTransactionResponse.getErrorMessage());
        if (aNetTransactionResponse.getResponseCode() != null &&
                aNetTransactionResponse.getResponseCode().equalsIgnoreCase(KeyConstants.ANET_TRANSACTION_SUCCESS_RESPONSE_CODE)) {
            authNetPayment.setReceiptNumber(OrderNumberGenerator.generateReceiptNumber());
        }
        if (aNetTransactionResponse.getResponseCode() != null)
            authNetPayment.setResponseCode(aNetTransactionResponse.getResponseCode());
        authNetPayment.setUserCardCountryCode(aNetTransactionResponse.getCountryCode());
        authNetPayment.setIsDomesticCard(aNetTransactionResponse.getCountryCode() != null && (aNetTransactionResponse.getCountryCode().equalsIgnoreCase("US")
                || aNetTransactionResponse.getCountryCode().equalsIgnoreCase("USA")
                || aNetTransactionResponse.getCountryCode().equalsIgnoreCase("UNITED STATES")
                || aNetTransactionResponse.getCountryCode().equalsIgnoreCase("UNITEDSTATES")));
        authNetPayment = authNetPaymentRepository.save(authNetPayment);
        //Creating QBO Invoice
        if (authNetPayment.getResponseCode() != null &&
                authNetPayment.getResponseCode().equalsIgnoreCase(KeyConstants.ANET_TRANSACTION_SUCCESS_RESPONSE_CODE)) {
            InvoiceManagement invoiceManagement = invoiceManagementRepository.findByOrderManagement(authNetPayment.getOrderManagement());
            try {
                fitwiseQboEntityService.createAndSyncQboInvoice(invoiceManagement);
            } catch (Exception exception) {
                log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
            }
        }
    }

    /**
     * Method to initiate Recurring subscription for Programs using Payment Profile
     *
     * @param recurringSubscriptionRequestView
     * @return
     */
    @Transactional
    public ResponseModel initiateRecurringProgramSubscriptionUsingPaymentProfile
    (ANetRecurringSubscriptionRequestViewWithPaymentProfile recurringSubscriptionRequestView) {

        OrderResponseView orderResponseView = new OrderResponseView();

        User user = userComponents.getUser();
        ARBCreateSubscriptionResponse arbResponse;

        if (fitwiseUtils.isCurrentMemberBlocked()) {
            throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_MEMBER_BLOCKED_CANT_SUBSCRIBE, MessageConstants.ERROR);
        }
        Programs program = validationService.validateProgramIdBlocked(recurringSubscriptionRequestView.getProgramId());
        if (!KeyConstants.KEY_PUBLISH.equalsIgnoreCase(program.getStatus())) {
            throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_PROGRAM_NOT_PUBLISHED_CANT_SUBSCRIBE, MessageConstants.ERROR);
        }

        PlatformType newClientDevicePlatform = platformsRepo.findByPlatformTypeId(recurringSubscriptionRequestView.getDevicePlatformId());

        boolean isTransactionSuccess = false;

        //TODO Check if the program is already subscribed before making subscription
        ProgramSubscription subscribedProgram = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), program.getProgramId());
        if (subscribedProgram != null) {

            // Program has been already subscribed

            SubscriptionPlan subscribedPlan = subscriptionPlansRepo.findByDuration(program.getDuration().getDuration());
            long duration = subscribedPlan.getDuration();

            Date subscribedDate = subscribedProgram.getSubscribedDate();

            Calendar cal = Calendar.getInstance();
            cal.setTime(subscribedDate);
            cal.add(Calendar.DATE, Math.toIntExact(duration));

            Date subscriptionEndDate = cal.getTime();
            Date todayDate = new Date();
            if (subscribedProgram.getSubscriptionStatus() != null) {
                if (((subscribedProgram.getSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID))
                        || (subscribedProgram.getSubscriptionStatus().getSubscriptionStatusName()
                        .equalsIgnoreCase(KeyConstants.KEY_PAYMENT_PENDING))) && subscriptionEndDate.after(todayDate)) {
                    //TODO Program already subscribed
                    throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_PROGRAM_ALREADY_SUBSCRIBED, MessageConstants.ERROR);

                } else {
                    /*
                     * Program has already been subscribed but subscription period was over. Allowing re-subscription
                     */

                    // 1 - Android; 3 - Web
                    if (recurringSubscriptionRequestView.getDevicePlatformId() == 1 || recurringSubscriptionRequestView.getDevicePlatformId() == 3) {

                        // Creating Order for the Transaction
                        OrderManagement orderManagement = createOrder(user, program, true, KeyConstants.KEY_AUTH_NET, newClientDevicePlatform, recurringSubscriptionRequestView.getOrderId());

                        orderResponseView.setOrderId(orderManagement.getOrderId());

                        // For first month, One time payment will be initiated. This decision was taken because the ARB payment transaction will take place
                        // only in Midnight of the day. So, a user has to wait for the whole day to access the program. Instead for the first month
                        // one time payment transaction is triggered and he can access program quickly without any delay and subsequent payments will
                        // be triggered from ARB.

                        ANetCustomerProfileRequestView customerProfileRequestView = new ANetCustomerProfileRequestView();
                        customerProfileRequestView.setProgramId(program.getProgramId());
                        customerProfileRequestView.setCustomerProfileId(recurringSubscriptionRequestView.getCustomerProfileId());
                        customerProfileRequestView.setCustomerPaymentProfileId(recurringSubscriptionRequestView.getCustomerPaymentProfileId());
                        customerProfileRequestView.setDevicePlatformId(recurringSubscriptionRequestView.getDevicePlatformId());

                        ANetTransactionResponse aNetTransactionResponse = paymentService.initiateOneTimeProgramSubscriptionByPaymentProfile(customerProfileRequestView, program.getProgramPrice(), orderManagement.getOrderId());

                        if (aNetTransactionResponse.getResponseCode() != null && aNetTransactionResponse.getResponseCode().equalsIgnoreCase(KeyConstants.ANET_TRANSACTION_SUCCESS_RESPONSE_CODE)) {
                            isTransactionSuccess = true;
                            // TODO CALL DUE DATE LOGICS
                        }

                        AuthNetPaymentProfile paymentProfile = authNetPaymentProfileRepository.findByArbCustomerProfileIdAndArbPaymentProfileId(recurringSubscriptionRequestView.getCustomerProfileId(), recurringSubscriptionRequestView.getCustomerPaymentProfileId());
                        if (paymentProfile != null)
                            aNetTransactionResponse.setCountryCode(paymentProfile.getUserCardCountryCode());

                        // Adding the newly subscribed program data and subscription audit data to table via the below method
                        overrideAlreadySubscribedProgramData(user, subscribedProgram, newClientDevicePlatform, isTransactionSuccess, true, orderManagement);
                        // Adding the transaction data to the Payment table
                        addTransactionDataToAnetPaymentTable(aNetTransactionResponse, orderManagement);

                        // If One time payment transaction is itself failed, throw exception
                        if (!isTransactionSuccess) {
                            return new ResponseModel(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_ADDING_SUBSCRIPTION, orderResponseView);
                        }

                        // Automatic Recurring Billing subscription
                        arbResponse = paymentService.initiateRecurringProgramSubscriptionUsingPaymentProfile(recurringSubscriptionRequestView, orderManagement);

                        // Updating AuthNetPayment table with the subscription Id
                        if (arbResponse.getSubscriptionId() != null) {

                            // Logging the subscription id in the Subscription table
                            AuthNetArbSubscription authNetARBSubscription = new AuthNetArbSubscription();
                            authNetARBSubscription.setUser(userComponents.getUser());
                            authNetARBSubscription.setProgram(program);
                            authNetARBSubscription.setANetSubscriptionId(arbResponse.getSubscriptionId());
                            authNetARBSubscription.setSubscribedViaPlatform(newClientDevicePlatform);
                            // Setting the subscription status as Active
                            AuthNetSubscriptionStatus authNetSubscriptionStatus = authNetSubscriptionStatusRepository.findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_ACTIVE);
                            authNetARBSubscription.setAuthNetSubscriptionStatus(authNetSubscriptionStatus);
                            authNetArbSubscriptionRepository.save(authNetARBSubscription);

                            // Logging in subscription tracker table
                            AuthNetSubscriptionChangesTracker authNetSubscriptionChangesTracker = new AuthNetSubscriptionChangesTracker();
                            authNetSubscriptionChangesTracker.setIsSubscriptionActive(true);
                            authNetSubscriptionChangesTracker.setOrderId(orderManagement.getOrderId());
                            authNetSubscriptionChangesTracker.setSubscriptionId(arbResponse.getSubscriptionId());
                            authNetSubscriptionChangesTrackerRepository.save(authNetSubscriptionChangesTracker);

                            AuthNetPayment authNetPayment = authNetPaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
                            authNetPayment.setArbSubscriptionId(arbResponse.getSubscriptionId());
                            authNetPayment.setIsARB(true);
                            authNetPaymentRepository.save(authNetPayment);
                        }

                    } else {
                        throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_INCORRECT_PAYMENT_MODE, null);
                    }
                }
            }
        } else {

            /*
             * New Program Subscription
             */

            // 1 - Android ; 3 - Web
            if (recurringSubscriptionRequestView.getDevicePlatformId() == 1 || recurringSubscriptionRequestView.getDevicePlatformId() == 3) {

                // Creating Order for the Transaction
                OrderManagement orderManagement = createOrder(user, program, true, KeyConstants.KEY_AUTH_NET, newClientDevicePlatform, recurringSubscriptionRequestView.getOrderId());
                orderResponseView.setOrderId(orderManagement.getOrderId());

                // For first month, One time payment will be initiated. This decision was taken because the ARB payment transaction will take place
                // only in Midnight of the day. So, a user has to wait for the whole day to access the program. Instead for the first month
                // one time payment transaction is triggered and he can access program quickly without any delay and subsequent payments will
                // be triggered from ARB.

                ANetCustomerProfileRequestView customerProfileRequestView = new ANetCustomerProfileRequestView();
                customerProfileRequestView.setProgramId(program.getProgramId());
                customerProfileRequestView.setCustomerProfileId(recurringSubscriptionRequestView.getCustomerProfileId());
                customerProfileRequestView.setCustomerPaymentProfileId(recurringSubscriptionRequestView.getCustomerPaymentProfileId());
                customerProfileRequestView.setDevicePlatformId(recurringSubscriptionRequestView.getDevicePlatformId());

                ANetTransactionResponse aNetTransactionResponse = paymentService.initiateOneTimeProgramSubscriptionByPaymentProfile(customerProfileRequestView, program.getProgramPrice(), orderManagement.getOrderId());

                if (aNetTransactionResponse.getResponseCode() != null && aNetTransactionResponse.getResponseCode().equalsIgnoreCase(KeyConstants.ANET_TRANSACTION_SUCCESS_RESPONSE_CODE)) {
                    isTransactionSuccess = true;
                }

                AuthNetPaymentProfile paymentProfile = authNetPaymentProfileRepository.findByArbCustomerProfileIdAndArbPaymentProfileId(recurringSubscriptionRequestView.getCustomerProfileId(), recurringSubscriptionRequestView.getCustomerPaymentProfileId());
                if (paymentProfile != null)
                    aNetTransactionResponse.setCountryCode(paymentProfile.getUserCardCountryCode());

                // Adding the subscription auditing data to table via the below method
                addNewlySubscribedProgramData(user, program, newClientDevicePlatform, isTransactionSuccess, true, orderManagement);
                // Adding the transaction data to the Payment table
                addTransactionDataToAnetPaymentTable(aNetTransactionResponse, orderManagement);

                // If One time payment transaction is itself failed, throw exception
                if (!isTransactionSuccess) {
                    return new ResponseModel(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_ADDING_SUBSCRIPTION, orderResponseView);
                }

                // Automatic Recurring Billing subscription
                arbResponse = paymentService.initiateRecurringProgramSubscriptionUsingPaymentProfile(recurringSubscriptionRequestView, orderManagement);

                // Updating AuthNetPayment table with the subscription Id
                if (arbResponse.getSubscriptionId() != null) {

                    // Logging the subscription id in the Subscription table
                    AuthNetArbSubscription authNetARBSubscription = new AuthNetArbSubscription();
                    authNetARBSubscription.setUser(userComponents.getUser());
                    authNetARBSubscription.setProgram(program);
                    authNetARBSubscription.setANetSubscriptionId(arbResponse.getSubscriptionId());
                    authNetARBSubscription.setSubscribedViaPlatform(newClientDevicePlatform);
                    // Setting the subscription status as Active
                    AuthNetSubscriptionStatus authNetSubscriptionStatus = authNetSubscriptionStatusRepository.findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_ACTIVE);
                    authNetARBSubscription.setAuthNetSubscriptionStatus(authNetSubscriptionStatus);
                    authNetArbSubscriptionRepository.save(authNetARBSubscription);

                    // Logging in subscription tracker table
                    AuthNetSubscriptionChangesTracker authNetSubscriptionChangesTracker = new AuthNetSubscriptionChangesTracker();
                    authNetSubscriptionChangesTracker.setIsSubscriptionActive(true);
                    authNetSubscriptionChangesTracker.setOrderId(orderManagement.getOrderId());
                    authNetSubscriptionChangesTracker.setSubscriptionId(arbResponse.getSubscriptionId());
                    authNetSubscriptionChangesTrackerRepository.save(authNetSubscriptionChangesTracker);

                    // Adding the subscription id to the ANet transaction table if the transaction was from Recurring billing.
                    AuthNetPayment authNetPayment = authNetPaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
                    authNetPayment.setArbSubscriptionId(arbResponse.getSubscriptionId());
                    authNetPayment.setIsARB(true);
                    authNetPaymentRepository.save(authNetPayment);
                }
            }
        }
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SUBSCRIPTION_ADDED, orderResponseView);
    }


    /**
     * Method to initiate Recurring Subscription for Programs using Form token
     *
     * @param requestViewWithCardData
     * @return
     */
    @Transactional
    public ResponseModel initiateRecurringProgramSubscriptionUsingFormToken
    (ANetRecurringSubscriptionRequestViewWithCardData requestViewWithCardData) {
        boolean isTransactionSuccess = false;
        OrderResponseView orderResponseView = new OrderResponseView();
        ANetTransactionResponse aNetTransactionResponse = null;
        OrderManagement orderManagement = null;

        try {

            if (!requestViewWithCardData.getOrderId().isEmpty()) {
                validationService.isValidOrder(requestViewWithCardData.getOrderId());
            }
            User user = userComponents.getUser();
            if (fitwiseUtils.isCurrentMemberBlocked()) {
                throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_MEMBER_BLOCKED_CANT_SUBSCRIBE, MessageConstants.ERROR);
            }

            if (!validationService.isStringContainsOnlyAlphabets(requestViewWithCardData.getFirstName())) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FIRST_NAME_ERROR, MessageConstants.ERROR);
            }

            if (!validationService.isStringContainsOnlyAlphabets(requestViewWithCardData.getLastName())) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_LAST_NAME_ERROR, MessageConstants.ERROR);
            }

            Programs program = validationService.validateProgramIdBlocked(requestViewWithCardData.getProgramId());
            if (!KeyConstants.KEY_PUBLISH.equalsIgnoreCase(program.getStatus())) {
                throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_PROGRAM_NOT_PUBLISHED_CANT_SUBSCRIBE, MessageConstants.ERROR);
            }
            PlatformType newClientDevicePlatform = platformsRepo.findByPlatformTypeId(requestViewWithCardData.getDevicePlatformTypeId());

            //TODO Check if the program is already subscribed before making subscription
            ProgramSubscription subscribedProgram = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), program.getProgramId());
            if (subscribedProgram != null) {

                // Program has been already subscribed

                SubscriptionPlan subscribedPlan = subscriptionPlansRepo.findByDuration(program.getDuration().getDuration());
                Long duration = null;
                if (subscribedPlan != null) {
                    duration = subscribedPlan.getDuration();
                }
                if (duration == null || duration == 0) {
                    throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_IN_DURATION, null);
                }

                // Getting the subscriptionDate
                Date subscribedDate = subscribedProgram.getSubscribedDate();

                // Adding program duration to the subscribed date to get the subscription End date
                Calendar cal = Calendar.getInstance();
                cal.setTime(subscribedDate);
                cal.add(Calendar.DATE, Math.toIntExact(duration));

                Date subscriptionEndDate = cal.getTime();
                Date currentDate = new Date();
                if (subscribedProgram.getSubscriptionStatus() != null) {
                    if (((subscribedProgram.getSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID))
                            || (subscribedProgram.getSubscriptionStatus().getSubscriptionStatusName()
                            .equalsIgnoreCase(KeyConstants.KEY_PAYMENT_PENDING))) && subscriptionEndDate.after(currentDate)) {

                        // Program already subscribed
                        throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_PROGRAM_ALREADY_SUBSCRIBED, MessageConstants.ERROR);
                    } else {

                        //Program has been subscribed but the subscription was ended
                        ANetOneTimeProgramSubscriptionUsingCardRequestView oneTimeProgramSubscriptionRequestView =
                                constructANetOneTimeProgramSubscriptionUsingCardRequestView(requestViewWithCardData);

                        // Creating Order for the Transaction
                        orderManagement = createOrder(user, program, true, KeyConstants.KEY_AUTH_NET, newClientDevicePlatform, requestViewWithCardData.getOrderId());

                        orderResponseView.setOrderId(orderManagement.getOrderId());

                        /*  Initiating One time payment transaction inorder to create a payment profile using transaction ID
                         *  This is done inorder to overcome PCI compliance by not passing the card data directly to server instead processing
                         *  payment via payment profile.*/
                        aNetTransactionResponse = paymentService.initiateOneTimePaymentTransactionByCard(oneTimeProgramSubscriptionRequestView, orderManagement.getOrderId());

                        if (aNetTransactionResponse.getResponseCode() != null && aNetTransactionResponse.getResponseCode().equalsIgnoreCase(KeyConstants.ANET_TRANSACTION_SUCCESS_RESPONSE_CODE)) {
                            isTransactionSuccess = true;
                        }

                        aNetTransactionResponse.setCountryCode(requestViewWithCardData.getCountry());

                        // Adding the subscription auditing data to table via the below method
                        overrideAlreadySubscribedProgramData(user, subscribedProgram, newClientDevicePlatform, isTransactionSuccess, true, orderManagement);
                        // Adding the transaction data to the Payment table
                        addTransactionDataToAnetPaymentTable(aNetTransactionResponse, orderManagement);

                        // If One time payment transaction is itself failed, throw exception
                        if (!isTransactionSuccess) {
                            return new ResponseModel(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_ADDING_SUBSCRIPTION, orderResponseView);
                        }
                    }
                }
            } else {
                // User Newly Subscribing the program
                ANetOneTimeProgramSubscriptionUsingCardRequestView oneTimeProgramSubscriptionRequestView =
                        constructANetOneTimeProgramSubscriptionUsingCardRequestView(requestViewWithCardData);

                // Creating Order for the Transaction
                orderManagement = createOrder(user, program, true, KeyConstants.KEY_AUTH_NET, newClientDevicePlatform, requestViewWithCardData.getOrderId());
                orderResponseView.setOrderId(orderManagement.getOrderId());

                /*  Initiating One time payment transaction inorder to create a payment profile using transaction ID
                 *  This is done inorder to overcome PCI compliance by not passing the card data directly to server instead processing
                 *  payment via payment profile.*/
                aNetTransactionResponse = paymentService.initiateOneTimePaymentTransactionByCard(oneTimeProgramSubscriptionRequestView, orderManagement.getOrderId());

                if (aNetTransactionResponse.getResponseCode() != null && aNetTransactionResponse.getResponseCode().equalsIgnoreCase(KeyConstants.ANET_TRANSACTION_SUCCESS_RESPONSE_CODE)) {
                    isTransactionSuccess = true;
                }

                aNetTransactionResponse.setCountryCode(requestViewWithCardData.getCountry());

                // Adding the subscription auditing data to table via the below method
                addNewlySubscribedProgramData(user, program, newClientDevicePlatform, isTransactionSuccess, true, orderManagement);
                // Adding the transaction data to the Payment table
                addTransactionDataToAnetPaymentTable(aNetTransactionResponse, orderManagement);

                // If One time payment transaction is itself failed, throw exception
                if (!isTransactionSuccess) {
                    return new ResponseModel(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_ADDING_SUBSCRIPTION, orderResponseView);
                }
            }
            return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SUBSCRIPTION_ADDED, orderResponseView);
        } finally {
            /**
             * If Transaction is success, we are creating ARB using the below method after a delay.
             */
            if (isTransactionSuccess) {
                AuthNetPayment authNetPayment = authNetPaymentRepository.findByTransactionIdOrderByModifiedDateDesc(aNetTransactionResponse.getTransactionId());
                authNetPayment.setIsARBUnderProcessing(true);
                authNetPaymentRepository.save(authNetPayment);
                createARBUsingCustomerProfileAfterADelay(orderManagement, aNetTransactionResponse, requestViewWithCardData, authNetPayment);
            }
        }
    }


    /**
     * In Production instance, after creating customer profile, we are facing issues while creating ARB immediately.
     * The reason is Authorize.net production servers are having a delay in syncing the customer profile data.
     * The error code for this issue is E0040
     * <p>
     * In order to overcome this issue, we are introducing a delay in creating ARB after customer profile is created.
     * And also if the ARB is not created, scheduler will be keep on trying to create ARB for 5 times. After 5 times, it will get cancelled
     * and ARB status will be marked as incomplete/failure
     * <p>
     * 20000 - 20 Secs delay
     *
     * @param orderManagement
     * @param aNetTransactionResponse
     * @param requestViewWithCardData
     */
    @Transactional
    public void createARBUsingCustomerProfileAfterADelay(OrderManagement orderManagement, ANetTransactionResponse aNetTransactionResponse, ANetRecurringSubscriptionRequestViewWithCardData requestViewWithCardData, AuthNetPayment authNetPayment) {

        Timer timer = new java.util.Timer();
        int[] counter = {0};
        timer.scheduleAtFixedRate(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        counter[0]++;

                        // If ARB is not created after 5 successful attempts, cancelling the timer task
                        if (counter[0] > 5) {
                            authNetPayment.setIsARBUnderProcessing(false);
                            authNetPaymentRepository.save(authNetPayment);
                            timer.cancel();
                            timer.purge();
                        }

                        if (aNetTransactionResponse.getCustomerProfile() == null) {
                            CreateCustomerProfileResponse customerProfileResponse = CreateCustomerProfileFromTransaction.run(aNetTransactionResponse.getTransactionId());

                            if (customerProfileResponse != null && customerProfileResponse.getCustomerProfileId() != null &&
                                    customerProfileResponse.getCustomerPaymentProfileIdList() != null
                                    && !customerProfileResponse.getCustomerPaymentProfileIdList().getNumericString().isEmpty()) {

                                CustomerProfile customerProfile = new CustomerProfile();
                                customerProfile.setCustomerProfileId(customerProfileResponse.getCustomerProfileId());
                                List<PaymentProfile> paymentProfileList = new ArrayList<>();

                                PaymentProfile paymentProfile = new PaymentProfile();
                                for (String s : customerProfileResponse.getCustomerPaymentProfileIdList().getNumericString()) {
                                    paymentProfile.setPaymentProfileId(s);
                                    paymentProfileList.add(paymentProfile);
                                }

                                customerProfile.setPaymentProfileList(paymentProfileList);
                                aNetTransactionResponse.setCustomerProfile(customerProfile);
                            }
                        }

                        if (aNetTransactionResponse.getCustomerProfile() != null) {
                            ANetRecurringSubscriptionRequestViewWithPaymentProfile recurringSubscriptionRequestView = new ANetRecurringSubscriptionRequestViewWithPaymentProfile();
                            recurringSubscriptionRequestView.setProgramId(requestViewWithCardData.getProgramId());
                            recurringSubscriptionRequestView.setDevicePlatformId(requestViewWithCardData.getDevicePlatformTypeId());
                            recurringSubscriptionRequestView.setCustomerProfileId(aNetTransactionResponse.getCustomerProfile().getCustomerProfileId());
                            recurringSubscriptionRequestView.setCustomerPaymentProfileId(aNetTransactionResponse.getCustomerProfile().getPaymentProfileList().get(0).getPaymentProfileId());
                            // Initiating ARB using Payment Profile
                            ARBCreateSubscriptionResponse apiResponse = paymentService.initiateRecurringProgramSubscriptionUsingPaymentProfile(recurringSubscriptionRequestView, orderManagement);
                            // Updating AuthNetPayment table with the subscription Id
                            if (apiResponse != null && apiResponse.getSubscriptionId() != null) {
                                apiResponse.getProfile().getCustomerPaymentProfileId();
                                // Logging the subscription id in the Subscription table
                                AuthNetArbSubscription authNetARBSubscription = new AuthNetArbSubscription();
                                authNetARBSubscription.setUser(orderManagement.getUser());
                                authNetARBSubscription.setProgram(orderManagement.getProgram());
                                authNetARBSubscription.setANetSubscriptionId(apiResponse.getSubscriptionId());
                                authNetARBSubscription.setSubscribedViaPlatform(orderManagement.getSubscribedViaPlatform());
                                // Setting the subscription status as Active
                                AuthNetSubscriptionStatus authNetSubscriptionStatus = authNetSubscriptionStatusRepository.findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_ACTIVE);
                                authNetARBSubscription.setAuthNetSubscriptionStatus(authNetSubscriptionStatus);
                                authNetArbSubscriptionRepository.save(authNetARBSubscription);
                                // Logging the subscription id in the Subscription table
                                AuthNetPayment authNetPayment = authNetPaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(orderManagement.getOrderId());
                                authNetPayment.setArbSubscriptionId(apiResponse.getSubscriptionId());
                                authNetPayment.setIsARB(true);
                                authNetPayment.setIsARBUnderProcessing(false);
                                authNetPaymentRepository.save(authNetPayment);
                                // Logging in subscription tracker table
                                AuthNetSubscriptionChangesTracker authNetSubscriptionChangesTracker = new AuthNetSubscriptionChangesTracker();
                                authNetSubscriptionChangesTracker.setIsSubscriptionActive(true);
                                authNetSubscriptionChangesTracker.setOrderId(orderManagement.getOrderId());
                                authNetSubscriptionChangesTracker.setSubscriptionId(apiResponse.getSubscriptionId());
                                authNetSubscriptionChangesTrackerRepository.save(authNetSubscriptionChangesTracker);
                                // Logging payment profile with user card data
                                if (aNetTransactionResponse.getCustomerProfile() != null && aNetTransactionResponse.getCustomerProfile().getPaymentProfileList().get(0) != null) {
                                    AuthNetPaymentProfile paymentProfile = authNetPaymentProfileRepository.findByArbCustomerProfileIdAndArbPaymentProfileId(aNetTransactionResponse.getCustomerProfile().getCustomerProfileId()
                                            , aNetTransactionResponse.getCustomerProfile().getPaymentProfileList().get(0).getPaymentProfileId());
                                    if (paymentProfile == null) {
                                        AuthNetPaymentProfile authNetPaymentProfile = new AuthNetPaymentProfile();
                                        authNetPaymentProfile.setArbCustomerProfileId(aNetTransactionResponse.getCustomerProfile().getCustomerProfileId());
                                        authNetPaymentProfile.setArbPaymentProfileId(aNetTransactionResponse.getCustomerProfile().getPaymentProfileList().get(0).getPaymentProfileId());
                                        authNetPaymentProfile.setUserCardCountryCode(aNetTransactionResponse.getCountryCode());
                                        authNetPaymentProfileRepository.save(authNetPaymentProfile);
                                    }
                                }
                                // If ARB is created cancelling the timer
                                timer.cancel();
                                timer.purge();
                            }
                        }
                    }
                },
                25000,
                30000
        );
    }

    public ANetOneTimeProgramSubscriptionUsingCardRequestView constructANetOneTimeProgramSubscriptionUsingCardRequestView
            (ANetRecurringSubscriptionRequestViewWithCardData requestViewWithCardData) {
        ANetOneTimeProgramSubscriptionUsingCardRequestView oneTimeProgramSubscriptionRequestView = new ANetOneTimeProgramSubscriptionUsingCardRequestView();
        oneTimeProgramSubscriptionRequestView.setDevicePlatformTypeId(requestViewWithCardData.getDevicePlatformTypeId());
        oneTimeProgramSubscriptionRequestView.setFormToken(requestViewWithCardData.getFormToken());
        oneTimeProgramSubscriptionRequestView.setProgramId(requestViewWithCardData.getProgramId());
        oneTimeProgramSubscriptionRequestView.setDoSaveCardData(true); // ARB flow wont work if this is made as false.
        oneTimeProgramSubscriptionRequestView.setFirstName(requestViewWithCardData.getFirstName());
        oneTimeProgramSubscriptionRequestView.setLastName(requestViewWithCardData.getLastName());
        oneTimeProgramSubscriptionRequestView.setAddress(requestViewWithCardData.getAddress());
        oneTimeProgramSubscriptionRequestView.setCity(requestViewWithCardData.getCity());
        oneTimeProgramSubscriptionRequestView.setState(requestViewWithCardData.getState());
        oneTimeProgramSubscriptionRequestView.setZip(requestViewWithCardData.getZip());
        oneTimeProgramSubscriptionRequestView.setCountry(requestViewWithCardData.getCountry());
        return oneTimeProgramSubscriptionRequestView;
    }


    @Transactional
    public ResponseModel subscribeProgramInTrialMode(Long programId, Long platformId) {
        log.info("subscribeProgramInTrialMode starts.");
        long apiStartTimeMillis = System.currentTimeMillis();

        User user = userComponents.getUser();

        long profilingStartTimeMillis = System.currentTimeMillis();
        Programs program = validationService.validateProgramIdBlocked(programId);
        if (fitwiseUtils.isCurrentMemberBlocked()) {
            throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_MEMBER_BLOCKED_CANT_SUBSCRIBE, MessageConstants.ERROR);
        }

        if (!KeyConstants.KEY_PUBLISH.equalsIgnoreCase(program.getStatus())) {
            throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_PROGRAM_NOT_PUBLISHED_CANT_SUBSCRIBE, MessageConstants.ERROR);
        }

        PlatformType platformType = validationService.validateAndGetPlatform(platformId);

        // Checking whether the program is already subscribed
        ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), programId);
        if (programSubscription != null) {
            SubscriptionStatus status = getMemberProgramSubscriptionStatus(programSubscription);
            if (status != null) {
                if (status.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_TRIAL)) {
                    throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_ALREADY_IN_TRAIL_MODE, null);
                }

                if ((status.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAYMENT_PENDING)) ||
                        (status.getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID))) {
                    throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_PROGRAM_ALREADY_SUBSCRIBED, null);
                }
            }
        }
        long profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Validation : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = System.currentTimeMillis();
        SubscriptionType subscriptionType = subscriptionTypesRepo.findByNameIgnoreCase(KeyConstants.KEY_PROGRAM);

        SubscriptionStatus subscriptionStatus =
                subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_TRIAL);

        boolean isProgramAlreadySubscribed = false;
        if (programSubscription != null) {
            programSubscription.setSubscribedDate(new Date());
            programSubscription.setSubscriptionStatus(subscriptionStatus);
            isProgramAlreadySubscribed = true;
        } else {
            programSubscription = new ProgramSubscription();
            programSubscription.setAutoRenewal(false);
            programSubscription.setProgram(program);
            programSubscription.setSubscribedDate(new Date());
            programSubscription.setSubscriptionStatus(subscriptionStatus);
            programSubscription.setUser(user);
        }

        SubscriptionPlan subscriptionPlan = subscriptionPlansRepo.findByDuration(program.getDuration().getDuration());
        programSubscription.setSubscriptionPlan(subscriptionPlan);
        programSubscription.setSubscribedViaPlatform(platformType);
        programSubscriptionRepo.save(programSubscription);


        SubscriptionAudit subscriptionAudit = new SubscriptionAudit();
        subscriptionAudit.setAutoRenewal(false);
        subscriptionAudit.setProgramSubscription(programSubscription);
        if (isProgramAlreadySubscribed) {
            subscriptionAudit.setRenewalStatus(KeyConstants.KEY_RENEWAL);
        } else {
            subscriptionAudit.setRenewalStatus(KeyConstants.KEY_NEW);
        }
        subscriptionAudit.setSubscriptionDate(new Date());
        subscriptionAudit.setSubscriptionStatus(subscriptionStatus);
        subscriptionAudit.setSubscribedViaPlatform(platformType);
        subscriptionAudit.setSubscriptionPlan(subscriptionPlan);
        subscriptionAudit.setUser(user);
        subscriptionAudit.setSubscriptionType(subscriptionType);
        subscriptionAuditRepo.save(subscriptionAudit);

        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("DB row addition : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        long apiEndTimeMillis = System.currentTimeMillis();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("subscribeProgramInTrialMode ends.");

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PROGRAMS_ADDED_TO_TRIAL, null);
    }


    /**
     * Method to subscribe program for one time via Authorize.net Payment gateway
     *
     * @param authorizeNetOneTimeProgramSubscriptionView - Contains the user input
     * @return response model
     */
    @Transactional
    public ResponseModel initiateOneTimeProgramSubscriptionUsingCard
    (ANetOneTimeProgramSubscriptionUsingCardRequestView authorizeNetOneTimeProgramSubscriptionView) {

        User user = userComponents.getUser();
        if (fitwiseUtils.isCurrentMemberBlocked()) {
            throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_MEMBER_BLOCKED_CANT_SUBSCRIBE, MessageConstants.ERROR);
        }

        Programs program = validationService.validateProgramIdBlocked(authorizeNetOneTimeProgramSubscriptionView.getProgramId());
        if (!KeyConstants.KEY_PUBLISH.equalsIgnoreCase(program.getStatus())) {
            throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_PROGRAM_NOT_PUBLISHED_CANT_SUBSCRIBE, MessageConstants.ERROR);
        }

        if (!validationService.isStringContainsOnlyAlphabets(authorizeNetOneTimeProgramSubscriptionView.getFirstName())) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FIRST_NAME_ERROR, MessageConstants.ERROR);
        }

        if (!validationService.isStringContainsOnlyAlphabets(authorizeNetOneTimeProgramSubscriptionView.getLastName())) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_LAST_NAME_ERROR, MessageConstants.ERROR);
        }

        ANetTransactionResponse aNetTransactionResponse = null;

        PlatformType newClientDevicePlatform = platformsRepo.findByPlatformTypeId(authorizeNetOneTimeProgramSubscriptionView.getDevicePlatformTypeId());

        //TODO Check if the program is already subscribed before making subscription
        ProgramSubscription subscribedProgram = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), program.getProgramId());
        if (subscribedProgram != null) {

            // Program has been already subscribed

            SubscriptionPlan subscribedPlan = subscriptionPlansRepo.findByDuration(program.getDuration().getDuration());
            Long duration = null;
            if (subscribedPlan != null) {
                duration = subscribedPlan.getDuration();
            }
            if (duration == null || duration == 0) {
                throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_IN_DURATION, null);
            }

            Date subscribedDate = subscribedProgram.getSubscribedDate();

            Calendar cal = Calendar.getInstance();
            cal.setTime(subscribedDate);
            cal.add(Calendar.DATE, Math.toIntExact(duration));

            Date subscriptionEndDate = cal.getTime();
            Date currentDate = new Date();
            if (subscribedProgram.getSubscriptionStatus() != null) {
                if (((subscribedProgram.getSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID))
                        || (subscribedProgram.getSubscriptionStatus().getSubscriptionStatusName()
                        .equalsIgnoreCase(KeyConstants.KEY_PAYMENT_PENDING))) && subscriptionEndDate.after(currentDate)) {
                    //TODO Program already subscribed
                    throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_PROGRAM_ALREADY_SUBSCRIBED, MessageConstants.ERROR);

                } else {

                    // TODO Program subscription
                    // TODO Program has been subscribed but subscription period was over
                    // TODO Allow re-subscription

                    // TODO Will be entered as new entry under subscription Audit

                    /*
                     * As per the DB, 1 refers to Android and 3 refers to Web with regarding to DevicePlatformTypeId.
                     * For Android and Web, Authorize.net is the payment gateway!
                     */
                    if (authorizeNetOneTimeProgramSubscriptionView.getDevicePlatformTypeId() == 1 || authorizeNetOneTimeProgramSubscriptionView.getDevicePlatformTypeId() == 3) {

                        /*
                         * Creating Order for the Transaction
                         */
                        OrderManagement orderManagement = createOrder(user, program, false, KeyConstants.KEY_AUTH_NET, newClientDevicePlatform, authorizeNetOneTimeProgramSubscriptionView.getOrderId());

                        // Making Authorize.net Payment request
                        aNetTransactionResponse =
                                paymentService.initiateOneTimePaymentTransactionByCard(authorizeNetOneTimeProgramSubscriptionView, orderManagement.getOrderId());
                        aNetTransactionResponse.setCountryCode(authorizeNetOneTimeProgramSubscriptionView.getCountry());

                        boolean isPaymentSuccess;
                        isPaymentSuccess = aNetTransactionResponse.getResponseCode() != null &&
                                aNetTransactionResponse.getResponseCode().equalsIgnoreCase(KeyConstants.ANET_TRANSACTION_SUCCESS_RESPONSE_CODE);

                        addTransactionDataToAnetPaymentTable(aNetTransactionResponse, orderManagement);
                        overrideAlreadySubscribedProgramData(user, subscribedProgram, newClientDevicePlatform, isPaymentSuccess, false, orderManagement);
                    }

                }
            }


        } else {
            //TODO User newly subscribing the program

            /*
             * Creating Order for the Transaction
             */
            OrderManagement orderManagement = createOrder(user, program, false, KeyConstants.KEY_AUTH_NET, newClientDevicePlatform, authorizeNetOneTimeProgramSubscriptionView.getOrderId());

            // Initiating one time payment
            aNetTransactionResponse = paymentService.initiateOneTimePaymentTransactionByCard(authorizeNetOneTimeProgramSubscriptionView, orderManagement.getOrderId());
            boolean isTransactionSuccess = false;

            if (aNetTransactionResponse != null && aNetTransactionResponse.getResponseCode() != null &&
                    aNetTransactionResponse.getResponseCode().equalsIgnoreCase(KeyConstants.ANET_TRANSACTION_SUCCESS_RESPONSE_CODE)) {
                isTransactionSuccess = true;
            }

            // Add the subscription auditing data via the below method
            addNewlySubscribedProgramData(user, program, newClientDevicePlatform, isTransactionSuccess, false, orderManagement);
            assert aNetTransactionResponse != null;
            aNetTransactionResponse.setCountryCode(authorizeNetOneTimeProgramSubscriptionView.getCountry());
            addTransactionDataToAnetPaymentTable(aNetTransactionResponse, orderManagement);
        }

        /*
         * Constructing the Payload for the response
         */
        ANetOneTimeTransactionResponseView responseView = new ANetOneTimeTransactionResponseView();
        if (aNetTransactionResponse != null) {
            if (aNetTransactionResponse.getResponseCode() != null)
                responseView.setResponseCode(aNetTransactionResponse.getResponseCode());
            if (aNetTransactionResponse.getTransactionStatus() != null)
                responseView.setTransactionStatus(aNetTransactionResponse.getTransactionStatus());
            if (aNetTransactionResponse.getTransactionId() != null)
                responseView.setTransactionId(aNetTransactionResponse.getTransactionId());
            if (aNetTransactionResponse.getCustomerProfile() != null &&
                    aNetTransactionResponse.getCustomerProfile().getCustomerProfileId() != null)
                responseView.setANetCustomerProfileId(aNetTransactionResponse.getCustomerProfile().getCustomerProfileId());
        }

        if (aNetTransactionResponse.getResponseCode() != null && aNetTransactionResponse.getResponseCode().equalsIgnoreCase(KeyConstants.ANET_TRANSACTION_SUCCESS_RESPONSE_CODE)) {
            return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SUBSCRIPTION_ADDED, responseView);
        }

        return new ResponseModel(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_ADDING_SUBSCRIPTION, responseView);
    }


    /**
     * Method used to initiate One time payment transaction using already saved card / Payment Profile
     *
     * @param aNetCustomerProfileRequestView
     * @return
     */
    public ResponseModel initiateOneTimeProgramSubscriptionUsingPaymentProfile(ANetCustomerProfileRequestView
                                                                                       aNetCustomerProfileRequestView) {
        User user = userComponents.getUser();
        if (fitwiseUtils.isCurrentMemberBlocked()) {
            throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_MEMBER_BLOCKED_CANT_SUBSCRIBE, MessageConstants.ERROR);
        }
        Programs program = validationService.validateProgramIdBlocked(aNetCustomerProfileRequestView.getProgramId());
        if (!KeyConstants.KEY_PUBLISH.equalsIgnoreCase(program.getStatus())) {
            throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_PROGRAM_NOT_PUBLISHED_CANT_SUBSCRIBE, MessageConstants.ERROR);
        }
        ANetTransactionResponse aNetTransactionResponse = null;

        PlatformType newClientDevicePlatform = platformsRepo.findByPlatformTypeId(aNetCustomerProfileRequestView.getDevicePlatformId());

        //TODO Check if the program is already subscribed before making subscription
        ProgramSubscription subscribedProgram = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), program.getProgramId());
        if (subscribedProgram != null) {

            // Program has been already subscribed

            SubscriptionPlan subscribedPlan = subscriptionPlansRepo.findByDuration(program.getDuration().getDuration());
            Long duration = null;
            if (subscribedPlan != null) {
                duration = subscribedPlan.getDuration();
            }
            if (duration == null || duration == 0) {
                throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_IN_DURATION, null);
            }

            // Getting the subscriptionDate
            Date subscribedDate = subscribedProgram.getSubscribedDate();

            // Adding program duration to the subscribed date to get the subscription End date
            Calendar cal = Calendar.getInstance();
            cal.setTime(subscribedDate);
            cal.add(Calendar.DATE, Math.toIntExact(duration));

            Date subscriptionEndDate = cal.getTime();
            Date currentDate = new Date();
            if (subscribedProgram.getSubscriptionStatus() != null) {
                if (((subscribedProgram.getSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_PAID))
                        || (subscribedProgram.getSubscriptionStatus().getSubscriptionStatusName()
                        .equalsIgnoreCase(KeyConstants.KEY_PAYMENT_PENDING))) && subscriptionEndDate.after(currentDate)) {
                    //TODO Program already subscribed
                    throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_PROGRAM_ALREADY_SUBSCRIBED, MessageConstants.ERROR);

                } else {

                    // TODO Program subscription
                    // TODO Program has been subscribed but subscription period was over
                    // TODO Allow re-subscription

                    // TODO Will be entered as new entry under subscription Audit

                    /*
                     * As per the DB, 1 refers to Android and 3 refers to Web with regarding to DevicePlatformTypeId.
                     * For Android and Web, Authorize.net is the payment gateway!
                     */
                    if (aNetCustomerProfileRequestView.getDevicePlatformId() == 1 || aNetCustomerProfileRequestView.getDevicePlatformId() == 3) {

                        /*
                         * Creating Order for the Transaction
                         */
                        OrderManagement orderManagement = createOrder(user, program, false, KeyConstants.KEY_AUTH_NET, newClientDevicePlatform, aNetCustomerProfileRequestView.getOrderId());

                        // Making Authorize.net Payment request
                        aNetTransactionResponse =
                                paymentService.initiateOneTimeProgramSubscriptionByPaymentProfile(aNetCustomerProfileRequestView, program.getProgramPrice(), orderManagement.getOrderId());

                        boolean isPaymentSuccess;
                        isPaymentSuccess = aNetTransactionResponse.getResponseCode() != null &&
                                aNetTransactionResponse.getResponseCode().equalsIgnoreCase(KeyConstants.ANET_TRANSACTION_SUCCESS_RESPONSE_CODE);

                        addTransactionDataToAnetPaymentTable(aNetTransactionResponse, orderManagement);
                        overrideAlreadySubscribedProgramData(user, subscribedProgram, newClientDevicePlatform, isPaymentSuccess, false, orderManagement);
                    }

                }

            }


        } else {
            //User newly subscribing the program

            /*
             * Creating Order for the Transaction
             */
            OrderManagement orderManagement = createOrder(user, program, false, KeyConstants.KEY_AUTH_NET, newClientDevicePlatform, aNetCustomerProfileRequestView.getOrderId());

            // Initiating one time payment
            aNetTransactionResponse = paymentService.initiateOneTimeProgramSubscriptionByPaymentProfile(aNetCustomerProfileRequestView, program.getProgramPrice(), orderManagement.getOrderId());
            boolean isTransactionSuccess = false;

            if (aNetTransactionResponse != null && aNetTransactionResponse.getResponseCode() != null &&
                    aNetTransactionResponse.getResponseCode().equalsIgnoreCase(KeyConstants.ANET_TRANSACTION_SUCCESS_RESPONSE_CODE)) {
                isTransactionSuccess = true;
            }

            // Add the subscription auditing data via the below method
            addNewlySubscribedProgramData(user, program, newClientDevicePlatform, isTransactionSuccess, false, orderManagement);
            assert aNetTransactionResponse != null;
            addTransactionDataToAnetPaymentTable(aNetTransactionResponse, orderManagement);
        }

        /*
         * Constructing the Payload for the response
         */
        ANetOneTimeTransactionResponseView responseView = new ANetOneTimeTransactionResponseView();
        if (aNetTransactionResponse != null) {
            if (aNetTransactionResponse.getResponseCode() != null)
                responseView.setResponseCode(aNetTransactionResponse.getResponseCode());
            if (aNetTransactionResponse.getTransactionStatus() != null)
                responseView.setTransactionStatus(aNetTransactionResponse.getTransactionStatus());
            if (aNetTransactionResponse.getTransactionId() != null)
                responseView.setTransactionId(aNetTransactionResponse.getTransactionId());
            if (aNetTransactionResponse.getCustomerProfile() != null &&
                    aNetTransactionResponse.getCustomerProfile().getCustomerProfileId() != null)
                responseView.setANetCustomerProfileId(aNetTransactionResponse.getCustomerProfile().getCustomerProfileId());
        }

        if (aNetTransactionResponse.getResponseCode() != null && aNetTransactionResponse.getResponseCode().equalsIgnoreCase(KeyConstants.ANET_TRANSACTION_SUCCESS_RESPONSE_CODE)) {
            return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SUBSCRIPTION_ADDED, responseView);
        }

        return new ResponseModel(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_ADDING_SUBSCRIPTION, responseView);
    }


    /**
     * Method used to cancel a recurring subscription in Authorize.net
     *
     * @param programId
     * @param platformId - SubscribedViaPlatform id
     * @return
     */
    public ResponseModel cancelRecurringProgramSubscription(Long programId, Long platformId) {
        User user = userComponents.getUser();
        return cancelRecurringProgramSubscription(programId, platformId, user, true);
    }

    @Transactional
    public ResponseModel cancelRecurringProgramSubscription(Long programId, Long platformId, User user, boolean sendMailNotification) {

        // If the subscription was done in Apple pay and user is trying to cancel that in android / web platforms
        if (platformId == 2) {
            throw new ApplicationException(Constants.CAN_EDIT, MessageConstants.MSG_APPLE_PAY_CANCELLATION_IN_AUTH_NET, null);
        }

        Programs program = validationService.validateProgramIdBlocked(programId);

        OrderManagement orderManagement = orderManagementRepository.findTop1ByUserAndProgramOrderByCreatedDateDesc(user, program);
        if (KeyConstants.KEY_STRIPE.equals(orderManagement.getModeOfPayment())) {
            stripeService.cancelStripeProgramSubscription(programId, platformId, user, false);
        } else {

            AuthNetArbSubscription arbSubscription = authNetArbSubscriptionRepository.findTop1ByUserUserIdAndProgramProgramIdOrderByModifiedDateDesc(user.getUserId(), program.getProgramId());
            if (arbSubscription == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PROGRAM_NOT_SUBSCRIBED, null);
            }
            // Throwing exception if the auto subscription is already inactive
            if (arbSubscription.getAuthNetSubscriptionStatus() != null &&
                    !arbSubscription.getAuthNetSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_ACTIVE)) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_AUTO_SUBSCRIPTION_ALREADY_INACTIVE, null);
            }

            CancelRecurringSubscriptionRequestView cancelRecurringSubscriptionRequestView = new CancelRecurringSubscriptionRequestView();
            cancelRecurringSubscriptionRequestView.setSubscriptionId(arbSubscription.getANetSubscriptionId());

            ANetApiResponse apiResponse = paymentService.cancelRecurringProgramSubscription(cancelRecurringSubscriptionRequestView);
            if (apiResponse.getMessages().getResultCode() != MessageTypeEnum.OK) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_CANCELLING_SUBSCRIPTION_FAILED, null);
            } else {
                //Logging the subscription cancelled event in table
                AuthNetSubscriptionStatus authNetSubscriptionStatus = authNetSubscriptionStatusRepository.findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_CANCELLED);
                AuthNetArbSubscription authNetArbSubscription = new AuthNetArbSubscription();
                authNetArbSubscription.setProgram(program);
                authNetArbSubscription.setUser(user);
                authNetArbSubscription.setAuthNetSubscriptionStatus(authNetSubscriptionStatus);
                authNetArbSubscription.setANetSubscriptionId(arbSubscription.getANetSubscriptionId());
                authNetArbSubscription.setSubscribedViaPlatform(arbSubscription.getSubscribedViaPlatform());
                authNetArbSubscriptionRepository.save(authNetArbSubscription);

                // Logging in subscription tracker table
                AuthNetSubscriptionChangesTracker tracker = authNetSubscriptionChangesTrackerRepository.findTop1BySubscriptionIdOrderByModifiedDateDesc(arbSubscription.getANetSubscriptionId());
                if (tracker != null) {
                    AuthNetSubscriptionChangesTracker authNetSubscriptionChangesTracker = new AuthNetSubscriptionChangesTracker();
                    authNetSubscriptionChangesTracker.setIsSubscriptionActive(false);
                    authNetSubscriptionChangesTracker.setOrderId(tracker.getOrderId());
                    authNetSubscriptionChangesTracker.setSubscriptionId(tracker.getSubscriptionId());
                    authNetSubscriptionChangesTrackerRepository.save(authNetSubscriptionChangesTracker);
                }

                // Setting the auto-renewal flag as false
                ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), programId);
                if (programSubscription != null) {
                    programSubscription.setAutoRenewal(false);
                    programSubscriptionRepo.save(programSubscription);
                }
            }
        }

        if (sendMailNotification) {
            String subject = EmailConstants.AUTORENEWAL_SUBJECT;
            String mailBody = EmailConstants.AUTORENEWAL_PROGRAM_CONTENT.replace(EmailConstants.LITERAL_PROGRAM_NAME, "<b>" + program.getTitle() + "</b>");
            String userName = fitwiseUtils.getUserFullName(user);
            mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, mailBody);
            mailBody = emailContentUtil.replaceMemberAppUrl(mailBody);
            asyncMailer.sendHtmlMail(user.getEmail(), subject, mailBody);
        }

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SUBSCRIPTION_CANCELLED_SUCCESSFULLY, null);
    }


    /**
     * Method used to cancel a recurring subscription if a subscription status is Suspended/Terminated
     *
     * @param programId
     * @param platformId
     * @return
     */
    @Transactional
    public ResponseModel cancelRecurringProgramSubscriptionForARBSuspendedTerminatedStatus(User user, Long
            programId, Long platformId) {
        Programs program = validationService.validateProgramIdBlocked(programId);

        AuthNetArbSubscription arbSubscription = authNetArbSubscriptionRepository.findTop1ByUserUserIdAndProgramProgramIdOrderByModifiedDateDesc(user.getUserId(), program.getProgramId());
        if (arbSubscription == null) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PROGRAM_NOT_SUBSCRIBED, null);
        }
        // Throwing exception if the auto subscription is already inactive
        if (!arbSubscription.getAuthNetSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_ACTIVE)) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_AUTO_SUBSCRIPTION_ALREADY_INACTIVE, null);
        }

        // If the subscription was done in Apple pay and user is trying to cancel that in android / web platforms
        if (arbSubscription.getSubscribedViaPlatform().getPlatformTypeId() == 2) {
            throw new ApplicationException(Constants.CAN_EDIT, MessageConstants.MSG_APPLE_PAY_CANCELLATION_IN_AUTH_NET, null);
        }

        CancelRecurringSubscriptionRequestView cancelRecurringSubscriptionRequestView = new CancelRecurringSubscriptionRequestView();
        cancelRecurringSubscriptionRequestView.setSubscriptionId(arbSubscription.getANetSubscriptionId());

        ANetApiResponse apiResponse = paymentService.cancelRecurringProgramSubscription(cancelRecurringSubscriptionRequestView);
        if (apiResponse.getMessages().getResultCode() != MessageTypeEnum.OK) {
            return new ResponseModel(Constants.ERROR_STATUS, MessageConstants.MSG_CANCELLING_SUBSCRIPTION_FAILED, null);
        } else {
            //Logging the subscription cancelled event in table
            AuthNetSubscriptionStatus authNetSubscriptionStatus = authNetSubscriptionStatusRepository.findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_CANCELLED);
            AuthNetArbSubscription authNetArbSubscription = new AuthNetArbSubscription();
            authNetArbSubscription.setProgram(program);
            authNetArbSubscription.setUser(user);
            authNetArbSubscription.setAuthNetSubscriptionStatus(authNetSubscriptionStatus);
            authNetArbSubscription.setANetSubscriptionId(arbSubscription.getANetSubscriptionId());
            authNetArbSubscription.setSubscribedViaPlatform(arbSubscription.getSubscribedViaPlatform());
            authNetArbSubscriptionRepository.save(authNetArbSubscription);

            // Logging in subscription tracker table
            AuthNetSubscriptionChangesTracker tracker = authNetSubscriptionChangesTrackerRepository.findTop1BySubscriptionIdOrderByModifiedDateDesc(arbSubscription.getANetSubscriptionId());
            if (tracker != null) {
                AuthNetSubscriptionChangesTracker authNetSubscriptionChangesTracker = new AuthNetSubscriptionChangesTracker();
                authNetSubscriptionChangesTracker.setIsSubscriptionActive(false);
                authNetSubscriptionChangesTracker.setOrderId(tracker.getOrderId());
                authNetSubscriptionChangesTracker.setSubscriptionId(tracker.getSubscriptionId());
                authNetSubscriptionChangesTrackerRepository.save(authNetSubscriptionChangesTracker);
            }

        }
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SUBSCRIPTION_CANCELLED_SUCCESSFULLY, null);
    }


    /**
     * Method used to capture the transaction data from
     *
     * @param apiResponse
     * @param subscriptionId
     */
    @Transactional
    public void saveTransactionFromARB(GetTransactionDetailsResponse apiResponse, String subscriptionId) {

        AuthNetArbSubscription arbSubscription = authNetArbSubscriptionRepository.findTop1ByANetSubscriptionId(subscriptionId);
        if (arbSubscription != null) {
            Programs program = arbSubscription.getProgram();
            User user = arbSubscription.getUser();

            ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), program.getProgramId());

            /*
             * Creating Order for the Transaction
             */
            OrderManagement newOrderManagement = createOrder(user, program, true, KeyConstants.KEY_AUTH_NET, arbSubscription.getSubscribedViaPlatform(), "");

            // Adding the subscription auditing data to table via the below method
            if (apiResponse.getMessages().getResultCode() == MessageTypeEnum.OK) {
                if (programSubscription != null) {
                    overrideAlreadySubscribedProgramData(user, programSubscription, arbSubscription.getSubscribedViaPlatform(), true, true, newOrderManagement);
                } else {
                    // This case will occur when a program is already subscribed,
                    addNewlySubscribedProgramData(user, program, arbSubscription.getSubscribedViaPlatform(), true, true, newOrderManagement);
                }
            }

            ANetTransactionResponse transactionResponse = new ANetTransactionResponse();
            if (apiResponse.getTransaction() != null && apiResponse.getTransaction().getResponseCode() != 0)
                transactionResponse.setResponseCode(String.valueOf(apiResponse.getTransaction().getResponseCode()));
            if (apiResponse.getTransaction() != null && apiResponse.getTransaction().getTransId() != null)
                transactionResponse.setTransactionId(apiResponse.getTransaction().getTransId());
            if (apiResponse.getTransaction() != null && apiResponse.getTransaction().getTransactionStatus() != null)
                transactionResponse.setTransactionStatus(KeyConstants.KEY_PAYMENT_PENDING);
            if (apiResponse.getMessages().getResultCode() != MessageTypeEnum.OK) {
                transactionResponse.setErrorCode(apiResponse.getMessages().getMessage().get(0).getCode());
                transactionResponse.setErrorMessage(apiResponse.getMessages().getMessage().get(0).getText());
                transactionResponse.setTransactionStatus(KeyConstants.KEY_PAYMENT_FAILED);
            }

            CustomerProfile customerProfile = new CustomerProfile();
            if (apiResponse.getTransaction() != null && apiResponse.getTransaction().getProfile() != null)
                customerProfile.setCustomerProfileId(apiResponse.getTransaction().getProfile().getCustomerProfileId());
            List<PaymentProfile> paymentProfiles = new ArrayList<>();
            PaymentProfile paymentProfile = new PaymentProfile();
            if (apiResponse.getTransaction() != null && apiResponse.getTransaction().getProfile() != null)
                paymentProfile.setPaymentProfileId(apiResponse.getTransaction().getProfile().getCustomerPaymentProfileId());
            paymentProfiles.add(paymentProfile);
            customerProfile.setPaymentProfileList(paymentProfiles);
            transactionResponse.setCustomerProfile(customerProfile);

            if (apiResponse.getTransaction() != null && apiResponse.getTransaction().getProfile() != null) {
                AuthNetPaymentProfile authNetPaymentProfile = authNetPaymentProfileRepository.findByArbCustomerProfileIdAndArbPaymentProfileId(apiResponse.getTransaction().getProfile().getCustomerProfileId(),
                        apiResponse.getTransaction().getProfile().getCustomerPaymentProfileId());
                transactionResponse.setCountryCode(authNetPaymentProfile.getUserCardCountryCode());
            }

            // Adding the transaction data to the Payment table
            addTransactionDataToAnetPaymentTable(transactionResponse, newOrderManagement);

            AuthNetPayment authNetPayment = authNetPaymentRepository.findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(newOrderManagement.getOrderId());
            authNetPayment.setArbSubscriptionId(String.valueOf(apiResponse.getTransaction().getSubscription().getId()));
            authNetPayment.setIsARB(true);
            authNetPaymentRepository.save(authNetPayment);

            // Logging in subscription tracker table
            AuthNetSubscriptionChangesTracker tracker = authNetSubscriptionChangesTrackerRepository.findTop1BySubscriptionIdOrderByModifiedDateDesc(subscriptionId);
            if (tracker != null) {
                AuthNetSubscriptionChangesTracker authNetSubscriptionChangesTracker = new AuthNetSubscriptionChangesTracker();
                authNetSubscriptionChangesTracker.setIsSubscriptionActive(true);
                authNetSubscriptionChangesTracker.setOrderId(newOrderManagement.getOrderId());
                authNetSubscriptionChangesTracker.setSubscriptionId(tracker.getSubscriptionId());
                authNetSubscriptionChangesTrackerRepository.save(authNetSubscriptionChangesTracker);
            }
        }
    }


    /*
     * Member subscribed a program for one time and at the same time he wants to auto-subscribe the program for future
     */
    public ResponseModel initiateRecurringSubscriptionForFutureWithCard
    (ANetRecurringSubscriptionRequestViewWithCardData oneTimeSubscriptionRequestView) {
        Programs program = validationService.validateProgramIdBlocked(oneTimeSubscriptionRequestView.getProgramId());
        User user = userComponents.getUser();

        List<AuthNetArbSubscription> authNetArbSubscriptions = authNetArbSubscriptionRepository.
                findByUserUserIdAndProgramProgramIdOrderByModifiedDateDesc(user.getUserId(), program.getProgramId());

        AuthNetArbSubscription authNetArbSubscription = authNetArbSubscriptions.get(0);
        if (authNetArbSubscription.getAuthNetSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_ACTIVE)) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PROGRAM_ALREADY_SUBSCRIBED, null);
        }

        return new ResponseModel();

    }

    /*
     * Member subscribed a program for one time and at the same time he wants to auto-subscribe the same program for future
     * using payment profile
     */
    public ResponseModel initiateRecurringSubscriptionForFutureWithPaymentProfile
    (ANetRecurringSubscriptionRequestViewWithPaymentProfile recurringSubscriptionRequestView) {
        User user = userComponents.getUser();
        Programs program = validationService.validateProgramIdBlocked(recurringSubscriptionRequestView.getProgramId());
        PlatformType platformType = validationService.validateAndGetPlatform(recurringSubscriptionRequestView.getDevicePlatformId());
        // Creating Order for the Transaction
        // TODO HARD_CODED Existing order id value
        OrderManagement orderManagement = createOrder(user, program, true, KeyConstants.KEY_AUTH_NET, platformType, "");
        // Initiating ARB using Payment Profile
        ARBCreateSubscriptionResponse apiResponse = paymentService.initiateARBForFutureUsingPaymentProfile(recurringSubscriptionRequestView, orderManagement);
        // Updating AuthNetPayment table with the subscription Id
        if (apiResponse.getSubscriptionId() != null) {
            // Logging the subscription id in the Subscription table
            AuthNetArbSubscription authNetARBSubscription = new AuthNetArbSubscription();
            authNetARBSubscription.setUser(userComponents.getUser());
            authNetARBSubscription.setProgram(program);
            authNetARBSubscription.setANetSubscriptionId(apiResponse.getSubscriptionId());
            authNetARBSubscription.setSubscribedViaPlatform(platformType);
            // Setting the subscription status as Active
            AuthNetSubscriptionStatus authNetSubscriptionStatus = authNetSubscriptionStatusRepository.findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_ACTIVE);
            authNetARBSubscription.setAuthNetSubscriptionStatus(authNetSubscriptionStatus);
            authNetArbSubscriptionRepository.save(authNetARBSubscription);
            // Logging in subscription tracker table
            AuthNetSubscriptionChangesTracker authNetSubscriptionChangesTracker = new AuthNetSubscriptionChangesTracker();
            authNetSubscriptionChangesTracker.setIsSubscriptionActive(true);
            authNetSubscriptionChangesTracker.setOrderId(orderManagement.getOrderId());
            authNetSubscriptionChangesTracker.setSubscriptionId(apiResponse.getSubscriptionId());
            authNetSubscriptionChangesTrackerRepository.save(authNetSubscriptionChangesTracker);
        } else {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_ADDING_SUBSCRIPTION, null);
        }
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SUBSCRIPTION_ADDED, null);
    }

    /**
     * Returns the Manage Subscriptions List
     *
     * @return
     */
    @Transactional
    public ResponseModel getSubscribedProgramsList(Optional<Boolean> isAllSubscriptionType) {
        log.info("Get subscribed programs starts");
        long start = new Date().getTime();
        long profilingStart;
        User user = userComponents.getUser();
        List<ManageSubscriptionForProgramWithStripe> manageSubscriptionForProgramWithStripeList = getActiveMemberProgramSubscriptions(user.getUserId());
        log.info("Basic validation and query : Time taken in millis : " + (new Date().getTime() - start));
        List<SubscribedProgramTileResponseView> subscribedProgramsResponseViews = new ArrayList<>();
        SubscribedProgramsResponseView responseView = new SubscribedProgramsResponseView();

        // Parsing through all the subscriptions the user has done till now
        profilingStart = new Date().getTime();
        for (ManageSubscriptionForProgramWithStripe manageSubscriptionForProgramWithStripe : manageSubscriptionForProgramWithStripeList) {
            long temp = new Date().getTime();
            log.info("Get member program subscription status : Time taken in millis : " + (new Date().getTime() - temp));

            Programs program = manageSubscriptionForProgramWithStripe.getProgram();
            SubscribedProgramTileResponseView tileResponseView = new SubscribedProgramTileResponseView();
            tileResponseView.setProgramDuration(program.getDuration().getDuration());
            tileResponseView.setProgramLevel(program.getProgramExpertiseLevel().getExpertiseLevel());
            tileResponseView.setProgramThumbnail(program.getImage().getImagePath());
            tileResponseView.setProgramName(program.getTitle());
            tileResponseView.setProgramId(program.getProgramId());
            tileResponseView.setPlatformType(manageSubscriptionForProgramWithStripe.getSubscribedViaPlatform());

            // Formatting and setting the subscribed date
            String formattedDate = fitwiseUtils.formatDate(manageSubscriptionForProgramWithStripe.getSubscribedDate());
            tileResponseView.setSubscribedDate(formattedDate);
            tileResponseView.setSubscribedDateTimeStamp(manageSubscriptionForProgramWithStripe.getSubscribedDate());
            temp = new Date().getTime();
            OrderManagement orderManagement = manageSubscriptionForProgramWithStripe.getOrderManagement();
            if (orderManagement.getSubscriptionType() != null) {
                tileResponseView.setSubscriptionType(orderManagement.getSubscriptionType().getName());
            }
            log.info("Order management query : Time taken in millis : " + (new Date().getTime() - temp));

            /**
             * Checking whether the program's auto-subscription is enabled or disabled
             */
            temp = new Date().getTime();
            if (manageSubscriptionForProgramWithStripe.getSubscribedViaPlatform().getPlatformTypeId() == 2) {
                // Program subscribed via apple iap
                //TODO PRATHEEPA :Check Required
                if (manageSubscriptionForProgramWithStripe.getAppleSubscriptionStatus() != null &&
                        manageSubscriptionForProgramWithStripe.getAppleSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_ACTIVE)) {
                    tileResponseView.setSubscriptionOn(true);
                }
            } else {
                if (KeyConstants.KEY_STRIPE.equals(orderManagement.getModeOfPayment()) && manageSubscriptionForProgramWithStripe.getStripeSubscriptionStatus() != null &&
                        manageSubscriptionForProgramWithStripe.getStripeSubscriptionStatus().getSubscriptionStatus().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_ACTIVE)) {
                    tileResponseView.setSubscriptionOn(true);
                }
            }
            log.info("Auto subscription status check : Time taken in millis : " + (new Date().getTime() - temp));
            subscribedProgramsResponseViews.add(tileResponseView);
        }
        log.info("Response construction for all subscribed programs : Time taken in millis : " + (new Date().getTime() - profilingStart));
        if (isAllSubscriptionType.isPresent() && isAllSubscriptionType.get()) {
            profilingStart = new Date().getTime();
            List<ManageSubscriptionWithStripeDAO> manageSubscriptionWithStripeDAOList = getActiveMemberPackageSubscriptions(user.getUserId());
            log.info("Package subscription query : Time taken in millis : " + (new Date().getTime() - profilingStart));
            profilingStart = new Date().getTime();
            for (ManageSubscriptionWithStripeDAO manageSubscriptionWithStripeDAO : manageSubscriptionWithStripeDAOList) {
                long temp = new Date().getTime();
                log.info("Get package subscription status : Time taken in millis : " + (new Date().getTime() - temp));
                SubscriptionPackage subscriptionPackage = manageSubscriptionWithStripeDAO.getSubscriptionPackage();
                SubscribedProgramTileResponseView tileResponseView = new SubscribedProgramTileResponseView();
                tileResponseView.setProgramDuration(subscriptionPackage.getPackageDuration().getDuration());
                tileResponseView.setProgramThumbnail(subscriptionPackage.getImage().getImagePath());
                tileResponseView.setProgramName(subscriptionPackage.getTitle());
                tileResponseView.setProgramId(subscriptionPackage.getSubscriptionPackageId());
                tileResponseView.setPlatformType(manageSubscriptionWithStripeDAO.getSubscribedViaPlatform());
                long programCount = manageSubscriptionWithStripeDAO.getProgramCount();
                tileResponseView.setProgramCount(programCount);
                long sessionCount = manageSubscriptionWithStripeDAO.getSessionCount();
                tileResponseView.setSessionCount(sessionCount);
                temp = new Date().getTime();
                OrderManagement orderManagement = manageSubscriptionWithStripeDAO.getOrderManagement();

                if (orderManagement.getSubscriptionType() != null) {
                    tileResponseView.setSubscriptionType(orderManagement.getSubscriptionType().getName());
                }
                log.info("Order management query : Time taken in millis : " + (new Date().getTime() - temp));

                // Formatting and setting the subscribed date
                String formattedDate = fitwiseUtils.formatDate(manageSubscriptionWithStripeDAO.getSubscribedDate());
                tileResponseView.setSubscribedDate(formattedDate);
                tileResponseView.setSubscribedDateTimeStamp(manageSubscriptionWithStripeDAO.getSubscribedDate());
                // Checking whether the auto-subscription is active or not
                temp = new Date().getTime();
                if (KeyConstants.KEY_STRIPE.equals(orderManagement.getModeOfPayment()) && manageSubscriptionWithStripeDAO.getStripeSubscriptionStatus() != null &&
                        manageSubscriptionWithStripeDAO.getStripeSubscriptionStatus().getSubscriptionStatus().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_ACTIVE)) {
                    tileResponseView.setSubscriptionOn(true);
                }
                log.info("Auto subscription status : Time taken in millis : " + (new Date().getTime() - temp));
                subscribedProgramsResponseViews.add(tileResponseView);
            }
            log.info("Response construction for all subscribed packages : Time taken in millis : " + (new Date().getTime() - profilingStart));
        }
        subscribedProgramsResponseViews.sort(Comparator.comparing(SubscribedProgramTileResponseView::getSubscribedDateTimeStamp).reversed());
        responseView.setSubscribedPrograms(subscribedProgramsResponseViews);
        if (subscribedProgramsResponseViews.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_NO_PROGRAMS_SUBSCRIBED, MessageConstants.ERROR);
        }
        log.info("Get subscribed programs : Total Time taken in millis : " + (new Date().getTime() - start));
        log.info("Get subscribed programs ends");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SUBSCRIBED_PROGRAMS_LIST_FETCHED, responseView);
    }

    /**
     * Method used to fetch the countries list from DB
     *
     * @return
     */
    @Transactional
    public ResponseModel getCountriesList() {
        List<Countries> countries = countriesRepository.findAll();
        List<CountryView> countryViews = new ArrayList<>();
        for (Countries country : countries) {
            CountryView countryView = new CountryView();
            countryView.setId(country.getId());
            countryView.setCountry_code(country.getCountryCode());
            countryView.setCountry_name(country.getCountryName());
            countryView.setIsd_code(country.getIsdCode());
            countryViews.add(countryView);
        }
        Map<String, Object> countriesMap = new HashMap();
        countriesMap.put(KeyConstants.KEY_COUNTRIES, countryViews);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_COUNTRIES_FETCHED, countriesMap);
    }

    public Date getProgramSubscriptionExpiry(ProgramSubscription programSubscription) {
        Date expiryDate = null;
        if (programSubscription.getSubscribedViaPlatform().getPlatform().equalsIgnoreCase(DBConstants.IOS)) {
            OrderManagement orderManagement = orderManagementRepo.findTop1ByUserAndProgramOrderByCreatedDateDesc(programSubscription.getUser(), programSubscription.getProgram());
            if (orderManagement != null) {
                ApplePayment applePayment = applePaymentRepository.findTop1ByOrderManagementOrderIdOrderByCreatedDateDesc(orderManagement.getOrderId());
                if (applePayment != null && applePayment.getExpiryDate() != null) {
                    expiryDate = applePayment.getExpiryDate();
                }
            }

        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(programSubscription.getSubscribedDate());
            cal.add(Calendar.DAY_OF_YEAR, Math.toIntExact(programSubscription.getSubscriptionPlan().getDuration()));
            expiryDate = cal.getTime();
        }
        return expiryDate;
    }

    public Date getPackageSubscriptionExpiry(PackageSubscription packageSubscription) {
        Date expiryDate;
        Calendar cal = Calendar.getInstance();
        cal.setTime(packageSubscription.getSubscribedDate());
        cal.add(Calendar.DAY_OF_YEAR, Math.toIntExact(packageSubscription.getSubscriptionPlan().getDuration()-1));
        expiryDate = cal.getTime();
        return expiryDate;
    }

}