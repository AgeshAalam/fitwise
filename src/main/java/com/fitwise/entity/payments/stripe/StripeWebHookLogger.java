package com.fitwise.entity.payments.stripe;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.util.Date;

/*
 * Created by Vignesh G on 03/11/20
 */
@Entity
@Getter
@Setter
public class StripeWebHookLogger {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @CreationTimestamp
    private Date receivedTime;

    private String eventType;

    @Lob
    @Column(length = 100000)
    private String webHookNotification;

}
