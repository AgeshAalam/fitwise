package com.fitwise.service.instructor;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.UserProgramGoalsMapping;
import com.fitwise.entity.UserRole;
import com.fitwise.entity.WorkoutCompletion;
import com.fitwise.entity.subscription.PackageSubscription;
import com.fitwise.entity.subscription.ProgramSubscription;
import com.fitwise.entity.subscription.SubscriptionStatus;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.UserProgramGoalsMappingRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.repository.UserRoleRepository;
import com.fitwise.repository.member.WorkoutCompletionRepository;
import com.fitwise.repository.subscription.PackageSubscriptionRepository;
import com.fitwise.repository.subscription.ProgramSubscriptionRepo;
import com.fitwise.service.SubscriptionService;
import com.fitwise.specifications.PackageSubscriptionSpcifications;
import com.fitwise.specifications.ProgramSubscriptionSpecifications;
import com.fitwise.specifications.UserSpecifications;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.view.ClientSearchListView;
import com.fitwise.view.InstructorClientResponseView;
import com.fitwise.view.InstructorClientView;
import com.fitwise.view.SubscriptionPackagePackageIdAndTitleView;
import com.fitwise.view.program.ProgramIdTitleView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
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
 * Created by Vignesh G on 08/05/20
 */
@Service
@Slf4j
public class InstructorSubscriptionService {

    @Autowired
    private UserComponents userComponents;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SubscriptionService subscriptionService;

    @Autowired
    private ProgramSubscriptionRepo programSubscriptionRepo;

    @Autowired
    private WorkoutCompletionRepository workoutCompletionRepository;

    @Autowired
    private UserProgramGoalsMappingRepository userProgramGoalsMappingRepository;

    @Autowired
    private FitwiseUtils fitwiseUtils;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PackageSubscriptionRepository packageSubscriptionRepository;

    /**
     * Get Lapsed clients
     * @param pageNo
     * @param pageSize
     * @param searchName
     * @param type
     * @return
     * @throws ParseException
     */
    public InstructorClientResponseView getLapseClientsOfAnInstructor(int pageNo, int pageSize, Optional<String> searchName,Optional<String> type) throws ParseException {
        User user = userComponents.getUser();
        return getLapseClientsOfAnInstructor(pageNo,pageSize,searchName,type,user);
    }
    /**
     * Method that returns the clients list of an instructor
     *
     * @param searchName - If search name is null or empty, entire list will be returned.
     *                   If search name is not empty, searched list will be returned.
     * @return
     */
    public InstructorClientResponseView getLapseClientsOfAnInstructor(int pageNo, int pageSize, Optional<String> searchName,Optional<String> type, User user) throws ParseException {
        log.info("Get lapsed clients of an instructor - instructor service method starts.");
        long apiStartTimeMillis = new Date().getTime();

        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ERROR, null);
        }

        List<String> statusList = new ArrayList<>();
        statusList.add(KeyConstants.KEY_TRIAL);
        statusList.add(KeyConstants.KEY_PAID);
        statusList.add(KeyConstants.KEY_PAYMENT_PENDING);

        Set<Long> lapseClientSet = new HashSet<>();

        long profilingEndTimeMillis = new Date().getTime();
        if(!type.isPresent() || (type.isPresent() && type.get().equalsIgnoreCase(KeyConstants.KEY_PROGRAM)) || (type.isPresent() && type.get().equalsIgnoreCase(KeyConstants.KEY_ALL))) {
            //All trial and paid subscriptions of instructor
            List<ProgramSubscription> totalProgramSubscriptions = programSubscriptionRepo.findByProgramOwnerUserIdAndSubscriptionStatusSubscriptionStatusNameIn(user.getUserId(), statusList);
            log.info("Query to get total program subscriptions : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();

            //All unique clients with trial and paid subscriptions of instructor
            Set<Long> clientSet = totalProgramSubscriptions.stream()
                    .filter(programSubscription -> programSubscription.getUser() != null)
                    .map(programSubscription -> programSubscription.getUser().getUserId())
                    .collect(Collectors.toSet());


            //For each client with trial/paid subscription , checking if he is a lapse client.
            for (Long clientId : clientSet) {

                List<ProgramSubscription> userProgramSubscriptions = programSubscriptionRepo.findByProgramOwnerUserIdAndUserUserId(user.getUserId(), clientId);

                //Check if the member is a paid client. If he is, he is not a lapse client.
                boolean isPaidClient = userProgramSubscriptions.stream().anyMatch(subscription -> {
                    SubscriptionStatus subscriptionStatus = subscriptionService.getMemberProgramSubscriptionStatus(subscription);
                    if (subscriptionStatus != null) {
                        String StatusName = subscriptionStatus.getSubscriptionStatusName();
                        if (StatusName.equals(KeyConstants.KEY_PAID) || StatusName.equals(KeyConstants.KEY_PAYMENT_PENDING)) {
                            return true;
                        }
                    }

                    return false;
                });
                if (isPaidClient) {
                    continue;
                }

                //Check if the member is a trial client. If he is, he is not a lapse client.
                boolean isTrialClient = false;
                for (ProgramSubscription userSubscription : userProgramSubscriptions) {
                    SubscriptionStatus subscriptionStatus = subscriptionService.getMemberProgramSubscriptionStatus(userSubscription);
                    if (subscriptionStatus != null) {
                        String StatusName = subscriptionStatus.getSubscriptionStatusName();
                        if (StatusName.equals(KeyConstants.KEY_TRIAL)) {
                            User member = userSubscription.getUser();
                            Programs program = userSubscription.getProgram();
                            List<WorkoutCompletion> workoutCompletionList = workoutCompletionRepository.findByMemberUserIdAndProgramProgramId(member.getUserId(), program.getProgramId());
                            int completedWorkouts = workoutCompletionList.size();
                            int trialWorkouts = fitwiseUtils.getTrialWorkoutsCountForProgram(user, program);

                            if (completedWorkouts < trialWorkouts) {
                                isTrialClient = true;
                                break;
                            } else if (completedWorkouts >= trialWorkouts) {
                                //For Completed trial subscriptions
                                Date trialCompletedDate = workoutCompletionList.get(completedWorkouts - 1).getCompletedDate();
                                Date now = new Date();
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(now);
                                cal.add(Calendar.DAY_OF_YEAR, -10);
                                Date tenDaysAgo = cal.getTime();

                                //Client is considered lapsed if Subscription is lapsed if
                                if (trialCompletedDate.after(tenDaysAgo)) {
                                    isTrialClient = true;
                                    break;
                                }
                            }
                        }
                    }

                }

                if (isTrialClient) {
                    continue;
                }

                lapseClientSet.add(clientId);
            }
            log.info("For each client with trial/paid subscription , checking if he is a lapse client : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        }
        if (!type.isPresent() || (type.isPresent() && type.get().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_PACKAGE)) || (type.isPresent() && type.get().equalsIgnoreCase(KeyConstants.KEY_ALL))) {
            //All trial and paid subscriptions of instructor
            List<PackageSubscription> totalPackageSubscriptions = packageSubscriptionRepository.findBySubscriptionPackageOwnerUserIdAndSubscriptionStatusSubscriptionStatusNameIn(user.getUserId(), statusList);
            log.info("Query to get total package subscriptions : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();

            //All unique clients with trial and paid subscriptions of instructor
            Set<Long> packageClientSet = totalPackageSubscriptions.stream()
                    .filter(packageSubscription -> packageSubscription.getUser() != null)
                    .map(packageSubscription -> packageSubscription.getUser().getUserId())
                    .collect(Collectors.toSet());


            //For each client with trial/paid subscription , checking if he is a lapse client.
            for (Long clientId : packageClientSet) {

                List<PackageSubscription> userPackageSubscriptions = packageSubscriptionRepository.findBySubscriptionPackageOwnerUserIdAndUserUserId(user.getUserId(), clientId);

                //Check if the member is a paid client. If he is, he is not a lapse client.
                boolean isPaidClient = userPackageSubscriptions.stream().anyMatch(subscription -> {
                    SubscriptionStatus subscriptionStatus = subscriptionService.getMemberPackageSubscriptionStatus(subscription);
                    if (subscriptionStatus != null) {
                        String StatusName = subscriptionStatus.getSubscriptionStatusName();
                        if (StatusName.equals(KeyConstants.KEY_PAID) || StatusName.equals(KeyConstants.KEY_PAYMENT_PENDING)) {
                            return true;
                        }
                    }

                    return false;
                });
                if (isPaidClient) {
                    continue;
                }
                lapseClientSet.add(clientId);
            }
            log.info("For each client with trial/paid subscription , checking if he is a lapse client : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));

        }

        if (lapseClientSet.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }

        profilingEndTimeMillis = new Date().getTime();
        List<Long> memberList = new ArrayList<>();
        memberList.addAll(lapseClientSet);
        InstructorClientResponseView instructorClientResponseView = constructClientResponse(memberList, pageNo, pageSize, searchName, user);
        log.info("Construct instructor client response view : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info("Method total execution duration : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get lapsed clients of an instructor - instructor service method ends.");

        return instructorClientResponseView;
    }


    /**
     * Get subscribed clients
     * @param pageNo
     * @param pageSize
     * @param searchName
     * @param type
     * @return
     */
    public InstructorClientResponseView getSubscribedClientsOfAnInstructor(int pageNo, int pageSize, Optional<String> searchName,Optional<String> type) {
        User user = userComponents.getUser();
        return getSubscribedClientsOfAnInstructor(pageNo,pageSize,searchName,type,user);
    }

        /**
         * Method that returns the clients list of an instructor
         *
         * @param searchName - If search name is null or empty, entire list will be returned.
         *                   If search name is not empty, searched list will be returned.
         * @return
         */
    public InstructorClientResponseView getSubscribedClientsOfAnInstructor(int pageNo, int pageSize, Optional<String> searchName,Optional<String> type, User user) {
        log.info("getSubscribedClientsOfAnInstructor starts.");
        long apiStartTimeMillis = System.currentTimeMillis();

        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ERROR, null);
        }

        long profilingStartTimeMillis = System.currentTimeMillis();

        List<String> statusList = new ArrayList<>();
        statusList.add(KeyConstants.KEY_PAID);
        statusList.add(KeyConstants.KEY_PAYMENT_PENDING);

        Set<Long> paidClientSet = new HashSet<>();

        if(!type.isPresent() || (type.isPresent() && type.get().equalsIgnoreCase(KeyConstants.KEY_PROGRAM)) || (type.isPresent() && type.get().equalsIgnoreCase(KeyConstants.KEY_ALL))){
            //All paid subscriptions of instructor
            List<ProgramSubscription> totalPaidProgramSubscriptions = programSubscriptionRepo.findByProgramOwnerUserIdAndSubscriptionStatusSubscriptionStatusNameIn(user.getUserId(), statusList);

            //All unique clients with paid subscriptions of instructor
            Set<Long> clientSet = totalPaidProgramSubscriptions.stream()
                    .filter(programSubscription -> programSubscription.getUser() != null)
                    .map(programSubscription -> programSubscription.getUser().getUserId())
                    .collect(Collectors.toSet());

            //For each client with paid subscription , checking if he is a paid client.
            for (Long clientId : clientSet) {

                List<ProgramSubscription> userPaidProgramSubscriptions = programSubscriptionRepo.findByProgramOwnerUserIdAndUserUserIdAndSubscriptionStatusSubscriptionStatusNameIn(user.getUserId(), clientId, statusList);

                //If user has any active subscription, he is a paid client.
                boolean isPaidClient = userPaidProgramSubscriptions.stream().anyMatch(paidSubscription -> {
                    SubscriptionStatus subscriptionStatus = subscriptionService.getMemberProgramSubscriptionStatus(paidSubscription);
                    if(subscriptionStatus != null){
                        String StatusName = subscriptionStatus.getSubscriptionStatusName();
                        if (StatusName.equals(KeyConstants.KEY_PAID) || StatusName.equals(KeyConstants.KEY_PAYMENT_PENDING)) {
                            return true;
                        }
                    }

                    return false;
                });

                if (isPaidClient) {
                    paidClientSet.add(clientId);
                }

            }
        }



        if(!type.isPresent() || (type.isPresent() && type.get().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_PACKAGE)) || (type.isPresent() && type.get().equalsIgnoreCase(KeyConstants.KEY_ALL))){
            //All paid subscriptions of instructor
            List<PackageSubscription> totalPaidPackageSubscriptions = packageSubscriptionRepository.findBySubscriptionPackageOwnerUserIdAndSubscriptionStatusSubscriptionStatusNameIn(user.getUserId(), statusList);

            //All unique clients with paid subscriptions of instructor
            Set<Long> packageClientSet = totalPaidPackageSubscriptions.stream()
                    .filter(packageSubscription -> packageSubscription.getUser() != null)
                    .map(packageSubscription -> packageSubscription.getUser().getUserId())
                    .collect(Collectors.toSet());

            //For each client with paid subscription , checking if he is a paid client.
            for (Long clientId : packageClientSet) {

                List<PackageSubscription> userPaidPackageSubscriptions = packageSubscriptionRepository.findBySubscriptionPackageOwnerUserIdAndUserUserIdAndSubscriptionStatusSubscriptionStatusNameIn(user.getUserId(), clientId, statusList);

                //If user has any active subscription, he is a paid client.
                boolean isPaidClient = userPaidPackageSubscriptions.stream().anyMatch(paidSubscription -> {
                    SubscriptionStatus subscriptionStatus = subscriptionService.getMemberPackageSubscriptionStatus(paidSubscription);
                    if(subscriptionStatus != null){
                        String StatusName = subscriptionStatus.getSubscriptionStatusName();
                        if (StatusName.equals(KeyConstants.KEY_PAID) || StatusName.equals(KeyConstants.KEY_PAYMENT_PENDING)) {
                            return true;
                        }
                    }

                    return false;
                });

                if (isPaidClient) {
                    paidClientSet.add(clientId);
                }

            }

        }

        long profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Client list and filtering based on conditions : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));


        if (paidClientSet.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }

        List<Long> memberList = new ArrayList<>();
        memberList.addAll(paidClientSet);

        InstructorClientResponseView instructorClientResponseView = constructClientResponse(memberList, pageNo, pageSize, searchName, user);

        long apiEndTimeMillis = System.currentTimeMillis();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("getSubscribedClientsOfAnInstructor ends.");

        return instructorClientResponseView;
    }


    /**
     * Get trial clients
      * @param pageNo
     * @param pageSize
     * @param searchName
     * @return
     * @throws ParseException
     */
    public InstructorClientResponseView getTrialClientsOfAnInstructor(int pageNo, int pageSize, Optional<String> searchName) throws ParseException {
        User user = userComponents.getUser();
        return getTrialClientsOfAnInstructor(pageNo, pageSize, searchName, user);
    }


        /**
         * Method that returns the clients list of an instructor
         *
         * @param searchName - If search name is null or empty, entire list will be returned.
         *                   If search name is not empty, searched list will be returned.
         * @return
         */
    public InstructorClientResponseView getTrialClientsOfAnInstructor(int pageNo, int pageSize, Optional<String> searchName, User user) throws ParseException {
        long startTime = new Date().getTime();
        log.info("Get Trial Clients for instructor start.");
        long temp = new Date().getTime();
        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ERROR, null);
        }
        log.info("Field validation " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        log.info("Get User " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        List<String> statusList = new ArrayList<>();
        statusList.add(KeyConstants.KEY_TRIAL);
        //All trial subscriptions of instructor
        List<ProgramSubscription> totalTrialProgramSubscriptions = programSubscriptionRepo.findByProgramOwnerUserIdAndSubscriptionStatusSubscriptionStatusNameIn(user.getUserId(), statusList);
        log.info("Get all trial subscription for user " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        //All unique clients with trial subscriptions of instructor
        Set<Long> clientSet = totalTrialProgramSubscriptions.stream()
                .filter(programSubscription -> programSubscription.getUser() != null)
                .map(programSubscription -> programSubscription.getUser().getUserId())
                .collect(Collectors.toSet());
        log.info("Filter client ids " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        List<String> paidStatusList = new ArrayList<>();
        paidStatusList.add(KeyConstants.KEY_PAID);
        paidStatusList.add(KeyConstants.KEY_PAYMENT_PENDING);
        Set<Long> trialClientSet = new HashSet<>();
        //For each client with trial subscription , checking if he is a trial client.
        for (Long clientId : clientSet) {
            List<ProgramSubscription> userProgramSubscriptions = programSubscriptionRepo.findByProgramOwnerUserIdAndUserUserId(user.getUserId(), clientId);
            boolean isTrialClient = false;
            for (ProgramSubscription userSubscription : userProgramSubscriptions) {
                User member = userSubscription.getUser();
                Programs program = userSubscription.getProgram();
                List<WorkoutCompletion> workoutCompletionList = workoutCompletionRepository.findByMemberUserIdAndProgramProgramId(member.getUserId(), program.getProgramId());
                int completedWorkouts = workoutCompletionList.size();
                int trialWorkouts = fitwiseUtils.getTrialWorkoutsCountForProgram(user, program);
                //If any trial is not completed, he is a trial client
                if (completedWorkouts < trialWorkouts) {
                    isTrialClient = true;
                    break;
                } else if (completedWorkouts >= trialWorkouts) {
                    //For Completed trial subscriptions
                    List<ProgramSubscription> paidSubscriptions = programSubscriptionRepo.findByProgramOwnerUserIdAndUserUserIdAndSubscriptionStatusSubscriptionStatusNameIn(user.getUserId(), member.getUserId(), paidStatusList);
                    boolean isPaidClient = paidSubscriptions.stream().anyMatch(paidSubscription -> {
                        SubscriptionStatus subscriptionStatus = subscriptionService .getMemberProgramSubscriptionStatus(paidSubscription);
                        if(subscriptionStatus != null){
                            String StatusName = subscriptionStatus.getSubscriptionStatusName();
                            if (StatusName.equals(KeyConstants.KEY_PAID) || StatusName.equals(KeyConstants.KEY_PAYMENT_PENDING)) {
                                return true;
                            }
                        }
                        return false;
                    });
                    Date trialCompletedDate = workoutCompletionList.get(completedWorkouts - 1).getCompletedDate();
                    Date now = new Date();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(now);
                    cal.add(Calendar.DAY_OF_YEAR, -10);
                    Date tenDaysAgo = cal.getTime();
                    //If Client does not have any paid subscription, and if any subscription was completed in the last 10 days, he is a trial client
                    if (!isPaidClient && trialCompletedDate.after(tenDaysAgo)) {
                        isTrialClient = true;
                        break;
                    }
                }
            }
            if (isTrialClient) {
                trialClientSet.add(clientId);
            }
        }
        log.info("User trial check " + (new Date().getTime() - temp));
        if (trialClientSet.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        List<Long> memberList = new ArrayList<>();
        memberList.addAll(trialClientSet);
        InstructorClientResponseView response = constructClientResponse(memberList, pageNo, pageSize, searchName, user);
        log.info("Trail clients for instructor completed : " + (new Date().getTime() - startTime));
        return response;
    }

    private InstructorClientResponseView constructClientResponse(List<Long> memberList, int pageNo, int pageSize, Optional<String> searchName, User instructor) {
        log.info("Construct client response");
        long temp = new Date().getTime();
        log.info("Get User " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        Specification<User> userSpecification = UserSpecifications.getUsersIdIn(memberList);
        if (searchName.isPresent() && !searchName.get().isEmpty()) {
            String search = searchName.get().toLowerCase();
            userSpecification = userSpecification.and(UserSpecifications.getUserByName(search));
        }
        Page<User> clientList = userRepository.findAll(userSpecification, PageRequest.of(pageNo - 1, pageSize));
        if(clientList.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        log.info("Get Client List " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        UserRole memberRole = userRoleRepository.findByName(KeyConstants.KEY_MEMBER);
        log.info("Get member role " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        //Constructing Client response view
        List<InstructorClientView> instructorClientViewList = new ArrayList<>();
        for (User client : clientList.getContent()) {
            InstructorClientView instructorClientView = new InstructorClientView();
            instructorClientView.setClientId(client.getUserId());
            UserProfile userProfile = userProfileRepository.findByUser(client);
            instructorClientView.setInstructorName(userProfile.getFirstName() + " " + userProfile.getLastName());
            instructorClientView.setClientName(userProfile.getFirstName() + " " + userProfile.getLastName());
            if (userProfile.getProfileImage() != null) {
                instructorClientView.setInstructorImgUrl(userProfile.getProfileImage().getImagePath());
                instructorClientView.setClientImgUrl(userProfile.getProfileImage().getImagePath());
            }
            boolean isActive = fitwiseUtils.isUserActive(client, memberRole);
            instructorClientView.setActiveStatus(isActive);
            List<UserProgramGoalsMapping> userProgramGoalsMappings = userProgramGoalsMappingRepository.findByUser(client);
            if (!userProgramGoalsMappings.isEmpty()) {
                String interests = userProgramGoalsMappings.stream()
                        .map(userProgramGoalsMapping -> userProgramGoalsMapping.getProgramExpertiseGoalsMapping().getProgramExpertiseMapping().getProgramType().getProgramTypeName())
                        .distinct().collect(Collectors.joining(","));
                instructorClientView.setInterestedIn(interests);
            }
            instructorClientView.setPrograms(getClientProgramNameList(instructor, client));
            instructorClientView.setSubscriptionPackages(getClientPackageNameList(instructor, client));
            instructorClientViewList.add(instructorClientView);
        }
        log.info("Instructor client view " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        long totalCount = clientList.getTotalElements();
        InstructorClientResponseView instructorClientResponseView = new InstructorClientResponseView();
        instructorClientResponseView.setClients(instructorClientViewList);
        instructorClientResponseView.setTotalClients(totalCount);
        log.info("Response " + (new Date().getTime() - temp));
        return instructorClientResponseView;
    }

    private List<ProgramIdTitleView> getClientProgramNameList(User user, User client) {
        List<ProgramIdTitleView> programIdTitleViewList = new ArrayList<>();

        List<String> statusList = new ArrayList<>();
        statusList.add(KeyConstants.KEY_TRIAL);
        statusList.add(KeyConstants.KEY_PAID);
        statusList.add(KeyConstants.KEY_PAYMENT_PENDING);

        //All trial, paid and expired subscriptions of client with instructor
        List<ProgramSubscription> totalProgramSubscriptions = programSubscriptionRepo.findByProgramOwnerUserIdAndUserUserIdAndSubscriptionStatusSubscriptionStatusNameIn(user.getUserId(), client.getUserId(), statusList);

        for (ProgramSubscription programSubscription : totalProgramSubscriptions) {
            Programs program = programSubscription.getProgram();

            ProgramIdTitleView programIdTitleView = new ProgramIdTitleView();
            programIdTitleView.setProgramId(program.getProgramId());
            programIdTitleView.setProgramTitle(program.getTitle());

            programIdTitleViewList.add(programIdTitleView);
        }
        return programIdTitleViewList;
    }

    private List<SubscriptionPackagePackageIdAndTitleView> getClientPackageNameList(User user, User client){
        List<SubscriptionPackagePackageIdAndTitleView> subscriptionPackagePackageIdAndTitleViews = new ArrayList<>();

        List<String> statusList = new ArrayList<>();
        statusList.add(KeyConstants.KEY_PAID);
        statusList.add(KeyConstants.KEY_PAYMENT_PENDING);

        List<PackageSubscription> packageSubscriptions = packageSubscriptionRepository.findBySubscriptionPackageOwnerUserIdAndUserUserIdAndSubscriptionStatusSubscriptionStatusNameIn(user.getUserId(),client.getUserId(),statusList);
        for(PackageSubscription packageSubscription : packageSubscriptions){
            SubscriptionPackagePackageIdAndTitleView subscriptionPackagePackageIdAndTitleView = new SubscriptionPackagePackageIdAndTitleView();
            subscriptionPackagePackageIdAndTitleView.setSubscriptionPackageId(packageSubscription.getSubscriptionPackage().getSubscriptionPackageId());
            subscriptionPackagePackageIdAndTitleView.setTitle(packageSubscription.getSubscriptionPackage().getTitle());

            subscriptionPackagePackageIdAndTitleViews.add(subscriptionPackagePackageIdAndTitleView);
        }

        return  subscriptionPackagePackageIdAndTitleViews;

    }

    /**
     * Method to get clients of an instructor
     * @param searchEmail
     * @return
     */
    public Map<String, Object> getClients(Optional<String> searchEmail) {

        User user = userComponents.getUser();

        List<String> statusList = new ArrayList<>();
        statusList.add(KeyConstants.KEY_TRIAL);
        statusList.add(KeyConstants.KEY_PAID);
        statusList.add(KeyConstants.KEY_PAYMENT_PENDING);

        //All subscriptions of instructor
        List<ProgramSubscription> totalProgramSubscriptions;
        if (searchEmail.isPresent() && !searchEmail.get().isEmpty()) {
            //totalProgramSubscriptions = programSubscriptionRepo.findByProgramOwnerUserIdAndSubscriptionStatusSubscriptionStatusNameInAndUserEmailIgnoreCaseContaining(user.getUserId(), statusList, searchEmail.get());

            Specification<ProgramSubscription> specification = ProgramSubscriptionSpecifications.getProgramSubscriptionByOwnerAndStatusAndMemberSearch(user.getUserId(), statusList, searchEmail.get());
            totalProgramSubscriptions = programSubscriptionRepo.findAll(specification);

        } else {
            totalProgramSubscriptions = programSubscriptionRepo.findByProgramOwnerUserIdAndSubscriptionStatusSubscriptionStatusNameIn(user.getUserId(), statusList);
        }

        List<PackageSubscription> totalPackageSubscriptions;
        if (searchEmail.isPresent() && !searchEmail.get().isEmpty()) {
            //totalProgramSubscriptions = programSubscriptionRepo.findByProgramOwnerUserIdAndSubscriptionStatusSubscriptionStatusNameInAndUserEmailIgnoreCaseContaining(user.getUserId(), statusList, searchEmail.get());

            Specification<PackageSubscription> specification = PackageSubscriptionSpcifications.getPackageSubscriptionByOwnerAndStatusAndMemberSearch(user.getUserId(), statusList, searchEmail.get());
            totalPackageSubscriptions = packageSubscriptionRepository.findAll(specification);

        } else {
            totalPackageSubscriptions = packageSubscriptionRepository.findBySubscriptionPackageOwnerUserIdAndSubscriptionStatusSubscriptionStatusNameIn(user.getUserId(), statusList);
        }

        if (totalProgramSubscriptions.isEmpty() && totalPackageSubscriptions.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        List<User> clientList = totalProgramSubscriptions.stream()
                .filter(programSubscription -> programSubscription.getUser() != null)
                .map(programSubscription -> programSubscription.getUser()).collect(Collectors.toList());

        List<User> packageClientList = totalPackageSubscriptions.stream()
                .filter(packageSubscription -> packageSubscription.getUser() != null)
                .map(packageSubscription -> packageSubscription.getUser()).collect(Collectors.toList());

        clientList.addAll(packageClientList);

        //removing duplicates
        clientList = clientList.stream()
                .collect(collectingAndThen(toCollection(() -> new TreeSet<User>(comparingLong(User::getUserId))), ArrayList::new));

        List<ClientSearchListView> clientSearchListViewList = new ArrayList<>();
        for (User client : clientList) {
            ClientSearchListView clientSearchListView = new ClientSearchListView();
            clientSearchListView.setUserId(client.getUserId());
            clientSearchListView.setEmail(client.getEmail());

            UserProfile userProfile = userProfileRepository.findByUser(client);
            String fullName = fitwiseUtils.getUserFullName(userProfile);
            clientSearchListView.setName(fullName);
            if (userProfile.getProfileImage() != null) {
                clientSearchListView.setImgUrl(userProfile.getProfileImage().getImagePath());
            }

            clientSearchListViewList.add(clientSearchListView);
        }

        Map<String, Object> respMap = new HashMap<>();
        respMap.put(KeyConstants.CLIENTS, clientSearchListViewList);
        respMap.put(KeyConstants.KEY_TOTAL_COUNT, clientSearchListViewList.size());

        return respMap;
    }

    public List<User> getClientsOfInstructor() {
        User user = userComponents.getUser();

        List<String> statusList = new ArrayList<>();
        statusList.add(KeyConstants.KEY_TRIAL);
        statusList.add(KeyConstants.KEY_PAID);
        statusList.add(KeyConstants.KEY_PAYMENT_PENDING);

        //All subscriptions of instructor
        List<ProgramSubscription> totalProgramSubscriptions;
        totalProgramSubscriptions = programSubscriptionRepo.findByProgramOwnerUserIdAndSubscriptionStatusSubscriptionStatusNameIn(user.getUserId(), statusList);
        List<PackageSubscription> totalPackageSubscriptions = packageSubscriptionRepository.findBySubscriptionPackageOwnerUserIdAndSubscriptionStatusSubscriptionStatusNameIn(user.getUserId(),statusList);
        if (totalProgramSubscriptions.isEmpty() && totalPackageSubscriptions.isEmpty()) {
            return null;
        }

        List<User> clientList = totalProgramSubscriptions.stream()
                .filter(programSubscription -> programSubscription.getUser() != null)
                .map(programSubscription -> programSubscription.getUser()).collect(Collectors.toList());

        List<User> packageClientList = totalPackageSubscriptions.stream()
                .filter(packageSubscription -> packageSubscription.getUser() != null)
                .map(packageSubscription -> packageSubscription.getUser()).collect(Collectors.toList());

        clientList.addAll(packageClientList);

        //removing duplicates
        clientList = clientList.stream()
                .collect(collectingAndThen(toCollection(() -> new TreeSet<User>(comparingLong(User::getUserId))), ArrayList::new));
        return clientList;
    }
}
