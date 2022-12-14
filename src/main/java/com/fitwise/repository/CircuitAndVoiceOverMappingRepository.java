package com.fitwise.repository;

import com.fitwise.entity.CircuitAndVoiceOverMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CircuitAndVoiceOverMappingRepository extends JpaRepository<CircuitAndVoiceOverMapping,Long> {

    List<CircuitAndVoiceOverMapping> findByVoiceOverVoiceOverId(Long voiceOverId);

    List<CircuitAndVoiceOverMapping> findByCircuitCircuitId(Long circuitId);

}
