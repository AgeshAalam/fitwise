package com.fitwise.entity.discounts;

import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.payments.common.OrderManagement;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/*
 * Created by Vignesh G on 18/11/20
 */
@Entity
@Getter
@Setter
public class OfferCodeDetailAndOrderMapping extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    private OfferCodeDetail offerCodeDetail;

    @OneToOne
    private OrderManagement orderManagement;

}
