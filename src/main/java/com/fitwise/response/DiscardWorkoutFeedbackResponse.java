package com.fitwise.response;

import com.fitwise.entity.Images;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Setter
@Getter
public class DiscardWorkoutFeedbackResponse {

    private String userName;
    private String description;
    private String image;
}
