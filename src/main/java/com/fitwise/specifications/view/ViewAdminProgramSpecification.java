package com.fitwise.specifications.view;

import com.fitwise.constants.SearchConstants;
import com.fitwise.constants.SecurityFilterConstants;
import com.fitwise.entity.view.ViewAdminPrograms;
import org.springframework.data.jpa.domain.Specification;
import javax.persistence.criteria.Expression;
import java.util.List;

/**
 * Admin view specification
 */
public class ViewAdminProgramSpecification {

    /**
     * get program type specification
     *
     * @param programTypeId
     * @return
     */
    public static Specification<ViewAdminPrograms> getProgramTypeSpecification(Long programTypeId){

        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("programTypeId"), programTypeId);
    }

    /**
     * get program title specification
     *
     * @param programTitle
     * @return
     */
    public static Specification<ViewAdminPrograms> getProgramTitleSpecification(String programTitle){
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.like(root.get("programName"), "%"+programTitle+"%");
    }

    /**
     * get instructor name specification
     *
     * @param instructorName
     * @return
     */
    public static Specification<ViewAdminPrograms> getInstructorNameSpecification(String instructorName){
        return (root, criteriaQuery, criteriaBuilder) -> {

            Expression<String> instructorNameExpression = criteriaBuilder.concat(root.get("instructorFirstName"), root.get("instructorLastName"));
            return criteriaBuilder.like(instructorNameExpression, "%"+instructorName+"%");
        };
    }

    /**
     * Get Specification for sort with given sort by and sort order
     * @param programStatus
     * @return
     */
    public static Specification<ViewAdminPrograms> getProgramStatusSpecification(List<String> programStatus){
        return (root, criteriaQuery, criteriaBuilder) -> root.get("programPublishStatus").in(programStatus);
    }

    /**
     * Get Specification for sort with given sort by and sort order
     * @param
     * @return
     */
    public static Specification<ViewAdminPrograms> getProgramBlockStatus(Boolean blockStatus){
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (blockStatus){
                return criteriaBuilder.isTrue(root.get("isBlocked"));
            } else {
                return criteriaBuilder.isFalse(root.get("isBlocked"));
            }
        };
    }

    /**
     * Get Specification for sort with given sort by and sort order
     * @param sortBy
     * @param order
     * @return
     */
    public static Specification<ViewAdminPrograms> getProgramSortSpecification(final String sortBy, final String order) {
        return (root, query, criteriaBuilder) -> {
            Expression<Object> sortExpression = null;
            //SORT EXPRESSION
            //program name
            if(SearchConstants.PROGRAM_NAME.equalsIgnoreCase(sortBy)){
                sortExpression = root.get("programName");
            }else if(SearchConstants.CREATED_DATE.equalsIgnoreCase(sortBy)){            //created date
                sortExpression = root.get("createdDate");
            }else if(SearchConstants.TOTAL_SUBSCRIPTION.equalsIgnoreCase(sortBy)){      //total subscription
                sortExpression = root.get("activeSubscriptionCount");
            } else if(SearchConstants.INSTRUCTOR_NAME.equalsIgnoreCase(sortBy) || SecurityFilterConstants.ROLE_INSTRUCTOR.equalsIgnoreCase(sortBy)){        //instructor name or instructor role
                sortExpression = root.get("instructorFullName");
            } else if(SearchConstants.RATING.equalsIgnoreCase(sortBy)){     //program rating
                sortExpression = root.get("rating");
            }
            Expression<String> programNameExpression = root.get("programName");
            if(SearchConstants.ORDER_ASC.equalsIgnoreCase(order)){
                query.orderBy(criteriaBuilder.asc(sortExpression), criteriaBuilder.asc(programNameExpression));
            }else{
                query.orderBy(criteriaBuilder.desc(sortExpression), criteriaBuilder.desc(programNameExpression));
            }
            query.distinct(true);
            return query.getRestriction();
        };
    }

}
