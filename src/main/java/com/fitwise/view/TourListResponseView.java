package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TourListResponseView {

    private int tourVideosCount;

    private List<QuickTourVideosResponseView> quickTourVideos;
}
