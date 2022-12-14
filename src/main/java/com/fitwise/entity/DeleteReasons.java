package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Setter
@Getter
public class DeleteReasons {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deleteReasonId;
    private String reason;
}
