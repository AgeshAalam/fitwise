package com.fitwise.response.flaggedvideo;

import com.fitwise.view.SubscriptionPackagePackageIdAndTitleView;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/*
 * Created by Vignesh G on 28/01/21
 */
@Setter
@Getter
public class FlaggedVideoAffectedProgram {

    private long programId;
    private String programTitle;

    private List<SubscriptionPackagePackageIdAndTitleView> associatedPackages;

}
