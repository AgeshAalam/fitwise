package com.fitwise.specifications;

import com.fitwise.entity.ProgramViewsAudit;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;

public class ProgramViewAuditSpecifications {

    /**
     * get unique program views
     * @param programId
     * @return
     */
    public static Specification<ProgramViewsAudit> getProgramViewsGroupByUser(Long programId) {
        return (root, query, criteriaBuilder) -> {


            Expression<Long> programExpression = root.get("program").get("programId");
            Expression<User> userExpression = root.get("user");

            //to remove duplicate user id
            query.groupBy(root.get("user"));


            //Program id criteria
            Predicate programIdCriteria = criteriaBuilder.equal(programExpression, programId);
            Predicate userNotNullCriteria = criteriaBuilder.isNotNull(userExpression);


            Predicate finalPredicate = criteriaBuilder.and(programIdCriteria, userNotNullCriteria);
            query.where(finalPredicate);
            query.select(root.get("user")).distinct(true);

            return query.getRestriction();
        };
    }


    /**
     * Sort by user name
     * @return
     */
    public static Specification<ProgramViewsAudit> getProgramViewsSortByUserName() {
        return (root, query, criteriaBuilder) -> {

            CriteriaQuery<ProgramViewsAudit> criteriaQuery = (CriteriaQuery<ProgramViewsAudit>) query;
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
     * get views between given time
     * @param startDate
     * @param endDate
     * @return
     */
    public static Specification<ProgramViewsAudit> getProgramViewsBetWeen(Date startDate, Date endDate) {
        return (root, query, criteriaBuilder) -> {


            Expression<Date> dateExpression = root.get("date");

            //Date criteria
            Predicate startDateCriteria = criteriaBuilder.greaterThanOrEqualTo(dateExpression, startDate);
            Predicate endDateCriteria = criteriaBuilder.lessThanOrEqualTo(dateExpression, endDate);

            Predicate finalPredicate = criteriaBuilder.and(startDateCriteria, endDateCriteria);
            query.where(finalPredicate);
            return query.getRestriction();
        };
    }

    /**
     * Get unique user views between and sort by name
     * @param programId
     * @param startDate
     * @param endDate
     * @return
     */
    public static Specification<ProgramViewsAudit> getUniqueProgramViewsBetWeenAndSortByUserName(Long programId, Date startDate, Date endDate) {

        Specification<ProgramViewsAudit> finalSpec = getProgramViewsSortByUserName().and(getProgramViewsBetWeen(startDate,endDate).and(getProgramViewsGroupByUser(programId)));
        return finalSpec;
    }

    public static Specification<ProgramViewsAudit> getUniqueProgramViewsBetWeen(Long programId, Date startDate, Date endDate) {

        Specification<ProgramViewsAudit> finalSpec = getProgramViewsGroupByUser(programId).and(getProgramViewsBetWeen(startDate,endDate));
        return finalSpec;
    }


}
