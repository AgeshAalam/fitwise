package com.fitwise.model.thumbnail;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
@Setter
public class ThumbnailSubTagsModel {

    private long thumbnailSubTagId;

    private String thumbnailSubTag;
}
