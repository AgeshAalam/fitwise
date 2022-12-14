package com.fitwise.entity.qbo;

import com.fitwise.entity.Programs;
import com.fitwise.entity.instructor.Tier;
import com.fitwise.entity.packaging.SubscriptionPackage;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Entity for managing qbo Product with fitwise program
 */
@Entity
@Getter
@Setter
public class QboProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fitwiseProductId;

    private String productIdAndroid;

    private String productIdIos;

    private String productIdWeb;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private Programs program;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private SubscriptionPackage subscriptionPackage;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id")
    private Tier tier;

    private Boolean needUpdate;

    private String updateStatus;

    private String skuNumber;
}
