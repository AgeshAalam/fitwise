package com.fitwise.repository;

import com.fitwise.entity.VoiceOver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoiceOverRepository extends JpaRepository<VoiceOver, Long>, JpaSpecificationExecutor<VoiceOver> {

    VoiceOver findByVoiceOverIdAndUserUserId(Long voiceOverId, Long userId);

    VoiceOver findByVoiceOverId(Long voiceOverId);

    List<VoiceOver> findByUserUserId(Long userId);

    int countByUserUserId(Long userId);

    int countByVoiceOverTagsTagIdAndUserUserId(Long tagId, Long userId);

    List<VoiceOver> findByUserUserIdAndTitle(Long userId, String title);

    List<VoiceOver> findByVoiceOverIdIn(List<Long> voiceOverIds);
}
