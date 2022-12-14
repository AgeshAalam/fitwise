package com.fitwise.response;

import com.fitwise.response.packaging.MemberPackageSessionView;
import lombok.Data;

import java.util.List;

@Data
public class MemberScheduleView {

    private int order;

    private String date;

    private List<AvailablePackageView> availablePackages;

    private int noOfSessionsBookedInADay;

    private boolean isBookingRestrictedForADay;

    private boolean isInstructorUnavailableForAWholeDay;

}
