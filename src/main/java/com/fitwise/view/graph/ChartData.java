package com.fitwise.view.graph;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/*
 * Created by Vignesh G on 10/03/20
 */

@Getter
@Setter
public class ChartData {

    private int totalCount;

    private List<ChartEntry> chartEntryList;
}
