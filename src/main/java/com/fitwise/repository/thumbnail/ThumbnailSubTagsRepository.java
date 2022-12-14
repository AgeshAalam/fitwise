package com.fitwise.repository.thumbnail;

import com.fitwise.entity.thumbnail.ThumbnailSubTags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThumbnailSubTagsRepository extends JpaRepository<ThumbnailSubTags,Long> {

    ThumbnailSubTags findByThumbnailSubTagId(final long tagId);


    List<ThumbnailSubTags> findByThumbnailSubTagIgnoreCaseContaining(final Optional<String> tagName);

    ThumbnailSubTags findByThumbnailMainTagsThumbnailMainTagIdAndThumbnailSubTagIgnoreCase(final long thumbnailMainTagId, final String tagName);

    List<ThumbnailSubTags> findByThumbnailMainTagsThumbnailMainTagId(final long thumbnailMainTagId);

}
