package com.fitwise.response.messaging;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/*
 * Created by Vignesh G on 03/04/20
 */
@Setter
@Getter
public class InboxConversationView {

    private long conversationId;

    private String recipientName;

    private String recipientProfileImage;

    private String lastMessage;

    private String lastMessageTime;

    private Date lastMessageDate;

    private boolean isUnread;

    private int unreadMessageCount;

    private boolean isBlocked;

    private boolean canUnblock;

    private boolean isReceiveOnly;

}
