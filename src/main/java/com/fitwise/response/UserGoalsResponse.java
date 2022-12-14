package com.fitwise.response;

import com.fitwise.entity.ProgramGoals;
import lombok.Getter;
import lombok.Setter;


import java.util.List;
@Setter
@Getter
public class UserGoalsResponse {

    private Long programTypeId;
    private String programType;
    private Long expertiseLevelId;
    private String expertiseLevel;

    private List<ProgramGoals> programGoals;
}
