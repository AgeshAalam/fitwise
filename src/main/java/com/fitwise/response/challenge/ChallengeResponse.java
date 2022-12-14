package com.fitwise.response.challenge;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChallengeResponse {

    private Long challengeId;
    private int challengeWorkouts;
    private int remainingWorkouts;
    private double percentage;
    private int completedDays;

    private boolean isExpired;

}