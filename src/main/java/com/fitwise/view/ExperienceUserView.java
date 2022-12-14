package com.fitwise.view;


import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class ExperienceUserView {

    private Long userId;

    List<InstructorExperienceView> instructorExperienceList;
}
