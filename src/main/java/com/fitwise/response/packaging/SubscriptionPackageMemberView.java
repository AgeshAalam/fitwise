package com.fitwise.response.packaging;

import com.fitwise.entity.Equipments;
import com.fitwise.entity.PlatformType;
import com.fitwise.model.packaging.CancellationDurationModel;
import com.fitwise.view.discounts.ProgramDiscountMappingListResponseView;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SubscriptionPackageMemberView {

    private Long subscriptionPackageId;

    private String title;

    private String shortDescription;

    private String description;

    private Long imageId;

    private String imageUrl;

    private Long duration;

    private Long instructorId;

    private String instructorFirstName;

    private String instructorLastName;

    private String instructorProfileImageUrl;

    private int numberOfPrograms;

    private List<ProgramTileForPackageView> packagePrograms;

    private List<Equipments> equipments;

    private Double price;

    private String formattedPrice;

    private List<SessionMemberView> sessions;

    private List<SessionScheduleMemberView> sessionSchedules;

    private Boolean isRestrictedAccess;

    private String clientMessage;

    private CancellationDurationModel cancellationDuration;

    private String subscriptionStatus;

    private boolean isPackageSubscribed;

    private Date subscribedDate;

    private String subscribedDateFormatted;

    private boolean isPackageAutoSubscribed = false;

    private String subscriptionValidity;

    private PlatformType subscribedViaPlatform;

    private Long subscriptionExpiry;

    private String subscriptionExpiryDate;

    private String orderStatus;

    private boolean isOrderUnderProcessing;

    private Boolean isSubscriptionRestricted;

    private boolean isMemberBlocked;

    private ProgramDiscountMappingListResponseView discountOffers ;

    private String promoUrl;

    private String promoThumbnailUrl;
   
    private String promoUploadStatus;
    
    private boolean freeToAccess = false;

    private Double flatTax;

}
