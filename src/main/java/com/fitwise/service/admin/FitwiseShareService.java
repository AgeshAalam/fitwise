package com.fitwise.service.admin;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.User;
import com.fitwise.entity.instructor.InstructorTierDetails;
import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.subscription.FitwiseSubscriptionShare;
import com.fitwise.entity.subscription.FitwiseSubscriptionShareForInstructor;
import com.fitwise.entity.subscription.SubscriptionType;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.UserRepository;
import com.fitwise.repository.instructor.InstructorTierDetailsRepository;
import com.fitwise.repository.subscription.FitwiseSubscriptionShareForInstructorRepository;
import com.fitwise.repository.subscription.FitwiseSubscriptionShareRepository;
import com.fitwise.repository.subscription.SubscriptionTypesRepo;
import com.fitwise.utils.FitwiseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FitwiseShareService {

    private final FitwiseSubscriptionShareRepository fitwiseSubscriptionShareRepository;
    private final FitwiseSubscriptionShareForInstructorRepository fitwiseSubscriptionShareForInstructorRepository;
    private final SubscriptionTypesRepo subscriptionTypesRepo;
    private final UserRepository userRepository;
    private final FitwiseUtils fitwiseUtils;

    /**
     * The instructor tier details repository.
     */
    @Autowired
    InstructorTierDetailsRepository instructorTierDetailsRepository;

    /**
     * Update global fitwise share
     * @param subscriptionTypeId Type of subscription
     * @param share Share in percentage
     */
    public void updateFitwiseShare(final Long subscriptionTypeId, final Long share) {
        SubscriptionType subscriptionType = subscriptionTypesRepo.findBySubscriptionTypeId(subscriptionTypeId);
        subscriptionTypeAndShareValidation(subscriptionType, share);
        List<FitwiseSubscriptionShare> fitwiseSubscriptionShares = fitwiseSubscriptionShareRepository.findBySubscriptionTypeAndActive(subscriptionType, true);
        if(!fitwiseSubscriptionShares.isEmpty()){
            boolean alreadyAvailable = false;
            for(FitwiseSubscriptionShare fitwiseSubscriptionShare : fitwiseSubscriptionShares){
                if(fitwiseSubscriptionShare.getShare() == share){
                    alreadyAvailable = true;
                }
                fitwiseSubscriptionShare.setActive(false);
            }
            if(alreadyAvailable){
                throw new ApplicationException(Constants.CONFLICT, MessageConstants.ERROR_SUBSCRIPTION_SHARE_ALREADY_EXISTS, null);
            }
            fitwiseSubscriptionShareRepository.saveAll(fitwiseSubscriptionShares);
        }
        FitwiseSubscriptionShare fitwiseSubscriptionShare = new FitwiseSubscriptionShare();
        fitwiseSubscriptionShare.setShare(share);
        fitwiseSubscriptionShare.setActive(true);
        fitwiseSubscriptionShare.setSubscriptionType(subscriptionType);
        fitwiseSubscriptionShareRepository.save(fitwiseSubscriptionShare);
    }

    /**
     * Update fitwise share for an instructor
     * @param subscriptionTypeId Type of subscription
     * @param email Instructor email
     * @param share Share in percentage
     */
    public void updateFitwiseShare(Long subscriptionTypeId, String email, Long share) {
        User user = userRepository.findByEmail(email);
        if(user == null){
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.ERROR_USER_NOT_FOUND, null);
        }
        if (!fitwiseUtils.isInstructor(user)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_INSTRUCTOR, MessageConstants.ERROR);
        }
        SubscriptionType subscriptionType = subscriptionTypesRepo.findBySubscriptionTypeId(subscriptionTypeId);
        subscriptionTypeAndShareValidation(subscriptionType, share);
        List<FitwiseSubscriptionShareForInstructor> fitwiseSubscriptionShareForInstructors = fitwiseSubscriptionShareForInstructorRepository.findBySubscriptionTypeAndUserAndActive(subscriptionType, user, true);
        if(!fitwiseSubscriptionShareForInstructors.isEmpty()){
            boolean alreadyAvailable = false;
            for(FitwiseSubscriptionShareForInstructor fitwiseSubscriptionShareForInstructor : fitwiseSubscriptionShareForInstructors){
                if(fitwiseSubscriptionShareForInstructor.getShare() == share){
                    alreadyAvailable = true;
                }
                fitwiseSubscriptionShareForInstructor.setActive(false);
            }
            if(alreadyAvailable){
                throw new ApplicationException(Constants.CONFLICT, MessageConstants.ERROR_SUBSCRIPTION_SHARE_ALREADY_EXISTS, null);
            }
            fitwiseSubscriptionShareForInstructorRepository.saveAll(fitwiseSubscriptionShareForInstructors);
        }
        FitwiseSubscriptionShareForInstructor fitwiseSubscriptionShareForInstructor = new FitwiseSubscriptionShareForInstructor();
        fitwiseSubscriptionShareForInstructor.setShare(share);
        fitwiseSubscriptionShareForInstructor.setActive(true);
        fitwiseSubscriptionShareForInstructor.setSubscriptionType(subscriptionType);
        fitwiseSubscriptionShareForInstructor.setUser(user);
        fitwiseSubscriptionShareForInstructorRepository.save(fitwiseSubscriptionShareForInstructor);
    }

    /**
     * Validation on share and subscription type
     * @param subscriptionType Type of subscription
     * @param share Share in percentage
     */
    private void subscriptionTypeAndShareValidation(SubscriptionType subscriptionType, Long share){
        if(subscriptionType == null){
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.ERROR_SUBSCRIPTION_TYPE_INVALID, null);
        }
        if(share == null || share <= 0){
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.ERROR_SUBSCRIPTION_SHARE_INVALID, null);
        }
    }

    /**
     * Get the fitwise share for an instructor or global
     * @param user Instructor
     * @param subscriptionTypeString Type of the subscription
     * @return Fitwise share percentage
     */
    public Double getFitwiseShare(final User user, final String subscriptionTypeString, final OrderManagement orderManagement){
        Double fitwiseShare = 0.0;
        SubscriptionType subscriptionType = subscriptionTypesRepo.findByNameIgnoreCase(subscriptionTypeString);
        InstructorTierDetails instructorTierDetails = instructorTierDetailsRepository.findByUserAndActive(user, true);
        if(instructorTierDetails != null) {
        	log.info("Coming inside instructor tier details");
        	if(subscriptionTypeString.equalsIgnoreCase("program")) {
        		fitwiseShare = instructorTierDetails.getTier().getTierTypeDetails().getProgramsFees();
        	} else {
        		int programCount = (orderManagement.getSubscriptionPackage().getPackageProgramMapping() != null) ? orderManagement.getSubscriptionPackage().getPackageProgramMapping().size() : 0;
        		int sessionCount = (orderManagement.getSubscriptionPackage().getPackageKloudlessMapping() != null && !orderManagement.getSubscriptionPackage().getPackageKloudlessMapping().isEmpty()) ? orderManagement.getSubscriptionPackage().getPackageKloudlessMapping().get(0).getTotalSessionCount() : 0;
        		if(programCount == 0 && sessionCount > 0) {
        			fitwiseShare = instructorTierDetails.getTier().getTierTypeDetails().getServicesPackagesFees();
        		} else {
        			fitwiseShare = instructorTierDetails.getTier().getTierTypeDetails().getPackagesFees();
        		}
        	}
        } else {
        	log.info("Not Coming inside instructor tier details");
        	List<FitwiseSubscriptionShareForInstructor> fitwiseSubscriptionShareForInstructor = fitwiseSubscriptionShareForInstructorRepository.findBySubscriptionTypeAndUserAndActive(subscriptionType, user, true);
            if(!fitwiseSubscriptionShareForInstructor.isEmpty()){
                fitwiseShare = fitwiseSubscriptionShareForInstructor.get(0).getShare().doubleValue();
            }else {
                List<FitwiseSubscriptionShare> fitwiseSubscriptionShares = fitwiseSubscriptionShareRepository.findBySubscriptionTypeAndActive(subscriptionType, true);
                if(!fitwiseSubscriptionShares.isEmpty()){
                    fitwiseShare = fitwiseSubscriptionShares.get(0).getShare().doubleValue();
               }
            } 
        }
        log.info("fitwise share -------------> {}", fitwiseShare);
        return fitwiseShare;
    }
}