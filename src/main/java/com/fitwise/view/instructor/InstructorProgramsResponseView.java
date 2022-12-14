package com.fitwise.view.instructor;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class InstructorProgramsResponseView {

    private long programId;
    private String programTitle;
    private Date programPublishedDate;
    private String programPublishedDateFormatted;
    private long programDuration;
    private String programThumbnail;

}
