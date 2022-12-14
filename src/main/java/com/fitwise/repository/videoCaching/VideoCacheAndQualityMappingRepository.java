package com.fitwise.repository.videoCaching;

import com.fitwise.entity.videoCaching.VideoCacheConfig;
import com.fitwise.entity.videoCaching.VideoCacheConfigAndQualityMapping;
import com.fitwise.entity.videoCaching.VideoQuality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoCacheAndQualityMappingRepository extends JpaRepository<VideoCacheConfigAndQualityMapping,Long> {

    List<VideoCacheConfigAndQualityMapping> findByVideoCacheConfigAndVideoQualityNotIn(VideoCacheConfig videoCacheConfig,VideoQuality videoQuality);

    VideoCacheConfigAndQualityMapping findByVideoCacheConfigAndVideoQuality(VideoCacheConfig videoCacheConfig, VideoQuality videoQuality);

    List<VideoCacheConfigAndQualityMapping> findByVideoCacheConfig(VideoCacheConfig videoCacheConfig);


}
