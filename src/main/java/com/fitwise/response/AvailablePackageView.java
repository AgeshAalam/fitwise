package com.fitwise.response;

import com.fitwise.model.packaging.CancellationDurationModel;
import com.fitwise.response.packaging.MemberPackageSessionView;
import com.fitwise.response.packaging.MemberSessionView;
import com.fitwise.view.InstructorUnavailabilityMemberView;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AvailablePackageView {

    private Long subscriptionPackageId;

    private String  title;

    private List<MemberSessionView> sessions;

    private boolean bookingRestrictedForAPackage;

    private String instructorName;

    private boolean isInstructorUnavailableForAWholeDay;

    private List<InstructorUnavailabilityMemberView> instructorUnavailabilities;

    private CancellationDurationModel cancellationDuration;

    private Date subscribedDate;
    private String subscribedDateFormatted;

    private Date expiryDate;
    private String expiryDateFormatted;

}
