package com.fitwise.specifications;

import com.fitwise.constants.KeyConstants;
import com.fitwise.entity.thumbnail.ThumbnailImages;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.util.List;

public class ThumbnailSpecifications {

    public static Specification<ThumbnailImages> getThumbnailImagesByTagIdsAndCriteria(List<Long> thumbnailMainTagIds,List<Long> thumbnailSubTagIds) {

        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            Predicate finalPred = criteriaBuilder.equal(root.get("type"), KeyConstants.KEY_SYSTEM);
            for (Long mainTagId : thumbnailMainTagIds) {
                Predicate subPredicate = criteriaBuilder.equal(root.join("thumbnailMainTags").get("thumbnailMainTagId"), mainTagId);
                finalPred = criteriaBuilder.and(finalPred, subPredicate);
            }
            for(Long subTagId : thumbnailSubTagIds){
                Predicate subPredicate = criteriaBuilder.equal(root.join("thumbnailSubTags").get("thumbnailSubTagId"), subTagId);
                finalPred = criteriaBuilder.and(finalPred, subPredicate);
            }
            query.where(finalPred);
            return query.getRestriction();

        };

    }

    public static Specification<ThumbnailImages> getThumbnailImagesByType(String type) {

        return (root, query, criteriaBuilder) -> {

            Expression<String> typeExpression = root.get("type");
            return  criteriaBuilder.equal(typeExpression,type);
        };
    }

    public static Specification<ThumbnailImages> getThumbnailImagesBySubTag(Long subTagId) {

        return (root, query, criteriaBuilder) -> {

            Expression<String> tagExpression = root.join("thumbnailSubTags").get("thumbnailSubTagId");
            return  criteriaBuilder.equal(tagExpression,subTagId);
        };
    }

    public static Specification<ThumbnailImages> getThumbnailImagesByMainTag(Long mainTagId) {

        return (root, query, criteriaBuilder) -> {

            Expression<String> tagExpression = root.join("thumbnailMainTags").get("thumbnailMainTagId");
            return  criteriaBuilder.equal(tagExpression,mainTagId);
        };
    }

    public static Specification<ThumbnailImages> getThumbnailImagesByMainTagSearch(String search) {

        return (root, query, criteriaBuilder) -> {

            query.distinct(true);
            Expression<String> tagExpression = root.join("thumbnailMainTags").get("thumbnailMainTag");
            return criteriaBuilder.like(tagExpression, "%" + search + "%");
        };
    }

    public static Specification<ThumbnailImages> getThumbnailImagesBySubTagSearch(String search) {

        return (root, query, criteriaBuilder) -> {

            query.distinct(true);
            Expression<String> tagExpression = root.join("thumbnailSubTags").get("thumbnailSubTag");
            return criteriaBuilder.like(tagExpression, "%" + search + "%");
        };
    }
    public static Specification<ThumbnailImages> getThumbnailImagesByMainTagOrSubTagSearch(String search) {
        Specification<ThumbnailImages> finalSpecification = getThumbnailImagesBySubTagSearch(search).or(getThumbnailImagesByMainTagSearch(search));
        return finalSpecification;
    }

    }
