package com.fitwise.entity.subscription;

import com.fitwise.entity.User;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity
@Getter
@Setter
public class FitwiseSubscriptionShareForInstructor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shareId;

    @ManyToOne
    @JoinColumn(name = "subscription_type_id")
    private SubscriptionType subscriptionType;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Long share;

    private Boolean active;

    @Column(name = "created_date", updatable = false)
    @CreationTimestamp
    private Date createdDate;
}
