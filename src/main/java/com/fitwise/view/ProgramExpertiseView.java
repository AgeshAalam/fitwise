package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProgramExpertiseView {


    private String programType;

    private boolean isProgramTypeSelected;

    private List<ExpertiseLevelView> expertiseLevels;
    
}
