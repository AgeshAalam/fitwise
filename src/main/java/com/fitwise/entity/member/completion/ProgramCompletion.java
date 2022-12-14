package com.fitwise.entity.member.completion;

import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;

/*
 * Created by Vignesh G on 02/03/21
 */
@Entity
@Getter
@Setter
public class ProgramCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date completedDate;

    @ManyToOne
    private User member;

    @ManyToOne(cascade = CascadeType.DETACH)
    private Programs program;

}
