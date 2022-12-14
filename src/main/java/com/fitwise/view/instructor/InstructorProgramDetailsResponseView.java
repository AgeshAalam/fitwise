package com.fitwise.view.instructor;

import com.fitwise.entity.Equipments;
import com.fitwise.program.model.ProgramPlatformPriceResponseModel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InstructorProgramDetailsResponseView {
    private long programId;
    private String programThumbnail;
    private String programPromoUrl;
    private String programTitle;
    private String programDescription;
    private String programPrice;
    private String formattedProgramPrice;
    private List<ProgramPlatformPriceResponseModel> programPlatformPriceResponseModels;
    private String programType;
    private List<Equipments> equipments;
    private String programLevel;
    private long programDuration;
    private List<InstructorWorkoutResponseView> workouts;

}
