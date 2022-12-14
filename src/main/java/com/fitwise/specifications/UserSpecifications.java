package com.fitwise.specifications;

import java.util.List;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.fitwise.constants.DBFieldConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.UserRoleMapping;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.payments.stripe.StripeProductAndPackageMapping;
import com.fitwise.entity.payments.stripe.billing.StripeProductAndProgramMapping;

/*
 * Created by Vignesh G on 24/06/20
 */
@Service
public class UserSpecifications {

    /**
     * Get user list by name contains
     * @param username
     * @return
     */
    public static Specification<User> getUserByName(String username) {

        return (root, query, criteriaBuilder) -> {

            CriteriaQuery<User> criteriaQuery = (CriteriaQuery<User>) query;
            Root<UserProfile> userProfileRoot = criteriaQuery.from(UserProfile.class);

            root.alias("u");
            userProfileRoot.alias("profile");

            criteriaQuery.select(root);

            //User, UserProfile inner Join criteria
            Predicate fieldEquals = criteriaBuilder.equal(root.get("userId"), userProfileRoot.get("user").get("userId"));

            //Name search criteria
            Expression<String> expression = criteriaBuilder.concat(userProfileRoot.get("firstName"), " ");
            expression = criteriaBuilder.concat(expression, userProfileRoot.get("lastName"));
            Predicate nameSearch = criteriaBuilder.like(expression, "%" + username + "%");

            Predicate finalPredicate = criteriaBuilder.and(fieldEquals, nameSearch);

            criteriaQuery.where(finalPredicate);
            return criteriaQuery.getRestriction();
        };
    }

    public static Specification<User> orderBy(String order) {
        return (root, query, criteriaBuilder) -> {
            CriteriaQuery<User> criteriaQuery = (CriteriaQuery<User>) query;
            Root<UserProfile> userProfileRoot = criteriaQuery.from(UserProfile.class);
            root.alias("u");
            userProfileRoot.alias("profile");
            criteriaQuery.select(root);
            //User, UserProfile inner Join criteria
            Predicate fieldEquals = criteriaBuilder.equal(root.get("userId"), userProfileRoot.get("user").get("userId"));
            //Name search criteria
            Expression<String> expression = criteriaBuilder.concat(userProfileRoot.get("firstName"), " ");
            expression = criteriaBuilder.concat(expression, userProfileRoot.get("lastName"));
            if(order.equalsIgnoreCase(SearchConstants.ORDER_DSC)){
                criteriaQuery.orderBy(criteriaBuilder.desc(expression));
            }else {
                criteriaQuery.orderBy(criteriaBuilder.asc(expression));
            }
            return criteriaQuery.where(fieldEquals).getRestriction();
        };
    }

    /**
     * Get users based on role id
     * @param roleId
     * @return
     */
    public static Specification<User> getUserByRoleId(Long roleId) {

        return (root, query, criteriaBuilder) -> {

            CriteriaQuery<User> criteriaQuery = (CriteriaQuery<User>) query;
            Root<UserRoleMapping> userRoleRoot = criteriaQuery.from(UserRoleMapping.class);

            criteriaQuery.select(root);

            //User, UserProfile inner Join criteria
            Predicate fieldEquals = criteriaBuilder.equal(root.get("userId"), userRoleRoot.get("user").get("userId"));

            //role id criteria
            Expression<Long> expression = userRoleRoot.get("userRole").get("roleId");
            Predicate roleEquals = criteriaBuilder.equal(expression, roleId);

            Predicate finalPredicate = criteriaBuilder.and(fieldEquals, roleEquals);

            criteriaQuery.where(finalPredicate);
            return criteriaQuery.getRestriction();
        };
    }

    /**
     * Get users with Id not in user id list
     * @param idList
     * @return
     */
    public static Specification<User> getUsersNotInIdList(List<Long> idList) {
        return (root, query, criteriaBuilder) -> root.get("userId").in(idList).not();
    }

    public static Specification<User> getUsersIdIn(List<Long> idList) {
        return (root, query, criteriaBuilder) -> root.get("userId").in(idList);
    }


    public static Specification<User> getUserByPublishPrograms() {

        return (root, query, criteriaBuilder) -> {

            Subquery<User> subQuery = query.subquery(User.class);
            Root<User> subqueryUserRoot = subQuery.from(User.class);
            subQuery.select(subqueryUserRoot);

            Root<Programs> subqueryProgramRoot = subQuery.from(Programs.class);
            Predicate fieldEquals = criteriaBuilder.equal(subqueryUserRoot.get("userId"), subqueryProgramRoot.get("owner").get("userId"));

            Expression<String> statusExpression = subqueryProgramRoot.get("status");
            Predicate statusCriteria = criteriaBuilder.like(statusExpression, KeyConstants.KEY_PUBLISH);

            Predicate finalPredicate = criteriaBuilder.and(fieldEquals, statusCriteria);

            subQuery.where(finalPredicate);

            return criteriaBuilder.exists(subQuery);

        };
    }

    public static Specification<User> getUserByEmailContains(String emailSearch) {
        return (root, query, criteriaBuilder) -> {

            Expression<String> expression = root.get("email");

            return criteriaBuilder.like(expression, "%" + emailSearch + "%");
        };
    }

    public static Specification<User> getUserWithOnlyPublishedPrograms(){
        return (root, query, criteriaBuilder) -> {

            CriteriaQuery<User> criteriaQuery = (CriteriaQuery<User>) query;
            Root<Programs> programsRoot = criteriaQuery.from(Programs.class);

            root.alias("u");
            programsRoot.alias("p");

            criteriaQuery.select(root);

            //User, UserProfile inner Join criteria
            Predicate fieldEquals = criteriaBuilder.equal(root.get("userId"), programsRoot.get("owner").get("userId"));

            //Name search criteria
            Predicate nameSearch = criteriaBuilder.equal(programsRoot.get("status"), "Published");

            Predicate finalPredicate = criteriaBuilder.and(fieldEquals, nameSearch);

            criteriaQuery.where(finalPredicate);
            return criteriaQuery.getRestriction();
        };
    }

    public static Specification<User> getUserOnlyPublishedPrograms(){
        return (root, query, criteriaBuilder) -> {

            CriteriaQuery<User> criteriaQuery = (CriteriaQuery<User>) query;
            Root<Programs> programsRoot = criteriaQuery.from(Programs.class);

            root.alias("u");
            programsRoot.alias("p");

            criteriaQuery.select(root);

            //User, UserProfile inner Join criteria
            Predicate fieldEquals = criteriaBuilder.equal(root.get("userId"), programsRoot.get("owner").get("userId"));

            //Name search criteria
            Predicate nameSearch = criteriaBuilder.equal(programsRoot.get("status"), "Published");

            Predicate finalPredicate = criteriaBuilder.and(fieldEquals, nameSearch);

            criteriaQuery.where(finalPredicate);
            return criteriaQuery.getRestriction();
        };
    }

    /**
     * Get Specification to get only users with published programs
     * @return
     */
    public static Specification<User> getInstructorsByPublishPrograms() {
        return (root, query, criteriaBuilder) -> {
            CriteriaQuery<User> criteriaQuery = (CriteriaQuery<User>) query;
            Root<Programs> programRoot = criteriaQuery.from(Programs.class);
            root.alias("u");
            programRoot.alias("p");
            criteriaQuery.select(root);
            criteriaQuery.distinct(true);
            Predicate fieldEquals = criteriaBuilder.equal(root.get("userId"), programRoot.get("owner").get("userId"));
            Expression<String> expression = programRoot.get("status");
            Predicate nameSearch = criteriaBuilder.equal(expression, KeyConstants.KEY_PUBLISH);
			Predicate finalPredicate = criteriaBuilder.and(fieldEquals, nameSearch);
            criteriaQuery.where(finalPredicate);
            return criteriaQuery.getRestriction();
        };
    }
    
	/**
	 * Gets the instructors by published subriction packages.
	 *
	 * @return the instructors by published subriction packages
	 */
	public static Specification<User> getInstructorsByPublishedSubrictionPackages() {
		return (root, query, criteriaBuilder) -> {
			CriteriaQuery<User> criteriaQuery = (CriteriaQuery<User>) query;
			Root<SubscriptionPackage> packageRoot = criteriaQuery.from(SubscriptionPackage.class);
			criteriaQuery.select(root);
			criteriaQuery.distinct(true);
			Predicate fieldEquals = criteriaBuilder.equal(root.get(DBFieldConstants.FIELD_USER_ID),
					packageRoot.get(DBFieldConstants.FIELD_OWNER).get(DBFieldConstants.FIELD_USER_ID));
			Expression<String> expression = packageRoot.get(SearchConstants.STATUS);
			Predicate statusSearch = criteriaBuilder.equal(expression, KeyConstants.KEY_PUBLISH);
			Predicate finalPredicate = criteriaBuilder.and(fieldEquals, statusSearch);
			criteriaQuery.where(finalPredicate);
			return criteriaQuery.getRestriction();
		};
	}

}
