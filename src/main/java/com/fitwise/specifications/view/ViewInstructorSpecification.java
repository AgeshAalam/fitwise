package com.fitwise.specifications.view;

import com.fitwise.constants.SearchConstants;
import com.fitwise.constants.SortConstants;
import com.fitwise.entity.view.ViewInstructor;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;

/**
 * Member view specification
 */
public class ViewInstructorSpecification {

    /**
     * Get specification for blocked status
     * @param status
     * @return
     */
    public static Specification<ViewInstructor> getBlockStatusSpecification(final Boolean status) {
        return (root, query, criteriaBuilder) -> {
            Expression<Long> expression = root.get("blocked");
            return criteriaBuilder.equal(expression, status);

        };
    }

    /**
     * Get specification for search in member view
     * @param name
     * @return
     */
    public static Specification<ViewInstructor> getSearchByNameSpecification(final String name) {
        return (root, query, criteriaBuilder) -> {
            Expression<String> expression = root.get("name");
            return criteriaBuilder.like(expression, "%" + name + "%");
        };
    }

    /**
     * Get specification for search in member view
     * @param name
     * @return
     */
    public static Specification<ViewInstructor> getSearchByEmailSpecification(final String name) {
        return (root, query, criteriaBuilder) -> {
            Expression<String> expression = root.get("email");
            return criteriaBuilder.like(expression, "%" + name + "%");
        };
    }


    /**
     * Get Specification for sort with given sort by and sort order
     * @param sortBy
     * @param order
     * @return
     */
    public static Specification<ViewInstructor> getInstructorSortSpecification(final String sortBy, final String order) {
        return (root, query, criteriaBuilder) -> {
            Expression<Object> sortExpression = null;
            if(SearchConstants.INSTRUCTOR_NAME.equalsIgnoreCase(sortBy)){
                sortExpression = root.get("name");
            }else if(SearchConstants.UPCOMING_PAYMENT.equalsIgnoreCase(sortBy)){
                sortExpression = root.get("outstandingBalance");
            }else if(SearchConstants.TOTAL_SUBSCRIPTION.equalsIgnoreCase(sortBy)){
                sortExpression = root.get("activeProgramSubscriptions");
            }else if(SearchConstants.PUBLISHED_PROGRAM.equalsIgnoreCase(sortBy)){
                sortExpression = root.get("publishedPrograms");
            }else if(SearchConstants.ONBOARDED_DATE.equalsIgnoreCase(sortBy)){
                sortExpression = root.get("onboardedOn");
            }else if(SearchConstants.TOTAL_EXERCISES.equalsIgnoreCase(sortBy)){
                sortExpression = root.get("exercises");
            }else if(SearchConstants.PACKAGE_SUBSCRIPTION_COUNT.equalsIgnoreCase(sortBy)){
                sortExpression = root.get("activePackageSubscriptions");
            }else if(SearchConstants.PUBLISHED_PACKAGE_COUNT.equalsIgnoreCase(sortBy)){
                sortExpression = root.get("publishedPackages");
            }else if(SearchConstants.USER_LAST_ACCESS_DATE.equalsIgnoreCase(sortBy)){
                sortExpression = root.get("lastUserAccess");
            }else if (SearchConstants.USER_EMAIL.equalsIgnoreCase(sortBy)){
                sortExpression = root.get("email");
            } else if (SortConstants.SORT_BY_TIER_TYPE.equalsIgnoreCase(sortBy)){
                sortExpression = root.get("tierType");
            }
            Expression<Long> userIdExpression = root.get("userId");
            if(SearchConstants.ORDER_ASC.equalsIgnoreCase(order)){
                query.orderBy(criteriaBuilder.asc(sortExpression), criteriaBuilder.asc(userIdExpression));
            }else{
                query.orderBy(criteriaBuilder.desc(sortExpression), criteriaBuilder.desc(userIdExpression));
            }
            query.distinct(true);
            return query.getRestriction();
        };
    }

    /**
    * Get specification for tier type
    * @param type
    * @return
    */

    public static Specification<ViewInstructor> getTierType(final String type){
        return (root, query, criteriaBuilder) -> {
            Expression<String> expression = root.get("tierType");
            return criteriaBuilder.like(expression, "%" + type + "%");
        };
    }
}
