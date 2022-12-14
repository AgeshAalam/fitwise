package com.fitwise.specifications;

import com.fitwise.constants.SearchConstants;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.subscription.PackageSubscription;
import com.fitwise.entity.subscription.SubscriptionPlan;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Date;
import java.util.List;

public class PackageSubscriptionSpcifications {

    private static Specification<PackageSubscription> getPackageSubscriptionByOwnerUserId(Long userId) {
        return (root, query, criteriaBuilder) -> {

            Expression<Long> expression = root.get("subscriptionPackage").get("owner").get("userId");

            return criteriaBuilder.equal(expression, userId);
        };
    }

    private static Specification<PackageSubscription> getPackageSubscriptionByUserId(Long userId) {
        return (root, query, criteriaBuilder) -> {
            Expression<Long> expression = root.get("user").get("userId");
            return criteriaBuilder.equal(expression, userId);
        };
    }

    /**
     * @param statusList
     * @return
     */
    private static Specification<PackageSubscription> getPackageSubscriptionBySubscriptionStatus(List<String> statusList) {
        return (root, query, criteriaBuilder) -> root.get("subscriptionStatus").get("subscriptionStatusName").in(statusList);
    }

    /**
     * @param emailSearch
     * @return
     */
    private static Specification<PackageSubscription> getPackageSubscriptionByMemberEmail(String emailSearch) {
        return (root, query, criteriaBuilder) -> {

            Expression<String> expression = root.get("user").get("email");

            return criteriaBuilder.like(expression, "%" + emailSearch + "%");
        };
    }

    private static Specification<PackageSubscription> getPackageSubscriptionByTitle(String title) {
        return (root, query, criteriaBuilder) -> {
            Expression<String> expression = root.get("subscriptionPackage").get("title");
            return criteriaBuilder.like(expression, "%" + title + "%");
        };
    }

    /**
     * @param username
     * @return
     */
    private static Specification<PackageSubscription> getPackageSubscriptionByMemberName(String username) {
        return (root, query, criteriaBuilder) -> {

            CriteriaQuery<PackageSubscription> criteriaQuery = (CriteriaQuery<PackageSubscription>) query;
            Root<UserProfile> userProfileRoot = criteriaQuery.from(UserProfile.class);

            root.alias("u");
            userProfileRoot.alias("p");

            criteriaQuery.select(root);

            //User, UserProfile inner Join criteria
            Predicate fieldEquals = criteriaBuilder.equal(root.get("user").get("userId"), userProfileRoot.get("user").get("userId"));

            //Name search criteria
            Expression<String> expression = criteriaBuilder.concat(userProfileRoot.get("firstName"), " ");
            expression = criteriaBuilder.concat(expression, userProfileRoot.get("lastName"));
            Predicate nameSearch = criteriaBuilder.like(expression, "%" + username + "%");

            Predicate finalPredicate = criteriaBuilder.and(fieldEquals, nameSearch);

            criteriaQuery.where(finalPredicate);
            return criteriaQuery.getRestriction();
        };
    }

    /**
     * @param userId
     * @param statusList
     * @param search
     * @return
     */
    public static Specification<PackageSubscription> getPackageSubscriptionByOwnerAndStatusAndMemberSearch(Long userId, List<String> statusList, String search) {
        Specification<PackageSubscription> finalSpec = getPackageSubscriptionByOwnerUserId(userId).and(getPackageSubscriptionBySubscriptionStatus(statusList));
        if (!search.isEmpty()) {
            finalSpec = finalSpec.and(getPackageSubscriptionByMemberEmail(search).or(getPackageSubscriptionByMemberName(search)));
        }
        return finalSpec;
    }

    public static Specification<PackageSubscription> getPackageSubscriptionByUserAndStatusAndTitleSearch(Long userId, List<String> statusList, String search) {
        Specification<PackageSubscription> finalSpec = getPackageSubscriptionByUserId(userId).and(getPackageSubscriptionBySubscriptionStatus(statusList));
        if (!search.isEmpty()) {
            finalSpec = finalSpec.and(getPackageSubscriptionByTitle(search));
        }
        return finalSpec;
    }

    public static Specification<PackageSubscription> getPackageSubscriptionBySubscriptionActive() {
        return (root, query, criteriaBuilder) -> {
            CriteriaQuery<PackageSubscription> criteriaQuery = (CriteriaQuery<PackageSubscription>) query;
            Root<SubscriptionPlan> subscriptionPlanRoot = criteriaQuery.from(SubscriptionPlan.class);
            root.alias("u");
            subscriptionPlanRoot.alias("sp");
            criteriaQuery.select(root);
            criteriaQuery.distinct(true);
            Predicate fieldEquals = criteriaBuilder.equal(root.get("subscriptionPlan").get("duration"), subscriptionPlanRoot.get("duration"));

            Expression<Integer> diff = criteriaBuilder.function("DATEDIFF", Integer.class, criteriaBuilder.currentDate(), root.get("subscribedDate"));
            Predicate durationPredicate = criteriaBuilder.lessThanOrEqualTo(diff, subscriptionPlanRoot.get("duration"));
            Predicate finalPredicate = criteriaBuilder.and(fieldEquals, durationPredicate);
            criteriaQuery.where(finalPredicate);
            return criteriaQuery.getRestriction();
        };
    }

    public static Specification<PackageSubscription> getPackageSubscriptionBySubscriptionExpiired() {
        return (root, query, criteriaBuilder) -> {
            CriteriaQuery<PackageSubscription> criteriaQuery = (CriteriaQuery<PackageSubscription>) query;
            Root<SubscriptionPlan> subscriptionPlanRoot = criteriaQuery.from(SubscriptionPlan.class);
            root.alias("u");
            subscriptionPlanRoot.alias("sp");
            criteriaQuery.select(root);
            criteriaQuery.distinct(true);
            Predicate fieldEquals = criteriaBuilder.equal(root.get("subscriptionPlan").get("duration"), subscriptionPlanRoot.get("duration"));
            Expression<Integer> diff = criteriaBuilder.function("DATEDIFF", Integer.class, criteriaBuilder.currentDate(), root.get("subscribedDate"));
            Predicate durationPredicate = criteriaBuilder.greaterThan(diff, subscriptionPlanRoot.get("duration"));
            Predicate finalPredicate = criteriaBuilder.and(fieldEquals, durationPredicate);
            criteriaQuery.where(finalPredicate);
            return criteriaQuery.getRestriction();
        };
    }

    public static Specification<PackageSubscription> getPackageSubscriptionByUserAndStatusAndMemberSearchWithActive(Long userId, List<String> statusList, String search) {
        Specification<PackageSubscription> finalSpec = getPackageSubscriptionByUserAndStatusAndTitleSearch(userId, statusList, search);
        return finalSpec.and(getPackageSubscriptionBySubscriptionActive());
    }

    public static Specification<PackageSubscription> getPackageSubscriptionByUserAndStatusAndMemberSearchWithExpired(Long userId, List<String> statusList, String search) {
        Specification<PackageSubscription> finalSpec = getPackageSubscriptionByUserAndStatusAndTitleSearch(userId, statusList, search);
        return finalSpec.and(getPackageSubscriptionBySubscriptionExpiired());
    }

    public static Specification<PackageSubscription> orderBySubscibedDate(String order) {
        return (root, query, criteriaBuilder) -> {

            Expression<Date> expression = root.get("subscribedDate");
            if (order.equalsIgnoreCase(SearchConstants.ORDER_DSC)) {
                query.orderBy(criteriaBuilder.desc(expression));
            } else {
                query.orderBy(criteriaBuilder.asc(expression));
            }
            return query.getRestriction();
        };
    }

    public static Specification<PackageSubscription> getPackageSubscriptionByUserAndStatusAndWithActiveOrderByDate(Long userId, List<String> statusList, String order) {
        Specification<PackageSubscription> finalSpec = getPackageSubscriptionByUserId(userId).and(getPackageSubscriptionBySubscriptionStatus(statusList)).and(orderBySubscibedDate(order));
        return finalSpec.and(getPackageSubscriptionBySubscriptionActive());
    }
}
