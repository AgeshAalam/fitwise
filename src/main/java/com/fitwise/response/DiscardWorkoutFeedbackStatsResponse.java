package com.fitwise.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DiscardWorkoutFeedbackStatsResponse {

    private String discardReason;
    private int discardCout;
}
