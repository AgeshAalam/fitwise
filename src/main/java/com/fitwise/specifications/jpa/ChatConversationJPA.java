package com.fitwise.specifications.jpa;

import com.fitwise.entity.ChatBlockStatus;
import com.fitwise.entity.ChatConversation;
import com.fitwise.entity.ChatMessage;
import com.fitwise.specifications.jpa.dao.ChatMessageDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ChatConversationJPA {

    @Autowired
    EntityManager entityManager;

    public Map<Long, ChatMessageDAO> getLastMessageAndBlockStatusByConversationIdList(List<Long> conversationIdList){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ChatMessageDAO> criteriaQuery = criteriaBuilder.createQuery(ChatMessageDAO.class);
        Root<ChatConversation> chatConversationRoot = criteriaQuery.from(ChatConversation.class);

        //sub query 1 to get last chat message id
        Subquery<Long> subQuery1 = criteriaQuery.subquery(Long.class);
        Root<ChatMessage> chatMessageRoot = subQuery1.from(ChatMessage.class);

        Predicate conversationIdEqualPredicate = criteriaBuilder.equal(chatConversationRoot.get("conversationId"), chatMessageRoot.get("conversation").get("conversationId"));

        subQuery1.where(conversationIdEqualPredicate);
        subQuery1.select(criteriaBuilder.max(chatMessageRoot.get("messageId")));

        //sub query 2 to get last chat message
        Subquery<ChatMessage> subQuery2 = criteriaQuery.subquery(ChatMessage.class);
        Root<ChatMessage> chatMessageRoot1 = subQuery2.from(ChatMessage.class);

        Predicate idEqualPredicate = criteriaBuilder.equal(chatMessageRoot1.get("messageId"), subQuery1.getSelection());

        subQuery2.where(idEqualPredicate);
        subQuery2.select(chatMessageRoot1);

        //sub query 3 to get block status
        Subquery<Long> subQuery3 = criteriaQuery.subquery(Long.class);
        Root<ChatBlockStatus> chatBlockStatusRoot = subQuery3.from(ChatBlockStatus.class);

        Predicate isConversationBlockedPredicate = criteriaBuilder.equal(chatConversationRoot.get("conversationId"), chatBlockStatusRoot.get("conversation").get("conversationId"));

        subQuery3.where(isConversationBlockedPredicate);
        subQuery3.select(criteriaBuilder.count(chatBlockStatusRoot));

        //Criteria query
        Predicate conversationIdListInPredicate = chatConversationRoot.get("conversationId").in(conversationIdList);

        //Result set expression
        Expression<Long> chatConversationIdExpression = chatConversationRoot.get("conversationId");
        Expression<ChatMessage> chatMessageExpression = subQuery2.getSelection();
        Expression<Long> blockStatusExpression = subQuery3.getSelection();

        criteriaQuery.where(conversationIdListInPredicate);
        criteriaQuery.multiselect(chatConversationIdExpression, chatMessageExpression, blockStatusExpression);

        List<ChatMessageDAO> chatMessageDAOList = entityManager.createQuery(criteriaQuery).getResultList();

        Map<Long, ChatMessageDAO> chatMessageDAOMap = new HashMap<>();
        if (!chatMessageDAOList.isEmpty()){
            chatMessageDAOMap = chatMessageDAOList.stream().collect(Collectors.toMap(ChatMessageDAO::getChatConversationId, chatMessageDAO -> chatMessageDAO));
        }

        return chatMessageDAOMap;
    }
}
