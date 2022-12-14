package com.fitwise.entity.user;

import com.fitwise.entity.UserProfile;
import com.fitwise.entity.social.SocialMedia;
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
public class UserLinkSocial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long linkId;

    @Column(name = "link", length = 500)
    private String link;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "social_media_id")
    private SocialMedia socialMedia;

    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.MERGE)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

}
