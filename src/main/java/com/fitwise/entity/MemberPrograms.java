package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
public class MemberPrograms {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long userProgramId;

    private Date startedDate;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne
    @JoinColumn(name = "programId")
    private Programs programs;


}
