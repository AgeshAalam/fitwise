package com.fitwise.specifications.jpa;

import com.fitwise.entity.ProgramPromoViews;
import com.fitwise.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;

/*
 * Created by Vignesh.G on 23/06/21
 */
@Component
public class ProgramPromoViewsJPA {

    @Autowired
    private EntityManager entityManager;

    public long getUniqueUsersForProgramBetweenDate(Long programId, Date startDate, Date endDate) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<ProgramPromoViews> programPromoViewsRoot = criteriaQuery.from(ProgramPromoViews.class);

        Expression<Long> countOfUsers = criteriaBuilder.countDistinct(programPromoViewsRoot.get("user"));
        criteriaQuery.select(countOfUsers);

        //user not null criteria
        Expression<User> userExpression = programPromoViewsRoot.get("user");
        Predicate userCriteria = userExpression.isNotNull();

        //Program id criteria
        Expression<Long> programExpression = programPromoViewsRoot.get("program").get("programId");
        Predicate programIdCriteria = criteriaBuilder.equal(programExpression, programId);

        //Date criteria
        Expression<Date> dateExpression = programPromoViewsRoot.get("date");
        Predicate startDateCriteria = criteriaBuilder.greaterThanOrEqualTo(dateExpression, startDate);
        Predicate endDateCriteria = criteriaBuilder.lessThanOrEqualTo(dateExpression, endDate);

        Predicate finalPredicate = criteriaBuilder.and(userCriteria, programIdCriteria, startDateCriteria, endDateCriteria);
        criteriaQuery.where(finalPredicate);

        Long count = entityManager.createQuery(criteriaQuery).getSingleResult();
        return count == null ? 0L : count.longValue();
    }

}
