package com.fitwise.view.circuit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitwise.entity.Equipments;
import com.fitwise.view.AudioResponseView;
import com.fitwise.view.RestResponseView;
import com.fitwise.view.VideoStandards;
import com.fitwise.workout.model.SupportingVideoMappingModel;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;

/*
 * Created by Vignesh G on 18/08/20
 */
@Data
public class ExerciseScheduleView implements Serializable, Comparable<ExerciseScheduleView> {
    /**
     *  Default Serialization Id
     */
    private static final long serialVersionUID = 1L;

    @JsonProperty("scheduleId")
    private Long scheduleId;

    @JsonProperty("exerciseId")
    private long exerciseId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("isRestVideo")
    private boolean restVideo;

    @JsonProperty("url")
    private String url;

    @JsonProperty("parsedUrl")
    private String parsedUrl;

    @JsonProperty("order")
    private Long order;

    @JsonProperty("loopCount")
    private long loopCount;

    /** The Thumbnail Url. */
    @JsonProperty("thumbnailUrl")
    private String thumbnailUrl;

    /** The Image Id. */
    @JsonProperty("imageId")
    private Long imageId;

    @JsonProperty("duration")
    private int duration;


    private RestResponseView workoutRestVideo;

    @JsonProperty("equipments")
    private List<Equipments> equipments;

    @JsonProperty("videoUploadStatus")
    private String videoUploadStatus;

    private SupportingVideoMappingModel supportingVideoMappingModel;

    private boolean isExerciseBlocked;

    private List<VideoStandards> videoStandards;

    private boolean isAudio;

    @JsonProperty("audioResponseView")
    private AudioResponseView audioResponseView;

    @Override
    public int compareTo(@NotNull ExerciseScheduleView exerciseScheduleView) {
        return getOrder().compareTo(exerciseScheduleView.getOrder());
    }
}
