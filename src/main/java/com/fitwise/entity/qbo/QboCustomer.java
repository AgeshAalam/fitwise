package com.fitwise.entity.qbo;

import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Entity for managing qbo customers with fitwise members
 */
@Entity
@Getter
@Setter
public class QboCustomer extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fitwiseCustomerId;

    private String customerId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Boolean needUpdate;

    private String updateStatus;

    private String displayName;

}
