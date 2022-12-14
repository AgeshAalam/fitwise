package com.fitwise.specifications;

import com.fitwise.constants.DBFieldConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.entity.FeaturedInstructors;
import com.fitwise.entity.Programs;
import com.fitwise.entity.packaging.SubscriptionPackage;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

/*
 * Created by Vignesh G on 24/06/20
 */
@Service
public class FeaturedInstructorSpecifications {


    /**
     * Get users with Id not in user id list
     * @param idList
     * @return
     */
    public static Specification<FeaturedInstructors> getUsersNotInIdList(List<Long> idList) {
        return (root, query, criteriaBuilder) -> root.get("user").get("userId").in(idList).not();
    }

    public static Specification<FeaturedInstructors> getInstructorsByPublishPrograms() {
        return (root, query, criteriaBuilder) -> {
            CriteriaQuery<FeaturedInstructors> criteriaQuery = (CriteriaQuery<FeaturedInstructors>) query;
            Root<Programs> programRoot = criteriaQuery.from(Programs.class);
            root.alias("u");
            programRoot.alias("p");
            Predicate fieldEquals = criteriaBuilder.equal(root.get("user").get("userId"), programRoot.get("owner").get("userId"));
            Expression<String> expression = programRoot.get("status");
            Predicate nameSearch = criteriaBuilder.equal(expression, KeyConstants.KEY_PUBLISH);
			Predicate finalPredicate = criteriaBuilder.and(fieldEquals, nameSearch);
            criteriaQuery.distinct(true);
            criteriaQuery.where(finalPredicate);
            criteriaQuery.orderBy(criteriaBuilder.asc(root.get("id")));
            return criteriaQuery.getRestriction();
        };
    }
    
	/**
	 * Gets the instructors by publish subriction packages.
	 *
	 * @return the instructors by publish subriction packages
	 */
	public static Specification<FeaturedInstructors> getInstructorsByPublishedSubrictionPackages() {
		return (root, query, criteriaBuilder) -> {
			CriteriaQuery<FeaturedInstructors> criteriaQuery = (CriteriaQuery<FeaturedInstructors>) query;
			Root<SubscriptionPackage> packageRoot = criteriaQuery.from(SubscriptionPackage.class);
			Predicate fieldEquals = criteriaBuilder.equal(
					root.get(DBFieldConstants.FIELD_USER).get(DBFieldConstants.FIELD_USER_ID),
					packageRoot.get(DBFieldConstants.FIELD_OWNER).get(DBFieldConstants.FIELD_USER_ID));
			Expression<String> expression = packageRoot.get(SearchConstants.STATUS);
			Predicate statusSearch = criteriaBuilder.equal(expression, KeyConstants.KEY_PUBLISH);
			Predicate finalPredicate = criteriaBuilder.and(fieldEquals, statusSearch);
			criteriaQuery.distinct(true);
			criteriaQuery.where(finalPredicate);
			criteriaQuery.orderBy(criteriaBuilder.asc(root.get(DBFieldConstants.FIELD_ID)));
			return criteriaQuery.getRestriction();
		};
	}
}
