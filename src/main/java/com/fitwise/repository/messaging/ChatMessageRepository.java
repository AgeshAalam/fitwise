package com.fitwise.repository.messaging;

import com.fitwise.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Created by Vignesh G on 05/04/20
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Get last message in a conversation
     * @param conversationId
     * @return
     */
    ChatMessage findFirstByConversationConversationIdOrderByCreatedDateDesc(Long conversationId);

    /**
     * Get last received message in a conversation
     * @param conversationId
     * @return
     */
    ChatMessage findFirstByConversationConversationIdAndSenderUserIdOrderByCreatedDateDesc(Long conversationId, Long userId);

    /**
     * Get list of messages in a conversation sorted by created date.
     * @param conversationId
     * @return
     */
    Page<ChatMessage> findByConversationConversationIdOrderByCreatedDateDesc(Long conversationId, Pageable pageable);

    /**
     * Get list of messages in a conversation by unread status.
     * @param conversationId
     * @return
     */
    List<ChatMessage> findByConversationConversationIdAndSenderUserIdAndIsUnread(Long conversationId, Long userId, boolean unreadStatus);

    List<ChatMessage> findByConversationConversationIdAndIsUnread(Long conversationId, boolean unreadStatus);

    /**
     * Get list of messages in a conversation by unread status.
     * @param conversationId
     * @return
     */
    int countByConversationConversationIdAndSenderUserIdAndIsUnread(Long conversationId, Long userId, boolean unreadStatus);

    int countByConversationConversationIdAndIsUnread(Long conversationId, boolean unreadStatus);


}
