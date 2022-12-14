package com.fitwise.entity.thumbnail;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class ThumbnailSubTags {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long thumbnailSubTagId;

    private String thumbnailSubTag;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "thumbnail_main_tag_id")
    private ThumbnailMainTags thumbnailMainTags;


}
