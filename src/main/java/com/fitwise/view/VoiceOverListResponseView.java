package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VoiceOverListResponseView {

    private List<VoiceOverResponseView> voiceOverList;

    private long totalVoiceOvers;
}
