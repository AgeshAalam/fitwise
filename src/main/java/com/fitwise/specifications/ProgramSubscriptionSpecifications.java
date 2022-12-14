package com.fitwise.specifications;

import com.fitwise.entity.UserProfile;
import com.fitwise.entity.subscription.ProgramSubscription;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

/*
 * Created by Vignesh G on 06/10/20
 */
public class ProgramSubscriptionSpecifications {

    /**
     * @param userId
     * @return
     */
    private static Specification<ProgramSubscription> getProgramSubscriptionByOwnerUserId(Long userId) {
        return (root, query, criteriaBuilder) -> {

            Expression<Long> expression = root.get("program").get("owner").get("userId");

            return criteriaBuilder.equal(expression, userId);
        };
    }

    /**
     * @param statusList
     * @return
     */
    private static Specification<ProgramSubscription> getProgramSubscriptionBySubscriptionStatus(List<String> statusList) {
        return (root, query, criteriaBuilder) -> root.get("subscriptionStatus").get("subscriptionStatusName").in(statusList);
    }

    /**
     * @param emailSearch
     * @return
     */
    private static Specification<ProgramSubscription> getProgramSubscriptionByMemberEmail(String emailSearch) {
        return (root, query, criteriaBuilder) -> {

            Expression<String> expression = root.get("user").get("email");

            return criteriaBuilder.like(expression, "%" + emailSearch + "%");
        };
    }

    /**
     * @param username
     * @return
     */
    private static Specification<ProgramSubscription> getProgramSubscriptionByMemberName(String username) {
        return (root, query, criteriaBuilder) -> {

            CriteriaQuery<ProgramSubscription> criteriaQuery = (CriteriaQuery<ProgramSubscription>) query;
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
    public static Specification<ProgramSubscription> getProgramSubscriptionByOwnerAndStatusAndMemberSearch(Long userId, List<String> statusList, String search) {
        Specification<ProgramSubscription> finalSpec = getProgramSubscriptionByOwnerUserId(userId).and(getProgramSubscriptionBySubscriptionStatus(statusList));

        finalSpec = finalSpec.and(getProgramSubscriptionByMemberEmail(search).or(getProgramSubscriptionByMemberName(search)));

        return finalSpec;
    }


}
