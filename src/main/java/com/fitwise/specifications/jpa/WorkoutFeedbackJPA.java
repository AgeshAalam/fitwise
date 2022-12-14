package com.fitwise.specifications.jpa;

import com.fitwise.constants.KeyConstants;
import com.fitwise.entity.InstructorPayment;
import com.fitwise.entity.WorkoutFeedback;
import com.fitwise.specifications.jpa.dao.InstructorPayout;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WorkoutFeedbackJPA {

    final private EntityManager entityManager;

    public List<Tuple> getWorkoutFeedbackCountByType(final Date startDate, final Date endDate){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createQuery(Tuple.class);

        Root<WorkoutFeedback> workoutFeedbackRoot = criteriaQuery.from(WorkoutFeedback.class);

        Expression<Date> dateExpression = workoutFeedbackRoot.get("createdDate");
        Predicate startDateCriteria = criteriaBuilder.greaterThanOrEqualTo(dateExpression, startDate);
        Predicate endDateCriteria = criteriaBuilder.lessThanOrEqualTo(dateExpression, endDate);

        Predicate finalPredicate = criteriaBuilder.and(startDateCriteria, endDateCriteria);

        Expression<Long> countExpression = criteriaBuilder.count(workoutFeedbackRoot.get("workoutFeedBackId"));
        Expression<Long> feedbackType = workoutFeedbackRoot.get("feedbackType").get("feedbackTypeId");

        criteriaQuery.where(finalPredicate).orderBy(criteriaBuilder.desc(workoutFeedbackRoot.get("feedbackType").get("feedbackTypeId")));
        criteriaQuery.multiselect(feedbackType, countExpression).groupBy(workoutFeedbackRoot.get("feedbackType").get("feedbackTypeId"));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }
}
