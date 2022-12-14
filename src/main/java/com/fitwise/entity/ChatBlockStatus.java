package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/*
 * Created by Vignesh G on 03/04/20
 */

@Entity
@Getter
@Setter
public class ChatBlockStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id;

    @OneToOne(cascade = CascadeType.DETACH)
    private ChatConversation conversation;

    @ManyToOne(cascade = CascadeType.DETACH)
    private User blockedByUser;

}
