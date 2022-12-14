package com.fitwise.entity.thumbnail;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fitwise.entity.Images;
import com.fitwise.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
public class ThumbnailImages{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long thumbnailId;

    @OneToOne(cascade = {CascadeType.DETACH,CascadeType.MERGE})
    @JoinColumn(name = "image_id")
    private Images images;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH,CascadeType.MERGE})
    @JoinColumn(name = "user_id")
    private User user;

    private String type;

    @ManyToMany(fetch = FetchType.LAZY,cascade = {CascadeType.DETACH,CascadeType.MERGE} )
    @JoinTable(name = "thumbnail_main_tag_mapping", joinColumns = @JoinColumn(name = "thumbnail_id"), inverseJoinColumns = @JoinColumn(name = "thumbnail_main_tag_id"))
    private List<ThumbnailMainTags> thumbnailMainTags;

    @ManyToMany(fetch = FetchType.LAZY,cascade = {CascadeType.DETACH,CascadeType.MERGE} )
    @JoinTable(name = "thumbnail_sub_tag_mapping", joinColumns = @JoinColumn(name = "thumbnail_id"), inverseJoinColumns = @JoinColumn(name = "thumbnail_sub_tag_id"))
    private List<ThumbnailSubTags> thumbnailSubTags;

}
