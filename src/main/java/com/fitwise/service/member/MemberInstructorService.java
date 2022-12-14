package com.fitwise.service.member;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SecurityFilterConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.ExpertiseLevels;
import com.fitwise.entity.InstructorAwards;
import com.fitwise.entity.InstructorCertification;
import com.fitwise.entity.InstructorProgramExperience;
import com.fitwise.entity.OtherExpertise;
import com.fitwise.entity.ProgramSubTypes;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.UserRole;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.AwardsRepository;
import com.fitwise.repository.BlockedUserRepository;
import com.fitwise.repository.CertificateRepository;
import com.fitwise.repository.ExpertiseLevelRepository;
import com.fitwise.repository.InstructorExperienceRepository;
import com.fitwise.repository.OtherExpertiseRepository;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.UserRoleRepository;
import com.fitwise.repository.subscription.ProgramSubscriptionRepo;
import com.fitwise.service.discountservice.DiscountsService;
import com.fitwise.service.v2.instructor.UserLinkService;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.specifications.ProgramSpecifications;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.RequestParamValidator;
import com.fitwise.view.ExperienceMemberView;
import com.fitwise.view.OtherExpertiseResponseView;
import com.fitwise.view.ResponseModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberInstructorService {

    private final ValidationService validationService;
    private final BlockedUserRepository blockedUserRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRoleRepository userRoleRepository;
    private final FitwiseUtils fitwiseUtils;
    private final CertificateRepository certificateRepository;
    private final AwardsRepository awardsRepository;
    private final InstructorExperienceRepository instructorExperienceRepository;
    private final OtherExpertiseRepository otherExpertiseRepository;
    private final UserComponents userComponents;
    private final ProgramRepository programRepository;
    private final DiscountsService discountsService;
    private final MemberService memberService;
    private final ProgramSubscriptionRepo programSubscriptionRepo;
    private final UserLinkService userLinkService;
    private final ExpertiseLevelRepository expertiseLevelRepository;

    /**
     * Get the instructor details
     * @param instructorId ID to get specific instructor
     * @return Instructor details
     * @throws NoSuchAlgorithmException Invalid algorithm
     * @throws KeyStoreException Keystore failure
     * @throws KeyManagementException Key fetch failure
     */
    public ResponseModel getInstructor(Long instructorId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        User instructor = validationService.validateInstructorId(instructorId);
        boolean isUserBlocked = blockedUserRepository.existsByUserUserIdAndUserRoleName(instructorId, KeyConstants.KEY_INSTRUCTOR);
        if (isUserBlocked) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_INSTRUCTOR_BLOCKED, null);
        }
        ResponseModel res = new ResponseModel();
        Map<String, Object> profileObj = new HashMap<>();
        UserProfile userProfile = userProfileRepository.findByUser(instructor);
        profileObj.put(KeyConstants.KEY_USER_ID, instructorId);
        profileObj.put(KeyConstants.KEY_USER_FIRST_NAME, userProfile.getFirstName());
        profileObj.put(KeyConstants.KEY_USER_LAST_NAME, userProfile.getLastName());
        profileObj.put(KeyConstants.KEY_USER_BIO, userProfile.getBiography());
        profileObj.put(KeyConstants.KEY_PROFILE_IMAGE, userProfile.getProfileImage());
        profileObj.put(KeyConstants.KEY_COVER_IMAGE, userProfile.getCoverImage());
        profileObj.put(KeyConstants.KEY_PROFILE_LOCATION, userProfile.getLocation());
        profileObj.put(KeyConstants.KEY_PROFILE_GYM, userProfile.getGym());
        UserRole userRole = userRoleRepository.findByName(SecurityFilterConstants.ROLE_INSTRUCTOR);
        boolean isActive = fitwiseUtils.isUserActive(userProfile.getUser(), userRole);
        profileObj.put(KeyConstants.KEY_ACTIVE, isActive);
        List<InstructorCertification> instructorCertificationList = certificateRepository.findByUser(instructor);
        profileObj.put(KeyConstants.KEY_CERTIFICATION, instructorCertificationList);
        List<InstructorAwards> instructorAwardsList = awardsRepository.findByUser(instructor);
        profileObj.put(KeyConstants.KEY_AWARDS, instructorAwardsList);
        List<ExperienceMemberView> instructorExperienceList = new ArrayList<>();
        List<InstructorProgramExperience> instructorProgramExperienceList = instructorExperienceRepository.findByUserUserId(instructor.getUserId());
		for (InstructorProgramExperience instructorProgramExperience : instructorProgramExperienceList) {
			List<ProgramSubTypes> programSubTypesList = instructorProgramExperience.getProgramType().getProgramSubType();
			String programSubTypeName = "";
			StringJoiner joiner = new StringJoiner(", ");
			if (programSubTypesList != null && !programSubTypesList.isEmpty()) {
				List<Programs> subTypeList = programRepository
						.findByOwnerAndProgramSubTypeIsNotNull(instructor.getUserId());
				for (Programs programSubTypes : subTypeList) {
					if (programSubTypes.getProgramSubType() != null)
						joiner.add(programSubTypes.getProgramSubType().getName());

				}
				programSubTypeName = joiner.toString();
			}
			ExperienceMemberView experienceMemberView = new ExperienceMemberView();
            String programType = (programSubTypeName.isEmpty() ? instructorProgramExperience.getProgramType().getProgramTypeName() : programSubTypeName);
            experienceMemberView.setProgramType(programType);
            experienceMemberView.setNumberOfYears(instructorProgramExperience.getExperience().getNumberOfYears());
            instructorExperienceList.add(experienceMemberView);
        }
        profileObj.put(KeyConstants.KEY_INSTRUCTOR_EXPERIENCE, instructorExperienceList);
        List<OtherExpertise> otherExpertiseList = otherExpertiseRepository.findByUserUserId(instructor.getUserId());
        List<OtherExpertiseResponseView> otherExpertiseResponseViews = new ArrayList<>();
        for (OtherExpertise otherExpertise : otherExpertiseList) {
            OtherExpertiseResponseView otherExpertiseResponseView = new OtherExpertiseResponseView();
            otherExpertiseResponseView.setProgramType(otherExpertise.getExpertiseType());
            otherExpertiseResponseView.setNumberOfYears(otherExpertise.getExperience().getNumberOfYears());
            otherExpertiseResponseViews.add(otherExpertiseResponseView);
        }
        profileObj.put(KeyConstants.KEY_OTHER_EXPERTISE, otherExpertiseResponseViews);
        profileObj.put(KeyConstants.SOCIAL_LINKS, userLinkService.getSocialLinks(instructor));
        profileObj.put(KeyConstants.EXTERNAL_LINKS, userLinkService.getExternalLinks(instructor));
        String promoUrl = null;
        String promoThumbnailUrl = null;
        if ( userProfile.getPromotion() != null && userProfile.getPromotion().getVideoManagement() != null && userProfile.getPromotion().getVideoManagement().getUploadStatus().equalsIgnoreCase(KeyConstants.KEY_COMPLETED)){
            promoUrl = userProfile.getPromotion().getVideoManagement().getUrl();
            promoThumbnailUrl = userProfile.getPromotion().getVideoManagement().getThumbnail().getImagePath();
        }
        profileObj.put(KeyConstants.KEY_PROMO_URL, promoUrl);
        profileObj.put(KeyConstants.KEY_PROMO_THUMBNAIL_URL, promoThumbnailUrl);
        res.setPayload(profileObj);
        res.setStatus(Constants.SUCCESS_STATUS);
        res.setMessage(MessageConstants.MSG_USER_PROFILE_FETCHED);
        return res;
    }

    /**
     * Get all the published programs for the requested instructor
     * @param instructorId ID to get specific instructor
     * @param pageNo Page number
     * @param pageSize Page Size
     * @param search title search
     * @param expertiseLevelId expertise level filter
     * @return List of programs
     * @throws NoSuchAlgorithmException Invalid algorithm
     * @throws KeyStoreException Key failure
     * @throws KeyManagementException Key fetch failure
     */
    public ResponseModel getPrograms(Long instructorId, int pageNo, int pageSize, Optional<String> search, Optional<Long> expertiseLevelId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        User member = userComponents.getAndValidateUser();
        User instructor = validationService.validateInstructorId(instructorId);
        boolean isUserBlocked = blockedUserRepository.existsByUserUserIdAndUserRoleName(instructorId, KeyConstants.KEY_INSTRUCTOR);
        if (isUserBlocked) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_INSTRUCTOR_BLOCKED, null);
        }
        RequestParamValidator.pageSetup(pageNo, pageSize);
        ResponseModel res = new ResponseModel();
        Map<String, Object> profileObj = new HashMap<>();
        long programCount;
        List<Programs> programs;
        Specification<Programs> programStatusSpec = ProgramSpecifications.getProgramByStatus(KeyConstants.KEY_PUBLISH);
        Specification<Programs> ownerSpec = ProgramSpecifications.getProgramByOwnerUserId(instructor.getUserId());
        Specification<Programs> stripeActiveSpec = ProgramSpecifications.getProgramByStripeMapping();
        Specification<Programs> finalSpec = programStatusSpec.and(ownerSpec).and(stripeActiveSpec);
        // Program Title search
        if (search.isPresent() && !search.get().isEmpty()) {
            Specification<Programs> titleSearchSpec = ProgramSpecifications.getProgramByTitleContains(search.get());
            finalSpec = finalSpec.and(titleSearchSpec);
        }
        // Expertise level filter
        if (expertiseLevelId.isPresent() && expertiseLevelId.get() != 0) {
            ExpertiseLevels expertiseLevels = expertiseLevelRepository.findByExpertiseLevelId(expertiseLevelId.get());
            if (expertiseLevels == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXPERTISE_LEVEL_ID_INVALID, null);
            }
            if (!expertiseLevels.getExpertiseLevel().equalsIgnoreCase(KeyConstants.KEY_ALL_LEVELS)) {
                Specification<Programs> expertiseLevelSpec = ProgramSpecifications.getProgramByExpertiseLevelId(expertiseLevelId.get());
                finalSpec = finalSpec.and(expertiseLevelSpec);
            }
        }
        PageRequest pageRequest = PageRequest.of(pageNo -1, pageSize);
        Page<Programs> programsPage = programRepository.findAll(finalSpec, pageRequest);
        programs = programsPage.getContent();
        programCount = programsPage.getTotalElements();
        List<Long> programIdList = programs.stream().map(Programs::getProgramId).collect(Collectors.toList());
        Map<Long, Long> offerCountMap = discountsService.getNoOfCurrentAvailableOffersOfProgramsForMember(programIdList, member);
        profileObj.put(KeyConstants.KEY_INSTRUCTOR_PROGRAMS, memberService.constructFreeProgramView(programs, offerCountMap));
        profileObj.put(KeyConstants.KEY_PROGRAM_COUNT, programCount);
        boolean isSubscribed = false;
        if (member != null) {
            isSubscribed = programSubscriptionRepo.existsByProgramOwnerUserIdAndUserUserId(instructorId, member.getUserId());
        }
        boolean instructorHasFreePrograms = memberService.checkIfInstructorHasFreeAccessPrograms(instructor);
        profileObj.put(KeyConstants.KEY_FREE_TO_ACCESS, instructorHasFreePrograms);
        profileObj.put(KeyConstants.KEY_IS_SUBSCRIBED, isSubscribed);
        res.setPayload(profileObj);
        res.setStatus(Constants.SUCCESS_STATUS);
        res.setMessage(MessageConstants.MSG_USER_PROFILE_FETCHED);
        return res;
    }
}