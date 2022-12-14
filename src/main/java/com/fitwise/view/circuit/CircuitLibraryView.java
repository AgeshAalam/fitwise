package com.fitwise.view.circuit;

import com.fitwise.entity.Equipments;
import lombok.Data;

import java.util.List;

/*
 * Created by Vignesh G on 13/05/20
 */
@Data
public class CircuitLibraryView {

    private Long circuitId;

    private String title;

    private long duration;

    private int exerciseCount;

    private List<Equipments> equipments;

    private List<String> exerciseThumbnails;

    private boolean isVideoProcessingPending;

    //private String circuitThumbnailUrl;

}
