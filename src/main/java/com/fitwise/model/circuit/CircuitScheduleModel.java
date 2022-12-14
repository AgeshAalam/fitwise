package com.fitwise.model.circuit;

import lombok.Data;

/*
 * Created by Vignesh G on 12/05/20
 */
@Data
public class CircuitScheduleModel {

    private Long CircuitScheduleId;

    private Long circuitId;

    private Long order;

    private long repeat;

    private Long restBetweenRepeat;

    private boolean isRestCircuit;

    private Long restDuration;

    private boolean isAudio;

}
