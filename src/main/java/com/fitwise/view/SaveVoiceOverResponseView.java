package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SaveVoiceOverResponseView {

    private Long voiceOverId;

    private String title;

    private Long audioId;

    private String filePath;

    private long duration;

    private List<VoiceOverTagsResponseView> voiceOverTags;
}
