package com.fitwise.specifications;

import com.fitwise.constants.StringConstants;
import com.fitwise.entity.Programs;
import com.fitwise.entity.payments.stripe.billing.StripeProductAndProgramMapping;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

/*
 * Created by Vignesh G on 29/05/20
 */
public class ProgramSpecifications {

    private ProgramSpecifications() {
    }

    /**
     *
     * @param userId
     * @return
     */
    public static Specification<Programs> getProgramByOwnerUserId(Long userId) {
        return (root, query, criteriaBuilder) -> {
            Expression<Long> expression = root.get("owner").get("userId");
            return criteriaBuilder.equal(expression, userId);
        };
    }

    /**
     *
     * @param status
     * @return
     */
    public static Specification<Programs> getProgramByStatus(String status) {
        return (root, query, criteriaBuilder) -> {
            Expression<String> expression = root.get("status");
            return criteriaBuilder.like(expression, status);
        };
    }

    /**
     *
     * @param titleSearch
     * @return
     */
    public static Specification<Programs> getProgramByTitleContains(String titleSearch) {
        return (root, query, criteriaBuilder) -> {
            Expression<String> expression = root.get("title");
            return criteriaBuilder.like(expression, "%" + titleSearch + "%");
        };
    }

    /**
     * @param expertiseIdList
     * @return
     */
    public static Specification<Programs> getProgramByExpertiseIn(List<Long> expertiseIdList) {
        return (root, query, criteriaBuilder) -> root.get(StringConstants.JSON_PROPERTY_KEY_PROGRAM_EXPERTISE_LEVEL).get("expertiseLevelId").in(expertiseIdList);
    }

    /**
     * @param programTypeIdList
     * @return
     */
    public static Specification<Programs> getProgramByTypeIn(List<Long> programTypeIdList) {
        return (root, query, criteriaBuilder) -> root.get(StringConstants.JSON_PROPERTY_KEY_PROGRAM_TYPE).get("programTypeId").in(programTypeIdList);
    }

    /**
     * @param programTypeId
     * @return
     */
    public static Specification<Programs> getProgramByType(Long programTypeId) {
        return (root, query, criteriaBuilder) -> {
            Expression<Long> expression = root.get(StringConstants.JSON_PROPERTY_KEY_PROGRAM_TYPE).get("programTypeId");
            return criteriaBuilder.equal(expression, programTypeId);
        };
    }

    /**
     * @param durationIdList
     * @return
     */
    public static Specification<Programs> getProgramByDurationIn(List<Long> durationIdList) {
        return (root, query, criteriaBuilder) -> root.get("duration").get("durationId").in(durationIdList);
    }

    /**
     * Specification for Program list based on stripe mapping
     * @return
     */
    public static Specification<Programs> getProgramByStripeMapping() {
        return (root, query, criteriaBuilder) -> {

            CriteriaQuery<Programs> criteriaQuery = (CriteriaQuery<Programs>) query;
            Root<StripeProductAndProgramMapping> stripeMappingRoot = criteriaQuery.from(StripeProductAndProgramMapping.class);

            root.alias("u");
            stripeMappingRoot.alias("p");

            criteriaQuery.select(root);

            //User, UserProfile inner Join criteria
            Predicate fieldEquals = criteriaBuilder.equal(root.get(StringConstants.JSON_PROPERTY_KEY_PROGRAM_ID), stripeMappingRoot.get("program").get(StringConstants.JSON_PROPERTY_KEY_PROGRAM_ID));

            //Name search criteria
            Expression<Boolean> expression = stripeMappingRoot.get("isActive");
            Predicate stripeActive = criteriaBuilder.isTrue(expression);

            Predicate finalPredicate = criteriaBuilder.and(fieldEquals, stripeActive);

            criteriaQuery.where(finalPredicate);
            return criteriaQuery.getRestriction();
        };
    }

    /**
     * @param idList
     * @return
     */
    public static Specification<Programs> getProgramsNotInIdList(List<Long> idList) {
        return (root, query, criteriaBuilder) -> root.get(StringConstants.JSON_PROPERTY_KEY_PROGRAM_ID).in(idList).not();
    }

    /**
     * @param idList
     * @return
     */
    public static Specification<Programs> getProgramsInIdList(List<Long> idList) {
        return (root, query, criteriaBuilder) -> root.get(StringConstants.JSON_PROPERTY_KEY_PROGRAM_ID).in(idList);
    }

    /**
     * Get programs by expertise level
     * @param expertiseLevelId
     * @return
     */
    public static Specification<Programs> getProgramByExpertiseLevelId(Long expertiseLevelId) {
        return (root, query, criteriaBuilder) -> {
            Expression<Long> expression = root.get(StringConstants.JSON_PROPERTY_KEY_PROGRAM_EXPERTISE_LEVEL).get("expertiseLevelId");
            return criteriaBuilder.equal(expression, expertiseLevelId);
        };
    }

}