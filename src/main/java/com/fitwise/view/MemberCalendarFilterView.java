package com.fitwise.view;

import com.fitwise.model.packaging.CancellationDurationModel;
import com.fitwise.response.packaging.MemberPackageSessionView;
import com.fitwise.response.packaging.SessionMemberView;
import lombok.Data;

import java.util.List;

@Data
public class MemberCalendarFilterView {

    private Long subscriptionPackageId;

    private String title;

    private List<SessionMemberView> sessions;

    private CancellationDurationModel cancellationDuration;
}
