package com.fitwise.view;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoalsResponseView {

    private Long goalId;

    private String goalName;

    private boolean isSelected=false;

}
