package com.fitwise.specifications.jpa;

import com.fitwise.constants.KeyConstants;
import com.fitwise.entity.PlatformType;
import com.fitwise.entity.Programs;
import com.fitwise.entity.packaging.PackageKloudlessMapping;
import com.fitwise.entity.packaging.PackageProgramMapping;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.payments.stripe.StripeSubscriptionAndUserPackageMapping;
import com.fitwise.entity.payments.stripe.billing.StripeSubscriptionStatus;
import com.fitwise.entity.subscription.*;
import com.fitwise.properties.StripeProperties;
import com.fitwise.specifications.jpa.dao.ManageSubscriptionDAOForPackage;
import com.fitwise.specifications.jpa.dao.PackageSubscriptionDAO;
import com.fitwise.specifications.jpa.dao.StripeSubscriptionStatusDAO;
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

@Component
public class PackageSubscriptionJpa {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private StripeProperties stripeProperties;

    /**
     * Get Active subscription count of an instructor for stripe
     * @param instructorId
     * @return
     */
    public Long getActivePackageSubscriptionCountOfAnInstructor(Long instructorId){

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<PackageSubscription> packageSubscriptionRoot = criteriaQuery.from(PackageSubscription.class);
        Predicate ownerPredicate = criteriaBuilder.equal(packageSubscriptionRoot.get("subscriptionPackage").get("owner").get("userId"),instructorId);
        List<String> statusList = Arrays.asList(new String[]{KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING});
        Predicate statusPredicate = packageSubscriptionRoot.get("subscriptionStatus").get("subscriptionStatusName").in(statusList);

        int bufferMinutes = stripeProperties.getSubscriptionExpiryBufferMinutes();
        LocalDateTime now = LocalDateTime.now();
        now = now.minusMinutes(bufferMinutes);
        Timestamp currentDateWithBuffer = new Timestamp(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()).getTime());

        Expression<Date> subscribedDate = packageSubscriptionRoot.get("subscribedDate");
        Expression<Integer> days = packageSubscriptionRoot.get("subscriptionPlan").get("duration");
        Expression<Timestamp> newDate = criteriaBuilder.function("ADDDATE",Timestamp.class,subscribedDate, days);

        Predicate durationPredicate = criteriaBuilder.greaterThanOrEqualTo(newDate, currentDateWithBuffer);

        Predicate finalPred = criteriaBuilder.and(ownerPredicate, statusPredicate, durationPredicate);
        criteriaQuery.where(finalPred);
        criteriaQuery.select(criteriaBuilder.count(packageSubscriptionRoot));

        return entityManager.createQuery(criteriaQuery).getSingleResult();

    }

    /**
     * Get Active package subscription dao of an member for stripe
     * @param
     * @return
     */
    public List<PackageSubscriptionDAO> getActivePackageSubscriptionDAOAnMember(List<String> subscriptionStatusList, Long memberId, Optional<String> searchName, boolean isPaid){

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PackageSubscriptionDAO> criteriaQuery = criteriaBuilder.createQuery(PackageSubscriptionDAO.class);
        Root<PackageSubscription> packageSubscriptionRoot = criteriaQuery.from(PackageSubscription.class);

        Root<PackageProgramSubscription> packageProgramSubscriptionRoot = criteriaQuery.from(PackageProgramSubscription.class);

        //Sub query 1 to get order management id
        Subquery<Long> subQuery1 = criteriaQuery.subquery(Long.class);
        Root<OrderManagement> orderManagementRoot = subQuery1.from(OrderManagement.class);

        Predicate programIdEqualPredicate = criteriaBuilder.equal(orderManagementRoot.get("subscriptionPackage").get("subscriptionPackageId"), packageSubscriptionRoot.get("subscriptionPackage").get("subscriptionPackageId"));
        Predicate memberIdEqualPredicate = criteriaBuilder.equal(orderManagementRoot.get("user").get("userId"), memberId);

        Predicate finalPredicateForSubQuery1 = criteriaBuilder.and(programIdEqualPredicate, memberIdEqualPredicate);

        subQuery1.where(finalPredicateForSubQuery1).groupBy(orderManagementRoot.get("subscriptionPackage").get("subscriptionPackageId"),orderManagementRoot.get("user").get("userId"));
        subQuery1.select(criteriaBuilder.max(orderManagementRoot.get("id")));

        //Sub query 2 to get order management based on the sub query 1 result
        Subquery<OrderManagement> subQuery2 = criteriaQuery.subquery(OrderManagement.class);
        Root<OrderManagement> orderManagementRoot1 = subQuery2.from(OrderManagement.class);

        Predicate orderManagementIdEqualPredicate = criteriaBuilder.equal(orderManagementRoot1.get("id"), subQuery1.getSelection());

        subQuery2.where(orderManagementIdEqualPredicate);
        subQuery2.select(orderManagementRoot1);

        //Package program's package_subscription_id and package subscription's id equal predicate
        Predicate packageSubscriptionIdEqualPredicate = criteriaBuilder.equal(packageSubscriptionRoot.get("id"), packageProgramSubscriptionRoot.get("packageSubscription").get("id"));

        //Member id predicate
        Predicate memberPredicate = criteriaBuilder.equal(packageSubscriptionRoot.get("user").get("userId"),memberId);

        Predicate statusPredicate = packageSubscriptionRoot.get("subscriptionStatus").get("subscriptionStatusName").in(subscriptionStatusList);

        int bufferMinutes = stripeProperties.getSubscriptionExpiryBufferMinutes();
        LocalDateTime now = LocalDateTime.now();
        now = now.minusMinutes(bufferMinutes);
        Timestamp currentDateWithBuffer = new Timestamp(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()).getTime());

        Expression<Date> subscribedDate = packageSubscriptionRoot.get("subscribedDate");
        Expression<Integer> days = packageSubscriptionRoot.get("subscriptionPlan").get("duration");
        Expression<Timestamp> newDate = criteriaBuilder.function("ADDDATE",Timestamp.class,subscribedDate, days);

        Predicate durationPredicate;
        if (isPaid){
            durationPredicate = criteriaBuilder.greaterThanOrEqualTo(newDate, currentDateWithBuffer);
        } else {
            durationPredicate = criteriaBuilder.lessThanOrEqualTo(newDate, currentDateWithBuffer);

        }

        //Result set expression
        Expression<PlatformType> subscribedViaPlatformExpression = packageProgramSubscriptionRoot.get("subscribedViaPlatform");
        Expression<Date> subscribedDateExpression = packageProgramSubscriptionRoot.get("subscribedDate");
        Expression<SubscriptionPlan> subscriptionPlanExpression = packageProgramSubscriptionRoot.get("subscriptionPlan");
        Expression<Programs> programsExpression = packageProgramSubscriptionRoot.get("program");
        Expression<SubscriptionPackage> subscriptionPackageExpression = packageProgramSubscriptionRoot.get("packageSubscription").get("subscriptionPackage");
        Expression<SubscriptionStatus> subscriptionStatusExpression = packageProgramSubscriptionRoot.get("subscriptionStatus");
        Expression<OrderManagement> orderManagementExpression = subQuery2.getSelection();

        //Adding search predicate
        Predicate finalPredicate;
        if (searchName.isPresent() && searchName.get().trim().length() != 0){
            Predicate searchNamePredicate = criteriaBuilder.like(packageProgramSubscriptionRoot.get("program").get("title"), "%"+searchName.get().trim()+"%");
            finalPredicate = criteriaBuilder.and(memberPredicate, statusPredicate, durationPredicate, packageSubscriptionIdEqualPredicate, searchNamePredicate);
        } else {
            finalPredicate = criteriaBuilder.and(memberPredicate, statusPredicate, durationPredicate, packageSubscriptionIdEqualPredicate);
        }

        criteriaQuery.where(finalPredicate);
        criteriaQuery.multiselect(subscribedViaPlatformExpression, subscribedDateExpression, subscriptionPlanExpression, programsExpression, subscriptionPackageExpression, subscriptionStatusExpression,
                orderManagementExpression);

        return entityManager.createQuery(criteriaQuery).getResultList();

    }

    /**
     * Get Active package subscription for manage subscription
     * @param
     * @return
     */
    public List<ManageSubscriptionDAOForPackage> getActivePackageSubscriptionDAOAnMember(Long memberId){

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ManageSubscriptionDAOForPackage> criteriaQuery = criteriaBuilder.createQuery(ManageSubscriptionDAOForPackage.class);
        Root<PackageSubscription> packageSubscriptionRoot = criteriaQuery.from(PackageSubscription.class);

        //Sub query 1 to get order management id
        Subquery<Long> subQuery1 = criteriaQuery.subquery(Long.class);
        Root<OrderManagement> orderManagementRoot = subQuery1.from(OrderManagement.class);

        Predicate programIdEqualPredicate = criteriaBuilder.equal(orderManagementRoot.get("subscriptionPackage").get("subscriptionPackageId"), packageSubscriptionRoot.get("subscriptionPackage").get("subscriptionPackageId"));
        Predicate memberIdEqualPredicate = criteriaBuilder.equal(orderManagementRoot.get("user").get("userId"), memberId);

        Predicate finalPredicateForSubQuery1 = criteriaBuilder.and(programIdEqualPredicate, memberIdEqualPredicate);

        subQuery1.where(finalPredicateForSubQuery1).groupBy(orderManagementRoot.get("subscriptionPackage").get("subscriptionPackageId"),orderManagementRoot.get("user").get("userId"));
        subQuery1.select(criteriaBuilder.max(orderManagementRoot.get("id")));

        //Sub query 2 to get order management based on the sub query 1 result
        Subquery<OrderManagement> subQuery2 = criteriaQuery.subquery(OrderManagement.class);
        Root<OrderManagement> orderManagementRoot1 = subQuery2.from(OrderManagement.class);

        Predicate orderManagementIdEqualPredicate = criteriaBuilder.equal(orderManagementRoot1.get("id"), subQuery1.getSelection());

        subQuery2.where(orderManagementIdEqualPredicate);
        subQuery2.select(orderManagementRoot1);

        //Sub query 3 to get program count in subscription package
        Subquery<Long> subQuery3 = criteriaQuery.subquery(Long.class);
        Root<PackageProgramMapping> packageProgramMappingRoot = subQuery3.from(PackageProgramMapping.class);

        Predicate subscriptionPackageIdEqualPredicateForPgmCount = criteriaBuilder.equal(packageSubscriptionRoot.get("subscriptionPackage").get("subscriptionPackageId"),
                packageProgramMappingRoot.get("subscriptionPackage").get("subscriptionPackageId"));

        subQuery3.where(subscriptionPackageIdEqualPredicateForPgmCount);
        subQuery3.select(criteriaBuilder.count(packageProgramMappingRoot.get("packageProgramId")));

        //Sub query 4 to get program session count
        Subquery<Long> subQuery4 = criteriaQuery.subquery(Long.class);
        Root<PackageKloudlessMapping> packageKloudlessMappingRoot = subQuery4.from(PackageKloudlessMapping.class);

        Predicate subscriptionPackageIdEqualPredicateForSessionCount = criteriaBuilder.equal(packageSubscriptionRoot.get("subscriptionPackage").get("subscriptionPackageId"),
                packageKloudlessMappingRoot.get("subscriptionPackage").get("subscriptionPackageId"));

        subQuery4.where(subscriptionPackageIdEqualPredicateForSessionCount);
        subQuery4.select(criteriaBuilder.sum(packageKloudlessMappingRoot.get("totalSessionCount")));

        //Member id predicate
        Predicate memberPredicate = criteriaBuilder.equal(packageSubscriptionRoot.get("user").get("userId"),memberId);
        List<String> statusList = Arrays.asList(new String[]{KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING});
        Predicate statusPredicate = packageSubscriptionRoot.get("subscriptionStatus").get("subscriptionStatusName").in(statusList);

        int bufferMinutes = stripeProperties.getSubscriptionExpiryBufferMinutes();
        LocalDateTime now = LocalDateTime.now();
        now = now.minusMinutes(bufferMinutes);
        Timestamp currentDateWithBuffer = new Timestamp(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()).getTime());

        Expression<Date> subscribedDate = packageSubscriptionRoot.get("subscribedDate");
        Expression<Integer> days = packageSubscriptionRoot.get("subscriptionPlan").get("duration");
        Expression<Timestamp> newDate = criteriaBuilder.function("ADDDATE",Timestamp.class,subscribedDate, days);

        Predicate durationPredicate = criteriaBuilder.greaterThanOrEqualTo(newDate, currentDateWithBuffer);

        //Result set expression
        Expression<PlatformType> subscribedViaPlatformExpression = packageSubscriptionRoot.get("subscribedViaPlatform");
        Expression<Date> subscribedDateExpression = packageSubscriptionRoot.get("subscribedDate");
        Expression<SubscriptionPackage> subscriptionPackageExpression = packageSubscriptionRoot.get("subscriptionPackage");
        Expression<OrderManagement> orderManagementExpression = subQuery2.getSelection();
        Expression<Long> programCountExpression = subQuery3.getSelection();
        Expression<Long> sessionCountExpression = subQuery4.getSelection();

        Predicate finalPredicate = criteriaBuilder.and(memberPredicate, statusPredicate, durationPredicate);

        criteriaQuery.where(finalPredicate).orderBy(criteriaBuilder.desc(packageSubscriptionRoot.get("subscribedDate")));
        criteriaQuery.multiselect(subscribedViaPlatformExpression, subscribedDateExpression, subscriptionPackageExpression, orderManagementExpression, programCountExpression, sessionCountExpression);

        return entityManager.createQuery(criteriaQuery).getResultList();

    }

    /**
     * Get Active package subscription for manage subscription
     * @param
     * @return
     */
    public List<StripeSubscriptionStatusDAO> getStripeSubscriptionStatusBySubscriptionIdListAndMemberId(List<Long> subscriptionIdList, Long memberId){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<StripeSubscriptionStatusDAO> criteriaQuery = criteriaBuilder.createQuery(StripeSubscriptionStatusDAO.class);
        Root<StripeSubscriptionAndUserPackageMapping> stripeSubscriptionAndUserPackageMappingRoot = criteriaQuery.from(StripeSubscriptionAndUserPackageMapping.class);

        Expression<Long> subscriptionId = stripeSubscriptionAndUserPackageMappingRoot.get("subscriptionPackage").get("subscriptionPackageId");
        Predicate subscriptionIdInPredicate = subscriptionId.in(subscriptionIdList);

        Predicate memberIdEqualPredicate = criteriaBuilder.equal(stripeSubscriptionAndUserPackageMappingRoot.get("user").get("userId"), memberId);

        Predicate finalPredicate = criteriaBuilder.and(subscriptionIdInPredicate, memberIdEqualPredicate);

        //Result set expressions
        Expression<Long> subscriptionIdExpression = stripeSubscriptionAndUserPackageMappingRoot.get("subscriptionPackage").get("subscriptionPackageId");
        Expression<StripeSubscriptionStatus> stripeSubscriptionStatusExpression = stripeSubscriptionAndUserPackageMappingRoot.get("subscriptionStatus");
        Expression<Date> modifiedDate = stripeSubscriptionAndUserPackageMappingRoot.get("modifiedDate");

        criteriaQuery.where(finalPredicate)
//                .groupBy(stripeSubscriptionAndUserPackageMappingRoot.get("subscriptionPackage").get("subscriptionPackageId"), stripeSubscriptionAndUserPackageMappingRoot.get("user").get("userId"));
                .orderBy(criteriaBuilder.asc(stripeSubscriptionAndUserPackageMappingRoot.get("subscriptionPackage").get("subscriptionPackageId")));
        criteriaQuery.multiselect(subscriptionIdExpression, stripeSubscriptionStatusExpression, modifiedDate);

        return entityManager.createQuery(criteriaQuery).getResultList();
    }


    /**
     * Get active subscription count for a package
     * @param packageId
     * @return
     */
    public Long getActiveSubscriptionCountForPackage(Long packageId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);

        Root<PackageSubscription> packageSubscriptionRoot = criteriaQuery.from(PackageSubscription.class);
        Predicate packageIdPredicate = criteriaBuilder.equal(packageSubscriptionRoot.get("subscriptionPackage").get("subscriptionPackageId"), packageId);
        List<String> statusList = Arrays.asList(new String[]{KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING});
        Predicate statusPredicate = packageSubscriptionRoot.get("subscriptionStatus").get("subscriptionStatusName").in(statusList);
        int bufferMinutes = stripeProperties.getSubscriptionExpiryBufferMinutes();
        LocalDateTime now = LocalDateTime.now();
        now = now.minusMinutes(bufferMinutes);
        Timestamp currentDateWithBuffer = new Timestamp(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()).getTime());
        Expression<Date> subscribedDate = packageSubscriptionRoot.get("subscribedDate");
        Expression<Integer> days = packageSubscriptionRoot.get("subscriptionPlan").get("duration");
        Expression<Timestamp> newDate = criteriaBuilder.function("ADDDATE", Timestamp.class, subscribedDate, days);
        Predicate durationPredicate = criteriaBuilder.greaterThanOrEqualTo(newDate, currentDateWithBuffer);
        criteriaQuery.where(criteriaBuilder.and(packageIdPredicate, statusPredicate, durationPredicate));
        criteriaQuery.select(criteriaBuilder.count(packageSubscriptionRoot));
        Long subscriptionCount = entityManager.createQuery(criteriaQuery).getSingleResult();
        if (subscriptionCount == null) {
            return 0L;
        }
        return subscriptionCount;
    }

    /**
     * Get active subscription count for a packages list
     * @param packageIds
     * @return
     */
    public Long getOverallActiveSubscriptionCountForPackagesList(List<Long> packageIds) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);

        Root<PackageSubscription> packageSubscriptionRoot = criteriaQuery.from(PackageSubscription.class);
        Predicate packageIdPredicate = packageSubscriptionRoot.get("subscriptionPackage").get("subscriptionPackageId").in(packageIds);
        List<String> statusList = Arrays.asList(new String[]{KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING});
        Predicate statusPredicate = packageSubscriptionRoot.get("subscriptionStatus").get("subscriptionStatusName").in(statusList);
        int bufferMinutes = stripeProperties.getSubscriptionExpiryBufferMinutes();
        LocalDateTime now = LocalDateTime.now();
        now = now.minusMinutes(bufferMinutes);
        Timestamp currentDateWithBuffer = new Timestamp(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()).getTime());
        Expression<Date> subscribedDate = packageSubscriptionRoot.get("subscribedDate");
        Expression<Integer> days = packageSubscriptionRoot.get("subscriptionPlan").get("duration");
        Expression<Timestamp> newDate = criteriaBuilder.function("ADDDATE", Timestamp.class, subscribedDate, days);
        Predicate durationPredicate = criteriaBuilder.greaterThanOrEqualTo(newDate, currentDateWithBuffer);
        criteriaQuery.where(criteriaBuilder.and(packageIdPredicate, statusPredicate, durationPredicate));
        criteriaQuery.select(criteriaBuilder.count(packageSubscriptionRoot));
        Long subscriptionCount = entityManager.createQuery(criteriaQuery).getSingleResult();
        if (subscriptionCount == null) {
            return 0L;
        }
        return subscriptionCount;
    }
}
