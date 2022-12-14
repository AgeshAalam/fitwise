package com.fitwise.specifications;

import com.fitwise.entity.ProgramPromoViews;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.subscription.SubscriptionAudit;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.List;

public class SubscriptionAuditSpecifications {

    /**
     * Get subscriptions by type(program/package)
     *
     * @param type
     * @return
     */
    public static Specification<SubscriptionAudit> getSubscriptionByType(String type) {
        return (root, query, criteriaBuilder) -> {

            Expression<String> expression = root.get("subscriptionType").get("name");

            return criteriaBuilder.like(expression, type);
        };
    }

    /**
     * Get subscriptions based on a status
     *
     * @param statusList
     * @return
     */
    public static Specification<SubscriptionAudit> getSubscriptionBySubscriptionStatusList(List<String> statusList) {
        return (root, query, criteriaBuilder) -> root.get("subscriptionStatus").get("subscriptionStatusName").in(statusList);
    }


    /**
     * Get subscriptions based on a renewal status
     *
     * @param status
     * @return
     */
    public static Specification<SubscriptionAudit> getSubscriptionByRenewalStatus(String status) {
        return (root, query, criteriaBuilder) -> {

            Expression<String> expression = root.get("renewalStatus");

            return criteriaBuilder.like(expression, status);
        };
    }

    /**
     * Get subscriptions in a given period
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static Specification<SubscriptionAudit> getSubscriptionsBetWeen(Date startDate, Date endDate) {
        return (root, query, criteriaBuilder) -> {


            Expression<Date> dateExpression = root.get("createdDate");

            //Date criteria
            Predicate startDateCriteria = criteriaBuilder.greaterThanOrEqualTo(dateExpression, startDate);
            Predicate endDateCriteria = criteriaBuilder.lessThanOrEqualTo(dateExpression, endDate);

            Predicate finalPredicate = criteriaBuilder.and(startDateCriteria, endDateCriteria);
            query.where(finalPredicate);
            return query.getRestriction();
        };
    }

    /**
     * Get subscriptions by programId
     *
     * @param programId
     * @return
     */
    public static Specification<SubscriptionAudit> getProgramSubscriptionByProgramId(Long programId) {
        return (root, query, criteriaBuilder) -> {

            Expression<Long> expression = root.get("programSubscription").get("program").get("programId");

            return criteriaBuilder.equal(expression, programId);
        };
    }

    /**
     * Get subscriptions based on a status
     *
     * @param status
     * @return
     */
    public static Specification<SubscriptionAudit> getProgramSubscriptionBySubscriptionStatus(String status) {
        return (root, query, criteriaBuilder) -> {

            Expression<String> expression = root.get("subscriptionStatus").get("subscriptionStatusName");

            return criteriaBuilder.like(expression, status);
        };
    }

    /**
     * Sort by user name
     * @return
     */
    public static Specification<SubscriptionAudit> getSubscriptionsSortByUserName() {
        return (root, query, criteriaBuilder) -> {

            CriteriaQuery<SubscriptionAudit> criteriaQuery = (CriteriaQuery<SubscriptionAudit>) query;
            Root<UserProfile> userProfileRoot = criteriaQuery.from(UserProfile.class);

            criteriaQuery.select(root);

            //User, UserProfile inner Join criteria
            Predicate fieldEquals = criteriaBuilder.equal(root.get("user"), userProfileRoot.get("user"));
            criteriaQuery.where(fieldEquals);

            Expression<Long> idExpression = root.get("user").get("userId");

            Expression<String> nameExpression = criteriaBuilder.concat(userProfileRoot.get("firstName"), " ");
            nameExpression = criteriaBuilder.concat(nameExpression, userProfileRoot.get("lastName"));
            criteriaQuery.orderBy(criteriaBuilder.asc(nameExpression), criteriaBuilder.asc(idExpression));

            return criteriaQuery.getRestriction();
        };
    }

    /**
     * Group by users
     * @return
     */
    public static Specification<SubscriptionAudit> getSubscriptiosGroupByUser() {
        return (root, query, criteriaBuilder) -> {

            query.groupBy(root.get("user"));
            return query.getRestriction();
        };
    }


}
