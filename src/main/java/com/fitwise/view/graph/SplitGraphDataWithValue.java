package com.fitwise.view.graph;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/*
 * Created by Vignesh G on 02/04/20
 */

@Getter
@Setter
public class SplitGraphDataWithValue<T> {

    private T overAllValue;

    private String overAllValueFormatted;

    private List<SplitGraphEntryWithValue<T>> graphEntryList;

}
