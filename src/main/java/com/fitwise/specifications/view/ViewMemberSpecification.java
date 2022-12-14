package com.fitwise.specifications.view;

import com.fitwise.constants.SearchConstants;
import com.fitwise.entity.view.ViewMember;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;

/**
 * Member view specification
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ViewMemberSpecification {

    /**
     * Get specification for blocked status
     * @param status User block status
     * @return specification
     */
    public static Specification<ViewMember> getBlockStatusSpecification(final Boolean status) {
        return (root, query, criteriaBuilder) -> {
            Expression<Long> expression = root.get("blocked");
            return criteriaBuilder.equal(expression, status);

        };
    }

    /**
     * Get specification for search in member view
     * @param name Search String
     * @return Search Specification
     */
    public static Specification<ViewMember> getSearchByNameSpecification(final String name) {
        return (root, query, criteriaBuilder) -> {
            Expression<String> expression = root.get("name");
            Expression<String> emailExpression = root.get("email");
            return criteriaBuilder.or(criteriaBuilder.like(expression, "%" + name + "%"), criteriaBuilder.like(emailExpression, "%" + name + "%"));
        };
    }

    /**
     * Get Specification for sort with given sort by and sort order
     * @param sortBy Column to sort
     * @param order Order of the sort
     * @return Sort Specification
     */
    public static Specification<ViewMember> getMemberSortSpecification(final String sortBy, final String order) {
        return (viewMemberRoot, query, criteriaBuilder) -> {
            Expression<Object> sortExpression = null;
            if(SearchConstants.MEMBER_NAME.equalsIgnoreCase(sortBy)){
                sortExpression = viewMemberRoot.get("name");
            }else if(SearchConstants.AMOUNT_SPENT.equalsIgnoreCase(sortBy)){
                sortExpression = viewMemberRoot.get("totalSpent");
            }else if(SearchConstants.TOTAL_SUBSCRIPTION.equalsIgnoreCase(sortBy)){
                sortExpression = viewMemberRoot.get("activeProgramSubscriptions");
            }else if(SearchConstants.COMPLETED_PROGRAM.equalsIgnoreCase(sortBy)){
                sortExpression = viewMemberRoot.get("completedPrograms");
            }else if(SearchConstants.PACKAGE_SUBSCRIPTION_COUNT.equalsIgnoreCase(sortBy)){
                sortExpression = viewMemberRoot.get("activePackageSubscriptions");
            }else if(SearchConstants.USER_LAST_ACCESS_DATE.equalsIgnoreCase(sortBy)){
                sortExpression = viewMemberRoot.get("lastUserAccess");
            }else if(SearchConstants.ONBOARDED_DATE.equalsIgnoreCase(sortBy)){
                sortExpression = viewMemberRoot.get("onboardedOn");
            }else if(SearchConstants.USER_EMAIL.equalsIgnoreCase(sortBy)){
                sortExpression = viewMemberRoot.get("email");
            }
            Expression<Long> userIdExpression = viewMemberRoot.get("userId");
            if(SearchConstants.ORDER_ASC.equalsIgnoreCase(order)){
                query.orderBy(criteriaBuilder.asc(sortExpression), criteriaBuilder.asc(userIdExpression));
            }else{
                query.orderBy(criteriaBuilder.desc(sortExpression), criteriaBuilder.desc(userIdExpression));
            }
            query.distinct(true);
            return query.getRestriction();
        };
    }

}