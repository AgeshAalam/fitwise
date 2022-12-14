package com.fitwise.specifications.jpa;

import com.fitwise.entity.subscription.TierSubscription;
import com.fitwise.specifications.jpa.dao.TierTypesAndCountsDAO;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class TierTypesAndCountsJPA {
    private final EntityManager entityManager;

    /**
     * Get count of Tiers - Group by tiers and between start and end date
     * @param startDate
     * @param endDate
     * @return
     */
    public List<TierTypesAndCountsDAO> getCountsOfTiersGroupByTier(Date startDate, Date endDate){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TierTypesAndCountsDAO> criteriaQuery = criteriaBuilder.createQuery(TierTypesAndCountsDAO.class);
        Root<TierSubscription> tierSubscriptionRoot = criteriaQuery.from(TierSubscription.class);
        Expression<Date> dateExpression = tierSubscriptionRoot.get("createdDate");
        Predicate startDateCriteria = criteriaBuilder.greaterThanOrEqualTo(dateExpression,startDate);
        Predicate endDateCriteria = criteriaBuilder.lessThanOrEqualTo(dateExpression,endDate);
        Predicate finalPredicate = criteriaBuilder.and(startDateCriteria, endDateCriteria);
        criteriaQuery.where(finalPredicate).multiselect(tierSubscriptionRoot.get("tier").get("tierType"),criteriaBuilder.count(tierSubscriptionRoot)).groupBy(tierSubscriptionRoot.get("tier").get("tierId"));
        return entityManager.createQuery(criteriaQuery).getResultList();
    }
}