package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Setter
@Getter
public class BlockedProgramsAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long blockedProgramsAuditId;

    @ManyToOne
    @JoinColumn(name = "program_id")
    private Programs program;
    private String status;
    private Date  happenedDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User doneBy;

    private String blockType;

}
