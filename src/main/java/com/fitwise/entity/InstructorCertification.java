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
public class InstructorCertification {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long instructorCertificateId;

    private String certificateTitle;

    private String academyName;

    private String issuedDate;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "certificate_image_id")
    private Images certificateImage;


}
