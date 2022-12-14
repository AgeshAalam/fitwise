package com.fitwise.response.flaggedvideo;

import com.fitwise.view.program.ProgramIdTitleView;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/*
 * Created by Vignesh G on 25/03/20
 */

@Setter
@Getter
public class FlaggedVideoDetailsView {

    private Long exerciseId;
    private String title;
    private String videoId;
    private String thumbnailUrl;

    private String owner;
    private String ownerProfileImage;

    private long flaggedCount;
    private String firstFlaggedtime;
    private String latestFlaggedtime;
    private String flagStatus;

    List<ReasonCountView> reasonForFlagging;

    List<FlaggedVideoAffectedProgram> impactedPrograms;
}
