package com.fitwise.entity;

import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.subscription.SubscriptionAudit;
import com.fitwise.entity.subscription.SubscriptionStatus;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
public class ProgramSubscriptionPaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long revenueAuditId;

    @Column(name = "created_date", updatable = false)
    @CreationTimestamp
    private Date createdDate;

    private double instructorShare;

    private double trainnrRevenue;

    private double taxCharges;

    private double programPrice;

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private OrderManagement orderManagement;

}
