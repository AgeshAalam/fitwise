package com.fitwise.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class UserProgramGoalsId {

    private long userId;
    private List<Long> programExpertiseGoalsMappingId;

}
