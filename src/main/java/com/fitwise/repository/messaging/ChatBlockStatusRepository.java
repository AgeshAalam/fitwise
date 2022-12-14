package com.fitwise.repository.messaging;

import com.fitwise.entity.ChatBlockStatus;
import com.fitwise.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by Vignesh G on 05/04/20
 */
@Repository
public interface ChatBlockStatusRepository extends JpaRepository<ChatBlockStatus, Long> {

    /**
     * Check if a conversation is blocked by sender, receiver or admin
     * @param conversationId
     * @return
     */
    boolean existsByConversationConversationId(Long conversationId);

    /**
     * Check if a conversation is blocked by the user. A conversation can be blocked by both users.
     * @param conversationId
     * @param blockedByUser
     * @return
     */
    ChatBlockStatus findByConversationConversationIdAndBlockedByUserUserId(Long conversationId, Long blockedByUser);

    /**
     * Find blocked cat row for chats blocked by admin
     * @param conversationId
     * @param userRole
     * @return
     */
    ChatBlockStatus findByConversationConversationIdAndBlockedByUserUserRoleMappingsUserRole(Long conversationId, UserRole userRole);

}
