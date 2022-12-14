package com.fitwise.program.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

import com.fitwise.view.discounts.ProgramDiscountMappingListResponseView;
import com.fitwise.view.discounts.ProgramDiscountMappingResponseView;

/**
 * The Class ProgramTileModel.
 */
@Getter
@Setter
public class ProgramTileModel {

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

    private int activeSubscriptions;

    private int numberOfCurrentAvailableOffers;
    
    /** Discount Offers **/
    private ProgramDiscountMappingListResponseView discountOffers ;
    //private List<ProgramDiscountMappingResponseView> discountOffers;/

    private String instructorName;

    private String instructorProfileUrl;
    private boolean freeToAccess = false;
    
    private Double flatTax;
}
