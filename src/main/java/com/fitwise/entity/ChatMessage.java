package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/*
 * Created by Vignesh G on 03/04/20
 */

@Entity
@Getter
@Setter
public class ChatMessage extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long messageId;

    @ManyToOne(cascade = CascadeType.DETACH)
    private ChatConversation conversation;

    @Column(length = 2000)
    private String content;

    @ManyToOne(cascade = CascadeType.DETACH)
    private User sender;

    private boolean isUnread;

}
