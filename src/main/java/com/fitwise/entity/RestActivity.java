package com.fitwise.entity;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/*
 * Created by Vignesh G on 18/05/20
 */
@Entity
@Data
public class RestActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long restActivityId;

    private String restActivity;

    @ManyToOne(cascade = {CascadeType.DETACH})
    @JoinColumn(name = "image_id")
    private Images image;

}
