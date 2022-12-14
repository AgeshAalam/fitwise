package com.fitwise.entity.packaging;

import com.fitwise.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Date;

/*
 * Created by Vignesh G on 29/01/21
 */
@Entity
@Setter
@Getter
public class DeletedPackageAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deletedProgramAuditId;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private SubscriptionPackage subscriptionPackage;

    private Date happenedDate;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User doneBy;

}
