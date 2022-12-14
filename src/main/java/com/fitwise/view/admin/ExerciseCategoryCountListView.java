package com.fitwise.view.admin;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExerciseCategoryCountListView {

    private long totalCount;

    private List<ExerciseCategoryCountView> exerciseCategories;
}
