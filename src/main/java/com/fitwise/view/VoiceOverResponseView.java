package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoiceOverResponseView {

    private Long voiceOverId;

    private String audioPath;

    private String title;

    private long duration;
}
