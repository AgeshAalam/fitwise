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
public class FeaturedProgramTypeAndPrograms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long featuredProgramId;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "program_type_id")
    private ProgramTypes programType;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "program_id")
    private Programs program;

}
