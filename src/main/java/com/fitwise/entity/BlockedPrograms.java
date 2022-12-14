package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Setter
@Getter
@Entity
public class BlockedPrograms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long blockedProgramsId;

    @ManyToOne
    @JoinColumn(name = "program_id")
    private Programs program;
    private Date programBlockedDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User whoBlocked;

    private String blockType;

}
