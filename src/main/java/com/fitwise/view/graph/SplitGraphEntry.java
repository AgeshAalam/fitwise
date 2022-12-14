package com.fitwise.view.graph;

import lombok.Getter;
import lombok.Setter;

/*
 * Created by Vignesh G on 13/03/20
 */

@Getter
@Setter
public class SplitGraphEntry {

    private int periodId;

    private String periodName;

    private String firstEntryName;

    private int firstEntryCount;

    private float firstEntryPercent;

    private String secondEntryName;

    private int secondEntryCount;

    private float secondEntryPercent;

    private int totalCount;

}
