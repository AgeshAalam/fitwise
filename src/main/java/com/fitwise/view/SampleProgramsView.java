package com.fitwise.view;

import com.fitwise.program.model.ProgramTileModel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SampleProgramsView {

    private int sampleProgramsCount;

    private List<ProgramTileModel> samplePrograms;
}
