package com.fitwise.program.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * The Class ProgramTypeWithProgramTileModel.
 */
@Getter
@Setter
public class ProgramTypeWithProgramTileModel {

    /** The program type id. */
    private Long programTypeId;
    
    /** The program type name. */
    private String programTypeName;

    /** The programs count. */
    private long programsCount;
    
    /** The programs. */
    private List<ProgramTileModel> programs;
}
