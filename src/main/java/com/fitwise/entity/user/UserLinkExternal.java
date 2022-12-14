package com.fitwise.entity.user;

import com.fitwise.entity.Images;
import com.fitwise.entity.UserProfile;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
@Getter
@Setter
public class UserLinkExternal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long linkId;

    @Column(name = "name", length = 30)
    private String name;

    @Column(name = "link", length = 500)
    private String link;

    @OneToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "image_id")
    private Images image;

    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.MERGE)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

}
