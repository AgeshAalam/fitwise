package com.fitwise.view;

import com.fitwise.view.program.ProgramIdTitleView;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InstructorClientView {
    private long clientId;
    private String instructorName;
    private String clientName;
    private String instructorImgUrl;
    private String clientImgUrl;
    private String interestedIn;
    private boolean activeStatus;
    List<ProgramIdTitleView> programs;
    List<SubscriptionPackagePackageIdAndTitleView> subscriptionPackages;
}
