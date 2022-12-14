package com.fitwise.repository.videoCaching;

import com.fitwise.entity.videoCaching.VideoQuality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoQualityRepository extends JpaRepository<VideoQuality,Long> {

    VideoQuality findByVideoQualityId(Long videoQualityId);

}
