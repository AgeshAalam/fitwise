package com.fitwise.view.instructor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExistingAndRequestedTierResponseView {

    private TierDetailsView existingTierDetails;
    private TierDetailsView requestedTierDetails;
    private Long numberOfDaysUtilized;
    private double subscriptionAmount;
    private double balanceAmount;

    @Getter
    @Setter
    public static class TierDetailsView {
        private String tierType;
        private Double price;
    }
}
