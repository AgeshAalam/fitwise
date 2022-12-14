package com.fitwise.service.member;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SecurityFilterConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.entity.BlockedUser;
import com.fitwise.entity.FeaturedInstructors;
import com.fitwise.entity.InstructorAwards;
import com.fitwise.entity.InstructorCertification;
import com.fitwise.entity.InstructorProgramExperience;
import com.fitwise.entity.OtherExpertise;
import com.fitwise.entity.ProgramExpertiseGoalsMapping;
import com.fitwise.entity.ProgramTypes;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.UserProgramGoalsMapping;
import com.fitwise.entity.UserRole;
import com.fitwise.entity.YearsOfExpertise;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.product.FreeAccessProgram;
import com.fitwise.exception.ApplicationException;
import com.fitwise.model.member.ExpertiseAndGoalsModel;
import com.fitwise.model.member.MemberInstructorFilterModel;
import com.fitwise.repository.AwardsRepository;
import com.fitwise.repository.BlockedUserRepository;
import com.fitwise.repository.CertificateRepository;
import com.fitwise.repository.FeaturedInstructorsRepository;
import com.fitwise.repository.InstructorExperienceRepository;
import com.fitwise.repository.OtherExpertiseRepository;
import com.fitwise.repository.ProgramExpertiseGoalsMappingRepository;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.ProgramTypeRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.UserProgramGoalsMappingRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.repository.UserRoleRepository;
import com.fitwise.repository.YearsOfExpertiseRepository;
import com.fitwise.repository.packaging.SubscriptionPackageRepository;
import com.fitwise.repository.subscription.ProgramSubscriptionRepo;
import com.fitwise.service.discountservice.DiscountsService;
import com.fitwise.service.freeproduct.FreeAccessService;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.specifications.FeaturedInstructorSpecifications;
import com.fitwise.specifications.InstructorProgramExperienceSpecifications;
import com.fitwise.specifications.PackageSpecification;
import com.fitwise.specifications.ProgramSpecifications;
import com.fitwise.specifications.UserProfileSpecifications;
import com.fitwise.specifications.UserSpecifications;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.RequestParamValidator;
import com.fitwise.view.ExperienceMemberView;
import com.fitwise.view.OtherExpertiseResponseView;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.UserProfileTileView;
import com.fitwise.view.member.Filter;
import com.fitwise.view.member.InstructorProfileProgramView;
import com.fitwise.view.member.MemberFilterView;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * Created by Vignesh G on 28/04/20
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

    @Autowired
    private UserComponents userComponents;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    BlockedUserRepository blockedUserRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserProgramGoalsMappingRepository userProgramGoalsMappingRepository;

    @Autowired
    private ProgramExpertiseGoalsMappingRepository programExpertiseGoalsMappingRepository;

    @Autowired
    CertificateRepository certificateRepository;

    @Autowired
    ProgramRepository programRepository;

    @Autowired
    YearsOfExpertiseRepository yearsOfExpertiseRepository;

    @Autowired
    ProgramTypeRepository programTypeRepository;

    @Autowired
    InstructorExperienceRepository instructorExperienceRepository;

    @Autowired
    private AwardsRepository awardsRepository;

    @Autowired
    private OtherExpertiseRepository otherExpertiseRepository;

    @Autowired
    FitwiseUtils fitwiseUtils;

    @Autowired
    ProgramSubscriptionRepo programSubscriptionRepo;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private FeaturedInstructorsRepository featuredInstructorsRepository;

    @Autowired
    private DiscountsService discountsService;

    private final FreeAccessService freeAccessService;
    private final SubscriptionPackageRepository subscriptionPackageRepository;

    /**
     * Get instructor filter
     * @return
     */
    public List<MemberFilterView> getInstructorFilter() {
        MemberFilterView programTypeFilter = new MemberFilterView();
        programTypeFilter.setFilterName(KeyConstants.KEY_PROGRAM_TYPE_FILTER_NAME);
        programTypeFilter.setType(KeyConstants.KEY_PROGRAM_TYPE_FILTER_TYPE);
        List<ProgramTypes> programTypes = programTypeRepository.findByOrderByProgramTypeNameAsc();
        List<Filter> programTypeFilterList = new ArrayList<>();
        for (ProgramTypes programType : programTypes) {
            Filter filter = new Filter();
            filter.setFilterId(programType.getProgramTypeId());
            filter.setFilterName(programType.getProgramTypeName());
            programTypeFilterList.add(filter);
        }
        programTypeFilter.setFilters(programTypeFilterList);
        List<MemberFilterView> instructorFilterViewList = new ArrayList<>();
        instructorFilterViewList.add(programTypeFilter);
        return instructorFilterViewList;
    }

    /**
     * Gets the instructors.
     *
     * @param pageNo
     * @param pageSize
     * @param filterModel
     * @param search
     * @return the instructors
     */
    public Map<String, Object> getInstructors(int pageNo, int pageSize, MemberInstructorFilterModel filterModel, Optional<String> search) {
        log.info("Member instructor L1 starts.");
        long apiStartTimeMillis = new Date().getTime();
        RequestParamValidator.pageSetup(pageNo, pageSize);
        List<User> users;
        List<User> userList = new ArrayList<>();
        List<BlockedUser> blockedUsersInstructors = blockedUserRepository.findByUserRoleName(SecurityFilterConstants.ROLE_INSTRUCTOR);
        List<Long> blockedUserId = blockedUsersInstructors.stream().map(user -> user.getUser().getUserId()).collect(Collectors.toList());
        long totalCount;
        long profilingStartTimeMillis = new Date().getTime();
        if (filterModel.getProgramType() == null || filterModel.getProgramType().isEmpty()) {
            Specification<User> blockSpec = UserSpecifications.getUsersNotInIdList(blockedUserId);
            UserRole userRole = userRoleRepository.findByName(SecurityFilterConstants.ROLE_INSTRUCTOR);
            if (userRole == null) {
                throw new ApplicationException(Constants.NOT_FOUND, MessageConstants.ERROR, null);
            }
            Specification<User> roleSpec = UserSpecifications.getUserByRoleId(userRole.getRoleId());
            Specification<User> blockAndRoleSpec;
            if (blockedUserId.isEmpty()) {
                blockAndRoleSpec = roleSpec;
            } else {
                blockAndRoleSpec = roleSpec.and(blockSpec);
            }
            if (search.isPresent() && !search.get().isEmpty()) {
                Specification<User> nameSpec = UserSpecifications.getUserByName(search.get());
                Specification<User> finalSpec = nameSpec.and(blockAndRoleSpec);
                users = userRepository.findAll(finalSpec);
            } else {
                users = userRepository.findAll(blockAndRoleSpec);
            }
        } else {
            List<Long> programTypeIdList;
            if (filterModel.getProgramType() != null && !filterModel.getProgramType().isEmpty()) {
                programTypeIdList = filterModel.getProgramType().stream().map(Filter::getFilterId).collect(Collectors.toList());
            } else {
                programTypeIdList = programTypeRepository.findByOrderByProgramTypeNameAsc().stream().map(ProgramTypes::getProgramTypeId).collect(Collectors.toList());
            }
            Specification<InstructorProgramExperience> programTypeSpec = InstructorProgramExperienceSpecifications.getInstructorProgramExperienceByProgramTypeIn(programTypeIdList);
            Specification<InstructorProgramExperience> blockSpec = InstructorProgramExperienceSpecifications.getInstructorsNotInUserList(blockedUserId);
            Specification<InstructorProgramExperience> programTypeAndBlockSpec;
            if (blockedUserId.isEmpty()) {
                programTypeAndBlockSpec = programTypeSpec;
            } else {
                programTypeAndBlockSpec = programTypeSpec.and(blockSpec);
            }
            List<InstructorProgramExperience> instructorProgramExperiencePage;
            if (search.isPresent() && !search.get().isEmpty()) {
                Specification<InstructorProgramExperience> nameSpec = InstructorProgramExperienceSpecifications.getInstructorProgramExperienceByName(search.get());
                Specification<InstructorProgramExperience> finalSpec = nameSpec.and(programTypeAndBlockSpec);
                instructorProgramExperiencePage = instructorExperienceRepository.findAll(finalSpec);
            } else {
                instructorProgramExperiencePage = instructorExperienceRepository.findAll(programTypeAndBlockSpec);
            }
            users = instructorProgramExperiencePage.stream().map(InstructorProgramExperience::getUser).collect(Collectors.toList());
        }
        long profilingEndTimeMillis = new Date().getTime();
        log.info("Query : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        if (users == null || users.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        profilingStartTimeMillis = new Date().getTime();
        List<FeaturedInstructors> featuredInstructors = featuredInstructorsRepository.findAllByOrderById();
        for(FeaturedInstructors featuredInstructor : featuredInstructors){
            if(users.stream().anyMatch(user -> user.getUserId().equals(featuredInstructor.getUser().getUserId()))){
                userList.add(featuredInstructor.getUser());
            }
        }
        for(User user : users){
            if (!(userList.stream().anyMatch(user1 -> user1.getUserId().equals(user.getUserId())))) {
                userList.add(user);
            }
        }
        profilingEndTimeMillis = new Date().getTime();
        log.info("Featured instructors set first : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        profilingStartTimeMillis = new Date().getTime();
        List<UserProfileTileView> userProfileTileViews = new ArrayList<>();
        for (User user : userList) {
            UserProfileTileView userProfileTileView = new UserProfileTileView();
            userProfileTileView.setUserId(user.getUserId());
            List<UserProgramGoalsMapping> userProgramGoalsMappings = userProgramGoalsMappingRepository.findByUser(user);
            List<ProgramTypes> programTypes = new ArrayList<>();
            for (UserProgramGoalsMapping userProgramGoalsMapping : userProgramGoalsMappings) {
                programTypes.add(userProgramGoalsMapping.getProgramExpertiseGoalsMapping().getProgramExpertiseMapping().getProgramType());
            }
            List<ExperienceMemberView> instructorExperience = new ArrayList<>();
            List<InstructorProgramExperience> instructorProgramExperienceList = instructorExperienceRepository.findByUserUserId(user.getUserId());
            for (InstructorProgramExperience instructorProgramExperience : instructorProgramExperienceList) {
                ExperienceMemberView experienceMemberView = new ExperienceMemberView();
                ProgramTypes programType = programTypeRepository.findByProgramTypeId(instructorProgramExperience.getProgramType().getProgramTypeId());
                experienceMemberView.setProgramType(programType.getProgramTypeName());
                YearsOfExpertise yearsOfExpertise = yearsOfExpertiseRepository.findByExperienceId(instructorProgramExperience.getExperience().getExperienceId());
                experienceMemberView.setNumberOfYears(yearsOfExpertise.getNumberOfYears());
                instructorExperience.add(experienceMemberView);
            }
            userProfileTileView.setInstructorExperience(instructorExperience);
            Specification<Programs> programStatusSpec = ProgramSpecifications.getProgramByStatus(KeyConstants.KEY_PUBLISH);
            Specification<Programs> stripeActiveSpec = ProgramSpecifications.getProgramByStripeMapping();
            Specification<Programs> ownerSpec = ProgramSpecifications.getProgramByOwnerUserId(user.getUserId());
            Specification<Programs> finalSpec = programStatusSpec.and(ownerSpec).and(stripeActiveSpec);
            List<Programs> programs = programRepository.findAll(finalSpec);
            int programCount = programs.size();
            UserProfile userProfile = userProfileRepository.findByUser(user);
            if (programCount == 0 || userProfile == null) {
                continue;
            }
            userProfileTileView.setFirstName(userProfile.getFirstName());
            userProfileTileView.setLastName(userProfile.getLastName());
            if (userProfile.getProfileImage() != null) {
                userProfileTileView.setProfileImageUrl(userProfile.getProfileImage().getImagePath());
            }
            userProfileTileView.setProgramCount(programCount);
            List<InstructorCertification> instructorCertification = certificateRepository.findByUser(user);
            userProfileTileView.setCertificateCount(instructorCertification.size());
            double instructorRating = 0.0;
            for (Programs program : programs) {
                double rating = (fitwiseUtils.getProgramRating(program.getProgramId())).doubleValue();
                instructorRating += rating;
            }
            BigDecimal averageInstructorRating = BigDecimal.valueOf(instructorRating/programs.size()).setScale(2, RoundingMode.HALF_UP);
            userProfileTileView.setAverageRating(averageInstructorRating);
            userProfileTileViews.add(userProfileTileView);
        }
        profilingEndTimeMillis = new Date().getTime();
        log.info("Response construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        profilingStartTimeMillis = new Date().getTime();
        int fromIndex = (pageNo - 1) * pageSize;
        if (userProfileTileViews.isEmpty() || userProfileTileViews.size() < fromIndex) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        totalCount = userProfileTileViews.size();
        userProfileTileViews = userProfileTileViews.subList(fromIndex, Math.min(fromIndex + pageSize, userProfileTileViews.size()));
        profilingEndTimeMillis = new Date().getTime();
        log.info("Pagination sublist : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        Map<String, Object> response = new HashMap<>();
        response.put(KeyConstants.KEY_TOTAL_COUNT, totalCount);
        response.put(KeyConstants.KEY_INSTRUCTORS, userProfileTileViews);
        long apiEndTimeMillis = new Date().getTime();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("Member instructor L1 ends.");
        return response;
    }

    /**
     * Get the instructors
     * @param pageNo
     * @param pageSize
     * @param programTypeIds
     * @param isFeatured
     * @param search
     * @return
     */
    public Map<String, Object> getInstructors(int pageNo, int pageSize, final List<Long> programTypeIds, final boolean isFeatured, Optional<String> search) {
        log.info("Start : " + new Date());
        long startIns = new Date().getTime();
        log.info("Get Instructor Start : " + startIns);
        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ERROR, null);
        }
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize);
        long tempTime = new Date().getTime();
        log.info("Get blocked instructors : " + tempTime);
        List<BlockedUser> blockedUsersInstructors = blockedUserRepository.findByUserRoleName(SecurityFilterConstants.ROLE_INSTRUCTOR);
        List<Long> blockedUserId = blockedUsersInstructors.stream().map(user -> user.getUser().getUserId()).collect(Collectors.toList());
        log.info("Get blocked instructors completed: " + (new Date().getTime() - tempTime));
        List<User> users;
        long totalCount;
        List<UserProfile> userProfiles = new LinkedList<>();
        if(isFeatured){
            tempTime = new Date().getTime();
            log.info("Get featured instructors : " + tempTime);
            Specification<FeaturedInstructors> publishedProgramLimitationSpec = FeaturedInstructorSpecifications.getInstructorsByPublishPrograms();
            Specification<FeaturedInstructors> publishedSubscriptionPackageLimitationSpec = FeaturedInstructorSpecifications.getInstructorsByPublishedSubrictionPackages();
            Specification<FeaturedInstructors> publishedProgramOrPackageSpec = publishedProgramLimitationSpec.or(publishedSubscriptionPackageLimitationSpec);
            Specification<FeaturedInstructors> featuredInstructorsSpecification = null;
            if (!blockedUserId.isEmpty()) {
                featuredInstructorsSpecification = FeaturedInstructorSpecifications.getUsersNotInIdList(blockedUserId);
            }
            if(featuredInstructorsSpecification != null){
                featuredInstructorsSpecification = featuredInstructorsSpecification.and(publishedProgramOrPackageSpec);
            }else{
                featuredInstructorsSpecification = publishedProgramOrPackageSpec;
            }
            Page<FeaturedInstructors> featuredInstructors = featuredInstructorsRepository.findAll(featuredInstructorsSpecification, pageRequest);
            for(FeaturedInstructors featuredInstructor: featuredInstructors){
                UserProfile userProfile = userProfileRepository.findByUser(featuredInstructor.getUser());
                userProfiles.add(userProfile);
            }
            totalCount = featuredInstructors.getTotalElements();
            log.info("Get featured instructors completed: " + (new Date().getTime() - tempTime));
        }else{
            Page<User> filteredOrAllUsers;
            if(programTypeIds == null || programTypeIds.isEmpty()){
                Specification<User> blockSpec = UserSpecifications.getUsersNotInIdList(blockedUserId);
                tempTime = new Date().getTime();
                log.info("Get User role: " + tempTime);
                UserRole userRole = userRoleRepository.findByName(SecurityFilterConstants.ROLE_INSTRUCTOR);
                log.info("Get User role completed: " + (new Date().getTime() - tempTime));
                if (userRole == null) {
                    throw new ApplicationException(Constants.NOT_FOUND, MessageConstants.ERROR, null);
                }
                Specification<User> roleSpec = UserSpecifications.getUserByRoleId(userRole.getRoleId());
                Specification<User> blockAndRoleSpec;
                if (blockedUserId.isEmpty()) {
                    blockAndRoleSpec = roleSpec;
                } else {
                    blockAndRoleSpec = roleSpec.and(blockSpec);
                }
                Specification<User> programLimitationSpec = UserSpecifications.getInstructorsByPublishPrograms();
                Specification<User> subscriptionPackageLimitationSpec = UserSpecifications.getInstructorsByPublishedSubrictionPackages();
                Specification<User> publishedProgramOrPackageSpec = programLimitationSpec.or(subscriptionPackageLimitationSpec);
                if (search.isPresent() && !search.get().isEmpty()) {
                    Specification<User> nameSpec = UserSpecifications.getUserByName(search.get());
                    Specification<User> finalSpec = nameSpec.and(blockAndRoleSpec).and(publishedProgramOrPackageSpec);
                    tempTime = new Date().getTime();
                    log.info("Get User by name and not blocked: " + tempTime);
                    filteredOrAllUsers = userRepository.findAll(finalSpec, pageRequest);
                    log.info("Get User by name and not blocked completed: " + (new Date().getTime() - tempTime));
                } else {
                    tempTime = new Date().getTime();
                    log.info("Get User by not blocked : " + tempTime);
                    filteredOrAllUsers = userRepository.findAll(blockAndRoleSpec.and(publishedProgramOrPackageSpec), pageRequest);
                    log.info("Get User by not blocked completed: " + (new Date().getTime() - tempTime));
                }
                totalCount = filteredOrAllUsers.getTotalElements();
                users = filteredOrAllUsers.getContent();
            }else{
                Specification<UserProfile> userprofileWithProgramTypeSpec = UserProfileSpecifications.getInstructorProgramExperienceByProgramTypeIn(programTypeIds);
                Specification<UserProfile> userBlockSpec = UserProfileSpecifications.getUserProfilesNotInUserIdList(blockedUserId);
                Specification<UserProfile> programLimitationSpec = UserProfileSpecifications.getInstructorsByPublishPrograms();
                Specification<UserProfile> subscriptionPackageLimitationSpec = UserProfileSpecifications.getInstructorsByPublishedSubrictionPackages();
                Specification<UserProfile> publishedProgramOrPackageSpec = programLimitationSpec.or(subscriptionPackageLimitationSpec);
                Specification<UserProfile> programTypeAndBlockSpec = publishedProgramOrPackageSpec;
                if (blockedUserId.isEmpty()) {
                    programTypeAndBlockSpec = programTypeAndBlockSpec.and(userprofileWithProgramTypeSpec);
                } else {
                    programTypeAndBlockSpec = programTypeAndBlockSpec.and(userprofileWithProgramTypeSpec).and(userBlockSpec);
                }
                Page<UserProfile> instructorProgramExperiencePage;
                if (search.isPresent() && !search.get().isEmpty()) {
                    Specification<UserProfile> nameSpec = UserProfileSpecifications.getUserProfileByName(search.get());
                    Specification<UserProfile> finalSpec = nameSpec.and(programTypeAndBlockSpec);
                    tempTime = new Date().getTime();
                    log.info("Get User by filter and name : " + tempTime);
                    instructorProgramExperiencePage = userProfileRepository.findAll(finalSpec, pageRequest);
                    log.info("Get User by filter and name completed: " + (new Date().getTime() - tempTime));
                } else {
                    tempTime = new Date().getTime();
                    log.info("Get User by filter: " + tempTime);
                    instructorProgramExperiencePage = userProfileRepository.findAll(programTypeAndBlockSpec, pageRequest);
                    log.info("Get User by filter completed: " + (new Date().getTime() - tempTime));
                }
                totalCount = instructorProgramExperiencePage.getTotalElements();
                users = instructorProgramExperiencePage.stream().map(UserProfile::getUser).collect(Collectors.toList());
            }
            userProfiles = userProfileRepository.findByUserIn(users);
        }
        if (userProfiles.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        List<UserProfileTileView> userProfileTileViews = new ArrayList<>();
        long instructorCountForZeroProgramAndPackage = 0;
        for (UserProfile userProfile : userProfiles) {
            User user = userProfile.getUser();
            UserProfileTileView userProfileTileView = new UserProfileTileView();
            userProfileTileView.setUserId(user.getUserId());
            userProfileTileView.setFirstName(userProfile.getFirstName());
            userProfileTileView.setLastName(userProfile.getLastName());
            if (userProfile.getProfileImage() != null) {
                userProfileTileView.setProfileImageUrl(userProfile.getProfileImage().getImagePath());
            }
            long tempTime1 = new Date().getTime();
            List<ExperienceMemberView> instructorExperience = new ArrayList<>();
            log.info("Get User Experience: " + tempTime1);
            List<InstructorProgramExperience> instructorProgramExperienceList = instructorExperienceRepository.findByUserUserId(userProfile.getUser().getUserId());
            for (InstructorProgramExperience instructorProgramExperience : instructorProgramExperienceList) {
                ExperienceMemberView experienceMemberView = new ExperienceMemberView();
                experienceMemberView.setProgramType(instructorProgramExperience.getProgramType().getProgramTypeName());
                experienceMemberView.setNumberOfYears(instructorProgramExperience.getExperience().getNumberOfYears());
                instructorExperience.add(experienceMemberView);
            }
            log.info("Get User Exp completed: " + (new Date().getTime() - tempTime1));
            userProfileTileView.setInstructorExperience(instructorExperience);
            Specification<Programs> programStatusSpec = ProgramSpecifications.getProgramByStatus(KeyConstants.KEY_PUBLISH);
            Specification<Programs> stripeActiveSpec = ProgramSpecifications.getProgramByStripeMapping();
            Specification<Programs> ownerSpec = ProgramSpecifications.getProgramByOwnerUserId(user.getUserId());
            Specification<Programs> finalSpec = programStatusSpec.and(ownerSpec).and(stripeActiveSpec);
            tempTime1 = new Date().getTime();
            log.info("Get User Programs: " + tempTime1);
            long programCount = programRepository.count(finalSpec);
            userProfileTileView.setProgramCount(programCount);
            log.info("Get User Pgm completed: " + (new Date().getTime() - tempTime1));

			Specification<SubscriptionPackage> packageStatusSpec = PackageSpecification
					.getSubscriptionPackageByStatus(KeyConstants.KEY_PUBLISH);
			Specification<SubscriptionPackage> packageOwnerSpec = PackageSpecification
					.getPackageByOwnerUserId(user.getUserId());
			Specification<SubscriptionPackage> packageStripeActiveSpec = PackageSpecification
					.getPackageByStripeMapping();
			Specification<SubscriptionPackage> nonRestrictedPackage = PackageSpecification.getNonRestrictedPackages();
			Specification<SubscriptionPackage> packageFinalSpec = packageStatusSpec.and(packageOwnerSpec)
					.and(packageStripeActiveSpec).and(nonRestrictedPackage);
            tempTime1 = new Date().getTime();
            log.info("Get User Subscription Packages: " + tempTime1);
            long packageCount = subscriptionPackageRepository.count(packageFinalSpec);
            userProfileTileView.setPackageCount(packageCount);
            log.info("Get User Subscription Packages completed: " + (new Date().getTime() - tempTime1));
            
            tempTime1 = new Date().getTime();
            log.info("Get User Certificate: " + tempTime1);
            userProfileTileView.setCertificateCount(certificateRepository.countByUser(user));
            log.info("Get User Cert count completed: " + (new Date().getTime() - tempTime1));
			if (programCount > 0 || packageCount > 0) {  // Workaround for to avoid if instructor has 0 published program and package
				userProfileTileViews.add(userProfileTileView);
			} else {
				instructorCountForZeroProgramAndPackage++;
			}
        }
        log.info("Constructing user response completed: " + (new Date().getTime() - tempTime));
        Map<String, Object> response = new HashMap<>();
        response.put(KeyConstants.KEY_TOTAL_COUNT, totalCount - instructorCountForZeroProgramAndPackage);
        response.put(KeyConstants.KEY_INSTRUCTORS, userProfileTileViews);
        log.info("Get Instructor End : " + (new Date().getTime() - startIns));
        log.info("End : " + new Date());
        return response;
    }

    /**
     * getting instructor profile for member app
     *
     * @param instructorId
     * @param pageNoOptional
     * @param pageSizeOptional
     * @return
     */
    public ResponseModel getInstructorProfileForMember(Long instructorId, Optional<Integer> pageNoOptional, Optional<Integer> pageSizeOptional) {
        log.info("Get Instructor profile for member starts");
        long startTime = System.currentTimeMillis();
        User member = userComponents.getAndValidateUser();
        //Validation
        User instructor = validationService.validateInstructorId(instructorId);
        boolean isUserBlocked = blockedUserRepository.existsByUserUserIdAndUserRoleName(instructorId, KeyConstants.KEY_INSTRUCTOR);
        if (isUserBlocked) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_INSTRUCTOR_BLOCKED, null);
        }
        log.info("Basic validations for member and instructor id : Time taken in millis : "+(System.currentTimeMillis() - startTime));
        boolean isPaginationPresent = false;

        if (pageNoOptional.isPresent() && pageSizeOptional.isPresent()) {
            isPaginationPresent = true;
            if (pageNoOptional.get() <= 0 || pageSizeOptional.get() <= 0) {
                throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ERROR, null);
            }
        }
        ResponseModel res = new ResponseModel();
        Map<String, Object> profileObj = new HashMap<>();
        long temp = System.currentTimeMillis();
        UserProfile userProfile = userProfileRepository.findByUser(instructor);
        profileObj.put(KeyConstants.KEY_USER_ID, instructorId);
        profileObj.put(KeyConstants.KEY_USER_FIRST_NAME, userProfile.getFirstName());
        profileObj.put(KeyConstants.KEY_USER_LAST_NAME, userProfile.getLastName());
        profileObj.put(KeyConstants.KEY_USER_BIO, userProfile.getBiography());
        profileObj.put(KeyConstants.KEY_PROFILE_IMAGE, userProfile.getProfileImage());
        profileObj.put(KeyConstants.KEY_COVER_IMAGE, userProfile.getCoverImage());
        String promoUrl = null;
        String promoThumbnailUrl = null;
        if ( userProfile.getPromotion() != null && userProfile.getPromotion().getVideoManagement() != null){
            promoUrl = userProfile.getPromotion().getVideoManagement().getUrl();
            promoThumbnailUrl = userProfile.getPromotion().getVideoManagement().getThumbnail().getImagePath();
        }

        profileObj.put(KeyConstants.KEY_PROMO_URL, promoUrl);
        profileObj.put(KeyConstants.KEY_PROMO_THUMBNAIL_URL, promoThumbnailUrl);
        UserRole userRole = userRoleRepository.findByName(SecurityFilterConstants.ROLE_INSTRUCTOR);
        boolean isActive = fitwiseUtils.isUserActive(userProfile.getUser(), userRole);
        profileObj.put(KeyConstants.KEY_ACTIVE, isActive);

        List<InstructorCertification> instructorCertificationList = certificateRepository.findByUser(instructor);
        profileObj.put(KeyConstants.KEY_CERTIFICATION, instructorCertificationList);
        List<InstructorAwards> instructorAwardsList = awardsRepository.findByUser(instructor);
        profileObj.put(KeyConstants.KEY_AWARDS, instructorAwardsList);
        log.info("Basic instructor details : Time taken in millis : "+(System.currentTimeMillis() - temp));
        temp = System.currentTimeMillis();
        List<ExperienceMemberView> instructorExperienceList = new ArrayList<>();
        List<InstructorProgramExperience> instructorProgramExperienceList = instructorExperienceRepository.findByUserUserId(instructor.getUserId());
        for (InstructorProgramExperience instructorProgramExperience : instructorProgramExperienceList) {
            ExperienceMemberView experienceMemberView = new ExperienceMemberView();
            experienceMemberView.setProgramType(instructorProgramExperience.getProgramType().getProgramTypeName());
            experienceMemberView.setNumberOfYears(instructorProgramExperience.getExperience().getNumberOfYears());
            instructorExperienceList.add(experienceMemberView);
        }
        profileObj.put(KeyConstants.KEY_INSTRUCTOR_EXPERIENCE, instructorExperienceList);
        log.info("Instructor experience details : Time taken in millis : "+(System.currentTimeMillis() - temp));
        temp = System.currentTimeMillis();
        List<OtherExpertise> otherExpertiseList = otherExpertiseRepository.findByUserUserId(instructor.getUserId());
        List<OtherExpertiseResponseView> otherExpertiseResponseViews = new ArrayList<>();
        for (OtherExpertise otherExpertise : otherExpertiseList) {
            OtherExpertiseResponseView otherExpertiseResponseView = new OtherExpertiseResponseView();
            otherExpertiseResponseView.setProgramType(otherExpertise.getExpertiseType());
            otherExpertiseResponseView.setNumberOfYears(otherExpertise.getExperience().getNumberOfYears());
            otherExpertiseResponseViews.add(otherExpertiseResponseView);
        }
        profileObj.put(KeyConstants.KEY_OTHER_EXPERTISE, otherExpertiseResponseViews);
        log.info("Other expertise details : Time taken in millis : "+(System.currentTimeMillis() - temp));
        long programCount;
        List<Programs> programs;
        temp = System.currentTimeMillis();
        Specification<Programs> programStatusSpec = ProgramSpecifications.getProgramByStatus(KeyConstants.KEY_PUBLISH);
        Specification<Programs> ownerSpec = ProgramSpecifications.getProgramByOwnerUserId(instructor.getUserId());
        Specification<Programs> stripeActiveSpec = ProgramSpecifications.getProgramByStripeMapping();
        Specification<Programs> finalSpec = programStatusSpec.and(ownerSpec).and(stripeActiveSpec);
        if (isPaginationPresent) {
            PageRequest pageRequest = PageRequest.of(pageNoOptional.get() -1, pageSizeOptional.get());
            Page<Programs> programsPage = programRepository.findAll(finalSpec, pageRequest);
            programs = programsPage.getContent();
            programCount = programsPage.getTotalElements();
        } else {
            programs = programRepository.findAll(finalSpec);
            programCount = programs.size();
        }
        log.info("Programs query : Time taken in millis : "+(System.currentTimeMillis() - temp));
        List<Long> programIdList = programs.stream().map(Programs::getProgramId).collect(Collectors.toList());
        temp = System.currentTimeMillis();
        Map<Long, Long> offerCountMap = discountsService.getNoOfCurrentAvailableOffersOfProgramsForMember(programIdList, member);
        log.info("Offer query : "+(System.currentTimeMillis() - temp));
        temp = System.currentTimeMillis();
        log.info("Construct program response : Time taken in millis : "+(System.currentTimeMillis() - temp));
        profileObj.put(KeyConstants.KEY_INSTRUCTOR_PROGRAMS, constructFreeProgramView(programs, offerCountMap));
        profileObj.put(KeyConstants.KEY_PROGRAM_COUNT, programCount);
        boolean isSubscribed = false;
        temp = System.currentTimeMillis();
        if (member != null) {
            isSubscribed = programSubscriptionRepo.existsByProgramOwnerUserIdAndUserUserId(instructorId, member.getUserId());
        }
        log.info("Member Subscription check : Time taken in millis : "+(System.currentTimeMillis() - temp));
        profileObj.put(KeyConstants.KEY_IS_SUBSCRIBED, isSubscribed);
        res.setPayload(profileObj);
        res.setStatus(Constants.SUCCESS_STATUS);
        res.setMessage(MessageConstants.MSG_USER_PROFILE_FETCHED);
        log.info("Get instructor profile for member : Total Time taken in millis : "+(System.currentTimeMillis() - startTime));
        log.info("Get instructor profile for member ends");
        return res;
    }

    public List<InstructorProfileProgramView> constructFreeProgramView(List<Programs> programs, Map<Long, Long> offerCountMap){
        List<InstructorProfileProgramView> programResponseModelList = new ArrayList<>();
        List<FreeAccessProgram> freeAccessPrograms = freeAccessService.getAllUserFreeProgramsList();
        List<Long> freeProgramIds = freeAccessService.getUserSpecificFreeAccessProgramsIds();
        for(FreeAccessProgram freeProduct : freeAccessPrograms){
            freeProgramIds.add(freeProduct.getProgram().getProgramId());
        }
        for (Programs program : programs) {
            InstructorProfileProgramView instructorProfileProgramView = constructProgramView(program, offerCountMap);
            if(freeProgramIds.contains(program.getProgramId())){
                instructorProfileProgramView.setFreeToAccess(true);
            }
            programResponseModelList.add(instructorProfileProgramView);
        }
        return programResponseModelList;
    }

    public InstructorProfileProgramView constructProgramView(Programs program, Map<Long, Long> offerCountMap) {
        InstructorProfileProgramView instructorProfileProgramView = new InstructorProfileProgramView();
        instructorProfileProgramView.setProgramId(program.getProgramId());
        instructorProfileProgramView.setTitle(program.getTitle());
        Date modifiedDate = program.getModifiedDate();
        instructorProfileProgramView.setCreatedDate(modifiedDate);
        instructorProfileProgramView.setCreatedDateFormatted(fitwiseUtils.formatDate(modifiedDate));
        if (program.getImage() != null) {
            instructorProfileProgramView.setImageId(program.getImage().getImageId());
            instructorProfileProgramView.setThumbnailUrl(program.getImage().getImagePath());
        }
        if (program.getProgramPrice() != null) {
            DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
            instructorProfileProgramView.setProgramPrice(decimalFormat.format(program.getProgramPrice()));
            instructorProfileProgramView.setFormattedProgramPrice(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(program.getProgramPrice()));
        }
        /** Discount offers **/
        long noOfCurrentAvailableOffers = offerCountMap.get(program.getProgramId());
        instructorProfileProgramView.setNumberOfCurrentAvailableOffers((int) noOfCurrentAvailableOffers);
        instructorProfileProgramView.setExpertiseLevel(program.getProgramExpertiseLevel().getExpertiseLevel());
        instructorProfileProgramView.setDuration(program.getDuration().getDuration());
        return instructorProfileProgramView;
    }

    /**
     * method to update ExpertiseAndGoals of a member
     *
     * @param expertiseAndGoalsModel
     */
    public void updateExpertiseAndGoals(ExpertiseAndGoalsModel expertiseAndGoalsModel) {
        User user = userComponents.getUser();
        List<UserProgramGoalsMapping> userProgramGoalsMappings = userProgramGoalsMappingRepository.findByUserUserId(user.getUserId());
        for (UserProgramGoalsMapping userProgramGoalsMapping : userProgramGoalsMappings) {
            userProgramGoalsMappingRepository.delete(userProgramGoalsMapping);
        }
        for (Long programExpertiseGoalMappingId : expertiseAndGoalsModel.getProgramExpertiseGoalMappingIds()) {
            UserProgramGoalsMapping userProgramGoalsMapping = new UserProgramGoalsMapping();
            ProgramExpertiseGoalsMapping programExpertiseGoalsMapping = programExpertiseGoalsMappingRepository.findByProgramExpertiseGoalsMappingId(programExpertiseGoalMappingId);
            userProgramGoalsMapping.setProgramExpertiseGoalsMapping(programExpertiseGoalsMapping);
            userProgramGoalsMapping.setUser(user);
            userProgramGoalsMappingRepository.save(userProgramGoalsMapping);
        }
    }
    
	public boolean checkIfInstructorHasFreeAccessPrograms(User instructor) {
        Long freeProgramCount = 0L;
		// Get free programs
		List<FreeAccessProgram> freeAccessPrograms = freeAccessService.getAllUserFreeProgramsList();
		List<Long> freeProgramIds = freeAccessService.getUserSpecificFreeAccessProgramsIds();
		for (FreeAccessProgram freeProduct : freeAccessPrograms) {
			freeProgramIds.add(freeProduct.getProgram().getProgramId());
		}
        if(!freeProgramIds.isEmpty()){
            // Get count if free programs id present in instructor programs
            Specification<Programs> programStatusSpec = ProgramSpecifications.getProgramByStatus(KeyConstants.KEY_PUBLISH);
            Specification<Programs> freeProgramIdInSpec = ProgramSpecifications.getProgramsInIdList(freeProgramIds);
            Specification<Programs> ownerSpec = ProgramSpecifications.getProgramByOwnerUserId(instructor.getUserId());
            Specification<Programs> finalSpec = programStatusSpec.and(freeProgramIdInSpec).and(ownerSpec);
            freeProgramCount = programRepository.count(finalSpec);
        }
		return freeProgramCount > 0;
	}
}