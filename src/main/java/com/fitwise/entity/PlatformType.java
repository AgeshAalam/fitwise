package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * The Class PlatformType.
 */
@Entity
@Getter
@Setter
public class PlatformType {

    /** The platform type id. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long platformTypeId;

    /** The platform. */
    private String platform;
}
