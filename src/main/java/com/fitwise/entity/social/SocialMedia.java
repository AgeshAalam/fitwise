package com.fitwise.entity.social;

import com.fitwise.entity.Images;
import lombok.Getter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
@Getter
public class SocialMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long socialMediaId;

    private String name;

    @OneToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "social_media_image_id")
    private Images socialMediaImage;

}