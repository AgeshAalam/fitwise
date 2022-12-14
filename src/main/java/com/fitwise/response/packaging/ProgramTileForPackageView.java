package com.fitwise.response.packaging;

import com.fitwise.view.discounts.ProgramDiscountMappingListResponseView;
import lombok.Data;

import java.util.Date;

@Data
public class ProgramTileForPackageView {

    /** The program id. */
    private Long programId;

    /** The program type. */
    private String programType;

    /** The program titile. */
    private String programTitle;

    /** The program expertise level. */
    private String programExpertiseLevel;

    /** The program duration. */
    private Long programDuration;

    /** The thumbnail url. */
    private String thumbnailUrl;

    /** The created on. */
    private Date createdOn;
    private String createdOnFormatted;

    /** The last updated on. */
    private Date lastUpdatedOn;
    private String lastUpdatedOnFormatted;

    /** The status. */
    private String status;

    /** The helping text. */
    private String helpingText;

    /** The program price. */
    private String price;

    private int numberOfCurrentAvailableOffers;

    private String progress;

    private long progressPercent;

    private Date completedDate;
    private String completedDateFormatted;


}
