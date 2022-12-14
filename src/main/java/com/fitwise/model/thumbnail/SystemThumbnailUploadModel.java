package com.fitwise.model.thumbnail;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SystemThumbnailUploadModel {

    private long imageId;

    List<ThumbnailMainTagsModel> thumbnailTags;
}
