package com.fitwise.view.instructor;

import com.fitwise.entity.Images;
import com.fitwise.entity.ProgramExpertiseMapping;
import com.fitwise.entity.ProgramGoals;
import com.fitwise.view.ProgramExpertiseLevelsView;
import com.fitwise.view.ProgramsResponseView;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ClientDetailsResponseView {

    private String firstName;

    private String lastName;

    private String bio;

    private String userStatus;

    private String profileImage;

    private List<String> programGoalsList;

    private List<ProgramExpertiseLevelsView> programExpertiseLevels;

    private List<ProgramsResponseView> programs;
}
