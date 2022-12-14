package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Setter
@Getter
public class DeletedProgramAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deletedProgramAuditId;

    @ManyToOne(cascade = CascadeType.MERGE , fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private Programs program;

    private Date happenedDate;

    @ManyToOne(cascade = CascadeType.MERGE , fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User doneBy;
}
