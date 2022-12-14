package com.fitwise.view.graph;

import lombok.Getter;
import lombok.Setter;

/*
 * Created by Vignesh G on 01/04/20
 */

@Getter
@Setter
public class ChartEntryWithValue<T> {

    private String entryName;

    private T value;

    private String formattedValue;

    private float percentage;

}
