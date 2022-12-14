package com.fitwise.view.graph;

import lombok.Getter;
import lombok.Setter;

/*
 * Created by Vignesh G on 02/04/20
 */

@Getter
@Setter
public class SplitGraphEntryWithValue<T> {

    private int periodId;

    private String periodName;

    private String firstEntryName;

    private T firstEntryValue;

    private float firstEntryPercent;

    private String firstValueFormatted;

    private String secondEntryName;

    private T secondEntryValue;

    private float secondEntryPercent;

    private String secondValueFormatted;

    private T totalValue;

    private String totalValueFormatted;

}
