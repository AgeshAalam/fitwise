package com.fitwise.view.graph;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/*
 * Created by Vignesh G on 01/04/20
 */

@Getter
@Setter
public class ChartDataWithValue<T> {

    private T totalValue;

    private String formattedTotalValue;

    private List<ChartEntryWithValue<T>> chartEntryList;

}
