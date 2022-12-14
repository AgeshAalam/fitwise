package com.fitwise.entity.admin;

import com.fitwise.entity.Exercises;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
@Getter
@Setter
public class ExerciseCategoryMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long exerciseCategoryMappingId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    private Exercises exercise;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    private ExerciseCategory exerciseCategory;

}
