package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ThumbnailTagResponseView {

    private long thumbnailMainTagId;

    private String thumbnailMainTag;

    private boolean isMultipleSubTagsAllowed;

    private long stockImageCountOnMainTag;

    private long thumbnailSubTagsCount;

    List<ThumbnailSubTagResponseView> thumbnailSubTags;


}