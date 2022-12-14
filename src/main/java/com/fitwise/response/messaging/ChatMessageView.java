package com.fitwise.response.messaging;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/*
 * Created by Vignesh G on 05/04/20
 */
@Setter
@Getter
public class ChatMessageView {

    private String messageContent;

    private String messageTime;

    private String messageDay;

    private Date messageTimeStamp;

    private boolean isMessageSent;

    private boolean isMessageNew;

}
