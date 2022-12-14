package com.fitwise.model;

import lombok.Data;

import java.util.List;

@Data
public class ExerciseListResponse {

    private long totalcount;

    private List<ExerciseResponse> exerciseResponses;
}
