package com.fitwise.model.thumbnail;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ThumbnailMainTagsModel {

    private long thumbnailMainTagId;

    private String thumbnailMainTag;


    private boolean isMultipleSubTagsAllowed;

    List<ThumbnailSubTagsModel> thumbnailSubTags;

}
