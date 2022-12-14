package com.fitwise.specifications.jpa;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import com.fitwise.entity.ProgramRating;
import com.fitwise.entity.Programs;
import com.fitwise.specifications.jpa.dao.TopRatedProgram;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CalculateTopRatingJpa {
    final private EntityManager entityManager;

    public List<TopRatedProgram> getTopRatedInstructor(Date startDate,Date endDate){
        
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TopRatedProgram> programCriteriaQuery = criteriaBuilder.createQuery(TopRatedProgram.class);
        Root<ProgramRating> programRatingRoot = programCriteriaQuery.from(ProgramRating.class);
        Root<Programs> programRoot = programCriteriaQuery.from(Programs.class);

        Predicate userIdMappingCriteria = criteriaBuilder.equal(programRatingRoot.get("program").get("owner").get("userId"), programRoot.get("owner").get("userId"));
        
        //Date criteria
        Expression<Date> dateExpression = programRatingRoot.get("modifiedDate");
        Predicate startDateCriteria = criteriaBuilder.greaterThanOrEqualTo(dateExpression, startDate);
        Predicate endDateCriteria = criteriaBuilder.lessThanOrEqualTo(dateExpression, endDate);
        
        Predicate finalPredicate = criteriaBuilder.and(userIdMappingCriteria,startDateCriteria,endDateCriteria);
        Expression userId = programRatingRoot.get("program").get("owner").get("userId");
        Expression avgRating = criteriaBuilder.avg(programRatingRoot.get("programRating"));
        Expression groupingExpression = programRatingRoot.get("program").get("programId");

        programCriteriaQuery.multiselect(userId,avgRating).where(finalPredicate).groupBy(groupingExpression);
        List<TopRatedProgram> topRatedPrograms= entityManager.createQuery(programCriteriaQuery).getResultList();
        return topRatedPrograms;
    }
}
