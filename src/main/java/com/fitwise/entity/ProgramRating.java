package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity
@Setter
@Getter
public class ProgramRating extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long programRatingId;

    @ManyToOne
    @JoinColumn(name = "program_id")
    private Programs program;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Float programRating;

    private Boolean isSubmissionAllowed;

}
