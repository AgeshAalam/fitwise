package com.fitwise.model.packaging;

import lombok.Data;

import java.util.List;

/*
 * Created by Vignesh G on 22/09/20
 */
@Data
public class SubscriptionPackageModel {

    private Long subscriptionPackageId;

    private String title;

    private String shortDescription;

    private String description;

    private Long imageId;

    private List<Long> programIdList;

    private List<MeetingModel> meetings;

    private CancellationDurationModel cancellationDuration;

    private Double price;

    private AccessModel access;

    private String clientMessage;

    private boolean isSaveAsDraft;

    private List<Long> discountOffersIds;

    private Long promotionId;


}
