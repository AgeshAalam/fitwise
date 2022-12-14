package com.fitwise.repository.challenge;

import com.fitwise.entity.Challenge;
import com.fitwise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    Challenge findByChallengeId(long challengeId);

    boolean existsByChallengeId(long challengeId);

    boolean existsByUserUserIdAndChallengeType(long userId, String type);

    List<Challenge> findByUserUserIdAndChallengeType(long userId, String type);

    Challenge findByUserUserIdAndChallengeTypeAndIsExpired(Long userId, String challengeType, boolean isExpired);

    boolean existsByUserUserIdAndChallengeTypeAndIsExpired(Long userId, String challengeType, boolean isExpired);

    Challenge findByChallengeIdAndUser(Long challengeId, User user);

    Challenge findFirstByUserAndChallengeTypeIgnoreCaseOrderByChallengeEndDateDesc(User user, String challengeType);

    /**
     * @param userId
     * @return
     */
    List<Challenge> findByUserUserId(Long userId);
}