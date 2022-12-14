package com.fitwise.response.packaging;

import com.fitwise.entity.Equipments;
import com.fitwise.entity.PackageDuration;
import com.fitwise.model.packaging.CancellationDurationModel;
import com.fitwise.program.model.ProgramTileModel;
import com.fitwise.view.discounts.ProgramDiscountMappingListResponseView;
import lombok.Data;

import java.util.List;

/*
 * Created by Vignesh G on 22/09/20
 */
@Data
public class SubscriptionPackageView {

    private Long subscriptionPackageId;

    private String title;

    private String shortDescription;

    private String description;

    private Long imageId;

    private String imageUrl;

    private PackageDuration duration;

    private List<ProgramTileModel> packagePrograms;

    private List<Equipments> equipments;

    private Double price;

    private String formattedPrice;

    private List<MeetingView> meetings;

    private CancellationDurationModel cancellationDuration;

    private Boolean isZoomLinkUpdated;

    private Boolean isRestrictedAccess;

    private List<AccessControlMemberView> accessControlMembers;

    private List<AccessControlExternalClientView> accessControlExternalClientMembers;

    private String clientMessage;

    private String status;

    private String postCompletionStatus;

    /** The Discounts. */
    private ProgramDiscountMappingListResponseView discountOffers ;

    private boolean isMaximumCountReachedForNewUsers;

    private boolean isMaximumCountReachedForExistingUsers;

    private int activeSubscriptions;

    private String instructorFirstName;

    private String instructorLastName;

    private String instructorProfileUrl;

    private String publishedDate;

    private String promoUrl;

    private String promoThumbnailUrl;

    private String promoUploadStatus;

    private Long promotionId;

    private Double flatTax;
}
