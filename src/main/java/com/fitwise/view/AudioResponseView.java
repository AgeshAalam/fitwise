package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AudioResponseView {

    private Long audioId;

    private Long voiceOverId;

    private String filePath;

    private long duration;

    private String title;

    private Long circuitAndVoiceOverMappingId;

}
