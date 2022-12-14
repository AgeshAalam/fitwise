package com.fitwise.specifications.jpa;

import com.fitwise.constants.DBConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.entity.PlatformType;
import com.fitwise.entity.Programs;
import com.fitwise.entity.payments.appleiap.ApplePayment;
import com.fitwise.entity.payments.appleiap.AppleProductSubscription;
import com.fitwise.entity.payments.appleiap.AppleSubscriptionStatus;
import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.payments.stripe.billing.StripeSubscriptionAndUserProgramMapping;
import com.fitwise.entity.payments.stripe.billing.StripeSubscriptionStatus;
import com.fitwise.entity.subscription.ProgramSubscription;
import com.fitwise.entity.subscription.SubscriptionPlan;
import com.fitwise.entity.subscription.SubscriptionStatus;
import com.fitwise.properties.StripeProperties;
import com.fitwise.specifications.jpa.dao.AppleSubscriptionStatusDAO;
import com.fitwise.specifications.jpa.dao.ManageSubscriptionDAO;
import com.fitwise.specifications.jpa.dao.ProgramSubscriptionDAO;
import com.fitwise.specifications.jpa.dao.StripeSubscriptionStatusDAO;
import com.fitwise.specifications.jpa.dao.SubscriptionCountDao;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/*
 * Created by Vignesh.G on 09/06/21
 */
@Component
public class ProgramSubscriptionJPA {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private StripeProperties stripeProperties;

    /**
     * Get Active subscription count of an instructor for ios
     * @param instructorId
     * @return
     */
    public Long getActiveSubscriptionCountOfAnInstructorForIos(Long instructorId){

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<ApplePayment> applePaymentRoot = criteriaQuery.from(ApplePayment.class);

        Subquery subquery = criteriaQuery.subquery(ApplePayment.class);
        Root<ApplePayment> applePaymentInnerRoot = subquery.from(ApplePayment.class);

        Subquery orderInnerSubQuery = subquery.subquery(OrderManagement.class);
        Root<OrderManagement> orderManagementInnerRoot = orderInnerSubQuery.from(OrderManagement.class);

        Predicate instructorPredicate = criteriaBuilder.equal(orderManagementInnerRoot.get("program").get("owner").get("userId"),instructorId);
        Predicate platformPredicate = criteriaBuilder.equal(orderManagementInnerRoot.get("subscribedViaPlatform").get("platformTypeId"), 2L);
        orderInnerSubQuery.where(criteriaBuilder.and(instructorPredicate, platformPredicate));
        orderInnerSubQuery.select(criteriaBuilder.max(orderManagementInnerRoot.get("id"))).groupBy(orderManagementInnerRoot.get("program"), orderManagementInnerRoot.get("user"));


        Predicate orderIdPredicate = applePaymentInnerRoot.get("orderManagement").get("id").in(orderInnerSubQuery.getSelection());
        subquery.where(orderIdPredicate);
        subquery.select(criteriaBuilder.max(applePaymentInnerRoot.get("id"))).groupBy(applePaymentInnerRoot.get("orderManagement").get("id"));

        Predicate applePaymentIdPredicate = applePaymentRoot.get("id").in(subquery.getSelection());
        Date date = new Date();
        Predicate expiryPredicate = criteriaBuilder.greaterThanOrEqualTo(applePaymentRoot.get("expiryDate"), date);
        criteriaQuery.where(criteriaBuilder.and(applePaymentIdPredicate, expiryPredicate));
        criteriaQuery.select(criteriaBuilder.count(applePaymentRoot));
        return entityManager.createQuery(criteriaQuery).getSingleResult();

    }

    /**
     * Get Active subscription count of an instructor for stripe
     * @param instructorId
     * @return
     */
    public Long getActiveSubscriptionCountOfAnInstructorForStripe(Long instructorId){

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<ProgramSubscription> programSubscriptionRoot = criteriaQuery.from(ProgramSubscription.class);

        Predicate ownerPredicate = criteriaBuilder.equal(programSubscriptionRoot.get("program").get("owner").get("userId"), instructorId);
        List<String> statusList = Arrays.asList(KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING);
        Predicate statusPredicate = programSubscriptionRoot.get("subscriptionStatus").get("subscriptionStatusName").in(statusList);


        int bufferMinutes = stripeProperties.getSubscriptionExpiryBufferMinutes();
        Expression<Date> subscribedDate = programSubscriptionRoot.get("subscribedDate");
        Expression<Integer> days = programSubscriptionRoot.get("subscriptionPlan").get("duration");
        Expression<Timestamp> newDate = criteriaBuilder.function("ADDDATE",Timestamp.class,subscribedDate, days);

        LocalDateTime now = LocalDateTime.now();
        now = now.minusMinutes(bufferMinutes);
        Timestamp currentDateWithBuffer = new Timestamp(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()).getTime());

        Predicate durationPredicate = criteriaBuilder.greaterThanOrEqualTo(newDate, currentDateWithBuffer);

        List<String> platformList = Arrays.asList(DBConstants.ANDROID, DBConstants.WEB);
        Predicate platformPredicate = programSubscriptionRoot.get("subscribedViaPlatform").get("platform").in(platformList);
        Predicate finalPred = criteriaBuilder.and(ownerPredicate, statusPredicate, durationPredicate, platformPredicate);
        criteriaQuery.where(finalPred);
        criteriaQuery.select(criteriaBuilder.count(programSubscriptionRoot));

        return entityManager.createQuery(criteriaQuery).getSingleResult();

    }


    //Getting program subscription DAO list
    public List<ProgramSubscriptionDAO> getActiveProgramSubscriptionsListByUserIdForStripe(List<String> statusList, Long memberId, boolean isPaid, Optional<String> searchName){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProgramSubscriptionDAO> criteriaQuery = criteriaBuilder.createQuery(ProgramSubscriptionDAO.class);
        Root<ProgramSubscription> programSubscriptionRoot = criteriaQuery.from(ProgramSubscription.class);

        //sub query 1 to get latest order management id
        Subquery<Long> subQuery1  = criteriaQuery.subquery(Long.class);
        Root<OrderManagement> orderManagementRoot = subQuery1.from(OrderManagement.class);

        Predicate userIdEqualPredicate = criteriaBuilder.equal(orderManagementRoot.get("user").get("userId"), memberId);
        Predicate programIdEqualPredicate = criteriaBuilder.equal(orderManagementRoot.get("program").get("programId"), programSubscriptionRoot.get("program").get("programId"));

        Predicate finalPredicate = criteriaBuilder.and(userIdEqualPredicate, programIdEqualPredicate);

        subQuery1.where(finalPredicate).groupBy(orderManagementRoot.get("program").get("programId"),orderManagementRoot.get("user").get("userId"));
        subQuery1.select(criteriaBuilder.max(orderManagementRoot.get("id")));

        //sub query 2 to get order management based on sub query 1 result
        Subquery<OrderManagement> subQuery2 = criteriaQuery.subquery(OrderManagement.class);
        Root<OrderManagement> orderManagementRoot1 = subQuery2.from(OrderManagement.class);

        Predicate orderManagementIdEqualPredicate = criteriaBuilder.equal(orderManagementRoot1.get("id"), subQuery1.getSelection());

        subQuery2.where(orderManagementIdEqualPredicate);
        subQuery2.select(orderManagementRoot1);

        Predicate memberPredicate = criteriaBuilder.equal(programSubscriptionRoot.get("user").get("userId"), memberId);
        Predicate statusPredicate = programSubscriptionRoot.get("subscriptionStatus").get("subscriptionStatusName").in(statusList);


        int bufferMinutes = stripeProperties.getSubscriptionExpiryBufferMinutes();
        Expression<Date> subscribedDate = programSubscriptionRoot.get("subscribedDate");
        Expression<Integer> days = programSubscriptionRoot.get("subscriptionPlan").get("duration");
        Expression<Timestamp> newDate = criteriaBuilder.function("ADDDATE",Timestamp.class,subscribedDate, days);

        LocalDateTime now = LocalDateTime.now();
        now = now.minusMinutes(bufferMinutes);
        Timestamp currentDateWithBuffer = new Timestamp(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()).getTime());

        Predicate durationPredicate;
        //Deciding whether the result program should be paid or expired
        if (isPaid){
            durationPredicate = criteriaBuilder.greaterThanOrEqualTo(newDate, currentDateWithBuffer);
        } else {
            durationPredicate = criteriaBuilder.lessThanOrEqualTo(newDate, currentDateWithBuffer);
        }

        List<String> platformList = Arrays.asList(DBConstants.ANDROID, DBConstants.WEB);
        Predicate platformPredicate = programSubscriptionRoot.get("subscribedViaPlatform").get("platform").in(platformList);

        //Result set expression
        Expression<PlatformType> subscribedViaPlatformExpression = programSubscriptionRoot.get("subscribedViaPlatform");
        Expression<Date> subscribedDateExpression = programSubscriptionRoot.get("subscribedDate");
        Expression<SubscriptionPlan> subscriptionPlanExpression = programSubscriptionRoot.get("subscriptionPlan");
        Expression<Programs> programsExpression = programSubscriptionRoot.get("program");
        Expression<SubscriptionStatus> subscriptionStatusExpression = programSubscriptionRoot.get("subscriptionStatus");
        Expression<OrderManagement> orderManagementExpression = subQuery2.getSelection();

        if (searchName.isPresent() && searchName.get().trim().length() != 0){
            Predicate searchNamePredicate = criteriaBuilder.like(programSubscriptionRoot.get("program").get("title"), "%"+searchName.get().trim()+"%");
            Predicate finalPredicateForMainCriteria = criteriaBuilder.and(memberPredicate, statusPredicate, durationPredicate, platformPredicate, searchNamePredicate);
            criteriaQuery.where(finalPredicateForMainCriteria);
        } else {
            Predicate finalPredicateForMainCriteria = criteriaBuilder.and(memberPredicate, statusPredicate, durationPredicate, platformPredicate);
            criteriaQuery.where(finalPredicateForMainCriteria);
        }
        criteriaQuery.multiselect(subscribedViaPlatformExpression, subscribedDateExpression, subscriptionPlanExpression, programsExpression, subscriptionStatusExpression, orderManagementExpression);

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    //Getting program subscription list
    public List<ProgramSubscriptionDAO> getProgramSubscriptionListByProgramListAndMemberId(List<Long> programIdList, List<String> subscriptionStatusList, Long memberId, Optional<String> searchName){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProgramSubscriptionDAO> criteriaQuery = criteriaBuilder.createQuery(ProgramSubscriptionDAO.class);

        Root<ProgramSubscription> programSubscriptionRoot = criteriaQuery.from(ProgramSubscription.class);

        //Sub query 1 to get order management id
        Subquery<Long> subQuery1 = criteriaQuery.subquery(Long.class);
        Root<OrderManagement> orderManagementRoot = subQuery1.from(OrderManagement.class);

        Predicate programIdEqualPredicate = criteriaBuilder.equal(orderManagementRoot.get("program").get("programId"), programSubscriptionRoot.get("program").get("programId"));
        Predicate memberIdEqualPredicate = criteriaBuilder.equal(orderManagementRoot.get("user").get("userId"), memberId);

        Predicate finalPredicateForSubQuery1 = criteriaBuilder.and(programIdEqualPredicate, memberIdEqualPredicate);

        subQuery1.where(finalPredicateForSubQuery1).groupBy(orderManagementRoot.get("program").get("programId"),orderManagementRoot.get("user").get("userId"));
        subQuery1.select(criteriaBuilder.max(orderManagementRoot.get("id")));

        //Sub query 2 to get order management based on the sub query 1 result
        Subquery<OrderManagement> subQuery2 = criteriaQuery.subquery(OrderManagement.class);
        Root<OrderManagement> orderManagementRoot1 = subQuery2.from(OrderManagement.class);

        Predicate orderManagementIdEqualPredicate = criteriaBuilder.equal(orderManagementRoot1.get("id"), subQuery1.getSelection());

        subQuery2.where(orderManagementIdEqualPredicate);
        subQuery2.select(orderManagementRoot1);

        Expression<Long> programIdInExpression = programSubscriptionRoot.get("program").get("programId");

        Predicate programIdInPredicate = programIdInExpression.in(programIdList);

        Expression<String> programSubscriptionStatusInExpression = programSubscriptionRoot.get("subscriptionStatus").get("subscriptionStatusName");

        Predicate programSubscriptionStatusInPredicate = programSubscriptionStatusInExpression.in(subscriptionStatusList);

        Predicate memberIdPredicate = criteriaBuilder.equal(programSubscriptionRoot.get("user").get("userId"), memberId);

        //Result set expression
        Expression<PlatformType> subscribedViaPlatformExpression = programSubscriptionRoot.get("subscribedViaPlatform");
        Expression<Date> subscribedDateExpression = programSubscriptionRoot.get("subscribedDate");
        Expression<SubscriptionPlan> subscriptionPlanExpression = programSubscriptionRoot.get("subscriptionPlan");
        Expression<Programs> programsExpression = programSubscriptionRoot.get("program");
        Expression<SubscriptionStatus> subscriptionStatusExpression = programSubscriptionRoot.get("subscriptionStatus");
        Expression<OrderManagement> orderManagementExpression = subQuery2.getSelection();

        if (searchName.isPresent() && searchName.get().trim().length() != 0){
            Predicate searchNamePredicate = criteriaBuilder.like(programSubscriptionRoot.get("program").get("title"), "%"+searchName.get().trim()+"%");
            Predicate finalPredicate = criteriaBuilder.and(programIdInPredicate, programSubscriptionStatusInPredicate, memberIdPredicate, searchNamePredicate);
            criteriaQuery.where(finalPredicate);
        } else {
            Predicate finalPredicate = criteriaBuilder.and(programIdInPredicate, programSubscriptionStatusInPredicate, memberIdPredicate);
            criteriaQuery.where(finalPredicate);
        }
        criteriaQuery.multiselect(subscribedViaPlatformExpression, subscribedDateExpression, subscriptionPlanExpression, programsExpression, subscriptionStatusExpression, orderManagementExpression);

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    public List<ProgramSubscriptionDAO> getProgramSubscriptionListForTrialMember(List<String> statusList, Long memberId, Optional<String> searchName){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProgramSubscriptionDAO> criteriaQuery = criteriaBuilder.createQuery(ProgramSubscriptionDAO.class);
        Root<ProgramSubscription> programSubscriptionRoot = criteriaQuery.from(ProgramSubscription.class);

        //sub query 1 to get latest order management id
        Subquery<Long> subQuery1  = criteriaQuery.subquery(Long.class);
        Root<OrderManagement> orderManagementRoot = subQuery1.from(OrderManagement.class);

        Predicate userIdEqualPredicate = criteriaBuilder.equal(orderManagementRoot.get("user").get("userId"), memberId);
        Predicate programIdEqualPredicate = criteriaBuilder.equal(orderManagementRoot.get("program").get("programId"), programSubscriptionRoot.get("program").get("programId"));

        Predicate finalPredicate = criteriaBuilder.and(userIdEqualPredicate, programIdEqualPredicate);

        subQuery1.where(finalPredicate).groupBy(orderManagementRoot.get("program").get("programId"),orderManagementRoot.get("user").get("userId"));
        subQuery1.select(criteriaBuilder.max(orderManagementRoot.get("id")));

        //sub query 2 to get order management based on sub query 1 result
        Subquery<OrderManagement> subQuery2 = criteriaQuery.subquery(OrderManagement.class);
        Root<OrderManagement> orderManagementRoot1 = subQuery2.from(OrderManagement.class);

        Predicate orderManagementIdEqualPredicate = criteriaBuilder.equal(orderManagementRoot1.get("id"), subQuery1.getSelection());

        subQuery2.where(orderManagementIdEqualPredicate);
        subQuery2.select(orderManagementRoot1);

        Predicate memberPredicate = criteriaBuilder.equal(programSubscriptionRoot.get("user").get("userId"), memberId);
        Predicate statusPredicate = programSubscriptionRoot.get("subscriptionStatus").get("subscriptionStatusName").in(statusList);

        //Result set expression
        Expression<PlatformType> subscribedViaPlatformExpression = programSubscriptionRoot.get("subscribedViaPlatform");
        Expression<Date> subscribedDateExpression = programSubscriptionRoot.get("subscribedDate");
        Expression<SubscriptionPlan> subscriptionPlanExpression = programSubscriptionRoot.get("subscriptionPlan");
        Expression<Programs> programsExpression = programSubscriptionRoot.get("program");
        Expression<SubscriptionStatus> subscriptionStatusExpression = programSubscriptionRoot.get("subscriptionStatus");
        Expression<OrderManagement> orderManagementExpression = subQuery2.getSelection();


        if (searchName.isPresent() && searchName.get().trim().length() != 0){
            Predicate searchNamePredicate = criteriaBuilder.like(programSubscriptionRoot.get("program").get("title"), "%"+searchName.get().trim()+"%");
            Predicate finalPredicateForMainCriteria = criteriaBuilder.and(memberPredicate, statusPredicate, searchNamePredicate);
            criteriaQuery.where(finalPredicateForMainCriteria);
        } else {
            Predicate finalPredicateForMainCriteria = criteriaBuilder.and(memberPredicate, statusPredicate);
            criteriaQuery.where(finalPredicateForMainCriteria);
        }
        criteriaQuery.multiselect(subscribedViaPlatformExpression, subscribedDateExpression, subscriptionPlanExpression, programsExpression, subscriptionStatusExpression, orderManagementExpression);

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    /**
     * Getting manage subscription DAO list for v1/member/getSubscribedProgramsList
     * @param memberId
     * @return
     */
    public List<ManageSubscriptionDAO> getActiveProgramSubscriptionsListByUserIdForStripe(Long memberId){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ManageSubscriptionDAO> criteriaQuery = criteriaBuilder.createQuery(ManageSubscriptionDAO.class);
        Root<ProgramSubscription> programSubscriptionRoot = criteriaQuery.from(ProgramSubscription.class);

        //sub query 1 to get latest order management id
        Subquery<Long> subQuery1  = criteriaQuery.subquery(Long.class);
        Root<OrderManagement> orderManagementRoot = subQuery1.from(OrderManagement.class);

        Predicate userIdEqualPredicate = criteriaBuilder.equal(orderManagementRoot.get("user").get("userId"), memberId);
        Predicate programIdEqualPredicate = criteriaBuilder.equal(orderManagementRoot.get("program").get("programId"), programSubscriptionRoot.get("program").get("programId"));

        Predicate finalPredicate = criteriaBuilder.and(userIdEqualPredicate, programIdEqualPredicate);

        subQuery1.where(finalPredicate).groupBy(orderManagementRoot.get("program").get("programId"),orderManagementRoot.get("user").get("userId"));
        subQuery1.select(criteriaBuilder.max(orderManagementRoot.get("id")));

        //sub query 2 to get order management based on sub query 1 result
        Subquery<OrderManagement> subQuery2 = criteriaQuery.subquery(OrderManagement.class);
        Root<OrderManagement> orderManagementRoot1 = subQuery2.from(OrderManagement.class);

        Predicate orderManagementIdEqualPredicate = criteriaBuilder.equal(orderManagementRoot1.get("id"), subQuery1.getSelection());

        subQuery2.where(orderManagementIdEqualPredicate);
        subQuery2.select(orderManagementRoot1);

        Predicate memberPredicate = criteriaBuilder.equal(programSubscriptionRoot.get("user").get("userId"), memberId);
        List<String> statusList = Arrays.asList(KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING);
        Predicate statusPredicate = programSubscriptionRoot.get("subscriptionStatus").get("subscriptionStatusName").in(statusList);


        int bufferMinutes = stripeProperties.getSubscriptionExpiryBufferMinutes();
        Expression<Date> subscribedDate = programSubscriptionRoot.get("subscribedDate");
        Expression<Integer> days = programSubscriptionRoot.get("subscriptionPlan").get("duration");
        Expression<Timestamp> newDate = criteriaBuilder.function("ADDDATE",Timestamp.class,subscribedDate, days);

        LocalDateTime now = LocalDateTime.now();
        now = now.minusMinutes(bufferMinutes);
        Timestamp currentDateWithBuffer = new Timestamp(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()).getTime());

        Predicate durationPredicate = criteriaBuilder.greaterThanOrEqualTo(newDate, currentDateWithBuffer);

        List<String> platformList = Arrays.asList(DBConstants.ANDROID, DBConstants.WEB);
        Predicate platformPredicate = programSubscriptionRoot.get("subscribedViaPlatform").get("platform").in(platformList);

        //Result set expression
        Expression<PlatformType> subscribedViaPlatformExpression = programSubscriptionRoot.get("subscribedViaPlatform");
        Expression<Date> subscribedDateExpression = programSubscriptionRoot.get("subscribedDate");
        Expression<Programs> programsExpression = programSubscriptionRoot.get("program");
        Expression<OrderManagement> orderManagementExpression = subQuery2.getSelection();

        Predicate finalPredicateForMainCriteria = criteriaBuilder.and(memberPredicate, statusPredicate, durationPredicate, platformPredicate);
        //Make the list desc by subscribed date
        criteriaQuery.where(finalPredicateForMainCriteria).orderBy(criteriaBuilder.desc(programSubscriptionRoot.get("subscribedDate")));
        criteriaQuery.multiselect(subscribedViaPlatformExpression, subscribedDateExpression, programsExpression, orderManagementExpression);

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    /**
     * Getting program subscription list for v1/member/getSubscribedProgramsList
     * @param programIdList
     * @param memberId
     * @return
     */
    public List<ManageSubscriptionDAO> getProgramSubscriptionListByProgramListAndMemberId(List<Long> programIdList, Long memberId){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ManageSubscriptionDAO> criteriaQuery = criteriaBuilder.createQuery(ManageSubscriptionDAO.class);

        Root<ProgramSubscription> programSubscriptionRoot = criteriaQuery.from(ProgramSubscription.class);

        //Sub query 1 to get order management id
        Subquery<Long> subQuery1 = criteriaQuery.subquery(Long.class);
        Root<OrderManagement> orderManagementRoot = subQuery1.from(OrderManagement.class);

        Predicate programIdEqualPredicate = criteriaBuilder.equal(orderManagementRoot.get("program").get("programId"), programSubscriptionRoot.get("program").get("programId"));
        Predicate memberIdEqualPredicate = criteriaBuilder.equal(orderManagementRoot.get("user").get("userId"), memberId);

        Predicate finalPredicateForSubQuery1 = criteriaBuilder.and(programIdEqualPredicate, memberIdEqualPredicate);

        subQuery1.where(finalPredicateForSubQuery1).groupBy(orderManagementRoot.get("program").get("programId"),orderManagementRoot.get("user").get("userId"));
        subQuery1.select(criteriaBuilder.max(orderManagementRoot.get("id")));

        //Sub query 2 to get order management based on the sub query 1 result
        Subquery<OrderManagement> subQuery2 = criteriaQuery.subquery(OrderManagement.class);
        Root<OrderManagement> orderManagementRoot1 = subQuery2.from(OrderManagement.class);

        Predicate orderManagementIdEqualPredicate = criteriaBuilder.equal(orderManagementRoot1.get("id"), subQuery1.getSelection());

        subQuery2.where(orderManagementIdEqualPredicate);
        subQuery2.select(orderManagementRoot1);

        Expression<Long> programIdInExpression = programSubscriptionRoot.get("program").get("programId");

        Predicate programIdInPredicate = programIdInExpression.in(programIdList);

        Predicate memberIdPredicate = criteriaBuilder.equal(programSubscriptionRoot.get("user").get("userId"), memberId);

        //Result set expression
        Expression<PlatformType> subscribedViaPlatformExpression = programSubscriptionRoot.get("subscribedViaPlatform");
        Expression<Date> subscribedDateExpression = programSubscriptionRoot.get("subscribedDate");
        Expression<Programs> programsExpression = programSubscriptionRoot.get("program");
        Expression<OrderManagement> orderManagementExpression = subQuery2.getSelection();

        Predicate finalPredicate = criteriaBuilder.and(programIdInPredicate, memberIdPredicate);
        //Make the list desc by subscribed date
        criteriaQuery.where(finalPredicate).orderBy(criteriaBuilder.asc(programSubscriptionRoot.get("subscribedDate")));
        criteriaQuery.multiselect(subscribedViaPlatformExpression, subscribedDateExpression, programsExpression, orderManagementExpression);

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    /**
     * Get Active package subscription for manage subscription - android and web subscriptions
     * @param
     * @return
     */
    public List<StripeSubscriptionStatusDAO> getStripeSubscriptionStatusByProgramIdListAndMemberId(List<Long> programIdList, Long memberId){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<StripeSubscriptionStatusDAO> criteriaQuery = criteriaBuilder.createQuery(StripeSubscriptionStatusDAO.class);
        Root<StripeSubscriptionAndUserProgramMapping> stripeSubscriptionAndUserProgramMappingRoot = criteriaQuery.from(StripeSubscriptionAndUserProgramMapping.class);

        Expression<Long> programId = stripeSubscriptionAndUserProgramMappingRoot.get("program").get("programId");
        Predicate programIdInPredicate = programId.in(programIdList);

        Predicate memberIdEqualPredicate = criteriaBuilder.equal(stripeSubscriptionAndUserProgramMappingRoot.get("user").get("userId"), memberId);

        Predicate finalPredicate = criteriaBuilder.and(programIdInPredicate, memberIdEqualPredicate);

        //Result set expressions
        Expression<Long> programIdExpression = stripeSubscriptionAndUserProgramMappingRoot.get("program").get("programId");
        Expression<StripeSubscriptionStatus> stripeSubscriptionStatusExpression = stripeSubscriptionAndUserProgramMappingRoot.get("subscriptionStatus");
        Expression<Date> modifiedDate = stripeSubscriptionAndUserProgramMappingRoot.get("modifiedDate");
        criteriaQuery.where(finalPredicate)
                .orderBy(criteriaBuilder.asc(stripeSubscriptionAndUserProgramMappingRoot.get("program").get("programId")));
        criteriaQuery.multiselect(programIdExpression, stripeSubscriptionStatusExpression, modifiedDate);
        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    /**
     * Get Active package subscription for manage subscription - IOS subscriptions
     * @param
     * @return
     */
    public List<AppleSubscriptionStatusDAO> getIOSSubscriptionStatusByProgramIdListAndMemberId(List<Long> programIdList, Long memberId){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AppleSubscriptionStatusDAO> criteriaQuery = criteriaBuilder.createQuery(AppleSubscriptionStatusDAO.class);
        Root<AppleProductSubscription> appleProductSubscriptionRoot = criteriaQuery.from(AppleProductSubscription.class);

        Expression<Long> programId = appleProductSubscriptionRoot.get("program").get("programId");
        Predicate programIdInPredicate = programId.in(programIdList);

        Predicate memberIdEqualPredicate = criteriaBuilder.equal(appleProductSubscriptionRoot.get("user").get("userId"), memberId);

        Predicate finalPredicate = criteriaBuilder.and(programIdInPredicate, memberIdEqualPredicate);

        //Result set expressions
        Expression<Long> programIdExpression = appleProductSubscriptionRoot.get("program").get("programId");
        Expression<AppleSubscriptionStatus> appleSubscriptionStatusExpression = appleProductSubscriptionRoot.get("appleSubscriptionStatus");
        Expression<Date> modifiedDate = appleProductSubscriptionRoot.get("modifiedDate");
        criteriaQuery.where(finalPredicate)
                .orderBy(criteriaBuilder.asc(appleProductSubscriptionRoot.get("program").get("programId")));
        criteriaQuery.multiselect(programIdExpression, appleSubscriptionStatusExpression, modifiedDate);
        return entityManager.createQuery(criteriaQuery).getResultList();
    }


    /**
     * Get Active subscription count of program
     * @param programId
     * @return
     */
    public SubscriptionCountDao getActiveSubscriptionCountForProgram(Long programId){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SubscriptionCountDao> criteriaQuery = criteriaBuilder.createQuery(SubscriptionCountDao.class);

        Root<ProgramSubscription> programSubscriptionRoot = criteriaQuery.from(ProgramSubscription.class);
        Predicate programIdPredicate = criteriaBuilder.equal(programSubscriptionRoot.get("program").get("programId"), programId);
        List<String> statusList = Arrays.asList(KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING);
        Predicate statusPredicate = programSubscriptionRoot.get("subscriptionStatus").get("subscriptionStatusName").in(statusList);
        List<String> platformList = Arrays.asList(DBConstants.ANDROID, DBConstants.WEB);
        Predicate platformPredicte = programSubscriptionRoot.get("subscribedViaPlatform").get("platform").in(platformList);
        int bufferMinutes = stripeProperties.getSubscriptionExpiryBufferMinutes();
        LocalDateTime now = LocalDateTime.now();
        now = now.minusMinutes(bufferMinutes);
        Timestamp currentDateWithBuffer = new Timestamp(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()).getTime());
        Expression<Date> subscribedDate = programSubscriptionRoot.get("subscribedDate");
        Expression<Integer> days = programSubscriptionRoot.get("subscriptionPlan").get("duration");
        Expression<Timestamp> newDate = criteriaBuilder.function("ADDDATE", Timestamp.class, subscribedDate, days);
        Predicate durationPredicate = criteriaBuilder.greaterThanOrEqualTo(newDate, currentDateWithBuffer);
        criteriaQuery.where(criteriaBuilder.and(programIdPredicate, statusPredicate, platformPredicte, durationPredicate));

        Subquery appleQuery = criteriaQuery.subquery(ApplePayment.class);
        Root<ApplePayment> applePaymentRoot = appleQuery.from(ApplePayment.class);

        Subquery subquery = appleQuery.subquery(ApplePayment.class);
        Root<ApplePayment> applePaymentInnerRoot = subquery.from(ApplePayment.class);

        Subquery orderInnerSubQuery = subquery.subquery(OrderManagement.class);
        Root<OrderManagement> orderManagementInnerRoot = orderInnerSubQuery.from(OrderManagement.class);

        Predicate programPredicate = criteriaBuilder.equal(orderManagementInnerRoot.get("program").get("programId"), programId);
        Predicate platformPredicate = criteriaBuilder.equal(orderManagementInnerRoot.get("subscribedViaPlatform").get("platformTypeId"), 2L);
        orderInnerSubQuery.where(criteriaBuilder.and(programPredicate, platformPredicate));
        orderInnerSubQuery.select(criteriaBuilder.max(orderManagementInnerRoot.get("id"))).groupBy(orderManagementInnerRoot.get("program"), orderManagementInnerRoot.get("user"));


        Predicate orderIdPredicate = applePaymentInnerRoot.get("orderManagement").get("id").in(orderInnerSubQuery.getSelection());
        subquery.where(orderIdPredicate);
        subquery.select(criteriaBuilder.max(applePaymentInnerRoot.get("id"))).groupBy(applePaymentInnerRoot.get("orderManagement").get("id"));

        Predicate applePaymentIdPredicate = applePaymentRoot.get("id").in(subquery.getSelection());
        Date date = new Date();
        Predicate expiryPredicate = criteriaBuilder.greaterThanOrEqualTo(applePaymentRoot.get("expiryDate"), date);
        appleQuery.where(criteriaBuilder.and(applePaymentIdPredicate, expiryPredicate));
        appleQuery.select(criteriaBuilder.count(applePaymentRoot));

        Expression<Long> stripeActiveSubscriptionCount = criteriaBuilder.count(programSubscriptionRoot);
        Expression<Long> iosActiveSubscriptionCount = appleQuery.getSelection();
        criteriaQuery.multiselect(stripeActiveSubscriptionCount, iosActiveSubscriptionCount);
        return  entityManager.createQuery(criteriaQuery).getSingleResult();
    }

    /**
     * Get overall active subscription count- stripe for list of programs
     * @param programIds
     * @return
     */
    public Long getStripeActiveSubscriptionCountForPrograms(List<Long> programIds){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<ProgramSubscription> programSubscriptionRoot = criteriaQuery.from(ProgramSubscription.class);
        Predicate programIdPredicate = programSubscriptionRoot.get("program").get("programId").in(programIds);
        List<String> statusList = Arrays.asList(KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING);
        Predicate statusPredicate = programSubscriptionRoot.get("subscriptionStatus").get("subscriptionStatusName").in(statusList);
        List<String> platformList = Arrays.asList(DBConstants.ANDROID, DBConstants.WEB);
        Predicate platformPredicte = programSubscriptionRoot.get("subscribedViaPlatform").get("platform").in(platformList);
        int bufferMinutes = stripeProperties.getSubscriptionExpiryBufferMinutes();
        LocalDateTime now = LocalDateTime.now();
        now = now.minusMinutes(bufferMinutes);
        Timestamp currentDateWithBuffer = new Timestamp(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()).getTime());
        Expression<Date> subscribedDate = programSubscriptionRoot.get("subscribedDate");
        Expression<Integer> days = programSubscriptionRoot.get("subscriptionPlan").get("duration");
        Expression<Timestamp> newDate = criteriaBuilder.function("ADDDATE", Timestamp.class, subscribedDate, days);
        Predicate durationPredicate = criteriaBuilder.greaterThanOrEqualTo(newDate, currentDateWithBuffer);
        criteriaQuery.where(criteriaBuilder.and(programIdPredicate, statusPredicate, platformPredicte, durationPredicate));
        criteriaQuery.select(criteriaBuilder.count(programSubscriptionRoot));
        Long subscriptionCount =  entityManager.createQuery(criteriaQuery).getSingleResult();
        if(subscriptionCount == null){
            return 0L;
        }
        return subscriptionCount;
    }

    /**
     * Get overall active subscription count- ios for list of programs
     * @param programIds
     * @return
     */
    public Long getIosActiveSubscriptionCountForPrograms(List<Long> programIds){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);

        Root<ApplePayment> applePaymentRoot = criteriaQuery.from(ApplePayment.class);

        Subquery subquery = criteriaQuery.subquery(ApplePayment.class);
        Root<ApplePayment> applePaymentInnerRoot = subquery.from(ApplePayment.class);

        Subquery orderInnerSubQuery = subquery.subquery(OrderManagement.class);
        Root<OrderManagement> orderManagementInnerRoot = orderInnerSubQuery.from(OrderManagement.class);

        Predicate programPredicate = orderManagementInnerRoot.get("program").get("programId").in(programIds);
        Predicate platformPredicate = criteriaBuilder.like(orderManagementInnerRoot.get("subscribedViaPlatform").get("platform"), DBConstants.IOS);
        orderInnerSubQuery.where(criteriaBuilder.and(programPredicate, platformPredicate));
        orderInnerSubQuery.select(criteriaBuilder.max(orderManagementInnerRoot.get("id"))).groupBy(orderManagementInnerRoot.get("program"), orderManagementInnerRoot.get("user"));


        Predicate orderIdPredicate = applePaymentInnerRoot.get("orderManagement").get("id").in(orderInnerSubQuery.getSelection());
        subquery.where(orderIdPredicate);
        subquery.select(criteriaBuilder.max(applePaymentInnerRoot.get("id"))).groupBy(applePaymentInnerRoot.get("orderManagement").get("id"));

        Predicate applePaymentIdPredicate = applePaymentRoot.get("id").in(subquery.getSelection());
        Date date = new Date();
        Predicate expiryPredicate = criteriaBuilder.greaterThanOrEqualTo(applePaymentRoot.get("expiryDate"), date);
        criteriaQuery.where(criteriaBuilder.and(applePaymentIdPredicate, expiryPredicate));
        criteriaQuery.select(criteriaBuilder.count(applePaymentRoot));
        Long subscriptionCount =  entityManager.createQuery(criteriaQuery).getSingleResult();

        if(subscriptionCount == null){
            return 0L;
        }
        return subscriptionCount;
    }
}
