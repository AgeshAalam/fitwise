package com.fitwise.specifications.jpa.dao;

import com.fitwise.entity.ChatBlockStatus;
import com.fitwise.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class ChatMessageDAO {

    private Long chatConversationId;

    private ChatMessage lastChatMessage;

    private Long blockStatus;
}
