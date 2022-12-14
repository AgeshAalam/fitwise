package com.fitwise.entity.packaging;

import com.fitwise.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Date;

/*
 * Created by Vignesh G on 29/01/21
 */
@Setter
@Getter
@Entity
public class BlockedPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private SubscriptionPackage subscriptionPackage;

    private Date blockedDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User blockedBy;

    private String blockType;

}
