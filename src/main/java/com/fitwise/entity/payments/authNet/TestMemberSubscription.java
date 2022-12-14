package com.fitwise.entity.payments.authNet;

import com.fitwise.entity.AuditingEntity;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/*
 * Created by Vignesh G on 02/09/20
 */
@Entity
@Data
public class TestMemberSubscription extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;
    private Long programId;
    private Long programSubscriptionId;
    private Long subscriptionAudit;
    private Integer orderId;

}
