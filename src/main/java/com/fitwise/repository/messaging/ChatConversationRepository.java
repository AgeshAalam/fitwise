package com.fitwise.repository.messaging;

import com.fitwise.entity.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Created by Vignesh G on 03/04/20
 */
@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long>, JpaSpecificationExecutor<ChatConversation> {

    /**
     * Find conversation of an PrimaryUser by Conversation ID
     * @param conversationId
     * @param primaryUserId
     * @return
     */
    ChatConversation findByConversationIdAndPrimaryUserUserUserId(Long conversationId, Long primaryUserId);

    /**
     * Find conversation of an secondaryUser by Conversation ID
     * @param conversationId
     * @param secondaryUserId
     * @return
     */
    ChatConversation findByConversationIdAndSecondaryUserUserUserId(Long conversationId, Long secondaryUserId);


    /**
     * Find list of conversation of an PrimaryUser
     *
     * @param primaryUserId
     * @return
     */
    List<ChatConversation> findByPrimaryUserUserUserId(Long primaryUserId);

    /**
     * Find list of conversation of a secondaryUser
     * @param secondaryUserId
     * @return
     */
    List<ChatConversation> findBySecondaryUserUserUserId(Long secondaryUserId);

    /**
     * Find conversation by primaryUser id and secondaryUser id
     * @param primaryUserId
     * @param secondaryUserId
     * @return
     */
    ChatConversation findByPrimaryUserUserUserIdAndSecondaryUserUserUserId(Long primaryUserId, Long secondaryUserId);

}
