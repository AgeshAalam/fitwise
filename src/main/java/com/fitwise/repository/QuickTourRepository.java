package com.fitwise.repository;

import com.fitwise.entity.QuickTourVideos;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuickTourRepository extends JpaRepository<QuickTourVideos,Long> {
    QuickTourVideos findByQuickTourVideoId(Long tourVideoId);
}
