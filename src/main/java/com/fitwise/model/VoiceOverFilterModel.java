package com.fitwise.model;

import com.fitwise.view.VoiceOverTagsResponseView;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VoiceOverFilterModel {

    List<VoiceOverTagsResponseView> voiceOverFilters;
}
