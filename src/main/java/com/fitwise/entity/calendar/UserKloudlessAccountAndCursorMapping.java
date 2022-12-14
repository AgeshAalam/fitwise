package com.fitwise.entity.calendar;

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
public class UserKloudlessAccountAndCursorMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private UserKloudlessAccount userKloudlessAccount;

}
