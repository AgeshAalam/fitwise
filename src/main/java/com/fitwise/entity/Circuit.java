package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

/*
 * Created by Vignesh G on 11/05/20
 */
@Entity
@Getter
@Setter
public class Circuit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long circuitId;

    private String title;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "user_id")
    private User owner;

    @OneToMany(mappedBy = "circuit", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<ExerciseSchedulers> exerciseSchedules = new HashSet<ExerciseSchedulers>();

    /* Circuit Library
    @ManyToOne(cascade = {CascadeType.MERGE,CascadeType.DETACH})
    @JoinColumn(name = "image_id")
    private Images thumbnail;
     */

    @Column(name = "is_audio")
    private Boolean isAudio;

    @OneToMany(mappedBy = "circuit", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<CircuitAndVoiceOverMapping> circuitAndVoiceOverMappings = new HashSet<>();

}
