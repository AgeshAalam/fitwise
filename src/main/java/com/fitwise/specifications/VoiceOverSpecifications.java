package com.fitwise.specifications;

import com.fitwise.entity.VoiceOver;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import java.util.List;

public class VoiceOverSpecifications {

    public static Specification<VoiceOver> getVoiceOverByTitleSearch(String searchName) {
        return (root, query, criteriaBuilder) -> {

            Expression<String> expression = root.get("title");

            return criteriaBuilder.like(expression, "%" + searchName + "%");
        };
    }

    public static Specification<VoiceOver> getVoiceOverByTags(List<Long> tagIdList) {
        return (root, query, criteriaBuilder) -> {

            query.distinct(true);

            return root.join("voiceOverTags").get("tagId").in(tagIdList);

        };
    }

    public static Specification<VoiceOver> getVoiceOverByUser(Long userId) {
        return (root, query, criteriaBuilder) -> {

            Expression<Long> expression = root.get("user").get("userId");
            return criteriaBuilder.equal(expression, userId);

        };
    }

    public static Specification<VoiceOver> getVoiceOverByTagId(Long tagId) {
        return (root, query, criteriaBuilder) -> {

            Expression<Long> expression = root.join("voiceOverTags").get("tagId");
            return criteriaBuilder.equal(expression, tagId);

        };
    }

    public static Specification<VoiceOver> getVoiceOverByTitle(String title) {
        return (root, query, criteriaBuilder) -> {

            Expression<String> expression = root.get("title");

            return criteriaBuilder.like(expression, title);
        };
    }
}
