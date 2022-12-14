package com.fitwise.entity.packaging;

import com.fitwise.entity.calendar.UserKloudlessMeeting;
import com.fitwise.entity.instructor.Location;
import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/*
 * Created by Vignesh G on 21/09/20
 */
@Entity
@Data
public class PackageKloudlessMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionMappingId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    private UserKloudlessMeeting userKloudlessMeeting;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    private SubscriptionPackage subscriptionPackage;

    @Column(name = "session_order")
    private Long order;

    private Integer countPerWeek;

    private String title;

    private Integer totalSessionCount;

    @ManyToOne
    private Location location;

    @Column(name = "meeting_url", length = 500)
    private String meetingUrl;

}
