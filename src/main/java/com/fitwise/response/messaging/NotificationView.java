package com.fitwise.response.messaging;

import lombok.Data;

/*
 * Created by Vignesh G on 10/06/20
 */
@Data
public class NotificationView {

    private boolean hasUnreadConversations;

    private int unreadConversationCount;

}
