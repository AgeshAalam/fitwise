package com.fitwise.response.messaging;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/*
 * Created by Vignesh G on 05/04/20
 */
@Setter
@Getter
public class ChatConversationView {

    private Long conversationId;

    private String recipientName;

    private String recipientProfileImage;

    private List<ChatMessageView> messages;

    private int totalMessageCount;

    private boolean isBlocked;

    private boolean canUnblock;

    private boolean isReceiveOnly;

}
