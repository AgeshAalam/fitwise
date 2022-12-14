package com.fitwise.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class InstructorProgramExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "instructor_program_experience_id")
    private Long instructorProgramExperienceId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "experience_id")
    private YearsOfExpertise experience;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "program_type_id")
    private ProgramTypes programType;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.MERGE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
