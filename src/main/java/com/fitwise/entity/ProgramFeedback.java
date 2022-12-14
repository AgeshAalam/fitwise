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
import javax.persistence.OneToOne;

@Entity
@Getter
@Setter
public class ProgramFeedback extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long programFeedBackId;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "program_id")
    private Programs program;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "feedback_type_id")
    private FeedbackTypes feedbackType;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
