package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ThumbnailTagView {

    private long totalStockImageCount;

    private long thumbnailMainTagCount;

    List<ThumbnailTagResponseView> thumbnailTagsList;
}
