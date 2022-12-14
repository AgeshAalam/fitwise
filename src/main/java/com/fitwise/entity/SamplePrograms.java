package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class SamplePrograms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sampleProgramsId;

    private String createdBy;

    @OneToOne
    @JoinColumn(name = "program_id")
    private Programs programs;
}
