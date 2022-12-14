package com.fitwise.entity.thumbnail;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class ThumbnailMainTags {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long thumbnailMainTagId;

    private String thumbnailMainTag;

    private boolean isMultipleTagsAllowed;

}
