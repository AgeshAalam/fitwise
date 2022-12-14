package com.fitwise.specifications;

import com.fitwise.entity.Exercises;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.util.List;

public class ExerciseSpecification {


    /**
     * Get Exercises by title search
     *
     * @param searchName
     * @return
     */
    public static Specification<Exercises> getExercisesByTitleSearch(String searchName) {
        return (root, query, criteriaBuilder) -> {

            Expression<String> expression = root.get("title");

            return criteriaBuilder.like(expression, "%" + searchName + "%");
        };
    }

    /**
     * Get exercises by categories
     *
     * @param categoryIdList
     * @return
     */
    public static Specification<Exercises> getExercisesByCategories(List<Long> categoryIdList) {
        return (root, query, criteriaBuilder) -> {

            query.distinct(true);

            return root.join("exerciseCategoryMappings").get("exerciseCategory").get("categoryId").in(categoryIdList);

        };
    }

    /**
     * Get stock exercises
     *
     * @return
     */
    public static Specification<Exercises> geStockExercises() {
        return (root, query, criteriaBuilder) -> {

            Expression<Boolean> expression = root.get("isByAdmin");
            Predicate stockExercisePredicate = criteriaBuilder.isTrue(expression);

            Predicate userNotNullPredicate = root.get("owner").isNotNull();
            Predicate finalPredicate = criteriaBuilder.and(stockExercisePredicate,userNotNullPredicate);
            query.where(finalPredicate);

            return query.getRestriction();
        };
    }

    /**
     * Get exercises by categories
     *
     * @param equipmentIdList
     * @return
     */
    public static Specification<Exercises> getExercisesByEquipments(List<Long> equipmentIdList) {
        return (root, query, criteriaBuilder) -> {

            query.distinct(true);

            return root.join("equipments").get("equipmentId").in(equipmentIdList);

        };
    }
}
