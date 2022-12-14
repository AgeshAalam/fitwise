package com.fitwise.specifications.jpa;

import com.fitwise.constants.SecurityFilterConstants;
import com.fitwise.entity.ProgramRating;
import com.fitwise.entity.UserRoleMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.List;

@Component
public class ProgramRatingJPA {

    @Autowired
    private EntityManager entityManager;

    public List<Long> getInstructorIdList(){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);

        Root<UserRoleMapping> userRoleMappingRoot = criteriaQuery.from(UserRoleMapping.class);

        Predicate userRoleExpression = criteriaBuilder.like(userRoleMappingRoot.get("userRole").get("name"), SecurityFilterConstants.ROLE_INSTRUCTOR);

        //Result set expression
        Expression<Long> userIdExpression = userRoleMappingRoot.get("user").get("userId");

        criteriaQuery.where(userRoleExpression);
        criteriaQuery.select(userIdExpression);

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    public List<Long> getUserIdListByUserListAndSubscriptionDateBetween(List<Long> userIdList, Date startDate, Date endDate){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        criteriaQuery.distinct(true);

        Root<ProgramRating> programRatingRoot = criteriaQuery.from(ProgramRating.class);

        Expression<Long> programOwnerIdExpression = programRatingRoot.get("program").get("owner").get("userId");
        Predicate programOwnerIdPredicate = programOwnerIdExpression.in(userIdList);

        //Date criteria
        Expression<Date> modifiedDateExpression = programRatingRoot.get("modifiedDate");
        Predicate startDatePredicate = criteriaBuilder.greaterThanOrEqualTo(modifiedDateExpression, startDate);
        Predicate endDatePredicate = criteriaBuilder.lessThanOrEqualTo(modifiedDateExpression, endDate);

        Predicate finalPredicate = criteriaBuilder.and(programOwnerIdPredicate, startDatePredicate, endDatePredicate);

        //ResultSet expression
        Expression<Long> userIdExpression = programRatingRoot.get("program").get("owner").get("userId");

        criteriaQuery.where(finalPredicate);
        criteriaQuery.select(userIdExpression).groupBy(programRatingRoot.get("program").get("owner").get("userId"));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    /**
     * @param instructorId
     * @param status
     * @return
     */
    public List<Double> getAverageProgramRatingOfInstructor(Long instructorId, String status) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> criteriaQuery = criteriaBuilder.createQuery(Double.class);
        Root<ProgramRating> programRatingRoot = criteriaQuery.from(ProgramRating.class);

        Predicate userIdPredicate = criteriaBuilder.equal(programRatingRoot.get("program").get("owner").get("userId"), instructorId);
        Predicate programStatusPredicate = criteriaBuilder.like(programRatingRoot.get("program").get("status"), status);
        Predicate finalPredicate = criteriaBuilder.and(userIdPredicate, programStatusPredicate);
        criteriaQuery.where(finalPredicate);

        //result set expression
        Expression<Double> programAvgRatingExpression = criteriaBuilder.avg(programRatingRoot.get("programRating"));
        criteriaQuery.select(programAvgRatingExpression);

        criteriaQuery.groupBy(programRatingRoot.get("program").get("programId"));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    /**
     * @param programId
     * @return
     */
    public Double getAverageProgramRating(Long programId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> criteriaQuery = criteriaBuilder.createQuery(Double.class);
        Root<ProgramRating> programRatingRoot = criteriaQuery.from(ProgramRating.class);

        Predicate programIdPredicate = criteriaBuilder.equal(programRatingRoot.get("program").get("programId"), programId);
        criteriaQuery.where(programIdPredicate);

        //result set expression
        Expression<Double> programAvgRatingExpression = criteriaBuilder.avg(programRatingRoot.get("programRating"));
        criteriaQuery.select(programAvgRatingExpression);

        return entityManager.createQuery(criteriaQuery).getSingleResult();
    }

}