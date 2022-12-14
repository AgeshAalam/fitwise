package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProgramResponseView {

    private Long programTypeId;

    private String programTypeName;

    private  boolean isSelected=false;

    List<ExpertiseLevelsResponseView> expertiseLevels;

}
