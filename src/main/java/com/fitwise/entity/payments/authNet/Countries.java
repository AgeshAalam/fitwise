package com.fitwise.entity.payments.authNet;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class Countries {

    @Id
    private Long id;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "country_name")
    private String countryName;

    @Column(name = "isd_code")
    private String isdCode;

    private Boolean isStripeSupported = false;
}
