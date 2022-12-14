package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Setter
@Getter
public class ProgramTrailAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long programTrailAuditId;

    @ManyToOne
    @JoinColumn(name = "program_id")
    private Programs program;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Date date;
}
