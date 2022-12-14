package com.fitwise.listeners.block.program;

import com.fitwise.entity.Programs;
import lombok.Data;

/*
 * Created by Vignesh G on 29/06/20
 */
@Data
public class ProgramBlockEvent {

    private Long programId;

    private Programs program;

    private String blockType;

}
