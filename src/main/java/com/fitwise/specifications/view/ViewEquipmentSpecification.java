package com.fitwise.specifications.view;

import com.fitwise.constants.SearchConstants;
import com.fitwise.entity.view.ViewEquipment;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;

public class ViewEquipmentSpecification {

    /**
     * Get Sort
     *
     * @param sortBy
     * @param order
     * @return
     */
    public static Specification<ViewEquipment> getInstructorSortSpecification(final String sortBy, final String order) {
        return (root, query, criteriaBuilder) -> {
            Expression<Object> sortExpression = null;
            if (SearchConstants.EQUIPMENT_NAME.equalsIgnoreCase(sortBy)) {
                sortExpression = root.get("equipmentName");
            } else if (SearchConstants.EXERCISE_COUNT.equalsIgnoreCase(sortBy)) {
                sortExpression = root.get("exerciseCount");
            } else if (SearchConstants.PROGRAM_COUNT.equalsIgnoreCase(sortBy)) {
                sortExpression = root.get("programCount");
            } else if (SearchConstants.CREATED_DATE.equalsIgnoreCase(sortBy)) {
                sortExpression = root.get("createdDate");
            } else if (SearchConstants.USAGE.equalsIgnoreCase(sortBy)) {
                sortExpression = root.get("isUsed");
            }

            Expression<Long> equipmentId = root.get("equipmentId");
            if (SearchConstants.ORDER_ASC.equalsIgnoreCase(order)) {
                query.orderBy(criteriaBuilder.asc(sortExpression), criteriaBuilder.asc(equipmentId));
            } else {
                query.orderBy(criteriaBuilder.desc(sortExpression), criteriaBuilder.desc(equipmentId));
            }
            query.distinct(true);
            return query.getRestriction();
        };
    }

    /**
     * Search by equipment name
     *
     * @param name
     * @return
     */
    public static Specification<ViewEquipment> getSearchByEquipmentNameSpecification(final String name) {
        return (root, query, criteriaBuilder) -> {
            Expression<String> expression = root.get("equipmentName");
            return criteriaBuilder.like(expression, "%" + name + "%");
        };
    }
}
