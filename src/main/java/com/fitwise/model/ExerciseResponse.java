package com.fitwise.model;

import com.fitwise.entity.Equipments;
import com.fitwise.exercise.model.SupportingVideoModel;
import com.fitwise.exercise.model.VimeoModel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ExerciseResponse {

    private Long exerciseId;

    /**  The file name */
    private String fileName;

    /** The file size */
    private Long fileSize;

    /**  The title */
    private String title;

    /**  The description */
    private String description;

    /** The Instructor Id. */
    private Long imageId;

    private String thumbnailUrl;

    /**
     * Vimeo Data
     */
    private VimeoModel vimeoData;

    private List<Equipments> equipments;

    private long duration;

    private String videoUploadStatus;

    private SupportingVideoModel supportingVideoModel;

    private Boolean isDeleteSupportVideo;

    private Boolean isExerciseBlocked;

    private List<ExerciseCategoryModel> exerciseCategories = new ArrayList<>();
    
	private long videoId;


}
