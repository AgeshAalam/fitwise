package com.fitwise.response.packaging;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionPackageTileViewForDiscover extends PackageTileView{

    private Long programCount;
    private Long sessionCount;
    private String instructorName;
    private String instructorProfileUrl;

}