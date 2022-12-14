package com.fitwise.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class OtherExpertise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long otherExpertiseId;

    private String expertiseType;

    @ManyToOne
    @JoinColumn(name = "experience_id")
    private YearsOfExpertise experience;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
}
