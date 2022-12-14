package com.fitwise.specifications;

import com.fitwise.constants.StringConstants;
import com.fitwise.entity.InstructorProgramExperience;
import com.fitwise.entity.UserProfile;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

/*
 * Created by Vignesh G on 25/06/20
 */
public class InstructorProgramExperienceSpecifications {

    /**
     * Get InstructorProgramExperience list by name contains
     * @param username
     * @return
     */
    public static Specification<InstructorProgramExperience> getInstructorProgramExperienceByName(String username) {

        return (root, query, criteriaBuilder) -> {

            CriteriaQuery<InstructorProgramExperience> criteriaQuery = (CriteriaQuery<InstructorProgramExperience>) query;
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
     * Get InstructorProgramExperience list by programTypeIdList
     * @param programTypeIdList
     * @return
     */
    public static Specification<InstructorProgramExperience> getInstructorProgramExperienceByProgramTypeIn(List<Long> programTypeIdList) {
        return (root, query, criteriaBuilder) -> {

            //to remove duplicate instructors
            query.groupBy(root.get("user").get("userId"));

            Predicate idInCriteria = root.get(StringConstants.JSON_PROPERTY_KEY_PROGRAM_TYPE).get("programTypeId").in(programTypeIdList);
            query.where(idInCriteria);
            return query.getRestriction();
        };
    }

    /**
     * Get InstructorProgramExperience list not in user id list
     * @param userIdList
     * @return
     */
    public static Specification<InstructorProgramExperience> getInstructorsNotInUserList(List<Long> userIdList) {
        return (root, query, criteriaBuilder) -> root.get("user").get("userId").in(userIdList).not();
    }

}
