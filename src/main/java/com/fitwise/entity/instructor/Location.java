package com.fitwise.entity.instructor;

import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.User;
import com.fitwise.entity.payments.authNet.Countries;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
@Data
public class Location extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long locationId;

    @OneToOne
    private User user;

    private String address;

    private String landMark;

    private String city;

    private String state;

    private String zipcode;

    @OneToOne
    private Countries country;

    @OneToOne
    private LocationType locationType;

    private boolean isDefault;

}
