package com.fitwise.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Getter
@Setter
public class SaveVoiceOverRequestModel {

    private Long voiceOverId;

    private Long audioId;

    private String title;

    private List<Long> tagIds;
}
