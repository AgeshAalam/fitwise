package com.fitwise.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
public class InstructorAwards {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long awardsId;

    private String awardsTitle;

    private String organizationRecognized;

    private String externalSiteLink;


    private String issuedDate;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @OneToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "award_image_id")
    private Images awardImage;

}
