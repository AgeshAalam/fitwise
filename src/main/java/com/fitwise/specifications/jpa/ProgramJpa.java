package com.fitwise.specifications.jpa;

import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.discounts.DiscountsConstants;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.WorkoutCompletion;
import com.fitwise.entity.discounts.DiscountOfferMapping;
import com.fitwise.entity.payments.appleiap.ApplePayment;
import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.subscription.ProgramSubscription;
import com.fitwise.properties.StripeProperties;
import com.fitwise.specifications.jpa.dao.OfferCountDao;
import com.fitwise.specifications.jpa.dao.ProgramOfferDao;
import com.fitwise.specifications.jpa.dao.WorkoutCompletionDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ProgramJpa {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private StripeProperties stripeProperties;

    public Map<Long, Long> getNumberOfCurrentOffersForMemberPrograms(List<Long> programIds, User member) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProgramOfferDao> criteriaQuery = criteriaBuilder.createQuery(ProgramOfferDao.class);

        Root<Programs> programsRoot = criteriaQuery.from(Programs.class);
        Predicate idInPredicate = programsRoot.get("programId").in(programIds);
        Date date = new Date();

        //New offer count sub query
        Subquery programNewOfferSubquery = criteriaQuery.subquery(DiscountOfferMapping.class);
        Root<DiscountOfferMapping> programNewOfferMappingRoot = programNewOfferSubquery.from(DiscountOfferMapping.class);
        Predicate programPredicateNew = criteriaBuilder.equal(programNewOfferMappingRoot.get("programs").get("programId"),
                programsRoot.get("programId"));
        Predicate isInUsePredicate = criteriaBuilder.isTrue(programNewOfferMappingRoot.get("offerCodeDetail").get("isInUse"));
        Predicate offerStatusPredicate = criteriaBuilder.like(programNewOfferMappingRoot.get("offerCodeDetail").get("offerStatus"), DiscountsConstants.OFFER_ACTIVE);
        Predicate startDatePredicate = criteriaBuilder.lessThanOrEqualTo(programNewOfferMappingRoot.get("offerCodeDetail").get("offerStartingDate"), date);
        Predicate endDatePredicate = criteriaBuilder.greaterThanOrEqualTo(programNewOfferMappingRoot.get("offerCodeDetail").get("offerEndingDate"), date);
        Predicate isNewPredicate = criteriaBuilder.isTrue(programNewOfferMappingRoot.get("offerCodeDetail").get("isNewUser"));
        programNewOfferSubquery.where(criteriaBuilder.and(programPredicateNew, isInUsePredicate, offerStatusPredicate,
                startDatePredicate, endDatePredicate, isNewPredicate));
        programNewOfferSubquery.select(criteriaBuilder.count(programNewOfferMappingRoot));


        //Existing offer count sub query
        Subquery programExistingOfferSubquery = criteriaQuery.subquery(DiscountOfferMapping.class);
        Root<DiscountOfferMapping> programExistingOfferMappingRoot = programExistingOfferSubquery.from(DiscountOfferMapping.class);
        Predicate programPredicateExisting = criteriaBuilder.equal(programExistingOfferMappingRoot.get("programs").get("programId"),
                programsRoot.get("programId"));
        Predicate isInUsePredicateForExisting = criteriaBuilder.isTrue(programExistingOfferMappingRoot.get("offerCodeDetail").get("isInUse"));
        Predicate offerStatusPredicateForExisting = criteriaBuilder.like(programExistingOfferMappingRoot.get("offerCodeDetail").get("offerStatus"), DiscountsConstants.OFFER_ACTIVE);
        Predicate startDatePredicateForExisting = criteriaBuilder.lessThanOrEqualTo(programExistingOfferMappingRoot.get("offerCodeDetail").get("offerStartingDate"), date);
        Predicate endDatePredicateForExisting = criteriaBuilder.greaterThanOrEqualTo(programExistingOfferMappingRoot.get("offerCodeDetail").get("offerEndingDate"), date);
        Predicate isExistingPredicate = criteriaBuilder.isFalse(programExistingOfferMappingRoot.get("offerCodeDetail").get("isNewUser"));
        programExistingOfferSubquery.where(criteriaBuilder.and(programPredicateExisting, isInUsePredicateForExisting, offerStatusPredicateForExisting,
                startDatePredicateForExisting, endDatePredicateForExisting, isExistingPredicate));
        programExistingOfferSubquery.select(criteriaBuilder.count(programExistingOfferMappingRoot));

        Subquery programSubscriptionSubquery = criteriaQuery.subquery(ProgramSubscription.class);
        Root<ProgramSubscription> programSubscriptionRoot = programSubscriptionSubquery.from(ProgramSubscription.class);

        Subquery programSubscriptionPlatformSubquery = criteriaQuery.subquery(ProgramSubscription.class);
        Root<ProgramSubscription> programSubscriptionPlatformRoot = programSubscriptionPlatformSubquery.from(ProgramSubscription.class);

        Subquery programSubscriptionStatusSubquery = criteriaQuery.subquery(ProgramSubscription.class);
        Root<ProgramSubscription> programSubscriptionStatusRoot = programSubscriptionStatusSubquery.from(ProgramSubscription.class);

        Subquery applePaymentSubquery = criteriaQuery.subquery(ApplePayment.class);
        Root<ApplePayment> applePaymentRoot = applePaymentSubquery.from(ApplePayment.class);


        if (member != null) {

            //Criteria to check member has history of subscription
            Predicate programIdPredicate = criteriaBuilder.equal(programSubscriptionRoot.get("program").get("programId"),
                    programsRoot.get("programId"));
            Predicate memberIdPredicate = criteriaBuilder.equal(programSubscriptionRoot.get("user").get("userId"), member.getUserId());
            programSubscriptionSubquery.where(criteriaBuilder.and(programIdPredicate, memberIdPredicate));
            programSubscriptionSubquery.select(criteriaBuilder.count(programSubscriptionRoot));

            //Criteria to check subscription platform
            Predicate programIdPredicateForPlatform = criteriaBuilder.equal(programSubscriptionPlatformRoot.get("program").get("programId"),
                    programsRoot.get("programId"));
            Predicate memberIdPredicatePlatform = criteriaBuilder.equal(programSubscriptionPlatformRoot.get("user").get("userId"), member.getUserId());
            programSubscriptionPlatformSubquery.where(criteriaBuilder.and(programIdPredicateForPlatform, memberIdPredicatePlatform));
            programSubscriptionPlatformSubquery.select(programSubscriptionPlatformRoot.get("subscribedViaPlatform").get("platformTypeId"));
            programSubscriptionPlatformSubquery.groupBy(programSubscriptionPlatformRoot.get("program").get("programId"),
            programsRoot.get("programId"));

            //Criteria to check subscription status
            Predicate programPredicate = criteriaBuilder.equal(programSubscriptionStatusRoot.get("program").get("programId"),
                    programsRoot.get("programId"));
            Predicate memberPredicate = criteriaBuilder.equal(programSubscriptionStatusRoot.get("user").get("userId"), member.getUserId());
            List<String> statusList = Arrays.asList(new String[]{KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING});
            Predicate statusPredicate = programSubscriptionStatusRoot.get("subscriptionStatus").get("subscriptionStatusName").in(statusList);
            int bufferMinutes = stripeProperties.getSubscriptionExpiryBufferMinutes();
            LocalDateTime now = LocalDateTime.now();
            now = now.minusMinutes(bufferMinutes);
            Timestamp currentDateWithBuffer = new Timestamp(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()).getTime());
            Expression<Date> subscribedDate = programSubscriptionStatusRoot.get("subscribedDate");
            Expression<Integer> days = programSubscriptionStatusRoot.get("subscriptionPlan").get("duration");
            Expression<Timestamp> newDate = criteriaBuilder.function("ADDDATE", Timestamp.class, subscribedDate, days);
            Predicate durationPredicate = criteriaBuilder.greaterThanOrEqualTo(newDate, currentDateWithBuffer);
            programSubscriptionStatusSubquery.where(criteriaBuilder.and(programPredicate, memberPredicate));
            Predicate platformIdPredicate = criteriaBuilder.not(criteriaBuilder.equal(programSubscriptionStatusRoot.get("subscribedViaPlatform").get("platformTypeId"), 2L));
            Predicate trailPredicate = criteriaBuilder.like(programSubscriptionStatusRoot.get("subscriptionStatus").get("subscriptionStatusName"), KeyConstants.KEY_TRIAL);
            Expression<Object> status = criteriaBuilder.selectCase()
                    .when(trailPredicate, "Trial")
                    .when(criteriaBuilder.and(platformIdPredicate, statusPredicate), criteriaBuilder.selectCase().when(durationPredicate, "Paid").otherwise("Expired"))
                    .otherwise("notApplicable");
            programSubscriptionStatusSubquery.select(status);
            programSubscriptionStatusSubquery.groupBy(programSubscriptionStatusRoot.get("program").get("programId"),
                    programsRoot.get("programId"));

            //criteria to check subscription status in case of ios platform
            Subquery appleInnerSubquery = applePaymentSubquery.subquery(ApplePayment.class);
            Root<ApplePayment> applePaymentInnerRoot = appleInnerSubquery.from(ApplePayment.class);


            Subquery orderInnerSubquery = appleInnerSubquery.subquery(OrderManagement.class);
            Root<OrderManagement> orderManagementInnerRoot = orderInnerSubquery.from(OrderManagement.class);
            Predicate programPredicateForOrder = criteriaBuilder.equal(orderManagementInnerRoot.get("program").get("programId"), programsRoot.get("programId"));
            Predicate memberPredicateForOrder = criteriaBuilder.equal(orderManagementInnerRoot.get("user").get("userId"), member.getUserId());
            Predicate platformPredicate = criteriaBuilder.equal(orderManagementInnerRoot.get("subscribedViaPlatform").get("platformTypeId"), 2L);
            orderInnerSubquery.where(criteriaBuilder.and(programPredicateForOrder, memberPredicateForOrder, platformPredicate));
            orderInnerSubquery.select(criteriaBuilder.max(orderManagementInnerRoot.get("id")));


            Predicate orderPredicate = criteriaBuilder.equal(applePaymentInnerRoot.get("orderManagement").get("id"), orderInnerSubquery.getSelection());
            Predicate check = criteriaBuilder.exists(orderInnerSubquery);
            appleInnerSubquery.where(criteriaBuilder.and(check, orderPredicate));
            appleInnerSubquery.select(criteriaBuilder.max(applePaymentInnerRoot.get("id")));

            Predicate idPredicate = criteriaBuilder.equal(applePaymentRoot.get("id"), appleInnerSubquery.getSelection());
            Predicate checkPredicate = criteriaBuilder.exists(appleInnerSubquery);

            Predicate startDateCriteria = criteriaBuilder.greaterThanOrEqualTo(applePaymentRoot.get("expiryDate"), date);
            applePaymentSubquery.where(criteriaBuilder.and(idPredicate, checkPredicate));
            Expression<Object> subStatus = criteriaBuilder.selectCase()
                    .when(startDateCriteria, "Paid")
                    .when(criteriaBuilder.not(startDateCriteria), "Expired")
                    .otherwise("notApplicable");
            applePaymentSubquery.select(subStatus);


        }
        criteriaQuery.where(idInPredicate);
        Expression<Long> subscriptionCount;
        Expression<String> stripeSubscriptionStatus;
        Expression<String> iosSubscriptionStatus;
        Expression<Long> platformTypeId;

        if (member == null) {
            subscriptionCount = criteriaBuilder.literal(0L);
            platformTypeId = criteriaBuilder.literal(0L);
            stripeSubscriptionStatus = criteriaBuilder.literal("notApplicable");
            iosSubscriptionStatus = criteriaBuilder.literal("notApplicable");

        } else {
            subscriptionCount = programSubscriptionSubquery.getSelection();
            platformTypeId = programSubscriptionPlatformSubquery.getSelection();
            stripeSubscriptionStatus = programSubscriptionStatusSubquery.getSelection();
            iosSubscriptionStatus = applePaymentSubquery.getSelection();

        }
        Expression<Long> programId = programsRoot.get("programId");
        Expression<Long> newOfferCount = programNewOfferSubquery.getSelection();
        Expression<Long> existingOfferCount = programExistingOfferSubquery.getSelection();
        criteriaQuery.multiselect(programId, subscriptionCount, platformTypeId, stripeSubscriptionStatus, iosSubscriptionStatus, newOfferCount, existingOfferCount);
		List<ProgramOfferDao> offerDaoList = new ArrayList<>();
		try {
			offerDaoList = entityManager.createQuery(criteriaQuery).getResultList();
		} catch (Exception exception) {
			log.error(exception.getMessage());
			exception.printStackTrace();
		}
        Map<Long, Long> offerCountMap = new HashMap<>();
        for (ProgramOfferDao offerDao : offerDaoList) {
            long offerCount = 0;
            if (member == null || offerDao.getSubscriptionCount() == 0 || offerDao.getStripeSubscriptionStatus().equalsIgnoreCase(KeyConstants.KEY_TRIAL)) {
                offerCount = offerDao.getNewOfferCount();
            } else {
                if (offerDao.getPlatformTypeId() != 2) {
                    if (offerDao.getStripeSubscriptionStatus().equals(KeyConstants.KEY_PAID)) {
                        offerCount = 0;
                    } else if (offerDao.getStripeSubscriptionStatus().equals(KeyConstants.KEY_EXPIRED)) {
                        offerCount = offerDao.getExistingOfferCount();
                    } else {
                        offerCount = offerDao.getNewOfferCount();
                    }
                } else {
                    if (offerDao.getIosSubscriptionStatus().equals(KeyConstants.KEY_PAID)) {
                        offerCount = 0;
                    } else if (offerDao.getIosSubscriptionStatus().equals(KeyConstants.KEY_EXPIRED)) {
                        offerCount = offerDao.getExistingOfferCount();
                    }
                }

            }
            offerCountMap.put(offerDao.getProgramId(), offerCount);
        }

        return offerCountMap;
    }

    /**
     * Get current offer count for instructor
     * @param programIds
     * @return
     */
    public Map<Long, Long> getNumberOfCurrentAvailableOffersOfProgramsForInstructor(List<Long> programIds){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<OfferCountDao> criteriaQuery = criteriaBuilder.createQuery(OfferCountDao.class);
        Root<Programs> programsRoot = criteriaQuery.from(Programs.class);
        Predicate programIdInPredicate = programsRoot.get("programId").in(programIds);

        Subquery subquery = criteriaQuery.subquery(DiscountOfferMapping.class);
        Root<DiscountOfferMapping> discountOfferMappingRoot = subquery.from(DiscountOfferMapping.class);
        Date date = new Date();
        Predicate idPredicate = criteriaBuilder.equal(discountOfferMappingRoot.get("programs").get("programId"), programsRoot.get("programId"));
        Predicate isInUsePredicate = criteriaBuilder.isTrue(discountOfferMappingRoot.get("offerCodeDetail").get("isInUse"));
        Predicate offerStatusPredicate = criteriaBuilder.like(discountOfferMappingRoot.get("offerCodeDetail").get("offerStatus"), DiscountsConstants.OFFER_ACTIVE);
        Predicate startDatePredicate = criteriaBuilder.lessThanOrEqualTo(discountOfferMappingRoot.get("offerCodeDetail").get("offerStartingDate"), date);
        Predicate endDatePredicate = criteriaBuilder.greaterThanOrEqualTo(discountOfferMappingRoot.get("offerCodeDetail").get("offerEndingDate"), date);
        subquery.where(criteriaBuilder.and(startDatePredicate, endDatePredicate, idPredicate, isInUsePredicate, offerStatusPredicate));
        subquery.select(criteriaBuilder.count(discountOfferMappingRoot));

        criteriaQuery.where(programIdInPredicate);
        Expression<Long> programId = programsRoot.get("programId");
        Expression<Long> offerCount = subquery.getSelection();
        criteriaQuery.multiselect(programId, offerCount);
        List<OfferCountDao> offerCountDaos =   entityManager.createQuery(criteriaQuery).getResultList();
        Map<Long, Long> offerCountDaoMap = offerCountDaos.stream()
                .collect(Collectors.toMap(offerCountDao -> offerCountDao.getId(), offerCountDao -> offerCountDao.getOfferCount()));
        return offerCountDaoMap;
    }


    /**
     * Get workout completion dao for member
     * @param programIdList
     * @param memberId
     * @return List<WorkoutCompletionDAO>
     */
    public List<WorkoutCompletionDAO> getWorkoutCompletionListByProgramIdList(List<Long> programIdList, Long memberId){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<WorkoutCompletionDAO> criteriaQuery = criteriaBuilder.createQuery(WorkoutCompletionDAO.class);
        Root<Programs> programsRoot = criteriaQuery.from(Programs.class);

        //Sub query to get first workout completion id
        Subquery<Long> subQuery1 = criteriaQuery.subquery(Long.class);
        Root<WorkoutCompletion> workoutCompletionRoot1 = subQuery1.from(WorkoutCompletion.class);

        Predicate programIdEqualPredicate = criteriaBuilder.equal(workoutCompletionRoot1.get("program").get("programId"), programsRoot.get("programId"));
        Predicate memberIdEqualPredicate = criteriaBuilder.equal(workoutCompletionRoot1.get("member").get("userId"), memberId);

        Predicate finalPredicateForSubQuery1 = criteriaBuilder.and(programIdEqualPredicate, memberIdEqualPredicate);

        subQuery1.where(finalPredicateForSubQuery1);
        subQuery1.select(criteriaBuilder.min(workoutCompletionRoot1.get("workoutCompletionId")));

        //Sub query to get first workout completion date
        Subquery<Date> subQuery2 = criteriaQuery.subquery(Date.class);
        Root<WorkoutCompletion> workoutCompletionRoot2 = subQuery2.from(WorkoutCompletion.class);

        Predicate workoutCompletionIdEqualPredicateForMin = criteriaBuilder.equal(workoutCompletionRoot2.get("workoutCompletionId"), subQuery1.getSelection());

        subQuery2.where(workoutCompletionIdEqualPredicateForMin);
        subQuery2.select(workoutCompletionRoot2.get("completedDate"));

        //Sub query to get last workout completion id
        Subquery<Long> subQuery3 = criteriaQuery.subquery(Long.class);
        Root<WorkoutCompletion> workoutCompletionRoot3 = subQuery3.from(WorkoutCompletion.class);

        Predicate programIdEqualPredicateForMax = criteriaBuilder.equal(workoutCompletionRoot3.get("program").get("programId"), programsRoot.get("programId"));
        Predicate memberIdEqualPredicateForMax = criteriaBuilder.equal(workoutCompletionRoot3.get("member").get("userId"), memberId);

        Predicate finalPredicateForSubQuery2 = criteriaBuilder.and(programIdEqualPredicateForMax, memberIdEqualPredicateForMax);

        subQuery3.where(finalPredicateForSubQuery2);
        subQuery3.select(criteriaBuilder.max(workoutCompletionRoot3.get("workoutCompletionId")));

        //Sub query to get first workout completion date
        Subquery<Date> subQuery4 = criteriaQuery.subquery(Date.class);
        Root<WorkoutCompletion> workoutCompletionRoot4 = subQuery4.from(WorkoutCompletion.class);

        Predicate workoutCompletionIdEqualPredicateForMax = criteriaBuilder.equal(workoutCompletionRoot4.get("workoutCompletionId"), subQuery3.getSelection());

        subQuery4.where(workoutCompletionIdEqualPredicateForMax);
        subQuery4.select(workoutCompletionRoot4.get("completedDate"));

        //Sub query 5 to get workout completion count
        Subquery<Long> subQuery5 = criteriaQuery.subquery(Long.class);
        Root<WorkoutCompletion> workoutCompletionRoot5 = subQuery5.from(WorkoutCompletion.class);

        Predicate programIdEqualPredicateForCount = criteriaBuilder.equal(workoutCompletionRoot5.get("program").get("programId"), programsRoot.get("programId"));
        Predicate memberIdEqualPredicateForCount = criteriaBuilder.equal(workoutCompletionRoot5.get("member").get("userId"), memberId);

        Predicate finalPredicateForSubQuery3 = criteriaBuilder.and(programIdEqualPredicateForCount, memberIdEqualPredicateForCount);

        subQuery5.where(finalPredicateForSubQuery3);
        subQuery5.select(criteriaBuilder.count(workoutCompletionRoot5));

        //Criteria query
        Predicate programIdListInPredicate = programsRoot.get("programId").in(programIdList);

        //Result set expression
        Expression<Long> programIdExpression = programsRoot.get("programId");
        Expression<Long> workoutCompletionCountExpression = subQuery5.getSelection();
        Expression<Date> firstWCCreatedDate = subQuery2.getSelection();
        Expression<Date> lastWCCreatedDate = subQuery4.getSelection();

        criteriaQuery.where(programIdListInPredicate);
        criteriaQuery.multiselect(programIdExpression, workoutCompletionCountExpression, firstWCCreatedDate, lastWCCreatedDate);

        return entityManager.createQuery(criteriaQuery).getResultList();
    }
}
