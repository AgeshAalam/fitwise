package com.fitwise.entity.qbo;

import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * Entity for managing qbo Vendor with fitwise instructor
 */
@Entity
@Getter
@Setter
public class QboVendor extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fitwiseVendorId;

    private String vendorId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Boolean needUpdate;

    private Boolean isBillCreated;

    private String updateStatus;

    private String displayName;
}
