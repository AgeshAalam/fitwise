package com.fitwise.response.packaging;

import com.fitwise.view.InstructorUnavailabilityMemberView;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class MemberPackageScheduleDayView {

    private int order;

    private String date;

    private List<MemberPackageSessionView> sessions;

    private int noOfSessionsBookedInADay;

    private boolean isBookingRestrictedForADay;

    private boolean isInstructorUnavailableForAWholeDay;

    private List<InstructorUnavailabilityMemberView> instructorUnavailabilities;

}
