package com.fitwise.specifications;

import com.fitwise.entity.InstructorPayment;
import com.fitwise.entity.UserProfile;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

/*
 * Created by Vignesh G on 18/01/21
 */
public class InstructorPaymentSpecifications {

    /**
     * @param username
     * @return
     */
    public static Specification<InstructorPayment> findByInstructorName(String username) {

        return (root, query, criteriaBuilder) -> {

            CriteriaQuery<InstructorPayment> criteriaQuery = (CriteriaQuery<InstructorPayment>) query;
            Root<UserProfile> userProfileRoot = criteriaQuery.from(UserProfile.class);

            root.alias("u");
            userProfileRoot.alias("p");

            criteriaQuery.select(root);

            //User, UserProfile inner Join criteria
            Predicate fieldEquals = criteriaBuilder.equal(root.get("instructor").get("userId"), userProfileRoot.get("user").get("userId"));

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
     * @param platformTypeIdList
     * @return
     */
    public static Specification<InstructorPayment> findByPlatformIn(List<Long> platformTypeIdList) {
        return (root, query, criteriaBuilder) -> root.get("orderManagement").get("subscribedViaPlatform").get("platformTypeId").in(platformTypeIdList);
    }

    /**
     * @param isTransferDone
     * @return
     */
    public static Specification<InstructorPayment> findByIsTransferDone(boolean isTransferDone) {
        return (root, query, criteriaBuilder) -> {

            //Name search criteria
            Expression<Boolean> expression = root.get("isTransferDone");
            Predicate isTransferDonePredicate;
            if (isTransferDone) {
                isTransferDonePredicate = criteriaBuilder.isTrue(expression);
            } else {
                isTransferDonePredicate = criteriaBuilder.isFalse(expression);
            }

            return isTransferDonePredicate;
        };
    }

    /**
     * @param isTransferFailed
     * @return
     */
    public static Specification<InstructorPayment> findByIsTransferFailed(boolean isTransferFailed) {
        return (root, query, criteriaBuilder) -> {

            //Name search criteria
            Expression<Boolean> expression = root.get("isTransferFailed");
            Predicate isTransferDonePredicate;
            if (isTransferFailed) {
                isTransferDonePredicate = criteriaBuilder.isTrue(expression);
            } else {
                isTransferDonePredicate = criteriaBuilder.isFalse(expression);
            }

            return isTransferDonePredicate;
        };
    }

    /**
     * @param isTopUpInitiated
     * @return
     */
    public static Specification<InstructorPayment> findByIsTopUpInitiated(boolean isTopUpInitiated) {
        return (root, query, criteriaBuilder) -> {

            //Name search criteria
            Expression<Boolean> expression = root.get("isTopUpInitiated");
            Predicate isTransferDonePredicate;
            if (isTopUpInitiated) {
                isTransferDonePredicate = criteriaBuilder.isTrue(expression);
            } else {
                isTransferDonePredicate = criteriaBuilder.isFalse(expression);
            }

            return isTransferDonePredicate;
        };
    }

}
