package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ThumbnailUploadResponseView {

    private long ThumbnailId;

    private String imagePath;

    private String fileName;

    private String type;

    List<ThumbnailTagResponseView> thumbnailTags;
}
