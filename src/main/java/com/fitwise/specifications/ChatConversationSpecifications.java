package com.fitwise.specifications;

import com.fitwise.entity.ChatConversation;
import com.fitwise.entity.ChatMessage;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;

/*
 * Created by Vignesh G on 07/04/20
 */
public class ChatConversationSpecifications {

    /**
     * Specification criteria for list of conversation by primary user name (first name + last name)
     * @param primaryUserName
     * @return
     */
    public static Specification<ChatConversation> getConversationByPrimaryUserName(String primaryUserName) {
        return (root, query, criteriaBuilder) -> {

            Expression<String> expression = criteriaBuilder.concat(root.get("primaryUser").get("firstName"), " ");
            expression = criteriaBuilder.concat(expression, root.get("primaryUser").get("lastName"));

            return criteriaBuilder.like(expression, "%" + primaryUserName + "%");
        };
    }

    /**
     * Specification criteria for list of conversation by secondary user name (first name + last name)
     * @param secondaryUserName
     * @return
     */
    public static Specification<ChatConversation> getConversationBySecondaryUserName(String secondaryUserName) {
        return (root, query, criteriaBuilder) -> {

            Expression<String> expression = criteriaBuilder.concat(root.get("secondaryUser").get("firstName"), " ");
            expression = criteriaBuilder.concat(expression, root.get("secondaryUser").get("lastName"));

            return criteriaBuilder.like(expression, "%" + secondaryUserName + "%");
        };
    }

    /**
     * Specification criteria for list of conversation by primary user id
     * @param userId
     * @return
     */
    public static Specification<ChatConversation> getConversationByPrimaryUserId(Long userId) {
        return (root, query, criteriaBuilder) -> {

            Expression<Long> expression = root.get("primaryUser").get("user").get("userId");

            return criteriaBuilder.equal(expression, userId);
        };
    }

    /**
     * Specification criteria for list of conversation by secondary user id
     * @param userId
     * @return
     */
    public static Specification<ChatConversation> getConversationBySecondaryUserId(Long userId) {
        return (root, query, criteriaBuilder) -> {

            Expression<Long> expression = root.get("secondaryUser").get("user").get("userId");

            return criteriaBuilder.equal(expression, userId);
        };
    }

    public static Specification<ChatConversation> getConversationsInnerJoinChatMsgs() {
        return (root, query, criteriaBuilder) -> {

            CriteriaQuery<ChatConversation> criteriaQuery = (CriteriaQuery<ChatConversation>) query;
            Root<ChatMessage> chatMessageRoot = criteriaQuery.from(ChatMessage.class);

            root.alias("con");
            chatMessageRoot.alias("msg");

            criteriaQuery.select(root);

            //User, UserProfile inner Join criteria
            Predicate fieldEquals = criteriaBuilder.equal(root.get("conversationId"), chatMessageRoot.get("conversation").get("conversationId"));
            criteriaQuery.where(fieldEquals);

            criteriaQuery.groupBy(root.get("conversationId"));

            Expression<Date> modifedDateColumn = chatMessageRoot.get("modifiedDate");
            modifedDateColumn = criteriaBuilder.greatest(modifedDateColumn);

            criteriaQuery.orderBy(criteriaBuilder.desc(modifedDateColumn));

            return criteriaQuery.getRestriction();
        };
    }

}
