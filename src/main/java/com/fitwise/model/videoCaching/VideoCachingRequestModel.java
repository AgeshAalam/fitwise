package com.fitwise.model.videoCaching;


import lombok.Data;

import java.util.List;

@Data
public class VideoCachingRequestModel {

    private List<VideoCacheProgramModel> programs;
}
