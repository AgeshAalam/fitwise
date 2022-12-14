package com.fitwise.repository.thumbnail;

import com.fitwise.entity.thumbnail.ThumbnailCount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThumbnailCountRepository extends JpaRepository<ThumbnailCount,Long> {

    ThumbnailCount findByThumbnailTagIdAndIsMainTag(final long thumbnailTagId, final boolean isMainTag);

    List<ThumbnailCount> findByIsMainTag(final boolean isMainTag);

    List<ThumbnailCount> findByIsMainTagAndThumbnailTagIdIn(final boolean isMainTag, final List<Long> tagIds);
}
