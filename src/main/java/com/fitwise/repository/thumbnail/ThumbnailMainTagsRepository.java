package com.fitwise.repository.thumbnail;

import com.fitwise.entity.thumbnail.ThumbnailMainTags;
import com.fitwise.entity.thumbnail.ThumbnailSubTags;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ThumbnailMainTagsRepository extends JpaRepository<ThumbnailMainTags, Long> {

    ThumbnailMainTags findByThumbnailMainTagId(final long thumbnailMainTagId);

    /**
     * @param thumbnailMainTag
     * @return
     */
    ThumbnailMainTags findByThumbnailMainTagIgnoreCase(String thumbnailMainTag);

    List<ThumbnailMainTags> findByThumbnailMainTagIgnoreCaseContaining(final Optional<String> tagName);




}
