package com.fitwise.repository;

import com.fitwise.entity.VoiceOverTags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoiceOverTagsRepository extends JpaRepository<VoiceOverTags,Long> {

    VoiceOverTags findByTagId(Long tagId);
}
