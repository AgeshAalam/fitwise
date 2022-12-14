package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Getter
@Setter
public class CircuitAndVoiceOverMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long circuitAndVoiceOverMappingId;

    @ManyToOne(cascade= {CascadeType.MERGE,CascadeType.DETACH} )
    private Circuit circuit;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "voiceOverId")
    private VoiceOver voiceOver;
}
