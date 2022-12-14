package com.fitwise.view.graph;

import lombok.Getter;
import lombok.Setter;

/*
 * Created by Vignesh G on 05/05/20
 */
@Getter
@Setter
public class GraphEntryWithValue<T> {

    private int periodId;

    private String periodName;

    private T value;

    private String formattedValue;

}
