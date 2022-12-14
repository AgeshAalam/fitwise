package com.fitwise.view.graph;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/*
 * Created by Vignesh G on 05/05/20
 */
@Getter
@Setter
public class GraphDataWithValue<T> {

    private T overAllValue;

    private String overAllValueFormatted;

    private List<GraphEntryWithValue<T>> graphEntryList;

}
