package com.fitwise.repository;

import com.fitwise.entity.Audios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AudioRepository extends JpaRepository<Audios,Long> {

    Audios findByAudioId(Long audioId);
}
