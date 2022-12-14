package com.fitwise.repository.thumbnail;

import com.fitwise.entity.User;
import com.fitwise.entity.thumbnail.ThumbnailImages;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ThumbnailRepository extends JpaRepository<ThumbnailImages,Long>, JpaSpecificationExecutor<ThumbnailImages> {

    ThumbnailImages findByThumbnailId(final long thumbnailId);

    List<ThumbnailImages> findByUserUserIdAndType(final Long userId, final String type);

    Page<ThumbnailImages> findByUserUserIdAndType(final Long userId, final String type, Pageable pageable);

    Page<ThumbnailImages> findByUserUserIdAndTypeAndImagesFileNameIgnoreCaseContaining(final long userId, final String type, final Optional<String> fileName, Pageable pageable);

    long countByType(final String type);

    ThumbnailImages findByImagesImageId(final long imageId);

    ThumbnailImages findByImagesImageIdAndTypeAndUserUserId(final long imageId, final String type, final long userId);

    List<ThumbnailImages> findByUserAndImagesFileName(final User user, final String originalFilename);

    List<ThumbnailImages> findByTypeAndImagesFileName(String type, String fileName);

    int countByThumbnailMainTagsThumbnailMainTagIdAndType(final long thumbnailMainTagId, final String type);

    int countByThumbnailSubTagsThumbnailSubTagIdAndType(final long thumbnailSubTagId, final String type);

}
