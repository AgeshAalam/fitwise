package com.fitwise.specifications;

import com.fitwise.entity.ProgramPromoViews;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;

public class ProgrampromoViewSpecifications {


    /**
     * Get unique user views
     *
     * @param programId
     * @return
     */
    public static Specification<ProgramPromoViews> getPromoViewsGroupByUser(Long programId) {
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
     * Get promo views by sorting user name
     *
     * @return
     */
    public static Specification<ProgramPromoViews> getPromoiewsSortByUserName() {
        return (root, query, criteriaBuilder) -> {

            CriteriaQuery<ProgramPromoViews> criteriaQuery = (CriteriaQuery<ProgramPromoViews>) query;
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
     * Get promo views between given period
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static Specification<ProgramPromoViews> getProgramViewsBetWeen(Date startDate, Date endDate) {
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
     * Get unique program views in a given period and sort by user name
     *
     * @param programId
     * @param startDate
     * @param endDate
     * @return
     */
    public static Specification<ProgramPromoViews> getUniqueProgramViewsBetWeenAndSortByUserName(Long programId, Date startDate, Date endDate) {

        Specification<ProgramPromoViews> finalSpec = getPromoiewsSortByUserName().and(getProgramViewsBetWeen(startDate, endDate).and(getPromoViewsGroupByUser(programId)));
        return finalSpec;
    }


}


