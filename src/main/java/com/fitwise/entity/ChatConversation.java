package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/*
 * Created by Vignesh G on 03/04/20
 */

@Entity
@Getter
@Setter
public class ChatConversation extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long conversationId;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "primary_user_id")
    private UserProfile primaryUser;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "secondary_user_id")
    private UserProfile secondaryUser;

    private boolean isReceiveOnly;

}
