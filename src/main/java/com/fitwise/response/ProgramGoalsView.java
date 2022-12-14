package com.fitwise.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgramGoalsView {
    Long programGoalId;
    String programGoal;
    long programTypeLevelGoalMappingId;
}
