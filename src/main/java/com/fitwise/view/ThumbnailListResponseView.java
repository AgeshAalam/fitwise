package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ThumbnailListResponseView {

    private long totalCount;

    List<ThumbnailResponseView> thumbnailsList;
}
