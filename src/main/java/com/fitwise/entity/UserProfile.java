package com.fitwise.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fitwise.entity.instructor.Tier;
import com.fitwise.entity.user.UserLinkSocial;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "user_profile", indexes = {
        @Index(name = "index_id", columnList = "profile_id", unique = true),
        @Index(name = "index_user_id", columnList = "profile_id,user_id"),
        @Index(name = "index_name",  columnList = "profile_id,first_name,last_name")
})
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;

    private String biography;

    private String shortBio;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String dob;

    private Boolean notificationStatus = true;

    private String contactNumber;

    private String countryCode;

    private String isdCode;

    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "profile_image_id")
    private Images profileImage;

    @OneToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "cover_image_id")
    private Images coverImage;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "gender_id")
    private Gender gender;

    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<InstructorProgramExperience> instructorProgramExperiences= new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<UserLinkSocial> userLinkSocials = new ArrayList<>();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    private Promotions promotion;
    
    private String location;

    private String gym;
   
}
