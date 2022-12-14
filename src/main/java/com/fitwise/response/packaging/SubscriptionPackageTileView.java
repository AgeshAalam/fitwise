package com.fitwise.response.packaging;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Created by Vignesh G on 29/09/20
 * Updated by Naveen L on 21/10/21
 */
@Getter
@Setter
public class SubscriptionPackageTileView extends PackageTileView{

    private Date createdOn;
    private String createdOnFormatted;
    private Date lastUpdatedOn;
    private String lastUpdatedOnFormatted;
    private int activeSubscriptions;
    private Date publishedDate;
    private String publishedDateFormatted;
    private Double flatTax;
}