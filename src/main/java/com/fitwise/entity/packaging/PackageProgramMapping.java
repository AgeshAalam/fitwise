package com.fitwise.entity.packaging;

import com.fitwise.entity.Programs;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/*
 * Created by Vignesh G on 18/09/20
 */
@Entity
@Getter
@Setter
public class PackageProgramMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long packageProgramId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    private SubscriptionPackage subscriptionPackage;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    private Programs program;

}
