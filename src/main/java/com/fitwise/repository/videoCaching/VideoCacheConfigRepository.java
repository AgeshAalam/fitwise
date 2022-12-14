package com.fitwise.repository.videoCaching;

import com.fitwise.entity.User;
import com.fitwise.entity.videoCaching.VideoCacheConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoCacheConfigRepository extends JpaRepository<VideoCacheConfig,Long> {

    VideoCacheConfig findByVideoCacheConfigIdAndUserUserId(Long videoCacheConfigId,Long userId);

    VideoCacheConfig findTop1ByUser(final User user);

}
