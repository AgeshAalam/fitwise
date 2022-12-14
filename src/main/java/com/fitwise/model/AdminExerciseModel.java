package com.fitwise.model;

import com.fitwise.entity.Equipments;
import com.fitwise.entity.admin.ExerciseCategory;
import com.fitwise.exercise.model.ExerciseModel;
import com.fitwise.exercise.model.SupportingVideoModel;
import com.fitwise.exercise.model.VimeoModel;
import lombok.Data;

import java.util.List;

@Data
public class AdminExerciseModel {

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

    private List<ExerciseCategoryModel> exerciseCategoryModels;
}
