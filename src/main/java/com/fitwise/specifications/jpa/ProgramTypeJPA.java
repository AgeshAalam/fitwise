package com.fitwise.specifications.jpa;

import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.entity.ProgramTypes;
import com.fitwise.entity.subscription.SubscriptionAudit;
import com.fitwise.specifications.jpa.dao.SubscriptionCountByProgramType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class ProgramTypeJPA {

    @Autowired
    private EntityManager entityManager;

    /**
     * Get SubscriptionByProgramType for Admin dashboard graph on Platform tab.
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws
     */
    public List<SubscriptionCountByProgramType> getSubscriptionCountByProgramType(Date startDate, Date endDate){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SubscriptionCountByProgramType> criteriaQuery = criteriaBuilder.createQuery(SubscriptionCountByProgramType.class);
        Root<ProgramTypes> programTypesRoot = criteriaQuery.from(ProgramTypes.class);

        //Sub query to get subscription count for each program type
        Subquery<Long> subQuery1 = criteriaQuery.subquery(Long.class);
        Root<SubscriptionAudit> subscriptionAuditRoot = subQuery1.from(SubscriptionAudit.class);

        List<String> statusList = Arrays.asList(new String[]{KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING});
        Expression<String> subscriptionStatusTypeExpression = subscriptionAuditRoot.get("subscriptionStatus").get("subscriptionStatusName");
        Predicate subscriptionStatusInPredicate = subscriptionStatusTypeExpression.in(statusList);

        Predicate subscriptionTypePredicate = criteriaBuilder.like(subscriptionAuditRoot.get("subscriptionType").get("name"), KeyConstants.KEY_PROGRAM);

        Predicate programTypeIdPredicate = criteriaBuilder.equal(subscriptionAuditRoot.get("programSubscription").get("program").get(StringConstants.JSON_PROPERTY_KEY_PROGRAM_TYPE).get("programTypeId"), programTypesRoot.get("programTypeId"));

        Expression<Date> dateExpression = subscriptionAuditRoot.get("subscriptionDate");
        Predicate startDatePredicate = criteriaBuilder.greaterThanOrEqualTo(dateExpression, startDate);
        Predicate endDatePredicate = criteriaBuilder.lessThanOrEqualTo(dateExpression, endDate);

        Predicate finalPredicate = criteriaBuilder.and(subscriptionStatusInPredicate, subscriptionTypePredicate, programTypeIdPredicate, startDatePredicate, endDatePredicate);

        Expression<Long> subscriptionCount = criteriaBuilder.count(subscriptionAuditRoot.get("auditId"));
        subQuery1.where(finalPredicate);
        subQuery1.select(subscriptionCount);

        //Result set expression
        Expression<String> programTypeExpression = programTypesRoot.get("programTypeName");
        Expression<Long> subscriptionCountExpression = subQuery1.getSelection();

        criteriaQuery.multiselect(programTypeExpression, subscriptionCountExpression).orderBy(criteriaBuilder.asc(programTypesRoot.get("programTypeName")));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }
}
