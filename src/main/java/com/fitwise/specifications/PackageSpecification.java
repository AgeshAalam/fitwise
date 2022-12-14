package com.fitwise.specifications;

import com.fitwise.constants.StringConstants;
import com.fitwise.entity.Programs;
import com.fitwise.entity.packaging.PackageProgramMapping;
import com.fitwise.entity.packaging.PackageKloudlessMapping;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.payments.stripe.StripeProductAndPackageMapping;
import com.fitwise.entity.payments.stripe.billing.StripeProductAndProgramMapping;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

public class PackageSpecification {

    public static Specification<SubscriptionPackage> getSubscriptionPackageByTitle(String subscriptionPackage){
        return (root, criteriaQuery, criteriaBuilder) -> {
            Expression<String> stringExpression = root.get("title");

            return criteriaBuilder.like(stringExpression, "%" +subscriptionPackage + "%");
        };
    }

    public static Specification<SubscriptionPackage> getSubscriptionPackageByStatus(String status){
        return (root, criteriaQuery, criteriaBuilder) -> {
            Expression<String> stringExpression = root.get("status");

            return criteriaBuilder.like(stringExpression, status);
        };
    }

    public static Specification<SubscriptionPackage> getSubscriptionPackageBySessionType(List<Long> meetingTypeIdList){
        return (root, criteriaQuery, criteriaBuilder) -> {
            CriteriaQuery<SubscriptionPackage> packageCriteriaQuery = (CriteriaQuery<SubscriptionPackage>) criteriaQuery;
            Root<PackageKloudlessMapping> packageKloudlessMappingRoot = packageCriteriaQuery.from(PackageKloudlessMapping.class);

            root.alias("u");
            packageKloudlessMappingRoot.alias("k");

            packageCriteriaQuery.select(root);
            packageCriteriaQuery.distinct(true);

            Predicate fieldEquals = criteriaBuilder.equal(root.get("subscriptionPackageId"), packageKloudlessMappingRoot.get("subscriptionPackage").get("subscriptionPackageId"));

            Predicate sessionTypeIdEqualsPredicate = packageKloudlessMappingRoot.get("userKloudlessMeeting").get("calendarMeetingType").get("meetingTypeId").in(meetingTypeIdList);

            Predicate finalPredicate = criteriaBuilder.and(fieldEquals, sessionTypeIdEqualsPredicate);

            criteriaQuery.where(finalPredicate);
            return packageCriteriaQuery.getRestriction();
        };
    }

    public static Specification<SubscriptionPackage> getSubscriptionPackageByProgramType(List<Long> programTypeIdList){
        return (root, criteriaQuery, criteriaBuilder) -> {
            CriteriaQuery<SubscriptionPackage> packageCriteriaQuery = (CriteriaQuery<SubscriptionPackage>) criteriaQuery;
            Root<PackageProgramMapping> packageProgramMappingRoot = packageCriteriaQuery.from(PackageProgramMapping.class);

            root.alias("u");
            packageProgramMappingRoot.alias("m");

            packageCriteriaQuery.select(root);
            packageCriteriaQuery.distinct(true);

            Predicate fieldEquals = criteriaBuilder.equal(root.get("subscriptionPackageId"), packageProgramMappingRoot.get("subscriptionPackage").get("subscriptionPackageId"));

            Predicate programTypeIdEqualsPredicate = packageProgramMappingRoot.get("program").get(StringConstants.JSON_PROPERTY_KEY_PROGRAM_TYPE).get("programTypeId").in(programTypeIdList);

            Predicate finalPredicate = criteriaBuilder.and(fieldEquals, programTypeIdEqualsPredicate);

            criteriaQuery.where(finalPredicate);
            return packageCriteriaQuery.getRestriction();
        };
    }

    /**
     * Specification for Subscription Package list based on stripe mapping
     * @return
     */
    public static Specification<SubscriptionPackage> getPackageByStripeMapping() {
        return (root, query, criteriaBuilder) -> {

            CriteriaQuery<SubscriptionPackage> criteriaQuery = (CriteriaQuery<SubscriptionPackage>) query;
            Root<StripeProductAndPackageMapping> stripeMappingRoot = criteriaQuery.from(StripeProductAndPackageMapping.class);

            root.alias("u");
            stripeMappingRoot.alias("p");

            criteriaQuery.select(root);
            criteriaQuery.distinct(true);

            //User, UserProfile inner Join criteria
            Predicate fieldEquals = criteriaBuilder.equal(root.get("subscriptionPackageId"), stripeMappingRoot.get("subscriptionPackage").get("subscriptionPackageId"));

            //Name search criteria
            Expression<Boolean> expression = stripeMappingRoot.get("isActive");
            Predicate stripeActive = criteriaBuilder.isTrue(expression);

            Predicate finalPredicate = criteriaBuilder.and(fieldEquals, stripeActive);

            criteriaQuery.where(finalPredicate);
            return criteriaQuery.getRestriction();
        };
    }

    public static Specification<SubscriptionPackage> getPackageByOwnerUserId(Long userId) {
        return (root, query, criteriaBuilder) -> {

            Expression<Long> expression = root.get("owner").get("userId");

            return criteriaBuilder.equal(expression, userId);
        };
    }

    public static Specification<SubscriptionPackage> getNonRestrictedPackages() {
        return (root, query, criteriaBuilder) -> {

            Expression<Boolean> expression = root.get("isRestrictedAccess");

            return criteriaBuilder.isFalse(expression);
        };
    }

    public static Specification<SubscriptionPackage> getPackageIdsIn(List<Long> packageIds) {
        return (root, query, criteriaBuilder) -> root.get("subscriptionPackageId").in(packageIds);
    }



}
