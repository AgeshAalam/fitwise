package com.fitwise.entity.calendar;

import com.fitwise.entity.AuditingEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/*
 * Created by Vignesh G on 02/02/21
 */
@Entity
@Getter
@Setter
public class UserKloudlessSubscription  extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private UserKloudlessAccount userKloudlessAccount;

    private Long subscriptionId;

    private String lastCursor;

    private String calendarId;

}
