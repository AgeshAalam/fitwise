package com.fitwise.view;

import com.fitwise.entity.Images;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestResponseView {

    private long workoutRestVideoId;

    private long restTime;

    private String url;

    private String parsedUrl;

    private Images thumbnail;

}
