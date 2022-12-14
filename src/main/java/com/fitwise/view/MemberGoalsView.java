package com.fitwise.view;

import com.fitwise.entity.ProgramGoals;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MemberGoalsView {

    private Long programExpertiseMappingId;

    private String programType;

    private List<GoalsView> goals;
}
