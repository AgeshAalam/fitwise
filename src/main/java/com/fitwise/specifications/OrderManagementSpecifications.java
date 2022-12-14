package com.fitwise.specifications;

import com.fitwise.entity.payments.common.OrderManagement;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.util.List;

/*
 * Created by Vignesh G on 19/08/20
 */
public class OrderManagementSpecifications {

    public static Specification<OrderManagement> getOrderByUserGroupByOrderId(Long userId) {
        return (root, query, criteriaBuilder) -> {

            //to remove duplicate order id
            query.groupBy(root.get("orderId"));

            Expression<Long> expression = root.get("user").get("userId");

            //User id criteria
            Predicate userIdCriteria = criteriaBuilder.equal(expression, userId);
            query.where(userIdCriteria);
            return query.getRestriction();
        };
    }

    /**
     * @param subscriptionTypeList
     * @return
     */
    public static Specification<OrderManagement> getOrderBySubscriptionTypeIn(List<String> subscriptionTypeList) {
        return (root, query, criteriaBuilder) -> root.get("subscriptionType").get("name").in(subscriptionTypeList);
    }

}
